package dev.equo.swt.size;

public final class ScaleTheme {
    private final TextStyle textStyle;

    public ScaleTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScaleTheme)) return false;
        ScaleTheme other = (ScaleTheme) o;
        return java.util.Objects.equals(textStyle, other.textStyle);
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(textStyle); }

    @Override
    public String toString() { return "ScaleTheme[textStyle=" + textStyle + "]"; }

    public static ScaleTheme get() {
        return Themes.getTheme().scale;
    }

    public static ScaleTheme getNonDefaultTheme() {
        return new ScaleTheme(TextStyle.def());
    }

    public static ScaleTheme getDefaultTheme() {
        return new ScaleTheme(TextStyle.def());
    }

}
