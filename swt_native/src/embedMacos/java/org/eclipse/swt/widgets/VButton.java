package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public class VButton extends VControl {

    protected VButton() {
    }

    protected VButton(DartButton impl) {
        super(impl);
    }

    public int getAlignment() {
        return ((DartButton) impl).getAlignment();
    }

    public void setAlignment(int value) {
        ((DartButton) impl)._setAlignment(value);
    }

    public boolean getGrayed() {
        return ((DartButton) impl).getGrayed();
    }

    public void setGrayed(boolean value) {
        ((DartButton) impl).grayed = value;
    }

    public Image getImage() {
        Image val = ((DartButton) impl).image;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setImage(Image value) {
        ((DartButton) impl).image = value;
    }

    public boolean getPrimary() {
        Button button = ((DartButton) impl).getApi();
        Composite parent = button.getImpl()._parent();
        while (parent != null && !(parent instanceof Shell)) {
            parent = parent.getImpl()._parent();
        }
        if (parent == null) {
            return true;
        }
        Shell shell = (Shell) parent;
        Button defaultButton = ((SwtDecorations) shell.getImpl()).defaultButton;
        return defaultButton != null && defaultButton == button;
    }

    public void setPrimary(boolean value) {
    }

    public boolean getSelection() {
        return ((DartButton) impl).getSelection();
    }

    public void setSelection(boolean value) {
        ((DartButton) impl).selection = value;
    }

    public String getText() {
        return ((DartButton) impl).getText();
    }

    public void setText(String value) {
        ((DartButton) impl).text = value;
    }

    public static final int ALIGNMENT = 12;

    public static final int GRAYED = 13;

    public static final int IMAGE = 14;

    public static final int PRIMARY = 15;

    public static final int SELECTION = 16;

    public static final int TEXT = 17;

    @Override
    public String wireName(int key) {
        switch(key) {
            case ALIGNMENT:
                return "alignment";
            case GRAYED:
                return "grayed";
            case IMAGE:
                return "image";
            case PRIMARY:
                return "primary";
            case SELECTION:
                return "selection";
            case TEXT:
                return "text";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 146:
                return false;
            case 147:
                return false;
            case 150:
                return false;
            case 12:
                w.write((int) getAlignment());
                return true;
            case 13:
                w.write(getGrayed());
                return true;
            case 14:
                w.image(getImage());
                return true;
            case 15:
                w.write(getPrimary());
                return true;
            case 16:
                w.write(getSelection());
                return true;
            case 17:
                w.write(getText());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 146:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 147:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 150:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 12:
                {
                    int v_ = getAlignment();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(12);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 13:
                {
                    boolean v_ = getGrayed();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(13);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 14:
                {
                    Image v_ = getImage();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(14);
                    w.image(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 15:
                {
                    boolean v_ = getPrimary();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(15);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 16:
                {
                    boolean v_ = getSelection();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(16);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 17:
                {
                    String v_ = getText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(17);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VControl.allWireKeys_(), new int[] { 12, 13, 14, 15, 16, 17 });

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
