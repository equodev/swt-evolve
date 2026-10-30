package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public class VToolTip extends VWidget {

    protected VToolTip() {
    }

    protected VToolTip(DartToolTip impl) {
        super(impl);
    }

    public boolean getAutoHide() {
        return ((DartToolTip) impl).getAutoHide();
    }

    public void setAutoHide(boolean value) {
        ((DartToolTip) impl).autoHide = value;
    }

    public Point getLocation() {
        return ((DartToolTip) impl).location;
    }

    public void setLocation(Point value) {
        ((DartToolTip) impl).location = value;
    }

    public String getMessage() {
        return ((DartToolTip) impl).getMessage();
    }

    public void setMessage(String value) {
        ((DartToolTip) impl).message = value;
    }

    public String getText() {
        return ((DartToolTip) impl).getText();
    }

    public void setText(String value) {
        ((DartToolTip) impl).text = value;
    }

    public boolean getVisible() {
        return ((DartToolTip) impl).getVisible();
    }

    public void setVisible(boolean value) {
        ((DartToolTip) impl).visible = value;
    }

    public static final int LOCATION = 164;

    public static final int MESSAGE = 165;

    public static final int TEXT = 166;

    public static final int VISIBLE = 167;

    @Override
    public String wireName(int key) {
        switch(key) {
            case LOCATION:
                return "location";
            case MESSAGE:
                return "message";
            case TEXT:
                return "text";
            case VISIBLE:
                return "visible";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 164:
                w.write(getLocation());
                return true;
            case 165:
                w.write(getMessage());
                return true;
            case 166:
                w.write(getText());
                return true;
            case 167:
                w.write(getVisible());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 164:
                {
                    Point v_ = getLocation();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(164);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 165:
                {
                    String v_ = getMessage();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(165);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 166:
                {
                    String v_ = getText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(166);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 167:
                {
                    boolean v_ = getVisible();
                    w.key(167);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VWidget.allWireKeys_(), new int[] { 164, 165, 166, 167 });

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
            case 167:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
