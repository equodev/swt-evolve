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
        ((DartTableColumn) impl).width = value;
    }

    public static final int ALIGNMENT = 558;

    public static final int MOVEABLE = 559;

    public static final int RESIZABLE = 560;

    public static final int WIDTH = 561;

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
            case 558:
                w.write((int) getAlignment());
                return true;
            case 559:
                w.write(getMoveable());
                return true;
            case 560:
                w.write(getResizable());
                return true;
            case 561:
                w.write((int) getWidth());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 558:
                {
                    int v_ = getAlignment();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(558);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 559:
                {
                    boolean v_ = getMoveable();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(559);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 560:
                {
                    boolean v_ = getResizable();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(560);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 561:
                {
                    int v_ = getWidth();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(561);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VItem.allWireKeys_(), new int[] { 558, 559, 560, 561 });

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
