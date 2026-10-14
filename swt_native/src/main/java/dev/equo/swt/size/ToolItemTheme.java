package dev.equo.swt.size;

public final class ToolItemTheme {
    private final TextStyle textStyle;

    public ToolItemTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ToolItemTheme)) return false;
        ToolItemTheme other = (ToolItemTheme) o;
        return java.util.Objects.equals(textStyle, other.textStyle);
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(textStyle); }

    @Override
    public String toString() { return "ToolItemTheme[textStyle=" + textStyle + "]"; }

    public static ToolItemTheme get() {
        return Themes.getTheme().toolItem;
    }

    public static ToolItemTheme getNonDefaultTheme() {
        return new ToolItemTheme(new TextStyle("Inter", 20, false, 500, 1.1428571428571428));
    }

    public static ToolItemTheme getDefaultTheme() {
        return new ToolItemTheme(new TextStyle("Inter", 20, false, 500));
    }

    public static ToolItemTheme getCompactTheme() {
        return new ToolItemTheme(new TextStyle("Inter", 20, false, 500, 1.1428571428571428));
    }

}
