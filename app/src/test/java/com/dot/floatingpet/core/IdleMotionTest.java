package com.dot.floatingpet.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class IdleMotionTest {
    private static final float EPSILON = 0.000001f;

    @Test public void initiallyUnavailableIsIdentityAndHasNoTimer() {
        IdleMotion motion = new IdleMotion();
        for (long now : new long[] {0, 100, 10_000, 86_400_000, Long.MAX_VALUE}) {
            assertIdentity(sample(motion, now));
            assertFalse(motion.needsFrame(now));
        }
    }

    @Test public void availableStartsSmoothThreeHundredMillisecondFade() {
        IdleMotion motion = new IdleMotion();
        motion.setAvailable(true, 1_000);
        assertIdentity(sample(motion, 1_000));
        assertTrue(motion.needsFrame(1_000));
        assertEquals(0.15625f, sample(motion, 1_075).amplitude, EPSILON);
        assertEquals(0.5f, sample(motion, 1_150).amplitude, EPSILON);
        assertEquals(0.84375f, sample(motion, 1_225).amplitude, EPSILON);
        assertEquals(1f, sample(motion, 1_300).amplitude, 0f);
        assertTrue(motion.needsFrame(1_300));
        assertEquals(40, IdleMotion.FRAME_INTERVAL_MS);
    }

    @Test public void breathIsFiveSecondsAndSwayIsTenSeconds() {
        IdleMotion motion = active();
        IdleMotion.Transform inhale = sample(motion, 2_500);
        assertEquals(1f - IdleMotion.MAX_HEIGHT_CONTRACTION, inhale.scaleY, EPSILON);
        assertEquals(IdleMotion.MAX_SWAY_WIDTH_FRACTION, inhale.swayWidthFraction, EPSILON);
        IdleMotion.Transform exhale = sample(motion, 5_000);
        assertEquals(1f, exhale.scaleY, EPSILON);
        assertEquals(0f, exhale.swayWidthFraction, EPSILON);
        assertEquals(-IdleMotion.MAX_SWAY_WIDTH_FRACTION, sample(motion, 7_500).swayWidthFraction, EPSILON);
        assertSameTransform(sample(motion, 2_500), sample(motion, 12_500));
    }

    @Test public void touchEntryFadesThenStopsSchedulingUntilReleased() {
        IdleMotion motion = active();
        IdleMotion.Transform before = sample(motion, 2_500);
        motion.setAllowed(false, 2_500);
        assertSameTransform(before, sample(motion, 2_500));
        assertEquals(0.5f, sample(motion, 2_650).amplitude, EPSILON);
        assertTrue(motion.needsFrame(2_799));
        assertIdentity(sample(motion, 2_800));
        assertFalse(motion.needsFrame(2_800));
        assertIdentity(sample(motion, 20_000));
        assertFalse(motion.needsFrame(20_000));
        motion.setAllowed(true, 20_000);
        assertIdentity(sample(motion, 20_000));
        assertTrue(motion.needsFrame(20_000));
        assertEquals(0.5f, sample(motion, 20_150).amplitude, EPSILON);
        assertEquals(1f, sample(motion, 20_300).amplitude, 0f);
    }

    @Test public void interruptedFadesContinueFromCurrentAmplitude() {
        IdleMotion motion = active();
        motion.setAllowed(false, 1_000);
        IdleMotion.Transform before = sample(motion, 1_150);
        assertEquals(0.5f, before.amplitude, EPSILON);
        motion.setAllowed(true, 1_150);
        assertSameTransform(before, sample(motion, 1_150));
        assertEquals(0.75f, sample(motion, 1_300).amplitude, EPSILON);
        motion.setAllowed(false, 1_300);
        assertEquals(0.75f, sample(motion, 1_300).amplitude, EPSILON);
        assertIdentity(sample(motion, 1_600));
        assertFalse(motion.needsFrame(1_600));
    }

    @Test public void repeatedGateUpdatesDoNotRestartFadesOrPhase() {
        IdleMotion motion = active();
        IdleMotion.Transform before = sample(motion, 2_500);
        motion.setAvailable(true, 2_500);
        motion.setEnabled(true, 2_500);
        motion.setAllowed(true, 2_500);
        assertSameTransform(before, sample(motion, 2_500));
        motion.setAllowed(false, 2_500);
        motion.setAllowed(false, 2_650);
        assertEquals(0.5f, sample(motion, 2_650).amplitude, EPSILON);
        assertFalse(motion.needsFrame(2_800));
    }

    @Test public void disablingPreferenceStopsImmediatelyAndReenablesGently() {
        IdleMotion motion = active();
        assertTrue(sample(motion, 2_500).amplitude > 0f);
        motion.setEnabled(false, 2_500);
        assertIdentity(sample(motion, 2_500));
        assertFalse(motion.needsFrame(2_500));
        motion.setAvailable(false, 3_000);
        motion.setAvailable(true, 4_000);
        assertIdentity(sample(motion, 4_500));
        assertFalse(motion.needsFrame(4_500));
        motion.setEnabled(true, 5_000);
        assertIdentity(sample(motion, 5_000));
        assertTrue(motion.needsFrame(5_000));
        assertEquals(0.5f, sample(motion, 5_150).amplitude, EPSILON);
    }

    @Test public void hiddenDetachedOrSystemDisabledStopsImmediately() {
        IdleMotion motion = active();
        motion.setAvailable(false, 2_500);
        assertIdentity(sample(motion, 2_500));
        assertFalse(motion.needsFrame(2_500));
        assertIdentity(sample(motion, 9_000));
        motion.setAvailable(true, 10_000);
        assertIdentity(sample(motion, 10_000));
        assertTrue(motion.needsFrame(10_000));
        assertEquals(0.5f, sample(motion, 10_150).amplitude, EPSILON);
        // A queued callback sampled after another detach cannot revive animation.
        motion.setAvailable(false, 10_180);
        assertIdentity(sample(motion, 10_220));
        assertFalse(motion.needsFrame(10_220));
    }

    @Test public void unavailableAllowedChangesNeverStartTimers() {
        IdleMotion motion = new IdleMotion();
        motion.setAllowed(false, 100);
        motion.setAllowed(true, 200);
        assertFalse(motion.needsFrame(200));
        motion.setAllowed(false, 300);
        motion.setAvailable(true, 400);
        assertIdentity(sample(motion, 700));
        assertFalse(motion.needsFrame(700));
        motion.setEnabled(false, 800);
        motion.setAllowed(true, 900);
        assertFalse(motion.needsFrame(900));
        motion.setEnabled(true, 1_000);
        assertTrue(motion.needsFrame(1_000));
        assertEquals(1f, sample(motion, 1_300).amplitude, 0f);
    }

    @Test public void rapidLifecycleChangesRemainQuietAndRestartCleanly() {
        IdleMotion motion = new IdleMotion();
        for (long now = 0; now < 2_000; now += 20) {
            motion.setAvailable(true, now);
            motion.setAllowed(false, now + 2);
            motion.setAllowed(true, now + 3);
            motion.setAvailable(false, now + 4);
            assertIdentity(sample(motion, now + 10));
            assertFalse(motion.needsFrame(now + 10));
        }
        motion.setAvailable(true, 3_000);
        assertIdentity(sample(motion, 3_000));
        assertEquals(1f, sample(motion, 3_300).amplitude, 0f);
    }

    @Test public void fullCellCornersAndBottomCenterStayContainedAtEveryPhase() {
        IdleMotion motion = active();
        for (long now = 0; now <= 20_000; now++) {
            if (now == 8_000) motion.setAllowed(false, now);
            if (now == 8_250) motion.setAllowed(true, now);
            assertContained(sample(motion, now));
        }
    }

    @Test public void successiveFortyMillisecondFramesStaySubpixelAtLargestSize() {
        IdleMotion motion = active();
        IdleMotion.Transform previous = sample(motion, 400);
        for (long now = 440; now < 20_000; now += 40) {
            IdleMotion.Transform current = sample(motion, now);
            // At 240 dp, steady-state top-edge changes stay well below 0.2 dp/frame.
            assertTrue(Math.abs(current.scaleY - previous.scaleY) * 260 < 0.2f);
            assertTrue(Math.abs(current.swayWidthFraction - previous.swayWidthFraction) * 240 < 0.1f);
            previous = current;
        }
    }

    @Test public void veryLongUptimeHasFiniteBoundedPeriodicOutput() {
        IdleMotion motion = active();
        long now = Long.MAX_VALUE - 1_000;
        assertContained(sample(motion, now));
        assertSameTransform(sample(motion, now), sample(motion, now % IdleMotion.SWAY_PERIOD_MS));
        motion.setAllowed(false, now);
        assertIdentity(sample(motion, now + 300));
        assertFalse(motion.needsFrame(now + 300));
    }

    @Test public void futureEpochAndClockRegressionNeverProduceInvalidTransforms() {
        IdleMotion motion = new IdleMotion();
        motion.setAvailable(true, 1_000);
        assertIdentity(sample(motion, 0));
        assertIdentity(sample(motion, -1));
        assertContained(sample(motion, Long.MAX_VALUE));
        motion.setAllowed(false, Long.MAX_VALUE - 200);
        assertContained(sample(motion, Long.MAX_VALUE));
    }

    @Test public void reusableOutputResetsAllFieldsAtIdentity() {
        IdleMotion motion = active();
        IdleMotion.Transform output = new IdleMotion.Transform();
        motion.sample(2_500, output);
        assertTrue(output.swayWidthFraction > 0f);
        motion.setEnabled(false, 2_500);
        motion.sample(2_500, output);
        assertIdentity(output);
        output.scaleX = 99;
        motion.sample(2_500, output);
        assertIdentity(output);
    }

    private static IdleMotion active() {
        IdleMotion motion = new IdleMotion();
        motion.setAvailable(true, 0);
        return motion;
    }

    private static IdleMotion.Transform sample(IdleMotion motion, long now) {
        IdleMotion.Transform output = new IdleMotion.Transform();
        motion.sample(now, output);
        return output;
    }

    private static void assertIdentity(IdleMotion.Transform transform) {
        assertEquals(1f, transform.scaleX, 0f);
        assertEquals(1f, transform.scaleY, 0f);
        assertEquals(0f, transform.swayWidthFraction, 0f);
        assertEquals(0f, transform.amplitude, 0f);
    }

    private static void assertSameTransform(IdleMotion.Transform a, IdleMotion.Transform b) {
        assertEquals(a.scaleX, b.scaleX, EPSILON);
        assertEquals(a.scaleY, b.scaleY, EPSILON);
        assertEquals(a.swayWidthFraction, b.swayWidthFraction, EPSILON);
        assertEquals(a.amplitude, b.amplitude, EPSILON);
    }

    private static void assertContained(IdleMotion.Transform transform) {
        assertTrue(Float.isFinite(transform.scaleX));
        assertTrue(Float.isFinite(transform.scaleY));
        assertTrue(Float.isFinite(transform.swayWidthFraction));
        assertTrue(transform.amplitude >= 0 && transform.amplitude <= 1);
        assertTrue(transform.scaleX >= 1f - IdleMotion.MAX_WIDTH_CONTRACTION && transform.scaleX <= 1);
        assertTrue(transform.scaleY >= 1f - IdleMotion.MAX_HEIGHT_CONTRACTION && transform.scaleY <= 1);
        assertTrue(Math.abs(transform.swayWidthFraction) <= IdleMotion.MAX_SWAY_WIDTH_FRACTION);
        // Match PetView's matrix: translate to bottom-center, shear, scale, translate back.
        for (float x : new float[] {0, 0.5f, 1}) {
            for (float y : new float[] {0, 1}) {
                float transformedX = 0.5f + transform.scaleX * (x - 0.5f)
                        + transform.swayWidthFraction * transform.scaleY * (1 - y);
                float transformedY = 1 - transform.scaleY * (1 - y);
                assertTrue(transformedX >= -EPSILON && transformedX <= 1 + EPSILON);
                assertTrue(transformedY >= -EPSILON && transformedY <= 1 + EPSILON);
                if (x == 0.5f && y == 1) {
                    assertEquals(0.5f, transformedX, 0f);
                    assertEquals(1f, transformedY, 0f);
                }
            }
        }
    }
}
