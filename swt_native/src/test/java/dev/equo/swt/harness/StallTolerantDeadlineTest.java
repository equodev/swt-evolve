package dev.equo.swt.harness;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class StallTolerantDeadlineTest {

    private final AtomicLong now = new AtomicLong(1_000);

    private StallTolerantDeadline deadline(long budgetMs, long maxCreditMs) {
        return new StallTolerantDeadline(budgetMs, maxCreditMs, 500, now::get);
    }

    /** Advance the clock as a healthy spin iteration would: a few ms per turn. */
    private void tick(long ms) {
        now.addAndGet(ms);
    }

    @Test
    void normalWaitExpiresOnItsBudget() {
        StallTolerantDeadline d = deadline(1_000, 1_000);
        for (int i = 0; i < 500; i++) {
            tick(5);
            if (!d.hasTimeLeft()) break;
        }
        assertThat(d.hasTimeLeft()).isFalse();
        assertThat(d.stalledMs()).isZero();
    }

    @Test
    void aFrozenIterationIsGivenBackInsteadOfSpent() {
        StallTolerantDeadline d = deadline(1_000, 10_000);
        tick(5);
        assertThat(d.hasTimeLeft()).isTrue();

        tick(30_000); // the process was not scheduled for 30s

        assertThat(d.hasTimeLeft()).as("the stall must not consume the budget").isTrue();
        assertThat(d.stalledMs()).isEqualTo(30_000);
        // The full budget survives the freeze: still ~995ms of running time to deliver a response.
        tick(900);
        assertThat(d.hasTimeLeft()).isTrue();
    }

    @Test
    void aStallIsCreditedInFullEvenWhenItDwarfsTheBudget() {
        StallTolerantDeadline d = deadline(1_000, 1_000);
        tick(30_000);
        assertThat(d.hasTimeLeft()).as("a partly-credited freeze still fails the operation").isTrue();
        assertThat(d.stalledMs()).isEqualTo(30_000);
    }

    @Test
    void stallCreditIsCappedSoAUniformlySlowMachineStillTerminates() {
        StallTolerantDeadline d = deadline(1_000, 2_000);
        int turns = 0;
        for (; turns < 1_000 && d.hasTimeLeft(); turns++) tick(600); // every iteration looks stalled
        assertThat(turns).as("the wait must end rather than extend forever").isLessThan(1_000);
        assertThat(d.stalledMs()).isGreaterThanOrEqualTo(2_000);
    }

    @Test
    void anIterationBelowTheThresholdIsOrdinaryWaiting() {
        StallTolerantDeadline d = deadline(1_000, 1_000);
        tick(499);
        assertThat(d.hasTimeLeft()).isTrue();
        assertThat(d.stalledMs()).as("499ms is slow, not frozen").isZero();
    }

    @Test
    void stallSuffixNamesTheStallOnlyWhenThereWasOne() {
        StallTolerantDeadline clean = deadline(1_000, 1_000);
        tick(5);
        clean.hasTimeLeft();
        assertThat(clean.stallSuffix()).isEmpty();

        StallTolerantDeadline stalled = deadline(1_000, 10_000);
        tick(20_000);
        stalled.hasTimeLeft();
        assertThat(stalled.stallSuffix()).contains("20.0s").contains("process-wide stall");
    }
}
