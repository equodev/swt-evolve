package org.eclipse.swt.printing;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

public class VPrintDialog extends VDialog {

    protected VPrintDialog() {
    }

    protected VPrintDialog(DartPrintDialog impl) {
        super(impl);
    }

    public int getEndPage() {
        return ((DartPrintDialog) impl).getEndPage();
    }

    public void setEndPage(int value) {
        ((DartPrintDialog) impl).endPage = value;
    }

    public boolean getPrintToFile() {
        return ((DartPrintDialog) impl).getPrintToFile();
    }

    public void setPrintToFile(boolean value) {
        ((DartPrintDialog) impl).printToFile = value;
    }

    public PrinterData getPrinterData() {
        return ((DartPrintDialog) impl).printerData;
    }

    public void setPrinterData(PrinterData value) {
        ((DartPrintDialog) impl).printerData = value;
    }

    public int getScope() {
        return ((DartPrintDialog) impl).getScope();
    }

    public void setScope(int value) {
        ((DartPrintDialog) impl).scope = value;
    }

    public int getStartPage() {
        return ((DartPrintDialog) impl).getStartPage();
    }

    public void setStartPage(int value) {
        ((DartPrintDialog) impl).startPage = value;
    }

    @Override
    public int wireTypeId() {
        return 77;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
        w.key(408);
        w.write((int) getEndPage());
        w.key(409);
        w.write(getPrintToFile());
        w.key(410);
        w.write((int) getScope());
        w.key(411);
        w.write((int) getStartPage());
    }
}
