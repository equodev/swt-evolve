package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class VCLabel extends VCanvas {

    protected VCLabel() {
    }

    protected VCLabel(DartCLabel impl) {
        super(impl);
    }

    public int getAlignment() {
        return ((DartCLabel) impl).getAlignment();
    }

    public void setAlignment(int value) {
        ((DartCLabel) impl).align = value;
    }

    public Image getBackgroundImage() {
        Image val = ((DartCLabel) impl).backgroundImage;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setBackgroundImage(Image value) {
        ((DartCLabel) impl).backgroundImage = value;
    }

    public int getBottomMargin() {
        return ((DartCLabel) impl).getBottomMargin();
    }

    public void setBottomMargin(int value) {
        ((DartCLabel) impl).bottomMargin = value;
    }

    public Color[] getGradientColors() {
        Color[] values = ((DartCLabel) impl).gradientColors;
        if (values == null)
            return null;
        int kept = 0;
        for (Color v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        Color[] result = new Color[kept];
        kept = 0;
        for (Color v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setGradientColors(Color[] value) {
        ((DartCLabel) impl).gradientColors = value;
    }

    public int[] getGradientPercents() {
        return ((DartCLabel) impl).gradientPercents;
    }

    public void setGradientPercents(int[] value) {
        ((DartCLabel) impl).gradientPercents = value;
    }

    public boolean getGradientVertical() {
        return ((DartCLabel) impl).gradientVertical;
    }

    public void setGradientVertical(boolean value) {
        ((DartCLabel) impl).gradientVertical = value;
    }

    public Image getImage() {
        Image val = ((DartCLabel) impl).image;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setImage(Image value) {
        ((DartCLabel) impl).image = value;
    }

    public int getLeftMargin() {
        return ((DartCLabel) impl).getLeftMargin();
    }

    public void setLeftMargin(int value) {
        ((DartCLabel) impl).leftMargin = value;
    }

    public int getRightMargin() {
        return ((DartCLabel) impl).getRightMargin();
    }

    public void setRightMargin(int value) {
        ((DartCLabel) impl).rightMargin = value;
    }

    public String getText() {
        return ((DartCLabel) impl).getText();
    }

    public void setText(String value) {
        ((DartCLabel) impl).text = value;
    }

    public int getTopMargin() {
        return ((DartCLabel) impl).getTopMargin();
    }

    public void setTopMargin(int value) {
        ((DartCLabel) impl).topMargin = value;
    }

    public static final int ALIGNMENT = 25;

    public static final int BACKGROUND_IMAGE = 26;

    public static final int BOTTOM_MARGIN = 27;

    public static final int GRADIENT_COLORS = 28;

    public static final int GRADIENT_PERCENTS = 29;

    public static final int GRADIENT_VERTICAL = 30;

    public static final int IMAGE = 31;

    public static final int LEFT_MARGIN = 32;

    public static final int RIGHT_MARGIN = 33;

    public static final int TEXT = 34;

    public static final int TOP_MARGIN = 35;

    @Override
    public String wireName(int key) {
        switch(key) {
            case ALIGNMENT:
                return "alignment";
            case BACKGROUND_IMAGE:
                return "backgroundImage";
            case BOTTOM_MARGIN:
                return "bottomMargin";
            case GRADIENT_COLORS:
                return "gradientColors";
            case GRADIENT_PERCENTS:
                return "gradientPercents";
            case GRADIENT_VERTICAL:
                return "gradientVertical";
            case IMAGE:
                return "image";
            case LEFT_MARGIN:
                return "leftMargin";
            case RIGHT_MARGIN:
                return "rightMargin";
            case TEXT:
                return "text";
            case TOP_MARGIN:
                return "topMargin";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 25:
                w.write((int) getAlignment());
                return true;
            case 26:
                w.image(getBackgroundImage());
                return true;
            case 27:
                w.write((int) getBottomMargin());
                return true;
            case 28:
                {
                    Color[] a_ = getGradientColors();
                    if (a_ == null)
                        w.listSize(-1);
                    else {
                        w.listSize(a_.length);
                        for (Color e_ : a_) w.write(e_);
                    }
                }
                return true;
            case 29:
                w.write(getGradientPercents());
                return true;
            case 30:
                w.write(getGradientVertical());
                return true;
            case 31:
                w.image(getImage());
                return true;
            case 32:
                w.write((int) getLeftMargin());
                return true;
            case 33:
                w.write((int) getRightMargin());
                return true;
            case 34:
                w.write(getText());
                return true;
            case 35:
                w.write((int) getTopMargin());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 25:
                {
                    int v_ = getAlignment();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(25);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 26:
                {
                    Image v_ = getBackgroundImage();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(26);
                    w.image(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 27:
                {
                    int v_ = getBottomMargin();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(27);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 28:
                {
                    Color[] v_ = getGradientColors();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(28);
                    {
                        Color[] a_ = v_;
                        if (a_ == null)
                            w.listSize(-1);
                        else {
                            w.listSize(a_.length);
                            for (Color e_ : a_) w.write(e_);
                        }
                    }
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 29:
                {
                    int[] v_ = getGradientPercents();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(29);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 30:
                {
                    boolean v_ = getGradientVertical();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(30);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 31:
                {
                    Image v_ = getImage();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(31);
                    w.image(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 32:
                {
                    int v_ = getLeftMargin();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(32);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 33:
                {
                    int v_ = getRightMargin();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(33);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 34:
                {
                    String v_ = getText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(34);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 35:
                {
                    int v_ = getTopMargin();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(35);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VCanvas.allWireKeys_(), new int[] { 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35 });

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
