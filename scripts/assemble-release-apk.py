#!/usr/bin/env python3
"""Reconstruct signed release bytes from repository parts and verify before replacement."""
from pathlib import Path
import hashlib,json,os,tempfile
root=Path(__file__).resolve().parents[1]
source=root/'release-assets/test11'
manifest=json.loads((source/'manifest.json').read_text())
assert manifest['filename']=='kossack-touch-0.1.10-test11.apk'
destination=root/'site/downloads'/manifest['filename']
# Verify every part before creating or replacing the complete APK.
for part in manifest['parts']:
    assert Path(part['name']).name==part['name']
    data=(source/part['name']).read_bytes()
    if len(data)!=part['sizeBytes'] or hashlib.sha256(data).hexdigest()!=part['sha256']:
        raise SystemExit('Signed APK part checksum mismatch: '+part['name'])
destination.parent.mkdir(parents=True,exist_ok=True)
fd,name=tempfile.mkstemp(prefix='.signed-apk-',dir=destination.parent)
try:
    with os.fdopen(fd,'wb') as output:
        for part in manifest['parts']:
            with (source/part['name']).open('rb') as input_file:
                while chunk:=input_file.read(1024*1024):output.write(chunk)
    candidate=Path(name)
    if candidate.stat().st_size!=manifest['sizeBytes'] or hashlib.sha256(candidate.read_bytes()).hexdigest()!=manifest['sha256']:
        raise SystemExit('Assembled signed APK checksum mismatch')
    os.replace(candidate,destination)
finally:
    Path(name).unlink(missing_ok=True)
print('Signed APK reconstructed and SHA-256 verified:',manifest['filename'])
