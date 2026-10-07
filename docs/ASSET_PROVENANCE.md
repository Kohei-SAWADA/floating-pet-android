# Asset provenance / 素材の由来

## Mofu sample

- Working name: **Mofu（もふ）**
- Brief: an original small, fluffy lavender-grey creature with mint leaf-like ear tufts and peach feet, made for this app
- Generated on **2026-10-06** using the built-in OpenAI image-generation tool
- No official OpenAI, ChatGPT, Codex, or other existing mascot artwork was used as a source/reference image
- No external stock art, downloaded character art, or font file was bundled
- The tool did not expose a precise underlying model identifier; none is claimed
- Intended runtime layout: v1, 1536 × 1872 RGBA PNG, 57 used frames in an 8 × 9 grid, 192 × 208 cells
- Original generated source: 1136 × 1385 RGBA PNG, SHA-256 `f3369bc8f690d90e0c076d5b158edc6aee213000e2901c6ce4b2c7544cc63d11`
- Final importable file and processing/checksum record: see `sample-pets/mofu/manifest.json`

The initial generator did not honor the exact pixel dimensions. After explicit approval, 57 individual generated poses were cropped, uniformly scaled by 1.12, and aligned into transparent 192 × 208 cells. The character design and colors were not redrawn. Jump-height differences were retained. The final 1536 × 1872 PNG passes actual-file decoding, production `SpriteAtlas` geometry validation, cell occupancy/margin checks, and fully transparent padding checks. Final SHA-256: `c8f656e04cbe5d803603f5042bc80977d79e146cc7822e07d8a8353f389f15c3`. Generation prompts are preserved in [ART_PROMPTS.md](ART_PROMPTS.md).

## README hero

- File: `docs/assets/floating-pet-hero.png`
- Generated on **2026-10-06** with the same tool, using the newly generated Mofu atlas only as a character identity reference
- SHA-256: `0c33ae6fa68b5ac0fbae4b06e74394ee3eda6cfe144da7977a37d3dd223b291d`
- A conceptual Android home-screen illustration with generic icons and readable feature labels
- Explicitly labeled **Illustrated preview**; it is not a screenshot and does not prove runtime behavior
- The drag trail is an illustration of a gesture, not a runtime feature

## Retained source assets

The original generic paw vector (`app/src/main/res/drawable/ic_pet.xml`) is retained from the user's source archive. That archive contained no project-wide license or separate icon attribution. The original icon's provenance remains an owner follow-up for a stable release; this development-preview publication does not establish new attribution or licensing facts. Previously supplied official/third-party pet sheets were not copied into this source package.

## Rights and limits

The artwork was requested as a new independent design. This records how it was produced; it does not guarantee uniqueness, copyright protection, trademark clearance, or freedom from every possible third-party claim. The owner has authorized publication of the included Mofu artwork for this development preview. A general artwork reuse/distribution license remains unselected. No open license has been applied automatically. Refer to [LICENSE-STATUS.md](../LICENSE-STATUS.md).

## 日本語

Mofuは今回のために新しく生成した、薄いラベンダー色の毛・ミント色の葉のような耳・淡い桃色の足を持つ独自デザインです。既存のOpenAI公式キャラクター・ロゴ・他者のスプライトを参照素材にしていません。生成元・加工内容・チェックサムを記録しますが、独占性・著作権の成立・あらゆる権利侵害がないことを法的に保証するものではありません。

サムネイルは同じ新キャラを参考に生成した機能説明図で、実機画面ではありません。実機スクリーンショットを後から追加する際も区別してください。素材の公開ライセンスは未選択です。

## User-provided setup and success screenshots (2026-10-07)

Four files in `docs/screenshots/` are pixel-preserving crops selected from six user-provided screenshots: the disabled overlay switch, denial dialog, wrong permissions subpage, and a successful Mofu home-screen display. These are actual screenshots, not generated artwork. The user explicitly confirmed success. See [screenshot provenance](screenshots/README.md).

Only cropped, metadata-stripped images are included. Status/notification information and unrelated apps/search UI were excluded. The depicted pet is the original sample Mofu. A separately requested private pet or sprite sheet is not part of this repository or its documentation. The owner has explicitly authorized this publication of the reviewed crops. No blanket screenshot reuse license is granted.
