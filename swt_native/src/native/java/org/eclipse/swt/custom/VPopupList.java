package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import java.util.ArrayList;

public class VPopupList implements dev.equo.swt.WireObject {

    protected VPopupList() {
    }

    protected VPopupList(DartPopupList impl) {
        this.impl = impl;
    }

    protected DartPopupList impl;

    public Font getFont() {
        Font val = ((DartPopupList) impl).font;
        if (val != null && !(val.getImpl() instanceof DartFont))
            return null;
        return val;
    }

    public void setFont(Font value) {
        ((DartPopupList) impl).font = value;
    }

    public String[] getItems() {
        String[] values = ((DartPopupList) impl).items;
        if (values == null)
            return null;
        ArrayList<String> result = new ArrayList<>(values.length);
        for (String v : values) if (v != null)
            result.add(v);
        return result.toArray(new String[0]);
    }

    public void setItems(String[] value) {
        ((DartPopupList) impl).items = value;
    }

    public int getMinimumWidth() {
        return ((DartPopupList) impl).getMinimumWidth();
    }

    public void setMinimumWidth(int value) {
        ((DartPopupList) impl).minimumWidth = value;
    }

    public String getSelection() {
        return ((DartPopupList) impl).selection;
    }

    public void setSelection(String value) {
        ((DartPopupList) impl).selection = value;
    }

    @Override
    public int wireTypeId() {
        return 657;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        w.key(653);
        w.object((getFont() == null || getFont().isDisposed() ? null : ((DartFont) getFont().getImpl()).getValue()));
        w.key(654);
        w.write(getItems());
        w.key(655);
        w.write((int) getMinimumWidth());
        w.key(656);
        w.write(getSelection());
    }
}
