#!/usr/bin/env python3
"""Record V-009 actions on the dedicated IPv4 loopback bench."""
import argparse
from datetime import datetime, timezone
import hashlib
import json
import os
from pathlib import Path
import pwd
import re
import select
import shutil
import signal
import socket
import subprocess
import sys
import tempfile
import time

HERE = Path(__file__).resolve().parent
JAR = HERE.parent / "construction/020-overlay/verified-jar/ott-streaming-1.0-SNAPSHOT.jar"
JAR_SHA256 = "303facafc148b96bd2cc0d98ea6afc7a773a50cb2b49d1d5c6c6c4c481de5b52"
PROFILES = {"BOOTSTRAP": "bootstrap.properties", "SERVER": "N0.properties",
            "RELAY": "N1.properties", "CLIENT": "N2.properties"}
NODE_PORTS = {"SERVER": 56110, "RELAY": 56120, "CLIENT": 56130}
TCP_PORTS = (56100, *NODE_PORTS.values())
UDP_PORTS = (56111, 56121, 56131)
LOOPBACK = "127.0.0.1"
MAIN = "pt.uminho.esr.ott.app.OttApplication"
BOOT_ID = Path("/proc/sys/kernel/random/boot_id")
CAPTURE_STATES = {"R", "S"}


def utc():
    return datetime.now(timezone.utc).isoformat(timespec="microseconds")


def save(path, value):
    with path.open("x") as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)
        stream.write("\n")


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def profile_hashes(directory):
    return {p.name: sha(p) for p in sorted(directory.glob("*.properties"))}


def check_java(java):
    result = subprocess.run([java, "-version"], capture_output=True, text=True, check=True, timeout=5)
    if not re.search(r'version "21(?:[."]|$)', result.stderr):
        raise ValueError("Use JDK 21 with --java")
    return result.stderr.strip()


def init(operator, java):
    version = check_java(java)
    if sha(JAR) != JAR_SHA256:
        raise ValueError("Unexpected reference JAR hash")
    (HERE / "runs").mkdir(exist_ok=True)
    run = Path(tempfile.mkdtemp(prefix=datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ-"),
                                dir=HERE / "runs"))
    shutil.copytree(HERE / "profiles", run / "profiles")
    save(run / "manifest.json", {
        "started_at": utc(), "operator": operator, "java": java, "java_version": version,
        "jar": str(JAR), "jar_sha256": JAR_SHA256, "profiles_sha256": profile_hashes(run / "profiles"),
        "kernel": os.uname().release, "machine": os.uname().machine,
        "tcp_ports": TCP_PORTS, "udp_ports": UDP_PORTS,
    })
    print(run)


def load(run):
    manifest = json.loads((run / "manifest.json").read_text())
    if sha(JAR) != JAR_SHA256 or manifest["jar_sha256"] != JAR_SHA256:
        raise ValueError("Unexpected reference JAR hash")
    if profile_hashes(run / "profiles") != manifest["profiles_sha256"]:
        raise ValueError("Run profiles changed after init")
    return manifest


def node(run, role):
    manifest = load(run)
    profile = run / "profiles" / PROFILES[role]
    cmd = [manifest["java"], "-Djava.net.preferIPv4Stack=true", "-cp", str(JAR), MAIN,
           "--role", role, "--config", str(profile)]
    with (run / f"{role}.log").open("xb") as log:
        process = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        try:
            save(run / f"{role}.start.json", {"at": utc(), "pid": process.pid, "command": cmd,
                                             "proc_start": proc_start(process.pid)})
            for line in process.stdout:
                log.write(line)
                log.flush()
                sys.stdout.buffer.write(line)
                sys.stdout.buffer.flush()
            return process.wait(timeout=5)
        finally:
            if process.poll() is None:
                process.terminate()
                try:
                    process.wait(timeout=5)
                except subprocess.TimeoutExpired:
                    process.kill()
                    process.wait(timeout=5)
            process.stdout.close()
            save(run / f"{role}.exit.json", {"at": utc(), "pid": process.pid,
                                            "java_exit": process.returncode})


def proc_fields(pid):
    return Path(f"/proc/{pid}/stat").read_text().rsplit(")", 1)[1].split()


def proc_start(pid):
    return proc_fields(pid)[19]


def proc_command(pid):
    return Path(f"/proc/{pid}/cmdline").read_bytes().rstrip(b"\0").split(b"\0")


def owned_pid(run, role):
    record = json.loads((run / f"{role}.start.json").read_text())
    pid = int(record["pid"])
    command = proc_command(pid)
    if proc_start(pid) != record["proc_start"] or command != [os.fsencode(x) for x in record["command"]]:
        raise ValueError(f"PID {pid} does not match this run's {role}")
    return pid


def port_check(run, label):
    results = []
    for kind, ports in ((socket.SOCK_STREAM, TCP_PORTS), (socket.SOCK_DGRAM, UDP_PORTS)):
        for port in ports:
            with socket.socket(socket.AF_INET, kind) as probe:
                if kind == socket.SOCK_STREAM:
                    # Test listener reuse. TIME-WAIT is not a live listener.
                    probe.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
                try:
                    probe.bind((LOOPBACK, port))
                    if kind == socket.SOCK_STREAM:
                        probe.listen(1)
                    results.append({"port": port, "kind": kind.name, "available": True})
                except OSError as error:
                    results.append({"port": port, "kind": kind.name,
                                    "available": False, "error": str(error)})
    record = {"at": utc(), "ports": results}
    save(run / f"ports-{label}.json", record)
    print(json.dumps(record))
    return 0 if all(p["available"] for p in results) else 1


def mark(run, label):
    entry = {"at": utc(), "monotonic_ns": time.monotonic_ns(), "label": label}
    with (run / "timeline.jsonl").open("a") as stream:
        stream.write(json.dumps(entry, ensure_ascii=False) + "\n")
    print(json.dumps(entry, ensure_ascii=False))


def observe(run):
    mark(run, "bootstrap-quiet-start")
    start = time.monotonic()
    time.sleep(30)
    mark(run, "bootstrap-quiet-end")
    print(f"Observed {time.monotonic() - start:.3f} s")


def ss_query(args):
    result = subprocess.run(["ss", "-4Htnp", *args], capture_output=True, text=True,
                            check=True, timeout=5)
    if result.stderr:
        raise ValueError(result.stderr.strip())
    return result.stdout.splitlines()


def tuple_selector(src, dst):
    return ["state", "established", "(", "src", LOOPBACK, "and", "sport", "=", f":{src}",
            "and", "dst", LOOPBACK, "and", "dport", "=", f":{dst}", ")"]


def destroy_command(selector):
    return ["sudo", "-n", "ss", "-4HKtn", *selector]


def row_owners(row):
    return {int(pid) for pid in re.findall(r"\bpid=(\d+),", row)}


def capture_command(run):
    binary = shutil.which("tcpdump")
    if binary is None:
        raise ValueError("tcpdump was not found")
    ports = " or ".join(f"port {port}" for port in TCP_PORTS)
    capture_filter = f"ip and tcp and src host {LOOPBACK} and dst host {LOOPBACK} and ({ports})"
    return [str(Path(binary).resolve()), "-Z", pwd.getpwuid(os.getuid()).pw_name,
            "-i", "lo", "-p", "-s", "0", "-U", "-c", "20000", "-w", str(run / "control.pcap"),
            capture_filter]


def matching_capture_pids(command):
    expected = [os.fsencode(argument) for argument in command]
    pids = []
    for directory in Path("/proc").iterdir():
        if directory.name.isdecimal():
            pid = int(directory.name)
            try:
                if proc_command(pid) == expected:
                    pids.append(pid)
            except (FileNotFoundError, ProcessLookupError, PermissionError):
                # An exited or unreadable PID cannot establish a capture identity.
                continue
    return pids


def is_descendant(pid, launcher_pid):
    while pid > 1 and pid != launcher_pid:
        pid = int(proc_fields(pid)[1])
    return pid == launcher_pid


def require_permission_probe(run):
    if json.loads((run / "permission-probe.json").read_text()).get("passed") is not True:
        raise ValueError("The permission probe has not passed")


def capture(run):
    require_permission_probe(run)
    if not hasattr(os, "pidfd_open"):
        raise ValueError("Use Python 3.9+ on Linux with pidfd support")
    os.close(os.pidfd_open(os.getpid()))
    if (run / "control.pcap").exists() or (run / "capture-start.json").exists():
        raise ValueError("Use a new run for another capture")
    command = capture_command(run)
    launch = ["sudo", "-n", "timeout", "--signal=INT", "--kill-after=5", "300", *command]
    log_path = run / "capture.log"
    with log_path.open("x", buffering=1) as log:
        log.write(f"CAPTURE_START {utc()}\nCOMMAND {json.dumps(launch)}\n")
        process = subprocess.Popen(launch, stdout=log, stderr=subprocess.STDOUT)
        try:
            deadline = time.monotonic() + 8
            while process.poll() is None and time.monotonic() < deadline:
                pids = matching_capture_pids(command)
                if len(pids) > 1:
                    raise ValueError("More than one tcpdump matches this run")
                if pids and "listening on lo" in log_path.read_text(errors="replace"):
                    pid = pids[0]
                    if not is_descendant(pid, process.pid):
                        raise ValueError("tcpdump is outside this capture launcher")
                    fields = proc_fields(pid)
                    if fields[0] in CAPTURE_STATES:
                        save(run / "capture-start.json", {"at": utc(), "pid": pid,
                             "proc_start": fields[19], "state": fields[0],
                             "boot_id": BOOT_ID.read_text().strip(), "command": command})
                        print(f"Capture ready: tcpdump PID {pid}. Press Ctrl-C to stop.", flush=True)
                        return process.wait()
                time.sleep(0.1)
            raise ValueError(f"tcpdump did not become ready. See {log_path}")
        except KeyboardInterrupt:
            return 130
        finally:
            try:
                if process.poll() is None:
                    process.send_signal(signal.SIGINT)
                    process.wait(timeout=10)
            finally:
                log.write(f"CAPTURE_EXIT {process.returncode}\nCAPTURE_END {utc()}\n")


def active_capture(run):
    try:
        if not hasattr(os, "pidfd_open"):
            raise ValueError("Python or the platform has no pidfd support")
        record = json.loads((run / "capture-start.json").read_text())
        command = capture_command(run)
        pid = record["pid"]
        if (type(pid) is not int or pid <= 1 or record["command"] != command
                or record["boot_id"] != BOOT_ID.read_text().strip()):
            raise ValueError("Capture identity does not match this run")
        if matching_capture_pids(command) != [pid]:
            raise ValueError("Expected exactly one matching tcpdump with the recorded PID")
        fd = os.pidfd_open(pid)
        try:
            poller = select.poll()
            poller.register(fd, select.POLLIN)
            actual_command = proc_command(pid)
            fields = proc_fields(pid)
            state = fields[0]
            if (fields[19] != record["proc_start"] or state not in CAPTURE_STATES
                    or actual_command != [os.fsencode(argument) for argument in command]
                    or poller.poll(0)):
                raise ValueError("The recorded tcpdump has changed identity or is not active")
            return {"at": utc(), "pid": pid, "proc_start": fields[19], "state": state}
        finally:
            os.close(fd)
    except (OSError, KeyError, TypeError, ValueError) as error:
        raise ValueError(f"Cut aborted: capture identity or state could not be verified ({error})") from error


def permission_probe(run):
    path = run / "permission-probe.json"
    if path.exists():
        raise ValueError("Use a new run for another permission probe")
    report = {"at": utc(), "capture": False, "destroy": False}
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as listener:
            listener.bind((LOOPBACK, 0))
            listener.listen(1)
            dst = listener.getsockname()[1]
            with socket.create_connection((LOOPBACK, dst), timeout=2) as client:
                server, _ = listener.accept()
                with server:
                    src = client.getsockname()[1]
                    capture = ["sudo", "-n", "timeout", "--signal=INT", "3", "tcpdump",
                               "-Z", subprocess.check_output(["id", "-un"], text=True).strip(),
                               "-i", "lo", "-p", "-s", "0", "-U", "-c", "1", "-w",
                               str(run / "probe.pcap"),
                               f"ip and tcp and src host {LOOPBACK} and src port {src} "
                               f"and dst host {LOOPBACK} and dst port {dst}"]
                    with subprocess.Popen(capture, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                                          text=True) as probe:
                        try:
                            # Repeated traffic gives tcpdump time to attach. Each byte stays in this test pair.
                            for _ in range(10):
                                if probe.poll() is not None:
                                    break
                                client.sendall(b"x")
                                time.sleep(0.1)
                            stdout, stderr = probe.communicate(timeout=5)
                        except BaseException:
                            probe.send_signal(signal.SIGINT)
                            probe.communicate(timeout=5)
                            raise
                        report["capture_result"] = {"command": capture, "exit": probe.returncode,
                                                    "stdout": stdout, "stderr": stderr}
                        report["capture"] = (probe.returncode == 0 and
                                             (run / "probe.pcap").stat().st_size > 24)
                    selector = tuple_selector(src, dst)
                    rows = ss_query(selector)
                    if len(rows) != 1 or row_owners(rows[0]) != {os.getpid()}:
                        raise ValueError("Disposable socket selection is not unique and owned")
                    command = destroy_command(selector)
                    result = subprocess.run(command, capture_output=True, text=True, timeout=5)
                    after = ss_query(selector)
                    report["destroy_result"] = {"command": command, "before": rows, "after": after,
                                                "exit": result.returncode, "stdout": result.stdout,
                                                "stderr": result.stderr}
                    report["destroy"] = (result.returncode == 0 and not result.stderr
                                         and bool(result.stdout.strip()) and not after)
    except (OSError, ValueError, subprocess.SubprocessError) as error:
        report["error"] = str(error)
    report["passed"] = report["capture"] and report["destroy"] and "error" not in report
    save(path, report)
    print(json.dumps(report, ensure_ascii=False))
    return 0 if report["passed"] else 1


def select_pair(run):
    # Higher node ID listens. The SERVER endpoint is the outgoing ephemeral port.
    server_pid = owned_pid(run, "SERVER")
    relay_pid = owned_pid(run, "RELAY")
    relay_port = NODE_PORTS["RELAY"]
    candidates = ss_query(["state", "established", "(", "dst", LOOPBACK,
                           "and", "dport", "=", f":{relay_port}", ")"])
    rows = [row for row in candidates if row_owners(row) == {server_pid}]
    if len(rows) != 1:
        raise ValueError("Expected one established SERVER socket to RELAY")
    match = re.fullmatch(r"\d+\s+\d+\s+127\.0\.0\.1:(\d+)\s+127\.0\.0\.1:"
                         + str(relay_port) + r"\s+.*", rows[0])
    if not match:
        raise ValueError("Unexpected socket listing for SERVER")
    src = int(match[1])
    reverse = ss_query(tuple_selector(relay_port, src))
    if len(reverse) != 1 or row_owners(reverse[0]) != {relay_pid}:
        raise ValueError("Reverse socket does not belong to the expected RELAY")
    return {"server_pid": server_pid, "relay_pid": relay_pid, "source_port": src,
            "destination_port": relay_port, "server_row": rows[0], "relay_row": reverse[0]}


def cut(run, expected_source_port):
    load(run)
    require_permission_probe(run)
    capture_log = (run / "capture.log").read_text()
    if ("listening on lo" not in capture_log or "CAPTURE_END " in capture_log
            or (run / "control.pcap").stat().st_size <= 24):
        raise ValueError("Start a valid capture and verify it is still running")
    active_capture(run)
    boot = json.loads((run / "BOOTSTRAP.exit.json").read_text())
    if boot["java_exit"] != 0:
        raise ValueError("Expected BOOTSTRAP quit with Java exit 0")
    listener = subprocess.run(["ss", "-4Hltn", "sport", "=", ":56100"],
                              capture_output=True, text=True, check=True, timeout=5)
    if listener.stdout.strip() or listener.stderr:
        raise ValueError("Bootstrap port has a listener or the listener check failed")
    pair = select_pair(run)
    if pair["source_port"] != expected_source_port:
        raise ValueError("Selected tuple changed since inspection")
    selector = tuple_selector(expected_source_port, pair["destination_port"])
    cmd = destroy_command(selector)
    save(run / "cut-before.json", {"at": utc(), "pair": pair, "command": cmd})
    try:
        capture_at_cut = active_capture(run)
    except ValueError as error:
        save(run / "cut-aborted.json", {"at": utc(), "reason": str(error)})
        raise
    record = {"capture_at_cut": capture_at_cut}
    try:
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=5)
        record.update(exit=result.returncode, stdout=result.stdout, stderr=result.stderr)
        after = ss_query(selector)
        record["old_tuple_rows"] = after
    except (OSError, ValueError, subprocess.SubprocessError) as error:
        record["error"] = str(error)
        raise
    finally:
        record["at"] = utc()
        save(run / "cut-result.json", record)
    print(json.dumps(record, ensure_ascii=False))
    return 0 if result.returncode == 0 and not result.stderr and result.stdout.strip() and not after else 1


def main():
    parser = argparse.ArgumentParser()
    commands = parser.add_subparsers(dest="command", required=True)
    setup = commands.add_parser("init")
    setup.add_argument("--operator", required=True)
    setup.add_argument("--java", required=True)
    for name in ("probe", "capture", "ports", "node", "mark", "observe", "pair", "cut"):
        sub = commands.add_parser(name)
        sub.add_argument("run", type=Path)
        if name == "ports":
            sub.add_argument("label", choices=("before", "after", "after-retry"))
        elif name == "node":
            sub.add_argument("role", choices=PROFILES)
        elif name == "mark":
            sub.add_argument("label")
        elif name == "cut":
            sub.add_argument("source_port", type=int)
    args = parser.parse_args()
    if args.command == "init":
        init(args.operator, str(Path(args.java).resolve(strict=True)))
        return 0
    run = args.run.resolve(strict=True)
    if run.parent != HERE / "runs":
        raise ValueError("Use a run directory created by init")
    load(run)
    if args.command == "probe":
        return permission_probe(run)
    if args.command == "capture":
        return capture(run)
    if args.command == "ports":
        return port_check(run, args.label)
    if args.command == "node":
        return node(run, args.role)
    if args.command == "mark":
        mark(run, args.label)
    if args.command == "observe":
        observe(run)
    if args.command == "pair":
        print(json.dumps(select_pair(run), indent=2))
    if args.command == "cut":
        return cut(run, args.source_port)
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (ValueError, OSError, subprocess.SubprocessError) as error:
        sys.exit(str(error))
