package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public class VFontDialog extends VDialog {

    protected VFontDialog() {
    }

    protected VFontDialog(DartFontDialog impl) {
        super(impl);
    }

    public RGB getRGB() {
        return ((DartFontDialog) impl).rgb;
    }

    public void setRGB(RGB value) {
        ((DartFontDialog) impl).rgb = value;
    }

    public boolean getEffectsVisible() {
        return ((DartFontDialog) impl).getEffectsVisible();
    }

    public void setEffectsVisible(boolean value) {
        ((DartFontDialog) impl).effectsVisible = value;
    }

    @SuppressWarnings("removal")
    public FontData getFontData() {
        FontData val = ((DartFontDialog) impl).fontData;
        if (val != null && !(val.getImpl() instanceof DartFontData))
            return null;
        return val;
    }

    public void setFontData(FontData value) {
        ((DartFontDialog) impl).fontData = value;
    }

    public FontData[] getFontList() {
        FontData[] values = ((DartFontDialog) impl).fontList;
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

    public void setFontList(FontData[] value) {
        ((DartFontDialog) impl).fontList = value;
    }

    @Override
    public int wireTypeId() {
        return 42;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
        w.key(235);
        w.write(getRGB());
        w.key(236);
        w.write(getEffectsVisible());
        w.key(237);
        w.object((getFontData() == null ? null : ((DartFontData) getFontData().getImpl()).getValue()));
        w.key(238);
        {
            FontData[] a_ = getFontList();
            if (a_ == null)
                w.listSize(-1);
            else {
                w.listSize(a_.length);
                for (FontData e_ : a_) w.object((e_ == null ? null : ((DartFontData) e_.getImpl()).getValue()));
            }
        }
    }
}
