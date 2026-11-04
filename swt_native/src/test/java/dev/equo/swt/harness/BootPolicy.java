package dev.equo.swt.harness;

import java.util.function.LongFunction;
import java.util.function.LongSupplier;

/**
 * When a headless browser that has not reported ready yet is replaced. A browser that wedges during
 * startup never asks the server for the app, and a fresh one clears it; one that is slow, because the
 * machine is busy, is still fetching and compiling the app, and replacing it restarts that work from
 * nothing. So an attempt only ends in a relaunch when its browser has asked for nothing; one that is
 * loading keeps the rest of the boot's budget, which stays {@code attempts * attemptMs} either way.
 * An attempt is timed like the budget: time the whole process spent frozen is not the browser's.
 */
final class BootPolicy {

    enum Verdict { WAIT, RELAUNCH, GIVE_UP }

    private final int attempts;
    private final long attemptMs;
    private final LongSupplier requestsServed;
    private final LongFunction<StallTolerantDeadline> deadlines;
    private final StallTolerantDeadline total;
    private StallTolerantDeadline attempt;
    private int launches;
    private long servedAtLaunch;

    /**
     * For a wait loop whose iterations are a few milliseconds, so a longer one is a process-wide stall
     * and is credited back. Create it right before the first launch: the budget starts here.
     */
    static BootPolicy stallTolerant(int attempts, long attemptMs, LongSupplier requestsServed) {
        return new BootPolicy(attempts, attemptMs, requestsServed, StallTolerantDeadline::new);
    }

    /**
     * For a wait loop whose iterations each wait up to {@code pollMs}: only an iteration longer than
     * that and the stall margin is a process-wide stall.
     */
    static BootPolicy polling(int attempts, long attemptMs, long pollMs, LongSupplier requestsServed) {
        return new BootPolicy(attempts, attemptMs, requestsServed, ms -> new StallTolerantDeadline(ms, ms,
                pollMs + StallTolerantDeadline.DEFAULT_STALL_MS, System::currentTimeMillis));
    }

    /** {@code deadlines} makes the boot's budget and each attempt's, from their length. */
    BootPolicy(int attempts, long attemptMs, LongSupplier requestsServed,
            LongFunction<StallTolerantDeadline> deadlines) {
        this.attempts = attempts;
        this.attemptMs = attemptMs;
        this.requestsServed = requestsServed;
        this.deadlines = deadlines;
        this.total = deadlines.apply(attempts * attemptMs);
    }

    /** Records that a browser was just (re)launched. */
    void launched() {
        launches++;
        attempt = deadlines.apply(attemptMs);
        servedAtLaunch = requestsServed.getAsLong();
    }

    /** Call once per wait-loop iteration, while the client is not ready. */
    Verdict check() {
        boolean budgetLeft = total.hasTimeLeft();
        boolean attemptLeft = attempt.hasTimeLeft();
        if (!budgetLeft) return Verdict.GIVE_UP;
        if (attemptLeft) return Verdict.WAIT;
        if (requestsServed.getAsLong() > servedAtLaunch) return Verdict.WAIT;
        return launches < attempts ? Verdict.RELAUNCH : Verdict.GIVE_UP;
    }

    /** Failure-message fragment: what the boot spent, and on how many browsers. */
    String describe() {
        return launches + " browser launch(es) within " + attempts * attemptMs + "ms"
                + total.stallSuffix();
    }
}
