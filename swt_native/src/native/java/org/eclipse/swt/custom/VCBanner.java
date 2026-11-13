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

    public int[] getCurveLine() {
        return ((DartCBanner) impl).wireCurveLine();
    }

    public int getBottomLine() {
        return ((DartCBanner) impl).wireBottomLine();
    }

    public static final int BOTTOM_LINE = 18;

    public static final int CURVE_LINE = 19;

    @Override
    public String wireName(int key) {
        switch(key) {
            case CURVE_LINE:
                return "curveLine";
            case BOTTOM_LINE:
                return "bottomLine";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 147:
                return false;
            case 149:
                return false;
            case 19:
                w.write(getCurveLine());
                return true;
            case 18:
                w.write((int) getBottomLine());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 147:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 149:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 19:
                {
                    int[] v_ = getCurveLine();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(19);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 18:
                {
                    int v_ = getBottomLine();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(18);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 19, 18 });

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
