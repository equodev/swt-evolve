package org.eclipse.swt.dnd;

import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

public class DropTargetHelper {

    /** The item of {@code control} (a Table, Tree or CTabFolder) the client names by {@code id}, or null. */
    public static Widget findItemById(Control control, long id) {
        // An item the client points at was sent to it, so it is found by its id; one of this control's.
        Object byId = FlutterBridge.findById(id);
        Widget known = byId instanceof DartWidget ? ((DartWidget) byId).getApi() : null;
        if (known instanceof TableItem && ((TableItem) known).getParent() == control) return known;
        if (known instanceof TreeItem && ((TreeItem) known).getParent() == control) return known;
        if (known instanceof CTabItem && ((CTabItem) known).getParent() == control) return known;
        if (control instanceof Table) {
            for (TableItem item : ((Table) control).getItems()) {
                if (FlutterBridge.id(item) == id)
                    return item;
            }
        } else if (control instanceof Tree) {
            for (TreeItem item : ((Tree) control).getItems()) {
                Widget found = findTreeItemById(item, id);
                if (found != null)
                    return found;
            }
        } else if (control instanceof CTabFolder) {
            for (CTabItem item : ((CTabFolder) control).getItems()) {
                if (FlutterBridge.id(item) == id)
                    return item;
            }
        }
        return null;
    }

    private static Widget findTreeItemById(TreeItem item, long id) {
        if (FlutterBridge.id(item) == id)
            return item;
        for (TreeItem child : item.getItems()) {
            Widget found = findTreeItemById(child, id);
            if (found != null)
                return found;
        }
        return null;
    }
}
