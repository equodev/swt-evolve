package org.eclipse.swt.dnd;

public class VRTFTransfer extends VByteArrayTransfer {

    protected VRTFTransfer() {
    }

    protected VRTFTransfer(DartRTFTransfer impl) {
        super(impl);
    }

    @Override
    public int wireTypeId() {
        return 82;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
    }
}
