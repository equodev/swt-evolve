package org.eclipse.swt.custom;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class VBusyIndicator implements dev.equo.swt.WireObject {

    protected VBusyIndicator() {
    }

    protected VBusyIndicator(DartBusyIndicator impl) {
        this.impl = impl;
    }

    protected DartBusyIndicator impl;

    @Override
    public int wireTypeId() {
        return 7;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
    }
}
