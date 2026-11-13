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

    public static final int ALIGNMENT = 573;

    public static final int MOVEABLE = 574;

    public static final int RESIZABLE = 575;

    public static final int TOOL_TIP_TEXT = 576;

    public static final int WIDTH = 577;

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
            case 573:
                w.write((int) getAlignment());
                return true;
            case 574:
                w.write(getMoveable());
                return true;
            case 575:
                w.write(getResizable());
                return true;
            case 576:
                w.write(getToolTipText());
                return true;
            case 577:
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
            case 573:
                {
                    int v_ = getAlignment();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(573);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 574:
                {
                    boolean v_ = getMoveable();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(574);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 575:
                {
                    boolean v_ = getResizable();
                    w.key(575);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 576:
                {
                    String v_ = getToolTipText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(576);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 577:
                {
                    int v_ = getWidth();
                    w.key(577);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VItem.allWireKeys_(), new int[] { 573, 574, 575, 576, 577 });

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
            case 575:
            case 577:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
