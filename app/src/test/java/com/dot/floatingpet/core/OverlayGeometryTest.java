package com.dot.floatingpet.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class OverlayGeometryTest {
    @Test public void widthDpConversionPreservesSpriteAspectAndDensity() {
        assertArrayEquals(new int[] {192, 208}, OverlayGeometry.sizeForWidthDp(192, 1));
        assertArrayEquals(new int[] {384, 416}, OverlayGeometry.sizeForWidthDp(192, 2));
        assertArrayEquals(new int[] {144, 156}, OverlayGeometry.sizeForWidthDp(96, 1.5f));
        assertArrayEquals(new int[] {128, 139}, OverlayGeometry.sizeForWidthDp(128, 1));
    }

    @Test public void fractionalDpConversionAlwaysProducesAtLeastOnePixel() {
        assertArrayEquals(new int[] {1, 1}, OverlayGeometry.sizeForWidthDp(0.01f, 0.01f));
    }

    @Test public void dpConversionRejectsInvalidNumbersAndOverflow() {
        float[] invalid = {0, -1, Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY};
        for (float value : invalid) {
            assertThrows(IllegalArgumentException.class, () -> OverlayGeometry.sizeForWidthDp(value, 1));
            assertThrows(IllegalArgumentException.class, () -> OverlayGeometry.sizeForWidthDp(1, value));
        }
        assertThrows(IllegalArgumentException.class, () -> OverlayGeometry.sizeForWidthDp(Float.MAX_VALUE, 2));
        assertThrows(IllegalArgumentException.class, () -> OverlayGeometry.sizeForWidthDp(2_000_000_000f, 1));
    }

    @Test public void clampsNegativeAndFarOffscreenPositions() {
        assertArrayEquals(new int[] {0, 0}, OverlayGeometry.clampPosition(-10, -100, 192, 208, 1080, 1920));
        assertArrayEquals(new int[] {888, 1712}, OverlayGeometry.clampPosition(9999, 9999, 192, 208, 1080, 1920));
        assertArrayEquals(new int[] {100, 200}, OverlayGeometry.clampPosition(100, 200, 192, 208, 1080, 1920));
    }

    @Test public void positionClampNeverGoesNegativeOnTinyScreens() {
        assertArrayEquals(new int[] {0, 0}, OverlayGeometry.clampPosition(40, 80, 192, 208, 1, 1));
        assertArrayEquals(new int[] {0, 0}, OverlayGeometry.clampPosition(40, 80, 192, 208, 0, 0));
    }

    @Test public void positionClampHandlesExtremeCoordinates() {
        assertArrayEquals(new int[] {0, 1712}, OverlayGeometry.clampPosition(
                Integer.MIN_VALUE, Integer.MAX_VALUE, 192, 208, 1080, 1920));
        assertArrayEquals(new int[] {0, 0}, OverlayGeometry.clampPosition(
                Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 1, 1));
    }

    @Test public void sizeClampPreservesAspectAndNeverEnlarges() {
        assertArrayEquals(new int[] {192, 208}, OverlayGeometry.clampSize(192, 208, 1080, 1920));
        assertArrayEquals(new int[] {96, 104}, OverlayGeometry.clampSize(192, 208, 96, 1000));
        assertArrayEquals(new int[] {96, 104}, OverlayGeometry.clampSize(192, 208, 1000, 104));
        assertArrayEquals(new int[] {12, 13}, OverlayGeometry.clampSize(192, 208, 12, 13));
    }

    @Test public void sizeClampFitsOnePixelAndEmptyScreens() {
        assertArrayEquals(new int[] {1, 1}, OverlayGeometry.clampSize(192, 208, 1, 1));
        assertArrayEquals(new int[] {1, 1}, OverlayGeometry.clampSize(192, 208, 1, 100));
        assertArrayEquals(new int[] {0, 0}, OverlayGeometry.clampSize(192, 208, 0, 10));
        assertArrayEquals(new int[] {0, 0}, OverlayGeometry.clampSize(192, 208, 10, 0));
        assertArrayEquals(new int[] {0, 0}, OverlayGeometry.clampSize(0, 208, 10, 10));
    }

    @Test public void combinedBoundsFitTheSizeBeforeClampingPosition() {
        assertArrayEquals(new int[] {0, 96, 96, 104}, OverlayGeometry.clampBounds(300, 500, 192, 208, 96, 200));
        assertArrayEquals(new int[] {0, 0, 1, 1}, OverlayGeometry.clampBounds(300, 500, 192, 208, 1, 1));
        assertArrayEquals(new int[] {0, 0, 0, 0}, OverlayGeometry.clampBounds(300, 500, 192, 208, 0, 0));
    }

    @Test public void negativeDimensionsAreRejected() {
        int[][] invalid = {{-1, 2, 3, 4}, {1, -2, 3, 4}, {1, 2, -3, 4}, {1, 2, 3, -4}};
        for (int[] dims : invalid) {
            assertThrows(IllegalArgumentException.class,
                    () -> OverlayGeometry.clampSize(dims[0], dims[1], dims[2], dims[3]));
            assertThrows(IllegalArgumentException.class,
                    () -> OverlayGeometry.clampPosition(0, 0, dims[0], dims[1], dims[2], dims[3]));
            assertThrows(IllegalArgumentException.class,
                    () -> OverlayGeometry.clampBounds(0, 0, dims[0], dims[1], dims[2], dims[3]));
        }
    }

    @Test public void genericClampHandlesBoundariesAndInvalidRange() {
        assertEquals(4, OverlayGeometry.clamp(-10, 4, 8));
        assertEquals(8, OverlayGeometry.clamp(20, 4, 8));
        assertEquals(6, OverlayGeometry.clamp(6, 4, 8));
        assertEquals(4, OverlayGeometry.clamp(6, 4, 4));
        assertThrows(IllegalArgumentException.class, () -> OverlayGeometry.clamp(0, 10, 5));
    }

    @Test public void touchSlopUsesEuclideanDistanceAndStrictBoundary() {
        assertFalse(OverlayGeometry.isDrag(0, 0, 0, 0, 5));
        assertFalse(OverlayGeometry.isDrag(0, 0, 3, 4, 5));
        assertTrue(OverlayGeometry.isDrag(0, 0, 4, 4, 5));
        assertTrue(OverlayGeometry.isDrag(10, 10, 4, 10, 5));
        assertTrue(OverlayGeometry.isDrag(0, 0, 0.01f, 0, 0));
        assertFalse(OverlayGeometry.isDrag(0, 0, 0, 0, 0));
    }

    @Test public void cumulativeSmallMovesBecomeDragRelativeToInitialTouch() {
        OverlayGeometry.DragTracker tracker = new OverlayGeometry.DragTracker(8);
        tracker.down(100, 100);
        assertFalse(tracker.move(102, 100));
        assertFalse(tracker.move(104, 100));
        assertFalse(tracker.move(106, 100));
        assertFalse(tracker.move(108, 100));
        assertTrue(tracker.move(110, 100));
        assertFalse(tracker.up(110, 100));
    }

    @Test public void smallOscillationsDoNotAccumulatePathLengthIntoDrag() {
        OverlayGeometry.DragTracker tracker = new OverlayGeometry.DragTracker(8);
        tracker.down(100, 100);
        for (int i = 0; i < 20; i++) {
            assertFalse(tracker.move(103, 100));
            assertFalse(tracker.move(97, 100));
        }
        assertTrue(tracker.up(100, 100));
    }

    @Test public void dragStaysLatchedAfterReturningToStart() {
        OverlayGeometry.DragTracker tracker = new OverlayGeometry.DragTracker(8);
        tracker.down(0, 0);
        assertTrue(tracker.move(10, 0));
        assertTrue(tracker.move(0, 0));
        assertTrue(tracker.isDragging());
        assertFalse(tracker.up(0, 0));
        assertFalse(tracker.isDragging());
        assertFalse(tracker.isActive());
    }

    @Test public void upChecksFinalPositionEvenWhenNoMoveEventWasDelivered() {
        OverlayGeometry.DragTracker tracker = new OverlayGeometry.DragTracker(8);
        tracker.down(0, 0);
        assertFalse(tracker.up(20, 0));
        tracker.down(0, 0);
        assertTrue(tracker.up(2, 0));
    }

    @Test public void cancelPreventsTapAndResetsGesture() {
        OverlayGeometry.DragTracker tracker = new OverlayGeometry.DragTracker(8);
        tracker.down(0, 0);
        tracker.move(10, 0);
        tracker.cancel();
        assertFalse(tracker.isActive());
        assertFalse(tracker.isDragging());
        assertFalse(tracker.up(0, 0));
        tracker.down(5, 5);
        assertTrue(tracker.up(5, 5));
    }

    @Test public void newDownResetsAnInterruptedDrag() {
        OverlayGeometry.DragTracker tracker = new OverlayGeometry.DragTracker(8);
        tracker.down(0, 0);
        tracker.move(10, 0);
        tracker.down(100, 100);
        assertTrue(tracker.isActive());
        assertFalse(tracker.isDragging());
        assertTrue(tracker.up(100, 100));
    }

    @Test public void strayMoveAndUpCannotCreateTapOrDrag() {
        OverlayGeometry.DragTracker tracker = new OverlayGeometry.DragTracker(8);
        assertFalse(tracker.move(100, 100));
        assertFalse(tracker.up(100, 100));
    }

    @Test public void touchHelpersRejectNonfiniteValuesAndNegativeThreshold() {
        float[] invalid = {Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
        for (float v : invalid) {
            assertThrows(IllegalArgumentException.class, () -> OverlayGeometry.isDrag(v, 0, 0, 0, 8));
            assertThrows(IllegalArgumentException.class, () -> OverlayGeometry.isDrag(0, v, 0, 0, 8));
            assertThrows(IllegalArgumentException.class, () -> OverlayGeometry.isDrag(0, 0, v, 0, 8));
            assertThrows(IllegalArgumentException.class, () -> OverlayGeometry.isDrag(0, 0, 0, v, 8));
            assertThrows(IllegalArgumentException.class, () -> new OverlayGeometry.DragTracker(v));
        }
        assertThrows(IllegalArgumentException.class, () -> OverlayGeometry.isDrag(0, 0, 0, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> new OverlayGeometry.DragTracker(-1));
    }

    @Test public void touchSlopHandlesExtremeFloatCoordinatesWithoutOverflow() {
        assertTrue(OverlayGeometry.isDrag(-Float.MAX_VALUE, 0, Float.MAX_VALUE, 0, Float.MAX_VALUE));
        assertFalse(OverlayGeometry.isDrag(0, 0, Float.MAX_VALUE / 2, 0, Float.MAX_VALUE));
    }
}
