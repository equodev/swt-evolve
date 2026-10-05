package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A {@code NO_TRIM | ON_TOP} popup holding only a vertical {@link ToolBar}, opened with
 * {@code setVisible(true)} and {@code setActive()}: native activates it once and focuses the ToolBar.
 *
 * <pre>./gradlew :swt-evolve:swt_native:test --tests '*PopupToolBarShellActivationFlutterTest'</pre>
 */
@Tag("flutter-it")
class PopupToolBarShellActivationFlutterTest {

    private Display display;
    private TestWebBridge web;
    private Shell popup;
    private ToolBar vertical;
    private final List<String> events = new ArrayList<>();

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    private void openPopup() {
        web = install(TestWebBridge::new);
        Shell main = new Shell(display);
        main.setSize(400, 300);
        new Text(main, SWT.SINGLE);
        main.open();

        popup = new Shell(main, SWT.NO_TRIM | SWT.ON_TOP);
        popup.setLayout(new FillLayout());
        vertical = new ToolBar(popup, SWT.VERTICAL | SWT.FLAT);
        for (String column : new String[]{"Name", "Depth"}) {
            ToolItem check = new ToolItem(vertical, SWT.CHECK);
            check.setText(column);
            check.setSelection(true);
        }
        popup.pack();
        popup.addListener(SWT.Activate, e -> events.add("Activate"));
        popup.addListener(SWT.Deactivate, e -> events.add("Deactivate"));

        popup.setVisible(true);
        popup.setActive();
    }

    private void clientReports(String suffix) {
        web.comm.fireContaining(channel(popup, suffix), new Event());
        while (display.readAndDispatch()) {
        }
    }

    @Test
    void theToolBarTakesTheFocus() {
        openPopup();

        assertThat(display.getFocusControl()).isSameAs(vertical);
    }

    @Test
    void theClientsEchoOfTheActivationIsNotASecondOne() {
        openPopup();

        clientReports("Shell/Activate");

        assertThat(events).containsExactly("Activate");
    }

    @Test
    void aReactivationAfterTheClientDeactivatedItStillArrives() {
        openPopup();
        clientReports("Shell/Activate");

        clientReports("Shell/Deactivate");
        clientReports("Shell/Activate");

        assertThat(events).containsExactly("Activate", "Deactivate", "Activate");
    }

    @Test
    void aShellTheClientAlreadyFocusedStillReportsItsNextActivation() {
        openPopup();
        // No echo comes back here: the client's next activation follows a deactivation.
        clientReports("Shell/Deactivate");
        clientReports("Shell/Activate");

        assertThat(events).containsExactly("Activate", "Deactivate", "Activate");
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
