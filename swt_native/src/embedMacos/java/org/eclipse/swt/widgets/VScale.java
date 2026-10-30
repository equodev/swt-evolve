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

    public static final int MAXIMUM = 442;

    public static final int MINIMUM = 443;

    public static final int SELECTION = 444;

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
            case 442:
                w.write((int) getMaximum());
                return true;
            case 443:
                w.write((int) getMinimum());
                return true;
            case 444:
                w.write((int) getSelection());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 442:
                {
                    int v_ = getMaximum();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(442);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 443:
                {
                    int v_ = getMinimum();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(443);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 444:
                {
                    int v_ = getSelection();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(444);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VControl.allWireKeys_(), new int[] { 442, 443, 444 });

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
