package org.eclipse.swt.graphics;

import java.util.*;
import org.eclipse.swt.*;

public class VCursor extends VResource {

    protected VCursor() {
    }

    protected VCursor(DartCursor impl) {
        super(impl);
    }

    public int getCursorStyle() {
        return ((DartCursor) impl).cursorStyle;
    }

    public void setCursorStyle(int value) {
        ((DartCursor) impl).cursorStyle = value;
    }

    public int getHotspotX() {
        return ((DartCursor) impl).hotspotX;
    }

    public void setHotspotX(int value) {
        ((DartCursor) impl).hotspotX = value;
    }

    public int getHotspotY() {
        return ((DartCursor) impl).hotspotY;
    }

    public void setHotspotY(int value) {
        ((DartCursor) impl).hotspotY = value;
    }

    public Image getImage() {
        Image val = ((DartCursor) impl).image;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setImage(Image value) {
        ((DartCursor) impl).image = value;
    }

    public static final int CURSOR_STYLE = 156;

    public static final int HOTSPOT_X = 157;

    public static final int HOTSPOT_Y = 158;

    public static final int IMAGE = 159;

    @Override
    public String wireName(int key) {
        switch(key) {
            case CURSOR_STYLE:
                return "cursorStyle";
            case HOTSPOT_X:
                return "hotspotX";
            case HOTSPOT_Y:
                return "hotspotY";
            case IMAGE:
                return "image";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 156:
                w.write((int) getCursorStyle());
                return true;
            case 157:
                w.write((int) getHotspotX());
                return true;
            case 158:
                w.write((int) getHotspotY());
                return true;
            case 159:
                w.image(getImage());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 156:
                {
                    int v_ = getCursorStyle();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(156);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 157:
                {
                    int v_ = getHotspotX();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(157);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 158:
                {
                    int v_ = getHotspotY();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(158);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 159:
                {
                    Image v_ = getImage();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(159);
                    w.image(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VResource.allWireKeys_(), new int[] { 156, 157, 158, 159 });

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

    @Override
    public int wireTypeId() {
        return 25;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
        w.key(156);
        w.write((int) getCursorStyle());
        w.key(157);
        w.write((int) getHotspotX());
        w.key(158);
        w.write((int) getHotspotY());
        w.key(159);
        w.image(getImage());
    }
}
