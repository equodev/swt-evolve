package dev.equo.swt.size;

public final class CComboTheme {
    private final TextStyle textStyle;

    public CComboTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    public static CComboTheme get() {
        return Themes.getTheme().cCombo;
    }

    public static CComboTheme getNonDefaultTheme() {
        return new CComboTheme(new TextStyle("Inter", 12, false, 500, 1.1428571428571428));
    }

}
