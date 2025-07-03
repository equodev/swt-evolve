package org.eclipse.swt.widgets;

import org.eclipse.swt.custom.DartCTabFolder;
import org.eclipse.swt.graphics.Point;

public class Sizes {
    public static Point compute(DartButton c) {
        return new Point(c.text.length()*15+20, 25);
    }

    public static Point compute(DartLabel c) {
        return new Point(c.text.length()*15, 18);
    }

    public static Point compute(DartToolBar c) {
        return new Point(c.getItems().length * 20, 20);
    }

    public static Point compute(DartCTabFolder impl) {
        return new Point(impl.getItems().length * 80, 28);
    }
}
