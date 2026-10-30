package org.eclipse.swt.dnd;

public class VURLTransfer extends VByteArrayTransfer {

    protected VURLTransfer() {
    }

    protected VURLTransfer(DartURLTransfer impl) {
        super(impl);
    }

    @Override
    public int wireTypeId() {
        return 638;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
    }
}
