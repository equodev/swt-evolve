package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.DartGC;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GCHelper;
import org.eclipse.swt.graphics.GraphicsUtils;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.FontMetricsUtil;
import dev.equo.swt.size.CTabFolderSizes;
import dev.equo.swt.size.TextStyle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DartControl;
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
    private static final int FLAGS = SWT.DRAW_TRANSPARENT | SWT.DRAW_MNEMONIC | SWT.DRAW_DELIMITER;
    private static final String CHEVRON_ELLIPSIS = "99+";

    /**
     * The repaint upstream does on Activate/Deactivate. A whole-tree render side gets only the flag:
     * a redraw would re-send the folder with the whole view nested in it, twice per focus change.
     */
    static void redrawActivation(DartCTabFolder folder) {
        if (folder.getBridge() instanceof org.eclipse.swt.widgets.EmbeddedBridge)
            folder.redraw();
        else
            FlutterBridge.send(folder, "activation", dev.equo.swt.Java8.map("active", folder._highlight()));
    }

    /**
     * A tab click from Flutter. setSelection(int, boolean) notifies only when the index actually
     * changes, so a click on the already-selected tab would leave no trace at all — while a native
     * one still moves focus onto the tab (SwtCTabFolder.onMouse, SWT.MouseDown) and so still reaches
     * anything that tracks which tab's content the user is working in. Focus takes the same route it
     * takes when the render side reports it, since that is what announces activation here.
     */
    public static void handleTabClick(DartCTabFolder obj, Event e) {
        if (obj.isDisposed()) return;
        int previous = obj.getSelectionIndex();
        obj.setSelection(e.index, true);
        if (obj.isDisposed() || previous != obj.getSelectionIndex()) return;
        CTabItem selected = obj.getSelection();
        Control content = selected == null || selected.isDisposed() ? null : selected.getControl();
        DartControl target = content != null && !content.isDisposed() && content.getImpl() instanceof DartControl
                ? (DartControl) content.getImpl()
                : obj;
        FlutterBridge bridge = target.getBridge();
        if (bridge != null && bridge.hasFocus(target)) return;
        // Taking the focus is what announces the activation here (ControlHelper.takeFocus), and
        // Composite.setFocus() hands it to a descendant, so the announcement starts deep enough to
        // pass through every control in between — which is where an embedder hangs its listener.
        target.getApi().setFocus();
    }

    /**
     * Where the render side draws each folder's tabs: {@code x, width} per tab, in the folder's own
     * coordinates.
     *
     * <p>Not part of the widget's state, and not something this side can work out: it lays the strip
     * out itself, without a scroll and with whatever does not fit parked off screen.
     */
    private static final java.util.Map<DartCTabFolder, int[]> stripLayout =
            java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());

    public static void handleStripLaidOut(DartCTabFolder obj, Event e) {
        if (obj.isDisposed()) return;
        if (e.text == null || e.text.isEmpty()) {
            stripLayout.remove(obj);
            return;
        }
        String[] parts = e.text.split(",");
        int[] layout = new int[parts.length];
        try {
            for (int i = 0; i < parts.length; i++) layout[i] = Integer.parseInt(parts[i].trim());
        } catch (NumberFormatException malformed) {
            return;
        }
        stripLayout.put(obj, layout);
    }

    /**
     * The tab at a point, resolved against the strip as it is drawn: the tabs laid out in order at
     * their drawn widths, shifted by however far the strip has been scrolled.
     *
     * <p>Not {@code item.getBounds()}, which is this side's own layout - it parks every tab past the
     * strip's width off screen, because upstream hides them behind the chevron rather than scrolling
     * them. The workbench asks this which view a drag picked up, so the answer has to be the tab
     * under the pointer, not the tab that would be there if the strip did not scroll.
     */
    public static CTabItem itemAt(DartCTabFolder folder, Point pt) {
        CTabItem[] items = folder.items;
        if (items.length == 0) return null;
        // As upstream's getItem does: a layout still pending would answer from widths that predate
        // the last change to the strip.
        folder.runUpdate();
        Point size = folder.getApi().getSize();
        if (pt.x < 0 || pt.x >= size.x) return null;
        int tabHeight = folder.getApi().getTabHeight();
        int top = folder.onBottom ? size.y - tabHeight : 0;
        if (pt.y < top || pt.y >= top + tabHeight) return null;
        int[] drawn = stripLayout.get(folder);
        if (drawn == null) {
            // Nothing drawn yet, or nothing that reports where: upstream's own answer, from the
            // bounds this side laid out, is better than none.
            for (int element : folder.priority) {
                CTabItem item = items[element];
                if (item.getBounds().contains(pt)) return item;
            }
            return null;
        }
        for (int i = 0; i < items.length && 2 * i + 1 < drawn.length; i++) {
            if (items[i].isDisposed()) continue;
            int left = drawn[2 * i], width = drawn[2 * i + 1];
            if (width > 0 && pt.x >= left && pt.x < left + width) return items[i];
        }
        return null;
    }

    public static void handleReorderItems(DartCTabFolder obj, Event e) {
        if (obj.isDisposed()) return;
        int from = e.index;
        int to = e.detail;
        int count = obj.items.length;
        if (from < 0 || from >= count || to < 0 || to > count || from == to) return;
        int[] indices = new int[count];
        int dst = 0;
        for (int src = 0; src < count; src++) {
            if (dst == to) {
                indices[dst++] = from;
            }
            if (src == from) continue;
            indices[dst++] = src;
        }
        if (dst == to) {
            indices[dst] = from;
        }
        obj.setItemOrder(indices);
    }

    /**
     * {@code CTabFolderEvent(Widget)} only fills {@code TypedEvent.widget} in newer SWT releases;
     * older ones record the folder as the EventObject source alone. Listeners read the field (e4's
     * MinMaxAddon resolves the part stack through it on the first line of minimize/maximize/restore),
     * so it is assigned here, as upstream CTabFolder does at each call site.
     */
    private static CTabFolderEvent folderEvent(DartCTabFolder obj) {
        CTabFolderEvent event = new CTabFolderEvent(obj.getApi());
        event.widget = obj.getApi();
        return event;
    }

        public static void handleShowList(DartCTabFolder obj, Event e) {
        CTabFolderEvent event = folderEvent(obj);
        event.x = e.x;
        event.y = e.y;
        event.width = e.width;
        event.height = e.height;
        event.doit = true;
        for (CTabFolder2Listener listener : obj.getApi().folderListeners) {
            try {
                listener.showList(event);
            } catch (Throwable ex) {
            }
        }
        if (!obj.isDisposed() && event.doit) {
            obj.showListPopupSeq++;
        }
    }

    public static void handleClose(DartCTabFolder obj, Event e) {
        if (obj.isDisposed()) return;
        if (e.index >= 0 && e.index < obj.items.length) {
            CTabItem item = obj.items[e.index];
            CTabFolderEvent closeEvent = folderEvent(obj);
            closeEvent.item = item;
            closeEvent.doit = true;
            for (CTabFolder2Listener listener : obj.getApi().folderListeners) {
                listener.close(closeEvent);
            }
            for (CTabFolderListener listener : obj.getApi().tabListeners) {
                listener.itemClosed(closeEvent);
            }
            if (closeEvent.doit)
                item.dispose();
        }
    }

    // The min/max flags belong to the listener, as in upstream onSelection: e4 marks a minimized
    // stack maximized=true so its one remaining button acts as restore. Assigning here overwrites that.
    public static void handleMinimize(DartCTabFolder obj, Event e) {
        if (obj.isDisposed()) return;
        CTabFolderEvent minimizeEvent = folderEvent(obj);
        for (CTabFolder2Listener listener : obj.getApi().folderListeners) {
            listener.minimize(minimizeEvent);
        }
    }

    public static void handleMaximize(DartCTabFolder obj, Event e) {
        if (obj.isDisposed()) return;
        CTabFolderEvent maximizeEvent = folderEvent(obj);
        for (CTabFolder2Listener listener : obj.getApi().folderListeners) {
            listener.maximize(maximizeEvent);
        }
    }

    public static void handleRestore(DartCTabFolder obj, Event e) {
        if (obj.isDisposed()) return;
        CTabFolderEvent restoreEvent = folderEvent(obj);
        for (CTabFolder2Listener listener : obj.getApi().folderListeners) {
            listener.restore(restoreEvent);
        }
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
        // Measurement only: disposing it must not announce a paint cycle to Flutter (as in TreeHelper).
        if (gc.getImpl() instanceof DartGC) ((DartGC) gc.getImpl()).silentDispose = true;
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

    /**
     * A tab's label at the size the render side draws it, which is what a tab is sized around.
     *
     * <p>The style is {@link CTabFolderSizes#tabLabelStyle()}, measured off a laid-out label rather
     * than written down here: a font copied from the render side's theme drifts from it silently.
     */
    static Point computeTextSize(GC gc, CTabItem item, String text, int FLAGS) {
        // A resize reaches this from an async runnable, which can run after the item's font was
        // disposed; a disposed font answers nothing, so measure in the label's own style instead.
        Font font = item.getFont();
        if (font != null && font.isDisposed())
            font = null;
        if (Config.getConfigFlags().use_swt_fonts)
            return GCHelper.textExtent(text, FLAGS, font);
        TextStyle style = CTabFolderSizes.tabLabelStyle().withStyleFrom(font);
        dev.equo.swt.size.PointD size = FontMetricsUtil.getFontSize(text, style);
        return new Point((int) Math.ceil(size.x()), (int) Math.ceil(size.y()));
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

    /**
     * Whether the render side draws the close button, which is what the tab has to leave room for.
     * It reads the item's own flag only - a folder-wide SWT.CLOSE does not reach it - so a width
     * that took the folder's flag into account would reserve room for a button nobody draws.
     */
    private static boolean isCloseButtonDrawn(CTabItem item, DartCTabFolder parent, int state) {
        return ((DartCTabItem) item.getImpl()).showClose
                && ((state & SWT.SELECTED) != 0 || parent.showUnselectedClose);
    }

    private static boolean shouldApplyLargeTextPadding(DartCTabFolder parent) {
        return !parent.showSelectedImage && !parent.showUnselectedImage;
    }

    /**
     * A tab at the size the render side draws it: its label, the icon and close button when they
     * are drawn, and the padding around them.
     *
     * <p>Takes no GC and goes through no renderer. The render side draws the strip from its own
     * theme, so an application's renderer has no say in a tab's width, and asking it would need a
     * live GC on a path that answers a hit test.
     *
     * <p>{@code MINIMUM_SIZE} is ignored: it asks how narrow a tab could be drawn if the strip ran
     * short of room, and the render side draws the whole label and hides what no longer fits.
     */
    static Point drawnSize(DartCTabFolder parent, int index, int state) {
        CTabItem item = parent.items[index];
        if (item.isDisposed()) return new Point(0, 0);
        int width = 0, height = 0;
        Image image = item.getImage();
        if (image != null && !image.isDisposed()) {
            if (((state & SWT.SELECTED) != 0 && parent.showSelectedImage)
                    || ((state & SWT.SELECTED) == 0 && parent.showUnselectedImage)) {
                width += CTabFolderSizes.TAB_IMAGE_EXTRA;
            }
            height = CTabFolderSizes.TAB_IMAGE_HEIGHT;
        }
        String text = item.getText();
        if (text != null) {
            Point size = computeTextSize(null, item, text, FLAGS);
            width += size.x;
            height = Math.max(height, size.y);
        }
        if (shouldApplyLargeTextPadding(parent)) {
            width += getLargeTextPadding(item) * 2;
        } else if (isCloseButtonDrawn(item, parent, state)) {
            width += CTabFolderSizes.TAB_CLOSE_EXTRA;
        }
        return new Point(width + CTabFolderSizes.TAB_LABEL_SURROUND,
                height + ITEM_TOP_MARGIN + ITEM_BOTTOM_MARGIN);
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
                if (0 <= part && part < parent.getItemCount()) return drawnSize(parent, part, state);
                break;
        }
        Rectangle trim = renderer.computeTrim(part, state, 0, 0, width, height);
        width = trim.width;
        height = trim.height;
        return new Point(width, height);
    }
}
