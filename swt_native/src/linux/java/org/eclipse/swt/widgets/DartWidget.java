/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2024 IBM Corporation and others.
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

import java.util.*;
import java.util.stream.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import dev.equo.swt.*;

/**
 * This class is the abstract superclass of all user interface objects.
 * Widgets are created, disposed and issue notification to listeners
 * when events occur which affect them.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Dispose</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em>
 * within the SWT implementation. However, it has not been marked
 * final to allow those outside of the SWT development team to implement
 * patched versions of the class in order to get around specific
 * limitations in advance of when those limitations can be addressed
 * by the team.  Any class built using subclassing to access the internals
 * of this class will likely fail to compile or run between releases and
 * may be strongly platform specific. Subclassing should not be attempted
 * without an intimate and detailed understanding of the workings of the
 * hierarchy. No support is provided for user-written classes which are
 * implemented as subclasses of this class.
 * </p>
 *
 * @see #checkSubclass
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public abstract class DartWidget implements IWidget {

    Display display;

    EventTable eventTable;

    Object data;

    /* Global state flags
	 *
	 * Common code pattern:
	 * & - think of AND as removing.
	 * | - think of OR as adding.
	 * state & ~flag  -- Think as "removing flag"
	 * state |  flag  -- Think as "adding flag"
	 *
	 * state |= flag  -- Flag is being added to state.
	 * state &= ~flag -- Flag is being removed from state.
	 * state & flag != 0 -- true if flag is present (think >0 = true)
	 * state & flag == 0 -- true if flag is absent  (think 0 = false)
	 *
	 * (state & (flag1 | flag2)) != 0 -- true if either of the flags are present.
	 * (state & (flag1 | flag2)) == 0 -- true if both flag1 & flag2 are absent.
	 */
    static final int DISPOSED = 1 << 0;

    static final int CANVAS = 1 << 1;

    static final int KEYED_DATA = 1 << 2;

    static final int HANDLE = 1 << 3;

    static final int DISABLED = 1 << 4;

    static final int MENU = 1 << 5;

    static final int OBSCURED = 1 << 6;

    static final int MOVED = 1 << 7;

    static final int RESIZED = 1 << 8;

    static final int ZERO_WIDTH = 1 << 9;

    static final int ZERO_HEIGHT = 1 << 10;

    static final int HIDDEN = 1 << 11;

    static final int FOREGROUND = 1 << 12;

    static final int BACKGROUND = 1 << 13;

    static final int FONT = 1 << 14;

    static final int PARENT_BACKGROUND = 1 << 15;

    static final int THEME_BACKGROUND = 1 << 16;

    /* A layout was requested on this widget */
    static final int LAYOUT_NEEDED = 1 << 17;

    /* The preferred size of a child has changed */
    static final int LAYOUT_CHANGED = 1 << 18;

    /* A layout was requested in this widget hierachy */
    static final int LAYOUT_CHILD = 1 << 19;

    /* More global state flags */
    static final int RELEASED = 1 << 20;

    static final int DISPOSE_SENT = 1 << 21;

    static final int FOREIGN_HANDLE = 1 << 22;

    static final int DRAG_DETECT = 1 << 23;

    /* Notify of the opportunity to skin this widget */
    static final int SKIN_NEEDED = 1 << 24;

    /* Should sub-windows be checked when EnterNotify received */
    static final int CHECK_SUBWINDOW = 1 << 25;

    /* Bidi "auto" text direction */
    static final int HAS_AUTO_DIRECTION = 0;

    /* Bidi flag and for auto text direction */
    static final int AUTO_TEXT_DIRECTION = SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT;

    /* Default size for widgets */
    static final int DEFAULT_WIDTH = 64;

    static final int DEFAULT_HEIGHT = 64;

    /* GTK signals data */
    static final int ACTIVATE = 1;

    static final int BUTTON_PRESS_EVENT = 2;

    static final int BUTTON_PRESS_EVENT_INVERSE = 3;

    static final int BUTTON_RELEASE_EVENT = 4;

    static final int BUTTON_RELEASE_EVENT_INVERSE = 5;

    static final int CHANGED = 6;

    static final int CHANGE_VALUE = 7;

    static final int CLICKED = 8;

    static final int COMMIT = 9;

    static final int CONFIGURE_EVENT = 10;

    static final int DELETE_EVENT = 11;

    static final int DELETE_RANGE = 12;

    static final int DELETE_TEXT = 13;

    static final int ENTER_NOTIFY_EVENT = 14;

    static final int EVENT = 15;

    static final int EVENT_AFTER = 16;

    static final int EXPAND_COLLAPSE_CURSOR_ROW = 17;

    static final int EXPOSE_EVENT = 18;

    static final int DRAW = EXPOSE_EVENT;

    static final int EXPOSE_EVENT_INVERSE = 19;

    static final int FOCUS = 20;

    static final int FOCUS_IN_EVENT = 21;

    static final int FOCUS_OUT_EVENT = 22;

    static final int GRAB_FOCUS = 23;

    static final int HIDE = 24;

    static final int INPUT = 25;

    static final int INSERT_TEXT = 26;

    static final int KEY_PRESS_EVENT = 27;

    static final int KEY_RELEASE_EVENT = 28;

    static final int LEAVE_NOTIFY_EVENT = 29;

    static final int MAP = 30;

    static final int MAP_EVENT = 31;

    static final int MNEMONIC_ACTIVATE = 32;

    static final int MOTION_NOTIFY_EVENT = 33;

    static final int MOTION_NOTIFY_EVENT_INVERSE = 34;

    static final int MOVE_FOCUS = 35;

    static final int OUTPUT = 36;

    static final int POPULATE_POPUP = 37;

    static final int POPUP_MENU = 38;

    static final int PREEDIT_CHANGED = 39;

    static final int REALIZE = 40;

    static final int ROW_ACTIVATED = 41;

    static final int SCROLL_CHILD = 42;

    static final int SCROLL_EVENT = 43;

    static final int SELECT = 44;

    static final int SHOW = 45;

    static final int SHOW_HELP = 46;

    static final int SIZE_ALLOCATE = 47;

    static final int STYLE_UPDATED = 48;

    static final int SWITCH_PAGE = 49;

    static final int TEST_COLLAPSE_ROW = 50;

    static final int TEST_EXPAND_ROW = 51;

    static final int TEXT_BUFFER_INSERT_TEXT = 52;

    static final int TOGGLED = 53;

    static final int UNMAP = 54;

    static final int UNMAP_EVENT = 55;

    static final int UNREALIZE = 56;

    static final int VALUE_CHANGED = 57;

    static final int WINDOW_STATE_EVENT = 59;

    static final int ACTIVATE_INVERSE = 60;

    static final int DAY_SELECTED = 61;

    static final int MONTH_CHANGED = 62;

    static final int STATUS_ICON_POPUP_MENU = 63;

    static final int ROW_INSERTED = 64;

    static final int ROW_DELETED = 65;

    static final int DAY_SELECTED_DOUBLE_CLICK = 66;

    static final int ICON_RELEASE = 67;

    static final int SELECTION_DONE = 68;

    static final int START_INTERACTIVE_SEARCH = 69;

    static final int BACKSPACE = 70;

    static final int BACKSPACE_INVERSE = 71;

    static final int COPY_CLIPBOARD = 72;

    static final int COPY_CLIPBOARD_INVERSE = 73;

    static final int CUT_CLIPBOARD = 74;

    static final int CUT_CLIPBOARD_INVERSE = 75;

    static final int PASTE_CLIPBOARD = 76;

    static final int PASTE_CLIPBOARD_INVERSE = 77;

    static final int DELETE_FROM_CURSOR = 78;

    static final int DELETE_FROM_CURSOR_INVERSE = 79;

    static final int MOVE_CURSOR = 80;

    static final int MOVE_CURSOR_INVERSE = 81;

    static final int DIRECTION_CHANGED = 82;

    static final int CREATE_MENU_PROXY = 83;

    static final int ROW_HAS_CHILD_TOGGLED = 84;

    static final int POPPED_UP = 85;

    static final int FOCUS_IN = 86;

    static final int FOCUS_OUT = 87;

    static final int IM_UPDATE = 88;

    static final int KEY_PRESSED = 89;

    static final int KEY_RELEASED = 90;

    static final int DECELERATE = 91;

    static final int SCROLL = 92;

    static final int SCROLL_BEGIN = 93;

    static final int SCROLL_END = 94;

    static final int ENTER = 95;

    static final int LEAVE = 96;

    static final int MOTION = 97;

    static final int MOTION_INVERSE = 98;

    static final int CLOSE_REQUEST = 99;

    static final int GESTURE_PRESSED = 100;

    static final int GESTURE_RELEASED = 101;

    static final int NOTIFY_STATE = 102;

    static final int SIZE_ALLOCATE_GTK4 = 103;

    static final int DPI_CHANGED = 104;

    static final int NOTIFY_DEFAULT_HEIGHT = 105;

    static final int NOTIFY_DEFAULT_WIDTH = 106;

    static final int NOTIFY_MAXIMIZED = 107;

    static final int COMPUTE_SIZE = 108;

    static final int LAST_SIGNAL = 109;

    //$NON-NLS-1$
    static final String IS_ACTIVE = "org.eclipse.swt.internal.control.isactive";

    //$NON-NLS-1$
    static final String KEY_CHECK_SUBWINDOW = "org.eclipse.swt.internal.control.checksubwindow";

    //$NON-NLS-1$
    static final String KEY_GTK_CSS = "org.eclipse.swt.internal.gtk.css";

    /**
     * Prevents uninitialized instances from being created outside the package.
     */
    DartWidget(Widget api) {
        setApi(api);
        notifyCreationTracker();
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
     * @param parent a widget which will be the parent of the new instance (cannot be null)
     * @param style the style of widget to construct
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the parent is disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see SWT
     * @see #checkSubclass
     * @see #getStyle
     */
    public DartWidget(Widget parent, int style, Widget api) {
        setApi(api);
        checkSubclass();
        checkParent(parent);
        this.getApi().style = style;
        display = parent.getImpl()._display();
        reskinWidget();
        notifyCreationTracker();
    }

    void _addListener(int eventType, Listener listener) {
        if (eventTable == null)
            eventTable = new EventTable();
        eventTable.hook(eventType, listener);
    }

    /**
     * Adds the listener to the collection of {@link Listener listeners} who will
     * be notified when an event of the given type occurs. When the
     * event does occur in the widget, the listener is notified by
     * sending it the <code>handleEvent()</code> message. The event
     * type is one of the event constants defined in class {@link SWT}.
     *
     * @param eventType the type of event to listen for
     * @param listener the listener which should be notified when the event occurs
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #getListeners(int)
     * @see #removeListener(int, Listener)
     * @see #notifyListeners
     */
    public void addListener(int eventType, Listener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        _addListener(eventType, listener);
    }

    /**
     * Adds the {@link EventListener typed listener} to the collection of listeners
     * who will be notified when an event of the given types occurs.
     * When the event does occur in the widget, the listener is notified
     * by calling the type's handling methods.
     * The event type is one of the event constants defined in class {@link SWT}
     * and must correspond to the listeners type.
     * If for example a {@link SelectionListener} is passed the {@code eventTypes}
     * can be {@link SWT#Selection} or {@link SWT#DefaultSelection}.
     *
     * @param listener the listener which should be notified when the event occurs
     * @param eventTypes the types of event to listen for
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #getTypedListeners(int, Class)
     * @see #removeTypedListener(int, EventListener)
     * @see #notifyListeners
     * @since 3.126
     */
    public void addTypedListener(EventListener listener, int... eventTypes) {
        checkWidget();
        if (listener == null) {
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        }
        TypedListener typedListener = new TypedListener(listener);
        for (int eventType : eventTypes) {
            _addListener(eventType, typedListener);
        }
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the widget is disposed. When the widget is
     * disposed, the listener is notified by sending it the
     * <code>widgetDisposed()</code> message.
     *
     * @param listener the listener which should be notified when the receiver is disposed
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see DisposeListener
     * @see #removeDisposeListener
     */
    public void addDisposeListener(DisposeListener listener) {
        addTypedListener(listener, SWT.Dispose);
    }

    long paintWindow() {
        return 0;
    }

    long paintSurface() {
        return 0;
    }

    long cssHandle() {
        return getApi().handle;
    }

    static int checkBits(int style, int int0, int int1, int int2, int int3, int int4, int int5) {
        int mask = int0 | int1 | int2 | int3 | int4 | int5;
        if ((style & mask) == 0)
            style |= int0;
        if ((style & int0) != 0)
            style = (style & ~mask) | int0;
        if ((style & int1) != 0)
            style = (style & ~mask) | int1;
        if ((style & int2) != 0)
            style = (style & ~mask) | int2;
        if ((style & int3) != 0)
            style = (style & ~mask) | int3;
        if ((style & int4) != 0)
            style = (style & ~mask) | int4;
        if ((style & int5) != 0)
            style = (style & ~mask) | int5;
        return style;
    }

    long cellDataProc(long tree_column, long cell, long tree_model, long iter, long data) {
        return 0;
    }

    public void checkOpen() {
        /* Do nothing */
    }

    void checkOrientation(Widget parent) {
        getApi().style &= ~SWT.MIRRORED;
        if ((getApi().style & (SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT)) == 0) {
            if (parent != null) {
                if ((parent.style & SWT.LEFT_TO_RIGHT) != 0)
                    getApi().style |= SWT.LEFT_TO_RIGHT;
                if ((parent.style & SWT.RIGHT_TO_LEFT) != 0)
                    getApi().style |= SWT.RIGHT_TO_LEFT;
            }
        }
        getApi().style = checkBits(getApi().style, SWT.LEFT_TO_RIGHT, SWT.RIGHT_TO_LEFT, 0, 0, 0, 0);
    }

    /**
     * Throws an exception if the specified widget can not be
     * used as a parent for the receiver.
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the parent is disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     * </ul>
     */
    void checkParent(Widget parent) {
        if (parent == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (parent.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        parent.checkWidget();
        parent.getImpl().checkOpen();
    }

    /**
     * Checks that this class can be subclassed.
     * <p>
     * The SWT class library is intended to be subclassed
     * only at specific, controlled points (most notably,
     * <code>Composite</code> and <code>Canvas</code> when
     * implementing new widgets). This method enforces this
     * rule unless it is overridden.
     * </p><p>
     * <em>IMPORTANT:</em> By providing an implementation of this
     * method that allows a subclass of a class which does not
     * normally allow subclassing to be created, the implementer
     * agrees to be fully responsible for the fact that any such
     * subclass will likely fail between SWT releases and will be
     * strongly platform specific. No support is provided for
     * user-written classes which are implemented in this fashion.
     * </p><p>
     * The ability to subclass outside of the allowed SWT classes
     * is intended purely to enable those not on the SWT development
     * team to implement patches in order to get around specific
     * limitations in advance of when those limitations can be
     * addressed by the team. Subclassing should not be attempted
     * without an intimate and detailed understanding of the hierarchy.
     * </p>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     */
    public void checkSubclass() {
        if (!isValidSubclass())
            error(SWT.ERROR_INVALID_SUBCLASS);
    }

    /**
     * Throws an <code>SWTException</code> if the receiver can not
     * be accessed by the caller. This may include both checks on
     * the state of the receiver and more generally on the entire
     * execution context. This method <em>should</em> be called by
     * widget implementors to enforce the standard SWT invariants.
     * <p>
     * Currently, it is an error to invoke any method (other than
     * <code>isDisposed()</code>) on a widget that has had its
     * <code>dispose()</code> method called. It is also an error
     * to call widget methods from any thread that is different
     * from the thread that created the widget.
     * </p><p>
     * In future releases of SWT, there may be more or fewer error
     * checks and exceptions may be thrown for different reasons.
     * </p>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void checkWidget() {
        Display display = this.display;
        if (display == null)
            error(SWT.ERROR_WIDGET_DISPOSED);
        //if (((SwtDisplay) display.getImpl()).thread != Thread.currentThread())
        //    error(SWT.ERROR_THREAD_INVALID_ACCESS);
        if ((getApi().state & DISPOSED) != 0)
            error(SWT.ERROR_WIDGET_DISPOSED);
    }

    void createHandle(int index) {
    }

    void createWidget(int index) {
        createHandle(index);
        setOrientation(true);
        hookEvents();
        register();
    }

    void deregister() {
        if (bridge != null)
            bridge.destroy(this);
    }

    void destroyWidget() {
        long topHandle = topHandle();
        releaseHandle();
        if (topHandle != 0 && (getApi().state & HANDLE) != 0) {
        }
    }

    /**
     * Disposes of the operating system resources associated with
     * the receiver and all its descendants. After this method has
     * been invoked, the receiver and all descendants will answer
     * <code>true</code> when sent the message <code>isDisposed()</code>.
     * Any internal connections between the widgets in the tree will
     * have been removed to facilitate garbage collection.
     * This method does nothing if the widget is already disposed.
     * <p>
     * NOTE: This method is not called recursively on the descendants
     * of the receiver. This means that, widget implementers can not
     * detect when a widget is being disposed of by re-implementing
     * this method, but should instead listen for the <code>Dispose</code>
     * event.
     * </p>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #addDisposeListener
     * @see #removeDisposeListener
     * @see #checkWidget
     */
    public void dispose() {
        /*
	* Note:  It is valid to attempt to dispose a widget
	* more than once.  If this happens, fail silently.
	*/
        if (isDisposed())
            return;
        if (!isValidThread())
            error(SWT.ERROR_THREAD_INVALID_ACCESS);
        release(true);
    }

    long dpiChanged(long object, long arg0) {
        return 0;
    }

    void error(int code) {
        SWT.error(code);
    }

    /**
     * Returns the application defined widget data associated
     * with the receiver, or null if it has not been set. The
     * <em>widget data</em> is a single, unnamed field that is
     * stored with every widget.
     * <p>
     * Applications may put arbitrary objects in this field. If
     * the object stored in the widget data needs to be notified
     * when the widget is disposed of, it is the application's
     * responsibility to hook the Dispose event on the widget and
     * do so.
     * </p>
     *
     * @return the widget data
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - when the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - when called from the wrong thread</li>
     * </ul>
     *
     * @see #setData(Object)
     */
    public Object getData() {
        checkWidget();
        return (getApi().state & KEYED_DATA) != 0 ? ((Object[]) data)[0] : data;
    }

    /**
     * Returns the application defined property of the receiver
     * with the specified name, or null if it has not been set.
     * <p>
     * Applications may have associated arbitrary objects with the
     * receiver in this fashion. If the objects stored in the
     * properties need to be notified when the widget is disposed
     * of, it is the application's responsibility to hook the
     * Dispose event on the widget and do so.
     * </p>
     *
     * @param	key the name of the property
     * @return the value of the property or null if it has not been set
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the key is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setData(String, Object)
     */
    public Object getData(String key) {
        checkWidget();
        if (key == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (key.equals(KEY_CHECK_SUBWINDOW)) {
            return (getApi().state & CHECK_SUBWINDOW) != 0;
        }
        if (key.equals(IS_ACTIVE))
            return isActive();
        if ((getApi().state & KEYED_DATA) != 0) {
            Object[] table = (Object[]) data;
            if (table == null) {
                return null;
            }
            for (int i = 1; i < table.length; i += 2) {
                if (key.equals(table[i]))
                    return table[i + 1];
            }
        }
        return null;
    }

    /**
     * Returns the <code>Display</code> that is associated with
     * the receiver.
     * <p>
     * A widget's display is either provided when it is created
     * (for example, top level <code>Shell</code>s) or is the
     * same as its parent's display.
     * </p>
     *
     * @return the receiver's display
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public Display getDisplay() {
        Display display = this.display;
        if (display == null)
            error(SWT.ERROR_WIDGET_DISPOSED);
        return display;
    }

    /**
     * Returns an array of {@link Listener listeners} who will be notified when an event
     * of the given type occurs. The event type is one of the event constants
     * defined in class {@link SWT}.
     *
     * @param eventType the type of event to listen for
     * @return an array of listeners that will be notified when the event occurs
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #addListener(int, Listener)
     * @see #removeListener(int, Listener)
     * @see #notifyListeners
     *
     * @since 3.4
     */
    public Listener[] getListeners(int eventType) {
        checkWidget();
        if (eventTable == null)
            return new Listener[0];
        return eventTable.getListeners(eventType);
    }

    /**
     * Returns the typed listeners who will be notified when an event of the given type occurs.
     * The event type is one of the event constants defined in class {@link SWT}
     * and the specified listener-type must correspond to that event.
     * If for example the {@code eventType} is {@link SWT#Selection},
     * the listeners type should be {@link SelectionListener}.
     *
     * @param eventType the type of event to listen for
     * @return a stream of typed listeners that will be notified when the event occurs
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #addTypedListener(EventListener, int...)
     * @see #removeTypedListener(int, EventListener)
     * @see #notifyListeners
     *
     * @since 3.126
     */
    public <L extends EventListener> Stream<L> getTypedListeners(int eventType, Class<L> listenerType) {
        return //
        Arrays.stream(getListeners(eventType)).filter(TypedListener.class::isInstance).map(l -> ((TypedListener) l).eventListener).filter(listenerType::isInstance).map(listenerType::cast);
    }

    String getName() {
        //	String string = getClass ().getName ();
        //	int index = string.lastIndexOf ('.');
        //	if (index == -1) return string;
        String string = getClass().getName();
        int index = string.length();
        while ((--index > 0) && (string.charAt(index) != '.')) {
        }
        return string.substring(index + 1, string.length());
    }

    String getNameText() {
        return "";
    }

    /**
     * Returns the receiver's style information.
     * <p>
     * Note that the value which is returned by this method <em>may
     * not match</em> the value which was provided to the constructor
     * when the receiver was created. This can occur when the underlying
     * operating system does not support a particular combination of
     * requested styles. For example, if the platform widget used to
     * implement a particular SWT widget always has scroll bars, the
     * result of calling this method would always have the
     * <code>SWT.H_SCROLL</code> and <code>SWT.V_SCROLL</code> bits set.
     * </p>
     *
     * @return the style bits
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getStyle() {
        return getApi().style;
    }

    int fontHeight(long font, long widgetHandle) {
        return 0;
    }

    long filterProc(long xEvent, long gdkEvent, long data2) {
        return 0;
    }

    boolean filters(int eventType) {
        return ((SwtDisplay) display.getImpl()).filters(eventType);
    }

    char[] fixMnemonic(String string) {
        return fixMnemonic(string, true);
    }

    char[] fixMnemonic(String string, boolean replace) {
        return fixMnemonic(string, replace, false);
    }

    char[] fixMnemonic(String string, boolean replace, boolean removeAppended) {
        int length = string.length();
        char[] text = new char[length];
        string.getChars(0, length, text, 0);
        int i = 0, j = 0;
        char[] result = new char[length * 2];
        while (i < length) {
            switch(text[i]) {
                case '&':
                    if (i + 1 < length && text[i + 1] == '&') {
                        result[j++] = text[i++];
                    } else {
                        if (replace)
                            result[j++] = '_';
                    }
                    i++;
                    break;
                /*
				 * In Japanese like languages where mnemonics are not taken from the
				 * source label text but appended in parentheses like "(&M)" at end. In order to
				 * allow the reuse of such label text as a tool-tip text as well, "(&M)" like
				 * character sequence has to be removed from the end of CJK-style mnemonics.
				 */
                case '(':
                    if (removeAppended && i + 4 == string.length() && text[i + 1] == '&' && text[i + 3] == ')') {
                        if (replace)
                            result[j++] = ' ';
                        i += 4;
                        // break switch case only if we are removing the mnemonic
                        break;
                    } else {
                        // otherwise fall through (default case applies)
                        result[j++] = text[i++];
                        break;
                    }
                case '_':
                    if (replace)
                        result[j++] = '_';
                //FALL THROUGH
                default:
                    result[j++] = text[i++];
            }
        }
        return result;
    }

    boolean isActive() {
        return true;
    }

    /**
     * Returns <code>true</code> if the widget has auto text direction,
     * and <code>false</code> otherwise.
     *
     * @return <code>true</code> when the widget has auto direction and <code>false</code> otherwise
     *
     * @see SWT#AUTO_TEXT_DIRECTION
     *
     * @since 3.105
     */
    public boolean isAutoDirection() {
        return false;
    }

    /**
     * Returns <code>true</code> if the widget has been disposed,
     * and <code>false</code> otherwise.
     * <p>
     * This method gets the dispose state for the widget.
     * When a widget has been disposed, it is an error to
     * invoke any other method (except {@link #dispose()}) using the widget.
     * </p>
     *
     * @return <code>true</code> when the widget is disposed and <code>false</code> otherwise
     */
    public boolean isDisposed() {
        return (getApi().state & DISPOSED) != 0;
    }

    /**
     * Returns <code>true</code> if there are any listeners
     * for the specified event type associated with the receiver,
     * and <code>false</code> otherwise. The event type is one of
     * the event constants defined in class <code>SWT</code>.
     *
     * @param eventType the type of event
     * @return true if the event is hooked
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SWT
     */
    public boolean isListening(int eventType) {
        checkWidget();
        return hooks(eventType);
    }

    boolean isValidThread() {
        return ((SwtDisplay) getDisplay().getImpl()).isValidThread();
    }

    boolean isValidSubclass() {
        return SwtDisplay.isValidClass(getClass());
    }

    void hookEvents() {
        if (getApi().handle != 0) {
        }
    }

    /*
 * Returns <code>true</code> if the specified eventType is
 * hooked, and <code>false</code> otherwise. Implementations
 * of SWT can avoid creating objects and sending events
 * when an event happens in the operating system but
 * there are no listeners hooked for the event.
 *
 * @param eventType the event to be checked
 *
 * @return <code>true</code> when the eventType is hooked and <code>false</code> otherwise
 *
 * @see #isListening
 */
    boolean hooks(int eventType) {
        if (eventTable == null)
            return false;
        return eventTable.hooks(eventType);
    }

    long hoverProc(long widget) {
        return 0;
    }

    boolean mnemonicHit(long mnemonicHandle, char key) {
        if (!mnemonicMatch(mnemonicHandle, key))
            return false;
        return false;
    }

    boolean mnemonicMatch(long mnemonicHandle, char key) {
        return false;
    }

    /**
     * Notifies all of the receiver's listeners for events
     * of the given type that one such event has occurred by
     * invoking their <code>handleEvent()</code> method.  The
     * event type is one of the event constants defined in class
     * <code>SWT</code>.
     *
     * @param eventType the type of event which has occurred
     * @param event the event data
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SWT
     * @see #addListener
     * @see #getListeners(int)
     * @see #removeListener(int, Listener)
     */
    public void notifyListeners(int eventType, Event event) {
        checkWidget();
        if (event == null)
            event = new Event();
        sendEvent(eventType, event);
    }

    void postEvent(int eventType) {
        sendEvent(eventType, null, false);
    }

    void postEvent(int eventType, Event event) {
        sendEvent(eventType, event, false);
    }

    void register() {
        _hookEvents();
        bridge = FlutterBridge.of(this);
    }

    public void release(boolean destroy) {
        try (ExceptionStash exceptions = new ExceptionStash()) {
            if ((getApi().state & DISPOSE_SENT) == 0) {
                getApi().state |= DISPOSE_SENT;
                try {
                    sendEvent(SWT.Dispose);
                } catch (Error | RuntimeException ex) {
                    exceptions.stash(ex);
                }
            }
            if ((getApi().state & DISPOSED) == 0) {
                try {
                    releaseChildren(destroy);
                } catch (Error | RuntimeException ex) {
                    exceptions.stash(ex);
                }
            }
            if ((getApi().state & RELEASED) == 0) {
                getApi().state |= RELEASED;
                if (destroy) {
                    releaseParent();
                    releaseWidget();
                    destroyWidget();
                } else {
                    releaseWidget();
                    releaseHandle();
                }
            }
            notifyDisposalTracker();
        }
    }

    void releaseChildren(boolean destroy) {
    }

    void releaseHandle() {
        getApi().handle = 0;
        getApi().state |= DISPOSED;
        display = null;
    }

    void releaseParent() {
        /* Do nothing */
    }

    void releaseWidget() {
        deregister();
        eventTable = null;
        data = null;
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when an event of the given type occurs. The event
     * type is one of the event constants defined in class <code>SWT</code>.
     *
     * @param eventType the type of event to listen for
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
     * @see Listener
     * @see SWT
     * @see #addListener
     * @see #getListeners(int)
     * @see #notifyListeners
     */
    public void removeListener(int eventType, Listener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(eventType, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when an event of the given type occurs.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the SWT
     * public API. It is marked public only so that it can be shared
     * within the packages provided by SWT. It should never be
     * referenced from application code.
     * </p>
     *
     * @param eventType the type of event to listen for
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
     * @see Listener
     * @see #addListener
     *
     * @noreference This method is not intended to be referenced by clients.
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     */
    public void removeListener(int eventType, SWTEventListener listener) {
        removeTypedListener(eventType, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when an event of the given type occurs.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the SWT
     * public API. It is marked public only so that it can be shared
     * within the packages provided by SWT. It should never be
     * referenced from application code.
     * </p>
     *
     * @param eventType the type of event to listen for
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
     * @see Listener
     * @see #addListener
     *
     * @noreference This method is not intended to be referenced by clients.
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     */
    public void removeTypedListener(int eventType, EventListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(eventType, listener);
    }

    long rendererGetPreferredWidthProc(long cell, long handle, long minimun_size, long natural_size) {
        return 0;
    }

    long rendererRenderProc(long cell, long cr, long handle, long background_area, long cell_area, long flags) {
        return 0;
    }

    long rendererSnapshotProc(long cell, long snapshot, long handle, long background_area, long cell_area, long flags) {
        return 0;
    }

    /**
     * Marks the widget to be skinned.
     * <p>
     * The skin event is sent to the receiver's display when appropriate (usually before the next event
     * is handled). Widgets are automatically marked for skinning upon creation as well as when its skin
     * id or class changes. The skin id and/or class can be changed by calling {@link Display#setData(String, Object)}
     * with the keys {@link SWT#SKIN_ID} and/or {@link SWT#SKIN_CLASS}. Once the skin event is sent to a widget, it
     * will not be sent again unless <code>reskin(int)</code> is called on the widget or on an ancestor
     * while specifying the <code>SWT.ALL</code> flag.
     * </p>
     * <p>
     * The parameter <code>flags</code> may be either:
     * </p>
     * <dl>
     * <dt><b>{@link SWT#ALL}</b></dt>
     * <dd>all children in the receiver's widget tree should be skinned</dd>
     * <dt><b>{@link SWT#NONE}</b></dt>
     * <dd>only the receiver should be skinned</dd>
     * </dl>
     * @param flags the flags specifying how to reskin
     *
     * @exception SWTException
     * <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @since 3.6
     */
    public void reskin(int flags) {
        checkWidget();
        reskinWidget();
        if ((flags & SWT.ALL) != 0)
            reskinChildren(flags);
    }

    void reskinChildren(int flags) {
    }

    void reskinWidget() {
        if ((getApi().state & SKIN_NEEDED) != SKIN_NEEDED) {
            this.getApi().state |= SKIN_NEEDED;
            ((SwtDisplay) display.getImpl()).addSkinnableWidget(this.getApi());
        }
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the widget is disposed.
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
     * @see DisposeListener
     * @see #addDisposeListener
     */
    public void removeDisposeListener(DisposeListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Dispose, listener);
    }

    public void sendEvent(Event event) {
        Display display = event.display;
        if (!((SwtDisplay) display.getImpl()).filterEvent(event)) {
            if (eventTable != null)
                ((SwtDisplay) display.getImpl()).sendEvent(eventTable, event);
        }
    }

    public void sendEvent(int eventType) {
        sendEvent(eventType, null, true);
    }

    public void sendEvent(int eventType, Event event) {
        sendEvent(eventType, event, true);
    }

    public void sendEvent(int eventType, Event event, boolean send) {
        if (eventTable == null && !((SwtDisplay) display.getImpl()).filters(eventType)) {
            return;
        }
        if (event == null) {
            event = new Event();
        }
        event.type = eventType;
        event.display = display;
        event.widget = this.getApi();
        if (event.time == 0) {
            event.time = ((SwtDisplay) display.getImpl()).getLastEventTime();
        }
        if (send) {
            sendEvent(event);
        } else {
            ((SwtDisplay) display.getImpl()).postEvent(event);
        }
    }

    boolean sendKeyEvent(int type, long event) {
        return false;
    }

    char[] sendIMKeyEvent(int type, long event, char[] chars) {
        int index = 0, count = 0, state = 0;
        long ptr = 0;
        if (event == 0) {
            if (ptr != 0) {
            } else {
            }
        } else {
            ptr = event;
        }
        while (index < chars.length) {
            Event javaEvent = new Event();
            if (ptr != 0 && chars.length <= 1) {
                setKeyState(javaEvent, ptr);
            } else {
                setInputState(javaEvent, state);
            }
            javaEvent.character = chars[index];
            sendEvent(type, javaEvent);
            /*
		* It is possible (but unlikely), that application
		* code could have disposed the widget in the key
		* events.  If this happens, end the processing of
		* the key by returning null.
		*/
            if (isDisposed()) {
                return null;
            }
            if (javaEvent.doit)
                chars[count++] = chars[index];
            index++;
        }
        if (count == 0)
            return null;
        if (index != count) {
            char[] result = new char[count];
            System.arraycopy(chars, 0, result, 0, count);
            return result;
        }
        return chars;
    }

    void sendSelectionEvent(int eventType) {
        sendSelectionEvent(eventType, null, false);
    }

    void sendSelectionEvent(int eventType, Event event, boolean send) {
        if (eventTable == null && !((SwtDisplay) display.getImpl()).filters(eventType)) {
            return;
        }
        if (event == null)
            event = new Event();
        sendEvent(eventType, event, send);
    }

    /**
     * Sets the application defined widget data associated
     * with the receiver to be the argument. The <em>widget
     * data</em> is a single, unnamed field that is stored
     * with every widget.
     * <p>
     * Applications may put arbitrary objects in this field. If
     * the object stored in the widget data needs to be notified
     * when the widget is disposed of, it is the application's
     * responsibility to hook the Dispose event on the widget and
     * do so.
     * </p>
     *
     * @param data the widget data
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - when the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - when called from the wrong thread</li>
     * </ul>
     *
     * @see #getData()
     */
    public void setData(Object data) {
        if ((getApi().state & KEYED_DATA) != 0) {
            ((Object[]) this.data)[0] = data;
        } else {
            this.data = data;
        }
    }

    /**
     * Sets the application defined property of the receiver
     * with the specified name to the given value.
     * <p>
     * Applications may associate arbitrary objects with the
     * receiver in this fashion. If the objects stored in the
     * properties need to be notified when the widget is disposed
     * of, it is the application's responsibility to hook the
     * Dispose event on the widget and do so.
     * </p>
     *
     * @param key the name of the property
     * @param value the new value for the property
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the key is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #getData(String)
     */
    public void setData(String key, Object value) {
        if (key == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (key.equals(KEY_CHECK_SUBWINDOW)) {
            if (value != null && value instanceof Boolean) {
                if (((Boolean) value).booleanValue()) {
                    getApi().state |= CHECK_SUBWINDOW;
                } else {
                    getApi().state &= ~CHECK_SUBWINDOW;
                }
            }
            return;
        }
        int index = 1;
        Object[] table = null;
        if ((getApi().state & KEYED_DATA) != 0) {
            table = (Object[]) data;
            while (index < table.length) {
                if (key.equals(table[index]))
                    break;
                index += 2;
            }
        }
        if (value != null) {
            if ((getApi().state & KEYED_DATA) != 0) {
                if (index == table.length) {
                    Object[] newTable = new Object[table.length + 2];
                    System.arraycopy(table, 0, newTable, 0, table.length);
                    data = table = newTable;
                }
            } else {
                table = new Object[3];
                table[0] = data;
                data = table;
                getApi().state |= KEYED_DATA;
            }
            table[index] = key;
            table[index + 1] = value;
        } else {
            if ((getApi().state & KEYED_DATA) != 0) {
                if (index != table.length) {
                    int length = table.length - 2;
                    if (length == 1) {
                        data = table[0];
                        getApi().state &= ~KEYED_DATA;
                    } else {
                        Object[] newTable = new Object[length];
                        System.arraycopy(table, 0, newTable, 0, index);
                        System.arraycopy(table, index + 2, newTable, index, length - index);
                        data = newTable;
                    }
                }
            }
        }
        if (key.equals(SWT.SKIN_CLASS) || key.equals(SWT.SKIN_ID))
            this.reskin(SWT.ALL);
        if (key.equals(KEY_GTK_CSS) && value instanceof String) {
        }
    }

    /**
     * @param fontDescription Font description in the form of
     *                        <code>PangoFontDescription*</code>. This pointer
     *                        will never be used by GTK after calling this
     *                        function, so it's safe to free it as soon as the
     *                        function completes.
     */
    void setFontDescription(long widget, long fontDescription) {
    }

    String convertPangoFontDescriptionToCss(long fontDescription) {
        String css = "* { ";
        css += " } ";
        return css;
    }

    void setButtonState(Event event, int eventButton) {
        switch(eventButton) {
            case 1:
                event.stateMask |= SWT.BUTTON1;
                break;
            case 2:
                event.stateMask |= SWT.BUTTON2;
                break;
            case 3:
                event.stateMask |= SWT.BUTTON3;
                break;
            case 4:
                event.stateMask |= SWT.BUTTON4;
                break;
            case 5:
                event.stateMask |= SWT.BUTTON5;
                break;
            default:
        }
    }

    boolean setInputState(Event event, int state) {
        return true;
    }

    /**
     * On Linux, the most common way to handle keyboard input is XKB.
     * The rest of the description explains XKB and related GTK stuff.
     *
     * XKB uses the following definitions:
     * <ul>
     *  <li>"group" - that's how they call keyboard layouts</li>
     *  <li>"keycode" - id of a physical key on a keyboard. For example,
     *   AB01 refers to 2nd row from the bottom (B), 1st key (01) </li>
     *  <li>"level" - Can be seen as a number that describes modifiers
     *   pressed together with the key. For example, in English US,
     *   pressing A would result in level 0, and pressing Shift+A would
     *   result in level 1. The other common levels are used for AltGr
     *   and Shift+AltGr, but a keyboard layout could have even more
     *   exotic levels.</li>
     *  <li>"modifiers" - a combination of modifier keys. Keyboard
     *   layouts could define their own modifiers.</li>
     *  <li>"keyval" - Can be seen as a final calculation of what was
     *   produced by a key press (by taking keycode, group, level, and
     *   other modifiers such as dead keys into account).
     * </ul>
     * Some examples:
     * <table>
     *  <tr><th>Layout</th><th>Key row</th><th>Key col</th><th>Keycode</th><th>Modifiers</th><th>Keyval</th><th>Character</th></tr>
     *  <tr><td>English US    </td><td>1</td><td>10</td><td>FK09</td><td></td><td>F9</td><td></td></tr>
     *  <tr><td>English US    </td><td>3</td><td>5</td><td>AD04</td><td></td><td>r</td><td>r</td></tr>
     *  <tr><td>English US    </td><td>3</td><td>5</td><td>AD04</td><td>Shift</td><td>R</td><td>R</td></tr>
     *  <tr><td>English US    </td><td>3</td><td>6</td><td>AD05</td><td></td><td>t</td><td>t</td></tr>
     *  <tr><td>English Dvorak</td><td>3</td><td>5</td><td>AD04</td><td></td><td>p</td><td>p</td></tr>
     *  <tr><td>English Dvorak</td><td>3</td><td>6</td><td>AD05</td><td></td><td>y</td><td>y</td></tr>
     *  <tr><td>Bulgarian     </td><td>3</td><td>5</td><td>AD04</td><td></td><td>Cyrillic_i</td><td>и</td></tr>
     *  <tr><td>Bulgarian     </td><td>3</td><td>6</td><td>AD05</td><td></td><td>Cyrillic_sha</td><td>ш</td></tr>
     * </table><br>
     *
     * XKB doesn't do two-step keyboard layout translation like Windows.
     * For this reason, binding keyboard shortcuts across keyboard layouts
     * quickly becomes ugly. Further, each major UI library (such as Qt
     * or GTK) and many major softwares (such as Firefox, LibreOffice)
     * has its own approach. Usually developed through trial, error and
     * pain.
     *
     * The common approach is to search all installed keyboard layouts
     * and find some that is latin. Then invoke keyboard shortcut using
     * that layout. That is, if current layout is Bulgarian:<br>
     * <ul>
     *  <li>notice that current layout is not latin</li>
     *  <li>search installed layouts to find a latin one</li>
     *  <li>map pressed key to a latin char using that layout</li>
     *  <li>invoke keyboard shortcut</li>
     * </ul>
     *
     * The details of how it's done differ across libraries and
     * softwares. For example:
     * <ul>
     *  <li>If currently pressed key produces latin keyval, some are
     *   happy with that and will not search for other layouts. Others
     *   do search anyway. This often results in multiple keys invoking
     *   the same shortcut (when there are multiple layouts with
     *   desired latin key in different positions). One example of
     *   affected software is 'gedit'.</li>
     *  <li>When they do search, some search all layouts, others
     *   search only the previous layouts (and insist that latin layout
     *   is installed before non-latin), some search for a first match,
     *   some search for the "most latin" layout, etc.</li>
     * </ul>
     * To my understanding, no matter which of these dark magics is
     * chosen, this always results in ugly corner cases, band-aided
     * with dirty workarounds which fix something while breaking
     * something else.
     *
     * For an example of how other software deals with all that mess, see
     * QXcbKeyboard::lookupLatinKeysym() in Qt.
     */
    boolean setKeyState(Event javaEvent, long event) {
        int[] eventKeyval = new int[1];
        int[] eventState = new int[1];
        boolean isNull = false;
        javaEvent.keyCode = SwtDisplay.translateKey(eventKeyval[0]);
        setLocationState(javaEvent, event);
        if (javaEvent.keyCode == 0 && javaEvent.character == 0) {
            if (!isNull)
                return false;
        }
        return setInputState(javaEvent, eventState[0]);
    }

    void setLocationState(Event event, long eventPtr) {
    }

    void setOrientation(boolean create) {
    }

    public boolean setTabGroupFocus(boolean next) {
        return setTabItemFocus(next);
    }

    public boolean setTabItemFocus(boolean next) {
        return false;
    }

    long shellMapProc(long handle, long arg0, long user_data) {
        return 0;
    }

    long sizeAllocateProc(long handle, long arg0, long user_data) {
        return 0;
    }

    long sizeRequestProc(long handle, long arg0, long user_data) {
        return 0;
    }

    /**
     * Converts an incoming snapshot into a gtk_draw() call, complete with
     * a Cairo context.
     *
     * @param handle the widget receiving the snapshot
     * @param snapshot the actual GtkSnapshot
     */
    void snapshotToDraw(long handle, long snapshot) {
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the receiver
     */
    @Override
    public String toString() {
        String string = "*Disposed*";
        if (!isDisposed()) {
            string = "*Wrong Thread*";
            if (isValidThread())
                string = getNameText();
        }
        return getName() + " {" + string + "}";
    }

    public long topHandle() {
        return getApi().handle;
    }

    long timerProc(long widget) {
        return 0;
    }

    boolean translateTraversal(int event) {
        return false;
    }

    void enterMotionProc(long controller, double x, double y, long user_data) {
        switch((int) user_data) {
            case ENTER:
                break;
            case MOTION:
                break;
            case MOTION_INVERSE:
                break;
        }
    }

    boolean scrollProc(long controller, double dx, double dy, long user_data) {
        switch((int) user_data) {
            case SCROLL:
        }
        return false;
    }

    void focusProc(long controller, long user_data) {
        switch((int) user_data) {
            case FOCUS_IN:
                break;
            case FOCUS_OUT:
                break;
        }
    }

    void windowActiveProc(long handle, long user_data) {
    }

    boolean keyPressReleaseProc(long controller, int keyval, int keycode, int state, long user_data) {
        switch((int) user_data) {
            case KEY_PRESSED:
            case KEY_RELEASED:
                break;
        }
        return false;
    }

    void gesturePressReleaseProc(long gesture, int n_press, double x, double y, long user_data) {
        switch((int) user_data) {
            case GESTURE_PRESSED:
                break;
            case GESTURE_RELEASED:
                break;
        }
    }

    void leaveProc(long controller, long handle, long user_data) {
        switch((int) user_data) {
            case LEAVE:
                break;
        }
    }

    long notifyProc(long object, long arg0, long user_data) {
        switch((int) user_data) {
            case DPI_CHANGED:
                return dpiChanged(object, arg0);
            case NOTIFY_STATE:
                return notifyState(object, arg0);
            case NOTIFY_DEFAULT_HEIGHT:
            case NOTIFY_DEFAULT_WIDTH:
            case NOTIFY_MAXIMIZED:
        }
        return 0;
    }

    long notifyState(long object, long argo0) {
        return 0;
    }

    void setToolTipText(long tipWidget, String string) {
        if (string != null && !string.isEmpty()) {
        }
    }

    void notifyCreationTracker() {
        if (WidgetSpy.isEnabled) {
            WidgetSpy.getInstance().widgetCreated(this.getApi());
        }
    }

    void notifyDisposalTracker() {
        if (WidgetSpy.isEnabled) {
            WidgetSpy.getInstance().widgetDisposed(this.getApi());
        }
    }

    public Display _display() {
        return display;
    }

    public EventTable _eventTable() {
        return eventTable;
    }

    public Object _data() {
        return data;
    }

    protected FlutterBridge bridge;

    public FlutterBridge getBridge() {
        return bridge;
    }

    protected void dirty() {
        FlutterBridge bridge = getBridge();
        if (bridge != null)
            bridge.dirty(this);
    }

    protected void _hookEvents() {
        FlutterBridge.on(this, "Dispose", "Dispose", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Dispose, e);
            });
        });
    }

    public Widget getApi() {
        return (Widget) api;
    }

    protected Widget api;

    public void setApi(Widget api) {
        this.api = api;
        if (api != null)
            api.impl = this;
    }

    protected VWidget value;

    public VWidget getValue() {
        if (value == null)
            value = new VWidget(this);
        return (VWidget) value;
    }
}
