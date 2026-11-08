package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class VTableEditor extends VControlEditor {

    protected VTableEditor() {
    }

    protected VTableEditor(DartTableEditor impl) {
        super(impl);
    }

    public int getColumn() {
        return ((DartTableEditor) impl).getColumn();
    }

    public void setColumn(int value) {
        ((DartTableEditor) impl).column = value;
    }

    public TableItem getItem() {
        TableItem val = ((DartTableEditor) impl).item;
        if (val != null && !(val.getImpl() instanceof DartTableItem))
            return null;
        return val;
    }

    public void setItem(TableItem value) {
        ((DartTableEditor) impl).item = value;
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 568:
                w.write((int) getColumn());
                return true;
            case 569:
                w.ref(getItem());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 568:
                {
                    int v_ = getColumn();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(568);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 569:
                {
                    TableItem v_ = getItem();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(569);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VControlEditor.allWireKeys_(), new int[] { 568, 569 });

    protected static int[] allWireKeys_() {
        return wireKeys_;
    }

    @Override
    public int[] wireKeys() {
        return wireKeys_;
    }

    @Override
    public boolean wireAlways(int key) {
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
