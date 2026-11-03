package org.eclipse.swt.widgets;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.*;

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

    public static final int MAXIMUM = 413;

    public static final int MINIMUM = 414;

    public static final int SELECTION = 415;

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
            case 413:
                w.write((int) getMaximum());
                return true;
            case 414:
                w.write((int) getMinimum());
                return true;
            case 415:
                w.write((int) getSelection());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 413:
                {
                    int v_ = getMaximum();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(413);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 414:
                {
                    int v_ = getMinimum();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(414);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 415:
                {
                    int v_ = getSelection();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(415);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VControl.allWireKeys_(), new int[] { 413, 414, 415 });

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
