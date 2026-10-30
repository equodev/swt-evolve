package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import java.util.ArrayList;

public class VIME extends VWidget {

    protected VIME() {
    }

    protected VIME(DartIME impl) {
        super(impl);
    }

    public int getCompositionOffset() {
        return ((DartIME) impl).getCompositionOffset();
    }

    public void setCompositionOffset(int value) {
        ((DartIME) impl).startOffset = value;
    }

    public int[] getRanges() {
        return ((DartIME) impl).ranges;
    }

    public void setRanges(int[] value) {
        ((DartIME) impl).ranges = value;
    }

    public TextStyle[] getStyles() {
        TextStyle[] values = ((DartIME) impl).styles;
        if (values == null)
            return null;
        ArrayList<TextStyle> result = new ArrayList<>(values.length);
        for (TextStyle v : values) if (v != null)
            result.add(v);
        return result.toArray(new TextStyle[0]);
    }

    public void setStyles(TextStyle[] value) {
        ((DartIME) impl).styles = value;
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
