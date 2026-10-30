package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins that work posted from another thread reaches the desktop-native surface's OS event loop.
 *
 * <p>That surface idles inside {@code FlutterNative.waitEvents}, which cannot observe the Java wake
 * permit {@code asyncExec} releases. Unless the wake is also posted natively the runnable waits for
 * the idle deadline instead — measured at p50 ≈ 8 ms, max 16 ms on all three desktop OSs, against
 * ≈ 0.1 ms on web.
 *
 * <p>The native window is stubbed behind the bridge's seams, as in {@link ShellCloseContractFlutterTest},
 * so the wake is observable in the JVM with no window.
 */
@Tag("flutter-it")
class DisplayWakeDeskFlutterTest {

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
    void asyncExecFromAnotherThread_wakesTheNativeLoop() throws Exception {
        TestDeskBridge desk = install();
        desk.armWake(); // the UI thread is about to wait, as Display.sleep arms it

        runOffUiThread(() -> display.asyncExec(() -> {
        }));

        assertThat(desk.nativeWakes.get())
                .as("asyncExec from another thread must post a native wake, not only release the permit")
                .isPositive();
    }

    @Test
    void asyncExecWhileTheUiThreadRunsATurn_postsNothingAndStillRuns() throws Exception {
        TestDeskBridge desk = install();
        while (display.readAndDispatch()) {
            /* the UI thread is mid-turn, not waiting */
        }
        desk.nativeWakes.set(0);
        boolean[] ran = { false };

        runOffUiThread(() -> display.asyncExec(() -> ran[0] = true));

        assertThat(desk.nativeWakes.get())
                .as("a running turn finds the work before it waits again; a wake would only be an OS event to dispatch")
                .isZero();
        while (display.readAndDispatch()) {
            /* the next turn runs it */
        }
        assertThat(ran[0]).isTrue();
    }

    @Test
    void asyncExecFromTheUiThreadItself_postsNothing() {
        TestDeskBridge desk = install();
        while (display.readAndDispatch()) {
            /* settle */
        }
        desk.nativeWakes.set(0);

        display.asyncExec(() -> {
        });

        assertThat(desk.nativeWakes.get())
                .as("the UI thread posting work to itself is running, and runs it before it waits")
                .isZero();
    }

    @Test
    void asyncExecFromTheUiThreadDuringTheNativeWait_wakesIt() {
        TestDeskBridge desk = install();
        while (display.readAndDispatch()) {
            /* settle */
        }
        desk.nativeWakes.set(0);
        // The OS loop runs the client's callbacks on the UI thread while Java waits in it.
        desk.duringWait = () -> display.asyncExec(() -> {
        });

        display.sleep();

        assertThat(desk.waits.get()).as("the UI thread waited").isEqualTo(1);
        assertThat(desk.nativeWakes.get())
                .as("work a callback posts inside the wait has to end it, though it comes from the UI thread")
                .isEqualTo(1);
    }

    @Test
    void stateDirtiedFromAnotherThread_wakesTheNativeLoop() throws Exception {
        TestDeskBridge desk = install();
        Shell shell = new Shell(display);
        while (display.readAndDispatch()) {
            /* settle the shell's own delivery */
        }
        desk.nativeWakes.set(0);
        desk.armWake();

        runOffUiThread(() -> desk.dirty((DartWidget) shell.getImpl()));

        assertThat(desk.nativeWakes.get())
                .as("a widget dirtied off the UI thread must interrupt the idle wait so the flush runs")
                .isPositive();
    }

    @Test
    void stateDirtiedOnTheUiThread_postsNothing() {
        TestDeskBridge desk = install();
        Shell shell = new Shell(display);
        while (display.readAndDispatch()) {
            /* settle the shell's own delivery */
        }
        desk.nativeWakes.set(0);

        desk.dirty((DartWidget) shell.getImpl());

        assertThat(desk.nativeWakes.get())
                .as("the UI thread is not waiting while it dirties state; its next readAndDispatch flushes it")
                .isZero();
    }

    @Test
    void wakesWhileOneIsQueued_postOnlyOnceUntilThePumpDrainsIt() throws Exception {
        TestDeskBridge desk = install();
        desk.armWake();

        runOffUiThread(() -> {
            for (int i = 0; i < 100; i++) display.asyncExec(() -> {
            });
        });

        assertThat(desk.nativeWakes.get())
                .as("one queued wake already ends the wait; every extra one is an OS event to dispatch")
                .isEqualTo(1);

        while (display.readAndDispatch()) {
            /* the pump drains the queued wake */
        }
        desk.armWake();
        runOffUiThread(() -> display.asyncExec(() -> {
        }));

        assertThat(desk.nativeWakes.get())
                .as("once the pump has drained it, the next wait is woken again")
                .isEqualTo(2);
    }

    @Test
    void wakeFromTheUiThreadItself_postsNothing() {
        TestDeskBridge desk = install();

        display.wake(); // the UI thread is running, not parked

        assertThat(desk.nativeWakes.get())
                .as("Display.wake() on the UI thread is a no-op in SWT and must stay one here")
                .isZero();
    }

    private void runOffUiThread(Runnable body) throws InterruptedException {
        Thread other = new Thread(body, "wake-test");
        other.start();
        other.join(5_000);
    }

    private TestDeskBridge install() {
        savedMode = System.getProperty("dev.equo.swt.mode");
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        TestDeskBridge bridge = new TestDeskBridge(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

    /** Desktop-native bridge with the native window stubbed: counts the wakes it would post. */
    private static final class TestDeskBridge extends DeskDisplayBridge {
        final RecordingComm comm = new RecordingComm();
        final AtomicInteger nativeWakes = new AtomicInteger();
        final AtomicInteger waits = new AtomicInteger();
        /** Runs inside each wait, as the OS loop runs callbacks while Java waits in it. */
        Runnable duringWait;

        TestDeskBridge(DartDisplay display) {
            super(display);
        }

        @Override
        protected org.eclipse.swt.graphics.Point shellWindowOrigin(long context) {
            return null; // a stood-in window sits nowhere; only a real one has a screen position
        }

        @Override
        protected int workAreaTop(org.eclipse.swt.graphics.Rectangle window) {
            return 0;
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        protected void start(DartDisplay display) {
            registerDisplayClientReady(display); // no native window, just the comm handshake
        }

        @Override
        protected boolean hasWindowSurface() {
            return true;
        }

        @Override
        protected void wakeNativeWindow() {
            nativeWakes.incrementAndGet();
        }

        @Override
        protected void waitNativeWindow(int millis) {
            waits.incrementAndGet();
            if (duringWait != null) duringWait.run();
        }

        @Override
        protected int pumpWindow() {
            return 0;
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
    }
}
