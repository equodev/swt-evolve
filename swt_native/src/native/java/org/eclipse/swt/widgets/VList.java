package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import java.util.ArrayList;

public class VList extends VScrollable {

    protected VList() {
    }

    protected VList(DartList impl) {
        super(impl);
    }

    public String[] getItems() {
        String[] values = ((DartList) impl).items;
        if (values == null)
            return null;
        ArrayList<String> result = new ArrayList<>(values.length);
        for (String v : values) if (v != null)
            result.add(v);
        return result.toArray(new String[0]);
    }

    public void setItems(String[] value) {
        ((DartList) impl).items = value;
    }

    public int[] getSelection() {
        return ((DartList) impl).selection;
    }

    public void setSelection(int[] value) {
        ((DartList) impl).selection = value;
    }

    public int getTopIndex() {
        return ((DartList) impl).getTopIndex();
    }

    public void setTopIndex(int value) {
        ((DartList) impl).topIndex = value;
    }

    public static final int ITEMS = 461;

    public static final int SELECTION = 462;

    public static final int TOP_INDEX = 1422;

    @Override
    public String wireName(int key) {
        switch(key) {
            case ITEMS:
                return "items";
            case SELECTION:
                return "selection";
            case TOP_INDEX:
                return "topIndex";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 461:
                w.write(getItems());
                return true;
            case 462:
                w.write(getSelection());
                return true;
            case 1422:
                w.write((int) getTopIndex());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 461:
                {
                    String[] v_ = getItems();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(461);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 462:
                {
                    int[] v_ = getSelection();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(462);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1422:
                {
                    int v_ = getTopIndex();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1422);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VScrollable.allWireKeys_(), new int[] { 461, 462, 1422 });

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
