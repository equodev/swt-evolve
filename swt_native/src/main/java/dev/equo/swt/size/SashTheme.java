package dev.equo.swt.size;

public final class SashTheme {
    private final TextStyle textStyle;

    public SashTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    public static SashTheme get() {
        return Themes.getTheme().sash;
    }

    public static SashTheme getNonDefaultTheme() {
        return new SashTheme(TextStyle.def());
    }

    public static SashTheme getDefaultTheme() {
        return new SashTheme(TextStyle.def());
    }

    public static SashTheme getCompactTheme() {
        return new SashTheme(TextStyle.def());
    }

}
