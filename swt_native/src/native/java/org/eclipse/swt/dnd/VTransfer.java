package org.eclipse.swt.dnd;

public class VTransfer implements dev.equo.swt.WireObject {

    protected VTransfer() {
    }

    protected VTransfer(ITransfer impl) {
        this.impl = impl;
    }

    protected ITransfer impl;

    @Override
    public int wireTypeId() {
        return 121;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
    }
}
