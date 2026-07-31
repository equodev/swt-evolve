package org.eclipse.swt.widgets;

import dev.equo.swt.harness.WidgetFlutterHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * #465 — the Command Palette (a JFace Dialog) did not close on Escape under the Flutter backend.
 * Dart controls have no OS window proc, so nothing translated an incoming KeyDown into the
 * {@link SWT#Traverse} event the dialog's Escape listener relies on. The fix lives in
 * {@link ControlHelper#sendFlutterKeyDown}: it now runs the native-style traversal walk, and for
 * an Escape observed at the shell (its Flutter focus scope caught the key because no child holds
 * Flutter focus) it starts the walk at the shell's focus control — where the palette's Traverse
 * listener actually sits.
 *
 * <pre>./gradlew :swt-evolve:swt_native:nativeTest</pre>
 */
@Tag("flutter-it")
class CommandPaletteEscapeFlutterTest {

    private WidgetFlutterHarness flutter;
    private Display display;
    private Shell palette;
    private Text search;
    private final boolean[] escaped = { false };

    @BeforeEach
    void setUp() {
        flutter = new WidgetFlutterHarness();
        flutter.init();
        display = new Display();
        // A palette-like shell: a search field carrying the Escape-to-close Traverse listener
        // (JFace Dialog default), on the field rather than the shell itself.
        palette = new Shell(display, SWT.SHELL_TRIM);
        palette.setLayout(new FillLayout());
        search = new Text(palette, SWT.SINGLE | SWT.BORDER);
        search.addListener(SWT.Traverse, e -> {
            if (e.detail == SWT.TRAVERSE_ESCAPE) escaped[0] = true;
        });
        palette.setSize(300, 200);
        flutter.show(palette);
        palette.open();
        search.setFocus();
        // The headless harness has no live focus round-trip (its bridge doesn't track focus), so
        // mark the search field as the display's focus control directly — the state an Rcp App's
        // forceFocus produces in the running app, where DisplayBridge tracks it.
        ((DartDisplay) display.getImpl()).focusControl = search;
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        if (flutter != null) flutter.teardown();
    }

    private void sendFlutterEscape(Control target) {
        Event e = new Event();
        e.keyCode = SWT.ESC;
        e.character = SWT.ESC;
        ControlHelper.sendFlutterKeyDown((DartWidget) target.getImpl(), e);
        while (display.readAndDispatch()) {
        }
    }

    @Test
    @DisplayName("Escape forwarded by the focused search field fires TRAVERSE_ESCAPE on it")
    void escapeFromFocusedControlFiresTraverse() {
        escaped[0] = false;
        sendFlutterEscape(search);
        assertThat(escaped[0]).as("TRAVERSE_ESCAPE reached the search field").isTrue();
    }

    @Test
    @DisplayName("Escape caught at the shell starts the walk at the focus control and fires TRAVERSE_ESCAPE")
    void escapeAtShellRedirectsToFocusControl() {
        assertThat(display.getFocusControl())
            .as("precondition: the search field is the tracked focus control")
            .isSameAs(search);
        escaped[0] = false;
        sendFlutterEscape(palette);
        assertThat(escaped[0])
            .as("shell-level Escape walked from the focused search field, not the shell alone")
            .isTrue();
    }
}
