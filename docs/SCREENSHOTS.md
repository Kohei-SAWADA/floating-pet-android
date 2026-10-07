# Real-device screenshot guide / 実機スクリーンショットの撮影

The README hero is a generated illustration, not evidence of a working device build. Keep it labeled. Add real screenshots once they have been captured on the phone.

## Recommended small set

| Filename under `docs/screenshots/` | What to capture | What it explains |
| --- | --- | --- |
| `home-screen.png` | Home screen with the imported Mofu pet | The app's main purpose |
| `settings-en.png` | English settings, sample loaded, permissions granted | Setup and English UI |
| `settings-ja.png` | The same screen in Japanese | Japanese support |
| `size-small.png` and `size-large.png` | Same clean wallpaper, two sizes | Size control |
| `notification.png` | Floating Pet's controls only | Hide, show, and stop |

If only one screenshot is available, prioritize `home-screen.png`.

## Safe capture

1. Save the original Mofu PNG from `app/src/main/assets/pets/mofu/spritesheet.png` to the phone and import it. Do not use a scaled preview.
2. Use a neutral wallpaper. Remove personal widgets, contact names, location/weather information, unread messages, private app names, and notification content before capture.
3. Capture the full-resolution screenshot on the phone. Retain the unmodified original privately. Crop only unrelated UI if needed, and label a crop when context is important.
4. Note Android version, app version/build, language, selected pet size, and whether this is an older 1.1.3 installation or the new source build.
5. Inspect the screenshot at thumbnail size and confirm the pet remains clearly visible.
6. Add English alt text to README.md and equivalent Japanese alt text to README.ja.md. Never imply an illustrated drag trail is a runtime feature.

Suggested block to place after the README introduction when the real file exists:

```html
<p align="center">
  <img src="docs/screenshots/home-screen.png" alt="Mofu floating on an Android home screen" width="320">
</p>
```

Do not add broken image references before capture. Four selected crops are now included: the disabled switch, denied-access dialog, wrong permissions subpage, and a user-confirmed Mofu home-screen result. See [provenance and verification limits](screenshots/README.md).

## 日本語

まず同じMofuのPNG原本を端末へ保存し、既存アプリの画像選択から読み込んでください。1.1.3で素材を試せますが、新しい「サンプルペットを使う」ボタンや英語UIの撮影には今回のソースからのビルドが必要です。

おすすめは「ホーム画面」「英語設定」「日本語設定」「大小のペット」「通知操作」です。ホーム画面だけでも機能が伝わります。通知・連絡先・住所や天気の位置・個人のウィジェットなどを先に隠し、Android版・アプリ版・言語・サイズを記録してください。実機画像を追加するまでは、サムネイルのイメージ図表記を残してください。

## 2026-10-07 documentation update

Four selected crops from six supplied screenshots document the disabled overlay switch, denial dialog, wrong permissions subpage, and successful Mofu display. The user explicitly confirmed success. Status-bar details, unrelated app icons/search bar, and unused screen area were removed; retained pixels and warning text were not changed. The phone model/Android version and broader interaction behavior remain unverified.

2026-10-07に提供された6枚から、拒否状態・間違えやすい階層・Mofuの表示成功を示す4点を切り抜きました。ユーザーから成功の明言がありました。時刻・通知等のステータスバーと無関係な他アプリ・検索バー・余白を除き、残した範囲の画素は変更していません。機種名・Android版や他の操作の動作は未確認です。
