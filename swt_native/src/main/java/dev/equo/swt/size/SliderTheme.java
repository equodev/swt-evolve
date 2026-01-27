package dev.equo.swt.size;

public record SliderTheme (TextStyle textStyle) {
    public static SliderTheme get() {
        return Themes.getTheme().slider;
    }

    public static SliderTheme getNonDefaultTheme() {
        return new SliderTheme(TextStyle.def());
    }

    public static SliderTheme getDefaultTheme() {
        return new SliderTheme(TextStyle.def());
    }

}
