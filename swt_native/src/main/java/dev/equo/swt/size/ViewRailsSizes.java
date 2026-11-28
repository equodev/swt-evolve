package dev.equo.swt.size;

/**
 * The view rails' widths, small, medium and large, from the view rails' theme.
 *
 * DO NOT EDIT MANUALLY - regenerate with flutter-lib/tool/measure_view_rails.dart
 */
public final class ViewRailsSizes {

    private static final int[] WIDTHS = {33, 40, 48};

    /** The height a rail asks for when it holds a view. */
    public static final int MIN_HEIGHT = 20;

    private ViewRailsSizes() {
    }

    /** The width of a rail of {@code size}: 0 small, 1 medium, 2 large. */
    public static int width(int size) {
        return WIDTHS[size];
    }
}
