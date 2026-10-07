# Release checklist / リリースチェックリスト

The owner has explicitly authorized this public 1.2.0-dev development preview. This checklist records remaining work for broader validation and a stable/production release. No Play Store submission or project-wide license selection is implied.

## Owner decisions

- [ ] Choose the source-code license and any separate artwork license; see [license status](../LICENSE-STATUS.md)
- [ ] Review the Mofu design and hero, confirm names/rights/attributions; no legal clearance is claimed
- [ ] Decide whether to retain `com.dot.floatingpet` or use a reverse-domain ID controlled by the publisher
- [ ] Establish production signing and a secure backup/recovery plan outside the repository
- [ ] Choose the release version; `1.2.0-dev` / versionCode 6 identifies this development preview only
- [x] Enable and verify GitHub private vulnerability reporting
- [x] Owner explicitly approved publication to `Kohei-SAWADA/floating-pet-android`

## Source and build

- [ ] Run `python3 scripts/check_repository.py` on the final tree
- [ ] Run `./gradlew --no-daemon --max-workers=1 clean assembleDebug testDebugUnitTest lintDebug`
- [ ] Review all compiler/lint warnings; inspect the merged manifest and APK permissions
- [ ] Keep Gradle distribution and wrapper checksums pinned and verified
- [ ] Review direct and transitive dependency licenses/security before the final release
- [ ] Ensure no signing keys, credentials, `.env`, `local.properties`, private screenshots, logs, or machine-specific paths enter Git history
- [ ] Check `.gitignore` and `git ls-files` before each public push; ignore rules alone do not untrack files
- [ ] Build and verify the actual signed release separately; a debug APK is not production-ready

## Device validation

- [ ] English first launch on a Japanese device; all three in-app language buttons; API33+ system language changes; API26–32 context persistence; OS-upgrade migration
- [ ] English and Japanese at normal and large font/display sizes
- [ ] Sample import; valid v1/v2; cancel; malformed PNG; over-12-MiB file; old-image preservation
- [ ] Rotate or change language during import, repeat both import buttons, then Show
- [ ] Deny/regrant/revoke overlay and Usage Access; deny notifications; verify app-side Stop still works
- [ ] Home → app → Home, app drawer, Recents, split screen and picture-in-picture; measure real transition delay
- [ ] Tap, slow drag, paused drag, cancellation, second pointer, movement back to start, screen edges
- [ ] Resize, rotate, relaunch, and verify normalized position/clamping
- [ ] Notification hide/show/stop; manual hide must survive returning Home
- [ ] Lock/unlock, screen-off/on, app restart, and reboot; no unsolicited restart
- [ ] Toggle response priority, idle animation, Android animations, and battery saver while running
- [ ] ChatGPT installed and absent; verify no specific chat or assistant is promised
- [ ] At least one API 26–32 device/emulator and one API 33+ device, including target-SDK behavior
- [ ] Record exact phone/OS/app build and results without private usage logs

## README and assets

- [ ] Review both READMEs and their reciprocal language links
- [ ] Add real-device screenshots following [SCREENSHOTS.md](SCREENSHOTS.md), with personal content removed
- [ ] Keep the generated hero labeled as an illustrated preview
- [ ] Confirm the downloadable sample PNG is byte-identical to the bundled file
- [ ] Check every relative link and all image paths after packaging
- [ ] Replace pre-publication status only after the corresponding release exists; do not invent download URLs
- [ ] Update [VERIFICATION.md](../VERIFICATION.md) with current passed/failed/not-run results

## 日本語の要点

ライセンス・素材の権利確認・公開先・正式署名は所有者の判断が必要です。ビルドと149件の既存単体テスト、lint、英日UI、権限拒否/取り消し、実機のホーム判定を確認し、実スクリーンショットを追加してください。今回は所有者がGitHubリポジトリ・開発版APKの公開を明示的に承認しています。安定版やPlay Store配布の準備が完了したことを意味しません。
