"""Capture an authorized incremental Maven run; preserve only reports selected now."""
from pathlib import Path
from datetime import datetime, timezone
import hashlib
import json
import os
import shutil
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[3]
out = Path(__file__).resolve().parent
phase, selector = sys.argv[1:]
if not phase.replace('-', '').isalnum() or (out / (phase + '-command.json')).exists():
    raise SystemExit('Use a new alphanumeric phase name')
command = ['mvn', '-B', '-ntp', '-Dtest=' + selector, 'test']
classes = [part.split('#')[0] for part in selector.split(',')]
reports = root / 'Projeto/target/surefire-reports'
for name in classes:
    for item in list(reports.glob('TEST-*.' + name + '.xml')) + list(reports.glob('*.' + name + '.txt')):
        item.unlink()
environment = dict(os.environ, JAVA_HOME='/usr/lib/jvm/java-21-openjdk-amd64')
sources = [root / 'Projeto/src/main/java/pt/uminho/esr/ott/config' / name for name in
           ['RuntimeConfiguration.java', 'NodeConfiguration.java', 'ConfigurationLoader.java']]
sources.append(root / 'Projeto/src/test/java/pt/uminho/esr/ott/config/ConfigurationLoaderTest.java')
fingerprints = {str(f.relative_to(root)): hashlib.sha256(f.read_bytes()).hexdigest() for f in sources}
now = lambda: datetime.now(timezone.utc).strftime('%Y-%m-%dT%H:%M:%SZ')
started, tick = now(), time.monotonic()
result = subprocess.run(command, cwd=root / 'Projeto', env=environment, text=True, capture_output=True)
finished, elapsed = now(), round(time.monotonic() - tick, 3)
log = out / (phase + '.log'); log.write_text(result.stdout + result.stderr)
suites = []
report_dir = out / (phase + '-surefire'); report_dir.mkdir()
for name in classes:
    for item in reports.glob('TEST-*.' + name + '.xml'):
        dest = report_dir / item.name; shutil.copyfile(item, dest)
        xml = ET.parse(dest).getroot()
        suites.append(dict(name=xml.attrib['name'], **{k: int(xml.attrib[k]) for k in ['tests','failures','errors','skipped']},
                           xml=str(dest.relative_to(root)), sha256=hashlib.sha256(dest.read_bytes()).hexdigest()))
    for item in reports.glob('*.' + name + '.txt'):
        shutil.copyfile(item, report_dir / item.name)
totals = {k: sum(s[k] for s in suites) for k in ['tests','failures','errors','skipped']}
metadata = dict(command=command, cwd=str(root/'Projeto'), JAVA_HOME=environment['JAVA_HOME'],
                started=started, finished=finished, elapsed_seconds=elapsed, exit_code=result.returncode,
                log=str(log.relative_to(root)), suites=suites, totals=totals,
                source_sha256_before=fingerprints,
                source_sha256_after={str(f.relative_to(root)): hashlib.sha256(f.read_bytes()).hexdigest() for f in sources},
                scope='Incremental Implement run only; not final Test/clean verify/manual/review gate.')
(out / (phase + '-command.json')).write_text(json.dumps(metadata, ensure_ascii=False, indent=2) + '\n')
print(json.dumps(metadata, ensure_ascii=False, indent=2))
raise SystemExit(result.returncode)
