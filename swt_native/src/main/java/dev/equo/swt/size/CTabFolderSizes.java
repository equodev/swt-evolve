package dev.equo.swt.size;

/**
 * What a CTabFolder lays out around the page it shows. Java hands the page exactly the
 * area these leave, so the numbers are the render side's rather than a second copy.
 *
 * DO NOT EDIT MANUALLY - regenerate from measure_ctabfolder.dart
 */
public class CTabFolderSizes {

    /** Height of the tab strip, on whichever edge the tabs sit. */
    public static final int TAB_STRIP_HEIGHT = 32;

    /** The frame drawn on the three edges the strip does not take. */
    public static final int BODY_BORDER = 2;

    /** The same frame, as SWT.BORDER widens it. */
    public static final int BODY_BORDER_STYLED = 3;

    /** What a tab draws around its label, both sides together. */
    public static final int TAB_LABEL_SURROUND = 14;

    /** What a close button adds to a tab's width, including its gap to the label. */
    public static final int TAB_CLOSE_EXTRA = 30;

    /** What an image adds to a tab's width, including its gap to the label. */
    public static final int TAB_IMAGE_EXTRA = 19;

    /** How tall an image on a tab is drawn, which is what sets the tab's height. */
    public static final int TAB_IMAGE_HEIGHT = 16;

    /** The style a tab's label is drawn in, which is the font to measure it in. */
    public static TextStyle tabLabelStyle() {
        return new TextStyle("Inter", 14, false, 500);
    }
}
