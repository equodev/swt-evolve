package org.eclipse.swt.graphics;

public class VFontMetrics implements dev.equo.swt.WireObject {

    protected VFontMetrics() {
    }

    protected VFontMetrics(DartFontMetrics impl) {
        this.impl = impl;
    }

    protected DartFontMetrics impl;

    @Override
    public int wireTypeId() {
        return 247;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
    }
}
