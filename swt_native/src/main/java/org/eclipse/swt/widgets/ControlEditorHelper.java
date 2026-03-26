package org.eclipse.swt.widgets;

import dev.equo.swt.RequestResponse;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import java.lang.reflect.Array;

public class ControlEditorHelper {

    @SuppressWarnings("unchecked")
    public static <T extends ControlEditor> T[] addEditor(T[] editors, T value, Class<T> type) {
        if (editors != null) {
            for (T e : editors) {
                if (e == value) return editors;
            }
        }
        T[] oldEditors = editors != null ? editors : (T[]) Array.newInstance(type, 0);
        T[] newEditors = (T[]) Array.newInstance(type, oldEditors.length + 1);
        System.arraycopy(oldEditors, 0, newEditors, 0, oldEditors.length);
        newEditors[oldEditors.length] = value;
        return newEditors;
    }

    @SuppressWarnings("unchecked")
    public static <T extends ControlEditor> T[] removeEditor(T[] editors, T value, Class<T> type) {
        if (editors == null) return null;
        int idx = -1;
        for (int i = 0; i < editors.length; i++) {
            if (editors[i] == value) { idx = i; break; }
        }
        if (idx == -1) return editors;
        T[] newEditors = (T[]) Array.newInstance(type, editors.length - 1);
        System.arraycopy(editors, 0, newEditors, 0, idx);
        System.arraycopy(editors, idx + 1, newEditors, idx, editors.length - idx - 1);
        return newEditors;
    }

    public static TreeItem getItemFromId(TreeItem[] items, int itemId) {
        return findItemByHashCode(items, itemId);
    }

    public static TreeItem findItemByHashCode(TreeItem[] items, int hashCode) {
        if (items == null) return null;
        for (TreeItem item : items) {
            if (item == null) continue;
            if (item.hashCode() == hashCode) {
                return item;
            }
            TreeItem found = findItemByHashCode(((DartTreeItem) item.getImpl()).items, hashCode);
            if (found != null) return found;
        }
        return null;
    }

    public static void markDirty(DartWidget widget) {
        if (widget != null) {
            widget.dirty();
        }
    }

    public static int getItemIdFromPosition(DartWidget widget, Point point) {
        return RequestResponse.call(widget, "GetIdFromPoint", point,
                Integer.class, 500, -1);
    }

    /**
     * Queries Flutter for the bounds of an item cell at a given column.
     * Works for both Tree and Table widgets.
     * Returns a Rectangle with the item's position and size, or (0,0,0,0) on failure.
     */
    public static Rectangle getItemBounds(DartWidget widget, int itemId, int columnIndex) {
        if (widget instanceof DartControl control && !control.isVisible()) {
            return new Rectangle(0, 0, 0, 0);
        }
        return RequestResponse.call(widget, "GetItemBounds", itemId + "," + columnIndex,
                Rectangle.class, 100, new Rectangle(0, 0, 0, 0));
    }
}