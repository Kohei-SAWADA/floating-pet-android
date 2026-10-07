# Install the development APK / 開発版APKの導入・入れ替え

This is a publicly distributed **debug-signed development APK**, not a stable, production-signed, or Play Store release. It starts in English and includes Japanese plus a device-language option. Build/static checks are separate from real-phone behavior, which still needs testing.

## Before removing the old app

1. Download the new APK and keep your pet's original PNG outside Floating Pet. Mofu is also bundled in the new APK.
2. Stop the old pet in its app or notification.
3. Record size and other settings if you want to restore them. **Uninstalling removes the private imported image and preferences.** Permissions must be configured again.
4. If your only copy of custom artwork is inside the old app, pause: it has no artwork-export feature. Locate the original PNG first.

## Verified signing comparison

The archived `Floating-Pet-1.1.3-debug.apk` was inspected:

| Property | Archived 1.1.3 | This development build |
| --- | --- | --- |
| Application ID | `com.dot.floatingpet` | `com.dot.floatingpet` |
| Version code | 5 | 6 |
| Version name | 1.1.3 | 1.2.0-dev |
| Minimum / target API | 26 / 35 | 26 / 35 |
| Certificate SHA-256 | `2dcf3a1cea90030c0daa77051333ff471aa1dddbd4572ff865a065725ede9aa3` | `7ca6e44964526bf7b8b9addde062f305fe0600a96a601d718d7861261b345351` |

Both inspected signatures verify with APK Signature Scheme v2, but **the certificates differ**. If the installed app is that archived 1.1.3 APK, Android will reject an in-place update from this new signer. Remove the old app only after preserving the original PNG/settings and obtaining the new APK. The phone's installed APK was not inspected, so this comparison does not establish the identity of every existing installation. No old private signing key was available, and no signing key is included in the source or download packages.

## Install and choose English

1. After preserving artwork/settings, uninstall the old app if it has the different signer above, or if you have chosen a fresh install.
2. Open the supplied APK on the phone. If Android asks whether that file-opening app may install unknown apps, allow only the trusted source you deliberately chose, and turn that permission off after installation. Keep Play Protect and other system protections enabled. Stop at unexpected warnings rather than blindly bypassing them.
3. Open Floating Pet. It starts in English on first launch. The top **App language** section contains **English**, **日本語**, and **Use device language**. Choose **English** explicitly if Android restored another language.
4. Android 13+ also exposes Android Settings → Apps → Floating Pet → Language. Older supported versions use the in-app buttons. Later system-side language choices are not reset at each launch.
5. Select **Use sample pet**, or choose an original compatible PNG.
6. Allow **Display over other apps** and **Usage access** using the app's settings links if you are comfortable with their scope. Notifications are optional and provide hide/show/stop buttons.
7. Tap **Show pet**, return Home, and try drag/tap/size/Stop. If it stays hidden, open another app and return Home once. Home detection has the limits described in the README.

Changing language stops the active pet; tap **Show pet** afterwards. The app does not auto-start after reboot.

## When Android blocks the overlay permission

If **Allow display over other apps** is disabled and **App was denied access** appears, use the illustrated [English troubleshooting steps](../README.md#app-was-denied-access--the-display-switch-is-disabled) or [日本語の対処手順](../README.ja.md#app-was-denied-accessで表示許可を有効にできない). Only enable restricted settings if you trust the APK/developer and understand the risk. The user supplied blocked-state screenshots and later confirmed success with a home-screen Mofu screenshot. This is one reported device result, not cross-device validation.

If the ⋮ menu only contains **All permissions**, go back from App permissions to its **App info** parent first. The separate **Manage app if unused** control is not used for this approval.

⋮に **All permissions** しか出ない場合は、**App permissions** から1回（**All permissions**からなら2回）戻り、親の **App info** 画面の⋮を使ってください。**Manage app if unused** は変更不要です。

## File integrity

Download the development APK from [GitHub releases](https://github.com/Kohei-SAWADA/floating-pet-android/releases). Verify the exact downloaded file with SHA-256: `6d4d43e3d6c03683280338cd46a113d6b77d737ed64fe6dd4926c846619f7e36`. APK and source ZIP hashes differ and change after code/package changes. The certificate fingerprint above is not the APK-file checksum.

## 日本語の手順

1. **新しいAPKを手元に保存してから**始めます。今のペットのPNG原本をアプリの外に残し、サイズ等の設定を控えて、旧アプリのペットを停止します。
2. **アンインストールすると画像と設定が消えます。** アプリに画像の書き出し機能はないため、原本が見つからない場合は先に確認してください。権限も再設定します。
3. 保管されていた1.1.3 APKと今回のAPKは同じapplication IDですが、署名証明書が異なることを実物で確認しています。その1.1.3が端末に入っている場合は上書きできません。保存が済んだら旧版を削除して新APKをインストールします。端末の実際のAPKそのものは確認していません。
4. 日本語端末でも初回は英語で起動します。上部の **English** で明示的に英語へ切り替えられます。**日本語**や**Use device language**も選べ、Android 13以降はOSのアプリ別言語設定と同期します。
5. **Use sample pet** → 表示許可 → 使用状況へのアクセス → 必要なら通知許可 → **Show pet** の順に設定し、ホームへ戻ります。
6. サイズ・ドラッグ・タップ・停止を確認します。言語を変えた後は **Show pet** で再開します。

実機確認用debug APKです。Play Protect等は無効にせず、想定外の警告が出たら進めず内容を確認してください。署名鍵は公開用ソース・Drive配布ZIPのどちらにも含めていません。
