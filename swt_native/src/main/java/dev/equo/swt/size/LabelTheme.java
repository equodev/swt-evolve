package dev.equo.swt.size;

public final class LabelTheme {
    private final TextStyle textStyle;

    public LabelTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LabelTheme)) return false;
        LabelTheme other = (LabelTheme) o;
        return java.util.Objects.equals(textStyle, other.textStyle);
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(textStyle); }

    @Override
    public String toString() { return "LabelTheme[textStyle=" + textStyle + "]"; }

    public static LabelTheme get() {
        return Themes.getTheme().label;
    }

    public static LabelTheme getNonDefaultTheme() {
        return new LabelTheme(new TextStyle("Inter", 14, false, 500, 1.1428571428571428));
    }

    public static LabelTheme getDefaultTheme() {
        return new LabelTheme(new TextStyle("Inter", 14, false, 500));
    }

}
