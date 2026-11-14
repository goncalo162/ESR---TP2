from pathlib import Path
import subprocess,os,datetime,hashlib,json,shutil,sys,xml.etree.ElementTree as ET
repo=Path(__file__).resolve().parents[4]; out=Path(__file__).resolve().parent/sys.argv[1];out.mkdir()
args=['mvn','-B','-ntp','-Dtest='+sys.argv[2],'test'];env=os.environ.copy();env['JAVA_HOME']='/usr/lib/jvm/java-21-openjdk-amd64';env['PATH']=env['JAVA_HOME']+'/bin:'+env['PATH']
started=datetime.datetime.now(datetime.timezone.utc);start=started.isoformat();sources={str(p.relative_to(repo)):hashlib.sha256(p.read_bytes()).hexdigest() for p in (repo/'Projeto').rglob('*') if p.is_file() and 'target' not in p.parts}
with (out/'maven.log').open('w') as log: result=subprocess.run(args,cwd=repo/'Projeto',env=env,stdout=log,stderr=subprocess.STDOUT)
reports=repo/'Projeto/target/surefire-reports';counts=dict(tests=0,failures=0,errors=0,skipped=0);xmls=[]
if reports.exists():
 for p in reports.glob('TEST-*.xml'):
  # Include only suites requested in this run; never count a stale unrelated XML.
  names=sys.argv[2].split(',')
  if p.stat().st_mtime+0.01<started.timestamp():continue
  if not any(p.stem.endswith('.'+n) or n=='*' for n in names):continue
  shutil.copyfile(p,out/p.name);xmls.append(p.name);d=ET.parse(p).getroot()
  for k in counts:counts[k]+=int(d.attrib.get(k,0))
data={'started_at':start,'finished_at':datetime.datetime.now(datetime.timezone.utc).isoformat(),'command':args,'JAVA_HOME':env['JAVA_HOME'],'exit':result.returncode,'counts':counts,'xmls':xmls,'source_sha256':sources}
(out/'result.json').write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n');print(json.dumps({k:data[k] for k in ['command','exit','counts','xmls']}))
sys.exit(result.returncode)
