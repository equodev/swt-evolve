package dev.equo.swt.size;

public final class ProgressBarTheme {
    private final TextStyle textStyle;

    public ProgressBarTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProgressBarTheme)) return false;
        ProgressBarTheme other = (ProgressBarTheme) o;
        return java.util.Objects.equals(textStyle, other.textStyle);
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(textStyle); }

    @Override
    public String toString() { return "ProgressBarTheme[textStyle=" + textStyle + "]"; }

    public static ProgressBarTheme get() {
        return Themes.getTheme().progressBar;
    }

    public static ProgressBarTheme getNonDefaultTheme() {
        return new ProgressBarTheme(TextStyle.def());
    }

    public static ProgressBarTheme getDefaultTheme() {
        return new ProgressBarTheme(TextStyle.def());
    }

}
