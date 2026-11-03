package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.DartGC;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GraphicsUtils;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.FontMetricsUtil;
import dev.equo.swt.size.CTabFolderSizes;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DartControl;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Sizes;
import org.eclipse.swt.widgets.ToolBar;

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
     * Upstream's {@code updateTabHeight}, with the heights from {@link Sizes}: the render side draws
     * the strip from its theme, so no GC or renderer is asked. The strip height goes to the render
     * side, which draws exactly that.
     */
    static boolean updateTabHeight(DartCTabFolder folder, boolean force) {
        int oldHeight = folder.tabHeight;
        int oldStripHeight = folder.stripHeight;
        folder.tabHeight = Sizes.cTabFolderTabHeight(folder);
        folder.stripHeight = Sizes.cTabFolderTabStripHeight(folder);
        if (!force && folder.tabHeight == oldHeight && folder.stripHeight == oldStripHeight)
            return false;
        folder.oldSize = null;
        folder.getValue().markDirty(VCTabFolder.TAB_HEIGHT);
        return true;
    }

    /**
     * How much taller than the drawn icon the tallest tab image is. The render side draws an icon
     * at least that tall and grows the whole strip by the difference, as upstream sizes the header
     * to its tallest tab.
     */
    public static int imageGrowth(DartCTabFolder folder) {
        int growth = 0;
        for (CTabItem item : folder.items)
            if (!item.isDisposed()) growth = Math.max(growth, imageGrowth(item.getImage()));
        return growth;
    }

    /** Whether {@code bar} is the one a folder builds for its own minimize, maximize or chevron button. */
    public static boolean ownsToolBar(ToolBar bar) {
        if (!(bar.getParent() instanceof CTabFolder)) return false;
        DartCTabFolder folder = (DartCTabFolder) bar.getParent().getImpl();
        return bar == folder.minMaxTb || bar == folder.chevronTb;
    }

    private static int imageGrowth(Image image) {
        if (image == null || image.isDisposed()) return 0;
        return Math.max(0, image.getBounds().height - CTabFolderSizes.TAB_IMAGE_HEIGHT);
    }

    /** Height of the row(s) SWT.WRAP topRight controls wrap onto when the folder is {@code size}. */
    public static int wrappedHeight(DartCTabFolder folder, Point size) {
        return folder.getWrappedHeight(size);
    }

    /**
     * The repaint upstream does on Activate/Deactivate. A whole-tree render side gets only the flag:
     * a redraw would re-send the folder with the whole view nested in it, twice per focus change.
     */
    static void redrawActivation(DartCTabFolder folder) {
        if (folder.getBridge() instanceof org.eclipse.swt.widgets.EmbeddedBridge)
            folder.redraw();
        else
            FlutterBridge.send(folder, dev.equo.swt.WireTable.CHANNEL_ACTIVATION, dev.equo.swt.Java8.map("active", folder._highlight()));
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

    /**
     * The strip, then after a {@code ;} each tab's drawn close button as {@code x, y, width, height}
     * in the folder's own coordinates, with a zero width for a tab that shows none, then after
     * another {@code ;} the part of the strip that is shown, as {@code x, width}, then after another
     * {@code ;} a {@code 1} for each tab whose label is drawn shortened and a {@code 0} for the others.
     */
    public static void handleStripLaidOut(DartCTabFolder obj, Event e) {
        if (obj.isDisposed()) return;
        if (e.text == null || e.text.isEmpty()) {
            stripLayout.remove(obj);
            obj.shownStrip = null;
            obj.drawnCloseButtons = null;
            obj.drawnShortened = null;
            return;
        }
        String[] sections = e.text.split(";", -1);
        int[] strip = parseInts(sections[0]);
        int[] close = sections.length > 1 ? parseInts(sections[1]) : new int[0];
        int[] viewport = sections.length > 2 ? parseInts(sections[2]) : new int[0];
        int[] shortened = sections.length > 3 ? parseInts(sections[3]) : new int[0];
        if (strip == null || close == null || viewport == null || shortened == null) return;
        stripLayout.put(obj, strip);
        obj.shownStrip = viewport.length == 2 ? viewport : null;
        obj.drawnCloseButtons = close;
        obj.drawnShortened = shortened;
    }

    /**
     * Drops the render side's shortened labels, which it reports by index: a tab inserted, moved or
     * disposed would shift them onto the wrong tabs until the next strip report.
     */
    public static void itemsChanged(DartCTabFolder folder) {
        folder.drawnShortened = null;
    }

    /**
     * Upstream's {@code CTabItem.getToolTipText}: the tab's own tooltip, else its whole text when its
     * label is drawn shortened. The render side draws the labels, so it says which ones it shortened;
     * this side's renderer never paints a tab and so never records a shortened text of its own.
     */
    public static String toolTipText(DartCTabItem item) {
        if (item.toolTipText != null || item.parent == null) return item.toolTipText;
        DartCTabFolder folder = (DartCTabFolder) item.parent.getImpl();
        int[] shortened = folder.drawnShortened;
        int index = item.parent.indexOf(item.getApi());
        if (shortened != null && index >= 0 && index < shortened.length && shortened[index] != 0) {
            return item.getText();
        }
        return null;
    }

    /**
     * Whether the tab is drawn whole inside the shown part of the strip. The render side scrolls the
     * strip, so this side's own layout, which parks what does not fit off screen, does not say. The
     * workbench lists the tabs that are not showing in bold in the chevron's list.
     */
    public static boolean isShowing(DartCTabItem item) {
        CTabFolder parent = item.parent;
        DartCTabFolder folder = (DartCTabFolder) parent.getImpl();
        int[] drawn = stripLayout.get(folder);
        int index = parent.indexOf(item.getApi());
        if (drawn == null || index < 0 || 2 * index + 1 >= drawn.length) return item.showing;
        int[] viewport = folder.shownStrip;
        int left = viewport != null ? viewport[0] : 0;
        int right = viewport != null ? viewport[0] + viewport[1] : parent.getSize().x;
        int x = drawn[2 * index], width = drawn[2 * index + 1];
        // Positions arrive rounded one by one, so a tab flush with an edge can overshoot it by one.
        return width > 0 && x >= left - 1 && x + width <= right + 1;
    }

    private static int[] parseInts(String list) {
        if (list.trim().isEmpty()) return new int[0];
        String[] parts = list.split(",");
        int[] values = new int[parts.length];
        try {
            for (int i = 0; i < parts.length; i++) values[i] = Integer.parseInt(parts[i].trim());
        } catch (NumberFormatException malformed) {
            return null;
        }
        return values;
    }

    /**
     * The folder's tooltip at a point, as upstream's {@code _getToolTip}, against the strip as it is
     * drawn: this side's own layout can park a drawn tab off screen or put its close rectangle where
     * the drawing has none.
     */
    public static String toolTipAt(DartCTabFolder folder, int x, int y) {
        CTabItem item = folder.getItem(new Point(x, y));
        if (item == null) return null;
        DartCTabItem impl = (DartCTabItem) item.getImpl();
        if (!isShowing(impl)) return null;
        if ((folder.showClose || impl.showClose) && onCloseButton(folder, impl, x, y)) {
            return SWT.getMessage("SWT_Close"); //$NON-NLS-1$
        }
        return item.getToolTipText();
    }

    private static boolean onCloseButton(DartCTabFolder folder, DartCTabItem item, int x, int y) {
        int[] close = folder.drawnCloseButtons;
        int i = 4 * folder.getApi().indexOf(item.getApi());
        if (close == null || i + 3 >= close.length) return item.closeRect.contains(x, y);
        return onDrawnCloseButton(close, i, x, y);
    }

    /** Whether a point lies on the drawn close button that starts at {@code i} in {@code close}. */
    private static boolean onDrawnCloseButton(int[] close, int i, int x, int y) {
        return close[i + 2] > 0 && x >= close[i] && x < close[i] + close[i + 2]
                && y >= close[i + 1] && y < close[i + 1] + close[i + 3];
    }

    /**
     * Upstream's rule that a drag starting on a close button is not a drag, tested against the close
     * buttons as they are drawn. This side's own close rectangles come from a layout that does not
     * match the drawing: a narrow folder puts one under the middle of a tab drawn without it.
     */
    public static void onDragDetect(DartCTabFolder folder, Event event) {
        int[] close = folder.drawnCloseButtons;
        if (close == null) return;
        for (int i = 0; i + 3 < close.length; i += 4) {
            if (onDrawnCloseButton(close, i, event.x, event.y)) {
                event.type = SWT.None;
                return;
            }
        }
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
        return FontMetricsUtil.labelSize(text, FLAGS, CTabFolderSizes.tabLabelStyle(), item.getFont());
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
     * Whether the render side draws the close button, which is what the tab has to leave room for:
     * the tab's own flag or the folder's SWT.CLOSE.
     */
    private static boolean isCloseButtonDrawn(CTabItem item, DartCTabFolder parent, int state) {
        return (((DartCTabItem) item.getImpl()).showClose || parent.showClose)
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
            // A taller image is drawn in a square as high as it is.
            int growth = imageGrowth(image);
            if (((state & SWT.SELECTED) != 0 && parent.showSelectedImage)
                    || ((state & SWT.SELECTED) == 0 && parent.showUnselectedImage)) {
                width += CTabFolderSizes.TAB_IMAGE_EXTRA + growth;
            }
            height = CTabFolderSizes.TAB_IMAGE_HEIGHT + growth;
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
