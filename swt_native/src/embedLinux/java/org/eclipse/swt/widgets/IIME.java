package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;

public interface IIME extends IWidget {

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
    int getCaretOffset();

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
    int getCommitCount();

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
    int getCompositionOffset();

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
    int[] getRanges();

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
    TextStyle[] getStyles();

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
    String getText();

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
    boolean getWideCaret();

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
    void setCompositionOffset(int offset);

    IME getApi();
}
