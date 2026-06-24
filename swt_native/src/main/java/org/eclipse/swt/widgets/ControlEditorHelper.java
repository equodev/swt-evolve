package org.eclipse.swt.widgets;

import org.eclipse.swt.custom.ControlEditor;

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
}