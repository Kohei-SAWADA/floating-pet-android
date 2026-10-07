package com.dot.floatingpet.core;

import org.junit.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public class SpriteAtlasTest {
    @Test public void declaresExactV2Layout() {
        assertEquals(1536, SpriteAtlas.WIDTH);
        assertEquals(2288, SpriteAtlas.HEIGHT);
        assertEquals(8, SpriteAtlas.COLUMNS);
        assertEquals(11, SpriteAtlas.ROWS);
        assertEquals(11, SpriteAtlas.rowCount());
        assertEquals(192, SpriteAtlas.CELL_WIDTH);
        assertEquals(208, SpriteAtlas.CELL_HEIGHT);
        assertEquals(SpriteAtlas.WIDTH, SpriteAtlas.COLUMNS * SpriteAtlas.CELL_WIDTH);
        assertEquals(SpriteAtlas.HEIGHT, SpriteAtlas.ROWS * SpriteAtlas.CELL_HEIGHT);
    }

    @Test public void acceptsExactDimensions() {
        SpriteAtlas.validateDimensions(1536, 1872);
        SpriteAtlas.validateDimensions(1536, 2288);
    }

    @Test public void rejectsWrongOrInvalidDimensions() {
        int[][] invalid = {{0, 0}, {-1536, 2288}, {1536, -2288}, {1535, 2288},
                {1537, 2288}, {1536, 2287}, {1536, 2289}, {768, 1144}, {2288, 1536},
                {1535, 1872}, {1537, 1872}, {1536, 1871}, {1536, 1873}, {768, 936},
                {1872, 1536}, {1536, 2080}, {1536, 1664}, {1536, Integer.MAX_VALUE}};
        for (int[] size : invalid) {
            assertThrows(IllegalArgumentException.class,
                    () -> SpriteAtlas.validateDimensions(size[0], size[1]));
            assertThrows(IllegalArgumentException.class,
                    () -> SpriteAtlas.forDimensions(size[0], size[1]));
        }
    }

    @Test public void everyOneOf73FramesHasExactUniqueInBoundsRectangle() {
        int[] counts = {6, 8, 8, 4, 5, 8, 6, 6, 6, 8, 8};
        Set<String> cells = new HashSet<>();
        int total = 0;
        for (int row = 0; row < counts.length; row++) {
            assertEquals(counts[row], SpriteAtlas.frameCount(row));
            for (int frame = 0; frame < counts[row]; frame++) {
                int[] rect = SpriteAtlas.frameRect(row, frame);
                assertArrayEquals(new int[] {frame * 192, row * 208,
                        (frame + 1) * 192, (row + 1) * 208}, rect);
                assertTrue(rect[0] >= 0 && rect[1] >= 0);
                assertTrue(rect[2] <= 1536 && rect[3] <= 2288);
                assertEquals(192, rect[2] - rect[0]);
                assertEquals(208, rect[3] - rect[1]);
                assertTrue(cells.add(rect[0] + ":" + rect[1]));
                total++;
            }
        }
        assertEquals(73, total);
        assertEquals(SpriteAtlas.TOTAL_FRAMES, total);
        assertEquals(73, cells.size());
    }

    @Test public void allRowsRejectNegativeFramesAndPaddingCells() {
        for (int row = 0; row < SpriteAtlas.ROWS; row++) {
            final int r = row;
            assertThrows(IndexOutOfBoundsException.class, () -> SpriteAtlas.frameRect(r, -1));
            for (int frame = SpriteAtlas.frameCount(row); frame <= SpriteAtlas.COLUMNS; frame++) {
                final int f = frame;
                assertThrows(IndexOutOfBoundsException.class, () -> SpriteAtlas.frameRect(r, f));
            }
            assertThrows(IndexOutOfBoundsException.class,
                    () -> SpriteAtlas.frameRect(r, Integer.MAX_VALUE));
        }
    }

    @Test public void allRowAccessorsRejectInvalidIndices() {
        int[] invalid = {-1, Integer.MIN_VALUE, 11, Integer.MAX_VALUE};
        for (int row : invalid) {
            assertThrows(IndexOutOfBoundsException.class, () -> SpriteAtlas.frameCount(row));
            assertThrows(IndexOutOfBoundsException.class, () -> SpriteAtlas.animationName(row));
            assertThrows(IndexOutOfBoundsException.class, () -> SpriteAtlas.frameRect(row, 0));
        }
    }

    @Test public void rowNamesMatchDonnaManifestAndDirectionalRows() {
        String[] expected = {"idle", "running-right", "running-left", "waving", "jumping",
                "failed", "waiting", "running", "review", "look-upper", "look-lower"};
        assertArrayEquals(expected, SpriteAtlas.animationNames());
        for (int row = 0; row < expected.length; row++) {
            assertEquals(expected[row], SpriteAtlas.animationName(row));
        }
    }

    @Test public void returnedArraysCannotMutateAtlas() {
        String[] names = SpriteAtlas.animationNames();
        names[0] = "changed";
        assertEquals("idle", SpriteAtlas.animationName(0));
        int[] rect = SpriteAtlas.frameRect(0, 0);
        rect[0] = 1000;
        assertArrayEquals(new int[] {0, 0, 192, 208}, SpriteAtlas.frameRect(0, 0));
    }

    @Test public void selectsExactV1AndV2Metadata() {
        SpriteAtlas.Layout v1 = SpriteAtlas.forDimensions(1536, 1872);
        SpriteAtlas.Layout v2 = SpriteAtlas.forDimensions(1536, 2288);
        assertEquals(1536, v1.width());
        assertEquals(1872, v1.height());
        assertEquals(9, v1.rowCount());
        assertEquals(57, v1.totalFrames());
        assertEquals(1536, v2.width());
        assertEquals(2288, v2.height());
        assertEquals(11, v2.rowCount());
        assertEquals(73, v2.totalFrames());
        assertNotSame(v1, v2);
        assertSame(v1, SpriteAtlas.forDimensions(1536, 1872));
        assertSame(v2, SpriteAtlas.forDimensions(1536, 2288));
    }

    @Test public void everyImageSpecificFrameHasExactUniqueInBoundsRectangle() {
        int[] counts = {6, 8, 8, 4, 5, 8, 6, 6, 6, 8, 8};
        for (int height : new int[] {1872, 2288}) {
            SpriteAtlas.Layout layout = SpriteAtlas.forDimensions(1536, height);
            Set<String> cells = new HashSet<>();
            int total = 0;
            for (int row = 0; row < layout.rowCount(); row++) {
                assertEquals(counts[row], layout.frameCount(row));
                for (int frame = 0; frame < counts[row]; frame++) {
                    int[] rect = layout.frameRect(row, frame);
                    assertArrayEquals(new int[] {frame * 192, row * 208,
                            (frame + 1) * 192, (row + 1) * 208}, rect);
                    assertTrue(rect[0] >= 0 && rect[1] >= 0);
                    assertTrue(rect[2] <= layout.width() && rect[3] <= layout.height());
                    assertTrue(cells.add(rect[0] + ":" + rect[1]));
                    total++;
                }
            }
            assertEquals(height == 1872 ? 57 : 73, total);
            assertEquals(layout.totalFrames(), total);
            assertEquals(total, cells.size());
        }
    }

    @Test public void imageSpecificRowsRejectNegativeAndUnusedFrames() {
        for (int height : new int[] {1872, 2288}) {
            SpriteAtlas.Layout layout = SpriteAtlas.forDimensions(1536, height);
            for (int row = 0; row < layout.rowCount(); row++) {
                final int r = row;
                assertThrows(IndexOutOfBoundsException.class, () -> layout.frameRect(r, -1));
                for (int frame = layout.frameCount(row); frame <= SpriteAtlas.COLUMNS; frame++) {
                    final int f = frame;
                    assertThrows(IndexOutOfBoundsException.class, () -> layout.frameRect(r, f));
                }
                assertThrows(IndexOutOfBoundsException.class,
                        () -> layout.frameRect(r, Integer.MAX_VALUE));
            }
        }
    }

    @Test public void imageSpecificAccessorsRejectAbsentRows() {
        for (int height : new int[] {1872, 2288}) {
            SpriteAtlas.Layout layout = SpriteAtlas.forDimensions(1536, height);
            int[] invalid = {-1, Integer.MIN_VALUE, layout.rowCount(),
                    layout.rowCount() + 1, Integer.MAX_VALUE};
            for (int row : invalid) {
                assertThrows(IndexOutOfBoundsException.class, () -> layout.frameCount(row));
                assertThrows(IndexOutOfBoundsException.class, () -> layout.animationName(row));
                assertThrows(IndexOutOfBoundsException.class, () -> layout.frameRect(row, 0));
            }
        }
    }

    @Test public void v1AndV2KeepIdenticalSharedPosesWithoutInventingRows() {
        SpriteAtlas.Layout v1 = SpriteAtlas.forDimensions(1536, 1872);
        SpriteAtlas.Layout v2 = SpriteAtlas.forDimensions(1536, 2288);
        String[] names = {"idle", "running-right", "running-left", "waving", "jumping",
                "failed", "waiting", "running", "review"};
        assertArrayEquals(names, v1.animationNames());
        assertArrayEquals(SpriteAtlas.animationNames(), v2.animationNames());
        for (int row = 0; row < v1.rowCount(); row++) {
            assertEquals(v1.animationName(row), v2.animationName(row));
            assertEquals(v1.frameCount(row), v2.frameCount(row));
            for (int frame = 0; frame < v1.frameCount(row); frame++) {
                assertArrayEquals(v1.frameRect(row, frame), v2.frameRect(row, frame));
                assertArrayEquals(SpriteAtlas.frameRect(row, frame), v2.frameRect(row, frame));
            }
        }
        assertThrows(IndexOutOfBoundsException.class, () -> v1.frameRect(9, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> v1.frameRect(10, 0));
        assertArrayEquals(new int[] {0, 1872, 192, 2080}, v2.frameRect(9, 0));
        assertArrayEquals(new int[] {1344, 2080, 1536, 2288}, v2.frameRect(10, 7));
    }

    @Test public void motionPosesStayValidAndIdenticalForBothFormats() {
        SpriteAtlas.Layout v1 = SpriteAtlas.forDimensions(1536, 1872);
        SpriteAtlas.Layout v2 = SpriteAtlas.forDimensions(1536, 2288);
        PetMotion motion = new PetMotion();
        assertSamePose(v1, v2, motion.pose(0));
        motion.down(0);
        for (long now = 0; now <= 500; now += 20) assertSamePose(v1, v2, motion.pose(now));
        long now = 1_000;
        for (int[] movement : new int[][] {{8, 0}, {-8, 0}, {0, 8}, {0, -8}}) {
            for (int i = 0; i < 100; i++, now += 40) {
                motion.moved(movement[0], movement[1], now);
                assertSamePose(v1, v2, motion.pose(now));
            }
        }
        assertSamePose(v1, v2, motion.pose(now + 500));
        motion.release();
        assertSamePose(v1, v2, motion.pose(now));
        motion.down(now);
        motion.cancel();
        assertSamePose(v1, v2, motion.pose(now));
    }

    @Test public void imageSpecificArraysCannotMutateEitherLayout() {
        for (int height : new int[] {1872, 2288}) {
            SpriteAtlas.Layout layout = SpriteAtlas.forDimensions(1536, height);
            String[] names = layout.animationNames();
            names[0] = "changed";
            assertEquals("idle", layout.animationName(0));
            int[] rect = layout.frameRect(0, 0);
            rect[0] = 1000;
            assertArrayEquals(new int[] {0, 0, 192, 208}, layout.frameRect(0, 0));
        }
    }

    private static void assertSamePose(SpriteAtlas.Layout v1, SpriteAtlas.Layout v2, int[] pose) {
        assertArrayEquals(v1.frameRect(pose[0], pose[1]), v2.frameRect(pose[0], pose[1]));
    }
}
