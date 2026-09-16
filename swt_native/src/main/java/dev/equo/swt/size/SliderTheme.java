package dev.equo.swt.size;

public final class SliderTheme {
    private final TextStyle textStyle;

    public SliderTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SliderTheme)) return false;
        SliderTheme other = (SliderTheme) o;
        return java.util.Objects.equals(textStyle, other.textStyle);
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(textStyle); }

    @Override
    public String toString() { return "SliderTheme[textStyle=" + textStyle + "]"; }

    public static SliderTheme get() {
        return Themes.getTheme().slider;
    }

    public static SliderTheme getNonDefaultTheme() {
        return new SliderTheme(TextStyle.def());
    }

    public static SliderTheme getDefaultTheme() {
        return new SliderTheme(TextStyle.def());
    }

}
