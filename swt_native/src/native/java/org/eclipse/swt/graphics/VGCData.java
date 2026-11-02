package org.eclipse.swt.graphics;

import org.eclipse.swt.*;

public class VGCData implements dev.equo.swt.WireObject {

    protected VGCData() {
    }

    protected VGCData(DartGCData impl) {
        this.impl = impl;
    }

    protected DartGCData impl;

    @Override
    public int wireTypeId() {
        return 48;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
    }
}
