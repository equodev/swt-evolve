package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public class VColorDialog extends VDialog {

    protected VColorDialog() {
    }

    protected VColorDialog(DartColorDialog impl) {
        super(impl);
    }

    public RGB getRGB() {
        return ((DartColorDialog) impl).rgb;
    }

    public void setRGB(RGB value) {
        ((DartColorDialog) impl).rgb = value;
    }

    public RGB[] getRGBs() {
        RGB[] values = ((DartColorDialog) impl).rgbs;
        if (values == null)
            return null;
        int kept = 0;
        for (RGB v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        RGB[] result = new RGB[kept];
        kept = 0;
        for (RGB v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setRGBs(RGB[] value) {
        ((DartColorDialog) impl).rgbs = value;
    }

    @Override
    public int wireTypeId() {
        return 19;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
        w.key(85);
        w.write(getRGB());
        w.key(86);
        {
            RGB[] a_ = getRGBs();
            if (a_ == null)
                w.listSize(-1);
            else {
                w.listSize(a_.length);
                for (RGB e_ : a_) w.write(e_);
            }
        }
    }
}
