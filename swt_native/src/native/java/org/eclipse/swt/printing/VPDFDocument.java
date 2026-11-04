package org.eclipse.swt.printing;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public class VPDFDocument extends VDevice {

    protected VPDFDocument() {
    }

    protected VPDFDocument(DartPDFDocument impl) {
        super(impl);
    }

    public double getHeight() {
        return ((DartPDFDocument) impl).getHeight();
    }

    public void setHeight(double value) {
        ((DartPDFDocument) impl).heightInPoints = value;
    }

    public double getWidth() {
        return ((DartPDFDocument) impl).getWidth();
    }

    public void setWidth(double value) {
        ((DartPDFDocument) impl).widthInPoints = value;
    }

    @Override
    public int wireTypeId() {
        return 71;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
        w.key(390);
        w.write(getHeight());
        w.key(391);
        w.write(getWidth());
    }
}
