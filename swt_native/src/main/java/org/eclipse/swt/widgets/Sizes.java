package org.eclipse.swt.widgets;

import org.eclipse.swt.custom.DartCTabFolder;
import org.eclipse.swt.custom.DartStyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class Sizes {
    private static final double AVERAGE_CHAR_WIDTH = 7.974;
    private static final double HORIZONTAL_PADDING = 12.0;
    
    public static Point compute(DartButton c) {
        double textWidth = (c.text != null ? c.text.length() : 0) * AVERAGE_CHAR_WIDTH;
        
        double totalWidth = textWidth + (2 * HORIZONTAL_PADDING);
        
        return new Point((int) totalWidth, 25);
    }

    public static Point compute(DartCombo c) {
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

    public static Point compute(DartStyledText c) {
        return new Point(c.getText().length()*15+20, 25);
    }

    public static Rectangle getBounds(DartToolItem item) {
        return new Rectangle(0,0,0,0);
    }

    public static Point getSize(DartScrollBar scrollBar) {
        return null;
    }

    public static Point computeSizeInPixels(DartComposite composite) {
        return new Point(10, 10); // TODO
    }
}
