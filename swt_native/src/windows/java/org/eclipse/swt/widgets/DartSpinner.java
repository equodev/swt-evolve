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

    long hwndText, hwndUpDown;

    boolean ignoreModify, ignoreCharacter;

    int pageIncrement, digits;

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
    void createHandle() {
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

    @Override
    Point computeSizeInPixels(int wHint, int hHint, boolean changed) {
        return Sizes.compute(this);
    }

    @Override
    Rectangle computeTrimInPixels(int x, int y, int width, int height) {
        checkWidget();
        if ((getApi().style & SWT.BORDER) != 0) {
            x -= 1;
            y -= 1;
            width += 2;
            height += 2;
        }
        return new Rectangle(x, y, width, height);
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
        if ((getApi().style & SWT.READ_ONLY) != 0)
            return;
    }

    @Override
    int defaultBackground() {
        return 0;
    }

    @Override
    void enableWidget(boolean enabled) {
        super.enableWidget(enabled);
    }

    @Override
    void deregister() {
        super.deregister();
        ((SwtDisplay) display.getImpl()).removeControl(hwndText);
        ((SwtDisplay) display.getImpl()).removeControl(hwndUpDown);
    }

    @Override
    boolean hasFocus() {
        return false;
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
        return digits;
    }

    String getDecimalSeparator() {
        return null;
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
        return pageIncrement;
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

    int getSelectionText(boolean[] parseFail) {
        try {
            if (digits > 0) {
                {
                }
            } else {
            }
        } catch (NumberFormatException e) {
        }
        parseFail[0] = true;
        return -1;
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

    @Override
    boolean isUseWsBorder() {
        return true;
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
        if ((getApi().style & SWT.READ_ONLY) != 0)
            return;
    }

    @Override
    void register() {
        super.register();
        ((SwtDisplay) display.getImpl()).addControl(hwndText, this.getApi());
        ((SwtDisplay) display.getImpl()).addControl(hwndUpDown, this.getApi());
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
        hwndText = hwndUpDown = 0;
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
    void setBackgroundImage(long hBitmap) {
        super.setBackgroundImage(hBitmap);
    }

    @Override
    void setBackgroundPixel(int pixel) {
        super.setBackgroundPixel(pixel);
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
        dirty();
        checkWidget();
        if (value < 0)
            error(SWT.ERROR_INVALID_ARGUMENT);
        if (value == this.digits)
            return;
        this.digits = value;
    }

    @Override
    void setForegroundPixel(int pixel) {
        super.setForegroundPixel(pixel);
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
        dirty();
        checkWidget();
        if (value < 1)
            return;
        this.increment = value;
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
        dirty();
        checkWidget();
        if (value < 1)
            return;
        pageIncrement = value;
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
        checkWidget();
        // Clamp value between min and max using instance variables
        value = Math.min(Math.max(this.minimum, value), this.maximum);
        setSelection(value, true, true, false);
    }

    void setSelection(int value, boolean setPos, boolean setText, boolean notify) {
        dirty();
        if (setPos) {
            // Position handled by Flutter widget
        }
        if (setText) {
            if (digits == 0) {
                // Integer value, Flutter handles text display
            } else {
                // Format decimal value for Flutter
                String string = String.valueOf(Math.abs(value));
                String decimalSeparator = getDecimalSeparator();
                int index = string.length() - digits;
                StringBuilder buffer = new StringBuilder();
                if (value < 0)
                    buffer.append("-");
                if (index > 0) {
                    buffer.append(string.substring(0, index));
                    buffer.append(decimalSeparator);
                    buffer.append(string.substring(index));
                } else {
                    buffer.append("0");
                    buffer.append(decimalSeparator);
                    while (index++ < 0) buffer.append("0");
                    buffer.append(string);
                }
            }
            if (hooks(SWT.Verify) || filters(SWT.Verify)) {
                // Verify events handled by Flutter
            }
        }
        if (notify)
            sendSelectionEvent(SWT.Selection);
        this.selection = value;
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
        dirty();
        checkWidget();
        if (limit == 0)
            error(SWT.ERROR_CANNOT_BE_ZERO);
        this.textLimit = limit;
    }

    @Override
    void setToolTipText(Shell shell, String string) {
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
        dirty();
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
        setIncrement(increment);
        this.pageIncrement = pageIncrement;
        this.digits = digits;
        setSelection(selection, true, true, false);
    }

    @Override
    void subclass() {
        super.subclass();
    }

    @Override
    void unsubclass() {
        super.unsubclass();
    }

    @Override
    void updateOrientation() {
        super.updateOrientation();
        if ((getApi().style & SWT.RIGHT_TO_LEFT) != 0) {
        } else {
        }
    }

    String verifyText(String string, int start, int end, Event keyEvent) {
        Event event = new Event();
        event.text = string;
        event.start = start;
        event.end = end;
        if (keyEvent != null) {
            event.character = keyEvent.character;
            event.keyCode = keyEvent.keyCode;
            event.stateMask = keyEvent.stateMask;
        }
        int index = 0;
        if (digits > 0) {
            String decimalSeparator = getDecimalSeparator();
            index = string.indexOf(decimalSeparator);
            if (index != -1) {
                string = string.substring(0, index) + string.substring(index + 1);
            }
            index = 0;
        }
        if (string.length() > 0) {
            int[] min = new int[1];
            if (min[0] < 0 && string.charAt(0) == '-')
                index++;
        }
        while (index < string.length()) {
            if (!Character.isDigit(string.charAt(index)))
                break;
            index++;
        }
        event.doit = index == string.length();
        sendEvent(SWT.Verify, event);
        if (!event.doit || isDisposed())
            return null;
        return event.text;
    }

    int increment;

    int maximum;

    int minimum;

    int selection;

    int textLimit;

    public long _hwndText() {
        return hwndText;
    }

    public long _hwndUpDown() {
        return hwndUpDown;
    }

    public boolean _ignoreModify() {
        return ignoreModify;
    }

    public boolean _ignoreCharacter() {
        return ignoreCharacter;
    }

    public int _pageIncrement() {
        return pageIncrement;
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
