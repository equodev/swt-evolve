package dev.equo.swt.size;

public record CLabelTheme (TextStyle textStyle) {
    public static CLabelTheme get() {
        return Themes.getTheme().cLabel;
    }

    public static CLabelTheme getNonDefaultTheme() {
        return new CLabelTheme(new TextStyle("Inter", 12, false, false));
    }

    public static CLabelTheme getDefaultTheme() {
        return new CLabelTheme(new TextStyle("System", 12, false, false));
    }

}
