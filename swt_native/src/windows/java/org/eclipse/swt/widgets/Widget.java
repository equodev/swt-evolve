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
 *      Pierre-Yves B., pyvesdev@gmail.com - Bug 219750: [styled text] Typing ~~ inserts é~~
 * *****************************************************************************
 */
package org.eclipse.swt.widgets;

import java.util.*;
import java.util.stream.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.win32.*;

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
public abstract class Widget {

    /**
     * the native zoom of the monitor in percent
     * (Warning: This field is platform dependent)
     * <p>
     * <b>IMPORTANT:</b> This field is <em>not</em> part of the SWT
     * public API. It is marked public only so that it can be shared
     * within the packages provided by SWT. It is not available on all
     * platforms and should never be accessed from application code.
     * </p>
     *
     * @noreference This field is not intended to be referenced by clients.
     */
    public int nativeZoom;

    int style, state;

    /**
     * Prevents uninitialized instances from being created outside the package.
     */
    Widget() {
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
    public Widget(Widget parent, int style) {
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
        getImpl().addListener(eventType, listener);
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
    protected void addTypedListener(EventListener listener, int... eventTypes) {
        getImpl().addTypedListener(listener, eventTypes);
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
        getImpl().addDisposeListener(listener);
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
    protected void checkSubclass() {
        getImpl().checkSubclass();
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
    protected void checkWidget() {
        getImpl().checkWidget();
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
        getImpl().dispose();
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
        return getImpl().getData();
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
        return getImpl().getData(key);
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
        return getImpl().getDisplay();
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
        return getImpl().getListeners(eventType);
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
        return getImpl().getTypedListeners(eventType, listenerType);
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
        return getImpl().getStyle();
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
        return getImpl().isAutoDirection();
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
        return getImpl().isDisposed();
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
        return getImpl().isListening(eventType);
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
        getImpl().notifyListeners(eventType, event);
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
        getImpl().removeListener(eventType, listener);
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
    protected void removeListener(int eventType, SWTEventListener listener) {
        getImpl().removeListener(eventType, listener);
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
    protected void removeTypedListener(int eventType, EventListener listener) {
        getImpl().removeTypedListener(eventType, listener);
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
        getImpl().removeDisposeListener(listener);
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
        getImpl().reskin(flags);
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
        getImpl().setData(data);
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
        getImpl().setData(key, value);
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the receiver
     */
    public String toString() {
        return getImpl().toString();
    }

    protected IWidget impl;

    protected Widget(IWidget impl) {
        if (impl != null)
            impl.setApi(this);
    }

    public IWidget getImpl() {
        return impl;
    }

    protected Widget setImpl(IWidget impl) {
        this.impl = impl;
        return this;
    }
}
