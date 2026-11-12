package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public class VTrayItem extends VItem {

    protected VTrayItem() {
    }

    protected VTrayItem(DartTrayItem impl) {
        super(impl);
    }

    public Image getHighlightImage() {
        Image val = ((DartTrayItem) impl).highlightImage;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setHighlightImage(Image value) {
        ((DartTrayItem) impl).highlightImage = value;
    }

    public ToolTip getToolTip() {
        ToolTip val = ((DartTrayItem) impl).toolTip;
        if (val != null && !(val.getImpl() instanceof DartToolTip))
            return null;
        return val;
    }

    public void setToolTip(ToolTip value) {
        ((DartTrayItem) impl).toolTip = value;
    }

    public String getToolTipText() {
        return ((DartTrayItem) impl).getToolTipText();
    }

    public void setToolTipText(String value) {
        ((DartTrayItem) impl).toolTipText = value;
    }

    public boolean getVisible() {
        return ((DartTrayItem) impl).getVisible();
    }

    public void setVisible(boolean value) {
        ((DartTrayItem) impl).visible = value;
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
            case 670:
                return false;
            case 353:
                return false;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 670:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 353:
                return dev.equo.swt.WidgetWire.UNHANDLED;
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VItem.allWireKeys_(), new int[] {});

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
