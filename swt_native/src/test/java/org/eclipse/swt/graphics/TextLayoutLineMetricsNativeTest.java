package org.eclipse.swt.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledTextHelper;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@code StyledText.getBaseline(int)} answers from {@code TextLayout.getLineMetrics(int)} whenever
 * the line height is not fixed — which is the case under word wrap. JFace's
 * {@code LineNumberRulerColumn} reads that baseline for every number it paints, so metrics that do
 * not describe the line take the whole ruler paint down with them.
 */
@Tag("native-unit")
public class TextLayoutLineMetricsNativeTest {

    private static final String TEXT = "The quick brown fox jumps over the lazy dog";
    private static final int FONT_SIZE = 12;

    private static Font font(Display display) {
        return new DartFont(display, "System", FONT_SIZE, SWT.NORMAL, null).getApi();
    }

    private static TextLayout layout(Display display, Font font, String text) {
        TextLayout layout = new TextLayout(display);
        layout.setFont(font);
        layout.setText(text);
        return layout;
    }

    @Test
    public void line_metrics_describe_the_line() {
        Display display = DartMocks.dartDisplay();
        TextLayout layout = layout(display, font(display), TEXT);

        FontMetrics metrics = layout.getLineMetrics(0);

        assertThat(metrics).isNotNull();
        assertThat(metrics.getAscent()).isGreaterThan(0);
        assertThat(metrics.getDescent()).isGreaterThan(0);
        assertThat(metrics.getAscent() + metrics.getDescent()).isEqualTo(metrics.getHeight());
    }

    @Test
    public void baseline_is_the_same_wrapped_and_unwrapped() {
        Display display = DartMocks.dartDisplay();
        Font font = font(display);
        TextLayout layout = layout(display, font, TEXT);
        // Narrow enough that the text takes several visual lines.
        layout.setWidth(layout.getBounds().width / 3);
        assertThat(layout.getLineCount()).isGreaterThan(1);

        // What StyledText.getBaseline(int) computes once the line height stops being fixed.
        int unwrapped = StyledTextHelper.calculateFontMetrics(font, 1).ascent;
        for (int line = 0; line < layout.getLineCount(); line++) {
            FontMetrics metrics = layout.getLineMetrics(line);
            assertThat(metrics.getAscent() + metrics.getLeading()).isEqualTo(unwrapped);
        }
    }

    @Test
    public void line_metrics_height_is_the_line_height() {
        Display display = DartMocks.dartDisplay();
        TextLayout layout = layout(display, font(display), TEXT);
        layout.setWidth(layout.getBounds().width / 3);

        for (int line = 0; line < layout.getLineCount(); line++) {
            assertThat(layout.getLineMetrics(line).getHeight())
                    .isEqualTo(layout.getLineBounds(line).height);
        }
    }

    @Test
    public void a_line_index_outside_the_layout_is_rejected() {
        Display display = DartMocks.dartDisplay();
        TextLayout layout = layout(display, font(display), TEXT);

        assertThatThrownBy(() -> layout.getLineMetrics(layout.getLineCount()))
                .hasMessageContaining("Index out of bounds");
    }
}
