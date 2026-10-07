package com.dot.floatingpet.core;

/**
 * Calm, touch-only animation state. Use one monotonic millisecond clock throughout.
 * The caller supplies actual overlay movement after clamping, never raw finger movement.
 * Render {@link #pose(long)} at interaction changes and while {@link #isActive(long)}
 * is true. Render once more when activity expires to restore idle, then stop polling.
 * This pose controller has no autonomous idle animation; the view may apply a separate gentle idle transform.
 */
public final class PetMotion {
    private static final int IDLE = 0;
    private static final int RIGHT = 1;
    private static final int LEFT = 2;
    private static final int WAVE = 3;
    private static final int JUMP = 4;
    private static final long WAVE_DURATION_MS = 400;
    private static final long MOVEMENT_GRACE_MS = 120;
    private static final long DRAG_FRAME_MS = 80;
    private static final float MIN_MOVEMENT_PX = 0.75f;
    private static final float AXIS_SWITCH_RATIO = 1.25f;

    private boolean held;
    private int row = IDLE;
    private long startedAt;
    private long lastMovedAt;

    /** A new touch plays one short wave, only for as long as that touch is held. */
    public void down(long nowMillis) {
        held = true;
        row = WAVE;
        startedAt = nowMillis;
    }

    /**
     * Records a movement that actually changed the clamped overlay position.
     * Zero/subpixel jitter and invalid deltas do not start or prolong animation.
     */
    public void moved(float dx, float dy, long nowMillis) {
        if (!held || !Float.isFinite(dx) || !Float.isFinite(dy)) return;
        float ax = Math.abs(dx);
        float ay = Math.abs(dy);
        if (Math.max(ax, ay) < MIN_MOVEMENT_PX) return;

        boolean continuing = isActive(nowMillis) && row != WAVE;
        boolean horizontal;
        if (continuing && (row == RIGHT || row == LEFT)) {
            horizontal = ay <= ax * AXIS_SWITCH_RATIO;
        } else if (continuing && row == JUMP) {
            horizontal = ax > ay * AXIS_SWITCH_RATIO;
        } else {
            horizontal = ax >= ay;
        }
        int nextRow = horizontal ? (dx > 0 ? RIGHT : LEFT) : JUMP;
        if (!continuing || nextRow != row) startedAt = nowMillis;
        row = nextRow;
        lastMovedAt = nowMillis;
    }

    /** Lifting the finger immediately restores the neutral idle atlas pose. */
    public void release() {
        held = false;
        row = IDLE;
    }

    /** Interrupted gestures have the same immediate neutral pose as release. */
    public void cancel() {
        release();
    }

    /** Returns a fresh {atlas row, frame}; idle is always {0, 0}. */
    public int[] pose(long nowMillis) {
        if (!isActive(nowMillis)) return new int[] {IDLE, 0};
        long frameDuration = row == WAVE
                ? WAVE_DURATION_MS / SpriteAtlas.frameCount(WAVE) : DRAG_FRAME_MS;
        int frame = (int) ((elapsed(nowMillis, startedAt) / frameDuration)
                % SpriteAtlas.frameCount(row));
        return new int[] {row, frame};
    }

    /** True only during a held-touch wave or within 120 ms of real drag movement. */
    public boolean isActive(long nowMillis) {
        if (!held || row == IDLE) return false;
        return row == WAVE
                ? elapsed(nowMillis, startedAt) < WAVE_DURATION_MS
                : elapsed(nowMillis, lastMovedAt) < MOVEMENT_GRACE_MS;
    }

    private static long elapsed(long nowMillis, long thenMillis) {
        return Math.max(0, nowMillis - thenMillis);
    }
}
