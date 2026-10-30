package org.eclipse.swt.dnd;

import java.util.concurrent.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import java.util.ArrayList;

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
        ArrayList<String> result = new ArrayList<>(values.length);
        for (String v : values) if (v != null)
            result.add(v);
        return result.toArray(new String[0]);
    }

    public void setAvailableTypeNames(String[] value) {
        ((DartClipboard) impl).availableTypeNames = value;
    }

    public TransferData[] getAvailableTypes() {
        TransferData[] values = ((DartClipboard) impl).availableTypes;
        if (values == null)
            return null;
        ArrayList<TransferData> result = new ArrayList<>(values.length);
        for (TransferData v : values) if (v != null)
            result.add(v);
        return result.toArray(new TransferData[0]);
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
        return 667;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        w.key(666);
        w.write(getAvailableTypeNames());
    }
}
