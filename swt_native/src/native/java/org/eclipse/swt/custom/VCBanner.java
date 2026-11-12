package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class VCBanner extends VComposite {

    protected VCBanner() {
    }

    protected VCBanner(DartCBanner impl) {
        super(impl);
    }

    public Control getBottom() {
        Control val = ((DartCBanner) impl).bottom;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setBottom(Control value) {
        ((DartCBanner) impl).bottom = value;
    }

    public Control getLeft() {
        Control val = ((DartCBanner) impl).left;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setLeft(Control value) {
        ((DartCBanner) impl).left = value;
    }

    public Control getRight() {
        Control val = ((DartCBanner) impl).right;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setRight(Control value) {
        ((DartCBanner) impl).right = value;
    }

    public Point getRightMinimumSize() {
        return ((DartCBanner) impl).rightMinimumSize;
    }

    public void setRightMinimumSize(Point value) {
        ((DartCBanner) impl).rightMinimumSize = value;
    }

    public int getRightWidth() {
        return ((DartCBanner) impl).getRightWidth();
    }

    public void setRightWidth(int value) {
        ((DartCBanner) impl).rightWidth = value;
    }

    public boolean getSimple() {
        return ((DartCBanner) impl).getSimple();
    }

    public void setSimple(boolean value) {
        ((DartCBanner) impl).simple = value;
    }

    @Override
    public String wireName(int key) {
        switch(key) {
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 144:
                return false;
            case 146:
                return false;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 144:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 146:
                return dev.equo.swt.WidgetWire.UNHANDLED;
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] {});

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
