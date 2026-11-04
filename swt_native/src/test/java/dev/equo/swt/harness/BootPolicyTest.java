package dev.equo.swt.harness;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class BootPolicyTest {

    private static final int ATTEMPTS = 3;
    private static final long ATTEMPT_MS = 12_000;

    private final AtomicLong now = new AtomicLong(1_000);
    private final AtomicLong served = new AtomicLong();

    private BootPolicy launchedPolicy() {
        BootPolicy boot = new BootPolicy(ATTEMPTS, ATTEMPT_MS, served::get,
                ms -> new StallTolerantDeadline(ms, 0, Long.MAX_VALUE, now::get));
        boot.launched();
        return boot;
    }

    @Test
    void timeTheWholeProcessSpentFrozenIsNotTheBrowsers() {
        BootPolicy boot = new BootPolicy(ATTEMPTS, ATTEMPT_MS, served::get,
                ms -> new StallTolerantDeadline(ms, ms, 500, now::get));
        boot.launched();

        now.addAndGet(10_000); // nothing was scheduled, the browser included
        assertThat(boot.check()).isEqualTo(BootPolicy.Verdict.WAIT);
        for (long ran = 0; ran < ATTEMPT_MS - 10; ran += 10) {
            now.addAndGet(10);
            assertThat(boot.check()).as("after %d ms of running time", ran + 10).isEqualTo(BootPolicy.Verdict.WAIT);
        }
        now.addAndGet(20);
        assertThat(boot.check()).as("a whole attempt of running time, nothing asked for")
                .isEqualTo(BootPolicy.Verdict.RELAUNCH);
    }

    @Test
    void aBrowserThatAsksForNothingIsReplacedAtTheEndOfItsAttempt() {
        BootPolicy boot = launchedPolicy();

        now.addAndGet(ATTEMPT_MS - 1);
        assertThat(boot.check()).isEqualTo(BootPolicy.Verdict.WAIT);
        now.addAndGet(1);
        assertThat(boot.check()).isEqualTo(BootPolicy.Verdict.RELAUNCH);
    }

    @Test
    void aBrowserThatIsLoadingTheAppIsNotReplaced() {
        BootPolicy boot = launchedPolicy();

        served.addAndGet(5); // index.html, main.dart.wasm, fonts...
        now.addAndGet(ATTEMPT_MS);
        assertThat(boot.check()).isEqualTo(BootPolicy.Verdict.WAIT);
        now.addAndGet(ATTEMPT_MS);
        assertThat(boot.check()).isEqualTo(BootPolicy.Verdict.WAIT);
    }

    @Test
    void aLoadingBrowserStillGetsOnlyTheBootsBudget() {
        BootPolicy boot = launchedPolicy();

        served.addAndGet(5);
        now.addAndGet(ATTEMPTS * ATTEMPT_MS - 1);
        assertThat(boot.check()).isEqualTo(BootPolicy.Verdict.WAIT);
        now.addAndGet(1);
        assertThat(boot.check()).isEqualTo(BootPolicy.Verdict.GIVE_UP);
    }

    @Test
    void theReplacementIsJudgedByWhatItAsksForItself() {
        BootPolicy boot = launchedPolicy();
        served.addAndGet(5);
        now.addAndGet(ATTEMPT_MS);
        assertThat(boot.check()).isEqualTo(BootPolicy.Verdict.WAIT);

        boot.launched();
        now.addAndGet(ATTEMPT_MS);
        assertThat(boot.check()).as("the second browser asked for nothing since its own launch")
                .isEqualTo(BootPolicy.Verdict.RELAUNCH);
    }

    @Test
    void theLastBrowserThatAsksForNothingEndsTheBoot() {
        BootPolicy boot = launchedPolicy();
        for (int launch = 1; launch < ATTEMPTS; launch++) {
            now.addAndGet(ATTEMPT_MS);
            assertThat(boot.check()).isEqualTo(BootPolicy.Verdict.RELAUNCH);
            boot.launched();
        }
        now.addAndGet(ATTEMPT_MS - 1);
        assertThat(boot.check()).isEqualTo(BootPolicy.Verdict.WAIT);
        now.addAndGet(1);
        assertThat(boot.check()).isEqualTo(BootPolicy.Verdict.GIVE_UP);
    }
}
