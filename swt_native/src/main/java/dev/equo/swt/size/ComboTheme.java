package dev.equo.swt.size;

public record ComboTheme (TextStyle textStyle) {
    public static ComboTheme get() {
        return Themes.getTheme().combo;
    }

    public static ComboTheme getNonDefaultTheme() {
        return new ComboTheme(new TextStyle("Inter", 14, false, false));
    }

    public static ComboTheme getDefaultTheme() {
        return new ComboTheme(new TextStyle("System", 12, false, false));
    }

}
