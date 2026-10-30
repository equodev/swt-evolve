package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public class VToolItem extends VItem {

    protected VToolItem() {
    }

    protected VToolItem(DartToolItem impl) {
        super(impl);
    }

    public Color getBackground() {
        return ((DartToolItem) impl).background;
    }

    public void setBackground(Color value) {
        ((DartToolItem) impl).background = value;
    }

    public Control getControl() {
        Control val = ((DartToolItem) impl).control;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setControl(Control value) {
        ((DartToolItem) impl).control = value;
    }

    public Image getDisabledImage() {
        Image val = ((DartToolItem) impl).disabledImage;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setDisabledImage(Image value) {
        ((DartToolItem) impl).disabledImage = value;
    }

    public boolean getEnabled() {
        return ((DartToolItem) impl).getEnabled();
    }

    public void setEnabled(boolean value) {
        ((DartToolItem) impl).enabled = value;
    }

    public Color getForeground() {
        return ((DartToolItem) impl).foreground;
    }

    public void setForeground(Color value) {
        ((DartToolItem) impl).foreground = value;
    }

    public Image getHotImage() {
        Image val = ((DartToolItem) impl).hotImage;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setHotImage(Image value) {
        ((DartToolItem) impl).hotImage = value;
    }

    public boolean getSelection() {
        return ((DartToolItem) impl).getSelection();
    }

    public void setSelection(boolean value) {
        ((DartToolItem) impl).selection = value;
    }

    public String getToolTipText() {
        return ((DartToolItem) impl).getToolTipText();
    }

    public void setToolTipText(String value) {
        ((DartToolItem) impl).toolTipText = value;
    }

    public int getWidth() {
        return ((DartToolItem) impl).getWidth();
    }

    public void setWidth(int value) {
        ((DartToolItem) impl).width = value;
    }

    public static final int BACKGROUND = 597;

    public static final int CONTROL = 598;

    public static final int DISABLED_IMAGE = 599;

    public static final int ENABLED = 600;

    public static final int FOREGROUND = 601;

    public static final int HOT_IMAGE = 602;

    public static final int SELECTION = 603;

    public static final int TOOL_TIP_TEXT = 604;

    public static final int WIDTH = 605;

    @Override
    public String wireName(int key) {
        switch(key) {
            case BACKGROUND:
                return "background";
            case CONTROL:
                return "control";
            case DISABLED_IMAGE:
                return "disabledImage";
            case ENABLED:
                return "enabled";
            case FOREGROUND:
                return "foreground";
            case HOT_IMAGE:
                return "hotImage";
            case SELECTION:
                return "selection";
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
            case 597:
                w.write(getBackground());
                return true;
            case 598:
                w.ref(getControl());
                return true;
            case 599:
                w.image(getDisabledImage());
                return true;
            case 600:
                w.write(getEnabled());
                return true;
            case 601:
                w.write(getForeground());
                return true;
            case 602:
                w.image(getHotImage());
                return true;
            case 603:
                w.write(getSelection());
                return true;
            case 604:
                w.write(getToolTipText());
                return true;
            case 605:
                w.write((int) getWidth());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 597:
                {
                    Color v_ = getBackground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(597);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 598:
                {
                    Control v_ = getControl();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(598);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 599:
                {
                    Image v_ = getDisabledImage();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(599);
                    w.image(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 600:
                {
                    boolean v_ = getEnabled();
                    w.key(600);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 601:
                {
                    Color v_ = getForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(601);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 602:
                {
                    Image v_ = getHotImage();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(602);
                    w.image(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 603:
                {
                    boolean v_ = getSelection();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(603);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 604:
                {
                    String v_ = getToolTipText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(604);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 605:
                {
                    int v_ = getWidth();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(605);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VItem.allWireKeys_(), new int[] { 597, 598, 599, 600, 601, 602, 603, 604, 605 });

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
            case 600:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
