# Privacy / プライバシー

This document describes the source in this package. Review it whenever behavior changes.

## Data stays on the device

Floating Pet does not request `INTERNET` and contains no account system, telemetry, analytics, advertising SDK, or backend. It does not read screenshots or the contents of other apps. No accessibility service or notification listener is used.

- **Pet artwork:** a chosen PNG is copied into private app storage through Android's document picker. The sample pet is bundled locally. Failed imports retain the last valid image. Temporary files are removed after an import attempt.
- **Settings:** size, normalized position, response-priority mode, idle-animation preference, and app-language selection are stored in local app preferences.
- **Usage Access:** current-session app-activity events are processed in memory to decide whether the default launcher is active. Usage history is not written to files, preferences, or logs, and is not uploaded. Normal queries overlap by two seconds; delayed/failed queries recover at most the latest 30 seconds of the current session. Transient activity state is bounded.
- **Permission state:** Android controls overlay, Usage Access, and notification permission. You can revoke them in Settings.

App backup and device-transfer extraction are disabled in the manifest/extraction rules. Uninstalling removes the private artwork and preferences. If you change developer signing certificates and must uninstall, export/retain your original PNG first.

## User control

The service starts only from an explicit user action, has stop controls in the app and notification, and does not auto-start after reboot. Manual hide is not overridden by returning Home. Screen-off stops regular usage queries. Android or manufacturer power management may stop the service.

## Leaving Floating Pet

Tapping the pet attempts to open the installed official ChatGPT app (`com.openai.chatgpt`). It does not send your pet image, usage history, chat text, credentials, or a conversation identifier. There is no deep link to a specific assistant or conversation. The external app has its own policies and permissions.

## 日本語

Floating Petは通信権限を要求せず、ログイン・アクセス解析・広告・バックエンドを持ちません。選択したPNGとサイズ・位置・動作・言語設定だけをアプリ専用領域に保存します。使用状況へのアクセスは標準ホームアプリの判定に使い、イベントは起動中のメモリ内で処理し、履歴を保存・送信しません。他のアプリの画面内容やスクリーンショットは読みません。

表示許可・使用状況へのアクセス・通知はAndroidの設定から取り消せます。停止はアプリ画面または通知から行え、再起動後に自動起動しません。バックアップと端末移行への抽出は無効です。アンインストールすると取り込み画像と設定は削除されます。

ペットのタップは公式ChatGPTアプリを通常起動するだけで、画像・利用履歴・会話・認証情報は渡しません。特定の会話へ直接移動する機能はありません。移動後はChatGPT側の規約とプライバシー条件が適用されます。
