#!/usr/bin/env python3
"""Reconstruct base + explicit integration allowlist without using any Git index."""
import argparse
import datetime
import hashlib
import json
from pathlib import Path
import re
import shutil
import subprocess
import tarfile
from urllib.parse import unquote

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--directory', required=True, type=Path)
    parser.add_argument('--receipt', required=True, type=Path)
    args = parser.parse_args()
    tree = args.directory.resolve(); receipt = args.receipt.resolve()
    if tree.exists() or receipt.is_relative_to(tree) or tree.is_relative_to(ROOT):
        raise RuntimeError('Use a new external simulation tree and a receipt outside that tree')
    manifest = json.loads((HERE / 'integration-manifest.json').read_text())
    paths = (HERE / 'integration-allowlist.txt').read_text().splitlines()
    if paths != [row['path'] for row in manifest['files']] or len(paths) != len(set(paths)):
        raise RuntimeError('Allowlist/manifest mismatch')
    for row in manifest['files']:
        p = Path(row['path'])
        if p.is_absolute() or '..' in p.parts or str(p).startswith('Projeto/target/'):
            raise RuntimeError('Invalid payload path: ' + str(p))
        original = ROOT / p
        if not original.is_file() or original.is_symlink():
            raise RuntimeError('Payload must be a regular file: ' + str(p))
        if row['sha256'] is not None and (sha(original) != row['sha256'] or original.stat().st_size != row['bytes']):
            raise RuntimeError('Payload changed: ' + str(p))
    tree.parent.mkdir(parents=True, exist_ok=True)
    archive = tree.parent / (tree.name + '-base.tar')
    subprocess.run(['git', 'archive', '--format=tar', '--output', str(archive), manifest['base']], cwd=ROOT, check=True)
    tree.mkdir()
    with tarfile.open(archive) as source:
        # Also works with the system Python versions predating tarfile extraction filters.
        for member in source:
            relative = Path(member.name)
            if relative.is_absolute() or '..' in relative.parts:
                raise RuntimeError('Unsafe base archive path')
            destination = tree / relative
            destination.parent.mkdir(parents=True, exist_ok=True)
            if member.isdir():
                destination.mkdir(exist_ok=True)
            elif member.isfile():
                with source.extractfile(member) as data, destination.open('wb') as target:
                    shutil.copyfileobj(data, target)
                destination.chmod(member.mode & 0o777)
            elif member.issym():
                if not (destination.parent / member.linkname).resolve().is_relative_to(tree):
                    raise RuntimeError('Base symlink escapes simulation')
                destination.symlink_to(member.linkname)
            else:
                raise RuntimeError('Unsupported base archive entry: ' + member.name)
    baseline = {str(p.relative_to(tree)): sha(p) for p in tree.rglob('*') if p.is_file()}
    for name in paths:
        destination = tree / name; destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(ROOT / name, destination); shutil.copymode(ROOT / name, destination)
    actual = {str(p.relative_to(tree)): sha(p) for p in tree.rglob('*') if p.is_file()}
    if set(actual) != set(baseline) | set(paths):
        raise RuntimeError('Unexpected/missing simulation files')
    if any(actual[p] != digest for p, digest in baseline.items() if p not in paths):
        raise RuntimeError('Unselected baseline file changed')
    if any(actual[p] != sha(ROOT / p) for p in paths):
        raise RuntimeError('Simulation does not match exact selected bytes')
    # Supplement, never replace or narrow V-ADD: check file links in every selected Markdown artifact.
    missing = []; checked = 0
    for name in paths:
        p = tree / name
        if p.suffix != '.md':
            continue
        text = re.sub(r'^```.*?^```[^\n]*$', '', p.read_text(), flags=re.M | re.S)
        for target in re.findall(r'\[[^\]]*\]\(([^)]+)\)', text):
            target = target.strip('<>')
            if re.match(r'^[a-zA-Z][\w+.-]*:', target):
                continue
            filename, _, _ = target.partition('#')
            destination = (p.parent / unquote(filename)).resolve() if filename else p
            checked += 1
            if not destination.is_relative_to(tree) or not destination.exists():
                missing.append({'source': name, 'target': target})
    # This command runs the unmodified validator FROM the reconstructed tree, not the worktree.
    validator = tree / 'scripts/validate-planning.py'
    result = subprocess.run(['python3', str(validator)], cwd=tree, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    print(result.stdout, end='')
    after = {str(p.relative_to(tree)): sha(p) for p in tree.rglob('*') if p.is_file()}
    if after != actual:
        raise RuntimeError('Validation mutated the frozen tree')
    record = {'checked_at': datetime.datetime.now(datetime.timezone.utc).isoformat(),
              'base': manifest['base'], 'tree': str(tree), 'payload_count': len(paths),
              'payload_sha256': {p: actual[p] for p in paths},
              'manifest_sha256': sha(HERE / 'integration-manifest.json'),
              'base_file_count': len(baseline), 'unchanged_base_files_not_selected': len(set(baseline) - set(paths)),
              'baseline_target_files_retained_without_payload_changes': sorted(p for p in baseline if p.startswith('Projeto/target/')),
              'no_target_payload_paths': True, 'no_index_git_commit_or_worktree_mutation': True,
              'validator_sha256': sha(validator), 'worktree_validator_sha256': sha(ROOT / 'scripts/validate-planning.py'),
              'validator_exit_code': result.returncode, 'validator_stdout': result.stdout,
              'selected_markdown_links_checked': checked, 'missing_selected_markdown_targets': missing,
              'frozen_tree_unchanged_by_validation': True}
    receipt.parent.mkdir(parents=True, exist_ok=True)
    with receipt.open('x') as stream:
        stream.write(json.dumps(record, indent=2) + '\n')
    print('EXACT_SIMULATION_RESULT ' + json.dumps({'payload_count':len(paths),'v_add_exit':result.returncode,
          'missing_selected_links':len(missing),'receipt':str(receipt),'receipt_sha256':sha(receipt),
          'manifest_sha256':record['manifest_sha256']}))
    return 0 if result.returncode == 0 and not missing else 1

if __name__ == '__main__':
    raise SystemExit(main())
