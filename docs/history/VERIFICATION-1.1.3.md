> Historical record supplied with version 1.1.3. These are prior reported results, not checks of this modified preparation. Personal device feedback and references to non-bundled official art have been omitted.

# Verification — version 1.1.3 — 2026-10-01

## Passed

- Final offline Gradle `assembleDebug testDebugUnitTest lintDebug`: successful
- **149 unit tests, 0 failures, 0 errors, 0 skipped**
  - SpriteAtlas: 15; all 57 v1 and 73 v2 frame bounds, exact dimensions, invalid rows/padding, immutable layout metadata and shared motion compatibility
  - OverlayGeometry: 21; resizing, boundaries, cumulative drag and cancellation
  - PetMotion: 15; neutral idle atlas pose, bounded touch response, directional actual movement, pause and cancellation
  - SystemAnimationPolicy: 8; observed setting/power gates, pre-33 stale-cache re-enable ordering, modern listener gate and invalid settings
  - IdleMotion: 15; breathing/sway periods, transform containment, bottom anchor, smooth interrupted fades, lifecycle gates, disabled motion, long uptime and schedule cessation
  - HomeVisibility: 48; per-activity transition ordering, 720 event permutations, replay, missing class names, lock/access/manual gates and bounded state
  - UsageQueryWindow: 8; current-session boundaries, query retry and bounded recovery
  - PollPolicy: 19; fast/economy cadence, minimum quiet time, slow/failure backoff, recovery and ineligibility
- Android lint: **No issues found**
- APK signature verified; same signing certificate as prior delivered versions; application ID `com.dot.floatingpet`, versionCode 5
- Manifest is unchanged from 1.1.1: no additional permission, INTERNET, accessibility service, notification listener or boot receiver
- Existing PNG, size and position storage remain compatible; install as an update without uninstalling
- Independent static review checked serialized/rate-capped scheduling, live mode changes, generation guards, deduplicated state/permission delivery and both atlas layouts

## Gentle idle rendering

- The original neutral atlas pose stays unchanged; rendering applies a 5-second breathing cycle and a 10-second sway cycle
- Maximum height contraction 1.2%, width contraction 1%, and top shear 0.4% of width; bottom-center is fixed and the complete target remains contained
- Interaction gates fade over 300 ms; app/system disable and unavailable visibility reset immediately to identity
- At most one idle callback is pending, separated by at least 40 ms (25 fps); hidden/detached/disabled views stop callbacks. Existing service screen-off and conservative home/lock visibility gates remain in control
- Android animator-scale observer and power-save notifications are attach-scoped; API 33+ duration-scale listener handles framework changes. API 26–32 uses independently observed global animator-scale and power-save state rather than waiting on a stale ValueAnimator cache; both idle and interaction stay still during battery saver. These are read-only public APIs, with no additional permissions
- Setting `待ち時間にゆっくり動く` defaults on; off restores still waiting. Interaction animation also respects system-disabled animations
- Actual Java controller samples were software-rendered into a 10-second, 25 fps preview and a contact sheet at 48, 112 and 240 pixel widths. The characteristic phases were visually inspected without enlarging the animation amplitude; this is a raster approximation of the Canvas transform, not an Android runtime recording. The local browser preview URL was blocked (`ERR_BLOCKED_BY_CLIENT`), and no Android visual runtime verification is claimed. Mathematical bounds/timing tests and static review remain separate from on-device appearance/smoothness checks

## Response-priority measurements and limits

The default response-priority setting targets **200 ms start-to-start**. Turning off “反応優先（オフで省電力）” selects **500 ms** economy mode. Query duration counts toward the interval rather than being added again. Every completion has at least 100 ms quiet time. Slow (at least 200 ms) or failed eligible queries back off to 1000, then 2000 ms intervals; a timely success restores the selected mode. Refresh requests cannot bypass the rate cap, and one worker serializes queries. Screen-off schedules no recurring query.

`benchmarks/results.txt` records a deterministic model using a simulated 5 ms query and uniformly distributed transition phases:

| Mode | Mean wait to next query start | P95 wait | Modeled queries / 60 s |
| --- | ---: | ---: | ---: |
| Old post-query 500 ms wait | 252.5 ms | 479.75 ms | 119 |
| New response priority | 100 ms | 190 ms | 300 |
| New economy | 250 ms | 475 ms | 120 |

The same artifact includes a measured pure-Java policy-loop CPU microbenchmark with explicit host/JIT caveats. **No Android Binder latency, phone CPU use or battery consumption was measured.** Fast mode allows up to 2.5× economy's query frequency; this does not imply 2.5× battery drain. OS event-delivery delays are additional and cannot be eliminated by this scheduler.

The default launcher is still resolved through the public PackageManager API; a potentially stale launcher cache was not introduced.

## v1 / v2 import validation

- v1: exact 1536×1872 PNG, 8 columns × 9 rows, frame counts 6,8,8,4,5,8,6,6,6
- v2: exact 1536×2288 PNG, same geometry plus two 8-frame look rows
- Both have 192×208 cells. No scaling, missing-row invention or artwork editing occurs during import
- The decoded bitmap chooses its immutable layout; drawing rejects absent rows and padding cells
- PNG signature, 12 MiB limit, bounds-first decode, alpha checks, post-decode dimensions and atomic replacement are preserved

## Privacy and retained limitations

Usage processing is current-session-only and in memory, with no persisted history or network. Manual hide/Stop and locked/revoked/unknown-state hiding remain effective. The 1.1.1 transition-order fixes and bounded 30-second query recovery are retained. Recents/app-drawer UI in the launcher package, same-class concurrent activity instances, split screen, picture-in-picture, delayed events outside the normal 2-second overlap, and the deliberate 128-key safety reset remain limitations.


## Build environment

JDK 21, SDK/Build Tools 35, Android Gradle Plugin 8.9.2, Gradle 8.13. Tool downloads were checksum-verified. The pinned build emits a Gradle 9 deprecation notice and an unwritable optional SDK-analytics-home warning; neither blocks the pinned build or tests.
