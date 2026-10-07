package com.dot.floatingpet.core;

/** Exact v1/v2 pet layouts. Coordinates use exclusive right/bottom edges. */
public final class SpriteAtlas {
    // The original static API retains its v2 defaults for shared animation code.
    public static final int WIDTH = 1536;
    public static final int HEIGHT = 2288;
    public static final int COLUMNS = 8;
    public static final int ROWS = 11;
    public static final int CELL_WIDTH = 192;
    public static final int CELL_HEIGHT = 208;
    public static final int TOTAL_FRAMES = 73;

    private static final int[] FRAME_COUNTS = {6, 8, 8, 4, 5, 8, 6, 6, 6, 8, 8};
    private static final String[] ANIMATION_NAMES = {
            "idle", "running-right", "running-left", "waving", "jumping",
            "failed", "waiting", "running", "review", "look-upper", "look-lower"
    };
    private static final Layout V1 = new Layout(1872, 9, 57);
    private static final Layout V2 = new Layout(HEIGHT, ROWS, TOTAL_FRAMES);

    private SpriteAtlas() { }

    /** Accepts only the two exact contracts, never scaled or guessed atlas layouts. */
    public static void validateDimensions(int width, int height) {
        forDimensions(width, height);
    }

    /** Select this layout from the decoded bitmap before requesting image crops. */
    public static Layout forDimensions(int width, int height) {
        if (width == WIDTH && height == V1.height()) return V1;
        if (width == WIDTH && height == V2.height()) return V2;
        throw new IllegalArgumentException("Expected a 1536x1872 v1 or 1536x2288 v2 atlas, got "
                + width + "x" + height);
    }

    public static int rowCount() {
        return V2.rowCount();
    }

    public static int frameCount(int row) {
        return V2.frameCount(row);
    }

    /** Rows 9 and 10 are directional looks, not looping movement animations. */
    public static String animationName(int row) {
        return V2.animationName(row);
    }

    /** Returns a fresh array so callers cannot mutate the atlas definition. */
    public static String[] animationNames() {
        return V2.animationNames();
    }

    /** Returns {left, top, right, bottom}; unused padding cells are not valid frames. */
    public static int[] frameRect(int row, int frame) {
        return V2.frameRect(row, frame);
    }

    /** Immutable image-specific layout; v1 cannot access the absent direction rows. */
    public static final class Layout {
        private final int height;
        private final int rows;
        private final int totalFrames;

        private Layout(int height, int rows, int totalFrames) {
            this.height = height;
            this.rows = rows;
            this.totalFrames = totalFrames;
        }

        public int width() { return WIDTH; }
        public int height() { return height; }
        public int rowCount() { return rows; }
        public int totalFrames() { return totalFrames; }

        public int frameCount(int row) {
            validateRow(row);
            return FRAME_COUNTS[row];
        }

        public String animationName(int row) {
            validateRow(row);
            return ANIMATION_NAMES[row];
        }

        public String[] animationNames() {
            return java.util.Arrays.copyOf(ANIMATION_NAMES, rows);
        }

        public int[] frameRect(int row, int frame) {
            validateRow(row);
            if (frame < 0 || frame >= FRAME_COUNTS[row]) {
                throw new IndexOutOfBoundsException("Frame " + frame + " is invalid for row " + row);
            }
            int left = frame * CELL_WIDTH;
            int top = row * CELL_HEIGHT;
            return new int[] {left, top, left + CELL_WIDTH, top + CELL_HEIGHT};
        }

        private void validateRow(int row) {
            if (row < 0 || row >= rows) {
                throw new IndexOutOfBoundsException("Invalid atlas row: " + row);
            }
        }
    }
}
