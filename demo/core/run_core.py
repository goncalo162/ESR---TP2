#!/usr/bin/env python3
"""Corre a demo OTT no CORE (contentor `core`, core-daemon) com a CLI real.

Uso (na raiz do repo, com o docker do CORE a correr — `cd CORE && docker compose up -d core`):

  python3 demo/core/run_core.py cenario2
  python3 demo/core/run_core.py cenario1 --source-run 20261123T151115Z-ff3a5f83

O que faz:
  1. valida pré-requisitos (contentor core Up, JDK 21, JAR + sha256, classes demo);
  2. gera os perfis por nó com os IPs do XML CORE (prepare_core.build_deployment) e
     copia JAR + classes + driver para CORE/volume/ott/<run>/;
  3. arranca tcpdump no contentor (pcap cru como evidência de wire);
  4. executa o driver dentro do contentor (sessão gRPC + JVM por nó + guião de 6
     passos, incluindo a etapa com a CLI real OttApplication);
  5. copia a evidência para experiments/core-demo/<cenario>/<run>/ e imprime o verdict.

Requer: docker, python3 no host; o contentor core com /opt/core/venv e o volume
CORE/volume montado em /volume. Não altera código de produção.
"""
import argparse
import hashlib
import json
import shutil
import subprocess
import sys
import uuid
from datetime import datetime, timezone
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import prepare_core as pc  # noqa: E402

DEFAULT_SOURCE_RUN = {
    "cenario1": "20261123T151115Z-ff3a5f83",
    "cenario2": "20261123T151525Z-1b1daa64",
    "cenario3": "20261123T152923Z-e135a4d5",
}
CORE_XML = "/root/.coregui/xmls/topologies/{scenario}.xml"


def sh(cmd, **kw):
    return subprocess.run(cmd, shell=isinstance(cmd, str), capture_output=True, text=True, **kw)


def read_props(path):
    props = {}
    for line in Path(path).read_text().splitlines():
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            k, v = line.split("=", 1)
            props[k.strip()] = v.strip()
    return props


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("scenario", choices=["cenario1", "cenario2", "cenario3"])
    ap.add_argument("--source-run", default=None, help="run local donde vêm os perfis (default automático)")
    ap.add_argument("--runs-dir", default="experiments/checkpoint-phase04/runs")
    ap.add_argument("--out-root", default="experiments/core-demo")
    ap.add_argument("--keep", action="store_true", help="não parar a sessão CORE no fim (debug)")
    args = ap.parse_args()

    root = Path(__file__).resolve().parents[2]
    scenario = args.scenario
    source_run = args.source_run or DEFAULT_SOURCE_RUN[scenario]
    run_dir = root / args.runs_dir / scenario / source_run

    # 1. pré-requisitos
    r = sh("docker ps --filter name=^core$ --format '{{.Status}}'")
    if "Up" not in r.stdout:
        raise SystemExit("contentor `core` não está a correr: cd CORE && docker compose up -d core")
    java_home = sh("readlink -f $(command -v java)").stdout.strip()
    jdk = sh(f"{java_home} -version").stderr.splitlines()[0]
    if "21" not in jdk:
        raise SystemExit(f"JDK 21 necessário, encontrado: {jdk}")
    jar = root / "Projeto/target/ott-streaming-1.0-SNAPSHOT.jar"
    if not jar.is_file():
        raise SystemExit("Falta o JAR: cd Projeto && mvn -B -ntp clean verify")
    jar_sha = hashlib.sha256(jar.read_bytes()).hexdigest()
    classes = root / "demo/.classes"
    srcs = [root / "demo/src/demo/Json.java", root / "demo/src/demo/DelayProxy.java",
            root / "demo/src/demo/CheckpointDemo.java", root / "demo/src/pt/uminho/esr/ott/overlay/DemoNode.java"]
    marker = classes / ".built"
    stale = not marker.is_file() or any(s.stat().st_mtime > marker.stat().st_mtime for s in srcs)
    if stale:
        classes.mkdir(exist_ok=True)
        javac = str(Path(java_home).parent.parent / "bin/javac")
        r = subprocess.run([javac, "--release", "21", "-cp", str(jar), "-d", str(classes)]
                           + [str(s) for s in srcs], capture_output=True, text=True)
        if r.returncode != 0:
            raise SystemExit("javac falhou: " + r.stderr)
        marker.write_text(jdk + "\n")
    git_head = sh("git rev-parse HEAD", cwd=root).stdout.strip()

    # 2. staging
    run_id = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ") + "-" + uuid.uuid4().hex[:8]
    stage = root / "CORE/volume/ott" / run_id
    if stage.exists():
        shutil.rmtree(stage)
    stage.mkdir(parents=True)
    xml = root / "CORE/topologies" / f"{scenario}.xml"
    manifest = pc.build_deployment(root, scenario, run_dir, xml, jar, classes, stage)
    shutil.copy(jar, stage / "app.jar")
    shutil.copytree(classes, stage / "classes")
    shutil.copy(Path(__file__).parent / "driver.py", stage / "driver.py")

    topo = read_props(stage / "topology.properties")
    roles = {n: topo[f"node.{n}.role"] for n in topo["node.ids"].split(",")}
    scen_props = read_props(root / "demo/config" / f"{scenario}.properties")
    delays_ms = {k[6:]: int(v) for k, v in scen_props.items() if k.startswith("delay.")}
    plan = {
        "runId": run_id, "scenario": scenario,
        "stageDir": f"/volume/ott/{run_id}",
        "xml": CORE_XML.format(scenario=scenario),
        "deviceMap": manifest["deviceMap"],
        "deviceIp": manifest["deviceIp"],
        "serverIp": manifest["server"]["ip"],
        "scenarioProps": scen_props,
        "roles": roles,
        "delaysMs": delays_ms,
        "gitHead": git_head, "jdk": jdk, "jarSha256": jar_sha,
        "sourceRun": str(run_dir.relative_to(root)),
    }
    (stage / "plan.json").write_text(json.dumps(plan, indent=1))
    (stage / "out").mkdir(exist_ok=True)
    (stage / "manifest.json").write_text(json.dumps(manifest, indent=1))
    tool_files = {"demo/core/driver.py": (Path(__file__).parent / "driver.py").read_bytes(),
                  "demo/core/run_core.py": (Path(__file__).parent / "run_core.py").read_bytes(),
                  "demo/core/prepare_core.py": (Path(__file__).parent / "prepare_core.py").read_bytes()}
    (stage / "tool-sha256.json").write_text(json.dumps(
        {k: hashlib.sha256(v).hexdigest() for k, v in tool_files.items()}, indent=1))

    # 3. pcap de wire (evidência cru)
    sh(f"docker exec -d core mkdir -p /volume/ott/{run_id}/out")
    sh(f"docker exec -d core tcpdump -i any -nn -U -w /volume/ott/{run_id}/out/wire.pcap tcp")

    # 4. driver dentro do contentor
    print(f"RUN {run_id} cenário={scenario} source={source_run} jar={jar_sha[:12]}", flush=True)
    r = subprocess.run(["docker", "exec", "core", "/opt/core/venv/bin/python",
                        f"/volume/ott/{run_id}/driver.py", f"/volume/ott/{run_id}/plan.json"],
                       capture_output=True, text=True)
    (stage / "driver-stdout.log").write_text(r.stdout + "\n---STDERR---\n" + r.stderr)
    print(r.stdout[-4000:])
    if r.returncode not in (0, 1):
        print(r.stderr[-2000:])

    # 5. evidência para o repo
    dest = root / args.out_root / scenario / run_id
    if dest.exists():
        shutil.rmtree(dest)
    dest.mkdir(parents=True)
    for item in ("out", "logs", "plan.json", "manifest.json", "tool-sha256.json", "driver-stdout.log"):
        src = stage / item
        if src.is_dir():
            shutil.copytree(src, dest / item)
        elif src.exists():
            shutil.copy(src, dest / item)
    result = {}
    rp = dest / "out/result.json"
    if rp.exists():
        result = json.loads(rp.read_text())
    print(f"RESULT {result.get('verdict', 'FAIL')} {dest}")
    print(f"JAR {jar_sha}  JDK {jdk}  HEAD {git_head}")
    return 0 if result.get("verdict") == "PASS" else 1


if __name__ == "__main__":
    sys.exit(main())
