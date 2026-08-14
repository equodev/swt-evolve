package dev.equo.swt.size;

/**
 * Width the optional controls row takes at the trailing end of the main toolbar when a build turns
 * the theme palette or the scaling control on. The row renders on the Flutter side only, so the
 * layout has to reserve its width here or the trim hands that strip to a contribution and the two
 * draw on top of each other.
 *
 * <p>Collapsed the row is a single chevron shared by both controls, which is why one constant
 * covers either flag. Pinned from the render side by
 * {@code toolbar_optional_controls_width_test.dart}.
 */
public class ToolbarControlsSizes {

    /** Chevron icon (18) plus its horizontal padding (4 either side). */
    public static final int COLLAPSED_WIDTH = 26;
}
