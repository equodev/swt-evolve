package org.eclipse.swt.accessibility;

import java.net.*;
import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class VAccessible implements dev.equo.swt.WireObject {

    protected VAccessible() {
    }

    protected VAccessible(DartAccessible impl) {
        this.impl = impl;
    }

    protected DartAccessible impl;

    public Control getControl() {
        Control val = ((DartAccessible) impl).control;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setControl(Control value) {
        ((DartAccessible) impl).control = value;
    }

    public int getFocus() {
        return ((DartAccessible) impl).focus;
    }

    public void setFocus(int value) {
        ((DartAccessible) impl).focus = value;
    }

    @Override
    public int wireTypeId() {
        return 170;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        w.key(168);
        w.ref(getControl());
        w.key(169);
        w.write((int) getFocus());
    }
}
