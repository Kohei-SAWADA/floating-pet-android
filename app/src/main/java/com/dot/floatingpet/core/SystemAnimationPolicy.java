package com.dot.floatingpet.core;

/** Combines observed public settings without polling a stale pre-33 framework cache. */
public final class SystemAnimationPolicy {
    private SystemAnimationPolicy() {}

    public static boolean allows(boolean hasDurationScaleListener, float durationScale,
                                 boolean powerSave, boolean frameworkEnabled) {
        if (!Float.isFinite(durationScale) || durationScale <= 0f || powerSave) return false;
        // API 26-32 has no notification when ValueAnimator's cached scale catches up.
        // Read the independently observed setting/power state there instead.
        return !hasDurationScaleListener || frameworkEnabled;
    }
}
