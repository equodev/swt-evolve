package org.eclipse.swt.dnd;

public class VTransferData implements dev.equo.swt.WireObject {

    protected VTransferData() {
    }

    protected VTransferData(DartTransferData impl) {
        this.impl = impl;
    }

    protected DartTransferData impl;

    @Override
    public int wireTypeId() {
        return 122;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
    }
}
