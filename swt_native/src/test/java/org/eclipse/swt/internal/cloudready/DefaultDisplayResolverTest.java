package org.eclipse.swt.internal.cloudready;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure-Java tests for the Display resolver hook. Verifies the
 * 'no resolver installed = no behavior change' contract that desktop SWT
 * usage relies on, and that an installed resolver routes through correctly.
 *
 * <p>The integration between this resolver and {@code SwtDisplay.getDefault()}
 * is exercised end-to-end by an embedding host that spawns a real JVM with SWT
 * on the classpath.
 */
class DefaultDisplayResolverTest {

    @AfterEach
    void clearResolver() {
        DefaultDisplayResolver.set(null);
    }

    @Test
    void get_returnsNull_whenResolverUnset() {
        // Default state. Desktop SWT relies on this — SwtDisplay.getDefault()
        // falls back to its legacy singleton when get() returns null.
        assertThat(DefaultDisplayResolver.get()).isNull();
    }

    @Test
    void get_returnsResolverOutput_whenSet() {
        Object marker = new Object();
        DefaultDisplayResolver.set(() -> marker);
        assertThat(DefaultDisplayResolver.get()).isSameAs(marker);
    }

    @Test
    void get_returnsNull_whenResolverReturnsNull() {
        DefaultDisplayResolver.set(() -> null);
        assertThat(DefaultDisplayResolver.get()).isNull();
    }

    @Test
    void set_null_clearsResolver() {
        DefaultDisplayResolver.set(() -> "something");
        DefaultDisplayResolver.set(null);
        assertThat(DefaultDisplayResolver.get()).isNull();
    }

    @Test
    void resolverState_isShared_acrossThreads() throws Exception {
        Object marker = new Object();
        DefaultDisplayResolver.set(() -> marker);

        Object[] fromOtherThread = new Object[1];
        Thread t = new Thread(() -> fromOtherThread[0] = DefaultDisplayResolver.get());
        t.start();
        t.join();
        assertThat(fromOtherThread[0]).isSameAs(marker);
    }

    @Test
    void resolver_canReturnDifferentValuePerCall() {
        // Sanity check: the supplier is invoked on every get(), not cached.
        int[] counter = {0};
        DefaultDisplayResolver.set(() -> ++counter[0]);
        assertThat(DefaultDisplayResolver.get()).isEqualTo(1);
        assertThat(DefaultDisplayResolver.get()).isEqualTo(2);
        assertThat(DefaultDisplayResolver.get()).isEqualTo(3);
    }
}
