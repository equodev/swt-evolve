package dev.equo.swt.size;

public final class SashTheme {
    private final TextStyle textStyle;

    public SashTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SashTheme)) return false;
        SashTheme other = (SashTheme) o;
        return java.util.Objects.equals(textStyle, other.textStyle);
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(textStyle); }

    @Override
    public String toString() { return "SashTheme[textStyle=" + textStyle + "]"; }

    public static SashTheme get() {
        return Themes.getTheme().sash;
    }

    public static SashTheme getNonDefaultTheme() {
        return new SashTheme(TextStyle.def());
    }

    public static SashTheme getDefaultTheme() {
        return new SashTheme(TextStyle.def());
    }

}
