package com.dot.floatingpet.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class SystemAnimationPolicyTest {
    @Test public void legacyReenableDoesNotLatchOnStaleFrameworkFalse() {
        assertTrue(SystemAnimationPolicy.allows(false, 1f, false, false));
    }
    @Test public void legacyDisableWinsOverStaleFrameworkTrue() {
        assertFalse(SystemAnimationPolicy.allows(false, 0f, false, true));
    }
    @Test public void legacyPowerSaveEntryStopsAndExitResumes() {
        assertFalse(SystemAnimationPolicy.allows(false, 1f, true, true));
        assertTrue(SystemAnimationPolicy.allows(false, 1f, false, false));
    }
    @Test public void modernListenerStateRemainsAdditionalGate() {
        assertFalse(SystemAnimationPolicy.allows(true, 1f, false, false));
        assertTrue(SystemAnimationPolicy.allows(true, 1f, false, true));
    }
    @Test public void modernDisabledSettingWinsOverEarlyFrameworkEnable() {
        assertFalse(SystemAnimationPolicy.allows(true, 0f, false, true));
    }
    @Test public void powerSaveAlwaysStopsMotion() {
        for (boolean modern : new boolean[]{false, true})
            for (boolean framework : new boolean[]{false, true})
                assertFalse(SystemAnimationPolicy.allows(modern, 1f, true, framework));
    }
    @Test public void invalidOrMissingSettingsFailClosed() {
        for (float scale : new float[]{0f, -1f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY})
            for (boolean modern : new boolean[]{false, true})
                assertFalse(SystemAnimationPolicy.allows(modern, scale, false, true));
    }
    @Test public void allPositiveFiniteScalesAllowWhenOtherwiseEnabled() {
        for (float scale : new float[]{0.5f, 1f, 2f, 10f})
            for (boolean modern : new boolean[]{false, true})
                assertTrue(SystemAnimationPolicy.allows(modern, scale, false, true));
    }
}
