package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A CCombo whose selection moves the focus to another control, as a workbench contribution that
 * activates a view does: natively the list closes with the focus loss, and opening it again focuses
 * the CCombo, so its arrow keys reach it.
 *
 * <pre>./gradlew :swt-evolve:swt_native:test --tests '*CComboListFocusFlutterTest'</pre>
 */
@Tag("flutter-it")
class CComboListFocusFlutterTest {

    private Display display;
    private TestWebBridge web;
    private CCombo combo;
    private Text view;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    private void openShell() {
        web = install(TestWebBridge::new);
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        combo = new CCombo(shell, SWT.READ_ONLY | SWT.BORDER);
        combo.setItems(new String[]{"Faults", "Frameworks", "Horizons"});
        view = new Text(shell, SWT.MULTI);
        shell.setSize(400, 300);
        shell.open();
        clientReports(combo, "Focus/FocusIn", 0);
    }

    private void clientReports(Widget widget, String suffix, int detail) {
        Event event = new Event();
        event.detail = detail;
        web.comm.fireContaining(channel(widget, suffix), event);
        while (display.readAndDispatch()) {
        }
    }

    @Test
    void theListStaysOpenWhileTheFocusMovesWithinTheCCombo() {
        openShell();
        view.setFocus();

        clientReports(combo, "List/Visible", 1);

        assertThat(combo.getListVisible()).isTrue();
    }

    @Test
    void theListClosesWhenTheApplicationMovesTheFocusAway() {
        openShell();
        clientReports(combo, "List/Visible", 1);
        assertThat(combo.getListVisible()).isTrue();

        view.setFocus();

        assertThat(display.getFocusControl()).isSameAs(view);
        assertThat(combo.getListVisible()).isFalse();
    }

    @Test
    void openingTheListAgainFocusesTheCCombo() {
        openShell();
        clientReports(combo, "List/Visible", 1);
        view.setFocus();
        clientReports(combo, "List/Visible", 0);

        clientReports(combo, "List/Visible", 1);

        // The CCombo or one of its parts (its list, natively) holds the focus, not the view.
        assertThat(combo.isFocusControl()).isTrue();
        assertThat(display.getFocusControl()).isNotSameAs(view);
    }

    // ---- harness ----------------------------------------------------------------------------------

    private static String channel(Widget widget, String suffix) {
        return "/" + widget.hashCode() + "/" + suffix;
    }

    /** See {@code ControlFocusRequestFlutterTest.install} — a Display whose bridge is the test bridge. */
    private <B extends DisplayBridge> B install(Function<DartDisplay, B> factory) {
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        B bridge = factory.apply(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
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

    private static final class TestWebBridge extends WebDisplayBridge {
        final RecordingComm comm = new RecordingComm();

        TestWebBridge(DartDisplay display) {
            super(display);
            // Stands in for the client having connected: update() defers every pending send until then.
            clientReady.complete(true);
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
}
