package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import java.util.ArrayList;

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
        ArrayList<RGB> result = new ArrayList<>(values.length);
        for (RGB v : values) if (v != null)
            result.add(v);
        return result.toArray(new RGB[0]);
    }

    public void setRGBs(RGB[] value) {
        ((DartColorDialog) impl).rgbs = value;
    }

    @Override
    public int wireTypeId() {
        return 625;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
        w.key(623);
        w.write(getRGB());
        w.key(624);
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
