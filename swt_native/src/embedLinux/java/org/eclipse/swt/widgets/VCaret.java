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
        if (val != null && val.getImpl() instanceof SwtFont)
            return GraphicsUtils.copyFont(val);
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

    public static final int BLINK_RATE = 69;

    public static final int BOUNDS = 70;

    public static final int FOCUSED = 71;

    public static final int IMAGE = 72;

    public static final int VISIBLE = 73;

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
            case 73:
                w.write(getVisible());
                return true;
            case 70:
                w.write(getBounds());
                return true;
            case 72:
                w.image(getImage());
                return true;
            case 71:
                w.write(getFocused());
                return true;
            case 69:
                w.write((int) getBlinkRate());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 73:
                {
                    boolean v_ = getVisible();
                    w.key(73);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 70:
                {
                    Rectangle v_ = getBounds();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(70);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 72:
                {
                    Image v_ = getImage();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(72);
                    w.image(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 71:
                {
                    boolean v_ = getFocused();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(71);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 69:
                {
                    int v_ = getBlinkRate();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(69);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VWidget.allWireKeys_(), new int[] { 73, 70, 72, 71, 69 });

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
            case 73:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
