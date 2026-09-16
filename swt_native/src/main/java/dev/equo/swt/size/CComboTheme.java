package dev.equo.swt.size;

public final class CComboTheme {
    private final TextStyle textStyle;

    public CComboTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CComboTheme)) return false;
        CComboTheme other = (CComboTheme) o;
        return java.util.Objects.equals(textStyle, other.textStyle);
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(textStyle); }

    @Override
    public String toString() { return "CComboTheme[textStyle=" + textStyle + "]"; }

    public static CComboTheme get() {
        return Themes.getTheme().cCombo;
    }

    public static CComboTheme getNonDefaultTheme() {
        return new CComboTheme(new TextStyle("Inter", 12, false, 500, 1.1428571428571428));
    }

    public static CComboTheme getDefaultTheme() {
        return new CComboTheme(new TextStyle("Inter", 12, false, 500));
    }

}
