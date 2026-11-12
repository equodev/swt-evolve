package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public class VScale extends VControl {

    protected VScale() {
    }

    protected VScale(DartScale impl) {
        super(impl);
    }

    public int getIncrement() {
        return ((DartScale) impl).getIncrement();
    }

    public void setIncrement(int value) {
        ((DartScale) impl).increment = value;
    }

    public int getMaximum() {
        return ((DartScale) impl).getMaximum();
    }

    public void setMaximum(int value) {
        ((DartScale) impl).maximum = value;
    }

    public int getMinimum() {
        return ((DartScale) impl).getMinimum();
    }

    public void setMinimum(int value) {
        ((DartScale) impl).minimum = value;
    }

    public int getPageIncrement() {
        return ((DartScale) impl).getPageIncrement();
    }

    public void setPageIncrement(int value) {
        ((DartScale) impl).pageIncrement = value;
    }

    public int getSelection() {
        return ((DartScale) impl).getSelection();
    }

    public void setSelection(int value) {
        ((DartScale) impl).selection = value;
    }

    public static final int MAXIMUM = 437;

    public static final int MINIMUM = 438;

    public static final int SELECTION = 439;

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
            case 437:
                w.write((int) getMaximum());
                return true;
            case 438:
                w.write((int) getMinimum());
                return true;
            case 439:
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
            case 437:
                {
                    int v_ = getMaximum();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(437);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 438:
                {
                    int v_ = getMinimum();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(438);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 439:
                {
                    int v_ = getSelection();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(439);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VControl.allWireKeys_(), new int[] { 437, 438, 439 });

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
