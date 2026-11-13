package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class VSashForm extends VComposite {

    protected VSashForm() {
    }

    protected VSashForm(DartSashForm impl) {
        super(impl);
    }

    public Control getMaximizedControl() {
        Control val = ((DartSashForm) impl).maxControl;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setMaximizedControl(Control value) {
        ((DartSashForm) impl).maxControl = value;
    }

    public int getSashWidth() {
        return ((DartSashForm) impl).getSashWidth();
    }

    public void setSashWidth(int value) {
        ((DartSashForm) impl).getApi().SASH_WIDTH = value;
    }

    public int[] getWeights() {
        return ((DartSashForm) impl).weights;
    }

    public void setWeights(int[] value) {
        ((DartSashForm) impl).weights = value;
    }

    public static final int MAXIMIZED_CONTROL = 437;

    public static final int SASH_WIDTH = 438;

    public static final int WEIGHTS = 439;

    @Override
    public String wireName(int key) {
        switch(key) {
            case MAXIMIZED_CONTROL:
                return "maximizedControl";
            case SASH_WIDTH:
                return "sashWidth";
            case WEIGHTS:
                return "weights";
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
            case 437:
                w.ref(getMaximizedControl());
                return true;
            case 438:
                w.write((int) getSashWidth());
                return true;
            case 439:
                w.write(getWeights());
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
            case 437:
                {
                    Control v_ = getMaximizedControl();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(437);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 438:
                {
                    int v_ = getSashWidth();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(438);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 439:
                {
                    int[] v_ = getWeights();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(439);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 437, 438, 439 });

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
