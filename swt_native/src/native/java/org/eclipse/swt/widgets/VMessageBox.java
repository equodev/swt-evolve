package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;

public class VMessageBox extends VDialog {

    protected VMessageBox() {
    }

    protected VMessageBox(DartMessageBox impl) {
        super(impl);
    }

    public String getMessage() {
        return ((DartMessageBox) impl).getMessage();
    }

    public void setMessage(String value) {
        ((DartMessageBox) impl).message = value;
    }

    public String[] getButtonLabels() {
        return ((DartMessageBox) impl).wireButtonLabels();
    }

    @Override
    public int wireTypeId() {
        return 70;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
        w.key(385);
        w.write(getMessage());
        w.key(384);
        w.write(getButtonLabels());
    }
}
