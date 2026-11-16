package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.graphics.Font;

/**
 * A date or time field is as tall as a bordered single-line Text in the same font, as on Win32: the
 * client draws the field over the whole height of its bounds, so a taller preferred height would
 * draw it taller than the fields beside it.
 */
public final class DateTimeSizes {

    private DateTimeSizes() {
    }

    public static int fieldHeight(Font font, Font explicitFont) {
        TextStyle style = !Config.getConfigFlags().use_swt_fonts || explicitFont == null
                ? TextTheme.get().textStyle().withStyleFrom(font)
                : TextStyle.from(font);
        PointD line = FontMetricsUtil.getFontSize("0", style);
        return (int) Math.ceil(Math.max(line.y() + TextSizes.LEFT_BORDER.VERTICAL_PADDING, TextSizes.LEFT_BORDER.MIN_HEIGHT));
    }
}
