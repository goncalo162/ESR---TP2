from pathlib import Path
import json
import re
import struct
import uuid

root = Path(__file__).resolve().parent
pending = {}
calls = []
for number, line in enumerate((root / 'traced-class.strace').read_text().splitlines(), 1):
    tid = line.split()[0]
    if '<unfinished ...>' in line:
        pending[tid] = (number, line)
        continue
    if '<... ' in line and ' resumed>' in line:
        if tid not in pending:
            continue
        original_number, original = pending.pop(tid)
        line = original.replace('<unfinished ...>', line.split(' resumed>', 1)[1])
        number = original_number
    match = re.search(r'"((?:\\x[0-9a-f]{2})+)"', line)
    data = bytes.fromhex(match[1].replace('\\x', '')) if match else b''
    prefix = line.split(', "', 1)[0]
    ports = re.findall(r'\]:(\d+)', prefix) if '<TCP' in prefix else []
    calls.append({'line': number, 'tid': tid, 'at': line.split()[1],
                  'ports': [int(p) for p in ports], 'data': data, 'raw': line})

def frame(data):
    if len(data) < 40 or struct.unpack('>I', data[:4])[0] != len(data) - 4:
        return None
    size = struct.unpack('>H', data[22:24])[0]
    if len(data) < 40 + size:
        return None
    return {'version': data[4], 'type': data[5],
            'requestId': str(uuid.UUID(bytes=data[6:22])),
            'senderId': data[24:24+size].decode(),
            'senderInstance': str(uuid.UUID(bytes=data[24+size:40+size])),
            'tail_hex': data[40+size:].hex()}

negative = []
for call in calls:
    if ' write(' not in call['raw'] or len(call['ports']) != 2:
        continue
    message = frame(call['data'])
    if message and message['type'] == 6 and message['tail_hex'] == '00000000':
        negative.append((call, message))
assert len(negative) == 2, len(negative)
result = []
selected = {}
for negative_call, reply in negative:
    ports = negative_call['ports']
    stream = [c for c in calls if c['ports'] in (ports, ports[::-1])]
    hellos = [(c, frame(c['data'])) for c in stream if ' write(' in c['raw']]
    hello_call, hello = next((c, f) for c, f in hellos if f and f['type'] == 5 and f['requestId'] == reply['requestId'])
    read = b''.join(c['data'] for c in stream if ' read(' in c['raw'] and c['ports'] == ports[::-1] and c['data'])
    assert read == negative_call['data'], (ports, read.hex())
    closes = [c for c in stream if 'close(' in c['raw'] or 'shutdown(' in c['raw']]
    logs = [c for c in calls if ' write(1<' in c['raw'] and reply['requestId'].encode() in c['data']
            and b'REQUIRED_DISCOVERY_MISSING' in c['data']]
    assert len(logs) == 1
    diagnostic = logs[0]['data'].decode()
    assert 'session=Optional.empty' in diagnostic
    connection = re.search(r'connectionId=Optional\[([^]]+)\]', diagnostic)[1]
    for c in stream + logs:
        selected[c['line']] = c['raw']
    result.append({'HELLO': hello, 'HELLO_OK_negative': reply,
                   'connectionId': connection, 'Session': None,
                   'Session_explanation': 'Rejected before ACTIVE; connectionId identifies this negotiation, no Session was created.',
                   'socket_ports_peer_to_node': ports,
                   'hello_write_trace_line': hello_call['line'],
                   'negative_write_trace_line': negative_call['line'],
                   'negative_bytes_read_complete': True,
                   'close_and_shutdown_trace_lines': [c['line'] for c in closes],
                   'diagnostic_trace_line': logs[0]['line'],
                   'diagnostic': diagnostic.strip(),
                   'flush_limit': 'Java SocketOutputStream.flush has no separate syscall; write returned 46 and original test then invokes flush and closes. Local flush is not peer acceptance.'})

(root / 'wire-correlation.json').write_text(json.dumps(result, indent=2) + '\n')
(root / 'wire-correlation.strace').write_text('\n'.join(f'{n}: {s}' for n, s in sorted(selected.items())) + '\n')
print(json.dumps([{'node': r['HELLO']['senderId'], 'requestId': r['HELLO']['requestId'],
                   'connectionId': r['connectionId'], 'Session': r['Session'],
                   'complete_negative_read': r['negative_bytes_read_complete']} for r in result], indent=2))
