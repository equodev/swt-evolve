package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.DartList;

/**
 * Item height and preferred size of a List, from the List theme.
 *
 * DO NOT EDIT MANUALLY - regenerate with flutter-lib/tool/measure_list.dart
 */
public class ListSizes {

    private static final double ROW_HEIGHT = 24.0;
    private static final double ROW_PADDING_VERTICAL = 4.0;
    private static final double ITEM_PADDING_HORIZONTAL = 16.0;
    private static final int EMPTY_WIDTH = 100;
    private static final int MIN_EXTENT = 64;

    private ListSizes() {
    }

    public static Point computeSize(DartList list, int wHint, int hHint, boolean changed) {
        int width = wHint == SWT.DEFAULT ? getContentWidth(list) : wHint;
        if (width <= 0)
            width = MIN_EXTENT;
        int height = hHint == SWT.DEFAULT ? list.getItemCount() * getItemHeight(list) : hHint;
        if (height <= 0)
            height = MIN_EXTENT;
        Rectangle trim = list.computeTrim(0, 0, width, height);
        return new Point(trim.width, trim.height);
    }

    /** One line of the item text and its padding, never less than the theme's item height. */
    public static int getItemHeight(DartList list) {
        double textHeight = FontMetricsUtil.getFontSize("Ag", textStyle(list)).y() + ROW_PADDING_VERTICAL;
        return (int) Math.ceil(Math.max(textHeight, ROW_HEIGHT));
    }

    /** The widest item and its padding; {@code EMPTY_WIDTH} when no item has text. */
    private static int getContentWidth(DartList list) {
        TextStyle ts = textStyle(list);
        double widest = 0;
        for (String item : list.getItems())
            widest = Math.max(widest, FontMetricsUtil.getFontSize(item, ts).x());
        return widest > 0 ? (int) Math.ceil(widest + ITEM_PADDING_HORIZONTAL) : EMPTY_WIDTH;
    }

    private static TextStyle textStyle(DartList list) {
        if (!Config.getConfigFlags().use_swt_fonts || list.getExplicitFont() == null)
            return ListTheme.get().textStyle().withStyleFrom(list.getFont());
        return TextStyle.from(list.getFont());
    }
}
