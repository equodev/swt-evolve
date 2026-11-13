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

    public static final int CONTROL = 230;

    public static final int EXPANDED = 231;

    public static final int HEADER_HEIGHT = 232;

    public static final int HEIGHT = 233;

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
            case 676:
                return false;
            case 230:
                w.ref(getControl());
                return true;
            case 231:
                w.write(getExpanded());
                return true;
            case 233:
                w.write((int) getHeight());
                return true;
            case 232:
                w.write((int) getHeaderHeight());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 676:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 230:
                {
                    Control v_ = getControl();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(230);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 231:
                {
                    boolean v_ = getExpanded();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(231);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 233:
                {
                    int v_ = getHeight();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(233);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 232:
                {
                    int v_ = getHeaderHeight();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(232);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VItem.allWireKeys_(), new int[] { 230, 231, 233, 232 });

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
