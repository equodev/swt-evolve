package org.eclipse.swt.dnd;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class VImageTransfer extends VByteArrayTransfer {

    protected VImageTransfer() {
    }

    protected VImageTransfer(DartImageTransfer impl) {
        super(impl);
    }

    @Override
    public int wireTypeId() {
        return 615;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
    }
}
