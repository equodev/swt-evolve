package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public class VCoolBar extends VComposite {

    protected VCoolBar() {
    }

    protected VCoolBar(DartCoolBar impl) {
        super(impl);
    }

    public int[] getItemOrder() {
        return ((DartCoolBar) impl).itemOrder;
    }

    public void setItemOrder(int[] value) {
        ((DartCoolBar) impl).itemOrder = value;
    }

    public Point[] getItemSizes() {
        Point[] values = ((DartCoolBar) impl).itemSizes;
        if (values == null)
            return null;
        int kept = 0;
        for (Point v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        Point[] result = new Point[kept];
        kept = 0;
        for (Point v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setItemSizes(Point[] value) {
        ((DartCoolBar) impl).itemSizes = value;
    }

    public CoolItem[] getItems() {
        CoolItem[] values = ((DartCoolBar) impl).items;
        if (values == null)
            return null;
        int kept = 0;
        for (CoolItem v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        CoolItem[] result = new CoolItem[kept];
        kept = 0;
        for (CoolItem v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setItems(CoolItem[] value) {
        ((DartCoolBar) impl).items = value;
    }

    public boolean getLocked() {
        return ((DartCoolBar) impl).getLocked();
    }

    public void setLocked(boolean value) {
        ((DartCoolBar) impl).locked = value;
    }

    public int[] getWrapIndices() {
        return ((DartCoolBar) impl).wrapIndices;
    }

    public void setWrapIndices(int[] value) {
        ((DartCoolBar) impl).wrapIndices = value;
    }

    public static final int ITEM_ORDER = 149;

    public static final int ITEM_SIZES = 150;

    public static final int ITEMS = 151;

    public static final int WRAP_INDICES = 152;

    @Override
    public String wireName(int key) {
        switch(key) {
            case ITEM_ORDER:
                return "itemOrder";
            case ITEM_SIZES:
                return "itemSizes";
            case ITEMS:
                return "items";
            case WRAP_INDICES:
                return "wrapIndices";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 149:
                w.write(getItemOrder());
                return true;
            case 150:
                {
                    Point[] a_ = getItemSizes();
                    if (a_ == null)
                        w.listSize(-1);
                    else {
                        w.listSize(a_.length);
                        for (Point e_ : a_) w.write(e_);
                    }
                }
                return true;
            case 151:
                w.refs(getItems());
                return true;
            case 152:
                w.write(getWrapIndices());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 149:
                {
                    int[] v_ = getItemOrder();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(149);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 150:
                {
                    Point[] v_ = getItemSizes();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(150);
                    {
                        Point[] a_ = v_;
                        if (a_ == null)
                            w.listSize(-1);
                        else {
                            w.listSize(a_.length);
                            for (Point e_ : a_) w.write(e_);
                        }
                    }
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 151:
                {
                    CoolItem[] v_ = getItems();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(151);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 152:
                {
                    int[] v_ = getWrapIndices();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(152);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 149, 150, 151, 152 });

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
