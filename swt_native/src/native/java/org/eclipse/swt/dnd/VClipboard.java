package org.eclipse.swt.dnd;

import java.util.concurrent.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

public class VClipboard implements dev.equo.swt.WireObject {

    protected VClipboard() {
    }

    protected VClipboard(DartClipboard impl) {
        this.impl = impl;
    }

    protected DartClipboard impl;

    public String[] getAvailableTypeNames() {
        String[] values = ((DartClipboard) impl).availableTypeNames;
        if (values == null)
            return null;
        int kept = 0;
        for (String v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        String[] result = new String[kept];
        kept = 0;
        for (String v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setAvailableTypeNames(String[] value) {
        ((DartClipboard) impl).availableTypeNames = value;
    }

    public TransferData[] getAvailableTypes() {
        TransferData[] values = ((DartClipboard) impl).availableTypes;
        if (values == null)
            return null;
        int kept = 0;
        for (TransferData v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        TransferData[] result = new TransferData[kept];
        kept = 0;
        for (TransferData v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setAvailableTypes(TransferData[] value) {
        ((DartClipboard) impl).availableTypes = value;
    }

    public Object getContents() {
        return ((DartClipboard) impl).contents;
    }

    public void setContents(Object value) {
        ((DartClipboard) impl).contents = value;
    }

    @Override
    public int wireTypeId() {
        return 17;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        w.key(75);
        w.write(getAvailableTypeNames());
    }
}
