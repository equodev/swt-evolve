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
        ((DartTreeColumn) impl).movable = value;
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
        ((DartTreeColumn) impl).width = value;
    }

    public static final int ALIGNMENT = 650;

    public static final int MOVEABLE = 651;

    public static final int RESIZABLE = 652;

    public static final int TOOL_TIP_TEXT = 653;

    public static final int WIDTH = 654;

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
            case 676:
                return false;
            case 650:
                w.write((int) getAlignment());
                return true;
            case 651:
                w.write(getMoveable());
                return true;
            case 652:
                w.write(getResizable());
                return true;
            case 653:
                w.write(getToolTipText());
                return true;
            case 654:
                w.write((int) getWidth());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 676:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 650:
                {
                    int v_ = getAlignment();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(650);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 651:
                {
                    boolean v_ = getMoveable();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(651);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 652:
                {
                    boolean v_ = getResizable();
                    w.key(652);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 653:
                {
                    String v_ = getToolTipText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(653);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 654:
                {
                    int v_ = getWidth();
                    w.key(654);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VItem.allWireKeys_(), new int[] { 650, 651, 652, 653, 654 });

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
            case 652:
            case 654:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
