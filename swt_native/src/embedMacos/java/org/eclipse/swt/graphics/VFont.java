package org.eclipse.swt.graphics;

import org.eclipse.swt.*;

public class VFont extends VResource {

    protected VFont() {
    }

    protected VFont(DartFont impl) {
        super(impl);
    }

    public FontData[] getFontData() {
        FontData[] values = ((DartFont) impl).fontData;
        if (values == null)
            return null;
        int kept = 0;
        for (FontData v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        FontData[] result = new FontData[kept];
        kept = 0;
        for (FontData v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setFontData(FontData[] value) {
        ((DartFont) impl).fontData = value;
    }

    public static final int FONT_DATA = 233;

    @Override
    public String wireName(int key) {
        switch(key) {
            case FONT_DATA:
                return "fontData";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 233:
                {
                    FontData[] a_ = getFontData();
                    if (a_ == null)
                        w.listSize(-1);
                    else {
                        w.listSize(a_.length);
                        for (FontData e_ : a_) w.object((e_ == null ? null : e_.getImpl() instanceof DartFontData ? ((DartFontData) e_.getImpl()).getValue() : ((DartFontData) org.eclipse.swt.graphics.GraphicsUtils.copyFontData(e_).getImpl()).getValue()));
                    }
                }
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 233:
                {
                    FontData[] v_ = getFontData();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(233);
                    {
                        FontData[] a_ = v_;
                        if (a_ == null)
                            w.listSize(-1);
                        else {
                            w.listSize(a_.length);
                            for (FontData e_ : a_) w.object((e_ == null ? null : e_.getImpl() instanceof DartFontData ? ((DartFontData) e_.getImpl()).getValue() : ((DartFontData) org.eclipse.swt.graphics.GraphicsUtils.copyFontData(e_).getImpl()).getValue()));
                        }
                    }
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VResource.allWireKeys_(), new int[] { 233 });

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
        return 40;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
        w.key(233);
        {
            FontData[] a_ = getFontData();
            if (a_ == null)
                w.listSize(-1);
            else {
                w.listSize(a_.length);
                for (FontData e_ : a_) w.object((e_ == null ? null : e_.getImpl() instanceof DartFontData ? ((DartFontData) e_.getImpl()).getValue() : ((DartFontData) org.eclipse.swt.graphics.GraphicsUtils.copyFontData(e_).getImpl()).getValue()));
            }
        }
    }
}
