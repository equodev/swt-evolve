package org.eclipse.swt.graphics;

import org.eclipse.swt.*;

public class VPattern extends VResource {

    protected VPattern() {
    }

    protected VPattern(DartPattern impl) {
        super(impl);
    }

    public Color getColor1() {
        return ((DartPattern) impl).color1;
    }

    public void setColor1(Color value) {
    }

    public Color getColor2() {
        return ((DartPattern) impl).color2;
    }

    public void setColor2(Color value) {
    }

    public float getEndX() {
        return ((DartPattern) impl).endX;
    }

    public void setEndX(float value) {
        ((DartPattern) impl).endX = value;
    }

    public float getEndY() {
        return ((DartPattern) impl).endY;
    }

    public void setEndY(float value) {
        ((DartPattern) impl).endY = value;
    }

    public Image getImage() {
        Image val = ((DartPattern) impl).image;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setImage(Image value) {
        ((DartPattern) impl).image = value;
    }

    public float getStartX() {
        return ((DartPattern) impl).startX;
    }

    public void setStartX(float value) {
        ((DartPattern) impl).startX = value;
    }

    public float getStartY() {
        return ((DartPattern) impl).startY;
    }

    public void setStartY(float value) {
        ((DartPattern) impl).startY = value;
    }

    public static final int COLOR1 = 394;

    public static final int COLOR2 = 395;

    public static final int END_X = 396;

    public static final int END_Y = 397;

    public static final int IMAGE = 398;

    public static final int START_X = 399;

    public static final int START_Y = 400;

    @Override
    public String wireName(int key) {
        switch(key) {
            case COLOR1:
                return "color1";
            case COLOR2:
                return "color2";
            case END_X:
                return "endX";
            case END_Y:
                return "endY";
            case IMAGE:
                return "image";
            case START_X:
                return "startX";
            case START_Y:
                return "startY";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 394:
                w.write(getColor1());
                return true;
            case 395:
                w.write(getColor2());
                return true;
            case 396:
                w.write(getEndX());
                return true;
            case 397:
                w.write(getEndY());
                return true;
            case 398:
                w.image(getImage());
                return true;
            case 399:
                w.write(getStartX());
                return true;
            case 400:
                w.write(getStartY());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 394:
                {
                    Color v_ = getColor1();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(394);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 395:
                {
                    Color v_ = getColor2();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(395);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 396:
                {
                    float v_ = getEndX();
                    w.key(396);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 397:
                {
                    float v_ = getEndY();
                    w.key(397);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 398:
                {
                    Image v_ = getImage();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(398);
                    w.image(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 399:
                {
                    float v_ = getStartX();
                    w.key(399);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 400:
                {
                    float v_ = getStartY();
                    w.key(400);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VResource.allWireKeys_(), new int[] { 394, 395, 396, 397, 398, 399, 400 });

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
        return 74;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
        w.key(394);
        w.write(getColor1());
        w.key(395);
        w.write(getColor2());
        w.key(396);
        w.write(getEndX());
        w.key(397);
        w.write(getEndY());
        w.key(398);
        w.image(getImage());
        w.key(399);
        w.write(getStartX());
        w.key(400);
        w.write(getStartY());
    }
}
