package org.eclipse.swt.widgets;

import com.google.gson.JsonElement;
import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A master-detail form sets the selected record's value into a Text that keeps the focus. After the user
 * edits record A's value, showing record B and then A again shows A's edited value, not B's.
 */
@Tag("flutter-it")
class TextSetTextAfterEditFlutterTest {

    private static final String A = "078 902 47 15";
    private static final String B = "076 318 44 70";

    private final WebDisplayHarness flutter = new WebDisplayHarness();
    private Display display;
    private Text text;

    @BeforeEach
    void open() {
        display = flutter.boot();
        Shell shell = new Shell(display);
        shell.setLayout(new RowLayout(SWT.VERTICAL));
        text = new Text(shell, SWT.BORDER);
        text.setText(A);
        shell.setSize(300, 200);
        shell.open();
        flutter.settle();
        double[] rect = flutter.renderedRect(text);
        flutter.input().click(rect[0] + rect[2] - 4, rect[1] + rect[3] / 2);
        flutter.settle();
        assertThat(display.getFocusControl()).isSameAs(text);
    }

    @AfterEach
    void shutdown() {
        flutter.teardown();
    }

    @Test
    void anEditedValueSetBackAfterAnotherIsShown() {
        flutter.input().type("9");
        flutter.settle();
        String edited = text.getText();
        assertThat(edited).isEqualTo(A + "9");

        text.setText(B);
        flutter.settle();
        assertThat(shownText()).isEqualTo(B);

        text.setText(edited);
        flutter.settle();
        assertThat(shownText()).isEqualTo(edited);
        assertThat(display.getFocusControl()).isSameAs(text);
    }

    /** The value of the browser's editing element, which the focused field keeps in step with what it shows. */
    private String shownText() {
        JsonElement value = flutter.devTools().evaluate(
                "(() => { const e = document.querySelector('flt-text-editing-host input, input, textarea');"
                        + " return e ? e.value : null; })()");
        return value == null || value.isJsonNull() ? null : value.getAsString();
    }
}
