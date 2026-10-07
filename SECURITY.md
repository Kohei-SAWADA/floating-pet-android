# Security

## Scope

This is an Android development preview. Stable-release support and a private security-reporting address have not been established. GitHub private vulnerability reporting is enabled: use the repository's Security → Advisories → Report a vulnerability flow for a private report. No separate email address is claimed here.

Do not include access tokens, signing keys, private PNGs, device identifiers, personal screenshots, or complete usage logs in public issues. A concise reproduction, app version, Android version, and device family are usually enough.

## Controls in this source

- No network permission or runtime analytics dependency
- The overlay service is not exported
- No boot receiver or automatic background start
- Bounded PNG import with format/dimension checks and atomic replacement
- Generic unexpected-error messages rather than exception details in the UI
- Explicit user-controlled overlay and Usage Access grants
- Conservative hiding after uncertain usage evidence, lock, or permission loss
- App-private image storage and backup exclusion

These controls are not a security audit or a guarantee. Device/OEM behavior and release-signing controls still require review.

## Before release

Run `python3 scripts/check_repository.py` and `./gradlew assembleDebug testDebugUnitTest lintDebug`. Inspect the final source archive for credentials and private files. The repository ignores common secrets, but `.gitignore` does not protect files already tracked by Git. Never commit production signing keys, credential-bearing local properties, or a private debug keystore.

## 日本語

公開中の開発プレビューです。安定版のサポート体制や専用メールアドレスは未設定です。GitHubの非公開脆弱性報告機能を有効にしています。リポジトリのSecurity → Advisories → Report a vulnerabilityから非公開で報告できます。公開Issueへ署名鍵・トークン・個人情報を含む画像・利用ログを貼り付けないでください。上記の対策は監査や安全性の保証ではなく、実機挙動と正式署名の管理は別途確認が必要です。
