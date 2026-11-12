package org.eclipse.swt.browser;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.program.*;
import org.eclipse.swt.widgets.*;

public class VBrowser extends VComposite {

    protected VBrowser() {
    }

    protected VBrowser(DartBrowser impl) {
        super(impl);
    }

    public boolean getJavascriptEnabled() {
        return ((DartBrowser) impl).getJavascriptEnabled();
    }

    public void setJavascriptEnabled(boolean value) {
        ((DartBrowser) impl).javascriptEnabled = value;
    }

    public String getText() {
        return ((DartBrowser) impl).getText();
    }

    public void setText(String value) {
        ((DartBrowser) impl).text = value;
    }

    public String getUrl() {
        return ((DartBrowser) impl).getUrl();
    }

    public void setUrl(String value) {
        ((DartBrowser) impl).url = value;
    }

    public String[] getFunctionNames() {
        return dev.equo.swt.BrowserFunctionRegistry.namesFor(dev.equo.swt.FlutterBridge.id(impl)).toArray(new String[0]);
    }

    public static final int FUNCTION_NAMES = 6;

    public static final int JAVASCRIPT_ENABLED = 7;

    public static final int TEXT = 8;

    public static final int URL = 9;

    @Override
    public String wireName(int key) {
        switch(key) {
            case JAVASCRIPT_ENABLED:
                return "javascriptEnabled";
            case TEXT:
                return "text";
            case URL:
                return "url";
            case FUNCTION_NAMES:
                return "functionNames";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 144:
                return false;
            case 146:
                return false;
            case 7:
                w.write(getJavascriptEnabled());
                return true;
            case 8:
                w.write(getText());
                return true;
            case 9:
                w.write(getUrl());
                return true;
            case 6:
                w.write(getFunctionNames());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 144:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 146:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 7:
                {
                    boolean v_ = getJavascriptEnabled();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(7);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 8:
                {
                    String v_ = getText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(8);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 9:
                {
                    String v_ = getUrl();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(9);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 6:
                {
                    String[] v_ = getFunctionNames();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(6);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 7, 8, 9, 6 });

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
