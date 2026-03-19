package org.eclipse.swt.widgets;

import org.eclipse.swt.graphics.Point;

public class DartStatusBar extends DartComposite {

    public DartStatusBar(Composite parent, int style, Composite composite) {
        super(parent, style, composite);
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        return new Point(1650, 36);
    }
}
