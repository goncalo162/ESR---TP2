import subprocess,sys,time,json,re,datetime,os
from pathlib import Path
root=Path.cwd();base=root/'experiments/construction/022-rotas';tool=base/'manual-tools/manual.py'
cmd=[sys.executable,'-B',str(tool)];run=None;children={};watch=None

def until(condition,label,timeout=10):
    began=time.monotonic()
    while not condition():
        if time.monotonic()-began>=timeout:raise AssertionError(label)
        time.sleep(.02)
def text(node):
    p=run/f'{node}.log';return p.read_text() if p.exists() else ''
def send(node,line):
    children[node].stdin.write((line+'\n').encode());children[node].stdin.flush()
def exited(node,expected):
    actual=children[node].wait(timeout=6)
    assert actual==expected,(node,actual,expected,text(node))
def listing(kind):
    at=len(text('C'));send('C',kind);until(lambda:kind.upper()+'_END' in text('C')[at:],kind);return text('C')[at:]
try:
    prepared=subprocess.run(cmd+['prepare','--operator','Codex — ensaio técnico do roteiro','--kind','agent'],capture_output=True,text=True,check=True)
    print(prepared.stdout,flush=True);run=Path(next(line[4:] for line in prepared.stdout.splitlines() if line.startswith('RUN=')))
    watch=subprocess.Popen(cmd+['watch','--run',str(run)],stdout=(run/'watch.log').open('wb'),stderr=subprocess.STDOUT)
    until(lambda:(run/'watch.ready.json').exists(),'UDP watcher readiness')
    for node in ['BOOT','S','R1','R2','C']:
        children[node]=subprocess.Popen(cmd+['node','--run',str(run),'--node',node],stdin=subprocess.PIPE,stdout=(run/f'{node}.wrapper.log').open('wb'),stderr=subprocess.STDOUT)
        until(lambda node=node:'READY' in text(node),'node ready '+node)
    for node in ['S','R1','R2','C']:
        until(lambda node=node:text(node).count('NeighborUp[')>=2,'neighbors '+node)
    began=time.monotonic()
    while 'state=INACTIVE' not in listing('routes'):
        assert time.monotonic()-began<10
        time.sleep(.1)
    listing('catalog');listing('metrics')
    send('S','trace 00000000-0000-4000-8000-000000000001')
    until(lambda:'TRACE_RESULT stream=' in text('S'),'local trace')
    send('C','trace 00000000-0000-4000-8000-000000000001')
    until(lambda:'TRACE_RESULT stream=' in text('C'),'first trace')
    match=re.search(r'^TRACE_RESULT stream=.*visited=\[C, (R[12]), S\]',text('C'),re.M);assert match,text('C')
    chosen=match.group(1);other='R2' if chosen=='R1' else 'R1'
    send('BOOT','quit');exited('BOOT',0)
    subprocess.run(cmd+['signal','--run',str(run),'--node',chosen,'--signal','KILL'],check=True)
    exited(chosen,137)
    until(lambda:'NeighborDown[session=Session[nodeId='+chosen in text('C'),'selected relay down')
    listing('routes');at=len(text('C'));send('C','trace 00000000-0000-4000-8000-000000000001')
    until(lambda:'TRACE_RESULT stream=' in text('C')[at:],'alternative trace')
    assert 'visited=[C, '+other+', S]' in text('C')[at:]
    send('C','trace bad');send('C','trace 11111111-1111-4111-8111-111111111111')
    until(lambda:'UNKNOWN_STREAM' in text('C'),'unknown stream');assert 'COMMAND_ERROR trace requires' in text('C')
    time.sleep(3.05)
    send('S','quit');exited('S',0)
    send(other,'quit');exited(other,0)
    until(lambda:'NeighborDown[session=Session[nodeId='+other in text('C'),'last relay down')
    at=len(text('C'));send('C','trace 00000000-0000-4000-8000-000000000001')
    until(lambda:'NO_ROUTE' in text('C')[at:],'no route')
    send('C','quit');exited('C',0)
    subprocess.run(cmd+['stop-watch','--run',str(run)],check=True);assert watch.wait(timeout=3)==0
    verification=subprocess.run(cmd+['verify','--run',str(run)],capture_output=True,text=True)
    (run/'verification.log').write_text(verification.stdout+verification.stderr);print(verification.stdout,flush=True);assert verification.returncode==0,verification.stderr
    report=json.loads((run/'report.json').read_text());assert report['kind']=='agent' and report['passed']
    print('TECHNICAL_AGENT_PASS; HUMAN_MANUAL_PENDING; REPORT='+str(run/'report.json'),flush=True)
except Exception as e:
    if run:(run/'agent-driver-error.json').write_text(json.dumps({'recorded_at':datetime.datetime.now(datetime.timezone.utc).isoformat(),'error':str(e),'kind':'agent'},indent=2)+'\n')
    raise
finally:
    for node,process in children.items():
        if process.poll() is None:
            try:send(node,'quit');process.wait(timeout=5)
            except Exception:process.terminate()
        try:process.wait(timeout=3)
        except subprocess.TimeoutExpired:process.kill();process.wait(timeout=3)
        if process.stdin:process.stdin.close()
    if watch and watch.poll() is None:watch.terminate();watch.wait(timeout=3)
