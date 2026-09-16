package dev.equo.swt.size;

public final class CLabelTheme {
    private final TextStyle textStyle;

    public CLabelTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CLabelTheme)) return false;
        CLabelTheme other = (CLabelTheme) o;
        return java.util.Objects.equals(textStyle, other.textStyle);
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(textStyle); }

    @Override
    public String toString() { return "CLabelTheme[textStyle=" + textStyle + "]"; }

    public static CLabelTheme get() {
        return Themes.getTheme().cLabel;
    }

    public static CLabelTheme getNonDefaultTheme() {
        return new CLabelTheme(new TextStyle("Inter", 12, false, 500, 1.1428571428571428));
    }

    public static CLabelTheme getDefaultTheme() {
        return new CLabelTheme(new TextStyle("Inter", 12, false, 500));
    }

}
