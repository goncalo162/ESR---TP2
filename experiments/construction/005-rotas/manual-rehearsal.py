#!/usr/bin/env python3
"""Agent technical rehearsal of the CLI sequence; NEVER evidence of a human manual."""
import argparse
import datetime
import json
import os
from pathlib import Path
import re
import runpy
import signal
import subprocess
import time

HERE = Path(__file__).resolve().parent
TOOLS = runpy.run_path(str(HERE / 'manual-run.py'))

def main():
    parser = argparse.ArgumentParser(description=__doc__); parser.add_argument('--dir', type=Path, required=True)
    args = parser.parse_args(); directory = args.dir.resolve(); directory.mkdir(parents=True, exist_ok=False)
    manifest = TOOLS['checked_manifest'](); inventory = []; checks = []; paused = None
    record = directory / 'rehearsal.log'
    def emit(line):
        print(line, flush=True)
        with record.open('a') as stream:
            stream.write(line + '\n')
    def check(label, condition):
        if not condition:
            raise AssertionError(label)
        checks.append(label); emit('CHECK_PASS ' + label)
    def text(child):
        size = child['log'].stat().st_size
        if size > 2_000_000:
            raise RuntimeError('Rehearsal log cap exceeded')
        return child['log'].read_text(errors='replace')
    def wait(predicate, label, seconds=10):
        deadline = time.monotonic() + seconds
        while time.monotonic() < deadline:
            if predicate():
                return
            time.sleep(.02)
        raise AssertionError('Deadline: ' + label)
    def launch(node, name):
        log = directory / (name + '.log'); handle = log.open('xb'); argv = TOOLS['command'](manifest, node)
        proc = subprocess.Popen(argv, stdin=subprocess.PIPE, stdout=handle, stderr=subprocess.STDOUT)
        child = dict(node=node, name=name, process=proc, log=log, handle=handle, argv=argv); inventory.append(child)
        wait(lambda: 'READY node=' + node in text(child), 'READY ' + name)
        check('ready_' + name, proc.poll() is None); return child
    def send(child, line):
        child['process'].stdin.write((line + '\n').encode()); child['process'].stdin.flush()
    def ups(child):
        return re.findall(r'NeighborUp\[session=Session\[nodeId=R2, instanceId=([^,]+), connectionId=([^,]+),', text(child))
    def downs(child):
        return text(child).count('NeighborDown[session=Session[nodeId=R2,')
    def finish(child):
        send(child, 'quit'); rc = child['process'].wait(timeout=5)
        (directory / (child['name'] + '.exit.json')).write_text(json.dumps({'JAVA_EXIT': rc, 'process_terminated': True}) + '\n')
        check('java_exit_zero_' + child['name'], rc == 0)
        check('shutdown_record_' + child['name'], 'SHUTDOWN node=' + child['node'] + ' phase=closing-output' in text(child))
    result = {'mode': 'agent_technical_rehearsal', 'human_manual_performed': False,
              'jar_sha256': manifest['jar_sha256'], 'started_at': datetime.datetime.now(datetime.timezone.utc).isoformat()}
    try:
        report = TOOLS['ports'](manifest); (directory / 'ports-before.json').write_text(json.dumps(report, indent=2) + '\n')
        check('ports_available_before', all(row['available'] for row in report['ports']))
        boot = launch('BOOT', 'BOOT-01'); a = launch('R1', 'R1-01'); b = launch('R2', 'R2-01')
        wait(lambda: len(ups(a)) >= 1 and 'type=PROBE_PONG' in text(a) and 'type=PROBE_PING' in text(a)
             and 'type=PROBE_PONG' in text(b), 'bidirectional probe traffic')
        check('probe_ping_pong_v2_both_directions', all('version=2, type=PROBE_' in text(c) for c in (a, b)))
        first = ups(a)[-1]; old_down = downs(a)
        os.kill(b['process'].pid, signal.SIGSTOP); paused = b
        emit('ACTION SIGSTOP node=R2 owned_pid=' + str(b['process'].pid))
        time.sleep(1.2)
        check('one_second_without_eof_does_not_kill_node', a['process'].poll() is None and downs(a) == old_down)
        wait(lambda: downs(a) > old_down, 'consecutive probe timeouts')
        check('timeout_detected_while_peer_process_alive', b['process'].poll() is None and a['process'].poll() is None)
        finish(boot)  # Existing instances can reconnect without a live bootstrap.
        os.kill(b['process'].pid, signal.SIGCONT); paused = None; emit('ACTION SIGCONT node=R2')
        wait(lambda: len(ups(a)) >= 2, 'same-instance reconnection')
        second = ups(a)[-1]
        check('same_instance_new_connection', first[0] == second[0] and first[1] != second[1])
        before = len(ups(a)); old_down = downs(a)
        b['process'].kill(); rc = b['process'].wait(timeout=3)
        (directory / 'R2-01.exit.json').write_text(json.dumps({'returncode': rc, 'intentional_signal': 9, 'process_terminated': True}) + '\n')
        check('intentional_peer_death_recorded', rc == -signal.SIGKILL)
        wait(lambda: downs(a) > old_down, 'EOF after real process death')
        check('survivor_alive_after_eof_without_bootstrap', a['process'].poll() is None)
        # A new process must perform its initial REGISTER; do not claim otherwise.
        new_boot = launch('BOOT', 'BOOT-02')
        replacement = launch('R2', 'R2-02'); wait(lambda: len(ups(a)) > before, 'new-instance initial discovery and connection')
        finish(new_boot)
        third = ups(a)[-1]
        check('new_instance_new_connection', third[0] != second[0] and third[1] != second[1])
        before_pong = text(a).count('type=PROBE_PONG')
        wait(lambda: text(a).count('type=PROBE_PONG') > before_pong, 'new probes after restart')
        check('no_fatal_on_surviving_or_restarted_nodes', all('FATAL ' not in text(c) for c in (a, replacement)))
        send(a, 'neighbors'); send(replacement, 'neighbors')
        wait(lambda: 'NEIGHBOR node=R2 state=ACTIVE' in text(a) and 'NEIGHBOR node=R1 state=ACTIVE' in text(replacement), 'neighbors after restart')
        check('neighbors_command_still_operational', True)
        finish(replacement); finish(a)
        check('all_owned_processes_exited', all(c['process'].poll() is not None for c in inventory))
        result['passed'] = True; emit('TECHNICAL_REHEARSAL_PASS CHECKS=' + str(len(checks)))
    except BaseException as error:
        result.update(passed=False, error=repr(error)); emit('TECHNICAL_REHEARSAL_FAIL ' + repr(error))
    finally:
        if paused is not None and paused['process'].poll() is None:
            os.kill(paused['process'].pid, signal.SIGCONT)
        for child in inventory:
            proc = child['process']
            if proc.poll() is None:
                proc.terminate()
                try:
                    proc.wait(timeout=4)
                except subprocess.TimeoutExpired:
                    proc.kill(); proc.wait(timeout=2)
            child['handle'].close(); proc.stdin.close()
        result.update(finished_at=datetime.datetime.now(datetime.timezone.utc).isoformat(), checks=checks,
                      processes=[{'name': c['name'], 'pid': c['process'].pid, 'returncode': c['process'].returncode, 'argv': c['argv']} for c in inventory],
                      no_owned_processes_pending=all(c['process'].poll() is not None for c in inventory))
        (directory / 'result.json').write_text(json.dumps(result, indent=2) + '\n')
        emit('CLEANUP_COMPLETE' if result['no_owned_processes_pending'] else 'CLEANUP_FAILED')
    return 0 if result.get('passed') and result['no_owned_processes_pending'] else 1

if __name__ == '__main__':
    raise SystemExit(main())
