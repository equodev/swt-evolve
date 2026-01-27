package dev.equo.swt.size;

public record ScaleTheme (TextStyle textStyle) {
    public static ScaleTheme get() {
        return Themes.getTheme().scale;
    }

    public static ScaleTheme getNonDefaultTheme() {
        return new ScaleTheme(TextStyle.def());
    }

    public static ScaleTheme getDefaultTheme() {
        return new ScaleTheme(TextStyle.def());
    }

}
