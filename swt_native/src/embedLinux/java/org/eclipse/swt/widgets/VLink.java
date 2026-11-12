package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public class VLink extends VControl {

    protected VLink() {
    }

    protected VLink(DartLink impl) {
        super(impl);
    }

    public Color getLinkForeground() {
        return ((DartLink) impl).linkColor;
    }

    public void setLinkForeground(Color value) {
        ((DartLink) impl).linkColor = value;
    }

    public String getText() {
        return ((DartLink) impl).getText();
    }

    public void setText(String value) {
        ((DartLink) impl).text = value;
    }

    public static final int LINK_FOREGROUND = 368;

    public static final int TEXT = 369;

    @Override
    public String wireName(int key) {
        switch(key) {
            case LINK_FOREGROUND:
                return "linkForeground";
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
            case 368:
                w.write(getLinkForeground());
                return true;
            case 369:
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
            case 368:
                {
                    Color v_ = getLinkForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(368);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 369:
                {
                    String v_ = getText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(369);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VControl.allWireKeys_(), new int[] { 368, 369 });

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
