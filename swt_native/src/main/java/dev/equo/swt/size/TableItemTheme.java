package dev.equo.swt.size;

public final class TableItemTheme {
    private final TextStyle textStyle;

    public TableItemTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TableItemTheme)) return false;
        TableItemTheme other = (TableItemTheme) o;
        return java.util.Objects.equals(textStyle, other.textStyle);
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(textStyle); }

    @Override
    public String toString() { return "TableItemTheme[textStyle=" + textStyle + "]"; }

    public static TableItemTheme get() {
        return Themes.getTheme().tableItem;
    }

    public static TableItemTheme getNonDefaultTheme() {
        return new TableItemTheme(new TextStyle("Inter", 14, false, 500, 1.1428571428571428));
    }

    public static TableItemTheme getDefaultTheme() {
        return new TableItemTheme(new TextStyle("Inter", 14, false, 500));
    }

}
