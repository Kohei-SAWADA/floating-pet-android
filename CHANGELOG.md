# Development preparation

## Documentation update (2026-10-07)

- Added four selected real-device crops covering restricted permissions, the wrong menu level, and user-confirmed Mofu display, with matching English/Japanese step-by-step README guidance
- Linked the official Android app-specific settings flow, trust/risk cautions, and verification limits
- Updated install/screenshot/provenance notes; app code and APK unchanged

## 1.2.0-dev (unreleased)

- English default UI and complete Japanese resources, including notifications, errors, and accessibility descriptions
- English first launch and explicit English/Japanese/device-language buttons on all supported versions
- Android 13+ per-app language integration and pre-33 persistent localized contexts
- Explicit bundled-sample button using the same bounded PNG validation as document import
- New generated original Mofu pet and labeled illustrated README hero
- Reciprocal English/Japanese READMEs, privacy/security/asset records, screenshot guide and release checklist
- Source package hygiene and offline repository checks; no signing secret or public release

The application ID and the original home/motion core remain unchanged. New development versionCode: 6. This is not a published release.

---

# Changelog

## 1.1.3

- Add subtle neutral-pose breathing (5 seconds) and sway (10 seconds) through a bounded rendering transform, without changing the imported artwork
- Keep bottom-center anchor, overlay position and touch bounds fixed; fade the idle effect around interaction over 300 ms
- Default-on `待ち時間にゆっくり動く` switch restores fully still idle when off
- Respect disabled Android animations and battery saver; observe pre-33 settings directly to avoid re-enable cache races; cap idle redraws at 25 fps and stop scheduling when hidden, detached or disabled
- Preserve 200 ms response priority, 500 ms economy mode, v1/v2 imports and the same permission/signing identity

## 1.1.2

- Default response-priority mode targets 200 ms between queries; a visible switch restores 500 ms economy mode
- Query execution time counts toward the cadence, with serial rate-capped scheduling and slow/failure backoff
- Duplicate unchanged state notifications are suppressed without losing permission changes or failed-query hiding
- Exact v1 (1536×1872, 57 poses) PNG support added alongside unchanged v2 support
- No new permissions, artwork transformations, or idle animation

## 1.1.1

- Fix a hidden-state latch when Home resumes before the previous app pauses
- Accept delayed lifecycle events per activity class rather than discarding them against one global timestamp
- Keep events from separate activities within the launcher independent
- Keep pending transition coverage across short scheduling delays and retry from the last successful query after temporary failures; bounded recovery reads at most the current session’s last 30 seconds
- Discard slow query output but confirm its parsed state on the next timely query instead of losing the Home transition
- Polling remains 500 ms, with the same permissions and no saved usage history
- Same signing identity and stored image/settings for an in-place update

## 1.1.0

- Calm idle: the pet stays on its neutral first frame when untouched
- Touch: one brief response while a finger is held; no idle animation loops
- Drag: movement-direction poses appear only during actual movement and stop when the finger pauses
- Release, cancellation, multiple pointers, hiding, rotation and screen-off return to stillness
- Tap continues to open ChatGPT immediately
- Automatic pose-cycling settings removed; existing image, size and normalized position retained
- Default-launcher visibility based on explicitly user-granted Usage Access; all processing stays on device without saved usage history
- Fail-closed behavior for unknown/non-home/locked/revoked/query-failure states; manual hide/stop preserved
- Usage polling pauses with screen off; generation-gated results prevent stale screen-off responses
- Same package/signing identity for an in-place update
- Known limitation: polling delay and launcher-owned Recents/app-drawer or multi-window UI prevent a strict home-only guarantee

## 1.0.0

Initial personal prototype: PNG import, floating overlay, dragging, resizing, ChatGPT launch and notification controls.
