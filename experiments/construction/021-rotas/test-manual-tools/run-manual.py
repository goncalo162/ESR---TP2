#!/usr/bin/env python3
"""Repeatable offline public-API manual against the frozen IMP-008 JAR."""
import argparse
from datetime import datetime, timezone
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import sys
import tempfile
from urllib.parse import unquote, urlsplit

HERE = Path(__file__).resolve().parent
BOLT = HERE.parent
ROOT = HERE.parents[3]


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def stamp():
    return datetime.now(timezone.utc).isoformat()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--operator', required=True)
    parser.add_argument('--kind', required=True, choices=('human', 'agent'))
    parser.add_argument('--java-home', default='/usr/lib/jvm/java-21-openjdk-amd64')
    args = parser.parse_args()
    if not args.operator.strip():
        parser.error('operator cannot be empty')
    manifest_path = BOLT / 'test-manual-manifest.json'
    manifest = json.loads(manifest_path.read_text())
    jar = ROOT / manifest['jar']['path']
    protected = dict(manifest['source_sha256'], **manifest['manual_tool_sha256'])
    protected[manifest['jar']['path']] = manifest['jar']['sha256']
    for name, expected in protected.items():
        path = ROOT / name
        if not path.is_file() or sha(path) != expected:
            raise RuntimeError('Frozen file changed: ' + name)
    java_home = Path(args.java_home).resolve()
    java, javac = java_home / 'bin/java', java_home / 'bin/javac'
    version = subprocess.run([str(java), '-version'], capture_output=True, text=True, timeout=10)
    if version.returncode != 0 or not re.search(r'version "21[.]', version.stderr + version.stdout):
        raise RuntimeError('This manual requires JDK 21: ' + version.stderr + version.stdout)
    parent = BOLT / 'test-manual-runs'
    parent.mkdir(exist_ok=True)
    run = Path(tempfile.mkdtemp(prefix=datetime.now(timezone.utc).strftime('%Y%m%dT%H%M%SZ-'), dir=parent))
    print(str(run), flush=True)
    classes = run / 'classes'
    classes.mkdir()
    compile_command = [str(javac), '-encoding', 'UTF-8', '--release', '21', '-cp', str(jar),
                       '-d', str(classes), str(HERE / 'ManualRouting.java')]
    java_command = [str(java), '-cp', str(jar) + os.pathsep + str(classes), 'ManualRouting',
                    '--interactive' if args.kind == 'human' else '--automatic']
    metadata = dict(started_at=stamp(), operator_declared=args.operator, kind_declared=args.kind,
                    human_identity_independently_verified=False, manifest_sha256=sha(manifest_path),
                    jar=manifest['jar'], java_home=str(java_home), java_version=version.stderr + version.stdout,
                    compile_command=compile_command, java_command=java_command,
                    source_sha256=manifest['source_sha256'], manual_tool_sha256=manifest['manual_tool_sha256'])
    (run / 'entry.json').write_text(json.dumps(metadata, ensure_ascii=False, indent=2) + '\n')
    child = None
    java_exit = None
    compile_exit = None
    problem = None
    try:
        with (run / 'compile.log').open('w') as log:
            compiled = subprocess.run(compile_command, stdout=log, stderr=subprocess.STDOUT, timeout=30)
        compile_exit = compiled.returncode
        if compile_exit != 0:
            raise RuntimeError('Compilation failed; see compile.log')
        child = subprocess.Popen(java_command, stdin=None if args.kind == 'human' else subprocess.DEVNULL,
                                 stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, bufsize=1)
        (run / 'java-pid.json').write_text(json.dumps({'pid': child.pid, 'started_at': stamp()}) + '\n')
        with (run / 'manual.log').open('w') as log:
            if args.kind == 'agent':
                output, _ = child.communicate(timeout=30)
                log.write(output)
                print(output, end='', flush=True)
            else:
                for line in child.stdout:
                    log.write(line)
                    log.flush()
                    print(line, end='', flush=True)
                child.wait()
        java_exit = child.returncode
    except BaseException as error:
        problem = type(error).__name__ + ': ' + str(error)
    finally:
        if child is not None:
            if child.poll() is None:
                child.terminate()
                try:
                    child.wait(timeout=3)
                except subprocess.TimeoutExpired:
                    child.kill()
                    child.wait(timeout=3)
            java_exit = child.returncode
            child.stdout.close()
    text = (run / 'manual.log').read_text() if (run / 'manual.log').exists() else ''
    checks = len(re.findall(r'^CHECK_PASS ', text, re.M))
    declared = re.findall(r'^CHECK_COUNT=(\d+)$', text, re.M)
    source = re.findall(r'^CODE_SOURCE=(.+)$', text, re.M)
    location_matches = bool(source) and Path(unquote(urlsplit(source[0]).path)).resolve() == jar.resolve()
    frozen_match = all((ROOT / p).is_file() and sha(ROOT / p) == h for p, h in protected.items())
    terminated = child is not None and child.poll() is not None
    passed = (problem is None and compile_exit == 0 and java_exit == 0 and terminated
              and checks == manifest['expected_checks'] and declared == [str(checks)]
              and 'MANUAL_PASS' in text.splitlines() and 'CHECK_FAIL' not in text
              and frozen_match and location_matches)
    report = dict(metadata, finished_at=stamp(), compile_exit=compile_exit, JAVA_EXIT=java_exit,
                  check_count=checks, expected_checks=manifest['expected_checks'], MANUAL_PASS='MANUAL_PASS' in text.splitlines(),
                  passed=passed, frozen_hashes_match=frozen_match, code_source_matches_jar=location_matches,
                  java_process_terminated=terminated, error=problem, scope='offline public API, no sockets or project CLI')
    report['evidence_sha256'] = {p.name: sha(p) for p in run.iterdir() if p.is_file()}
    (run / 'report.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n')
    print('REPORT=' + str(run / 'report.json'))
    print('JAVA_EXIT=' + str(java_exit))
    print('PASSED=' + str(passed).lower())
    return 0 if passed else 1


if __name__ == '__main__':
    sys.exit(main())
