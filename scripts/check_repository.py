#!/usr/bin/env python3
"""Offline source-package checks; stdlib only. Not a replacement for Android lint/tests."""
from pathlib import Path
import hashlib
import json
import re
import struct
import sys
import xml.etree.ElementTree as ET
import zlib

ROOT = Path(__file__).resolve().parents[1]
errors = []
checks = 0

def check(condition, message):
    global checks
    checks += 1
    if not condition:
        errors.append(message)

def strings(path):
    tree = ET.parse(path)
    rows = tree.getroot().findall('string')
    result = {x.attrib['name']: ''.join(x.itertext()) for x in rows}
    check(len(result) == len(rows), f'Duplicate string name in {path.relative_to(ROOT)}')
    return result

def alpha_rows(path):
    data = path.read_bytes()
    check(data[:8] == b'\x89PNG\r\n\x1a\n', 'Sample must be PNG')
    offset, compressed, info = 8, bytearray(), None
    while offset < len(data):
        length = struct.unpack_from('>I', data, offset)[0]
        kind = data[offset + 4:offset + 8]
        payload = data[offset + 8:offset + 8 + length]
        checksum = struct.unpack_from('>I', data, offset + 8 + length)[0]
        check(zlib.crc32(kind + payload) & 0xffffffff == checksum, f'PNG {kind!r} CRC mismatch')
        if kind == b'IHDR':
            info = struct.unpack('>IIBBBBB', payload)
        if kind == b'IDAT':
            compressed.extend(payload)
        offset += length + 12
        if kind == b'IEND':
            break
    if not info:
        raise ValueError('PNG has no IHDR')
    w, h, depth, color, compression, filtering, interlace = info
    check((w, h) == (1536, 1872), 'Mofu must use exact v1 dimensions 1536x1872')
    if depth != 8 or color != 6 or interlace != 0:
        raise ValueError('Sample checker expects non-interlaced RGBA8 PNG')
    raw = zlib.decompress(compressed)
    stride, bpp = w * 4, 4
    check(len(raw) == h * (stride + 1), 'Unexpected PNG data length')
    rows, prev, offset = [], bytearray(stride), 0
    for _ in range(h):
        method = raw[offset]
        row = bytearray(raw[offset + 1:offset + 1 + stride])
        offset += stride + 1
        for x in range(stride):
            a = row[x - bpp] if x >= bpp else 0
            b = prev[x]
            c = prev[x - bpp] if x >= bpp else 0
            if method == 1:
                predictor = a
            elif method == 2:
                predictor = b
            elif method == 3:
                predictor = (a + b) // 2
            elif method == 4:
                p = a + b - c
                distances = (abs(p - a), abs(p - b), abs(p - c))
                predictor = (a, b, c)[distances.index(min(distances))]
            elif method == 0:
                predictor = 0
            else:
                raise ValueError('Unknown PNG filter')
            row[x] = (row[x] + predictor) & 255
        rows.append(row[3::4])
        prev = row
    return rows

try:
    en = strings(ROOT / 'app/src/main/res/values/strings.xml')
    ja = strings(ROOT / 'app/src/main/res/values-ja/strings.xml')
    check(en.keys() == ja.keys(), 'English/Japanese string keys differ')
    formats = re.compile(r'%(?:\d+\$)?[a-zA-Z]')
    for key in en.keys() & ja.keys():
        check(sorted(formats.findall(en[key])) == sorted(formats.findall(ja[key])), f'Format arguments differ: {key}')
        check(key == 'language_japanese' or not re.search(r'[\u3040-\u30ff\u3400-\u9fff]', en[key]), f'Japanese text in default English resource: {key}')
    for path in (ROOT / 'app/src').rglob('*.xml'):
        ET.parse(path)
    java = '\n'.join(p.read_text() for p in (ROOT / 'app/src/main/java').rglob('*.java'))
    used = set(re.findall(r'R\.string\.(\w+)', java))
    check(used <= en.keys(), f'Undefined string resources: {sorted(used - en.keys())}')
    check(not re.search(r'[\u3040-\u30ff\u3400-\u9fff]', java), 'Hardcoded Japanese remains in Java')
    check('.getMessage()' not in java, 'Raw exception message display needs review')
    ns = '{http://schemas.android.com/apk/res/android}'
    manifest = ET.parse(ROOT / 'app/src/main/AndroidManifest.xml').getroot()
    permissions = {p.attrib[ns + 'name'] for p in manifest.findall('uses-permission')}
    expected = {'android.permission.' + p for p in ('SYSTEM_ALERT_WINDOW', 'PACKAGE_USAGE_STATS', 'FOREGROUND_SERVICE', 'FOREGROUND_SERVICE_SPECIAL_USE', 'POST_NOTIFICATIONS')}
    check(permissions == expected, f'Unexpected permission set: {permissions ^ expected}')
    app = manifest.find('application')
    check(app.attrib.get(ns + 'allowBackup') == 'false', 'Backup must remain disabled')
    check(all(x.attrib.get(ns + 'exported') == 'false' for x in app.findall('service')), 'Service must not be exported')
    check(not app.findall('receiver'), 'Unexpected receiver added')
    locales = ET.parse(ROOT / 'app/src/main/res/xml/locales_config.xml').getroot()
    check({x.attrib[ns + 'name'] for x in locales} == {'en', 'ja'}, 'Locale config must declare en and ja')
    for name, target in [('README.md', 'README.ja.md'), ('README.ja.md', 'README.md')]:
        text = (ROOT / name).read_text()
        check(f'href="{target}"' in text[:300] and 'align="right"' in text[:300], f'Missing right-aligned language link: {name}')
    for path in ROOT.rglob('*.md'):
        text = path.read_text()
        for target in re.findall(r'\]\(([^)]+)\)|(?:src|href)="([^"]+)"', text):
            link = next(x for x in target if x).split('#')[0]
            if not link or re.match(r'[a-z]+:', link) or '<' in link:
                continue
            # Screenshot guide contains an explicit future-file example, not a live README image.
            if path.name == 'SCREENSHOTS.md' and link.startswith('docs/screenshots/'):
                continue
            check((path.parent / link).exists(), f'Broken relative link in {path.relative_to(ROOT)}: {link}')
    wrapper = ROOT / 'gradle/wrapper/gradle-wrapper.jar'
    check(hashlib.sha256(wrapper.read_bytes()).hexdigest() == '81a82aaea5abcc8ff68b3dfcb58b3c3c429378efd98e7433460610fecd7ae45f', 'Wrapper JAR checksum mismatch')
    props = (ROOT / 'gradle/wrapper/gradle-wrapper.properties').read_text()
    check('distributionSha256Sum=20f1b1176237254a6fc204d8434196fa11a4cfb387567519c61556e8710aed78' in props, 'Missing pinned Gradle distribution checksum')
    forbidden_suffixes = {'.jks', '.keystore', '.p12', '.pfx', '.pem', '.key', '.apk', '.aab'}
    forbidden_names = {'local.properties', 'keystore.properties', 'signing.properties', '.env', 'google-services.json'}
    sensitive = re.compile(r'(?:-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----|AKIA[A-Z0-9]{16}|gh[pousr]_[A-Za-z0-9]{30,}|sk-[A-Za-z0-9]{32,})')
    for p in ROOT.rglob('*'):
        if not p.is_file() or any(part in {'.git', '.gradle', 'build', '.verification', '__pycache__'} for part in p.relative_to(ROOT).parts):
            continue
        check(p.suffix not in forbidden_suffixes and p.name not in forbidden_names, f'Private/build artifact in package: {p.relative_to(ROOT)}')
        if p.suffix in {'.java', '.xml', '.md', '.gradle', '.properties', '.yml', '.py', '.sh'}:
            check(not sensitive.search(p.read_text()), f'Possible credential in {p.relative_to(ROOT)}')
    sample = ROOT / 'app/src/main/assets/pets/mofu/spritesheet.png'
    check(sample.exists(), 'Bundled Mofu sample is missing')
    if sample.exists():
        check(sample.stat().st_size <= 12 * 1024 * 1024, 'Sample exceeds 12 MiB')
        alpha = alpha_rows(sample)
        counts = (6, 8, 8, 4, 5, 8, 6, 6, 6)
        for r, frames in enumerate(counts):
            for c in range(8):
                occupied = [(x, y) for y in range(208) for x in range(192) if alpha[r * 208 + y][c * 192 + x] > 0]
                if c >= frames:
                    check(not occupied, f'Padding cell row {r}, col {c} is not transparent')
                else:
                    check(len(occupied) > 1000, f'Empty/sparse used cell row {r}, col {c}')
                    if occupied:
                        xs, ys = zip(*occupied)
                        check(min(xs) >= 4 and max(xs) <= 187 and min(ys) >= 4 and max(ys) <= 203, f'Pose touches cell border at row {r}, col {c}')
        metadata = json.loads((ROOT / 'sample-pets/mofu/manifest.json').read_text())
        check(metadata['sha256'] == hashlib.sha256(sample.read_bytes()).hexdigest(), 'Sample checksum metadata mismatch')
except Exception as exc:
    errors.append(f'Check could not finish: {type(exc).__name__}: {exc}')

print(f'{checks} checks; {len(errors)} issue(s)')
for issue in errors:
    print(f'FAIL: {issue}')
if errors:
    sys.exit(1)
print('PASS: source/resources/links/permissions/wrapper/sample checks. Android build, lint and device checks remain separate.')
