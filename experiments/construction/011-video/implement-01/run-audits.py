from pathlib import Path
import json,subprocess,concurrent.futures,datetime
P=Path('experiments/construction/011-video/implement-01');s=Path(json.loads((P/'entry.json').read_text())['scratch'])
def audit(alias):
 cmd=['/Users/maria/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home/bin/java','-Xmx256m','-Djava.awt.headless=true','-cp',str(s/'audit'),'AuditMjpeg',f'Projeto/src/main/resources/media/{alias.lower()}.Mjpeg',str(P/(alias+'-frames.jsonl')),'640','360']
 r=subprocess.run(cmd,capture_output=True,text=True,timeout=600);(P/(alias+'-audit.log')).write_text(r.stdout+r.stderr);(P/(alias+'-audit-command.json')).write_text(json.dumps({'command':cmd,'exit_code':r.returncode,'finished_at':datetime.datetime.now(datetime.timezone.utc).isoformat()},indent=2)+'\n');print(alias,'AUDIT_EXIT',r.returncode,r.stdout,r.stderr,flush=True);assert r.returncode==0;return json.loads(r.stdout)
with concurrent.futures.ThreadPoolExecutor(max_workers=3) as pool:results=list(pool.map(audit,['B','C','D']))
(P/'full-audit-results.json').write_text(json.dumps([json.loads((P/'A-audit.log').read_text())]+results,indent=2)+'\n')
