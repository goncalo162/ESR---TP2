from pathlib import Path
import json,subprocess,hashlib,datetime
P=Path('experiments/construction/011-video/test-01');s=Path(json.loads((P/'entry.json').read_text())['scratch']);folder=s/'negative-audits';folder.mkdir()
with Path('Projeto/src/main/resources/media/a.Mjpeg').open('rb') as f:L=int(f.read(5));jpeg=f.read(L)
def pack(j):return f'{len(j):05d}'.encode()+j
cases=[('empty',b'',False),*[(f'prefix-{i}',b'1234'[:i],False) for i in range(1,5)],('nondecimal',b'0000x'+jpeg,False),('zero',b'00000',False),('partial-payload',f'{L:05d}'.encode()+jpeg[:-1],False),('invalid-jpeg',b'00004xxxx',False),('missing-soi',pack(b'xx'+jpeg[2:]),False),('missing-eoi',pack(jpeg[:-2]),False),('trailing-file-byte',pack(jpeg)+b'\n',False),('extra-inside-payload',pack(jpeg+b'junk'),False),('six-digit-length',b'100000'+jpeg+bytes(100000-len(jpeg)),False),('clean-eof-between-records',pack(jpeg)+pack(jpeg),True)]
# Add ignored APP15 segments without altering decoded pixels, to exercise the actual99999 boundary.
remaining=99999-len(jpeg);segments=bytearray()
while remaining:
 total=min(remaining,65537)
 if 0<remaining-total<4:total-=4-(remaining-total)
 assert total>=4;segments+=b'\xff\xef'+(total-2).to_bytes(2,'big')+bytes(total-4);remaining-=total
maximum=jpeg[:2]+segments+jpeg[2:];assert len(maximum)==99999
cases.append(('valid-jpeg-length-99999',b'99999'+maximum,True))
results=[]
for name,data,expected in cases:
 file=folder/(name+'.Mjpeg');file.write_bytes(data);inventory=folder/(name+'.jsonl')
 cmd=['/Users/maria/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home/bin/java','-Xmx256m','-Djava.awt.headless=true','-cp',str(s),'AuditMjpeg',str(file),str(inventory),'384','288'];r=subprocess.run(cmd,capture_output=True,text=True,timeout=20)
 result={'name':name,'expected_accept':expected,'exit_code':r.returncode,'passed':(r.returncode==0)==expected,'fixture_sha256':hashlib.sha256(data).hexdigest(),'fixture_bytes':len(data),'command':cmd,'stdout':r.stdout,'stderr':r.stderr};results.append(result);assert result['passed'],result
(P/'negative-results.json').write_text(json.dumps({'recorded_at':datetime.datetime.now(datetime.timezone.utc).isoformat(),'cases':results,'passed':len(results),'failed':0,'scope':'Temporary synthetic byte-level audit inputs only; real media files not modified.'},indent=2)+'\n');print('Independent boundary/negative audits',len(results),'passed')
