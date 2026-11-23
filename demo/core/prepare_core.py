#!/usr/bin/env python3
"""Gera artefactos de deployment da demo OTT para o CORE (emulador Coreemu).

Só de leitura sobre a run e o XML: produz, em --out, os perfis por nó com os
IPs do cenário CORE, a topologia reescrita, o bootstrap no host do servidor,
um manifesto e comandos de arranque por nó. Não executa nada no CORE.

Uso:
  python3 demo/core/prepare_core.py --scenario cenario1 \
      --run 20261123T151115Z-ff3a5f83 \
      --runs-dir experiments/checkpoint-phase04/runs \
      --xml CORE/topologies/cenario1.xml \
      --jar Projeto/target/ott-streaming-1.0-SNAPSHOT.jar \
      --out demo/core/out/cenario1-20261123T151115Z-ff3a5f83
"""
import argparse
import json
import re
import xml.etree.ElementTree as ET
from pathlib import Path

# Mapeamento dispositivo CORE -> nó overlay, por cenário (Figura 1 / enunciado).
DEVICE_MAP = {
    "cenario1": {"Servidor": "S1", "Router1": "R1", "Cliente1": "C1", "Cliente2": "C2"},
    "cenario2": {"Servidor": "S1", "Router1": "R1", "Router2": "R2", "Router3": "R3",
                 "Cliente1": "C1", "Cliente2": "C2"},
    "cenario3": {"Servidor": "O1", "Router1": "O2", "Router2": "O3", "Router3": "O5",
                 "Router4": "O6", "Router5": "O7", "Cliente1": "O4", "Cliente2": "O8",
                 "Cliente3": "O9", "Cliente4": "O10", "Cliente5": "O11"},
}


def parse_xml(path):
    """Devolve device name -> IPv4 da primeira interface, e lista de arestas."""
    root = ET.parse(path).getroot()
    devices = {}
    for dev in root.find("devices").findall("device"):
        devices[dev.get("id")] = dev.get("name")
    ip_by_node = {}
    edges = []
    for link in root.find("links").findall("link"):
        n1, n2 = link.get("node1"), link.get("node2")
        for tag in ("iface1", "iface2"):
            iface = link.find(tag)
            if iface is not None and iface.get("ip4"):
                node = n1 if tag == "iface1" else n2
                ip_by_node[node] = iface.get("ip4").split("/")[0] if "/" in iface.get("ip4") else iface.get("ip4")
        edges.append((devices.get(n1, n1), devices.get(n2, n2)))
    name_to_ip = {devices[nid]: ip for nid, ip in ip_by_node.items() if nid in devices}
    return name_to_ip, edges


def read_props(path):
    props = {}
    for line in Path(path).read_text().splitlines():
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            k, v = line.split("=", 1)
            props[k.strip()] = v.strip()
    return props


def write_props(path, props, comment):
    lines = ["# Gerado por demo/core/prepare_core.py a partir de run demo — " + comment]
    for k in sorted(props):
        lines.append(f"{k}={props[k]}")
    Path(path).write_text("\n".join(lines) + "\n")


def build_deployment(root, scenario, run_dir, xml_path, jar, classes, out):
    """Gera o deployment na pasta out; devolve o manifest. Reutilizado por run_core.py."""
    run_dir = Path(run_dir)
    cfg = run_dir / "config"
    if not cfg.is_dir():
        raise SystemExit(f"config da run não encontrada: {cfg}")

    name_to_ip, edges = parse_xml(xml_path)
    mapping = DEVICE_MAP[scenario]
    missing = [d for d in mapping if d not in name_to_ip]
    if missing:
        raise SystemExit(f"dispositivos em falta no XML {xml_path}: {missing}")

    out = Path(out)
    (out / "nodes").mkdir(parents=True, exist_ok=True)

    # topologia da run -> IPs CORE
    topo = read_props(cfg / "topology.properties")
    node_ids = topo["node.ids"].split(",")
    out_topo = {"node.ids": topo["node.ids"]}
    for node in node_ids:
        dev = next(d for d, n in mapping.items() if n == node)
        prefix = f"node.{node}."
        out_topo[prefix + "host"] = name_to_ip[dev]
        out_topo[prefix + "controlPort"] = topo[prefix + "controlPort"]
        out_topo[prefix + "dataPort"] = topo[prefix + "dataPort"]
        out_topo[prefix + "role"] = topo[prefix + "role"]
        out_topo[prefix + "neighbors"] = topo[prefix + "neighbors"]

    server_node = next(n for n in node_ids if topo[f"node.{n}.role"] == "SERVER")
    server_ip = name_to_ip[next(d for d, n in mapping.items() if n == server_node)]

    boot = read_props(cfg / "BOOT.properties")
    boot["bind.host"] = server_ip
    boot["advertised.host"] = server_ip
    boot["topology.file"] = "topology.properties"
    write_props(out / "BOOT.properties", boot, f"bootstrap no host de {server_node} ({server_ip})")

    for node in node_ids:
        p = read_props(cfg / f"{node}.properties")
        dev = next(d for d, n in mapping.items() if n == node)
        ip = name_to_ip[dev]
        p["bind.host"] = ip
        p["advertised.host"] = ip
        p["bootstrap.host"] = server_ip
        p["bootstrap.port"] = boot["control.port"]
        write_props(out / "nodes" / f"{node}.properties", p, f"{node} no dispositivo CORE {dev} ({ip})")

    (out / "topology.properties").write_text(
        "\n".join(f"{k}={v}" for k, v in sorted(out_topo.items())) + "\n")
    for extra in ("streams.properties",):
        (out / extra).write_text((cfg / extra).read_text())
        # os perfis em nodes/ resolvem streams.file relativamente à própria pasta
        (out / "nodes" / extra).write_text((cfg / extra).read_text())

    jar = Path(jar).resolve()
    classes = Path(classes).resolve()
    cp = f"{jar}:{classes}"
    commands = {"BOOT": f"java -cp {jar} pt.uminho.esr.ott.app.OttApplication --role BOOTSTRAP --config BOOT.properties"}
    for node in node_ids:
        role = topo[f"node.{node}.role"]
        commands[node] = (f"java -cp {cp} pt.uminho.esr.ott.overlay.DemoNode nodes/{node}.properties {role}"
                          f"   # ou CLI: java -cp {jar} pt.uminho.esr.ott.app.OttApplication --role {role} --config nodes/{node}.properties")
    manifest = {
        "scenario": scenario,
        "sourceRun": str(run_dir.relative_to(root)) if str(run_dir).startswith(str(root)) else str(run_dir),
        "xml": str(xml_path),
        "deviceMap": mapping,
        "deviceIp": name_to_ip,
        "coreEdges": edges,
        "server": {"node": server_node, "ip": server_ip},
        "jar": str(jar),
        "jarSha256": __import__("hashlib").sha256(jar.read_bytes()).hexdigest(),
        "commands": commands,
        "note": "Control-plane only. Video/UDP/player is PHASE-05 (TASK-019..024) and not part of this demo. "
                "Copy this folder to CORE/volume/ott/<scenario>/ and run the commands on each CORE device.",
    }
    (out / "manifest.json").write_text(json.dumps(manifest, indent=1) + "\n")
    (out / "README.md").write_text(
        f"# Deployment no CORE — {scenario} (run {run_dir.name})\n\n"
        f"1. No CORE GUI, abrir a topologia `{xml_path}` e confirmar IPs (manifest.json → deviceIp).\n"
        f"2. Copiar esta pasta para `CORE/volume/ott/{scenario}/` (volume montado em /volume).\n"
        f"3. No host de {server_node} ({server_ip}): arrancar o bootstrap (comando em manifest.json → BOOT).\n"
        f"4. Em cada nó: `cd /volume/ott/{scenario} && <comando do manifest.json para o nó>`.\n"
        f"5. Validar com a CLI real (`OttApplication`) ou com o adaptador DemoNode (subscribe/trace/snapshot).\n"
        f"6. Recolher logs em experiments/ segundo docs/09-TESTING-STRATEGY.md.\n\n"
        f"Gerado por prepare_core.py; ver demo/GUIA-DEMO.md para o guia completo.\n")
    return manifest


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--scenario", required=True, choices=sorted(DEVICE_MAP))
    ap.add_argument("--run", required=True, help="id da run em runs/<cenario>/")
    ap.add_argument("--runs-dir", default="experiments/checkpoint-phase04/runs")
    ap.add_argument("--xml", required=True)
    ap.add_argument("--jar", required=True)
    ap.add_argument("--out", required=True)
    ap.add_argument("--classes", default="demo/.classes", help="classes do adaptador DemoNode")
    args = ap.parse_args()

    root = Path(__file__).resolve().parents[2]
    manifest = build_deployment(root, args.scenario, root / args.runs_dir / args.scenario / args.run,
                                root / args.xml, root / args.jar, root / args.classes, args.out)
    out = Path(args.out)
    print(f"OK {out}")
    print(json.dumps({"server": manifest["server"], "nodes": manifest["deviceIp"]}, indent=1))


if __name__ == "__main__":
    main()
