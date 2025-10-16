package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;

public interface IGroup extends IComposite, ImplGroup {

    void checkSubclass();

    /**
     * Returns the receiver's text, which is the string that the
     * is used as the <em>title</em>. If the text has not previously
     * been set, returns an empty string.
     *
     * @return the text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    String getText();

    void setFont(Font font);

    /**
     * Sets the receiver's text, which is the string that will
     * be displayed as the receiver's <em>title</em>, to the argument,
     * which may not be null. The string may include the mnemonic character.
     * <p>
     * Mnemonics are indicated by an '&amp;' that causes the next
     * character to be the mnemonic.  When the user presses a
     * key sequence that matches the mnemonic, focus is assigned
     * to the first child of the group. On most platforms, the
     * mnemonic appears underlined but may be emphasised in a
     * platform specific manner.  The mnemonic indicator character
     * '&amp;' can be escaped by doubling it in the string, causing
     * a single '&amp;' to be displayed.
     * </p><p>
     * Note: If control characters like '\n', '\t' etc. are used
     * in the string, then the behavior is platform dependent.
     * </p>
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
    void setText(String string);

    Group getApi();
}
