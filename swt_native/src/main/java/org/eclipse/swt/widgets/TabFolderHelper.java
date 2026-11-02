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
