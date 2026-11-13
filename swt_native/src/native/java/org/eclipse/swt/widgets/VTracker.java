package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public class VTracker extends VWidget {

    protected VTracker() {
    }

    protected VTracker(DartTracker impl) {
        super(impl);
    }

    public Cursor getCursor() {
        Cursor val = ((DartTracker) impl).clientCursor;
        if (val != null && !(val.getImpl() instanceof DartCursor))
            return null;
        return val;
    }

    public void setCursor(Cursor value) {
        ((DartTracker) impl).clientCursor = value;
    }

    public Rectangle[] getRectangles() {
        Rectangle[] values = ((DartTracker) impl).rectangles;
        if (values == null)
            return null;
        int kept = 0;
        for (Rectangle v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        Rectangle[] result = new Rectangle[kept];
        kept = 0;
        for (Rectangle v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setRectangles(Rectangle[] value) {
        ((DartTracker) impl).rectangles = value;
    }

    public boolean getStippled() {
        return ((DartTracker) impl).getStippled();
    }

    public void setStippled(boolean value) {
        ((DartTracker) impl).stippled = value;
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
            case 676:
                return false;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 676:
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
