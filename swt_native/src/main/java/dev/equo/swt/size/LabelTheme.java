package dev.equo.swt.size;

public record LabelTheme (TextStyle textStyle) {
    public static LabelTheme get() {
        return Themes.getTheme().label;
    }

    public static LabelTheme getNonDefaultTheme() {
        return new LabelTheme(new TextStyle("Inter", 14, false, false));
    }

    public static LabelTheme getDefaultTheme() {
        return new LabelTheme(new TextStyle("System", 14, false, false));
    }

}
