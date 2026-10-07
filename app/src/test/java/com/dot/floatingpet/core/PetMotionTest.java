package com.dot.floatingpet.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class PetMotionTest {
    @Test public void idleIsOneFrozenPoseEvenAfterALongTime() {
        PetMotion motion = new PetMotion();
        for (long now : new long[] {0, 100, 10_000, 86_400_000, Long.MAX_VALUE}) {
            assertArrayEquals(new int[] {0, 0}, motion.pose(now));
            assertFalse(motion.isActive(now));
        }
    }

    @Test public void touchPlaysExactlyOneBoundedWaveWhileHeld() {
        PetMotion motion = new PetMotion();
        motion.down(1_000);
        for (int frame = 0; frame < 4; frame++) {
            assertArrayEquals(new int[] {3, frame}, motion.pose(1_000 + frame * 100));
            assertTrue(motion.isActive(1_000 + frame * 100));
        }
        assertArrayEquals(new int[] {3, 3}, motion.pose(1_399));
        assertArrayEquals(new int[] {0, 0}, motion.pose(1_400));
        assertArrayEquals(new int[] {0, 0}, motion.pose(10_000));
        assertFalse(motion.isActive(1_400));
    }

    @Test public void actualHorizontalMovementSelectsDirectionAndAdvancesFrames() {
        PetMotion motion = new PetMotion();
        motion.down(0);
        motion.moved(8, 1, 10);
        assertArrayEquals(new int[] {1, 0}, motion.pose(10));
        motion.moved(8, 1, 90);
        assertArrayEquals(new int[] {1, 1}, motion.pose(90));
        motion.moved(-8, 1, 100);
        assertArrayEquals(new int[] {2, 0}, motion.pose(100));
        motion.moved(-8, 1, 180);
        assertArrayEquals(new int[] {2, 1}, motion.pose(180));
    }

    @Test public void actualVerticalMovementUsesJumpInEitherDirection() {
        PetMotion motion = new PetMotion();
        motion.down(0);
        motion.moved(0, -8, 10);
        assertArrayEquals(new int[] {4, 0}, motion.pose(10));
        motion.moved(0, 8, 90);
        assertArrayEquals(new int[] {4, 1}, motion.pose(90));
    }

    @Test public void stoppingWhileHeldReturnsToIdleWithoutResumingWave() {
        PetMotion motion = new PetMotion();
        motion.down(0);
        motion.moved(8, 0, 10);
        assertTrue(motion.isActive(129));
        assertArrayEquals(new int[] {1, 1}, motion.pose(129));
        assertFalse(motion.isActive(130));
        assertArrayEquals(new int[] {0, 0}, motion.pose(130));
        assertArrayEquals(new int[] {0, 0}, motion.pose(300));
        assertArrayEquals(new int[] {0, 0}, motion.pose(100_000));
    }

    @Test public void clampOrTinyJitterCannotExtendMovement() {
        PetMotion motion = new PetMotion();
        motion.down(0);
        motion.moved(8, 0, 10);
        motion.moved(0, 0, 100);
        motion.moved(0.2f, -0.3f, 110);
        motion.moved(Float.NaN, 2, 115);
        motion.moved(2, Float.POSITIVE_INFINITY, 119);
        assertFalse(motion.isActive(130));
        assertArrayEquals(new int[] {0, 0}, motion.pose(130));
        motion.moved(0, 0, 500);
        assertFalse(motion.isActive(500));
    }

    @Test public void zeroMovementCannotRestartExpiredTouchWave() {
        PetMotion motion = new PetMotion();
        motion.down(0);
        motion.moved(0, 0, 500);
        motion.moved(0.2f, 0.3f, 600);
        assertArrayEquals(new int[] {0, 0}, motion.pose(600));
        assertFalse(motion.isActive(600));
    }

    @Test public void diagonalAxisHysteresisAvoidsAlternatingRunAndJump() {
        PetMotion motion = new PetMotion();
        motion.down(0);
        motion.moved(6, 5, 10);
        motion.moved(5, 6, 20);
        assertEquals(1, motion.pose(20)[0]);
        motion.moved(5, 7, 30);
        assertEquals(4, motion.pose(30)[0]);
        motion.moved(6, 5, 40);
        assertEquals(4, motion.pose(40)[0]);
        motion.moved(7, 5, 50);
        assertEquals(1, motion.pose(50)[0]);
    }

    @Test public void restartingMovementAfterPauseBeginsFromFrameZero() {
        PetMotion motion = new PetMotion();
        motion.down(0);
        motion.moved(8, 0, 10);
        assertEquals(1, motion.pose(90)[1]);
        assertFalse(motion.isActive(130));
        motion.moved(8, 0, 500);
        assertArrayEquals(new int[] {1, 0}, motion.pose(500));
        assertTrue(motion.isActive(500));
    }

    @Test public void releaseStopsWaveAndFurtherMovementImmediately() {
        PetMotion motion = new PetMotion();
        motion.down(0);
        motion.release();
        assertArrayEquals(new int[] {0, 0}, motion.pose(10));
        motion.moved(8, 0, 20);
        assertFalse(motion.isActive(20));
        assertArrayEquals(new int[] {0, 0}, motion.pose(20));
    }

    @Test public void releaseStopsDragImmediately() {
        PetMotion motion = new PetMotion();
        motion.down(0);
        motion.moved(-8, 0, 10);
        motion.release();
        assertFalse(motion.isActive(10));
        assertArrayEquals(new int[] {0, 0}, motion.pose(10));
    }

    @Test public void cancelStopsWaveAndDragImmediatelyAndIsIdempotent() {
        PetMotion motion = new PetMotion();
        motion.down(0);
        motion.cancel();
        assertArrayEquals(new int[] {0, 0}, motion.pose(0));
        motion.down(100);
        motion.moved(0, 8, 110);
        motion.cancel();
        motion.cancel();
        assertFalse(motion.isActive(110));
        assertArrayEquals(new int[] {0, 0}, motion.pose(110));
    }

    @Test public void nextTouchStartsAFreshWave() {
        PetMotion motion = new PetMotion();
        motion.down(0);
        motion.moved(-8, 0, 10);
        motion.release();
        motion.down(1_000);
        assertArrayEquals(new int[] {3, 0}, motion.pose(1_000));
        assertArrayEquals(new int[] {3, 1}, motion.pose(1_100));
    }

    @Test public void animationNeverSelectsPaddingFramesDuringLongDrag() {
        for (int[] movement : new int[][] {{8, 0, 1}, {-8, 0, 2}, {0, 8, 4}}) {
            PetMotion motion = new PetMotion();
            motion.down(0);
            for (long now = 1; now < 10_000; now += 40) {
                motion.moved(movement[0], movement[1], now);
                int[] pose = motion.pose(now);
                assertEquals(movement[2], pose[0]);
                assertTrue(pose[1] >= 0);
                assertTrue(pose[1] < SpriteAtlas.frameCount(pose[0]));
                SpriteAtlas.frameRect(pose[0], pose[1]);
            }
        }
    }

    @Test public void returnedPoseCannotMutateState() {
        PetMotion motion = new PetMotion();
        motion.down(0);
        int[] pose = motion.pose(0);
        pose[0] = 10;
        pose[1] = 99;
        assertArrayEquals(new int[] {3, 0}, motion.pose(0));
    }
}
