package dev.equo.swt.size;

import org.eclipse.swt.widgets.DartTree;

public class TreeSizes {

    // Must match tree_theme_settings.dart's itemHeight/itemPadding/itemHeightWithCols.
    private static final double ROW_HEIGHT = 32.0 + 2.0;
    private static final double ROW_HEIGHT_WITH_COLS = 40.0;

    public static int getItemHeight(DartTree tree) {
        double height = tree.getColumnCount() > 1 ? ROW_HEIGHT_WITH_COLS : ROW_HEIGHT;
        return (int) Math.ceil(height);
    }
}
