import com.dot.floatingpet.core.*;
import java.util.Arrays;

/** Small dependency-free sanity check. The full JUnit suite remains authoritative. */
public final class CoreSmoke {
    private static int checks;
    private static void check(boolean ok, String message) {
        checks++;
        if (!ok) throw new AssertionError(message);
    }
    private static void rejected(Runnable action, String message) {
        boolean thrown = false;
        try { action.run(); } catch (IllegalArgumentException | IndexOutOfBoundsException expected) { thrown = true; }
        check(thrown, message);
    }
    public static void main(String[] args) {
        for (int height : new int[]{1872, 2288}) {
            SpriteAtlas.Layout atlas = SpriteAtlas.forDimensions(1536, height);
            int total = 0;
            for (int row = 0; row < atlas.rowCount(); row++) {
                for (int frame = 0; frame < atlas.frameCount(row); frame++) {
                    int[] rect = atlas.frameRect(row, frame);
                    check(rect[0] >= 0 && rect[1] >= 0 && rect[2] <= 1536 && rect[3] <= height, "Frame out of bounds");
                    check(rect[2] - rect[0] == 192 && rect[3] - rect[1] == 208, "Wrong frame dimensions");
                    total++;
                }
                final int r = row;
                rejected(() -> atlas.frameRect(r, atlas.frameCount(r)), "Padding accepted");
            }
            check(total == atlas.totalFrames(), "Frame total mismatch");
        }
        rejected(() -> SpriteAtlas.forDimensions(1136, 1385), "Unnormalized generated sheet accepted");
        PollPolicy policy = new PollPolicy();
        check(policy.nextDelayMillis(true, 5, true, true) == 195, "Fast cadence");
        check(policy.nextDelayMillis(false, 5, true, true) == 495, "Economy cadence");
        check(policy.nextDelayMillis(true, 5, false, true) == 995, "First failure backoff");
        check(policy.nextDelayMillis(true, 5, false, true) == 1995, "Second failure backoff");
        check(policy.nextDelayMillis(true, 5, true, true) == 195, "Backoff recovery");
        check(policy.nextDelayMillis(true, 0, true, false) == 1000, "Ineligible cadence");
        PetMotion motion = new PetMotion();
        check(Arrays.equals(motion.pose(0), new int[]{0, 0}), "Initial idle");
        motion.down(1000);
        check(motion.pose(1000)[0] == 3, "Touch response");
        check(!motion.isActive(1400), "Bounded touch response");
        motion.moved(10, 0, 1500);
        check(motion.pose(1500)[0] == 1, "Right drag");
        motion.moved(-10, 0, 1600);
        check(motion.pose(1600)[0] == 2, "Left drag");
        check(!motion.isActive(1720), "Drag pause");
        motion.cancel();
        check(Arrays.equals(motion.pose(1720), new int[]{0, 0}), "Cancelled idle");
        check(Arrays.equals(OverlayGeometry.sizeForWidthDp(192, 1), new int[]{192, 208}), "Aspect ratio");
        check(Arrays.equals(OverlayGeometry.clampBounds(-2, 500, 192, 208, 100, 100), new int[]{0, 0, 92, 100}), "Clamped geometry");
        OverlayGeometry.DragTracker drag = new OverlayGeometry.DragTracker(8);
        drag.down(0, 0); drag.move(20, 0);
        check(!drag.up(0, 0), "Returning to start must not become tap");
        drag.down(0, 0); drag.cancel();
        check(!drag.up(0, 0), "Cancelled gesture must not tap");
        check(UsageQueryWindow.begin(1000, 2000, 2100) == 1000, "Session boundary");
        check(UsageQueryWindow.begin(1000, 2000, 50000) == 20000, "Recovery bound");
        check(!SystemAnimationPolicy.allows(true, 1, true, true), "Battery saver gate");
        check(!SystemAnimationPolicy.allows(true, 0, false, true), "Animation scale gate");
        check(SystemAnimationPolicy.allows(false, 1, false, false), "Legacy cache gate");
        System.out.println("PASS: " + checks + " dependency-free core checks (not the full JUnit suite)");
    }
}
