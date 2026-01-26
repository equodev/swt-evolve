package dev.equo.swt.size;

public record CComboTheme (TextStyle textStyle) {
    public static CComboTheme get() {
        return Themes.getTheme().cCombo;
    }

    public static CComboTheme getNonDefaultTheme() {
        return new CComboTheme(new TextStyle("Inter", 12, false, false));
    }

    public static CComboTheme getDefaultTheme() {
        return new CComboTheme(new TextStyle("System", 12, false, false));
    }

}
