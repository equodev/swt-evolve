package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class VControlEditor {

    protected VControlEditor() {
    }

    protected VControlEditor(DartControlEditor impl) {
        this.impl = impl;
    }

    protected DartControlEditor impl;

    public Control getEditor() {
        Control val = ((DartControlEditor) impl).editor;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        if (val != null && val.isDisposed())
            return null;
        return val;
    }

    public void setEditor(Control value) {
        ((DartControlEditor) impl).editor = value;
    }

    /**
     * Writes one property's value into a frame of the binary wire by its number; false for a number no class up to this one writes.
     */
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 147:
                w.ref(getEditor());
                return true;
        }
        return false;
    }

    /**
     * Writes one property of a whole description, key and value, unless it is at a default the client starts from, which is recognised without writing it; returns which (dev.equo.swt.WidgetWire.WRITTEN, AT_ZERO, AT_EMPTY or UNHANDLED).
     */
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 147:
                {
                    Control v_ = getEditor();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(147);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return dev.equo.swt.WidgetWire.UNHANDLED;
    }

    private static final int[] wireKeys_ = new int[] { 147 };

    protected static int[] allWireKeys_() {
        return wireKeys_;
    }

    /**
     * The number of every property a whole description writes.
     */
    public int[] wireKeys() {
        return wireKeys_;
    }

    /**
     * Whether a whole description writes this property even at its default.
     */
    public boolean wireAlways(int key) {
        return false;
    }

    /**
     * Whether this property is written when empty: empty is not its default, null is.
     */
    public boolean wireKeepsEmpty(int key) {
        return false;
    }
}
