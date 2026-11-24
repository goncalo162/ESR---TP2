from pathlib import Path
import subprocess,concurrent.futures,json,datetime,hashlib,zipfile,urllib.request,os
P=Path('experiments/construction/011-video/implement-01');scratch=Path(json.loads((P/'entry.json').read_text())['scratch']);
JOBS=[('A','https://www.di.uminho.pt/~flavio/ESR/ProgEx.zip','A.zip'),('B','https://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4.zip','B.zip'),('C','https://download.blender.org/durian/movies/Sintel.2010.720p.mkv.zip','C.zip'),('D','https://download.blender.org/demo/movies/ToS/tears_of_steel_720p.mov','D.mov')]
def sha(path):
 h=hashlib.sha256()
 with path.open('rb') as f:
  for block in iter(lambda:f.read(1048576),b''):h.update(block)
 return h.hexdigest()
def acquire(job):
 alias,url,name=job;dst=scratch/name;assert not dst.exists();start=datetime.datetime.now(datetime.timezone.utc).isoformat()
 cmd=['curl','--fail','--location','--connect-timeout','10','--max-time','1200','--max-filesize','1100000000','--silent','--show-error','--dump-header',str(P/(alias+'-download.headers.txt')),'--output',str(dst),'--write-out','%{http_code}\t%{url_effective}\t%{size_download}\n',url]
 r=subprocess.run(cmd,capture_output=True,text=True);(P/(alias+'-download.log')).write_text(r.stdout+r.stderr)
 result={'alias':alias,'url':url,'cache':str(dst),'started_at':start,'finished_at':datetime.datetime.now(datetime.timezone.utc).isoformat(),'command':cmd,'exit_code':r.returncode,'curl_result':r.stdout.strip(),'size':dst.stat().st_size if dst.exists() else None,'sha256':sha(dst) if dst.exists() else None}
 (P/(alias+'-download.json')).write_text(json.dumps(result,indent=2)+'\n');print(alias,'DOWNLOAD_EXIT',r.returncode,'SIZE',result['size'],flush=True);return result
def tool():
 metadata=scratch/'imageio-ffmpeg-pypi.json'
 with urllib.request.urlopen('https://pypi.org/pypi/imageio-ffmpeg/json',timeout=30) as r:metadata.write_bytes(r.read(2000000))
 data=json.loads(metadata.read_text());wheels=[x for x in data['urls'] if x['filename'].endswith('.whl') and 'macosx' in x['filename'] and ('intel' in x['filename'] or 'x86_64' in x['filename'])];assert len(wheels)==1,wheels
 w=wheels[0];wheel=scratch/w['filename']
 cmd=['curl','--fail','--location','--connect-timeout','10','--max-time','180','--silent','--show-error','--output',str(wheel),w['url']];r=subprocess.run(cmd,capture_output=True,text=True);assert r.returncode==0,r.stderr;assert sha(wheel)==w['digests']['sha256']
 tools=scratch/'tools';tools.mkdir()
 with zipfile.ZipFile(wheel) as z:
  names=[x for x in z.namelist() if '/binaries/ffmpeg-' in x and not x.endswith('/')];assert len(names)==1,names
  binary=tools/'ffmpeg';binary.write_bytes(z.read(names[0]));binary.chmod(0o755)
 version=subprocess.run([str(binary),'-version'],capture_output=True,text=True);assert version.returncode==0;(P/'ffmpeg-version.txt').write_text(version.stdout+version.stderr)
 result={'package':'imageio-ffmpeg','version':data['info']['version'],'metadata_url':'https://pypi.org/pypi/imageio-ffmpeg/json','wheel_url':w['url'],'wheel_sha256':sha(wheel),'binary':str(binary),'binary_sha256':sha(binary),'scope':'Standalone temporary offline tool only; not system/Python/runtime/POM installation','retrieved_at':datetime.datetime.now(datetime.timezone.utc).isoformat()};(P/'tool.json').write_text(json.dumps(result,indent=2)+'\n');print('OFFLINE_TOOL_READY',binary,flush=True);return result
with concurrent.futures.ThreadPoolExecutor(max_workers=5) as pool:
 tasks=[pool.submit(acquire,j) for j in JOBS]+[pool.submit(tool)]
 results=[t.result() for t in tasks]
(P/'acquisition.json').write_text(json.dumps(results,indent=2)+'\n')
assert all(r.get('exit_code',0)==0 for r in results),'Some downloads failed; preserve negatives before retry'
