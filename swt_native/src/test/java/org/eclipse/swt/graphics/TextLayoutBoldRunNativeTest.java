package org.eclipse.swt.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A styled cell that bolds the matched part of its text (JFace's StyledCellLabelProvider) draws
 * the run after the bold one where the layout measured the bold one to end. A bold face is wider
 * than the regular one, so the bold run has to be measured with bold advances, also in a family
 * the metrics table has no entry of its own for; otherwise the next run is drawn over the bold
 * run's tail and the character after it disappears.
 */
@Tag("native-unit")
class TextLayoutBoldRunNativeTest {

    /** The name the Dart-backed device gives its system font; the metrics table spells it "System". */
    private static final String SYSTEM = "system";

    private static TextLayout layout(Display display, Font font, String text) {
        TextLayout layout = new TextLayout(display);
        layout.setFont(font);
        layout.setText(text);
        return layout;
    }

    @Test
    void textAfterABoldRunStartsPastTheBoldGlyphs() {
        Display display = DartMocks.dartDisplay();
        Font regular = new Font(display, SYSTEM, 11, SWT.NORMAL);
        Font bold = new Font(display, SYSTEM, 11, SWT.BOLD);
        TextLayout plain = layout(display, regular, "Project Explorer (General)");
        TextLayout styled = layout(display, regular, "Project Explorer (General)");
        styled.setStyle(new TextStyle(bold, null, null), 0, 7);
        try {
            assertThat(styled.getLocation(7, false).x)
                    .as("the space after a bold \"Project\"")
                    .isGreaterThan(plain.getLocation(7, false).x);
            assertThat(styled.getBounds().width)
                    .as("the whole line, one bold run longer")
                    .isGreaterThan(plain.getBounds().width);
        } finally {
            plain.dispose();
            styled.dispose();
            regular.dispose();
            bold.dispose();
        }
    }
}
