package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression coverage for issue #526's follow-up: opening a secondary Shell (Katalon's Web
 * Recorder dialog, which never calls pack()/setSize() itself) must not shrink the main shell, and
 * a same-geometry setBounds()/setLocation() on a Chromium-standalone main window must not reach
 * the native window.
 */
@Tag("flutter-it")
class WebRecorderShrinkFlutterTest {

    private Display display;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
        System.clearProperty("dev.equo.swt.mode");
    }

    private DartDisplay dartDisplay() {
        return (DartDisplay) display.getImpl();
    }

    private <B extends DisplayBridge> B install(java.util.function.Function<DartDisplay, B> factory) {
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = dartDisplay();
        B bridge = factory.apply(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

    private static void clientReady(RecordingComm comm, int width, int height, boolean isFirst) {
        ClientReadyPayload p = new ClientReadyPayload();
        p.width = width;
        p.height = height;
        p.isFirst = isFirst;
        comm.fireContaining("ClientReady", p);
    }

    @Test
    void secondaryDialog_neverCallingSetSize_doesNotShrinkMainShell() {
        TestWebBridge web = install(TestWebBridge::new);
        Shell main = new Shell(display);
        clientReady(web.comm, 1728, 1009, true);
        main.open();

        assertThat(main.getBounds())
                .as("sanity: main shell fills the viewport before the recorder opens")
                .isEqualTo(new Rectangle(0, 0, 1728, 1009));

        // Mimic Katalon's WebRecorderDialog: a secondary Shell owned by main, never sized/packed,
        // just opened.
        Shell recorder = new Shell(main, SWT.SHELL_TRIM);
        recorder.open();

        assertThat(main.getBounds())
                .as("opening the recorder dialog must not shrink the main shell")
                .isEqualTo(new Rectangle(0, 0, 1728, 1009));
        assertThat(recorder.getBounds())
                .as("the recorder must get its own sane default size, not the full display bounds "
                        + "(the original bug: a secondary shell clobbered to full size races its "
                        + "first Flutter frame and renders blank)")
                .isEqualTo(new Rectangle(0, 0, 1036, 605));
    }

    @Test
    void parentlessSecondaryShell_explicitBounds_doesNotShrinkMainShellOrReachNativeWindow() {
        // A parentless secondary Shell (e.g. Katalon's "Mobile Recorder", owned by the Display
        // directly, just like the main shell) that sizes itself via setBounds() must not be
        // mistaken for the window-driving shell.
        TestChromiumWebBridge web = install(TestChromiumWebBridge::new);
        Shell main = new Shell(display);
        clientReady(web.comm, 1728, 1001, true);
        main.open();
        web.forwarded.clear();

        Shell recorder = new Shell(display, SWT.SHELL_TRIM); // no parent — like Katalon's Mobile Recorder
        recorder.open();
        recorder.setBounds(569, 288, 590, 425); // Katalon sizes its own dialog explicitly

        assertThat(web.forwarded)
                .as("a secondary shell's own dialog geometry must never reach the native/Chromium window")
                .isEmpty();
        assertThat(main.getBounds())
                .as("opening/sizing the secondary shell must not shrink the main shell")
                .isEqualTo(new Rectangle(0, 0, 1728, 1001));

        assertThat(recorder.getBounds())
                .as("the recorder keeps the geometry Katalon gave it")
                .isEqualTo(new Rectangle(569, 288, 590, 425));
    }

    @Test
    void e4MainShell_recognizedOverEarlierTransientShell() {
        // A transient shell (e.g. an e4 splash screen) alive before the real e4 workbench window
        // must not be mistaken for the main shell just for having been created first.
        TestWebBridge web = install(TestWebBridge::new);

        Shell splash = new Shell(display); // parentless, non-modal, non-TOOL, plain layout
        Shell workbench = new Shell(display);
        workbench.setLayout(new org.eclipse.e4.ui.workbench.renderers.swt.TrimmedPartLayout());

        clientReady(web.comm, 1728, 1001, true);
        splash.open();
        workbench.open();

        assertThat(workbench.getBounds())
                .as("the workbench shell must fill the viewport, not the earlier splash shell")
                .isEqualTo(new Rectangle(0, 0, 1728, 1001));
    }

    @Test
    void redundantSetLocation_onChromiumMainWindow_isNotForwarded() {
        // Mirrors Katalon's DialogUtil.createDialogShell(), which calls setLocation(x, y) on the
        // active (main) shell with the x/y it already has while centering an unrelated dialog.
        TestChromiumWebBridge web = install(TestChromiumWebBridge::new);
        Shell main = new Shell(display);
        clientReady(web.comm, 1728, 1003, true);
        main.open();
        web.forwarded.clear();

        main.setLocation(main.getBounds().x, main.getBounds().y); // exactly what DialogUtil does

        assertThat(web.forwarded)
                .as("a setLocation() call that changes nothing must not reach the native window")
                .isEmpty();
        assertThat(main.getBounds())
                .as("the shell's own bounds are obviously unaffected by a same-position setLocation")
                .isEqualTo(new Rectangle(0, 0, 1728, 1003));

        main.setBounds(0, 0, 1000, 700); // a genuine, different geometry must still go through

        assertThat(web.forwarded)
                .as("a real app-driven resize must still reach the native window")
                .containsExactly(new Rectangle(0, 0, 1000, 700));
    }

    /** Chromium-standalone-mode web bridge with the native window stubbed so forwarding is observable. */
    private static final class TestChromiumWebBridge extends WebDisplayBridge {
        final RecordingComm comm = new RecordingComm();
        final List<Rectangle> forwarded = new CopyOnWriteArrayList<>();

        TestChromiumWebBridge(DartDisplay display) {
            super(display);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        protected void start(DartDisplay display) {
            registerDisplayClientReady(display);
            registerWindowControls(display);
        }

        @Override
        protected boolean hasNativeWindow() {
            return true;
        }

        @Override
        protected void forwardWindowBounds(Rectangle bounds) {
            forwarded.add(bounds);
        }
    }

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
            registerWindowControls(display);
        }
    }
}
