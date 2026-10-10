"""Exercise atomic reconstruction and reject corrupt release inputs in isolation."""
from pathlib import Path
import hashlib, json, subprocess, tempfile

script = Path(__file__).resolve().parents[1] / 'scripts/assemble-release-apk.py'
checks = 0

def check(ok):
    global checks
    checks += 1
    assert ok, f'release assembly check {checks}'

with tempfile.TemporaryDirectory() as directory:
    root = Path(directory)
    (root / 'scripts').mkdir()
    (root / 'scripts/assemble-release-apk.py').write_bytes(script.read_bytes())
    source = root / 'release-assets/test11'
    source.mkdir(parents=True)
    destination = root / 'site/downloads/kossack-touch-0.1.10-test11.apk'
    destination.parent.mkdir(parents=True)
    parts = [b'first signed byte range', b'second signed byte range']
    full = b''.join(parts)
    manifest = {'filename': destination.name, 'sizeBytes': len(full),
                'sha256': hashlib.sha256(full).hexdigest(), 'parts': []}
    for index, data in enumerate(parts, 1):
        name = f'part-{index:02}.bin'
        (source / name).write_bytes(data)
        manifest['parts'].append({'name': name, 'sizeBytes': len(data),
                                  'sha256': hashlib.sha256(data).hexdigest()})
    def run(config):
        (source / 'manifest.json').write_text(json.dumps(config))
        return subprocess.run(['python3', str(root / 'scripts/assemble-release-apk.py')],
                              capture_output=True).returncode
    check(run(manifest) == 0)
    check(destination.read_bytes() == full)
    # Corruption must never overwrite a previously complete APK.
    (source / 'part-01.bin').write_bytes(b'corrupted')
    check(run(manifest) != 0)
    check(destination.read_bytes() == full)
    (source / 'part-01.bin').write_bytes(parts[0])
    # Valid individual parts in the wrong order must fail the complete digest.
    reordered = dict(manifest, parts=list(reversed(manifest['parts'])))
    check(run(reordered) != 0)
    check(destination.read_bytes() == full)
    wrong_size = dict(manifest, sizeBytes=len(full) + 1)
    check(run(wrong_size) != 0)
    check(destination.read_bytes() == full)
    check(not list(destination.parent.glob('.signed-apk-*')))
    # A missing part must fail without leaving an apparently downloadable APK.
    destination.unlink()
    (source / 'part-02.bin').unlink()
    check(run(manifest) != 0)
    check(not destination.exists())
    check(not list(destination.parent.glob('.signed-apk-*')))
print(f'PASS {checks} release assembly checks: exact bytes, corruption, ordering, size, missing part, atomic preservation and cleanup.')
