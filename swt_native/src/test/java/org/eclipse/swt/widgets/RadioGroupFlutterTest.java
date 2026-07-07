package org.eclipse.swt.widgets;

import dev.equo.swt.harness.WidgetFlutterHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Full-stack reproduction of issue #597 (a radio group must keep one selection): drives the real
 * SWT → bridge → Flutter path through {@link WidgetFlutterHarness} and reads back the
 * <em>rendered</em> selection from a live Flutter web client for each radio.
 *
 * <p>Run via the {@code nativeTest} task, which compiles this against the whole-tree-Flutter Java
 * backend ({@code src/main + src/native + src/native<currentOs>}) — so the real {@code native} tree
 * {@code DartButton.selectRadio()} runs. Widgets are real Dart-backed SWT widgets (web
 * {@code Display}/{@code Shell} construct headlessly, no native handles).
 *
 * <p>In {@code org.eclipse.swt.widgets} so it can call the package-private {@code selectRadio()} —
 * the group logic a radio Selection event triggers on every platform.
 *
 * <pre>./gradlew :swt-evolve:swt_native:nativeTest</pre>
 */
@Tag("flutter-it")
class RadioGroupFlutterTest {

    private WidgetFlutterHarness flutter;
    private Display display;
    private Shell shell;

    @BeforeEach
    void setUp() {
        flutter = new WidgetFlutterHarness();
        flutter.init(); // wire the harness as the global bridge BEFORE creating widgets
        display = new Display();
        shell = new Shell(display);
        shell.setLayout(new FillLayout());
        shell.setSize(400, 300);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        if (flutter != null) flutter.teardown();
    }

    private void select(Button radio) {
        ((DartButton) radio.getImpl()).selectRadio();
        flutter.flush();
    }

    @Test
    @DisplayName("only one radio is rendered selected at a time (real Flutter)")
    void exclusiveSelectionRendersCorrectly() {
        Composite group = new Composite(shell, SWT.NONE);
        Button r1 = new Button(group, SWT.RADIO); r1.setText("Option 1");
        Button r2 = new Button(group, SWT.RADIO); r2.setText("Option 2");
        Button r3 = new Button(group, SWT.RADIO); r3.setText("Option 3");

        shell.open();
        shell.layout(true, true);
        flutter.show(shell);

        select(r1);
        assertOnlySelected(r1, r1, r2, r3);

        select(r2);
        assertOnlySelected(r2, r1, r2, r3);

        select(r3);
        assertOnlySelected(r3, r1, r2, r3);
    }

    private void assertOnlySelected(Button expected, Button... all) {
        for (Button b : all) {
            assertThat(flutter.renderedSelection(b))
                    .as("Flutter-rendered selection of %s (expected only %s selected)",
                            b.getText(), expected.getText())
                    .isEqualTo(b == expected);
        }
    }
}
