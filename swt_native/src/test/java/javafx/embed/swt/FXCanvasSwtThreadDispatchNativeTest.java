package javafx.embed.swt;

import com.sun.glass.ui.Application;
import com.sun.javafx.tk.Toolkit;
import javafx.application.Platform;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression guard for FXCanvas SWT-thread input dispatch.
 *
 * <p>A JavaFX event handler in an {@code FXCanvas}-embedded scene (e.g. a link whose handler opens a
 * JFace preference dialog) must be able to call SWT/JFace. Upstream FXCanvas achieves this by
 * running on the SWT thread ({@code javafx.embed.isEventThread=true}); Evolve keeps JavaFX on its own
 * thread for rendering and instead dispatches input on the SWT thread via
 * {@link FXCanvas#runAsFxUserThread(Runnable)}. For that to be legal, the work must observe itself as
 * the JavaFX thread on <em>both</em> gates JavaFX enforces — {@link Toolkit#checkFxUserThread()} and
 * {@link Application#checkEventThread()}, which read different static fields. Repointing only one
 * (the original bug) throws {@code IllegalStateException} inside the handler, is swallowed, and the
 * link silently does nothing.</p>
 *
 * <p>Drives the real production method against a live headless (Monocle) toolkit — no SWT Display or
 * scene peer needed — and asserts the work ran on the calling thread with both gates satisfied, is
 * reentrant, and restores the thread ownership afterwards.</p>
 */
@Tag("native-unit")
public class FXCanvasSwtThreadDispatchNativeTest {

    @BeforeAll
    static void startToolkit() throws InterruptedException {
        // Headless Monocle + software Prism: a JavaFX thread with no display server, as CI runs.
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.vsync", "false");
        Platform.setImplicitExit(false);
        CountDownLatch up = new CountDownLatch(1);
        try {
            Platform.startup(up::countDown);
        } catch (IllegalStateException alreadyStarted) {
            up.countDown();
        }
        assertThat(up.await(20, TimeUnit.SECONDS)).as("JavaFX toolkit started").isTrue();
    }

    @AfterAll
    static void stopToolkit() {
        // Leave the (idle) toolkit for the forked test JVM to reap; Platform.exit() cannot be undone
        // and would break any later re-entry in the same JVM.
    }

    @Test
    void dispatchRunsOnCallingThreadPassingBothJavaFxThreadGates() {
        Thread caller = Thread.currentThread();
        AtomicBoolean ran = new AtomicBoolean(false);
        AtomicBoolean onCallingThread = new AtomicBoolean(false);
        AtomicBoolean reentrantRan = new AtomicBoolean(false);
        AtomicReference<Throwable> gateFailure = new AtomicReference<>();

        boolean dispatched = FXCanvas.runAsFxUserThread(() -> {
            ran.set(true);
            onCallingThread.set(Thread.currentThread() == caller);
            try {
                // The two guards JavaFX's mouse processing hits. Either throws if its static thread
                // field was not repointed at this thread — the exact failure this guards against.
                Toolkit.getToolkit().checkFxUserThread();
                Application.checkEventThread();
            } catch (Throwable t) {
                gateFailure.set(t);
            }
            // Reentrant dispatch (an event fired while a modal dialog opened by this handler pumps
            // its own loop on this thread) must run inline, not deadlock re-parking the FX thread.
            boolean nested = FXCanvas.runAsFxUserThread(() -> reentrantRan.set(true));
            assertThat(nested).as("reentrant dispatch ran").isTrue();
        });

        assertThat(dispatched).as("runAsFxUserThread dispatched (FX internals reachable)").isTrue();
        assertThat(ran).as("work ran").isTrue();
        assertThat(onCallingThread).as("work ran synchronously on the calling thread, not the FX thread").isTrue();
        assertThat(gateFailure.get())
                .as("both JavaFX thread gates passed inside the dispatch (fxUserThread AND eventThread repointed)")
                .isNull();
        assertThat(reentrantRan).as("reentrant dispatch ran inline").isTrue();

        // Ownership restored: the caller is no longer the JavaFX thread once dispatch returns.
        assertThat(Toolkit.getToolkit().isFxUserThread())
                .as("Toolkit.fxUserThread restored off the calling thread").isFalse();
        assertThat(Application.isEventThread())
                .as("Application.eventThread restored off the calling thread").isFalse();
    }
}
