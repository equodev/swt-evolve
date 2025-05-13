package org.eclipse.swt.browser;

import java.util.*;
import org.eclipse.swt.*;

public interface IBrowserFunction {

    /**
     * Disposes of the resources associated with this BrowserFunction.
     * Applications must dispose of all BrowserFunctions that they create.
     * <p>
     * Note that disposing a Browser automatically disposes all
     * BrowserFunctions associated with it.
     * </p>
     */
    void dispose();

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
    Object function(Object[] arguments);

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
    IBrowser getBrowser();

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
    String getName();

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
    boolean isDisposed();

    BrowserFunction getApi();

    void setApi(BrowserFunction api);
}
