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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A CCombo driven from the keyboard, with every key arriving the way the client sends it: once, on
 * the Display, for Java to route to its focused control. Alt+Down opens the list, an arrow in it
 * selects the next item and fires Selection, Enter fires DefaultSelection and closes it, and Enter
 * on the closed CCombo fires DefaultSelection.
 *
 * <pre>./gradlew :swt-evolve:swt_native:test --tests '*CComboKeyboardFlutterTest'</pre>
 */
@Tag("flutter-it")
class CComboKeyboardFlutterTest {

    private static final String[] ITEMS = {"Alpha", "Bravo", "Charlie", "Delta", "Echo"};

    private Display display;
    private TestWebBridge web;
    private final List<String> events = new ArrayList<>();

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    private CCombo openShell(int style) {
        web = install(TestWebBridge::new);
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        CCombo combo = new CCombo(shell, style);
        combo.setItems(ITEMS);
        combo.select(0);
        combo.addListener(SWT.Selection, e -> events.add("Selection " + combo.getText()));
        combo.addListener(SWT.DefaultSelection, e -> events.add("DefaultSelection " + combo.getText()));
        shell.setSize(400, 300);
        shell.open();
        combo.setFocus();
        clientReports(combo, "Focus/FocusIn", 0);
        return combo;
    }

    @Test
    void readOnlyArrowsInTheOpenListSelectAndEnterCommits() {
        CCombo combo = openShell(SWT.READ_ONLY | SWT.BORDER);

        altDown(combo);
        web.comm.sent.clear();
        press(SWT.ARROW_DOWN, (char) 0, 0);
        assertThat(shipped(combo, "text")).as("the field shows the item the list moved to").isEqualTo("Bravo");
        press(SWT.ARROW_DOWN, (char) 0, 0);

        assertThat(combo.getSelectionIndex()).isEqualTo(2);
        assertThat(combo.getText()).isEqualTo("Charlie");
        assertThat(events).containsExactly("Selection Bravo", "Selection Charlie");

        press(SWT.CR, SWT.CR, 0);

        assertThat(events).endsWith("DefaultSelection Charlie");
        assertThat(combo.getListVisible()).isFalse();
        assertThat(combo.getText()).isEqualTo("Charlie");
    }

    /** A click opens the list in Flutter alone, and Java's focus stays on the field. */
    @Test
    void readOnlyListOpenedByTheClientTakesArrowsAndEnter() {
        CCombo combo = openShell(SWT.READ_ONLY | SWT.BORDER);

        clientReports(combo, "List/Visible", 1);
        press(SWT.ARROW_DOWN, (char) 0, 0);
        press(SWT.ARROW_DOWN, (char) 0, 0);
        press(SWT.CR, SWT.CR, 0);

        assertThat(events).containsExactly("Selection Bravo", "Selection Charlie", "DefaultSelection Charlie");
        assertThat(combo.getListVisible()).isFalse();
    }

    @Test
    void readOnlyEnterOnTheClosedComboFiresDefaultSelection() {
        CCombo combo = openShell(SWT.READ_ONLY | SWT.BORDER);

        press(SWT.CR, SWT.CR, 0);

        assertThat(events).containsExactly("DefaultSelection Alpha");
    }

    @Test
    void editableArrowsInTheOpenListSelectAndEnterCommits() {
        CCombo combo = openShell(SWT.BORDER);

        altDown(combo);
        press(SWT.ARROW_DOWN, (char) 0, 0);
        press(SWT.ARROW_DOWN, (char) 0, 0);
        press(SWT.CR, SWT.CR, 0);

        assertThat(events).containsExactly("Selection Bravo", "Selection Charlie", "DefaultSelection Charlie");
        assertThat(combo.getListVisible()).isFalse();
    }

    @Test
    void editableEnterOnTheClosedComboFiresDefaultSelection() {
        CCombo combo = openShell(SWT.BORDER);

        press(SWT.CR, SWT.CR, 0);

        assertThat(events).containsExactly("DefaultSelection Alpha");
    }

    /** A KeyDown listener that vetoes Enter keeps it from the field, as natively: no DefaultSelection. */
    @Test
    void enterVetoedByAKeyDownListenerFiresNoDefaultSelection() {
        CCombo combo = openShell(SWT.READ_ONLY | SWT.BORDER);
        combo.addListener(SWT.KeyDown, e -> e.doit = false);

        press(SWT.CR, SWT.CR, 0);

        assertThat(events).isEmpty();
    }

    @Test
    void enterThatMakesAKeyDownListenerDisposeTheComboFiresNothingMore() {
        CCombo combo = openShell(SWT.READ_ONLY | SWT.BORDER);
        combo.addListener(SWT.KeyDown, e -> combo.dispose());

        press(SWT.CR, SWT.CR, 0);

        assertThat(combo.isDisposed()).isTrue();
        assertThat(events).isEmpty();
    }

    // ---- harness ----------------------------------------------------------------------------------

    /** Alt+Down opens the list in Java, which tells the client to draw it; the client then says it did. */
    private void altDown(CCombo combo) {
        web.comm.sent.clear();
        press(SWT.ARROW_DOWN, (char) 0, SWT.ALT);
        assertThat(combo.getListVisible()).isTrue();
        assertThat(shipped(combo, "listVisible")).as("the client is told to draw the list").isEqualTo("true");
        clientReports(combo, "List/Visible", 1);
    }

    /** The value of {@code key} in the last record sent for the CCombo itself, or null when none had it. */
    private String shipped(CCombo combo, String key) {
        String value = null;
        for (RecordingComm.Frame frame : web.comm.sent) {
            for (java.util.Map<String, Object> record : dev.equo.swt.harness.WireValues.describing(frame.value, combo.hashCode())) {
                if (record.containsKey(key))
                    value = String.valueOf(record.get(key));
            }
        }
        return value;
    }

    private void press(int keyCode, char character, int stateMask) {
        Event key = new Event();
        key.keyCode = keyCode;
        key.character = character;
        key.stateMask = stateMask;
        web.comm.fireContaining("Display/" + display.hashCode() + "/Key/KeyDown", key);
        flush();
    }

    private void clientReports(Widget widget, String suffix, int detail) {
        Event event = new Event();
        event.detail = detail;
        web.comm.fireContaining("/" + widget.hashCode() + "/" + suffix, event);
        flush();
    }

    private void flush() {
        while (display.readAndDispatch()) {
        }
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
            clientReady.complete(true);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        protected void start(DartDisplay display) {
            registerDisplayClientReady(display);
            registerDisplayKeyEvents(display);
        }
    }
}
