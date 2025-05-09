/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2011 IBM Corporation and others.
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

import org.eclipse.swt.*;

/**
 *  This class is the abstract superclass of the classes
 *  that represent the built in platform dialogs.
 *  A <code>Dialog</code> typically contains other widgets
 *  that are not accessible. A <code>Dialog</code> is not
 *  a <code>Widget</code>.
 *  <p>
 *  This class can also be used as the abstract superclass
 *  for user-designed dialogs. Such dialogs usually consist
 *  of a Shell with child widgets. The basic template for a
 *  user-defined dialog typically looks something like this:</p>
 *  <pre><code>
 *  public class MyDialog extends Dialog {
 * 	Object result;
 *
 * 	public MyDialog (Shell parent, int style) {
 * 		super (parent, style);
 * 	}
 * 	public MyDialog (Shell parent) {
 * 		this (parent, 0); // your default style bits go here (not the Shell's style bits)
 * 	}
 * 	public Object open () {
 * 		Shell parent = getParent();
 * 		Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
 * 		shell.setText(getText());
 * 		// Your code goes here (widget creation, set result, etc).
 * 		shell.open();
 * 		Display display = parent.getDisplay();
 * 		while (!shell.isDisposed()) {
 * 			if (!display.readAndDispatch()) display.sleep();
 * 		}
 * 		return result;
 * 	}
 *  }
 *  </code></pre>
 *  <p>
 *  Note: The <em>modality</em> styles supported by this class
 *  are treated as <em>HINT</em>s, because not all are supported
 *  by every subclass on every platform. If a modality style is
 *  not supported, it is "upgraded" to a more restrictive modality
 *  style that is supported.  For example, if <code>PRIMARY_MODAL</code>
 *  is not supported by a particular dialog, it would be upgraded to
 *  <code>APPLICATION_MODAL</code>. In addition, as is the case
 *  for shells, the window manager for the desktop on which the
 *  instance is visible has ultimate control over the appearance
 *  and behavior of the instance, including its modality.
 *  </p>
 *  <dl>
 *  <dt><b>Styles:</b></dt>
 *  <dd>APPLICATION_MODAL, PRIMARY_MODAL, SYSTEM_MODAL, SHEET</dd>
 *  <dt><b>Events:</b></dt>
 *  <dd>(none)</dd>
 *  </dl>
 *  <p>
 *  Note: Only one of the styles APPLICATION_MODAL, PRIMARY_MODAL,
 *  and SYSTEM_MODAL may be specified.
 *  </p>
 *
 *  @see Shell
 *  @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 *  @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public abstract class Dialog {

    /**
     * Returns the receiver's parent, which must be a <code>Shell</code>
     * or null.
     *
     * @return the receiver's parent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Shell getParent() {
        return getDelegate().getParent().getApi();
    }

    /**
     * Returns the receiver's style information.
     * <p>
     * Note that, the value which is returned by this method <em>may
     * not match</em> the value which was provided to the constructor
     * when the receiver was created.
     * </p>
     *
     * @return the style bits
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getStyle() {
        return getDelegate().getStyle();
    }

    /**
     * Returns the receiver's text, which is the string that the
     * window manager will typically display as the receiver's
     * <em>title</em>. If the text has not previously been set,
     * returns an empty string.
     *
     * @return the text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public String getText() {
        return getDelegate().getText();
    }

    /**
     * Sets the receiver's text, which is the string that the
     * window manager will typically display as the receiver's
     * <em>title</em>, to the argument, which must not be null.
     *
     * @param string the new text
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the text is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setText(String string) {
        getDelegate().setText(string);
    }

    IDialog delegate;

    protected Dialog(IDialog delegate) {
        this.delegate = delegate;
        delegate.setApi(this);
    }

    protected IDialog getDelegate() {
        return delegate;
    }
}
