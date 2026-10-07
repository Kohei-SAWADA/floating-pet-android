import com.dot.floatingpet.core.PollPolicy;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.Locale;

/** Standalone synthetic model, not an Android, Binder, battery or UI benchmark. */
public final class PollPolicyBenchmark {
    private static final long QUERY_COST_MS = 5;
    private static final int PHASE_SAMPLES = 100000;
    private static final int ITERATIONS = 5000000;
    private static volatile long sink;

    public static void main(String[] args) {
        Locale.setDefault(Locale.ROOT);
        System.out.println("SYNTHETIC POLLING MODEL AND PURE-JAVA POLICY MICROBENCHMARK");
        System.out.println("No Android device, UsageStats/Binder calls, UI, real query latency or battery measured.");
        System.out.println("Timing model: constant simulated query duration = 5 ms; uniformly distributed transition phases.");
        System.out.println("Wait is until the next query START; result delivery would add query execution and OS/event delays.");
        System.out.println("100000 deterministic midpoint phases; simulated 60 s counts include the query at t=0, exclude t=60000.");
        System.out.println();
        model("old fixed post-query 500 ms", 500 + QUERY_COST_MS);
        model("new fast", QUERY_COST_MS + new PollPolicy().nextDelayMillis(true, QUERY_COST_MS, true, true));
        model("new economy", QUERY_COST_MS + new PollPolicy().nextDelayMillis(false, QUERY_COST_MS, true, true));
        System.out.println();
        PollPolicy policy = new PollPolicy();
        System.out.printf("Slow/failure example delays (ms): %d, %d; timely success recovers to %d.%n",
                policy.nextDelayMillis(true, 5, false, true),
                policy.nextDelayMillis(true, 200, true, true),
                policy.nextDelayMillis(true, 5, true, true));
        System.out.println("Screen-off no-reschedule and single-flight are Android caller responsibilities, not measured here.");
        System.out.println();
        cpuBenchmark();
    }

    private static void model(String label, long interval) {
        double[] waits = new double[PHASE_SAMPLES];
        double sum = 0;
        for (int i = 0; i < PHASE_SAMPLES; i++) {
            double phase = interval * (i + 0.5) / PHASE_SAMPLES;
            waits[i] = interval - phase;
            sum += waits[i];
        }
        Arrays.sort(waits);
        int queries = 0;
        for (long start = 0; start < 60000; start += interval) queries++;
        System.out.printf("%s: interval=%d ms; wait mean=%.3f ms, p95=%.3f ms, sampled max=%.3f ms (upper bound %d ms); rate=%.4f queries/s; 60 s queries=%d%n",
                label, interval, sum / PHASE_SAMPLES, waits[(int) Math.ceil(PHASE_SAMPLES * .95) - 1],
                waits[PHASE_SAMPLES - 1], interval, 1000.0 / interval, queries);
    }

    private static long runPolicyLoop(int iterations) {
        PollPolicy policy = new PollPolicy();
        long checksum = 0;
        for (int i = 0; i < iterations; i++) {
            // Exercise both modes, slow queries, backoff, recovery and ineligibility.
            checksum += policy.nextDelayMillis((i & 1) == 0, i % 251, i % 31 != 0, i % 97 != 0);
        }
        return checksum;
    }

    private static void cpuBenchmark() {
        for (int i = 0; i < 3; i++) sink = runPolicyLoop(ITERATIONS);
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        boolean cpuAvailable = bean.isCurrentThreadCpuTimeSupported() && bean.isThreadCpuTimeEnabled();
        long cpuStart = cpuAvailable ? bean.getCurrentThreadCpuTime() : 0;
        long wallStart = System.nanoTime();
        sink = runPolicyLoop(ITERATIONS);
        long wallNanos = System.nanoTime() - wallStart;
        long cpuNanos = cpuAvailable ? bean.getCurrentThreadCpuTime() - cpuStart : 0;
        System.out.printf("Host Java: %s; OS: %s %s%n", System.getProperty("java.version"), System.getProperty("os.name"), System.getProperty("os.arch"));
        System.out.printf("Pure policy loop: %,d calls; System.nanoTime wall=%.3f ms (%.2f ns/call); checksum=%d%n",
                ITERATIONS, wallNanos / 1000000.0, wallNanos / (double) ITERATIONS, sink);
        if (cpuAvailable) {
            System.out.printf("Calling-thread CPU=%.3f ms (%.2f ns/call); simulated query cost is NOT CPU work.%n",
                    cpuNanos / 1000000.0, cpuNanos / (double) ITERATIONS);
        } else {
            System.out.println("Calling-thread CPU counter unavailable; wall time is not CPU time.");
        }
        System.out.println("Microbenchmark includes loop/input arithmetic and is JIT/host-sensitive; not JMH, device performance or power evidence.");
        System.out.println("Fast mode requests about 2.5x economy's normal query rate; real device energy cost remains unmeasured.");
    }
}
