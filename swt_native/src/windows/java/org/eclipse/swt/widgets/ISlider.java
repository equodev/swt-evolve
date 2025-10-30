package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ISlider extends IControl, ImplSlider {

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
     * Returns the 'selection', which is the receiver's value.
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
     * Returns the receiver's thumb value.
     *
     * @return the thumb value
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int getThumb();

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
     * @param value the new maximum, which must be greater than the current minimum
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
     * Sets the 'selection', which is the receiver's
     * value, to the argument which must be greater than or equal
     * to zero.
     *
     * @param value the new selection (must be zero or greater)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setSelection(int value);

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

    Slider getApi();
}
