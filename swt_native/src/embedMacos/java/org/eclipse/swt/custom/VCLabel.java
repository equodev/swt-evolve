package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import java.util.ArrayList;

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
        ArrayList<Color> result = new ArrayList<>(values.length);
        for (Color v : values) if (v != null)
            result.add(v);
        return result.toArray(new Color[0]);
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

    public static final int ALIGNMENT = 541;

    public static final int BACKGROUND_IMAGE = 542;

    public static final int BOTTOM_MARGIN = 543;

    public static final int GRADIENT_COLORS = 1357;

    public static final int GRADIENT_PERCENTS = 1358;

    public static final int GRADIENT_VERTICAL = 1359;

    public static final int IMAGE = 544;

    public static final int LEFT_MARGIN = 545;

    public static final int RIGHT_MARGIN = 546;

    public static final int TEXT = 547;

    public static final int TOP_MARGIN = 548;

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
            case 541:
                w.write((int) getAlignment());
                return true;
            case 542:
                w.image(getBackgroundImage());
                return true;
            case 543:
                w.write((int) getBottomMargin());
                return true;
            case 1357:
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
            case 1358:
                w.write(getGradientPercents());
                return true;
            case 1359:
                w.write(getGradientVertical());
                return true;
            case 544:
                w.image(getImage());
                return true;
            case 545:
                w.write((int) getLeftMargin());
                return true;
            case 546:
                w.write((int) getRightMargin());
                return true;
            case 547:
                w.write(getText());
                return true;
            case 548:
                w.write((int) getTopMargin());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 541:
                {
                    int v_ = getAlignment();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(541);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 542:
                {
                    Image v_ = getBackgroundImage();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(542);
                    w.image(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 543:
                {
                    int v_ = getBottomMargin();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(543);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1357:
                {
                    Color[] v_ = getGradientColors();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(1357);
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
            case 1358:
                {
                    int[] v_ = getGradientPercents();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(1358);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1359:
                {
                    boolean v_ = getGradientVertical();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1359);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 544:
                {
                    Image v_ = getImage();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(544);
                    w.image(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 545:
                {
                    int v_ = getLeftMargin();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(545);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 546:
                {
                    int v_ = getRightMargin();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(546);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 547:
                {
                    String v_ = getText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(547);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 548:
                {
                    int v_ = getTopMargin();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(548);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VCanvas.allWireKeys_(), new int[] { 541, 542, 543, 1357, 1358, 1359, 544, 545, 546, 547, 548 });

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
