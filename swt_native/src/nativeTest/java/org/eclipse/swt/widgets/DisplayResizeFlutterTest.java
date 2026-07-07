package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the desktop/web window-resize contract — the fix for the resize loop where a window resize on
 * desk (and Chromium) was echoed straight back to the OS window and oscillated bigger/smaller.
 *
 * <p>The conceptual model encoded here:
 * <ul>
 *   <li><b>web</b> — the browser viewport <em>is</em> the Display. A {@code Display/{id}/ClientReady}
 *       resize syncs <em>both</em> {@code display.bounds} and the main shell to the reported size;
 *       repeating the same size is idempotent (no re-push).</li>
 *   <li><b>desk</b> — the Display is the monitor and is governed independently of the window. A
 *       {@code ClientReady} resize drives the main <em>shell</em> only, never {@code display.bounds},
 *       and is <em>never</em> echoed back to the native window (that round-trip is the loop). A
 *       genuine app-driven {@code shell.setBounds} <em>is</em> forwarded to the window.</li>
 * </ul>
 *
 * <p>Like {@link DisplayWakeFlutterTest} this is a {@code @Tag("flutter-it")} test driven by the
 * {@code nativeTest} task; it needs no renderer (a {@link RecordingComm} stands in for the Flutter
 * client) and runs single-threaded — the JUnit thread is the Display's UI thread, so the
 * {@code ClientReady} handler's {@code syncExec} runs inline. It reaches the per-surface bridges by
 * sub-classing them with the native window-forward and window-presence stubbed, making the otherwise
 * native-only echo observable in the JVM.
 */
@Tag("flutter-it")
class DisplayResizeFlutterTest {

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

    // ---- desk -------------------------------------------------------------------------------------

    @Test
    void desk_windowResize_drivesShellOnly_andDoesNotEchoBack() {
        TestDeskBridge desk = install(TestDeskBridge::new);
        Shell shell = newMainShell();
        Rectangle monitorBefore = dartDisplay().bounds; // the Display is the monitor (placeholder)
        desk.forwarded.clear();

        clientReady(desk.comm, 1280, 800, true);   // initial handshake: the native window viewport
        clientReady(desk.comm, 1300, 820, false);  // the user drags the native window larger

        assertThat(shell.getBounds())
                .as("the main shell follows the native window viewport")
                .isEqualTo(new Rectangle(0, 0, 1300, 820));
        assertThat(dartDisplay().bounds)
                .as("on desk the Display is the monitor and must NOT be overwritten by the viewport")
                .isEqualTo(monitorBefore);
        assertThat(desk.forwarded)
                .as("a client-driven resize must not be echoed back to the window (the resize loop)")
                .isEmpty();
    }

    @Test
    void desk_firstClientReady_sizesShellToWindow_displayStaysMonitor() {
        TestDeskBridge desk = install(TestDeskBridge::new);
        Shell shell = newMainShell(); // created at the placeholder Display (monitor) bounds
        Rectangle monitorBefore = dartDisplay().bounds;
        desk.forwarded.clear();

        clientReady(desk.comm, 1280, 800, true); // the window reports its actual viewport once ready

        assertThat(shell.getBounds())
                .as("the initial handshake sizes the main shell to the native window")
                .isEqualTo(new Rectangle(0, 0, 1280, 800));
        assertThat(dartDisplay().bounds)
                .as("the Display stays the monitor")
                .isEqualTo(monitorBefore);
        assertThat(desk.forwarded)
                .as("sizing the shell to the size the window already has must not echo back")
                .isEmpty();
    }

    @Test
    void desk_resizeSequence_growThenShrink_neverEchoes() {
        TestDeskBridge desk = install(TestDeskBridge::new);
        Shell shell = newMainShell();
        desk.forwarded.clear();

        clientReady(desk.comm, 1280, 800, true);
        clientReady(desk.comm, 1400, 900, false); // user drags larger
        clientReady(desk.comm, 1100, 700, false); // then smaller
        clientReady(desk.comm, 1100, 700, false); // a redundant report at the same size

        assertThat(shell.getBounds())
                .as("the shell tracks the final window viewport")
                .isEqualTo(new Rectangle(0, 0, 1100, 700));
        assertThat(desk.forwarded)
                .as("no client-driven step may echo back to the window (no bigger/smaller loop)")
                .isEmpty();
    }

    @Test
    void desk_reportedMonitor_setsDisplayBounds_shellTracksViewport() {
        TestDeskBridge desk = install(TestDeskBridge::new);
        Shell shell = newMainShell();
        desk.forwarded.clear();

        clientReady(desk.comm, 1280, 800, 2560, 1440, true); // 1280x800 window on a 2560x1440 monitor

        assertThat(dartDisplay().bounds)
                .as("the Display (the monitor) reflects the reported screen size")
                .isEqualTo(new Rectangle(0, 0, 2560, 1440));
        assertThat(dartDisplay().getPrimaryMonitor().getBounds())
                .as("getPrimaryMonitor() reports the real screen, not an empty rectangle")
                .isEqualTo(new Rectangle(0, 0, 2560, 1440));
        assertThat(dartDisplay().getMonitors()[0].getBounds())
                .as("getMonitors() reports the real screen")
                .isEqualTo(new Rectangle(0, 0, 2560, 1440));
        assertThat(shell.getBounds())
                .as("the main shell still tracks the window viewport, not the monitor")
                .isEqualTo(new Rectangle(0, 0, 1280, 800));
        assertThat(desk.forwarded)
                .as("reporting the screen + sizing the shell must not echo back to the window")
                .isEmpty();
    }

    @Test
    void desk_nonOriginMainShell_isSlavedToViewport() {
        TestDeskBridge desk = install(TestDeskBridge::new);
        Shell shell = newMainShell();
        // The Eclipse workbench opens its main shell at a saved, non-origin geometry — it is NOT at
        // (0,0) and NOT maximized. The resize must still slave it to the window viewport.
        shell.setBounds(200, 100, 1024, 768);
        desk.forwarded.clear();

        clientReady(desk.comm, 1600, 1000, false);

        assertThat(shell.getBounds())
                .as("a non-origin main shell is still slaved to the window viewport on resize")
                .isEqualTo(new Rectangle(0, 0, 1600, 1000));
    }

    @Test
    void desk_appDrivenResize_isForwardedToWindow() {
        TestDeskBridge desk = install(TestDeskBridge::new);
        Shell shell = newMainShell();
        clientReady(desk.comm, 1280, 800, true);
        desk.forwarded.clear();

        shell.setBounds(0, 0, 1000, 700); // genuine app-driven geometry, not a client echo

        assertThat(desk.forwarded)
                .as("a genuine app-driven shell resize must reach the native window")
                .containsExactly(new Rectangle(0, 0, 1000, 700));
    }

    // ---- web --------------------------------------------------------------------------------------

    @Test
    void web_clientReady_syncsDisplayAndMainShell() {
        TestWebBridge web = install(TestWebBridge::new);
        Shell shell = newMainShell();

        clientReady(web.comm, 1024, 768, true);

        assertThat(dartDisplay().bounds)
                .as("on web the browser viewport IS the Display")
                .isEqualTo(new Rectangle(0, 0, 1024, 768));
        assertThat(shell.getBounds())
                .as("the main shell is slaved to the browser viewport")
                .isEqualTo(new Rectangle(0, 0, 1024, 768));
    }

    @Test
    void web_resizeSequence_keepsDisplayAndShellInLockstep() {
        TestWebBridge web = install(TestWebBridge::new);
        Shell shell = newMainShell();

        clientReady(web.comm, 1024, 768, true);
        clientReady(web.comm, 1280, 800, false); // chrome grown
        clientReady(web.comm, 900, 600, false);  // chrome shrunk

        assertThat(dartDisplay().bounds)
                .as("the Display tracks the browser viewport through the whole sequence")
                .isEqualTo(new Rectangle(0, 0, 900, 600));
        assertThat(shell.getBounds())
                .as("the main shell stays in lockstep with the Display")
                .isEqualTo(new Rectangle(0, 0, 900, 600));
    }

    @Test
    void web_ignoresReportedMonitor_displayIsViewport() {
        TestWebBridge web = install(TestWebBridge::new);
        Shell shell = newMainShell();

        clientReady(web.comm, 1024, 768, 2560, 1440, true); // a monitor is reported but irrelevant on web

        assertThat(dartDisplay().bounds)
                .as("on web the Display is the browser viewport, never the monitor")
                .isEqualTo(new Rectangle(0, 0, 1024, 768));
        assertThat(shell.getBounds()).isEqualTo(new Rectangle(0, 0, 1024, 768));
    }

    @Test
    void web_repeatedSameSize_isIdempotent() {
        TestWebBridge web = install(TestWebBridge::new);
        newMainShell();
        clientReady(web.comm, 1024, 768, true);
        int framesAfterFirst = web.comm.sent.size();

        clientReady(web.comm, 1024, 768, false); // the browser reports the same size again

        assertThat(web.comm.sent.size())
                .as("a repeated identical viewport must not re-push (that would feed a resize loop)")
                .isEqualTo(framesAfterFirst);
    }

    @Test
    void web_nonOriginMainShell_isSlavedToViewport() {
        TestWebBridge web = install(TestWebBridge::new);
        Shell shell = newMainShell();
        // Regression: the workbench main shell sitting at a non-origin geometry (not maximized) must
        // still follow the browser viewport — the old atOrigin-only guard skipped it and its content
        // never relaid out on resize.
        shell.setBounds(200, 100, 1024, 768);

        clientReady(web.comm, 1600, 1000, true);

        assertThat(shell.getBounds())
                .as("a non-origin main shell is still slaved to the browser viewport")
                .isEqualTo(new Rectangle(0, 0, 1600, 1000));
    }

    @Test
    void web_shellShownAtStaleSize_fillsCurrentViewport() {
        TestWebBridge web = install(TestWebBridge::new);
        clientReady(web.comm, 2560, 1440, true); // browser maximized: the current viewport
        Shell shell = newMainShell();
        // A smaller, non-origin geometry left over from a previous run (the workbench persists its
        // window bounds). A maximized browser fires no resize, so without the on-show fix it would
        // stay this stale size until the user manually resized.
        shell.setBounds(100, 50, 1200, 800);

        shell.setVisible(true);

        assertThat(shell.getBounds())
                .as("a main shell shown at a stale non-origin size fills the current viewport")
                .isEqualTo(new Rectangle(0, 0, 2560, 1440));
    }

    @Test
    void web_winClose_closesMainShell() {
        TestWebBridge web = install(TestWebBridge::new);
        Shell shell = newMainShell();
        clientReady(web.comm, 1024, 768, true);

        // the browser tab/window is closing → the Dart client sends WinClose
        web.comm.fireContaining("WinClose", "");
        drainEventLoop(); // onClientWindowClosed() marshals the shell close via asyncExec

        assertThat(shell.isDisposed())
                .as("closing the browser tab closes the main shell, like the native window close on desk")
                .isTrue();
    }

    @Test
    void web_winClose_afterDisplayDisposed_isSafe() {
        TestWebBridge web = install(TestWebBridge::new);
        newMainShell();
        clientReady(web.comm, 1024, 768, true);
        display.dispose(); // the tab teardown already disposed the Display

        // A second WinClose (pagehide + beforeunload both fire) arrives late on the comm thread; it
        // must not throw despite the Display being gone (the prod NPE was a half-released synchronizer).
        web.comm.fireContaining("WinClose", "");
    }

    // ---- harness ----------------------------------------------------------------------------------

    private DartDisplay dartDisplay() {
        return (DartDisplay) display.getImpl();
    }

    /** Run the queued UI work (e.g. an asyncExec'd shell close) to completion. */
    private void drainEventLoop() {
        while (display.readAndDispatch()) {
            /* drain */
        }
    }

    /**
     * Creates a Display whose display-level bridge is the test bridge produced by {@code factory},
     * with no real Flutter surface. A no-op bridge is injected only so {@code Display.init()} skips
     * {@code initForDisplay}'s real surface bridge; it is cleared straight after so every widget
     * routes to the installed display bridge.
     */
    private <B extends DisplayBridge> B install(Function<DartDisplay, B> factory) {
        savedMode = System.getProperty("dev.equo.swt.mode");
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = dartDisplay();
        B bridge = factory.apply(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

    private Shell newMainShell() {
        return new Shell(display); // default (empty) title — a top-level, display-tracking main shell
    }

    private static void clientReady(RecordingComm comm, int width, int height, boolean isFirst) {
        clientReady(comm, width, height, 0, 0, isFirst); // no monitor reported
    }

    private static void clientReady(RecordingComm comm, int width, int height, int monitorW, int monitorH, boolean isFirst) {
        ClientReadyPayload p = new ClientReadyPayload();
        p.width = width;
        p.height = height;
        p.isFirst = isFirst;
        p.displayWidth = monitorW;
        p.displayHeight = monitorH;
        comm.fireContaining("ClientReady", p);
    }

    /** A stub injected only so {@code Display.init()} skips creating a real surface bridge. */
    private static final class NoopBridge extends FlutterBridge {
        final RecordingComm comm = new RecordingComm();

        NoopBridge() {
            clientReady.complete(true);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        public void initFlutterView(Composite parent, DartControl control) {
        }

        @Override
        public void destroy(DartWidget control) {
        }
    }

    /** Desktop-native surface bridge with the native window stubbed so the echo is observable. */
    private static final class TestDeskBridge extends DeskDisplayBridge {
        final RecordingComm comm = new RecordingComm();
        final List<Rectangle> forwarded = new CopyOnWriteArrayList<>();

        TestDeskBridge(DartDisplay display) {
            super(display);
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
        protected boolean hasNativeWindow() {
            return true;
        }

        @Override
        protected void forwardWindowBounds(Rectangle bounds) {
            forwarded.add(bounds);
        }

        @Override
        public void setWindowTitle(DartControl control, String title) {
            // the real title forward is a native call; irrelevant to the resize contract under test
        }
    }

    /** Web (browser) surface bridge; pure-web has no window to forward to. */
    private static final class TestWebBridge extends WebDisplayBridge {
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
            registerWindowControls(display); // wires the WinClose (tab-close) handler
        }
    }
}
