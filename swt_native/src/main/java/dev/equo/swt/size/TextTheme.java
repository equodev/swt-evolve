package dev.equo.swt.size;

public final class TextTheme {
    private final TextStyle textStyle;

    public TextTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TextTheme)) return false;
        TextTheme other = (TextTheme) o;
        return java.util.Objects.equals(textStyle, other.textStyle);
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(textStyle); }

    @Override
    public String toString() { return "TextTheme[textStyle=" + textStyle + "]"; }

    public static TextTheme get() {
        return Themes.getTheme().text;
    }

    public static TextTheme getNonDefaultTheme() {
        return new TextTheme(new TextStyle("Inter", 14, false, 500, 1.1428571428571428));
    }

    public static TextTheme getDefaultTheme() {
        return new TextTheme(new TextStyle("Inter", 14, false, 500));
    }

}
