package dev.equo.swt.size;

public final class SliderTheme {
    private final TextStyle textStyle;

    public SliderTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    public static SliderTheme get() {
        return Themes.getTheme().slider;
    }

    public static SliderTheme getNonDefaultTheme() {
        return new SliderTheme(TextStyle.def());
    }

}
