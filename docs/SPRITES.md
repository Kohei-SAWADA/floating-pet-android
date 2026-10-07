# Sprite sheet contract / スプライト仕様

The runtime accepts two fixed layouts and never guesses or resizes the imported sheet.

- PNG with an alpha channel, maximum 12 MiB
- 8 columns, 192 × 208 pixels per cell
- v1: 1536 × 1872 pixels, 9 rows, 57 used frames
- v2: 1536 × 2288 pixels, 11 rows, 73 used frames
- Top-left origin; row/column indices below are zero-based
- Place each pose inside its own cell with transparent margins and stable body scale
- Padding cells should be fully transparent; do not place labels or backgrounds there

| Row | Animation | Frames | Current app behavior |
| --- | --- | --- | --- |
| 0 | idle | 6 | Uses frame 0 with a gentle render transform |
| 1 | running-right | 8 | Rightward drag |
| 2 | running-left | 8 | Leftward drag |
| 3 | waving | 4 | Short held-touch response |
| 4 | jumping | 5 | Vertical drag |
| 5 | failed | 8 | Retained format compatibility |
| 6 | waiting | 6 | Retained format compatibility |
| 7 | running | 6 | Retained format compatibility |
| 8 | review | 6 | Retained format compatibility |
| 9, v2 only | look-upper | 8 | Retained format compatibility |
| 10, v2 only | look-lower | 8 | Retained format compatibility |

The runtime checks signature, file size, decoded dimensions, and alpha support, but does not inspect artistic quality or prove that every RGBA pixel has transparency. The repository's sample checker additionally checks transparent padding, occupied cells, and margins. A still avatar file is not a compatible atlas.

## Mofu

The sample lives at `app/src/main/assets/pets/mofu/spritesheet.png`. This exact PNG is the downloadable sample. Its metadata and checksum are in `sample-pets/mofu/manifest.json`; the app does not require a JSON import.

The sample can be imported into an existing compatible 1.1.3 installation. Use the original file, not a README-rendered image, messenger-compressed image, or screenshot. The newly added sample button copies the bundled PNG through the same bounded validation and atomic-replacement path as the file picker.

## Motion is intentionally calm

Idle is a small breathing/sway transform, not autonomous walking. A held touch responds once for at most 400 ms. Drag animation rests within 120 ms after movement pauses and immediately on release/cancellation. System-disabled animations and battery saver also keep it still. Full row animation previews are demonstrations of artwork, not a claim that the app plays every row.

## 日本語

透明PNGを1536×1872（v1）または1536×2288（v2）に揃え、1コマ192×208、8列に配置します。各行のコマ数は表の通りです。取り込み時の拡大縮小や形式推測は行いません。空きマスに画像・文字を入れず、コマの端に余白を残してください。

Mofuはアプリ同梱PNGとダウンロード用PNGが同じファイルです。JSON設定の取り込みは不要です。通常の待機は0行0コマを小さく変形して表現し、自動で画面を歩き回る機能ではありません。
