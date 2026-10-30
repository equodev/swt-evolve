package dev.equo.swt.size;

public final class ToolItemTheme {
    private final TextStyle textStyle;

    public ToolItemTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    public static ToolItemTheme get() {
        return Themes.getTheme().toolItem;
    }

    public static ToolItemTheme getNonDefaultTheme() {
        return new ToolItemTheme(new TextStyle("Inter", 20, false, 500, 1.1428571428571428));
    }

    public static ToolItemTheme getDefaultTheme() {
        return new ToolItemTheme(new TextStyle("Inter", 20, false, 500));
    }

    public static ToolItemTheme getCompactTheme() {
        return new ToolItemTheme(new TextStyle("Inter", 20, false, 500, 1.1428571428571428));
    }

}
