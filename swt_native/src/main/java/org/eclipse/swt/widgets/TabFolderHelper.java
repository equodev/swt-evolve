package org.eclipse.swt.widgets;

import dev.equo.swt.FontMetricsUtil;
import dev.equo.swt.size.Mnemonics;
import dev.equo.swt.size.TabFolderTheme;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Where the tabs of a TabFolder sit. The tab strip is drawn by the render side, so the bounds are
 * modelled from its layout, with the sizes {@link TabFolderTheme} measured off it: a row of tabs from
 * the left edge, each as wide as its label plus what the tab draws around it.
 */
public class TabFolderHelper {

    static boolean tabsAtBottom(TabFolder folder) {
        return (folder.getStyle() & SWT.BOTTOM) != 0;
    }

    static int tabWidth(TabItem item) {
        TabFolderTheme theme = TabFolderTheme.get();
        int width = theme.tabLabelSurround() + labelWidth(item.getText(), item.getParent().getFont());
        return item.getImage() != null ? width + theme.tabImageExtra() : width;
    }

    /** The label's width in the style the render side draws it in, the folder's font applied over it. */
    static int labelWidth(String text, Font font) {
        return FontMetricsUtil.labelSize(Mnemonics.display(text), SWT.NONE, TabFolderTheme.get().tabLabelStyle(), font).x;
    }

    /** The tabs share the height of the tallest one, and a tab holding an image is the taller. */
    static int tabHeight(TabFolder folder) {
        TabFolderTheme theme = TabFolderTheme.get();
        for (TabItem item : folder.getItems()) {
            if (item.getImage() != null)
                return Math.max(theme.tabHeight(), theme.tabHeightWithImage());
        }
        return theme.tabHeight();
    }

    /** The strip is its tallest tab plus its own line. */
    public static int stripHeight(TabFolder folder) {
        TabFolderTheme theme = TabFolderTheme.get();
        return theme.tabStripHeight() + tabHeight(folder) - theme.tabHeight();
    }

    static int tabTop(TabFolder folder) {
        return tabsAtBottom(folder) ? folder.getSize().y - tabHeight(folder) : 0;
    }

    /** How far the page starts below the folder's top: past the strip, unless the strip is at the bottom. */
    static int pageTop(DartTabFolder folder) {
        TabFolder api = folder.getApi();
        return tabsAtBottom(api) ? TabFolderTheme.get().bodyBorder() : stripHeight(api);
    }

    /**
     * Drops {@code item} from the selection if it holds it, and answers whether it did: the folder then selects the item
     * before it and announces it, as on Win32 and Cocoa. Once the last item went the selection stays empty.
     */
    static boolean deselectDestroyed(DartTabFolder folder, TabItem item) {
        if (folder.selection.length == 0 || folder.selection[0] != item)
            return false;
        folder.selection = new TabItem[0];
        folder.getValue().markDirty(VTabFolder.SELECTION);
        return true;
    }

    /**
     * Leaves the folder with no tab selected and hides the page the selected tab showed, as the javadoc of
     * setSelection(TabItem[]) says for an empty array and Win32 does.
     */
    static void clearSelection(DartTabFolder folder) {
        int index = folder.getSelectionIndex();
        if (index != -1) {
            Control control = ((DartTabItem) folder.items[index].getImpl()).control;
            if (control != null)
                control.setVisible(false);
        }
        folder.selection = new TabItem[0];
        folder.getValue().markDirty(VTabFolder.SELECTION);
    }

    public static Rectangle getBounds(DartTabItem item) {
        TabFolder folder = item.parent;
        int x = 0;
        for (TabItem other : folder.getItems()) {
            int width = tabWidth(other);
            if (other == item.getApi()) {
                return new Rectangle(x, tabTop(folder), width, tabHeight(folder));
            }
            x += width;
        }
        return new Rectangle(0, 0, 0, 0);
    }

    /**
     * Whether a press focuses the folder ahead of its MouseDown: a primary press on one of its tabs
     * does and one on the strip past them does not, as on GTK. Win32 focuses on a press anywhere but
     * on a tab it switches to, Cocoa never.
     */
    static boolean pressFocuses(DartControl control, Event press) {
        if (!(control instanceof DartTabFolder) || press.button != 1) return false;
        if ((control.getApi().getStyle() & SWT.NO_FOCUS) != 0) return false;
        return itemAt((DartTabFolder) control, new Point(press.x, press.y)) != null;
    }

    /**
     * Selects tab {@code index} and announces it with the input's {@code stateMask}: the button of a
     * tab click, Ctrl of a page traversal. A tab already selected is no change and fires nothing.
     */
    public static void select(DartTabFolder folder, int index, int stateMask) {
        if (index == folder.getSelectionIndex()) return;
        folder.setSelection(index);
        Event event = new Event();
        event.item = folder.getApi().getItem(index);
        event.stateMask = stateMask;
        folder.sendSelectionEvent(SWT.Selection, event, true);
    }

    /** ← and → on the focused folder select the tab beside the selected one, without wrapping. */
    static void moveSelectionByKey(DartTabFolder folder, Event key) {
        int step = key.keyCode == SWT.ARROW_RIGHT ? 1 : key.keyCode == SWT.ARROW_LEFT ? -1 : 0;
        if (step == 0 || (key.stateMask & SWT.MODIFIER_MASK) != 0) return;
        int index = folder.getSelectionIndex() + step;
        if (index >= 0 && index < folder.getItemCount()) select(folder, index, 0);
    }

    /**
     * Ctrl+PgDn or Ctrl+PgUp: the nearest folder at or above {@code control} turns the page, wrapping
     * around, as {@code TabFolder.traversePage} does natively, and announces it with Ctrl held.
     */
    static boolean traversePage(DartControl control, boolean next, int stateMask) {
        for (Control c = control.getApi(); c != null; c = c.getParent()) {
            if (!(c.getImpl() instanceof DartTabFolder)) continue;
            DartTabFolder folder = (DartTabFolder) c.getImpl();
            int count = folder.getItemCount();
            if (count == 0) return false;
            // A folder with items always has a selection.
            select(folder, (folder.getSelectionIndex() + (next ? 1 : -1) + count) % count, stateMask);
            return true;
        }
        return false;
    }

    public static TabItem itemAt(DartTabFolder folder, Point point) {
        TabFolder api = folder.getApi();
        int y = tabTop(api);
        int height = tabHeight(api);
        int x = 0;
        for (TabItem item : api.getItems()) {
            int width = tabWidth(item);
            if (new Rectangle(x, y, width, height).contains(point))
                return item;
            x += width;
        }
        return null;
    }
}
