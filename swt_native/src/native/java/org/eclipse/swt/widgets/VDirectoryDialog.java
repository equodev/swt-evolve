package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;

public class VDirectoryDialog extends VDialog {

    protected VDirectoryDialog() {
    }

    protected VDirectoryDialog(DartDirectoryDialog impl) {
        super(impl);
    }

    public String getFilterPath() {
        return ((DartDirectoryDialog) impl).getFilterPath();
    }

    public void setFilterPath(String value) {
        ((DartDirectoryDialog) impl).filterPath = value;
    }

    public String getMessage() {
        return ((DartDirectoryDialog) impl).getMessage();
    }

    public void setMessage(String value) {
        ((DartDirectoryDialog) impl).message = value;
    }

    @Override
    public int wireTypeId() {
        return 30;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
        w.key(181);
        w.write(getFilterPath());
        w.key(182);
        w.write(getMessage());
    }
}
