package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public class VSlider extends VControl {

    protected VSlider() {
    }

    protected VSlider(DartSlider impl) {
        super(impl);
    }

    public int getIncrement() {
        return ((DartSlider) impl).getIncrement();
    }

    public void setIncrement(int value) {
        ((DartSlider) impl).increment = value;
    }

    public int getMaximum() {
        return ((DartSlider) impl).getMaximum();
    }

    public void setMaximum(int value) {
        ((DartSlider) impl).maximum = value;
    }

    public int getMinimum() {
        return ((DartSlider) impl).getMinimum();
    }

    public void setMinimum(int value) {
        ((DartSlider) impl).minimum = value;
    }

    public int getPageIncrement() {
        return ((DartSlider) impl).getPageIncrement();
    }

    public void setPageIncrement(int value) {
        ((DartSlider) impl).pageIncrement = value;
    }

    public int getSelection() {
        return ((DartSlider) impl).getSelection();
    }

    public void setSelection(int value) {
        ((DartSlider) impl).selection = value;
    }

    public int getThumb() {
        return ((DartSlider) impl).getThumb();
    }

    public void setThumb(int value) {
        ((DartSlider) impl).thumb = value;
    }

    public static final int INCREMENT = 467;

    public static final int MAXIMUM = 468;

    public static final int MINIMUM = 469;

    public static final int PAGE_INCREMENT = 470;

    public static final int SELECTION = 471;

    public static final int THUMB = 472;

    @Override
    public String wireName(int key) {
        switch(key) {
            case INCREMENT:
                return "increment";
            case MAXIMUM:
                return "maximum";
            case MINIMUM:
                return "minimum";
            case PAGE_INCREMENT:
                return "pageIncrement";
            case SELECTION:
                return "selection";
            case THUMB:
                return "thumb";
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
            case 150:
                return false;
            case 153:
                return false;
            case 467:
                w.write((int) getIncrement());
                return true;
            case 468:
                w.write((int) getMaximum());
                return true;
            case 469:
                w.write((int) getMinimum());
                return true;
            case 470:
                w.write((int) getPageIncrement());
                return true;
            case 471:
                w.write((int) getSelection());
                return true;
            case 472:
                w.write((int) getThumb());
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
            case 150:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 153:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 467:
                {
                    int v_ = getIncrement();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(467);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 468:
                {
                    int v_ = getMaximum();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(468);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 469:
                {
                    int v_ = getMinimum();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(469);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 470:
                {
                    int v_ = getPageIncrement();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(470);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 471:
                {
                    int v_ = getSelection();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(471);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 472:
                {
                    int v_ = getThumb();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(472);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VControl.allWireKeys_(), new int[] { 467, 468, 469, 470, 471, 472 });

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
