from pathlib import Path
import datetime
import hashlib
import json
import re
import shutil
import sys
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parent
project = root.parents[3] / 'Projeto'
label, command = sys.argv[1:3]
log = root / (label + '.log')
text = log.read_text()
assert 'BUILD SUCCESS' in text or 'BUILD FAILURE' in text, 'Run not complete'
names = sorted(set(re.findall(r'Running ([\w.]+)', text)))
out = root / label
out.mkdir(exist_ok=False)
suites = []
for name in names:
    source = project / 'target/surefire-reports' / ('TEST-' + name + '.xml')
    tree = ET.parse(source).getroot()
    shutil.copy2(source, out / source.name)
    suites.append({'suite': name, **{k: int(tree.attrib[k]) for k in ('tests', 'failures', 'errors', 'skipped')}})
counts = {k: sum(s[k] for s in suites) for k in ('tests', 'failures', 'errors', 'skipped')}
summary = tuple(map(int, re.findall(r'Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)', text)[-1]))
assert tuple(counts.values()) == summary, (counts, summary)
authorized = [
    'src/main/java/pt/uminho/esr/ott/routing/Route.java',
    'src/main/java/pt/uminho/esr/ott/routing/Subscription.java',
    'src/main/java/pt/uminho/esr/ott/routing/RoutingService.java',
    'src/test/java/pt/uminho/esr/ott/routing/ActivationTest.java',
    'src/test/java/pt/uminho/esr/ott/overlay/OverlayNodeMonitoringTest.java',
    'src/main/java/pt/uminho/esr/ott/overlay/NeighborManager.java',
    'src/test/java/pt/uminho/esr/ott/overlay/NeighborManagerTest.java',
]
record = {
    'recorded_at': datetime.datetime.now(datetime.timezone.utc).isoformat(),
    'kind': 'authorized Q-C-37 incremental Implement verification; no Test-stage acceptance',
    'command': command, 'log': '../' + log.name, **counts, 'suites': suites,
    'source_hashes': {'Projeto/' + p: hashlib.sha256((project / p).read_bytes()).hexdigest() for p in authorized},
    'java_version': tree.find("./properties/property[@name='java.version']").attrib['value'],
    'formal_V014': 'not consolidated or accepted',
}
(out / 'result.json').write_text(json.dumps(record, indent=2) + '\n')
print(json.dumps({'run': label, **counts, 'suites': len(suites)}))
