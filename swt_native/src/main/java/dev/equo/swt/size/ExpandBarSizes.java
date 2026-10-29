package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GCHelper;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.DartExpandItem;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;

/**
 * How an ExpandBar sizes itself: SWT's sum of its items' headers and open bodies, each header as
 * wide as its label plus what the render side draws around it.
 *
 * DO NOT EDIT MANUALLY - regenerate from measure_expandbar.dart
 */
public class ExpandBarSizes {

    /** SWT's size on an axis with nothing to measure. */
    public static final int DEFAULT_SIZE = 64;

    /** What a header draws around its label: the insets on both sides and the chevron. */
    public static final int HEADER_LABEL_SURROUND = 36;

    /** The gap between a header's image and its label. */
    public static final int HEADER_IMAGE_GAP = 6;

    /** The chevron's size, which is also the least a header's band is tall. */
    public static final int CHEVRON_SIZE = 24;

    /** The style a header's label is drawn in, which is the font to measure it in. */
    public static TextStyle headerLabelStyle() {
        return new TextStyle("Inter", 14, false, 500);
    }

    /** SWT's band: the chevron, or a line of the bar's font when that is taller. */
    public static int bandHeight(ExpandBar bar) {
        GC gc = new GC(bar);
        try {
            return Math.max(CHEVRON_SIZE, gc.getFontMetrics().getHeight());
        } finally {
            gc.dispose();
        }
    }

    /**
     * SWT's computeSize: the headers, the spacing and the open bodies add up, the hints replace the
     * sums, and the trim comes last.
     */
    public static Point computeSize(ExpandBar bar, int wHint, int hHint) {
        int width = 0;
        int height = 0;
        if (wHint == SWT.DEFAULT || hHint == SWT.DEFAULT) {
            ExpandItem[] items = bar.getItems();
            if (items.length > 0) {
                int spacing = bar.getSpacing();
                height += spacing;
                for (ExpandItem item : items) {
                    height += item.getHeaderHeight();
                    if (item.getExpanded()) {
                        height += item.getHeight();
                    }
                    height += spacing;
                    width = Math.max(width, headerWidth(bar, item));
                }
            }
        }
        if (width == 0) {
            width = DEFAULT_SIZE;
        }
        if (height == 0) {
            height = DEFAULT_SIZE;
        }
        if (wHint != SWT.DEFAULT) {
            width = wHint;
        }
        if (hHint != SWT.DEFAULT) {
            height = hHint;
        }
        Rectangle trim = bar.computeTrim(0, 0, width, height);
        return new Point(trim.width, trim.height);
    }

    /**
     * A header's width: its label and what surrounds it, plus the image and its gap. The image is
     * as wide as it was when it was set, as in SWT, so disposing it later changes nothing.
     */
    public static int headerWidth(ExpandBar bar, ExpandItem item) {
        int width = HEADER_LABEL_SURROUND;
        if (item.getImage() != null) {
            width += ((DartExpandItem) item.getImpl())._imageWidth() + HEADER_IMAGE_GAP;
        }
        String text = item.getText();
        if (text.length() > 0) {
            width += labelWidth(bar, text);
        }
        return width;
    }

    private static int labelWidth(ExpandBar bar, String text) {
        Font font = bar.getFont();
        if (Config.getConfigFlags().use_swt_fonts) {
            return GCHelper.textExtent(text, 0, font).x;
        }
        PointD size = FontMetricsUtil.getFontSize(text, headerLabelStyle().withStyleFrom(font));
        return (int) Math.ceil(size.x());
    }
}
