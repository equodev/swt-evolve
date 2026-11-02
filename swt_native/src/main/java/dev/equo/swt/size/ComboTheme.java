package dev.equo.swt.size;

public final class ComboTheme {
    private final TextStyle textStyle;

    public ComboTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    public static ComboTheme get() {
        return Themes.getTheme().combo;
    }

    public static ComboTheme getNonDefaultTheme() {
        return new ComboTheme(new TextStyle("Inter", 14, false, 500, 1.1428571428571428));
    }

    public static ComboTheme getDefaultTheme() {
        return new ComboTheme(new TextStyle("Inter", 14, false, 500));
    }

    public static ComboTheme getCompactTheme() {
        return new ComboTheme(new TextStyle("Inter", 10, false, 500, 1.1428571428571428));
    }

}
