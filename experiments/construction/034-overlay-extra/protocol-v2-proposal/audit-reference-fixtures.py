"""Independent offline inspection of proposed vectors; not a Java/runtime validation."""
from pathlib import Path
import datetime
import hashlib
import json
import re
import uuid

ROOT = Path(__file__).resolve().parent


class Invalid(Exception):
    def __init__(self, code, recoverable=False):
        self.code, self.recoverable = code, recoverable


class Cursor:
    def __init__(self, data):
        self.data, self.pos = data, 0

    def take(self, n):
        if n < 0 or self.pos + n > len(self.data):
            raise Invalid("INVALID_MESSAGE")
        b = self.data[self.pos:self.pos+n]
        self.pos += n
        return b

    def uint(self, n): return int.from_bytes(self.take(n), "big")
    def ident(self): return str(uuid.UUID(bytes=self.take(16)))

    def text(self, maximum, node=False, content=False):
        n = self.uint(2)
        if n > maximum: raise Invalid("RESOURCE_LIMIT")
        raw = self.take(n)
        try: value = raw.decode("utf-8", "strict")
        except UnicodeDecodeError: raise Invalid("INVALID_MESSAGE", content)
        if "\0" in value or (node and not re.fullmatch(r"[A-Za-z0-9_-]{1,32}", value)):
            raise Invalid("INVALID_MESSAGE", content)
        return value

    def path(self):
        n = self.uint(2)
        if n > 32: raise Invalid("RESOURCE_LIMIT")
        return [self.text(32, node=True) for _ in range(n)]

    def endpoint(self, zero=False, content=False):
        node = self.text(32, node=True, content=content)
        instance = self.ident()
        host = self.text(253, content=content)
        control, data, role = self.uint(2), self.uint(2), self.uint(1)
        if bool(uuid.UUID(instance).int == 0) != zero or not host or not host.isascii() or not control or not data or role not in [1, 2, 3]:
            raise Invalid("INVALID_MESSAGE", content)
        return node, instance

    def nonnegative(self):
        x = int.from_bytes(self.take(8), "big", signed=True)
        if x < 0: raise Invalid("INVALID_MESSAGE")
        return x


def inspect(data):
    if len(data) < 4: raise Invalid("EOFException")
    size = int.from_bytes(data[:4], "big")
    if size < 37: raise Invalid("INVALID_MESSAGE")
    if size > 65536: raise Invalid("RESOURCE_LIMIT")
    if len(data) < size + 4: raise Invalid("EOFException")
    if len(data) > size + 4: raise Invalid("INVALID_MESSAGE")
    c = Cursor(data[4:])
    version = c.uint(1)
    if version != 2: raise Invalid("UNSUPPORTED_VERSION")
    kind = c.uint(1)
    if kind not in [1, 2, *range(4, 20)]: raise Invalid("INVALID_MESSAGE")
    request, sender, instance = c.ident(), c.text(32, node=True), c.ident()
    fields = {"version": version, "type": kind, "bodyLength": size, "requestId": request, "senderId": sender, "senderInstance": instance}
    if kind in [18, 19] and size > 32768: raise Invalid("RESOURCE_LIMIT")
    if kind in [1, 5]:
        fields["endpointId"], fields["endpointInstance"] = c.endpoint()
        if kind == 5:
            fields["supported"], fields["required"] = c.uint(4), c.uint(4)
            if fields["supported"] != 1 or fields["required"] != 1: raise Invalid("INVALID_MESSAGE")
    elif kind == 2:
        count = c.uint(2)
        if count > 64: raise Invalid("RESOURCE_LIMIT")
        endpoints = [c.endpoint(zero=True) for _ in range(count)]
        fields["neighborIds"], fields["neighborInstances"] = [p[0] for p in endpoints], [p[1] for p in endpoints]
    elif kind == 4:
        fields["reason"] = c.uint(1)
        if fields["reason"] not in [0, 1]: raise Invalid("INVALID_MESSAGE")
    elif kind == 6:
        fields["selected"] = c.uint(4)
        if fields["selected"] != 1: raise Invalid("INVALID_MESSAGE")
    elif kind in [7, 8]: fields["probeId"] = c.nonnegative()
    elif kind == 9:
        fields["streamId"] = c.ident()
        c.text(32, node=True)
        fields["title"] = c.text(512)
        if not 1 <= len(fields["title"]) <= 128: raise Invalid("INVALID_MESSAGE")
        if c.uint(1) != 1 or not 1 <= c.uint(2) <= 60: raise Invalid("INVALID_MESSAGE")
        c.ident()
        fields["announceSeq"], fields["costMicros"], fields["path"] = c.nonnegative(), c.nonnegative(), c.path()
    elif kind == 10:
        fields["streamId"] = c.ident(); c.ident(); fields["announceSeq"] = c.nonnegative()
    elif kind in [11, 12, 13, 14]:
        c.ident(); c.ident(); c.ident(); fields["revision"] = c.nonnegative()
        if kind == 12: fields["grantedLeaseMs"] = c.uint(4)
        if kind in [11, 14]: fields["leaseMs"], fields["expectedPath"] = c.uint(4), c.path()
    elif kind == 15:
        c.ident(); c.ident(); c.ident(); fields["ttl"], fields["visited"] = c.uint(1), c.path()
    elif kind == 16:
        c.ident(); c.ident(); fields["visited"] = c.path()
    elif kind == 17:
        fields["code"], fields["relatedType"], fields["detail"] = c.uint(2), c.uint(1), c.text(256)
        if fields["code"] not in range(1, 13): raise Invalid("INVALID_MESSAGE")
    elif kind in [18, 19]:
        fields["helloRequestId"], fields["recipientId"], fields["recipientInstance"] = c.ident(), c.text(32, node=True), c.ident()
        if kind == 19:
            fields["generation"], count = int.from_bytes(c.take(8), "big", signed=True), c.uint(2)
            if count > 63: raise Invalid("RESOURCE_LIMIT", True)
            endpoints = [c.endpoint(content=True) for _ in range(count)]
            fields["candidateIds"] = [p[0] for p in endpoints]
            if fields["generation"] <= 0 or len(set(fields["candidateIds"])) != count:
                raise Invalid("INVALID_MESSAGE", True)
    if c.pos != len(c.data): raise Invalid("INVALID_MESSAGE", kind in [18, 19])
    return fields


manifest = json.loads((ROOT / "manifest.json").read_text())
rows = []
accepted_types = set()
for row in manifest["fixtures"]:
    raw = (ROOT / "fixtures" / row["binary"]).read_bytes()
    assert len(raw) == row["bytes"] and hashlib.sha256(raw).hexdigest() == row["sha256"], row["id"]
    assert bytes.fromhex((ROOT / "fixtures" / row["hex"]).read_text()) == raw, row["id"]
    expected = row["expected"]
    try:
        fields = inspect(raw)
    except Invalid as failure:
        observed = "reject-content" if failure.recoverable else "reject"
        assert (observed, failure.code) == (expected["outcome"], expected["error"]), (row["id"], observed, failure.code)
        rows.append({"id": row["id"], "outcome": observed, "error": failure.code, "integrity": "passed"})
    else:
        assert expected["outcome"] == "accept", row["id"]
        for key, value in expected["fields"].items(): assert fields[key] == value, (row["id"], key)
        accepted_types.add(fields["type"])
        rows.append({"id": row["id"], "outcome": "accept", "fields": fields, "integrity": "passed"})
assert accepted_types == {1, 2, *range(4, 20)}
result = {"recorded_at": datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
          "command": "python3 experiments/construction/034-overlay-extra/protocol-v2-proposal/audit-reference-fixtures.py",
          "scope": "independent Python structural/semantic inspection of proposed reference fixtures; not Java codec/tests/transport execution",
          "fixtures": len(rows), "active_types_covered": sorted(accepted_types), "passed": True,
          "conditional_content_recovery": "requires owner validation of current session; this offline audit cannot establish session authorization",
          "results": rows}
(ROOT / "audit.json").write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n")
print(json.dumps({"fixtures": len(rows), "types": len(accepted_types), "passed": True, "java_executed": False}))
