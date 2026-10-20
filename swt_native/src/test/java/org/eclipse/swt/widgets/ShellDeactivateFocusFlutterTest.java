package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.NoopBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Switching to another application deactivates the window, and the client reports the focused
 * control losing the focus. Natively that is all that happens while the window is in the
 * background: the focus control is saved, and the window's next activation hands it back.
 *
 * <pre>./gradlew :swt-evolve:swt_native:test --tests '*ShellDeactivateFocusFlutterTest'</pre>
 */
@Tag("flutter-it")
class ShellDeactivateFocusFlutterTest {

    private Display display;
    private TestWebBridge web;
    private Shell shell;
    private Canvas first;
    private Canvas second;
    private final List<String> log = new ArrayList<>();

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    /** Two canvases, each inside its own composite, the way a workbench part hosts its control. */
    private void openTwoParts() {
        web = install(TestWebBridge::new);
        shell = new Shell(display);
        shell.setLayout(new FillLayout());
        first = part("first");
        second = part("second");
        shell.addListener(SWT.Activate, e -> log.add("Activate"));
        shell.addListener(SWT.Deactivate, e -> log.add("Deactivate"));
        shell.setSize(600, 300);
        shell.open();
        fire(shell, "Shell/Activate");
        fire(second, "Focus/FocusIn");
        assertThat(display.getFocusControl()).as("precondition: the clicked canvas holds the focus").isSameAs(second);
        log.clear();
    }

    private Canvas part(String name) {
        Composite part = new Composite(shell, SWT.NONE);
        part.setLayout(new FillLayout());
        part.addListener(SWT.Activate, e -> log.add("Activate part " + name));
        part.addListener(SWT.Deactivate, e -> log.add("Deactivate part " + name));
        Canvas canvas = new Canvas(part, SWT.BORDER);
        canvas.addListener(SWT.FocusIn, e -> log.add("FocusIn " + name));
        canvas.addListener(SWT.FocusOut, e -> log.add("FocusOut " + name));
        return canvas;
    }

    /** What the client reports when the user switches to another application. */
    private void switchToAnotherApplication() {
        web.comm.fireContaining(channel(second, "Focus/FocusOut"), new Event());
        web.comm.fireContaining(channel(shell, "Shell/Deactivate"), new Event());
        dispatch();
    }

    @Test
    void deactivatingTheWindowMovesNoFocusInsideIt() {
        openTwoParts();

        switchToAnotherApplication();

        assertThat(log)
                .as("the background window gets no focus or part activation")
                .doesNotContain("FocusIn first", "Activate part first")
                .contains("FocusOut second", "Deactivate");
    }

    @Test
    void activatingTheWindowAgainRestoresTheFocusItHad() {
        openTwoParts();
        switchToAnotherApplication();
        log.clear();

        fire(shell, "Shell/Activate");

        assertThat(display.getFocusControl()).as("the focus returns to the control that had it").isSameAs(second);
        assertThat(log).doesNotContain("FocusIn first", "Activate part first");
    }

    @Test
    void theClientDeactivateOfAShellJavaAlreadyDeactivatedSendsNoSecondDeactivate() {
        openTwoParts();
        Shell other = new Shell(display);
        other.setSize(200, 100);
        other.open();
        dispatch();
        assertThat(log).as("precondition: opening another shell deactivated this one").containsOnlyOnce("Deactivate");
        log.clear();

        fire(shell, "Shell/Deactivate");

        assertThat(log).doesNotContain("Deactivate", "FocusIn first", "Activate part first");
    }

    // ---- harness ----------------------------------------------------------------------------------

    private void fire(Widget widget, String suffix) {
        web.comm.fireContaining(channel(widget, suffix), new Event());
        dispatch();
    }

    private void dispatch() {
        while (display.readAndDispatch()) {
        }
    }

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
