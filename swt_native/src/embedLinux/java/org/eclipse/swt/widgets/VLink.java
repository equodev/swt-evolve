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

    public static final int LINK_FOREGROUND = 363;

    public static final int TEXT = 364;

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
            case 363:
                w.write(getLinkForeground());
                return true;
            case 364:
                w.write(getText());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 363:
                {
                    Color v_ = getLinkForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(363);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 364:
                {
                    String v_ = getText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(364);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VControl.allWireKeys_(), new int[] { 363, 364 });

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
