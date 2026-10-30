package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import java.util.ArrayList;

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
        ArrayList<TrayItem> result = new ArrayList<>(values.length);
        for (TrayItem v : values) if (v != null)
            result.add(v);
        return result.toArray(new TrayItem[0]);
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
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
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
