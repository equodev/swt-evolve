package dev.equo.swt.size;

public final class TreeItemTheme {
    private final TextStyle textStyle;

    public TreeItemTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TreeItemTheme)) return false;
        TreeItemTheme other = (TreeItemTheme) o;
        return java.util.Objects.equals(textStyle, other.textStyle);
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(textStyle); }

    @Override
    public String toString() { return "TreeItemTheme[textStyle=" + textStyle + "]"; }

    public static TreeItemTheme get() {
        return Themes.getTheme().treeItem;
    }

    public static TreeItemTheme getNonDefaultTheme() {
        return new TreeItemTheme(new TextStyle("Inter", 14, false, 500, 1.4285714285714286));
    }

    public static TreeItemTheme getDefaultTheme() {
        return new TreeItemTheme(new TextStyle("Inter", 14, false, 500));
    }

}
