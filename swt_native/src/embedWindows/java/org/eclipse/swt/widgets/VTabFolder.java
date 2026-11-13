package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public class VTabFolder extends VComposite {

    protected VTabFolder() {
    }

    protected VTabFolder(DartTabFolder impl) {
        super(impl);
    }

    public TabItem[] getItems() {
        TabItem[] values = ((DartTabFolder) impl).items;
        if (values == null)
            return null;
        int kept = 0;
        for (TabItem v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        TabItem[] result = new TabItem[kept];
        kept = 0;
        for (TabItem v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setItems(TabItem[] value) {
        ((DartTabFolder) impl).items = value;
    }

    public TabItem[] getSelection() {
        TabItem[] values = ((DartTabFolder) impl).selection;
        if (values == null)
            return null;
        int kept = 0;
        for (TabItem v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        TabItem[] result = new TabItem[kept];
        kept = 0;
        for (TabItem v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setSelection(TabItem[] value) {
        ((DartTabFolder) impl).selection = value;
    }

    public static final int ITEMS = 555;

    public static final int SELECTION = 556;

    @Override
    public String wireName(int key) {
        switch(key) {
            case ITEMS:
                return "items";
            case SELECTION:
                return "selection";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 149:
                return false;
            case 555:
                w.refs(getItems());
                return true;
            case 556:
                w.refs(getSelection());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 149:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 555:
                {
                    TabItem[] v_ = getItems();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(555);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 556:
                {
                    TabItem[] v_ = getSelection();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(556);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 555, 556 });

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
