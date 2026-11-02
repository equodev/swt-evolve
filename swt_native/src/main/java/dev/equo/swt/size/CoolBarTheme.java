package dev.equo.swt.size;

/**
 * The frame a CoolBar draws between its bounds and its content, per theme.
 *
 * DO NOT EDIT MANUALLY - regenerate from measure_coolbar.dart
 */
public final class CoolBarTheme {
    private final int frameLeft;
    private final int frameTop;
    private final int frameRight;
    private final int frameBottom;

    public CoolBarTheme(int frameLeft, int frameTop, int frameRight, int frameBottom) {
        this.frameLeft = frameLeft;
        this.frameTop = frameTop;
        this.frameRight = frameRight;
        this.frameBottom = frameBottom;
    }

    public int frameLeft() { return frameLeft; }
    public int frameTop() { return frameTop; }
    public int frameRight() { return frameRight; }
    public int frameBottom() { return frameBottom; }

    public static CoolBarTheme get() {
        return Themes.getTheme().coolBar;
    }

    public static CoolBarTheme getNonDefaultTheme() {
        return new CoolBarTheme(3, 3, 3, 3);
    }

    public static CoolBarTheme getDefaultTheme() {
        return new CoolBarTheme(3, 3, 3, 3);
    }

    public static CoolBarTheme getCompactTheme() {
        return new CoolBarTheme(3, 3, 3, 3);
    }

}
