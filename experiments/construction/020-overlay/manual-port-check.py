"""Availability probe only: binds configured loopback ports without listening, then closes all."""
from pathlib import Path
import socket, json, datetime, sys
manifest = json.loads((Path(__file__).parent / 'manual-preparation.json').read_text())
held, results = [], []
try:
    for profile in manifest['profiles']:
        for field, kind in [('control_tcp', socket.SOCK_STREAM), ('data_udp', socket.SOCK_DGRAM)]:
            port = profile[field]
            if not port: continue
            result = {'node': profile['node'], 'protocol': 'TCP' if kind == socket.SOCK_STREAM else 'UDP', 'host': profile['host'], 'port': port}
            probe = socket.socket(socket.AF_INET, kind)
            held.append(probe)
            try:
                probe.bind((profile['host'], port)); result['available'] = True
            except OSError as error:
                result.update(available=False, error=str(error))
            results.append(result)
finally:
    for probe in held: probe.close()
print(json.dumps({'checked_at': datetime.datetime.now(datetime.timezone.utc).isoformat(), 'ports': results, 'reservation_retained': False}, indent=2))
sys.exit(0 if all(result['available'] for result in results) else 1)
