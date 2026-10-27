package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.NoopBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A click on a CCombo focuses it after the Shell took the focus from a previous one: the Shell's
 * focus request leaves no report to answer it, and the click's report must not be taken for one
 * older than that request.
 *
 * <pre>./gradlew :swt-evolve:swt_native:test --tests '*CComboFocusAfterShellFocusFlutterTest'</pre>
 */
@Tag("flutter-it")
class CComboFocusAfterShellFocusFlutterTest {

    private Display display;
    private TestWebBridge web;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void aClickFocusesACComboRebuiltAfterTheShellTookTheFocus() {
        web = install(TestWebBridge::new);
        Shell shell = new Shell(display);
        shell.setSize(400, 300);
        shell.open();
        CCombo first = newCombo(shell);
        clientReports(first, "Focus/FocusIn");
        assertThat(first.isFocusControl()).as("the first CCombo took the click's focus").isTrue();

        shell.forceFocus();
        pump();
        first.dispose();
        CCombo rebuilt = newCombo(shell);
        shell.forceFocus();
        pump();
        clientReports(rebuilt, "Focus/FocusIn");

        assertThat(rebuilt.isFocusControl()).isTrue();
    }

    private static CCombo newCombo(Shell shell) {
        CCombo combo = new CCombo(shell, SWT.BORDER);
        combo.setBounds(20, 20, 200, 30);
        return combo;
    }

    private void clientReports(Widget widget, String suffix) {
        web.comm.fireContaining("/" + widget.hashCode() + "/" + suffix, new Event());
        pump();
    }

    private void pump() {
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
}
