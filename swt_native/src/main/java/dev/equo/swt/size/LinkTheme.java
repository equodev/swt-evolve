package dev.equo.swt.size;

public record LinkTheme (TextStyle textStyle) {
    public static LinkTheme get() {
        return Themes.getTheme().link;
    }

    public static LinkTheme getNonDefaultTheme() {
        return new LinkTheme(new TextStyle("Inter", 14, false, 500, 1.1428571428571428));
    }

    public static LinkTheme getDefaultTheme() {
        return new LinkTheme(new TextStyle("System", 14, false, 400));
    }

}
