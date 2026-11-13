package dev.equo.swt.size;

public final class CLabelTheme {
    private final TextStyle textStyle;

    public CLabelTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    public static CLabelTheme get() {
        return Themes.getTheme().cLabel;
    }

    public static CLabelTheme getNonDefaultTheme() {
        return new CLabelTheme(new TextStyle("Inter", 12, false, 500, 1.1428571428571428));
    }

}
