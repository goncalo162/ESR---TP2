"""Offline design vectors from literal contract fields; never imports production code or v1 bytes."""
from pathlib import Path
import hashlib
import json
import struct
import uuid

ROOT = Path(__file__).resolve().parent
OUT = ROOT / "fixtures"
OUT.mkdir(exist_ok=True)
REQ = "00112233-4455-6677-8899-aabbccddeeff"
INST = "10213243-5465-7687-98a9-bacbdcedfe0f"
OTHER = "20314253-6475-8697-a8b9-cadbecfd0e1f"
HELLO = "11223344-5566-4788-99aa-bbccddeeff00"
STREAM = "00000000-0000-4000-8000-000000000001"
EPOCH = "deadbeef-0000-4000-8000-000000000001"
SUB = "fedcba98-7654-4321-8765-0123456789ab"
ZERO = "00000000-0000-0000-0000-000000000000"
U8 = lambda n: struct.pack(">B", n)
U16 = lambda n: struct.pack(">H", n)
U32 = lambda n: struct.pack(">I", n)
I64 = lambda n: struct.pack(">q", n)
UUID = lambda s: uuid.UUID(s).bytes


def string(s):
    b = s.encode("utf-8")
    return U16(len(b)) + b


def strings(values):
    return U16(len(values)) + b"".join(string(v) for v in values)


def endpoint(node="S1", instance=INST, host="10.0.0.1", control=5000, data=5001, role=1):
    return string(node) + UUID(instance) + string(host) + U16(control) + U16(data) + U8(role)


def frame(kind, payload=b"", sender="S1", instance=INST, request=REQ, version=2):
    body = U8(version) + U8(kind) + UUID(request) + string(sender) + UUID(instance) + payload
    return U32(len(body)) + body


rows = []


def add(name, data, outcome="accept", stage="codec", error=None, **fields):
    stem = "tcp-" + name
    (OUT / (stem + ".bin")).write_bytes(data)
    (OUT / (stem + ".hex")).write_text("\n".join(data[i:i+16].hex(" ") for i in range(0, len(data), 16)) + "\n")
    expected = {"outcome": outcome, "stage": stage, "fields": fields}
    if error:
        expected["error"] = error
    rows.append({"id": stem, "binary": stem + ".bin", "hex": stem + ".hex", "bytes": len(data),
                 "sha256": hashlib.sha256(data).hexdigest(), "expected": expected})



# Original independent literals remain frozen; only four explicitly approved mask vectors are rebuilt.
import shutil
BASE=ROOT.parents[1]/"construction/034-overlay-extra/protocol-v2-proposal"
changed={"tcp-hello","tcp-hello-ok","tcp-invalid-capability-not-required","tcp-invalid-capability-reserved"}
for row in json.loads((BASE/"manifest.json").read_text())["fixtures"]:
    if row["id"] in changed:continue
    for key in ["binary","hex"]:shutil.copyfile(BASE/"fixtures"/row[key],OUT/row[key])
    rows.append(row)
add("hello",frame(5,endpoint()+U32(3)+U32(3)),type=5,supported=3,required=3,endpointId="S1",endpointInstance=INST)
add("hello-ok",frame(6,U32(3),sender="C"),type=6,selected=3,bodyLength=41)
for supported,required,suffix in [(3,0,"not-required"),(7,3,"reserved")]:
    add("invalid-capability-"+suffix,frame(5,endpoint()+U32(supported)+U32(required)),"reject","handshake","INVALID_MESSAGE")
maximum_host="a"*63+"."+"b"*63+"."+"c"*63+"."+"d"*61
minimum=frame(20,endpoint("A",INST,"a",1,1,2)+U32(3)+U32(3),sender="A")
maximum=frame(20,endpoint("N"*32,INST,maximum_host,65535,65535,2)+U32(3)+U32(3),sender="N"*32)
add("repair-hello-min",minimum,type=20,supported=3,required=3)
add("repair-hello-max",maximum,type=20,supported=3,required=3)
for supported,required,suffix in [(1,1,"discovery-only"),(0,0,"mandatory-missing"),(7,3,"reserved-mask")]:
    add("repair-hello-"+suffix,frame(20,endpoint()+U32(supported)+U32(required)),"reject","handshake","INVALID_MESSAGE")
add("repair-hello-zero-request",frame(20,endpoint()+U32(3)+U32(3),request=ZERO),"reject","codec","INVALID_MESSAGE")
add("repair-hello-truncated-endpoint",frame(20,string("S1")+UUID(INST)+U16(5)+b"ab"),"reject","codec","INVALID_MESSAGE")
add("repair-hello-invalid-role",frame(20,endpoint(role=255)+U32(3)+U32(3)),"reject","codec","INVALID_MESSAGE")
add("repair-hello-length-small",U32(36)+bytes([2,20])+b"\x00"*34,"reject","framing","INVALID_MESSAGE")
add("repair-hello-length-above-global-cap",U32(65537),"reject","framing","RESOURCE_LIMIT")
add("repair-hello-unknown-type21",frame(21),"reject","codec","INVALID_MESSAGE")
add("repair-hello-duplicate-owner",minimum,type=20,ownerExpected="DUPLICATE_ID")
assert len(rows)==62
manifest={"fixtureFormat":"ott-tcp-v2-repair-reference","version":2,"activeTypes":[1,2,*range(4,21)],"retiredType":3,
    "generatedBy":"literal Python struct/uuid, no production Java codec; 46 original independent vectors preserved",
    "fixtures":rows}
(ROOT/"manifest.json").write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+"\n")
print("Independent TCP references:",len(rows),"new=12 migrated=4 preserved=46")
