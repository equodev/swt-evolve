package dev.equo.swt.size;

public final class LinkTheme {
    private final TextStyle textStyle;

    public LinkTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    public static LinkTheme get() {
        return Themes.getTheme().link;
    }

    public static LinkTheme getNonDefaultTheme() {
        return new LinkTheme(new TextStyle("Inter", 14, false, 500, 1.1428571428571428));
    }

}
