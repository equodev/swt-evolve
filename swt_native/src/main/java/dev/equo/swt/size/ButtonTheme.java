package dev.equo.swt.size;

public record ButtonTheme (TextStyle textStyle) {
    public static ButtonTheme get() {
        return Themes.getTheme().button;
    }

    public static ButtonTheme getNonDefaultTheme() {
        return new ButtonTheme(new TextStyle("Inter", 14, false, false));
    }

    public static ButtonTheme getDefaultTheme() {
        return new ButtonTheme(new TextStyle("System", 14, false, false));
    }

}
