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
        int width = 0;
        int height = 18;

        // Add text width if present
        if (c.text != null && !c.text.isEmpty()) {
            width += c.text.length() * 15;
        }

        // Add image dimensions if present
        if (c.image != null) {
            int imageWidth = c.image.getImpl()._width();
            int imageHeight = c.image.getImpl()._height();

            // If we have both text and image, add some spacing
            if (c.text != null && !c.text.isEmpty()) {
                width += 8; // spacing between image and text
            }

            width += imageWidth;
            height = Math.max(height, imageHeight);
        }

        return new Point(width, height);
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

    public static Point compute(DartTree t) {
        int columnCount = t.getColumnCount();
        int itemCount = t.getItemCount();

        int width = columnCount > 0 ? columnCount * 30 : 200;
        int height = itemCount * 20;

        return new Point(width, height);
    }

    public static Point getSize(DartScrollBar scrollBar) {
        return null;
    }

    public static Point computeSizeInPixels(DartComposite composite) {
        return new Point(10, 10); // TODO
    }

    public static Point compute(DartTable c) {
        return new Point(c.getColumnCount() * 70, c.getItemCount() * 20);
    }
}
