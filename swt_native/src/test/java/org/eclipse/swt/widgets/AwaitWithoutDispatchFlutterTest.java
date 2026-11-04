package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.FlutterNative;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DisplayBridge#awaitWithoutDispatch}: the wait inside a native call SWT answers synchronously
 * (an image's pixels) ends at its answer or its deadline and runs no application code on the way,
 * on every surface.
 */
@Tag("flutter-it")
class AwaitWithoutDispatchFlutterTest {

    private Display display;
    private String savedMode;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
        if (savedMode == null)
            System.clearProperty("dev.equo.swt.mode");
        else
            System.setProperty("dev.equo.swt.mode", savedMode);
    }

    @Test
    void anUnansweredWaitEndsAtItsDeadlineAndRunsNoAsyncExec() {
        install(TestWebBridge::new);
        AtomicBoolean ran = new AtomicBoolean();
        display.asyncExec(() -> ran.set(true));

        long started = System.nanoTime();
        DisplayBridge.awaitWithoutDispatch(display, new CompletableFuture<Void>(), 200);
        long tookMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);

        assertThat(tookMillis).as("the wait honours its deadline").isBetween(150L, 5000L);
        assertThat(ran).as("an asyncExec queued before the wait runs after it, not inside it").isFalse();
        drainEventLoop();
        assertThat(ran).isTrue();
    }

    @Test
    void anAnswerEndsTheWait() {
        install(TestWebBridge::new);
        CompletableFuture<Void> answer = new CompletableFuture<>();
        completeLater(answer, 50);

        long started = System.nanoTime();
        DisplayBridge.awaitWithoutDispatch(display, answer, 30000);

        assertThat(answer).isDone();
        assertThat(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started)).isLessThan(5000L);
    }

    @Test
    void noDisplayOrADisposedOneDoesNotWait() {
        install(TestWebBridge::new);
        long started = System.nanoTime();
        DisplayBridge.awaitWithoutDispatch(null, new CompletableFuture<Void>(), 30000);
        display.dispose();
        DisplayBridge.awaitWithoutDispatch(display, new CompletableFuture<Void>(), 30000);
        assertThat(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started)).isLessThan(5000L);
    }

    @Test
    void aPumpDrivenSurfaceIsPumpedWithoutItsEventLoopTurn() {
        PumpingWebBridge bridge = install(PumpingWebBridge::new);
        AtomicBoolean ran = new AtomicBoolean();
        display.asyncExec(() -> ran.set(true));
        CompletableFuture<Void> answer = new CompletableFuture<>();
        completeLater(answer, 100);

        DisplayBridge.awaitWithoutDispatch(display, answer, 30000);

        assertThat(answer).isDone();
        assertThat(bridge.enginePumps.get()).as("the surface's engine keeps running").isPositive();
        assertThat(bridge.eventLoopTurns.get()).as("no event-loop turn: no bounds sync, no menu bar").isZero();
        assertThat(ran).isFalse();
    }

    @Test
    void aWaitOffTheUiThreadPumpsNothing() throws Exception {
        PumpingWebBridge bridge = install(PumpingWebBridge::new);
        AtomicBoolean ended = new AtomicBoolean();
        Thread reader = new Thread(() -> {
            DisplayBridge.awaitWithoutDispatch(display, new CompletableFuture<Void>(), 100);
            ended.set(true);
        });
        reader.start();
        reader.join(5000);

        assertThat(ended).as("the wait ends at its deadline off the UI thread too").isTrue();
        assertThat(bridge.enginePumps.get()).as("only the display's thread drives its event loop").isZero();
    }

    @Test
    void theNativeWindowWaitMovesNoShellAndOnlyQueuesAClose() {
        TestDeskBridge desk = install(TestDeskBridge::new);
        Shell shell = new Shell(display);
        shell.setBounds(0, 0, 300, 200);
        shell.open();
        drainEventLoop();
        AtomicInteger moves = new AtomicInteger();
        AtomicInteger closes = new AtomicInteger();
        shell.addListener(SWT.Move, e -> moves.incrementAndGet());
        shell.addListener(SWT.Resize, e -> moves.incrementAndGet());
        shell.addListener(SWT.Close, e -> {
            closes.incrementAndGet();
            e.doit = false;
        });
        desk.origin = new Point(120, 80);
        desk.pumpStatus.add(FlutterNative.PUMP_CLOSE_REQUESTED);
        CompletableFuture<Void> answer = new CompletableFuture<>();
        completeLater(answer, 100);

        DisplayBridge.awaitWithoutDispatch(display, answer, 30000);

        assertThat(desk.pumps.get()).as("the window's event loop is pumped").isPositive();
        assertThat(moves.get()).as("the window's origin is not applied to the shell inside the wait").isZero();
        assertThat(closes.get()).as("the close request is not run inside the wait").isZero();
        drainEventLoop();
        assertThat(closes.get()).as("the close request is queued, not lost").isEqualTo(1);
    }

    // ---- harness ----------------------------------------------------------------------------------

    private static void completeLater(CompletableFuture<Void> future, long millis) {
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            future.complete(null);
        });
        t.setDaemon(true);
        t.start();
    }

    private void drainEventLoop() {
        while (display.readAndDispatch()) {
            /* drain */
        }
    }

    private <B extends DisplayBridge> B install(Function<DartDisplay, B> factory) {
        savedMode = System.getProperty("dev.equo.swt.mode");
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        B bridge = factory.apply(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

    private static class TestWebBridge extends WebDisplayBridge {
        final RecordingComm comm = new RecordingComm();

        TestWebBridge(DartDisplay display) {
            super(display);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        protected void start(DartDisplay display) {
            registerDisplayClientReady(display);
        }
    }

    /** A surface whose event loop is pull-driven, like the Chromium standalone window. */
    private static final class PumpingWebBridge extends TestWebBridge {
        final AtomicInteger enginePumps = new AtomicInteger();
        final AtomicInteger eventLoopTurns = new AtomicInteger();

        PumpingWebBridge(DartDisplay display) {
            super(display);
        }

        @Override
        public boolean needsPump() {
            return true;
        }

        @Override
        public void pumpEngineOnly() {
            enginePumps.incrementAndGet();
        }

        @Override
        public void onUpdate() {
            eventLoopTurns.incrementAndGet();
        }
    }

    /** Desktop-native bridge with the native window stubbed: a scripted pump at a given origin. */
    private static final class TestDeskBridge extends DeskDisplayBridge {
        final RecordingComm comm = new RecordingComm();
        final Deque<Integer> pumpStatus = new ArrayDeque<>();
        final AtomicInteger pumps = new AtomicInteger();
        volatile Point origin;

        TestDeskBridge(DartDisplay display) {
            super(display);
        }

        @Override
        protected Point shellWindowOrigin(long context) {
            return origin;
        }

        @Override
        protected int workAreaTop(Rectangle window) {
            return 0;
        }

        @Override
        protected void wakeNativeWindow() {
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        protected void start(DartDisplay display) {
            registerDisplayClientReady(display);
        }

        @Override
        protected boolean hasWindowSurface() {
            return true;
        }

        @Override
        protected int pumpWindow() {
            pumps.incrementAndGet();
            Integer status = pumpStatus.poll();
            return status == null ? 0 : status;
        }

        @Override
        protected void disposeNativeWindow() {
        }

        @Override
        public void setWindowTitle(DartControl control, String title) {
        }

        @Override
        protected void forwardWindowBounds(Rectangle bounds) {
        }

        @Override
        public void sleep(int millis) throws InterruptedException {
            Thread.sleep(Math.min(millis, 10));
        }
    }
}
