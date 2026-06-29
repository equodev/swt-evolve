package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;

public interface ILink extends IControl, ImplLink {

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the control is selected by the user, by sending
     * it one of the messages defined in the <code>SelectionListener</code>
     * interface.
     * <p>
     * <code>widgetSelected</code> is called when the control is selected by the user.
     * <code>widgetDefaultSelected</code> is not called.
     * </p>
     *
     * @param listener the listener which should be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SelectionListener
     * @see #removeSelectionListener
     * @see SelectionEvent
     */
    void addSelectionListener(SelectionListener listener);

    /**
     * Returns the link foreground color.
     *
     * @return the receiver's link foreground color.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @since 3.105
     */
    Color getLinkForeground();

    /**
     * Returns the receiver's text, which will be an empty
     * string if it has never been set.
     *
     * @return the receiver's text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    String getText();

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the control is selected by the user.
     *
     * @param listener the listener which should no longer be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SelectionListener
     * @see #addSelectionListener
     */
    void removeSelectionListener(SelectionListener listener);

    /**
     * Sets the link foreground color to the color specified
     * by the argument, or to the default system color for the link
     * if the argument is null.
     * <p>
     * Note: This operation is a hint and may be overridden by the platform.
     * </p>
     * @param color the new color (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @since 3.105
     */
    void setLinkForeground(Color color);

    /**
     * Sets the receiver's text.
     * <p>
     * The string can contain both regular text and hyperlinks.  A hyperlink
     * is delimited by an anchor tag, &lt;a&gt; and &lt;/a&gt;.  Within an
     * anchor, a single HREF attribute is supported.  When a hyperlink is
     * selected, the text field of the selection event contains either the
     * text of the hyperlink or the value of its HREF, if one was specified.
     * In the rare case of identical hyperlinks within the same string, the
     * HREF attribute can be used to distinguish between them.  The string may
     * include the mnemonic character and line delimiters. The only delimiter
     * the HREF attribute supports is the quotation mark ("). Text containing
     * angle-bracket characters &lt; or &gt; may be escaped using \\, however
     * this operation is a hint and varies from platform to platform.
     * </p>
     * <p>
     * Mnemonics are indicated by an '&amp;' that causes the next
     * character to be the mnemonic. The receiver can have a
     * mnemonic in the text preceding each link. When the user presses a
     * key sequence that matches the mnemonic, focus is assigned
     * to the link that follows the text. Mnemonics in links and in
     * the trailing text are ignored. On most platforms,
     * the mnemonic appears underlined but may be emphasised in a
     * platform specific manner.  The mnemonic indicator character
     * '&amp;' can be escaped by doubling it in the string, causing
     * a single '&amp;' to be displayed.
     * </p>
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
    void setText(String string);

    Link getApi();
}
