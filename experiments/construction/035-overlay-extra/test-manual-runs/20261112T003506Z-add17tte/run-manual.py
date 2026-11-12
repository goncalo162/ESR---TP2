#!/usr/bin/env python3
"""V-034: separate real JVMs, public JAR APIs, real sequential SIGKILL; no sudo/fake clock/wire injections."""
import argparse
import datetime as dt
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import signal
import socket
import subprocess
import sys
import tempfile
import threading
import time
import uuid

ROOT=Path(__file__).resolve().parents[2]
EVIDENCE=ROOT/'experiments/construction/035-overlay-extra'
HELPER=Path(__file__).with_name('ManualV034Node.java')

def utc():return dt.datetime.now(dt.timezone.utc).isoformat()
def sha(p):return hashlib.sha256(Path(p).read_bytes()).hexdigest()
def save(p,data):Path(p).write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')

class Child:
    def __init__(self,run,ident,role,profile,classpath,token):
        self.run=run;self.ident=ident;self.profile=profile;self.token=token;self.records=[];self.replies={};self.cv=threading.Condition()
        self.log_path=profile.with_suffix('.log');self.command=[str(run.java),'-cp',classpath,'ManualV034Node',role,str(profile),token,run.jar_hash]
        self.proc=subprocess.Popen(self.command,cwd=ROOT,stdin=subprocess.PIPE,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,bufsize=1)
        self.start_identity=self.identity();self.reader=threading.Thread(target=self.read,daemon=True);self.reader.start()
        run.actions.append({'at':utc(),'action':'spawn','node':ident,'pid':self.proc.pid,'identity':self.start_identity,'command':self.command})
    def identity(self):
        data=Path(f'/proc/{self.proc.pid}/stat').read_text();return data[data.rfind(')')+2:].split()[19]
    def read(self):
        with self.log_path.open('w') as log:
            for line in self.proc.stdout:
                log.write(line);log.flush()
                try:record=json.loads(line)
                except ValueError:continue
                with self.cv:
                    if len(self.records)>=10000:
                        self.records.append({'kind':'parent-log-overflow'});self.cv.notify_all();return
                    self.records.append(record)
                    if record.get('kind')=='state':self.replies[record['payload']['request']]=record
                    self.cv.notify_all()
    def wait(self,predicate,timeout=15):
        deadline=time.monotonic()+timeout
        with self.cv:
            while True:
                value=predicate()
                if value:return value
                if self.proc.poll() is not None:raise RuntimeError(f'{self.ident} exited {self.proc.returncode}; see {self.log_path}')
                left=deadline-time.monotonic()
                if left<=0:raise TimeoutError(f'{self.ident} observation timeout; see {self.log_path}')
                self.cv.wait(min(left,.1))
    def ready(self):
        return self.wait(lambda:next((r for r in self.records if r.get('kind')=='ready'),None))
    def send(self,text):self.proc.stdin.write(text+'\n');self.proc.stdin.flush()
    def state(self):
        request=uuid.uuid4().hex;self.send('state '+request)
        record=self.wait(lambda:self.replies.pop(request,None),5)
        assert record['pid']==self.proc.pid and record['token']==self.token
        assert record['payload']['dropped']==0 and record['payload'].get('failure') is None,record
        return record
    def finish(self):
        code=self.proc.wait(timeout=8);self.reader.join(2)
        assert not self.reader.is_alive(),self.ident+' log reader did not end'
        self.proc.stdin.close();self.proc.stdout.close();return code
    def quit(self):self.send('quit');return self.finish()
    def kill_relay(self):
        assert self.ident.startswith('R') and self.proc.poll() is None
        cmdline=Path(f'/proc/{self.proc.pid}/cmdline').read_bytes().split(b'\0')
        assert self.identity()==self.start_identity and str(self.profile).encode() in cmdline and self.token.encode() in cmdline
        assert b'ManualV034Node' in cmdline and Path(cmdline[0].decode()).resolve()==self.run.java.resolve()
        self.run.actions.append({'at':utc(),'action':'SIGKILL','node':self.ident,'pid':self.proc.pid,'identity':self.start_identity,'cmdline':[s.decode() for s in cmdline if s]})
        self.proc.kill();code=self.finish();self.run.check(code==-signal.SIGKILL,f'{self.ident}: real SIGKILL exit -9')
    def events(self,kind):
        with self.cv:return [r for r in self.records if r.get('kind')=='event' and r['payload']['type']==kind]

class Run:
    def __init__(self,args):
        self.operator=args.operator;self.java=Path(args.java).resolve();self.javac=self.java.with_name('javac')
        self.build=json.loads((EVIDENCE/'test-build-summary.json').read_text());self.jar=(ROOT/args.jar).resolve() if args.jar else ROOT/self.build['jar']['path']
        self.jar_hash=self.build['jar']['sha256'];self.started=utc();self.actions=[];self.checks=[];self.graphs=[];self.all_children=[]
        runs=EVIDENCE/'test-manual-runs';runs.mkdir(exist_ok=True)
        self.path=Path(tempfile.mkdtemp(prefix=dt.datetime.now(dt.timezone.utc).strftime('%Y%m%dT%H%M%SZ-'),dir=runs))
        print(self.path,flush=True);self.log=(self.path/'manual.log').open('w');self.helper_hash=sha(HELPER)
    def message(self,text):print(text,flush=True);self.log.write(utc()+' '+text+'\n');self.log.flush()
    def check(self,condition,label):
        if not condition:raise AssertionError(label)
        self.checks.append({'at':utc(),'label':label});self.message('CHECK_PASS '+label)
    def prepare(self):
        self.check(sha(self.jar)==self.jar_hash,'verified clean-verify JAR hash before any child')
        version=subprocess.run([str(self.java),'-version'],capture_output=True,text=True);(self.path/'java-version.log').write_text(version.stdout+version.stderr)
        self.check(version.returncode==0 and re.search(r'version "21[.\"]',version.stderr+version.stdout) is not None,'JDK 21 selected')
        self.classes=self.path/'helper-classes';self.classes.mkdir();command=[str(self.javac),'--release','21','-cp',str(self.jar),'-d',str(self.classes),str(HELPER)]
        result=subprocess.run(command,capture_output=True,text=True);(self.path/'helper-compile.log').write_text('COMMAND '+json.dumps(command)+'\n'+result.stdout+result.stderr+'\nEXIT '+str(result.returncode)+'\n')
        self.check(result.returncode==0,'observation helper compiled against verified JAR')
        self.classpath=os.pathsep.join([str(self.jar),str(self.classes)])
    def snapshot(self,label,children):
        value={key:child.state() for key,child in children.items() if key!='BOOT' and child.proc.poll() is None}
        for key,record in value.items():self.record_proofs(key,record)
        return value
    def record_proofs(self,key,state):
        if not hasattr(self,'proofs'):self.proofs={}
        bucket=self.proofs.setdefault(key,[])
        for candidate in state['payload']['candidates']:
            for proof in candidate['sources']:
                row={'target':candidate['nodeId'],'proof':proof}
                if row not in bucket:bucket.append(row)
    def wait_graph(self,children,expected,timeout=15):
        deadline=time.monotonic()+timeout
        while True:
            value=self.snapshot('poll',children)
            if set(value)==set(expected) and all(set(value[n]['payload']['sessions'])==set(peers) for n,peers in expected.items()):return value
            if time.monotonic()>=deadline:raise TimeoutError('graph not converged: '+json.dumps({n:list(s['payload']['sessions']) for n,s in value.items()}))
            time.sleep(.1)
    def ss(self,folder,label,children,pair):
        # ss may observe a different network context in a managed terminal. Keep it as diagnostic;
        # correlate the owned JVM FDs with each PID's actual /proc network table for the assertion.
        command=['ss','-4Htnp','state','established'];result=subprocess.run(command,capture_output=True,text=True,timeout=5)
        (folder/(label+'-ss.log')).write_text('COMMAND '+json.dumps(command)+'\n'+result.stdout+result.stderr+'\nEXIT '+str(result.returncode)+'\n')
        links={};observed={}
        def address(raw):
            ip,port=raw.split(':');return socket.inet_ntoa(bytes.fromhex(ip)[::-1])+':'+str(int(port,16))
        for name in pair:
            child=children[name];pid=child.proc.pid
            assert child.proc.poll() is None and child.identity()==child.start_identity
            fdlinks={}
            for fd in Path(f'/proc/{pid}/fd').iterdir():
                try:target=os.readlink(fd)
                except FileNotFoundError:continue
                match=re.fullmatch(r'socket:\[(\d+)\]',target)
                if match:fdlinks[fd.name]=match.group(1)
            table=Path(f'/proc/{pid}/net/tcp').read_text();owned=[]
            (folder/(label+'-'+name+'-proc-tcp.log')).write_text(table)
            for row in table.splitlines()[1:]:
                fields=row.split()
                if fields[3]!='01' or fields[9] not in fdlinks.values():continue
                endpoints=tuple(sorted([address(fields[1]),address(fields[2])]))
                owned.append({'inode':fields[9],'endpoints':list(endpoints)})
                links.setdefault(endpoints,set()).add(pid)
            observed[name]={'pid':pid,'start_identity':child.start_identity,'network_namespace':os.readlink(f'/proc/{pid}/ns/net'),'socket_fds':fdlinks,'established_owned':owned}
        pids={children[n].proc.pid for n in pair};matching=[list(key) for key,owners in links.items() if pids<=owners]
        save(folder/(label+'-physical-tcp.json'),{'method':'per-PID /proc FD inode and TCP established tuple correlation','nodes':observed,'matching_connections':matching,'ss_is_diagnostic_only':True})
        self.check(len(matching)==1,label+': exactly one owned physical TCP between '+','.join(pair))
        return matching
    def graph(self,deeper):
        name='B-S-R1-R2R3-C' if deeper else 'A-S-R1R2-C';folder=self.path/name;folder.mkdir();token=uuid.uuid4().hex
        edges={'S':['R1'],'R1':['S','R2','R3'],'R2':['R1','C'],'R3':['R1','C'],'C':['R2','R3']} if deeper else {'S':['R1','R2'],'R1':['S','C'],'R2':['S','C'],'C':['R1','R2']}
        reservations=[];ports=[]
        try:
            for n in range(1+2*len(edges)):
                sock=socket.socket();sock.bind(('127.0.0.1',0));reservations.append(sock);ports.append(sock.getsockname()[1])
        finally:
            for sock in reservations:sock.close()
        roles={n:'SERVER' if n=='S' else 'CLIENT' if n=='C' else 'RELAY' for n in edges}
        assigned={n:(ports[1+2*i],ports[2+2*i]) for i,n in enumerate(edges)}
        topology='node.ids='+','.join(edges)+'\n'
        for n,peers in edges.items():
            topology+='\n'+f'node.{n}.host=127.0.0.1\nnode.{n}.controlPort={assigned[n][0]}\nnode.{n}.dataPort={assigned[n][1]}\nnode.{n}.role={roles[n]}\nnode.{n}.neighbors='+','.join(peers)+'\n'
        (folder/'topology.properties').write_text(topology)
        profiles={};common='bind.host=127.0.0.1\nadvertised.host=127.0.0.1\n'
        profiles['BOOT']=folder/'BOOT.properties';profiles['BOOT'].write_text('node.id=BOOT\nnode.role=BOOTSTRAP\n'+common+f'control.port={ports[0]}\ntopology.file=topology.properties\n')
        streamfile=ROOT/'Projeto/src/main/resources/config/streams-s1.properties'
        for n in edges:
            p=folder/(n+'.properties');text=f'node.id={n}\nnode.role={roles[n]}\n'+common+f'control.port={assigned[n][0]}\ndata.port={assigned[n][1]}\nbootstrap.host=127.0.0.1\nbootstrap.port={ports[0]}\n'
            if n=='S':text+=f'streams.file={streamfile}\nmedia.base={ROOT/"media"}\n'
            p.write_text(text);profiles[n]=p
        command=[str(self.java),'-cp',self.classpath,'ManualV034Node','validate']+[str(p) for p in profiles.values()]
        validated=subprocess.run(command,capture_output=True,text=True,timeout=10);(folder/'offline-validation.log').write_text('COMMAND '+json.dumps(command)+'\n'+validated.stdout+validated.stderr+'\nEXIT '+str(validated.returncode)+'\n')
        self.check(validated.returncode==0,name+': complete profiles validated offline')
        children={}
        def start(n,role):
            child=Child(self,n,role,profiles[n],self.classpath,token);children[n]=child;self.all_children.append(child);return child
        boot=start('BOOT','BOOTSTRAP');boot.ready()
        for n in edges:start(n,roles[n])
        for n in edges:children[n].ready()
        for n,c in children.items():
            begun=next(r for r in c.records if r.get('kind')=='started')
            self.check(begun['payload']['jarSha256']==self.jar_hash,name+': '+n+' actual loaded JAR hash matches')
        initial=self.wait_graph(children,edges);save(folder/'01-initial.json',initial);self.check(True,name+': all configured sessions ACTIVE')
        survivor_ids=['S','R1','C'] if deeper else ['S','C'];original={n:{'pid':initial[n]['pid'],'instance':initial[n]['instance']} for n in survivor_ids}
        healthy={n:initial[n]['payload']['sessions'][other] for n,other in [('S','R1'),('R1','S')]} if deeper else {}
        targets={'C':'R1','R1':'C'} if deeper else {'C':'S','S':'C'}
        def learned(states):return all(any(c['nodeId']==target and c['state']=='AVAILABLE' and c['sources'] for c in states[n]['payload']['candidates']) for n,target in targets.items())
        deadline=time.monotonic()+12
        while True:
            before=self.snapshot('before',children)
            if learned(before):break
            if time.monotonic()>=deadline:raise TimeoutError(name+': candidates not learned')
            time.sleep(.1)
        save(folder/'02-learned-before-failures.json',before)
        self.check(True,name+': both repair endpoints learned before failures')
        for n,target in targets.items():
            c=next(c for c in before[n]['payload']['candidates'] if c['nodeId']==target)
            self.check(all(p['source']['nodeId'] in edges[n] and p['generation']>0 and p['ttlNanos']==60_000_000_000 and p['learnedNanos']>=p['queryStartedNanos'] and p['source']['connectionId']==before[n]['payload']['sessions'][p['source']['nodeId']]['connectionId'] for p in c['sources']),name+': '+n+' direct provenance and original TTL recorded')
        self.actions.append({'at':utc(),'action':'quit-bootstrap','graph':name,'pid':boot.proc.pid});self.check(boot.quit()==0,name+': bootstrap stopped with exit 0 before relay deaths')
        first,second=('R2','R3') if deeper else ('R1','R2');children[first].kill_relay()
        expected={n:[p for p in peers if p!=first] for n,peers in edges.items() if n!=first}
        after_first=self.wait_graph(children,expected);save(folder/'03-after-first-death.json',after_first)
        self.check(True,name+': first death preserved configured alternative without new edge')
        first_observations=[];until=time.monotonic()+1
        while time.monotonic()<until:
            states=self.snapshot('first-window',children);first_observations.append(states)
            assert all(set(states[n]['payload']['sessions'])==set(peers) for n,peers in expected.items());time.sleep(.15)
        save(folder/'04-first-death-window.json',first_observations)
        self.check(len(first_observations)>1,name+': no premature repair during recorded first-death interval')
        before_second=self.snapshot('before-second',children);save(folder/'05-before-second-death.json',before_second)
        self.check(learned(before_second),name+': candidates retained after first announcer death')
        pinned={n:list(self.proofs.get(n,[])) for n in targets};save(folder/'05-proofs-seen-before-second-death.json',pinned)
        children[second].kill_relay()
        expected={'S':['R1'],'R1':['S','C'],'C':['R1']} if deeper else {'S':['C'],'C':['S']}
        repaired=self.wait_graph(children,expected);save(folder/'06-repaired.json',repaired);self.check(True,name+': new direct edge confirmed on both endpoints')
        for n,old in original.items():self.check(repaired[n]['pid']==old['pid'] and repaired[n]['instance']==old['instance'] and children[n].proc.poll() is None,name+': '+n+' original process/instance survived')
        if deeper:
            self.check(repaired['S']['payload']['sessions']['R1']==healthy['S'] and repaired['R1']['payload']['sessions']['S']==healthy['R1'],name+': S-R1 sessions/connectionIds unchanged in both directions')
        selection_events=[]
        for n,target in targets.items():
            events=[r for r in children[n].events('RepairStarted') if r['payload']['data']['attempt']['targetId']==target]
            for event in events:
                a=event['payload']['data']['attempt'];proofs=[v['proof'] for v in pinned[n] if v['target']==target]
                self.check(any(a['source']==p['source'] and a['generation']==p['generation'] and a['queryStartedNanos']==p['queryStartedNanos'] and a['ttlNanos']==p['ttlNanos'] for p in proofs),name+': '+n+' selection matches provenance observed before death')
                self.check(0<=a['selectedAtNanos']-a['queryStartedNanos']<a['ttlNanos'],name+': '+n+' selected within original claim TTL')
                selection_events.append(event)
        self.check(bool(selection_events),name+': repair had a locally learned selection witness');save(folder/'07-selected-provenance.json',selection_events)
        observations=[];until=time.monotonic()+2
        while time.monotonic()<until:
            states=self.snapshot('stable',children);observations.append(states)
            for n,peers in expected.items():
                assert set(states[n]['payload']['sessions'])==set(peers)
                for peer in peers:assert states[n]['payload']['sessions'][peer]==repaired[n]['payload']['sessions'][peer]
            time.sleep(.2)
        save(folder/'08-stability-window.json',observations);self.check(len(observations)>1,name+': sessions unchanged during recorded two-second interval')
        for n,target in targets.items():
            ups=[r for r in children[n].events('NeighborUp') if r['payload']['data']['session']['nodeId']==target]
            downs=[r for r in children[n].events('NeighborDown') if r['payload']['data']['session']['nodeId']==target]
            self.check(len(ups)==1 and not downs,name+': '+n+' exactly one repaired NeighborUp and no repaired NeighborDown')
        pair=('R1','C') if deeper else ('S','C');physical=self.ss(folder,'09-repaired',children,pair)
        if deeper:self.ss(folder,'10-preserved-S-R1',children,('S','R1'))
        self.check(boot.proc.poll()==0,name+': bootstrap remains stopped after repair')
        # End surviving processes gracefully and preserve their individual exit codes.
        for n in survivor_ids:children[n].send('quit')
        codes={n:children[n].finish() for n in survivor_ids};self.check(all(c==0 for c in codes.values()),name+': every surviving JVM exited 0 after bounded shutdown')
        for port in ports:
            with socket.socket() as s:s.setsockopt(socket.SOL_SOCKET,socket.SO_REUSEADDR,1);s.bind(('127.0.0.1',port))
        self.check(all(c.proc.poll() is not None and not c.reader.is_alive() for c in children.values()),name+': no test process/log reader remains and listener ports reusable')
        result={'graph':name,'token':token,'profiles':{n:str(p.relative_to(ROOT)) for n,p in profiles.items()},'ports':ports,'original_survivors':original,'final_exits':{n:c.proc.returncode for n,c in children.items()},'physical_repaired_TCP':physical,'passed':True}
        self.graphs.append(result);save(folder/'result.json',result)
    def execute(self):
        passed=False;error=None
        try:
            self.prepare();self.graph(False);self.graph(True)
            self.check(sha(self.jar)==self.jar_hash and sha(HELPER)==self.helper_hash,'verified JAR and observation helper hashes unchanged')
            self.message('MANUAL_PASS');passed=True
        except BaseException as failure:
            error=repr(failure);self.message('MANUAL_FAIL '+error)
        finally:
            cleanup=[]
            for child in self.all_children:
                if child.proc.poll() is None:
                    passed=False
                    try:
                        self.actions.append({'at':utc(),'action':'failure-cleanup-terminate-owned-child','pid':child.proc.pid,'node':child.ident})
                        child.proc.terminate()
                        try:child.proc.wait(timeout=5)
                        except subprocess.TimeoutExpired:child.proc.kill();child.proc.wait(timeout=5)
                        child.reader.join(2);cleanup.append({'pid':child.proc.pid,'exit':child.proc.returncode})
                    except BaseException as failure:cleanup.append({'pid':child.proc.pid,'error':repr(failure)})
            save(self.path/'actions.json',self.actions)
            inventory={str(p.relative_to(self.path)):sha(p) for p in self.path.rglob('*') if p.is_file() and p.name not in ['result.json','manual.log']}
            save(self.path/'result.json',{'operator':self.operator,'started_at':self.started,'finished_at':utc(),'run_directory':str(self.path.relative_to(ROOT)),'jar':str(self.jar.relative_to(ROOT)) if self.jar.is_relative_to(ROOT) else str(self.jar),'jar_sha256':self.jar_hash,'helper_sha256':self.helper_hash,'runner_sha256':sha(__file__),'passed':passed,'error':error,'checks':self.checks,'check_pass':len(self.checks),'graphs':self.graphs,'cleanup_after_failure':cleanup,'files':inventory,'human_approval':'not inferred','scope':'real separate JVMs/default public OverlayNode and BootstrapServer APIs, real sequential SIGKILL; loopback control only; helper not production OttApplication CLI; no video/CORE','no_processes_pending':all(c.proc.poll() is not None and not c.reader.is_alive() for c in self.all_children)})
            self.log.close()
        return 0 if passed else 1

def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--operator',required=True);parser.add_argument('--java',default='/usr/lib/jvm/java-21-openjdk-amd64/bin/java');parser.add_argument('--jar');args=parser.parse_args()
    return Run(args).execute()
if __name__=='__main__':sys.exit(main())
