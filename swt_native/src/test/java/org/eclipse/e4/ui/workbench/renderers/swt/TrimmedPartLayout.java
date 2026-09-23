package org.eclipse.e4.ui.workbench.renderers.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

/**
 * Test-only same-named stand-in for the real Eclipse e4 workbench class, which isn't a
 * dependency of this module.
 *
 * <p>Evolve routes a Composite to its {@code DartMainToolbar} / {@code DartStatusBar} /
 * {@code DartSideBar} implementation only when its parent Shell carries a layout of this exact
 * fully-qualified name <em>and</em> the Composite is constructed from a method called
 * {@code getTrimComposite}. Both are reproduced here, and the trim field is assigned only after
 * the constructor returns, exactly as the real layout does -- Evolve classifies the trim by
 * elimination over the fields already assigned, so that ordering is part of the contract.
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
            case SWT.TOP -> top = trim;
            case SWT.BOTTOM -> bottom = trim;
            case SWT.LEFT -> left = trim;
            default -> right = trim;
        }
        return trim;
    }

    @Override
    protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        return new Point(0, 0);
    }

    @Override
    protected void layout(Composite composite, boolean flushCache) {
    }
}
