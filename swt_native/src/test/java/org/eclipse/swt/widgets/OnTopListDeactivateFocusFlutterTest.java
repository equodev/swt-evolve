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
 * A part stack's chevron opens its part list as an ON_TOP shell whose filter field takes the focus,
 * and the list closes on Deactivate unless, one asyncExec later, the focus is still one of its
 * controls. Natively the list's controls have lost the focus by then. A click outside the list, on
 * a part body that takes no focus itself, makes the client report the list's Deactivate and its
 * field's FocusOut together; the parent's activation comes in a later batch.
 *
 * <pre>./gradlew :swt-evolve:swt_native:test --tests '*OnTopListDeactivateFocusFlutterTest'</pre>
 */
@Tag("flutter-it")
class OnTopListDeactivateFocusFlutterTest {

    private Display display;
    private TestWebBridge web;
    private Shell shell;
    private Shell list;
    private Text filter;
    private final List<Control> focusSeenOnDeactivate = new ArrayList<>();

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    /** The part list as the workbench opens it: ON_TOP, force-activated, the filter field focused. */
    private void openList() {
        web = install(TestWebBridge::new);
        shell = new Shell(display);
        shell.setLayout(new FillLayout());
        new Composite(shell, SWT.NONE);
        shell.setSize(600, 400);
        shell.open();
        fire(shell, "Shell/Activate");

        list = new Shell(shell, SWT.ON_TOP);
        list.setLayout(new FillLayout());
        Composite body = new Composite(list, SWT.NONE);
        body.setLayout(new FillLayout());
        filter = new Text(body, SWT.SINGLE);
        list.setBounds(100, 50, 220, 240);
        list.setVisible(true);
        list.forceActive();
        list.forceFocus();
        filter.setFocus();
        fire(list, "Shell/Activate");
        fire(filter, "Focus/FocusIn");
        list.addListener(SWT.Deactivate, e -> display.asyncExec(() -> {
            if (list.isDisposed())
                return;
            Control focus = display.getFocusControl();
            focusSeenOnDeactivate.add(focus);
            if (focus == null || focus.getShell() != list)
                list.dispose();
        }));
        assertThat(display.getFocusControl()).as("precondition: the filter field holds the focus").isSameAs(filter);
    }

    @Test
    void anOutsideClickClosesTheList() {
        openList();

        web.comm.fireContaining(channel(list, "Shell/Deactivate"), new Event());
        web.comm.fireContaining(channel(filter, "Focus/FocusOut"), new Event());
        dispatch();

        assertThat(focusSeenOnDeactivate)
                .as("the focus the list's Deactivate listener reads, one asyncExec later")
                .hasSize(1)
                .doesNotContain(filter);
        assertThat(list.isDisposed()).as("the list closed").isTrue();
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
