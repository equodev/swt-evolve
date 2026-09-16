package org.eclipse.swt.widgets;

import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.DartControlEditor;

import java.lang.reflect.Array;

public class ControlEditorHelper {

    /**
     * Drops every association in {@code editors} that points at {@code control}, answering whether
     * any did. Called when the control leaves its parent — which, for a cell editor, is how an
     * application closes one: it disposes the control it put over the cell. The editor outlives
     * that (it is reused for the next cell), so nothing else clears the reference, and the dead
     * control would go on being serialized under the parent's {@code editors} — leaving the client
     * with an overlay it can never take down over a control that no longer exists.
     */
    public static <T extends ControlEditor> boolean releaseEditorControl(T[] editors, Control control) {
        if (editors == null || control == null)
            return false;
        boolean released = false;
        for (T editor : editors) {
            if (editor != null && editor.getImpl() instanceof DartControlEditor
                    && ((DartControlEditor) editor.getImpl()).releaseEditor(control)) {
                released = true;
            }
        }
        return released;
    }

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
}