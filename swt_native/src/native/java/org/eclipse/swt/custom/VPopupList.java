package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

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
        return 77;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        w.key(413);
        w.object((getFont() == null || getFont().isDisposed() ? null : ((DartFont) getFont().getImpl()).getValue()));
        w.key(414);
        w.write(getItems());
        w.key(415);
        w.write((int) getMinimumWidth());
        w.key(416);
        w.write(getSelection());
    }
}
