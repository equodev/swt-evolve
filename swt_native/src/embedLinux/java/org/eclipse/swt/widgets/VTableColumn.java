package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public class VTableColumn extends VItem {

    protected VTableColumn() {
    }

    protected VTableColumn(DartTableColumn impl) {
        super(impl);
    }

    public int getAlignment() {
        return ((DartTableColumn) impl).getAlignment();
    }

    public void setAlignment(int value) {
        ((DartTableColumn) impl).setAlignment(value);
    }

    public boolean getMoveable() {
        return ((DartTableColumn) impl).getMoveable();
    }

    public void setMoveable(boolean value) {
        ((DartTableColumn) impl).moveable = value;
    }

    public boolean getResizable() {
        return ((DartTableColumn) impl).getResizable();
    }

    public void setResizable(boolean value) {
        ((DartTableColumn) impl).resizable = value;
    }

    public String getToolTipText() {
        return ((DartTableColumn) impl).getToolTipText();
    }

    public void setToolTipText(String value) {
        ((DartTableColumn) impl).toolTipText = value;
    }

    public int getWidth() {
        return ((DartTableColumn) impl).getWidth();
    }

    public void setWidth(int value) {
        ((DartTableColumn) impl).lastWidth = value;
    }

    public static final int ALIGNMENT = 212;

    public static final int MOVEABLE = 1380;

    public static final int RESIZABLE = 213;

    public static final int WIDTH = 214;

    @Override
    public String wireName(int key) {
        switch(key) {
            case ALIGNMENT:
                return "alignment";
            case MOVEABLE:
                return "moveable";
            case RESIZABLE:
                return "resizable";
            case WIDTH:
                return "width";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 212:
                w.write((int) getAlignment());
                return true;
            case 1380:
                w.write(getMoveable());
                return true;
            case 213:
                w.write(getResizable());
                return true;
            case 214:
                w.write((int) getWidth());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 212:
                {
                    int v_ = getAlignment();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(212);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1380:
                {
                    boolean v_ = getMoveable();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1380);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 213:
                {
                    boolean v_ = getResizable();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(213);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 214:
                {
                    int v_ = getWidth();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(214);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VItem.allWireKeys_(), new int[] { 212, 1380, 213, 214 });

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
