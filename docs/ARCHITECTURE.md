# Architecture

## Android shell

- `MainActivity`: English/Japanese settings, permission links, document-picker import, explicit bundled-sample import, start/stop. Import work is serialized; buttons are disabled while busy. Resource IDs keep import outcomes translatable after configuration changes.
- `PetStore`: at most 12 MiB per PNG, bounds-first decode, exact v1/v2 dimensions, PNG signature and alpha support, private temporary file, atomic replacement after input/output streams close. Unexpected provider exceptions become generic localized errors.
- `PetService`: user-started foreground notification, overlay drag/tap, normalized position, bounds, and interruption cleanup. It is not exported. Screen-off and lock hide the pet; Stop shuts down monitoring.
- `HomeMonitor`: serialized background usage-event queries, generation-gated delivery, fresh home evidence, conservative error handling, and bounded session recovery. No persistent usage history.
- `PetView`: atlas cropping without per-frame bitmap allocations, visibility-aware idle rendering, and attach-scoped system-animation/power observers. Accessibility text follows locale changes.

- `AppLanguage`: English on first launch, explicit English/Japanese/device choices; Android 13+ LocaleManager integration without overriding later system-side changes, legacy configuration contexts below API33, and a one-time migration after an OS upgrade. Language changes stop the active pet before rebuilding the settings UI.

## Testable Java core

- `HomeVisibility`: activity-class lifecycle evidence, replay/stale-event protection, lock/permission/manual gates, and a 128-key bound
- `UsageQueryWindow`: current-session query coverage; 2-second overlap, at most 30-second recovery
- `PollPolicy`: 200/500 ms target intervals, at least 100 ms quiet time, 1–2 second slow/failure backoff
- `PetMotion`: interaction-only row/frame selection with bounded response and cancellation
- `IdleMotion`: small bottom-anchored breathing/sway and 300 ms interaction fades
- `SystemAnimationPolicy`: framework/observed scale/power gates, including pre-33 stale-cache handling
- `SpriteAtlas`: immutable exact 57-frame v1 and 73-frame v2 layout metadata
- `OverlayGeometry`: sizing, clamping, cumulative drag detection and cancellation

No runtime dependency is added by localization or the sample-pet feature. The app's original home-detection and motion core is preserved.

## Known boundaries

Usage events do not prove that only the home screen is visible. Launcher-owned Recents/drawer UI, concurrent same-class activities, split-screen/PiP, and delayed events can be ambiguous. Uncertainty hides the pet, but a transition can still have visible lag. Full device testing remains necessary.

## 日本語

Android依存の画面・通知・読み込みと、純Javaのホーム判定・動作・座標計算を分離しています。今回の変更は表示文言の英日対応とサンプル素材の読み込みを中心にしており、元の判定・動作coreは維持しています。ホーム判定は利用イベントに依存するため、画面遷移の遅れやランチャー内の画面区別には限界があります。
