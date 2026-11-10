"""Read-only source/oracle preservation audit; run from repository root."""
import hashlib, json, pathlib, subprocess, xml.etree.ElementTree as ET, shutil
root = pathlib.Path.cwd()
out = root / 'experiments/construction/003-overlay'
base = '26aaa4ae6775e9c78e1d7658472ccaca0b376427'
def git(*args):
    return subprocess.check_output(['git', *args])
def digest(data):
    return hashlib.sha256(data).hexdigest()
allowed = {
 'Projeto/src/main/java/pt/uminho/esr/ott/bootstrap/BootstrapServer.java',
 'Projeto/src/main/java/pt/uminho/esr/ott/overlay/Neighbor.java',
 'Projeto/src/main/java/pt/uminho/esr/ott/overlay/NeighborManager.java',
 'Projeto/src/test/java/pt/uminho/esr/ott/overlay/NeighborManagerTest.java',
}
files = git('ls-tree', '-r', '--name-only', base, '--', 'Projeto').decode().splitlines()
preserved, changed = [], []
for name in files:
    if name.startswith('Projeto/target/'):
        continue
    before = git('show', base + ':' + name)
    after = (root / name).read_bytes()
    record = {'path': name, 'base_sha256': digest(before), 'current_sha256': digest(after)}
    (preserved if before == after else changed).append(record)
assert {r['path'] for r in changed} == allowed, changed
q = (root / 'docs/construction-duvidas.md').read_text()
old_q = git('show', 'HEAD:docs/construction-duvidas.md').decode()
def section(text, header):
    content = text[text.index(header):]
    return content.split('\n## ', 1)[0]
for header in ['## Q-C-05 —', '## Q-C-06 —']:
    assert section(q, header) == section(old_q, header), header
name = 'memory-bank/bolts/003-overlay/ddd-02-technical-design.md'
assert (root / name).read_bytes() == git('show', 'HEAD:' + name), 'D01-D22/design changed'
xml_dir = out / 'implement-regression-xml'
xml_dir.mkdir(exist_ok=True)
suites = []
for p in sorted((root / 'Projeto/target/surefire-reports').glob('TEST-*.xml')):
    suite = ET.parse(p).getroot()
    suites.append({key: suite.attrib[key] for key in ['name', 'tests', 'failures', 'errors', 'skipped']})
    shutil.copyfile(p, xml_dir / p.name)
assert sum(int(s['tests']) for s in suites) == 903, suites
assert all(int(s[k]) == 0 for s in suites for k in ['failures', 'errors', 'skipped']), suites
result = {'base': base, 'entry_head': git('rev-parse', 'HEAD').decode().strip(),
          'changed_sources': changed, 'unchanged_project_files': preserved,
          'q_c_05_q_c_06_unchanged': True, 'approved_design_d01_d22_unchanged': True,
          'suites': suites, 'command': 'mvn -B -ntp test',
          'excluded_generated': 'Projeto/target/; XML copied separately. No clean verify or fresh JAR in Implement.'}
(out / 'implementation-preservation.json').write_text(json.dumps(result, indent=2, ensure_ascii=False) + '\n')
(out / 'implementation-source.diff').write_bytes(git('diff', base, '--', 'Projeto/src'))
(out / 'implementation-source.diff.numstat').write_bytes(git('diff', '--numstat', base, '--', 'Projeto/src'))
(out / 'bootstrap-authorized-restoration.diff').write_bytes(git('diff', 'HEAD', '--', 'Projeto/src/main/java/pt/uminho/esr/ott/bootstrap/BootstrapServer.java'))
print(json.dumps({'unchanged_project_files': len(preserved), 'changed_sources': len(changed), 'tests': sum(int(s['tests']) for s in suites), 'oracles_preserved': True}))
