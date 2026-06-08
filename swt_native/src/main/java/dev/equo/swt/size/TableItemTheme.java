package dev.equo.swt.size;

public record TableItemTheme (TextStyle textStyle) {
    public static TableItemTheme get() {
        return Themes.getTheme().tableItem;
    }

    public static TableItemTheme getNonDefaultTheme() {
        return new TableItemTheme(new TextStyle("Inter", 14, false, 500, 1.1428571428571428));
    }

    public static TableItemTheme getDefaultTheme() {
        return new TableItemTheme(new TextStyle("System", 14, false, 400));
    }

}
