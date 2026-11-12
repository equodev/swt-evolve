package org.eclipse.swt.program;

import java.io.*;
import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class VProgram implements dev.equo.swt.WireObject {

    protected VProgram() {
    }

    protected VProgram(DartProgram impl) {
        this.impl = impl;
    }

    protected DartProgram impl;

    @Override
    public int wireTypeId() {
        return 80;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
    }
}
