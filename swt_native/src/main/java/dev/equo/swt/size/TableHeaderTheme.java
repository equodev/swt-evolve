package dev.equo.swt.size;

public record TableHeaderTheme (TextStyle textStyle) {
    public static TableHeaderTheme get() {
        return Themes.getTheme().tableHeader;
    }

    public static TableHeaderTheme getNonDefaultTheme() {
        return new TableHeaderTheme(new TextStyle("Inter", 14, false, 600, 20.0 / 14.0));
    }

    public static TableHeaderTheme getDefaultTheme() {
        return new TableHeaderTheme(new TextStyle("Inter", 14, false, 600, 20.0 / 14.0));
    }
}
