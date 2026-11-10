"""Read-only final Test audit. Run from the repository root; updates only evidence files."""
from pathlib import Path
import hashlib,json,subprocess,zipfile,xml.etree.ElementTree as ET,re
p=Path('experiments/construction/003-overlay')
entry=json.loads((p/'test-entry.json').read_text())
build=json.loads((p/'test-build-results.json').read_text())
manual=json.loads((p/'test-manual-result.json').read_text())
def sha(path):return hashlib.sha256(Path(path).read_bytes()).hexdigest()
changed=[name for name,h in entry['frozen_files'].items() if not Path(name).is_file() or sha(name)!=h]
assert not changed,changed
assert sha(build['jar'])==build['sha256']==manual['jar_sha256']
assert sha('Projeto/target/ott-streaming-1.0-SNAPSHOT.jar')==build['sha256']
assert sha(p/'ManualNeighborSmoke.java')==manual['harness_sha256']
assert sha('scripts/validate-planning.py')==hashlib.sha256(subprocess.check_output(['git','show','HEAD:scripts/validate-planning.py'])).hexdigest()
assert subprocess.check_output(['git','rev-parse','HEAD'],text=True).strip()==entry['head']
assert subprocess.check_output(['git','branch','--show-current'],text=True).strip()==entry['branch']
with zipfile.ZipFile(build['jar']) as z:
 names=z.namelist();(p/'test-jar-list.txt').write_text('\n'.join(names)+'\n')
 assert 'pt/uminho/esr/ott/overlay/NeighborManager.class' in names
 assert 'pt/uminho/esr/ott/bootstrap/BootstrapServer$EventLoop.class' in names
 assert 'pt/uminho/esr/ott/bootstrap/BootstrapServer$RealDeadlines.class' in names
 assert not any('ManualNeighborSmoke' in name or name.startswith('org/junit/') or name.endswith('Test.class') for name in names)
log=Path(manual['log']).read_text()
assert len(re.findall(r' CHECK_PASS ',log))==manual['checks_passed']==49
assert ' CHECK_FAIL ' not in log and 'MANUAL_PASS CHECKS=49' in log and 'CLEANUP_COMPLETE' in log
suites=[]
for f in (Path(build['jar']).parent/'surefire-reports').glob('TEST-*.xml'):
 t=ET.parse(f).getroot();suites.append({key:int(t.attrib[key]) for key in ['tests','failures','errors','skipped']})
totals={key:sum(s[key] for s in suites) for key in ['tests','failures','errors','skipped']}
assert totals=={'tests':903,'failures':0,'errors':0,'skipped':0},totals
vadd=(p/'test-planning-validation-final.log').read_text()
assert 'Erros encontrados: 3' in vadd and vadd.count('unclosed code fence')==2 and vadd.count('missing target bolts/001-auth-service/adr-001-jwt-auth.md')==1
base='26aaa4ae6775e9c78e1d7658472ccaca0b376427'
for output,args in [('test-source.diff.numstat',['diff','--numstat',base,'--','Projeto/src']),('test-cumulative.diff',['diff',base,'--','Projeto/src','docs','memory-bank']),('test-cumulative.diff.numstat',['diff','--numstat',base,'--','Projeto/src','docs','memory-bank'])]:
 (p/output).write_bytes(subprocess.check_output(['git',*args]))
result={'frozen_files_unchanged':len(entry['frozen_files']),'project_files_unchanged':155,'tool_files_unchanged':67,'validator_matches_head':True,'project_source_delta_during_test':{'added_lines':0,'removed_lines':0},'clean_verify_exit_code':0,'junit':totals,'manual_exit_code':manual['process_exit_code'],'manual_checks_passed':49,'manual_checked_jar_sha256':build['sha256'],'v_add_exit_code':1,'v_add_approved':False,'q_c_08':'open; no tool edits or reduced validator scope','test_approved':False,'bolt_closed':False,'head_unchanged':True,'branch_unchanged':True}
(p/'test-final-audit.json').write_text(json.dumps(result,indent=2,ensure_ascii=False)+'\n')
print(json.dumps(result,ensure_ascii=False))
