package dev.equo.swt.size;

public final class TableHeaderTheme {
    private final TextStyle textStyle;

    public TableHeaderTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TableHeaderTheme)) return false;
        TableHeaderTheme other = (TableHeaderTheme) o;
        return java.util.Objects.equals(textStyle, other.textStyle);
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(textStyle); }

    @Override
    public String toString() { return "TableHeaderTheme[textStyle=" + textStyle + "]"; }

    public static TableHeaderTheme get() {
        return Themes.getTheme().tableHeader;
    }

    public static TableHeaderTheme getNonDefaultTheme() {
        return new TableHeaderTheme(new TextStyle("Inter", 14, false, 600, 20.0 / 14.0));
    }

    public static TableHeaderTheme getDefaultTheme() {
        return new TableHeaderTheme(new TextStyle("Inter", 14, false, 600, 20.0 / 14.0));
    }
}
