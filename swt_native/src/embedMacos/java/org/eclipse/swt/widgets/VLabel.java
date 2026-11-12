package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public class VLabel extends VControl {

    protected VLabel() {
    }

    protected VLabel(DartLabel impl) {
        super(impl);
    }

    public int getAlignment() {
        return ((DartLabel) impl).getAlignment();
    }

    public void setAlignment(int value) {
        ((DartLabel) impl).setAlignment(value);
    }

    public Image getImage() {
        Image val = ((DartLabel) impl)._displayedImage();
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setImage(Image value) {
        ((DartLabel) impl).image = value;
    }

    public String getText() {
        return ((DartLabel) impl)._displayedText();
    }

    public void setText(String value) {
        ((DartLabel) impl).text = value;
    }

    public static final int IMAGE = 355;

    public static final int TEXT = 356;

    @Override
    public String wireName(int key) {
        switch(key) {
            case IMAGE:
                return "image";
            case TEXT:
                return "text";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 148:
                return false;
            case 151:
                return false;
            case 355:
                w.image(getImage());
                return true;
            case 356:
                w.write(getText());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 148:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 151:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 355:
                {
                    Image v_ = getImage();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(355);
                    w.image(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 356:
                {
                    String v_ = getText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(356);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VControl.allWireKeys_(), new int[] { 355, 356 });

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
