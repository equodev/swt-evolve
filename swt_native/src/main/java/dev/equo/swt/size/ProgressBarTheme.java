package dev.equo.swt.size;

public record ProgressBarTheme (TextStyle textStyle) {
    public static ProgressBarTheme get() {
        return Themes.getTheme().progressBar;
    }

    public static ProgressBarTheme getNonDefaultTheme() {
        return new ProgressBarTheme(TextStyle.def());
    }

    public static ProgressBarTheme getDefaultTheme() {
        return new ProgressBarTheme(TextStyle.def());
    }

}
