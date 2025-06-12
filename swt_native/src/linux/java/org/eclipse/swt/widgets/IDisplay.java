package org.eclipse.swt.widgets;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.regex.Pattern;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.GDBus.*;
import org.eclipse.swt.internal.cairo.*;
import org.eclipse.swt.internal.gtk.*;
import org.eclipse.swt.internal.gtk3.*;
import org.eclipse.swt.internal.gtk4.*;

public interface IDisplay {

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when an event of the given type occurs anywhere
     * in a widget. The event type is one of the event constants
     * defined in class <code>SWT</code>. When the event does occur,
     * the listener is notified by sending it the <code>handleEvent()</code>
     * message.
     * <p>
     * Setting the type of an event to <code>SWT.None</code> from
     * within the <code>handleEvent()</code> method can be used to
     * change the event type and stop subsequent Java listeners
     * from running. Because event filters run before other listeners,
     * event filters can both block other listeners and set arbitrary
     * fields within an event. For this reason, event filters are both
     * powerful and dangerous. They should generally be avoided for
     * performance, debugging and code maintenance reasons.
     * </p>
     *
     * @param eventType the type of event to listen for
     * @param listener the listener which should be notified when the event occurs
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see Listener
     * @see SWT
     * @see #removeFilter
     * @see #removeListener
     *
     * @since 3.0
     */
    void addFilter(int eventType, Listener listener);

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when an event of the given type occurs. The event
     * type is one of the event constants defined in class <code>SWT</code>.
     * When the event does occur in the display, the listener is notified by
     * sending it the <code>handleEvent()</code> message.
     *
     * @param eventType the type of event to listen for
     * @param listener the listener which should be notified when the event occurs
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see Listener
     * @see SWT
     * @see #removeListener
     *
     * @since 2.0
     */
    void addListener(int eventType, Listener listener);

    /**
     * Causes the <code>run()</code> method of the runnable to
     * be invoked by the user-interface thread at the next
     * reasonable opportunity. The caller of this method continues
     * to run in parallel, and is not notified when the
     * runnable has completed.  Specifying <code>null</code> as the
     * runnable simply wakes the user-interface thread when run.
     * <p>
     * Note that at the time the runnable is invoked, widgets
     * that have the receiver as their display may have been
     * disposed. Therefore, it is necessary to check for this
     * case inside the runnable before accessing the widget.
     * </p>
     *
     * @param runnable code to run on the user-interface thread or <code>null</code>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #syncExec
     */
    void asyncExec(Runnable runnable);

    /**
     * Executes the given runnable in the user-interface thread of this Display.
     * <ul>
     * <li>If the calling thread is the user-interface thread of this display it is
     * executed immediately and the method returns after the command has run, as with
     * the method {@link Display#syncExec(Runnable)}.</li>
     * <li>In all other cases the <code>run()</code> method of the runnable is
     * asynchronously executed as with the method
     * {@link Display#asyncExec(Runnable)} at the next reasonable opportunity. The
     * caller of this method continues to run in parallel, and is not notified when
     * the runnable has completed.</li>
     * </ul>
     * <p>
     * This can be used in cases where one want to execute some piece of code that
     * should be guaranteed to run in the user-interface thread regardless of the
     * current thread.
     * </p>
     *
     * <p>
     * Note that at the time the runnable is invoked, widgets that have the receiver
     * as their display may have been disposed. Therefore, it is advised to check
     * for this case inside the runnable before accessing the widget.
     * </p>
     *
     * @param runnable the runnable to execute in the user-interface thread, never
     *                 <code>null</code>
     * @throws RejectedExecutionException if this task cannot be accepted for
     *                                    execution
     * @throws NullPointerException       if runnable is null
     */
    void execute(Runnable runnable);

    /**
     * Causes the system hardware to emit a short sound
     * (if it supports this capability).
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void beep();

    /**
     * Requests that the connection between SWT and the underlying
     * operating system be closed.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see Device#dispose
     *
     * @since 2.0
     */
    void close();

    /**
     * Causes the <code>run()</code> method of the runnable to
     * be invoked by the user-interface thread just before the
     * receiver is disposed.  Specifying a <code>null</code> runnable
     * is ignored.
     *
     * @param runnable code to run at dispose time.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void disposeExec(Runnable runnable);

    /**
     * Given the operating system handle for a widget, returns
     * the instance of the <code>Widget</code> subclass which
     * represents it in the currently running application, if
     * such exists, or null if no matching widget can be found.
     * <p>
     * <b>IMPORTANT:</b> This method should not be called from
     * application code. The arguments are platform-specific.
     * </p>
     *
     * @param handle the handle for the widget
     * @return the SWT widget that the handle represents
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @noreference This method is not intended to be referenced by clients.
     */
    IWidget findWidget(long handle);

    /**
     * Given the operating system handle for a widget,
     * and widget-specific id, returns the instance of
     * the <code>Widget</code> subclass which represents
     * the handle/id pair in the currently running application,
     * if such exists, or null if no matching widget can be found.
     * <p>
     * <b>IMPORTANT:</b> This method should not be called from
     * application code. The arguments are platform-specific.
     * </p>
     *
     * @param handle the handle for the widget
     * @param id the id for the subwidget (usually an item)
     * @return the SWT widget that the handle/id pair represents
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @noreference This method is not intended to be referenced by clients.
     *
     * @since 3.1
     */
    IWidget findWidget(long handle, long id);

    /**
     * Given a widget and a widget-specific id, returns the
     * instance of the <code>Widget</code> subclass which represents
     * the widget/id pair in the currently running application,
     * if such exists, or null if no matching widget can be found.
     *
     * @param widget the widget
     * @param id the id for the subwidget (usually an item)
     * @return the SWT subwidget (usually an item) that the widget/id pair represents
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @noreference This method is not intended to be referenced by clients.
     *
     * @since 3.3
     */
    IWidget findWidget(IWidget widget, long id);

    /**
     * Returns the currently active <code>Shell</code>, or null
     * if no shell belonging to the currently running application
     * is active.
     *
     * @return the active shell or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    IShell getActiveShell();

    /**
     * Returns a rectangle describing the receiver's size and location. Note that
     * on multi-monitor systems the origin can be negative.
     *
     * @return the bounding rectangle
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    Rectangle getBounds();

    /**
     * Returns a rectangle which describes the area of the
     * receiver which is capable of displaying data.
     *
     * @return the client area
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #getBounds
     */
    Rectangle getClientArea();

    /**
     * Returns the control which the on-screen pointer is currently
     * over top of, or null if it is not currently over one of the
     * controls built by the currently running application.
     *
     * @return the control under the cursor or <code>null</code>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    IControl getCursorControl();

    /**
     * Returns the location of the on-screen pointer relative
     * to the top left corner of the screen.
     *
     * @return the cursor location
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    Point getCursorLocation();

    /**
     * Returns an array containing the recommended cursor sizes.
     *
     * @return the array of cursor sizes
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.0
     */
    Point[] getCursorSizes();

    /**
     * Returns the application defined property of the receiver
     * with the specified name, or null if it has not been set.
     * <p>
     * Applications may have associated arbitrary objects with the
     * receiver in this fashion. If the objects stored in the
     * properties need to be notified when the display is disposed
     * of, it is the application's responsibility to provide a
     * <code>disposeExec()</code> handler which does so.
     * </p>
     *
     * @param key the name of the property
     * @return the value of the property or null if it has not been set
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the key is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #setData(String, Object)
     * @see #disposeExec(Runnable)
     */
    Object getData(String key);

    /**
     * Returns the application defined, display specific data
     * associated with the receiver, or null if it has not been
     * set. The <em>display specific data</em> is a single,
     * unnamed field that is stored with every display.
     * <p>
     * Applications may put arbitrary objects in this field. If
     * the object stored in the display specific data needs to
     * be notified when the display is disposed of, it is the
     * application's responsibility to provide a
     * <code>disposeExec()</code> handler which does so.
     * </p>
     *
     * @return the display specific data
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #setData(Object)
     * @see #disposeExec(Runnable)
     */
    Object getData();

    /**
     * Returns the single instance of the application menu bar, or
     * <code>null</code> if there is no application menu bar for the platform.
     *
     * @return the application menu bar, or <code>null</code>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.7
     */
    IMenu getMenuBar();

    /**
     * Returns the button dismissal alignment, one of <code>LEFT</code> or <code>RIGHT</code>.
     * The button dismissal alignment is the ordering that should be used when positioning the
     * default dismissal button for a dialog.  For example, in a dialog that contains an OK and
     * CANCEL button, on platforms where the button dismissal alignment is <code>LEFT</code>, the
     * button ordering should be OK/CANCEL.  When button dismissal alignment is <code>RIGHT</code>,
     * the button ordering should be CANCEL/OK.
     *
     * @return the button dismissal order
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 2.1
     */
    int getDismissalAlignment();

    /**
     * Returns the longest duration, in milliseconds, between
     * two mouse button clicks that will be considered a
     * <em>double click</em> by the underlying operating system.
     *
     * @return the double click time
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    int getDoubleClickTime();

    /**
     * Returns the control which currently has keyboard focus,
     * or null if keyboard events are not currently going to
     * any of the controls built by the currently running
     * application.
     *
     * @return the focus control or <code>null</code>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    IControl getFocusControl();

    /**
     * Returns true when the high contrast mode is enabled.
     * Otherwise, false is returned.
     * <p>
     * Note: This operation is a hint and is not supported on
     * platforms that do not have this concept.
     * </p>
     *
     * @return the high contrast mode
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.0
     */
    boolean getHighContrast();

    int getDepth();

    /**
     * Returns the maximum allowed depth of icons on this display, in bits per pixel.
     * On some platforms, this may be different than the actual depth of the display.
     *
     * @return the maximum icon depth
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see Device#getDepth
     */
    int getIconDepth();

    /**
     * Returns an array containing the recommended icon sizes.
     *
     * @return the array of icon sizes
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see Decorations#setImages(Image[])
     *
     * @since 3.0
     */
    Point[] getIconSizes();

    /**
     * Returns an array of monitors attached to the device.
     *
     * @return the array of monitors
     *
     * @since 3.0
     */
    IMonitor[] getMonitors();

    /**
     * Returns the primary monitor for that device.
     *
     * @return the primary monitor
     *
     * @since 3.0
     */
    IMonitor getPrimaryMonitor();

    /**
     * Returns a (possibly empty) array containing all shells which have
     * not been disposed and have the receiver as their display.
     *
     * @return the receiver's shells
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    IShell[] getShells();

    /**
     * Gets the synchronizer used by the display.
     *
     * @return the receiver's synchronizer
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.4
     */
    ISynchronizer getSynchronizer();

    /**
     * Returns the thread that has invoked <code>syncExec</code>
     * or null if no such runnable is currently being invoked by
     * the user-interface thread.
     * <p>
     * Note: If a runnable invoked by asyncExec is currently
     * running, this method will return null.
     * </p>
     *
     * @return the receiver's sync-interface thread
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    Thread getSyncThread();

    /**
     * Returns the matching standard color for the given
     * constant, which should be one of the color constants
     * specified in class <code>SWT</code>. Any value other
     * than one of the SWT color constants which is passed
     * in will result in the color black. This color should
     * not be free'd because it was allocated by the system,
     * not the application.
     *
     * @param id the color constant
     * @return the matching color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see SWT
     */
    Color getSystemColor(int id);

    /**
     * Returns the matching standard platform cursor for the given
     * constant, which should be one of the cursor constants
     * specified in class <code>SWT</code>. This cursor should
     * not be free'd because it was allocated by the system,
     * not the application.  A value of <code>null</code> will
     * be returned if the supplied constant is not an SWT cursor
     * constant.
     *
     * @param id the SWT cursor constant
     * @return the corresponding cursor or <code>null</code>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see SWT#CURSOR_ARROW
     * @see SWT#CURSOR_WAIT
     * @see SWT#CURSOR_CROSS
     * @see SWT#CURSOR_APPSTARTING
     * @see SWT#CURSOR_HELP
     * @see SWT#CURSOR_SIZEALL
     * @see SWT#CURSOR_SIZENESW
     * @see SWT#CURSOR_SIZENS
     * @see SWT#CURSOR_SIZENWSE
     * @see SWT#CURSOR_SIZEWE
     * @see SWT#CURSOR_SIZEN
     * @see SWT#CURSOR_SIZES
     * @see SWT#CURSOR_SIZEE
     * @see SWT#CURSOR_SIZEW
     * @see SWT#CURSOR_SIZENE
     * @see SWT#CURSOR_SIZESE
     * @see SWT#CURSOR_SIZESW
     * @see SWT#CURSOR_SIZENW
     * @see SWT#CURSOR_UPARROW
     * @see SWT#CURSOR_IBEAM
     * @see SWT#CURSOR_NO
     * @see SWT#CURSOR_HAND
     *
     * @since 3.0
     */
    Cursor getSystemCursor(int id);

    /**
     * Returns the matching standard platform image for the given
     * constant, which should be one of the icon constants
     * specified in class <code>SWT</code>. This image should
     * not be free'd because it was allocated by the system,
     * not the application.  A value of <code>null</code> will
     * be returned either if the supplied constant is not an
     * SWT icon constant or if the platform does not define an
     * image that corresponds to the constant.
     *
     * @param id the SWT icon constant
     * @return the corresponding image or <code>null</code>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see SWT#ICON_ERROR
     * @see SWT#ICON_INFORMATION
     * @see SWT#ICON_QUESTION
     * @see SWT#ICON_WARNING
     * @see SWT#ICON_WORKING
     *
     * @since 3.0
     */
    Image getSystemImage(int id);

    /**
     * Returns the single instance of the system-provided menu for the application, or
     * <code>null</code> on platforms where no menu is provided for the application.
     *
     * @return the system menu, or <code>null</code>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.7
     */
    IMenu getSystemMenu();

    /**
     * Returns the single instance of the system taskBar or null
     * when there is no system taskBar available for the platform.
     *
     * @return the system taskBar or <code>null</code>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.6
     */
    ITaskBar getSystemTaskBar();

    /**
     * Returns the single instance of the system tray or null
     * when there is no system tray available for the platform.
     *
     * @return the system tray or <code>null</code>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.0
     */
    ITray getSystemTray();

    /**
     * Returns the user-interface thread for the receiver.
     *
     * @return the receiver's user-interface thread
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    Thread getThread();

    /**
     * Returns a boolean indicating whether a touch-aware input device is
     * attached to the system and is ready for use.
     *
     * @return <code>true</code> if a touch-aware input device is detected, or <code>false</code> otherwise
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.7
     */
    boolean getTouchEnabled();

    /**
     * Invokes platform specific functionality to dispose a GC handle.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
     * API for <code>Display</code>. It is marked public only so that it
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
    void internal_dispose_GC(long hDC, GCData data);

    /**
     * Invokes platform specific functionality to allocate a new GC handle.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
     * API for <code>Display</code>. It is marked public only so that it
     * can be shared within the packages provided by SWT. It is not
     * available on all platforms, and should never be called from
     * application code.
     * </p>
     *
     * @param data the platform specific GC data
     * @return the platform specific GC handle
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES if a handle could not be obtained for gc creation</li>
     * </ul>
     *
     * @noreference This method is not intended to be referenced by clients.
     */
    long internal_new_GC(GCData data);

    /**
     * Maps a point from one coordinate system to another.
     * When the control is null, coordinates are mapped to
     * the display.
     * <p>
     * NOTE: On right-to-left platforms where the coordinate
     * systems are mirrored, special care needs to be taken
     * when mapping coordinates from one control to another
     * to ensure the result is correctly mirrored.
     *
     * Mapping a point that is the origin of a rectangle and
     * then adding the width and height is not equivalent to
     * mapping the rectangle.  When one control is mirrored
     * and the other is not, adding the width and height to a
     * point that was mapped causes the rectangle to extend
     * in the wrong direction.  Mapping the entire rectangle
     * instead of just one point causes both the origin and
     * the corner of the rectangle to be mapped.
     * </p>
     *
     * @param from the source <code>Control</code> or <code>null</code>
     * @param to the destination <code>Control</code> or <code>null</code>
     * @param point to be mapped
     * @return point with mapped coordinates
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the Control from or the Control to have been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 2.1.2
     */
    Point map(IControl from, IControl to, Point point);

    /**
     * Maps a point from one coordinate system to another.
     * When the control is null, coordinates are mapped to
     * the display.
     * <p>
     * NOTE: On right-to-left platforms where the coordinate
     * systems are mirrored, special care needs to be taken
     * when mapping coordinates from one control to another
     * to ensure the result is correctly mirrored.
     *
     * Mapping a point that is the origin of a rectangle and
     * then adding the width and height is not equivalent to
     * mapping the rectangle.  When one control is mirrored
     * and the other is not, adding the width and height to a
     * point that was mapped causes the rectangle to extend
     * in the wrong direction.  Mapping the entire rectangle
     * instead of just one point causes both the origin and
     * the corner of the rectangle to be mapped.
     * </p>
     *
     * @param from the source <code>Control</code> or <code>null</code>
     * @param to the destination <code>Control</code> or <code>null</code>
     * @param x coordinates to be mapped
     * @param y coordinates to be mapped
     * @return point with mapped coordinates
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the Control from or the Control to have been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 2.1.2
     */
    Point map(IControl from, IControl to, int x, int y);

    /**
     * Maps a point from one coordinate system to another.
     * When the control is null, coordinates are mapped to
     * the display.
     * <p>
     * NOTE: On right-to-left platforms where the coordinate
     * systems are mirrored, special care needs to be taken
     * when mapping coordinates from one control to another
     * to ensure the result is correctly mirrored.
     *
     * Mapping a point that is the origin of a rectangle and
     * then adding the width and height is not equivalent to
     * mapping the rectangle.  When one control is mirrored
     * and the other is not, adding the width and height to a
     * point that was mapped causes the rectangle to extend
     * in the wrong direction.  Mapping the entire rectangle
     * instead of just one point causes both the origin and
     * the corner of the rectangle to be mapped.
     * </p>
     *
     * @param from the source <code>Control</code> or <code>null</code>
     * @param to the destination <code>Control</code> or <code>null</code>
     * @param rectangle to be mapped
     * @return rectangle with mapped coordinates
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the rectangle is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the Control from or the Control to have been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 2.1.2
     */
    Rectangle map(IControl from, IControl to, Rectangle rectangle);

    /**
     * Maps a point from one coordinate system to another.
     * When the control is null, coordinates are mapped to
     * the display.
     * <p>
     * NOTE: On right-to-left platforms where the coordinate
     * systems are mirrored, special care needs to be taken
     * when mapping coordinates from one control to another
     * to ensure the result is correctly mirrored.
     *
     * Mapping a point that is the origin of a rectangle and
     * then adding the width and height is not equivalent to
     * mapping the rectangle.  When one control is mirrored
     * and the other is not, adding the width and height to a
     * point that was mapped causes the rectangle to extend
     * in the wrong direction.  Mapping the entire rectangle
     * instead of just one point causes both the origin and
     * the corner of the rectangle to be mapped.
     * </p>
     *
     * @param from the source <code>Control</code> or <code>null</code>
     * @param to the destination <code>Control</code> or <code>null</code>
     * @param x coordinates to be mapped
     * @param y coordinates to be mapped
     * @param width coordinates to be mapped
     * @param height coordinates to be mapped
     * @return rectangle with mapped coordinates
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the Control from or the Control to have been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 2.1.2
     */
    Rectangle map(IControl from, IControl to, int x, int y, int width, int height);

    /**
     * Generate a low level system event.
     *
     * <code>post</code> is used to generate low level keyboard
     * and mouse events. The intent is to enable automated UI
     * testing by simulating the input from the user.  Most
     * SWT applications should never need to call this method.
     * <p>
     * Note that this operation can fail when the operating system
     * fails to generate the event for any reason.  For example,
     * this can happen when there is no such key or mouse button
     * or when the system event queue is full.
     * </p>
     * <p>
     * <b>Event Types:</b>
     * <p>KeyDown, KeyUp
     * <p>The following fields in the <code>Event</code> apply:
     * <ul>
     * <li>(in) type KeyDown or KeyUp</li></ul>
     * <p> Either one of:</p>
     * <ul><li>(in) character a character that corresponds to a keyboard key</li>
     * <li>(in) keyCode the key code of the key that was typed,
     *          as defined by the key code constants in class <code>SWT</code></li></ul>
     * <p> Optional (on some platforms): </p>
     * <ul><li>(in) stateMask the state of the keyboard modifier,
     * 			as defined by the key code constants in class <code>SWT</code>
     * </li>
     * </ul>
     * <p>MouseDown, MouseUp</p>
     * <p>The following fields in the <code>Event</code> apply:
     * <ul>
     * <li>(in) type MouseDown or MouseUp
     * <li>(in) button the button that is pressed or released
     * </ul>
     * <p>MouseMove</p>
     * <p>The following fields in the <code>Event</code> apply:
     * <ul>
     * <li>(in) type MouseMove</li>
     * <li>(in) x the x coordinate to move the mouse pointer to in screen coordinates</li>
     * <li>(in) y the y coordinate to move the mouse pointer to in screen coordinates</li>
     * </ul>
     * <p>MouseWheel</p>
     * <p>The following fields in the <code>Event</code> apply:</p>
     * <ul>
     * <li>(in) type MouseWheel</li>
     * <li>(in) detail either SWT.SCROLL_LINE or SWT.SCROLL_PAGE</li>
     * <li>(in) count the number of lines or pages to scroll</li>
     * </ul>
     *
     * @param event the event to be generated
     *
     * @return true if the event was generated or false otherwise
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the event is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.0
     */
    boolean post(Event event);

    /**
     * Reads an event from the operating system's event queue,
     * dispatches it appropriately, and returns <code>true</code>
     * if there is potentially more work to do, or <code>false</code>
     * if the caller can sleep until another event is placed on
     * the event queue.
     * <p>
     * In addition to checking the system event queue, this method also
     * checks if any inter-thread messages (created by <code>syncExec()</code>
     * or <code>asyncExec()</code>) are waiting to be processed, and if
     * so handles them before returning.
     * </p>
     *
     * @return <code>false</code> if the caller can sleep upon return from this method
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_FAILED_EXEC - if an exception occurred while running an inter-thread message</li>
     * </ul>
     *
     * @see #sleep
     * @see #wake
     */
    boolean readAndDispatch();

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when an event of the given type occurs anywhere in
     * a widget. The event type is one of the event constants defined
     * in class <code>SWT</code>.
     *
     * @param eventType the type of event to listen for
     * @param listener the listener which should no longer be notified when the event occurs
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Listener
     * @see SWT
     * @see #addFilter
     * @see #addListener
     *
     * @since 3.0
     */
    void removeFilter(int eventType, Listener listener);

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when an event of the given type occurs. The event type
     * is one of the event constants defined in class <code>SWT</code>.
     *
     * @param eventType the type of event to listen for
     * @param listener the listener which should no longer be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see Listener
     * @see SWT
     * @see #addListener
     *
     * @since 2.0
     */
    void removeListener(int eventType, Listener listener);

    /**
     * Sets the location of the on-screen pointer relative to the top left corner
     * of the screen.  <b>Note: It is typically considered bad practice for a
     * program to move the on-screen pointer location.</b>
     *
     * @param x the new x coordinate for the cursor
     * @param y the new y coordinate for the cursor
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 2.1
     */
    void setCursorLocation(int x, int y);

    /**
     * Sets the location of the on-screen pointer relative to the top left corner
     * of the screen.  <b>Note: It is typically considered bad practice for a
     * program to move the on-screen pointer location.</b>
     *
     * @param point new position
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_NULL_ARGUMENT - if the point is null
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 2.0
     */
    void setCursorLocation(Point point);

    /**
     * Sets the application defined property of the receiver
     * with the specified name to the given argument.
     * <p>
     * Applications may have associated arbitrary objects with the
     * receiver in this fashion. If the objects stored in the
     * properties need to be notified when the display is disposed
     * of, it is the application's responsibility provide a
     * <code>disposeExec()</code> handler which does so.
     * </p>
     *
     * @param key the name of the property
     * @param value the new value for the property
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the key is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #getData(String)
     * @see #disposeExec(Runnable)
     */
    void setData(String key, Object value);

    /**
     * Sets the application defined, display specific data
     * associated with the receiver, to the argument.
     * The <em>display specific data</em> is a single,
     * unnamed field that is stored with every display.
     * <p>
     * Applications may put arbitrary objects in this field. If
     * the object stored in the display specific data needs to
     * be notified when the display is disposed of, it is the
     * application's responsibility provide a
     * <code>disposeExec()</code> handler which does so.
     * </p>
     *
     * @param data the new display specific data
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #getData()
     * @see #disposeExec(Runnable)
     */
    void setData(Object data);

    /**
     * Sets the synchronizer used by the display to be
     * the argument, which can not be null.
     *
     * @param synchronizer the new synchronizer for the display (must not be null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the synchronizer is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_FAILED_EXEC - if an exception occurred while running an inter-thread message</li>
     * </ul>
     */
    void setSynchronizer(ISynchronizer synchronizer);

    /**
     * Sets a callback that will be invoked whenever an exception is thrown by a listener or external
     * callback function. The application may use this to set a global exception handling policy:
     * the most common policies are either to log and discard the exception or to re-throw the
     * exception.
     * <p>
     * The default SWT error handling policy is to rethrow exceptions.
     *
     * @param runtimeExceptionHandler new exception handler to be registered.
     * @since 3.106
     */
    void setRuntimeExceptionHandler(Consumer<RuntimeException> runtimeExceptionHandler);

    /**
     * Returns the current exception handler. It will receive all exceptions thrown by listeners
     * and external callbacks in this display. If code wishes to temporarily replace the exception
     * handler (for example, during a unit test), it is common practice to invoke this method prior
     * to replacing the exception handler so that the old handler may be restored afterward.
     *
     * @return the current exception handler. Never <code>null</code>.
     * @since 3.106
     */
    Consumer<RuntimeException> getRuntimeExceptionHandler();

    /**
     * Sets a callback that will be invoked whenever an error is thrown by a listener or external
     * callback function. The application may use this to set a global exception handling policy:
     * the most common policies are either to log and discard the exception or to re-throw the
     * exception.
     * <p>
     * The default SWT error handling policy is to rethrow exceptions.
     *
     * @param errorHandler new error handler to be registered.
     * @since 3.106
     */
    void setErrorHandler(Consumer<Error> errorHandler);

    /**
     * Returns the current exception handler. It will receive all errors thrown by listeners
     * and external callbacks in this display. If code wishes to temporarily replace the error
     * handler (for example, during a unit test), it is common practice to invoke this method prior
     * to replacing the error handler so that the old handler may be restored afterward.
     *
     * @return the current error handler. Never <code>null</code>.
     * @since 3.106
     */
    Consumer<Error> getErrorHandler();

    /**
     * Causes the user-interface thread to <em>sleep</em> (that is,
     * to be put in a state where it does not consume CPU cycles)
     * until an event is received or it is otherwise awakened.
     *
     * @return <code>true</code> if an event requiring dispatching was placed on the queue.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #wake
     */
    boolean sleep();

    /**
     * Causes the <code>run()</code> method of the runnable to
     * be invoked by the user-interface thread after the specified
     * number of milliseconds have elapsed. If milliseconds is less
     * than zero, the runnable is not executed.
     * <p>
     * Note that at the time the runnable is invoked, widgets
     * that have the receiver as their display may have been
     * disposed. Therefore, it is necessary to check for this
     * case inside the runnable before accessing the widget.
     * </p>
     *
     * @param milliseconds the delay before running the runnable
     * @param runnable code to run on the user-interface thread
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the runnable is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #asyncExec
     */
    void timerExec(int milliseconds, Runnable runnable);

    /**
     * Sends a SWT.PreExternalEventDispatch event.
     *
     * @noreference This method is not intended to be referenced by clients.
     */
    void sendPreExternalEventDispatchEvent();

    /**
     * Sends a SWT.PostExternalEventDispatch event.
     *
     * @noreference This method is not intended to be referenced by clients.
     */
    void sendPostExternalEventDispatchEvent();

    /**
     * Causes the <code>run()</code> method of the runnable to
     * be invoked by the user-interface thread at the next
     * reasonable opportunity. The thread which calls this method
     * is suspended until the runnable completes.  Specifying <code>null</code>
     * as the runnable simply wakes the user-interface thread.
     * <p>
     * Note that at the time the runnable is invoked, widgets
     * that have the receiver as their display may have been
     * disposed. Therefore, it is necessary to check for this
     * case inside the runnable before accessing the widget.
     * </p>
     *
     * @param runnable code to run on the user-interface thread or <code>null</code>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_FAILED_EXEC - if an exception occurred when executing the runnable</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #asyncExec
     */
    void syncExec(Runnable runnable);

    /**
     * Calls the callable on the user-interface thread at the next reasonable
     * opportunity, and returns the its result from this method. The thread which
     * calls this method is suspended until the callable completes.
     * <p>
     * Note that at the time the callable is invoked, widgets that have the receiver
     * as their display may have been disposed. Therefore, it is necessary to check
     * for this case inside the callable before accessing the widget.
     * </p>
     * <p>
     * Any exception that is thrown from the callable is re-thrown in the calling
     * thread. Note: The exception retains its original stack trace from the
     * throwing thread. The call to {@code syncCall} will not be present in the
     * stack trace.
     * </p>
     *
     * @param callable the code to call on the user-interface thread
     *
     * @exception SWTException <code>ERROR_DEVICE_DISPOSED</code> - if the receiver
     *                         has been disposed
     * @exception E            An exception that is thrown by the callable on the
     *                         user-interface thread, and re-thrown on the calling
     *                         thread
     *
     * @see #syncExec(Runnable)
     * @see SwtCallable#call()
     * @since 3.118
     */
    <T, E extends Exception> T syncCall(SwtCallable<T, E> callable) throws E;

    /**
     * Forces all outstanding paint requests for the display
     * to be processed before this method returns.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see Control#update()
     */
    void update();

    /**
     * If the receiver's user-interface thread was <code>sleep</code>ing,
     * causes it to be awakened and start running again. Note that this
     * method may be called from any thread.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #sleep
     */
    void wake();

    /**
     * {@return whether rescaling of shells at runtime when the DPI scaling of a
     * shell's monitor changes is activated for this device}
     * <p>
     * <b>Note:</b> This functionality is only available on Windows. Calling this
     * method on other operating system will always return false.
     *
     * @since 3.127
     */
    boolean isRescalingAtRuntime();

    /**
     * Activates or deactivates rescaling of shells at runtime whenever the DPI
     * scaling of the shell's monitor changes. This is only safe to call as long as
     * no shell has been created for this display. When changing the value after a
     * shell has been created for this display, the effect is undefined.
     * <p>
     * <b>Note:</b> This functionality is only available on Windows. Calling this
     * method on other operating system will have no effect.
     *
     * @param activate whether rescaling shall be activated or deactivated
     * @return whether activating or deactivating the rescaling was successful
     * @since 3.127
     */
    boolean setRescalingAtRuntime(boolean activate);
}
