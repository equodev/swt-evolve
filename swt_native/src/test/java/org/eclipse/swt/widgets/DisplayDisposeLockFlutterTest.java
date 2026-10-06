package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Several Displays can live in one JVM, each on its own UI thread. Disposing one runs application
 * listeners, and a listener that blocks there (a dialog opened while the Display is torn down, whose
 * nested event loop never returns) must stall only that Display — not the class-wide state every other
 * Display goes through ({@code findDisplay}, {@code getThread}, {@code asyncExec}, a new Display).
 */
@Tag("flutter-it")
class DisplayDisposeLockFlutterTest {

    private final CountDownLatch releaseListener = new CountDownLatch(1);
    private Thread uiThread;

    @AfterEach
    void tearDown() throws InterruptedException {
        releaseListener.countDown();
        if (uiThread != null)
            uiThread.join(TimeUnit.SECONDS.toMillis(10));
        FlutterBridge.set(null);
    }

    @Test
    void aListenerBlockedInsideDispose_doesNotBlockOtherDisplays() throws Exception {
        CountDownLatch insideDispose = new CountDownLatch(1);
        AtomicReference<Display> display = new AtomicReference<>();
        AtomicReference<Throwable> uiError = new AtomicReference<>();
        CountDownLatch ready = new CountDownLatch(1);

        uiThread = new Thread(() -> {
            try {
                FlutterBridge.set(new RecordingBridge());
                Display d = new Display();
                Shell shell = new Shell(d);
                shell.addListener(SWT.Dispose, e -> {
                    insideDispose.countDown();
                    try {
                        releaseListener.await(10, TimeUnit.SECONDS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                });
                shell.open();
                display.set(d);
                ready.countDown();
                while (!d.isDisposed()) {
                    if (!d.readAndDispatch())
                        d.sleep();
                }
            } catch (Throwable t) {
                uiError.set(t);
                ready.countDown();
            }
        }, "ui-display-being-disposed");
        uiThread.setDaemon(true);
        uiThread.start();

        assertThat(ready.await(5, TimeUnit.SECONDS)).as("UI thread should create the Display").isTrue();
        if (uiError.get() != null)
            throw new IllegalStateException("UI thread failed to start", uiError.get());

        Display d = display.get();
        d.asyncExec(d::dispose);
        assertThat(insideDispose.await(5, TimeUnit.SECONDS))
                .as("the Shell's Dispose listener should run while the Display is disposed").isTrue();

        Thread probeThread = new Thread(() -> {}, "other-display-thread");
        CompletableFuture<Display> otherDisplay =
                CompletableFuture.supplyAsync(() -> Display.findDisplay(probeThread));
        try {
            assertThat(otherDisplay.get(2, TimeUnit.SECONDS)).isNull();
        } catch (TimeoutException blocked) {
            fail("Display.findDisplay blocked while another Display's dispose was inside a listener");
        }
    }
}
