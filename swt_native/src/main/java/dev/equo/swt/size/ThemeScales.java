package dev.equo.swt.size;

/**
 * Which type scale each named theme draws in.
 *
 * Generated from namedThemes by measure_theme_scales.dart. DO NOT EDIT MANUALLY.
 */
public final class ThemeScales {

    private ThemeScales() {
    }

    public static Themes.Theme forThemeName(String themeName) {
        if (themeName == null) return Themes.Theme.NonDefault;
        switch (themeName.trim()) {
            default:
                return Themes.Theme.NonDefault;
        }
    }
}
