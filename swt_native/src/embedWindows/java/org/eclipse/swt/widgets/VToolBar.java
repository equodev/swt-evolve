package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public class VToolBar extends VComposite {

    protected VToolBar() {
    }

    protected VToolBar(DartToolBar impl) {
        super(impl);
    }

    public ToolItem[] getItems() {
        ToolItem[] values = ((DartToolBar) impl).items;
        if (values == null)
            return null;
        int kept = 0;
        for (ToolItem v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        ToolItem[] result = new ToolItem[kept];
        kept = 0;
        for (ToolItem v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setItems(ToolItem[] value) {
        ((DartToolBar) impl).items = value;
    }

    public static final int ITEMS = 599;

    @Override
    public String wireName(int key) {
        switch(key) {
            case ITEMS:
                return "items";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 599:
                w.refs(getItems());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 599:
                {
                    ToolItem[] v_ = getItems();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(599);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 599 });

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
