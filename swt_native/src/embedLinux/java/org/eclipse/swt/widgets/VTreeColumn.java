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

    public static final int ALIGNMENT = 647;

    public static final int MOVEABLE = 648;

    public static final int RESIZABLE = 649;

    public static final int TOOL_TIP_TEXT = 650;

    public static final int WIDTH = 651;

    @Override
    public String wireName(int key) {
        switch(key) {
            case ALIGNMENT:
                return "alignment";
            case MOVEABLE:
                return "moveable";
            case RESIZABLE:
                return "resizable";
            case TOOL_TIP_TEXT:
                return "toolTipText";
            case WIDTH:
                return "width";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 670:
                return false;
            case 647:
                w.write((int) getAlignment());
                return true;
            case 648:
                w.write(getMoveable());
                return true;
            case 649:
                w.write(getResizable());
                return true;
            case 650:
                w.write(getToolTipText());
                return true;
            case 651:
                w.write((int) getWidth());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 670:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 647:
                {
                    int v_ = getAlignment();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(647);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 648:
                {
                    boolean v_ = getMoveable();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(648);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 649:
                {
                    boolean v_ = getResizable();
                    w.key(649);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 650:
                {
                    String v_ = getToolTipText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(650);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 651:
                {
                    int v_ = getWidth();
                    w.key(651);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VItem.allWireKeys_(), new int[] { 647, 648, 649, 650, 651 });

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
            case 649:
            case 651:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
