package org.eclipse.swt.dnd;

public class VHTMLTransfer extends VByteArrayTransfer {

    protected VHTMLTransfer() {
    }

    protected VHTMLTransfer(DartHTMLTransfer impl) {
        super(impl);
    }

    @Override
    public int wireTypeId() {
        return 54;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
    }
}
