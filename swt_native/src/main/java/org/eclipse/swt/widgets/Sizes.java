package org.eclipse.swt.widgets;

import org.eclipse.swt.custom.DartCCombo;
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
        int maxLength = 0;
        String[] items = c._items();
        if (items != null) {
            for (String item : items) {
                if (item != null && item.length() > maxLength) {
                    maxLength = item.length();
                }
            }
        }

        String text = c._text();
        int textLength = (text != null ? text.length() : 0);
        maxLength = Math.max(maxLength, textLength);

        int width = maxLength > 0 ? (int)(maxLength * AVERAGE_CHAR_WIDTH + 2 * HORIZONTAL_PADDING) : 100;
        width += 30;

        int height = 25;
        if ((c.getApi().style & org.eclipse.swt.SWT.SIMPLE) != 0) {
            int visibleCount = c.getApi().getVisibleItemCount();
            int itemCount = items != null ? items.length : 0;
            height = Math.min(visibleCount, itemCount) * 24 + 32;
        }

        return new Point(Math.max(width, 120), height);
    }

    public static Point compute(DartCCombo c) {
        int maxLength = 0;
        String[] items = c._items();
        if (items != null) {
            for (String item : items) {
                if (item != null && item.length() > maxLength) {
                    maxLength = item.length();
                }
            }
        }

        String text = c.__text();
        int textLength = (text != null ? text.length() : 0);
        maxLength = Math.max(maxLength, textLength);

        int width = maxLength > 0 ? (int)(maxLength * AVERAGE_CHAR_WIDTH + 2 * HORIZONTAL_PADDING) : 100;
        width += 30;

        int height = 25;

        return new Point(Math.max(width, 120), height);
    }

    public static Point compute(DartLabel c) {
        int width = 0;
        int height = 18;

        if (c.text != null && !c.text.isEmpty()) {
            width += c.text.length() * 15;
        }

        if (c.image != null) {
            int imageWidth = c.image.getImpl().getBounds().width;
            int imageHeight = c.image.getImpl().getBounds().height;

            if (c.text != null && !c.text.isEmpty()) {
                width += 8;
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

    public static Point compute(DartTabFolder impl) {
        TabItem[] items = impl.getItems();
        int itemCount = items != null ? items.length : 0;

        // Calculate width: each tab gets approximately 80 pixels
        int width = itemCount > 0 ? itemCount * 80 : 200;

        // Tab bar height is 28 pixels (same as CTabFolder)
        int height = 28;

        return new Point(width, height);
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
        return new Point(10, 10);
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
            int width = 200;
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
        boolean hasMenu = false;
        int maxMenuHeight = 0;

        if (children != null && children.length > 0) {
            for (Control child : children) {
                Point childSize = child.computeSize(org.eclipse.swt.SWT.DEFAULT, org.eclipse.swt.SWT.DEFAULT);
                childrenWidth += childSize.x;
                childrenHeight = Math.max(childrenHeight, childSize.y);

                if (child instanceof Combo || child instanceof org.eclipse.swt.custom.CCombo) {
                    hasCombo = true;
                    comboCount++;
                }

                Menu menu = child.getMenu();
                if (menu != null && menu.getImpl() instanceof DartMenu) {
                    hasMenu = true;
                    Point menuSize = compute((DartMenu) menu.getImpl());
                    maxMenuHeight = Math.max(maxMenuHeight, menuSize.y);
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
        int extraSpace = 0;

        if (children != null && children.length > 0) {
            int estimatedHeight = childrenHeight * Math.max(1, children.length / 2);
            height = estimatedHeight + titleHeightSpace + verticalPadding;

            if (hasCombo) {
                int dropdownSpace = 150;
                height += dropdownSpace;
            }

            if (hasMenu) {
                extraSpace += maxMenuHeight;
                height += extraSpace;
            }
        } else {
            height = childrenHeight + titleHeightSpace + verticalPadding;
        }

        int minHeight = hasCombo ? 200 : 100;
        return new Point(Math.max(width, 300), Math.max(height, minHeight));
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

        if (isVertical) {
            return new Point(48, 250);
        } else {
            return new Point(250, 48);
        }
    }

    public static Point compute(DartSpinner c) {
        return new Point(120, 32);
    }

    public static Point compute(DartScale c) {
        int style = c.getApi().style;
        boolean isVertical = (style & org.eclipse.swt.SWT.VERTICAL) != 0;

        if (isVertical) {
            return new Point(40, 200);
        } else {
            return new Point(200, 40);
        }
    }

    public static Point compute(DartProgressBar c) {
        int style = c.getApi().style;
        boolean isVertical = (style & org.eclipse.swt.SWT.VERTICAL) != 0;

        if (isVertical) {
            return new Point(20, 200);
        } else {
            return new Point(200, 20);
        }
    }

    public static Point compute(DartControl dartControl) {
        return new Point(10,10); // ToDo
    }
    public static Point compute(DartMenu c) {
        int style = c.getApi().style;
        boolean isBar = (style & org.eclipse.swt.SWT.BAR) != 0;
        boolean isDropDown = (style & org.eclipse.swt.SWT.DROP_DOWN) != 0;
        boolean isPopUp = (style & org.eclipse.swt.SWT.POP_UP) != 0;

        if (isBar) {
            return new Point(300, 28);
        } else if (isDropDown || isPopUp) {
            MenuItem[] items = c._items();
            int itemCount = items != null ? items.length : 1;

            int maxWidth = 150;
            if (items != null) {
                for (MenuItem item : items) {
                    if (item != null) {
                        String text = item.getText();
                        if (text != null && !text.isEmpty()) {
                            int textWidth = (int)(text.length() * AVERAGE_CHAR_WIDTH + 2 * HORIZONTAL_PADDING);
                            maxWidth = Math.max(maxWidth, textWidth + 40);
                        }
                    }
                }
            }

            int itemHeight = 24;
            int height = Math.max(50, itemCount * itemHeight + 16);

            return new Point(maxWidth, height);
        }

        return new Point(150, 50);
    }

    public static Point compute(DartCoolBar dartCoolBar) {
        return new Point(0,0);
    }

    public static Point compute(DartCoolItem dartCoolItem) {
        int defaultHeight = 25;

        // Try to get the preferred size first
        Point preferredSize = dartCoolItem.getApi().getPreferredSize();
        if (preferredSize != null && (preferredSize.x > 0 || preferredSize.y > 0)) {
            return new Point(
                preferredSize.x > 0 ? preferredSize.x : 50,
                preferredSize.y > 0 ? preferredSize.y : defaultHeight
            );
        }

        // Fall back to computing from control size
        Control control = dartCoolItem.getApi().getControl();
        if (control != null && !control.isDisposed()) {
            Point controlSize = control.getSize();
            if (controlSize.x > 0 && controlSize.y > 0) {
                int gripperWidth = 12;
                int padding = 8;

                CoolBar parent = dartCoolItem.getApi().getParent();
                boolean isVertical = parent != null && (parent.style & org.eclipse.swt.SWT.VERTICAL) != 0;

                if (isVertical) {
                    return new Point(controlSize.x + padding, controlSize.y + gripperWidth);
                } else {
                    return new Point(controlSize.x + gripperWidth + padding, controlSize.y + padding);
                }
            }
        }

        return new Point(50, defaultHeight);
    }
}
