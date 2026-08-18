package dev.equo.swt.harness;

import java.util.Locale;
import java.util.function.LongSupplier;

/**
 * A wait budget that does not charge the operation for time the whole process spent frozen.
 *
 * <p>A harness wait is a spin loop whose idle branch is a short sleep, so a healthy iteration costs
 * a known handful of milliseconds. An iteration orders of magnitude longer measured nothing — the
 * JVM, or the container under it, was not scheduled — and counting it against the wait is what
 * turns an environment stall into a test failure: on resume the waiting thread can reach its
 * deadline check before the comm thread has delivered a response that already arrived.
 *
 * <p>Such an iteration is credited back in full; crediting it partway would still charge the
 * remainder to the operation. {@code maxCreditMs} bounds the total credited, so a uniformly slow
 * machine still terminates. Callers should append {@link #stallSuffix()} to their failure message:
 * a wait that ran out with no stall credited is a genuine timeout.
 */
public final class StallTolerantDeadline {

    /** An iteration longer than this measured nothing — the process was not running. */
    private static final long DEFAULT_STALL_MS = Long.getLong("harness.stallThresholdMs", 500);

    private final long maxCreditMs;
    private final long stallMs;
    private final LongSupplier clock;
    private long deadline;
    private long lastTick;
    private long stalledMs;

    /** A budget of {@code budgetMs} of running time, tolerating up to as much again in stalls. */
    public StallTolerantDeadline(long budgetMs) {
        this(budgetMs, budgetMs, DEFAULT_STALL_MS, System::currentTimeMillis);
    }

    StallTolerantDeadline(long budgetMs, long maxCreditMs, long stallMs, LongSupplier clock) {
        this.maxCreditMs = maxCreditMs;
        this.stallMs = stallMs;
        this.clock = clock;
        this.lastTick = clock.getAsLong();
        this.deadline = lastTick + budgetMs;
    }

    /**
     * Whether budget remains. Call exactly once per spin-loop iteration — the gap between two calls
     * is what identifies a stall, so calling it twice in one iteration hides one.
     */
    public boolean hasTimeLeft() {
        long now = clock.getAsLong();
        long sinceLastTick = now - lastTick;
        if (sinceLastTick >= stallMs && stalledMs < maxCreditMs) {
            deadline += sinceLastTick;
            stalledMs += sinceLastTick;
        }
        lastTick = now;
        return now < deadline;
    }

    /** Milliseconds of this wait the process spent frozen rather than waiting. */
    public long stalledMs() {
        return stalledMs;
    }

    /** Failure-message fragment naming the stall, or the empty string when there was none. */
    public String stallSuffix() {
        return stalledMs == 0 ? ""
                : String.format(Locale.ROOT,
                        " (%.1fs of the wait was a process-wide stall, not the operation)",
                        stalledMs / 1000.0);
    }
}
