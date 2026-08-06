package org.eclipse.e4.ui.workbench.renderers.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * Minimal stand-in for the e4 workbench layout of the same name, so a plain snippet can build the
 * trimmed window shape (main toolbar on top, status bar at the bottom, side bars, client area in
 * the middle) without pulling in the whole workbench.
 *
 * <p>Evolve routes a Composite to its {@code DartMainToolbar} / {@code DartStatusBar} /
 * {@code DartSideBar} implementation only when its parent Shell carries a layout of this exact
 * fully-qualified name <em>and</em> the Composite is being constructed from a method called
 * {@code getTrimComposite}. Both conditions are reproduced below; the trim fields are assigned
 * after the constructor returns, exactly as the real layout does, because Evolve depends on that
 * ordering to classify the trim by elimination.
 *
 * <p>Snippet-only: this class is never on the classpath of a real RCP run, where the genuine
 * workbench layout takes its place.
 */
public class TrimmedPartLayout extends Layout {

    public Composite top;
    public Composite bottom;
    public Composite left;
    public Composite right;
    public Composite clientArea;

    /** Creates the trim Composite for {@code side} (SWT.TOP/BOTTOM/LEFT/RIGHT). */
    public Composite getTrimComposite(Composite parent, int side) {
        Composite trim = new Composite(parent, SWT.NONE);
        switch (side) {
            case SWT.TOP:
                top = trim;
                break;
            case SWT.BOTTOM:
                bottom = trim;
                break;
            case SWT.LEFT:
                left = trim;
                break;
            default:
                right = trim;
                break;
        }
        return trim;
    }

    /** The area every non-trim part lives in. */
    public Composite getClientArea(Composite parent) {
        if (clientArea == null) {
            clientArea = new Composite(parent, SWT.NONE);
        }
        return clientArea;
    }

    @Override
    protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        return new Point(wHint == SWT.DEFAULT ? 800 : wHint, hHint == SWT.DEFAULT ? 600 : hHint);
    }

    @Override
    protected void layout(Composite composite, boolean flushCache) {
        Rectangle area = composite.getClientArea();
        int topH = heightOf(top, area.width);
        int bottomH = heightOf(bottom, area.width);
        int leftW = widthOf(left);
        int rightW = widthOf(right);
        int midY = area.y + topH;
        int midH = Math.max(0, area.height - topH - bottomH);

        if (top != null) top.setBounds(area.x, area.y, area.width, topH);
        if (bottom != null) bottom.setBounds(area.x, area.y + area.height - bottomH, area.width, bottomH);
        if (left != null) left.setBounds(area.x, midY, leftW, midH);
        if (right != null) right.setBounds(area.x + area.width - rightW, midY, rightW, midH);
        if (clientArea != null)
            clientArea.setBounds(area.x + leftW, midY, Math.max(0, area.width - leftW - rightW), midH);
    }

    private static int heightOf(Control control, int width) {
        return control == null ? 0 : control.computeSize(width, SWT.DEFAULT).y;
    }

    private static int widthOf(Control control) {
        return control == null ? 0 : control.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
    }
}
