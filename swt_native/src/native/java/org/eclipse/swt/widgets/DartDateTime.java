/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2016 IBM Corporation and others.
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

    Shell popupShell;

    DateTime popupCalendar;

    int savedYear, savedMonth, savedDay;

    Listener clickListener;

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

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        return Sizes.computeSize(this, wHint, hHint, changed);
    }

    @Override
    void createHandle() {
    }

    void createPopupShell(int year, int month, int day) {
        popupShell = new Shell(getShell(), SWT.NO_TRIM | SWT.ON_TOP);
        ((DartShell) popupShell.getImpl()).isPopup = true;
        popupCalendar = new DateTime(popupShell, SWT.CALENDAR);
        if (font != null)
            popupCalendar.setFont(font);
        if (clickListener == null) {
            clickListener = event -> {
                if (event.widget instanceof Control c && event.widget != DartDateTime.this.getApi()) {
                    if (c.getShell() != popupShell) {
                        hideCalendar();
                    }
                }
            };
        }
        popupCalendar.addListener(SWT.Selection, event -> {
            int year1 = popupCalendar.getYear();
            int month1 = popupCalendar.getMonth();
            int day1 = popupCalendar.getDay();
            setDate(year1, month1, day1);
            Event e = new Event();
            e.time = event.time;
            notifyListeners(SWT.Selection, e);
            hideCalendar();
        });
        addListener(SWT.Dispose, event -> {
            if (popupShell != null && !popupShell.isDisposed()) {
                disposePopupShell();
            }
        });
        addListener(SWT.FocusOut, event -> {
            hideCalendar();
            display.removeFilter(SWT.MouseDown, clickListener);
        });
        if (year != -1)
            popupCalendar.setDate(year, month, day);
    }

    @Override
    void deregister() {
        super.deregister();
    }

    void disposePopupShell() {
        popupShell.dispose();
        popupShell = null;
        popupCalendar = null;
    }

    void showCalendar() {
        if (isDropped())
            return;
        savedYear = getYear();
        savedMonth = getMonth();
        savedDay = getDay();
        if (getShell() != popupShell.getParent()) {
            disposePopupShell();
            createPopupShell(savedYear, savedMonth, savedDay);
        }
        Point dateBounds = getSize();
        Point calendarSize = popupCalendar.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
        popupCalendar.setBounds(1, 1, Math.max(dateBounds.x - 2, calendarSize.x), calendarSize.y);
        popupCalendar.setDate(savedYear, savedMonth, savedDay);
        Rectangle parentRect = display.map(getParent(), null, getBounds());
        Rectangle displayRect = getMonitor().getClientArea();
        int width = Math.max(dateBounds.x, calendarSize.x + 2);
        int height = calendarSize.y + 2;
        int x = parentRect.x;
        int y = parentRect.y + dateBounds.y;
        if (y + height > displayRect.y + displayRect.height)
            y = parentRect.y - height;
        if (x + width > displayRect.x + displayRect.width)
            x = displayRect.x + displayRect.width - calendarSize.x;
        popupShell.setBounds(x, y, width, height);
        popupShell.setVisible(true);
        if (isFocusControl())
            popupCalendar.setFocus();
        display.addFilter(SWT.MouseDown, clickListener);
    }

    void hideCalendar() {
        if (!isDropped())
            return;
        popupShell.setVisible(false);
        if (!isDisposed() && isFocusControl()) {
            setFocus();
        }
        display.removeFilter(SWT.MouseDown, clickListener);
    }

    int getBezelInset() {
        return 0;
    }

    int getBezelSize() {
        return 0;
    }

    @Override
    public Control[] getChildren() {
        checkWidget();
        return new Control[0];
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

    boolean isDropped() {
        return popupShell.getVisible();
    }

    @Override
    boolean isEventView(long id) {
        return true;
    }

    @Override
    void register() {
        super.register();
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
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

    @Override
    void resized() {
        super.resized();
    }

    @Override
    void sendSelection() {
        {
            // SWT.DATE or SWT.TIME
            sendSelectionEvent(SWT.Selection);
        }
    }

    /* Drop-down arrow button has been pressed. */
    @Override
    void sendVerticalSelection() {
        setFocus();
        if (isDropped()) {
            hideCalendar();
        } else {
            showCalendar();
        }
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
        if (!org.eclipse.swt.widgets.DateTimeHelper.isValidDate(year, month, day))
            return;
        if (this.year != year || this.month != month || this.day != day) {
            dirty();
        }
        this.year = year;
        this.month = month;
        this.day = day;
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
        checkWidget();
        if (!org.eclipse.swt.widgets.DateTimeHelper.isValidDate(this.year, this.month, day))
            return;
        if (this.day != day)
            dirty();
        this.day = day;
    }

    @Override
    void setForeground(double[] color) {
        if (color == null) {
            if ((getApi().style & SWT.CALENDAR) != 0) {
            } else {
            }
        } else {
        }
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
        checkWidget();
        if (!org.eclipse.swt.widgets.DateTimeHelper.isValidDate(this.year, month, this.day))
            return;
        if (this.month != month)
            dirty();
        this.month = month;
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

    @Override
    void setSmallSize() {
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
        checkWidget();
        if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59 || seconds < 0 || seconds > 59)
            return;
        if (this.hours != hours || this.minutes != minutes || this.seconds != seconds) {
            dirty();
        }
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
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
        checkWidget();
        if (year < MIN_YEAR || year > MAX_YEAR)
            return;
        if (!org.eclipse.swt.widgets.DateTimeHelper.isValidDate(year, this.month, this.day))
            return;
        if (this.year != year)
            dirty();
        this.year = year;
    }

    int day;

    int hours;

    int minutes;

    int month;

    int seconds;

    int year;

    public Shell _popupShell() {
        return popupShell;
    }

    public DateTime _popupCalendar() {
        return popupCalendar;
    }

    public int _savedYear() {
        return savedYear;
    }

    public int _savedMonth() {
        return savedMonth;
    }

    public int _savedDay() {
        return savedDay;
    }

    public Listener _clickListener() {
        return clickListener;
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
