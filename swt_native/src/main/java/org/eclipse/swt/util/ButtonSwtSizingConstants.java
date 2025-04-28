package org.eclipse.swt.util; // Or your desired package

/**
 * Generated constants and calculation methods for ButtonSwt sizing.
 * Source: button_swt_sizing_params_flutter.json
 * DO NOT EDIT MANUALLY - Regenerate using ButtonSizeConstantsGenerator.
 */
public final class ButtonSwtSizingConstants {

    // --- General Character Metrics ---
    /** Average character width used for text length calculations. */
    public static final double AVERAGE_CHAR_WIDTH = 7.974099995177469;

    // --- SWT.PUSH Constants ---
    /** Minimum width for an SWT.PUSH button. */
    public static final double PUSH_MIN_WIDTH = 24.0;
    /** Horizontal padding (each side) for an SWT.PUSH button. */
    public static final double PUSH_HORIZONTAL_PADDING = 12.0;
    /** Fixed height for an SWT.PUSH button. */
    public static final double PUSH_FIXED_HEIGHT = 27.0;

    // --- SWT.CHECK Constants ---
    /** Size allocated for the checkbox visual element in an SWT.CHECK button. */
    public static final double CHECK_CHECKBOX_SIZE = 28.0;
    /** Fixed height for an SWT.CHECK button. */
    public static final double CHECK_FIXED_HEIGHT = 20.0;

    // --- SWT.RADIO Constants ---
    /** Size allocated for the radio button visual element in an SWT.RADIO button. */
    public static final double RADIO_RADIOBUTTON_SIZE = 26.0;
    /** Fixed height for an SWT.RADIO button. */
    public static final double RADIO_FIXED_HEIGHT = 20.0;

    // --- SWT.TOGGLE Constants (Common) ---
    /** Fixed height for all variants of SWT.TOGGLE buttons. */
    public static final double TOGGLE_FIXED_HEIGHT = 30.0;
    /** Minimum width for all variants of SWT.TOGGLE buttons. */
    public static final double TOGGLE_MIN_WIDTH = 70.0;

    // --- SWT.TOGGLE Constants (Text Only) ---
    /** Horizontal padding (each side) for a text-only SWT.TOGGLE button. */
    public static final double TOGGLE_TEXT_HORIZONTAL_PADDING = 16.0;

    // --- SWT.TOGGLE Constants (Icon Only) ---
    /** Size allocated for the icon in an icon-only SWT.TOGGLE button. */
    public static final double TOGGLE_ICON_ONLY_SIZE = 32.0;

    // --- SWT.TOGGLE Constants (Text & Icon) ---
    /** Horizontal padding (each side) for a text-and-icon SWT.TOGGLE button. */
    public static final double TOGGLE_TEXTICON_HORIZONTAL_PADDING = 16.0;
    /** Size allocated for the icon in a text-and-icon SWT.TOGGLE button. */
    public static final double TOGGLE_TEXTICON_ICON_SIZE = 16.0;
    /** Spacing between the icon and text in a text-and-icon SWT.TOGGLE button. */
    public static final double TOGGLE_TEXTICON_ICON_SPACING = 6.0;

    // --- Calculation Methods ---

    /**
     * Calculates the required width for an SWT.PUSH button based on text length.
     * Formula: max(minWidth, (textLength * averageCharWidth) + (2 * horizontalPadding))
     * @param textLength Number of characters in the button text.
     * @return Calculated width.
     */
    public static double calculatePushWidth(int textLength) {
        double textWidth = textLength * AVERAGE_CHAR_WIDTH;
        double calculatedWidth = textWidth + (2 * PUSH_HORIZONTAL_PADDING);
        return Math.max(PUSH_MIN_WIDTH, calculatedWidth);
    }

    /**
     * Calculates the required width for an SWT.CHECK button based on text length.
     * Formula: checkboxSize + (textLength * averageCharWidth)
     * @param textLength Number of characters in the button text (can be 0).
     * @return Calculated width.
     */
    public static double calculateCheckWidth(int textLength) {
        double textWidth = textLength * AVERAGE_CHAR_WIDTH;
        return CHECK_CHECKBOX_SIZE + textWidth;
    }

    /**
     * Calculates the required width for an SWT.RADIO button based on text length.
     * Formula: radioButtonSize + (textLength * averageCharWidth)
     * @param textLength Number of characters in the button text (can be 0).
     * @return Calculated width.
     */
    public static double calculateRadioWidth(int textLength) {
        double textWidth = textLength * AVERAGE_CHAR_WIDTH;
        return RADIO_RADIOBUTTON_SIZE + textWidth;
    }

    /**
     * Calculates the required width for a text-only SWT.TOGGLE button based on text length.
     * Formula: max(minWidth, (textLength * averageCharWidth) + (2 * horizontalPadding))
     * @param textLength Number of characters in the button text.
     * @return Calculated width.
     */
    public static double calculateToggleTextOnlyWidth(int textLength) {
        double textWidth = textLength * AVERAGE_CHAR_WIDTH;
        double calculatedWidth = textWidth + (2 * TOGGLE_TEXT_HORIZONTAL_PADDING);
        return Math.max(TOGGLE_MIN_WIDTH, calculatedWidth);
    }

    /**
     * Calculates the required width for an icon-only SWT.TOGGLE button.
     * Formula: max(minWidth, iconOnlySize)
     * @return Calculated width.
     */
    public static double calculateToggleIconOnlyWidth() {
        return Math.max(TOGGLE_MIN_WIDTH, TOGGLE_ICON_ONLY_SIZE);
    }

    /**
     * Calculates the required width for a text-and-icon SWT.TOGGLE button based on text length.
     * Formula: max(minWidth, (textLength * averageCharWidth) + iconSize + iconSpacing + (2 * horizontalPadding))
     * @param textLength Number of characters in the button text.
     * @return Calculated width.
     */
    public static double calculateToggleTextAndIconWidth(int textLength) {
        double textWidth = textLength * AVERAGE_CHAR_WIDTH;
        double contentWidth = textWidth + TOGGLE_TEXTICON_ICON_SIZE + TOGGLE_TEXTICON_ICON_SPACING;
        double calculatedWidth = contentWidth + (2 * TOGGLE_TEXTICON_HORIZONTAL_PADDING);
        return Math.max(TOGGLE_MIN_WIDTH, calculatedWidth);
    }

    // Private constructor to prevent instantiation
    private ButtonSwtSizingConstants() {}

}
