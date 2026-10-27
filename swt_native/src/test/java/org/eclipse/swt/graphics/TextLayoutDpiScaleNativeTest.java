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
 * TextLayout must report widths in the same device pixels the render side paints. The glyph
 * table ({@code GenFontMetrics}) stores 72-dpi point advances, while text is painted at the
 * display DPI (96 on this backend), so every width answer has to carry the 96/72 conversion —
 * exactly as line heights already do via {@code StyledTextHelper.computeLineHeight}. Without it,
 * horizontal positions drift from the painted glyphs proportionally to the column.
 */
@Tag("native-unit")
public class TextLayoutDpiScaleNativeTest {

    private static final String TEXT = "The quick brown fox jumps over the lazy dog";
    private static final int FONT_SIZE = 12;

    private static double dpiScale(Display display) {
        return display.getDPI().x / 72.0;
    }

    /**
     * Expected width in device px, resolving the font id the way the product path does
     * (through the substitution table) — a JVM that booted the web server has substitutions
     * registered, and a literal "System-0-3" oracle would measure a different family.
     */
    private static double expectedPx(Display display, Font font, String text) {
        String fontId = FontMetricsUtil.getId(font.getFontData()[0]);
        return FontMetricsUtil.getFontSize(text, fontId, FONT_SIZE).x() * dpiScale(display);
    }

    private TextLayout layout(Display display, Font font, String text) {
        TextLayout layout = new TextLayout(display);
        layout.setFont(font);
        layout.setText(text);
        return layout;
    }

    @Test
    public void bounds_width_is_in_device_pixels() {
        Display display = DartMocks.dartDisplay();
        Font font = new DartFont(display, "System", FONT_SIZE, SWT.NORMAL, null).getApi();
        TextLayout layout = layout(display, font, TEXT);

        double expected = expectedPx(display, font, TEXT);
        assertThat(expected).isGreaterThan(0);

        assertThat((double) layout.getBounds().width).isCloseTo(expected, within(2.0));
    }

    @Test
    public void location_at_line_end_is_in_device_pixels() {
        Display display = DartMocks.dartDisplay();
        Font font = new DartFont(display, "System", FONT_SIZE, SWT.NORMAL, null).getApi();
        TextLayout layout = layout(display, font, TEXT);

        assertThat((double) layout.getLocation(TEXT.length(), false).x)
                .isCloseTo(expectedPx(display, font, TEXT), within(2.0));
    }

    @Test
    public void location_at_line_end_equals_bounds_width() {
        Display display = DartMocks.dartDisplay();
        Font font = new DartFont(display, "System", FONT_SIZE, SWT.NORMAL, null).getApi();
        TextLayout layout = layout(display, font, TEXT);

        // The TextLayout contract upstream tests rely on: a location at the line's visual
        // edge equals the layout width exactly, whatever rounding either side applies.
        assertThat(layout.getLocation(TEXT.length(), false).x)
                .isEqualTo(layout.getBounds().width)
                .isEqualTo(layout.getLineBounds(0).width);
    }

    @Test
    public void location_mid_line_is_in_device_pixels() {
        Display display = DartMocks.dartDisplay();
        Font font = new DartFont(display, "System", FONT_SIZE, SWT.NORMAL, null).getApi();
        TextLayout layout = layout(display, font, TEXT);

        int offset = 20;
        assertThat((double) layout.getLocation(offset, false).x)
                .isCloseTo(expectedPx(display, font, TEXT.substring(0, offset)), within(2.0));
    }
}
