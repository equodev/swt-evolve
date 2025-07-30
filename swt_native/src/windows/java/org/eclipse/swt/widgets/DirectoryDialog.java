/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2017 IBM Corporation and others.
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
 *      Martin Karpisek <martin.karpisek@gmail.com> - Bug 443250
 * *****************************************************************************
 */
package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.internal.ole.win32.*;
import org.eclipse.swt.internal.win32.*;

/**
 * Instances of this class allow the user to navigate
 * the file system and select a directory.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#directorydialog">DirectoryDialog snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample, Dialog tab</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DirectoryDialog extends Dialog {

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
    public DirectoryDialog(Shell parent) {
        this((IDirectoryDialog) null);
        setImpl(new SwtDirectoryDialog(parent, this));
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
     * </p>
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
     */
    public DirectoryDialog(Shell parent, int style) {
        this((IDirectoryDialog) null);
        setImpl(new SwtDirectoryDialog(parent, style, this));
    }

    /**
     * Returns the path which the dialog will use to filter
     * the directories it shows.
     *
     * @return the filter path
     *
     * @see #setFilterPath
     */
    public String getFilterPath() {
        return getImpl().getFilterPath();
    }

    /**
     * Returns the dialog's message, which is a description of
     * the purpose for which it was opened. This message will be
     * visible on the dialog while it is open.
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
     * @return a string describing the absolute path of the selected directory,
     *         or null if the dialog was cancelled or an error occurred
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the dialog has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the dialog</li>
     * </ul>
     */
    public String open() {
        return getImpl().open();
    }

    /**
     * Makes the dialog visible and brings it to the front of the display.
     * Equal to {@link DirectoryDialog#open()} but also exposes for state information like user cancellation.
     *
     * @return an Optional that either contains the absolute path of the selected directory
     *         or is empty in case the dialog was canceled
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the dialog has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the dialog</li>
     *    <li>ERROR_INVALID_RETURN_VALUE - if the dialog was not cancelled and did not return a valid path</li>
     * </ul>
     *
     * @since 3.126
     */
    public Optional<String> openDialog() {
        return getImpl().openDialog();
    }

    /**
     * Sets the path that the dialog will use to filter
     * the directories it shows to the argument, which may
     * be null. If the string is null, then the operating
     * system's default filter path will be used.
     * <p>
     * Note that the path string is platform dependent.
     * For convenience, either '/' or '\' can be used
     * as a path separator.
     * </p>
     *
     * @param string the filter path
     */
    public void setFilterPath(String string) {
        getImpl().setFilterPath(string);
    }

    /**
     * Sets the dialog's message, which is a description of
     * the purpose for which it was opened. This message will be
     * visible on the dialog while it is open.
     * <p>
     * NOTE: This operation is a hint and is not supported on some platforms. For
     * example, on Windows (Vista and later), the <code>DirectoryDialog</code>
     * doesn't have any provision to set a message.
     * </p>
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

    protected DirectoryDialog(IDirectoryDialog impl) {
        super(impl);
    }

    static DirectoryDialog createApi(IDirectoryDialog impl) {
        return new DirectoryDialog(impl);
    }

    public IDirectoryDialog getImpl() {
        return (IDirectoryDialog) super.getImpl();
    }
}
