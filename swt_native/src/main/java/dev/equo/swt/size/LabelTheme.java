package dev.equo.swt.size;

public final class LabelTheme {
    private final TextStyle textStyle;

    public LabelTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    public static LabelTheme get() {
        return Themes.getTheme().label;
    }

    public static LabelTheme getNonDefaultTheme() {
        return new LabelTheme(new TextStyle("Inter", 14, false, 500, 1.1428571428571428));
    }

    public static LabelTheme getDefaultTheme() {
        return new LabelTheme(new TextStyle("Inter", 14, false, 500));
    }

    public static LabelTheme getCompactTheme() {
        return new LabelTheme(new TextStyle("Inter", 10, false, 500, 1.1428571428571428));
    }

}
