"""Capture a fresh full Test run; source hashes and every new Surefire report."""
from pathlib import Path
from datetime import datetime, timezone
import hashlib
import json
import os
import shutil
import subprocess
import time
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[3]
out = Path(__file__).resolve().parent
phase = 'test-clean-verify'
if (out / (phase + '-command.json')).exists():
    raise SystemExit('This evidence exists; use a new phase instead of overwriting it')
source = json.loads((out / 'test-baseline.json').read_text())['source_sha256']
assert all(hashlib.sha256((root / f).read_bytes()).hexdigest() == h for f, h in source.items())
command = ['mvn', '-B', '-ntp', 'clean', 'verify']
environment = dict(os.environ, JAVA_HOME='/usr/lib/jvm/java-21-openjdk-amd64')
now = lambda: datetime.now(timezone.utc).strftime('%Y-%m-%dT%H:%M:%SZ')
started, tick = now(), time.monotonic()
result = subprocess.run(command, cwd=root / 'Projeto', env=environment, text=True, capture_output=True)
finished, elapsed = now(), round(time.monotonic() - tick, 3)
log = out / (phase + '.log')
log.write_text(result.stdout + result.stderr)
destination = out / (phase + '-surefire')
reports = root / 'Projeto/target/surefire-reports'
if reports.exists():
    shutil.copytree(reports, destination)
suites = []
for item in sorted(destination.glob('TEST-*.xml')):
    suite = ET.parse(item).getroot()
    suites.append(dict(name=suite.attrib['name'],
                       **{k: int(suite.attrib[k]) for k in ['tests', 'failures', 'errors', 'skipped']},
                       java_version=next(x.attrib['value'] for x in suite.findall('./properties/property')
                                         if x.attrib['name'] == 'java.version'),
                       xml=str(item.relative_to(root)), sha256=hashlib.sha256(item.read_bytes()).hexdigest()))
jar = root / 'Projeto/target/ott-streaming-1.0-SNAPSHOT.jar'
metadata = dict(command=command, cwd=str(root / 'Projeto'), JAVA_HOME=environment['JAVA_HOME'],
                started=started, finished=finished, elapsed_seconds=elapsed, exit_code=result.returncode,
                log=str(log.relative_to(root)), suites=suites,
                totals={k: sum(s[k] for s in suites) for k in ['tests', 'failures', 'errors', 'skipped']},
                source_sha256_before=source,
                source_sha256_after={f: hashlib.sha256((root / f).read_bytes()).hexdigest() for f in source},
                jar_sha256=hashlib.sha256(jar.read_bytes()).hexdigest() if jar.exists() else None,
                context='Full fresh clean verify with JDK21 and real local TCP; no closure or human approval implied.')
(out / (phase + '-command.json')).write_text(json.dumps(metadata, ensure_ascii=False, indent=2) + '\n')
print(json.dumps(metadata, ensure_ascii=False, indent=2))
raise SystemExit(result.returncode)
