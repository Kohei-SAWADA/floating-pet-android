#!/usr/bin/env python3
"""Create a clean source ZIP after offline checks; never creates/pushes a Git repo."""
from pathlib import Path
import hashlib
import os
import subprocess
import sys
import zipfile

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT.parent.parent / 'deliverables' / 'Floating-Pet-1.2.0-dev-source.zip'
EXCLUDED_PARTS = {'.git', '.gradle', '.idea', '.verification', '__pycache__', 'build', 'dist', 'local-tools'}
EXCLUDED_NAMES = {'local.properties', '.env', 'keystore.properties', 'signing.properties', 'google-services.json'}
EXCLUDED_SUFFIXES = {'.apk', '.aab', '.apks', '.jks', '.keystore', '.p12', '.pfx', '.pem', '.key', '.class', '.log'}
subprocess.run([sys.executable, str(ROOT / 'scripts/check_repository.py')], check=True)
files = []
for path in sorted(ROOT.rglob('*')):
    rel = path.relative_to(ROOT)
    if any(part in EXCLUDED_PARTS for part in rel.parts) or path.name in EXCLUDED_NAMES or path.suffix in EXCLUDED_SUFFIXES:
        continue
    if path.is_symlink():
        raise SystemExit(f'Refusing symlink: {rel}')
    if path.is_file():
        files.append(path)
OUT.parent.mkdir(parents=True, exist_ok=True)
with zipfile.ZipFile(OUT, 'w', zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
    for path in files:
        rel = Path('floating-pet-android') / path.relative_to(ROOT)
        info = zipfile.ZipInfo(str(rel), (2026, 10, 6, 0, 0, 0))
        info.create_system = 3
        mode = 0o755 if os.access(path, os.X_OK) else 0o644
        info.external_attr = (0o100000 | mode) << 16
        info.compress_type = zipfile.ZIP_DEFLATED
        archive.writestr(info, path.read_bytes())
with zipfile.ZipFile(OUT) as archive:
    if archive.testzip() is not None:
        raise SystemExit('ZIP integrity check failed')
digest = hashlib.sha256(OUT.read_bytes()).hexdigest()
OUT.with_suffix('.zip.sha256').write_text(f'{digest}  {OUT.name}\n')
print(f'{len(files)} source files packed; {OUT.stat().st_size} bytes; SHA-256 {digest}')
print(OUT)
