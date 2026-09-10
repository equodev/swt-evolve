package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.FlutterNative;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins {@code Shell.close()} semantics for the two close gestures a Dart-backed Display can receive:
 * the OS window's title-bar close on the desktop-native surface, and the Flutter-drawn (client-side
 * decoration) close button on any surface.
 *
 * <p>Both must run the same contract: fire {@code SWT.Close} <em>while the window still exists</em>,
 * honor {@code event.doit == false} by keeping shell and window alive (this is what lets an
 * application put up "Save changes before exiting?"), and otherwise dispose the shell and take the
 * window down with it.
 *
 * <p>Like {@link DisplayResizeFlutterTest} this is a {@code @Tag("flutter-it")} test driven by the
 * {@code nativeTest} task, with the native window stubbed behind the bridge's seams so the whole
 * close round trip is observable in the JVM.
 */
@Tag("flutter-it")
class ShellCloseContractFlutterTest {

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

    // ---- the OS window close gesture (desktop-native) ---------------------------------------------

    @Test
    void windowCloseRequest_vetoed_keepsShellAndWindowAlive() {
        TestDeskBridge desk = install(TestDeskBridge::new);
        Shell shell = newMainShell();
        AtomicInteger closes = new AtomicInteger();
        shell.addListener(SWT.Close, e -> {
            closes.incrementAndGet();
            e.doit = false;
        });

        desk.pumpStatus.add(FlutterNative.PUMP_CLOSE_REQUESTED);
        desk.onUpdate();
        drainEventLoop();

        assertThat(closes.get())
                .as("the title-bar close must reach the shell as SWT.Close")
                .isEqualTo(1);
        assertThat(shell.isDisposed())
                .as("doit = false must keep the shell alive")
                .isFalse();
        assertThat(desk.windowDisposals)
                .as("a vetoed close must not tear the native window down")
                .isZero();
        assertThat(desk.hasNativeWindow())
                .as("the window must still be up, or the confirmation dialog has nothing to render into")
                .isTrue();
    }

    @Test
    void windowCloseRequest_accepted_disposesShellAndTearsDownWindow() {
        TestDeskBridge desk = install(TestDeskBridge::new);
        Shell shell = newMainShell();
        AtomicInteger closes = new AtomicInteger();
        shell.addListener(SWT.Close, e -> closes.incrementAndGet());

        desk.pumpStatus.add(FlutterNative.PUMP_CLOSE_REQUESTED);
        desk.onUpdate();
        drainEventLoop();

        assertThat(closes.get()).isEqualTo(1);
        assertThat(shell.isDisposed())
                .as("an accepted close disposes the shell")
                .isTrue();
        assertThat(desk.windowDisposals)
                .as("the last top-level shell going away takes the native window with it")
                .isEqualTo(1);
    }

    @Test
    void windowCloseRequest_isReportedOnce_notOncePerPump() {
        TestDeskBridge desk = install(TestDeskBridge::new);
        Shell shell = newMainShell();
        AtomicInteger closes = new AtomicInteger();
        shell.addListener(SWT.Close, e -> {
            closes.incrementAndGet();
            e.doit = false;
        });

        desk.pumpStatus.add(FlutterNative.PUMP_CLOSE_REQUESTED);
        desk.onUpdate();
        drainEventLoop();
        desk.onUpdate(); // the pump has nothing more to report
        drainEventLoop();

        assertThat(closes.get())
                .as("one close gesture is one SWT.Close, however often the loop pumps afterwards")
                .isEqualTo(1);
    }

    @Test
    void splashShellDisposedBeforeTheMainShellOpens_keepsTheWindow() {
        // An RCP's startup shape: a short-lived top-level shell is disposed before the real shell
        // exists, so for an instant the Display has no top-level shell at all. Taking the window down
        // there strands the application — it runs on with a shell it can never show.
        TestDeskBridge desk = install(TestDeskBridge::new);
        Shell splash = newMainShell();

        splash.dispose();
        drainEventLoop();

        assertThat(desk.windowDisposals)
                .as("a shell disposed outside an accepted close must not take the window with it")
                .isZero();
        assertThat(desk.hasNativeWindow())
                .as("the window must survive to host the main shell that opens next")
                .isTrue();

        Shell main = newMainShell();
        main.setVisible(true);
        assertThat(desk.hasNativeWindow())
                .as("the shell opened after the splash still has a window")
                .isTrue();
    }

    @Test
    void windowAlreadyGone_stillClosesTopLevelShells() {
        TestDeskBridge desk = install(TestDeskBridge::new);
        Shell shell = newMainShell();

        desk.pumpStatus.add(-1); // the window really is gone (crash / OS-forced close)
        desk.onUpdate();
        drainEventLoop();

        assertThat(shell.isDisposed())
                .as("the last-resort path still tears the SWT side down")
                .isTrue();
        assertThat(desk.hasNativeWindow())
                .as("nothing may be forwarded to a window that no longer exists")
                .isFalse();
    }

    // ---- the Flutter-drawn (CSD) close button -----------------------------------------------------

    @Test
    void flutterCloseButton_accepted_disposesShell() {
        TestDeskBridge desk = install(TestDeskBridge::new);
        Shell shell = newMainShell();
        AtomicInteger closes = new AtomicInteger();
        shell.addListener(SWT.Close, e -> closes.incrementAndGet());

        desk.comm.fireContaining("/Shell/Close", new Event());
        drainEventLoop();

        assertThat(closes.get()).isEqualTo(1);
        assertThat(shell.isDisposed())
                .as("the CSD close button must dispose the shell, not just announce the event")
                .isTrue();
    }

    @Test
    void flutterCloseButton_vetoed_keepsShellAlive() {
        TestDeskBridge desk = install(TestDeskBridge::new);
        Shell shell = newMainShell();
        shell.addListener(SWT.Close, e -> e.doit = false);

        desk.comm.fireContaining("/Shell/Close", new Event());
        drainEventLoop();

        assertThat(shell.isDisposed())
                .as("doit = false must veto the CSD close too")
                .isFalse();
    }

    // ---- harness ----------------------------------------------------------------------------------

    private DartDisplay dartDisplay() {
        return (DartDisplay) display.getImpl();
    }

    /** Run the queued UI work (the asyncExec'd shell close) to completion. */
    private void drainEventLoop() {
        while (display.readAndDispatch()) {
            /* drain */
        }
    }

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
        return new Shell(display);
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

    /** Desktop-native bridge with the native window stubbed: a scripted pump and an observable teardown. */
    private static final class TestDeskBridge extends DeskDisplayBridge {
        final RecordingComm comm = new RecordingComm();
        final Deque<Integer> pumpStatus = new ArrayDeque<>();
        int windowDisposals;
        private boolean windowSurface = true;

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
        protected boolean hasWindowSurface() {
            return windowSurface;
        }

        @Override
        protected int pumpWindow() {
            Integer status = pumpStatus.poll();
            return status == null ? 0 : status;
        }

        @Override
        protected void disposeNativeWindow() {
            windowDisposals++;
            windowSurface = false;
        }

        @Override
        public void setWindowTitle(DartControl control, String title) {
        }

        @Override
        protected void forwardWindowBounds(Rectangle bounds) {
        }
    }
}
