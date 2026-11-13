package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public class VMenuItem extends VItem {

    protected VMenuItem() {
    }

    protected VMenuItem(DartMenuItem impl) {
        super(impl);
    }

    public int getID() {
        return ((DartMenuItem) impl).getID();
    }

    public void setID(int value) {
        ((DartMenuItem) impl).ID = value;
    }

    public int getAccelerator() {
        return ((DartMenuItem) impl).getAccelerator();
    }

    public void setAccelerator(int value) {
        ((DartMenuItem) impl).accelerator = value;
    }

    public boolean getEnabled() {
        return ((DartMenuItem) impl).getEnabled();
    }

    public void setEnabled(boolean value) {
        ((DartMenuItem) impl).enabled = value;
    }

    public Menu getMenu() {
        Menu val = ((DartMenuItem) impl).menu;
        if (val != null && !(val.getImpl() instanceof DartMenu))
            return null;
        return val;
    }

    public void setMenu(Menu value) {
        ((DartMenuItem) impl).menu = value;
    }

    public boolean getSelection() {
        return ((DartMenuItem) impl).getSelection();
    }

    public void setSelection(boolean value) {
        ((DartMenuItem) impl).selection = value;
    }

    public String getToolTipText() {
        return ((DartMenuItem) impl).getToolTipText();
    }

    public void setToolTipText(String value) {
        ((DartMenuItem) impl).toolTipText = value;
    }

    public static final int ACCELERATOR = 381;

    public static final int ENABLED = 382;

    public static final int MENU = 383;

    public static final int SELECTION = 384;

    public static final int TOOL_TIP_TEXT = 385;

    @Override
    public String wireName(int key) {
        switch(key) {
            case ACCELERATOR:
                return "accelerator";
            case ENABLED:
                return "enabled";
            case MENU:
                return "menu";
            case SELECTION:
                return "selection";
            case TOOL_TIP_TEXT:
                return "toolTipText";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 381:
                w.write((int) getAccelerator());
                return true;
            case 382:
                w.write(getEnabled());
                return true;
            case 383:
                w.ref(getMenu());
                return true;
            case 384:
                w.write(getSelection());
                return true;
            case 385:
                w.write(getToolTipText());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 381:
                {
                    int v_ = getAccelerator();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(381);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 382:
                {
                    boolean v_ = getEnabled();
                    w.key(382);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 383:
                {
                    Menu v_ = getMenu();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(383);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 384:
                {
                    boolean v_ = getSelection();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(384);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 385:
                {
                    String v_ = getToolTipText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(385);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VItem.allWireKeys_(), new int[] { 381, 382, 383, 384, 385 });

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
            case 382:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
