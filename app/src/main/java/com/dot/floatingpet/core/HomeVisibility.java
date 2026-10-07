package com.dot.floatingpet.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Android-independent, conservative home visibility evidence from usage events.
 *
 * <p>Lifecycle watermarks belong to activities, not the entire event stream. A home
 * resume can therefore remain a candidate while an older app is still resumed,
 * then become visible when that app's delayed pause/stop arrives. A candidate must
 * be newer than every observed non-home resume, so pausing an app cannot resurrect
 * home evidence from before that app was opened. Any still-resumed non-home activity
 * blocks visibility, including a second activity in the same package.
 *
 * <p>The caller supplies the best activity key available from public APIs (currently
 * the activity class name). Package names are included separately in identity.
 * Missing keys use one conservative package-level fallback. Different activity
 * classes are independent, but simultaneous instances of the same class cannot be
 * distinguished without an instance identifier from the event source.
 * A missing-key home pause/stop invalidates older home candidates of every class;
 * any home pause/stop invalidates an older missing-key home candidate.
 *
 * <p>Delivery order and overlapping-query replay do not change the result: each
 * activity retains its latest timestamp, and pause/stop wins over resume when that
 * activity's timestamps tie. Equal-time home and non-home resumes also fail closed.
 * Only bounded, transient lifecycle state is retained, never an event log. Capacity
 * exhaustion invalidates evidence instead of evicting a possible active blocker.
 *
 * <p>This is evidence, not a real-time foreground-window API. Keep polling and
 * suppress output during query failures until complete, timely coverage confirms
 * retained evidence. Reset on invalidation or lost coverage, and do not replay
 * pre-reset history as current evidence. Usage events are delayed and cannot
 * reliably distinguish home from Recents or other UI owned by the launcher.
 * Use on one thread, or synchronize.
 */
public final class HomeVisibility {
    static final int MAX_TRACKED_ACTIVITIES = 128;
    private static final int RESUMED = 1;
    private static final int PAUSED = 2;
    private static final int STOPPED = 4;

    private final Map<ActivityKey, Lifecycle> activities = new HashMap<>();
    private String homePackage;
    private Lifecycle unknownActivity;
    private boolean hasNonHomeResume;
    private long latestNonHomeResume;
    private boolean hasUnknownEvent;
    private long latestUnknownEvent;
    private boolean hasHomeTerminal;
    private long latestHomeTerminal;
    private boolean hasUnidentifiedHomeTerminal;
    private long latestUnidentifiedHomeTerminal;
    private boolean hasTimestamp;
    private long latestTimestamp;
    private boolean hasResetWatermark;
    private long resetWatermark;

    public HomeVisibility() {
        this(null);
    }

    public HomeVisibility(String homePackage) {
        this.homePackage = knownPackage(homePackage);
    }

    /** A changed or unresolved default launcher invalidates all foreground evidence. */
    public void setHomePackage(String packageName) {
        String next = knownPackage(packageName);
        if (!Objects.equals(homePackage, next)) {
            homePackage = next;
            reset();
        }
    }

    /** Package-only compatibility entry point; prefer the activity-key overload. */
    public void resumed(String packageName, long at) {
        resumed(packageName, null, at);
    }

    public void resumed(String packageName, String activityKey, long at) {
        apply(packageName, activityKey, at, RESUMED);
    }

    /** Package-only compatibility entry point; prefer the activity-key overload. */
    public void paused(String packageName, long at) {
        paused(packageName, null, at);
    }

    public void paused(String packageName, String activityKey, long at) {
        apply(packageName, activityKey, at, PAUSED);
    }

    public void stopped(String packageName, long at) {
        stopped(packageName, null, at);
    }

    public void stopped(String packageName, String activityKey, long at) {
        apply(packageName, activityKey, at, STOPPED);
    }

    /**
     * Discards foreground evidence while retaining the maximum observed timestamp.
     * Events at or before that reset boundary, even from another activity, cannot
     * revive evidence. The caller must additionally exclude unobserved old history.
     */
    public void reset() {
        activities.clear();
        unknownActivity = null;
        hasNonHomeResume = false;
        hasUnknownEvent = false;
        hasHomeTerminal = false;
        hasUnidentifiedHomeTerminal = false;
        if (hasTimestamp) {
            hasResetWatermark = true;
            resetWatermark = latestTimestamp;
        }
    }

    /**
     * Manual hide only suppresses output; off/locked/revoked states also invalidate
     * foreground evidence and require a fresh event before the pet can reappear.
     */
    public boolean shouldShow(boolean userHidden, boolean screenOn, boolean unlocked,
            boolean usageGranted) {
        if (!screenOn || !unlocked || !usageGranted) {
            reset();
            return false;
        }
        if (userHidden || homePackage == null
                || (unknownActivity != null && unknownActivity.isResumed())) return false;

        boolean hasEligibleHome = false;
        for (Map.Entry<ActivityKey, Lifecycle> entry : activities.entrySet()) {
            Lifecycle lifecycle = entry.getValue();
            if (!lifecycle.isResumed()) continue;
            if (!homePackage.equals(entry.getKey().packageName)) return false;
            hasEligibleHome |= (!hasNonHomeResume || lifecycle.at > latestNonHomeResume)
                    && (!hasUnknownEvent || lifecycle.at > latestUnknownEvent)
                    && (!hasUnidentifiedHomeTerminal
                            || lifecycle.at > latestUnidentifiedHomeTerminal)
                    && (!entry.getKey().activity.isEmpty() || !hasHomeTerminal
                            || lifecycle.at > latestHomeTerminal);
        }
        return hasEligibleHome;
    }

    private void apply(String packageName, String activityKey, long at, int event) {
        if (hasResetWatermark && at <= resetWatermark) return;
        if (!hasTimestamp || at > latestTimestamp) latestTimestamp = at;
        hasTimestamp = true;

        String known = knownPackage(packageName);
        if (known == null) {
            // An unattributable lifecycle change invalidates older home evidence.
            // An unknown resume additionally blocks until it is paused/stopped.
            if (!hasUnknownEvent || at > latestUnknownEvent) latestUnknownEvent = at;
            hasUnknownEvent = true;
            if (unknownActivity == null) unknownActivity = new Lifecycle(at, event);
            else unknownActivity.apply(at, event);
            return;
        }

        if (event == RESUMED && !known.equals(homePackage)) {
            // Even a delayed resume whose later pause is already known proves that
            // home evidence from before that resume must not be resurrected.
            if (!hasNonHomeResume || at > latestNonHomeResume) latestNonHomeResume = at;
            hasNonHomeResume = true;
        }

        String activity = knownActivity(activityKey);
        if (event != RESUMED && known.equals(homePackage)) {
            if (!hasHomeTerminal || at > latestHomeTerminal) latestHomeTerminal = at;
            hasHomeTerminal = true;
            if (activity.isEmpty()) {
                if (!hasUnidentifiedHomeTerminal || at > latestUnidentifiedHomeTerminal) {
                    latestUnidentifiedHomeTerminal = at;
                }
                hasUnidentifiedHomeTerminal = true;
            }
        }

        ActivityKey key = new ActivityKey(known, activity);
        Lifecycle lifecycle = activities.get(key);
        if (lifecycle == null) {
            if (activities.size() >= MAX_TRACKED_ACTIVITIES) {
                reset();
                return;
            }
            activities.put(key, new Lifecycle(at, event));
        } else {
            lifecycle.apply(at, event);
        }
    }

    private static final class Lifecycle {
        long at;
        int events;

        Lifecycle(long at, int event) {
            this.at = at;
            events = event;
        }

        void apply(long at, int event) {
            if (at > this.at) {
                this.at = at;
                events = event;
            } else if (at == this.at) {
                // A set of event types makes same-timestamp overlap idempotent.
                events |= event;
            }
        }

        boolean isResumed() {
            return events == RESUMED;
        }
    }

    private static final class ActivityKey {
        final String packageName;
        final String activity;

        ActivityKey(String packageName, String activity) {
            this.packageName = packageName;
            this.activity = activity;
        }

        @Override public boolean equals(Object other) {
            if (!(other instanceof ActivityKey)) return false;
            ActivityKey key = (ActivityKey) other;
            return packageName.equals(key.packageName) && activity.equals(key.activity);
        }

        @Override public int hashCode() {
            return 31 * packageName.hashCode() + activity.hashCode();
        }
    }

    private static String knownPackage(String packageName) {
        return packageName == null || packageName.trim().isEmpty() ? null : packageName;
    }

    private static String knownActivity(String activityKey) {
        return activityKey == null || activityKey.trim().isEmpty() ? "" : activityKey;
    }
}
