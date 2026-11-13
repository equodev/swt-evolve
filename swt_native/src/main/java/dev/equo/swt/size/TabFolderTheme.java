package dev.equo.swt.size;

/**
 * What a TabFolder draws around the page it shows and around each tab's label, per theme.
 * Java hands the page the area these leave and sizes a tab from its label, so the numbers
 * are the render side's rather than a second copy.
 *
 * DO NOT EDIT MANUALLY - regenerate from measure_tabfolder.dart
 */
public final class TabFolderTheme {
    private final int tabStripHeight;
    private final int tabHeight;
    private final int bodyBorder;
    private final int tabLabelSurround;
    private final int tabImageExtra;
    private final int tabHeightWithImage;
    private final TextStyle tabLabelStyle;

    public TabFolderTheme(int tabStripHeight, int tabHeight, int bodyBorder, int tabLabelSurround, int tabImageExtra, int tabHeightWithImage, TextStyle tabLabelStyle) {
        this.tabStripHeight = tabStripHeight;
        this.tabHeight = tabHeight;
        this.bodyBorder = bodyBorder;
        this.tabLabelSurround = tabLabelSurround;
        this.tabImageExtra = tabImageExtra;
        this.tabHeightWithImage = tabHeightWithImage;
        this.tabLabelStyle = tabLabelStyle;
    }

    public int tabStripHeight() { return tabStripHeight; }
    public int tabHeight() { return tabHeight; }
    public int bodyBorder() { return bodyBorder; }
    public int tabLabelSurround() { return tabLabelSurround; }
    public int tabImageExtra() { return tabImageExtra; }
    public int tabHeightWithImage() { return tabHeightWithImage; }
    public TextStyle tabLabelStyle() { return tabLabelStyle; }

    public static TabFolderTheme get() {
        return Themes.getTheme().tabFolder;
    }

    public static TabFolderTheme getNonDefaultTheme() {
        return new TabFolderTheme(18, 17, 0, 19, 19, 18, new TextStyle("Inter", 12, false, 400, 1.1428571428571428));
    }

}
