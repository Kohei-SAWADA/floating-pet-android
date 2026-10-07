package com.dot.floatingpet.core;

/** Android-independent geometry and touch-slop rules for the floating pet. */
public final class OverlayGeometry {
    private OverlayGeometry() { }

    /** Converts a requested width in dp into pixels, preserving the atlas-cell aspect. */
    public static int[] sizeForWidthDp(float widthDp, float density) {
        requirePositiveFinite(widthDp, "widthDp");
        requirePositiveFinite(density, "density");
        double width = (double) widthDp * density;
        if (width > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Pixel width is too large");
        }
        int widthPx = Math.max(1, (int) Math.round(width));
        double height = (double) widthPx * SpriteAtlas.CELL_HEIGHT / SpriteAtlas.CELL_WIDTH;
        if (height > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Pixel height is too large");
        }
        return new int[] {widthPx, Math.max(1, (int) Math.round(height))};
    }

    /**
     * Shrinks both dimensions uniformly to fit; never enlarges the requested size.
     * Results are rounded to whole pixels. An empty size/screen yields {0, 0}.
     */
    public static int[] clampSize(int width, int height, int screenWidth, int screenHeight) {
        validateDimensions(width, height, screenWidth, screenHeight);
        if (width == 0 || height == 0 || screenWidth == 0 || screenHeight == 0) {
            return new int[] {0, 0};
        }
        double scale = Math.min(1.0, Math.min((double) screenWidth / width,
                (double) screenHeight / height));
        return new int[] {
                Math.min(screenWidth, Math.max(1, (int) Math.round(width * scale))),
                Math.min(screenHeight, Math.max(1, (int) Math.round(height * scale)))
        };
    }

    /** Oversized overlays are anchored at zero rather than a negative coordinate. */
    public static int[] clampPosition(int x, int y, int width, int height,
                                      int screenWidth, int screenHeight) {
        validateDimensions(width, height, screenWidth, screenHeight);
        return new int[] {
                clamp(x, 0, Math.max(0, screenWidth - width)),
                clamp(y, 0, Math.max(0, screenHeight - height))
        };
    }

    /** Fits the size first, then the position; returns {x, y, width, height}. */
    public static int[] clampBounds(int x, int y, int width, int height,
                                    int screenWidth, int screenHeight) {
        int[] size = clampSize(width, height, screenWidth, screenHeight);
        int[] position = clampPosition(x, y, size[0], size[1], screenWidth, screenHeight);
        return new int[] {position[0], position[1], size[0], size[1]};
    }

    public static int clamp(int value, int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min must not exceed max");
        }
        return Math.max(min, Math.min(max, value));
    }

    /** Uses distance from the original down point, not the distance of the last move. */
    public static boolean isDrag(float startX, float startY, float x, float y, float slop) {
        requireFinite(startX, "startX");
        requireFinite(startY, "startY");
        requireFinite(x, "x");
        requireFinite(y, "y");
        requireNonNegativeFinite(slop, "slop");
        double dx = (double) x - startX;
        double dy = (double) y - startY;
        return dx * dx + dy * dy > (double) slop * slop;
    }

    /** A drag remains a drag if the pointer later moves back to its initial position. */
    public static final class DragTracker {
        private final float slop;
        private float startX;
        private float startY;
        private boolean active;
        private boolean dragging;

        public DragTracker(float slop) {
            requireNonNegativeFinite(slop, "slop");
            this.slop = slop;
        }

        public void down(float x, float y) {
            requireFinite(x, "x");
            requireFinite(y, "y");
            startX = x;
            startY = y;
            active = true;
            dragging = false;
        }

        /** Returns whether this active gesture has crossed the touch slop. */
        public boolean move(float x, float y) {
            requireFinite(x, "x");
            requireFinite(y, "y");
            if (active) {
                dragging |= isDrag(startX, startY, x, y, slop);
            }
            return dragging;
        }

        /** Processes the final coordinates, ends the gesture and returns whether it was a tap. */
        public boolean up(float x, float y) {
            move(x, y);
            boolean tap = active && !dragging;
            cancel();
            return tap;
        }

        public boolean isDragging() {
            return dragging;
        }

        public boolean isActive() {
            return active;
        }

        /** Cancellation never emits a tap; a subsequent down begins a fresh gesture. */
        public void cancel() {
            active = false;
            dragging = false;
        }
    }

    private static void validateDimensions(int width, int height, int screenWidth, int screenHeight) {
        if (width < 0 || height < 0 || screenWidth < 0 || screenHeight < 0) {
            throw new IllegalArgumentException("Dimensions must be nonnegative");
        }
    }

    private static void requireFinite(float value, String name) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }

    private static void requirePositiveFinite(float value, String name) {
        requireFinite(value, name);
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }

    private static void requireNonNegativeFinite(float value, String name) {
        requireFinite(value, name);
        if (value < 0) {
            throw new IllegalArgumentException(name + " must be nonnegative");
        }
    }
}
