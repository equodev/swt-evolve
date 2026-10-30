package org.eclipse.swt.widgets;

import java.util.*;
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
        return ((DartLink) impl).getLinkForeground();
    }

    public void setLinkForeground(Color value) {
        ((DartLink) impl).setLinkForeground(value);
    }

    public String getText() {
        return ((DartLink) impl).getText();
    }

    public void setText(String value) {
        ((DartLink) impl).text = value;
    }

    public static final int LINK_FOREGROUND = 402;

    public static final int TEXT = 403;

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
            case 402:
                w.write(getLinkForeground());
                return true;
            case 403:
                w.write(getText());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 402:
                {
                    Color v_ = getLinkForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(402);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 403:
                {
                    String v_ = getText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(403);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VControl.allWireKeys_(), new int[] { 402, 403 });

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
