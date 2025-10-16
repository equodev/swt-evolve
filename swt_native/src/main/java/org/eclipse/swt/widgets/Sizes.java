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

    public static Point compute(DartList c) {
        int maxLength = 0;
        String[] items = c._items();
        if (items != null) {
            for (String item : items) {
                if (item != null && item.length() > maxLength) {
                    maxLength = item.length();
                }
            }
        }

        int width = maxLength > 0 ? (int)(maxLength * AVERAGE_CHAR_WIDTH + 2 * HORIZONTAL_PADDING) : 100;
        int height = c.getItemCount() * 24;

        return new Point(width, height);
    }

    public static Point compute(DartText c) {
        String text = c._text();
        int textLength = (text != null ? text.length() : 0);

        if ((c.getApi().style & org.eclipse.swt.SWT.MULTI) != 0) {
            int width = 200; // Default width
            if (text != null && !text.isEmpty()) {
                String[] lines = text.split("\n");
                int maxLineLength = 0;
                for (String line : lines) {
                    if (line.length() > maxLineLength) {
                        maxLineLength = line.length();
                    }
                }
                width = (int)(maxLineLength * AVERAGE_CHAR_WIDTH + 2 * HORIZONTAL_PADDING);
            }
            return new Point(Math.max(width, 200), 120);
        }

        int width = (int)(textLength * AVERAGE_CHAR_WIDTH + 2 * HORIZONTAL_PADDING);

        if ((c.getApi().style & org.eclipse.swt.SWT.SEARCH) != 0) {
            width += 64;
        }

        return new Point(Math.max(width, 100), 32);
    }

    public static Point compute(DartLink c) {
        String text = c._text();
        if (text == null || text.isEmpty()) {
            return new Point(100, 32);
        }

        // Remove HTML tags to calculate actual visible text length
        String plainText = text.replaceAll("<a[^>]*>", "").replaceAll("</a>", "");
        int textLength = plainText.length();

        int width = (int)(textLength * AVERAGE_CHAR_WIDTH + 2 * HORIZONTAL_PADDING);

        return new Point(Math.max(width, 100), 32);
    }

    public static Point compute(DartGroup c) {
        String text = c._text();
        int textLength = (text != null ? text.length() : 0);

        // Calculate minimum width based on title text
        // Title needs space for text plus padding on both sides
        int titleWidth = (int)(textLength * AVERAGE_CHAR_WIDTH + 2 * HORIZONTAL_PADDING);

        // Calculate size needed for children
        Control[] children = c.getChildren();
        int childrenWidth = 0;
        int childrenHeight = 0;

        if (children != null && children.length > 0) {
            // Sum the widths of all children (assuming horizontal layout)
            for (Control child : children) {
                Point childSize = child.computeSize(org.eclipse.swt.SWT.DEFAULT, org.eclipse.swt.SWT.DEFAULT);
                childrenWidth += childSize.x;
                childrenHeight = Math.max(childrenHeight, childSize.y);
            }
            // Add spacing between children: 12px SizedBox between each pair
            if (children.length > 1) {
                childrenWidth += (children.length - 1) * 12;
            }
        }

        // Group needs CLIENT_INSET on all sides plus additional padding
        int CLIENT_INSET = 3;
        // Padding calculation based on Flutter implementation:
        // Container padding: left=6, right=6 + Inner Padding: all=10
        int horizontalPadding = (6 * 2) + (10 * 2) + (CLIENT_INSET * 2); // 12 + 20 + 6 = 38
        int verticalPadding = (10 + 8 + 10) + (CLIENT_INSET * 2); // top=10, bottom=8, inner=10 + 6 = 34
        int titleHeightSpace = 18; // Space for the title at the top (8 margin + 10 for text)

        // Width is the maximum of title width and children width, plus horizontal padding
        int width = Math.max(titleWidth, childrenWidth) + horizontalPadding;

        // Height calculation - if children exist, be more generous
        int height;
        if (children != null && children.length > 0) {
            // For groups with children, multiply height by number of children to account for vertical layouts
            // This is a heuristic since we don't know the actual layout type
            int estimatedHeight = childrenHeight * Math.max(1, children.length / 2);
            height = estimatedHeight + titleHeightSpace + verticalPadding;
        } else {
            height = childrenHeight + titleHeightSpace + verticalPadding;
        }

        // Minimum size for a Group - increased minimum height
        return new Point(Math.max(width, 150), Math.max(height, 100));
    }
}
