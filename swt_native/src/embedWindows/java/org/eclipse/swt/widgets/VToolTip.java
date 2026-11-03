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

    public static final int LOCATION = 608;

    public static final int MESSAGE = 609;

    public static final int TEXT = 610;

    public static final int VISIBLE = 611;

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
            case 608:
                w.write(getLocation());
                return true;
            case 609:
                w.write(getMessage());
                return true;
            case 610:
                w.write(getText());
                return true;
            case 611:
                w.write(getVisible());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 608:
                {
                    Point v_ = getLocation();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(608);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 609:
                {
                    String v_ = getMessage();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(609);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 610:
                {
                    String v_ = getText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(610);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 611:
                {
                    boolean v_ = getVisible();
                    w.key(611);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VWidget.allWireKeys_(), new int[] { 608, 609, 610, 611 });

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
            case 611:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
