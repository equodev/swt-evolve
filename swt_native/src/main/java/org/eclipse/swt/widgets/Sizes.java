package org.eclipse.swt.widgets;

import org.eclipse.swt.graphics.Point;

public class Sizes {
    public static Point compute(DartButton c) {
        return new Point(c.text.length()*15+20, 25);
    }
}
