#!/usr/bin/env python3
"""Personal terminal logger for the unchanged, verified OTT JAR. No new product CLI."""
import argparse
import datetime
import hashlib
import json
import os
from pathlib import Path
import socket
import subprocess
import sys

HERE = Path(__file__).resolve().parent
MAIN = 'pt.uminho.esr.ott.app.OttApplication'

def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()

def checked_manifest():
    manifest = json.loads((HERE / 'manual-manifest.json').read_text())
    if digest(manifest['jar']) != manifest['jar_sha256']:
        raise RuntimeError('Verified JAR changed; do not run this manual')
    for path, sha in manifest['files'].items():
        if digest(path) != sha:
            raise RuntimeError('Profile/topology changed: ' + path)
    return manifest

def command(manifest, node):
    if node not in ('BOOT', 'R1', 'R2'):
        raise ValueError('Unknown manual profile')
    return ['java', '-cp', manifest['jar'], MAIN, '--role',
            'BOOTSTRAP' if node == 'BOOT' else 'RELAY', '--config',
            str(HERE / 'manual-profiles' / (node + '.properties'))]

def ports(manifest):
    results = []
    for kind, values in ((socket.SOCK_STREAM, manifest['tcp_ports']),
                         (socket.SOCK_DGRAM, manifest['udp_configured_ports'])):
        for port in values:
            row = {'transport': 'TCP' if kind == socket.SOCK_STREAM else 'UDP', 'port': port}
            try:
                with socket.socket(socket.AF_INET, kind) as probe:
                    if kind == socket.SOCK_STREAM:
                        probe.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
                    probe.bind((manifest['host'], port))
                row['available'] = True
            except OSError as error:
                row.update(available=False, error=str(error))
            results.append(row)
    return {'checked_at': datetime.datetime.now(datetime.timezone.utc).isoformat(),
            'temporary_bind_only_no_listen_no_reservation': True, 'ports': results}

def run_node(manifest, node, name, directory, operator):
    if not operator.strip() or not name or any(c not in 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_' for c in name):
        raise ValueError('Specify operator and a simple, unique execution name')
    directory.mkdir(parents=True, exist_ok=True)
    files = [directory / (name + suffix) for suffix in ('.log', '.pid.json', '.exit.json')]
    if any(path.exists() for path in files):
        raise RuntimeError('Execution already exists; choose a new name/directory')
    argv = command(manifest, node)
    with files[0].open('xb') as log:
        metadata = {'started_at': datetime.datetime.now(datetime.timezone.utc).isoformat(),
                    'operator_declared': operator, 'node': node, 'argv': argv,
                    'jar_sha256': manifest['jar_sha256']}
        log.write(('RUN ' + json.dumps(metadata) + '\n').encode()); log.flush()
        child = subprocess.Popen(argv, stdin=None, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, bufsize=0)
        files[1].write_text(json.dumps(dict(metadata, java_pid=child.pid), indent=2) + '\n')
        logger_error = None
        try:
            while True:
                chunk = child.stdout.read(4096)
                if not chunk:
                    break
                log.write(chunk); log.flush()
                sys.stdout.buffer.write(chunk); sys.stdout.buffer.flush()
            rc = child.wait()
        except BaseException as error:
            logger_error = repr(error)
            if child.poll() is None:
                child.terminate()
                try:
                    child.wait(timeout=4)
                except subprocess.TimeoutExpired:
                    child.kill(); child.wait(timeout=2)
            rc = child.returncode
        finally:
            child.stdout.close()
        result = {'finished_at': datetime.datetime.now(datetime.timezone.utc).isoformat(),
                  'returncode': rc, 'signal': -rc if rc < 0 else None,
                  'JAVA_EXIT': rc if rc >= 0 else None,
                  'shell_status': rc if rc >= 0 else 128 - rc,
                  'logger_error': logger_error, 'process_terminated': child.poll() is not None}
        files[2].write_text(json.dumps(result, indent=2) + '\n')
        log.write(('PROCESS_RESULT ' + json.dumps(result) + '\n').encode()); log.flush()
        print('PROCESS_RESULT ' + json.dumps(result), flush=True)
    return result['shell_status'] if logger_error is None else 3

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    sub = parser.add_subparsers(dest='action', required=True)
    sub.add_parser('check'); sub.add_parser('ports')
    run = sub.add_parser('run')
    run.add_argument('node', choices=['BOOT', 'R1', 'R2']); run.add_argument('--name', required=True)
    run.add_argument('--dir', type=Path, required=True); run.add_argument('--operator', required=True)
    args = parser.parse_args(); manifest = checked_manifest()
    if args.action == 'check':
        print('MANUAL_BUILD_AND_PROFILES_HASH_OK ' + manifest['jar_sha256']); return 0
    if args.action == 'ports':
        result = ports(manifest); print(json.dumps(result, indent=2))
        return 0 if all(row['available'] for row in result['ports']) else 1
    return run_node(manifest, args.node, args.name, args.dir.resolve(), args.operator)

if __name__ == '__main__':
    sys.exit(main())
