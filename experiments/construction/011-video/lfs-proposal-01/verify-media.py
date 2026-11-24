#!/usr/bin/env python3
"""Read-only verification of real media in a hydrated tree or future Codigo.zip."""
import argparse
import hashlib
import json
from pathlib import Path
import stat
import sys
import zipfile

EXPECTED = {'Projeto/src/main/resources/media/a.Mjpeg': {'bytes': 4269893, 'sha256': 'ca461cb1edce18efb355794ffb7d52f73dc7ce4c03422224894eb0fc550b0cca'}, 'Projeto/src/main/resources/media/b.Mjpeg': {'bytes': 111255866, 'sha256': 'ceb6497674bde7210bff2162b07e4305a74b33ed2b5a381b3e973d30fc837c5f'}, 'Projeto/src/main/resources/media/c.Mjpeg': {'bytes': 167319819, 'sha256': 'd2a050894ced76969b2bafae669532141dccf770fa2352e9ac4d0a4374bed3ab'}, 'Projeto/src/main/resources/media/d.Mjpeg': {'bytes': 176974550, 'sha256': 'c47ef9b41f05904e1bb6657424e2d88fe504e0925c0dffbdee85c58bf8c8e4cc'}, 'experiments/media/manifest.json': {'bytes': 24993, 'sha256': 'b5555a03c31f51b8eb392f2df7bbc1bb1fd24a46faf2f3a6e6ccfb184833b51f'}}

POINTER = b'version https://git-lfs.github.com/spec/v1'


def verify(stream, actual_size, expected, label):
    first = stream.read(128)
    if first.startswith(POINTER):
        raise ValueError('Ponteiro Git LFS em vez do ficheiro real: '+label)
    if actual_size != expected['bytes']:
        raise ValueError('Comprimento diferente do aprovado: '+label)
    digest = hashlib.sha256(first)
    count = len(first)
    for block in iter(lambda: stream.read(1048576), b''):
        count += len(block)
        if count > expected['bytes']:
            raise ValueError('Limite de bytes excedido: '+label)
        digest.update(block)
    if count != expected['bytes'] or digest.hexdigest() != expected['sha256']:
        raise ValueError('SHA-256 diferente do aprovado: '+label)
    return {'path':label, 'bytes':count, 'sha256':digest.hexdigest(), 'real_bytes':True}


def main():
    parser = argparse.ArgumentParser()
    mode = parser.add_mutually_exclusive_group(required=True)
    mode.add_argument('--root',type=Path)
    mode.add_argument('--zip',type=Path)
    parser.add_argument('--prefix',default='',help='Prefixo literal dentro do ZIP,por exemplo Codigo/')
    args = parser.parse_args()
    results=[]
    if args.root is not None:
        if args.prefix:
            parser.error('--prefix só se aplica ao ZIP')
        for name, expected in EXPECTED.items():
            path=args.root/name
            if not path.is_file() or path.is_symlink():
                raise ValueError('Ficheiro real ausente ou symlink: '+str(path))
            with path.open('rb') as stream:
                results.append(verify(stream,path.stat().st_size,expected,name))
    else:
        prefix=args.prefix
        if prefix and (prefix.startswith('/') or '..' in Path(prefix).parts or not prefix.endswith('/')):
            parser.error('Prefixo deve ser relativo e terminar em /')
        with zipfile.ZipFile(args.zip) as archive:
            names=archive.namelist()
            for name,expected in EXPECTED.items():
                key=prefix+name
                if names.count(key)!=1:
                    raise ValueError('Entrada ausente ou duplicada: '+key)
                info=archive.getinfo(key)
                if info.is_dir() or stat.S_ISLNK(info.external_attr >> 16):
                    raise ValueError('Entrada não é ficheiro regular: '+key)
                with archive.open(info) as stream:
                    results.append(verify(stream,info.file_size,expected,key))
    print(json.dumps({'status':'passed','scope':'Data integrity only;no delivery/redistribution approval',
                      'media_files':4,'media_bytes':459820128,'manifest_verified':True,
                      'files':results,'LOCAL04':'B credits unreadable;open for TASK-030',
                      'G_MEDIA_DISTRIBUTION':'open'},indent=2))
    return 0


if __name__=='__main__':
    try:
        sys.exit(main())
    except (OSError,ValueError,zipfile.BadZipFile) as error:
        print('VERIFICATION_FAILED: '+str(error),file=sys.stderr)
        sys.exit(1)
