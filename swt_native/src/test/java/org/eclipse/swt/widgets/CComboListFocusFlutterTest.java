package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.NoopBridge;
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

    @Test
    void aPressGivesTheFieldTheFocusBeforeTheMouseDown() {
        openShell();
        view.setFocus();
        Control[] focusAtPress = new Control[1];
        combo.addListener(SWT.MouseDown, e -> focusAtPress[0] = display.getFocusControl());

        Event press = new Event();
        press.button = 3;
        press.count = 1;
        web.comm.fireContaining(channel(combo, "Mouse/MouseDown"), press);
        while (display.readAndDispatch()) {
        }

        // The field holds it, as natively: the keys then reach the CCombo through its field.
        assertThat(focusAtPress[0]).isInstanceOf(Text.class).isNotSameAs(view);
        assertThat(focusAtPress[0].getParent()).isSameAs(combo);
    }

    @Test
    void theClientFocusingTheCComboItsFieldHoldsIsNoChange() {
        openShell();
        view.setFocus();
        Event press = new Event();
        press.button = 1;
        press.count = 1;
        web.comm.fireContaining(channel(combo, "Mouse/MouseDown"), press);
        while (display.readAndDispatch()) {
        }
        Control field = display.getFocusControl();
        java.util.List<String> events = new java.util.ArrayList<>();
        combo.addListener(SWT.FocusIn, e -> events.add("CCombo.FocusIn"));
        field.addListener(SWT.FocusOut, e -> events.add("field.FocusOut"));

        // The client's own report of the focus the press gave the CCombo.
        clientReports(combo, "Focus/FocusIn", 0);

        assertThat(display.getFocusControl()).isSameAs(field);
        assertThat(events).isEmpty();
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

}
