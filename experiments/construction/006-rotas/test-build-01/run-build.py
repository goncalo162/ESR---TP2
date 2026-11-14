from pathlib import Path
import datetime,subprocess,hashlib,json,os,shutil,sys,xml.etree.ElementTree as ET,zipfile,struct
out=Path(__file__).resolve().parent;repo=out.parents[3];entry=json.loads((out/'entry.json').read_text());sha=lambda p:hashlib.sha256(Path(p).read_bytes()).hexdigest()
assert all(sha(repo/p)==h for p,h in entry['project_non_target_sha256'].items())
env=os.environ.copy();env['JAVA_HOME']='/usr/lib/jvm/java-21-openjdk-amd64';env['PATH']=env['JAVA_HOME']+'/bin:'+env['PATH']
versions={}
for name,args in [('java',['java','-version']),('javac',['javac','-version']),('maven',['mvn','-version'])]:
 p=subprocess.run(args,cwd=repo/'Projeto',env=env,capture_output=True,text=True);versions[name]={'exit':p.returncode,'output':p.stdout+p.stderr}
(out/'environment.json').write_text(json.dumps({'JAVA_HOME':env['JAVA_HOME'],'versions':versions},indent=2)+'\n')
started=datetime.datetime.now(datetime.timezone.utc);command=['mvn','-B','-ntp','clean','verify']
with (out/'maven.log').open('w') as log:result=subprocess.run(command,cwd=repo/'Projeto',env=env,stdout=log,stderr=subprocess.STDOUT)
counts=dict(tests=0,failures=0,errors=0,skipped=0);suites={};sure=out/'surefire';sure.mkdir()
for p in (repo/'Projeto/target/surefire-reports').glob('*'):
 if p.is_file():shutil.copyfile(p,sure/p.name)
 if p.name.startswith('TEST-') and p.suffix=='.xml':
  root=ET.parse(p).getroot();suites[root.get('name')]={k:int(root.get(k,0)) for k in counts}
  for k in counts:counts[k]+=int(root.get(k,0))
jar={};audit={};artifact=repo/'Projeto/target/ott-streaming-1.0-SNAPSHOT.jar'
if result.returncode==0 and artifact.is_file():
 frozen=out/artifact.name;shutil.copyfile(artifact,frozen);jar={'path':str(frozen.relative_to(repo)),'bytes':frozen.stat().st_size,'sha256':sha(frozen)}
 with zipfile.ZipFile(frozen) as z:
  classes=[p for p in z.namelist() if p.endswith('.class')];majors={p:struct.unpack('>H',z.read(p)[6:8])[0] for p in classes}
  audit={'class_count':len(classes),'all_class_major_65':all(n==65 for n in majors.values()),'no_test_classes':not any('Test' in Path(p).name for p in classes),'key_classes':{p:majors[p] for p in majors if p.endswith(('RoutingService.class','OverlayNode.class','StreamCatalog.class','StreamInfo.class'))},'manifest':z.read('META-INF/MANIFEST.MF').decode()}
 (out/'jar-audit.json').write_text(json.dumps(audit,indent=2)+'\n')
unchanged=all((repo/p).is_file() and sha(repo/p)==h for p,h in entry['project_non_target_sha256'].items())
r={'started_at':started.isoformat(),'finished_at':datetime.datetime.now(datetime.timezone.utc).isoformat(),'command':command,'cwd':str(repo/'Projeto'),'JAVA_HOME':env['JAVA_HOME'],'exit':result.returncode,'counts':counts,'XML_suites':len(suites),'suites':suites,'jar':jar,'jar_audit':audit,'all_non_target_sources_unchanged':unchanged,'source_sha256':entry['project_non_target_sha256'],'evidence_sha256':{str(p.relative_to(out)):sha(p) for p in out.rglob('*') if p.is_file() and p.name!='result.json'}}
(out/'result.json').write_text(json.dumps(r,ensure_ascii=False,indent=2)+'\n');print(json.dumps({k:r[k] for k in ['exit','counts','XML_suites','jar','all_non_target_sources_unchanged']},indent=2));sys.exit(result.returncode if result.returncode else (0 if unchanged and jar and audit['all_class_major_65'] and audit['no_test_classes'] else 1))
