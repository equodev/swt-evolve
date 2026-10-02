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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CoolBarTheme)) return false;
        CoolBarTheme other = (CoolBarTheme) o;
        return frameLeft == other.frameLeft
            && frameTop == other.frameTop
            && frameRight == other.frameRight
            && frameBottom == other.frameBottom;
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(frameLeft, frameTop, frameRight, frameBottom); }

    @Override
    public String toString() { return "CoolBarTheme[frameLeft=" + frameLeft + ", frameTop=" + frameTop + ", frameRight=" + frameRight + ", frameBottom=" + frameBottom + "]"; }

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
