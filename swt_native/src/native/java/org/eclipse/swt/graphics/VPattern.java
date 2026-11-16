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

    public static final int COLOR1 = 407;

    public static final int COLOR2 = 408;

    public static final int END_X = 409;

    public static final int END_Y = 410;

    public static final int IMAGE = 411;

    public static final int START_X = 412;

    public static final int START_Y = 413;

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
            case 407:
                w.write(getColor1());
                return true;
            case 408:
                w.write(getColor2());
                return true;
            case 409:
                w.write(getEndX());
                return true;
            case 410:
                w.write(getEndY());
                return true;
            case 411:
                w.image(getImage());
                return true;
            case 412:
                w.write(getStartX());
                return true;
            case 413:
                w.write(getStartY());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 407:
                {
                    Color v_ = getColor1();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(407);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 408:
                {
                    Color v_ = getColor2();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(408);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 409:
                {
                    float v_ = getEndX();
                    w.key(409);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 410:
                {
                    float v_ = getEndY();
                    w.key(410);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 411:
                {
                    Image v_ = getImage();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(411);
                    w.image(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 412:
                {
                    float v_ = getStartX();
                    w.key(412);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 413:
                {
                    float v_ = getStartY();
                    w.key(413);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VResource.allWireKeys_(), new int[] { 407, 408, 409, 410, 411, 412, 413 });

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
        w.key(407);
        w.write(getColor1());
        w.key(408);
        w.write(getColor2());
        w.key(409);
        w.write(getEndX());
        w.key(410);
        w.write(getEndY());
        w.key(411);
        w.image(getImage());
        w.key(412);
        w.write(getStartX());
        w.key(413);
        w.write(getStartY());
    }
}
