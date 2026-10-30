package org.eclipse.swt.graphics;

import org.eclipse.swt.*;
import java.util.ArrayList;

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
        ArrayList<FontData> result = new ArrayList<>(values.length);
        for (FontData v : values) if (v != null)
            result.add(v);
        return result.toArray(new FontData[0]);
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
        return 632;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        w.key(629);
        {
            FontData[] a_ = getFontList();
            if (a_ == null)
                w.listSize(-1);
            else {
                w.listSize(a_.length);
                for (FontData e_ : a_) w.object((e_ == null ? null : ((DartFontData) e_.getImpl()).getValue()));
            }
        }
        w.key(630);
        w.write(getTracking());
        w.key(631);
        w.write(getWarnings());
    }
}
