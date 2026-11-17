package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public class VCaret extends VWidget {

    protected VCaret() {
    }

    protected VCaret(DartCaret impl) {
        super(impl);
    }

    public Font getFont() {
        Font val = ((DartCaret) impl).font;
        if (val != null && !(val.getImpl() instanceof DartFont))
            return null;
        return val;
    }

    public void setFont(Font value) {
        ((DartCaret) impl).font = value;
    }

    public boolean getVisible() {
        return ((DartCaret) impl).getVisible();
    }

    public void setVisible(boolean value) {
        ((DartCaret) impl).isVisible = value;
    }

    public Rectangle getBounds() {
        return ((DartCaret) impl).getBounds();
    }

    public Image getImage() {
        return ((DartCaret) impl).image;
    }

    public boolean getFocused() {
        return ((DartCaret) impl).isFocusCaret();
    }

    public int getBlinkRate() {
        return ((DartCaret) impl).blinkRate;
    }

    public static final int BLINK_RATE = 704;

    public static final int BOUNDS = 705;

    public static final int FOCUSED = 706;

    public static final int IMAGE = 707;

    public static final int VISIBLE = 708;

    @Override
    public String wireName(int key) {
        switch(key) {
            case VISIBLE:
                return "visible";
            case BOUNDS:
                return "bounds";
            case IMAGE:
                return "image";
            case FOCUSED:
                return "focused";
            case BLINK_RATE:
                return "blinkRate";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 6208:
                return false;
            case 708:
                w.write(getVisible());
                return true;
            case 705:
                w.write(getBounds());
                return true;
            case 707:
                w.image(getImage());
                return true;
            case 706:
                w.write(getFocused());
                return true;
            case 704:
                w.write((int) getBlinkRate());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 6208:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 708:
                {
                    boolean v_ = getVisible();
                    w.key(708);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 705:
                {
                    Rectangle v_ = getBounds();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(705);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 707:
                {
                    Image v_ = getImage();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(707);
                    w.image(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 706:
                {
                    boolean v_ = getFocused();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(706);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 704:
                {
                    int v_ = getBlinkRate();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(704);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VWidget.allWireKeys_(), new int[] { 708, 705, 707, 706, 704 });

    protected static int[] allWireKeys_() {
        return wireKeys_;
    }

    @Override
    public int[] wireKeys() {
        return wireKeys_;
    }

    @Override
    public boolean wireAlways(int key) {
        switch(key) {
            case 708:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
