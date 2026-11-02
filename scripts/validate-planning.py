#!/usr/bin/env python3
"""Validate planning artifacts without running Java, GUI, CORE or deployments.

Frontmatter uses a small YAML subset: JSON scalar/array values and one nested
mapping for complexity. This validator needs only the Python standard library.
"""
import argparse
from collections import Counter
from datetime import datetime, timezone
import hashlib
import json
from pathlib import Path
import re
import sys
from urllib.parse import unquote
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
errors = []
checks = []


def require(condition, message):
    if not condition:
        errors.append(message)


def read_metadata(path):
    text = path.read_text(encoding="utf-8")
    match = re.match(r"---\n(.*?)\n---(?:\n|$)", text, re.S)
    if not match:
        raise ValueError(f"Missing frontmatter: {path}")
    result, parent = {}, None
    for line in match[1].splitlines():
        if not line or line.lstrip().startswith("#"):
            continue
        key, separator, value = line.strip().partition(":")
        if not separator:
            raise ValueError(f"Invalid metadata: {path}: {line}")
        if line.startswith("  "):
            if parent is None:
                raise ValueError(f"Missing metadata parent: {path}: {line}")
            require(key not in result[parent], f"{path}: duplicate key {key}")
            result[parent][key] = json.loads(value.strip())
        elif not value.strip():
            require(key not in result, f"{path}: duplicate key {key}")
            result[key], parent = {}, key
        else:
            require(key not in result, f"{path}: duplicate key {key}")
            result[key], parent = json.loads(value.strip()), None
    return result, text


def section(text, n):
    match = re.search(rf"^## {n}\. [^\n]*\n(.*?)(?=^## |\Z)", text, re.M | re.S)
    return match[1].strip() if match else ""


def normalized(text):
    return " ".join(re.sub(r"\[([^\]]+)\]\([^)]+\)", r"\1", text).split())


def ancestors(node, graph):
    seen, pending = set(), list(graph.get(node, []))
    while pending:
        item = pending.pop()
        if item not in seen:
            seen.add(item)
            pending.extend(graph.get(item, []))
    return seen


def validate_dag(graph, label):
    for node, deps in graph.items():
        require(all(dep in graph for dep in deps), f"{label}/{node}: unknown dependency")
        require(node not in ancestors(node, graph), f"{label}/{node}: dependency cycle")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check-snapshot", action="store_true")
    parser.add_argument("--report", type=Path)
    args = parser.parse_args()
    manifest = json.loads((ROOT / "memory-bank/planning-source-manifest.json").read_text())
    catalog = json.loads((ROOT / "memory-bank/architecture-traceability.json").read_text())
    base = ROOT / "memory-bank/intents" / manifest["intent"]
    expected = manifest["expected_counts"]
    add = (ROOT / "add.md").read_text()
    spec = (ROOT / "specs.md").read_text()
    decisions = {d["id"]: d for d in catalog["decisions"]}
    imps = {i["id"]: i for i in catalog["implementation_tasks"]}
    requirements = catalog["requirements"]
    require(catalog["architecture_source"] == "add.md", "Derived index has incorrect architectural authority")
    require(re.findall(r"^## (\d+)\. ", add, re.M) == [str(i) for i in range(1, 14)],
            "ADD must contain the 13 required sections in order")
    spec_ids = re.findall(r"^## ((?:REQ|NFR|CON|INT)-\d{3}) —", spec, re.M)
    require(Counter(spec_ids) == Counter(requirements.keys()), "Canonical requirements missing/duplicated")
    require(len(spec_ids) == expected["requirements"] == catalog["counts"]["requirements"],
            "Incorrect canonical requirement count")
    for rid, r in requirements.items():
        body = re.search(rf"^## {rid} — [^\n]*\n(.*?)(?=^## |\Z)", spec, re.M | re.S)
        criterion = re.search(r"^- Critério de aceitação: (.*)$", body[1], re.M) if body else None
        require(criterion is not None and normalized(criterion[1]) == normalized(r["acceptance"]),
                f"{rid}: canonical acceptance criterion changed")
    # Check legacy aliases against the original criteria instead of renumbering them.
    source = (ROOT / "docs/02-REQUIREMENTS.md").read_text()
    alias_map = {r["legacy_id"]: rid for rid, r in requirements.items() if r["legacy_id"]}
    legacy_matches = list(re.finditer(
        r"^### ((?:RF|RNF|RC|RP)-\d{3}) — [^\n]*\n(.*?)(?=^### |^## |\Z)", source, re.M | re.S))
    require(len(legacy_matches) == expected["legacy_requirements"], "Legacy requirements count changed")
    for match in legacy_matches:
        old, body = match.groups()
        original = re.search(r"^- Critério de aceitação: (.*)$", body, re.M)
        require(old in alias_map, f"{old}: missing canonical alias")
        if original and old in alias_map:
            require(normalized(original[1]) == normalized(requirements[alias_map[old]]["acceptance"]),
                    f"{old}: original acceptance criterion changed during consolidation")
    checks.append(f"Requisitos/constraints/intents: {len(requirements)}; 35 aliases e critérios legados preservados.")

    # Human document is normative; JSON entries must agree with the actual text.
    actual_adrs = re.findall(r"^### (ADR-\d{3}) —", add, re.M)
    require(Counter(actual_adrs) == Counter(decisions.keys()) and len(decisions) == expected["adrs"],
            "ADR entries missing/duplicated")
    for aid, d in decisions.items():
        body = re.search(rf"^### {aid} — [^\n]*\n(.*?)(?=^### |^## |\Z)", add, re.M | re.S)
        require(bool(d["drivers"]) and set(d["drivers"]) <= requirements.keys(), f"{aid}: orphan/unknown driver")
        for field in ("context", "decision", "alternatives", "consequences"):
            require(bool(d[field]), f"{aid}: missing {field}")
            if body:
                require(normalized(d[field]) in normalized(body[1]), f"{aid}: ADD/index mismatch in {field}")
        require(bool(d["components"]) and bool(d["tests"]), f"{aid}: missing component/validation")
        require(d["status"] in {"Aceite", "Proposta", "Substituída"}, f"{aid}: invalid status")
        if body:
            for field in ("Estado", "Contexto e drivers", "Decisão", "Alternativas rejeitadas",
                          "Consequências, riscos e trade-offs", "Impacto", "Como validar"):
                require("**" + field + ":**" in body[1], f"{aid}: missing required ADR field {field}")
            require(all(driver in body[1] for driver in d["drivers"]), f"{aid}: drivers not in ADD")
    require(decisions.get("ADR-003", {}).get("status") == "Substituída" and "[ADR-013](#adr-013)" in add,
            "ADR-003 replacement history missing")
    expected_imps = re.findall(r"^### (IMP-\d{3}) —", add, re.M)
    require(Counter(expected_imps) == Counter(imps.keys()) and len(imps) == expected["implementation_tasks"],
            "Implementation tasks missing/duplicated")
    for iid, i in imps.items():
        body = re.search(rf"^### {iid} — [^\n]*\n(.*?)(?=^### |^## |\Z)", add, re.M | re.S)
        require(i["size"] in {"S", "M"}, f"{iid}: oversized task must be decomposed")
        require(bool(i["criteria"]) and bool(i["files"]) and bool(i["test_ids"]), f"{iid}: incomplete task")
        require(set(i["adr_ids"]) <= decisions.keys() and bool(i["adr_ids"]), f"{iid}: unknown/missing ADR")
        require(set(i["requirements"]) <= requirements.keys(), f"{iid}: unknown requirement")
        if body:
            for criterion in i["criteria"]:
                require(normalized(criterion) in normalized(body[1]), f"{iid}: criterion absent from ADD")
            require(all(t in body[1] for t in i["test_ids"]), f"{iid}: tests absent from ADD")
    matrix_rows = re.findall(r"^\| ((?:REQ|NFR|CON|INT)-\d{3}) \|", add, re.M)
    require(Counter(matrix_rows) == Counter(requirements.keys()), "ADD traceability matrix missing/duplicated row")
    require(Counter(r["requirement"] for r in catalog["coverage"]) == Counter(requirements.keys()),
            "Derived traceability missing/duplicated requirement")
    for row in catalog["coverage"]:
        rid = row["requirement"]
        require(all(row[k] for k in ("adrs", "components", "implementation_tasks", "tests")),
                f"{rid}: uncovered requirement")
        require(set(row["adrs"]) <= decisions.keys(), f"{rid}: unknown ADR in coverage")
        require(set(row["implementation_tasks"]) <= imps.keys(), f"{rid}: unknown IMP in coverage")
        require(all(rid in decisions[a]["drivers"] for a in row["adrs"] if a in decisions),
                f"{rid}: coverage ADR does not declare the requirement driver")
        line = next((l for l in add.splitlines() if l.startswith("| " + rid + " |")), "")
        require(all(value in line for key in ("adrs", "components", "implementation_tasks", "tests") for value in row[key]),
                f"{rid}: matrix/index mismatch")
    for key, default, role, validation, use in catalog["parameters"]:
        rows = [line for line in add.splitlines() if line.startswith("| " + chr(96) + key + chr(96) + " |")]
        require(len(rows) == 1, f"{key}: parameter missing or duplicated")
        if rows:
            require(str(default).replace("0.2", "0,2") in rows[0] and validation in rows[0] and role in rows[0],
                    f"{key}: parameter contract differs from derived index")
    require(catalog["approval"]["responses"] == ["1A", "2A", "3B", "4A"], "Approval record mismatch")
    require("Sem recuperação automática de falha exclusiva UDP" in add and
            "sem watchdog/rerouting novo" in add, "Approved UDP limitation missing")
    checks.append(f"ADD: 13 secções; {len(decisions)} ADR com driver/alternativa; matriz {len(requirements)}/{len(requirements)} sem órfãos.")

    tasks = {}
    for p in sorted((ROOT / "docs/tasks").glob("PHASE-*/TASK-*.md")):
        text = p.read_text()
        line = next((l for l in section(text, 1).splitlines() if l.startswith("- Dependências:")), "")
        tasks[p.stem] = dict(text=text, deps=list(dict.fromkeys(re.findall(r"TASK-\d{3}", line))))
    require(len(tasks) == expected["tasks"], "Incorrect source TASK count")
    validate_dag({k: v["deps"] for k, v in tasks.items()}, "TASK")
    units, owners = {}, []
    for p in sorted((base / "units").glob("*/unit-brief.md")):
        m, _ = read_metadata(p)
        require(m["unit"] == p.parent.name and m["intent"] == manifest["intent"], f"{p}: invalid unit identity")
        units[p.parent.name] = m
        owners.extend(m["owned_requirements"])
    require(len(units) == expected["units"] and Counter(owners) == Counter(requirements.keys()),
            "Unit count or unique requirement owner differs")
    validate_dag({k: v["requires_units"] for k, v in units.items()}, "Unit")
    stories, story_imps = {}, Counter()
    story_index = (ROOT / "memory-bank/story-index.md").read_text()
    for p in sorted((base / "units").glob("*/stories/*.md")):
        m, text = read_metadata(p)
        uid, sid = p.parent.parent.name, p.stem
        require(m["unit"] == uid and m["id"] == sid and uid in units, f"{p}: invalid story identity")
        require(m["intent"] == manifest["intent"], f"{p}: incorrect intent")
        require(f"{uid}/{sid}.md ✅ GENERATED" in story_index, f"{sid}: not indexed")
        require(len(m["implementation_tasks"]) == 1, f"{sid}: story must cover one atomic task")
        for iid in m["implementation_tasks"]:
            story_imps[iid] += 1
            require(iid in imps, f"{sid}: unknown IMP")
            if iid in imps:
                i = imps[iid]
                require(m["bolt"] == i["bolt"] and m["unit"] == i["unit"], f"{sid}: mapping mismatch")
                require(m["source_tasks"] == i["source_tasks"], f"{sid}: parent TASK mismatch")
                require(m["requirements"] == i["requirements"] and m["adr_ids"] == i["adr_ids"], f"{sid}: driver/ADR mismatch")
                for criterion in i["criteria"]:
                    require(normalized(criterion) in normalized(text), f"{sid}: missing source acceptance criterion")
        stories[(uid, sid)] = m
    require(len(stories) == expected["stories"] and story_imps == Counter(imps.keys()), "Story/IMP coverage not unique")
    bolts, used_stories, bolt_imps = {}, Counter(), Counter()
    fields = {"id", "unit", "intent", "type", "status", "stories", "created", "requires_bolts", "enables_bolts",
              "requires_units", "blocks", "complexity", "source_tasks", "source_task_order", "validation_ids",
              "external_gates", "execution_agent", "implementation_tasks", "adr_ids", "requirements",
              "test_ids", "architecture_source", "size"}
    for p in sorted((ROOT / "memory-bank/bolts").glob("*/bolt.md")):
        m, text = read_metadata(p)
        bid = p.parent.name
        require(fields <= m.keys(), f"{bid}: required fields missing")
        require(m["id"] == bid and m["intent"] == manifest["intent"] and m["unit"] in units, f"{bid}: invalid identity")
        require(m["architecture_source"] == "add.md" and m["execution_agent"] == "construction", f"{bid}: incorrect authority/executor")
        require(m["status"] in {"planned", "in-progress", "complete", "blocked"}, f"{bid}: invalid status")
        require(len(m["stories"]) == 1 and len(m["implementation_tasks"]) == 1, f"{bid}: must cover one atomic story/task")
        require(m["source_tasks"] == m["source_task_order"], f"{bid}: parent order mismatch")
        require(m["validation_ids"] == ["V-" + t.split("-")[1] for t in m["source_tasks"]], f"{bid}: missing V-XXX")
        require(set(m["complexity"]) == {"avg_complexity", "avg_uncertainty", "max_dependencies", "testing_scope"} and
                all(isinstance(v, (int, float)) and 1 <= v <= 3 for v in m["complexity"].values()),
                f"{bid}: invalid complexity")
        require(m["type"] in {"ddd-construction-bolt", "simple-construction-bolt"}, f"{bid}: invalid type")
        require((ROOT / ".specsmd/aidlc/templates/construction/bolt-types" / (m["type"] + ".md")).exists(), f"{bid}: missing type definition")
        stages = ["model", "design", "adr", "implement", "test"] if m["type"] == "ddd-construction-bolt" else ["plan", "implement", "test"]
        require(re.findall(r"^\d+\. \[.\] \*\*([a-z]+)\*\*", text, re.M) == stages, f"{bid}: missing or misordered execution stages")
        for field in ("created", "updated", "started", "completed"):
            value = m.get(field)
            require(value is None or isinstance(value, str) and re.fullmatch(r"\d{4}-\d\d-\d\dT\d\d:\d\d:\d\dZ", value),
                    f"{bid}: {field} must include UTC date/time")
        if m["status"] == "planned":
            require(all(m.get(k) is None for k in ("started", "completed", "current_stage")) and m["stages_completed"] == [],
                    f"{bid}: fabricated execution progress")
        for sid in m["stories"]:
            key = m["unit"], sid
            used_stories[key] += 1
            require(key in stories and stories[key]["bolt"] == bid, f"{bid}: missing or inconsistent story")
        for iid in m["implementation_tasks"]:
            bolt_imps[iid] += 1
            require(iid in imps, f"{bid}: unknown IMP")
            if iid in imps:
                i = imps[iid]
                require(i["bolt"] == bid and m["source_tasks"] == i["source_tasks"] and m["size"] == i["size"],
                        f"{bid}: task identity/size mismatch")
                for f in ("requires_bolts", "requires_units", "enables_bolts", "adr_ids", "requirements", "test_ids"):
                    require(m[f] == i[f], f"{bid}: ADD/index mismatch in {f}")
                for criterion in i["criteria"]:
                    require(normalized(criterion) in normalized(text), f"{bid}: missing acceptance criterion")
                require("add.md#" + iid.lower() in text, f"{bid}: missing normative IMP link")
        bolts[bid] = m
    require(len(bolts) == expected["bolts"] and bolt_imps == Counter(imps.keys()) and
            used_stories == Counter(stories.keys()), "Bolt/IMP/story coverage not unique")
    graph = {k: v["requires_bolts"] for k, v in bolts.items()}
    validate_dag(graph, "Bolt")
    by_parent = {tid: {bid for bid, m in bolts.items() if tid in m["source_tasks"]} for tid in tasks}
    require(all(by_parent.values()), "Some source TASK has no implementation increment")
    for bid, m in bolts.items():
        deps = ancestors(bid, graph)
        require(set(m["enables_bolts"]) == {k for k, v in graph.items() if bid in v}, f"{bid}: enables mismatch")
        for tid in m["source_tasks"]:
            require(tid in tasks, f"{bid}: unknown parent TASK")
            for dep in tasks.get(tid, {}).get("deps", []):
                require(by_parent.get(dep, set()) <= deps | {bid}, f"{bid}: lost TASK dependency {tid} -> {dep}")
        for uid in m["requires_units"]:
            require(uid in units and uid != m["unit"], f"{bid}: invalid unit gate")
            require({k for k, v in bolts.items() if v["unit"] == uid} <= deps, f"{bid}: unit gate omits required bolts")
        require(m["blocks"] == any(bolts.get(d, {}).get("status") != "complete" for d in m["requires_bolts"]),
                f"{bid}: blocks flag mismatch")
    for tid, planned in manifest["task_to_bolts"].items():
        require(set(planned) == by_parent.get(tid, set()), f"{tid}: snapshot parent/parts mapping mismatch")
    checks.append(f"Unidades: {len(units)}; histórias/IMP/bolts: {len(stories)}/{len(imps)}/{len(bolts)}; TASK pai: {len(tasks)}.")
    checks.append("DAG sem ciclos; dependências de todas as partes e gates de fase preservados; nenhum IMP L.")
    fixtures_dir = ROOT / "Projeto/src/test/resources/protocol"
    fixtures = json.loads((fixtures_dir / "manifest.json").read_text())["fixtures"]
    for item in fixtures:
        data = (fixtures_dir / item["binary"]).read_bytes()
        require(len(data) == item["bytes"] and hashlib.sha256(data).hexdigest() == item["sha256"],
                f"{item['id']}: size/hash mismatch")
        require(bytes.fromhex((fixtures_dir / item["hex"]).read_text()) == data, f"{item['id']}: hex mismatch")
    xml_files = list((ROOT / "CORE/topologies").glob("*.xml"))
    for p in xml_files:
        ET.parse(p)
    checks.append(f"Fixtures verificadas: {len(fixtures)}; XML CORE analisáveis: {len(xml_files)} (sem execução).")
    paths = list((ROOT / "memory-bank").rglob("*.md")) + list((ROOT / ".specsmd").rglob("*.md"))
    paths += [ROOT / "add.md", ROOT / "specs.md"] + list((ROOT / "docs").rglob("*.md"))
    paths += [ROOT / name for name in (
        "README.md", "docs/00-README.md", "docs/07-API-AND-INTERFACES.md", "docs/08-IMPLEMENTATION-ROADMAP.md",
        "docs/09-TESTING-STRATEGY.md", "docs/12-RISKS-AND-DECISIONS.md", "docs/15-PROJECT-PROGRESS.md",
        "docs/AI-IMPLEMENTATION-GUIDE.md", "docs/17-INCEPTION-AUDIT.md", "docs/18-CONSTRUCTION-BOLTS.md",
        "docs/tasks/PHASE-02/TASK-006.md")]
    report_path = (ROOT / args.report).resolve() if args.report else None
    if report_path and report_path.is_file():
        paths.append(report_path)
    links, anchor_cache = 0, {}
    for p in sorted(set(paths)):
        text = p.read_text(encoding="utf-8")
        require(len(re.findall("^" + chr(96) * 3, text, re.M)) % 2 == 0, f"{p}: unclosed code fence")
        require(all(l.rstrip() == l for l in text.splitlines()), f"{p}: trailing whitespace")
        for target in re.findall(r"\[[^\]]*\]\(([^)]+)\)", text):
            target = target.strip("<>")
            if re.match(r"^[a-zA-Z][\w+.-]*:", target):
                continue
            filename, _, anchor = target.partition("#")
            dest = (p.parent / unquote(filename)).resolve() if filename else p
            require(dest.exists(), f"{p.relative_to(ROOT)}: missing target {target}")
            if anchor and dest.is_file() and dest.suffix == ".md":
                if dest not in anchor_cache:
                    dt = dest.read_text()
                    values = set(re.findall(r'<a\s+(?:id|name)="([^"]+)"', dt))
                    for heading in re.findall(r"^#{1,6}\s+(.+?)\s*#*\s*$", dt, re.M):
                        heading = re.sub(r"<[^>]+>", "", heading).lower()
                        values.add(re.sub(r"[^\w\- ]", "", heading).replace(" ", "-"))
                    anchor_cache[dest] = values
                require(unquote(anchor) in anchor_cache[dest], f"{p.relative_to(ROOT)}: missing anchor {target}")
            links += 1
    checks.append(f"Referências locais/âncoras verificadas: {links}.")
    if args.check_snapshot:
        for filename, digest in manifest["files"].items():
            p = ROOT / filename
            require(p.is_file() and hashlib.sha256(p.read_bytes()).hexdigest() == digest, f"Source changed since import: {filename}")
        checks.append(f"Snapshot das fontes importadas: {len(manifest['files'])} hashes conferidos.")
    stamp = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    command = "python3 scripts/validate-planning.py" + (" --check-snapshot" if args.check_snapshot else "")
    if args.report:
        command += " --report " + str(args.report)
    lines = ["# Validação do ADD e planeamento Construction", "", f"Execução: {stamp}. Base: {manifest['baseline_revision']}.",
             "Comando: " + command, "", "## Resultado", ""] + ["- " + c for c in checks]
    lines += [f"- Erros encontrados: {len(errors)}.", "", "## Alcance", "",
              "Validação estrutural/documental e de bytes de referência. Não executa Maven,",
              "testes Java, rede entre processos, GUI, CORE ou submissão. Os 149 testes",
              "continuam a ser evidência histórica, não resultado desta execução.",
              "A nova organização continua sujeita à revisão humana; nenhum bolt iniciado."]
    if errors:
        lines += ["", "## Erros", ""] + ["- " + e for e in errors]
    report = "\n".join(lines) + "\n"
    if report_path:
        report_path.parent.mkdir(parents=True, exist_ok=True)
        report_path.write_text(report, encoding="utf-8")
    print(report)
    return bool(errors)


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (KeyError, ValueError, OSError, ET.ParseError) as error:
        print(f"Validation failed: {error}", file=sys.stderr)
        sys.exit(1)
