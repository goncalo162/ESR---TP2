#!/usr/bin/env python3
"""Run the standalone V-033 manual with an explicit operator and preserve every attempt."""
from pathlib import Path
import argparse,datetime,hashlib,json,os,signal,subprocess,tempfile
root=Path(__file__).resolve().parents[2]
evidence=root/'experiments/construction/034-overlay-extra'
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--operator',required=True)
parser.add_argument('--java',default='/usr/lib/jvm/java-21-openjdk-amd64/bin/java')
args=parser.parse_args()
if not args.operator.strip():parser.error('operator is required')
build=json.loads((evidence/'test-build-summary.json').read_text())
jar=root/build['jar'];source=Path(__file__).with_name('ManualV033.java')
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
if sha(jar)!=build['jar_sha256']:raise SystemExit('Verified JAR hash mismatch; no manual started')
now=lambda:datetime.datetime.now(datetime.timezone.utc).isoformat()
parent=evidence/'test-manual-runs';parent.mkdir(exist_ok=True)
run=Path(tempfile.mkdtemp(prefix=datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%dT%H%M%SZ-'),dir=parent))
command=[args.java,'--class-path',str(jar),str(source),args.operator,build['jar_sha256'],str(jar),str(run)]
started=now();source_hash=sha(source);before=sha(jar);log=run/'manual.log'
print(run,flush=True);timed_out=False
with log.open('w') as out:
    out.write('COMMAND '+json.dumps(command)+'\nSTARTED '+started+'\n');out.flush()
    process=subprocess.Popen(command,cwd=root,stdout=out,stderr=subprocess.STDOUT,start_new_session=True)
    try:code=process.wait(timeout=180)
    except subprocess.TimeoutExpired:
        timed_out=True;os.killpg(process.pid,signal.SIGTERM)
        try:code=process.wait(timeout=5)
        except subprocess.TimeoutExpired:os.killpg(process.pid,signal.SIGKILL);code=process.wait(timeout=5)
text=log.read_text();result={'operator':args.operator,'started_at':started,'finished_at':now(),'command':command,'java_exit':code,'timed_out':timed_out,'check_pass':sum(line.startswith('CHECK_PASS ') for line in text.splitlines()),'manual_pass':sum(line.startswith('MANUAL_PASS ') for line in text.splitlines()),'jar_sha256_before':before,'jar_sha256_after':sha(jar),'source_sha256_before':source_hash,'source_sha256_after':sha(source),'log':str(log.relative_to(root)),'log_sha256':sha(log),'scope':'Standalone public API over real loopback TCP, embedded nodes in one JVM plus separate real CLI JVMs. API close/EOF, not physical host death, CORE, video or IMP-035 repair.','passed':code==0 and not timed_out and sum(line.startswith('MANUAL_PASS ') for line in text.splitlines())==1 and before==sha(jar) and source_hash==sha(source)}
(run/'result.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n')
print(text,flush=True);print(json.dumps(result,ensure_ascii=False,indent=2),flush=True)
raise SystemExit(0 if result['passed'] else 1)
