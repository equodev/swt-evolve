package org.eclipse.e4.ui.workbench.renderers.swt;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

/**
 * Test-only same-named stand-in for the real Eclipse e4 workbench class, which isn't a
 * dependency of this module. Evolve recognises the perspective's main sash area by a layout of
 * this exact fully-qualified class name.
 */
public class SashLayout extends Layout {

    @Override
    protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        return new Point(0, 0);
    }

    @Override
    protected void layout(Composite composite, boolean flushCache) {
    }
}
