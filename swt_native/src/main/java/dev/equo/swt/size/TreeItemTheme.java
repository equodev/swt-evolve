package dev.equo.swt.size;

public final class TreeItemTheme {
    private final TextStyle textStyle;

    public TreeItemTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    public static TreeItemTheme get() {
        return Themes.getTheme().treeItem;
    }

    public static TreeItemTheme getNonDefaultTheme() {
        return new TreeItemTheme(new TextStyle("Inter", 14, false, 500, 1.4285714285714286));
    }

    public static TreeItemTheme getDefaultTheme() {
        return new TreeItemTheme(new TextStyle("Inter", 14, false, 500));
    }

    public static TreeItemTheme getCompactTheme() {
        return new TreeItemTheme(new TextStyle("Inter", 10, false, 500, 1.4285714285714286));
    }

}
