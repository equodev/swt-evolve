package dev.equo.swt.size;

public final class TableItemTheme {
    private final TextStyle textStyle;

    public TableItemTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    public static TableItemTheme get() {
        return Themes.getTheme().tableItem;
    }

    public static TableItemTheme getNonDefaultTheme() {
        return new TableItemTheme(new TextStyle("Inter", 14, false, 500, 1.1428571428571428));
    }

    public static TableItemTheme getDefaultTheme() {
        return new TableItemTheme(new TextStyle("Inter", 14, false, 500));
    }

    public static TableItemTheme getCompactTheme() {
        return new TableItemTheme(new TextStyle("Inter", 10, false, 500, 1.1428571428571428));
    }

}
