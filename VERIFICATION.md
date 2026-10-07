# Verification: 1.2.0-dev

Date: **2026-10-06 UTC**. This report describes the final source and development test APK. It is not a production release approval.

## Documentation update: 2026-10-07

- Added four selected pixel-preserving, metadata-stripped crops of user-provided Android permission screenshots to both READMEs
- Added equivalent English/Japanese troubleshooting using Google Android Help, with explicit APK/developer trust and permission-risk conditions
- Added the warning → App info → Allow restricted settings → return to the overlay switch flow; kept Play Protect enabled and excluded forced/ADB or device-wide bypass advice
- Verified crop contents visually and pixel-for-pixel, relative image/document links, English/Japanese section presence, repository checks, and source-ZIP integrity
- Compared the restored baseline and update: no application code, manifest, resources, Gradle configuration, sample pet, or APK was changed
- No new Android build or device interaction was performed for this documentation-only change. The 2026-10-06 build/test results below remain historical checks of the unchanged app code
- Later user screenshots were inspected to distinguish App permissions / All permissions from the App info parent. Both READMEs explain the correct level and include one relevant crop; full screenshots are not bundled
- The user later explicitly reported success and supplied a screenshot showing Mofu on the home screen. This is one user-reported successful display; exact device/OS, all interactions, and cross-device behavior remain unverified

## Passed (application build checked 2026-10-06)

- Final pinned **Gradle 8.13 / Android Gradle Plugin 8.9.2** build: `assembleDebug testDebugUnitTest lintDebug`, run offline after verified official dependency setup, with one worker
- **149 JUnit tests, 0 failures, 0 errors, 0 skipped** in the final Gradle report
- **Android lint: 0 errors, 1 warning**. `UnusedAttribute` notes that `localeConfig` is used only on Android 13 / API33+. That declarative attribute is intentionally ignored on older versions, which use the app's localized contexts instead
- **84 English and 84 Japanese string resources**, matching names and format arguments; native language-name labels intentionally include 日本語 in the English resource set
- **305 dependency-free core smoke assertions**, also run with JDK17 `--release 17`
- Independent compilation of all app Java against Android35 and AAPT2 resource compile/link passed before the final Gradle checks
- Original `core/` controller sources retained byte-for-byte; localization and sample loading add no runtime dependency
- The original Gradle wrapper JAR and distribution checksum matched official Gradle values
- Manifest remains limited to overlay, Usage Access, foreground-service/special-use, and notification permissions; no Internet, accessibility, camera, microphone, location, contacts, or storage-read permission
- Services are not exported; backup/device-transfer extraction remains disabled; no boot receiver
- The built APK's signature verifies with **APK Signature Scheme v2**; `zipalign -c 4` passes
- APK metadata confirms package `com.dot.floatingpet`, versionCode **6**, versionName **1.2.0-dev**, minSdk **26**, targetSdk **35**, and Japanese/default resources
- The sample PNG inside the APK is byte-identical to the bundled source and separately delivered sample
- Final source ZIP integrity checked; executable wrapper mode preserved; build output, SDK/JDK tools, credentials, private signing keys, and local machine properties are excluded

## Sample art checks

The generator originally returned a nonconforming size. After approved crop/scale/alignment, the actual PNG was decoded and checked through the app's production `SpriteAtlas` API:

- RGBA **1536 × 1872** (v1), below 12 MiB
- **57 occupied poses** with safe cell margins
- **15 fully transparent padding cells**
- Exact **192 × 208** frame geometry
- SHA-256: `c8f656e04cbe5d803603f5042bc80977d79e146cc7822e07d8a8353f389f15c3`

The pet was visually inspected at 48/112/240 pixel widths on light and dark backgrounds. The hero was inspected for legible labels and its explicit “Illustrated preview” caption. These are artwork inspections, not Android runtime screenshots.

## Development APK

File: `Floating-Pet-1.2.0-dev-English-default-debug.apk`

- Size: **2,027,311 bytes**
- File SHA-256: `6d4d43e3d6c03683280338cd46a113d6b77d737ed64fe6dd4926c846619f7e36`
- Certificate SHA-256: `7ca6e44964526bf7b8b9addde062f305fe0600a96a601d718d7861261b345351`

The archived 1.1.3 APK has the same application ID but a different signing certificate. The phone's installed APK was not inspected. See [installation and data-loss precautions](docs/INSTALL.md). No signing secret is included in this repository or the delivered ZIPs.

## Language behavior implemented

The development build defaults to English on first launch and offers English, 日本語, and device-language buttons near the top. Android13+ uses LocaleManager and respects later changes in Android Settings. API26–32 uses persisted configuration contexts; an OS upgrade to API33+ migrates the stored choice once. Language changes stop the running pet. App-bundle language splitting is disabled so both bundled languages remain available if an AAB is built later.

Compilation and lint check this code, but the real-device behavior below remains unverified.

## Not verified / owner follow-up

- **No assistant-operated physical Android phone or emulator UI test** was run. User-provided blocked-state and successful Mofu-display screenshots were reviewed on 2026-10-07, with explicit user confirmation. This is a limited real-device success example. Confirm first launch on a Japanese-language device, all language buttons, system-side language changes, and older-OS behavior
- Repeated/import-cancel/error flows, orientation/font size, notification blocking, Usage Access/overlay revocation, touch cancellation, screen lock, actual home-transition lag, OEM power management, and ChatGPT launch need device testing
- No phone CPU, battery, or Android IPC benchmark. Existing synthetic polling results are not device measurements
- No comprehensive dependency vulnerability/security audit or legal clearance of generated art
- No production signing or Play Store submission/approval. GitHub development-preview publication is recorded separately below.
- Source/artwork licenses and the retained generic icon's provenance remain owner decisions

The pinned Gradle build reports a Gradle9 deprecation notice. The project remains pinned to the verified Gradle8.13 version; no unsupported automatic upgrade was performed.

## Repeat locally

```sh
./scripts/run_core_smoke.sh
./scripts/run_sample_check.sh
python3 scripts/check_repository.py
./gradlew --no-daemon --max-workers=1 assembleDebug testDebugUnitTest lintDebug
```

Use JDK17 or21 and Android SDK35 / Build Tools35.0.0. Run the final command after any source change; older reports do not validate newer code. See [the full release checklist](docs/RELEASE_CHECKLIST.md).

## Historical record

The supplied 1.1.3 report is retained in [docs/history/VERIFICATION-1.1.3.md](docs/history/VERIFICATION-1.1.3.md), with private device feedback and non-bundled official-art references omitted. Its prior APK/signing claims are not attributed to this build.

Original source ZIP SHA-256: `26a99d7a3c3b2992ded49d59c3ca49753bd500c838a943d9386afc178227cdb7`.

## 日本語

最終版のAPKビルド・149件のJUnit（失敗0）・Gradle lint（エラー0、API33専用属性の注意1件）が完了しました。84組の英日文字列、305項目smoke、PNGの57コマ・15透明マス、APKの署名・配置・同梱画像一致も確認済みです。

日本語端末でも初回は英語で起動し、アプリ内のEnglish／日本語／端末言語を選べる実装です。ただし**実機・エミュレーターのUI試験は未実施**なので、言語切り替え・権限・通知・ホーム判定などは端末で確認してください。旧保管APKとは署名が異なり、入れ替え前にPNG原本と設定を残す必要があります。

正式署名・Play Store公開は行っていません。GitHubでの開発プレビュー公開は別途下記に記録します。コードと素材の一般利用ライセンスは未選択です。

## Publication review: 2026-10-08 (Japan time)

- The authoritative source ZIP and supplied APK each match their own SHA-256: source `b4ff68ae9616e3a4105732f18d622e5e479865a0a6a91e3c8c765eb9b2e03ff6`; APK `6d4d43e3d6c03683280338cd46a113d6b77d737ed64fe6dd4926c846619f7e36`
- All 81 extracted source files initially matched the authoritative ZIP byte-for-byte. Publication changes are documentation-only; app code, manifest, resources, build configuration, and art remain unchanged
- Independent scan of source, APK, and nested archives found no signing private keys, authentication credentials, personal paths/emails, or private blue/black-beret pet. All seven PNGs were visually reviewed; only the public Mofu sample appears, with no image metadata or unrelated personal screen content
- APK binary manifest matches source package/version/API levels and permissions: `com.dot.floatingpet`, versionCode 6, `1.2.0-dev`, minSdk 26, targetSdk 35, `debuggable=true`, backup disabled, service unexported
- The APK contains only the Mofu pet asset; its bytes match the source PNG
- Independent Python cryptographic verification passed the APK v2 RSA/SHA-256 signature and whole-APK content digest. Certificate CN is `Android Debug`, with the fingerprint above. This is separate from the historical official Android-tool checks
- No fresh Java/Gradle build, JUnit/core-smoke run, official apksigner/zipalign run, or Android device/emulator UI test was performed at publication. This Mac has no available Java runtime or Android SDK tools. The historical 149 unit tests and build/lint evidence remain dated 2026-10-06
- Matching metadata/assets and signatures do not prove a fully reproducible source-to-APK build. That comparison remains unverified
- Publication-time offline source/resource/link/permission/wrapper/sample checks passed: **564 checks, 0 issues**
- GitHub private vulnerability reporting was enabled and verified
- Public GitHub commit, README/image rendering, prerelease asset download, and profile links will be checked after upload
