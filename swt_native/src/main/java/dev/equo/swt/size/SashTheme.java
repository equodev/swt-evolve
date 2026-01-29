package dev.equo.swt.size;

public record SashTheme (TextStyle textStyle) {
    public static SashTheme get() {
        return Themes.getTheme().sash;
    }

    public static SashTheme getNonDefaultTheme() {
        return new SashTheme(TextStyle.def());
    }

    public static SashTheme getDefaultTheme() {
        return new SashTheme(TextStyle.def());
    }

}
