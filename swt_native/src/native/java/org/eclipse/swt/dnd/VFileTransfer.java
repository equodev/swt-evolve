package org.eclipse.swt.dnd;

public class VFileTransfer extends VByteArrayTransfer {

    protected VFileTransfer() {
    }

    protected VFileTransfer(DartFileTransfer impl) {
        super(impl);
    }

    @Override
    public int wireTypeId() {
        return 39;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
    }
}
