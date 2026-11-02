package dev.equo.swt.size;

public final class ProgressBarTheme {
    private final TextStyle textStyle;

    public ProgressBarTheme(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public TextStyle textStyle() { return textStyle; }

    public static ProgressBarTheme get() {
        return Themes.getTheme().progressBar;
    }

    public static ProgressBarTheme getNonDefaultTheme() {
        return new ProgressBarTheme(TextStyle.def());
    }

    public static ProgressBarTheme getDefaultTheme() {
        return new ProgressBarTheme(TextStyle.def());
    }

    public static ProgressBarTheme getCompactTheme() {
        return new ProgressBarTheme(TextStyle.def());
    }

}
