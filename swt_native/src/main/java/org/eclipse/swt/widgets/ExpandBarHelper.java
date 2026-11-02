package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;

/**
 * What the Dart-backed ExpandBar needs on the Java side and the generated implementation cannot hold: the
 * expand/collapse plumbing. Its size and band are measured from the rendered header, in
 * {@link dev.equo.swt.size.ExpandBarSizes}.
 * <p>
 * A Flutter-side expand/collapse arrives as an Expand/Collapse event carrying the item index; the Java item's
 * state must follow before the SWT event is fired, or {@code ExpandItem.getExpanded()} goes stale (and a later
 * Java-side value push would silently collapse the item again).
 */
public class ExpandBarHelper {

    public static void sendExpand(DartExpandBar bar, Event event, boolean expand) {
        ExpandItem item = itemAt(bar, event == null ? -1 : event.index);
        if (item == null) {
            return;
        }
        event.item = item;
        if (item.getExpanded() != expand) {
            item.setExpanded(expand);
            bar.sendEvent(expand ? SWT.Expand : SWT.Collapse, event);
        }
    }

    /**
     * Return and Space open or close the focused item. The client has no key handling of its own, so it learns the new
     * state from Java.
     */
    static void openedByKey(Event event, ExpandItem focusItem) {
        if (event.keyCode == SWT.CR || event.keyCode == ' ') {
            ((DartExpandItem) focusItem.getImpl()).getValue().markDirty(VExpandItem.EXPANDED);
        }
    }

    /**
     * The band changed (a font was set), and with it the header of every item.
     */
    static void headersChanged(DartExpandBar bar) {
        for (ExpandItem item : bar.items) {
            if (item != null) {
                ((DartExpandItem) item.getImpl()).getValue().markDirty(VExpandItem.HEADER_HEIGHT);
            }
        }
    }

    private static ExpandItem itemAt(DartExpandBar bar, int index) {
        if (bar.items == null || index < 0 || index >= bar.items.length) {
            return null;
        }
        return bar.items[index];
    }
}
