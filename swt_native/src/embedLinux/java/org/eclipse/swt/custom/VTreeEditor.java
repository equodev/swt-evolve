package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class VTreeEditor extends VControlEditor {

    protected VTreeEditor() {
    }

    protected VTreeEditor(DartTreeEditor impl) {
        super(impl);
    }

    public int getColumn() {
        return ((DartTreeEditor) impl).getColumn();
    }

    public void setColumn(int value) {
        ((DartTreeEditor) impl).column = value;
    }

    public Control getEditor() {
        Control val = ((DartTreeEditor) impl).editor;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        if (val != null && val.isDisposed())
            return null;
        return val;
    }

    public void setEditor(Control value) {
        ((DartTreeEditor) impl).editor = value;
    }

    public TreeItem getItem() {
        TreeItem val = ((DartTreeEditor) impl).item;
        if (val != null && !(val.getImpl() instanceof DartTreeItem))
            return null;
        return val;
    }

    public void setItem(TreeItem value) {
        ((DartTreeEditor) impl).item = value;
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 533:
                w.write((int) getColumn());
                return true;
            case 534:
                w.ref(getEditor());
                return true;
            case 535:
                w.ref(getItem());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 533:
                {
                    int v_ = getColumn();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(533);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 534:
                {
                    Control v_ = getEditor();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(534);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 535:
                {
                    TreeItem v_ = getItem();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(535);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VControlEditor.allWireKeys_(), new int[] { 533, 534, 535 });

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
