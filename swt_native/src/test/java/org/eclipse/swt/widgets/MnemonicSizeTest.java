package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A label, a button, a CLabel and a link draw their text without the {@code '&'} that marks a mnemonic,
 * so each is sized for the text it draws: "&amp;Save" is as wide as "Save", and "&amp;&amp;Save" as wide as "&amp;Save".
 */
@Tag("native-unit")
class MnemonicSizeTest {

    private static final String LONG_TEXT = "one two three four five six seven eight nine ten eleven twelve";

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static Point natural(Control control) {
        return control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    }

    @Test
    @DisplayName("a label is sized for the text it draws, not for its mnemonic marker")
    void labelIgnoresTheMarker() {
        Shell shell = DartMocks.dartShell();
        Label label = new Label(shell, SWT.NONE);
        label.setText("Save");
        Point plain = natural(label);
        label.setText("&Save");
        assertThat(natural(label)).isEqualTo(plain);
        label.setText("Sa&ve");
        assertThat(natural(label)).isEqualTo(plain);
        label.setText("&&Save");
        assertThat(natural(label).x).isGreaterThan(plain.x);
    }

    @Test
    @DisplayName("a button is sized for the text it draws, not for its mnemonic marker")
    void buttonIgnoresTheMarker() {
        Shell shell = DartMocks.dartShell();
        for (int type : new int[] { SWT.PUSH, SWT.CHECK, SWT.RADIO, SWT.TOGGLE }) {
            Button button = new Button(shell, type);
            button.setText("Save");
            Point plain = natural(button);
            button.setText("&Save");
            assertThat(natural(button)).as("type %d", type).isEqualTo(plain);
            button.setText("&&Save");
            assertThat(natural(button).x).as("type %d", type).isGreaterThan(plain.x);
        }
    }

    @Test
    @DisplayName("a CLabel and a Link are sized for the text they draw, not for its mnemonic marker")
    void clabelAndLinkIgnoreTheMarker() {
        Shell shell = DartMocks.dartShell();
        org.eclipse.swt.custom.CLabel clabel = new org.eclipse.swt.custom.CLabel(shell, SWT.NONE);
        clabel.setText("Save");
        Point plainCLabel = natural(clabel);
        clabel.setText("&Save");
        assertThat(natural(clabel)).isEqualTo(plainCLabel);
        clabel.setText("&&Save");
        assertThat(natural(clabel).x).isGreaterThan(plainCLabel.x);

        Link link = new Link(shell, SWT.NONE);
        link.setText("<a>Save</a> now");
        Point plainLink = natural(link);
        link.setText("<a>&Save</a> now");
        assertThat(natural(link)).isEqualTo(plainLink);
    }

    @Test
    @DisplayName("a GC measures a trailing marker as not drawn, like any other")
    void gcDropsATrailingMarker() {
        Shell shell = DartMocks.dartShell();
        org.eclipse.swt.graphics.GC gc = new org.eclipse.swt.graphics.GC(shell);
        try {
            assertThat(gc.textExtent("Save&", SWT.DRAW_MNEMONIC)).isEqualTo(gc.textExtent("Save", SWT.DRAW_MNEMONIC));
            assertThat(gc.textExtent("&File &Edit", SWT.DRAW_MNEMONIC)).isEqualTo(gc.textExtent("File Edit", SWT.DRAW_MNEMONIC));
        } finally {
            gc.dispose();
        }
    }

    @Test
    @DisplayName("wrapped text wraps at the same places with or without markers")
    void wrappedTextIgnoresTheMarkers() {
        Shell shell = DartMocks.dartShell();
        Label plain = new Label(shell, SWT.WRAP);
        plain.setText(LONG_TEXT);
        Label marked = new Label(shell, SWT.WRAP);
        marked.setText(LONG_TEXT.replace("one", "&one").replace("six", "&six").replace("ten", "&ten"));
        for (int width : new int[] { 60, 100, 160 })
            assertThat(marked.computeSize(width, SWT.DEFAULT)).as("width hint %d", width)
                    .isEqualTo(plain.computeSize(width, SWT.DEFAULT));
    }
}
