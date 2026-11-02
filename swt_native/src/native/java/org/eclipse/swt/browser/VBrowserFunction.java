package org.eclipse.swt.browser;

import java.util.*;
import org.eclipse.swt.*;

public class VBrowserFunction implements dev.equo.swt.WireObject {

    protected VBrowserFunction() {
    }

    protected VBrowserFunction(DartBrowserFunction impl) {
        this.impl = impl;
    }

    protected DartBrowserFunction impl;

    public Browser getBrowser() {
        Browser val = ((DartBrowserFunction) impl).browser;
        if (val != null && !(val.getImpl() instanceof DartBrowser))
            return null;
        return val;
    }

    public void setBrowser(Browser value) {
        ((DartBrowserFunction) impl).browser = value;
    }

    public String getName() {
        return ((DartBrowserFunction) impl).getName();
    }

    public void setName(String value) {
        ((DartBrowserFunction) impl).name = value;
    }

    @Override
    public int wireTypeId() {
        return 6;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        w.key(10);
        w.ref(getBrowser());
        w.key(11);
        w.write(getName());
    }
}
