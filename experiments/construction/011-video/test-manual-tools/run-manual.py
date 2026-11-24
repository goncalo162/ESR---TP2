#!/usr/bin/env python3
"""Human inspection of pinned final media; no redistribution/readability approval inferred."""
import argparse
import datetime
import hashlib
import html
import json
from pathlib import Path
import shlex
import sys
import tempfile
import uuid

TOOLS = Path(__file__).resolve().parent
ROOT = TOOLS.parents[3]


def sha(path):
    value = hashlib.sha256()
    with Path(path).open('rb') as stream:
        for block in iter(lambda: stream.read(1048576), b''):
            value.update(block)
    return value.hexdigest()


def prepare_pack(root, destination, config):
    """Copy selected original JPEG bytes after checking pinned index and asset hashes."""
    sample_root = destination / 'samples'
    sample_root.mkdir()
    parts = ['<!doctype html><html lang="pt"><meta charset="utf-8"><title>IMP-017 — Inspeção humana</title>',
             '<style>body{font:18px sans-serif;max-width:1400px;margin:30px auto;background:#eee;color:#111}',
             'img{display:block;max-width:100%;height:auto;background:#111}figure{display:inline-block;vertical-align:top;',
             'padding:10px;margin:8px;background:white}section{border-top:3px solid #777;padding:20px 0}',
             'figcaption{max-width:640px;font-size:15px}a{margin-right:12px}</style>',
             '<h1>IMP-017 — Manual sobre os ficheiros finais</h1>',
             '<p>Imagens JPEG originais,sem recorte ou redimensionamento adicional. A edição preparada já inclui ',
             'as transformações registadas. Abra imagens individualmente para escala nativa. ',
             'A posição é calculada por índice/FPS configurado; não mede reprodução.</p>',
             '<p>Inspecionar amostras não aprova redistribuição,legibilidade de todos os créditos,Test ou GUI/CORE. ',
             'A autoria/licença audiovisual de A continua por confirmar. Registe limitações no terminal.</p><nav>']
    for alias in 'ABCD':
        parts.append(f'<a href="#content-{alias}">Conteúdo {alias}</a>')
    for alias in 'BCD':
        parts.append(f'<a href="#credits-{alias}">Créditos {alias}</a>')
    parts.append('</nav>')
    witnesses = []
    for asset in config['assets']:
        alias = asset['alias']
        path = root / asset['path']
        if sha(path) != asset['sha256']:
            raise RuntimeError('Asset hash changed: ' + str(path))
        index_path = root / asset['records']
        if sha(index_path) != asset['records_sha256']:
            raise RuntimeError('Audit inventory changed: ' + str(index_path))
        rows = [json.loads(line) for line in index_path.read_text().splitlines()]
        count = len(rows)
        if count != asset['frames']:
            raise RuntimeError('Frame count mismatch')
        content = sorted({0, count//10, count//4, count//2, 3*count//4, 9*count//10, count-1})
        credits = sorted(set(range(max(0, count-1500), count, 50)) | {count-1}) if alias != 'A' else []
        alias_root = sample_root / alias
        alias_root.mkdir()
        selected = sorted(set(content + credits))
        copied = {}
        with path.open('rb') as source:
            for number in selected:
                row = rows[number]
                source.seek(row['offset'])
                prefix = source.read(5)
                if len(prefix) != 5 or not all(48 <= c <= 57 for c in prefix):
                    raise RuntimeError('Invalid prefix at selected offset')
                length = int(prefix)
                if not 1 <= length <= 99999 or length != row['length']:
                    raise RuntimeError('Invalid selected length')
                jpeg = source.read(length)
                if len(jpeg) != length or hashlib.sha256(jpeg).hexdigest() != row['jpeg_sha256']:
                    raise RuntimeError('Selected JPEG differs from audited bytes')
                output = alias_root / (str(number) + '.jpg')
                output.write_bytes(jpeg)
                copied[number] = output.relative_to(destination).as_posix()
                witnesses.append({'alias': alias, 'index': number, 'offset': row['offset'],
                                  'jpeg_sha256': row['jpeg_sha256'], 'file': copied[number],
                                  'sample_types': [kind for kind, values in [('content', content), ('credits', credits)]
                                                   if number in values]})
        for label, numbers in [('content', content), ('credits', credits)]:
            if not numbers:
                continue
            title = 'Conteúdo' if label == 'content' else 'Créditos'
            parts.append(f'<section id="{label}-{alias}"><h2>{title} {alias}: {html.escape(asset["title"])}</h2>')
            parts.append(f'<p>Origem {asset["server"]}; FPS configurado {asset["fps"]}; '
                         f'SHA-256 {asset["sha256"]}</p>')
            if label == 'credits':
                parts.append('<p>Últimos150s calculados,amostras a cada5s. Confirme presença e classifique ',
                             )
                parts.append('legibilidade sem inferir aprovação. Em B,a fonte320×180 tem texto pequeno; ',
                             )
                parts.append('o upscale não recupera detalhe. Se não conseguir confirmar,registe a divergência e pare.</p>')
            for number in numbers:
                link = copied[number]
                parts.append(f'<figure><a href="{link}" target="_blank"><img src="{link}" alt="{alias} frame {number}"></a>'
                             f'<figcaption>Índice {number}; posição calculada {number/asset["fps"]:.1f}s</figcaption></figure>')
            parts.append('</section>')
    parts.append('</html>')
    (destination / 'index.html').write_text(''.join(parts))
    (destination / 'samples.json').write_text(json.dumps(witnesses, indent=2)+'\n')
    return witnesses


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--operator', required=True)
    parser.add_argument('--kind', choices=['human', 'agent'], required=True)
    args = parser.parse_args()
    if args.kind == 'human' and not sys.stdin.isatty():
        parser.error('Use um terminal interativo; não confirmar por pipe')
    config = json.loads((TOOLS / 'approved-media.json').read_text())
    if sha(Path(__file__)) != config['runner_sha256']:
        raise SystemExit('Runner changed;stop')
    stamp = datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%dT%H%M%SZ')
    run = TOOLS.parent / 'test-manual-runs' / (stamp+'-'+uuid.uuid4().hex[:8])
    run.mkdir(parents=True, exist_ok=False)
    report = {'operator': args.operator, 'kind': args.kind, 'started_at': stamp,
              'media_manifest_sha256': config['manifest_sha256'], 'assets': config['assets'],
              'runner_sha256': sha(Path(__file__)), 'config_sha256': sha(TOOLS/'approved-media.json'), 'checks_passed': 0, 'stages_confirmed': 0,
              'answers': {}, 'credits_legibility': {}, 'passed': False,
              'G_MEDIA_DISTRIBUTION': 'open;not approved by this manual',
              'Test_approval': 'not inferred;user approval required',
              'final_review': 'pending', 'integration': 'pending',
              'app_processes_started': False, 'JVM_started': False}
    exit_code = 1
    with (run / 'manual.log').open('w') as log:
        def emit(text):
            print(text, flush=True)
            log.write(text+'\n')
            log.flush()
        def check(label):
            report['checks_passed'] += 1
            emit('CHECK_PASS '+str(report['checks_passed'])+' '+label)
        def answer(key, question, choices=None):
            emit(question)
            value = input('> ').strip()
            emit('ANSWER '+key+'='+value)
            if not value or choices is not None and value not in choices:
                raise RuntimeError('Resposta vazia/inválida;manual incompleto')
            report['answers'][key] = value
            return value
        def confirmed(stage):
            report['stages_confirmed'] += 1
            emit('OPERATOR_CONFIRM_STAGE='+str(stage))
        try:
            emit('KIND='+args.kind+' OPERATOR='+args.operator)
            if sha(ROOT / 'experiments/media/manifest.json') != config['manifest_sha256']:
                raise RuntimeError('Approved media manifest changed')
            check('manifesto aprovado intacto')
            for asset in config['assets']:
                if sha(ROOT/asset['path']) != asset['sha256']:
                    raise RuntimeError('Asset changed: '+asset['alias'])
                check('hash '+asset['alias']+' intacto')
            witnesses = prepare_pack(ROOT, run, config)
            report['samples_count'] = len(witnesses)
            report['samples_sha256'] = sha(run/'samples.json')
            check('amostras são bytes JPEG auditados,sem edição')
            emit('Abra no navegador,sem iniciar a aplicação OTT:')
            emit('open '+shlex.quote(str(run/'index.html')))
            emit('Pode clicar nas imagens para inspeção à escala nativa. Browser é aberto pelo operador,não pelo runner.')
            for stage, pair in [(1, 'A/B'), (2, 'C/D')]:
                answer('pair-'+pair, 'Inspecione conteúdo '+pair+'. São identificáveis e diferentes? [sim/nao]', ['sim','nao'])
                if report['answers']['pair-'+pair] != 'sim':
                    raise RuntimeError('Conteúdos não confirmados como distintos: '+pair)
                answer('description-'+pair, 'Descreva brevemente o que observou em cada conteúdo:')
                confirmed(stage)
                check('par '+pair+' visualmente distinto por confirmação do operador')
            if answer('four-distinct', 'Comparando A/B/C/D,há quatro conteúdos distintos,não aliases/cópias da mesma obra? [sim/nao]', ['sim','nao']) != 'sim':
                raise RuntimeError('Quatro conteúdos distintos não confirmados')
            confirmed(3)
            check('quatro conteúdos distintos confirmados pelo operador')
            for stage, alias in enumerate('BCD', 4):
                if answer('credits-'+alias, 'Inspecione a secção créditos '+alias+'. Observou o credit scroll? [sim/nao]', ['sim','nao']) != 'sim':
                    raise RuntimeError('Créditos não confirmados nas amostras: '+alias)
                rating = answer('readability-'+alias, 'Legibilidade observada dos créditos '+alias+': [legivel/parcial/ilegivel]',
                                ['legivel','parcial','ilegivel'])
                report['credits_legibility'][alias] = rating
                answer('notes-'+alias, 'Registe limites/divergências/detalhes observados (obrigatório;use sem divergencias se aplicável):')
                confirmed(stage)
                check('créditos '+alias+' inspecionados;legibilidade registada '+rating)
            answer('limits', 'Confirma que A tem licença audiovisual por verificar,G-MEDIA-DISTRIBUTION continua aberto e LOCAL-03 não instalou Projeto/media? [confirmo]', ['confirmo'])
            confirmed(7)
            check('limites/gates reconhecidos,sem aprovação de redistribuição')
            report['passed'] = report['checks_passed']==13 and report['stages_confirmed']==7
            report['legibility_fully_confirmed_in_samples'] = all(v=='legivel' for v in report['credits_legibility'].values())
            report['all_credits_legible'] = 'not inferred from sampling'
            exit_code = 0 if report['passed'] else 1
        except (Exception, KeyboardInterrupt) as exc:
            report['passed'] = False
            report['error'] = type(exc).__name__+': '+str(exc)
            emit('MANUAL_ERROR='+report['error'])
        finally:
            report['WRAPPER_EXIT'] = exit_code
            report['finished_at'] = datetime.datetime.now(datetime.timezone.utc).isoformat()
            if report['passed']:
                emit(('MANUAL_PASS' if args.kind=='human' else 'AGENT_PROBE_PASS')+' CHECKS=13')
            emit('PASSED='+str(report['passed']).lower())
            emit('WRAPPER_EXIT='+str(exit_code))
            emit('NO_APP_PROCESSES_STARTED=true')
            emit('LEGIBILITY_AND_REDISTRIBUTION_NOT_APPROVED_BY_AUTOMATION')
            (run/'report.json').write_text(json.dumps(report,indent=2)+'\n')
            emit('REPORT='+str(run/'report.json'))
    return exit_code


if __name__ == '__main__':
    sys.exit(main())
