package org.eclipse.swt.graphics;

import org.eclipse.swt.*;

public class VColor extends VResource {

    protected VColor() {
    }

    protected VColor(IColor impl) {
        super(impl);
    }

    public int getAlpha() {
        return ((IColor) impl).getAlpha();
    }

    public void setAlpha(int value) {
    }

    public int getBlue() {
        return ((IColor) impl).getBlue();
    }

    public void setBlue(int value) {
    }

    public int getGreen() {
        return ((IColor) impl).getGreen();
    }

    public void setGreen(int value) {
    }

    public int getRed() {
        return ((IColor) impl).getRed();
    }

    public void setRed(int value) {
    }

    public static final int A = 45;

    public static final int B = 46;

    public static final int G = 47;

    public static final int R = 48;

    @Override
    public String wireName(int key) {
        switch(key) {
            case A:
                return "a";
            case B:
                return "b";
            case G:
                return "g";
            case R:
                return "r";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 45:
                w.write((int) getAlpha());
                return true;
            case 46:
                w.write((int) getBlue());
                return true;
            case 47:
                w.write((int) getGreen());
                return true;
            case 48:
                w.write((int) getRed());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 45:
                {
                    int v_ = getAlpha();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(45);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 46:
                {
                    int v_ = getBlue();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(46);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 47:
                {
                    int v_ = getGreen();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(47);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 48:
                {
                    int v_ = getRed();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(48);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VResource.allWireKeys_(), new int[] { 45, 46, 47, 48 });

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
        return 49;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
        w.key(45);
        w.write((int) getAlpha());
        w.key(46);
        w.write((int) getBlue());
        w.key(47);
        w.write((int) getGreen());
        w.key(48);
        w.write((int) getRed());
    }
}
