from pathlib import Path
import subprocess,json,hashlib,tempfile,os,datetime,shutil,zipfile
P=Path('experiments/construction/011-video/lfs-implementation-01');root=Path.cwd();entry=json.loads((P/'entry.json').read_text());cfg=json.loads(Path('experiments/construction/011-video/test-manual-tools/approved-media.json').read_text())
def sha(f):
 h=hashlib.sha256()
 with Path(f).open('rb') as s:
  for block in iter(lambda:s.read(1048576),b''):h.update(block)
 return h.hexdigest()
assert sha('.git/index')==entry['git_index_sha256'];assert sha(Path.home()/'.gitconfig')==entry['git_global_config_sha256']
for f,v in entry['hooks_before'].items():assert sha(f)==v['sha256'] and Path(f).stat().st_mode==v['mode']
expected_attrs=''.join('/'+a['path']+' filter=lfs diff=lfs merge=lfs -text\n' for a in cfg['assets']);assert Path('.gitattributes').read_text()==expected_attrs
lfenv=subprocess.check_output(['git','lfs','env'],text=True);(P/'git-lfs-env.txt').write_text(lfenv);media_dir=Path(next(x.split('=',1)[1] for x in lfenv.splitlines() if x.startswith('LocalMediaDir=')));assert str(media_dir.resolve()).startswith(str((root/'.git').resolve())+'/')
assert shutil.disk_usage(root).free>3*459820128
results=[];env=os.environ.copy();env.update({'GIT_LFS_SKIP_SMUDGE':'0','GIT_LFS_SKIP_PUSH':'1','GIT_TERMINAL_PROMPT':'0','GIT_TRACE':'1'})
with tempfile.TemporaryDirectory(prefix='ott-lfs-materialization-') as temp:
 temp=Path(temp);empty=temp/'no-network-fallback';empty.mkdir()
 for a in cfg['assets']:
  path=a['path'];alias=a['alias'];assert sha(path)==a['sha256'];pointer=P/(alias+'.pointer')
  with Path(path).open('rb') as inp,pointer.open('wb') as output:
   r=subprocess.run(['git','lfs','clean',path],stdin=inp,stdout=output,stderr=subprocess.PIPE,env=env,timeout=120)
  (P/(alias+'-clean.log')).write_bytes(r.stderr);assert r.returncode==0,r.stderr.decode()
  expected=f"version https://git-lfs.github.com/spec/v1\noid sha256:{a['sha256']}\nsize {Path(path).stat().st_size}\n";assert pointer.read_text()==expected
  obj=media_dir/a['sha256'][:2]/a['sha256'][2:4]/a['sha256'];assert obj.is_file() and sha(obj)==a['sha256']
  materialized=temp/path;materialized.parent.mkdir(parents=True,exist_ok=True)
  command=['git','-c','lfs.url='+empty.as_uri(),'-c','lfs.remote.autodetect=false','-c','lfs.remote.searchall=false','-c','lfs.fetchinclude='+path,'-c','lfs.fetchexclude=','lfs','smudge',path]
  with pointer.open('rb') as inp,materialized.open('wb') as out:r=subprocess.run(command,stdin=inp,stdout=out,stderr=subprocess.PIPE,env=env,timeout=120)
  (P/(alias+'-smudge.log')).write_bytes(r.stderr);assert r.returncode==0,r.stderr.decode();assert sha(materialized)==a['sha256'] and materialized.stat().st_size==Path(path).stat().st_size
  assert b'HTTP:' not in r.stderr
  results.append({'alias':alias,'path':path,'real_bytes':Path(path).stat().st_size,'real_sha256':a['sha256'],'pointer_file':str(pointer),'pointer_bytes':pointer.stat().st_size,'pointer_blob_sha256':sha(pointer),'oid':'sha256:'+a['sha256'],'local_object':str(obj),'local_object_sha256':sha(obj),'materialized_sha256':sha(materialized),'materialized_bytes':materialized.stat().st_size,'clean_exit':0,'smudge_exit':0,'smudge_command':command,'remote_access':'Only an empty local file:// fallback allowed,autodetect/searchall false;cache present and hash-verified before smudge'})
  print(alias,'CLEAN_CACHE_SMUDGE_PASS',a['sha256'],flush=True)
 manifest=temp/'experiments/media/manifest.json';manifest.parent.mkdir(parents=True);shutil.copy2('experiments/media/manifest.json',manifest)
 verifier='experiments/construction/011-video/lfs-proposal-01/verify-media.py'
 working=subprocess.run(['python3','-B',verifier,'--root',str(temp)],capture_output=True,text=True);(P/'materialized-tree-check.json').write_text(working.stdout);assert working.returncode==0,working.stderr
 archive=temp/'media-only-validation.zip'
 with zipfile.ZipFile(archive,'w',zipfile.ZIP_STORED) as z:
  for a in cfg['assets']:z.write(temp/a['path'],a['path'])
  z.write(manifest,'experiments/media/manifest.json')
 check=subprocess.run(['python3','-B',verifier,'--zip',str(archive)],capture_output=True,text=True);(P/'materialized-zip-check.json').write_text(check.stdout);assert check.returncode==0,check.stderr
 archive_info={'sha256':sha(archive),'bytes':archive.stat().st_size,'scope':'Temporary data-only ZIP validating4 real media and manifest;not full Codigo.zip/not a delivery;removed with temporary directory','verification_exit':check.returncode}
assert sha('.git/index')==entry['git_index_sha256'];assert sha(Path.home()/'.gitconfig')==entry['git_global_config_sha256'];assert subprocess.check_output(['git','rev-parse','HEAD'],text=True).strip()==entry['HEAD'];assert not subprocess.check_output(['git','diff','--cached','--name-only'],text=True)
(P/'materialization.json').write_text(json.dumps({'recorded_at':datetime.datetime.now(datetime.timezone.utc).isoformat(),'results':results,'materialized_files':4,'media_bytes':459820128,'all_hashes_match':True,'temporary_outputs':'Removed;objects remain only in local Git LFS cache','temporary_ZIP':archive_info,'LFS_remote_network':False,'uploads_push_commits_staging':False,'git_index_unchanged':True,'global_git_config_unchanged':True,'history_HEAD_unchanged':entry['HEAD']},indent=2)+'\n');print('Four local objects materialized and real-data ZIP verified;index/global/HEAD unchanged. No remote LFS access.')
