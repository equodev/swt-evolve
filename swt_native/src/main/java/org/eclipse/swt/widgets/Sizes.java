package org.eclipse.swt.widgets;

import dev.equo.swt.size.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.DartCCombo;
import org.eclipse.swt.custom.DartCLabel;
import org.eclipse.swt.custom.DartCTabFolder;
import org.eclipse.swt.custom.DartStyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class Sizes {
    private static final double AVERAGE_CHAR_WIDTH = 7.974;
    private static final double HORIZONTAL_PADDING = 12.0;

    public static Point compute(DartCTabFolder impl) {
        return new Point(impl.getItems().length * 80, 28);
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

    public static Point computeSize(DartButton c, int wHint, int hHint, boolean changed) {
        return ButtonSizes.computeSize(c, wHint, hHint, changed);
    }

    public static Point computeSize(DartCCombo c, int wHint, int hHint, boolean changed) {
        return CComboSizes.computeSize(c, wHint, hHint, changed);
    }

    public static Point computeSize(DartCLabel c, int wHint, int hHint, boolean changed) {
        return CLabelSizes.computeSize(c, wHint, hHint, changed);
    }

    public static Point computeSize(DartCombo c, int wHint, int hHint, boolean changed) {
        return ComboSizes.computeSize(c, wHint, hHint, changed);
    }

    public static Point computeSize(DartComposite composite, int wHint, int hHint, boolean changed) {
        Point size;
        if (composite.layout != null) {
            if (wHint == SWT.DEFAULT || hHint == SWT.DEFAULT) {
                changed |= (composite.getApi().state & DartWidget.LAYOUT_CHANGED) != 0;
                size = composite.layout.computeSize(composite.getApi(), wHint, hHint, changed);
                composite.getApi().state &= ~DartWidget.LAYOUT_CHANGED;
            } else {
                size = new Point(wHint, hHint);
            }
        } else {
            size = composite.minimumSize(wHint, hHint, changed);
            if (size.x == 0)
                size.x = DartWidget.DEFAULT_WIDTH;
            if (size.y == 0)
                size.y = DartWidget.DEFAULT_HEIGHT;
        }
        if (wHint != SWT.DEFAULT)
            size.x = wHint;
        if (hHint != SWT.DEFAULT)
            size.y = hHint;
        Rectangle trim = composite.computeTrim(0, 0, size.x, size.y);
        return new Point(trim.width, trim.height);
    }

    public static Point computeSize(DartControl dartControl, int wHint, int hHint, boolean changed) {
        return new Point(100, 100);
    }

    public static Point computeSize(DartCoolBar w, int wHint, int hHint, boolean changed) {
        return new Point(w.getItemCount() * 20, 28);
    }

    public static Point computeSize(DartCoolItem dartCoolItem, int wHint, int hHint) {
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

    public static Point computeSize(DartExpandBar dartExpandBar, int wHint, int hHint, boolean changed) {
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

    public static Point computeSize(DartLabel c, int wHint, int hHint, boolean changed) {
        return LabelSizes.computeSize(c, wHint, hHint, changed);
    }

    public static Point computeSize(DartLink c, int wHint, int hHint, boolean changed) {
        return LinkSizes.computeSize(c, wHint, hHint, changed);
    }

    public static Point computeSize(DartList c, int wHint, int hHint, boolean changed) {
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

    public static Point computeSize(DartProgressBar c, int wHint, int hHint, boolean changed) {
        return ProgressBarSizes.computeSize(c, wHint, hHint, changed);
    }

    public static Point computeSize(DartSash w, int wHint, int hHint, boolean changed) {
        if (wHint != SWT.DEFAULT && wHint < 0) wHint = 0;
        if (hHint != SWT.DEFAULT && hHint < 0) hHint = 0;

        int DEFAULT_CROSS_AXIS_SIZE = 3;
        int border = w.getBorderWidth();
        int width = border * 2;
        int height = border * 2;

        if ((w.getApi().style & SWT.HORIZONTAL) != 0) {
            width += 64;
            height += DEFAULT_CROSS_AXIS_SIZE;
        } else {
            width += DEFAULT_CROSS_AXIS_SIZE;
            height += 64;
        }

        if (wHint != SWT.DEFAULT) width = wHint + (border * 2);
        if (hHint != SWT.DEFAULT) height = hHint + (border * 2);

        return new Point(width, height);
    }

    public static Point computeSize(DartScale c, int wHint, int hHint, boolean changed) {
        int style = c.getApi().style;
        boolean isVertical = (style & org.eclipse.swt.SWT.VERTICAL) != 0;

        if (isVertical) {
            return new Point(40, 200);
        } else {
            return new Point(200, 40);
        }
    }

    public static Point computeSize(DartSlider c, int wHint, int hHint, boolean changed) {
        int style = c.getApi().style;
        boolean isVertical = (style & org.eclipse.swt.SWT.VERTICAL) != 0;

        if (isVertical) {
            return new Point(48, 250);
        } else {
            return new Point(250, 48);
        }
    }

    public static Point computeSize(DartSpinner c, int wHint, int hHint, boolean changed) {
        return new Point(120, 32);
    }

    public static Point computeSize(DartStyledText c, int wHint, int hHint, boolean changed) {
        return new Point(c.getText().length()*15+20, 25);
    }

    public static Point computeSize(DartTable c, int wHint, int hHint, boolean changed) {
        return new Point(c.getColumnCount() * 70, c.getItemCount() * 20);
    }

    public static Point computeSize(DartTabFolder w, int wHint, int hHint, boolean flushCache) {
        Control[] children = w._getChildren();
        int width = 0, height = 0;
        for (int i = 0; i < w.children.length; i++) {
            Control child = children[i];
            int index = 0;
            int count = w.getItemCount();
            while (index < count) {
                if (((DartTabItem) w.items[index].getImpl()).control == child)
                    break;
                index++;
            }
            if (index == count) {
                Rectangle rect = child.getBounds();
                width = Math.max(width, rect.x + rect.width);
                height = Math.max(height, rect.y + rect.height);
            } else {
                Point size = child.computeSize(wHint, hHint, flushCache);
                width = Math.max(width, size.x);
                height = Math.max(height, size.y);
            }
        }
        return new Point(width, height);
    }

    public static Point computeSize(DartToolBar c, int wHint, int hHint, boolean changed) {
        return new Point(c.getItems().length * 20, 20);
    }

    public static Point computeSize(DartToolItem w) {
        int width = 0, height = 0;
        if ((w.getApi().style & SWT.SEPARATOR) != 0) {
            // In the unified toolbar case the width is ignored if 0, DEFAULT, or SEPARATOR_FILL.
            if ((w.parent.style & SWT.HORIZONTAL) != 0) {
                width = w.getWidth();
                if (width <= 0)
                    width = 6;
                height = DartToolItem.DEFAULT_HEIGHT;
            } else {
                width = DartToolItem.DEFAULT_WIDTH;
                height = w.getWidth();
                if (height <= 0)
                    height = 6;
            }
            if (w.control != null) {
                height = Math.max(height, 0);
            }
        } else {
            if (w.text.length() != 0 || w.image != null) {
            } else {
                width = DartToolItem.DEFAULT_WIDTH;
                height = DartToolItem.DEFAULT_HEIGHT;
            }
            if ((w.getApi().style & SWT.DROP_DOWN) != 0) {
                width += 5 + 3;
            }
            {
                height -= 2;
            }
        }
        return new Point(width, height);
    }

    public static Point computeSize(DartTree t, int wHint, int hHint, boolean changed) {
        int columnCount = t.getColumnCount();
        int itemCount = t.getItemCount();

        int width = columnCount > 0 ? columnCount * 30 : 200;
        int height = itemCount * 20;

        return new Point(width, height);
    }

    public static Point computeSize(DartText c, int wHint, int hHint, boolean changed) {
        return TextSizes.computeSize(c, wHint, hHint, changed);
    }

    public static Rectangle computeTrim(DartGroup widget, int x, int y, int width, int height) {
        return new Rectangle(x, y, width+5+5, height+5+5);
    }

    public static Rectangle computeTrim(DartScrollable widget, int x, int y, int width, int height) {
        return new Rectangle(x, y, width, height);
    }

    public static Rectangle getBounds(DartTableItem item) {
        Table parent = item._parent();
        int itemIndex = parent.indexOf(item.getApi());
        if (itemIndex == -1)
            return new Rectangle(0, 0, 0, 0);
        DartTable dartTable = (DartTable) parent.getImpl();
        int itemHeight = dartTable.getItemHeight();
        if (itemHeight <= 0)
            itemHeight = 20;
        int headerHeight = dartTable.getHeaderHeight();
        int y = headerHeight + (itemIndex * itemHeight);
        Rectangle parentBounds = parent.getBounds();
        int width = parentBounds != null ? parentBounds.width : 100;
        return new Rectangle(0, y, width, itemHeight);
    }

    public static Rectangle getBounds(DartTableItem item, int index) {
        Table parent = item._parent();
        DartTable dartTable = (DartTable) parent.getImpl();
        int columnCount = dartTable.columnCount;
        if (!(0 <= index && index < Math.max(1, columnCount)))
            return new Rectangle(0, 0, 0, 0);
        int itemIndex = parent.indexOf(item.getApi());
        if (itemIndex == -1)
            return new Rectangle(0, 0, 0, 0);
        int itemHeight = dartTable.getItemHeight();
        if (itemHeight <= 0)
            itemHeight = 20;
        int headerHeight = dartTable.getHeaderHeight();
        int y = headerHeight + (itemIndex * itemHeight);
        int x = 0;
        int width = 100;
        if (columnCount > 0) {
            TableColumn[] columns = dartTable.getColumns();
            for (int i = 0; i < index && i < columns.length; i++) {
                x += columns[i].getWidth();
            }
            if (index < columns.length) {
                width = columns[index].getWidth();
            }
        } else {
            Rectangle parentBounds = parent.getBounds();
            width = parentBounds != null ? parentBounds.width : 100;
        }
        return new Rectangle(x, y, width, itemHeight);
    }

    public static Rectangle getTextBounds(DartTableItem item, int index) {
        Table parent = item._parent();
        DartTable dartTable = (DartTable) parent.getImpl();
        int columnCount = dartTable.columnCount;
        if (!(0 <= index && index < Math.max(1, columnCount)))
            return new Rectangle(0, 0, 0, 0);
        Rectangle cellBounds = getBounds(item, index);
        if (cellBounds == null)
            return new Rectangle(0, 0, 0, 0);
        Image[] images = item._images();
        Image image = index == 0 ? item.getImage() : (images != null && index < images.length) ? images[index] : null;
        int imageWidth = 0;
        if (image != null) {
            Rectangle imageBounds = image.getBounds();
            imageWidth = imageBounds.width + 4;
        }
        return new Rectangle(cellBounds.x + imageWidth, cellBounds.y, Math.max(0, cellBounds.width - imageWidth), cellBounds.height);
    }

    public static Rectangle getBounds(DartToolItem item) {
        Point size = computeSize(item);
        return new Rectangle(0, 0, size.x, size.y);
    }

    public static Rectangle getClientArea(DartCoolItem widget) {
        Rectangle b = widget.getBounds();
        return new Rectangle(0, 28, b.width, b.height-28);
    }

    public static Rectangle getClientArea(DartCTabFolder widget) {
        Rectangle b = widget.getBounds();
        return new Rectangle(0, 28, b.width, b.height-28);
    }

    public static Rectangle getClientArea(DartGroup widget) {
        Rectangle b = widget.getBounds();
        return new Rectangle(5, 5, Math.max(0, b.width-5-5), Math.max(0, b.height-5-5));
    }

    public static Rectangle getClientArea(DartScrollable widget) {
        return widget.getBounds();
    }

    public static Point getSize(DartScrollBar scrollBar) {
        return null;
    }

    public static Point minimumSize(DartComposite composite, int wHint, int hHint, boolean changed) {
        Rectangle clientArea = composite.getClientArea();
        int width = 0, height = 0;
        for (Control child : composite._getChildren()) {
            Rectangle rect = child.getBounds();
            width = Math.max(width, rect.x - clientArea.x + rect.width);
            height = Math.max(height, rect.y - clientArea.y + rect.height);
        }
        return new Point(width, height);
    }

    public static Point minimumSize(DartTabFolder w, int wHint, int hHint, boolean flushCache) {
        Control[] children = w._getChildren();
        int width = 0, height = 0;
        for (int i = 0; i < children.length; i++) {
            Control child = children[i];
            int index = 0;
            int count = w.getItemCount();
            while (index < count) {
                if (((DartTabItem) w.items[index].getImpl()).control == child)
                    break;
                index++;
            }
            if (index == count) {
                Rectangle rect = child.getBounds();
                width = Math.max(width, rect.x + rect.width);
                height = Math.max(height, rect.y + rect.height);
            } else {
                Point size = child.computeSize(wHint, hHint, flushCache);
                width = Math.max(width, size.x);
                height = Math.max(height, size.y);
            }
        }
        return new Point(width, height);
    }

}