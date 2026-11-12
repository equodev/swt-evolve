package org.eclipse.swt.widgets;

import org.eclipse.swt.*;

public class VTray extends VWidget {

    protected VTray() {
    }

    protected VTray(DartTray impl) {
        super(impl);
    }

    public TrayItem[] getItems() {
        TrayItem[] values = ((DartTray) impl).items;
        if (values == null)
            return null;
        int kept = 0;
        for (TrayItem v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        TrayItem[] result = new TrayItem[kept];
        kept = 0;
        for (TrayItem v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setItems(TrayItem[] value) {
        ((DartTray) impl).items = value;
    }

    @Override
    public String wireName(int key) {
        switch(key) {
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 670:
                return false;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 670:
                return dev.equo.swt.WidgetWire.UNHANDLED;
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VWidget.allWireKeys_(), new int[] {});

    protected static int[] allWireKeys_() {
        return wireKeys_;
    }

    @Override
    public int[] wireKeys() {
        return wireKeys_;
    }

    @Override
    public boolean wireAlways(int key) {
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
