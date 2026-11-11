package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A FocusIn the client sent before it applied a hide names a control Java hid, such as the content
 * of an editor whose tab another editor replaced. A hidden control never holds the focus in SWT, so
 * its FocusIn neither takes the focus nor activates its part.
 */
@Tag("flutter-it")
class ClientFocusOnHiddenControlFlutterTest {

    private static final String FOCUS_CHANNEL = "swt.evolve.focus";

    private Display display;
    private TestWebBridge web;
    private Composite scriptPart;
    private StyledText script;
    private Canvas grid;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void aFocusInOnAControlJavaHidLeavesTheFocusWhereJavaPutIt() {
        openEditorOverScript();
        List<String> scriptEvents = recordScriptEvents();
        web.comm.sent.clear();

        reportFocusIn(script);

        assertThat(display.getFocusControl())
                .as("the focus stays on the control Java focused")
                .isSameAs(grid);
        assertThat(scriptEvents)
                .as("the hidden script neither takes the focus nor activates its part")
                .isEmpty();
        assertThat(focusRequests())
                .as("the client's focus goes back to the focus control")
                .containsExactly(String.valueOf(FlutterBridge.id((DartWidget) grid.getImpl())));
    }

    @Test
    void aFocusInOnTheControlShownAgainTakesTheFocus() {
        openEditorOverScript();
        reportFocusIn(script);
        // The client follows the request back to the grid, then the user shows the script and clicks it.
        reportFocusIn(grid);
        scriptPart.setVisible(true);
        List<String> scriptEvents = recordScriptEvents();

        reportFocusIn(script);

        assertThat(display.getFocusControl()).isSameAs(script);
        assertThat(scriptEvents).containsExactly("Activate", "FocusIn");
    }

    @Test
    void aFocusOnTheControlOnceShownReachesTheClientWhenNothingHeldTheFocus() {
        web = install();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        scriptPart = new Composite(shell, SWT.NONE);
        script = new StyledText(scriptPart, SWT.NONE);
        shell.setSize(600, 300);
        // Reported while its shell is not open yet: there is no focus control to send the client back to.
        assertThat(display.getFocusControl()).isNull();
        reportFocusIn(script);
        web.comm.sent.clear();

        shell.open();

        assertThat(display.getFocusControl()).isSameAs(script);
        assertThat(focusRequests())
                .as("the client is asked to focus the script")
                .containsExactly(String.valueOf(FlutterBridge.id((DartWidget) script.getImpl())));
    }

    /** The user works in the script, then opens an editor from the navigator, which replaces it. */
    private void openEditorOverScript() {
        web = install();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        scriptPart = new Composite(shell, SWT.NONE);
        script = new StyledText(scriptPart, SWT.NONE);
        Composite navigatorPart = new Composite(shell, SWT.NONE);
        Tree navigator = new Tree(navigatorPart, SWT.NONE);
        Composite editorPart = new Composite(shell, SWT.NONE);
        grid = new Canvas(editorPart, SWT.NONE);
        grid.addListener(SWT.KeyDown, e -> { });
        shell.setSize(600, 300);
        shell.open();

        reportFocusIn(script);
        reportFocusIn(navigator);

        // Selecting the new editor's tab hides the script's part; the editor focuses its grid.
        scriptPart.setVisible(false);
        assertThat(grid.setFocus()).isTrue();
        assertThat(display.getFocusControl()).isSameAs(grid);
    }

    private List<String> recordScriptEvents() {
        List<String> events = new ArrayList<>();
        script.addListener(SWT.FocusIn, e -> events.add("FocusIn"));
        scriptPart.addListener(SWT.Activate, e -> events.add("Activate"));
        return events;
    }

    private void reportFocusIn(Control control) {
        web.comm.fireContaining(FlutterBridge.event((DartWidget) control.getImpl(), "Focus", "FocusIn"), new Event());
        while (display.readAndDispatch()) {
        }
    }

    /** The control ids named by every {@code swt.evolve.focus} frame sent so far, in order. */
    private List<String> focusRequests() {
        return web.comm.sent.stream()
                .filter(f -> f.event.equals(FOCUS_CHANNEL))
                .map(f -> String.valueOf(((Number) dev.equo.swt.harness.WireValues.at(f.value, "id")).longValue()))
                .toList();
    }

    private TestWebBridge install() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        TestWebBridge bridge = new TestWebBridge(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }
}
