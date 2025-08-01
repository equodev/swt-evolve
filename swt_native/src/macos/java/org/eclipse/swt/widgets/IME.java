/**
 * ****************************************************************************
 *  Copyright (c) 2007, 2017 IBM Corporation and others.
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
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.cocoa.*;

/**
 * Instances of this class represent input method editors.
 * These are typically in-line pre-edit text areas that allow
 * the user to compose characters from Far Eastern languages
 * such as Japanese, Chinese or Korean.
 *
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>ImeComposition</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.4
 * @noextend This class is not intended to be subclassed by clients.
 */
public class IME extends Widget {

    /**
     * Prevents uninitialized instances from being created outside the package.
     */
    IME() {
        this((IIME) null);
        setImpl(new SwtIME(this));
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
     * @param parent a canvas control which will be the parent of the new instance (cannot be null)
     * @param style the style of control to construct
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public IME(Canvas parent, int style) {
        this((IIME) null);
        setImpl(new SwtIME(parent, style, this));
    }

    /**
     * Returns the offset of the caret from the start of the document.
     * -1 means that there is currently no active composition.
     * The caret is within the current composition.
     *
     * @return the caret offset
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getCaretOffset() {
        return getImpl().getCaretOffset();
    }

    /**
     * Returns the commit count of the composition.  This is the
     * number of characters that have been composed.  When the
     * commit count is equal to the length of the composition
     * text, then the in-line edit operation is complete.
     *
     * @return the commit count
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see IME#getText
     */
    public int getCommitCount() {
        return getImpl().getCommitCount();
    }

    /**
     * Returns the offset of the composition from the start of the document.
     * This is the start offset of the composition within the document and
     * in not changed by the input method editor itself during the in-line edit
     * session.
     *
     * @return the offset of the composition
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getCompositionOffset() {
        return getImpl().getCompositionOffset();
    }

    /**
     * Returns the ranges for the style that should be applied during the
     * in-line edit session.
     * <p>
     * The ranges array contains start and end pairs.  Each pair refers to
     * the corresponding style in the styles array.  For example, the pair
     * that starts at ranges[n] and ends at ranges[n+1] uses the style
     * at styles[n/2] returned by <code>getStyles()</code>.
     * </p>
     * @return the ranges for the styles
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see IME#getStyles
     */
    public int[] getRanges() {
        return getImpl().getRanges();
    }

    /**
     * Returns the styles for the ranges.
     * <p>
     * The ranges array contains start and end pairs.  Each pair refers to
     * the corresponding style in the styles array.  For example, the pair
     * that starts at ranges[n] and ends at ranges[n+1] uses the style
     * at styles[n/2].
     * </p>
     *
     * @return the ranges for the styles
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see IME#getRanges
     */
    public TextStyle[] getStyles() {
        return getImpl().getStyles();
    }

    /**
     * Returns the composition text.
     * <p>
     * The text for an IME is the characters in the widget that
     * are in the current composition. When the commit count is
     * equal to the length of the composition text, then the
     * in-line edit operation is complete.
     * </p>
     *
     * @return the widget text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public String getText() {
        return getImpl().getText();
    }

    /**
     * Returns <code>true</code> if the caret should be wide, and
     * <code>false</code> otherwise.  In some languages, for example
     * Korean, the caret is typically widened to the width of the
     * current character in the in-line edit session.
     *
     * @return the wide caret state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean getWideCaret() {
        return getImpl().getWideCaret();
    }

    /**
     * Sets the offset of the composition from the start of the document.
     * This is the start offset of the composition within the document and
     * in not changed by the input method editor itself during the in-line edit
     * session but may need to be changed by clients of the IME.  For example,
     * if during an in-line edit operation, a text editor inserts characters
     * above the IME, then the IME must be informed that the composition
     * offset has changed.
     *
     * @param offset the offset of the composition
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setCompositionOffset(int offset) {
        getImpl().setCompositionOffset(offset);
    }

    protected IME(IIME impl) {
        super(impl);
    }

    static IME createApi(IIME impl) {
        return new IME(impl);
    }

    public IIME getImpl() {
        return (IIME) super.getImpl();
    }
}
