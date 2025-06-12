package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.internal.ExceptionStash;
import org.eclipse.swt.internal.SWTEventListener;
import org.eclipse.swt.values.WidgetValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * This class is the abstract superclass of all user interface objects. Widgets
 * are created, disposed and issue notification to listeners when events occur
 * which affect them.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Dispose</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em> within the
 * SWT implementation. However, it has not been marked final to allow those
 * outside of the SWT development team to implement patched versions of the
 * class in order to get around specific limitations in advance of when those
 * limitations can be addressed by the team. Any class built using subclassing
 * to access the internals of this class will likely fail to compile or run
 * between releases and may be strongly platform specific. Subclassing should
 * not be attempted without an intimate and detailed understanding of the
 * workings of the hierarchy. No support is provided for user-written classes
 * which are implemented as subclasses of this class.
 * </p>
 *
 * @see #checkSubclass
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 */
public abstract class FlutterWidget implements IWidget {
    public void addTypedListener(EventListener listener, int... eventTypes) {
    }

    protected long flutterContext;

    /**
     * the handle to the OS resource (Warning: This field is platform dependent)
     * <p>
     * <b>IMPORTANT:</b> This field is <em>not</em> part of the SWT public API. It
     * is marked public only so that it can be shared within the packages provided
     * by SWT. It is not available on all platforms and should never be accessed
     * from application code.
     * </p>
     *
     * @noreference This field is not intended to be referenced by clients.
     */
    public long handle;

    protected int style, state;

    protected SWTDisplay display;

    EventTable eventTable;

    Object data;

    /*
     * Global state flags
     *
     * Common code pattern: & - think of AND as removing. | - think of OR as adding.
     * state & ~flag -- Think as "removing flag" state | flag -- Think as
     * "adding flag"
     *
     * state |= flag -- Flag is being added to state. state &= ~flag -- Flag is
     * being removed from state. state & flag != 0 -- true if flag is present (think
     * >0 = true) state & flag == 0 -- true if flag is absent (think 0 = false)
     *
     * (state & (flag1 | flag2)) != 0 -- true if either of the flags are present.
     * (state & (flag1 | flag2)) == 0 -- true if both flag1 & flag2 are absent.
     */
    static final int DISPOSED = 1 << 0;

    static final int KEYED_DATA = 1 << 2;

    static final int HANDLE = 1 << 3;

    /* More global state flags */
    static final int RELEASED = 1 << 20;

    static final int DISPOSE_SENT = 1 << 21;

    /* Should sub-windows be checked when EnterNotify received */
    static final int CHECK_SUBWINDOW = 1 << 25;

    // $NON-NLS-1$
    static final String IS_ACTIVE = "org.eclipse.swt.internal.control.isactive";

    // $NON-NLS-1$
    static final String KEY_CHECK_SUBWINDOW = "org.eclipse.swt.internal.control.checksubwindow";

    /* Default size for widgets */
    static final int DEFAULT_WIDTH = 64;
    static final int DEFAULT_HEIGHT = 64;

    protected CompletableFuture<Boolean> clientReady = new CompletableFuture<>();

    /**
     * Prevents uninitialized instances from being created outside the package.
     */
    FlutterWidget() {
        notifyCreationTracker();
    }

    /**
     * Constructs a new instance of this class given its parent and a style value
     * describing its behavior and appearance.
     * <p>
     * The style value is either one of the style constants defined in class
     * <code>SWT</code> which is applicable to instances of this class, or must be
     * built by <em>bitwise OR</em>'ing together (that is, using the
     * <code>int</code> "|" operator) two or more of those <code>SWT</code> style
     * constants. The class description lists the style constants that are
     * applicable to the class. Style bits are also inherited from superclasses.
     * </p>
     *
     * @param parent a widget which will be the parent of the new instance (cannot
     *               be null)
     * @param style  the style of widget to construct
     *
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_NULL_ARGUMENT - if the parent
     *                                     is null</li>
     *                                     <li>ERROR_INVALID_ARGUMENT - if the
     *                                     parent is disposed</li>
     *                                     </ul>
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS - if not
     *                                     called from the thread that created the
     *                                     parent</li>
     *                                     <li>ERROR_INVALID_SUBCLASS - if this
     *                                     class is not an allowed subclass</li>
     *                                     </ul>
     *
     * @see SWT
     * @see #checkSubclass
     * @see #getStyle
     */
    public FlutterWidget(IWidget parent, int style) {
        checkSubclass();
        if (parent instanceof FlutterWidget flutterParent) {
            checkParent(flutterParent);
            reskinWidget();
            notifyCreationTracker();
            if (flutterParent.children == null) {
                flutterParent.children = new ArrayList<>();
            }
            flutterParent.children.add(this);
            FlutterSwt.dirty(flutterParent);
        }
        display = (SWTDisplay) parent.getDisplay();
        this.style = style;
    }

    void _addListener(int eventType, Listener listener) {
        if (eventTable == null)
            eventTable = new EventTable();
        eventTable.hook(eventType, listener);
    }

    /**
     * Adds the listener to the collection of listeners who will be notified when an
     * event of the given type occurs. When the event does occur in the widget, the
     * listener is notified by sending it the <code>handleEvent()</code> message.
     * The event type is one of the event constants defined in class
     * <code>SWT</code>.
     *
     * @param eventType the type of event to listen for
     * @param listener  the listener which should be notified when the event occurs
     *
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_NULL_ARGUMENT - if the listener
     *                                     is null</li>
     *                                     </ul>
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_WIDGET_DISPOSED - if the
     *                                     receiver has been disposed</li>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS - if not
     *                                     called from the thread that created the
     *                                     receiver</li>
     *                                     </ul>
     *
     * @see Listener
     * @see SWT
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
     * Adds the listener to the collection of listeners who will be notified when
     * the widget is disposed. When the widget is disposed, the listener is notified
     * by sending it the <code>widgetDisposed()</code> message.
     *
     * @param listener the listener which should be notified when the receiver is
     *                 disposed
     *
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_NULL_ARGUMENT - if the listener
     *                                     is null</li>
     *                                     </ul>
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_WIDGET_DISPOSED - if the
     *                                     receiver has been disposed</li>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS - if not
     *                                     called from the thread that created the
     *                                     receiver</li>
     *                                     </ul>
     *
     * @see DisposeListener
     * @see #removeDisposeListener
     */
    public void addDisposeListener(DisposeListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        TypedListener typedListener = new TypedListener(listener);
        addListener(SWT.Dispose, typedListener);
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

    void checkOpen() {
        /* Do nothing */
    }

    /**
     * Throws an exception if the specified widget can not be used as a parent for
     * the receiver.
     *
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_NULL_ARGUMENT - if the parent
     *                                     is null</li>
     *                                     <li>ERROR_INVALID_ARGUMENT - if the
     *                                     parent is disposed</li>
     *                                     </ul>
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS - if not
     *                                     called from the thread that created the
     *                                     parent</li>
     *                                     </ul>
     */
    void checkParent(FlutterWidget parent) {
        if (parent == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (parent.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        parent.checkWidget();
        parent.checkOpen();
    }

    /**
     * Checks that this class can be subclassed.
     * <p>
     * The SWT class library is intended to be subclassed only at specific,
     * controlled points (most notably, <code>Composite</code> and
     * <code>Canvas</code> when implementing new widgets). This method enforces this
     * rule unless it is overridden.
     * </p>
     * <p>
     * <em>IMPORTANT:</em> By providing an implementation of this method that allows
     * a subclass of a class which does not normally allow subclassing to be
     * created, the implementer agrees to be fully responsible for the fact that any
     * such subclass will likely fail between SWT releases and will be strongly
     * platform specific. No support is provided for user-written classes which are
     * implemented in this fashion.
     * </p>
     * <p>
     * The ability to subclass outside of the allowed SWT classes is intended purely
     * to enable those not on the SWT development team to implement patches in order
     * to get around specific limitations in advance of when those limitations can
     * be addressed by the team. Subclassing should not be attempted without an
     * intimate and detailed understanding of the hierarchy.
     * </p>
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_INVALID_SUBCLASS - if this class is not an
     *                         allowed subclass</li>
     *                         </ul>
     */
    protected void checkSubclass() {
    }

    /**
     * Throws an <code>SWTException</code> if the receiver can not be accessed by
     * the caller. This may include both checks on the state of the receiver and
     * more generally on the entire execution context. This method <em>should</em>
     * be called by widget implementors to enforce the standard SWT invariants.
     * <p>
     * Currently, it is an error to invoke any method (other than
     * <code>isDisposed()</code>) on a widget that has had its
     * <code>dispose()</code> method called. It is also an error to call widget
     * methods from any thread that is different from the thread that created the
     * widget.
     * </p>
     * <p>
     * In future releases of SWT, there may be more or fewer error checks and
     * exceptions may be thrown for different reasons.
     * </p>
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     */
    public void checkWidget() {
        IDisplay display = this.display;
        if (display == null)
            error(SWT.ERROR_WIDGET_DISPOSED);
//        if (display.thread != Thread.currentThread())
//            error(SWT.ERROR_THREAD_INVALID_ACCESS);
        if ((state & DISPOSED) != 0)
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
        if (handle == 0)
            return;
        if ((state & HANDLE) != 0)
            display.removeWidget(handle);
    }

    void destroyWidget() {
        // Not Generated
        releaseHandle();
    }

    /**
     * Disposes of the operating system resources associated with the receiver and
     * all its descendants. After this method has been invoked, the receiver and all
     * descendants will answer <code>true</code> when sent the message
     * <code>isDisposed()</code>. Any internal connections between the widgets in
     * the tree will have been removed to facilitate garbage collection. This method
     * does nothing if the widget is already disposed.
     * <p>
     * NOTE: This method is not called recursively on the descendants of the
     * receiver. This means that, widget implementers can not detect when a widget
     * is being disposed of by re-implementing this method, but should instead
     * listen for the <code>Dispose</code> event.
     * </p>
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     *
     * @see #addDisposeListener
     * @see #removeDisposeListener
     * @see #checkWidget
     */
    public void dispose() {
        /*
         * Note: It is valid to attempt to dispose a widget more than once. If this
         * happens, fail silently.
         */
        if (isDisposed())
            return;
        if (!isValidThread())
            error(SWT.ERROR_THREAD_INVALID_ACCESS);
        release(true);
//        FlutterSwt.CloseFlutterWindow(flutterContext);
    }

    void error(int code) {
        SWT.error(code);
    }

    /**
     * Returns the application defined widget data associated with the receiver, or
     * null if it has not been set. The <em>widget data</em> is a single, unnamed
     * field that is stored with every widget.
     * <p>
     * Applications may put arbitrary objects in this field. If the object stored in
     * the widget data needs to be notified when the widget is disposed of, it is
     * the application's responsibility to hook the Dispose event on the widget and
     * do so.
     * </p>
     *
     * @return the widget data
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - when the receiver has
     *                         been disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - when called from
     *                         the wrong thread</li>
     *                         </ul>
     *
     * @see #setData(Object)
     */
    public Object getData() {
        checkWidget();
        return (state & KEYED_DATA) != 0 ? ((Object[]) data)[0] : data;
    }

    /**
     * Returns the application defined property of the receiver with the specified
     * name, or null if it has not been set.
     * <p>
     * Applications may have associated arbitrary objects with the receiver in this
     * fashion. If the objects stored in the properties need to be notified when the
     * widget is disposed of, it is the application's responsibility to hook the
     * Dispose event on the widget and do so.
     * </p>
     *
     * @param key the name of the property
     * @return the value of the property or null if it has not been set
     *
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_NULL_ARGUMENT - if the key is
     *                                     null</li>
     *                                     </ul>
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_WIDGET_DISPOSED - if the
     *                                     receiver has been disposed</li>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS - if not
     *                                     called from the thread that created the
     *                                     receiver</li>
     *                                     </ul>
     *
     * @see #setData(String, Object)
     */
    public Object getData(String key) {
        checkWidget();
        if (key == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (key.equals(KEY_CHECK_SUBWINDOW)) {
            return (state & CHECK_SUBWINDOW) != 0;
        }
        if (key.equals(IS_ACTIVE))
            return isActive();
        if ((state & KEYED_DATA) != 0) {
            Object[] table = (Object[]) data;
            for (int i = 1; i < table.length; i += 2) {
                if (key.equals(table[i]))
                    return table[i + 1];
            }
        }
        return null;
    }

    /**
     * Returns the <code>Display</code> that is associated with the receiver.
     * <p>
     * A widget's display is either provided when it is created (for example, top
     * level <code>Shell</code>s) or is the same as its parent's display.
     * </p>
     *
     * @return the receiver's display
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         </ul>
     */
    public IDisplay getDisplay() {
        IDisplay display = this.display;
        if (display == null)
            error(SWT.ERROR_WIDGET_DISPOSED);
        return display;
    }

    /**
     * Returns an array of listeners who will be notified when an event of the given
     * type occurs. The event type is one of the event constants defined in class
     * <code>SWT</code>.
     *
     * @param eventType the type of event to listen for
     * @return an array of listeners that will be notified when the event occurs
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     *
     * @see Listener
     * @see SWT
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
     * Returns the receiver's style information.
     * <p>
     * Note that the value which is returned by this method <em>may not match</em>
     * the value which was provided to the constructor when the receiver was
     * created. This can occur when the underlying operating system does not support
     * a particular combination of requested styles. For example, if the platform
     * widget used to implement a particular SWT widget always has scroll bars, the
     * result of calling this method would always have the <code>SWT.H_SCROLL</code>
     * and <code>SWT.V_SCROLL</code> bits set.
     * </p>
     *
     * @return the style bits
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     */
    public int getStyle() {
        // Not Generated
        checkWidget();
        return style;
    }

    boolean filters(int eventType) {
        return display.filters(eventType);
    }

    boolean isActive() {
        return true;
    }

    /**
     * Returns <code>true</code> if the widget has auto text direction, and
     * <code>false</code> otherwise.
     *
     * @return <code>true</code> when the widget has auto direction and
     *         <code>false</code> otherwise
     *
     * @see SWT#AUTO_TEXT_DIRECTION
     *
     * @since 3.105
     */
    public boolean isAutoDirection() {
        return false;
    }

    /**
     * Returns <code>true</code> if the widget has been disposed, and
     * <code>false</code> otherwise.
     * <p>
     * This method gets the dispose state for the widget. When a widget has been
     * disposed, it is an error to invoke any other method (except
     * {@link #dispose()}) using the widget.
     * </p>
     *
     * @return <code>true</code> when the widget is disposed and <code>false</code>
     *         otherwise
     */
    public boolean isDisposed() {
        return (state & DISPOSED) != 0;
    }

    /**
     * Returns <code>true</code> if there are any listeners for the specified event
     * type associated with the receiver, and <code>false</code> otherwise. The
     * event type is one of the event constants defined in class <code>SWT</code>.
     *
     * @param eventType the type of event
     * @return true if the event is hooked
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     *
     * @see SWT
     */
    public boolean isListening(int eventType) {
        checkWidget();
        return hooks(eventType);
    }

    boolean isValidThread() {
        return ((SWTDisplay) getDisplay()).isValidThread();
    }

    protected void hookEvents() {
        String ev = FlutterSwt.getEvent(this);
        FlutterSwt.CLIENT.getComm().on(ev + "/Dispose/Dispose", p -> {
            System.out.println(ev + "/Dispose/Dispose event");
            display.asyncExec(() -> {
                sendEvent(SWT.Dispose);
            });
        });
        final String clientReadyId = ev + "/ClientReady";
        FlutterSwt.CLIENT.getComm().on(clientReadyId, p -> {
            if (!clientReady.isDone()) {
                System.out.println(clientReadyId);
                clientReady.complete(true);
            }
        });
    }

    /*
     * Returns <code>true</code> if the specified eventType is hooked, and
     * <code>false</code> otherwise. Implementations of SWT can avoid creating
     * objects and sending events when an event happens in the operating system but
     * there are no listeners hooked for the event.
     *
     * @param eventType the event to be checked
     *
     * @return <code>true</code> when the eventType is hooked and <code>false</code>
     * otherwise
     *
     * @see #isListening
     */
    boolean hooks(int eventType) {
        if (eventTable == null)
            return false;
        return eventTable.hooks(eventType);
    }

    /**
     * Notifies all of the receiver's listeners for events of the given type that
     * one such event has occurred by invoking their <code>handleEvent()</code>
     * method. The event type is one of the event constants defined in class
     * <code>SWT</code>.
     *
     * @param eventType the type of event which has occurred
     * @param event     the event data
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
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
        if (handle == 0)
            return;
        if ((state & HANDLE) != 0)
            display.addWidget(handle, this);
    }

    void release(boolean destroy) {
        try (ExceptionStash exceptions = new ExceptionStash()) {
            if ((state & DISPOSE_SENT) == 0) {
                state |= DISPOSE_SENT;
                try {
                    sendEvent(SWT.Dispose);
                } catch (Error | RuntimeException ex) {
                    exceptions.stash(ex);
                }
            }
            if ((state & DISPOSED) == 0) {
                try {
                    releaseChildren(destroy);
                } catch (Error | RuntimeException ex) {
                    exceptions.stash(ex);
                }
            }
            if ((state & RELEASED) == 0) {
                state |= RELEASED;
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
        handle = 0;
        state |= DISPOSED;
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
     * Removes the listener from the collection of listeners who will be notified
     * when an event of the given type occurs. The event type is one of the event
     * constants defined in class <code>SWT</code>.
     *
     * @param eventType the type of event to listen for
     * @param listener  the listener which should no longer be notified
     *
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_NULL_ARGUMENT - if the listener
     *                                     is null</li>
     *                                     </ul>
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_WIDGET_DISPOSED - if the
     *                                     receiver has been disposed</li>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS - if not
     *                                     called from the thread that created the
     *                                     receiver</li>
     *                                     </ul>
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
     * Removes the listener from the collection of listeners who will be notified
     * when an event of the given type occurs.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the SWT public API. It
     * is marked public only so that it can be shared within the packages provided
     * by SWT. It should never be referenced from application code.
     * </p>
     *
     * @param eventType the type of event to listen for
     * @param listener  the listener which should no longer be notified
     *
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_NULL_ARGUMENT - if the listener
     *                                     is null</li>
     *                                     </ul>
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_WIDGET_DISPOSED - if the
     *                                     receiver has been disposed</li>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS - if not
     *                                     called from the thread that created the
     *                                     receiver</li>
     *                                     </ul>
     *
     * @see Listener
     * @see #addListener
     *
     * @noreference This method is not intended to be referenced by clients.
     * @nooverride This method is not intended to be re-implemented or extended by
     *             clients.
     */
    protected void removeListener(int eventType, SWTEventListener handler) {
        checkWidget();
        if (handler == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(eventType, handler);
    }

    /**
     * Marks the widget to be skinned.
     * <p>
     * The skin event is sent to the receiver's display when appropriate (usually
     * before the next event is handled). Widgets are automatically marked for
     * skinning upon creation as well as when its skin id or class changes. The skin
     * id and/or class can be changed by calling
     * {@link Display#setData(String, Object)} with the keys {@link SWT#SKIN_ID}
     * and/or {@link SWT#SKIN_CLASS}. Once the skin event is sent to a widget, it
     * will not be sent again unless <code>reskin(int)</code> is called on the
     * widget or on an ancestor while specifying the <code>SWT.ALL</code> flag.
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
     * 
     * @param flags the flags specifying how to reskin
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     * @since 3.6
     */
    public void reskin(int flags) {
    }

    void reskinWidget() {
    }

    /**
     * Removes the listener from the collection of listeners who will be notified
     * when the widget is disposed.
     *
     * @param listener the listener which should no longer be notified
     *
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_NULL_ARGUMENT - if the listener
     *                                     is null</li>
     *                                     </ul>
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_WIDGET_DISPOSED - if the
     *                                     receiver has been disposed</li>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS - if not
     *                                     called from the thread that created the
     *                                     receiver</li>
     *                                     </ul>
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

    void sendEvent(Event event) {
        SWTDisplay display = (SWTDisplay) event.display.delegate;
        if (!display.filterEvent(event)) {
            if (eventTable != null)
                display.sendEvent(eventTable, event);
        }
    }

    protected void sendEvent(int eventType) {
        sendEvent(eventType, null, true);
    }

    void sendEvent(int eventType, Event event) {
        sendEvent(eventType, event, true);
    }

    void sendEvent(int eventType, Event event, boolean send) {
        if (eventTable == null && !display.filters(eventType)) {
            return;
        }
        if (event == null) {
            event = new Event();
        }
        event.type = eventType;
        event.display = Display.getInstance(display);
        event.widget = Widget.getInstance(this);
        if (event.time == 0) {
            event.time = display.getLastEventTime();
        }
        if (send) {
            sendEvent(event);
        } else {
            display.postEvent(event);
        }
    }

    /**
     * Sets the application defined widget data associated with the receiver to be
     * the argument. The <em>widget data</em> is a single, unnamed field that is
     * stored with every widget.
     * <p>
     * Applications may put arbitrary objects in this field. If the object stored in
     * the widget data needs to be notified when the widget is disposed of, it is
     * the application's responsibility to hook the Dispose event on the widget and
     * do so.
     * </p>
     *
     * @param data the widget data
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - when the receiver has
     *                         been disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - when called from
     *                         the wrong thread</li>
     *                         </ul>
     *
     * @see #getData()
     */
    public void setData(Object data) {
        checkWidget();
        if ((state & KEYED_DATA) != 0) {
            ((Object[]) this.data)[0] = data;
        } else {
            this.data = data;
        }
    }

    /**
     * Sets the application defined property of the receiver with the specified name
     * to the given value.
     * <p>
     * Applications may associate arbitrary objects with the receiver in this
     * fashion. If the objects stored in the properties need to be notified when the
     * widget is disposed of, it is the application's responsibility to hook the
     * Dispose event on the widget and do so.
     * </p>
     *
     * @param key   the name of the property
     * @param value the new value for the property
     *
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_NULL_ARGUMENT - if the key is
     *                                     null</li>
     *                                     </ul>
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_WIDGET_DISPOSED - if the
     *                                     receiver has been disposed</li>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS - if not
     *                                     called from the thread that created the
     *                                     receiver</li>
     *                                     </ul>
     *
     * @see #getData(String)
     */
    public void setData(String key, Object value) {
        checkWidget();
        if (key == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (key.equals(KEY_CHECK_SUBWINDOW)) {
            if (value != null && value instanceof Boolean) {
                if (((Boolean) value).booleanValue()) {
                    state |= CHECK_SUBWINDOW;
                } else {
                    state &= ~CHECK_SUBWINDOW;
                }
            }
            return;
        }
        int index = 1;
        Object[] table = null;
        if ((state & KEYED_DATA) != 0) {
            table = (Object[]) data;
            while (index < table.length) {
                if (key.equals(table[index]))
                    break;
                index += 2;
            }
        }
        if (value != null) {
            if ((state & KEYED_DATA) != 0) {
                if (index == table.length) {
                    Object[] newTable = new Object[table.length + 2];
                    System.arraycopy(table, 0, newTable, 0, table.length);
                    data = table = newTable;
                }
            } else {
                table = new Object[3];
                table[0] = data;
                data = table;
                state |= KEYED_DATA;
            }
            table[index] = key;
            table[index + 1] = value;
        } else {
            if ((state & KEYED_DATA) != 0) {
                if (index != table.length) {
                    int length = table.length - 2;
                    if (length == 1) {
                        data = table[0];
                        state &= ~KEYED_DATA;
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
    }

    void setOrientation(boolean create) {
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     *
     * @return a string representation of the receiver
     */
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    void notifyCreationTracker() {
    }

    void notifyDisposalTracker() {
    }

    public long getHandle() {
        return handle;
    }

    public long topHandle() {
        return handle;
    }

    public <L extends EventListener> Stream<L> getTypedListeners(int eventType, Class<L> listenerType) {
        return //
        Arrays.stream(getListeners(eventType)).filter(TypedListener.class::isInstance)
                .map(l -> ((TypedListener) l).eventListener).filter(listenerType::isInstance).map(listenerType::cast);
    }

    public List<FlutterWidget> children;

    protected WidgetValue.Builder builder;

    public abstract WidgetValue.Builder builder();

}
