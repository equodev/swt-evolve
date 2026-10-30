package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import java.util.ArrayList;

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
        ArrayList<Rectangle> result = new ArrayList<>(values.length);
        for (Rectangle v : values) if (v != null)
            result.add(v);
        return result.toArray(new Rectangle[0]);
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
