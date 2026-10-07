# Contributing

This development preview is published with the owner's authorization. The owner has not selected a general code/artwork license or established a contribution policy yet. Check [LICENSE-STATUS.md](LICENSE-STATUS.md) before redistributing or proposing public contributions.

## Development checks

1. Use the pinned Gradle wrapper, JDK 17/21, and Android SDK 35.
2. Keep new user-visible text in `res/values/strings.xml` and `res/values-ja/strings.xml`, with matching names and format arguments.
3. Keep `core/` free of Android dependencies so its behavior stays unit-testable.
4. Add regression tests for changes to visibility, polling, geometry, motion, or atlas contracts.
5. Run `python3 scripts/check_repository.py`, then `./gradlew --no-daemon --max-workers=1 assembleDebug testDebugUnitTest lintDebug`.
6. Test touched UI flows on a device in English and Japanese, including cancellation, permission denial/revocation, repeated start/stop, and large text.
7. Update both READMEs when behavior changes; record which checks were not run.

Do not add network access, analytics, silent permission changes, startup behavior, or new external art without an explicit design/privacy review. Never copy an official mascot or assume that a generated image has cleared third-party rights.

## 日本語

所有者の承認で開発プレビューを公開しています。コード・素材の一般利用ライセンスと貢献方針は未決定です。英日リソースとREADMEを揃え、変更した動作の回帰テストを追加してください。通信・解析・自動起動・外部素材は勝手に追加せず、秘密情報や実機の個人情報をコミットしないでください。
