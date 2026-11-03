package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public class VTreeColumn extends VItem {

    protected VTreeColumn() {
    }

    protected VTreeColumn(DartTreeColumn impl) {
        super(impl);
    }

    public int getAlignment() {
        return ((DartTreeColumn) impl).getAlignment();
    }

    public void setAlignment(int value) {
        ((DartTreeColumn) impl).setAlignment(value);
    }

    public boolean getMoveable() {
        return ((DartTreeColumn) impl).getMoveable();
    }

    public void setMoveable(boolean value) {
        ((DartTreeColumn) impl).moveable = value;
    }

    public boolean getResizable() {
        return ((DartTreeColumn) impl).getResizable();
    }

    public void setResizable(boolean value) {
        ((DartTreeColumn) impl).resizable = value;
    }

    public String getToolTipText() {
        return ((DartTreeColumn) impl).getToolTipText();
    }

    public void setToolTipText(String value) {
        ((DartTreeColumn) impl).toolTipText = value;
    }

    public int getWidth() {
        return ((DartTreeColumn) impl).getWidth();
    }

    public void setWidth(int value) {
        ((DartTreeColumn) impl).lastWidth = value;
    }

    public static final int ALIGNMENT = 635;

    public static final int MOVEABLE = 636;

    public static final int RESIZABLE = 637;

    public static final int WIDTH = 638;

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
            case 635:
                w.write((int) getAlignment());
                return true;
            case 636:
                w.write(getMoveable());
                return true;
            case 637:
                w.write(getResizable());
                return true;
            case 638:
                w.write((int) getWidth());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 635:
                {
                    int v_ = getAlignment();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(635);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 636:
                {
                    boolean v_ = getMoveable();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(636);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 637:
                {
                    boolean v_ = getResizable();
                    w.key(637);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 638:
                {
                    int v_ = getWidth();
                    w.key(638);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VItem.allWireKeys_(), new int[] { 635, 636, 637, 638 });

    protected static int[] allWireKeys_() {
        return wireKeys_;
    }

    @Override
    public int[] wireKeys() {
        return wireKeys_;
    }

    @Override
    public boolean wireAlways(int key) {
        switch(key) {
            case 637:
            case 638:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
