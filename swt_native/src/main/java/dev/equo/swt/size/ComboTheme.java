package dev.equo.swt.size;

public record ComboTheme (TextStyle textStyle) {
    public static ComboTheme get() {
        return Themes.getTheme().combo;
    }

    public static ComboTheme getNonDefaultTheme() {
        return new ComboTheme(new TextStyle("Inter", 14, false, 500, 1.1428571428571428));
    }

    public static ComboTheme getDefaultTheme() {
        return new ComboTheme(new TextStyle("System", 12, false, 400));
    }

}
