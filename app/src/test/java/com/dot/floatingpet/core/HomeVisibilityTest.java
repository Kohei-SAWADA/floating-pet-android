package com.dot.floatingpet.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class HomeVisibilityTest {
    private static final String HOME = "com.example.launcher";
    private static final String YOUTUBE = "com.google.android.youtube";

    private static boolean show(HomeVisibility tracker) {
        return tracker.shouldShow(false, true, true, true);
    }

    @Test public void startsHiddenWithoutForegroundEvidence() {
        assertFalse(show(new HomeVisibility(HOME)));
    }

    @Test public void homeToYouTubeToHome() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(HOME, 10);
        assertTrue(show(tracker));
        tracker.paused(HOME, 20);
        tracker.resumed(YOUTUBE, 21);
        assertFalse(show(tracker));
        tracker.paused(YOUTUBE, 30);
        assertFalse(show(tracker));
        tracker.resumed(HOME, 31);
        assertTrue(show(tracker));
    }

    @Test public void homePauseHidesBeforeNextAppResumes() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(HOME, 10);
        tracker.paused(HOME, 20);
        assertFalse(show(tracker));
        tracker.resumed(YOUTUBE, 21);
        assertFalse(show(tracker));
    }

    @Test public void anotherResumeHidesEvenWithoutAHomePause() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(HOME, 10);
        tracker.resumed(YOUTUBE, 20);
        assertFalse(show(tracker));
        tracker.paused(YOUTUBE, 30);
        assertFalse(show(tracker));
        tracker.resumed(HOME, 40);
        assertTrue(show(tracker));
    }

    @Test public void homeCandidateBecomesVisibleWhenOlderAppFinallyPauses() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(YOUTUBE, 10);
        tracker.resumed(HOME, 20);
        assertFalse(show(tracker));
        tracker.paused(YOUTUBE, 30);
        assertTrue(show(tracker)); // No second home event should be necessary.
    }

    @Test public void manualHideSuppressesAndUnhidePreservesCurrentHomeEvidence() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(HOME, 10);
        assertFalse(tracker.shouldShow(true, true, true, true));
        assertTrue(show(tracker));
        tracker.resumed(YOUTUBE, 20);
        assertFalse(tracker.shouldShow(true, true, true, true));
        assertFalse(show(tracker));
    }

    @Test public void revokedUsageAccessInvalidatesEvidenceUntilANewResume() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(HOME, 10);
        assertFalse(tracker.shouldShow(false, true, true, false));
        assertFalse(show(tracker));
        tracker.resumed(HOME, 10);
        assertFalse(show(tracker));
        tracker.resumed(HOME, 11);
        assertTrue(show(tracker));
    }

    @Test public void lockInvalidatesEvidenceEvenWhileManuallyHidden() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(HOME, 10);
        assertFalse(tracker.shouldShow(true, true, false, true));
        assertFalse(show(tracker));
        tracker.resumed(HOME, 11);
        assertTrue(show(tracker));
    }

    @Test public void screenOffInvalidatesEvidence() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(HOME, 10);
        assertFalse(tracker.shouldShow(false, false, true, true));
        assertFalse(show(tracker));
        tracker.resumed(HOME, 11);
        assertTrue(show(tracker));
    }

    @Test public void unknownResumeBlocksEvenWhenHomeResumesAfterIt() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(HOME, 10);
        tracker.resumed(null, 20);
        assertFalse(show(tracker));
        tracker.resumed(HOME, 30);
        assertFalse(show(tracker));
        tracker.paused(null, 40);
        assertFalse(show(tracker));
        tracker.resumed(HOME, 50);
        assertTrue(show(tracker));
    }

    @Test public void unknownAndBlankPausesHideRatherThanAssumeHome() {
        for (String unknown : new String[] {null, "", "  "}) {
            HomeVisibility tracker = new HomeVisibility(HOME);
            tracker.resumed(HOME, 10);
            tracker.paused(unknown, 20);
            assertFalse(show(tracker));
            tracker.resumed(HOME, 30);
            assertTrue(show(tracker));
        }
    }

    @Test public void unresolvedDefaultHomeAlwaysHides() {
        for (String unknown : new String[] {null, "", " "}) {
            HomeVisibility tracker = new HomeVisibility(unknown);
            tracker.resumed(HOME, 10);
            assertFalse(show(tracker));
            tracker.resumed(unknown, 20);
            assertFalse(show(tracker));
        }
    }

    @Test public void staleHomeResumeCannotOverrideTheLatestApp() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(YOUTUBE, 20);
        tracker.paused(YOUTUBE, 30);
        tracker.resumed(HOME, 10);
        assertFalse(show(tracker));
        tracker.resumed(HOME, 31);
        assertTrue(show(tracker));
    }

    @Test public void staleEventsCannotOverrideNewerEvidenceForTheSameActivity() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(HOME, 20);
        tracker.paused(HOME, 10);
        tracker.paused(YOUTUBE, 12);
        tracker.resumed(YOUTUBE, 11);
        tracker.resumed(null, 12);
        tracker.paused(null, 13);
        assertTrue(show(tracker));
    }

    @Test public void equalTimestampEventsFailClosedOnAmbiguousRepeatedTransitions() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(HOME, 10);
        tracker.resumed(YOUTUBE, 10);
        assertFalse(show(tracker));
        tracker.resumed(HOME, 10);
        assertFalse(show(tracker));
        tracker.paused(YOUTUBE, 10);
        assertFalse(show(tracker));
        tracker.resumed(HOME, 10);
        assertFalse(show(tracker));
        tracker.resumed(HOME, 11);
        assertTrue(show(tracker));
        tracker.paused(HOME, 11);
        assertFalse(show(tracker));
    }

    @Test public void overlappingQueryReplayPreservesRecoveredHomeCandidate() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(YOUTUBE, 10);
        tracker.resumed(HOME, 20);
        tracker.paused(YOUTUBE, 20);
        assertTrue(show(tracker));
        // Replay the same ordered batch, as an overlapping UsageEvents query does.
        tracker.resumed(YOUTUBE, 10);
        tracker.resumed(HOME, 20);
        tracker.paused(YOUTUBE, 20);
        assertTrue(show(tracker));
    }

    @Test public void sameActivityPauseWinsTimestampTieInEitherOrder() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(HOME, 10);
        tracker.paused(HOME, 20);
        assertFalse(show(tracker));
        tracker.resumed(HOME, 20);
        assertFalse(show(tracker)); // Cannot distinguish real transitions from replay.
        tracker.paused(HOME, 20);
        tracker.resumed(HOME, 20);
        assertFalse(show(tracker));
        tracker.resumed(HOME, 21);
        assertTrue(show(tracker));
    }

    @Test public void replayedOtherResumeCannotUndoItsSameTimestampPause() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(YOUTUBE, 10);
        tracker.paused(YOUTUBE, 10);
        tracker.resumed(YOUTUBE, 10);
        tracker.resumed(HOME, 11);
        assertTrue(show(tracker));
    }

    @Test public void replayingAnUnambiguousHomeResumeRetainsVisibility() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(HOME, 10);
        tracker.resumed(HOME, 10);
        assertTrue(show(tracker));
    }

    @Test public void duplicateHomeResumesDoNotRequireMultiplePauses() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(HOME, 10);
        tracker.resumed(HOME, 20);
        assertTrue(show(tracker));
        tracker.paused(HOME, 30);
        assertFalse(show(tracker));
    }

    @Test public void resetForQueryFailureHidesAndRejectsHistoricalReplay() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(HOME, 10);
        tracker.reset();
        tracker.reset();
        assertFalse(show(tracker));
        tracker.resumed(HOME, 9);
        tracker.resumed(HOME, 10);
        assertFalse(show(tracker));
        tracker.resumed(HOME, 11);
        assertTrue(show(tracker));
    }

    @Test public void resetClearsPreviousMultiWindowBlockers() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(YOUTUBE, 10);
        tracker.resumed(null, 11);
        tracker.reset();
        tracker.resumed(HOME, 12);
        assertTrue(show(tracker));
    }

    @Test public void changingDefaultHomeRequiresFreshEvidence() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(HOME, 10);
        tracker.setHomePackage(HOME);
        assertTrue(show(tracker));
        tracker.setHomePackage("com.example.newlauncher");
        assertFalse(show(tracker));
        tracker.resumed("com.example.newlauncher", 10);
        assertFalse(show(tracker));
        tracker.resumed("com.example.newlauncher", 11);
        assertTrue(show(tracker));
        tracker.setHomePackage(null);
        assertFalse(show(tracker));
    }

    @Test public void constructorWithoutHomeCanBeResolvedLater() {
        HomeVisibility tracker = new HomeVisibility();
        tracker.reset();
        tracker.setHomePackage(HOME);
        tracker.resumed(HOME, 0);
        assertTrue(show(tracker));
    }

    @Test public void homeBeforeYouTubePauseRecoversWithoutAnotherHomeResume() {
        HomeVisibility tracker = homeToYouTube();
        tracker.resumed(HOME, "HomeActivity", 31);
        assertFalse(show(tracker));
        tracker.paused(YOUTUBE, "WatchActivity", 32);
        assertTrue(show(tracker));
    }

    @Test public void delayedOlderYouTubePauseRecoversAfterNewerHomeEvent() {
        HomeVisibility tracker = homeToYouTube();
        tracker.resumed(HOME, "HomeActivity", 31);
        assertFalse(show(tracker));
        tracker.paused(YOUTUBE, "WatchActivity", 30);
        assertTrue(show(tracker));
    }

    @Test public void delayedYouTubeStopAlsoRecoversHomeCandidate() {
        HomeVisibility tracker = homeToYouTube();
        tracker.resumed(HOME, "HomeActivity", 31);
        tracker.stopped(YOUTUBE, "WatchActivity", 30);
        assertTrue(show(tracker));
    }

    @Test public void equalTimestampAppPauseAndHomeResumeRecoverInEitherOrder() {
        for (boolean homeFirst : new boolean[] {false, true}) {
            HomeVisibility tracker = homeToYouTube();
            if (homeFirst) tracker.resumed(HOME, "HomeActivity", 30);
            tracker.paused(YOUTUBE, "WatchActivity", 30);
            if (!homeFirst) tracker.resumed(HOME, "HomeActivity", 30);
            assertTrue(show(tracker));
            // Repeat the entire overlap, including both sides of the transition.
            tracker.resumed(YOUTUBE, "WatchActivity", 21);
            tracker.resumed(HOME, "HomeActivity", 30);
            tracker.paused(YOUTUBE, "WatchActivity", 30);
            assertTrue(show(tracker));
        }
    }

    @Test public void delayedOldHomeResumeCannotOverrideNewerYouTubeResume() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(YOUTUBE, "WatchActivity", 40);
        tracker.resumed(HOME, "HomeActivity", 30);
        assertFalse(show(tracker));
        tracker.paused(YOUTUBE, "WatchActivity", 50);
        assertFalse(show(tracker)); // No home candidate newer than YouTube.
        tracker.resumed(HOME, "HomeActivity", 51);
        assertTrue(show(tracker));
    }

    @Test public void oldAppPauseCannotClearThatActivitysNewerResume() {
        HomeVisibility tracker = homeToYouTube();
        tracker.resumed(HOME, "HomeActivity", 31);
        tracker.resumed(YOUTUBE, "WatchActivity", 40);
        tracker.paused(YOUTUBE, "WatchActivity", 30);
        assertFalse(show(tracker));
        tracker.stopped(YOUTUBE, "WatchActivity", 41);
        assertFalse(show(tracker));
        tracker.resumed(HOME, "HomeActivity", 42);
        assertTrue(show(tracker));
    }

    @Test public void terminalEventArrivingBeforeResumeStillInvalidatesOldHome() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(HOME, "HomeActivity", 10);
        tracker.paused(YOUTUBE, "WatchActivity", 30);
        tracker.resumed(YOUTUBE, "WatchActivity", 20);
        assertFalse(show(tracker));
        tracker.resumed(HOME, "HomeActivity", 31);
        assertTrue(show(tracker));
    }

    @Test public void stopFromOldLauncherClassDoesNotClearNewHomeActivity() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(HOME, "OldHomeActivity", 10);
        tracker.paused(HOME, "OldHomeActivity", 20);
        tracker.resumed(YOUTUBE, "WatchActivity", 21);
        tracker.resumed(HOME, "NewHomeActivity", 31);
        tracker.paused(YOUTUBE, "WatchActivity", 30);
        assertTrue(show(tracker));
        tracker.stopped(HOME, "OldHomeActivity", 32);
        assertTrue(show(tracker));
        tracker.resumed(HOME, "OldHomeActivity", 10); // Overlap replay.
        tracker.stopped(HOME, "OldHomeActivity", 32);
        assertTrue(show(tracker));
        tracker.stopped(HOME, "NewHomeActivity", 33);
        assertFalse(show(tracker));
    }

    @Test public void delayedOldLauncherPauseDoesNotClearFreshResumeOfSameClass() {
        HomeVisibility tracker = homeToYouTube();
        tracker.resumed(HOME, "HomeActivity", 31);
        tracker.paused(HOME, "HomeActivity", 20);
        tracker.paused(YOUTUBE, "WatchActivity", 30);
        assertTrue(show(tracker));
    }

    @Test public void multiWindowActivitiesInSamePackageBlockIndependently() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(YOUTUBE, "WatchActivity", 10);
        tracker.resumed(YOUTUBE, "SettingsActivity", 11);
        tracker.resumed(HOME, "HomeActivity", 20);
        tracker.paused(YOUTUBE, "WatchActivity", 21);
        assertFalse(show(tracker));
        tracker.stopped(YOUTUBE, "WatchActivity", 22);
        assertFalse(show(tracker));
        tracker.paused(YOUTUBE, "SettingsActivity", 19); // Delayed per-class event.
        assertTrue(show(tracker));
    }

    @Test public void matchingClassNamesInDifferentPackagesStayIndependent() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(YOUTUBE, "MainActivity", 10);
        tracker.resumed(HOME, "MainActivity", 20);
        assertFalse(show(tracker));
        tracker.paused(YOUTUBE, "MainActivity", 21);
        assertTrue(show(tracker));
    }

    @Test public void delayedUnclosedNonHomeActivityRemainsAConservativeBlocker() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(HOME, "HomeActivity", 30);
        tracker.resumed(YOUTUBE, "WatchActivity", 10);
        assertFalse(show(tracker));
        tracker.paused(YOUTUBE, "WatchActivity", 20);
        assertTrue(show(tracker));
    }

    @Test public void simultaneousHomeAndNonHomeResumesAreAmbiguousInEitherOrder() {
        for (boolean homeFirst : new boolean[] {false, true}) {
            HomeVisibility tracker = new HomeVisibility(HOME);
            if (homeFirst) tracker.resumed(HOME, "HomeActivity", 10);
            tracker.resumed(YOUTUBE, "WatchActivity", 10);
            if (!homeFirst) tracker.resumed(HOME, "HomeActivity", 10);
            tracker.paused(YOUTUBE, "WatchActivity", 11);
            assertFalse(show(tracker));
            tracker.resumed(HOME, "HomeActivity", 10);
            assertFalse(show(tracker));
            tracker.resumed(HOME, "HomeActivity", 12);
            assertTrue(show(tracker));
        }
    }

    @Test public void sameTimestampStopWinsOverResumeRegardlessOfReplayOrder() {
        for (boolean resumeFirst : new boolean[] {false, true}) {
            HomeVisibility tracker = new HomeVisibility(HOME);
            if (resumeFirst) tracker.resumed(HOME, "HomeActivity", 10);
            tracker.stopped(HOME, "HomeActivity", 10);
            if (!resumeFirst) tracker.resumed(HOME, "HomeActivity", 10);
            tracker.resumed(HOME, "HomeActivity", 10);
            assertFalse(show(tracker));
            tracker.resumed(HOME, "HomeActivity", 11);
            assertTrue(show(tracker));
        }
    }

    @Test public void missingActivityClassUsesOnePackageFallback() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(YOUTUBE, null, 10);
        tracker.resumed(HOME, null, 20);
        assertFalse(show(tracker));
        tracker.paused(YOUTUBE, " ", 21);
        assertTrue(show(tracker));
        tracker.stopped(HOME, "", 22);
        assertFalse(show(tracker));
    }

    @Test public void unknownStopRequiresFreshHomeEvidence() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(HOME, "HomeActivity", 10);
        tracker.stopped(null, null, 20);
        assertFalse(show(tracker));
        tracker.resumed(HOME, "HomeActivity", 21);
        assertTrue(show(tracker));
    }

    @Test public void missingClassHomeTerminalInvalidatesNamedHomeCandidates() {
        for (boolean stop : new boolean[] {false, true}) {
            for (boolean terminalFirst : new boolean[] {false, true}) {
                HomeVisibility tracker = new HomeVisibility(HOME);
                if (!terminalFirst) tracker.resumed(HOME, "HomeActivity", 10);
                if (stop) tracker.stopped(HOME, null, 20);
                else tracker.paused(HOME, null, 20);
                if (terminalFirst) tracker.resumed(HOME, "HomeActivity", 10);
                assertFalse(show(tracker));
                tracker.resumed(HOME, "HomeActivity", 21);
                assertTrue(show(tracker));
            }
        }
    }

    @Test public void namedHomeTerminalInvalidatesOlderMissingClassCandidate() {
        for (boolean stop : new boolean[] {false, true}) {
            for (boolean terminalFirst : new boolean[] {false, true}) {
                HomeVisibility tracker = new HomeVisibility(HOME);
                if (!terminalFirst) tracker.resumed(HOME, null, 10);
                if (stop) tracker.stopped(HOME, "HomeActivity", 20);
                else tracker.paused(HOME, "HomeActivity", 20);
                if (terminalFirst) tracker.resumed(HOME, null, 10);
                assertFalse(show(tracker));
                tracker.resumed(HOME, null, 21);
                assertTrue(show(tracker));
            }
        }
    }

    @Test public void oldMissingClassHomeTerminalDoesNotInvalidateNewerNamedHome() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(HOME, "HomeActivity", 30);
        tracker.stopped(HOME, null, 20);
        assertTrue(show(tracker));
    }

    @Test public void namedHomeTerminalDoesNotInvalidateDifferentNamedHomeCandidate() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(HOME, null, 10);
        tracker.resumed(HOME, "NewHomeActivity", 20);
        tracker.stopped(HOME, "OldHomeActivity", 30);
        assertTrue(show(tracker));
    }

    @Test public void missingClassAppPauseDoesNotClearKnownActivityBlocker() {
        HomeVisibility tracker = homeToYouTube();
        tracker.resumed(HOME, "HomeActivity", 30);
        tracker.paused(YOUTUBE, null, 31);
        assertFalse(show(tracker));
        tracker.stopped(YOUTUBE, "WatchActivity", 32);
        assertTrue(show(tracker));
    }

    @Test public void resetRejectsReplayFromEveryActivityAtItsMaximumTimestamp() {
        HomeVisibility tracker = homeToYouTube();
        tracker.resumed(HOME, "NewHomeActivity", 40);
        tracker.paused(YOUTUBE, "WatchActivity", 30);
        assertTrue(show(tracker));
        tracker.reset();
        tracker.resumed(HOME, "PreviouslyUnseenHomeActivity", 40);
        tracker.resumed(YOUTUBE, "PreviouslyUnseenWatchActivity", 39);
        assertFalse(show(tracker));
        tracker.resumed(HOME, "NewHomeActivity", 41);
        assertTrue(show(tracker));
    }

    @Test public void boundedStateOverflowFailsClosedAndRejectsOldReplay() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(HOME, "HomeActivity", 10);
        for (int i = 1; i < HomeVisibility.MAX_TRACKED_ACTIVITIES; i++) {
            tracker.paused(YOUTUBE, "Activity" + i, 10 + i);
        }
        assertTrue(show(tracker));
        tracker.paused(YOUTUBE, "OverflowActivity", 1000);
        assertFalse(show(tracker));
        tracker.resumed(HOME, "HomeActivity", 10);
        tracker.resumed(HOME, "NewHomeActivity", 1000);
        assertFalse(show(tracker));
        tracker.resumed(HOME, "HomeActivity", 1001);
        assertTrue(show(tracker));
    }

    @Test public void everyBatchedDeliveryOrderAndTwoSecondOverlapConverges() {
        int[] events = {0, 1, 2, 3, 4, 5};
        assertEveryOrderRecovers(events, 0);
    }

    private static HomeVisibility homeToYouTube() {
        HomeVisibility tracker = new HomeVisibility(HOME);
        tracker.resumed(HOME, "HomeActivity", 10);
        tracker.paused(HOME, "HomeActivity", 20);
        tracker.resumed(YOUTUBE, "WatchActivity", 21);
        assertFalse(show(tracker));
        return tracker;
    }

    private static void assertEveryOrderRecovers(int[] events, int index) {
        if (index == events.length) {
            HomeVisibility tracker = new HomeVisibility(HOME);
            for (int event : events) applyBatchedEvent(tracker, event);
            assertTrue("Batch must recover regardless of cross-activity delivery order", show(tracker));
            for (int replay = 0; replay < 3; replay++) {
                for (int event = 0; event < events.length; event++) {
                    applyBatchedEvent(tracker, event);
                    assertTrue("Overlapping historical replay must not change visibility", show(tracker));
                }
            }
            tracker.resumed(YOUTUBE, "WatchActivity", 1300);
            for (int event : events) {
                applyBatchedEvent(tracker, event);
                assertFalse("Historical home replay must not override a newer app", show(tracker));
            }
            return;
        }
        for (int i = index; i < events.length; i++) {
            int old = events[index];
            events[index] = events[i];
            events[i] = old;
            assertEveryOrderRecovers(events, index + 1);
            old = events[index];
            events[index] = events[i];
            events[i] = old;
        }
    }

    private static void applyBatchedEvent(HomeVisibility tracker, int event) {
        // Entire history is within a two-second overlap of the latest event.
        switch (event) {
            case 0: tracker.resumed(HOME, "HomeActivity", 1000); break;
            case 1: tracker.paused(HOME, "HomeActivity", 1100); break;
            case 2: tracker.resumed(YOUTUBE, "WatchActivity", 1101); break;
            case 3: tracker.resumed(HOME, "HomeActivity", 1200); break;
            case 4: tracker.paused(YOUTUBE, "WatchActivity", 1199); break;
            case 5: tracker.stopped(YOUTUBE, "WatchActivity", 1202); break;
            default: fail("Unknown test event");
        }
    }
}
