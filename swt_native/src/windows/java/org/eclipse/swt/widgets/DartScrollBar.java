/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2013 IBM Corporation and others.
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
import dev.equo.swt.*;

/**
 * Instances of this class are selectable user interface
 * objects that represent a range of positive, numeric values.
 * <p>
 * At any given moment, a given scroll bar will have a
 * single 'selection' that is considered to be its
 * value, which is constrained to be within the range of
 * values the scroll bar represents (that is, between its
 * <em>minimum</em> and <em>maximum</em> values).
 * </p><p>
 * Typically, scroll bars will be made up of five areas:</p>
 * <ol>
 * <li>an arrow button for decrementing the value</li>
 * <li>a page decrement area for decrementing the value by a larger amount</li>
 * <li>a <em>thumb</em> for modifying the value by mouse dragging</li>
 * <li>a page increment area for incrementing the value by a larger amount</li>
 * <li>an arrow button for incrementing the value</li>
 * </ol>
 * <p>
 * Based on their style, scroll bars are either <code>HORIZONTAL</code>
 * (which have a left facing button for decrementing the value and a
 * right facing button for incrementing it) or <code>VERTICAL</code>
 * (which have an upward facing button for decrementing the value
 * and a downward facing buttons for incrementing it).
 * </p><p>
 * On some platforms, the size of the scroll bar's thumb can be
 * varied relative to the magnitude of the range of values it
 * represents (that is, relative to the difference between its
 * maximum and minimum values). Typically, this is used to
 * indicate some proportional value such as the ratio of the
 * visible area of a document to the total amount of space that
 * it would take to display it. SWT supports setting the thumb
 * size even if the underlying platform does not, but in this
 * case the appearance of the scroll bar will not change.
 * </p><p>
 * Scroll bars are created by specifying either <code>H_SCROLL</code>,
 * <code>V_SCROLL</code> or both when creating a <code>Scrollable</code>.
 * They are accessed from the <code>Scrollable</code> using
 * <code>getHorizontalBar</code> and <code>getVerticalBar</code>.
 * </p><p>
 * Note: Scroll bars are not Controls.  On some platforms, scroll bars
 * that appear as part of some standard controls such as a text or list
 * have no operating system resources and are not children of the control.
 * For this reason, scroll bars are treated specially.  To create a control
 * that looks like a scroll bar but has operating system resources, use
 * <code>Slider</code>.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>HORIZONTAL, VERTICAL</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles HORIZONTAL and VERTICAL may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see Slider
 * @see Scrollable
 * @see Scrollable#getHorizontalBar
 * @see Scrollable#getVerticalBar
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartScrollBar extends DartWidget implements IScrollBar {

    Scrollable parent;

    int increment, pageIncrement;

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
     * @see SWT#HORIZONTAL
     * @see SWT#VERTICAL
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    DartScrollBar(Scrollable parent, int style, ScrollBar api) {
        super(parent, checkStyle(style), api);
        this.parent = parent;
        createWidget();
    }

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
    public void addSelectionListener(SelectionListener listener) {
        addTypedListener(listener, SWT.Selection, SWT.DefaultSelection);
    }

    static int checkStyle(int style) {
        return checkBits(style, SWT.HORIZONTAL, SWT.VERTICAL, 0, 0, 0, 0);
    }

    void createWidget() {
        increment = 1;
        pageIncrement = 10;
        /*
	* Do not set the initial values of the maximum
	* or the thumb.  These values normally default
	* to 100 and 10 but may have been set already
	* by the widget that owns the scroll bar.  For
	* example, a scroll bar that is created for a
	* list widget, setting these defaults would
	* override the initial values provided by the
	* list widget.
	*/
    }

    @Override
    void destroyWidget() {
        long hwnd = hwndScrollBar();
        int type = scrollBarType();
        ((DartScrollable) parent.getImpl()).destroyScrollBar(getApi().style);
        releaseHandle();
    }

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
    public boolean getEnabled() {
        checkWidget();
        return (getApi().state & DISABLED) == 0;
    }

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
    public int getIncrement() {
        checkWidget();
        return increment;
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
    public int getPageIncrement() {
        checkWidget();
        return pageIncrement;
    }

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
    public Scrollable getParent() {
        checkWidget();
        return parent;
    }

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
    public int getSelection() {
        checkWidget();
        return this.selection;
    }

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
    public Point getSize() {
        checkWidget();
        return DPIUtil.scaleDown(getSizeInPixels(), getZoom());
    }

    public Point getSizeInPixels() {
        return new Point(20, 20);
    }

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
    public int getThumb() {
        checkWidget();
        return this.thumb;
    }

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
    public Rectangle getThumbBounds() {
        checkWidget();
        return DPIUtil.scaleDown(getThumbBoundsInPixels(), getZoom());
    }

    Rectangle getThumbBoundsInPixels() {
        ((DartControl) parent.getImpl()).forceResize();
        int x, y, width, height;
        if ((getApi().style & SWT.HORIZONTAL) != 0) {
        } else {
        }
        return null;
    }

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
    public Rectangle getThumbTrackBounds() {
        checkWidget();
        return DPIUtil.scaleDown(getThumbTrackBoundsInPixels(), getZoom());
    }

    Rectangle getThumbTrackBoundsInPixels() {
        ((DartControl) parent.getImpl()).forceResize();
        int x = 0, y = 0, width, height;
        if ((getApi().style & SWT.HORIZONTAL) != 0) {
        } else {
        }
        return null;
    }

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
    public boolean getVisible() {
        checkWidget();
        return this.visible;
    }

    long hwndScrollBar() {
        return ((DartScrollable) parent.getImpl()).scrolledHandle();
    }

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
    public boolean isEnabled() {
        checkWidget();
        return getEnabled() && parent.isEnabled();
    }

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
    public boolean isVisible() {
        checkWidget();
        return getVisible() && parent.isVisible();
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
        parent = null;
    }

    @Override
    void releaseParent() {
        super.releaseParent();
        if (((DartScrollable) parent.getImpl()).horizontalBar == this.getApi())
            ((DartScrollable) parent.getImpl()).horizontalBar = null;
        if (((DartScrollable) parent.getImpl()).verticalBar == this.getApi())
            ((DartScrollable) parent.getImpl()).verticalBar = null;
    }

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
    public void removeSelectionListener(SelectionListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Selection, listener);
        eventTable.unhook(SWT.DefaultSelection, listener);
    }

    int scrollBarType() {
        return 0;
    }

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
    public void setEnabled(boolean enabled) {
        checkWidget();
        this.enabled = enabled;
        long hwnd = hwndScrollBar();
        int type = scrollBarType();
        if (enabled) {
            getApi().state &= ~DISABLED;
        } else {
            getApi().state |= DISABLED;
        }
        getBridge().dirty(this);
    }

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
    public void setIncrement(int value) {
        checkWidget();
        if (value < 1)
            return;
        increment = value;
        getBridge().dirty(this);
    }

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
    public void setMaximum(int value) {
        checkWidget();
        this.maximum = value;
        if (value < 0)
            return;
        getBridge().dirty(this);
    }

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
    public void setMinimum(int value) {
        checkWidget();
        this.minimum = value;
        if (value < 0)
            return;
        getBridge().dirty(this);
    }

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
    public void setPageIncrement(int value) {
        checkWidget();
        if (value < 1)
            return;
        pageIncrement = value;
        getBridge().dirty(this);
    }

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
    public void setSelection(int selection) {
        checkWidget();
        this.selection = selection;
        getBridge().dirty(this);
    }

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
    public void setThumb(int value) {
        checkWidget();
        this.thumb = value;
        if (value < 1)
            return;
        getBridge().dirty(this);
    }

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
    public void setValues(int selection, int minimum, int maximum, int thumb, int increment, int pageIncrement) {
        checkWidget();
        if (minimum < 0)
            return;
        if (maximum < 0)
            return;
        if (thumb < 1)
            return;
        if (increment < 1)
            return;
        if (pageIncrement < 1)
            return;
        this.increment = increment;
        this.pageIncrement = pageIncrement;
        getBridge().dirty(this);
    }

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
    public void setVisible(boolean visible) {
        checkWidget();
        this.visible = visible;
        if (visible == getVisible())
            return;
        /*
	* Set the state bits before calling ShowScrollBar ()
	* because hiding and showing the scroll bar can cause
	* WM_SIZE messages when the client area is resized.
	* Setting the state before the call means that code
	* that runs during WM_SIZE that queries the visibility
	* of the scroll bar will get the correct value.
	*/
        getApi().state = visible ? getApi().state & ~HIDDEN : getApi().state | HIDDEN;
        long hwnd = hwndScrollBar();
        int type = scrollBarType();
        /*
	* Bug in Windows 7. Windows will cause pixel corruption
	* when there is only one scroll bar visible and it is
	* hidden.  The fix is to temporarily show the other scroll
	* bar and hide both.
	*/
        if (!visible) {
        }
        getBridge().dirty(this);
    }

    boolean enabled = true;

    int maximum;

    int minimum;

    int selection;

    int thumb;

    boolean visible = true;

    public Scrollable _parent() {
        return parent;
    }

    public int _increment() {
        return increment;
    }

    public int _pageIncrement() {
        return pageIncrement;
    }

    public boolean _enabled() {
        return enabled;
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

    public int _thumb() {
        return thumb;
    }

    public boolean _visible() {
        return visible;
    }

    public FlutterBridge getBridge() {
        if (bridge != null)
            return bridge;
        return ((DartWidget) parent.getImpl()).getBridge();
    }

    protected void hookEvents() {
        super.hookEvents();
        FlutterBridge.on(this, "Selection", "Selection", e -> {
            getDisplay().asyncExec(() -> {
                setSelection(e.index);
            });
        });
        FlutterBridge.on(this, "Selection", "DefaultSelection", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.DefaultSelection, e);
            });
        });
    }

    public ScrollBar getApi() {
        if (api == null)
            api = ScrollBar.createApi(this);
        return (ScrollBar) api;
    }

    public VScrollBar getValue() {
        if (value == null)
            value = new VScrollBar(this);
        return (VScrollBar) value;
    }
}
