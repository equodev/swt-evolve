package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.custom.DartCCombo;

/**
 * The height of a row of a CCombo's list, from the CCombo theme.
 *
 * DO NOT EDIT MANUALLY - regenerate with flutter-lib/tool/measure_ccombo_list.dart
 */
public class CComboListSizes {

    static final double ITEM_HEIGHT = 32.0;
    static final double ITEM_PADDING_VERTICAL = 16.0;

    /** The theme's item height, or one line of the item text plus its vertical padding when that is taller. */
    public static int getItemHeight(DartCCombo combo) {
        double line = FontMetricsUtil.getFontSize("Ag", textStyle(combo)).y() + ITEM_PADDING_VERTICAL;
        return (int) Math.ceil(Math.max(line, ITEM_HEIGHT));
    }

    private static TextStyle textStyle(DartCCombo combo) {
        if (!Config.getConfigFlags().use_swt_fonts || combo.getExplicitFont() == null)
            return CComboTheme.get().textStyle().withStyleFrom(combo.getFont());
        return TextStyle.from(combo.getFont());
    }
}
