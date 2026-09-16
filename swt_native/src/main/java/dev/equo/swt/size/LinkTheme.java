package dev.equo.swt.size;

public final class LinkTheme {
    private final TextStyle textStyle;

    public LinkTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LinkTheme)) return false;
        LinkTheme other = (LinkTheme) o;
        return java.util.Objects.equals(textStyle, other.textStyle);
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(textStyle); }

    @Override
    public String toString() { return "LinkTheme[textStyle=" + textStyle + "]"; }

    public static LinkTheme get() {
        return Themes.getTheme().link;
    }

    public static LinkTheme getNonDefaultTheme() {
        return new LinkTheme(new TextStyle("Inter", 14, false, 500, 1.1428571428571428));
    }

    public static LinkTheme getDefaultTheme() {
        return new LinkTheme(new TextStyle("Inter", 14, false, 500));
    }

}
