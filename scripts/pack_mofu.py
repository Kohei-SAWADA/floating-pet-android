#!/usr/bin/env python3
"""Repack already-generated Mofu artwork; Pillow is needed only for this optional tool."""
from pathlib import Path
from PIL import Image
import hashlib
import json

ROOT = Path(__file__).resolve().parents[1]
folder = ROOT / 'sample-pets/mofu'
metadata = json.loads((folder / 'manifest.json').read_text())
source = folder / 'source-artwork.png'
if hashlib.sha256(source.read_bytes()).hexdigest() != metadata['source_sha256']:
    raise SystemExit('Source artwork hash differs from the reviewed generation')
im = Image.open(source).convert('RGBA')
canvas = Image.new('RGBA', (1536, 1872), (0, 0, 0, 0))
config = metadata['processing']
scale, pad, baseline = config['scale'], config['source_crop_padding'], config['foot_baseline']
for row, bounds in enumerate(config['source_pose_bounds']):
    original_baseline = max(box[3] for box in bounds)
    for col, (left, top, right, bottom) in enumerate(bounds):
        crop_box = (max(0, left-pad), max(0, top-pad), min(im.width, right+pad), min(im.height, bottom+pad))
        pose = im.crop(crop_box)
        pose = pose.resize((round(pose.width*scale), round(pose.height*scale)), Image.Resampling.LANCZOS)
        jump = round((original_baseline-bottom)*scale) if row == 4 else 0
        x = col*192 + (192-pose.width)//2
        y = row*208 + baseline - jump - pose.height
        if x < col*192+4 or y < row*208+4 or x+pose.width > (col+1)*192-4 or y+pose.height > (row+1)*208-4:
            raise SystemExit(f'Pose outside safe cell at row {row}, column {col}')
        canvas.alpha_composite(pose, (x,y))
output = ROOT / metadata['bundled_file']
output.parent.mkdir(parents=True, exist_ok=True)
canvas.save(output, compress_level=9)
metadata['sha256'] = hashlib.sha256(output.read_bytes()).hexdigest()
metadata['file_size_bytes'] = output.stat().st_size
(folder/'manifest.json').write_text(json.dumps(metadata,ensure_ascii=False,indent=2)+'\n')
print(f'Packed 57 generated poses: {output} ({output.stat().st_size} bytes)')
print(metadata['sha256'])
