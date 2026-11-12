package org.eclipse.swt.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import dev.equo.swt.harness.CarriedContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The action given to {@link BusyIndicator#compute(org.eclipse.swt.SwtCallable, java.util.concurrent.Executor)}
 * runs on the caller's executor under the context of the calling thread.
 */
@Tag("native-unit")
@ExtendWith(Mocks.class)
class BusyIndicatorCallerContextNativeTest {

    private Display display;
    private final ExecutorService pool = Executors.newSingleThreadExecutor();

    @BeforeEach
    void setUp() {
        display = Mocks.display();
        doReturn(new Shell[0]).when(display).getShells();
    }

    @AfterEach
    void tearDown() {
        pool.shutdownNow();
        CarriedContext.uninstall();
        doReturn(null).when(display).getShells();
    }

    @Test
    void theActionRunsOnTheCallersExecutorUnderTheContextOfTheCallingThread() throws Exception {
        CarriedContext.install();
        CarriedContext.set("caller");
        Thread caller = Thread.currentThread();

        String seen = BusyIndicator.compute(() -> {
            assertThat(Thread.currentThread()).isNotSameAs(caller);
            return CarriedContext.get();
        }, pool).get(10, TimeUnit.SECONDS);

        assertThat(seen).isEqualTo("caller");
    }
}
