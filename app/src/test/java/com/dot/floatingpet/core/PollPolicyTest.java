package com.dot.floatingpet.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class PollPolicyTest {
    @Test public void fastAccountsForQueryDuration() {
        assertEquals(195, new PollPolicy().nextDelayMillis(true, 5, true, true));
    }

    @Test public void economyAccountsForQueryDuration() {
        assertEquals(495, new PollPolicy().nextDelayMillis(false, 5, true, true));
    }

    @Test public void zeroDurationNeverCreatesBusyLoop() {
        PollPolicy policy = new PollPolicy();
        assertEquals(200, policy.nextDelayMillis(true, 0, true, true));
        assertEquals(500, policy.nextDelayMillis(false, 0, true, true));
    }

    @Test public void modeChangesApplyToNextDelay() {
        PollPolicy policy = new PollPolicy();
        assertEquals(195, policy.nextDelayMillis(true, 5, true, true));
        assertEquals(495, policy.nextDelayMillis(false, 5, true, true));
        assertEquals(195, policy.nextDelayMillis(true, 5, true, true));
    }

    @Test public void heavyTimelyQueryStillGetsQuietTime() {
        assertEquals(100, new PollPolicy().nextDelayMillis(true, 199, true, true));
    }

    @Test public void exactSlowThresholdStartsBackoff() {
        assertEquals(800, new PollPolicy().nextDelayMillis(true, 200, true, true));
    }

    @Test public void repeatedSlowQueriesReachAndKeepCap() {
        PollPolicy policy = new PollPolicy();
        assertEquals(800, policy.nextDelayMillis(true, 200, true, true));
        for (int i = 0; i < 10000; i++) {
            assertEquals(1800, policy.nextDelayMillis(true, 200, true, true));
        }
    }

    @Test public void failuresBackOffEvenWhenFast() {
        PollPolicy policy = new PollPolicy();
        assertEquals(995, policy.nextDelayMillis(true, 5, false, true));
        assertEquals(1995, policy.nextDelayMillis(true, 5, false, true));
    }

    @Test public void failureAndSlowQueryShareCappedBackoff() {
        PollPolicy policy = new PollPolicy();
        assertEquals(995, policy.nextDelayMillis(true, 5, false, true));
        assertEquals(1700, policy.nextDelayMillis(true, 300, true, true));
        assertEquals(1995, policy.nextDelayMillis(true, 5, false, true));
    }

    @Test public void modesCannotBypassActiveBackoff() {
        PollPolicy policy = new PollPolicy();
        assertEquals(995, policy.nextDelayMillis(false, 5, false, true));
        assertEquals(1995, policy.nextDelayMillis(true, 5, false, true));
        assertEquals(1995, policy.nextDelayMillis(false, 5, false, true));
    }

    @Test public void timelySuccessImmediatelyRecoversFastCadence() {
        PollPolicy policy = new PollPolicy();
        policy.nextDelayMillis(true, 5, false, true);
        policy.nextDelayMillis(true, 5, false, true);
        assertEquals(195, policy.nextDelayMillis(true, 5, true, true));
        assertEquals(995, policy.nextDelayMillis(true, 5, false, true));
    }

    @Test public void timelySuccessRecoversToCurrentEconomyMode() {
        PollPolicy policy = new PollPolicy();
        policy.nextDelayMillis(true, 5, false, true);
        assertEquals(495, policy.nextDelayMillis(false, 5, true, true));
    }

    @Test public void overrunCannotCreateCatchUpBurst() {
        PollPolicy policy = new PollPolicy();
        assertEquals(100, policy.nextDelayMillis(true, 5000, true, true));
        assertEquals(100, policy.nextDelayMillis(false, Long.MAX_VALUE, false, true));
    }

    @Test public void negativeDurationIsDefensivelyClamped() {
        assertEquals(200, new PollPolicy().nextDelayMillis(true, -1, true, true));
        assertEquals(500, new PollPolicy().nextDelayMillis(false, Long.MIN_VALUE, true, true));
    }

    @Test public void ineligibleUsesCheapRecheckRegardlessOfModeOrResult() {
        PollPolicy policy = new PollPolicy();
        assertEquals(1000, policy.nextDelayMillis(true, 5, true, false));
        assertEquals(1000, policy.nextDelayMillis(false, 5000, false, false));
    }

    @Test public void ineligibleClearsBackoffForFreshEligibleSession() {
        PollPolicy policy = new PollPolicy();
        policy.nextDelayMillis(true, 5, false, true);
        policy.nextDelayMillis(true, 5, false, true);
        policy.nextDelayMillis(true, 0, false, false);
        assertEquals(995, policy.nextDelayMillis(true, 5, false, true));
    }

    @Test public void everyDurationHonorsMinimumIntervalAndQuietTime() {
        for (boolean fast : new boolean[] {true, false}) {
            long minimum = fast ? 200 : 500;
            PollPolicy policy = new PollPolicy();
            for (int duration = 0; duration <= 10000; duration++) {
                long delay = policy.nextDelayMillis(fast, duration, duration % 3 != 0, true);
                assertTrue("No immediate catch-up after completion", delay >= 100);
                assertTrue("No start-to-start interval below mode minimum", duration + delay >= minimum);
            }
        }
    }

    @Test public void sixtySecondsOfSequentialFastQueriesHasBoundedCount() {
        PollPolicy policy = new PollPolicy();
        long start = 0;
        int queries = 0;
        while (start < 60000) {
            queries++;
            start += 5 + policy.nextDelayMillis(true, 5, true, true);
        }
        assertEquals(300, queries);
        assertEquals(60000, start);
    }

    @Test public void independentMonitorsDoNotShareBackoff() {
        new PollPolicy().nextDelayMillis(true, 5, false, true);
        assertEquals(195, new PollPolicy().nextDelayMillis(true, 5, true, true));
    }
}
