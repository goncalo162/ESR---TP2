from collections import defaultdict
from datetime import datetime, timezone
from pathlib import Path
import hashlib,json,re,struct,subprocess,sys,tempfile

run=Path(sys.argv[1])
capture=run/'control.pcap'
raw=capture.read_bytes()
assert raw[:4] in (b'\xd4\xc3\xb2\xa1',b'\xa1\xb2\xc3\xd4')
endian='<' if raw[:4]==b'\xd4\xc3\xb2\xa1' else '>'
assert struct.unpack_from(endian+'I',raw,20)[0]==1
p=24
flows=[]
by_pair={}
packets=[]
while p<len(raw):
    sec,usec,size,original=struct.unpack_from(endian+'IIII',raw,p)
    data=raw[p+16:p+16+size]
    p+=16+size
    assert size==original==len(data) and data[12:14]==b'\x08\x00'
    ip=data[14:]
    assert ip[0]>>4==4 and ip[9]==6 and ip[12:20]==b'\x7f\x00\x00\x01'*2
    assert not struct.unpack_from('!H',ip,6)[0]&0x3fff
    ip_len=struct.unpack_from('!H',ip,2)[0]
    assert ip_len<=len(ip)
    tcp=ip[(ip[0]&15)*4:ip_len]
    src,dst,seq,ack=struct.unpack_from('!HHII',tcp)
    flags=tcp[13]
    payload=tcp[(tcp[12]>>4)*4:]
    ts=sec*1_000_000+usec
    key=tuple(sorted((src,dst)))
    if flags&0x12==2 and (key not in by_pair or by_pair[key]['initial']!=(src,seq)):
        flow={'id':len(flows),'initial':(src,seq),'initiator':src,'acceptor':dst,'packets':[]}
        flows.append(flow)
        by_pair[key]=flow
    assert key in by_pair,(src,dst,flags)
    packet=(ts,src,dst,seq,flags,payload)
    packets.append(packet)
    by_pair[key]['packets'].append(packet)
assert p==len(raw)
out=run/'analysis'
out.mkdir(exist_ok=True)
jar=Path('experiments/construction/020-overlay/verified-jar/ott-streaming-1.0-SNAPSHOT.jar').resolve()
inspect=Path('experiments/milestone-1/Inspect.java').resolve()
jdk=Path('/usr/lib/jvm/java-21-openjdk-amd64/bin')
frames=[]
def utc(ts):
    return datetime.fromtimestamp(ts/1_000_000,timezone.utc).isoformat(timespec='microseconds')
with tempfile.TemporaryDirectory(prefix='ott-v009-codec-') as classes:
    subprocess.run([jdk/'javac','-cp',jar,'-d',classes,inspect],check=True)
    for flow in flows:
        for port in (flow['initiator'],flow['acceptor']):
            ps=[x for x in flow['packets'] if x[1]==port]
            payloads=[x for x in ps if x[-1]]
            if not payloads:continue
            syns={x[3] for x in ps if x[4]&2}
            assert len(syns)==1,(flow['id'],port,syns)
            base=(syns.pop()+1)&0xffffffff
            chunks=sorted([((x[3]-base)&0xffffffff,x[0],x[-1]) for x in payloads])
            content=bytearray()
            for offset,ts,data in chunks:
                assert offset<=len(content) and offset+len(data)<=1_048_576
                overlap=min(len(content)-offset,len(data))
                assert content[offset:offset+overlap]==data[:overlap]
                content.extend(data[overlap:])
            target=out/f'flow-{flow["id"]:02}-{port}.bin'
            target.write_bytes(content)
            decoded=subprocess.run([jdk/'java','-cp',f'{classes}:{jar}','Inspect','frames',target],
                                    capture_output=True,text=True,check=True).stdout
            target.with_suffix('.frames.log').write_text(decoded)
            offset=0
            for line in decoded.splitlines():
                index,kind,request,sender,instance,payload=line.split(' ',5)
                size=struct.unpack_from('!I',content,offset)[0]+4
                assert offset+size<=len(content)
                timestamp=min(ts for start,ts,data in chunks if start<=offset<start+len(data))
                frames.append({'flow':flow['id'],'source':port,'destination':flow['acceptor'] if port==flow['initiator'] else flow['initiator'],
                               'at':utc(timestamp),'type':kind,'request_id':request.split('=',1)[1],
                               'sender':sender.split('=',1)[1],'instance_id':instance.split('=',1)[1],
                               'payload':payload.removeprefix('payload=')})
                offset+=size
            assert offset==len(content)
frames.sort(key=lambda x:x['at'])
(out/'frames.json').write_text(json.dumps(frames,indent=2)+'\n')
capture_log=(run/'capture.log').read_text()
marks={x['label']:x for x in map(json.loads,(run/'timeline.jsonl').read_text().splitlines())}
quiet=(marks['bootstrap-quiet-start']['at'],marks['bootstrap-quiet-end']['at'])
off=(marks['bootstrap-off']['at'],marks['scenario-ended']['at'])
bootstrap_window={}
for name,(a,b) in [('quiet',quiet),('off_through_reconnection',(off[0],marks['reconnected']['at']))]:
    matches=[x for x in packets if 56100 in x[1:3] and a<=utc(x[0])<=b]
    bootstrap_window[name]={'start':a,'end':b,'packets':len(matches),'syns':sum(bool(x[4]&2) for x in matches)}
    assert not matches
    assert utc(packets[0][0])<=a<b<=utc(packets[-1][0])
assert (marks['bootstrap-quiet-end']['monotonic_ns']-marks['bootstrap-quiet-start']['monotonic_ns'])/1e9>=30
register=[f for f in frames if f['type']=='REGISTER']
hello=[f for f in frames if f['type']=='HELLO']
assert len(register)==3 and {f['sender'] for f in register}=={'N0','N1','N2'}
assert len(hello)==4
def response(request,kind):
    matches=[f for f in frames if f['flow']==request['flow'] and f['type']==kind
             and f['request_id']==request['request_id'] and f['at']>=request['at']
             and f['source']==request['destination']]
    assert len(matches)==1,(request,kind)
    return matches[0]
discovery=[]
for r in register:
    ok=response(r,'REGISTER_OK')
    assert r['destination']==56100 and ok['sender']=='BOOT'
    assert any(x[1]==56100 and x[4]&1 and utc(x[0])>=ok['at'] for x in flows[r['flow']]['packets'])
    if r['sender'] in ('N1','N2'):
        assert 'nodeId=N0' in ok['payload'] and ok['at']<marks['client-relay-ready-server-absent']['at']
    discovery.append({'node':r['sender'],'flow':r['flow'],'request_id':r['request_id'],'response':ok['at']})
handshakes=[]
for h in hello:
    ok=response(h,'HELLO_OK')
    handshakes.append({'pair':sorted([h['sender'],ok['sender']]),'flow':h['flow'],
                       'request_id':h['request_id'],'after_bootstrap_exit':h['at']>off[0],
                       'sender_instance':h['instance_id'],'peer_instance':ok['instance_id']})
initial=[h['pair'] for h in handshakes if not h['after_bootstrap_exit']]
assert sorted(initial)==[['N0','N1'],['N0','N2'],['N1','N2']]
reconnect,=[h for h in handshakes if h['after_bootstrap_exit']]
original,=[h for h in handshakes if h['pair']==reconnect['pair'] and not h['after_bootstrap_exit']]
assert reconnect['pair']==['N0','N1'] and reconnect['flow']!=original['flow']
assert [reconnect[k] for k in ['sender_instance','peer_instance']]==[original[k] for k in ['sender_instance','peer_instance']]
client_leave=[f for f in frames if f['sender']=='N2' and f['type']=='NODE_LEAVE']
assert len(client_leave)==2 and all(f['payload']=='Leave[reason=0]' and
    f['at']>=marks['client-quit']['at'] for f in client_leave)
assert {tuple(h['pair']) for h in handshakes if h['flow'] in {f['flow'] for f in client_leave}}=={('N0','N2'),('N1','N2')}
def read(name):return json.loads((run/name).read_text())
exits={role:read(role+'.exit.json') for role in ['BOOTSTRAP','SERVER','RELAY','CLIENT']}
assert all(x['java_exit']==0 for x in exits.values())
assert all(read(role+'.start.json')['pid']==x['pid'] for role,x in exits.items())
remaining=[x['pid'] for x in exits.values() if Path(f"/proc/{x['pid']}").exists()]
assert not remaining
ports=read('ports-after.json')['ports']
assert len(ports)==7 and all(x['available'] for x in ports)
cut=read('cut-result.json')
assert cut['exit']==0 and not cut['old_tuple_rows'] and cut['capture_at_cut']['pid']==read('capture-start.json')['pid']
assert cut['capture_at_cut']['at']>off[0]
assert not Path(f"/proc/{read('capture-start.json')['pid']}").exists()
actions=[json.loads(line) for line in (run/'technical-actions.jsonl').read_text().splitlines()]
stats=int(re.search(r'^(\d+) packets captured$',capture_log,re.M)[1])
assert stats==len(packets)
assert '0 packets dropped by kernel' in capture_log
complete=('CAPTURE_EXIT 0' in capture_log and actions[-1]['scenario']=='actions-completed')
result={'run':run.name,'operator':read('manifest.json')['operator'],
        'technical_status':'PASS' if complete else 'INCOMPLETE',
        'capture_sha256':hashlib.sha256(raw).hexdigest(),'packets':len(packets),
        'packets_reported_received_by_filter':int(re.search(r'^(\d+) packets received by filter$',capture_log,re.M)[1]),
        'first_packet_at':utc(packets[0][0]),'last_packet_at':utc(packets[-1][0]),
        'dropped_by_kernel':0,'capture_clean_exit':complete,'bootstrap_windows':bootstrap_window,
        'quiet_seconds':(marks['bootstrap-quiet-end']['monotonic_ns']-marks['bootstrap-quiet-start']['monotonic_ns'])/1e9,
        'discovery':discovery,'handshakes':handshakes,'client_leave_frames':client_leave,
        'java_exits':exits,'ports_available_after':len(ports),'remaining_recorded_pids':remaining,
        'independent_repetition':False}
(out/'result.json').write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps(result,indent=2))
