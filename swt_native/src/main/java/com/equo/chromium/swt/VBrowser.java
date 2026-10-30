package com.equo.chromium.swt;

import org.eclipse.swt.widgets.VComposite;

/**
 * Hand-written rather than generated. Its impl schedules with {@code dirty()} rather than naming
 * what changed, so the value is always described whole.
 */
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

    /** Java to Dart only: {@link dev.equo.swt.BrowserFunctionRegistry} is the source of truth. */
    public String[] getFunctionNames() {
        return dev.equo.swt.BrowserFunctionRegistry
                .namesFor(dev.equo.swt.FlutterBridge.id(impl))
                .toArray(new String[0]);
    }

    // The numbers of the generated org.eclipse.swt.browser.VBrowser: the client reads both as one.
    public static final int FUNCTION_NAMES = org.eclipse.swt.browser.VBrowser.FUNCTION_NAMES;
    public static final int JAVASCRIPT_ENABLED = org.eclipse.swt.browser.VBrowser.JAVASCRIPT_ENABLED;
    public static final int TEXT = org.eclipse.swt.browser.VBrowser.TEXT;
    public static final int URL = org.eclipse.swt.browser.VBrowser.URL;

    @Override
    public String wireName(int key) {
        switch (key) {
            case JAVASCRIPT_ENABLED: return "javascriptEnabled";
            case TEXT: return "text";
            case URL: return "url";
            case FUNCTION_NAMES: return "functionNames";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch (key) {
            case JAVASCRIPT_ENABLED: w.write(getJavascriptEnabled()); return true;
            case TEXT: w.write(getText()); return true;
            case URL: w.write(getUrl()); return true;
            case FUNCTION_NAMES: w.write(getFunctionNames()); return true;
        }
        return super.writeWire(w, key);
    }

    private static int[] wireKeys_;

    @Override
    public int[] wireKeys() {
        if (wireKeys_ == null)
            wireKeys_ = dev.equo.swt.WidgetWire.concat(super.wireKeys(),
                    new int[] { JAVASCRIPT_ENABLED, TEXT, URL, FUNCTION_NAMES });
        return wireKeys_;
    }
}
