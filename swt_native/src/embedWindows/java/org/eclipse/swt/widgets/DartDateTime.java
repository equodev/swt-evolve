/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
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
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import java.util.Objects;
import dev.equo.swt.*;

/**
 * Instances of this class are selectable user interface
 * objects that allow the user to enter and modify date
 * or time values.
 * <p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not make sense to add children to it, or set a layout on it.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>DATE, TIME, CALENDAR, SHORT, MEDIUM, LONG, DROP_DOWN, CALENDAR_WEEKNUMBERS</dd>
 * <dt><b>Events:</b></dt>
 * <dd>DefaultSelection, Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles DATE, TIME, or CALENDAR may be specified,
 * and only one of the styles SHORT, MEDIUM, or LONG may be specified.
 * The DROP_DOWN style is only valid with the DATE style.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="https://eclipse.dev/eclipse/swt/snippets/#datetime">DateTime snippets</a>
 * @see <a href="https://eclipse.dev/eclipse/swt/examples.html">SWT Example: ControlExample</a>
 * @see <a href="https://eclipse.dev/eclipse/swt/">Sample code and further information</a>
 *
 * @since 3.3
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartDateTime extends DartComposite implements IDateTime {

    // Gregorian switchover in North America: September 19, 1752
    static final int MIN_YEAR = 1752;

    static final int MAX_YEAR = 9999;

    boolean doubleClick, ignoreSelection;

    // short date format may include quoted text
    static final char SINGLE_QUOTE = '\'';

    // 1-4 lowercase 'd's represent day
    static final char DAY_FORMAT_CONSTANT = 'd';

    // 1-4 uppercase 'M's represent month
    static final char MONTH_FORMAT_CONSTANT = 'M';

    // 1-5 lowercase 'y's represent year
    static final char YEAR_FORMAT_CONSTANT = 'y';

    // 1-2 upper or lowercase 'h's represent hours
    static final char HOURS_FORMAT_CONSTANT = 'h';

    // 1-2 lowercase 'm's represent minutes
    static final char MINUTES_FORMAT_CONSTANT = 'm';

    // 1-2 lowercase 's's represent seconds
    static final char SECONDS_FORMAT_CONSTANT = 's';

    // 1-2 lowercase 't's represent am/pm
    static final char AMPM_FORMAT_CONSTANT = 't';

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
     * @param parent a composite control which will be the parent of the new instance (cannot be null)
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
     * @see SWT#DATE
     * @see SWT#TIME
     * @see SWT#CALENDAR
     * @see SWT#CALENDAR_WEEKNUMBERS
     * @see SWT#SHORT
     * @see SWT#MEDIUM
     * @see SWT#LONG
     * @see SWT#DROP_DOWN
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartDateTime(Composite parent, int style, DateTime api) {
        super(parent, checkStyle(style), api);
        if ((this.getApi().style & SWT.SHORT) != 0) {
        }
    }

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
    public void addSelectionListener(SelectionListener listener) {
        addTypedListener(listener, SWT.Selection, SWT.DefaultSelection);
    }

    static int checkStyle(int style) {
        /*
	* Even though it is legal to create this widget
	* with scroll bars, they serve no useful purpose
	* because they do not automatically scroll the
	* widget's client area.  The fix is to clear
	* the SWT style.
	*/
        style &= ~(SWT.H_SCROLL | SWT.V_SCROLL);
        style = checkBits(style, SWT.DATE, SWT.TIME, SWT.CALENDAR, 0, 0, 0);
        style = checkBits(style, SWT.MEDIUM, SWT.SHORT, SWT.LONG, 0, 0, 0);
        if ((style & SWT.DATE) == 0)
            style &= ~SWT.DROP_DOWN;
        return style;
    }

    @Override
    public void checkSubclass() {
        if (!isValidSubclass())
            error(SWT.ERROR_INVALID_SUBCLASS);
    }

    @Override
    Point computeSizeInPixels(Point hintInPoints, int zoom, boolean changed) {
        return Sizes.computeSize(this, hintInPoints.x, hintInPoints.y, changed);
    }

    @Override
    void createHandle() {
    }

    @Override
    int defaultBackground() {
        return 0;
    }

    String getCustomShortDateFormat() {
        return null;
    }

    String getCustomShortTimeFormat() {
        StringBuilder buffer = new StringBuilder(getTimeFormat());
        int length = buffer.length();
        boolean inQuotes = false;
        int start = 0, end = 0;
        while (start < length) {
            char ch = buffer.charAt(start);
            if (ch == SINGLE_QUOTE)
                inQuotes = !inQuotes;
            else if (ch == SECONDS_FORMAT_CONSTANT && !inQuotes) {
                end = start + 1;
                while (end < length && buffer.charAt(end) == SECONDS_FORMAT_CONSTANT) end++;
                // skip the preceding separator
                while (start > 0 && buffer.charAt(start) != MINUTES_FORMAT_CONSTANT) start--;
                start++;
                break;
            }
            start++;
        }
        if (start < end)
            buffer.delete(start, end);
        return buffer.toString();
    }

    String getTimeFormat() {
        return null;
    }

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
    public int getDay() {
        checkWidget();
        return this.day;
    }

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
    public int getHours() {
        checkWidget();
        return this.hours;
    }

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
    public int getMinutes() {
        checkWidget();
        return this.minutes;
    }

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
    public int getMonth() {
        checkWidget();
        return this.month;
    }

    @Override
    String getNameText() {
        return (getApi().style & SWT.TIME) != 0 ? getHours() + ":" + getMinutes() + ":" + getSeconds() : (getMonth() + 1) + "/" + getDay() + "/" + getYear();
    }

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
    public int getSeconds() {
        checkWidget();
        return this.seconds;
    }

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
    public int getYear() {
        checkWidget();
        return this.year;
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
    }

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
    public void removeSelectionListener(SelectionListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Selection, listener);
        eventTable.unhook(SWT.DefaultSelection, listener);
    }

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
    public void setDate(int year, int month, int day) {
        checkWidget();
        if (year < MIN_YEAR || year > MAX_YEAR)
            return;
    }

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
    public void setDay(int day) {
        int newValue = day;
        if (!java.util.Objects.equals(this.day, newValue)) {
            dirty();
        }
        checkWidget();
        this.day = newValue;
    }

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
    public void setHours(int hours) {
        int newValue = hours;
        if (!java.util.Objects.equals(this.hours, newValue)) {
            dirty();
        }
        checkWidget();
        if (hours < 0 || hours > 23)
            return;
        this.hours = newValue;
    }

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
    public void setMinutes(int minutes) {
        int newValue = minutes;
        if (!java.util.Objects.equals(this.minutes, newValue)) {
            dirty();
        }
        checkWidget();
        if (minutes < 0 || minutes > 59)
            return;
        this.minutes = newValue;
    }

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
    public void setMonth(int month) {
        int newValue = month;
        if (!java.util.Objects.equals(this.month, newValue)) {
            dirty();
        }
        checkWidget();
        this.month = newValue;
    }

    @Override
    public void setOrientation(int orientation) {
        dirty();
        /* Currently supported only for CALENDAR style. */
        if ((getApi().style & SWT.CALENDAR) != 0)
            super.setOrientation(orientation);
    }

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
    public void setSeconds(int seconds) {
        int newValue = seconds;
        if (!java.util.Objects.equals(this.seconds, newValue)) {
            dirty();
        }
        checkWidget();
        if (seconds < 0 || seconds > 59)
            return;
        this.seconds = newValue;
    }

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
    public void setTime(int hours, int minutes, int seconds) {
        dirty();
        checkWidget();
        if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59 || seconds < 0 || seconds > 59)
            return;
        if ((getApi().style & SWT.CALENDAR) != 0 && hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59 && seconds >= 0 && seconds <= 59) {
        }
    }

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
    public void setYear(int year) {
        int newValue = year;
        if (!java.util.Objects.equals(this.year, newValue)) {
            dirty();
        }
        checkWidget();
        if (year < MIN_YEAR || year > MAX_YEAR)
            return;
        this.year = newValue;
    }

    @Override
    int widgetStyle() {
        if ((getApi().style & SWT.CALENDAR_WEEKNUMBERS) != 0) {
        }
        if ((getApi().style & SWT.DATE) != 0) {
        }
        return 0;
    }

    int day;

    int hours;

    int minutes;

    int month;

    int seconds;

    int year;

    public boolean _doubleClick() {
        return doubleClick;
    }

    public boolean _ignoreSelection() {
        return ignoreSelection;
    }

    public int _day() {
        return day;
    }

    public int _hours() {
        return hours;
    }

    public int _minutes() {
        return minutes;
    }

    public int _month() {
        return month;
    }

    public int _seconds() {
        return seconds;
    }

    public int _year() {
        return year;
    }

    protected void _hookEvents() {
        super._hookEvents();
        FlutterBridge.on(this, "Selection", "DefaultSelection", e -> {
            getDisplay().asyncExec(() -> {
                if (isDisposed())
                    return;
                sendEvent(SWT.DefaultSelection, e);
            });
        });
        FlutterBridge.on(this, "Selection", "Selection", e -> {
            getDisplay().asyncExec(() -> {
                if (isDisposed())
                    return;
                if (e != null) {
                    if ((getApi().style & SWT.TIME) != 0) {
                        hours = e.height;
                        minutes = e.count;
                        seconds = e.index;
                    } else {
                        year = e.x;
                        month = e.y;
                        day = e.width;
                    }
                }
                sendEvent(SWT.Selection, e);
            });
        });
    }

    public DateTime getApi() {
        if (api == null)
            api = DateTime.createApi(this);
        return (DateTime) api;
    }

    public VDateTime getValue() {
        if (value == null)
            value = new VDateTime(this);
        return (VDateTime) value;
    }
}
