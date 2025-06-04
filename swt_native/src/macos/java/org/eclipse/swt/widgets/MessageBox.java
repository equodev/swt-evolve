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
import org.eclipse.swt.internal.cocoa.*;

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
public class MessageBox extends Dialog {

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
    public MessageBox(Shell parent) {
        this((IMessageBox) null);
        setImpl(new SwtMessageBox(parent));
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
    public MessageBox(Shell parent, int style) {
        this((IMessageBox) null);
        setImpl(new SwtMessageBox(parent, style));
    }

    /**
     * Returns the dialog's message, or an empty string if it does not have one.
     * The message is a description of the purpose for which the dialog was opened.
     * This message will be visible in the dialog while it is open.
     *
     * @return the message
     */
    public String getMessage() {
        return getImpl().getMessage();
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
        return getImpl().open();
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
        getImpl().setMessage(string);
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
        getImpl().setButtonLabels(labels);
    }

    protected MessageBox(IMessageBox impl) {
        super(impl);
    }

    static MessageBox createApi(IMessageBox impl) {
        if (dev.equo.swt.Creation.creating.peek() instanceof MessageBox inst) {
            inst.impl = impl;
            return inst;
        } else
            return new MessageBox(impl);
    }

    public IMessageBox getImpl() {
        return (IMessageBox) super.getImpl();
    }
}
