package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GraphicsUtils;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

public class CTabFolderHelper {
    // Constants from CTabFolderRenderer
    private static final int PART_BORDER = -3;
    private static final int PART_HEADER = -2;
    private static final int PART_MAX_BUTTON = -5;
    private static final int PART_MIN_BUTTON = -6;
    private static final int PART_CHEVRON_BUTTON = -7;
    private static final int PART_CLOSE_BUTTON = -8;
    private static final int MINIMUM_SIZE = 1 << 24;

    // Constants from DartCTabFolderRenderer
    private static final int BUTTON_SIZE = 16;
    private static final int ITEM_TOP_MARGIN = 2;
    private static final int ITEM_BOTTOM_MARGIN = 2;
    private static final int INTERNAL_SPACING = 4;
    private static final int FLAGS = SWT.DRAW_TRANSPARENT | SWT.DRAW_MNEMONIC | SWT.DRAW_DELIMITER;
    private static final String ELLIPSIS = "...";
    private static final String CHEVRON_ELLIPSIS = "99+";

    public static void handleClose(DartCTabFolder obj, Event e) {
        if (obj.isDisposed()) return;
        if (e.index >= 0 && e.index < obj.items.length) {
            CTabItem item = obj.items[e.index];
            CTabFolderEvent closeEvent = new CTabFolderEvent(obj.getApi());
            closeEvent.item = item;
            closeEvent.doit = true;
            for (CTabFolder2Listener listener : obj.folderListeners) {
                listener.close(closeEvent);
            }
            for (CTabFolderListener listener : obj.tabListeners) {
                listener.itemClosed(closeEvent);
            }
            if (closeEvent.doit)
                item.dispose();
        }
    }

    public static void handleMinimize(DartCTabFolder obj, Event e) {
        if (obj.isDisposed()) return;
        CTabFolderEvent minimizeEvent = new CTabFolderEvent(obj.getApi());
        for (CTabFolder2Listener listener : obj.folderListeners) {
            listener.minimize(minimizeEvent);
        }
        obj.minimized = true;
        obj.maximized = false;
    }

    public static void handleMaximize(DartCTabFolder obj, Event e) {
        if (obj.isDisposed()) return;
        CTabFolderEvent maximizeEvent = new CTabFolderEvent(obj.getApi());
        for (CTabFolder2Listener listener : obj.folderListeners) {
            listener.maximize(maximizeEvent);
        }
        obj.maximized = true;
        obj.minimized = false;
    }

    public static void handleRestore(DartCTabFolder obj, Event e) {
        if (obj.isDisposed()) return;
        CTabFolderEvent restoreEvent = new CTabFolderEvent(obj.getApi());
        for (CTabFolder2Listener listener : obj.folderListeners) {
            listener.restore(restoreEvent);
        }
        obj.minimized = false;
        obj.maximized = false;
    }

    public static Image createButtonImage(DartCTabFolder obj, Display display, int button) {
        final Point size = obj.renderer.computeSize(button, SWT.NONE, null, SWT.DEFAULT, SWT.DEFAULT);
        final Rectangle trim = obj.renderer.computeTrim(button, SWT.NONE, 0, 0, 0, 0);
        final Point imageSize = new Point(size.x - trim.width, size.y - trim.height);

        org.eclipse.swt.graphics.ImageData imageData = new org.eclipse.swt.graphics.ImageData(
            imageSize.x, imageSize.y, 24,
            new org.eclipse.swt.graphics.PaletteData(0xFF0000, 0x00FF00, 0x0000FF));

        Color foreground = obj.getForeground();
        Font font = obj.getFont();
        int fontHash = (font != null) ? font.hashCode() : 0;

        if (foreground != null) {
            byte[] data = imageData.data;
            int bytesPerLine = imageData.bytesPerLine;
            for (int y = 0; y < imageData.height; y++) {
                for (int x = 0; x < imageData.width; x++) {
                    int index = y * bytesPerLine + x * 3;
                    int modifier = (x + y + fontHash) & 0xFF;
                    data[index] = (byte) (foreground.getBlue() ^ modifier);
                    data[index + 1] = (byte) (foreground.getGreen() ^ modifier);
                    data[index + 2] = (byte) (foreground.getRed() ^ modifier);
                }
            }
        }

        Image image = new Image(display, imageData);
        return GraphicsUtils.copyImage(display, image);
    }

    public static boolean updateItems(DartCTabFolder obj, int showIndex) {
        GC gc = new GC(obj.getApi());
        if (!obj.single && !obj.mru && showIndex != -1) {
            int firstIndex = showIndex;
            if (obj.priority[0] < showIndex) {
                int maxWidth = obj.getRightItemEdge(gc) - obj.getLeftItemEdge(gc, PART_BORDER);
                int width = 0;
                int[] widths = new int[obj.items.length];
                for (int i = obj.priority[0]; i <= showIndex; i++) {
                    int state = MINIMUM_SIZE;
                    if (i == obj.selectedIndex)
                        state |= SWT.SELECTED;
                    widths[i] = obj.renderer.computeSize(i, state, gc, SWT.DEFAULT, SWT.DEFAULT).x;
                    width += widths[i];
                    if (width > maxWidth)
                        break;
                }
                if (width > maxWidth) {
                    width = 0;
                    for (int i = showIndex; i >= 0; i--) {
                        if (widths[i] == 0) {
                            int state = MINIMUM_SIZE;
                            if (i == obj.selectedIndex)
                                state |= SWT.SELECTED;
                            widths[i] = obj.renderer.computeSize(i, state, gc, SWT.DEFAULT, SWT.DEFAULT).x;
                        }
                        width += widths[i];
                        if (width > maxWidth)
                            break;
                        firstIndex = i;
                    }
                } else {
                    firstIndex = obj.priority[0];
                    for (int i = showIndex + 1; i < obj.items.length; i++) {
                        int state = MINIMUM_SIZE;
                        if (i == obj.selectedIndex)
                            state |= SWT.SELECTED;
                        widths[i] = obj.renderer.computeSize(i, state, gc, SWT.DEFAULT, SWT.DEFAULT).x;
                        width += widths[i];
                        if (width >= maxWidth)
                            break;
                    }
                    if (width < maxWidth) {
                        for (int i = obj.priority[0] - 1; i >= 0; i--) {
                            if (widths[i] == 0) {
                                int state = MINIMUM_SIZE;
                                if (i == obj.selectedIndex)
                                    state |= SWT.SELECTED;
                                widths[i] = obj.renderer.computeSize(i, state, gc, SWT.DEFAULT, SWT.DEFAULT).x;
                            }
                            width += widths[i];
                            if (width > maxWidth)
                                break;
                            firstIndex = i;
                        }
                    }
                }
            }
            if (firstIndex != obj.priority[0]) {
                int index = 0;
                for (int i = firstIndex; i < obj.items.length; i++) {
                    obj.priority[index++] = i;
                }
                for (int i = firstIndex - 1; i >= 0; i--) {
                    obj.priority[index++] = i;
                }
            }
        }
        boolean oldShowChevron = obj.showChevron;
        boolean changed = obj.setItemSize(gc);
        obj.updateButtons();
        boolean chevronChanged = obj.showChevron != oldShowChevron;
        if (chevronChanged) {
            if (obj.updateTabHeight(false)) {
                changed |= obj.setItemSize(gc);
            }
        }
        changed |= obj.setItemLocation(gc);
        obj.setButtonBounds();
        changed |= chevronChanged;
        if (changed && obj.getToolTipText() != null) {
            Point pt = obj.getDisplay().getCursorLocation();
            pt = obj.toControl(pt);
            obj._setToolTipText(pt.x, pt.y);
        }
        gc.dispose();
        return changed;
    }

    public static int computeChevronWidth(GC gc, CTabFolder parent) {
        final int AVG_CHAR_WIDTH_CHEVRON = 8;
        int widthOfDoubleArrows = 8; // see drawChevron method
        int width = CHEVRON_ELLIPSIS.length() * AVG_CHAR_WIDTH_CHEVRON + widthOfDoubleArrows;
        return width;
    }

    public static Point computeTextSize(GC gc, CTabItem item, String text, int FLAGS) {
        final int AVG_CHAR_WIDTH = 8;
        final int DEFAULT_TEXT_HEIGHT = 16;
        int textWidth = text.length() * AVG_CHAR_WIDTH;
        return new Point(textWidth, DEFAULT_TEXT_HEIGHT);
    }

    private static boolean shouldDrawCloseIcon(CTabItem item, DartCTabFolder parent) {
        boolean showClose = parent.showClose || ((DartCTabItem) item.getImpl()).showClose;
        boolean isSelectedOrShowCloseForUnselected = (item.state & SWT.SELECTED) != 0 || parent.showUnselectedClose;
        return showClose && isSelectedOrShowCloseForUnselected;
    }

    private static int getLargeTextPadding(CTabItem item) {
        final int TABS_WITHOUT_ICONS_PADDING = 14;
        CTabFolder parent = item.getParent();
        String text = item.getText();
        if (text != null && parent.getMinimumCharacters() != 0) {
            return TABS_WITHOUT_ICONS_PADDING;
        }
        return 0;
    }

    private static boolean shouldApplyLargeTextPadding(DartCTabFolder parent) {
        return !parent.showSelectedImage && !parent.showUnselectedImage;
    }

    public static Point computeSize(DartCTabFolderRenderer renderer, DartCTabFolder parent, CTabFolder parentApi, int part, int state, GC gc, int wHint, int hHint) {
        int width = 0, height = 0;

        switch (part) {
            case PART_HEADER:
                if (parent.fixedTabHeight != SWT.DEFAULT) {
                    height = parent.fixedTabHeight == 0 ? 0 : parent.fixedTabHeight + 1;
                } else {
                    CTabItem[] items = parent.items;
                    if (items.length == 0) {
                        final int DEFAULT_TEXT_HEIGHT = 16;
                        height = DEFAULT_TEXT_HEIGHT + ITEM_TOP_MARGIN + ITEM_BOTTOM_MARGIN;
                    } else {
                        for (int i = 0; i < items.length; i++) {
                            height = Math.max(height, computeSize(renderer, parent, parentApi, i, SWT.NONE, gc, wHint, hHint).y);
                        }
                    }
                    // gc.dispose(); // commented out - handled by caller
                }
                break;
            case PART_MAX_BUTTON:
            case PART_MIN_BUTTON:
            case PART_CLOSE_BUTTON:
                width = height = BUTTON_SIZE;
                break;
            case PART_CHEVRON_BUTTON:
                width = computeChevronWidth(gc, parentApi);
                height = BUTTON_SIZE;
                break;
            default:
                if (0 <= part && part < parent.getItemCount()) {
                    renderer.updateCurves();
                    CTabItem item = parent.items[part];
                    if (item.isDisposed()) return new Point(0, 0);
                    Image image = item.getImage();
                    if (image != null && !image.isDisposed()) {
                        Rectangle bounds = image.getBounds();
                        if (((state & SWT.SELECTED) != 0 && parent.showSelectedImage)
                                || ((state & SWT.SELECTED) == 0 && parent.showUnselectedImage)) {
                            width += bounds.width;
                        }
                        height = bounds.height;
                    }
                    String text = null;
                    if ((state & MINIMUM_SIZE) != 0) {
                        int minChars = parent.minChars;
                        text = minChars == 0 ? null : item.getText();
                        if (text != null && text.length() > minChars) {
                            if (renderer.useEllipses()) {
                                int end = minChars < ELLIPSIS.length() + 1 ? minChars : minChars - ELLIPSIS.length();
                                text = text.substring(0, end);
                                if (minChars > ELLIPSIS.length() + 1) text += ELLIPSIS;
                            } else {
                                int end = minChars;
                                text = text.substring(0, end);
                            }
                        }
                    } else {
                        text = item.getText();
                    }
                    if (text != null) {
                        if (width > 0) width += INTERNAL_SPACING;
                        Point size = computeTextSize(gc, item, text, FLAGS);
                        width += size.x;
                        height = Math.max(height, size.y);
                    }

                    if (shouldApplyLargeTextPadding(parent)) {
                        width += getLargeTextPadding(item) * 2;
                    } else if (shouldDrawCloseIcon(item, parent)) {
                        if (width > 0) width += INTERNAL_SPACING;
                        width += computeSize(renderer, parent, parentApi, PART_CLOSE_BUTTON, SWT.NONE, gc, SWT.DEFAULT, SWT.DEFAULT).x;
                    }
                }
                break;
        }
        Rectangle trim = renderer.computeTrim(part, state, 0, 0, width, height);
        width = trim.width;
        height = trim.height;
        return new Point(width, height);
    }
}
