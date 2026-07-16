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
import java.util.Objects;
import dev.equo.swt.*;

/**
 *  Instances of this class implement rubber banding rectangles that are
 *  drawn onto a parent <code>Composite</code> or <code>Display</code>.
 *  These rectangles can be specified to respond to mouse and key events
 *  by either moving or resizing themselves accordingly.  Trackers are
 *  typically used to represent window geometries in a lightweight manner.
 *
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>LEFT, RIGHT, UP, DOWN, RESIZE</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Move, Resize</dd>
 * </dl>
 * <p>
 * Note: Rectangle move behavior is assumed unless RESIZE is specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#tracker">Tracker snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartTracker extends DartWidget implements ITracker {

    Control parent;

    boolean tracking, cancelled, stippled;

    Cursor clientCursor, resizeCursor;

    Rectangle[] rectangles = new Rectangle[0], proportions = rectangles;

    Rectangle bounds;

    int cursorOrientation = SWT.NONE;

    boolean inEvent = false;

    int oldX, oldY;

    /*
	* The following values mirror step sizes on Windows
	*/
    final static int STEPSIZE_SMALL = 1;

    final static int STEPSIZE_LARGE = 9;

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
     * @param parent a widget which will be the parent of the new instance (cannot be null)
     * @param style the style of widget to construct
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see SWT#LEFT
     * @see SWT#RIGHT
     * @see SWT#UP
     * @see SWT#DOWN
     * @see SWT#RESIZE
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartTracker(Composite parent, int style, Tracker api) {
        super(parent, checkStyle(style), api);
        this.parent = parent;
    }

    /**
     * Constructs a new instance of this class given the display
     * to create it on and a style value describing its behavior
     * and appearance.
     * <p>
     * The style value is either one of the style constants defined in
     * class <code>SWT</code> which is applicable to instances of this
     * class, or must be built by <em>bitwise OR</em>'ing together
     * (that is, using the <code>int</code> "|" operator) two or more
     * of those <code>SWT</code> style constants. The class description
     * lists the style constants that are applicable to the class.
     * Style bits are also inherited from superclasses.
     * </p><p>
     * Note: Currently, null can be passed in for the display argument.
     * This has the effect of creating the tracker on the currently active
     * display if there is one. If there is no current display, the
     * tracker is created on a "default" display. <b>Passing in null as
     * the display argument is not considered to be good coding style,
     * and may not be supported in a future release of SWT.</b>
     * </p>
     *
     * @param display the display to create the tracker on
     * @param style the style of control to construct
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see SWT#LEFT
     * @see SWT#RIGHT
     * @see SWT#UP
     * @see SWT#DOWN
     * @see SWT#RESIZE
     */
    public DartTracker(Display display, int style, Tracker api) {
        super(api);
        if (display == null)
            display = DartDisplay.getCurrent();
        if (display == null)
            display = DartDisplay.getDefault();
        if (!((DartDisplay) display.getImpl()).isValidThread()) {
            error(SWT.ERROR_THREAD_INVALID_ACCESS);
        }
        this.getApi().style = checkStyle(style);
        this.display = display;
        reskinWidget();
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the control is moved or resized, by sending
     * it one of the messages defined in the <code>ControlListener</code>
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
     * @see ControlListener
     * @see #removeControlListener
     */
    public void addControlListener(ControlListener listener) {
        addTypedListener(listener, SWT.Resize, SWT.Move);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when keys are pressed and released on the system keyboard, by sending
     * it one of the messages defined in the <code>KeyListener</code>
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
     * @see KeyListener
     * @see #removeKeyListener
     */
    public void addKeyListener(KeyListener listener) {
        addTypedListener(listener, SWT.KeyUp, SWT.KeyDown);
    }

    Point adjustMoveCursor() {
        if (bounds == null)
            return null;
        int newX = bounds.x + bounds.width / 2;
        int newY = bounds.y;
        /*
	 * Convert to screen coordinates if needed
	 */
        if (parent != null) {
            Point pt = parent.toDisplay(newX, newY);
            newX = pt.x;
            newY = pt.y;
        }
        display.setCursorLocation(newX, newY);
        return new Point(newX, newY);
    }

    Point adjustResizeCursor(boolean movePointer) {
        if (bounds == null)
            return null;
        int newX, newY;
        if ((cursorOrientation & SWT.LEFT) != 0) {
            newX = bounds.x;
        } else if ((cursorOrientation & SWT.RIGHT) != 0) {
            newX = bounds.x + bounds.width;
        } else {
            newX = bounds.x + bounds.width / 2;
        }
        if ((cursorOrientation & SWT.UP) != 0) {
            newY = bounds.y;
        } else if ((cursorOrientation & SWT.DOWN) != 0) {
            newY = bounds.y + bounds.height;
        } else {
            newY = bounds.y + bounds.height / 2;
        }
        /*
	 * Convert to screen coordinates if needed
	 */
        if (parent != null) {
            Point pt = parent.toDisplay(newX, newY);
            newX = pt.x;
            newY = pt.y;
        }
        if (movePointer) {
            display.setCursorLocation(newX, newY);
        }
        /*
	* If the client has not provided a custom cursor then determine
	* the appropriate resize cursor.
	*/
        if (clientCursor == null) {
            Cursor newCursor = new Cursor(display, switch(cursorOrientation) {
                case SWT.UP, SWT.DOWN ->
                    SWT.CURSOR_SIZENS;
                case SWT.LEFT, SWT.RIGHT ->
                    SWT.CURSOR_SIZEWE;
                case (SWT.LEFT | SWT.UP), (SWT.RIGHT | SWT.DOWN) ->
                    SWT.CURSOR_SIZENWSE;
                case (SWT.LEFT | SWT.DOWN), (SWT.RIGHT | SWT.UP) ->
                    SWT.CURSOR_SIZENESW;
                default ->
                    SWT.CURSOR_SIZEALL;
            });
            ((DartDisplay) display.getImpl()).lockCursor = false;
            ((DartDisplay) display.getImpl()).lockCursor = true;
            if (resizeCursor != null) {
                resizeCursor.dispose();
            }
            resizeCursor = newCursor;
        }
        return new Point(newX, newY);
    }

    static int checkStyle(int style) {
        if ((style & (SWT.LEFT | SWT.RIGHT | SWT.UP | SWT.DOWN)) == 0) {
            style |= SWT.LEFT | SWT.RIGHT | SWT.UP | SWT.DOWN;
        }
        return style;
    }

    /**
     * Stops displaying the tracker rectangles.  Note that this is not considered
     * to be a cancelation by the user.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void close() {
        checkWidget();
        tracking = false;
    }

    Rectangle computeBounds() {
        if (rectangles.length == 0)
            return null;
        int xMin = rectangles[0].x;
        int yMin = rectangles[0].y;
        int xMax = rectangles[0].x + rectangles[0].width;
        int yMax = rectangles[0].y + rectangles[0].height;
        for (int i = 1; i < rectangles.length; i++) {
            if (rectangles[i].x < xMin)
                xMin = rectangles[i].x;
            if (rectangles[i].y < yMin)
                yMin = rectangles[i].y;
            int rectRight = rectangles[i].x + rectangles[i].width;
            if (rectRight > xMax)
                xMax = rectRight;
            int rectBottom = rectangles[i].y + rectangles[i].height;
            if (rectBottom > yMax)
                yMax = rectBottom;
        }
        return new Rectangle(xMin, yMin, xMax - xMin, yMax - yMin);
    }

    Rectangle[] computeProportions(Rectangle[] rects) {
        Rectangle[] result = new Rectangle[rects.length];
        bounds = computeBounds();
        if (bounds != null) {
            for (int i = 0; i < rects.length; i++) {
                int x = 0, y = 0, width = 0, height = 0;
                if (bounds.width != 0) {
                    x = (rects[i].x - bounds.x) * 100 / bounds.width;
                    width = rects[i].width * 100 / bounds.width;
                } else {
                    width = 100;
                }
                if (bounds.height != 0) {
                    y = (rects[i].y - bounds.y) * 100 / bounds.height;
                    height = rects[i].height * 100 / bounds.height;
                } else {
                    height = 100;
                }
                result[i] = new Rectangle(x, y, width, height);
            }
        }
        return result;
    }

    /**
     * Returns the bounds that are being drawn, expressed relative to the parent
     * widget.  If the parent is a <code>Display</code> then these are screen
     * coordinates.
     *
     * @return the bounds of the Rectangles being drawn
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Rectangle[] getRectangles() {
        checkWidget();
        Rectangle[] result = new Rectangle[rectangles.length];
        for (int i = 0; i < rectangles.length; i++) {
            Rectangle current = rectangles[i];
            result[i] = new Rectangle(current.x, current.y, current.width, current.height);
        }
        return result;
    }

    /**
     * Returns <code>true</code> if the rectangles are drawn with a stippled line, <code>false</code> otherwise.
     *
     * @return the stippled effect of the rectangles
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean getStippled() {
        checkWidget();
        return stippled;
    }

    void moveRectangles(int xChange, int yChange) {
        if (bounds == null)
            return;
        if (xChange < 0 && ((getApi().style & SWT.LEFT) == 0))
            xChange = 0;
        if (xChange > 0 && ((getApi().style & SWT.RIGHT) == 0))
            xChange = 0;
        if (yChange < 0 && ((getApi().style & SWT.UP) == 0))
            yChange = 0;
        if (yChange > 0 && ((getApi().style & SWT.DOWN) == 0))
            yChange = 0;
        if (xChange == 0 && yChange == 0)
            return;
        bounds.x += xChange;
        bounds.y += yChange;
        for (int i = 0; i < rectangles.length; i++) {
            rectangles[i].x += xChange;
            rectangles[i].y += yChange;
        }
    }

    /**
     * Displays the Tracker rectangles for manipulation by the user.  Returns when
     * the user has either finished manipulating the rectangles or has cancelled the
     * Tracker.
     *
     * @return <code>true</code> if the user did not cancel the Tracker, <code>false</code> otherwise
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean open() {
        checkWidget();
        Display display = this.display;
        cancelled = false;
        tracking = true;
        /*
	* If exactly one of UP/DOWN is specified as a style then set the cursor
	* orientation accordingly (the same is done for LEFT/RIGHT styles below).
	*/
        int vStyle = getApi().style & (SWT.UP | SWT.DOWN);
        if (vStyle == SWT.UP || vStyle == SWT.DOWN) {
            cursorOrientation |= vStyle;
        }
        int hStyle = getApi().style & (SWT.LEFT | SWT.RIGHT);
        if (hStyle == SWT.LEFT || hStyle == SWT.RIGHT) {
            cursorOrientation |= hStyle;
        }
        Point cursorPos;
        boolean down = false;
        if (down) {
            cursorPos = display.getCursorLocation();
        } else {
            if ((getApi().style & SWT.RESIZE) != 0) {
                cursorPos = adjustResizeCursor(true);
            } else {
                cursorPos = adjustMoveCursor();
            }
        }
        if (cursorPos != null) {
            oldX = cursorPos.x;
            oldY = cursorPos.y;
        }
        Control oldTrackingControl = ((DartDisplay) display.getImpl()).trackingControl;
        ((DartDisplay) display.getImpl()).trackingControl = null;
        /* Tracker behaves like a Dialog with its own OS event loop. */
        while (tracking && !cancelled) {
            ((DartDisplay) display.getImpl()).addPool();
            try {
                if (parent != null && parent.isDisposed())
                    break;
                ((DartDisplay) display.getImpl()).runSkin();
                ((DartDisplay) display.getImpl()).runDeferredLayouts();
                if (clientCursor != null && resizeCursor == null) {
                    ((DartDisplay) display.getImpl()).lockCursor = false;
                    ((DartDisplay) display.getImpl()).lockCursor = true;
                }
                ((DartDisplay) display.getImpl()).runAsyncMessages(false);
            } finally {
                ((DartDisplay) display.getImpl()).removePool();
            }
        }
        /*
	* Cleanup: If this tracker was resizing then the last cursor that it created
	* needs to be destroyed.
	*/
        if (resizeCursor != null)
            resizeCursor.dispose();
        resizeCursor = null;
        if (oldTrackingControl != null && !oldTrackingControl.isDisposed()) {
            ((DartDisplay) display.getImpl()).trackingControl = oldTrackingControl;
        }
        ((DartDisplay) display.getImpl()).setCursor(((DartDisplay) display.getImpl()).findControl(true));
        if (!isDisposed()) {
        }
        tracking = false;
        return !cancelled;
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        parent = null;
        rectangles = proportions = null;
        bounds = null;
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the control is moved or resized.
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
     * @see ControlListener
     * @see #addControlListener
     */
    public void removeControlListener(ControlListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Resize, listener);
        eventTable.unhook(SWT.Move, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when keys are pressed and released on the system keyboard.
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
     * @see KeyListener
     * @see #addKeyListener
     */
    public void removeKeyListener(KeyListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.KeyUp, listener);
        eventTable.unhook(SWT.KeyDown, listener);
    }

    /*
 * Returns true if the pointer's orientation was initialized in some dimension,
 * and false otherwise.
 */
    boolean resizeRectangles(int xChange, int yChange) {
        if (bounds == null)
            return false;
        boolean orientationInit = false;
        /*
	* If the cursor orientation has not been set in the orientation of
	* this change then try to set it here.
	*/
        if (xChange < 0 && ((getApi().style & SWT.LEFT) != 0) && ((cursorOrientation & SWT.RIGHT) == 0)) {
            if ((cursorOrientation & SWT.LEFT) == 0) {
                cursorOrientation |= SWT.LEFT;
                orientationInit = true;
            }
        }
        if (xChange > 0 && ((getApi().style & SWT.RIGHT) != 0) && ((cursorOrientation & SWT.LEFT) == 0)) {
            if ((cursorOrientation & SWT.RIGHT) == 0) {
                cursorOrientation |= SWT.RIGHT;
                orientationInit = true;
            }
        }
        if (yChange < 0 && ((getApi().style & SWT.UP) != 0) && ((cursorOrientation & SWT.DOWN) == 0)) {
            if ((cursorOrientation & SWT.UP) == 0) {
                cursorOrientation |= SWT.UP;
                orientationInit = true;
            }
        }
        if (yChange > 0 && ((getApi().style & SWT.DOWN) != 0) && ((cursorOrientation & SWT.UP) == 0)) {
            if ((cursorOrientation & SWT.DOWN) == 0) {
                cursorOrientation |= SWT.DOWN;
                orientationInit = true;
            }
        }
        /*
	 * If the bounds will flip about the x or y axis then apply the adjustment
	 * up to the axis (ie.- where bounds width/height becomes 0), change the
	 * cursor's orientation accordingly, and flip each Rectangle's origin (only
	 * necessary for > 1 Rectangles)
	 */
        if ((cursorOrientation & SWT.LEFT) != 0) {
            if (xChange > bounds.width) {
                if ((getApi().style & SWT.RIGHT) == 0)
                    return orientationInit;
                cursorOrientation |= SWT.RIGHT;
                cursorOrientation &= ~SWT.LEFT;
                bounds.x += bounds.width;
                xChange -= bounds.width;
                bounds.width = 0;
                if (proportions.length > 1) {
                    for (int i = 0; i < proportions.length; i++) {
                        Rectangle proportion = proportions[i];
                        proportion.x = 100 - proportion.x - proportion.width;
                    }
                }
            }
        } else if ((cursorOrientation & SWT.RIGHT) != 0) {
            if (bounds.width < -xChange) {
                if ((getApi().style & SWT.LEFT) == 0)
                    return orientationInit;
                cursorOrientation |= SWT.LEFT;
                cursorOrientation &= ~SWT.RIGHT;
                xChange += bounds.width;
                bounds.width = 0;
                if (proportions.length > 1) {
                    for (int i = 0; i < proportions.length; i++) {
                        Rectangle proportion = proportions[i];
                        proportion.x = 100 - proportion.x - proportion.width;
                    }
                }
            }
        }
        if ((cursorOrientation & SWT.UP) != 0) {
            if (yChange > bounds.height) {
                if ((getApi().style & SWT.DOWN) == 0)
                    return orientationInit;
                cursorOrientation |= SWT.DOWN;
                cursorOrientation &= ~SWT.UP;
                bounds.y += bounds.height;
                yChange -= bounds.height;
                bounds.height = 0;
                if (proportions.length > 1) {
                    for (int i = 0; i < proportions.length; i++) {
                        Rectangle proportion = proportions[i];
                        proportion.y = 100 - proportion.y - proportion.height;
                    }
                }
            }
        } else if ((cursorOrientation & SWT.DOWN) != 0) {
            if (bounds.height < -yChange) {
                if ((getApi().style & SWT.UP) == 0)
                    return orientationInit;
                cursorOrientation |= SWT.UP;
                cursorOrientation &= ~SWT.DOWN;
                yChange += bounds.height;
                bounds.height = 0;
                if (proportions.length > 1) {
                    for (int i = 0; i < proportions.length; i++) {
                        Rectangle proportion = proportions[i];
                        proportion.y = 100 - proportion.y - proportion.height;
                    }
                }
            }
        }
        // apply the bounds adjustment
        if ((cursorOrientation & SWT.LEFT) != 0) {
            bounds.x += xChange;
            bounds.width -= xChange;
        } else if ((cursorOrientation & SWT.RIGHT) != 0) {
            bounds.width += xChange;
        }
        if ((cursorOrientation & SWT.UP) != 0) {
            bounds.y += yChange;
            bounds.height -= yChange;
        } else if ((cursorOrientation & SWT.DOWN) != 0) {
            bounds.height += yChange;
        }
        Rectangle[] newRects = new Rectangle[rectangles.length];
        for (int i = 0; i < rectangles.length; i++) {
            Rectangle proportion = proportions[i];
            newRects[i] = new Rectangle(proportion.x * bounds.width / 100 + bounds.x, proportion.y * bounds.height / 100 + bounds.y, proportion.width * bounds.width / 100, proportion.height * bounds.height / 100);
        }
        rectangles = newRects;
        return orientationInit;
    }

    /**
     * Sets the <code>Cursor</code> of the Tracker.  If this cursor is <code>null</code>
     * then the cursor reverts to the default.
     *
     * @param newCursor the new <code>Cursor</code> to display
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setCursor(Cursor newCursor) {
        checkWidget();
        if (!java.util.Objects.equals(this.clientCursor, newCursor)) {
            dirty();
        }
        clientCursor = newCursor;
        if (newCursor != null) {
            ((DartDisplay) display.getImpl()).lockCursor = false;
            ((DartDisplay) display.getImpl()).lockCursor = true;
        }
    }

    /**
     * Specifies the rectangles that should be drawn, expressed relative to the parent
     * widget.  If the parent is a Display then these are screen coordinates.
     *
     * @param rectangles the bounds of the rectangles to be drawn
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the set of rectangles is null or contains a null rectangle</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setRectangles(Rectangle[] rectangles) {
        checkWidget();
        if (!java.util.Objects.equals(this.rectangles, rectangles)) {
            dirty();
        }
        if (rectangles == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        int length = rectangles.length;
        this.rectangles = new Rectangle[length];
        for (int i = 0; i < length; i++) {
            Rectangle current = rectangles[i];
            if (current == null)
                error(SWT.ERROR_NULL_ARGUMENT);
            this.rectangles[i] = new Rectangle(current.x, current.y, current.width, current.height);
        }
        proportions = computeProportions(rectangles);
    }

    /**
     * Changes the appearance of the line used to draw the rectangles.
     *
     * @param stippled <code>true</code> if rectangle should appear stippled
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setStippled(boolean stippled) {
        checkWidget();
        if (!java.util.Objects.equals(this.stippled, stippled)) {
            dirty();
        }
        this.stippled = stippled;
    }

    public Control _parent() {
        return parent;
    }

    public boolean _tracking() {
        return tracking;
    }

    public boolean _cancelled() {
        return cancelled;
    }

    public boolean _stippled() {
        return stippled;
    }

    public Cursor _clientCursor() {
        return clientCursor;
    }

    public Cursor _resizeCursor() {
        return resizeCursor;
    }

    public Rectangle[] _rectangles() {
        return rectangles;
    }

    public Rectangle[] _proportions() {
        return proportions;
    }

    public Rectangle _bounds() {
        return bounds;
    }

    public int _cursorOrientation() {
        return cursorOrientation;
    }

    public boolean _inEvent() {
        return inEvent;
    }

    public int _oldX() {
        return oldX;
    }

    public int _oldY() {
        return oldY;
    }

    protected void _hookEvents() {
        super._hookEvents();
        FlutterBridge.on(this, "Control", "Move", e -> {
            getDisplay().asyncExec(() -> {
                if (isDisposed())
                    return;
                sendEvent(SWT.Move, e);
            });
        });
        FlutterBridge.on(this, "Control", "Resize", e -> {
            getDisplay().asyncExec(() -> {
                if (isDisposed())
                    return;
                sendEvent(SWT.Resize, e);
            });
        });
        FlutterBridge.on(this, "Key", "KeyDown", e -> {
            getDisplay().asyncExec(() -> {
                if (isDisposed())
                    return;
                ControlHelper.sendFlutterKeyDown(this, e);
            });
        });
        FlutterBridge.on(this, "Key", "KeyUp", e -> {
            getDisplay().asyncExec(() -> {
                if (isDisposed())
                    return;
                sendEvent(SWT.KeyUp, e);
            });
        });
    }

    public Tracker getApi() {
        if (api == null)
            api = Tracker.createApi(this);
        return (Tracker) api;
    }

    public VTracker getValue() {
        if (value == null)
            value = new VTracker(this);
        return (VTracker) value;
    }
}
