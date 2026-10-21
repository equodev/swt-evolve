package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.widgets.DartControl;

/**
 * The height of a single-line Text in a control's font. Check boxes, radios and drop-down combos
 * are at least this tall, so a row of them laid out top-aligned (RowLayout, a GridLayout row with
 * the default vertical alignment) lines their content up, as the native controls do.
 */
public final class SingleLineHeight {

    private SingleLineHeight() {
    }

    public static int of(DartControl control) {
        TextStyle style = Config.getConfigFlags().use_swt_fonts && control.getExplicitFont() != null
                ? TextStyle.from(control.getFont())
                : TextTheme.get().textStyle().withStyleFrom(control.getFont());
        double textHeight = FontMetricsUtil.getFontSize("", style).y();
        return (int) Math.ceil(Math.max(textHeight + TextSizes.LEFT.VERTICAL_PADDING, TextSizes.LEFT.MIN_HEIGHT));
    }
}
