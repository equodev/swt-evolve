package org.eclipse.swt.printing;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public class VPrinter extends VDevice {

    protected VPrinter() {
    }

    protected VPrinter(DartPrinter impl) {
        super(impl);
    }

    public PrinterData getPrinterData() {
        return ((DartPrinter) impl).data;
    }

    public void setPrinterData(PrinterData value) {
        ((DartPrinter) impl).data = value;
    }

    @Override
    public int wireTypeId() {
        return 665;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
    }
}
