package dev.equo.swt.size;

public record TreeItemTheme (TextStyle textStyle) {
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
