package org.eclipse.swt.widgets;

import java.text.*;
import java.text.AttributedCharacterIterator.*;
import java.text.DateFormat.*;
import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;

public interface IDateTime extends IComposite {

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the control is selected by the user, by sending
     * it one of the messages defined in the <code>SelectionListener</code>
     * interface.
     * <p>
     * <code>widgetSelected</code> is called when the user changes the control's value.
     * <code>widgetDefaultSelected</code> is typically called when ENTER is pressed.
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

    void checkSubclass();

    /**
     * Returns the receiver's date, or day of the month.
     * <p>
     * The first day of the month is 1, and the last day depends on the month and year.
     * </p>
     *
     * @return a positive integer beginning with 1
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int getDay();

    /**
     * Returns the receiver's hours.
     * <p>
     * Hours is an integer between 0 and 23.
     * </p>
     *
     * @return an integer between 0 and 23
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int getHours();

    /**
     * Returns the receiver's minutes.
     * <p>
     * Minutes is an integer between 0 and 59.
     * </p>
     *
     * @return an integer between 0 and 59
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int getMinutes();

    /**
     * Returns the receiver's month.
     * <p>
     * The first month of the year is 0, and the last month is 11.
     * </p>
     *
     * @return an integer between 0 and 11
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int getMonth();

    /**
     * Returns the receiver's seconds.
     * <p>
     * Seconds is an integer between 0 and 59.
     * </p>
     *
     * @return an integer between 0 and 59
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int getSeconds();

    /**
     * Returns the receiver's year.
     * <p>
     * The first year is 1752 and the last year is 9999.
     * </p>
     *
     * @return an integer between 1752 and 9999
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int getYear();

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

    void setBackground(Color color);

    void setEnabled(boolean enabled);

    void setFont(Font font);

    void setForeground(Color color);

    /**
     * Sets the receiver's year, month, and day in a single operation.
     * <p>
     * This is the recommended way to set the date, because setting the year,
     * month, and day separately may result in invalid intermediate dates.
     * </p>
     *
     * @param year an integer between 1752 and 9999
     * @param month an integer between 0 and 11
     * @param day a positive integer beginning with 1
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    void setDate(int year, int month, int day);

    /**
     * Sets the receiver's date, or day of the month, to the specified day.
     * <p>
     * The first day of the month is 1, and the last day depends on the month and year.
     * If the specified day is not valid for the receiver's month and year, then it is ignored.
     * </p>
     *
     * @param day a positive integer beginning with 1
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setDate
     */
    void setDay(int day);

    /**
     * Sets the receiver's hours.
     * <p>
     * Hours is an integer between 0 and 23.
     * </p>
     *
     * @param hours an integer between 0 and 23
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setHours(int hours);

    void setMenu(Menu menu);

    /**
     * Sets the receiver's minutes.
     * <p>
     * Minutes is an integer between 0 and 59.
     * </p>
     *
     * @param minutes an integer between 0 and 59
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setMinutes(int minutes);

    /**
     * Sets the receiver's month.
     * <p>
     * The first month of the year is 0, and the last month is 11.
     * If the specified month is not valid for the receiver's day and year, then it is ignored.
     * </p>
     *
     * @param month an integer between 0 and 11
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setDate
     */
    void setMonth(int month);

    /**
     * Sets the receiver's seconds.
     * <p>
     * Seconds is an integer between 0 and 59.
     * </p>
     *
     * @param seconds an integer between 0 and 59
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setSeconds(int seconds);

    /**
     * Sets the receiver's hours, minutes, and seconds in a single operation.
     *
     * @param hours an integer between 0 and 23
     * @param minutes an integer between 0 and 59
     * @param seconds an integer between 0 and 59
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    void setTime(int hours, int minutes, int seconds);

    /**
     * Sets the receiver's year.
     * <p>
     * The first year is 1752 and the last year is 9999.
     * If the specified year is not valid for the receiver's day and month, then it is ignored.
     * </p>
     *
     * @param year an integer between 1752 and 9999
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setDate
     */
    void setYear(int year);

    DateTime getApi();
}
