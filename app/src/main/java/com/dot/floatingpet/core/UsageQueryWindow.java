package com.dot.floatingpet.core;

/** Bounded, current-session query coverage; scheduling delays must not drop transitions. */
public final class UsageQueryWindow {
    public static final long OVERLAP_MS=2000;
    public static final long MAX_SPAN_MS=30000;
    private UsageQueryWindow() { }
    public static long begin(long sessionStart,long lastSuccessfulEnd,long now) {
        if(sessionStart<0||lastSuccessfulEnd<sessionStart||now<lastSuccessfulEnd)
            throw new IllegalArgumentException("Invalid query clock/session");
        return Math.max(sessionStart,Math.max(lastSuccessfulEnd-OVERLAP_MS,now-MAX_SPAN_MS));
    }
    /** A gap beyond bounded coverage invalidates old activity evidence, not recent events. */
    public static boolean incompleteCoverage(long lastSuccessfulEnd,long now) {
        return now-lastSuccessfulEnd>MAX_SPAN_MS;
    }
}
