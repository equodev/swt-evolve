package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import java.util.ArrayList;

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
        ArrayList<FontData> result = new ArrayList<>(values.length);
        for (FontData v : values) if (v != null)
            result.add(v);
        return result.toArray(new FontData[0]);
    }

    public void setFontList(FontData[] value) {
        ((DartFontDialog) impl).fontList = value;
    }

    @Override
    public int wireTypeId() {
        return 672;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
        w.key(668);
        w.write(getRGB());
        w.key(669);
        w.write(getEffectsVisible());
        w.key(670);
        w.object((getFontData() == null ? null : ((DartFontData) getFontData().getImpl()).getValue()));
        w.key(671);
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
