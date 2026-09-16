package dev.equo.swt.size;

public final class ComboTheme {
    private final TextStyle textStyle;

    public ComboTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ComboTheme)) return false;
        ComboTheme other = (ComboTheme) o;
        return java.util.Objects.equals(textStyle, other.textStyle);
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(textStyle); }

    @Override
    public String toString() { return "ComboTheme[textStyle=" + textStyle + "]"; }

    public static ComboTheme get() {
        return Themes.getTheme().combo;
    }

    public static ComboTheme getNonDefaultTheme() {
        return new ComboTheme(new TextStyle("Inter", 14, false, 500, 1.1428571428571428));
    }

    public static ComboTheme getDefaultTheme() {
        return new ComboTheme(new TextStyle("Inter", 14, false, 500));
    }

}
