package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public class VProgressBar extends VControl {

    protected VProgressBar() {
    }

    protected VProgressBar(DartProgressBar impl) {
        super(impl);
    }

    public int getMaximum() {
        return ((DartProgressBar) impl).getMaximum();
    }

    public void setMaximum(int value) {
        ((DartProgressBar) impl).maximum = value;
    }

    public int getMinimum() {
        return ((DartProgressBar) impl).getMinimum();
    }

    public void setMinimum(int value) {
        ((DartProgressBar) impl).minimum = value;
    }

    public int getSelection() {
        return ((DartProgressBar) impl).getSelection();
    }

    public void setSelection(int value) {
        ((DartProgressBar) impl).selection = value;
    }

    public static final int MAXIMUM = 420;

    public static final int MINIMUM = 421;

    public static final int SELECTION = 422;

    @Override
    public String wireName(int key) {
        switch(key) {
            case MAXIMUM:
                return "maximum";
            case MINIMUM:
                return "minimum";
            case SELECTION:
                return "selection";
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
            case 147:
                return false;
            case 150:
                return false;
            case 420:
                w.write((int) getMaximum());
                return true;
            case 421:
                w.write((int) getMinimum());
                return true;
            case 422:
                w.write((int) getSelection());
                return true;
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
            case 147:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 150:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 420:
                {
                    int v_ = getMaximum();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(420);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 421:
                {
                    int v_ = getMinimum();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(421);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 422:
                {
                    int v_ = getSelection();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(422);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VControl.allWireKeys_(), new int[] { 420, 421, 422 });

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
