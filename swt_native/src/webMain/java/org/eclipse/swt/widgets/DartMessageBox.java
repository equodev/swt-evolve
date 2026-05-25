/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2022 IBM Corporation and others.
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
package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;
import dev.equo.swt.*;

/**
 * Instances of this class are used to inform or warn the user.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>ICON_ERROR, ICON_INFORMATION, ICON_QUESTION, ICON_WARNING, ICON_WORKING</dd>
 * <dd>OK, OK | CANCEL</dd>
 * <dd>YES | NO, YES | NO | CANCEL</dd>
 * <dd>RETRY | CANCEL</dd>
 * <dd>ABORT | RETRY | IGNORE</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles ICON_ERROR, ICON_INFORMATION, ICON_QUESTION,
 * ICON_WARNING and ICON_WORKING may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample, Dialog tab</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartMessageBox extends DartDialog implements IMessageBox {

    String message = "";

    int userResponse;

    Map<Integer, String> labels;

    /**
     * Constructs a new instance of this class given only its parent.
     *
     * @param parent a shell which will be the parent of the new instance
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     */
    public DartMessageBox(Shell parent, MessageBox api) {
        this(parent, SWT.OK | SWT.ICON_INFORMATION | SWT.APPLICATION_MODAL, api);
    }

    /**
     * Constructs a new instance of this class given its parent
     * and a style value describing its behavior and appearance.
     * <p>
     * The style value is either one of the style constants defined in
     * class <code>SWT</code> which is applicable to instances of this
     * class, or must be built by <em>bitwise OR</em>'ing together
     * (that is, using the <code>int</code> "|" operator) two or more
     * of those <code>SWT</code> style constants. The class description
     * lists the style constants that are applicable to the class.
     * Style bits are also inherited from superclasses.
     *
     * @param parent a shell which will be the parent of the new instance
     * @param style the style of dialog to construct
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see SWT#ICON_ERROR
     * @see SWT#ICON_INFORMATION
     * @see SWT#ICON_QUESTION
     * @see SWT#ICON_WARNING
     * @see SWT#ICON_WORKING
     * @see SWT#OK
     * @see SWT#CANCEL
     * @see SWT#YES
     * @see SWT#NO
     * @see SWT#ABORT
     * @see SWT#RETRY
     * @see SWT#IGNORE
     */
    public DartMessageBox(Shell parent, int style, MessageBox api) {
        super(parent, checkStyle(parent, checkStyle(style)), api);
        if (DartDisplay.getSheetEnabled()) {
            if (parent != null && (style & SWT.SHEET) != 0)
                this.style |= SWT.SHEET;
        }
        checkSubclass();
    }

    static int checkStyle(int style) {
        int mask = (SWT.YES | SWT.NO | SWT.OK | SWT.CANCEL | SWT.ABORT | SWT.RETRY | SWT.IGNORE);
        int bits = style & mask;
        if (bits == SWT.OK || bits == SWT.CANCEL || bits == (SWT.OK | SWT.CANCEL))
            return style;
        if (bits == SWT.YES || bits == SWT.NO || bits == (SWT.YES | SWT.NO) || bits == (SWT.YES | SWT.NO | SWT.CANCEL))
            return style;
        if (bits == (SWT.RETRY | SWT.CANCEL) || bits == (SWT.ABORT | SWT.RETRY | SWT.IGNORE))
            return style;
        style = (style & ~mask) | SWT.OK;
        return style;
    }

    private int getBits() {
        int mask = (SWT.YES | SWT.NO | SWT.OK | SWT.CANCEL | SWT.ABORT | SWT.RETRY | SWT.IGNORE);
        int bits = style & mask;
        return bits;
    }

    /**
     * Returns the dialog's message, or an empty string if it does not have one.
     * The message is a description of the purpose for which the dialog was opened.
     * This message will be visible in the dialog while it is open.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

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
    public int open() {
        if ((style & SWT.ICON_ERROR) != 0) {
        }
        if (((style & SWT.ICON_INFORMATION) != 0) || ((style & SWT.ICON_WORKING) != 0) || ((style & SWT.ICON_QUESTION) != 0)) {
        }
        if ((style & SWT.ICON_WARNING) != 0) {
        }
        int bits = getBits();
        switch(bits) {
            case SWT.OK:
                break;
            case SWT.CANCEL:
                break;
            case SWT.OK | SWT.CANCEL:
                break;
            case SWT.YES:
                break;
            case SWT.NO:
                break;
            case SWT.YES | SWT.NO:
                //			no.setKeyEquivalent(NSString.stringWith("\033"));
                break;
            case SWT.YES | SWT.NO | SWT.CANCEL:
                break;
            case SWT.RETRY | SWT.CANCEL:
                break;
            case SWT.ABORT | SWT.RETRY | SWT.IGNORE:
                break;
        }
        long jniRef = 0;
        Display display = parent != null ? parent.getDisplay() : DartDisplay.getCurrent();
        if ((style & SWT.SHEET) != 0) {
            if (jniRef == 0)
                error(SWT.ERROR_NO_HANDLES);
            if ((style & SWT.APPLICATION_MODAL) != 0) {
            } else {
            }
        } else {
        }
        ((DartDisplay) display.getImpl()).setModalDialog(null);
        releaseHandler();
        return userResponse;
    }

    long _completionHandler(long result) {
        userResponse = handleResponse(getBits(), (int) result);
        return result;
    }

    int handleResponse(int bits, int response) {
        switch(bits) {
            case SWT.OK:
                break;
            case SWT.CANCEL:
                break;
            case SWT.OK | SWT.CANCEL:
                break;
            case SWT.YES:
                break;
            case SWT.NO:
                break;
            case SWT.YES | SWT.NO:
                break;
            case SWT.YES | SWT.NO | SWT.CANCEL:
                break;
            case SWT.RETRY | SWT.CANCEL:
                break;
            case SWT.ABORT | SWT.RETRY | SWT.IGNORE:
                break;
        }
        return SWT.CANCEL;
    }

    void releaseHandler() {
    }

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
    public void setMessage(String string) {
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        message = string;
    }

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
    public void setButtonLabels(Map<Integer, String> labels) {
        if (labels == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        this.labels = labels;
    }

    public String _message() {
        return message;
    }

    public int _userResponse() {
        return userResponse;
    }

    public Map<Integer, String> _labels() {
        return labels;
    }

    public MessageBox getApi() {
        if (api == null)
            api = MessageBox.createApi(this);
        return (MessageBox) api;
    }

    public VMessageBox getValue() {
        if (value == null)
            value = new VMessageBox(this);
        return (VMessageBox) value;
    }
}
