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

    public static final int INCREMENT = 454;

    public static final int MAXIMUM = 455;

    public static final int MINIMUM = 456;

    public static final int PAGE_INCREMENT = 457;

    public static final int SELECTION = 458;

    public static final int THUMB = 459;

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
            case 454:
                w.write((int) getIncrement());
                return true;
            case 455:
                w.write((int) getMaximum());
                return true;
            case 456:
                w.write((int) getMinimum());
                return true;
            case 457:
                w.write((int) getPageIncrement());
                return true;
            case 458:
                w.write((int) getSelection());
                return true;
            case 459:
                w.write((int) getThumb());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 454:
                {
                    int v_ = getIncrement();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(454);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 455:
                {
                    int v_ = getMaximum();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(455);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 456:
                {
                    int v_ = getMinimum();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(456);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 457:
                {
                    int v_ = getPageIncrement();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(457);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 458:
                {
                    int v_ = getSelection();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(458);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 459:
                {
                    int v_ = getThumb();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(459);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VControl.allWireKeys_(), new int[] { 454, 455, 456, 457, 458, 459 });

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
