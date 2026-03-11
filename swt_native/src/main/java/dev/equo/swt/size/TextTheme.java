package dev.equo.swt.size;

public record TextTheme (TextStyle textStyle) {
    public static TextTheme get() {
        return Themes.getTheme().text;
    }

    public static TextTheme getNonDefaultTheme() {
        return new TextTheme(new TextStyle("Inter", 14, false, 500, 1.1428571428571428));
    }

    public static TextTheme getDefaultTheme() {
        return new TextTheme(new TextStyle("System", 14, false, 400));
    }

}
