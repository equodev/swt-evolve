package org.eclipse.swt.widgets;

import dev.equo.swt.size.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.DartCCombo;
import org.eclipse.swt.custom.DartCLabel;
import org.eclipse.swt.custom.DartCTabFolder;
import org.eclipse.swt.custom.DartStyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GCHelper;
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

    public static Point computeSize(DartStyledText c, int wHint, int hHint, boolean changed) {
        StyledTextContent content = c._content();
        int lc = (c.getApi().getStyle() & SWT.SINGLE) != 0 ? 1 : content.getLineCount();
        int width = 0;
        int height = 0;
        if (wHint == SWT.DEFAULT || hHint == SWT.DEFAULT) {
            Font font = c.getFont();
            for (int lineIndex = 0; lineIndex < lc; lineIndex++) {
                String line = content.getLine(lineIndex);
                Point extent = GCHelper.textExtent(line != null ? line : "", SWT.DRAW_TAB, font);
                width = Math.max(width, extent.x);
                height += extent.y;
            }
        }
        if (width == 0) width = DartWidget.DEFAULT_WIDTH;
        if (height == 0) height = DartWidget.DEFAULT_HEIGHT;
        if (wHint != SWT.DEFAULT) width = wHint;
        if (hHint != SWT.DEFAULT) height = hHint;
        int wTrim = c._leftMargin() + c._rightMargin();
        Caret caret = c.getCaret();
        if (caret != null) wTrim += caret.getSize().x;
        int hTrim = c._topMargin() + c._bottomMargin();
        Rectangle rect = c.computeTrim(0, 0, width + wTrim, height + hTrim);
        return new Point(rect.width, rect.height);
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
        return SashSizes.computeSize(w, wHint, hHint, changed);
    }

    public static Point computeSize(DartScale c, int wHint, int hHint, boolean changed) {
        return ScaleSizes.computeSize(c, wHint, hHint, changed);
    }

    public static Point computeSize(DartSlider c, int wHint, int hHint, boolean changed) {
        return SliderSizes.computeSize(c, wHint, hHint, changed);
    }

    public static Point computeSize(DartSpinner c, int wHint, int hHint, boolean changed) {
        return new Point(120, 32);
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
        ToolItem[] items = c.getItems();
        int itemCount = items.length;

        if (itemCount == 0) {
            return new Point(DartToolItem.DEFAULT_WIDTH, DartToolItem.DEFAULT_HEIGHT);
        }

        boolean isVertical = (c.getApi().style & SWT.VERTICAL) != 0;

        int totalWidth = 0;
        int totalHeight = 0;
        int maxWidth = 0;
        int maxHeight = 0;

        for (ToolItem item : items) {
            DartToolItem dartItem = (DartToolItem) item.getImpl();
            Point itemSize = computeSize(dartItem);

            totalWidth += itemSize.x;
            totalHeight += itemSize.y;
            maxWidth = Math.max(maxWidth, itemSize.x);
            maxHeight = Math.max(maxHeight, itemSize.y);
        }

        int width, height;
        if (isVertical) {
            // Vertical toolbar: width = max item width, height = sum of heights
            width = maxWidth;
            height = totalHeight;
        } else {
            // Horizontal toolbar: width = sum of widths, height = max item height
            width = totalWidth;
            height = maxHeight;
        }

        // Apply hints if provided
        if (wHint != SWT.DEFAULT) {
            width = wHint;
        }
        if (hHint != SWT.DEFAULT) {
            height = hHint;
        }

        return new Point(width, height);
    }

    public static Point computeSize(DartToolItem w) {
        int width = 0, height = 0;
        boolean isSeparator = (w.getApi().style & SWT.SEPARATOR) != 0;
        boolean hasText = w.text != null && w.text.length() != 0;
        boolean hasImage = w.image != null;

        if (isSeparator) {
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
            if (hasText || hasImage) {
                // Calculate size based on image and/or text
                int imageWidth = 0, imageHeight = 0;
                int textWidth = 0, textHeight = 0;

                if (hasImage) {
                    Rectangle imageBounds = w.image.getBounds();
                    imageWidth = imageBounds.width;
                    imageHeight = imageBounds.height;
                }

                if (hasText) {
                    Point textSize = GCHelper.textExtent(w.text, SWT.DRAW_MNEMONIC, w.parent.getFont());
                    textWidth = textSize.x;
                    textHeight = textSize.y;
                }

                // Check if toolbar has RIGHT style (text beside image) or default (text below image)
                boolean rightStyle = (w.parent.style & SWT.RIGHT) != 0;

                if (hasImage && hasText) {
                    if (rightStyle) {
                        // Text beside image: width = image + spacing + text, height = max
                        width = imageWidth + DartToolItem.INSET + textWidth;
                        height = Math.max(imageHeight, textHeight);
                    } else {
                        // Text below image: width = max, height = image + spacing + text
                        width = Math.max(imageWidth, textWidth);
                        height = imageHeight + DartToolItem.INSET + textHeight;
                    }
                } else if (hasImage) {
                    width = imageWidth;
                    height = imageHeight;
                } else if (hasText) {
                    width = textWidth;
                    height = textHeight;
                }

                // Add padding/inset around the content
                width += DartToolItem.INSET * 2;
                height += DartToolItem.INSET * 2;

                // Ensure minimum size
                width = Math.max(width, DartToolItem.DEFAULT_WIDTH);
                height = Math.max(height, DartToolItem.DEFAULT_HEIGHT);
            } else {
                width = DartToolItem.DEFAULT_WIDTH;
                height = DartToolItem.DEFAULT_HEIGHT;
            }

            if ((w.getApi().style & SWT.DROP_DOWN) != 0) {
                // Add space for dropdown arrow
                width += DartToolItem.ARROW_WIDTH + DartToolItem.INSET;
            }
        }
        return new Point(width, height);
    }

    private static final int DEFAULT_VISIBLE_ITEM_COUNT = 15;
    private static final int DEFAULT_ITEM_HEIGHT = 20;

    public static Point computeSize(DartTree t, int wHint, int hHint, boolean changed) {
        int columnCount = t.getColumnCount();
        int itemCount = t.getItemCount();

        int width = wHint != SWT.DEFAULT ? wHint : (columnCount > 0 ? columnCount * 30 : 200);
        int height = hHint != SWT.DEFAULT ? hHint : Math.min(itemCount, DEFAULT_VISIBLE_ITEM_COUNT) * DEFAULT_ITEM_HEIGHT;

        return new Point(width, height);
    }

    public static Point computeSize(DartText c, int wHint, int hHint, boolean changed) {
        return TextSizes.computeSize(c, wHint, hHint, changed);
    }

    private static final int GROUP_BORDER_WIDTH = 3;
    private static final int GROUP_MARGIN = 3;
    private static final int GROUP_TITLE_PADDING = 8;
    private static final int GROUP_BORDER = GROUP_BORDER_WIDTH + GROUP_MARGIN;

    private static int getGroupTitleHeight(DartGroup widget) {
        String text = widget._text();
        if (text != null && !text.isEmpty()) {
            Point textSize = GCHelper.textExtent(text, SWT.DRAW_MNEMONIC, widget.getFont());
            return textSize.y + GROUP_TITLE_PADDING * 2;
        }
        return 0;
    }

    public static Rectangle computeTrim(DartGroup widget, int x, int y, int width, int height) {
        int titleHeight = getGroupTitleHeight(widget);
        int topMargin = Math.max(GROUP_BORDER, titleHeight);

        return new Rectangle(
            x - GROUP_BORDER,
            y - topMargin,
            width + GROUP_BORDER * 2,
            height + topMargin + GROUP_BORDER
        );
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
        int topMargin = Math.max(GROUP_BORDER, getGroupTitleHeight(widget));

        return new Rectangle(
            GROUP_BORDER,
            topMargin,
            Math.max(0, b.width - GROUP_BORDER * 2),
            Math.max(0, b.height - topMargin - GROUP_BORDER)
        );
    }

    public static Rectangle getClientArea(DartScrollable widget) {
        Rectangle b = widget.getBounds();
        return new Rectangle(0, 0, Math.max(0, b.width), Math.max(0, b.height));
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