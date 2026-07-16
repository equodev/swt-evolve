package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;

/**
 * Shared logic for the Dart-backed ExpandBar. A Flutter-side expand/collapse arrives as an
 * Expand/Collapse event carrying the item index; the Java item's state must follow before the
 * SWT event is fired, or {@code ExpandItem.getExpanded()} goes stale (and a later Java-side
 * value push would silently collapse the item again).
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

    private static ExpandItem itemAt(DartExpandBar bar, int index) {
        if (bar.items == null || index < 0 || index >= bar.items.length) {
            return null;
        }
        return bar.items[index];
    }
}
