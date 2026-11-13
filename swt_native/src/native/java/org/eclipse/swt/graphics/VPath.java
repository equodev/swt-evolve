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

    public static final int PATH_DATA = 400;

    public static final int TEXT_FONTS = 401;

    public static final int TEXT_ORIGINS = 402;

    public static final int TEXT_STRINGS = 403;

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
            case 400:
                w.object(getPathData());
                return true;
            case 403:
                w.write(getTextStrings());
                return true;
            case 402:
                w.write(getTextOrigins());
                return true;
            case 401:
                {
                    Font[] a_ = getTextFonts();
                    if (a_ == null)
                        w.listSize(-1);
                    else {
                        w.listSize(a_.length);
                        for (int i_ = 0; i_ < a_.length; i_++) {
                            Font e_ = a_[i_];
                            if (e_ == null) {
                                i_ += w.nulls(a_, i_) - 1;
                                continue;
                            }
                            w.object((e_ == null || e_.isDisposed() ? null : ((DartFont) e_.getImpl()).getValue()));
                        }
                    }
                }
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 400:
                {
                    PathData v_ = getPathData();
                    dev.equo.swt.WireObject o_ = v_;
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(400);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 403:
                {
                    String[] v_ = getTextStrings();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(403);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 402:
                {
                    float[] v_ = getTextOrigins();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(402);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 401:
                {
                    Font[] v_ = getTextFonts();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(401);
                    {
                        Font[] a_ = v_;
                        if (a_ == null)
                            w.listSize(-1);
                        else {
                            w.listSize(a_.length);
                            for (int i_ = 0; i_ < a_.length; i_++) {
                                Font e_ = a_[i_];
                                if (e_ == null) {
                                    i_ += w.nulls(a_, i_) - 1;
                                    continue;
                                }
                                w.object((e_ == null || e_.isDisposed() ? null : ((DartFont) e_.getImpl()).getValue()));
                            }
                        }
                    }
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VResource.allWireKeys_(), new int[] { 400, 403, 402, 401 });

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
        w.key(400);
        w.object(getPathData());
        w.key(403);
        w.write(getTextStrings());
        w.key(402);
        w.write(getTextOrigins());
        w.key(401);
        {
            Font[] a_ = getTextFonts();
            if (a_ == null)
                w.listSize(-1);
            else {
                w.listSize(a_.length);
                for (int i_ = 0; i_ < a_.length; i_++) {
                    Font e_ = a_[i_];
                    if (e_ == null) {
                        i_ += w.nulls(a_, i_) - 1;
                        continue;
                    }
                    w.object((e_ == null || e_.isDisposed() ? null : ((DartFont) e_.getImpl()).getValue()));
                }
            }
        }
    }
}
