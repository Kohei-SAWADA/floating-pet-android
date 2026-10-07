package com.dot.floatingpet.core;
import org.junit.Test;
import static org.junit.Assert.*;
public class UsageQueryWindowTest {
    @Test public void neverReadsBeforeExplicitSession(){assertEquals(10000,UsageQueryWindow.begin(10000,10000,10500));}
    @Test public void normalPollingOverlapsForDelayedEvents(){assertEquals(18000,UsageQueryWindow.begin(10000,20000,20500));}
    @Test public void sixSecondSchedulingDelayKeepsMissedHomeEvent(){assertEquals(18000,UsageQueryWindow.begin(10000,20000,26000));assertFalse(UsageQueryWindow.incompleteCoverage(20000,26000));}
    @Test public void retryAfterFailureUsesLastSuccessfulEnd(){assertEquals(18000,UsageQueryWindow.begin(10000,20000,22500));}
    @Test public void longGapHasBoundedRecovery(){assertEquals(70000,UsageQueryWindow.begin(10000,20000,100000));assertTrue(UsageQueryWindow.incompleteCoverage(20000,100000));}
    @Test public void exactCoverageLimitDoesNotDropTransition(){assertEquals(20000,UsageQueryWindow.begin(10000,20000,50000));assertFalse(UsageQueryWindow.incompleteCoverage(20000,50000));}
    @Test(expected=IllegalArgumentException.class) public void backwardsClockRequiresNewSession(){UsageQueryWindow.begin(10000,20000,15000);}
    @Test public void newlyUnlockedSessionNeverReplaysPreUnlock(){assertEquals(90000,UsageQueryWindow.begin(90000,90000,90500));}
}
