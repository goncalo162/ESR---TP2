#!/usr/bin/env python3
"""Drive the approved V-009 commands in an authenticated local terminal."""
import argparse
from datetime import datetime, timezone
import json
import os
from pathlib import Path
import re
import signal
import subprocess
import sys
import time

from bench import active_capture

BENCH = Path(__file__).with_name("bench.py").resolve()
BASE = [sys.executable, "-B", str(BENCH)]
NODES = {"SERVER": "N0", "RELAY": "N1", "CLIENT": "N2"}
SESSION = re.compile(r"NeighborUp\[session=Session\[nodeId=(\w+), instanceId=([\w-]+), connectionId=([\w-]+)")


def require(condition, description):
    if not condition:
        raise RuntimeError(description)


def read_json(path):
    return json.loads(path.read_text())


class Trial:
    def __init__(self, run):
        self.run = run
        self.children = {}
        self.files = []

    def log(self, **entry):
        with (self.run / "technical-actions.jsonl").open("a") as output:
            output.write(json.dumps({"at": datetime.now(timezone.utc).isoformat(), **entry}) + "\n")

    def command(self, verb, *args, check=True):
        cmd = [*BASE, verb, str(self.run), *args]
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=45)
        self.log(command=cmd, exit=result.returncode, stdout=result.stdout, stderr=result.stderr)
        if check:
            require(result.returncode == 0, f"{verb} failed: {result.stderr or result.stdout}")
        return result

    def start(self, label, verb, *args):
        cmd = [*BASE, verb, str(self.run), *args]
        output = (self.run / f"{label}-controller.log").open("xb")
        self.files.append(output)
        child = subprocess.Popen(cmd, stdin=subprocess.PIPE if verb == "node" else subprocess.DEVNULL,
                                 stdout=output, stderr=subprocess.STDOUT)
        self.children[label] = child
        self.log(command=cmd, controller_pid=child.pid)
        return child

    def wait(self, description, predicate, labels=(), seconds=12):
        deadline = time.monotonic() + seconds
        while time.monotonic() < deadline:
            for label in labels:
                code = self.children[label].poll()
                require(code is None, f"{label} exited {code} waiting for {description}")
            try:
                value = predicate()
            except (FileNotFoundError, json.JSONDecodeError):
                value = None
            if value:
                self.log(observed=description)
                return value
            time.sleep(0.05)
        raise RuntimeError(f"Timed out waiting for {description}")

    def text(self, role):
        return (self.run / f"{role}.log").read_text(errors="replace")

    def send(self, role, message):
        child = self.children[role]
        require(child.poll() is None, f"{role} is not running")
        self.log(role=role, input=message)
        child.stdin.write((message + "\n").encode())
        child.stdin.flush()

    def quit(self, role):
        self.send(role, "quit")
        child = self.children[role]
        require(child.wait(timeout=10) == 0, f"{role} wrapper exit was nonzero")
        record = read_json(self.run / f"{role}.exit.json")
        require(record["java_exit"] == 0, f"{role} Java exit was nonzero")
        self.log(role=role, java_exit=record["java_exit"], java_pid=record["pid"])

    def stop_capture(self):
        capture = self.children["capture"]
        identity = read_json(self.run / "capture-start.json")
        fd = os.pidfd_open(identity["pid"])
        try:
            current = active_capture(self.run)
            require(current["pid"] == identity["pid"]
                    and current["proc_start"] == identity["proc_start"],
                    "Capture identity changed before stop")
            signal.pidfd_send_signal(fd, signal.SIGINT)
            self.log(capture_stop_signal="SIGINT", tcpdump_pid=current["pid"])
        finally:
            os.close(fd)
        return capture.wait(timeout=15)

    def sessions(self, role):
        return {peer: (instance, connection) for peer, instance, connection in SESSION.findall(self.text(role))}

    def snapshot(self, role, active_nodes):
        offset = len(self.text(role))
        self.send(role, "neighbors")
        def matches():
            tail = self.text(role)[offset:]
            if "NEIGHBORS " not in tail:
                return False
            active = re.findall(r"^NEIGHBOR node=(\w+) state=ACTIVE\b", tail, re.M)
            return sorted(active) == sorted(active_nodes)
        self.wait(f"{role} neighbors ACTIVE {sorted(active_nodes)}", matches, [role, "capture"])

    def observe(self):
        self.command("probe")
        self.command("ports", "before")
        self.start("capture", "capture")
        self.wait("capture identity", lambda: read_json(self.run / "capture-start.json"), ["capture"])
        for role in ("BOOTSTRAP", "CLIENT", "RELAY"):
            self.start(role, "node", role)
            self.wait(f"{role} READY", lambda: "READY " in self.text(role), [role, "capture"])
        self.command("mark", "client-relay-ready-server-absent")
        self.start("SERVER", "node", "SERVER")
        for role, node in NODES.items():
            peers = set(NODES.values()) - {node}
            self.wait(f"{role} two NeighborUp events", lambda: set(self.sessions(role)) == peers,
                      [role, "capture"])
            self.snapshot(role, peers)
        before = {role: self.sessions(role) for role in NODES}
        self.log(sessions_before=before)
        self.command("mark", "converged")
        self.command("observe")
        self.quit("BOOTSTRAP")
        self.command("mark", "bootstrap-off")
        pair = json.loads(self.command("pair").stdout)
        offsets = {role: len(self.text(role)) for role in NODES}
        self.command("cut", str(pair["source_port"]))
        for role, peer in (("SERVER", "N1"), ("RELAY", "N0")):
            self.wait(f"{role} TRANSPORT then new session with {peer}",
                      lambda: re.search(rf"NeighborDown\[session=Session\[nodeId={peer},[^\n]*reason=TRANSPORT",
                                        self.text(role)[offsets[role]:])
                      and self.sessions(role).get(peer, (None, None))[1] != before[role][peer][1],
                      [role, "capture"])
        after = {role: self.sessions(role) for role in NODES}
        for role, peers in before.items():
            require(after[role].keys() == peers.keys(), f"{role} neighbor set changed")
            for peer, (instance, old_connection) in peers.items():
                new_instance, new_connection = after[role][peer]
                changed = (role, peer) in {("SERVER", "N1"), ("RELAY", "N0")}
                require(new_instance == instance and (new_connection != old_connection) == changed,
                        f"Unexpected session change for {role}/{peer}")
        self.log(sessions_after=after)
        for role, node in NODES.items():
            self.snapshot(role, set(NODES.values()) - {node})
        self.command("mark", "reconnected")
        offsets = {role: len(self.text(role)) for role in ("SERVER", "RELAY")}
        self.command("mark", "client-quit")
        self.quit("CLIENT")
        for role, peer in (("SERVER", "N1"), ("RELAY", "N0")):
            self.wait(f"{role} CLIENT LEAVE",
                      lambda: re.search(r"NeighborDown\[session=Session\[nodeId=N2,[^\n]*reason=LEAVE",
                                        self.text(role)[offsets[role]:]),
                      [role, "capture"])
            self.snapshot(role, [peer])
            require(self.sessions(role)[peer] == after[role][peer], f"{role} session changed after LEAVE")
        for role in ("RELAY", "SERVER"):
            self.quit(role)
        self.command("ports", "after", check=False)
        self.command("mark", "scenario-ended")
        capture = self.children["capture"]
        require(capture.poll() is None, "Capture stopped before the end mark")
        capture_exit = self.stop_capture()
        self.log(capture_controller_exit=capture_exit)
        capture_log = (self.run / "capture.log").read_text()
        require(capture_exit == 0 and "CAPTURE_EXIT 0" in capture_log
                and re.search(r"^0 packets dropped by kernel$", capture_log, re.M)
                and "CAPTURE_END " in capture_log,
                "Capture completion or packet loss needs review")
        require((self.run / "control.pcap").stat().st_size > 24, "Capture has no packets")

    def cleanup(self):
        for role, child in reversed(list(self.children.items())):
            if child.poll() is None:
                try:
                    if role == "capture":
                        if (self.run / "capture-start.json").is_file():
                            self.stop_capture()
                        else:
                            child.send_signal(signal.SIGINT)
                    else:
                        self.send(role, "quit")
                    child.wait(timeout=12)
                except (OSError, RuntimeError, ValueError, subprocess.TimeoutExpired):
                    child.terminate()
                    try:
                        child.wait(timeout=5)
                    except subprocess.TimeoutExpired:
                        child.kill()
                        child.wait(timeout=5)
            if child.stdin is not None:
                child.stdin.close()
        for output in self.files:
            output.close()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--java", required=True)
    parser.add_argument("--operator", default="Codex (roteiro automático iniciado por Luis)",
                        help="name of the person running and checking an independent repetition")
    args = parser.parse_args()
    require(os.geteuid() != 0, "Run this controller as the normal user")
    subprocess.run(["sudo", "-n", "-v"], check=True)
    initialized = subprocess.run([*BASE, "init", "--operator", args.operator,
                                  "--java", args.java], capture_output=True, text=True, check=True, timeout=10)
    run = Path(initialized.stdout.strip())
    print(run, flush=True)
    trial = Trial(run)
    try:
        trial.observe()
    except (OSError, ValueError, RuntimeError, subprocess.SubprocessError, KeyboardInterrupt) as error:
        trial.log(scenario="incomplete", error=str(error))
        print(f"Ensaio incompleto: {error}", file=sys.stderr)
        return 1
    else:
        trial.log(scenario="actions-completed", capture_analysis="pending")
        print("Ações concluídas. A análise da captura fica pendente.")
        return 0
    finally:
        trial.cleanup()


if __name__ == "__main__":
    sys.exit(main())
