package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import java.util.ArrayList;

public class VExpandBar extends VComposite {

    protected VExpandBar() {
    }

    protected VExpandBar(DartExpandBar impl) {
        super(impl);
    }

    public ExpandItem[] getItems() {
        ExpandItem[] values = ((DartExpandBar) impl).items;
        if (values == null)
            return null;
        ArrayList<ExpandItem> result = new ArrayList<>(values.length);
        for (ExpandItem v : values) if (v != null)
            result.add(v);
        return result.toArray(new ExpandItem[0]);
    }

    public void setItems(ExpandItem[] value) {
        ((DartExpandBar) impl).items = value;
    }

    public int getSpacing() {
        return ((DartExpandBar) impl).getSpacing();
    }

    public void setSpacing(int value) {
        ((DartExpandBar) impl).spacing = value;
    }

    public static final int ITEMS = 76;

    public static final int SPACING = 77;

    @Override
    public String wireName(int key) {
        switch(key) {
            case ITEMS:
                return "items";
            case SPACING:
                return "spacing";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 76:
                w.refs(getItems());
                return true;
            case 77:
                w.write((int) getSpacing());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 76:
                {
                    ExpandItem[] v_ = getItems();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(76);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 77:
                {
                    int v_ = getSpacing();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(77);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 76, 77 });

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
