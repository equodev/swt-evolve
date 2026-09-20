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

        runOffUiThread(() -> display.asyncExec(() -> {
        }));

        assertThat(desk.nativeWakes.get())
                .as("asyncExec from another thread must post a native wake, not only release the permit")
                .isPositive();
    }

    @Test
    void stateDirtiedFromAnotherThread_wakesTheNativeLoop() throws Exception {
        TestDeskBridge desk = install();
        Shell shell = new Shell(display);
        while (display.readAndDispatch()) {
            /* settle the shell's own delivery */
        }
        desk.nativeWakes.set(0);

        runOffUiThread(() -> desk.dirty((DartWidget) shell.getImpl()));

        assertThat(desk.nativeWakes.get())
                .as("a widget dirtied off the UI thread must interrupt the idle wait so the flush runs")
                .isPositive();
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

        TestDeskBridge(DartDisplay display) {
            super(display);
        }

        @Override
        protected org.eclipse.swt.graphics.Point shellWindowOrigin(long context) {
            return null; // a stood-in window sits nowhere; only a real one has a screen position
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
