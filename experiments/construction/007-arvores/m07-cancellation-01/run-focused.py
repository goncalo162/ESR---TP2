"""Fresh core-only JDK21/JUnit diagnostic while unrelated CLI compilation is blocked.
No product source is patched or copied with modifications; this is not a Maven regression.
"""
from pathlib import Path
import datetime
import hashlib
import json
import os
import subprocess
import sys
import tempfile

ROOT = Path(__file__).resolve().parents[4]
BASE = Path(__file__).resolve().parent
label = sys.argv[1]
method = sys.argv[2] if len(sys.argv) > 2 else 'm07CancelledChildCannotBeReauthorizedByLateAckAcceptance'
out = BASE / label
out.mkdir(exist_ok=False)
repo = Path.home() / '.m2/repository'
coordinates = [
    ('org/junit/jupiter/junit-jupiter-api', '5.13.4'),
    ('org/junit/jupiter/junit-jupiter-engine', '5.13.4'),
    ('org/junit/platform/junit-platform-commons', '1.13.4'),
    ('org/junit/platform/junit-platform-engine', '1.13.4'),
    ('org/junit/platform/junit-platform-launcher', '1.13.4'),
    ('org/opentest4j/opentest4j', '1.3.0'),
    ('org/apiguardian/apiguardian-api', '1.1.2'),
]
jars = [repo / name / version / (name.rsplit('/', 1)[1] + '-' + version + '.jar') for name, version in coordinates]
assert all(p.is_file() for p in jars)
excluded = ROOT / 'Projeto/src/main/java/pt/uminho/esr/ott/app/OttApplication.java'
sources = [p for p in (ROOT / 'Projeto/src/main/java').rglob('*.java') if p != excluded]
sources.append(ROOT / 'Projeto/src/test/java/pt/uminho/esr/ott/routing/ActivationTest.java')
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
record = {'recorded_at': datetime.datetime.now(datetime.timezone.utc).isoformat(),
          'scope': 'fresh isolated focused core test; CLI excluded due preexisting compiler error, not full regression',
          'excluded': str(excluded.relative_to(ROOT)), 'method': method,
          'source_hashes': {str(p.relative_to(ROOT)): sha(p) for p in sources},
          'junit_jars': {str(p): sha(p) for p in jars}}
with tempfile.TemporaryDirectory(prefix='ott-m07-') as temporary:
    classes = Path(temporary) / 'classes'
    classes.mkdir()
    compile_command = ['javac', '--release', '21', '-encoding', 'UTF-8', '-cp', os.pathsep.join(map(str, jars)), '-d', str(classes), *map(str, sources)]
    compilation = subprocess.run(compile_command, cwd=ROOT, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    (out / 'compile.log').write_text(compilation.stdout)
    record['compile_exit'] = compilation.returncode
    if compilation.returncode:
        (out / 'result.json').write_text(json.dumps(record, indent=2) + '\n')
        raise SystemExit(compilation.returncode)
    script = '''import org.junit.platform.launcher.core.*;
import org.junit.platform.launcher.listeners.*;
import org.junit.platform.engine.discovery.DiscoverySelectors;
var listener = new SummaryGeneratingListener();
var launcher = LauncherFactory.create();
launcher.registerTestExecutionListeners(listener);
var request = LauncherDiscoveryRequestBuilder.request().selectors(DiscoverySelectors.selectMethod("pt.uminho.esr.ott.routing.ActivationTest", "METHOD")).build();
launcher.execute(request);
var summary = listener.getSummary();
summary.printTo(new java.io.PrintWriter(System.out, true));
summary.printFailuresTo(new java.io.PrintWriter(System.out, true));
System.out.println("M07_COUNTS tests=" + summary.getTestsFoundCount() + " failed=" + summary.getTestsFailedCount() + " skipped=" + summary.getTestsSkippedCount() + " aborted=" + summary.getTestsAbortedCount());
/exit (summary.getTestsFailedCount()==0L && summary.getTestsFoundCount()>0L ? 0 : 1)
'''.replace('METHOD', method)
    script_path = out / 'runner.jsh'
    script_path.write_text(script)
    command = ['jshell', '--class-path', os.pathsep.join(map(str, [classes, *jars])), str(script_path)]
    execution = subprocess.run(command, cwd=ROOT, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    (out / 'test.log').write_text(execution.stdout)
    record['test_exit'] = execution.returncode
    record['runner'] = str(script_path.relative_to(ROOT))
    (out / 'result.json').write_text(json.dumps(record, indent=2) + '\n')
    print(execution.stdout)
    raise SystemExit(execution.returncode)
