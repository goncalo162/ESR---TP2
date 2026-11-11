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


# All 18 active types. Declared fields are literal semantic oracles, not decoder output.
add("register", frame(1, endpoint()), type=1, senderId="S1", endpointId="S1", endpointInstance=INST)
add("register-ok", frame(2, U16(1) + endpoint(instance=ZERO), sender="B"),
    type=2, senderId="B", neighborIds=["S1"], neighborInstances=[ZERO])
add("node-leave", frame(4, U8(0)), type=4, reason=0)
add("node-leave-min", frame(4, U8(0), sender="C"), type=4, senderId="C", bodyLength=38, reason=0)
add("hello", frame(5, endpoint() + U32(1) + U32(1)), type=5, supported=1, required=1, endpointId="S1", endpointInstance=INST)
add("hello-ok", frame(6, U32(1), sender="C"), type=6, selected=1, bodyLength=41)
add("probe-ping", frame(7, I64(42)), type=7, probeId=42)
add("probe-pong", frame(8, I64(42)), type=8, probeId=42)
for title, name in [("A", "stream-announce"), ("🎬", "stream-announce-utf8")]:
    payload = UUID(STREAM) + string("S1") + string(title) + U8(1) + U16(10) + UUID(EPOCH) + I64(1) + I64(0) + strings(["S1"])
    add(name, frame(9, payload), type=9, streamId=STREAM, title=title, announceSeq=1, costMicros=0, path=["S1"])
add("stream-withdraw", frame(10, UUID(STREAM) + UUID(EPOCH) + I64(1)), type=10, streamId=STREAM, announceSeq=1)
activation = UUID(STREAM) + UUID(EPOCH) + UUID(SUB) + I64(1) + U32(6000) + strings(["S1", "C1"])
add("activate", frame(11, activation, sender="C1"), type=11, revision=1, leaseMs=6000, expectedPath=["S1", "C1"])
add("activate-ack", frame(12, UUID(STREAM) + UUID(EPOCH) + UUID(SUB) + I64(1) + U32(6000)), type=12, revision=1, grantedLeaseMs=6000)
add("deactivate", frame(13, UUID(STREAM) + UUID(EPOCH) + UUID(SUB) + I64(1), sender="C1"), type=13, revision=1)
add("subscription-renew", frame(14, activation, sender="C1"), type=14, revision=1, leaseMs=6000, expectedPath=["S1", "C1"])
add("route-trace", frame(15, UUID(STREAM) + UUID(EPOCH) + UUID(HELLO) + U8(32) + strings(["C1"]), sender="C1"), type=15, ttl=32, visited=["C1"])
add("trace-result", frame(16, UUID(STREAM) + UUID(HELLO) + strings(["C1", "S1"])), type=16, visited=["C1", "S1"])
add("error-no-route", frame(17, U16(6) + U8(11) + string("no route")), type=17, code=6, relatedType=11, detail="no route")
add("error-discovery", frame(17, U16(1) + U8(19) + string("snapshot rejected")), type=17, code=1, relatedType=19, detail="snapshot rejected")
correlation = UUID(HELLO) + string("C") + UUID(OTHER)
add("discovery-query", frame(18, correlation), type=18, helloRequestId=HELLO, recipientId="C", recipientInstance=OTHER)
add("discovery-snapshot-empty", frame(19, correlation + I64(1) + U16(0)), type=19, generation=1, candidateIds=[])
add("discovery-snapshot", frame(19, correlation + I64(2) + U16(1) + endpoint("R1", OTHER, "10.0.0.2", 5002, 5003, 2)),
    type=19, generation=2, candidateIds=["R1"])

# Independently constructed framing and identity negatives, all with v2 except explicit version negatives.
add("invalid-short-prefix", bytes.fromhex("00 00 00"), "reject", "framing", "EOFException")
add("invalid-length-small", U32(36) + b"\x02" + b"\x00" * 35, "reject", "framing", "INVALID_MESSAGE")
add("invalid-length-large", U32(65537), "reject", "framing", "RESOURCE_LIMIT")
add("invalid-length-unsigned", U32(0xFFFFFFFF), "reject", "framing", "RESOURCE_LIMIT")
add("invalid-truncated-body", U32(38) + b"\x02" + b"\x00" * 36, "reject", "framing", "EOFException")
for version in [0, 1, 3, 255]:
    add("invalid-version-" + str(version), frame(6, U32(1), sender="C", version=version), "reject", "version", "UNSUPPORTED_VERSION")
add("invalid-retired-type", frame(3, I64(1)), "reject", "codec", "INVALID_MESSAGE")
add("invalid-type", frame(255), "reject", "codec", "INVALID_MESSAGE")
bad_identity = b"\x02\x06" + UUID(REQ) + U16(1) + b"\x80" + UUID(INST) + U32(1)
add("invalid-identity-utf8", U32(len(bad_identity)) + bad_identity, "reject", "identity", "INVALID_MESSAGE")
add("invalid-trailing-byte", frame(6, U32(1) + b"\x00", sender="C"), "reject", "codec", "INVALID_MESSAGE")
add("invalid-negative-probe", frame(7, I64(-1)), "reject", "codec", "INVALID_MESSAGE")
add("invalid-role", frame(1, endpoint(role=255)), "reject", "codec", "INVALID_MESSAGE")
add("invalid-path-count", frame(16, UUID(STREAM) + UUID(HELLO) + strings(["N" + str(i) for i in range(33)])), "reject", "codec", "RESOURCE_LIMIT")
for supported, required, suffix in [(1, 0, "not-required"), (0, 0, "missing"), (3, 1, "reserved")]:
    add("invalid-capability-" + suffix, frame(5, endpoint() + U32(supported) + U32(required)), "reject", "handshake", "INVALID_MESSAGE")
add("invalid-selection", frame(6, U32(0), sender="C"), "reject", "handshake", "INVALID_MESSAGE")

# Complete, delimited extra content failures: may be recoverable ONLY after owner validates current session identity.
add("snapshot-generation-zero", frame(19, correlation + I64(0) + U16(0)), "reject-content", "content", "INVALID_MESSAGE")
add("snapshot-duplicate", frame(19, correlation + I64(3) + U16(2) + endpoint("R1", OTHER) + endpoint("R1", OTHER)), "reject-content", "content", "INVALID_MESSAGE")
add("snapshot-invalid-role", frame(19, correlation + I64(3) + U16(1) + endpoint("R1", OTHER, role=255)), "reject-content", "content", "INVALID_MESSAGE")
add("snapshot-invalid-count", frame(19, correlation + I64(3) + U16(64)), "reject-content", "content", "RESOURCE_LIMIT")
add("snapshot-trailing", frame(19, correlation + I64(3) + U16(0) + b"\x00"), "reject-content", "content", "INVALID_MESSAGE")
add("snapshot-truncated-endpoint", frame(19, correlation + I64(3) + U16(1) + U16(2) + b"R"), "reject", "codec", "INVALID_MESSAGE")
add("snapshot-field-length", frame(19, correlation + I64(3) + U16(1) + U16(33)), "reject", "codec", "RESOURCE_LIMIT")
add("discovery-over-cap", frame(18, correlation + b"\x00" * 32768), "reject", "codec", "RESOURCE_LIMIT")

manifest = {
    "fixtureFormat": "ott-tcp-v2-proposed-reference-fixtures",
    "status": "design proposal; not run through Java production codec",
    "generatedBy": "build-reference-fixtures.py: literal semantic fields + Python struct; no Java codec or v1 binary/hex input",
    "contract": "../../../../memory-bank/bolts/034-overlay-extra/ddd-02-technical-design.md",
    "version": 2, "activeTypes": [1, 2, *range(4, 20)], "retiredType": 3,
    "notes": ["TCP only; UDP OTT1/version 1 and its 16 existing fixtures remain unchanged.",
              "reject-content is conditional on current authorized session; bad identity/framing remains terminal.",
              "Synthetic payloads test protocol structure, not media playback or operational availability."],
    "fixtures": rows,
}
(ROOT / "manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n")
print(json.dumps({"fixtures": len(rows), "accept": sum(r["expected"]["outcome"] == "accept" for r in rows),
                  "reject_content": sum(r["expected"]["outcome"] == "reject-content" for r in rows),
                  "reject": sum(r["expected"]["outcome"] == "reject" for r in rows)}))
