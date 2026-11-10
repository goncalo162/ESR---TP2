from contextlib import contextmanager
import importlib.util
import json
import os
from pathlib import Path
import signal
import subprocess
import tempfile
import time
from unittest.mock import patch

path = Path('experiments/milestone-1/bench.py')
spec = importlib.util.spec_from_file_location('bench', path)
bench = importlib.util.module_from_spec(spec)
spec.loader.exec_module(bench)

def base(r):
    (r/'permission-probe.json').write_text('{"passed":true}')
    (r/'capture.log').write_text('tcpdump: listening on lo\n')
    (r/'control.pcap').write_bytes(b'\0'*100)
    (r/'BOOTSTRAP.exit.json').write_text('{"java_exit":0}')

def identity(r, p, cmd, start=None):
    (r/'capture-start.json').write_text(json.dumps({
        'pid':p.pid,'command':cmd,'boot_id':bench.BOOT_ID.read_text().strip(),
        'proc_start':bench.proc_start(p.pid) if start is None else start,
    }))

@contextmanager
def run_dir():
    with tempfile.TemporaryDirectory(prefix='ott-capture-guard-') as d:
        r = Path(d)
        (r/'tcpdump').symlink_to('/usr/bin/sleep')
        base(r)
        with patch.object(bench,'capture_command',return_value=[str(r/'tcpdump'),'30']), patch.object(bench,'load'):
            yield r, [str(r/'tcpdump'),'30']

def rejects(r, message):
    with patch.object(bench.subprocess,'run') as run:
        try:
            bench.cut(r,45101)
        except ValueError as error:
            assert message in str(error),str(error)
        else:
            raise AssertionError('Expected cut refusal')
        run.assert_not_called()

def wait_state(pid, desired):
    until = time.monotonic() + 2
    while bench.proc_fields(pid)[0] != desired:
        assert time.monotonic() < until
        time.sleep(.01)

with run_dir() as (r,cmd):
    rejects(r,'capture-start.json')
    print('PASS log/PCAP sem processo registado recusados')

with run_dir() as (r,cmd), subprocess.Popen(cmd) as process:
    identity(r,process,cmd)
    assert bench.active_capture(r)['pid']==process.pid
    process.kill()
    wait_state(process.pid,'Z')
    rejects(r,'recorded PID')
    process.wait()
    rejects(r,'recorded PID')
    print('PASS processo real vivo reconhecido, zombie e processo desaparecido recusados')

with run_dir() as (r,cmd), subprocess.Popen(cmd) as process:
    try:
        identity(r,process,cmd,start='0')
        rejects(r,'changed identity')
        identity(r,process,cmd)
        process.send_signal(signal.SIGSTOP)
        wait_state(process.pid,'T')
        rejects(r,'not active')
        process.send_signal(signal.SIGCONT)
        with subprocess.Popen(cmd) as duplicate:
            try:
                rejects(r,'exactly one matching tcpdump')
            finally:
                duplicate.kill()
    finally:
        process.kill()
    print('PASS identidade alterada, estado suspenso e comando duplicado recusados')

with run_dir() as (r,cmd), subprocess.Popen(cmd) as process:
    identity(r,process,cmd)
    def select_after_exit(_):
        process.kill()
        process.wait()
        return {'source_port':45101,'destination_port':56120}
    with patch.object(bench,'select_pair',side_effect=select_after_exit), patch.object(bench.subprocess,'run',return_value=subprocess.CompletedProcess([],0,'','')) as run:
        try:
            bench.cut(r,45101)
        except ValueError as error:
            assert 'capture identity or state' in str(error)
        else:
            raise AssertionError('Expected refusal after capture died during selection')
        assert [call.args[0][0] for call in run.call_args_list]==['ss']
        assert 'reason' in json.loads((r/'cut-aborted.json').read_text())
        assert not (r/'cut-result.json').exists()
    print('PASS desaparecimento durante a seleção abortou antes de sudo/ss -K')

with run_dir() as (r,cmd), subprocess.Popen(cmd) as process:
    try:
        identity(r,process,cmd)
        listener=subprocess.CompletedProcess([],0,'','')
        cut=subprocess.CompletedProcess([],0,'socket selected','')
        with patch.object(bench,'select_pair',return_value={'source_port':45101,'destination_port':56120}), patch.object(bench,'ss_query',return_value=[]), patch.object(bench.subprocess,'run',side_effect=[listener,cut]) as run:
            assert bench.cut(r,45101)==0
            assert run.call_args_list[-1].args[0][:3]==['sudo','-n','ss']
        assert json.loads((r/'cut-result.json').read_text())['capture_at_cut']['pid']==process.pid
    finally:
        process.kill()
    print('PASS caminho simulado de sucesso regista o estado da captura antes do corte')

with tempfile.TemporaryDirectory(prefix='ott-launch-capture-') as d:
    r=Path(d)
    (r/'permission-probe.json').write_text('{"passed":true}')
    (r/'tcpdump').symlink_to('/usr/bin/sleep')
    fake_sudo=r/'sudo'
    fake_sudo.write_text('#!/bin/sh\nshift\nprintf "tcpdump: listening on lo\\n"\nexec "$@"\n')
    fake_sudo.chmod(0o755)
    cmd=[str(r/'tcpdump'),'1']
    with patch.dict(os.environ,{'PATH':str(r)+':'+os.environ['PATH']}), patch.object(bench,'capture_command',return_value=cmd):
        assert bench.capture(r)==0
        record=json.loads((r/'capture-start.json').read_text())
        assert record['pid']>1 and record['state'] in bench.CAPTURE_STATES and record['command']==cmd
        assert 'CAPTURE_EXIT 0' in (r/'capture.log').read_text()
        try:
            bench.active_capture(r)
        except ValueError:
            pass
        else:
            raise AssertionError('Expected refusal after capture launcher finished')
    print('PASS lançador registou o PID do filho e recusou-o depois do fim')
