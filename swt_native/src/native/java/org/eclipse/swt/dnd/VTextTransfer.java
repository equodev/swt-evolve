package org.eclipse.swt.dnd;

public class VTextTransfer extends VByteArrayTransfer {

    protected VTextTransfer() {
    }

    protected VTextTransfer(DartTextTransfer impl) {
        super(impl);
    }

    @Override
    public int wireTypeId() {
        return 662;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
    }
}
