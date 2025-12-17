/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2018 IBM Corporation and others.
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
 * objects that allow the user to enter and modify numeric
 * values.
 * <p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not make sense to add children to it, or set a layout on it.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>READ_ONLY, WRAP</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection, Modify, Verify</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#spinner">Spinner snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.1
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartSpinner extends DartComposite implements ISpinner {

    static final int MIN_ARROW_WIDTH = 6;

    int lastEventTime = 0;

    long imContext;

    long gdkEventKey = 0;

    long entryHandle;

    int fixStart = -1, fixEnd = -1;

    double climbRate = 1;

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
     * @see SWT#READ_ONLY
     * @see SWT#WRAP
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartSpinner(Composite parent, int style, Spinner api) {
        super(parent, checkStyle(style), api);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the receiver's text is modified, by sending
     * it one of the messages defined in the <code>ModifyListener</code>
     * interface.
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
     * @see ModifyListener
     * @see #removeModifyListener
     */
    public void addModifyListener(ModifyListener listener) {
        addTypedListener(listener, SWT.Modify);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the control is selected by the user, by sending
     * it one of the messages defined in the <code>SelectionListener</code>
     * interface.
     * <p>
     * <code>widgetSelected</code> is not called for texts.
     * <code>widgetDefaultSelected</code> is typically called when ENTER is pressed in a single-line text.
     * </p>
     *
     * @param listener the listener which should be notified when the control is selected by the user
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

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the receiver's text is verified, by sending
     * it one of the messages defined in the <code>VerifyListener</code>
     * interface.
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
     * @see VerifyListener
     * @see #removeVerifyListener
     */
    void addVerifyListener(VerifyListener listener) {
        addTypedListener(listener, SWT.Verify);
    }

    static int checkStyle(int style) {
        /*
	* Even though it is legal to create this widget
	* with scroll bars, they serve no useful purpose
	* because they do not automatically scroll the
	* widget's client area.  The fix is to clear
	* the SWT style.
	*/
        return style & ~(SWT.H_SCROLL | SWT.V_SCROLL);
    }

    @Override
    public void checkSubclass() {
        if (!isValidSubclass())
            error(SWT.ERROR_INVALID_SUBCLASS);
    }

    @Override
    Point computeSizeInPixels(int wHint, int hHint, boolean changed) {
        return Sizes.compute(this);
    }

    @Override
    Rectangle computeTrimInPixels(int x, int y, int width, int height) {
        checkWidget();
        int xborder = 0, yborder = 0;
        Rectangle trim = super.computeTrimInPixels(x, y, width, height);
        if ((getApi().style & SWT.BORDER) != 0) {
        }
        trim.x -= xborder;
        trim.y -= yborder;
        trim.width += 2 * xborder;
        trim.height += 2 * yborder;
        return new Rectangle(trim.x, trim.y, trim.width, trim.height);
    }

    /**
     * Copies the selected text.
     * <p>
     * The current selection is copied to the clipboard.
     * </p>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void copy() {
        checkWidget();
    }

    @Override
    void createHandle(int index) {
        // In GTK 3 font description is inherited from parent widget which is not how SWT has always worked,
    }

    /**
     * Cuts the selected text.
     * <p>
     * The current selection is first copied to the
     * clipboard and then deleted from the widget.
     * </p>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void cut() {
        checkWidget();
    }

    @Override
    void deregister() {
        super.deregister();
        long imContext = imContext();
        if (imContext != 0)
            ((SwtDisplay) display.getImpl()).removeWidget(imContext);
    }

    @Override
    long eventWindow() {
        return paintWindow();
    }

    @Override
    long eventSurface() {
        return paintSurface();
    }

    @Override
    long enterExitHandle() {
        return fixedHandle;
    }

    @Override
    boolean filterKey(long event) {
        gdkEventKey = event;
        return false;
    }

    void fixIM() {
        /*
	*  The IM filter has to be called one time for each key press event.
	*  When the IM is open the key events are duplicated. The first event
	*  is filtered by SWT and the second event is filtered by GTK.  In some
	*  cases the GTK handler does not run (the widget is destroyed, the
	*  application code consumes the event, etc), for these cases the IM
	*  filter has to be called by SWT.
	*/
        if (gdkEventKey != 0 && gdkEventKey != -1) {
            long imContext = imContext();
            if (imContext != 0) {
                gdkEventKey = -1;
                return;
            }
        }
        gdkEventKey = 0;
    }

    @Override
    int getBorderWidthInPixels() {
        checkWidget();
        if ((this.getApi().style & SWT.BORDER) != 0) {
            return getThickness(getApi().handle).x;
        }
        return 0;
    }

    /**
     * Returns the amount that the receiver's value will be
     * modified by when the up/down arrows are pressed.
     *
     * @return the increment
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getIncrement() {
        checkWidget();
        return this.increment;
    }

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
    public int getMaximum() {
        checkWidget();
        return this.maximum;
    }

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
    public int getMinimum() {
        checkWidget();
        return this.minimum;
    }

    /**
     * Returns the amount that the receiver's position will be
     * modified by when the page up/down keys are pressed.
     *
     * @return the page increment
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getPageIncrement() {
        checkWidget();
        return this.pageIncrement;
    }

    /**
     * Returns the <em>selection</em>, which is the receiver's position.
     *
     * @return the selection
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getSelection() {
        checkWidget();
        return this.selection;
    }

    /**
     * Returns a string containing a copy of the contents of the
     * receiver's text field, or an empty string if there are no
     * contents.
     *
     * @return the receiver's text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    public String getText() {
        checkWidget();
        return null;
    }

    /**
     * Returns the maximum number of characters that the receiver's
     * text field is capable of holding. If this has not been changed
     * by <code>setTextLimit()</code>, it will be the constant
     * <code>Spinner.LIMIT</code>.
     *
     * @return the text limit
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #LIMIT
     *
     * @since 3.4
     */
    public int getTextLimit() {
        checkWidget();
        return this.textLimit;
    }

    /**
     * Returns the number of decimal places used by the receiver.
     *
     * @return the digits
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getDigits() {
        checkWidget();
        return this.digits;
    }

    String getDecimalSeparator() {
        return null;
    }

    @Override
    void hookEvents() {
        super.hookEvents();
        long imContext = imContext();
        if (imContext != 0) {
        }
    }

    long imContext() {
        if (imContext != 0)
            return imContext;
        return 0;
    }

    @Override
    long paintWindow() {
        long window = super.paintWindow();
        return window;
    }

    @Override
    long paintSurface() {
        long surface = super.paintSurface();
        /* TODO: GTK4 no access to children of the surface. Need to find alternative, note that class hierarchy can change from GTK3 */
        return surface;
    }

    /**
     * Pastes text from clipboard.
     * <p>
     * The selected text is deleted from the widget
     * and new text inserted from the clipboard.
     * </p>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void paste() {
        checkWidget();
    }

    @Override
    void register() {
        super.register();
        long imContext = imContext();
        if (imContext != 0)
            ((SwtDisplay) display.getImpl()).addWidget(imContext, this.getApi());
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        fixIM();
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the receiver's text is modified.
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
     * @see ModifyListener
     * @see #addModifyListener
     */
    public void removeModifyListener(ModifyListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Modify, listener);
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
     * Removes the listener from the collection of listeners who will
     * be notified when the control is verified.
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
     * @see VerifyListener
     * @see #addVerifyListener
     */
    void removeVerifyListener(VerifyListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Verify, listener);
    }

    @Override
    public void setCursor(long cursor) {
        long defaultCursor = 0;
        if (cursor == 0) {
        }
        super.setCursor(cursor != 0 ? cursor : defaultCursor);
    }

    /**
     * Sets the amount that the receiver's value will be
     * modified by when the up/down arrows are pressed to
     * the argument, which must be at least one.
     *
     * @param value the new increment (must be greater than zero)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setIncrement(int value) {
        int newValue = value;
        if (!java.util.Objects.equals(this.increment, newValue)) {
            dirty();
        }
        checkWidget();
        if (value < 1)
            return;
        this.increment = newValue;
    }

    /**
     * Sets the maximum value that the receiver will allow.  This new
     * value will be ignored if it is less than the receiver's current
     * minimum value.  If the new maximum is applied then the receiver's
     * selection value will be adjusted if necessary to fall within its new range.
     *
     * @param value the new maximum, which must be greater than or equal to the current minimum
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setMaximum(int value) {
        dirty();
        checkWidget();
        if (value < this.minimum)
            return;
        this.maximum = value;
    }

    /**
     * Sets the minimum value that the receiver will allow.  This new
     * value will be ignored if it is greater than the receiver's
     * current maximum value.  If the new minimum is applied then the receiver's
     * selection value will be adjusted if necessary to fall within its new range.
     *
     * @param value the new minimum, which must be less than or equal to the current maximum
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setMinimum(int value) {
        dirty();
        checkWidget();
        if (value > this.maximum)
            return;
        this.minimum = value;
    }

    /**
     * Sets the amount that the receiver's position will be
     * modified by when the page up/down keys are pressed
     * to the argument, which must be at least one.
     *
     * @param value the page increment (must be greater than zero)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setPageIncrement(int value) {
        int newValue = value;
        if (!java.util.Objects.equals(this.pageIncrement, newValue)) {
            dirty();
        }
        checkWidget();
        if (value < 1)
            return;
        this.pageIncrement = newValue;
    }

    /**
     * Sets the <em>selection</em>, which is the receiver's
     * position, to the argument. If the argument is not within
     * the range specified by minimum and maximum, it will be
     * adjusted to fall within this range.
     *
     * @param value the new selection (must be zero or greater)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setSelection(int value) {
        int newValue = value;
        if (!java.util.Objects.equals(this.selection, newValue)) {
            dirty();
        }
        checkWidget();
        this.selection = newValue;
    }

    /**
     * Sets the maximum number of characters that the receiver's
     * text field is capable of holding to be the argument.
     * <p>
     * To reset this value to the default, use <code>setTextLimit(Spinner.LIMIT)</code>.
     * Specifying a limit value larger than <code>Spinner.LIMIT</code> sets the
     * receiver's limit to <code>Spinner.LIMIT</code>.
     * </p>
     * @param limit new text limit
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_CANNOT_BE_ZERO - if the limit is zero</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #LIMIT
     *
     * @since 3.4
     */
    public void setTextLimit(int limit) {
        int newValue = limit;
        if (!java.util.Objects.equals(this.textLimit, newValue)) {
            dirty();
        }
        checkWidget();
        if (limit == 0)
            error(SWT.ERROR_CANNOT_BE_ZERO);
        this.textLimit = newValue;
    }

    /**
     * Sets the number of decimal places used by the receiver.
     * <p>
     * The digit setting is used to allow for floating point values in the receiver.
     * For example, to set the selection to a floating point value of 1.37 call setDigits() with
     * a value of 2 and setSelection() with a value of 137. Similarly, if getDigits() has a value
     * of 2 and getSelection() returns 137 this should be interpreted as 1.37. This applies to all
     * numeric APIs.
     * </p>
     *
     * @param value the new digits (must be greater than or equal to zero)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the value is less than zero</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setDigits(int value) {
        int newValue = value;
        if (!java.util.Objects.equals(this.digits, newValue)) {
            dirty();
        }
        checkWidget();
        if (value < 0)
            error(SWT.ERROR_INVALID_ARGUMENT);
        int factor = 1;
        {
            climbRate /= factor;
        }
        this.digits = newValue;
    }

    /**
     * Sets the receiver's selection, minimum value, maximum
     * value, digits, increment and page increment all at once.
     * <p>
     * Note: This is similar to setting the values individually
     * using the appropriate methods, but may be implemented in a
     * more efficient fashion on some platforms.
     * </p>
     *
     * @param selection the new selection value
     * @param minimum the new minimum value
     * @param maximum the new maximum value
     * @param digits the new digits value
     * @param increment the new increment value
     * @param pageIncrement the new pageIncrement value
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    public void setValues(int selection, int minimum, int maximum, int digits, int increment, int pageIncrement) {
        checkWidget();
        if (maximum < minimum)
            return;
        if (digits < 0)
            return;
        if (increment < 1)
            return;
        if (pageIncrement < 1)
            return;
        selection = Math.min(Math.max(minimum, selection), maximum);
        double factor = 1;
        for (int i = 0; i < digits; i++) factor *= 10;
        climbRate = 1.0 / factor;
    }

    @Override
    boolean checkSubwindow() {
        return false;
    }

    @Override
    boolean translateTraversal(long event) {
        return super.translateTraversal(event);
    }

    String verifyText(String string, int start, int end) {
        if (string.length() == 0 && start == end)
            return null;
        Event event = new Event();
        event.text = string;
        event.start = start;
        event.end = end;
        int index = 0;
        if (string.length() > 0) {
        }
        while (index < string.length()) {
            if (!Character.isDigit(string.charAt(index)))
                break;
            index++;
        }
        event.doit = index == string.length();
        /*
	 * It is possible (but unlikely), that application
	 * code could have disposed the widget in the verify
	 * event.  If this happens, answer null to cancel
	 * the operation.
	 */
        sendEvent(SWT.Verify, event);
        if (!event.doit || isDisposed())
            return null;
        return event.text;
    }

    int digits;

    int increment;

    int maximum;

    int minimum;

    int pageIncrement;

    int selection;

    int textLimit;

    public int _lastEventTime() {
        return lastEventTime;
    }

    public long _imContext() {
        return imContext;
    }

    public long _entryHandle() {
        return entryHandle;
    }

    public int _fixStart() {
        return fixStart;
    }

    public int _fixEnd() {
        return fixEnd;
    }

    public double _climbRate() {
        return climbRate;
    }

    public int _digits() {
        return digits;
    }

    public int _increment() {
        return increment;
    }

    public int _maximum() {
        return maximum;
    }

    public int _minimum() {
        return minimum;
    }

    public int _pageIncrement() {
        return pageIncrement;
    }

    public int _selection() {
        return selection;
    }

    public int _textLimit() {
        return textLimit;
    }

    protected void _hookEvents() {
        super._hookEvents();
        FlutterBridge.on(this, "Modify", "Modify", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Modify, e);
            });
        });
        FlutterBridge.on(this, "Selection", "DefaultSelection", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.DefaultSelection, e);
            });
        });
        FlutterBridge.on(this, "Selection", "Selection", e -> {
            getDisplay().asyncExec(() -> {
                setSelection(e.index);
            });
        });
        FlutterBridge.on(this, "Verify", "Verify", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Verify, e);
            });
        });
    }

    public Spinner getApi() {
        if (api == null)
            api = Spinner.createApi(this);
        return (Spinner) api;
    }

    public VSpinner getValue() {
        if (value == null)
            value = new VSpinner(this);
        return (VSpinner) value;
    }
}
