package com.dot.floatingpet.core;

/**
 * Android-independent, bottom-anchored idle transform. This never selects atlas frames,
 * changes overlay bounds, or owns a timer. Use a monotonic, nonnegative millisecond clock.
 * The caller schedules at most one frame every {@link #FRAME_INTERVAL_MS} while
 * {@link #needsFrame(long)} is true, including one last draw when a fade finishes.
 */
public final class IdleMotion {
    public static final long FRAME_INTERVAL_MS = 40;
    public static final long FADE_DURATION_MS = 300;
    public static final long BREATH_PERIOD_MS = 5_000;
    public static final long SWAY_PERIOD_MS = 10_000;
    public static final float MAX_HEIGHT_CONTRACTION = 0.012f;
    public static final float MAX_WIDTH_CONTRACTION = 0.010f;
    public static final float MAX_SWAY_WIDTH_FRACTION = 0.004f;

    /** Reusable draw output. Sway is a fraction of the untransformed target width. */
    public static final class Transform {
        public float scaleX = 1f;
        public float scaleY = 1f;
        public float swayWidthFraction;
        public float amplitude;
    }

    private boolean enabled = true;
    private boolean available;
    private boolean allowed = true;
    private long phaseStartedAt;
    private long fadeStartedAt;
    private float fadeFrom;
    private float fadeTo;

    /** User preference. Turning it off is an immediate identity transform, without a fade. */
    public void setEnabled(boolean enabled, long nowMillis) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        restartOrStop(nowMillis);
    }

    /**
     * True only when attached, shown, and system animations are enabled. Losing any of
     * these conditions immediately stops; becoming available starts a fresh gentle fade.
     */
    public void setAvailable(boolean available, long nowMillis) {
        if (this.available == available) return;
        this.available = available;
        restartOrStop(nowMillis);
    }

    /** Interaction/service gate. Entry and exit fade smoothly instead of snapping. */
    public void setAllowed(boolean allowed, long nowMillis) {
        if (this.allowed == allowed) return;
        this.allowed = allowed;
        if (!enabled || !available) return;
        fadeFrom = amplitudeAt(nowMillis);
        fadeTo = allowed ? 1f : 0f;
        fadeStartedAt = nonnegative(nowMillis);
    }

    /** False at rest after an interaction fade, or immediately when disabled/unavailable. */
    public boolean needsFrame(long nowMillis) {
        return enabled && available && (allowed || amplitudeAt(nowMillis) > 0f);
    }

    /**
     * Returns the transform through a reusable output object. Height only contracts;
     * horizontal contraction leaves room for the very small, bottom-anchored shear.
     * Thus even art that fills an entire cell remains within the original draw target.
     */
    public void sample(long nowMillis, Transform output) {
        float amplitude = amplitudeAt(nowMillis);
        output.amplitude = amplitude;
        if (amplitude == 0f) {
            output.scaleX = 1f;
            output.scaleY = 1f;
            output.swayWidthFraction = 0f;
            return;
        }
        long elapsed = elapsed(nowMillis, phaseStartedAt);
        double breathPhase = (elapsed % BREATH_PERIOD_MS) * (2.0 * Math.PI / BREATH_PERIOD_MS);
        double swayPhase = (elapsed % SWAY_PERIOD_MS) * (2.0 * Math.PI / SWAY_PERIOD_MS);
        float breath = (float) ((1.0 - Math.cos(breathPhase)) * 0.5);
        output.scaleX = 1f - MAX_WIDTH_CONTRACTION * amplitude;
        output.scaleY = 1f - MAX_HEIGHT_CONTRACTION * amplitude * breath;
        output.swayWidthFraction = MAX_SWAY_WIDTH_FRACTION * amplitude * (float) Math.sin(swayPhase);
    }

    private void restartOrStop(long nowMillis) {
        phaseStartedAt = nonnegative(nowMillis);
        fadeStartedAt = phaseStartedAt;
        fadeFrom = 0f;
        fadeTo = enabled && available && allowed ? 1f : 0f;
    }

    private float amplitudeAt(long nowMillis) {
        if (!enabled || !available) return 0f;
        long elapsed = elapsed(nowMillis, fadeStartedAt);
        if (elapsed >= FADE_DURATION_MS) return fadeTo;
        float progress = (float) elapsed / FADE_DURATION_MS;
        // Smoothstep has zero endpoint velocity and keeps interrupted fades continuous.
        float eased = progress * progress * (3f - 2f * progress);
        return fadeFrom + (fadeTo - fadeFrom) * eased;
    }

    private static long nonnegative(long nowMillis) {
        return Math.max(0, nowMillis);
    }

    private static long elapsed(long nowMillis, long thenMillis) {
        long now = nonnegative(nowMillis);
        return now <= thenMillis ? 0 : now - thenMillis;
    }
}
