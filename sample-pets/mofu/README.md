# Mofu / もふ

A newly generated sample pet for Floating Pet. Download the original `spritesheet.png` from `app/src/main/assets/pets/mofu/` and import it with the app's file picker. The source artwork in this folder is a generation record, **not** the importable file.

- Exact v1 PNG: 1536 × 1872 pixels, RGBA, 8 columns × 9 rows
- 192 × 208 pixels per cell; 57 used poses and 15 fully transparent padding cells
- Compatible format for the existing Floating Pet 1.1.3 importer
- No JSON/configuration import needed; `manifest.json` documents the file and its checksum
- New source builds also provide a **Use sample pet** button
- Full sheet previews show all available poses. Current app interactions use the first five rows only

The source character was generated with the built-in OpenAI image-generation tool. Pose crops were uniformly resized and aligned after user approval, without drawing a replacement character or changing its colors. `scripts/pack_mofu.py` reproduces the normalized atlas from the reviewed source crops; Pillow is only needed for this optional art tool, not for the Android app.

The generated design is not an official OpenAI character. See [provenance](../../docs/ASSET_PROVENANCE.md) and [license status](../../LICENSE-STATUS.md). The owner has not selected a public artwork license; this is not a legal clearance statement.

## 日本語

アプリに取り込むのは `app/src/main/assets/pets/mofu/spritesheet.png` のPNG原本です。このフォルダのsource-artwork.pngは生成の記録用で、取り込み用ではありません。PNGを縮小・スクリーンショット化せず保存し、既存アプリの画像選択から選んでください。JSONを取り込む必要はありません。

1.1.3が対応するv1形式です。新しいソースからビルドすれば「サンプルペットを使う」ボタンも利用できます。57コマのうち、現在のアプリ動作が使うのは最初の5行です。画面を自動で歩き回る機能ではありません。
