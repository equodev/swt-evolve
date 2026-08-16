package org.eclipse.swt.graphics;

import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * A tab must advance to the next tab stop ({@link TextLayout#setTabs}, positions in device
 * pixels; a single-element array repeats as a fixed interval, longer arrays repeat their last
 * interval) — not by one average character width. This is how both native SWT and the render
 * side lay tabs out; StyledText feeds every layout a single-element array via its renderer.
 * A style range carrying {@link GlyphMetrics} still pins the advance to {@code metrics.width}.
 */
@Tag("native-unit")
public class TextLayoutTabStopsNativeTest {

    private static final int FONT_SIZE = 12;

    private TextLayout layout(Display display, String text) {
        Font font = new DartFont(display, "System", FONT_SIZE, SWT.NORMAL, null).getApi();
        TextLayout layout = new TextLayout(display);
        layout.setFont(font);
        layout.setText(text);
        return layout;
    }

    // Resolve the font id the way the product path does (through the substitution table),
    // so the oracle measures the same family in a JVM with web-font substitutions registered.
    private static double charPx(Display display, String s) {
        String fontId = FontMetricsUtil.getId("System", false, false);
        return FontMetricsUtil.getFontSize(s, fontId, FONT_SIZE).x() * display.getDPI().x / 72.0;
    }

    @Test
    public void tab_advances_to_next_repeating_stop() {
        Display display = DartMocks.dartDisplay();
        TextLayout layout = layout(display, "\tX");
        layout.setTabs(new int[] { 40 });

        assertThat((double) layout.getLocation(1, false).x).isCloseTo(40, within(1.0));
        assertThat((double) layout.getBounds().width)
                .isCloseTo(40 + charPx(display, "X"), within(2.0));
    }

    @Test
    public void tab_after_text_snaps_to_following_stop() {
        Display display = DartMocks.dartDisplay();
        // "ab" is a few px wide, so the tab after it must land on the first stop (40),
        // not advance by a fixed width from wherever "ab" ended.
        TextLayout layout = layout(display, "ab\tX");
        layout.setTabs(new int[] { 40 });

        assertThat((double) layout.getLocation(3, false).x).isCloseTo(40, within(1.0));
    }

    @Test
    public void stops_beyond_the_array_repeat_the_last_interval() {
        Display display = DartMocks.dartDisplay();
        TextLayout layout = layout(display, "\t\t\tX");
        layout.setTabs(new int[] { 40, 100 });

        // 0 -> 40 (first stop) -> 100 (second stop) -> 160 (last interval, 60, repeats)
        assertThat((double) layout.getLocation(3, false).x).isCloseTo(160, within(1.0));
    }

    @Test
    public void glyph_metrics_still_pin_the_tab_advance() {
        Display display = DartMocks.dartDisplay();
        TextLayout layout = layout(display, "\tX");
        layout.setTabs(new int[] { 40 });
        TextStyle style = new TextStyle(null, null, null);
        style.metrics = new GlyphMetrics(0, 0, 77);
        layout.setStyle(style, 0, 0);

        assertThat((double) layout.getLocation(1, false).x).isCloseTo(77, within(1.0));
    }
}
