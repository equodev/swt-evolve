/**
 * ****************************************************************************
 *  Copyright (c) 2008, 2018 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package com.equo.chromium.swt;

import java.util.*;
import org.eclipse.swt.*;

/**
 * Dart/web-backed implementation of {@link BrowserFunction}. Mirror of
 * {@code org.eclipse.swt.browser.DartBrowserFunction} (which is generated); kept
 * in sync by hand because the {@code com.equo.chromium.swt} package is not run
 * through the generator. The only intentional difference is the absence of
 * {@code VBrowserFunction}/{@code getValue()} — this package has no value DTO for
 * BrowserFunction.
 *
 * <p>Instead of binding to a native browser, registers the callback with the
 * backing {@link EvolveBrowser} (which exposes it to the iframe page through a
 * same-origin XHR shim — see {@link EvolveBrowser#createFunction}).
 *
 * @see #dispose()
 * @see #function(Object[])
 *
 * @since 3.5
 */
public class DartBrowserFunction implements IBrowserFunction {

    Browser browser;

    String name;

    String functionString;

    int index;

    boolean isEvaluate, top;

    String token;

    String[] frameNames;

    /**
     * Constructs a new instance of this class, which will be invokable
     * by javascript running in the specified Browser.  The function will
     * be accessible in the top-level window and all child frames.  To
     * create a function with a reduced scope of accessibility use the
     * <code>BrowserFunction</code> constructor that accepts frame names
     * instead.
     *
     * @param browser the browser whose javascript can invoke this function
     * @param name the name that javascript will use to invoke this function
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the browser is null</li>
     *    <li>ERROR_NULL_ARGUMENT - if the name is null</li>
     * </ul>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the browser has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public DartBrowserFunction(Browser browser, String name, BrowserFunction api) {
        this(browser, name, true, null, true, api);
    }

    /**
     * Constructs a new instance of this class, which will be invokable
     * by javascript running in the specified Browser.  The accessibility
     * of the function to the top-level window and its child frames is
     * determined by the <code>top</code> and <code>frameNames</code>
     * arguments.
     *
     * @param browser the browser whose javascript can invoke this function
     * @param name the name that javascript will use to invoke this function
     * @param top <code>true</code> if the function should be accessible to the
     * top-level window and <code>false</code> otherwise
     * @param frameNames the names of the child frames that the function should
     * be accessible in
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the browser is null</li>
     *    <li>ERROR_NULL_ARGUMENT - if the name is null</li>
     * </ul>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the browser has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.8
     */
    public DartBrowserFunction(Browser browser, String name, boolean top, String[] frameNames, BrowserFunction api) {
        this(browser, name, top, frameNames, true, api);
    }

    DartBrowserFunction(Browser browser, String name, boolean top, String[] frameNames, boolean create, BrowserFunction api) {
        super();
        setApi(api);
        if (browser == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (name == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (browser.isDisposed())
            SWT.error(SWT.ERROR_WIDGET_DISPOSED);
        browser.checkWidget();
        this.browser = browser;
        this.name = name;
        this.top = top;
        this.frameNames = frameNames;
        Random random = new Random();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        StringBuilder buffer = new StringBuilder();
        for (byte b : bytes) {
            buffer.append(Integer.toHexString(b & 0xff));
        }
        token = buffer.toString();
        if (create)
            ((DartBrowser) browser.getImpl()).webBrowser.createFunction(this.getApi());
    }

    /**
     * Disposes of the resources associated with this BrowserFunction.
     * Applications must dispose of all BrowserFunctions that they create.
     * <p>
     * Note that disposing a Browser automatically disposes all
     * BrowserFunctions associated with it.
     * </p>
     */
    public void dispose() {
        dispose(true);
    }

    void dispose(boolean remove) {
        if (index < 0)
            return;
        if (remove)
            ((DartBrowser) browser.getImpl()).webBrowser.destroyFunction(this.getApi());
        browser = null;
        name = functionString = null;
        index = -1;
    }

    /**
     * Subclasses should override this method.  This method is invoked when
     * the receiver's function is called from javascript.  If all of the
     * arguments that are passed to the javascript function call are of
     * supported types then this method is invoked with the argument values
     * converted as follows:
     *
     * javascript null or undefined -&gt; <code>null</code>
     * javascript number -&gt; <code>java.lang.Double</code>
     * javascript string -&gt; <code>java.lang.String</code>
     * javascript boolean -&gt; <code>java.lang.Boolean</code>
     * javascript array whose elements are all of supported types -&gt; <code>java.lang.Object[]</code>
     *
     * If any of the javascript arguments are of unsupported types then the
     * function invocation will fail and this method will not be called.
     *
     * This method must return a value with one of these supported java types to
     * the javascript caller.  Note that <code>null</code> values are converted
     * to javascript's <code>null</code> value (not <code>undefined</code>), and
     * instances of any <code>java.lang.Number</code> subclass will be converted
     * to a javascript number.
     *
     * @param arguments the javascript arguments converted to java equivalents
     * @return the value to return to the javascript caller
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     *    <li>ERROR_FUNCTION_DISPOSED when the BrowserFunction has been disposed</li>
     * </ul>
     */
    public Object function(Object[] arguments) {
        if (index < 0)
            SWT.error(SWT.ERROR_FUNCTION_DISPOSED);
        browser.checkWidget();
        return null;
    }

    /**
     * Returns the Browser whose pages can invoke this BrowserFunction.
     *
     * @return the Browser associated with this BrowserFunction
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     *    <li>ERROR_FUNCTION_DISPOSED when the BrowserFunction has been disposed</li>
     * </ul>
     */
    public Browser getBrowser() {
        if (index < 0)
            SWT.error(SWT.ERROR_FUNCTION_DISPOSED);
        browser.checkWidget();
        return browser;
    }

    /**
     * Returns the name that javascript can use to invoke this BrowserFunction.
     *
     * @return the BrowserFunction's name
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     *    <li>ERROR_FUNCTION_DISPOSED when the BrowserFunction has been disposed</li>
     * </ul>
     */
    public String getName() {
        if (index < 0)
            SWT.error(SWT.ERROR_FUNCTION_DISPOSED);
        browser.checkWidget();
        return name;
    }

    /**
     * Returns <code>true</code> if this BrowserFunction has been disposed
     * and <code>false</code> otherwise.
     * <p>
     * This method gets the dispose state for the BrowserFunction.
     * When a BrowserFunction has been disposed it is an error to
     * invoke any of its methods.
     * </p><p>
     * Note that disposing a Browser automatically disposes all
     * BrowserFunctions associated with it.
     * </p>
     * @return <code>true</code> if this BrowserFunction has been disposed
     * and <code>false</code> otherwise
     */
    public boolean isDisposed() {
        return index < 0;
    }

    public Browser _browser() {
        return browser;
    }

    public String _name() {
        return name;
    }

    public String _functionString() {
        return functionString;
    }

    public int _index() {
        return index;
    }

    public boolean _isEvaluate() {
        return isEvaluate;
    }

    public boolean _top() {
        return top;
    }

    public String _token() {
        return token;
    }

    public String[] _frameNames() {
        return frameNames;
    }

    public BrowserFunction getApi() {
        if (api == null)
            api = BrowserFunction.createApi(this);
        return (BrowserFunction) api;
    }

    protected BrowserFunction api;

    public void setApi(BrowserFunction api) {
        this.api = api;
        if (api != null)
            api.impl = this;
    }
}
