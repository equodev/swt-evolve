package org.eclipse.swt.dnd;

public class VByteArrayTransfer extends VTransfer {

    protected VByteArrayTransfer() {
    }

    protected VByteArrayTransfer(IByteArrayTransfer impl) {
        super(impl);
    }

    @Override
    public int wireTypeId() {
        return 8;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
    }
}
