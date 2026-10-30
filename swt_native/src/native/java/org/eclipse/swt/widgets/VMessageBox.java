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
        return 69;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
        w.key(375);
        w.write(getMessage());
        w.key(374);
        w.write(getButtonLabels());
    }
}
