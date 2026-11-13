package dev.equo.swt.size;

public final class ButtonTheme {
    private final TextStyle textStyle;

    public ButtonTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    public static ButtonTheme get() {
        return Themes.getTheme().button;
    }

    public static ButtonTheme getNonDefaultTheme() {
        return new ButtonTheme(new TextStyle("Inter", 14, false, 500, 1.1428571428571428));
    }

}
