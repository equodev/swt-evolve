/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2021 IBM Corporation and others.
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
 *      Stefan Xenos (Google) - bug 468854 - Add a requestLayout method to Control
 * *****************************************************************************
 */
package org.eclipse.swt.widgets;

import java.util.*;
import java.util.stream.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.gdip.*;
import dev.equo.swt.*;

/**
 * Control is the abstract superclass of all windowed user interface classes.
 * <dl>
 * <dt><b>Styles:</b>
 * <dd>BORDER</dd>
 * <dd>LEFT_TO_RIGHT, RIGHT_TO_LEFT, FLIP_TEXT_DIRECTION</dd>
 * <dt><b>Events:</b>
 * <dd>DragDetect, FocusIn, FocusOut, Help, KeyDown, KeyUp, MenuDetect, MouseDoubleClick, MouseDown, MouseEnter,
 *     MouseExit, MouseHover, MouseUp, MouseMove, MouseWheel, MouseHorizontalWheel, MouseVerticalWheel, Move,
 *     Paint, Resize, Traverse</dd>
 * </dl>
 * <p>
 * Only one of LEFT_TO_RIGHT or RIGHT_TO_LEFT may be specified.
 * </p><p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em>
 * within the SWT implementation.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#control">Control snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class DartControl extends DartWidget implements Drawable, IControl {

    Composite parent;

    Cursor cursor;

    Menu menu, activeMenu;

    String toolTipText;

    Object layoutData;

    Accessible accessible;

    Image backgroundImage;

    Region region;

    Font font;

    int drawCount, foreground, background, backgroundAlpha = 255;

    /**
     * Prevents uninitialized instances from being created outside the package.
     */
    DartControl(Control api) {
        super(api);
    }

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
     * @see SWT#BORDER
     * @see SWT#LEFT_TO_RIGHT
     * @see SWT#RIGHT_TO_LEFT
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartControl(Composite parent, int style, Control api) {
        super(parent, style, api);
        this.parent = parent;
        createWidget();
        ControlUtils.addToParentChildren(this);
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
     * be notified when a drag gesture occurs, by sending it
     * one of the messages defined in the <code>DragDetectListener</code>
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
     * @see DragDetectListener
     * @see #removeDragDetectListener
     *
     * @since 3.3
     */
    public void addDragDetectListener(DragDetectListener listener) {
        addTypedListener(listener, SWT.DragDetect);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the control gains or loses focus, by sending
     * it one of the messages defined in the <code>FocusListener</code>
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
     * @see FocusListener
     * @see #removeFocusListener
     */
    public void addFocusListener(FocusListener listener) {
        addTypedListener(listener, SWT.FocusIn, SWT.FocusOut);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when gesture events are generated for the control,
     * by sending it one of the messages defined in the
     * <code>GestureListener</code> interface.
     * <p>
     * NOTE: If <code>setTouchEnabled(true)</code> has previously been
     * invoked on the receiver then <code>setTouchEnabled(false)</code>
     * must be invoked on it to specify that gesture events should be
     * sent instead of touch events.
     * </p>
     * <p>
     * <b>Warning</b>: This API is currently only implemented on Windows and Cocoa.
     * SWT doesn't send Gesture or Touch events on GTK.
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
     * @see GestureListener
     * @see #removeGestureListener
     * @see #setTouchEnabled
     *
     * @since 3.7
     */
    public void addGestureListener(GestureListener listener) {
        addTypedListener(listener, SWT.Gesture);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when help events are generated for the control,
     * by sending it one of the messages defined in the
     * <code>HelpListener</code> interface.
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
     * @see HelpListener
     * @see #removeHelpListener
     */
    public void addHelpListener(HelpListener listener) {
        addTypedListener(listener, SWT.Help);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when keys are pressed and released on the system keyboard, by sending
     * it one of the messages defined in the <code>KeyListener</code>
     * interface.
     * <p>
     * When a key listener is added to a control, the control
     * will take part in widget traversal.  By default, all
     * traversal keys (such as the tab key and so on) are
     * delivered to the control.  In order for a control to take
     * part in traversal, it should listen for traversal events.
     * Otherwise, the user can traverse into a control but not
     * out.  Note that native controls such as table and tree
     * implement key traversal in the operating system.  It is
     * not necessary to add traversal listeners for these controls,
     * unless you want to override the default traversal.
     * </p>
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
    public void addMenuDetectListener(MenuDetectListener listener) {
        addTypedListener(listener, SWT.MenuDetect);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when mouse buttons are pressed and released, by sending
     * it one of the messages defined in the <code>MouseListener</code>
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
     * @see MouseListener
     * @see #removeMouseListener
     */
    public void addMouseListener(MouseListener listener) {
        addTypedListener(listener, SWT.MouseDown, SWT.MouseUp, SWT.MouseDoubleClick);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the mouse passes or hovers over controls, by sending
     * it one of the messages defined in the <code>MouseTrackListener</code>
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
     * @see MouseTrackListener
     * @see #removeMouseTrackListener
     */
    public void addMouseTrackListener(MouseTrackListener listener) {
        addTypedListener(listener, SWT.MouseEnter, SWT.MouseExit, SWT.MouseHover);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the mouse moves, by sending it one of the
     * messages defined in the <code>MouseMoveListener</code>
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
     * @see MouseMoveListener
     * @see #removeMouseMoveListener
     */
    public void addMouseMoveListener(MouseMoveListener listener) {
        addTypedListener(listener, SWT.MouseMove);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the mouse wheel is scrolled, by sending
     * it one of the messages defined in the
     * <code>MouseWheelListener</code> interface.
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
     * @see MouseWheelListener
     * @see #removeMouseWheelListener
     *
     * @since 3.3
     */
    public void addMouseWheelListener(MouseWheelListener listener) {
        addTypedListener(listener, SWT.MouseWheel);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the receiver needs to be painted, by sending it
     * one of the messages defined in the <code>PaintListener</code>
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
     * @see PaintListener
     * @see #removePaintListener
     */
    public void addPaintListener(PaintListener listener) {
        addTypedListener(listener, SWT.Paint);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when touch events occur, by sending it
     * one of the messages defined in the <code>TouchListener</code>
     * interface.
     * <p>
     * NOTE: You must also call <code>setTouchEnabled(true)</code> to
     * specify that touch events should be sent, which will cause gesture
     * events to not be sent.
     * </p>
     * <p>
     * <b>Warning</b>: This API is currently only implemented on Windows and Cocoa.
     * SWT doesn't send Gesture or Touch events on GTK.
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
     * @see TouchListener
     * @see #removeTouchListener
     * @see #setTouchEnabled
     *
     * @since 3.7
     */
    public void addTouchListener(TouchListener listener) {
        addTypedListener(listener, SWT.Touch);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when traversal events occur, by sending it
     * one of the messages defined in the <code>TraverseListener</code>
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
     * @see TraverseListener
     * @see #removeTraverseListener
     */
    public void addTraverseListener(TraverseListener listener) {
        addTypedListener(listener, SWT.Traverse);
    }

    int binarySearch(int[] indices, int start, int end, int index) {
        int low = start, high = end - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            if (indices[mid] == index)
                return mid;
            if (indices[mid] < index) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return -low - 1;
    }

    long borderHandle() {
        return getApi().handle;
    }

    void checkBackground() {
    }

    void checkBorder() {
        if (getBorderWidthInPixels() == 0)
            getApi().style &= ~SWT.BORDER;
    }

    void checkBuffered() {
        getApi().style &= ~SWT.DOUBLE_BUFFERED;
    }

    void checkComposited() {
        /* Do nothing */
    }

    void checkMirrored() {
        if ((getApi().style & SWT.RIGHT_TO_LEFT) != 0) {
        }
    }

    /**
     * Returns the preferred size (in points) of the receiver.
     * <p>
     * The <em>preferred size</em> of a control is the size that it would
     * best be displayed at. The width hint and height hint arguments
     * allow the caller to ask a control questions such as "Given a particular
     * width, how high does the control need to be to show all of the contents?"
     * To indicate that the caller does not wish to constrain a particular
     * dimension, the constant <code>SWT.DEFAULT</code> is passed for the hint.
     * </p>
     *
     * @param wHint the width hint (can be <code>SWT.DEFAULT</code>)
     * @param hHint the height hint (can be <code>SWT.DEFAULT</code>)
     * @return the preferred size of the control
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Layout
     * @see #getBorderWidth
     * @see #getBounds
     * @see #getSize
     * @see #pack(boolean)
     * @see "computeTrim, getClientArea for controls that implement them"
     */
    public Point computeSize(int wHint, int hHint) {
        return computeSize(wHint, hHint, true);
    }

    /**
     * Returns the preferred size (in points) of the receiver.
     * <p>
     * The <em>preferred size</em> of a control is the size that it would
     * best be displayed at. The width hint and height hint arguments
     * allow the caller to ask a control questions such as "Given a particular
     * width, how high does the control need to be to show all of the contents?"
     * To indicate that the caller does not wish to constrain a particular
     * dimension, the constant <code>SWT.DEFAULT</code> is passed for the hint.
     * </p><p>
     * If the changed flag is <code>true</code>, it indicates that the receiver's
     * <em>contents</em> have changed, therefore any caches that a layout manager
     * containing the control may have been keeping need to be flushed. When the
     * control is resized, the changed flag will be <code>false</code>, so layout
     * manager caches can be retained.
     * </p>
     *
     * @param wHint the width hint (can be <code>SWT.DEFAULT</code>)
     * @param hHint the height hint (can be <code>SWT.DEFAULT</code>)
     * @param changed <code>true</code> if the control's contents have changed, and <code>false</code> otherwise
     * @return the preferred size of the control.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Layout
     * @see #getBorderWidth
     * @see #getBounds
     * @see #getSize
     * @see #pack(boolean)
     * @see "computeTrim, getClientArea for controls that implement them"
     */
    public Point computeSize(int wHint, int hHint, boolean changed) {
        checkWidget();
        return Sizes.compute(this);
    }

    Point computeSizeInPixels(int wHint, int hHint, boolean changed) {
        int width = DEFAULT_WIDTH;
        int height = DEFAULT_HEIGHT;
        if (wHint != SWT.DEFAULT)
            width = wHint;
        if (hHint != SWT.DEFAULT)
            height = hHint;
        int border = getBorderWidthInPixels();
        width += border * 2;
        height += border * 2;
        return new Point(width, height);
    }

    Widget computeTabGroup() {
        if (isTabGroup())
            return this.getApi();
        if (parent instanceof Decorations) {
            return parent;
        }
        return ((DartControl) parent.getImpl()).computeTabGroup();
    }

    Control computeTabRoot() {
        Control[] tabList = parent.getImpl()._getTabList();
        if (tabList != null) {
            int index = 0;
            while (index < tabList.length) {
                if (tabList[index] == this.getApi())
                    break;
                index++;
            }
            if (index == tabList.length) {
                if (isTabGroup())
                    return this.getApi();
            }
        }
        if (parent instanceof Decorations) {
            return parent;
        }
        return ((DartControl) parent.getImpl()).computeTabRoot();
    }

    public Widget[] computeTabList() {
        if (isTabGroup()) {
            if (getVisible() && getEnabled()) {
                return new Widget[] { this.getApi() };
            }
        }
        return new Widget[0];
    }

    void createHandle() {
    }

    void checkGesture() {
    }

    void createWidget() {
        getApi().state |= DRAG_DETECT;
        foreground = background = -1;
        checkOrientation(parent);
        createHandle();
        checkBackground();
        checkBuffered();
        checkComposited();
        register();
        subclass();
        //setDefaultFont();
        ;
        checkMirrored();
        checkBorder();
        checkGesture();
        if ((getApi().state & PARENT_BACKGROUND) != 0) {
            setBackground();
        }
    }

    int defaultBackground() {
        return 0;
    }

    long defaultFont() {
        return 0;
    }

    int defaultForeground() {
        return 0;
    }

    void deregister() {
        if (bridge != null)
            bridge.destroy(this);
    }

    @Override
    void destroyWidget() {
        long hwnd = topHandle();
        releaseHandle();
        if (hwnd != 0) {
        }
    }

    /**
     * Detects a drag and drop gesture.  This method is used
     * to detect a drag gesture when called from within a mouse
     * down listener.
     *
     * <p>By default, a drag is detected when the gesture
     * occurs anywhere within the client area of a control.
     * Some controls, such as tables and trees, override this
     * behavior.  In addition to the operating system specific
     * drag gesture, they require the mouse to be inside an
     * item.  Custom widget writers can use <code>setDragDetect</code>
     * to disable the default detection, listen for mouse down,
     * and then call <code>dragDetect()</code> from within the
     * listener to conditionally detect a drag.
     * </p>
     *
     * @param event the mouse down event
     *
     * @return <code>true</code> if the gesture occurred, and <code>false</code> otherwise.
     *
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_NULL_ARGUMENT if the event is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see DragDetectListener
     * @see #addDragDetectListener
     *
     * @see #getDragDetect
     * @see #setDragDetect
     *
     * @since 3.3
     */
    public boolean dragDetect(Event event) {
        checkWidget();
        if (event == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        return false;
    }

    /**
     * Detects a drag and drop gesture.  This method is used
     * to detect a drag gesture when called from within a mouse
     * down listener.
     *
     * <p>By default, a drag is detected when the gesture
     * occurs anywhere within the client area of a control.
     * Some controls, such as tables and trees, override this
     * behavior.  In addition to the operating system specific
     * drag gesture, they require the mouse to be inside an
     * item.  Custom widget writers can use <code>setDragDetect</code>
     * to disable the default detection, listen for mouse down,
     * and then call <code>dragDetect()</code> from within the
     * listener to conditionally detect a drag.
     * </p>
     *
     * @param event the mouse down event
     *
     * @return <code>true</code> if the gesture occurred, and <code>false</code> otherwise.
     *
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_NULL_ARGUMENT if the event is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see DragDetectListener
     * @see #addDragDetectListener
     *
     * @see #getDragDetect
     * @see #setDragDetect
     *
     * @since 3.3
     */
    public boolean dragDetect(MouseEvent event) {
        checkWidget();
        if (event == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        return false;
    }

    boolean dragDetect(int button, int count, int stateMask, int x, int y) {
        if (button != 1 || count != 1)
            return false;
        return sendDragEvent(button, stateMask, x, y);
    }

    void drawBackground(long hDC) {
    }

    void enableDrag(boolean enabled) {
        /* Do nothing */
    }

    void maybeEnableDarkSystemTheme() {
        maybeEnableDarkSystemTheme(getApi().handle);
    }

    void enableWidget(boolean enabled) {
    }

    public Control findBackgroundControl() {
        return parent.getImpl().findBackgroundControl();
    }

    public long findBrush(long value, int lbStyle) {
        return parent.getImpl().findBrush(value, lbStyle);
    }

    public Cursor findCursor() {
        if (cursor != null)
            return cursor;
        return parent.getImpl().findCursor();
    }

    Control findImageControl() {
        Control control = findBackgroundControl();
        return control != null && control.getImpl()._backgroundImage() != null ? control : null;
    }

    public Control findThemeControl() {
        return background == -1 && backgroundImage == null ? parent.getImpl().findThemeControl() : null;
    }

    public Menu[] findMenus(Control control) {
        if (menu != null && this.getApi() != control)
            return new Menu[] { menu };
        return new Menu[0];
    }

    char findMnemonic(String string) {
        int index = 0;
        int length = string.length();
        do {
            while (index < length && string.charAt(index) != '&') index++;
            if (++index >= length)
                return '\0';
            if (string.charAt(index) != '&')
                return string.charAt(index);
            index++;
        } while (index < length);
        return '\0';
    }

    public void fixChildren(Shell newShell, Shell oldShell, Decorations newDecorations, Decorations oldDecorations, Menu[] menus) {
        ((SwtShell) oldShell.getImpl()).fixShell(newShell, this.getApi());
        ((SwtDecorations) oldDecorations.getImpl()).fixDecorations(newDecorations, this.getApi(), menus);
    }

    void fixFocus(Control focusControl) {
        Shell shell = getShell();
        Control control = this.getApi();
        while (control != shell && (control = control.getImpl()._parent()) != null) {
            if (control.setFocus())
                return;
        }
        ((SwtDecorations) shell.getImpl()).setSavedFocus(focusControl);
    }

    /**
     * Forces the receiver to have the <em>keyboard focus</em>, causing
     * all keyboard events to be delivered to it.
     *
     * @return <code>true</code> if the control got focus, and <code>false</code> if it was unable to.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setFocus
     */
    public boolean forceFocus() {
        checkWidget();
        if (((SwtDisplay) display.getImpl()).focusEvent == SWT.FocusOut)
            return false;
        Decorations shell = menuShell();
        ((SwtDecorations) shell.getImpl()).setSavedFocus(this.getApi());
        if (!isEnabled() || !isVisible() || !isActive())
            return false;
        if (display.getActiveShell() != shell && !SwtDisplay.isActivateShellOnForceFocus())
            return false;
        if (isFocusControl())
            return true;
        getBridge().setFocus(this);
        ((SwtDecorations) shell.getImpl()).setSavedFocus(null);
        /*
	* This code is intentionally commented.
	*
	* When setting focus to a control, it is
	* possible that application code can set
	* the focus to another control inside of
	* WM_SETFOCUS.  In this case, the original
	* control will no longer have the focus
	* and the call to setFocus() will return
	* false indicating failure.
	*
	* We are still working on a solution at
	* this time.
	*/
        if (isDisposed())
            return false;
        ((SwtDecorations) shell.getImpl()).setSavedFocus(this.getApi());
        return isFocusControl();
    }

    void forceResize() {
        if (parent == null)
            return;
    }

    /**
     * Returns the accessible object for the receiver.
     * <p>
     * If this is the first time this object is requested,
     * then the object is created and returned. The object
     * returned by getAccessible() does not need to be disposed.
     * </p>
     *
     * @return the accessible object
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Accessible#addAccessibleListener
     * @see Accessible#addAccessibleControlListener
     *
     * @since 2.0
     */
    public Accessible getAccessible() {
        checkWidget();
        if (accessible == null)
            accessible = new_Accessible(this.getApi());
        return accessible;
    }

    /**
     * Returns the receiver's background color.
     * <p>
     * Note: This operation is a hint and may be overridden by the platform.
     * For example, on some versions of Windows the background of a TabFolder,
     * is a gradient rather than a solid color.
     * </p>
     * @return the background color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Color getBackground() {
        checkWidget();
        if (background != -1) {
            return _background;
        }
        if (backgroundAlpha != 0) {
            Control control = findBackgroundControl();
            if (control == null)
                control = this.getApi();
            if (control != null && control != this.getApi()) {
                return control.getBackground();
            }
        }
        return _background;
    }

    /**
     * Returns the receiver's background image.
     *
     * @return the background image
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    public Image getBackgroundImage() {
        checkWidget();
        Control control = findBackgroundControl();
        if (control == null)
            control = this.getApi();
        return control.getImpl()._backgroundImage();
    }

    public int getBackgroundPixel() {
        return background != -1 ? background : defaultBackground();
    }

    /**
     * Returns the receiver's border width in points.
     *
     * @return the border width
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getBorderWidth() {
        checkWidget();
        return DPIUtil.pixelToPoint(getBorderWidthInPixels(), getZoom());
    }

    int getBorderWidthInPixels() {
        return 0;
    }

    /**
     * Returns a rectangle describing the receiver's size and location in points
     * relative to its parent (or its display if its parent is null),
     * unless the receiver is a shell. In this case, the location is
     * relative to the display.
     *
     * @return the receiver's bounding rectangle
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Rectangle getBounds() {
        checkWidget();
        return this.bounds;
    }

    Rectangle getBoundsInPixels() {
        forceResize();
        return this.bounds;
    }

    int getCodePage() {
        return 0;
    }

    String getClipboardText() {
        String string = "";
        return string;
    }

    /**
     * Returns the receiver's cursor, or null if it has not been set.
     * <p>
     * When the mouse pointer passes over a control its appearance
     * is changed to match the control's cursor.
     * </p>
     *
     * @return the receiver's cursor or <code>null</code>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.3
     */
    public Cursor getCursor() {
        checkWidget();
        return cursor;
    }

    /**
     * Returns <code>true</code> if the receiver is detecting
     * drag gestures, and  <code>false</code> otherwise.
     *
     * @return the receiver's drag detect state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.3
     */
    public boolean getDragDetect() {
        checkWidget();
        return (getApi().state & DRAG_DETECT) != 0;
    }

    boolean getDrawing() {
        return drawCount <= 0;
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
        return this.enabled;
    }

    /**
     * Returns the font that the receiver will use to paint textual information.
     *
     * @return the receiver's font
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Font getFont() {
        checkWidget();
        if (font != null)
            return font;
        return display.getSystemFont();
    }

    /**
     * Returns the foreground color that the receiver will use to draw.
     *
     * @return the receiver's foreground color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Color getForeground() {
        checkWidget();
        return this._foreground;
    }

    int getForegroundPixel() {
        return foreground != -1 ? foreground : defaultForeground();
    }

    /**
     * Returns layout data which is associated with the receiver.
     *
     * @return the receiver's layout data
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Object getLayoutData() {
        checkWidget();
        return layoutData;
    }

    /**
     * Returns a point describing the receiver's location relative to its parent in
     * points (or its display if its parent is null), unless the receiver is a
     * shell. In this case, the point is usually relative to the display.
     * <p>
     * <b>Warning:</b> When executing this operation on a shell, it may not yield a
     * value with the expected meaning on some platforms. For example, executing
     * this operation on a shell when the environment uses the Wayland protocol, the
     * result is <b>not</b> a coordinate relative to the display. It will not change
     * when moving the shell.
     *
     * @return the receiver's location
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Point getLocation() {
        checkWidget();
        return new Point(bounds.x, bounds.y);
    }

    Point getLocationInPixels() {
        forceResize();
        return new Point(bounds.x, bounds.y);
    }

    /**
     * Returns the receiver's pop up menu if it has one, or null
     * if it does not. All controls may optionally have a pop up
     * menu that is displayed when the user requests one for
     * the control. The sequence of key strokes, button presses
     * and/or button releases that are used to request a pop up
     * menu is platform specific.
     *
     * @return the receiver's menu
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    @Override
    public Menu getMenu() {
        checkWidget();
        return menu;
    }

    /**
     * Returns the receiver's monitor.
     *
     * @return the receiver's monitor
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    public Monitor getMonitor() {
        checkWidget();
        return display.getPrimaryMonitor();
    }

    /**
     * Returns the orientation of the receiver, which will be one of the
     * constants <code>SWT.LEFT_TO_RIGHT</code> or <code>SWT.RIGHT_TO_LEFT</code>.
     *
     * @return the orientation style
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.7
     */
    public int getOrientation() {
        checkWidget();
        return getApi().style & (SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT);
    }

    /**
     * Returns the receiver's parent, which must be a <code>Composite</code>
     * or null when the receiver is a shell that was created with null or
     * a display for a parent.
     *
     * @return the receiver's parent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Composite getParent() {
        checkWidget();
        return parent;
    }

    public Control[] getPath() {
        int count = 0;
        Shell shell = getShell();
        Control control = this.getApi();
        while (control != shell) {
            count++;
            control = control.getImpl()._parent();
        }
        control = this.getApi();
        Control[] result = new Control[count];
        while (control != shell) {
            result[--count] = control;
            control = control.getImpl()._parent();
        }
        return result;
    }

    /**
     * Returns the region that defines the shape of the control,
     * or null if the control has the default shape.
     *
     * @return the region that defines the shape of the shell (or null)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    public Region getRegion() {
        checkWidget();
        return region;
    }

    /**
     * Returns the receiver's shell. For all controls other than
     * shells, this simply returns the control's nearest ancestor
     * shell. Shells return themselves, even if they are children
     * of other shells.
     *
     * @return the receiver's shell
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #getParent
     */
    public Shell getShell() {
        checkWidget();
        return parent.getShell();
    }

    /**
     * Returns a point describing the receiver's size in points. The
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
        return new Point(bounds.width, bounds.height);
    }

    public Point getSizeInPixels() {
        forceResize();
        return new Point(bounds.width, bounds.height);
    }

    /**
     * Calculates a slightly different color, e.g. for the hot state of a button.
     * @param pixel the color to start with
     */
    int getSlightlyDifferentColor(int pixel) {
        return getDifferentColor(pixel, 0.1);
    }

    /**
     * Calculates a different color, e.g. for the checked state of a toggle button
     * or to highlight a selected button.
     * @param pixel the color to start with
     */
    int getDifferentColor(int pixel) {
        return getDifferentColor(pixel, 0.2);
    }

    /**
     * @param factor must be between [0..1]. The bounds are not checked
     */
    int getDifferentColor(int pixel, double factor) {
        int red = pixel & 0xFF;
        int green = (pixel & 0xFF00) >> 8;
        int blue = (pixel & 0xFF0000) >> 16;
        red += calcDiff(red, factor);
        green += calcDiff(green, factor);
        blue += calcDiff(blue, factor);
        return (red & 0xFF) | ((green & 0xFF) << 8) | ((blue & 0xFF) << 16);
    }

    long /* int */
    calcDiff(int component, double factor) {
        if (component > 127) {
            return Math.round(component * -1 * factor);
        } else {
            return Math.round((255 - component) * factor);
        }
    }

    /**
     * Calculates a slightly different background color, e.g. for highlighting the sort column
     * in a table or tree. This method produces less contrast that {@link #getSlightlyDifferentColor(int)}.
     * @param pixel the color to start with
     */
    int getSlightlyDifferentBackgroundColor(int pixel) {
        int offset = 8;
        int red = pixel & 0xFF;
        int green = (pixel & 0xFF00) >> 8;
        int blue = (pixel & 0xFF0000) >> 16;
        red = red > 127 ? red - offset : red + offset;
        green = green > 127 ? green - offset : green + offset;
        blue = blue > 127 ? blue - offset : blue + offset;
        return (red & 0xFF) | ((green & 0xFF) << 8) | ((blue & 0xFF) << 16);
    }

    /**
     * Returns the text direction of the receiver, which will be one of the
     * constants <code>SWT.LEFT_TO_RIGHT</code> or <code>SWT.RIGHT_TO_LEFT</code>.
     *
     * @return the text direction style
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.102
     */
    public int getTextDirection() {
        checkWidget();
        if (this.textDirection == AUTO_TEXT_DIRECTION) {
            int resolved = resolveTextDirection();
            if (resolved == SWT.NONE) {
                return getOrientation();
            }
            return resolved;
        }
        return this.textDirection;
    }

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
    public String getToolTipText() {
        checkWidget();
        return toolTipText;
    }

    /**
     * Returns <code>true</code> if this control is set to send touch events, or
     * <code>false</code> if it is set to send gesture events instead.  This method
     * also returns <code>false</code> if a touch-based input device is not detected
     * (this can be determined with <code>Display#getTouchEnabled()</code>).  Use
     * {@link #setTouchEnabled(boolean)} to switch the events that a control sends
     * between touch events and gesture events.
     *
     * @return <code>true</code> if the control is set to send touch events, or <code>false</code> otherwise
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setTouchEnabled
     * @see Display#getTouchEnabled
     *
     * @since 3.7
     */
    public boolean getTouchEnabled() {
        checkWidget();
        return this.touchEnabled;
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
        if (!getDrawing())
            return (getApi().state & HIDDEN) == 0;
        return this.visible;
    }

    boolean hasCursor() {
        return false;
    }

    boolean hasCustomBackground() {
        return background != -1;
    }

    boolean hasCustomForeground() {
        return foreground != -1;
    }

    boolean hasFocus() {
        return getBridge().hasFocus(this);
    }

    /**
     * Invokes platform specific functionality to allocate a new GC handle.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
     * API for <code>Control</code>. It is marked public only so that it
     * can be shared within the packages provided by SWT. It is not
     * available on all platforms, and should never be called from
     * application code.
     * </p>
     *
     * @param data the platform specific GC data
     * @return the platform specific GC handle
     *
     * @noreference This method is not intended to be referenced by clients.
     */
    @Override
    public long internal_new_GC(GCData data) {
        return 0;
    }

    /**
     * Invokes platform specific functionality to dispose a GC handle.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
     * API for <code>Control</code>. It is marked public only so that it
     * can be shared within the packages provided by SWT. It is not
     * available on all platforms, and should never be called from
     * application code.
     * </p>
     *
     * @param hDC the platform specific GC handle
     * @param data the platform specific GC data
     *
     * @noreference This method is not intended to be referenced by clients.
     */
    @Override
    public void internal_dispose_GC(long hDC, GCData data) {
        checkWidget();
        long hwnd = getApi().handle;
        if (data != null && data.hwnd != 0) {
            hwnd = data.hwnd;
        }
    }

    public boolean isActive() {
        Dialog dialog = ((SwtDisplay) display.getImpl()).getModalDialog();
        if (dialog != null) {
            Shell dialogShell = ((SwtDialog) dialog.getImpl()).parent;
            if (dialogShell != null && !dialogShell.isDisposed()) {
                if (dialogShell != getShell())
                    return false;
            }
        }
        Shell shell = null;
        Shell[] modalShells = ((SwtDisplay) display.getImpl()).modalShells;
        if (modalShells != null) {
            int bits = SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL;
            int index = modalShells.length;
            while (--index >= 0) {
                Shell modal = modalShells[index];
                if (modal != null) {
                    if ((modal.style & bits) != 0) {
                        Control control = this.getApi();
                        while (control != null) {
                            if (control == modal)
                                break;
                            control = control.getImpl()._parent();
                        }
                        if (control != modal)
                            return false;
                        break;
                    }
                    if ((modal.style & SWT.PRIMARY_MODAL) != 0) {
                        if (shell == null)
                            shell = getShell();
                        if (modal.getImpl()._parent() == shell)
                            return false;
                    }
                }
            }
        }
        if (shell == null)
            shell = getShell();
        return shell.getEnabled();
    }

    /**
     * Returns <code>true</code> if the receiver is enabled and all
     * ancestors up to and including the receiver's nearest ancestor
     * shell are enabled.  Otherwise, <code>false</code> is returned.
     * A disabled control is typically not selectable from the user
     * interface and draws with an inactive or "grayed" look.
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
     * Returns <code>true</code> if the receiver has the user-interface
     * focus, and <code>false</code> otherwise.
     *
     * @return the receiver's focus state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean isFocusControl() {
        checkWidget();
        Control focusControl = ((SwtDisplay) display.getImpl()).focusControl;
        if (focusControl != null && !focusControl.isDisposed()) {
            return this.getApi() == focusControl;
        }
        return hasFocus();
    }

    boolean isFocusAncestor(Control control) {
        while (control != null && control != this.getApi() && !(control instanceof Shell)) {
            control = control.getImpl()._parent();
        }
        return control == this.getApi();
    }

    /**
     * Returns <code>true</code> if the underlying operating
     * system supports this reparenting, otherwise <code>false</code>
     *
     * @return <code>true</code> if the widget can be reparented, otherwise <code>false</code>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean isReparentable() {
        checkWidget();
        return true;
    }

    boolean isShowing() {
        /*
	* This is not complete.  Need to check if the
	* widget is obscured by a parent or sibling.
	*/
        if (!isVisible())
            return false;
        Control control = this.getApi();
        while (control != null) {
            Point size = control.getImpl().getSizeInPixels();
            if (size.x == 0 || size.y == 0) {
                return false;
            }
            control = control.getImpl()._parent();
        }
        return true;
        /*
	* Check to see if current damage is included.
	*/
        //	if (!OS.IsWindowVisible (handle)) return false;
        //	int flags = OS.DCX_CACHE | OS.DCX_CLIPCHILDREN | OS.DCX_CLIPSIBLINGS;
        //	long hDC = OS.GetDCEx (handle, 0, flags);
        //	int result = OS.GetClipBox (hDC, new RECT ());
        //	OS.ReleaseDC (handle, hDC);
        //	return result != OS.NULLREGION;
    }

    public boolean isTabGroup() {
        Control[] tabList = parent.getImpl()._getTabList();
        if (tabList != null) {
            for (Control element : tabList) {
                if (element == this.getApi())
                    return true;
            }
        }
        return false;
    }

    public boolean isTabItem() {
        Control[] tabList = parent.getImpl()._getTabList();
        if (tabList != null) {
            for (Control element : tabList) {
                if (element == this.getApi())
                    return false;
            }
        }
        return true;
    }

    /**
     * Returns <code>true</code> if the receiver is visible and all
     * ancestors up to and including the receiver's nearest ancestor
     * shell are visible. Otherwise, <code>false</code> is returned.
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

    /**
     * Custom theming: whether to use WS_BORDER instead of WS_EX_CLIENTEDGE for SWT.BORDER
     * Intended for override.
     */
    boolean isUseWsBorder() {
        return (display != null) && ((SwtDisplay) display.getImpl()).useWsBorderAll;
    }

    public void markLayout(boolean changed, boolean all) {
        /* Do nothing */
    }

    public Decorations menuShell() {
        return parent.getImpl().menuShell();
    }

    boolean mnemonicHit(char key) {
        return false;
    }

    boolean mnemonicMatch(char key) {
        return false;
    }

    /**
     * Moves the receiver above the specified control in the
     * drawing order. If the argument is null, then the receiver
     * is moved to the top of the drawing order. The control at
     * the top of the drawing order will not be covered by other
     * controls even if they occupy intersecting areas.
     *
     * @param control the sibling control (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the control has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Control#moveBelow
     * @see Composite#getChildren
     */
    public void moveAbove(Control control) {
        checkWidget();
        long topHandle = topHandle();
        if (control != null) {
            if (control.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
            if (parent != control.getImpl()._parent())
                return;
            long hwnd = control.getImpl().topHandle();
            if (hwnd == 0 || hwnd == topHandle)
                return;
        }
    }

    /**
     * Moves the receiver below the specified control in the
     * drawing order. If the argument is null, then the receiver
     * is moved to the bottom of the drawing order. The control at
     * the bottom of the drawing order will be covered by all other
     * controls which occupy intersecting areas.
     *
     * @param control the sibling control (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the control has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Control#moveAbove
     * @see Composite#getChildren
     */
    public void moveBelow(Control control) {
        checkWidget();
        if (control != null) {
            if (control.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
            if (parent != control.getImpl()._parent())
                return;
        } else {
            /*
		* Bug in Windows.  When SetWindowPos() is called
		* with HWND_BOTTOM on a dialog shell, the dialog
		* and the parent are moved to the bottom of the
		* desktop stack.  The fix is to move the dialog
		* to the bottom of the dialog window stack by
		* moving behind the first dialog child.
		*/
            Shell shell = getShell();
            if (this.getApi() == shell && parent != null) {
            }
        }
    }

    Accessible new_Accessible(Control control) {
        return SwtAccessible.internal_new_Accessible(this.getApi());
    }

    @Override
    GC new_GC(GCData data) {
        return null;
    }

    /**
     * Causes the receiver to be resized to its preferred size.
     * For a composite, this involves computing the preferred size
     * from its layout, if there is one.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #computeSize(int, int, boolean)
     */
    public void pack() {
        checkWidget();
        pack(true);
    }

    /**
     * Causes the receiver to be resized to its preferred size.
     * For a composite, this involves computing the preferred size
     * from its layout, if there is one.
     * <p>
     * If the changed flag is <code>true</code>, it indicates that the receiver's
     * <em>contents</em> have changed, therefore any caches that a layout manager
     * containing the control may have been keeping need to be flushed. When the
     * control is resized, the changed flag will be <code>false</code>, so layout
     * manager caches can be retained.
     * </p>
     *
     * @param changed whether or not the receiver's contents have changed
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #computeSize(int, int, boolean)
     */
    public void pack(boolean changed) {
        checkWidget();
        /*
	 * Since computeSize is overridden by Custom classes like CCombo
	 * etc... hence we cannot call computeSizeInPixels directly.
	 */
        setSize(computeSize(SWT.DEFAULT, SWT.DEFAULT, changed));
    }

    /**
     * Prints the receiver and all children.
     *
     * @param gc the gc where the drawing occurs
     * @return <code>true</code> if the operation was successful and <code>false</code> otherwise
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the gc is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the gc has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    public boolean print(GC gc) {
        checkWidget();
        if (gc == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (gc.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        long gdipGraphics = gc.getGCData().gdipGraphics;
        if (gdipGraphics != 0) {
            long clipRgn = 0;
            float[] lpXform = null;
            if (lpXform != null) {
            }
            if (clipRgn != 0) {
            }
        }
        if (gdipGraphics != 0) {
        }
        return true;
    }

    /**
     * Requests that this control and all of its ancestors be repositioned by
     * their layouts at the earliest opportunity. This should be invoked after
     * modifying the control in order to inform any dependent layouts of
     * the change.
     * <p>
     * The control will not be repositioned synchronously. This method is
     * fast-running and only marks the control for future participation in
     * a deferred layout.
     * <p>
     * Invoking this method multiple times before the layout occurs is an
     * inexpensive no-op.
     *
     * @since 3.105
     */
    public void requestLayout() {
        getShell().layout(new Control[] { this.getApi() }, SWT.DEFER);
    }

    /**
     * Causes the entire bounds of the receiver to be marked
     * as needing to be redrawn. The next time a paint request
     * is processed, the control will be completely painted,
     * including the background.
     * <p>
     * Schedules a paint request if the invalidated area is visible
     * or becomes visible later. It is not necessary for the caller
     * to explicitly call {@link #update()} after calling this method,
     * but depending on the platform, the automatic repaints may be
     * delayed considerably.
     * </p>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #update()
     * @see PaintListener
     * @see SWT#Paint
     * @see SWT#NO_BACKGROUND
     * @see SWT#NO_REDRAW_RESIZE
     * @see SWT#NO_MERGE_PAINTS
     * @see SWT#DOUBLE_BUFFERED
     */
    public void redraw() {
        checkWidget();
    }

    /**
     * Causes the rectangular area of the receiver specified by
     * the arguments to be marked as needing to be redrawn.
     * The next time a paint request is processed, that area of
     * the receiver will be painted, including the background.
     * If the <code>all</code> flag is <code>true</code>, any
     * children of the receiver which intersect with the specified
     * area will also paint their intersecting areas. If the
     * <code>all</code> flag is <code>false</code>, the children
     * will not be painted.
     * <p>
     * Schedules a paint request if the invalidated area is visible
     * or becomes visible later. It is not necessary for the caller
     * to explicitly call {@link #update()} after calling this method,
     * but depending on the platform, the automatic repaints may be
     * delayed considerably.
     * </p>
     *
     * @param x the x coordinate of the area to draw
     * @param y the y coordinate of the area to draw
     * @param width the width of the area to draw
     * @param height the height of the area to draw
     * @param all <code>true</code> if children should redraw, and <code>false</code> otherwise
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #update()
     * @see PaintListener
     * @see SWT#Paint
     * @see SWT#NO_BACKGROUND
     * @see SWT#NO_REDRAW_RESIZE
     * @see SWT#NO_MERGE_PAINTS
     * @see SWT#DOUBLE_BUFFERED
     */
    public void redraw(int x, int y, int width, int height, boolean all) {
        checkWidget();
        if (width <= 0 || height <= 0)
            return;
    }

    boolean redrawChildren() {
        Control control = findBackgroundControl();
        if (control == null) {
            if ((getApi().state & THEME_BACKGROUND) != 0) {
            }
        } else {
            if (control.getImpl()._backgroundImage() != null) {
                return true;
            }
        }
        return false;
    }

    void register() {
        super.register();
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
        getApi().handle = 0;
        parent = null;
    }

    @Override
    void releaseParent() {
        parent.getImpl().removeControl(this.getApi());
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        if (toolTipText != null) {
            setToolTipText(getShell(), null);
        }
        toolTipText = null;
        if (menu != null && !menu.isDisposed()) {
            menu.dispose();
        }
        backgroundImage = null;
        menu = null;
        cursor = null;
        unsubclass();
        deregister();
        layoutData = null;
        if (accessible != null) {
            accessible.internal_dispose_Accessible();
        }
        accessible = null;
        region = null;
        font = null;
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
        eventTable.unhook(SWT.Move, listener);
        eventTable.unhook(SWT.Resize, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when a drag gesture occurs.
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
     * @see DragDetectListener
     * @see #addDragDetectListener
     *
     * @since 3.3
     */
    public void removeDragDetectListener(DragDetectListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.DragDetect, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the control gains or loses focus.
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
     * @see FocusListener
     * @see #addFocusListener
     */
    public void removeFocusListener(FocusListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.FocusIn, listener);
        eventTable.unhook(SWT.FocusOut, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when gesture events are generated for the control.
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
     * @see GestureListener
     * @see #addGestureListener
     *
     * @since 3.7
     */
    public void removeGestureListener(GestureListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Gesture, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the help events are generated for the control.
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
     * @see HelpListener
     * @see #addHelpListener
     */
    public void removeHelpListener(HelpListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Help, listener);
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
    public void removeMenuDetectListener(MenuDetectListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.MenuDetect, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the mouse passes or hovers over controls.
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
     * @see MouseTrackListener
     * @see #addMouseTrackListener
     */
    public void removeMouseTrackListener(MouseTrackListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.MouseEnter, listener);
        eventTable.unhook(SWT.MouseExit, listener);
        eventTable.unhook(SWT.MouseHover, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when mouse buttons are pressed and released.
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
     * @see MouseListener
     * @see #addMouseListener
     */
    public void removeMouseListener(MouseListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.MouseDown, listener);
        eventTable.unhook(SWT.MouseUp, listener);
        eventTable.unhook(SWT.MouseDoubleClick, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the mouse moves.
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
     * @see MouseMoveListener
     * @see #addMouseMoveListener
     */
    public void removeMouseMoveListener(MouseMoveListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.MouseMove, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the mouse wheel is scrolled.
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
     * @see MouseWheelListener
     * @see #addMouseWheelListener
     *
     * @since 3.3
     */
    public void removeMouseWheelListener(MouseWheelListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.MouseWheel, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the receiver needs to be painted.
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
     * @see PaintListener
     * @see #addPaintListener
     */
    public void removePaintListener(PaintListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Paint, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when touch events occur.
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
     * @see TouchListener
     * @see #addTouchListener
     *
     * @since 3.7
     */
    public void removeTouchListener(TouchListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Touch, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when traversal events occur.
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
     * @see TraverseListener
     * @see #addTraverseListener
     */
    public void removeTraverseListener(TraverseListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Traverse, listener);
    }

    int resolveTextDirection() {
        /*
	 * For generic Controls do nothing here. Text-enabled Controls will resolve
	 * AUTO text direction according to their text content.
	 */
        return SWT.NONE;
    }

    void showWidget(boolean visible) {
    }

    @Override
    public boolean sendFocusEvent(int type) {
        Shell shell = getShell();
        /*
	* Feature in Windows.  During the processing of WM_KILLFOCUS,
	* when the focus window is queried using GetFocus(), it has
	* already been assigned to the new window.  The fix is to
	* remember the control that is losing or gaining focus and
	* answer it during WM_KILLFOCUS.  If a WM_SETFOCUS occurs
	* during WM_KILLFOCUS, the focus control needs to be updated
	* to the current control.  At any other time, the focus
	* control matches Windows.
	*/
        Display display = this.display;
        ((SwtDisplay) display.getImpl()).focusEvent = type;
        ((SwtDisplay) display.getImpl()).focusControl = this.getApi();
        sendEvent(type);
        // widget could be disposed at this point
        ((SwtDisplay) display.getImpl()).focusEvent = SWT.None;
        ((SwtDisplay) display.getImpl()).focusControl = null;
        /*
	* It is possible that the shell may be
	* disposed at this point.  If this happens
	* don't send the activate and deactivate
	* events.
	*/
        if (!shell.isDisposed()) {
            switch(type) {
                case SWT.FocusIn:
                    ((SwtShell) shell.getImpl()).setActiveControl(this.getApi());
                    break;
                case SWT.FocusOut:
                    if (shell != display.getActiveShell()) {
                        ((SwtShell) shell.getImpl()).setActiveControl(null);
                    }
                    break;
            }
        }
        return true;
    }

    void sendMove() {
        sendEvent(SWT.Move);
    }

    void sendResize() {
        sendEvent(SWT.Resize);
    }

    void setBackground() {
        Control control = findBackgroundControl();
        if (control == null)
            control = this.getApi();
        if (control.getImpl()._backgroundImage() != null) {
            Shell shell = getShell();
            ((SwtShell) shell.getImpl()).releaseBrushes();
        } else {
            setBackgroundPixel(control.getImpl()._background() == -1 ? ((DartControl) control.getImpl()).defaultBackground() : control.getImpl()._background());
        }
    }

    /**
     * Sets the receiver's background color to the color specified
     * by the argument, or to the default system color for the control
     * if the argument is null.
     * <p>
     * Note: This operation is a hint and may be overridden by the platform.
     * </p>
     * <p>
     * Note: The background color can be overridden by setting a background image.
     * </p>
     * @param color the new color (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setBackground(Color color) {
        checkWidget();
        _setBackground(color);
        if (color != null) {
            this.updateBackgroundMode();
        }
    }

    private void _setBackground(Color color) {
        Color newValue = color;
        if (!java.util.Objects.equals(this._background, newValue)) {
            dirty();
        }
        int pixel = -1;
        int alpha = 255;
        if (color != null) {
            if (color.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
            pixel = color.handle;
            alpha = color.getAlpha();
        }
        if (pixel == background && alpha == backgroundAlpha)
            return;
        background = pixel;
        backgroundAlpha = alpha;
        this._background = newValue;
        updateBackgroundColor();
    }

    /**
     * Sets the receiver's background image to the image specified
     * by the argument, or to the default system color for the control
     * if the argument is null.  The background image is tiled to fill
     * the available space.
     * <p>
     * Note: This operation is a hint and may be overridden by the platform.
     * For example, on Windows the background of a Button cannot be changed.
     * </p>
     * <p>
     * Note: Setting a background image overrides a set background color.
     * </p>
     * @param image the new image (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument is not a bitmap</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    public void setBackgroundImage(Image image) {
        checkWidget();
        if (!java.util.Objects.equals(this.backgroundImage, image)) {
            dirty();
        }
        if (image != null) {
            if (image.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
            if (image.type != SWT.BITMAP)
                error(SWT.ERROR_INVALID_ARGUMENT);
        }
        if (backgroundImage == image && backgroundAlpha > 0)
            return;
        backgroundAlpha = 255;
        backgroundImage = image;
        Shell shell = getShell();
        ((SwtShell) shell.getImpl()).releaseBrushes();
        updateBackgroundImage();
    }

    void setBackgroundImage(long hBitmap) {
        dirty();
    }

    void setBackgroundPixel(int pixel) {
    }

    /**
     * Sets the receiver's size and location in points to the rectangular
     * area specified by the arguments. The <code>x</code> and
     * <code>y</code> arguments are relative to the receiver's
     * parent (or its display if its parent is null), unless
     * the receiver is a shell. In this case, the <code>x</code>
     * and <code>y</code> arguments are relative to the display.
     * <p>
     * Note: Attempting to set the width or height of the
     * receiver to a negative number will cause that
     * value to be set to zero instead.
     * </p>
     * <p>
     * Note: On GTK, attempting to set the width or height of the
     * receiver to a number higher or equal 2^14 will cause them to be
     * set to (2^14)-1 instead.
     * </p>
     *
     * @param x the new x coordinate for the receiver
     * @param y the new y coordinate for the receiver
     * @param width the new width for the receiver
     * @param height the new height for the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setBounds(int x, int y, int width, int height) {
        dirty();
        Rectangle newValue = new Rectangle(x, y, width, height);
        this.bounds = newValue;
        setBounds(new Rectangle(x, y, width, height));
        getBridge().setBounds(this, bounds);
    }

    void setBoundsInPixels(int x, int y, int width, int height) {
    }

    void setBoundsInPixels(int x, int y, int width, int height, int flags) {
        setBoundsInPixels(x, y, width, height, flags, true);
    }

    void setBoundsInPixels(int x, int y, int width, int height, int flags, boolean defer) {
        if (findImageControl() != null) {
        } else {
        }
        if (defer && parent != null) {
            forceResize();
        }
    }

    /**
     * Sets the receiver's size and location in points to the rectangular
     * area specified by the argument. The <code>x</code> and
     * <code>y</code> fields of the rectangle are relative to
     * the receiver's parent (or its display if its parent is null).
     * <p>
     * Note: Attempting to set the width or height of the
     * receiver to a negative number will cause that
     * value to be set to zero instead.
     * </p>
     * <p>
     * Note: On GTK, attempting to set the width or height of the
     * receiver to a number higher or equal 2^14 will cause them to be
     * set to (2^14)-1 instead.
     * </p>
     *
     * @param rect the new bounds for the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setBounds(Rectangle rect) {
        Rectangle newValue = rect;
        if (!java.util.Objects.equals(this.bounds, newValue)) {
            dirty();
        }
        checkWidget();
        if (rect == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        this.bounds = newValue;
        getBridge().setBounds(this, bounds);
    }

    void setBoundsInPixels(Rectangle rect) {
        Rectangle newValue = rect;
        if (!java.util.Objects.equals(this.bounds, newValue)) {
            dirty();
        }
        this.bounds = newValue;
        setBoundsInPixels(rect.x, rect.y, rect.width, rect.height);
        getBridge().setBounds(this, bounds);
    }

    /**
     * If the argument is <code>true</code>, causes the receiver to have
     * all mouse events delivered to it until the method is called with
     * <code>false</code> as the argument.  Note that on some platforms,
     * a mouse button must currently be down for capture to be assigned.
     *
     * @param capture <code>true</code> to capture the mouse, and <code>false</code> to release it
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setCapture(boolean capture) {
        boolean newValue = capture;
        if (!java.util.Objects.equals(this.capture, newValue)) {
            dirty();
        }
        checkWidget();
        this.capture = newValue;
        if (capture) {
        } else {
        }
    }

    public void setCursor() {
    }

    /**
     * Sets the receiver's cursor to the cursor specified by the
     * argument, or to the default cursor for that kind of control
     * if the argument is null.
     * <p>
     * When the mouse pointer passes over a control its appearance
     * is changed to match the control's cursor.
     * </p>
     *
     * @param cursor the new cursor (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setCursor(Cursor cursor) {
        checkWidget();
        if (!java.util.Objects.equals(this.cursor, cursor)) {
            dirty();
        }
        if (cursor != null && cursor.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        this.cursor = cursor;
    }

    void setDefaultFont() {
    }

    /**
     * Sets the receiver's drag detect state. If the argument is
     * <code>true</code>, the receiver will detect drag gestures,
     * otherwise these gestures will be ignored.
     *
     * @param dragDetect the new drag detect state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.3
     */
    public void setDragDetect(boolean dragDetect) {
        boolean newValue = dragDetect;
        if (!java.util.Objects.equals(this.dragDetect, newValue)) {
            dirty();
        }
        checkWidget();
        if (dragDetect) {
            getApi().state |= DRAG_DETECT;
        } else {
            getApi().state &= ~DRAG_DETECT;
        }
        this.dragDetect = newValue;
        enableDrag(dragDetect);
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
        boolean newValue = enabled;
        if (!java.util.Objects.equals(this.enabled, newValue)) {
            dirty();
        }
        checkWidget();
        /*
	* Feature in Windows.  If the receiver has focus, disabling
	* the receiver causes no window to have focus.  The fix is
	* to assign focus to the first ancestor window that takes
	* focus.  If no window will take focus, set focus to the
	* desktop.
	*/
        Control control = null;
        boolean fixFocus = false;
        if (!enabled) {
            if (((SwtDisplay) display.getImpl()).focusEvent != SWT.FocusOut) {
                control = display.getFocusControl();
                fixFocus = isFocusAncestor(control);
            }
        }
        enableWidget(enabled);
        this.enabled = newValue;
        if (fixFocus)
            fixFocus(control);
    }

    /**
     * Causes the receiver to have the <em>keyboard focus</em>,
     * such that all keyboard events will be delivered to it.  Focus
     * reassignment will respect applicable platform constraints.
     *
     * @return <code>true</code> if the control got focus, and <code>false</code> if it was unable to.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #forceFocus
     */
    public boolean setFocus() {
        checkWidget();
        if ((getApi().style & SWT.NO_FOCUS) != 0)
            return false;
        return forceFocus();
    }

    /**
     * Sets the font that the receiver will use to paint textual information
     * to the font specified by the argument, or to the default font for that
     * kind of control if the argument is null.
     *
     * @param font the new font (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setFont(Font font) {
        font = GraphicsUtils.copyFont(font);
        checkWidget();
        if (!java.util.Objects.equals(this.font, font)) {
            dirty();
        }
        Font newFont = font;
        if (newFont != null) {
            if (newFont.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
            newFont = DartFont.win32_new(newFont, getNativeZoom());
        }
        long hFont = 0;
        if (newFont != null) {
            if (newFont.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
        }
        this.font = newFont;
        if (hFont == 0)
            hFont = defaultFont();
    }

    /**
     * Sets the receiver's foreground color to the color specified
     * by the argument, or to the default system color for the control
     * if the argument is null.
     * <p>
     * Note: This operation is a hint and may be overridden by the platform.
     * </p>
     * @param color the new color (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setForeground(Color color) {
        Color newValue = color;
        if (!java.util.Objects.equals(this._foreground, newValue)) {
            dirty();
        }
        checkWidget();
        int pixel = -1;
        if (color != null) {
            if (color.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
            pixel = color.handle;
        }
        if (pixel == foreground)
            return;
        foreground = pixel;
        this._foreground = newValue;
        setForegroundPixel(pixel);
    }

    void setForegroundPixel(int pixel) {
    }

    /**
     * Sets the layout data associated with the receiver to the argument.
     *
     * @param layoutData the new layout data for the receiver.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setLayoutData(Object layoutData) {
        checkWidget();
        this.layoutData = layoutData;
    }

    /**
     * Sets the receiver's location to the point specified by the arguments which
     * are relative to the receiver's parent (or its display if its parent is null),
     * unless the receiver is a shell. In this case, the point is relative to the
     * display.
     * <p>
     * <b>Warning:</b> When executing this operation on a shell, it may not have the
     * intended effect on some platforms. For example, executing this operation on a
     * shell when the environment uses the Wayland protocol, nothing will happen.
     *
     * @param x the new x coordinate for the receiver
     * @param y the new y coordinate for the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setLocation(int x, int y) {
        checkWidget();
        setLocationInPixels(x, y);
    }

    void setLocationInPixels(int x, int y) {
        dirty();
        this.bounds = new Rectangle(x, y, bounds.width, bounds.height);
        getBridge().setBounds(this, bounds);
    }

    /**
     * Sets the receiver's location to the point specified by the argument which
     * is relative to the receiver's parent (or its display if its parent is null),
     * unless the receiver is a shell. In this case, the point is relative to the
     * display.
     * <p>
     * <b>Warning:</b> When executing this operation on a shell, it may not have the
     * intended effect on some platforms. For example, executing this operation on a
     * shell when the environment uses the Wayland protocol, nothing will happen.
     *
     * @param location the new location for the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setLocation(Point location) {
        checkWidget();
        if (location == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        setLocationInPixels(location.x, location.y);
    }

    /**
     * Sets the receiver's pop up menu to the argument.
     * All controls may optionally have a pop up
     * menu that is displayed when the user requests one for
     * the control. The sequence of key strokes, button presses
     * and/or button releases that are used to request a pop up
     * menu is platform specific.
     * <p>
     * Note: Disposing of a control that has a pop up menu will
     * dispose of the menu.  To avoid this behavior, set the
     * menu to null before the control is disposed.
     * </p>
     *
     * @param menu the new pop up menu
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_MENU_NOT_POP_UP - the menu is not a pop up menu</li>
     *    <li>ERROR_INVALID_PARENT - if the menu is not in the same widget tree</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the menu has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setMenu(Menu menu) {
        checkWidget();
        if (!java.util.Objects.equals(this.menu, menu)) {
            dirty();
        }
        if (menu != null) {
            if (menu.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
            if ((menu.style & SWT.POP_UP) == 0) {
                error(SWT.ERROR_MENU_NOT_POP_UP);
            }
            if (menu.getImpl()._parent() != menuShell()) {
                error(SWT.ERROR_INVALID_PARENT);
            }
        }
        this.menu = menu;
        if (menu != null && menu.getImpl() instanceof DartMenu) {
            ((DartMenu) menu.getImpl()).ownerControl = this.getApi();
        }
    }

    /**
     * Sets the orientation of the receiver, which must be one
     * of the constants <code>SWT.LEFT_TO_RIGHT</code> or <code>SWT.RIGHT_TO_LEFT</code>.
     *
     * @param orientation new orientation style
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.7
     */
    public void setOrientation(int orientation) {
        dirty();
        checkWidget();
        int flags = SWT.RIGHT_TO_LEFT | SWT.LEFT_TO_RIGHT;
        if ((orientation & flags) == 0 || (orientation & flags) == flags)
            return;
        getApi().style &= ~SWT.MIRRORED;
        getApi().style &= ~flags;
        getApi().style |= orientation & flags;
        getApi().style &= ~SWT.FLIP_TEXT_DIRECTION;
        updateOrientation();
        checkMirrored();
    }

    public boolean setRadioFocus(boolean tabbing) {
        return false;
    }

    boolean setRadioSelection(boolean value) {
        return false;
    }

    /**
     * If the argument is <code>false</code>, causes subsequent drawing
     * operations in the receiver to be ignored. No drawing of any kind
     * can occur in the receiver until the flag is set to true.
     * Graphics operations that occurred while the flag was
     * <code>false</code> are lost. When the flag is set to <code>true</code>,
     * the entire widget is marked as needing to be redrawn.  Nested calls
     * to this method are stacked.
     * <p>
     * Note: This operation is a hint and may not be supported on some
     * platforms or for some widgets.
     * </p>
     *
     * @param redraw the new redraw state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #redraw(int, int, int, int, boolean)
     * @see #update()
     */
    public void setRedraw(boolean redraw) {
        boolean newValue = redraw;
        if (!java.util.Objects.equals(this.redraw, newValue)) {
            dirty();
        }
        checkWidget();
        /*
	 * Feature in Windows.  When WM_SETREDRAW is used to turn
	 * off drawing in a widget, it clears the WS_VISIBLE bits
	 * and then sets them when redraw is turned back on.  This
	 * means that WM_SETREDRAW will make a widget unexpectedly
	 * visible.  The fix is to track the visibility state while
	 * drawing is turned off and restore it when drawing is
	 * turned back on.
	 */
        if (drawCount == 0) {
        }
        this.redraw = newValue;
        if (redraw) {
            if (--drawCount == 0) {
                if ((getApi().state & HIDDEN) != 0) {
                    getApi().state &= ~HIDDEN;
                } else {
                }
            }
        } else {
            if (drawCount++ == 0) {
            }
        }
    }

    /**
     * Sets the shape of the control to the region specified
     * by the argument.  When the argument is null, the
     * default shape of the control is restored.
     *
     * @param region the region that defines the shape of the control (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the region has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    public void setRegion(Region region) {
        checkWidget();
        if (!java.util.Objects.equals(this.region, region)) {
            dirty();
        }
        if (region != null && region.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        if (region != null) {
        }
        this.region = region;
    }

    /**
     * Sets the receiver's size to the point specified by the arguments.
     * <p>
     * Note: Attempting to set the width or height of the
     * receiver to a negative number will cause that
     * value to be set to zero instead.
     * </p>
     * <p>
     * Note: On GTK, attempting to set the width or height of the
     * receiver to a number higher or equal 2^14 will cause them to be
     * set to (2^14)-1 instead.
     * </p>
     *
     * @param width the new width in points for the receiver
     * @param height the new height in points for the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setSize(int width, int height) {
        checkWidget();
        setSizeInPixels(width, height);
    }

    void setSizeInPixels(int width, int height) {
        if (this.bounds == null) {
            this.bounds = new Rectangle(0, 0, width, height);
        } else {
            this.bounds = new Rectangle(this.bounds.x, this.bounds.y, width, height);
        }
        dirty();
        getBridge().setBounds(this, bounds);
    }

    /**
     * Sets the receiver's size to the point specified by the argument.
     * <p>
     * Note: Attempting to set the width or height of the
     * receiver to a negative number will cause them to be
     * set to zero instead.
     * </p>
     * <p>
     * Note: On GTK, attempting to set the width or height of the
     * receiver to a number higher or equal 2^14 will cause them to be
     * set to (2^14)-1 instead.
     * </p>
     *
     * @param size the new size in points for the receiver
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setSize(Point size) {
        checkWidget();
        if (size == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        setSizeInPixels(size.x, size.y);
    }

    @Override
    public boolean setTabItemFocus() {
        if (!isShowing())
            return false;
        return forceFocus();
    }

    /**
     * Sets the base text direction (a.k.a. "paragraph direction") of the receiver,
     * which must be one of the constants <code>SWT.LEFT_TO_RIGHT</code>,
     * <code>SWT.RIGHT_TO_LEFT</code>, or <code>SWT.AUTO_TEXT_DIRECTION</code>.
     * <p>
     * <code>setOrientation</code> would override this value with the text direction
     * that is consistent with the new orientation.
     * </p>
     * <p>
     * <b>Warning</b>: This API is currently only implemented on Windows.
     * It doesn't set the base text direction on GTK and Cocoa.
     * </p>
     *
     * @param textDirection the base text direction style
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SWT#LEFT_TO_RIGHT
     * @see SWT#RIGHT_TO_LEFT
     * @see SWT#AUTO_TEXT_DIRECTION
     * @see SWT#FLIP_TEXT_DIRECTION
     *
     * @since 3.102
     */
    public void setTextDirection(int textDirection) {
        int newValue = textDirection;
        if (!java.util.Objects.equals(this.textDirection, newValue)) {
            dirty();
        }
        checkWidget();
        textDirection &= (SWT.RIGHT_TO_LEFT | SWT.LEFT_TO_RIGHT);
        updateTextDirection(textDirection);
        this.textDirection = newValue;
        if (textDirection == AUTO_TEXT_DIRECTION) {
            getApi().state |= HAS_AUTO_DIRECTION;
        } else {
            getApi().state &= ~HAS_AUTO_DIRECTION;
        }
    }

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
    public void setToolTipText(String string) {
        checkWidget();
        if (!java.util.Objects.equals(this.toolTipText, string)) {
            dirty();
        }
        if (!Objects.equals(string, toolTipText)) {
            toolTipText = string;
            setToolTipText(getShell(), string);
        }
    }

    void setToolTipText(Shell shell, String string) {
        dirty();
    }

    /**
     * Sets whether this control should send touch events (by default controls do not).
     * Setting this to <code>false</code> causes the receiver to send gesture events
     * instead.  No exception is thrown if a touch-based input device is not
     * detected (this can be determined with <code>Display#getTouchEnabled()</code>).
     *
     * @param enabled the new touch-enabled state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    </ul>
     *
     * @see Display#getTouchEnabled
     *
     * @since 3.7
     */
    public void setTouchEnabled(boolean enabled) {
        boolean newValue = enabled;
        if (!java.util.Objects.equals(this.touchEnabled, newValue)) {
            dirty();
        }
        checkWidget();
        this.touchEnabled = newValue;
        if (enabled) {
        } else {
        }
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
        boolean newValue = visible;
        if (!java.util.Objects.equals(this.visible, newValue)) {
            dirty();
        }
        checkWidget();
        if (!getDrawing()) {
            if (((getApi().state & HIDDEN) == 0) == visible)
                return;
        } else {
        }
        if (visible) {
            sendEvent(SWT.Show);
            if (isDisposed())
                return;
        }
        /*
	* Feature in Windows.  If the receiver has focus, hiding
	* the receiver causes no window to have focus.  The fix is
	* to assign focus to the first ancestor window that takes
	* focus.  If no window will take focus, set focus to the
	* desktop.
	*/
        Control control = null;
        boolean fixFocus = false;
        if (!visible) {
            if (((SwtDisplay) display.getImpl()).focusEvent != SWT.FocusOut) {
                control = display.getFocusControl();
                fixFocus = isFocusAncestor(control);
            }
        }
        if (!getDrawing()) {
            getApi().state = visible ? getApi().state & ~HIDDEN : getApi().state | HIDDEN;
        } else {
            showWidget(visible);
            if (isDisposed())
                return;
        }
        if (!visible) {
            sendEvent(SWT.Hide);
            if (isDisposed())
                return;
        }
        this.visible = newValue;
        if (fixFocus)
            fixFocus(control);
    }

    void sort(int[] items) {
        /* Shell Sort from K&R, pg 108 */
        int length = items.length;
        for (int gap = length / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < length; i++) {
                for (int j = i - gap; j >= 0; j -= gap) {
                    if (items[j] <= items[j + gap]) {
                        int swap = items[j];
                        items[j] = items[j + gap];
                        items[j + gap] = swap;
                    }
                }
            }
        }
    }

    void subclass() {
    }

    /**
     * Returns a point which is the result of converting the
     * argument, which is specified in display relative coordinates,
     * to coordinates relative to the receiver.
     * <p>
     * NOTE: To properly map a rectangle or a corner of a rectangle on a right-to-left platform, use
     * {@link Display#map(Control, Control, Rectangle)}.
     * </p>
     *
     * @param x the x coordinate in points to be translated
     * @param y the y coordinate in points to be translated
     * @return the translated coordinates
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 2.1
     */
    public Point toControl(int x, int y) {
        checkWidget();
        return new Point(x, y);
    }

    Point toControlInPixels(int x, int y) {
        return new Point(x, y);
    }

    /**
     * Returns a point which is the result of converting the
     * argument, which is specified in display relative coordinates,
     * to coordinates relative to the receiver.
     * <p>
     * NOTE: To properly map a rectangle or a corner of a rectangle on a right-to-left platform, use
     * {@link Display#map(Control, Control, Rectangle)}.
     * </p>
     *
     * @param point the point to be translated (must not be null)
     * @return the translated coordinates
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Point toControl(Point point) {
        checkWidget();
        if (point == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        return toControl(point.x, point.y);
    }

    /**
     * Returns a point which is the result of converting the
     * argument, which is specified in coordinates relative to
     * the receiver, to display relative coordinates.
     * <p>
     * NOTE: To properly map a rectangle or a corner of a rectangle on a right-to-left platform, use
     * {@link Display#map(Control, Control, Rectangle)}.
     * </p>
     *
     * @param x the x coordinate to be translated
     * @param y the y coordinate to be translated
     * @return the translated coordinates
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 2.1
     */
    public Point toDisplay(int x, int y) {
        return new Point(x, y);
    }

    Point toDisplayInPixels(int x, int y) {
        return new Point(x, y);
    }

    /**
     * Returns a point which is the result of converting the
     * argument, which is specified in coordinates relative to
     * the receiver, to display relative coordinates.
     * <p>
     * NOTE: To properly map a rectangle or a corner of a rectangle on a right-to-left platform, use
     * {@link Display#map(Control, Control, Rectangle)}.
     * </p>
     *
     * @param point the point to be translated (must not be null)
     * @return the translated coordinates
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Point toDisplay(Point point) {
        checkWidget();
        if (point == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        return toDisplay(point.x, point.y);
    }

    public long topHandle() {
        return getApi().handle;
    }

    public boolean translateMnemonic(Event event, Control control) {
        if (control == this.getApi())
            return false;
        if (!isVisible() || !isEnabled())
            return false;
        event.doit = mnemonicMatch(event.character);
        return traverse(event);
    }

    public boolean traverse(Event event) {
        /*
	* It is possible (but unlikely), that application
	* code could have disposed the widget in the traverse
	* event.  If this happens, return true to stop further
	* event processing.
	*/
        sendEvent(SWT.Traverse, event);
        if (isDisposed())
            return true;
        if (!event.doit)
            return false;
        switch(event.detail) {
            case SWT.TRAVERSE_NONE:
                return true;
            case SWT.TRAVERSE_ESCAPE:
                return traverseEscape();
            case SWT.TRAVERSE_RETURN:
                return traverseReturn();
            case SWT.TRAVERSE_TAB_NEXT:
                return traverseGroup(true);
            case SWT.TRAVERSE_TAB_PREVIOUS:
                return traverseGroup(false);
            case SWT.TRAVERSE_ARROW_NEXT:
                return traverseItem(true);
            case SWT.TRAVERSE_ARROW_PREVIOUS:
                return traverseItem(false);
            case SWT.TRAVERSE_MNEMONIC:
                return traverseMnemonic(event.character);
            case SWT.TRAVERSE_PAGE_NEXT:
                return traversePage(true);
            case SWT.TRAVERSE_PAGE_PREVIOUS:
                return traversePage(false);
        }
        return false;
    }

    /**
     * Based on the argument, perform one of the expected platform
     * traversal action. The argument should be one of the constants:
     * <code>SWT.TRAVERSE_ESCAPE</code>, <code>SWT.TRAVERSE_RETURN</code>,
     * <code>SWT.TRAVERSE_TAB_NEXT</code>, <code>SWT.TRAVERSE_TAB_PREVIOUS</code>,
     * <code>SWT.TRAVERSE_ARROW_NEXT</code>, <code>SWT.TRAVERSE_ARROW_PREVIOUS</code>,
     * <code>SWT.TRAVERSE_PAGE_NEXT</code> and <code>SWT.TRAVERSE_PAGE_PREVIOUS</code>.
     *
     * @param traversal the type of traversal
     * @return true if the traversal succeeded
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean traverse(int traversal) {
        checkWidget();
        Event event = new Event();
        event.doit = true;
        event.detail = traversal;
        return traverse(event);
    }

    /**
     * Performs a platform traversal action corresponding to a <code>KeyDown</code> event.
     *
     * <p>Valid traversal values are
     * <code>SWT.TRAVERSE_NONE</code>, <code>SWT.TRAVERSE_MNEMONIC</code>,
     * <code>SWT.TRAVERSE_ESCAPE</code>, <code>SWT.TRAVERSE_RETURN</code>,
     * <code>SWT.TRAVERSE_TAB_NEXT</code>, <code>SWT.TRAVERSE_TAB_PREVIOUS</code>,
     * <code>SWT.TRAVERSE_ARROW_NEXT</code>, <code>SWT.TRAVERSE_ARROW_PREVIOUS</code>,
     * <code>SWT.TRAVERSE_PAGE_NEXT</code> and <code>SWT.TRAVERSE_PAGE_PREVIOUS</code>.
     * If <code>traversal</code> is <code>SWT.TRAVERSE_NONE</code> then the Traverse
     * event is created with standard values based on the KeyDown event.  If
     * <code>traversal</code> is one of the other traversal constants then the Traverse
     * event is created with this detail, and its <code>doit</code> is taken from the
     * KeyDown event.
     * </p>
     *
     * @param traversal the type of traversal, or <code>SWT.TRAVERSE_NONE</code> to compute
     * this from <code>event</code>
     * @param event the KeyDown event
     *
     * @return <code>true</code> if the traversal succeeded
     *
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_NULL_ARGUMENT if the event is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.6
     */
    public boolean traverse(int traversal, Event event) {
        checkWidget();
        if (event == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        return traverse(traversal, event.character, event.keyCode, event.keyLocation, event.stateMask, event.doit);
    }

    /**
     * Performs a platform traversal action corresponding to a <code>KeyDown</code> event.
     *
     * <p>Valid traversal values are
     * <code>SWT.TRAVERSE_NONE</code>, <code>SWT.TRAVERSE_MNEMONIC</code>,
     * <code>SWT.TRAVERSE_ESCAPE</code>, <code>SWT.TRAVERSE_RETURN</code>,
     * <code>SWT.TRAVERSE_TAB_NEXT</code>, <code>SWT.TRAVERSE_TAB_PREVIOUS</code>,
     * <code>SWT.TRAVERSE_ARROW_NEXT</code>, <code>SWT.TRAVERSE_ARROW_PREVIOUS</code>,
     * <code>SWT.TRAVERSE_PAGE_NEXT</code> and <code>SWT.TRAVERSE_PAGE_PREVIOUS</code>.
     * If <code>traversal</code> is <code>SWT.TRAVERSE_NONE</code> then the Traverse
     * event is created with standard values based on the KeyDown event.  If
     * <code>traversal</code> is one of the other traversal constants then the Traverse
     * event is created with this detail, and its <code>doit</code> is taken from the
     * KeyDown event.
     * </p>
     *
     * @param traversal the type of traversal, or <code>SWT.TRAVERSE_NONE</code> to compute
     * this from <code>event</code>
     * @param event the KeyDown event
     *
     * @return <code>true</code> if the traversal succeeded
     *
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_NULL_ARGUMENT if the event is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.6
     */
    public boolean traverse(int traversal, KeyEvent event) {
        checkWidget();
        if (event == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        return traverse(traversal, event.character, event.keyCode, event.keyLocation, event.stateMask, event.doit);
    }

    public boolean traverse(int traversal, char character, int keyCode, int keyLocation, int stateMask, boolean doit) {
        if (traversal == SWT.TRAVERSE_NONE) {
            switch(keyCode) {
                case SWT.ESC:
                    {
                        traversal = SWT.TRAVERSE_ESCAPE;
                        doit = true;
                        break;
                    }
                case SWT.CR:
                    {
                        traversal = SWT.TRAVERSE_RETURN;
                        doit = true;
                        break;
                    }
                case SWT.ARROW_DOWN:
                case SWT.ARROW_RIGHT:
                    {
                        traversal = SWT.TRAVERSE_ARROW_NEXT;
                        doit = false;
                        break;
                    }
                case SWT.ARROW_UP:
                case SWT.ARROW_LEFT:
                    {
                        traversal = SWT.TRAVERSE_ARROW_PREVIOUS;
                        doit = false;
                        break;
                    }
                case SWT.TAB:
                    {
                        traversal = (stateMask & SWT.SHIFT) != 0 ? SWT.TRAVERSE_TAB_PREVIOUS : SWT.TRAVERSE_TAB_NEXT;
                        doit = true;
                        break;
                    }
                case SWT.PAGE_DOWN:
                    {
                        if ((stateMask & SWT.CTRL) != 0) {
                            traversal = SWT.TRAVERSE_PAGE_NEXT;
                            doit = true;
                        }
                        break;
                    }
                case SWT.PAGE_UP:
                    {
                        if ((stateMask & SWT.CTRL) != 0) {
                            traversal = SWT.TRAVERSE_PAGE_PREVIOUS;
                            doit = true;
                        }
                        break;
                    }
                default:
                    {
                        if (character != 0 && (stateMask & (SWT.ALT | SWT.CTRL)) == SWT.ALT) {
                            traversal = SWT.TRAVERSE_MNEMONIC;
                            doit = true;
                        }
                        break;
                    }
            }
        }
        Event event = new Event();
        event.character = character;
        event.detail = traversal;
        event.doit = doit;
        event.keyCode = keyCode;
        event.keyLocation = keyLocation;
        event.stateMask = stateMask;
        Shell shell = getShell();
        boolean all = false;
        switch(traversal) {
            case SWT.TRAVERSE_ESCAPE:
            case SWT.TRAVERSE_RETURN:
            case SWT.TRAVERSE_PAGE_NEXT:
            case SWT.TRAVERSE_PAGE_PREVIOUS:
                {
                    all = true;
                    // FALL THROUGH
                }
            case SWT.TRAVERSE_ARROW_NEXT:
            case SWT.TRAVERSE_ARROW_PREVIOUS:
            case SWT.TRAVERSE_TAB_NEXT:
            case SWT.TRAVERSE_TAB_PREVIOUS:
                {
                    /* traversal is a valid traversal action */
                    break;
                }
            case SWT.TRAVERSE_MNEMONIC:
                {
                    return translateMnemonic(event, null) || shell.getImpl().translateMnemonic(event, this.getApi());
                }
            default:
                {
                    /* traversal is not a valid traversal action */
                    return false;
                }
        }
        Control control = this.getApi();
        do {
            if (control.getImpl().traverse(event)) {
                return true;
            }
            if (!event.doit && ((DartWidget) control.getImpl()).hooks(SWT.Traverse))
                return false;
            if (control == shell)
                return false;
            control = control.getImpl()._parent();
        } while (all && control != null);
        return false;
    }

    boolean traverseEscape() {
        return false;
    }

    boolean traverseGroup(boolean next) {
        Control root = computeTabRoot();
        Widget group = computeTabGroup();
        Widget[] list = root.getImpl().computeTabList();
        int length = list.length;
        int index = 0;
        while (index < length) {
            if (list[index] == group)
                break;
            index++;
        }
        /*
	* It is possible (but unlikely), that application
	* code could have disposed the widget in focus in
	* or out events.  Ensure that a disposed widget is
	* not accessed.
	*/
        if (index == length)
            return false;
        int start = index, offset = (next) ? 1 : -1;
        while ((index = ((index + offset + length) % length)) != start) {
            Widget widget = list[index];
            if (!widget.isDisposed() && widget.getImpl().setTabGroupFocus()) {
                return true;
            }
        }
        if (group.isDisposed())
            return false;
        return group.getImpl().setTabGroupFocus();
    }

    boolean traverseItem(boolean next) {
        Control[] children = parent.getImpl()._getChildren();
        int length = children.length;
        int index = 0;
        while (index < length) {
            if (children[index] == this.getApi())
                break;
            index++;
        }
        /*
	* It is possible (but unlikely), that application
	* code could have disposed the widget in focus in
	* or out events.  Ensure that a disposed widget is
	* not accessed.
	*/
        if (index == length)
            return false;
        int start = index, offset = (next) ? 1 : -1;
        while ((index = (index + offset + length) % length) != start) {
            Control child = children[index];
            if (!child.isDisposed() && child.getImpl().isTabItem()) {
                if (child.getImpl().setTabItemFocus())
                    return true;
            }
        }
        return false;
    }

    boolean traverseMnemonic(char key) {
        if (mnemonicHit(key)) {
            return true;
        }
        return false;
    }

    boolean traversePage(boolean next) {
        return false;
    }

    boolean traverseReturn() {
        return false;
    }

    void unsubclass() {
    }

    /**
     * Forces all outstanding paint requests for the widget
     * to be processed before this method returns. If there
     * are no outstanding paint request, this method does
     * nothing.
     * <p>Note:</p>
     * <ul>
     * <li>This method does not cause a redraw.</li>
     * <li>Some OS versions forcefully perform automatic deferred painting.
     * This method does nothing in that case.</li>
     * </ul>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #redraw()
     * @see #redraw(int, int, int, int, boolean)
     * @see PaintListener
     * @see SWT#Paint
     */
    public void update() {
        checkWidget();
        update(false);
    }

    void update(boolean all) {
    }

    void updateBackgroundColor() {
        Control control = findBackgroundControl();
        if (control == null)
            control = this.getApi();
        setBackgroundPixel(control.getImpl()._background());
    }

    void updateBackgroundImage() {
    }

    public void updateBackgroundMode() {
        int oldState = getApi().state & PARENT_BACKGROUND;
        checkBackground();
        if (oldState != (getApi().state & PARENT_BACKGROUND)) {
            setBackground();
        }
    }

    void updateFont(Font oldFont, Font newFont) {
        if (getFont().equals(oldFont))
            setFont(newFont);
    }

    public void updateLayout(boolean resize, boolean all) {
        /* Do nothing */
    }

    void updateOrientation() {
        if ((getApi().style & SWT.RIGHT_TO_LEFT) != 0) {
        } else {
        }
    }

    boolean updateTextDirection(int textDirection) {
        if (textDirection == AUTO_TEXT_DIRECTION) {
            textDirection = resolveTextDirection();
            getApi().state |= HAS_AUTO_DIRECTION;
        } else {
            getApi().state &= ~HAS_AUTO_DIRECTION;
        }
        if (textDirection == 0)
            return false;
        return true;
    }

    int widgetExtStyle() {
        int bits = 0;
        if (!isUseWsBorder()) {
        }
        return bits;
    }

    long widgetParent() {
        return parent.handle;
    }

    int widgetStyle() {
        if (isUseWsBorder()) {
        }
        return 0;
    }

    /**
     *  Changes the parent of the widget to be the one provided.
     *  Returns <code>true</code> if the parent is successfully changed.
     *
     *  @param parent the new parent for the control.
     *  @return <code>true</code> if the parent is changed and <code>false</code> otherwise.
     *
     *  @exception IllegalArgumentException <ul>
     *     <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     *     <li>ERROR_NULL_ARGUMENT - if the parent is <code>null</code></li>
     *  </ul>
     *  @exception SWTException <ul>
     *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * 	</ul>
     */
    public boolean setParent(Composite parent) {
        checkWidget();
        if (parent == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (parent.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        if (this.parent == parent)
            return true;
        if (!isReparentable())
            return false;
        releaseParent();
        Shell newShell = parent.getShell(), oldShell = getShell();
        Decorations newDecorations = parent.getImpl().menuShell(), oldDecorations = menuShell();
        if (oldShell != newShell || oldDecorations != newDecorations) {
            Menu[] menus = oldShell.getImpl().findMenus(this.getApi());
            fixChildren(newShell, oldShell, newDecorations, oldDecorations, menus);
        }
        getBridge().reparent(this, parent);
        ControlUtils.reparent(this, parent);
        // If parent changed, zoom level might need to be adjusted
        if (parent.nativeZoom != getApi().nativeZoom) {
        }
        reskin(SWT.ALL);
        return true;
    }

    void handleMonitorSpecificDpiChange(int newNativeZoom, Rectangle newBoundsInPixels) {
        DPIUtil.setDeviceZoom(newNativeZoom);
        this.setBoundsInPixels(newBoundsInPixels.x, newBoundsInPixels.y, newBoundsInPixels.width, newBoundsInPixels.height);
    }

    private static void handleDPIChange(Widget widget, int newZoom, float scalingFactor) {
        if (!(widget instanceof Control control)) {
            return;
        }
        resizeFont(control, ((DartWidget) control.getImpl()).getNativeZoom());
        Image image = ((DartControl) control.getImpl()).backgroundImage;
        if (image != null) {
            if (image.isDisposed()) {
                control.setBackgroundImage(null);
            } else {
                control.setBackgroundImage(image);
            }
        }
        if (control.getRegion() != null) {
            control.setRegion(control.getRegion());
        }
    }

    private static void resizeFont(Control control, int newZoom) {
        Font font = control.getImpl()._font();
        if (font == null) {
        } else {
            control.setFont(DartFont.win32_new(font, newZoom));
        }
    }

    Color _background;

    Rectangle bounds = new Rectangle(0, 0, 0, 0);

    boolean capture;

    boolean dragDetect;

    boolean enabled = true;

    Color _foreground = new Color(0, 0, 0);

    boolean redraw;

    int textDirection;

    boolean touchEnabled;

    boolean visible = true;

    public Composite _parent() {
        return parent;
    }

    public Cursor _cursor() {
        return cursor;
    }

    public Menu _menu() {
        return menu;
    }

    public Menu _activeMenu() {
        return activeMenu;
    }

    public String _toolTipText() {
        return toolTipText;
    }

    public Object _layoutData() {
        return layoutData;
    }

    public Accessible _accessible() {
        return accessible;
    }

    public Image _backgroundImage() {
        return backgroundImage;
    }

    public Region _region() {
        return region;
    }

    public Font _font() {
        return font;
    }

    public int _drawCount() {
        return drawCount;
    }

    public int _foreground() {
        return foreground;
    }

    public int _background() {
        return background;
    }

    public int _backgroundAlpha() {
        return backgroundAlpha;
    }

    public Color __background() {
        return _background;
    }

    public Rectangle _bounds() {
        return bounds;
    }

    public boolean _capture() {
        return capture;
    }

    public boolean _dragDetect() {
        return dragDetect;
    }

    public boolean _enabled() {
        return enabled;
    }

    public Color __foreground() {
        return _foreground;
    }

    public boolean _redraw() {
        return redraw;
    }

    public int _textDirection() {
        return textDirection;
    }

    public boolean _touchEnabled() {
        return touchEnabled;
    }

    public boolean _visible() {
        return visible;
    }

    public FlutterBridge getBridge() {
        if (bridge != null)
            return bridge;
        Composite p = parent;
        while (p != null && !(p.getImpl() instanceof DartWidget)) p = p.getImpl()._parent();
        return p != null ? ((DartWidget) p.getImpl()).getBridge() : null;
    }

    protected void _hookEvents() {
        super._hookEvents();
        FlutterBridge.on(this, "Control", "Move", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Move, e);
            });
        });
        FlutterBridge.on(this, "Control", "Resize", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Resize, e);
            });
        });
        FlutterBridge.on(this, "DragDetect", "DragDetect", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.DragDetect, e);
            });
        });
        FlutterBridge.on(this, "Focus", "FocusIn", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.FocusIn, e);
            });
        });
        FlutterBridge.on(this, "Focus", "FocusOut", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.FocusOut, e);
            });
        });
        FlutterBridge.on(this, "Gesture", "Gesture", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Gesture, e);
            });
        });
        FlutterBridge.on(this, "Help", "Help", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Help, e);
            });
        });
        FlutterBridge.on(this, "Key", "KeyDown", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.KeyDown, e);
            });
        });
        FlutterBridge.on(this, "Key", "KeyUp", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.KeyUp, e);
            });
        });
        FlutterBridge.on(this, "MenuDetect", "MenuDetect", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.MenuDetect, e);
            });
        });
        FlutterBridge.on(this, "Mouse", "MouseDoubleClick", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.MouseDoubleClick, e);
            });
        });
        FlutterBridge.on(this, "Mouse", "MouseDown", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.MouseDown, e);
            });
        });
        FlutterBridge.on(this, "Mouse", "MouseUp", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.MouseUp, e);
            });
        });
        FlutterBridge.on(this, "MouseMove", "MouseMove", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.MouseMove, e);
            });
        });
        FlutterBridge.on(this, "MouseTrack", "MouseEnter", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.MouseEnter, e);
            });
        });
        FlutterBridge.on(this, "MouseTrack", "MouseExit", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.MouseExit, e);
            });
        });
        FlutterBridge.on(this, "MouseTrack", "MouseHover", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.MouseHover, e);
            });
        });
        FlutterBridge.on(this, "MouseWheel", "MouseWheel", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.MouseWheel, e);
            });
        });
        FlutterBridge.on(this, "Paint", "Paint", e -> {
            getDisplay().asyncExec(() -> {
                try {
                    if (!Class.forName("org.eclipse.draw2d.FigureCanvas").isInstance(getApi())) {
                        Event event = new Event();
                        event.gc = new GC(this.getApi());
                        sendEvent(SWT.Paint, event);
                    }
                } catch (ClassNotFoundException ex) {
                    Event event = new Event();
                    event.gc = new GC(this.getApi());
                    sendEvent(SWT.Paint, event);
                }
            });
        });
        FlutterBridge.on(this, "Touch", "Touch", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Touch, e);
            });
        });
        FlutterBridge.on(this, "Traverse", "Traverse", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Traverse, e);
            });
        });
    }

    public Control getApi() {
        return (Control) api;
    }

    public VControl getValue() {
        if (value == null)
            value = new VControl(this);
        return (VControl) value;
    }
}
