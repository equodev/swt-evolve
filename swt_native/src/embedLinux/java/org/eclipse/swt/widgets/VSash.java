package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public class VSash extends VControl {

    protected VSash() {
    }

    protected VSash(DartSash impl) {
        super(impl);
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

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VControl.allWireKeys_(), new int[] {});

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
