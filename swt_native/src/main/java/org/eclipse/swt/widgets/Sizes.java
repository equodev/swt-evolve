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
        // Find the longest item in the combo
        int maxLength = 0;
        String[] items = c._items();
        if (items != null) {
            for (String item : items) {
                if (item != null && item.length() > maxLength) {
                    maxLength = item.length();
                }
            }
        }

        // Also consider the current text
        String text = c._text();
        int textLength = (text != null ? text.length() : 0);
        maxLength = Math.max(maxLength, textLength);

        // Calculate width based on the longest text
        int width = maxLength > 0 ? (int)(maxLength * AVERAGE_CHAR_WIDTH + 2 * HORIZONTAL_PADDING) : 100;
        // Add space for dropdown arrow
        width += 30;

        // For SIMPLE style, calculate height based on number of visible items
        int height = 25; // Default height for DROP_DOWN and READ_ONLY
        if ((c.getApi().style & org.eclipse.swt.SWT.SIMPLE) != 0) {
            int visibleCount = c.getApi().getVisibleItemCount();
            int itemCount = items != null ? items.length : 0;
            // Height should accommodate visible items
            height = Math.min(visibleCount, itemCount) * 24 + 32; // 24px per item + padding
        }

        Point result = new Point(Math.max(width, 120), height);
        System.out.println("Combo size calculated: width=" + result.x + ", height=" + result.y + ", items=" + (items != null ? items.length : 0) + ", maxLength=" + maxLength);
        return result;
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

        int titleWidth = (int)(textLength * AVERAGE_CHAR_WIDTH + 2 * HORIZONTAL_PADDING);

        Control[] children = c.getChildren();
        int childrenWidth = 0;
        int childrenHeight = 0;
        boolean hasCombo = false;
        int comboCount = 0;

        if (children != null && children.length > 0) {
            for (Control child : children) {
                Point childSize = child.computeSize(org.eclipse.swt.SWT.DEFAULT, org.eclipse.swt.SWT.DEFAULT);
                childrenWidth += childSize.x;
                childrenHeight = Math.max(childrenHeight, childSize.y);

                // Check if this child is a Combo
                if (child instanceof Combo) {
                    hasCombo = true;
                    comboCount++;
                }
            }
            if (children.length > 1) {
                childrenWidth += (children.length - 1) * 12;
            }
        }

        int CLIENT_INSET = 3;

        int horizontalPadding = (6 * 2) + (10 * 2) + (CLIENT_INSET * 2);
        int verticalPadding = (10 + 8 + 10) + (CLIENT_INSET * 2);
        int titleHeightSpace = 18;

        int width = Math.max(titleWidth, childrenWidth) + horizontalPadding;

        int height;
        if (children != null && children.length > 0) {
            int estimatedHeight = childrenHeight * Math.max(1, children.length / 2);
            height = estimatedHeight + titleHeightSpace + verticalPadding;

            // If the group contains Combos, add extra space for dropdown menus
            // Each combo needs approximately 150px to show ~5-6 dropdown items
            if (hasCombo) {
                int dropdownSpace = 150 * comboCount;
                height += dropdownSpace;
                System.out.println("Group contains " + comboCount + " Combo(s), adding " + dropdownSpace + "px for dropdowns");
            }
        } else {
            height = childrenHeight + titleHeightSpace + verticalPadding;
        }

        // Minimum size for a Group - ensure enough space for combo dropdowns
        // Use a more generous minimum width to accommodate dropdown menus
        int minHeight = hasCombo ? 200 : 100;
        Point result = new Point(Math.max(width, 300), Math.max(height, minHeight));
        System.out.println("Group size calculated: width=" + result.x + ", height=" + result.y + ", text='" + text + "', childrenWidth=" + childrenWidth + ", childrenCount=" + (children != null ? children.length : 0) + ", hasCombo=" + hasCombo);
        return result;
    }

    public static Point compute(DartExpandBar dartExpandBar) {
        ExpandItem[] items = dartExpandBar.getItems();
        int spacing = dartExpandBar._spacing();

        if (items == null || items.length == 0) {
            return new Point(200, 100);
        }

        int totalHeight = spacing;
        int maxWidth = 0;
        int headerHeight = 56;

        for (ExpandItem item : items) {
            DartExpandItem dartItem = (DartExpandItem) item.getImpl();
            totalHeight += headerHeight;

            String text = dartItem._text();
            int textLength = (text != null ? text.length() : 0);
            int itemWidth = (int)(textLength * AVERAGE_CHAR_WIDTH + 2 * HORIZONTAL_PADDING) + 40;
            maxWidth = Math.max(maxWidth, itemWidth);

            Control control = dartItem._control();
            if (control != null) {
                Rectangle controlBounds = ((DartControl)control.getImpl()).getBounds();
                int controlHeight = controlBounds.height > 0 ? controlBounds.height :
                                   control.computeSize(org.eclipse.swt.SWT.DEFAULT, org.eclipse.swt.SWT.DEFAULT).y;
                int controlWidth = controlBounds.width > 0 ? controlBounds.width :
                                  control.computeSize(org.eclipse.swt.SWT.DEFAULT, org.eclipse.swt.SWT.DEFAULT).x;

                totalHeight += controlHeight;
                maxWidth = Math.max(maxWidth, controlWidth + 32);
            }

            totalHeight += spacing;
        }

        maxWidth = Math.max(maxWidth, 200);
        return new Point(maxWidth, totalHeight);
    }

    public static Point compute(DartSlider c) {
        int style = c.getApi().style;
        boolean isVertical = (style & org.eclipse.swt.SWT.VERTICAL) != 0;

        // Same track length (250px), just different orientation
        if (isVertical) {
            // Vertical slider: width is thickness, height is track length
            return new Point(48, 250);
        } else {
            // Horizontal slider: width is track length, height is thickness
            return new Point(250, 48);
        }
    }

    public static Point compute(DartSpinner c) {
        // Spinner: text field + up/down buttons
        return new Point(120, 32);
    }

    public static Point compute(DartScale c) {
        int style = c.getApi().style;
        boolean isVertical = (style & org.eclipse.swt.SWT.VERTICAL) != 0;

        // Similar to Slider but can be slightly smaller since Scale is simpler
        if (isVertical) {
            // Vertical scale: width is thickness, height is track length
            return new Point(40, 200);
        } else {
            // Horizontal scale: width is track length, height is thickness
            return new Point(200, 40);
        }
    }

    public static Point compute(DartProgressBar c) {
        int style = c.getApi().style;
        boolean isVertical = (style & org.eclipse.swt.SWT.VERTICAL) != 0;

        // ProgressBar is thinner than Scale/Slider
        if (isVertical) {
            // Vertical progress bar: width is thickness, height is bar length
            return new Point(20, 200);
        } else {
            // Horizontal progress bar: width is bar length, height is thickness
            return new Point(200, 20);
        }
    }

}
