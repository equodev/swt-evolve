package org.eclipse.swt.graphics;

import org.eclipse.swt.*;

public class VDevice implements dev.equo.swt.WireObject {

    protected VDevice() {
    }

    protected VDevice(IDevice impl) {
        this.impl = impl;
    }

    protected IDevice impl;

    public DeviceData getDeviceData() {
        return ((DartDevice) impl).deviceData;
    }

    public void setDeviceData(DeviceData value) {
        ((DartDevice) impl).deviceData = value;
    }

    public FontData[] getFontList() {
        FontData[] values = ((DartDevice) impl).fontList;
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
        ((DartDevice) impl).fontList = value;
    }

    public boolean getTracking() {
        return ((DartDevice) impl).tracking;
    }

    public void setTracking(boolean value) {
        ((DartDevice) impl).tracking = value;
    }

    public boolean getWarnings() {
        return ((DartDevice) impl).getWarnings();
    }

    public void setWarnings(boolean value) {
        ((DartDevice) impl).warnings = value;
    }

    @Override
    public int wireTypeId() {
        return 28;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        w.key(174);
        {
            FontData[] a_ = getFontList();
            if (a_ == null)
                w.listSize(-1);
            else {
                w.listSize(a_.length);
                for (int i_ = 0; i_ < a_.length; i_++) {
                    FontData e_ = a_[i_];
                    if (e_ == null) {
                        i_ += w.nulls(a_, i_) - 1;
                        continue;
                    }
                    w.object((e_ == null ? null : ((DartFontData) e_.getImpl()).getValue()));
                }
            }
        }
        w.key(175);
        w.write(getTracking());
        w.key(176);
        w.write(getWarnings());
    }
}
