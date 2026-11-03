package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The client-drawn macOS green traffic light asks Java for full screen ({@code WinFullScreen} /
 * {@code WinExitFullScreen}). The request must land on the main shell, so {@code getFullScreen()}
 * reports it, and reach the host window — not resize the window to the screen's work area, which
 * leaves the Dock and the menu bar visible.
 *
 * <p>The desktop window can also enter or leave full screen on its own (the OS, accessibility
 * tools). As on native Cocoa, the shell then follows the window, nothing is commanded back to it,
 * and the client is told the new state so its controls follow too. The resize full screen causes
 * must not take the shell out of full screen.
 *
 * <p>Driven like {@link DisplayResizeFlutterTest}: a {@link RecordingComm} stands in for the Flutter
 * client and the JUnit thread is the Display's UI thread.
 */
@Tag("flutter-it")
class WindowFullScreenFlutterTest {

    private Display display;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void web_fullScreenRequest_putsMainShellInFullScreen() {
        TestWebBridge web = install(TestWebBridge::new);
        Shell shell = new Shell(display);

        web.comm.fireContaining("/WinFullScreen", "");
        drainEventLoop();

        assertThat(shell.getFullScreen()).as("the green traffic light puts the main shell in full screen").isTrue();
        assertThat(web.windowFullScreen).as("the host window is asked for full screen").containsExactly(true);

        web.comm.fireContaining("/WinExitFullScreen", "");
        drainEventLoop();

        assertThat(shell.getFullScreen()).as("pressing it again leaves full screen").isFalse();
        assertThat(web.windowFullScreen).containsExactly(true, false);
    }

    @Test
    void desk_fullScreenRequest_putsMainShellInFullScreen() {
        TestDeskBridge desk = install(TestDeskBridge::new);
        Shell shell = new Shell(display);

        desk.comm.fireContaining("/WinFullScreen", "");
        drainEventLoop();

        assertThat(shell.getFullScreen()).isTrue();
        assertThat(desk.windowFullScreen).containsExactly(true);

        desk.comm.fireContaining("/WinExitFullScreen", "");
        drainEventLoop();

        assertThat(shell.getFullScreen()).isFalse();
        assertThat(desk.windowFullScreen).containsExactly(true, false);
    }

    @Test
    void web_viewportResizeInFullScreen_keepsFullScreen_andDoesNotEchoToWindow() {
        TestWebBridge web = install(TestWebBridge::new);
        Shell shell = new Shell(display);
        clientReady(web.comm, 900, 600, true);

        web.comm.fireContaining("/WinFullScreen", "");
        drainEventLoop();
        clientReady(web.comm, 2992, 1877, false); // the size full screen gave the window
        drainEventLoop();

        assertThat(shell.getFullScreen()).as("a resize reported by the window keeps full screen").isTrue();
        assertThat(web.windowFullScreen).as("the window is never told to leave full screen").containsExactly(true);
        assertThat(lastFullScreenState(web.comm)).isEqualTo("true");
    }

    @Test
    void desk_windowEntersAndLeavesFullScreenOnItsOwn_shellFollows() {
        TestDeskBridge desk = install(TestDeskBridge::new);
        Shell shell = new Shell(display);

        desk.comm.fireContaining("/WinDidEnterFullScreen", "");
        drainEventLoop();

        assertThat(shell.getFullScreen()).isTrue();
        assertThat(lastFullScreenState(desk.comm)).isEqualTo("true");

        desk.comm.fireContaining("/WinDidExitFullScreen", "");
        drainEventLoop();

        assertThat(shell.getFullScreen()).isFalse();
        assertThat(lastFullScreenState(desk.comm)).isEqualTo("false");
        assertThat(desk.windowFullScreen).as("nothing is commanded back to the window").isEmpty();
    }

    @Test
    void desk_viewportResizeInFullScreen_keepsFullScreen_andDoesNotEchoToWindow() {
        TestDeskBridge desk = install(TestDeskBridge::new);
        Shell shell = new Shell(display);
        clientReady(desk.comm, 900, 600, true);

        desk.comm.fireContaining("/WinFullScreen", "");
        drainEventLoop();
        desk.comm.fireContaining("/WinDidEnterFullScreen", "");
        drainEventLoop();
        clientReady(desk.comm, 2992, 1877, false);
        drainEventLoop();

        assertThat(shell.getFullScreen()).isTrue();
        assertThat(desk.windowFullScreen).containsExactly(true);
    }

    @Test
    void applicationSetFullScreen_drivesWindow_andTellsClient() {
        TestWebBridge web = install(TestWebBridge::new);
        Shell shell = new Shell(display);
        clientReady(web.comm, 900, 600, true);

        shell.setFullScreen(true); // e.g. the Eclipse workbench's Toggle Full Screen (Ctrl+Cmd+F)

        assertThat(web.windowFullScreen).containsExactly(true);
        assertThat(lastFullScreenState(web.comm)).as("the client controls are told").isEqualTo("true");

        shell.setFullScreen(false);

        assertThat(web.windowFullScreen).containsExactly(true, false);
        assertThat(lastFullScreenState(web.comm)).isEqualTo("false");
    }

    @Test
    void fullScreenRequest_afterDisplayDisposed_isSafe() {
        TestWebBridge web = install(TestWebBridge::new);
        new Shell(display);
        display.dispose();

        web.comm.fireContaining("/WinFullScreen", "");
    }

    private static void clientReady(RecordingComm comm, int width, int height, boolean isFirst) {
        ClientReadyPayload p = new ClientReadyPayload();
        p.wireFingerprint = dev.equo.swt.WireTable.FINGERPRINT;
        p.width = width;
        p.height = height;
        p.isFirst = isFirst;
        comm.fireContaining("ClientReady", p);
    }

    /** The {@code fullScreen} value of the last state frame sent to the client, or null. */
    private static String lastFullScreenState(RecordingComm comm) {
        String last = null;
        for (RecordingComm.Frame frame : comm.sent) {
            if (frame.event != null && frame.event.endsWith("/WinFullScreenState"))
                last = String.valueOf(((java.util.Map<?, ?>) frame.value).get("fullScreen"));
        }
        return last;
    }

    private void drainEventLoop() {
        while (display.readAndDispatch()) {
            /* drain */
        }
    }

    /** A Display whose display-level bridge is the one {@code factory} builds, with no real surface. */
    private <B extends DisplayBridge> B install(Function<DartDisplay, B> factory) {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        B bridge = factory.apply(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

    private static final class TestWebBridge extends WebDisplayBridge {
        final RecordingComm comm = new RecordingComm();
        final List<Boolean> windowFullScreen = new CopyOnWriteArrayList<>();

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
            registerDisplayFullScreen(display);
        }

        @Override
        protected boolean hasNativeWindow() {
            return true; // a stood-in Chromium window, observed through the forward seams
        }

        @Override
        protected void forwardWindowFullScreen(boolean fullScreen) {
            windowFullScreen.add(fullScreen);
        }

        @Override
        protected void forwardWindowBounds(org.eclipse.swt.graphics.Rectangle bounds) {
        }

        @Override
        public void setWindowTitle(DartControl control, String title) {
        }

        @Override
        public void setWindowMaximized(DartControl control, boolean maximized) {
        }

        @Override
        public void setWindowMinimized(DartControl control, boolean minimized) {
        }
    }

    private static final class TestDeskBridge extends DeskDisplayBridge {
        final RecordingComm comm = new RecordingComm();
        final List<Boolean> windowFullScreen = new CopyOnWriteArrayList<>();

        TestDeskBridge(DartDisplay display) {
            super(display);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        protected org.eclipse.swt.graphics.Point shellWindowOrigin(long context) {
            return null;
        }

        @Override
        protected void wakeNativeWindow() {
        }

        @Override
        protected void start(DartDisplay display) {
            registerDisplayClientReady(display);
            registerDisplayFullScreen(display);
        }

        @Override
        protected boolean hasWindowSurface() {
            return true;
        }

        @Override
        protected int pumpWindow() {
            return 0;
        }

        @Override
        protected void disposeNativeWindow() {
        }

        @Override
        protected void forwardWindowFullScreen(boolean fullScreen) {
            windowFullScreen.add(fullScreen);
        }

        @Override
        protected void forwardWindowBounds(org.eclipse.swt.graphics.Rectangle bounds) {
        }

        @Override
        public void setWindowTitle(DartControl control, String title) {
        }
    }
}
