package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public class VExpandItem extends VItem {

    protected VExpandItem() {
    }

    protected VExpandItem(DartExpandItem impl) {
        super(impl);
    }

    public Control getControl() {
        Control val = ((DartExpandItem) impl).control;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setControl(Control value) {
        ((DartExpandItem) impl).control = value;
    }

    public boolean getExpanded() {
        return ((DartExpandItem) impl).getExpanded();
    }

    public void setExpanded(boolean value) {
        ((DartExpandItem) impl).expanded = value;
    }

    public int getHeight() {
        return ((DartExpandItem) impl).getHeight();
    }

    public void setHeight(int value) {
        ((DartExpandItem) impl).height = value;
    }

    public int getHeaderHeight() {
        return ((DartExpandItem) impl).wireHeaderHeight();
    }

    public static final int CONTROL = 1984;

    public static final int EXPANDED = 1985;

    public static final int HEADER_HEIGHT = 1986;

    public static final int HEIGHT = 1987;

    @Override
    public String wireName(int key) {
        switch(key) {
            case CONTROL:
                return "control";
            case EXPANDED:
                return "expanded";
            case HEIGHT:
                return "height";
            case HEADER_HEIGHT:
                return "headerHeight";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 6208:
                return false;
            case 1984:
                w.ref(getControl());
                return true;
            case 1985:
                w.write(getExpanded());
                return true;
            case 1987:
                w.write((int) getHeight());
                return true;
            case 1986:
                w.write((int) getHeaderHeight());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 6208:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 1984:
                {
                    Control v_ = getControl();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1984);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1985:
                {
                    boolean v_ = getExpanded();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1985);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1987:
                {
                    int v_ = getHeight();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1987);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1986:
                {
                    int v_ = getHeaderHeight();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1986);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VItem.allWireKeys_(), new int[] { 1984, 1985, 1987, 1986 });

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
