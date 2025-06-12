package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.gtk.*;
import org.eclipse.swt.internal.gtk3.*;
import org.eclipse.swt.internal.gtk4.*;

public interface IMessageBox extends IDialog {

    /**
     * Returns the dialog's message, or an empty string if it does not have one.
     * The message is a description of the purpose for which the dialog was opened.
     * This message will be visible in the dialog while it is open.
     *
     * @return the message
     */
    String getMessage();

    /**
     * Sets the dialog's message, which is a description of
     * the purpose for which it was opened. This message will be
     * visible on the dialog while it is open.
     *
     * @param string the message
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
     * </ul>
     */
    void setMessage(String string);

    /**
     * Makes the dialog visible and brings it to the front
     * of the display.
     *
     * @return the ID of the button that was selected to dismiss the
     *         message box (e.g. SWT.OK, SWT.CANCEL, etc.)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the dialog has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the dialog</li>
     * </ul>
     */
    int open();

    /**
     * Set custom text for <code>MessageDialog</code>'s buttons:
     *
     * @param labels a <code>Map</code> where a valid 'key' is from below listed
     *               styles:<ul>
     * <li>SWT#OK</li>
     * <li>SWT#CANCEL</li>
     * <li>SWT#YES</li>
     * <li>SWT#NO</li>
     * <li>SWT#ABORT</li>
     * <li>SWT#RETRY</li>
     * <li>SWT#IGNORE</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if labels is null</li>
     * </ul>
     * @since 3.121
     */
    void setButtonLabels(Map<Integer, String> labels);
}
