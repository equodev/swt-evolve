package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;

public class DartStatusBar extends DartComposite {

    public DartStatusBar(Composite parent, int style, Composite composite) {
        super(parent, style, composite);
    }

    /**
     * The trim side is only a guess at construction time when the Shell carries no e4 model -- e4
     * assigns the layout field after the constructor returns, so the side is classified by
     * elimination and a window with no bottom trim makes every trim built after the toolbar look
     * like the bottom one. Its real side is authoritative here (the field is populated by layout
     * time).
     */
    private boolean isSideRail() {
        int side = Config.trimSide(getApi().getParent(), getApi());
        return side == SWT.LEFT || side == SWT.RIGHT;
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        // A bar's width reported from a side slot is taken off the client area once per rail, which
        // leaves the window body nothing to lay out in.
        if (isSideRail()) return DartSideBar.railSize(this);
        return statusSize(this, wHint, hHint, changed);
    }

    /**
     * Size of a bottom trim bar. Evolve renders it at a fixed height, but its layout is still asked:
     * e4's TrimBarLayout caches the lines it wraps the bar into and rebuilds them only when
     * computeSize flushes, so skipping the call keeps the lines of the first, wider pass and lays
     * every contribution past the current width out of sight. Without a width hint there is
     * nothing to wrap against: the layout would put each contribution on a line of its own.
     * Shared with {@link DartMainToolbar}, which can land on the bottom trim.
     */
    static Point statusSize(DartComposite bar, int wHint, int hHint, boolean changed) {
        if (bar.layout != null && wHint != SWT.DEFAULT)
            bar.layout.computeSize(bar.getApi(), wHint, hHint, changed);
        return new Point(1650, 36);
    }
}
