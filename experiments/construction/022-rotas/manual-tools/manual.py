#!/usr/bin/env python3
"""Local manual runner; never marks the bolt/Test approved or authenticates the operator."""
import argparse
import datetime
import hashlib
import json
import os
from pathlib import Path
import select
import shutil
import signal
import socket
import subprocess
import sys
import tempfile
import time

ROOT = Path(__file__).resolve().parents[4]
BASE = ROOT / 'experiments/construction/022-rotas'
IDS = ('BOOT', 'S', 'R1', 'R2', 'C')
PORTS = dict(zip(IDS, range(20880, 20885)))
DATA = dict(zip(IDS[1:], range(20981, 20985)))
VIDEO = '00000000-0000-4000-8000-000000000001'
OTHER = '00000000-0000-4000-8000-000000000002'


def now():
    return datetime.datetime.now(datetime.timezone.utc).isoformat()


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def save(path, value):
    Path(path).write_text(json.dumps(value, indent=2, ensure_ascii=False) + '\n')


def check_frozen():
    frozen = json.loads((BASE / 'manual-tools/frozen.json').read_text())
    for name, expected in frozen['hashes'].items():
        if digest(ROOT / name) != expected:
            raise RuntimeError('Hash changed: ' + name)
    return frozen


def load_run(directory):
    run = Path(directory).resolve()
    meta = json.loads((run / 'run.json').read_text())
    check_frozen()
    for name, expected in meta['hashes'].items():
        if digest(run / name) != expected:
            raise RuntimeError('Run hash changed: ' + name)
    return run, meta


def ports_available():
    opened = []
    try:
        for kind, ports in ((socket.SOCK_STREAM, PORTS.values()), (socket.SOCK_DGRAM, DATA.values())):
            for port in ports:
                sock = socket.socket(socket.AF_INET, kind)
                opened.append(sock)
                if kind == socket.SOCK_STREAM:
                    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
                sock.bind(('127.0.0.1', port))
    finally:
        for sock in opened:
            sock.close()


def prepare(args):
    check_frozen()
    java = Path(args.java_home).expanduser() / 'bin/java' if args.java_home else Path(shutil.which('java') or '')
    version = subprocess.run([str(java), '-version'], capture_output=True, text=True, check=True)
    if 'version "21.' not in version.stderr + version.stdout:
        raise RuntimeError('JDK 21 required; select --java-home')
    ports_available()  # Point-in-time preflight, not a reservation for the later terminals.
    parent = BASE / 'manual-runs'
    parent.mkdir(exist_ok=True)
    run = Path(tempfile.mkdtemp(prefix=datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%dT%H%M%SZ-'), dir=parent))
    shutil.copy2(BASE / 'test-build-01/ott-streaming-1.0-SNAPSHOT.jar', run / 'ott.jar')
    graph = ['node.ids=S,R1,R2,C']
    for node in IDS[1:]:
        role = 'SERVER' if node == 'S' else 'CLIENT' if node == 'C' else 'RELAY'
        for key, value in {'host': '127.0.0.1', 'controlPort': PORTS[node], 'dataPort': DATA[node], 'role': role,
                           'neighbors': 'R1,R2' if node in ('S', 'C') else 'S,C'}.items():
            graph.append(f'node.{node}.{key}={value}')
    (run / 'topology.properties').write_text('\n'.join(graph) + '\n')
    streams = [f'stream.ids={VIDEO},{OTHER}']
    for index, stream in enumerate((VIDEO, OTHER), 1):
        for key, value in {'title': f'Diagnostico {index}', 'file': f'not-read-{index}.Mjpeg', 'fps': 25, 'loop': 'false'}.items():
            streams.append(f'stream.{stream}.{key}={value}')
    (run / 'streams.properties').write_text('\n'.join(streams) + '\n')
    for node in IDS:
        role = 'BOOTSTRAP' if node == 'BOOT' else 'SERVER' if node == 'S' else 'CLIENT' if node == 'C' else 'RELAY'
        props = [f'node.id={node}', f'node.role={role}', 'bind.host=127.0.0.1', 'advertised.host=127.0.0.1',
                 f'control.port={PORTS[node]}', 'control.shutdown.timeout.ms=1500']
        if node == 'BOOT':
            props.append('topology.file=topology.properties')
        else:
            props += [f'data.port={DATA[node]}', 'bootstrap.host=127.0.0.1', f'bootstrap.port={PORTS["BOOT"]}',
                      'control.connect.timeout.ms=300', 'overlay.retry.initial.ms=50', 'overlay.retry.max.ms=200',
                      'overlay.retry.jitter.percent=0']
        if node == 'S':
            props += ['streams.file=streams.properties', f'media.base={run}']
        (run / f'{node}.properties').write_text('\n'.join(props) + '\n')
    files = [run / 'ott.jar', *run.glob('*.properties')]
    save(run / 'run.json', {'prepared_at': now(), 'operator_declared': args.operator, 'kind': args.kind,
                           'identity_authenticated': False, 'java': str(java.resolve()), 'java_version': version.stderr + version.stdout,
                           'control_ports': PORTS, 'data_ports': DATA,
                           'hashes': {f.name: digest(f) for f in files}, 'manual_executed': False})
    print('RUN=' + str(run))
    print('Profiles prepared; manual not executed. Start BOOT first, then S/R1/R2/C.')


def node_console(args):
    run, meta = load_run(args.run)
    node = args.node
    if (run / f'{node}.pid.json').exists():
        raise RuntimeError('Node already launched in this run; prepare a new run for a fresh repetition')
    role = 'BOOTSTRAP' if node == 'BOOT' else 'SERVER' if node == 'S' else 'CLIENT' if node == 'C' else 'RELAY'
    command = [meta['java'], '-cp', str(run / 'ott.jar'), 'pt.uminho.esr.ott.app.OttApplication',
               '--role', role, '--config', str(run / f'{node}.properties')]
    child = subprocess.Popen(command, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    save(run / f'{node}.pid.json', {'pid': child.pid, 'wrapper_pid': os.getpid(), 'started_at': now(), 'command': command})
    inputs = [sys.stdin, child.stdout]
    command_buffer = bytearray()
    overlong = False
    with (run / f'{node}.log').open('wb') as log, (run / f'{node}.commands.jsonl').open('w') as commands:
        try:
            while True:
                ready, _, _ = select.select(inputs, [], [], .1)
                if child.stdout in ready:
                    data = os.read(child.stdout.fileno(), 65536)
                    if data:
                        log.write(data); log.flush(); sys.stdout.buffer.write(data); sys.stdout.buffer.flush()
                    else:
                        break
                if sys.stdin in ready:
                    data = os.read(sys.stdin.fileno(), 4096)
                    if not data:
                        child.stdin.close(); inputs.remove(sys.stdin)
                    else:
                        # Forward every byte; avoid TextIOWrapper read-ahead hiding pasted commands from select().
                        for byte in data:
                            if byte == 10:
                                commands.write(json.dumps({'at': now(), 'time_ns': time.time_ns(),
                                                           'line': command_buffer.decode('utf-8', errors='replace'),
                                                           'truncated': overlong}) + '\n'); commands.flush()
                                command_buffer.clear(); overlong = False
                            elif len(command_buffer) < 4096:
                                command_buffer.append(byte)
                            else:
                                overlong = True
                        child.stdin.write(data); child.stdin.flush()
        except (KeyboardInterrupt, BrokenPipeError):
            if child.poll() is None:
                child.terminate()
        finally:
            forced = False
            try:
                code = child.wait(timeout=5)
            except subprocess.TimeoutExpired:
                child.kill(); code = child.wait(timeout=3); forced = True
            if child.stdin and not child.stdin.closed:
                child.stdin.close()
            if child.stdout:
                child.stdout.close()
            shell_status = code if code >= 0 else 128 - code
            save(run / f'{node}.exit.json', {'ended_at': now(), 'time_ns': time.time_ns(), 'returncode': code,
                                           'signal': -code if code < 0 else None, 'shell_status': shell_status,
                                           'forced_by_wrapper': forced, 'reaped': True})
    load_run(run)
    print(f'PROCESS_RETURN={code} SHELL_STATUS={shell_status} REAPED=true')
    if code >= 0:
        print(f'JAVA_EXIT={code}')
    return shell_status


def send_signal(args):
    run, _ = load_run(args.run)
    if (run / f'{args.node}.exit.json').exists():
        raise RuntimeError('Node already exited; refusing stale PID')
    if args.signal == 'KILL' and args.node not in ('R1', 'R2'):
        raise RuntimeError('Intentional KILL is restricted to the relay chosen in this run')
    if args.signal != 'KILL' and args.node != 'S':
        raise RuntimeError('STOP/CONT restricted to the source of this run')
    state = json.loads((run / f'{args.node}.pid.json').read_text())
    os.kill(int(state['pid']), getattr(signal, 'SIG' + args.signal))
    with (run / 'signals.jsonl').open('a') as f:
        f.write(json.dumps({'at': now(), 'node': args.node, 'pid': state['pid'], 'signal': args.signal}) + '\n')
    print(args.signal + ' sent to owned run PID ' + str(state['pid']))


def watch(args):
    run, _ = load_run(args.run)
    opened, count = [], 0
    stop = False
    def finish(*_):
        nonlocal stop
        stop = True
    signal.signal(signal.SIGINT, finish)
    signal.signal(signal.SIGTERM, finish)
    began = time.time_ns()
    try:
        for port in DATA.values():
            sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM); opened.append(sock)
            sock.bind(('127.0.0.1', port))
        save(run / 'watch.ready.json', {'pid': os.getpid(), 'started_ns': began, 'ports': DATA})
        print('UDP_WATCH_READY', DATA, flush=True)
        with (run / 'udp-observed.jsonl').open('w') as log:
            while not stop:
                ready, _, _ = select.select(opened, [], [], .2)
                for sock in ready:
                    data, peer = sock.recvfrom(1201); count += 1
                    log.write(json.dumps({'at': now(), 'port': sock.getsockname()[1], 'peer': peer, 'bytes_observed_up_to_1201': len(data)}) + '\n'); log.flush()
    finally:
        for sock in opened:
            sock.close()
        save(run / 'watch.result.json', {'started_ns': began, 'ended_ns': time.time_ns(), 'datagrams_observed': count,
                                        'ports_closed': True, 'scope': 'local observation interval only'})
    print('UDP_OBSERVED=' + str(count), flush=True)
    return 0 if count == 0 else 1


def stop_watch(args):
    run, _ = load_run(args.run)
    if (run / 'watch.result.json').exists():
        raise RuntimeError('Watcher already ended')
    state = json.loads((run / 'watch.ready.json').read_text())
    os.kill(state['pid'], signal.SIGINT)


def verify(args):
    run, meta = load_run(args.run)
    checks = []
    def check(condition, label):
        checks.append({'check': label, 'passed': bool(condition)})
        print(('CHECK_PASS ' if condition else 'CHECK_FAIL ') + label)
    exits = {node: json.loads((run / f'{node}.exit.json').read_text()) for node in IDS}
    signals = [json.loads(line) for line in (run / 'signals.jsonl').read_text().splitlines()]
    killed = [entry['node'] for entry in signals if entry['signal'] == 'KILL']
    check(len(killed) == 1 and killed[0] in ('R1', 'R2'), 'one intentionally killed relay')
    for node, status in exits.items():
        check(status['reaped'] and not status['forced_by_wrapper'], node + ' reaped without forced wrapper cleanup')
        check(status['returncode'] == (-9 if node in killed else 0), node + ' expected Java exit or intentional signal9')
    logs = {node: (run / f'{node}.log').read_text() for node in IDS}
    client = logs['C']; source = logs['S']
    check(any(line.startswith('TRACE_RESULT stream=' + VIDEO) and 'visited=[S]' in line for line in source.splitlines()), 'source local trace')
    for relay in ('R1', 'R2'):
        check(any(line.startswith('TRACE_RESULT stream=' + VIDEO) and 'visited=[C, ' + relay + ', S]' in line for line in client.splitlines()), 'observed successful path via ' + relay)
    for text in ('CATALOG_STREAM', 'ROUTE_CANDIDATE', 'METRIC ', 'UNKNOWN_STREAM', 'NO_ROUTE'):
        check(text in client, 'CLI observed ' + text)
    check('COMMAND_ERROR trace requires' in client, 'invalid UUID diagnosed locally')
    for node, text in logs.items():
        check(not any(flag in text for flag in ('FATAL', 'OUTPUT_ERROR', 'COMMAND_OVERFLOW', 'CLEANUP_ERROR')), node + ' no fatal logger/cleanup error')
        check(not any('type=' + kind + ',' in text for kind in ('ACTIVATE', 'ACTIVATE_ACK', 'DEACTIVATE', 'SUBSCRIPTION_RENEW')), node + ' no subscription messages observed')
    observed = json.loads((run / 'watch.result.json').read_text())
    commands = [json.loads(line) for node in IDS for line in (run / f'{node}.commands.jsonl').read_text().splitlines()]
    trace_times = [row['time_ns'] for row in commands if row['line'].startswith('trace ')]
    check(bool(trace_times) and observed['started_ns'] <= min(trace_times) and observed['ended_ns'] >= max(row['time_ns'] for row in exits.values()), 'UDP watcher covers traces through process termination')
    check(observed['datagrams_observed'] == 0 and observed['ports_closed'], 'zero datagrams in observed interval; watcher ports closed')
    ports_available(); check(True, 'all five TCP and four UDP ports available after cleanup')
    load_run(run); check(True, 'JAR profiles source and helper hashes unchanged')
    passed = all(c['passed'] for c in checks)
    save(run / 'report.json', {'recorded_at': now(), 'operator_declared': meta['operator_declared'], 'kind': meta['kind'],
                              'identity_authenticated': False, 'passed': passed, 'checks': checks, 'exits': exits,
                              'manual_human_approval': 'requires user confirmation; never inferred by this helper',
                              'limitations': 'No CORE/GUI/performance proof. UDP observation has the recorded interval. No FIN/RST classification.'})
    print('MANUAL_PASS' if passed else 'MANUAL_FAIL')
    return 0 if passed else 1


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    sub = parser.add_subparsers(dest='action', required=True)
    prep = sub.add_parser('prepare'); prep.add_argument('--operator', required=True); prep.add_argument('--kind', choices=['human', 'agent'], required=True); prep.add_argument('--java-home')
    prep.set_defaults(function=prepare)
    for action, function in [('node', node_console), ('signal', send_signal), ('watch', watch), ('stop-watch', stop_watch), ('verify', verify)]:
        command = sub.add_parser(action); command.add_argument('--run', required=True); command.set_defaults(function=function)
        if action in ('node', 'signal'):
            command.add_argument('--node', choices=IDS, required=True)
        if action == 'signal':
            command.add_argument('--signal', choices=['KILL', 'STOP', 'CONT'], required=True)
    args = parser.parse_args()
    return args.function(args) or 0


if __name__ == '__main__':
    try:
        sys.exit(main())
    except Exception as error:
        print('MANUAL_ERROR ' + str(error), file=sys.stderr)
        sys.exit(1)
