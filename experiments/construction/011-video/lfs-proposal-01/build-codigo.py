#!/usr/bin/env python3
"""Proposed future packager. Never use git archive's LFS pointers as delivered media."""
import argparse
import hashlib
import importlib.util
import json
import os
from pathlib import Path, PurePosixPath
import re
import shutil
import stat
import subprocess
import sys
import tarfile
import tempfile
import zipfile


def digest(path):
    h=hashlib.sha256()
    with path.open('rb') as f:
        for block in iter(lambda:f.read(1048576),b''):
            h.update(block)
    return h.hexdigest()


def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--repo',type=Path,required=True)
    parser.add_argument('--commit',required=True,help='Full reviewed40-hex commit,not a moving branch')
    parser.add_argument('--output',type=Path,required=True)
    args=parser.parse_args()
    repo=args.repo.resolve()
    output=args.output.resolve()
    if not re.fullmatch('[0-9a-f]{40}',args.commit):
        raise ValueError('Specify the full reviewed commit SHA')
    if output.exists() or not output.parent.is_dir():
        raise ValueError('Output already exists or parent is missing;no overwrite')
    helper=Path(__file__).with_name('verify-media.py')
    spec=importlib.util.spec_from_file_location('verify_approved_media',helper)
    module=importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    expected=module.EXPECTED
    media=[name for name in expected if name.endswith('.Mjpeg')]
    def git(*parts):
        return subprocess.check_output(['git','-C',str(repo),*parts])
    if git('rev-parse',args.commit+'^{commit}').decode().strip()!=args.commit:
        raise ValueError('Not a valid reviewed commit')
    attrs=''.join('/'+name+' filter=lfs diff=lfs merge=lfs -text\n' for name in media)
    if git('show',args.commit+':.gitattributes').decode()!=attrs:
        raise ValueError('Commit attributes differ from the proposed exact4 rules')
    for name in media:
        size=int(git('cat-file','-s',args.commit+':'+name))
        if size>1024:
            raise ValueError('Commit contains raw data instead of the intended LFS pointer: '+name)
        lines=git('show',args.commit+':'+name).decode('ascii').splitlines()
        required=['version https://git-lfs.github.com/spec/v1',
                  'oid sha256:'+expected[name]['sha256'],'size '+str(expected[name]['bytes'])]
        if lines!=required:
            raise ValueError('Pointer oid/size differs from approved media: '+name)
    manifest='experiments/media/manifest.json'
    if hashlib.sha256(git('show',args.commit+':'+manifest)).hexdigest()!=expected[manifest]['sha256']:
        raise ValueError('Commit media manifest differs from approval')
    for name,value in expected.items():
        file=repo/name
        if not file.is_file() or file.is_symlink():
            raise ValueError('Missing real approved working file: '+name)
        with file.open('rb') as f:
            module.verify(f,file.stat().st_size,value,name)
    with tempfile.TemporaryDirectory(prefix='codigo-package-',dir=output.parent) as temp:
        work=Path(temp)
        archive=work/'source.tar'
        with archive.open('wb') as f:
            subprocess.run(['git','-C',str(repo),'archive','--format=tar',args.commit],stdout=f,check=True)
        candidate=work/'Codigo.zip'
        seen=set()
        with tarfile.open(archive) as source,zipfile.ZipFile(candidate,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as result:
            for member in source:
                name=member.name
                parts=PurePosixPath(name).parts
                if name.startswith('/') or '..' in parts:
                    raise ValueError('Unsafe archive path')
                if name=='Projeto/target' or name.startswith('Projeto/target/'):
                    continue
                if member.isdir():
                    continue
                if not member.isfile() or name in seen:
                    raise ValueError('Unsupported archive member or duplicate: '+name)
                seen.add(name)
                if name in media:
                    result.write(repo/name,name)
                else:
                    info=zipfile.ZipInfo(name)
                    info.compress_type=zipfile.ZIP_DEFLATED
                    info.external_attr=(stat.S_IFREG|member.mode)<<16
                    with source.extractfile(member) as inp,result.open(info,'w',force_zip64=True) as target:
                        shutil.copyfileobj(inp,target,1048576)
        if not set(expected)<=seen:
            raise ValueError('Required files absent from reviewed commit archive')
        with zipfile.ZipFile(candidate) as result:
            for name,value in expected.items():
                info=result.getinfo(name)
                with result.open(info) as stream:
                    module.verify(stream,info.file_size,value,name)
        # Exclusive creation:cannot replace an existing delivery archive.
        os.link(candidate,output)
    print(json.dumps({'status':'passed','reviewed_commit':args.commit,'archive':str(output),
                      'archive_sha256':digest(output),'archive_bytes':output.stat().st_size,
                      'real_media_verified':4,'media_bytes':459820128,'manifest_verified':True,
                      'redistribution_approval':'not inferred;G-MEDIA-DISTRIBUTION remains a separate required decision'},indent=2))
    return 0


if __name__=='__main__':
    try:
        sys.exit(main())
    except (OSError,ValueError,subprocess.CalledProcessError,zipfile.BadZipFile) as error:
        print('PACKAGING_FAILED: '+str(error),file=sys.stderr)
        sys.exit(1)
