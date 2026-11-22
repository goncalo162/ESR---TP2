#!/usr/bin/env python3
"""Wrapper do segundo marco (IMP-015 / V-018).

Cria uma pasta nova em runs/<utc>-<rand>/, grava manifest.json (data UTC,
commit, hash do JAR, versao Java, comandos), executa o auxiliar Milestone2.java
em modo fonte contra o JAR congelado com a saida em run.log, e escreve
result.json. Nunca sobrescreve execucoes anteriores. Sai 0 se o auxiliar
terminar 0 (todos os CHECK_PASS), 1 caso contrario.
"""
import argparse
import datetime
import hashlib
import json
import os
import platform
import random
import string
import subprocess
import sys


def sha256(path):
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(65536), b""):
            h.update(chunk)
    return h.hexdigest()


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--jar", required=True, help="caminho do JAR congelado (nao usar target/ solto)")
    ap.add_argument("--java", default="java", help="binario Java 21")
    ap.add_argument("--root", default=os.path.dirname(os.path.abspath(__file__)))
    args = ap.parse_args()

    jar = os.path.abspath(args.jar)
    if not os.path.isfile(jar):
        print(f"JAR nao encontrado: {jar}", file=sys.stderr)
        return 2
    jar_hash = sha256(jar)

    stamp = datetime.datetime.now(datetime.timezone.utc).strftime("%Y%m%dT%H%M%SZ")
    rand = "".join(random.choice(string.ascii_lowercase + string.digits) for _ in range(6))
    run = os.path.join(args.root, "runs", f"{stamp}-{rand}")
    os.makedirs(run, exist_ok=False)

    repo = os.path.dirname(args.root)
    commit = subprocess.run(["git", "rev-parse", "HEAD"], capture_output=True, text=True, cwd=repo).stdout.strip()
    branch = subprocess.run(["git", "branch", "--show-current"], capture_output=True, text=True, cwd=repo).stdout.strip()

    java_version = subprocess.run([args.java, "-version"], capture_output=True, text=True).stderr.strip().splitlines()
    manifest = {
        "run_id": os.path.basename(run),
        "created_utc": stamp,
        "bolt": "009-arvores",
        "imp": "IMP-015",
        "validation": "V-018",
        "git_commit": commit,
        "git_branch": branch,
        "jar": jar,
        "jar_sha256": jar_hash,
        "jar_bytes": os.path.getsize(jar),
        "java": java_version,
        "python": platform.python_version(),
        "command": [args.java, "--class-path", jar, os.path.join(args.root, "Milestone2.java"), run],
        "note": "Auxiliar de demonstracao executado em modo fonte contra o JAR; sem video/GUI/CORE/UDP.",
    }
    with open(os.path.join(run, "manifest.json"), "w") as f:
        json.dump(manifest, f, indent=2)

    log_path = os.path.join(run, "run.log")
    with open(log_path, "w") as log:
        log.write(json.dumps(manifest, indent=2) + "\n")
        proc = subprocess.run(
            [args.java, "--class-path", jar, os.path.join(args.root, "Milestone2.java"), run],
            stdout=log, stderr=subprocess.STDOUT,
        )

    checks_pass = 0
    checks_fail = 0
    with open(log_path) as f:
        for line in f:
            if "CHECK_PASS" in line:
                checks_pass += 1
            elif "CHECK_FAIL" in line:
                checks_fail += 1
    result = {
        "run_id": os.path.basename(run),
        "exit": proc.returncode,
        "checks_pass": checks_pass,
        "checks_fail": checks_fail,
        "jar_sha256": jar_hash,
        "verdict": "PASS" if proc.returncode == 0 and checks_fail == 0 else "FAIL",
    }
    with open(os.path.join(run, "result.json"), "w") as f:
        json.dump(result, f, indent=2)

    print(f"RUN_DIR={run}")
    print(f"EXIT={proc.returncode} CHECK_PASS={checks_pass} CHECK_FAIL={checks_fail} VERDICT={result['verdict']}")
    return 0 if result["verdict"] == "PASS" else 1


if __name__ == "__main__":
    sys.exit(main())
