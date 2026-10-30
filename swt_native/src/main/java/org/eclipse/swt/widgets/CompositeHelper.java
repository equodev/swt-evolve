package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;

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
        return bar != null && (!shownOnly || bar.getVisible() && hasRange(bar)) ? Sizes.SCROLL_BAR_SIZE : 0;
    }

    /**
     * The client draws a visible bar only while it has a range to scroll; the content takes the place of one it
     * does not. A Dart composite's bars are Dart bars on every backend (DartScrollable.createScrollBar).
     */
    private static boolean hasRange(ScrollBar bar) {
        return ((DartScrollBar) bar.getImpl()).hasRange;
    }

    /**
     * The bounds less the border and the bars that are shown, at the origin. A bar counts while it is set
     * visible, whether or not its window is on screen yet, and has a range to scroll.
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
     * the image or the colour of the control that does. An image repeats from that control's origin, moved by
     * the offsets, as upstream's pattern does. The GC keeps its background, background pattern and transform.
     */
    public static void fillBackground(DartComposite composite, GC gc, int x, int y, int width, int height, int offsetX, int offsetY) {
        Control control = composite.findBackgroundControl();
        if (control == null) {
            gc.fillRectangle(x, y, width, height);
            return;
        }
        Image image = ((DartControl) control.getImpl()).backgroundImage;
        if (image != null && !image.isDisposed()) {
            Point origin = originIn(composite.getApi(), control);
            fillWithImage(gc, image, x, y, width, height, origin.x - offsetX, origin.y - offsetY);
            return;
        }
        // setBackground drops the GC's background pattern, so the pattern is put back too.
        Pattern previousPattern = gc.getBackgroundPattern();
        Color previous = gc.getBackground();
        gc.setBackground(control.getBackground());
        gc.fillRectangle(x, y, width, height);
        gc.setBackground(previous);
        gc.setBackgroundPattern(previousPattern);
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

    /** Fills the rectangle with the image repeated edge to edge from (originX, originY). */
    private static void fillWithImage(GC gc, Image image, int x, int y, int width, int height, int originX, int originY) {
        Device device = gc.getDevice();
        Pattern previousPattern = gc.getBackgroundPattern();
        Pattern pattern = new Pattern(device, image);
        Transform previous = null;
        Transform shifted = null;
        try {
            // A pattern repeats from the GC's origin, so the GC is moved to where the tiles start.
            if (originX != 0 || originY != 0) {
                previous = new Transform(device);
                gc.getTransform(previous);
                shifted = new Transform(device);
                gc.getTransform(shifted);
                shifted.translate(originX, originY);
                gc.setTransform(shifted);
            }
            gc.setBackgroundPattern(pattern);
            gc.fillRectangle(x - originX, y - originY, width, height);
        } finally {
            gc.setBackgroundPattern(previousPattern);
            if (previous != null) {
                gc.setTransform(previous);
                previous.dispose();
                shifted.dispose();
            }
            pattern.dispose();
        }
    }
}
