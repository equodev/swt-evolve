package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface IScrollBar extends IWidget, ImplScrollBar {

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the user changes the receiver's value, by sending
     * it one of the messages defined in the <code>SelectionListener</code>
     * interface.
     * <p>
     * When <code>widgetSelected</code> is called, the event object detail field contains one of the following values:
     * <code>SWT.NONE</code> - for the end of a drag.
     * <code>SWT.DRAG</code>.
     * <code>SWT.HOME</code>.
     * <code>SWT.END</code>.
     * <code>SWT.ARROW_DOWN</code>.
     * <code>SWT.ARROW_UP</code>.
     * <code>SWT.PAGE_DOWN</code>.
     * <code>SWT.PAGE_UP</code>.
     * <code>widgetDefaultSelected</code> is not called.
     * </p>
     *
     * @param listener the listener which should be notified when the user changes the receiver's value
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
     * Returns <code>true</code> if the receiver is enabled, and
     * <code>false</code> otherwise. A disabled control is typically
     * not selectable from the user interface and draws with an
     * inactive or "grayed" look.
     *
     * @return the receiver's enabled state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #isEnabled
     */
    boolean getEnabled();

    /**
     * Returns the amount that the receiver's value will be
     * modified by when the up/down (or right/left) arrows
     * are pressed.
     *
     * @return the increment
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int getIncrement();

    /**
     * Returns the maximum value which the receiver will allow.
     *
     * @return the maximum
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int getMaximum();

    /**
     * Returns the minimum value which the receiver will allow.
     *
     * @return the minimum
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int getMinimum();

    /**
     * Returns the amount that the receiver's value will be
     * modified by when the page increment/decrement areas
     * are selected.
     *
     * @return the page increment
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int getPageIncrement();

    /**
     * Returns the receiver's parent, which must be a Scrollable.
     *
     * @return the receiver's parent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    Scrollable getParent();

    /**
     * Returns the single 'selection' that is the receiver's value.
     *
     * @return the selection
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int getSelection();

    /**
     * Returns a point describing the receiver's size. The
     * x coordinate of the result is the width of the receiver.
     * The y coordinate of the result is the height of the
     * receiver.
     *
     * @return the receiver's size
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    Point getSize();

    /**
     * Returns the receiver's thumb value.
     *
     * @return the thumb value
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see ScrollBar
     */
    int getThumb();

    /**
     * Returns a rectangle describing the size and location of the
     * receiver's thumb relative to its parent.
     *
     * @return the thumb bounds, relative to the {@link #getParent() parent}
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.6
     */
    Rectangle getThumbBounds();

    /**
     * Returns a rectangle describing the size and location of the
     * receiver's thumb track relative to its parent. This rectangle
     * comprises the areas 2, 3, and 4 as described in {@link ScrollBar}.
     *
     * @return the thumb track bounds, relative to the {@link #getParent() parent}
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.6
     */
    Rectangle getThumbTrackBounds();

    /**
     * Returns <code>true</code> if the receiver is visible, and
     * <code>false</code> otherwise.
     * <p>
     * If one of the receiver's ancestors is not visible or some
     * other condition makes the receiver not visible, this method
     * may still indicate that it is considered visible even though
     * it may not actually be showing.
     * </p>
     *
     * @return the receiver's visibility state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    boolean getVisible();

    /**
     * Returns <code>true</code> if the receiver is enabled and all
     * of the receiver's ancestors are enabled, and <code>false</code>
     * otherwise. A disabled control is typically not selectable from the
     * user interface and draws with an inactive or "grayed" look.
     *
     * @return the receiver's enabled state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #getEnabled
     */
    boolean isEnabled();

    /**
     * Returns <code>true</code> if the receiver is visible and all
     * of the receiver's ancestors are visible and <code>false</code>
     * otherwise.
     *
     * @return the receiver's visibility state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #getVisible
     */
    boolean isVisible();

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the user changes the receiver's value.
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
     * Enables the receiver if the argument is <code>true</code>,
     * and disables it otherwise. A disabled control is typically
     * not selectable from the user interface and draws with an
     * inactive or "grayed" look.
     *
     * @param enabled the new enabled state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setEnabled(boolean enabled);

    /**
     * Sets the amount that the receiver's value will be
     * modified by when the up/down (or right/left) arrows
     * are pressed to the argument, which must be at least
     * one.
     *
     * @param value the new increment (must be greater than zero)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setIncrement(int value);

    /**
     * Sets the maximum. If this value is negative or less than or
     * equal to the minimum, the value is ignored. If necessary, first
     * the thumb and then the selection are adjusted to fit within the
     * new range.
     *
     * @param value the new maximum
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setMaximum(int value);

    /**
     * Sets the minimum value. If this value is negative or greater
     * than or equal to the maximum, the value is ignored. If necessary,
     * first the thumb and then the selection are adjusted to fit within
     * the new range.
     *
     * @param value the new minimum
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setMinimum(int value);

    /**
     * Sets the amount that the receiver's value will be
     * modified by when the page increment/decrement areas
     * are selected to the argument, which must be at least
     * one.
     *
     * @param value the page increment (must be greater than zero)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setPageIncrement(int value);

    /**
     * Sets the single <em>selection</em> that is the receiver's
     * value to the argument which must be greater than or equal
     * to zero.
     *
     * @param selection the new selection (must be zero or greater)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setSelection(int selection);

    /**
     * Sets the thumb value. The thumb value should be used to represent
     * the size of the visual portion of the current range. This value is
     * usually the same as the page increment value.
     * <p>
     * This new value will be ignored if it is less than one, and will be
     * clamped if it exceeds the receiver's current range.
     * </p>
     *
     * @param value the new thumb value, which must be at least one and not
     * larger than the size of the current range
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setThumb(int value);

    /**
     * Sets the receiver's selection, minimum value, maximum
     * value, thumb, increment and page increment all at once.
     * <p>
     * Note: This is similar to setting the values individually
     * using the appropriate methods, but may be implemented in a
     * more efficient fashion on some platforms.
     * </p>
     *
     * @param selection the new selection value
     * @param minimum the new minimum value
     * @param maximum the new maximum value
     * @param thumb the new thumb value
     * @param increment the new increment value
     * @param pageIncrement the new pageIncrement value
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setValues(int selection, int minimum, int maximum, int thumb, int increment, int pageIncrement);

    /**
     * Marks the receiver as visible if the argument is <code>true</code>,
     * and marks it invisible otherwise.
     * <p>
     * If one of the receiver's ancestors is not visible or some
     * other condition makes the receiver not visible, marking
     * it visible may not actually cause it to be displayed.
     * </p>
     *
     * @param visible the new visibility state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setVisible(boolean visible);

    ScrollBar getApi();
}
