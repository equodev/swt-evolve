package dev.equo.swt.size;

public final class ScaleTheme {
    private final TextStyle textStyle;

    public ScaleTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    public static ScaleTheme get() {
        return Themes.getTheme().scale;
    }

    public static ScaleTheme getNonDefaultTheme() {
        return new ScaleTheme(TextStyle.def());
    }

    public static ScaleTheme getDefaultTheme() {
        return new ScaleTheme(TextStyle.def());
    }

    public static ScaleTheme getCompactTheme() {
        return new ScaleTheme(TextStyle.def());
    }

}
