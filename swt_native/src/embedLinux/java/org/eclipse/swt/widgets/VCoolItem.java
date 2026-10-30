package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public class VCoolItem extends VItem {

    protected VCoolItem() {
    }

    protected VCoolItem(DartCoolItem impl) {
        super(impl);
    }

    public Control getControl() {
        Control val = ((DartCoolItem) impl).control;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setControl(Control value) {
        ((DartCoolItem) impl).control = value;
    }

    public Point getMinimumSize() {
        return ((DartCoolItem) impl).getMinimumSize();
    }

    public void setMinimumSize(Point value) {
        ((DartCoolItem) impl).setMinimumSize(value);
    }

    public Point getPreferredSize() {
        return ((DartCoolItem) impl).getPreferredSize();
    }

    public void setPreferredSize(Point value) {
        ((DartCoolItem) impl).setPreferredSize(value);
    }

    public static final int CONTROL = 171;

    public static final int PREFERRED_SIZE = 172;

    @Override
    public String wireName(int key) {
        switch(key) {
            case CONTROL:
                return "control";
            case PREFERRED_SIZE:
                return "preferredSize";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 171:
                w.ref(getControl());
                return true;
            case 172:
                w.write(getPreferredSize());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 171:
                {
                    Control v_ = getControl();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(171);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 172:
                {
                    Point v_ = getPreferredSize();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(172);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VItem.allWireKeys_(), new int[] { 171, 172 });

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
