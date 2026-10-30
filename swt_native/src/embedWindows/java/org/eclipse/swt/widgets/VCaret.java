package org.eclipse.swt.widgets;

import java.util.*;
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

    public static final int BOUNDS = 365;

    public static final int FOCUSED = 1418;

    public static final int IMAGE = 1419;

    public static final int VISIBLE = 1420;

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
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 1420:
                w.write(getVisible());
                return true;
            case 365:
                w.write(getBounds());
                return true;
            case 1419:
                w.image(getImage());
                return true;
            case 1418:
                w.write(getFocused());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 1420:
                {
                    boolean v_ = getVisible();
                    w.key(1420);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 365:
                {
                    Rectangle v_ = getBounds();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(365);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1419:
                {
                    Image v_ = getImage();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1419);
                    w.image(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1418:
                {
                    boolean v_ = getFocused();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1418);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VWidget.allWireKeys_(), new int[] { 1420, 365, 1419, 1418 });

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
            case 1420:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
