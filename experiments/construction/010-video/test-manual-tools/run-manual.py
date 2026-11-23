#!/usr/bin/env python3
"""Reproducible local codec manual; never infer a human run from an agent execution."""
import argparse
import datetime
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import sys
import tempfile
import uuid


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--operator', required=True)
    parser.add_argument('--kind', choices=['human', 'agent'], required=True)
    parser.add_argument('--java-home', required=True, type=Path)
    args = parser.parse_args()
    if args.kind == 'human' and not sys.stdin.isatty():
        parser.error('Human manual requires an interactive terminal; no piped confirmations')
    tools = Path(__file__).resolve().parent
    root = tools.parents[3]
    config_path = tools / 'approved-build.json'
    config = json.loads(config_path.read_text())
    jar = root / config['jar']
    if sha(jar) != config['jar_sha256']:
        raise SystemExit('JAR hash mismatch; stop before manual')
    for relative, expected in config['source_and_fixture_sha256'].items():
        if sha(root / relative) != expected:
            raise SystemExit('Approved source/fixture changed: ' + relative)
    source = tools / 'ManualProbe.java.txt'
    if sha(source) != config['probe_sha256']:
        raise SystemExit('Manual probe changed')
    if sha(Path(__file__)) != config['runner_sha256']:
        raise SystemExit('Manual runner changed')
    java = args.java_home / 'bin/java'
    javac = args.java_home / 'bin/javac'
    version = subprocess.run([str(java), '-version'], capture_output=True, text=True, check=True)
    if not re.search(r'version "21(?:\.|\")', version.stderr + version.stdout):
        raise SystemExit('JDK21 required')
    stamp = datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%dT%H%M%SZ')
    run = tools.parent / 'test-manual-runs' / (stamp + '-' + uuid.uuid4().hex[:8])
    run.mkdir(parents=True, exist_ok=False)
    report = {'operator': args.operator, 'kind': args.kind, 'started_at': stamp,
              'jar': str(jar), 'jar_sha256': sha(jar), 'config_sha256': sha(config_path),
              'probe_sha256': sha(source), 'runner_sha256': sha(Path(__file__)),
              'java_version': version.stderr + version.stdout, 'JAVA_EXIT': None,
              'checks_passed': 0, 'stages_confirmed': 0, 'cleanup_complete': False,
              'passed': False, 'manual_human_executed': args.kind == 'human',
              'approval': 'not inferred; user must review and confirm'}
    process = None
    output = []
    wrapper_exit = 1
    with (run / 'manual.log').open('w', encoding='utf-8') as log:
        def emit(line):
            print(line, flush=True)
            log.write(line + '\n')
            log.flush()
        try:
            emit('KIND=' + args.kind + ' OPERATOR=' + args.operator)
            emit('JAR_SHA256=' + report['jar_sha256'])
            with tempfile.TemporaryDirectory(prefix='imp016-manual-') as temp:
                work = Path(temp)
                probe = work / 'ManualProbe.java'
                probe.write_bytes(source.read_bytes())
                result = subprocess.run([str(javac), '--release', '21', '-cp', str(jar),
                                         '-d', str(work), str(probe)], capture_output=True,
                                        text=True, timeout=30)
                (run / 'compile.log').write_text(result.stdout + result.stderr)
                report['compile_exit'] = result.returncode
                if result.returncode != 0:
                    raise RuntimeError('Manual probe compilation failed; inspect compile.log')
                command = [str(java), '-cp', str(jar) + os.pathsep + str(work),
                           'ManualProbe', str(jar), str(root / 'Projeto/src/test/resources/protocol')]
                report['command'] = command
                process = subprocess.Popen(command, cwd=root, stdout=subprocess.PIPE,
                                           stderr=subprocess.STDOUT, text=True, encoding='utf-8')
                for line in process.stdout:
                    line = line.rstrip('\n')
                    output.append(line)
                    emit(line)
                report['JAVA_EXIT'] = process.wait(timeout=5)
                process.stdout.close()
                report['checks_passed'] = sum(line.startswith('CHECK_PASS ') for line in output)
                report['stages_confirmed'] = sum(line.startswith('OPERATOR_CONFIRM_STAGE=') for line in output)
                report['class_origin'] = next((line.split('=', 1)[1] for line in output
                                               if line.startswith('STREAM_PACKET_ORIGIN=')), None)
                report['passed'] = (report['JAVA_EXIT'] == 0 and report['checks_passed'] == 24
                                    and report['stages_confirmed'] == 8
                                    and 'PROBE_PASS CHECKS=24' in output
                                    and report['class_origin'] == str(jar.resolve())
                                    and sha(jar) == config['jar_sha256'])
        except (Exception, KeyboardInterrupt) as exc:
            report['passed'] = False
            report['error'] = type(exc).__name__ + ': ' + str(exc)
            emit('MANUAL_ERROR=' + report['error'])
        finally:
            if process is not None and process.poll() is None:
                process.terminate()
                try:
                    process.wait(timeout=5)
                except subprocess.TimeoutExpired:
                    process.kill()
                    process.wait(timeout=5)
                report['JAVA_EXIT'] = process.returncode
            if process is not None and process.stdout is not None:
                process.stdout.close()
            report['cleanup_complete'] = process is None or process.poll() is not None
            if report['cleanup_complete']:
                emit('CLEANUP_COMPLETE')
            report['passed'] = report['passed'] and report['cleanup_complete']
            if report['passed']:
                prefix = 'MANUAL_PASS' if args.kind == 'human' else 'AGENT_PROBE_PASS'
                emit(prefix + ' CHECKS=24')
            emit('JAVA_EXIT=' + str(report['JAVA_EXIT']))
            emit('PASSED=' + str(report['passed']).lower())
            wrapper_exit = 0 if report['passed'] else 1
            report['WRAPPER_EXIT'] = wrapper_exit
            emit('WRAPPER_EXIT=' + str(wrapper_exit))
            report['finished_at'] = datetime.datetime.now(datetime.timezone.utc).isoformat()
            (run / 'report.json').write_text(json.dumps(report, indent=2) + '\n')
            emit('REPORT=' + str(run / 'report.json'))
    return wrapper_exit


if __name__ == '__main__':
    sys.exit(main())
