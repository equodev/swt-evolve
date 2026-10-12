package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/** What a Composite does that its generated implementation cannot say by itself. */
public final class CompositeHelper {

    /** The outline composite_evolve.dart draws inside the bounds of a BORDER composite. */
    static final int BORDER_WIDTH = 1;

    private CompositeHelper() {
    }

    /**
     * Whether the composite frames its content the way upstream's scroll view does: a plain Composite or
     * Canvas does, a widget built on one (StyledText, ScrolledComposite, CCombo...) keeps its own trim.
     */
    static boolean framesItsContent(DartScrollable widget) {
        Class<?> type = widget.getClass();
        return type == DartComposite.class || type == DartCanvas.class;
    }

    /** The border on each side, then the thickness of the vertical bar and of the horizontal one. */
    private static int[] frame(DartComposite composite, boolean shownBarsOnly) {
        int border = (composite.getApi().style & SWT.BORDER) != 0 ? BORDER_WIDTH : 0;
        return new int[] {border, bar(composite.verticalBar, shownBarsOnly), bar(composite.horizontalBar, shownBarsOnly)};
    }

    private static int bar(ScrollBar bar, boolean shownOnly) {
        return bar != null && (!shownOnly || bar.getVisible()) ? Sizes.SCROLL_BAR_SIZE : 0;
    }

    /**
     * The bounds less the border and the bars that are shown, at the origin. A bar counts while it is set
     * visible, whether or not its window is on screen yet.
     */
    static Rectangle getClientArea(DartComposite composite) {
        Rectangle bounds = composite.getBounds();
        int[] frame = frame(composite, true);
        return new Rectangle(0, 0, Math.max(0, bounds.width - 2 * frame[0] - frame[1]),
                Math.max(0, bounds.height - 2 * frame[0] - frame[2]));
    }

    /** The border around the rectangle and room for every bar the style asks for, shown or not. */
    static Rectangle computeTrim(DartComposite composite, int x, int y, int width, int height) {
        int[] frame = frame(composite, false);
        return new Rectangle(x - frame[0], y - frame[0], width + 2 * frame[0] + frame[1], height + 2 * frame[0] + frame[2]);
    }

    /**
     * Composite.drawBackground: the GC's own background where nothing paints behind the composite, otherwise
     * the image or the colour of the control that does. An image is tiled from that control's origin, moved by
     * the offsets. The GC keeps its background.
     */
    public static void fillBackground(DartComposite composite, GC gc, int x, int y, int width, int height, int offsetX, int offsetY) {
        Control control = composite.findBackgroundControl();
        if (control == null) {
            gc.fillRectangle(x, y, width, height);
            return;
        }
        DartControl owner = (DartControl) control.getImpl();
        Image image = owner.backgroundImage;
        if (image != null && !image.isDisposed()) {
            Point origin = originIn(composite.getApi(), control);
            tile(gc, image, x, y, width, height, origin.x - offsetX, origin.y - offsetY);
            return;
        }
        Color previous = gc.getBackground();
        gc.setBackground(control.getBackground());
        gc.fillRectangle(x, y, width, height);
        gc.setBackground(previous);
    }

    /** Where the client area of the control or of one of its ancestors starts, in the control's own coordinates. */
    static Point originIn(Control control, Control ancestor) {
        int x = 0, y = 0;
        for (Control c = control; c != ancestor; c = c.getParent()) {
            Rectangle bounds = c.getBounds();
            x -= bounds.x;
            y -= bounds.y;
        }
        return new Point(x, y);
    }

    /** Fills the rectangle with copies of the image laid edge to edge from (originX, originY). */
    static void tile(GC gc, Image image, int x, int y, int width, int height, int originX, int originY) {
        if (width <= 0 || height <= 0)
            return;
        Rectangle size = image.getBounds();
        int startX = originX + Math.floorDiv(x - originX, size.width) * size.width;
        int startY = originY + Math.floorDiv(y - originY, size.height) * size.height;
        for (int tileY = startY; tileY < y + height; tileY += size.height) {
            int top = Math.max(y, tileY), bottom = Math.min(y + height, tileY + size.height);
            for (int tileX = startX; tileX < x + width; tileX += size.width) {
                int left = Math.max(x, tileX), right = Math.min(x + width, tileX + size.width);
                gc.drawImage(image, left - tileX, top - tileY, right - left, bottom - top, left, top, right - left, bottom - top);
            }
        }
    }
}
