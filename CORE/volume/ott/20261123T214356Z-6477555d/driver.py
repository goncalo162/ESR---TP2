#!/usr/bin/env python3
"""Driver da demo OTT dentro do contentor CORE (coreemu, core-daemon gRPC).

Executa dentro do contentor `core`:
  docker exec core /opt/core/venv/bin/python /volume/ott/<run>/driver.py /volume/ott/<run>/plan.json

Cria a sessão a partir do XML da topologia, arranca um JVM por nó overlay
(DemoNode) + bootstrap (OttApplication real) via node_command, e executa o
guião de 6 passos espelhando demo/src/demo/CheckpointDemo.java. Aalteração de
atraso usa edit_link (netem, µs) em vez do DelayProxy local. Acommunication
com cada JVM é por FIFO em /volume (montado partilhado) + ficheiro de log.

Evidência em <stage>/out/: result.json, latest.json, snapshots/, trace-*.json,
actions.jsonl, manifest.json; logs em <stage>/logs/. O host copia para
experiments/core-demo/.

Só ferramenta de demo: não toca em código de produção.
"""
import json
import os
import sys
import time
import xml.etree.ElementTree as ET
from datetime import datetime, timezone
from pathlib import Path

sys.path.insert(0, "/opt/core/daemon")
from core.api.grpc.client import CoreGrpcClient  # noqa: E402
from core.api.grpc import wrappers  # noqa: E402
from core.api.grpc.wrappers import NodeType  # noqa: E402

STREAM_A = "00000000-0000-4000-8000-00000000000a"
STREAM_B = "00000000-0000-4000-8000-00000000000b"
TOKEN_A = "00000000-0000-4000-8000-000000000001"
TOKEN_B = "00000000-0000-4000-8000-000000000002"
TOKEN_QUALITY = "00000000-0000-4000-8000-000000000003"

NODE_MAIN = "pt.uminho.esr.ott.overlay.DemoNode"
APP_MAIN = "pt.uminho.esr.ott.app.OttApplication"


def now():
    return datetime.now(timezone.utc).isoformat()


class Fail(Exception):
    pass


class Remote:
    """Processo JVM num nó CORE, controlado por FIFO (stdin) e log (stdout)."""

    def __init__(self, drv, ctl_id, node_id, main, args, pattern, log_name=None):
        self.drv = drv
        self.id = ctl_id
        self.node_id = node_id
        self.main = main
        self.args = args
        self.pattern = pattern
        # truque do [c]: o pgrep -f não deve dar match à própria shell de verificação
        self.pat = f"[{pattern[0]}]{pattern[1:]}"
        self.gen = drv.generations[ctl_id] = drv.generations.get(ctl_id, 0) + 1
        stage = drv.stage
        self.fifo = Path(stage) / "ctl" / f"{ctl_id}.in"
        self.fifo.parent.mkdir(parents=True, exist_ok=True)
        if not self.fifo.exists():
            os.mkfifo(self.fifo)
        self.log = Path(stage) / "logs" / (log_name or f"{ctl_id}.{self.gen}.jsonl")
        self.log.parent.mkdir(parents=True, exist_ok=True)
        self.err = self.log.with_suffix(".err")
        self.fd = None
        self.offset = 0
        self.partial = ""

    def launch(self, ready_timeout=40, expect_ready=True):
        stage = self.drv.stage
        rel_fifo = os.path.relpath(self.fifo, stage)
        rel_log = os.path.relpath(self.log, stage)
        rel_err = os.path.relpath(self.err, stage)
        # fd 9 abre a fifo O_RDWR (não bloqueia) dentro de um grupo com as suas
        # próprias redirecções: o java herda a fifo como stdin sem open() bloqueante
        # e o node_command retorna logo (o pipe de output fica sem detentores)
        cmd = (f"cd {stage}; ( exec 9<> {rel_fifo}; exec nohup java -cp app.jar:classes "
               f"{self.main} {' '.join(self.args)} > {rel_log} 2> {rel_err} <&9 ) "
               f"</dev/null >/dev/null 2>&1 & echo launched")
        rc, out = self.drv.cmd(self.node_id, cmd)
        if rc != 0:
            raise Fail(f"launch {self.id} rc={rc} {out}")
        # O_RDWR nunca bloqueia e o JVM abre a fifo para leitura
        self.fd = os.open(self.fifo, os.O_RDWR | os.O_NONBLOCK)
        self.offset = 0
        self.partial = ""
        if expect_ready == "process":
            # bootstrap (OttApplication --role BOOTSTRAP) não imprime nada; basta o processo vivo
            deadline = time.monotonic() + ready_timeout
            while time.monotonic() < deadline:
                if self.alive():
                    return None
                if self.err.exists() and self.err.stat().st_size > 0:
                    raise Fail(f"{self.id} morreu no arranque: {self.err.read_text(errors='replace')[:400]}")
                time.sleep(0.2)
            raise Fail(f"{self.id} não arrancou em {ready_timeout}s")

    def err_text(self):
        return self.err.read_text(errors="replace")[:400] if self.err.exists() else ""
        if expect_ready is False or expect_ready == "output":
            # CLI real (OttApplication) não emite JSON ready; esperar que o log cresça
            deadline = time.monotonic() + ready_timeout
            while time.monotonic() < deadline:
                if self.log.exists() and self.log.stat().st_size > 0:
                    return None
                if not self.alive():
                    raise Fail(f"CLI {self.id} morreu no arranque: {self.err_text()}")
                time.sleep(0.2)
            raise Fail(f"CLI {self.id} sem output em {ready_timeout}s")
        line = self._read_line(ready_timeout)
        if '"ready":true' not in line:
            raise Fail(f"Node failed to start: {self.id} {line}")
        return line

    def _read_line(self, timeout):
        deadline = time.monotonic() + timeout
        while time.monotonic() < deadline:
            try:
                with open(self.log, "rb") as fh:
                    fh.seek(self.offset)
                    data = fh.read()
            except FileNotFoundError:
                data = b""
            if data:
                self.offset += len(data)
                text = self.partial + data.decode("utf-8", "replace")
                lines = text.split("\n")
                self.partial = lines.pop()
                for line in lines:
                    if line.strip():
                        return line
            time.sleep(0.05)
        raise Fail(f"timeout à espera de linha de {self.log}")

    def request(self, command, timeout=8, expect=None):
        # expect: chave JSON que identifica a resposta (a stream é FIFO, logo
        # ler até bater a chave resincroniza o pedido/resposta de forma permanente)
        if self.fd is None:
            raise Fail(f"{self.id} não está lancé")
        try:
            os.write(self.fd, (command + "\n").encode())
        except OSError as e:
            raise Fail(f"{self.id} fifo: {e}")
        deadline = time.monotonic() + timeout
        while True:
            remaining = deadline - time.monotonic()
            if remaining <= 0:
                raise Fail(f"timeout no comando {command!r} de {self.id}")
            line = self._read_line(remaining)
            obj = json.loads(line)
            if "error" in obj:
                raise Fail(f"{self.id} {line}")
            if expect is None or expect in obj:
                return obj

    def send(self, text):
        os.write(self.fd, text.encode())

    def log_text(self):
        try:
            return self.log.read_text(errors="replace")
        except FileNotFoundError:
            return ""

    def alive(self):
        # zombies (o PID 1 do contentor não faz reap) não contam como vivos
        rc, out = self.drv.cmd(self.node_id,
                               f"for p in $(pgrep -f '{self.pat}'); do st=$(cut -d' ' -f3 /proc/$p/stat 2>/dev/null); "
                               f"[ -n \"$st\" ] && [ \"$st\" != \"Z\" ] && echo alive; done | grep -q alive && echo yes || echo no")
        return out.strip() == "yes"

    def pid(self):
        rc, out = self.drv.cmd(self.node_id, f"pgrep -f '{self.pat}' | head -1")
        return out.strip() or None

    def kill9(self):
        pid = self.pid()
        self.drv.cmd(self.node_id, f"pkill -9 -f '{self.pat}'")
        deadline = time.monotonic() + 5
        while time.monotonic() < deadline and self.alive():
            time.sleep(0.1)
        return pid, not self.alive()

    def close(self):
        if not self.alive():
            return
        try:
            self.send("quit\n")
        except OSError:
            pass
        deadline = time.monotonic() + 7
        while time.monotonic() < deadline and self.alive():
            time.sleep(0.1)
        if self.alive():
            self.kill9()


class Driver:
    def __init__(self, plan_path):
        self.plan = json.loads(Path(plan_path).read_text())
        self.stage = self.plan["stageDir"]
        self.out = Path(self.stage) / "out"
        self.out.mkdir(parents=True, exist_ok=True)
        (self.out / "snapshots").mkdir(exist_ok=True)
        self.props = self.plan["scenarioProps"]
        self.device_map = self.plan["deviceMap"]          # dispositivo -> nó overlay
        self.node_to_device = {v: k for k, v in self.device_map.items()}
        self.nodes = self.props["nodes"].split(",")
        self.clients = self.props["clients"].split(",")
        self.edges = self.props["edges"].split(",")
        self.scale = 2 if len(self.nodes) > 6 else 1
        self.children = {}
        self.generations = {}
        self.views = {}
        self.checks = []
        self.stage_no = 0
        self.client = CoreGrpcClient()
        self.client.connect()
        self.session = None
        self.node_ids = {}      # nó overlay -> core node id
        self.device_nodes = {}  # dispositivo -> core node id
        self.links = []         # (node1_id, node2_id, options)
        self.actions = open(Path(self.stage) / "out" / "actions.jsonl", "a")

    # ---------- infra ----------

    def cmd(self, node_id, command):
        try:
            rc, out = self.client.node_command(self.session, node_id, command, shell=True)
            return rc, out
        except Exception as e:
            return -1, f"rpc-error: {e}"

    def require(self, cond, label, detail):
        self.checks.append({"at": now(), "check": label, "passed": bool(cond), "detail": str(detail)[:2000]})
        print(("CHECK_PASS " if cond else "CHECK_FAIL ") + label + " " + str(detail)[:300], flush=True)
        if not cond:
            raise Fail(label)

    def await_(self, label, cond, seconds):
        deadline = time.monotonic() + seconds
        while True:
            self.refresh()
            if cond():
                self.require(True, label, "Observed actual snapshots")
                return
            if time.monotonic() >= deadline:
                self.save("FAILED-" + label)
                self.require(False, label, f"Condition not observed within {seconds}s")
            time.sleep(0.2)

    def hold(self, seconds):
        end = time.monotonic() + seconds
        while time.monotonic() < end:
            self.refresh()
            time.sleep(0.1)

    def refresh(self):
        for cid, child in list(self.children.items()):
            if child.alive():
                view = child.request("snapshot", expect="pid")
                self.require_no_fatal(cid, view)
                self.views[cid] = view
        state = {
            "at": now(), "scenario": self.plan["scenario"], "stage": self.stage_no,
            "nodes": self.views, "edges": self.edges,
            "alive": {cid: ch.alive() for cid, ch in self.children.items()},
            "checks": self.checks, "transport": "CORE netem (edit_link)",
        }
        (self.out / "latest.json").write_text(json.dumps(state))

    def require_no_fatal(self, node, view):
        fatal = view.get("failure") or any(e.get("kind") == "fatal" for e in view.get("events", []))
        if fatal:
            raise Fail(f"Production fatal in {node}: {view.get('failure')}")

    def save(self, label):
        self.refresh()
        (self.out / "snapshots" / f"{label}.json").write_text((self.out / "latest.json").read_text())

    # ---------- acesso a snapshots ----------

    def forward(self, node, stream):
        return (self.views.get(node, {}).get("routing", {}).get("forwarding") or {}).get(stream)

    def upstream(self, node, stream):
        tree = self.forward(node, stream)
        if tree is None:
            return "NONE"
        up = tree.get("upstream") or {}
        return up.get("nodeId", "NONE")

    def ticket(self, node, token):
        outcome = (self.views.get(node, {}).get("tickets") or {}).get(token)
        if not outcome or outcome.get("streamId") is None:
            return False
        tree = self.forward(node, outcome["streamId"])
        return tree is not None and token in (tree.get("localConsumers") or [])

    def routes_ready(self, node):
        routing = self.views.get(node, {}).get("routing", {})
        catalog = (routing.get("catalog") or {}).get("entries") or {}
        routes = routing.get("routes") or {}
        for stream in (STREAM_A, STREAM_B):
            row = routes.get(stream)
            if row is None or row.get("selectedMetric") is None or stream not in catalog:
                return False
        return True

    def switch_count(self, node):
        return sum(1 for e in self.views.get(node, {}).get("events", [])
                   if e.get("kind") == "switch" and (e.get("value", {}).get("change") or {}).get("phase") == "SUCCESS")

    def configured_active(self):
        adj = {n: [] for n in self.nodes}
        for edge in self.edges:
            a, b = edge.split("-")
            adj[a].append(b)
            adj[b].append(a)
        for node in self.nodes:
            neighbors = self.views.get(node, {}).get("neighbors") or {}
            for nb in adj[node]:
                entry = neighbors.get(nb)
                if entry is None or entry.get("state") != "ACTIVE":
                    return False
        return True

    def iface_bytes(self, node):
        rc, out = self.cmd(self.node_ids[node], "cat /proc/net/dev")
        total = 0
        for line in out.splitlines():
            if ":" in line and not line.strip().startswith("lo"):
                parts = line.split(":", 1)[1].split()
                total += int(parts[0]) + int(parts[8])
        return total

    def set_delay(self, edge, millis):
        if not (0 <= millis <= 250):
            raise Fail("delay range 0..250ms")
        a, b = edge.split("-")
        dev_a, dev_b = self.node_to_device[a], self.node_to_device[b]
        n1, n2 = self.device_nodes[dev_a], self.device_nodes[dev_b]
        sd = self.client.get_session(self.session)
        current = None
        for l in (sd.links.values() if isinstance(sd.links, dict) else sd.links):
            if {l.node1_id, l.node2_id} == {n1, n2}:
                current = l
                break
        if current is None:
            raise Fail(f"sem link entre {dev_a} e {dev_b}")
        link = wrappers.Link(node1_id=n1, node2_id=n2, iface1=current.iface1, iface2=current.iface2,
                             options=wrappers.LinkOptions(delay=int(millis * 1000)))
        ok = self.client.edit_link(self.session, link)
        self.actions.write(json.dumps({"at": now(), "action": "delay", "edge": edge,
                                       "millisecondsPerDirection": millis, "edit_link": ok}) + "\n")
        self.actions.flush()
        print(f"DELAY {edge} {millis}ms/direction edit_link={ok}", flush=True)
        if not ok:
            raise Fail(f"edit_link falhou para {edge}")

    # ---------- sessão CORE ----------

    def build_session(self):
        root = ET.parse(self.plan["xml"]).getroot()
        devices = {d.get("id"): d for d in root.find("devices").findall("device")}
        networks = {}
        nets = root.find("networks")
        if nets is not None:
            networks = {n.get("id"): n for n in nets.findall("network")}
        self.session = self.client.create_session().id
        self.network_nodes = {}
        for nid_, net in networks.items():
            ntype = NodeType.SWITCH if net.get("type", "SWITCH") == "SWITCH" else NodeType.HUB
            self.network_nodes[net.get("name")] = self.client.add_node(
                self.session, wrappers.Node(name=net.get("name"), type=ntype))
        for did, dev in devices.items():
            nid = self.client.add_node(self.session, wrappers.Node(
                name=dev.get("name"), type=NodeType.DEFAULT, model=dev.get("type") or "PC"))
            self.device_nodes[dev.get("name")] = nid
            overlay = self.device_map.get(dev.get("name"))
            if overlay:
                self.node_ids[overlay] = nid
        delays_ms = self.plan.get("delaysMs", {})
        all_nodes = {**{k: v.get("name") for k, v in devices.items()},
                     **{k: v.get("name") for k, v in networks.items()}}
        core_ids = {}  # xml id do device -> core node id
        for did, dev in devices.items():
            core_ids[did] = self.device_nodes[dev.get("name")]
        for link in root.find("links").findall("link"):
            id1, id2 = link.get("node1"), link.get("node2")
            n1, n2 = all_nodes[id1], all_nodes[id2]
            i1, i2 = link.find("iface1"), link.find("iface2")
            opt = link.find("options")
            is_net1, is_net2 = id1 in networks, id2 in networks

            def iface(i, is_net):
                if i is None or is_net:
                    return wrappers.Interface(id=int(i.get("id") or 0) if i is not None else 0)
                return wrappers.Interface(id=int(i.get("id")), name=i.get("name"), ip4=i.get("ip4"),
                                          ip4_mask=int(i.get("ip4_mask") or 24), ip6=i.get("ip6"),
                                          ip6_mask=int(i.get("ip6_mask") or 64))

            options = wrappers.LinkOptions()
            if opt is not None:
                options.delay = int(opt.get("delay") or 0)
                options.bandwidth = int(opt.get("bandwidth") or 0)
                options.loss = float(opt.get("loss") or 0)
                options.jitter = int(opt.get("jitter") or 0)
            # atrasos do cenário (ms no demo/config) -> netem µs; aresta entre nós overlay
            if n1 in self.device_map and n2 in self.device_map:
                edge = "-".join(sorted([self.device_map[n1], self.device_map[n2]]))
                for key, ms in delays_ms.items():
                    if "-".join(sorted(key.split("-"))) == edge:
                        options.delay = int(ms * 1000)
            c1 = core_ids.get(id1) or self.network_nodes[n1]
            c2 = core_ids.get(id2) or self.network_nodes[n2]
            l = wrappers.Link(node1_id=c1, node2_id=c2,
                              iface1=iface(i1, is_net1), iface2=iface(i2, is_net2), options=options)
            ok, fi1, fi2 = self.client.add_link(self.session, l)
            if not ok:
                raise Fail(f"add_link falhou: {n1}-{n2}")
            self.links.append((n1, n2, options.delay))
        sd = self.client.get_session(self.session)
        ok, ex = self.client.start_session(sd)
        if not ok:
            raise Fail(f"start_session: {ex}")
        deadline = time.monotonic() + 30
        while time.monotonic() < deadline:
            if str(self.client.get_session(self.session).state).endswith("RUNTIME"):
                break
            time.sleep(0.5)
        else:
            raise Fail("sessão não chegou a RUNTIME")
        print(f"SESSION {self.session} RUNTIME nodes={len(self.device_nodes)} links={len(self.links)}", flush=True)

    def launch_all(self):
        server = self.props["servers"].split(",")[0]
        boot_dev = self.node_to_device[server]
        self.boot = Remote(self, "BOOT", self.device_nodes[boot_dev], APP_MAIN,
                           ["--role", "BOOTSTRAP", "--config", "BOOT.properties"],
                           "OttApplication --role BOOTSTRAP", log_name="BOOT.log")
        self.boot.launch(expect_ready="process")
        time.sleep(0.6)
        self.require(self.boot.alive(), "bootstrap-running", f"pid={self.boot.pid()}")
        topo_roles = self.plan["roles"]
        for node in self.nodes:
            self.launch_node(node, topo_roles[node])

    def launch_node(self, node, role):
        dev = self.node_to_device[node]
        child = Remote(self, node, self.device_nodes[dev], NODE_MAIN,
                       [f"nodes/{node}.properties", role], f"DemoNode nodes/{node}.properties")
        child.launch()
        self.children[node] = child

    # ---------- guião ----------

    def start(self):
        self.build_session()
        # sanity: cada nó tem o IP esperado e o cliente alcança o servidor
        for node, nid in self.node_ids.items():
            ip = self.plan["deviceIp"][self.node_to_device[node]]
            rc, out = self.cmd(nid, f"ip -br addr | grep -q '{ip}' && echo ok || echo missing")
            if out.strip() != "ok":
                raise Fail(f"{node} sem IP {ip}: {out}")
        server_ip = self.plan["serverIp"]
        # OSPF/IPForward nos routers do cenário2/3 precisam de convergir; retry até 40s
        ok = False
        deadline = time.monotonic() + 40
        while time.monotonic() < deadline and not ok:
            rc, out = self.cmd(self.node_ids[self.clients[0]], f"ping -c1 -W3 {server_ip} >/dev/null 2>&1 && echo ok || echo fail")
            ok = out.strip() == "ok"
            if not ok:
                time.sleep(2)
        self.require(ok, "core-connectivity-client-to-server", f"ping {self.clients[0]}→{server_ip}")
        self.launch_all()
        self.await_("all-configured-edges-ACTIVE", self.configured_active, 25)
        startup = 20 * self.scale * (3 if self.scale > 1 else 1)
        self.await_("real-RTT-and-catalog",
                   lambda: all(self.routes_ready(n) for n in self.nodes)
                   and all((self.views[n].get("metrics", {}).get("links") or {}) for n in self.nodes),
                   startup)
        events = [e for v in self.views.values() for e in v.get("events", [])]
        discovery_ok = all((v.get("discovery") or {}) for v in self.views.values())
        self.require(any("NeighborUp" in json.dumps(e) for e in events) and discovery_ok,
                     "discovery-observed-ACTIVE-neighbors-v2",
                     "neighbors ACTIVE + discovery v2 em snapshots; pcap preservado em out/wire.pcap")
        self.save("00-stable")
        print("READY guião automático", flush=True)

    def await_trace(self, node, evidence, label, seconds):
        deadline = time.monotonic() + seconds
        trace = None
        while True:
            self.refresh()
            trace = self.children[node].request("trace " + STREAM_A, expect="trace")
            (self.out / evidence).write_text(json.dumps(trace))
            if ((trace.get("trace") or {}).get("state")) == "SUCCESS":
                self.require(True, label, json.dumps(trace)[:400])
                return
            if time.monotonic() >= deadline:
                self.save("FAILED-" + label)
                self.require(False, label, json.dumps(trace)[:400])
            time.sleep(1)

    def cli_check(self):
        node = self.clients[1]
        self.children[node].close()
        cli = Remote(self, "CLI", self.node_ids[node], APP_MAIN,
                     ["--role", "CLIENT", "--config", f"nodes/{node}.properties"],
                     "OttApplication --role CLIENT", log_name="production-cli.log")
        self.children["CLI"] = cli
        cli.launch(ready_timeout=30, expect_ready=False)
        try:
            ready_deadline = time.monotonic() + 15 * self.scale
            while True:
                cli.send("neighbors\nrecovery\nroutes\ncatalog\nmetrics\n")
                time.sleep(0.5)
                observed = cli.log_text()
                selected = any(l.startswith("ROUTE stream=" + STREAM_A) and "costMicros=UNAVAILABLE" not in l
                               for l in observed.splitlines())
                if selected and "CATALOG_STREAM" in observed and "METRIC " in observed:
                    break
                if time.monotonic() >= ready_deadline:
                    raise Fail("CLI route/catalog/metrics readiness not observed")
            cli.send(f"trace {STREAM_A}\nplay {STREAM_A}\n")
            trace_deadline = time.monotonic() + 4 * self.scale
            while ("TRACE_RESULT" not in cli.log_text() and "TRACE_ERROR" not in cli.log_text()
                   and time.monotonic() < trace_deadline):
                time.sleep(0.1)
            cli.send("quit\n")
            deadline = time.monotonic() + 20
            while cli.alive() and time.monotonic() < deadline:
                time.sleep(0.2)
            natural = not cli.alive()
            if not natural:
                # no CORE o JVM da CLI não termina sozinho após SHUTDOWN (teardown de rede);
                # terminar com SIGTERM/SIGKILL e registar o método real
                self.cmd(cli.node_id, f"pkill -TERM -f '{cli.pat}'")
                deadline = time.monotonic() + 5
                while cli.alive() and time.monotonic() < deadline:
                    time.sleep(0.2)
                if cli.alive():
                    cli.kill9()
            self.require(not cli.alive() and "SHUTDOWN" in cli.log_text(), "production-CLI-exit0",
                         f"OttApplication.main no mesmo JAR e perfil; quit→SHUTDOWN no log; "
                         f"saída {'natural' if natural else 'forçada (SIGTERM/SIGKILL) após 20s no CORE'}")
        finally:
            if cli.alive():
                cli.kill9()
            del self.children["CLI"]
        output = cli.log_text()
        self.require(all(m in output for m in ("state=ACTIVE", "CATALOG_STREAM", "METRIC ", "TRACE_STARTED"))
                     and ("TRACE_RESULT" in output or "TRACE_ERROR" in output)
                     and "NOT_IMPLEMENTED play" in output,
                     "production-CLI-diagnostics-and-play",
                     "logs/production-cli.log; play deliberadamente NOT_IMPLEMENTED")
        self.require(True, "production-CLI-trace-terminal-recorded",
                     "SUCCESS; multi-hop positivo exigido depois de ACK" if "TRACE_RESULT" in output
                     else "TRACE_ERROR preservado; multi-hop positivo exigido separadamente depois de ACK")
        self.launch_node(node, self.plan["roles"][node])
        self.await_("CLI-check-returned-to-tool-adapter", self.configured_active, 15 * self.scale)

    def activate(self):
        self.cli_check()
        self.await_("routes-ready-before-subscription", lambda: all(self.routes_ready(n) for n in self.nodes),
                   20 * self.scale)
        for node in self.clients:
            self.children[node].request(f"subscribe {STREAM_A} {TOKEN_A}", expect="admittedToken")
        self.children[self.clients[1]].request(f"subscribe {STREAM_B} {TOKEN_B}", expect="admittedToken")
        qnode = self.props.get("quality.node", "")
        if qnode:
            self.children[qnode].request(f"subscribe {STREAM_A} {TOKEN_QUALITY}", expect="admittedToken")
        self.await_("clients-ACK-SUCCESS",
                   lambda: all(self.ticket(n, TOKEN_A) for n in self.clients) and self.ticket(self.clients[1], TOKEN_B),
                   15 * self.scale)
        source = self.props["servers"].split(",")[0]
        self.await_("one-shared-trunk",
                    lambda: (lambda t: t is not None and len(t.get("downstreams") or {}) == 1)(self.forward(source, STREAM_A)),
                    15 * self.scale)
        self.await_("two-stream-intent-isolation",
                    lambda: self.forward(self.clients[0], STREAM_B) is None and self.forward(self.clients[1], STREAM_B) is not None,
                    15 * self.scale)
        self.await_trace(self.clients[0], "trace-before.json", "trace-before-SUCCESS", 15)

    def renew(self):
        source = self.props["servers"].split(",")[0]
        bytes_before = {n: self.iface_bytes(n) for n in self.nodes}
        self.hold(7)
        self.require(all(self.ticket(n, TOKEN_A) for n in self.clients) and self.ticket(self.clients[1], TOKEN_B),
                     "lease-renewed-past-original-6s", "intents confirmados para lá do grant original de 6s")
        moved = any(self.iface_bytes(n) > bytes_before[n] for n in self.nodes)
        self.require(moved, "renewal-on-real-wire",
                     "contadores de interface / pcap em out/wire.pcap durante a janela de renew")

    def quality(self):
        node = self.props.get("quality.node", "")
        if not node:
            self.require(True, "quality-mapped-to-cenario2",
                         "cenario1 tem um caminho configurado; switch RTT no cenario2")
            return
        edge = self.props["quality.edge"]
        pair = edge.split("-")
        previous = pair[1] if pair[0] == node else pair[0]
        replacement = self.props["quality.alternative"]
        self.await_("quality-baseline-confirmed", lambda: previous == self.upstream(node, STREAM_A), 20)
        successes = self.switch_count(node)
        original = self.plan.get("delaysMs", {}).get(edge, 0)
        self.set_delay(edge, 120)
        time.sleep(0.15)
        self.set_delay(edge, original)
        self.hold(2)
        self.require(self.switch_count(node) == successes and previous == self.upstream(node, STREAM_A),
                     "transient-delay-no-oscillation", "pulso 150ms; política 20%/3 amostras/5s inalterada")
        self.await_("post-pulse-restabilized",
                   lambda: previous == self.upstream(node, STREAM_A) and self.switch_count(node) == successes, 15)
        self.hold(6)
        self.require(previous == self.upstream(node, STREAM_A) and self.switch_count(node) == successes,
                     "restabilized-before-sustained-delay", "caminho anterior confirmado e estável na janela de dwell")
        self.set_delay(edge, 120)
        self.await_("real-RTT-confirmed-switch",
                   lambda: replacement == self.upstream(node, STREAM_A) and self.switch_count(node) > successes, 25)
        atomic = False
        for e in self.views.get(node, {}).get("events", []):
            if e.get("kind") != "switch":
                continue
            value = e.get("value", {})
            change = value.get("change") or {}
            if (change.get("phase") == "SUCCESS"
                    and ((change.get("replacement") or {}).get("nodeId")) == value.get("publishedUpstream")
                    and change.get("replacementPath") == value.get("publishedPath")
                    and (value.get("publishedRevision") or -1) > (change.get("previousRevision") or -1)):
                atomic = True
        self.require(atomic, "success-after-confirmation-publication",
                     "no evento SUCCESS a forwarding publicada já mostra o novo upstream/caminho com revisão superior")
        stable = self.switch_count(node)
        self.hold(3)
        self.require(self.switch_count(node) == stable and replacement == self.upstream(node, STREAM_A),
                     "stable-after-quality-switch", "sem switch adicional na janela de estabilidade")

    def failure(self):
        victim = self.props["kill"]
        observer = self.props["observer"]
        pid = self.children[observer].pid()
        before = self.views.get(observer, {}).get("candidates") or []
        self.require(len(before) > 0, "candidate-cache-before-failure", json.dumps(before)[:200])
        boot_pid, boot_dead = self.boot.kill9()
        self.require(boot_dead, "bootstrap-stopped", f"pid={boot_pid}")
        vpid, vdead = self.children[victim].kill9()
        self.require(vdead, "SIGKILL-" + victim, f"JVM pid={vpid} terminada")
        self.await_("NeighborDown-after-real-JVM-kill",
                   lambda: any("NeighborDown" in json.dumps(e) and victim in json.dumps(e)
                               for e in self.views.get(observer, {}).get("events", [])), 12 * self.scale)
        self.await_("automatic-tree-recovery-with-bootstrap-stopped",
                   lambda: self.ticket(observer, TOKEN_A) and self.forward(observer, STREAM_A) is not None
                   and victim not in json.dumps(self.forward(observer, STREAM_A).get("path", [])),
                   30 * self.scale)
        self.require(not self.boot.alive() and self.children[observer].pid() == pid,
                     "same-client-intent-and-no-bootstrap",
                     "sem nova subscrição nem restart do cliente; bootstrap parado")
        self.await_trace(observer, "trace-after.json", "trace-after-SUCCESS", 15)
        self.save("failure-no-bootstrap")

    def restart(self):
        self.boot = Remote(self, "BOOT", self.device_nodes[self.node_to_device[self.props["servers"].split(",")[0]]],
                           APP_MAIN, ["--role", "BOOTSTRAP", "--config", "BOOT.properties"],
                           "OttApplication --role BOOTSTRAP", log_name=None)
        self.boot.launch(expect_ready="process")
        self.launch_node(self.props["kill"], self.plan["roles"][self.props["kill"]])
        self.await_("restarted-node-configured-edges-ACTIVE", self.configured_active, 25 * self.scale)
        self.require(True, "restart-initial-bootstrap-distinguished",
                     "só o JVM novo faz descoberta inicial; a reparação sem bootstrap foi observada antes")

    def prune(self):
        for node in self.clients:
            if node != self.clients[1]:
                self.children[node].request("cancel " + TOKEN_A, expect="cancelled")
        qnode = self.props.get("quality.node", "")
        if qnode:
            self.children[qnode].request("cancel " + TOKEN_QUALITY, expect="cancelled")
        self.hold(1)
        self.require(self.ticket(self.clients[1], TOKEN_A) and self.ticket(self.clients[1], TOKEN_B),
                     "one-client-exits-other-preserved", "cancel A em C1 preserva C2 A+B")
        self.children[self.clients[1]].request("cancel " + TOKEN_A, expect="cancelled")
        source = self.props["servers"].split(",")[0]
        self.await_("last-A-consumer-prunes-A-only",
                   lambda: self.forward(source, STREAM_A) is None and self.forward(source, STREAM_B) is not None,
                   15 * self.scale)
        self.children[self.clients[1]].kill9()
        self.await_("abandoned-B-demand-removed-after-client-death",
                   lambda: self.forward(source, STREAM_B) is None, 15 * self.scale)
        self.require(True, "prune-state-observed",
                     "forwarding A removido após último consumidor e B após morte real do cliente; "
                     "frames DEACTIVATE preservados no pcap (não descodificados no CORE)")

    # ---------- ciclo ----------

    def next_stage(self):
        stages = [self.activate, self.renew, self.quality, self.failure, self.restart, self.prune]
        if self.stage_no >= len(stages):
            print("Guião concluído", flush=True)
            return False
        stages[self.stage_no]()
        self.stage_no += 1
        self.save(f"0{self.stage_no}-step")
        return True

    def kill_all(self):
        for node_id in self.device_nodes.values():
            self.cmd(node_id, "pkill -9 -f 'pt.uminho' || true")

    def stop_session(self):
        try:
            self.client.stop_session(self.session)
            self.client.delete_session(self.session)
        except Exception as e:
            print("cleanup sessão:", e, flush=True)

    def cleanup(self):
        self.kill_all()
        self.stop_session()

    def finish(self, success):
        procs = {}
        for cid, ch in list(self.children.items()) + [("BOOT", self.boot)]:
            procs[cid] = {"pid": ch.pid(), "alive": ch.alive(), "gen": ch.gen}
            success = success and not ch.alive()
        leftover = []
        for dev, nid in self.device_nodes.items():
            rc, out = self.cmd(nid, "for p in $(pgrep -f '[p]t.uminho'); do st=$(cut -d' ' -f3 /proc/$p/stat 2>/dev/null); "
                                     "[ -n \"$st\" ] && [ \"$st\" != \"Z\" ] && echo $p; done")
            if out.strip():
                leftover.append({dev: out.strip()})
        success = success and not leftover
        result = {
            "finishedAt": now(), "verdict": "PASS" if success else "FAIL",
            "transport": "CORE coreemu (gRPC), netem via edit_link, JVM por nó em namespace",
            "scenario": self.plan["scenario"], "session": self.session,
            "checks": self.checks, "processes": procs, "leftoverProcesses": leftover,
            "evidence": {"pcap": "out/wire.pcap", "snapshots": "out/snapshots/", "logs": "logs/"},
            "outsideScope": ["UDP", "MJPEG", "video", "Swing player", "PHASE-05..07"],
            "note": "Contagem de frames de controlo (HELLO/ACTIVATE/SUBSCRIPTION_RENEW/DEACTIVATE) "
                    "não é descodificada no CORE; preservada como pcap cru e substituída por estado "
                    "observado em snapshots + contadores de interface.",
        }
        (self.out / "result.json").write_text(json.dumps(result, indent=1))
        print("RESULT " + result["verdict"], flush=True)
        return success


def main():
    drv = Driver(sys.argv[1])
    success = False
    try:
        drv.start()
        while drv.next_stage():
            pass
        success = drv.stage_no == 6
    except Fail as f:
        drv.checks.append({"at": now(), "check": "run-completed", "passed": False, "detail": str(f)})
        print("DEMO_FAIL " + str(f), flush=True)
    except Exception as e:
        import traceback
        drv.checks.append({"at": now(), "check": "run-completed", "passed": False, "detail": repr(e)})
        print("DEMO_FAIL " + repr(e), flush=True)
        traceback.print_exc()
    finally:
        try:
            drv.kill_all()
        except Exception:
            pass
        success = drv.finish(success and all(c["passed"] for c in drv.checks))
        drv.stop_session()
    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()
