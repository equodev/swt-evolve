package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * There is no OS focus on the whole-tree surface, so an activation the client reports has to name
 * the control {@link Display#getFocusControl()} answers with. Without that, clicking into a popup
 * left the display with no focus holder at all: the previous control's FocusOut cleared it and
 * nothing took its place, a state native SWT never reaches while a shell is active.
 *
 * <p>An autocomplete popup is the shape that breaks on it — a {@code Text} with an
 * {@link SWT#ON_TOP} shell carrying the proposal {@link Table}, which hides itself when focus
 * leaves both. Reading "nobody is focused" as "focus left me", it hid on the very click meant to
 * pick a proposal, and the pick then found the popup already invisible and did nothing.
 *
 * <pre>./gradlew :swt-evolve:swt_native:nativeTest</pre>
 */
@Tag("flutter-it")
class PopupShellFocusHolderFlutterTest {

    private Display display;
    private TestWebBridge web;
    private Shell main;
    private Text field;
    private Shell popup;
    private Table proposals;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    /** The widget tree an autocompleter builds: a field, and an ON_TOP shell holding the proposals. */
    private void openAutocompleter() {
        web = install(TestWebBridge::new);
        main = new Shell(display);
        main.setSize(400, 300);
        main.open();
        field = new Text(main, SWT.SINGLE);
        popup = new Shell(main, SWT.ON_TOP);
        proposals = new Table(popup, SWT.VIRTUAL);
        new TableColumn(proposals, SWT.NONE);
        proposals.setItemCount(3);
        popup.setBounds(0, 20, 200, 60);
        popup.setVisible(true);
        web.setFocus((DartControl) field.getImpl());
    }

    /** The click the client reports when the pointer goes down inside the popup. */
    private void clientClicksIntoThePopup() {
        web.comm.fireContaining(channel(field, "Focus/FocusOut"), new Event());
        web.comm.fireContaining(channel(popup, "Shell/Activate"), new Event());
        while (display.readAndDispatch()) {
        }
    }

    @Test
    void activatingAPopupNamesAFocusHolderInsideIt() {
        openAutocompleter();
        assertThat(display.getFocusControl()).as("precondition: the field holds the focus").isSameAs(field);

        clientClicksIntoThePopup();

        assertThat(display.getFocusControl())
                .as("the proposal table is the focus holder once its shell is active")
                .isSameAs(proposals);
    }

    @Test
    void aPopupThatClosesOnLostFocusSurvivesAClickInsideIt() {
        openAutocompleter();

        clientClicksIntoThePopup();

        // The autocompleter's own rule, verbatim: focus is gone unless it sits on the field or on
        // the proposals.
        Control focus = display.getFocusControl();
        if (focus == null || (focus != field && focus != proposals))
            popup.setVisible(false);

        assertThat(popup.isVisible())
                .as("the popup stays open for the click that picks a proposal")
                .isTrue();
    }

    @Test
    void aFieldStillOutranksAnEmptyContainer() {
        web = install(TestWebBridge::new);
        Shell dialog = new Shell(display);
        dialog.setSize(400, 300);
        // A spacer ahead of the real content: only a container with nothing focusable anywhere in
        // the shell may claim the focus, or a layout filler would take it from the first field.
        new Composite(dialog, SWT.NONE);
        Text first = new Text(dialog, SWT.SINGLE);
        dialog.open();

        assertThat(((DartShell) dialog.getImpl()).focusFirstFocusable()).isTrue();
        assertThat(display.getFocusControl())
                .as("the first field takes the focus, not the empty spacer")
                .isSameAs(first);
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
