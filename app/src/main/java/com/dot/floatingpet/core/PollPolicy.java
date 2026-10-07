package com.dot.floatingpet.core;

/**
 * Pure scheduling policy for a single, sequential polling worker. All durations must
 * come from a monotonic clock. This class neither queries usage events nor schedules
 * work: the caller must gate queries on eligibility and stop scheduling screen-off.
 * Use one instance per monitor, confined to its worker thread.
 */
public final class PollPolicy {
    public static final long FAST_INTERVAL_MS = 200;
    public static final long ECONOMY_INTERVAL_MS = 500;
    public static final long SLOW_QUERY_MS = 200;
    public static final long MIN_QUIET_MS = 100;
    public static final long INELIGIBLE_DELAY_MS = 1000;
    public static final long INITIAL_BACKOFF_MS = 1000;
    public static final long MAX_BACKOFF_MS = 2000;

    private long backoffMillis;

    /**
     * Returns a delay to apply AFTER the current check completes. Normal intervals
     * are start-to-start, so query duration is included rather than added again.
     * Every completion gets at least 100 ms of quiet time; a busy/slow worker can
     * therefore run less often than the selected minimum interval, never faster.
     *
     * Slow or failed eligible queries back off to a 1000 ms, then 2000 ms interval.
     * A timely successful query immediately restores the currently selected mode.
     * Ineligibility clears backoff and requests a 1000 ms eligibility recheck only;
     * it is NOT permission to perform a query. Screen-off should not reschedule.
     * Negative durations are treated as zero defensively, never as a catch-up hint.
     */
    public long nextDelayMillis(boolean fast, long queryDurationMillis,
                                boolean querySucceeded, boolean eligible) {
        if (!eligible) {
            backoffMillis = 0;
            return INELIGIBLE_DELAY_MS;
        }
        long duration = Math.max(0, queryDurationMillis);
        long interval = fast ? FAST_INTERVAL_MS : ECONOMY_INTERVAL_MS;
        if (!querySucceeded || duration >= SLOW_QUERY_MS) {
            backoffMillis = backoffMillis == 0
                    ? INITIAL_BACKOFF_MS
                    : Math.min(MAX_BACKOFF_MS, backoffMillis * 2);
            interval = Math.max(interval, backoffMillis);
        } else {
            backoffMillis = 0;
        }
        return Math.max(MIN_QUIET_MS, interval - duration);
    }
}
