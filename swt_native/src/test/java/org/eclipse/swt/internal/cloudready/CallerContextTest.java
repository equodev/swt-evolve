package org.eclipse.swt.internal.cloudready;

import dev.equo.swt.Java8;
import dev.equo.swt.harness.CarriedContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A task handed to a thread the caller does not own runs under the caller's context; with no
 * capturer, or no context on the calling thread, it is handed over as it is.
 */
class CallerContextTest {

    private final ExecutorService pool = Executors.newSingleThreadExecutor();

    @AfterEach
    void clear() {
        CarriedContext.uninstall();
        pool.shutdownNow();
    }

    private String contextSeenBy(java.util.function.Consumer<Runnable> handOver) throws Exception {
        CompletableFuture<String> seen = new CompletableFuture<>();
        handOver.accept(() -> seen.complete(String.valueOf(CarriedContext.get())));
        return seen.get(10, TimeUnit.SECONDS);
    }

    @Test
    void aWrappedTaskRunsUnderTheContextOfTheThreadThatWrappedIt() throws Exception {
        CarriedContext.install();
        CarriedContext.set("caller");

        assertThat(contextSeenBy(task -> pool.execute(CallerContext.wrap(task)))).isEqualTo("caller");
    }

    @Test
    void aBoundExecutorRunsEachTaskUnderTheContextOfTheThreadThatHandsItOver() throws Exception {
        CarriedContext.install();
        java.util.concurrent.Executor bound = CallerContext.bind(pool);

        CarriedContext.set("first");
        assertThat(contextSeenBy(bound::execute)).isEqualTo("first");
        CarriedContext.set("second");
        assertThat(contextSeenBy(bound::execute)).isEqualTo("second");
    }

    @Test
    void withNoCapturerATaskIsHandedOverAsItIs() {
        CarriedContext.set("caller");
        Runnable task = () -> {};

        assertThat(CallerContext.wrap(task)).isSameAs(task);
    }

    @Test
    void onAThreadWithNoContextATaskIsHandedOverAsItIs() {
        CarriedContext.install();
        Runnable task = () -> {};

        assertThat(CallerContext.wrap(task)).isSameAs(task);
    }

    @Test
    void whatIsChainedOnAFutureThatTimesOutRunsUnderTheContextOfTheThreadThatSetTheTimeout() throws Exception {
        CarriedContext.install();
        CarriedContext.set("caller");
        AtomicReference<String> seen = new AtomicReference<>();
        CompletableFuture<Void> done = new CompletableFuture<>();

        Java8.orTimeout(new CompletableFuture<Void>(), 10, TimeUnit.MILLISECONDS)
                .whenComplete((ignored, error) -> {
                    seen.set(CarriedContext.get());
                    done.complete(null);
                });

        done.get(10, TimeUnit.SECONDS);
        assertThat(seen.get()).isEqualTo("caller");
    }
}
