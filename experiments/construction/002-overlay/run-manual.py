"""Capture the agent manual separately from the user's future repetition."""
from pathlib import Path
from datetime import datetime, timezone
import subprocess, json, hashlib
root = Path(__file__).resolve().parents[3]
out = Path(__file__).resolve().parent
meta = out / 'test-manual-command.json'
if meta.exists(): raise SystemExit('Preserve existing evidence; choose a new name for another run')
java = '/usr/lib/jvm/java-21-openjdk-amd64/bin/java'
source = out / 'ManualBootstrapCheck.java'
jar = root / 'Projeto/target/ott-streaming-1.0-SNAPSHOT.jar'
expected = json.loads((out / 'test-clean-verify-command.json').read_text())['jar_sha256']
assert hashlib.sha256(jar.read_bytes()).hexdigest() == expected
cmd = [java, '--class-path', str(jar.relative_to(root)), str(source.relative_to(root)), 'Codex', expected]
now = lambda: datetime.now(timezone.utc).strftime('%Y-%m-%dT%H:%M:%SZ')
started = now()
result = subprocess.run(cmd, cwd=root, text=True, capture_output=True, timeout=60)
finished = now()
log = out / 'test-manual.log'; log.write_text(result.stdout + result.stderr)
data = dict(command=cmd, cwd=str(root), started=started, finished=finished, operator='Codex',
            exit_code=result.returncode, check_pass=sum(s.startswith('CHECK_PASS ') for s in result.stdout.splitlines()),
            manual_pass=sum(s.startswith('MANUAL_PASS ') for s in result.stdout.splitlines()),
            log=str(log.relative_to(root)), log_sha256=hashlib.sha256(log.read_bytes()).hexdigest(),
            script_sha256=hashlib.sha256(source.read_bytes()).hexdigest(),
            jar_sha256_before=expected, jar_sha256_after=hashlib.sha256(jar.read_bytes()).hexdigest(),
            scope='Agent manual over real local TCP and public service API; user repetition/reviewer/final approval pending.')
meta.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n')
print(result.stdout + result.stderr)
print(json.dumps(data, ensure_ascii=False, indent=2))
raise SystemExit(result.returncode)
