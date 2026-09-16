package dev.equo.swt.size;

public final class ButtonTheme {
    private final TextStyle textStyle;

    public ButtonTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ButtonTheme)) return false;
        ButtonTheme other = (ButtonTheme) o;
        return java.util.Objects.equals(textStyle, other.textStyle);
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(textStyle); }

    @Override
    public String toString() { return "ButtonTheme[textStyle=" + textStyle + "]"; }

    public static ButtonTheme get() {
        return Themes.getTheme().button;
    }

    public static ButtonTheme getNonDefaultTheme() {
        return new ButtonTheme(new TextStyle("Inter", 14, false, 500, 1.1428571428571428));
    }

    public static ButtonTheme getDefaultTheme() {
        return new ButtonTheme(new TextStyle("Inter", 14, false, 500));
    }

}
