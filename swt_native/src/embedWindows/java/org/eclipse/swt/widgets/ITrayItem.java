package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ITrayItem extends IItem {

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the receiver is selected by the user, by sending
     * it one of the messages defined in the <code>SelectionListener</code>
     * interface.
     * <p>
     * <code>widgetSelected</code> is called when the receiver is selected
     * <code>widgetDefaultSelected</code> is called when the receiver is double-clicked
     * </p>
     *
     * @param listener the listener which should be notified when the receiver is selected by the user
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
     * Adds the listener to the collection of listeners who will
     * be notified when the platform-specific context menu trigger
     * has occurred, by sending it one of the messages defined in
     * the <code>MenuDetectListener</code> interface.
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
     * @see MenuDetectListener
     * @see #removeMenuDetectListener
     *
     * @since 3.3
     */
    void addMenuDetectListener(MenuDetectListener listener);

    void checkSubclass();

    /**
     * Returns the receiver's highlight image if it has one, or null
     * if it does not.
     *
     * @return the receiver's highlight image
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.8
     */
    Image getHighlightImage();

    /**
     * Returns the receiver's parent, which must be a <code>Tray</code>.
     *
     * @return the receiver's parent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    Tray getParent();

    /**
     * Returns the receiver's tool tip, or null if it has
     * not been set.
     *
     * @return the receiver's tool tip text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    ToolTip getToolTip();

    /**
     * Returns the receiver's tool tip text, or null if it has
     * not been set.
     *
     * @return the receiver's tool tip text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    String getToolTipText();

    /**
     * Returns <code>true</code> if the receiver is visible and
     * <code>false</code> otherwise.
     *
     * @return the receiver's visibility
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    boolean getVisible();

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the receiver is selected by the user.
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
     * Removes the listener from the collection of listeners who will
     * be notified when the platform-specific context menu trigger has
     * occurred.
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
     * @see MenuDetectListener
     * @see #addMenuDetectListener
     *
     * @since 3.3
     */
    void removeMenuDetectListener(MenuDetectListener listener);

    /**
     * Sets the receiver's highlight image.
     *
     * @param image the new highlight image
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.8
     */
    void setHighlightImage(Image image);

    /**
     * Sets the receiver's image.
     *
     * @param image the new image
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setImage(Image image);

    /**
     * Sets the receiver's tool tip to the argument, which
     * may be null indicating that no tool tip should be shown.
     *
     * @param toolTip the new tool tip (or null)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    void setToolTip(ToolTip toolTip);

    /**
     * Sets the receiver's tool tip text to the argument, which
     * may be null indicating that the default tool tip for the
     * control will be shown. For a control that has a default
     * tool tip, such as the Tree control on Windows, setting
     * the tool tip text to an empty string replaces the default,
     * causing no tool tip text to be shown.
     * <p>
     * The mnemonic indicator (character '&amp;') is not displayed in a tool tip.
     * To display a single '&amp;' in the tool tip, the character '&amp;' can be
     * escaped by doubling it in the string.
     * </p>
     * <p>
     * NOTE: This operation is a hint and behavior is platform specific, on Windows
     * for CJK-style mnemonics of the form " (&amp;C)" at the end of the tooltip text
     * are not shown in tooltip.
     * </p>
     *
     * @param string the new tool tip text (or null)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setToolTipText(String string);

    /**
     * Makes the receiver visible if the argument is <code>true</code>,
     * and makes it invisible otherwise.
     *
     * @param visible the new visibility state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setVisible(boolean visible);

    TrayItem getApi();
}
