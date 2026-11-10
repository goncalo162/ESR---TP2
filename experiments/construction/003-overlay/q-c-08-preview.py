"""Validate proposed Q-C-08 patch in an isolated snapshot; never edit workspace tools."""
from pathlib import Path
import datetime,hashlib,json,re,shutil,subprocess,sys,tempfile
root=Path.cwd();out=root/'experiments/construction/003-overlay'
meta=json.loads((out/'q-c-08-proposal-inputs.json').read_text())
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
for name,item in meta['files'].items():assert sha(root/name)==item['before_sha256'],f'Changed proposal input: {name}'
assert sha(root/'scripts/validate-planning.py')==meta['validator_sha256']
with tempfile.TemporaryDirectory(prefix='ott-q-c-08-',dir='/private/tmp') as tmp:
 snapshot=Path(tmp)/'repository'
 shutil.copytree(root,snapshot,symlinks=True,ignore=shutil.ignore_patterns('.git','.aws','target'))
 assert sha(snapshot/'scripts/validate-planning.py')==meta['validator_sha256']
 before=subprocess.run([sys.executable,'scripts/validate-planning.py'],cwd=snapshot,capture_output=True,text=True)
 (out/'q-c-08-preview-before.log').write_text(before.stdout+before.stderr)
 assert before.returncode==1 and 'Erros encontrados: 3' in before.stdout,before.stdout
 for suffix,args in [('check',['git','apply','--check',str(out/'q-c-08-proposed.patch')]),('apply',['git','apply',str(out/'q-c-08-proposed.patch')])]:
  r=subprocess.run(args,cwd=snapshot,capture_output=True,text=True)
  (out/('q-c-08-preview-'+suffix+'.log')).write_text(r.stdout+r.stderr)
  assert r.returncode==0,r.stderr
 for name,item in meta['files'].items():assert sha(snapshot/name)==item['proposed_sha256']
 assert sha(snapshot/'scripts/validate-planning.py')==meta['validator_sha256']
 after=subprocess.run([sys.executable,'scripts/validate-planning.py'],cwd=snapshot,capture_output=True,text=True)
 (out/'q-c-08-preview-after.log').write_text(after.stdout+after.stderr)
 assert after.returncode==0 and 'Erros encontrados: 0' in after.stdout,after.stdout
 def links(text):return int(re.search(r'Referências locais/âncoras verificadas: (\d+)',text).group(1))
 assert links(before.stdout)==links(after.stdout)
 result={'recorded_at':datetime.datetime.now(datetime.timezone.utc).strftime('%Y-%m-%dT%H:%M:%SZ'),'scope':'isolated snapshot only; actual workspace not patched','snapshot_before_exit':before.returncode,'snapshot_before_errors':3,'snapshot_after_exit':after.returncode,'snapshot_after_errors':0,'links_checked_before':links(before.stdout),'links_checked_after':links(after.stdout),'validator_sha256_unchanged':True,'workspace_tools_unchanged':True,'applied_to_workspace':False,'v_add_approved':False,'excluded_from_snapshot':['.git','.aws','target'],'snapshot_baseline_reproduces_same_three_errors':True}
 for name,item in meta['files'].items():assert sha(root/name)==item['before_sha256']
 assert sha(root/'scripts/validate-planning.py')==meta['validator_sha256']
 (out/'q-c-08-preview-result.json').write_text(json.dumps(result,indent=2,ensure_ascii=False)+'\n')
 print(json.dumps(result,ensure_ascii=False))
