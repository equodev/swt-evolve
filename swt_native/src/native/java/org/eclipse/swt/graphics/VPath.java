package org.eclipse.swt.graphics;

import org.eclipse.swt.*;

public class VPath extends VResource {

    protected VPath() {
    }

    protected VPath(DartPath impl) {
        super(impl);
    }

    public PathData getPathData() {
        return ((DartPath) impl).pathData;
    }

    public void setPathData(PathData value) {
        ((DartPath) impl).pathData = value;
    }

    public String[] getTextStrings() {
        return ((DartPath) impl).wireTextStrings();
    }

    public float[] getTextOrigins() {
        return ((DartPath) impl).wireTextOrigins();
    }

    public Font[] getTextFonts() {
        return ((DartPath) impl).wireTextFonts();
    }

    public static final int PATH_DATA = 392;

    public static final int TEXT_FONTS = 393;

    public static final int TEXT_ORIGINS = 394;

    public static final int TEXT_STRINGS = 395;

    @Override
    public String wireName(int key) {
        switch(key) {
            case PATH_DATA:
                return "pathData";
            case TEXT_STRINGS:
                return "textStrings";
            case TEXT_ORIGINS:
                return "textOrigins";
            case TEXT_FONTS:
                return "textFonts";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 392:
                w.object(getPathData());
                return true;
            case 395:
                w.write(getTextStrings());
                return true;
            case 394:
                w.write(getTextOrigins());
                return true;
            case 393:
                {
                    Font[] a_ = getTextFonts();
                    if (a_ == null)
                        w.listSize(-1);
                    else {
                        w.listSize(a_.length);
                        for (Font e_ : a_) w.object((e_ == null || e_.isDisposed() ? null : ((DartFont) e_.getImpl()).getValue()));
                    }
                }
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 392:
                {
                    PathData v_ = getPathData();
                    dev.equo.swt.WireObject o_ = v_;
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(392);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 395:
                {
                    String[] v_ = getTextStrings();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(395);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 394:
                {
                    float[] v_ = getTextOrigins();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(394);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 393:
                {
                    Font[] v_ = getTextFonts();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(393);
                    {
                        Font[] a_ = v_;
                        if (a_ == null)
                            w.listSize(-1);
                        else {
                            w.listSize(a_.length);
                            for (Font e_ : a_) w.object((e_ == null || e_.isDisposed() ? null : ((DartFont) e_.getImpl()).getValue()));
                        }
                    }
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VResource.allWireKeys_(), new int[] { 392, 395, 394, 393 });

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
        return 72;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
        w.key(392);
        w.object(getPathData());
        w.key(395);
        w.write(getTextStrings());
        w.key(394);
        w.write(getTextOrigins());
        w.key(393);
        {
            Font[] a_ = getTextFonts();
            if (a_ == null)
                w.listSize(-1);
            else {
                w.listSize(a_.length);
                for (Font e_ : a_) w.object((e_ == null || e_.isDisposed() ? null : ((DartFont) e_.getImpl()).getValue()));
            }
        }
    }
}
