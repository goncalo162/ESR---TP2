"""Inspect contract bytes independently; does not invoke the Java codec."""
from collections import Counter
from datetime import datetime, timezone
import hashlib
import json
from pathlib import Path
import struct
import uuid

ROOT = Path(__file__).resolve().parents[3]
FIXTURES = ROOT / "Projeto/src/test/resources/protocol"
OUT = Path(__file__).resolve().parent
manifest = json.loads((FIXTURES / "manifest.json").read_text())
rows = []


def u16(data, offset):
    return struct.unpack_from(">H", data, offset)[0]


def string(data, offset):
    size = u16(data, offset)
    return data[offset + 2:offset + 2 + size].decode("utf-8", errors="strict"), offset + 2 + size


def envelope(data):
    sender, offset = string(data, 22)
    return {
        "bodyLength": struct.unpack_from(">I", data)[0],
        "version": data[4], "type": data[5],
        "requestId": str(uuid.UUID(bytes=data[6:22])), "senderId": sender,
        "senderInstance": str(uuid.UUID(bytes=data[offset:offset + 16])),
    }, offset + 16


def announcement_path_offset(data):
    offset = envelope(data)[1] + 16
    _, offset = string(data, offset)
    _, offset = string(data, offset)
    return offset + 1 + 2 + 16 + 8 + 8


for item in manifest["fixtures"]:
    data = (FIXTURES / item["binary"]).read_bytes()
    assert len(data) == item["bytes"], item["id"]
    assert hashlib.sha256(data).hexdigest() == item["sha256"], item["id"]
    assert bytes.fromhex((FIXTURES / item["hex"]).read_text()) == data, item["id"]
    row = {"id": item["id"], "bytes": len(data), "integrity": "pass",
           "declared_outcome": item["expected"]["outcome"]}
    if item["id"].startswith("tcp-") and item["expected"]["outcome"] == "accept":
        observed, offset = envelope(data)
        assert observed["bodyLength"] == len(data) - 4
        assert 37 <= observed["bodyLength"] <= 65536
        assert observed["version"] == 1 and observed["type"] in [1, 2, *range(4, 18)]
        for key, value in observed.items():
            assert item["expected"]["fields"][key] == value, (item["id"], key)
        row.update(envelope=observed, payload_offset=offset)
    if item["id"].startswith("udp-"):
        violations = []
        if len(data) < 64:
            violations.append("short-header")
        else:
            magic, version, flags, header, stream, session, seq, media, length, index, count = struct.unpack_from(
                ">4sBBH16s16sqqIHH", data)
            fields = {"magic": magic.decode("ascii"), "version": version, "flags": flags,
                      "headerLength": header, "streamId": str(uuid.UUID(bytes=stream)),
                      "sessionId": str(uuid.UUID(bytes=session)), "frameSeq": seq,
                      "mediaTimeUs": media, "frameLength": length, "fragmentIndex": index,
                      "fragmentCount": count, "payloadBytes": len(data) - 64}
            if magic != b"OTT1": violations.append("magic")
            if version != 1: violations.append("version")
            if flags != 0: violations.append("flags")
            if header != 64: violations.append("header-length")
            if not 1 <= length <= 2097152: violations.append("frame-length")
            if seq < 0: violations.append("sequence")
            if media < 0: violations.append("media-time")
            if not 1 <= count <= 1847 or count != (length + 1135) // 1136:
                violations.append("count")
            if not 0 <= index < count: violations.append("index")
            if not 65 <= len(data) <= 1200: violations.append("datagram-size")
            expected_payload = 1136 if index < count - 1 else length - 1136 * (count - 1)
            if len(data) - 64 != expected_payload: violations.append("payload-length")
            row["header_fields"] = fields
            if item["expected"]["outcome"] == "accept":
                assert fields == item["expected"]["fields"], item["id"]
        assert bool(violations) == (item["expected"]["outcome"] == "reject"), item["id"]
        row["contract_violations"] = violations
    rows.append(row)

tcp_invalid_checks = {
    "short-prefix": lambda b: len(b) < 4,
    "length-small": lambda b: struct.unpack_from(">I", b)[0] == 36,
    "length-large": lambda b: struct.unpack_from(">I", b)[0] == 65537,
    "truncated-body": lambda b: struct.unpack_from(">I", b)[0] == 37 and len(b) - 4 == 36,
    "version": lambda b: b[4] == 2,
    "type": lambda b: b[5] == 255,
    "retired-type": lambda b: b[5] == 3,
    "utf8": lambda b: b[24:25] == bytes([0x80]),
    "trailing-byte": lambda b: b[5] == 6 and len(b) > envelope(b)[1],
    "negative-probe": lambda b: struct.unpack_from(">q", b, envelope(b)[1])[0] == -1,
    "role": lambda b: b[-1] == 255,
    "path-count": lambda b: u16(b, announcement_path_offset(b)) == 33,
}
for suffix, predicate in tcp_invalid_checks.items():
    assert predicate((FIXTURES / ("tcp-invalid-" + suffix + ".bin")).read_bytes()), suffix

minimum = (FIXTURES / "tcp-hello-ok-min.bin").read_bytes()
hello = (FIXTURES / "tcp-hello.bin").read_bytes()
reply = (FIXTURES / "tcp-register-ok.bin").read_bytes()
announce = (FIXTURES / "tcp-stream-announce.bin").read_bytes()
unicode_announce = (FIXTURES / "tcp-stream-announce-utf8.bin").read_bytes()
activate = (FIXTURES / "tcp-activate.bin").read_bytes()
error = (FIXTURES / "tcp-error-no-route.bin").read_bytes()
assert len(minimum) == 41 and envelope(minimum)[1] == 41
assert len(hello) == 77 and string(hello, 42)[0] == "S1"
assert uuid.UUID(bytes=hello[46:62]).int != 0
assert string(hello, 62)[0] == "10.0.0.1"
assert (u16(hello, 72), u16(hello, 74), hello[76]) == (5000, 5001, 1)
assert len(reply) == 78 and u16(reply, 41) == 1
assert string(reply, 43)[0] == "S1" and reply[47:63] == bytes(16)
assert string(announce, 62)[0] == "A" and u16(announce, 100) == 1
assert string(unicode_announce, 62)[0] == "🎬" and u16(unicode_announce, 62) == 4
assert struct.unpack_from(">I", activate, 98)[0] == 6000 and u16(activate, 102) == 2
assert u16(error, 42) == 6 and error[44] == 11 and string(error, 45)[0] == "no route"
first = (FIXTURES / "udp-split-first.bin").read_bytes()[64:]
last = (FIXTURES / "udp-split-last.bin").read_bytes()[64:]
assert first + last == b"\xab" * 1136 + b"\xcd"
types = {row["envelope"]["type"] for row in rows if "envelope" in row}
assert types == {1, 2, *range(4, 18)} and len(rows) == 45
counts = Counter((item["id"].split("-")[0], item["expected"]["outcome"]) for item in manifest["fixtures"])
result = {
    "checked_at": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
    "status": "pass", "fixtures": len(rows), "active_tcp_types": sorted(types),
    "counts": {"/".join(k): v for k, v in counts.items()},
    "tcp_invalid_contract_observations": sorted(tcp_invalid_checks),
    "offset_samples": ["HELLO_OK", "HELLO", "REGISTER_OK", "ANNOUNCE", "ANNOUNCE UTF-8", "ACTIVATE", "ERROR"],
    "udp_split_payload_bytes": len(first + last), "rows": rows,
    "audit_note": "Initial audit assumed path.count at offset 100 for every ANNOUNCE. The invalid fixture has senderId N32 (3 bytes), so its field is at 101. Audit corrected to derive offsets from envelope/strings; no fixture/application change.",
    "scope": "Byte/hash/hex/offset audit independent of Java; UDP payload is synthetic, not a Java UDP codec or real video test.",
}
(OUT / "fixture-audit.json").write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n")
print("PASS: 45 fixtures; hash/hex/length; 16 active TCP types; seven TCP offset samples; all invalid contract observations; UDP split 1137 bytes.")
