/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2022 IBM Corporation and others.
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
 *      Christoph Läubrich - Issue #64 - Integration with java.util.concurrent framework
 * *****************************************************************************
 */
package org.eclipse.swt.widgets;

import java.lang.Runtime.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import dev.equo.swt.*;

/**
 * Instances of this class are responsible for managing the
 * connection between SWT and the underlying operating
 * system. Their most important function is to implement
 * the SWT event loop in terms of the platform event model.
 * They also provide various methods for accessing information
 * about the operating system, and have overall control over
 * the operating system resources which SWT allocates.
 * <p>
 * Applications which are built with SWT will <em>almost always</em>
 * require only a single display. In particular, some platforms
 * which SWT supports will not allow more than one <em>active</em>
 * display. In other words, some platforms do not support
 * creating a new display if one already exists that has not been
 * sent the <code>dispose()</code> message.
 * <p>
 * In SWT, the thread which creates a <code>Display</code>
 * instance is distinguished as the <em>user-interface thread</em>
 * for that display.
 * </p>
 * The user-interface thread for a particular display has the
 * following special attributes:
 * <ul>
 * <li>
 * The event loop for that display must be run from the thread.
 * </li>
 * <li>
 * Some SWT API methods (notably, most of the public methods in
 * <code>Widget</code> and its subclasses), may only be called
 * from the thread. (To support multi-threaded user-interface
 * applications, class <code>Display</code> provides inter-thread
 * communication methods which allow threads other than the
 * user-interface thread to request that it perform operations
 * on their behalf.)
 * </li>
 * <li>
 * The thread is not allowed to construct other
 * <code>Display</code>s until that display has been disposed.
 * (Note that, this is in addition to the restriction mentioned
 * above concerning platform support for multiple displays. Thus,
 * the only way to have multiple simultaneously active displays,
 * even on platforms which support it, is to have multiple threads.)
 * </li>
 * </ul>
 * <p>
 * Enforcing these attributes allows SWT to be implemented directly
 * on the underlying operating system's event model. This has
 * numerous benefits including smaller footprint, better use of
 * resources, safer memory management, clearer program logic,
 * better performance, and fewer overall operating system threads
 * required. The down side however, is that care must be taken
 * (only) when constructing multi-threaded applications to use the
 * inter-thread communication mechanisms which this class provides
 * when required.
 * </p><p>
 * All SWT API methods which may only be called from the user-interface
 * thread are distinguished in their documentation by indicating that
 * they throw the "<code>ERROR_THREAD_INVALID_ACCESS</code>"
 * SWT exception.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Close, Dispose, OpenDocument, Settings, Skin</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * @see #syncExec
 * @see #asyncExec
 * @see #wake
 * @see #readAndDispatch
 * @see #sleep
 * @see Device#dispose
 * @see <a href="http://www.eclipse.org/swt/snippets/#display">Display snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartDisplay extends DartDevice implements Executor, IDisplay {

    static byte[] types = { '*', '\0' };

    Display.APPEARANCE appAppearance;

    /* System property to be set for SWT application to use the system's theme */
    static final String USE_SYSTEM_THEME = "org.eclipse.swt.display.useSystemTheme";

    /* Windows and Events */
    Event[] eventQueue;

    EventTable eventTable, filterTable;

    boolean disposing;

    int sendEventCount;

    /* gesture event state */
    double rotation;

    double magnification;

    boolean gestureActive;

    /* touch event state */
    int touchCounter;

    long primaryIdentifier;

    TouchSource[] touchSources;

    /* Sync/Async Widget Communication */
    Synchronizer synchronizer;

    Consumer<RuntimeException> runtimeExceptionHandler = DefaultExceptionHandler.RUNTIME_EXCEPTION_HANDLER;

    Consumer<Error> errorHandler = DefaultExceptionHandler.RUNTIME_ERROR_HANDLER;

    Thread thread;

    boolean allowTimers = true, runAsyncMessages = true;

    /* AWT Invoke Later */
    //$NON-NLS-1$
    static final String RUN_AWT_INVOKE_LATER_KEY = "org.eclipse.swt.internal.runAWTInvokeLater";

    GCData[] contexts;

    Caret currentCaret;

    boolean sendEvent;

    int clickCountButton, clickCount;

    int blinkTime;

    Control currentControl, trackingControl, tooltipControl, ignoreFocusControl;

    Widget tooltipTarget;

    /* Fonts */
    boolean smallFonts;

    boolean useNativeItemHeight;

    Shell[] modalShells;

    Dialog modalDialog;

    Menu menuBar;

    Menu[] menus, popups;

    /* Menu items with ESC key as accelerator need to be handled differently on Cocoa */
    boolean escAsAcceleratorPresent = false;

    boolean isEmbedded;

    static boolean launched = false;

    /* Focus */
    Control focusControl, currentFocusControl;

    int focusEvent;

    int poolCount, loopCount;

    int[] screenID = new int[32];

    boolean lockCursor = true;

    //$NON-NLS-1$
    static final String LOCK_CURSOR = "org.eclipse.swt.internal.lockCursor";

    Combo currentCombo;

    /* Display Shutdown */
    Runnable[] disposeList;

    /* Deferred Layout list */
    Composite[] layoutDeferred;

    int layoutDeferredCount;

    /* System Tray */
    Tray tray;

    TrayItem currentTrayItem;

    Menu trayItemMenu;

    /* Main menu bar and application menu */
    Menu appMenuBar, appMenu;

    /* TaskBar */
    TaskBar taskBar;

    /* System Resources */
    Image errorImage, infoImage, warningImage;

    Cursor[] cursors = new Cursor[SWT.CURSOR_HAND + 1];

    /* System Colors */
    double[][] colors;

    double[] alternateSelectedControlTextColor, selectedControlTextColor;

    private double[] alternateSelectedControlColor, secondarySelectedControlColor;

    /* Key Mappings. */
    static int[][] KeyTable = { /* Keyboard and Mouse Masks */
    { 58, SWT.ALT }, { 56, SWT.SHIFT }, { 59, SWT.CONTROL }, { 55, SWT.COMMAND }, { 61, SWT.ALT }, { 62, SWT.CONTROL }, { 60, SWT.SHIFT }, { 54, SWT.COMMAND }, /* Non-Numeric Keypad Keys */
    { 126, SWT.ARROW_UP }, { 125, SWT.ARROW_DOWN }, { 123, SWT.ARROW_LEFT }, { 124, SWT.ARROW_RIGHT }, { 116, SWT.PAGE_UP }, { 121, SWT.PAGE_DOWN }, { 115, SWT.HOME }, { 119, SWT.END }, //		{??,	SWT.INSERT},
    /* Virtual and Ascii Keys */
    { 51, SWT.BS }, { 36, SWT.CR }, { 117, SWT.DEL }, { 53, SWT.ESC }, { 76, SWT.LF }, { 48, SWT.TAB }, /* Functions Keys */
    { 122, SWT.F1 }, { 120, SWT.F2 }, { 99, SWT.F3 }, { 118, SWT.F4 }, { 96, SWT.F5 }, { 97, SWT.F6 }, { 98, SWT.F7 }, { 100, SWT.F8 }, { 101, SWT.F9 }, { 109, SWT.F10 }, { 103, SWT.F11 }, { 111, SWT.F12 }, { 105, SWT.F13 }, { 107, SWT.F14 }, { 113, SWT.F15 }, { 106, SWT.F16 }, { 64, SWT.F17 }, { 79, SWT.F18 }, { 80, SWT.F19 }, //		{??, SWT.F20},
    /* Numeric Keypad Keys */
    { 67, SWT.KEYPAD_MULTIPLY }, { 69, SWT.KEYPAD_ADD }, { 76, SWT.KEYPAD_CR }, { 78, SWT.KEYPAD_SUBTRACT }, { 65, SWT.KEYPAD_DECIMAL }, { 75, SWT.KEYPAD_DIVIDE }, { 82, SWT.KEYPAD_0 }, { 83, SWT.KEYPAD_1 }, { 84, SWT.KEYPAD_2 }, { 85, SWT.KEYPAD_3 }, { 86, SWT.KEYPAD_4 }, { 87, SWT.KEYPAD_5 }, { 88, SWT.KEYPAD_6 }, { 89, SWT.KEYPAD_7 }, { 91, SWT.KEYPAD_8 }, { 92, SWT.KEYPAD_9 }, { 81, SWT.KEYPAD_EQUAL }, /* Other keys */
    { 57, SWT.CAPS_LOCK }, { 71, SWT.NUM_LOCK }, //		{??,	SWT.SCROLL_LOCK},
    //		{??,	SWT.PAUSE},
    //		{??,	SWT.BREAK},
    //		{??,	SWT.PRINT_SCREEN},
    { 114, SWT.HELP } };

    static String APP_NAME;

    //$NON-NLS-1$
    static String APP_VERSION = "";

    //$NON-NLS-1$
    static final String ADD_WIDGET_KEY = "org.eclipse.swt.internal.addWidget";

    static final byte[] SWT_OBJECT = { 'S', 'W', 'T', '_', 'O', 'B', 'J', 'E', 'C', 'T', '\0' };

    //$NON-NLS-1$
    static final String SET_MODAL_DIALOG = "org.eclipse.swt.internal.modalDialog";

    /* Multiple Displays. */
    static Display Default;

    static Display[] Displays = new Display[1];

    /* Skinning support */
    static final int GROW_SIZE = 1024;

    Widget[] skinList = new Widget[GROW_SIZE];

    int skinCount;

    /* Package Name */
    static final String PACKAGE_PREFIX = "org.eclipse.swt.widgets.";

    /* Timer */
    Runnable[] timerList;

    /* Settings */
    boolean runSettings;

    static final int DEFAULT_BUTTON_INTERVAL = 30;

    /* Display Data */
    Object data;

    String[] keys;

    Object[] values;

    /*
	* TEMPORARY CODE.  Install the runnable that
	* gets the current display. This code will
	* be removed in the future.
	*/
    static {
        DeviceFinder = () -> {
            Device device = getCurrent();
            if (device == null) {
                device = getDefault();
            }
            setDevice(device);
        };
    }

    static {
        configureSystemOptions();
    }

    /*
* TEMPORARY CODE.
*/
    static void setDevice(Device device) {
        CurrentDevice = device;
    }

    static byte[] ascii(String name) {
        int length = name.length();
        char[] chars = new char[length];
        name.getChars(0, length, chars, 0);
        byte[] buffer = new byte[length + 1];
        for (int i = 0; i < length; i++) {
            buffer[i] = (byte) chars[i];
        }
        return buffer;
    }

    static int translateKey(int key) {
        for (int i = 0; i < KeyTable.length; i++) {
            if (KeyTable[i][0] == key)
                return KeyTable[i][1];
        }
        return 0;
    }

    static int untranslateKey(int key) {
        for (int i = 0; i < KeyTable.length; i++) {
            if (KeyTable[i][1] == key)
                return KeyTable[i][0];
        }
        return 0;
    }

    void addContext(GCData context) {
        if (contexts == null)
            contexts = new GCData[12];
        for (int i = 0; i < contexts.length; i++) {
            if (contexts[i] == null || contexts[i] == context) {
                contexts[i] = context;
                return;
            }
        }
        GCData[] newContexts = new GCData[contexts.length + 12];
        newContexts[contexts.length] = context;
        System.arraycopy(contexts, 0, newContexts, 0, contexts.length);
        contexts = newContexts;
    }

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
    public void addFilter(int eventType, Listener listener) {
        checkDevice();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (filterTable == null)
            filterTable = new EventTable();
        filterTable.hook(eventType, listener);
    }

    void addLayoutDeferred(Composite comp) {
        if (layoutDeferred == null)
            layoutDeferred = new Composite[64];
        if (layoutDeferredCount == layoutDeferred.length) {
            Composite[] temp = new Composite[layoutDeferred.length + 64];
            System.arraycopy(layoutDeferred, 0, temp, 0, layoutDeferred.length);
            layoutDeferred = temp;
        }
        layoutDeferred[layoutDeferredCount++] = comp;
    }

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
    public void addListener(int eventType, Listener listener) {
        checkDevice();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            eventTable = new EventTable();
        eventTable.hook(eventType, listener);
    }

    void addMenu(Menu menu) {
        if (menus == null)
            menus = new Menu[12];
        for (int i = 0; i < menus.length; i++) {
            if (menus[i] == null) {
                menus[i] = menu;
                return;
            }
        }
        Menu[] newMenus = new Menu[menus.length + 12];
        newMenus[menus.length] = menu;
        System.arraycopy(menus, 0, newMenus, 0, menus.length);
        menus = newMenus;
    }

    void addPool() {
    }

    void addPopup(Menu menu) {
        if (popups == null)
            popups = new Menu[4];
        int length = popups.length;
        for (int i = 0; i < length; i++) {
            if (popups[i] == menu)
                return;
        }
        int index = 0;
        while (index < length) {
            if (popups[index] == null)
                break;
            index++;
        }
        if (index == length) {
            Menu[] newPopups = new Menu[length + 4];
            System.arraycopy(popups, 0, newPopups, 0, length);
            popups = newPopups;
        }
        popups[index] = menu;
        if (displayBridge != null) {
            displayBridge.sendDisplayUpdate(this);
        }
    }

    void addSkinnableWidget(Widget widget) {
        if (skinCount >= skinList.length) {
            Widget[] newSkinWidgets = new Widget[(skinList.length + 1) * 3 / 2];
            System.arraycopy(skinList, 0, newSkinWidgets, 0, skinList.length);
            skinList = newSkinWidgets;
        }
        skinList[skinCount++] = widget;
    }

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
    public void asyncExec(Runnable runnable) {
        synchronized (DartDisplay.class) {
            if (isDisposed())
                error(SWT.ERROR_DEVICE_DISPOSED);
            synchronizer.asyncExec(runnable);
        }
    }

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
    @Override
    public void execute(Runnable runnable) {
        Objects.requireNonNull(runnable);
        if (isDisposed()) {
            throw new RejectedExecutionException(new SWTException(SWT.ERROR_WIDGET_DISPOSED, null));
        }
        if (thread == Thread.currentThread()) {
            syncExec(runnable);
        } else {
            asyncExec(runnable);
        }
    }

    /**
     * Causes the system hardware to emit a short sound
     * (if it supports this capability).
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public void beep() {
        checkDevice();
    }

    @Override
    public void checkDevice() {
        if (thread == null)
            error(SWT.ERROR_WIDGET_DISPOSED);
        //if (thread != Thread.currentThread())
        //    error(SWT.ERROR_THREAD_INVALID_ACCESS);
        if (isDisposed())
            error(SWT.ERROR_DEVICE_DISPOSED);
    }

    void checkFocus() {
        Control oldControl = currentFocusControl;
        Control newControl = getFocusControl();
        if (oldControl == ignoreFocusControl && newControl == null) {
            /*
		* Bug in Cocoa. On Mac 10.8, a control loses and gains focus
		* when its bounds changes.  The fix is to ignore these events.
		* See Bug 388574 & 433275.
		*/
            return;
        }
        if (oldControl != newControl) {
            if (oldControl != null && !oldControl.isDisposed()) {
                oldControl.getImpl().sendFocusEvent(SWT.FocusOut);
            }
            currentFocusControl = newControl;
            if (newControl != null && !newControl.isDisposed()) {
                newControl.getImpl().sendFocusEvent(SWT.FocusIn);
            }
        }
    }

    /**
     * Checks that this class can be subclassed.
     * <p>
     * IMPORTANT: See the comment in <code>Widget.checkSubclass()</code>.
     * </p>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see Widget#checkSubclass
     */
    public void checkSubclass() {
        if (!DartDisplay.isValidClass(getClass()))
            error(SWT.ERROR_INVALID_SUBCLASS);
    }

    /**
     * Constructs a new instance of this class.
     * <p>
     * Note: The resulting display is marked as the <em>current</em>
     * display. If this is the first display which has been
     * constructed since the application started, it is also
     * marked as the <em>default</em> display.
     * </p>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if called from a thread that already created an existing display</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see #getCurrent
     * @see #getDefault
     * @see Widget#checkSubclass
     * @see Shell
     */
    public DartDisplay(Display api) {
        this(null, api);
    }

    /**
     * Constructs a new instance of this class using the parameter.
     *
     * @param data the device data
     */
    public DartDisplay(DeviceData data, Display api) {
        super(data, api);
    }

    static void checkDisplay(Thread thread, boolean multiple) {
        synchronized (DartDisplay.class) {
            for (int i = 0; i < Displays.length; i++) {
                if (Displays[i] != null) {
                    if (!multiple)
                        SWT.error(SWT.ERROR_NOT_IMPLEMENTED, null, " [multiple displays]");
                    if (((DartDisplay) Displays[i].getImpl()).thread == thread)
                        SWT.error(SWT.ERROR_THREAD_INVALID_ACCESS);
                }
            }
        }
    }

    static String convertToLf(String text) {
        char Cr = '\r';
        char Lf = '\n';
        int length = text.length();
        if (length == 0)
            return text;
        /* Check for an LF or CR/LF.  Assume the rest of the string
	 * is formated that way.  This will not work if the string
	 * contains mixed delimiters. */
        int i = text.indexOf(Lf, 0);
        if (i == -1 || i == 0)
            return text;
        if (text.charAt(i - 1) != Cr)
            return text;
        /* The string is formatted with CR/LF.
	 * Create a new string with the LF line delimiter. */
        i = 0;
        StringBuilder result = new StringBuilder();
        while (i < length) {
            int j = text.indexOf(Cr, i);
            if (j == -1)
                j = length;
            String s = text.substring(i, j);
            result.append(s);
            i = j + 2;
            result.append(Lf);
        }
        return result.toString();
    }

    void clearModal(Shell shell) {
        if (modalShells == null)
            return;
        int index = 0, length = modalShells.length;
        while (index < length) {
            if (modalShells[index] == shell)
                break;
            if (modalShells[index] == null)
                return;
            index++;
        }
        if (index == length)
            return;
        System.arraycopy(modalShells, index + 1, modalShells, index, --length - index);
        modalShells[length] = null;
        if (index == 0 && modalShells[0] == null)
            modalShells = null;
        Shell[] shells = getShells();
        for (int i = 0; i < shells.length; i++) ((DartShell) shells[i].getImpl()).updateModal();
    }

    void clearPool() {
    }

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
    public void close() {
        checkDevice();
        Event event = new Event();
        sendEvent(SWT.Close, event);
        if (event.doit)
            dispose();
    }

    static private void configureSystemOption(String option, boolean value) {
    }

    static private void configureSystemOptions() {
    }

    /**
     * Creates the device in the operating system.  If the device
     * does not have a handle, this method may do nothing depending
     * on the device.
     * <p>
     * This method is called before <code>init</code>.
     * </p>
     *
     * @param data the DeviceData which describes the receiver
     *
     * @see #init
     */
    @Override
    public void create(DeviceData data) {
        checkSubclass();
        checkDisplay(thread = Thread.currentThread(), false);
        createDisplay(data);
        register(this.getApi());
        synchronizer = new Synchronizer(this.getApi());
        if (Default == null)
            Default = this.getApi();
    }

    void createDisplay(DeviceData data) {
    }

    void createMainMenu() {
    }

    static void deregister(Display display) {
        synchronized (DartDisplay.class) {
            for (int i = 0; i < Displays.length; i++) {
                if (display == Displays[i])
                    Displays[i] = null;
            }
        }
    }

    /**
     * Destroys the device in the operating system and releases
     * the device's handle.  If the device does not have a handle,
     * this method may do nothing depending on the device.
     * <p>
     * This method is called after <code>release</code>.
     * </p>
     * @see Device#dispose
     * @see #release
     */
    @Override
    public void destroy() {
        if (this.getApi() == Default)
            Default = null;
        deregister(this.getApi());
        destroyDisplay();
    }

    void destroyDisplay() {
    }

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
    public void disposeExec(Runnable runnable) {
        checkDevice();
        if (disposeList == null)
            disposeList = new Runnable[4];
        for (int i = 0; i < disposeList.length; i++) {
            if (disposeList[i] == null) {
                disposeList[i] = runnable;
                return;
            }
        }
        Runnable[] newDisposeList = new Runnable[disposeList.length + 4];
        System.arraycopy(disposeList, 0, newDisposeList, 0, disposeList.length);
        newDisposeList[disposeList.length] = runnable;
        disposeList = newDisposeList;
    }

    void error(int code) {
        SWT.error(code);
    }

    boolean filterEvent(Event event) {
        if (filterTable != null) {
            int type = event.type;
            sendPreEvent(type);
            try {
                filterTable.sendEvent(event);
            } finally {
                sendPostEvent(type);
            }
        }
        return false;
    }

    boolean filters(int eventType) {
        if (filterTable == null)
            return false;
        return filterTable.hooks(eventType);
    }

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
    public Widget findWidget(long handle) {
        checkDevice();
        return null;
    }

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
    public Widget findWidget(long handle, long id) {
        checkDevice();
        return null;
    }

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
    public Widget findWidget(Widget widget, long id) {
        checkDevice();
        return null;
    }

    /**
     * Returns the display which the given thread is the
     * user-interface thread for, or null if the given thread
     * is not a user-interface thread for any display.  Specifying
     * <code>null</code> as the thread will return <code>null</code>
     * for the display.
     *
     * @param thread the user-interface thread
     * @return the display for the given thread
     */
    public static Display findDisplay(Thread thread) {
        synchronized (DartDisplay.class) {
            for (int i = 0; i < Displays.length; i++) {
                Display display = Displays[i];
                if (display != null && ((DartDisplay) display.getImpl()).thread == thread) {
                    return display;
                }
            }
            return null;
        }
    }

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
    public Shell getActiveShell() {
        checkDevice();
        Control focus = getFocusControl();
        if (focus != null && !focus.isDisposed()) {
            Shell shell = focus.getShell();
            if (shell != null && !shell.isDisposed() && shell.getVisible()) {
                return shell;
            }
        }
        Shell[] currentShells = getShells();
        for (int i = currentShells.length - 1; i >= 0; i--) {
            Shell shell = currentShells[i];
            if (shell != null && !shell.isDisposed() && shell.getVisible()) {
                return shell;
            }
        }
        return null;
    }

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
    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * Returns the display which the currently running thread is
     * the user-interface thread for, or null if the currently
     * running thread is not a user-interface thread for any display.
     *
     * @return the current display
     */
    public static Display getCurrent() {
        return findDisplay(Thread.currentThread());
    }

    public int getCaretBlinkTime() {
        //	checkDevice ();
        return blinkTime;
    }

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
    @Override
    public Rectangle getClientArea() {
        checkDevice();
        return null;
    }

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
    public Control getCursorControl() {
        checkDevice();
        return findControl(false);
    }

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
    public Point getCursorLocation() {
        checkDevice();
        return this.cursorLocation;
    }

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
    public Point[] getCursorSizes() {
        checkDevice();
        return new Point[] { new Point(16, 16) };
    }

    /**
     * Returns the default display. One is created (making the
     * thread that invokes this method its user-interface thread)
     * if it did not already exist.
     *
     * @return the default display
     */
    public static Display getDefault() {
        synchronized (DartDisplay.class) {
            if (Default == null)
                Default = new Display();
            return Default;
        }
    }

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
    public Object getData(String key) {
        checkDevice();
        if (key == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (keys == null)
            return null;
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equals(key))
                return values[i];
        }
        return null;
    }

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
    public Object getData() {
        checkDevice();
        return data;
    }

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
    public int getDismissalAlignment() {
        checkDevice();
        return SWT.RIGHT;
    }

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
    public int getDoubleClickTime() {
        checkDevice();
        return 0;
    }

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
    public Control getFocusControl() {
        checkDevice();
        if (focusControl != null && !focusControl.isDisposed()) {
            return focusControl;
        }
        return null;
    }

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
    public boolean getHighContrast() {
        checkDevice();
        return false;
    }

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
    public int getIconDepth() {
        return getDepth();
    }

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
    public Point[] getIconSizes() {
        checkDevice();
        return new Point[] { new Point(16, 16), new Point(32, 32), new Point(64, 64), new Point(128, 128) };
    }

    /**
     * Returns <code>true</code> if the current OS theme has a dark appearance, else
     * returns <code>false</code>.
     * <p>
     * Note: This operation is a hint and is not supported on platforms that do not
     * have this concept.
     * </p>
     * <p>
     * Note: Windows 10 onwards users can separately configure the theme for OS and
     * Application level and this can be read from the Windows registry. Since the
     * application needs to honor the application level theme, this API reads the
     * Application level theme setting.
     * </p>
     *
     * @return <code>true</code> if the current OS theme has a dark appearance, else
     *         returns <code>false</code>.
     *
     * @since 3.112
     */
    public static boolean isSystemDarkTheme() {
        return false;
    }

    int getLastEventTime() {
        return 0;
    }

    Menu[] getMenus(Decorations shell) {
        if (menus == null)
            return new Menu[0];
        int count = 0;
        for (int i = 0; i < menus.length; i++) {
            Menu menu = menus[i];
            if (menu != null && ((DartMenu) menu.getImpl()).parent == shell)
                count++;
        }
        int index = 0;
        Menu[] result = new Menu[count];
        for (int i = 0; i < menus.length; i++) {
            Menu menu = menus[i];
            if (menu != null && ((DartMenu) menu.getImpl()).parent == shell) {
                result[index++] = menu;
            }
        }
        return result;
    }

    Dialog getModalDialog() {
        return modalDialog;
    }

    /**
     * Returns an array of monitors attached to the device.
     *
     * @return the array of monitors
     *
     * @since 3.0
     */
    public Monitor[] getMonitors() {
        checkDevice();
        {
            Monitor m = new Monitor();
            m.setBounds(bounds);
            m.setClientArea(bounds);
            monitors = new Monitor[] { m };
        }
        return this.monitors;
    }

    /**
     * Returns the primary monitor for that device.
     *
     * @return the primary monitor
     *
     * @since 3.0
     */
    public Monitor getPrimaryMonitor() {
        checkDevice();
        Monitor monitor = new Monitor();
        return monitor;
    }

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
    public Shell[] getShells() {
        return shells.clone();
    }

    static boolean getSheetEnabled() {
        return !"false".equals(System.getProperty("org.eclipse.swt.sheet"));
    }

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
    public Synchronizer getSynchronizer() {
        checkDevice();
        return synchronizer;
    }

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
    public Thread getSyncThread() {
        synchronized (DartDisplay.class) {
            if (isDisposed())
                error(SWT.ERROR_DEVICE_DISPOSED);
            return ((DartSynchronizer) synchronizer.getImpl()).syncThread;
        }
    }

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
    @Override
    public Color getSystemColor(int id) {
        return switch(id) {
            case SWT.COLOR_WHITE ->
                new Color(this.getApi(), 255, 255, 255);
            case SWT.COLOR_BLACK ->
                new Color(this.getApi(), 0, 0, 0);
            case SWT.COLOR_RED ->
                new Color(this.getApi(), 255, 0, 0);
            case SWT.COLOR_DARK_RED ->
                new Color(this.getApi(), 128, 0, 0);
            case SWT.COLOR_GREEN ->
                new Color(this.getApi(), 0, 255, 0);
            case SWT.COLOR_DARK_GREEN ->
                new Color(this.getApi(), 0, 128, 0);
            case SWT.COLOR_YELLOW ->
                new Color(this.getApi(), 255, 255, 0);
            case SWT.COLOR_DARK_YELLOW ->
                new Color(this.getApi(), 128, 128, 0);
            case SWT.COLOR_BLUE ->
                new Color(this.getApi(), 0, 0, 255);
            case SWT.COLOR_DARK_BLUE ->
                new Color(this.getApi(), 0, 0, 128);
            case SWT.COLOR_MAGENTA ->
                new Color(this.getApi(), 255, 0, 255);
            case SWT.COLOR_DARK_MAGENTA ->
                new Color(this.getApi(), 128, 0, 128);
            case SWT.COLOR_CYAN ->
                new Color(this.getApi(), 0, 255, 255);
            case SWT.COLOR_DARK_CYAN ->
                new Color(this.getApi(), 0, 128, 128);
            case SWT.COLOR_GRAY ->
                new Color(this.getApi(), 192, 192, 192);
            case SWT.COLOR_DARK_GRAY ->
                new Color(this.getApi(), 128, 128, 128);
            default ->
                getWidgetColor(id);
        };
    }

    Color getWidgetColor(int id) {
        return switch(id) {
            case SWT.COLOR_WIDGET_DARK_SHADOW ->
                new Color(this.getApi(), 64, 64, 64);
            case SWT.COLOR_WIDGET_NORMAL_SHADOW ->
                new Color(this.getApi(), 128, 128, 128);
            case SWT.COLOR_WIDGET_LIGHT_SHADOW ->
                new Color(this.getApi(), 192, 192, 192);
            case SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW ->
                new Color(this.getApi(), 224, 224, 224);
            case SWT.COLOR_WIDGET_FOREGROUND ->
                new Color(this.getApi(), 0, 0, 0);
            case SWT.COLOR_WIDGET_BACKGROUND ->
                new Color(this.getApi(), 240, 240, 240);
            case SWT.COLOR_WIDGET_BORDER ->
                new Color(this.getApi(), 0, 0, 0);
            case SWT.COLOR_LIST_FOREGROUND ->
                new Color(this.getApi(), 0, 0, 0);
            case SWT.COLOR_LIST_BACKGROUND ->
                new Color(this.getApi(), 255, 255, 255);
            case SWT.COLOR_LIST_SELECTION ->
                new Color(this.getApi(), 51, 153, 255);
            case SWT.COLOR_LIST_SELECTION_TEXT ->
                new Color(this.getApi(), 255, 255, 255);
            case SWT.COLOR_INFO_FOREGROUND ->
                new Color(this.getApi(), 0, 0, 0);
            case SWT.COLOR_INFO_BACKGROUND ->
                new Color(this.getApi(), 255, 255, 225);
            case SWT.COLOR_TITLE_FOREGROUND ->
                new Color(this.getApi(), 255, 255, 255);
            case SWT.COLOR_TITLE_BACKGROUND ->
                new Color(this.getApi(), 51, 102, 204);
            case SWT.COLOR_TITLE_BACKGROUND_GRADIENT ->
                new Color(this.getApi(), 102, 153, 255);
            case SWT.COLOR_TITLE_INACTIVE_FOREGROUND ->
                new Color(this.getApi(), 128, 128, 128);
            case SWT.COLOR_TITLE_INACTIVE_BACKGROUND ->
                new Color(this.getApi(), 192, 192, 192);
            case SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT ->
                new Color(this.getApi(), 213, 213, 213);
            case SWT.COLOR_LINK_FOREGROUND ->
                new Color(this.getApi(), 0, 102, 204);
            case SWT.COLOR_TRANSPARENT ->
                new Color(this.getApi(), 0, 0, 0, 0);
            case SWT.COLOR_TEXT_DISABLED_BACKGROUND ->
                new Color(this.getApi(), 240, 240, 240);
            case SWT.COLOR_WIDGET_DISABLED_FOREGROUND ->
                new Color(this.getApi(), 128, 128, 128);
            default ->
                throw new IllegalStateException("Unexpected color: " + id);
        };
    }

    double[] getWidgetColorRGB(int id) {
        return null;
    }

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
    public Cursor getSystemCursor(int id) {
        checkDevice();
        if (!(0 <= id && id < cursors.length))
            return null;
        if (cursors[id] == null) {
            cursors[id] = new Cursor(this.getApi(), id);
        }
        return cursors[id];
    }

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
    public Image getSystemImage(int id) {
        checkDevice();
        return null;
    }

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
    public Menu getMenuBar() {
        checkDevice();
        if (appMenuBar != null)
            return appMenuBar;
        appMenuBar = new Menu(this.getApi());
        // the menubar will be updated when the Shell or the application activates.
        return appMenuBar;
    }

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
    public Menu getSystemMenu() {
        checkDevice();
        if (appMenu == null) {
        }
        return appMenu;
    }

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
    public Tray getSystemTray() {
        checkDevice();
        if (tray != null)
            return tray;
        return tray = new Tray(this.getApi(), SWT.NONE);
    }

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
    public TaskBar getSystemTaskBar() {
        checkDevice();
        if (taskBar != null)
            return taskBar;
        taskBar = new TaskBar(this.getApi(), SWT.NONE);
        return taskBar;
    }

    /**
     * Used for selection in Table and Tree when in focus.
     * @return Returns the system color used for the face of a selected control in a Table or Tree when in focus
     */
    double[] getAlternateSelectedControlColor() {
        if (alternateSelectedControlColor == null) {
        }
        return alternateSelectedControlColor;
    }

    /**
     * Used for selection in Table and Tree when not in focus.
     * @return Returns the system color used for selected controls in non-key views.
     */
    double[] getSecondarySelectedControlColor() {
        if (secondarySelectedControlColor == null) {
        }
        return secondarySelectedControlColor;
    }

    /**
     * Returns the user-interface thread for the receiver.
     *
     * @return the receiver's user-interface thread
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public Thread getThread() {
        synchronized (DartDisplay.class) {
            if (isDisposed())
                error(SWT.ERROR_DEVICE_DISPOSED);
            return thread;
        }
    }

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
    public boolean getTouchEnabled() {
        checkDevice();
        return true;
    }

    int getToolTipTime() {
        checkDevice();
        //TODO get OS value (NSTooltipManager?)
        return 560;
    }

    Widget getWidget(long id) {
        return null;
    }

    static Widget GetWidget(long id) {
        long[] jniRef = new long[1];
        if (jniRef[0] == 0)
            return null;
        return null;
    }

    boolean hasDefaultButton() {
        return false;
    }

    /**
     * Initializes any internal resources needed by the
     * device.
     * <p>
     * This method is called after <code>create</code>.
     * </p>
     *
     * @see #create
     */
    @Override
    public void init() {
        super.init();
        if ("true".equalsIgnoreCase(System.getProperty(USE_SYSTEM_THEME))) {
        }
        initClasses();
        initColors();
        initFonts();
        setDeviceZoom();
        useNativeItemHeight = initUseNativeItemHeight();
        /*
	 * Feature in Cocoa:  NSApplication.finishLaunching() adds an apple menu to the menu bar that isn't accessible via NSMenu.
	 * If Display objects are created and disposed of multiple times in a single process, another apple menu is added to the menu bar.
	 * It must be called or the dock icon will continue to bounce. So, it should only be called once per process, not just once per
	 * creation of a Display.  Use a static so creation of additional Display objects won't affect the menu bar.
	 */
        if (!DartDisplay.launched) {
            DartDisplay.launched = true;
            /* only add the shutdown hook once */
            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {
                }
            });
        }
        if (blinkTime == 0)
            blinkTime = 560;
        SwtFlutterBridge.initForDisplay(this);
    }

    /**
     * Checks if the native item height should be enforced as a minimum (which is true by default).
     *
     * Newer version of macOS may use a default item height in Table, Tree and List
     * controls that is larger than what is traditionally expected.
     *
     * Enforcing the default height as a minimum may break existing assumptions and
     * render UI elements with a padding that may be considered too large.
     */
    private boolean initUseNativeItemHeight() {
        return Boolean.parseBoolean(System.getProperty("org.eclipse.swt.internal.cocoa.useNativeItemHeight", "true"));
    }

    void addEventMethods(long cls, long proc2, long proc3, long drawRectProc, long hitTestProc, long needsDisplayInRectProc) {
        if (proc3 != 0) {
        }
        if (proc2 != 0) {
        }
        if (needsDisplayInRectProc != 0) {
        }
        if (drawRectProc != 0) {
        }
        if (hitTestProc != 0) {
        }
    }

    void addFrameMethods(long cls, long setFrameOriginProc, long setFrameSizeProc) {
    }

    void addAccessibilityMethods(long cls, long proc2, long proc3, long proc4, long accessibilityHitTestProc) {
    }

    long registerCellSubclass(long cellClass, int size, int align, byte[] types) {
        return 0;
    }

    long createWindowSubclass(long baseClass, String newClass, boolean isDynamic) {
        return 0;
    }

    long createMenuSubclass(long baseClass, String newClass, boolean isDynamic) {
        return 0;
    }

    long createMenuItemSubclass(long baseClass, String newClass, boolean isDynamic) {
        return 0;
    }

    void initClasses() {
        String className;
        className = "SWTBox";
        className = "SWTButton";
        className = "SWTButtonCell";
        className = "SWTCanvasView";
        className = "SWTComboBox";
        className = "SWTDatePicker";
        className = "SWTEditorView";
        className = "SWTImageView";
        className = "SWTImageTextCell";
        className = "SWTOpenSavePanelDelegate";
        className = "SWTOutlineView";
        className = "SWTPanelDelegate";
        className = "SWTPopUpButton";
        className = "SWTProgressIndicator";
        className = "SWTScroller";
        className = "SWTScrollView";
        className = "SWTSearchField";
        className = "SWTSearchFieldCell";
        // Don't subclass NSSecureTextFieldCell -- you'll get an NSException from [NSSecureTextField setCellClass:]!
        className = "SWTSecureTextField";
        className = "SWTSlider";
        className = "SWTStepper";
        className = "SWTTableHeaderCell";
        className = "SWTTableHeaderView";
        className = "SWTTableView";
        className = "SWTTabView";
        className = "SWTTabViewItem";
        className = "SWTTextView";
        className = "SWTTextField";
        className = "SWTTreeItem";
        className = "SWTView";
        className = "SWTWindow";
        className = "SWTPanel";
        className = "SWTToolbar";
        className = "SWTToolbarView";
        className = "SWTWindowDelegate";
    }

    void initColors(boolean ignoreColorChange) {
        if (ignoreColorChange && colors != null) {
            /*
		 * Code to ignore changes to System textColor, textBackgroundColor and controlTextColor
		 */
            double[] color_list_foreground = colors[SWT.COLOR_LIST_FOREGROUND];
            double[] color_list_background = colors[SWT.COLOR_LIST_BACKGROUND];
            double[] color_widget_foreground = colors[SWT.COLOR_WIDGET_FOREGROUND];
            initColors();
            colors[SWT.COLOR_LIST_FOREGROUND] = color_list_foreground;
            colors[SWT.COLOR_LIST_BACKGROUND] = color_list_background;
            colors[SWT.COLOR_WIDGET_FOREGROUND] = color_widget_foreground;
        } else {
            initColors();
        }
    }

    void initColors() {
        colors = new double[SWT.COLOR_WIDGET_DISABLED_FOREGROUND + 1][];
        colors[SWT.COLOR_INFO_FOREGROUND] = getWidgetColorRGB(SWT.COLOR_INFO_FOREGROUND);
        colors[SWT.COLOR_INFO_BACKGROUND] = getWidgetColorRGB(SWT.COLOR_INFO_BACKGROUND);
        colors[SWT.COLOR_TITLE_FOREGROUND] = getWidgetColorRGB(SWT.COLOR_TITLE_FOREGROUND);
        colors[SWT.COLOR_TITLE_BACKGROUND] = getWidgetColorRGB(SWT.COLOR_TITLE_BACKGROUND);
        colors[SWT.COLOR_TITLE_BACKGROUND_GRADIENT] = getWidgetColorRGB(SWT.COLOR_TITLE_BACKGROUND_GRADIENT);
        colors[SWT.COLOR_TITLE_INACTIVE_FOREGROUND] = getWidgetColorRGB(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
        colors[SWT.COLOR_TITLE_INACTIVE_BACKGROUND] = getWidgetColorRGB(SWT.COLOR_TITLE_INACTIVE_BACKGROUND);
        colors[SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT] = getWidgetColorRGB(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT);
        colors[SWT.COLOR_WIDGET_DARK_SHADOW] = getWidgetColorRGB(SWT.COLOR_WIDGET_DARK_SHADOW);
        colors[SWT.COLOR_WIDGET_NORMAL_SHADOW] = getWidgetColorRGB(SWT.COLOR_WIDGET_NORMAL_SHADOW);
        colors[SWT.COLOR_WIDGET_LIGHT_SHADOW] = getWidgetColorRGB(SWT.COLOR_WIDGET_LIGHT_SHADOW);
        colors[SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW] = getWidgetColorRGB(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
        colors[SWT.COLOR_WIDGET_BACKGROUND] = getWidgetColorRGB(SWT.COLOR_WIDGET_BACKGROUND);
        colors[SWT.COLOR_WIDGET_FOREGROUND] = getWidgetColorRGB(SWT.COLOR_WIDGET_FOREGROUND);
        colors[SWT.COLOR_WIDGET_BORDER] = getWidgetColorRGB(SWT.COLOR_WIDGET_BORDER);
        colors[SWT.COLOR_LIST_FOREGROUND] = getWidgetColorRGB(SWT.COLOR_LIST_FOREGROUND);
        colors[SWT.COLOR_LIST_BACKGROUND] = getWidgetColorRGB(SWT.COLOR_LIST_BACKGROUND);
        colors[SWT.COLOR_LIST_SELECTION_TEXT] = getWidgetColorRGB(SWT.COLOR_LIST_SELECTION_TEXT);
        colors[SWT.COLOR_LIST_SELECTION] = getWidgetColorRGB(SWT.COLOR_LIST_SELECTION);
        colors[SWT.COLOR_LINK_FOREGROUND] = getWidgetColorRGB(SWT.COLOR_LINK_FOREGROUND);
        colors[SWT.COLOR_TEXT_DISABLED_BACKGROUND] = getWidgetColorRGB(SWT.COLOR_TEXT_DISABLED_BACKGROUND);
        colors[SWT.COLOR_WIDGET_DISABLED_FOREGROUND] = getWidgetColorRGB(SWT.COLOR_WIDGET_DISABLED_FOREGROUND);
        /* These are set in the getter */
        alternateSelectedControlColor = null;
        secondarySelectedControlColor = null;
    }

    void initFonts() {
        smallFonts = System.getProperty("org.eclipse.swt.internal.carbon.smallFonts") != null;
    }

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
    @Override
    public long internal_new_GC(GCData data) {
        if (isDisposed())
            error(SWT.ERROR_DEVICE_DISPOSED);
        //	NSAffineTransform transform = NSAffineTransform.transform();
        //	NSSize size = handle.size();
        //	transform.translateXBy(0, size.height);
        //	transform.scaleXBy(1, -1);
        //	transform.set();
        if (data != null) {
            int mask = SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT;
            if ((data.style & mask) == 0) {
                data.style |= SWT.LEFT_TO_RIGHT;
            }
            data.device = this.getApi();
            data.background = getSystemColor(SWT.COLOR_WHITE).handle;
            data.foreground = getSystemColor(SWT.COLOR_BLACK).handle;
            data.font = getSystemFont();
        }
        return 0;
    }

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
    @Override
    public void internal_dispose_GC(long hDC, GCData data) {
        if (isDisposed())
            error(SWT.ERROR_DEVICE_DISPOSED);
    }

    boolean isBundled() {
        return false;
    }

    boolean isBundledIconSet() {
        return false;
    }

    static boolean isValidClass(Class<?> clazz) {
        String name = clazz.getName();
        int index = name.lastIndexOf('.');
        return name.substring(0, index + 1).equals(PACKAGE_PREFIX);
    }

    boolean isValidThread() {
        return thread == Thread.currentThread();
    }

    static long getCurrentKeyLayout() {
        return 0;
    }

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
    public boolean post(Event event) {
        synchronized (DartDisplay.class) {
            if (isDisposed())
                error(SWT.ERROR_DEVICE_DISPOSED);
            if (event == null)
                error(SWT.ERROR_NULL_ARGUMENT);
            long eventRef = 0;
            boolean returnValue = false;
            int[] deadKeyState = new int[1];
            int type = event.type;
            switch(type) {
                case SWT.KeyDown:
                case SWT.KeyUp:
                    {
                        short vKey = (short) DartDisplay.untranslateKey(event.keyCode);
                        if (vKey == 0) {
                            long keyLayout = getCurrentKeyLayout();
                            if (keyLayout == 0)
                                return false;
                            int maxStringLength = 256;
                            vKey = -1;
                            char[] output = new char[maxStringLength];
                            for (short i = 0; i <= 0x7F; i++) {
                                deadKeyState[0] = 0;
                                if (output[0] == event.character) {
                                    vKey = i;
                                    break;
                                }
                            }
                            if (vKey == -1) {
                                for (short i = 0; i <= 0x7F; i++) {
                                    deadKeyState[0] = 0;
                                    if (output[0] == event.character) {
                                        vKey = i;
                                        break;
                                    }
                                }
                            }
                        }
                        /**
                         * Bug(?) in UCKeyTranslate:  If event.keyCode doesn't map to a valid SWT constant and event.character is 0 we still need to post an event.
                         * In Carbon, KeyTranslate eventually found a key that generated 0 but UCKeyTranslate never generates 0.
                         * When that happens, post an event from key 127, which does nothing.
                         */
                        if (vKey == -1 && event.character == 0) {
                            vKey = 127;
                        }
                        if (vKey != -1) {
                        }
                        break;
                    }
                case SWT.MouseDown:
                case SWT.MouseMove:
                case SWT.MouseUp:
                    {
                        if (type == SWT.MouseMove) {
                        } else {
                        }
                        break;
                    }
                case SWT.MouseWheel:
                    {
                        break;
                    }
            }
            if (eventRef != 0) {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                }
                returnValue = true;
            }
            return returnValue;
        }
    }

    void postEvent(Event event) {
        /*
	* Place the event at the end of the event queue.
	* This code is always called in the Display's
	* thread so it must be re-enterant but does not
	* need to be synchronized.
	*/
        if (eventQueue == null)
            eventQueue = new Event[4];
        int index = 0;
        int length = eventQueue.length;
        while (index < length) {
            if (eventQueue[index] == null)
                break;
            index++;
        }
        if (index == length) {
            Event[] newQueue = new Event[length + 4];
            System.arraycopy(eventQueue, 0, newQueue, 0, length);
            eventQueue = newQueue;
        }
        eventQueue[index] = event;
    }

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
    public Point map(Control from, Control to, Point point) {
        checkDevice();
        if (point == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        return map(from, to, point.x, point.y);
    }

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
    public Point map(Control from, Control to, int x, int y) {
        checkDevice();
        if (from != null && from.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        if (to != null && to.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        if (from == to)
            return new Point(x, y);
        return DisplayHelper.mapOrigin(from, to, x, y);
    }

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
    public Rectangle map(Control from, Control to, Rectangle rectangle) {
        checkDevice();
        if (rectangle == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        return map(from, to, rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

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
    public Rectangle map(Control from, Control to, int x, int y, int width, int height) {
        checkDevice();
        if (from != null && from.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        if (to != null && to.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        Rectangle rectangle = new Rectangle(x, y, width, height);
        if (from == to)
            return rectangle;
        Point origin = DisplayHelper.mapOrigin(from, to, x, y);
        rectangle.x = origin.x;
        rectangle.y = origin.y;
        return rectangle;
    }

    long observerProc(long observer, long activity, long info) {
        return 0;
    }

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
    public boolean readAndDispatch() {
        checkDevice();
        dev.equo.swt.CrashReporter.checkPendingNativeCrashesIfNeeded();
        dev.equo.swt.FlutterBridge.update();
        addPool();
        runSkin();
        runDeferredLayouts();
        loopCount++;
        boolean events = false;
        try {
            events |= runSettings();
            events |= runTimers();
            events |= runContexts();
            events |= runPopups();
            events |= runPaint();
            events |= runDeferredEvents();
            if (!events) {
                events = isDisposed() || runAsyncMessages(false);
            }
        } finally {
            removePool();
            loopCount--;
        }
        return events;
    }

    static void register(Display display) {
        synchronized (DartDisplay.class) {
            for (int i = 0; i < Displays.length; i++) {
                if (Displays[i] == null) {
                    Displays[i] = display;
                    return;
                }
            }
            Display[] newDisplays = new Display[Displays.length + 4];
            System.arraycopy(Displays, 0, newDisplays, 0, Displays.length);
            newDisplays[Displays.length] = display;
            Displays = newDisplays;
        }
    }

    /**
     * Releases any internal resources back to the operating
     * system and clears all fields except the device handle.
     * <p>
     * Disposes all shells which are currently open on the display.
     * After this method has been invoked, all related related shells
     * will answer <code>true</code> when sent the message
     * <code>isDisposed()</code>.
     * </p><p>
     * When a device is destroyed, resources that were acquired
     * on behalf of the programmer need to be returned to the
     * operating system.  For example, if the device allocated a
     * font to be used as the system font, this font would be
     * freed in <code>release</code>.  Also,to assist the garbage
     * collector and minimize the amount of memory that is not
     * reclaimed when the programmer keeps a reference to a
     * disposed device, all fields except the handle are zero'd.
     * The handle is needed by <code>destroy</code>.
     * </p>
     * This method is called before <code>destroy</code>.
     *
     * @see Device#dispose
     * @see #destroy
     */
    @Override
    public void release() {
        try (ExceptionStash exceptions = new ExceptionStash()) {
            disposing = true;
            try {
                sendEvent(SWT.Dispose, new Event());
            } catch (Error | RuntimeException ex) {
                exceptions.stash(ex);
            }
            for (Shell shell : getShells()) {
                try {
                    if (!shell.isDisposed())
                        shell.dispose();
                } catch (Error | RuntimeException ex) {
                    exceptions.stash(ex);
                }
            }
            try {
                if (tray != null)
                    tray.dispose();
            } catch (Error | RuntimeException ex) {
                exceptions.stash(ex);
            }
            tray = null;
            try {
                if (taskBar != null)
                    taskBar.dispose();
            } catch (Error | RuntimeException ex) {
                exceptions.stash(ex);
            }
            taskBar = null;
            for (; ; ) {
                try {
                    if (!readAndDispatch())
                        break;
                } catch (Error | RuntimeException ex) {
                    exceptions.stash(ex);
                }
            }
            if (disposeList != null) {
                for (Runnable next : disposeList) {
                    if (next == null)
                        continue;
                    try {
                        next.run();
                    } catch (Error | RuntimeException ex) {
                        exceptions.stash(ex);
                    }
                }
            }
            disposeList = null;
            ((DartSynchronizer) synchronizer.getImpl()).releaseSynchronizer();
            synchronizer = null;
            try {
                if (appMenu != null)
                    appMenu.dispose();
            } catch (Error | RuntimeException ex) {
                exceptions.stash(ex);
            }
            appMenu = null;
            try {
                if (appMenuBar != null)
                    appMenuBar.dispose();
            } catch (Error | RuntimeException ex) {
                exceptions.stash(ex);
            }
            appMenuBar = null;
            releaseDisplay();
            super.release();
        }
    }

    void releaseDisplay() {
        /* Release the System Images */
        if (errorImage != null)
            errorImage.dispose();
        if (infoImage != null)
            infoImage.dispose();
        if (warningImage != null)
            warningImage.dispose();
        errorImage = infoImage = warningImage = null;
        currentCaret = null;
        if (caretTimer != null)
            timerExec(-1, caretTimer);
        caretTimer = null;
        /* Release the System Cursors */
        for (int i = 0; i < cursors.length; i++) {
            if (cursors[i] != null)
                cursors[i].dispose();
        }
        cursors = null;
        modalShells = null;
        modalDialog = null;
        menuBar = null;
        menus = null;
        // Clear the menu bar if we created it.
        if (!isEmbedded) {
        }
        // The autorelease pool is cleaned up when we call NSApplication.terminate().
    }

    void removeContext(GCData context) {
        if (contexts == null)
            return;
        int count = 0;
        for (int i = 0; i < contexts.length; i++) {
            if (contexts[i] != null) {
                if (contexts[i] == context) {
                    contexts[i] = null;
                } else {
                    count++;
                }
            }
        }
        if (count == 0)
            contexts = null;
    }

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
    public void removeFilter(int eventType, Listener listener) {
        checkDevice();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (filterTable == null)
            return;
        filterTable.unhook(eventType, listener);
        if (filterTable.size() == 0)
            filterTable = null;
    }

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
    public void removeListener(int eventType, Listener listener) {
        checkDevice();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(eventType, listener);
    }

    void removeMenu(Menu menu) {
        if (menus == null)
            return;
        for (int i = 0; i < menus.length; i++) {
            if (menus[i] == menu) {
                menus[i] = null;
                break;
            }
        }
    }

    void removePool() {
        if (poolCount == 0) {
        }
    }

    void removePopup(Menu menu) {
        if (popups == null)
            return;
        for (int i = 0; i < popups.length; i++) {
            if (popups[i] == menu) {
                popups[i] = null;
                if (displayBridge != null) {
                    displayBridge.sendDisplayUpdate(this);
                }
                return;
            }
        }
    }

    /**
     * Resets the cached alternateSelectedControlColor and secondarySelectedControlColor.
     */
    void resetSelectedControlColors() {
        alternateSelectedControlColor = secondarySelectedControlColor = null;
    }

    boolean runAsyncMessages(boolean all) {
        return ((DartSynchronizer) synchronizer.getImpl()).runAsyncMessages(all);
    }

    boolean runAWTInvokeLater() {
        allowTimers = runAsyncMessages = false;
        allowTimers = runAsyncMessages = true;
        return true;
    }

    boolean runContexts() {
        if (contexts != null) {
            for (int i = 0; i < contexts.length; i++) {
            }
        }
        return false;
    }

    boolean runDeferredEvents() {
        boolean run = false;
        /*
	* Run deferred events.  This code is always
	* called  in the Display's thread so it must
	* be re-enterant need not be synchronized.
	*/
        while (eventQueue != null) {
            /* Take an event off the queue */
            Event event = eventQueue[0];
            if (event == null)
                break;
            int length = eventQueue.length;
            System.arraycopy(eventQueue, 1, eventQueue, 0, --length);
            eventQueue[length] = null;
            /* Run the event */
            Widget widget = event.widget;
            if (widget != null && !widget.isDisposed()) {
                Widget item = event.item;
                if (item == null || !item.isDisposed()) {
                    run = true;
                    widget.notifyListeners(event.type, event);
                }
            }
            /*
		* At this point, the event queue could
		* be null due to a recursive invokation
		* when running the event.
		*/
        }
        /* Clear the queue */
        eventQueue = null;
        return run;
    }

    boolean runDeferredLayouts() {
        if (layoutDeferredCount != 0) {
            Composite[] temp = layoutDeferred;
            int count = layoutDeferredCount;
            layoutDeferred = null;
            layoutDeferredCount = 0;
            for (int i = 0; i < count; i++) {
                Composite comp = temp[i];
                if (!comp.isDisposed())
                    comp.setLayoutDeferred(false);
            }
            return true;
        }
        return false;
    }

    boolean runPaint() {
        return false;
    }

    boolean runPopups() {
        if (popups == null)
            return false;
        boolean result = false;
        while (popups != null) {
            Menu menu = popups[0];
            if (menu == null)
                break;
            int length = popups.length;
            System.arraycopy(popups, 1, popups, 0, --length);
            popups[length] = null;
            runDeferredEvents();
            if (!menu.isDisposed())
                menu.getImpl()._setVisible(true);
            result = true;
        }
        popups = null;
        return result;
    }

    boolean runSettings() {
        if (!runSettings)
            return false;
        runSettings = false;
        boolean ignoreColorChange = false;
        /*
	 * Feature in Cocoa: When dark mode is enabled on OSX version >= 10.10 and a SWT TrayItem (NSStatusItem) is present in the menubar,
	 * changing the OSX appearance or changing the configuration of attached displays causes the textColor and textBackground color to change.
	 * This sets the text foreground of several widgets as white and hence text is invisible. The workaround is to detect this case and prevent
	 * the update of LIST_FOREGROUND, LIST_BACKGROUND and COLOR_WIDGET_FOREGROUND colors.
	 */
        if (tray != null && ((DartTray) tray.getImpl()).itemCount > 0) {
        }
        initColors(ignoreColorChange);
        sendEvent(SWT.Settings, null);
        Shell[] shells = getShells();
        for (int i = 0; i < shells.length; i++) {
            Shell shell = shells[i];
            if (!shell.isDisposed()) {
                ((DartControl) shell.getImpl()).redraw(true);
                shell.layout(true, true);
            }
        }
        return true;
    }

    void setAppAppearance(Display.APPEARANCE newMode) {
    }

    void setWindowsAppearance(Display.APPEARANCE newMode) {
    }

    boolean runSkin() {
        if (skinCount > 0) {
            Widget[] oldSkinWidgets = skinList;
            int count = skinCount;
            skinList = new Widget[GROW_SIZE];
            skinCount = 0;
            if (eventTable != null && eventTable.hooks(SWT.Skin)) {
                for (int i = 0; i < count; i++) {
                    Widget widget = oldSkinWidgets[i];
                    if (widget != null && !widget.isDisposed()) {
                        widget.state &= ~DartWidget.SKIN_NEEDED;
                        oldSkinWidgets[i] = null;
                        Event event = new Event();
                        event.widget = widget;
                        sendEvent(SWT.Skin, event);
                    }
                }
            }
            return true;
        }
        return false;
    }

    boolean runTimers() {
        if (timerList == null)
            return false;
        boolean result = false;
        for (int i = 0; i < timerList.length; i++) {
        }
        return result;
    }

    private void sendJDKInternalEvent(int eventType) {
        sendJDKInternalEvent(eventType, 0);
    }

    /**
     * does sent event with JDK time*
     */
    private void sendJDKInternalEvent(int eventType, int detail) {
        if (eventTable == null || !eventTable.hooks(eventType)) {
            return;
        }
        Event event = new Event();
        event.detail = detail;
        event.display = this.getApi();
        event.type = eventType;
        // time is set for debugging purpose only:
        event.time = (int) (System.nanoTime() / 1000_000L);
        if (!filterEvent(event)) {
            sendEvent(eventTable, event);
        }
    }

    void sendEvent(int eventType, Event event) {
        if (eventTable == null && filterTable == null) {
            return;
        }
        if (event == null)
            event = new Event();
        event.display = this.getApi();
        event.type = eventType;
        if (event.time == 0)
            event.time = getLastEventTime();
        sendEvent(eventTable, event);
    }

    void sendEvent(EventTable table, Event event) {
        try {
            sendEventCount++;
            if (!filterEvent(event)) {
                if (table != null) {
                    int type = event.type;
                    sendPreEvent(type);
                    try {
                        table.sendEvent(event);
                    } finally {
                        sendPostEvent(type);
                    }
                }
            }
        } finally {
            sendEventCount--;
        }
    }

    void sendPreEvent(int eventType) {
        if (eventType != SWT.PreEvent && eventType != SWT.PostEvent && eventType != SWT.PreExternalEventDispatch && eventType != SWT.PostExternalEventDispatch) {
            sendJDKInternalEvent(SWT.PreEvent, eventType);
        }
    }

    void sendPostEvent(int eventType) {
        if (eventType != SWT.PreEvent && eventType != SWT.PostEvent && eventType != SWT.PreExternalEventDispatch && eventType != SWT.PostExternalEventDispatch) {
            sendJDKInternalEvent(SWT.PostEvent, eventType);
        }
    }

    /**
     * Sends a SWT.PreExternalEventDispatch event.
     *
     * @noreference This method is not intended to be referenced by clients.
     */
    public void sendPreExternalEventDispatchEvent() {
        sendJDKInternalEvent(SWT.PreExternalEventDispatch);
    }

    /**
     * Sends a SWT.PostExternalEventDispatch event.
     *
     * @noreference This method is not intended to be referenced by clients.
     */
    public void sendPostExternalEventDispatchEvent() {
        sendJDKInternalEvent(SWT.PostExternalEventDispatch);
    }

    /**
     * Returns the application name.
     *
     * @return the application name
     *
     * @see #setAppName(String)
     *
     * @since 3.6
     */
    public static String getAppName() {
        return APP_NAME;
    }

    /**
     * Returns the application version.
     *
     * @return the application version
     *
     * @see #setAppVersion(String)
     *
     * @since 3.6
     */
    public static String getAppVersion() {
        return APP_VERSION;
    }

    /**
     * Sets the application name to the argument.
     * <p>
     * The application name can be used in several ways,
     * depending on the platform and tools being used.
     * Accessibility tools could ask for the application
     * name. On Windows, if the application name is set
     * to any value other than "SWT" (case insensitive),
     * it is used to set the application user model ID
     * which is used by the OS for taskbar grouping.
     * </p>
     * <p>
     * Specifying <code>null</code> for the name clears it.
     * </p>
     *
     * @see <a href="http://msdn.microsoft.com/en-us/library/windows/desktop/dd378459%28v=vs.85%29.aspx#HOW">AppUserModelID (Windows)</a>
     *
     * @param name the new app name or <code>null</code>
     */
    public static void setAppName(String name) {
        APP_NAME = name;
    }

    /**
     * Sets the application version to the argument.
     *
     * @param version the new app version
     *
     * @since 3.6
     */
    public static void setAppVersion(String version) {
        APP_VERSION = version;
    }

    //TODO - use custom timer instead of timerExec
    Runnable caretTimer = new Runnable() {

        @Override
        public void run() {
            if (currentCaret != null) {
                if (currentCaret == null || currentCaret.isDisposed())
                    return;
                if (currentCaret.getImpl().blinkCaret()) {
                    int blinkRate = ((DartCaret) currentCaret.getImpl()).blinkRate;
                    if (blinkRate != 0)
                        timerExec(blinkRate, this);
                } else {
                    currentCaret = null;
                }
            }
        }
    };

    public void setCurrentCaret(Caret caret) {
        currentCaret = caret;
        int blinkRate = currentCaret != null ? ((DartCaret) currentCaret.getImpl()).blinkRate : -1;
        timerExec(blinkRate, caretTimer);
    }

    void setCursor(Control control) {
        Cursor cursor = null;
        if (control != null && !control.isDisposed())
            cursor = control.getImpl().findCursor();
        if (cursor == null) {
            cursor = getSystemCursor(SWT.CURSOR_ARROW);
        }
        lockCursor = false;
        lockCursor = true;
    }

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
    public void setCursorLocation(int x, int y) {
        Point newValue = new Point(x, y);
        checkDevice();
        Event e = new Event();
        e.type = SWT.MouseMove;
        e.x = x;
        e.y = y;
        this.cursorLocation = newValue;
        post(e);
    }

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
    public void setCursorLocation(Point point) {
        checkDevice();
        if (point == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        setCursorLocation(point.x, point.y);
    }

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
    public void setData(String key, Object value) {
        checkDevice();
        if (key == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (key.equals(ADD_WIDGET_KEY)) {
            Object[] data = (Object[]) value;
            Widget widget = (Widget) data[1];
            if (widget == null) {
            } else {
            }
        }
        if (key.equals(SET_MODAL_DIALOG)) {
            setModalDialog(value != null ? (Dialog) value : null);
        }
        if (key.equals(LOCK_CURSOR)) {
            lockCursor = ((Boolean) value).booleanValue();
        }
        if (key.equals(RUN_AWT_INVOKE_LATER_KEY)) {
            if (value != null) {
                value = runAWTInvokeLater();
            }
        }
        /* Remove the key/value pair */
        if (value == null) {
            if (keys == null)
                return;
            int index = 0;
            while (index < keys.length && !keys[index].equals(key)) index++;
            if (index == keys.length)
                return;
            if (keys.length == 1) {
                keys = null;
                values = null;
            } else {
                String[] newKeys = new String[keys.length - 1];
                Object[] newValues = new Object[values.length - 1];
                System.arraycopy(keys, 0, newKeys, 0, index);
                System.arraycopy(keys, index + 1, newKeys, index, newKeys.length - index);
                System.arraycopy(values, 0, newValues, 0, index);
                System.arraycopy(values, index + 1, newValues, index, newValues.length - index);
                keys = newKeys;
                values = newValues;
            }
            return;
        }
        /* Add the key/value pair */
        if (keys == null) {
            keys = new String[] { key };
            values = new Object[] { value };
            return;
        }
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equals(key)) {
                values[i] = value;
                return;
            }
        }
        String[] newKeys = new String[keys.length + 1];
        Object[] newValues = new Object[values.length + 1];
        System.arraycopy(keys, 0, newKeys, 0, keys.length);
        System.arraycopy(values, 0, newValues, 0, values.length);
        newKeys[keys.length] = key;
        newValues[values.length] = value;
        keys = newKeys;
        values = newValues;
    }

    void setDeviceZoom() {
        DPIUtil.setDeviceZoom(getDeviceZoom());
    }

    static void cancelRootMenuTracking() {
    }

    void setMenuBar(Menu menu) {
        // If passed a null menu bar don't clear out the menu bar, but switch back to the
        // application menu bar instead, if it exists.  If the app menu bar is already active
        // we jump out without harming the current menu bar.
        if (menu == null)
            menu = appMenuBar;
        if (menu == menuBar)
            return;
        menuBar = menu;
        /*
	* For some reason, NSMenu.cancelTracking() does not dismisses
	* the menu right away when the menu bar is set in a stacked
	* event loop. The fix is to use CancelMenuTracking() instead.
	*/
        //	menubar.cancelTracking();
        cancelRootMenuTracking();
        //set parent of each item to NULL and add them to menubar
        if (menu != null) {
            MenuItem[] items = menu.getItems();
            for (int i = 0; i < items.length; i++) {
            }
        }
    }

    void setModalDialog(Dialog modalDialog) {
    }

    void setModalShell(Shell shell) {
        if (modalShells == null)
            modalShells = new Shell[4];
        int index = 0, length = modalShells.length;
        while (index < length) {
            if (modalShells[index] == shell)
                return;
            if (modalShells[index] == null)
                break;
            index++;
        }
        if (index == length) {
            Shell[] newModalShells = new Shell[length + 4];
            System.arraycopy(modalShells, 0, newModalShells, 0, length);
            modalShells = newModalShells;
        }
        modalShells[index] = shell;
        Shell[] shells = getShells();
        for (int i = 0; i < shells.length; i++) ((DartShell) shells[i].getImpl()).updateModal();
    }

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
    public void setData(Object data) {
        checkDevice();
        this.data = data;
    }

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
    public void setSynchronizer(Synchronizer synchronizer) {
        checkDevice();
        if (synchronizer == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (synchronizer == this.synchronizer)
            return;
        Synchronizer oldSynchronizer;
        synchronized (DartDisplay.class) {
            oldSynchronizer = this.synchronizer;
            this.synchronizer = synchronizer;
        }
        if (oldSynchronizer != null) {
            ((DartSynchronizer) oldSynchronizer.getImpl()).moveAllEventsTo(synchronizer);
        }
    }

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
    public final void setRuntimeExceptionHandler(Consumer<RuntimeException> runtimeExceptionHandler) {
        checkDevice();
        this.runtimeExceptionHandler = Objects.requireNonNull(runtimeExceptionHandler);
    }

    /**
     * Returns the current exception handler. It will receive all exceptions thrown by listeners
     * and external callbacks in this display. If code wishes to temporarily replace the exception
     * handler (for example, during a unit test), it is common practice to invoke this method prior
     * to replacing the exception handler so that the old handler may be restored afterward.
     *
     * @return the current exception handler. Never <code>null</code>.
     * @since 3.106
     */
    public final Consumer<RuntimeException> getRuntimeExceptionHandler() {
        return runtimeExceptionHandler;
    }

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
    public final void setErrorHandler(Consumer<Error> errorHandler) {
        checkDevice();
        this.errorHandler = Objects.requireNonNull(errorHandler);
    }

    /**
     * Returns the current exception handler. It will receive all errors thrown by listeners
     * and external callbacks in this display. If code wishes to temporarily replace the error
     * handler (for example, during a unit test), it is common practice to invoke this method prior
     * to replacing the error handler so that the old handler may be restored afterward.
     *
     * @return the current error handler. Never <code>null</code>.
     * @since 3.106
     */
    public final Consumer<Error> getErrorHandler() {
        return errorHandler;
    }

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
    public boolean sleep() {
        checkDevice();
        if (!((DartSynchronizer) synchronizer.getImpl()).isMessagesEmpty())
            return true;
        sendPreExternalEventDispatchEvent();
        try {
            addPool();
            allowTimers = runAsyncMessages = false;
            allowTimers = runAsyncMessages = true;
        } finally {
            removePool();
        }
        sendPostExternalEventDispatchEvent();
        return true;
    }

    int sourceProc(int info) {
        return 0;
    }

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
    public void syncExec(Runnable runnable) {
        Synchronizer synchronizer;
        synchronized (DartDisplay.class) {
            if (isDisposed())
                error(SWT.ERROR_DEVICE_DISPOSED);
            synchronizer = this.synchronizer;
        }
        synchronizer.syncExec(runnable);
    }

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
    public <T, E extends Exception> T syncCall(SwtCallable<T, E> callable) throws E {
        Objects.nonNull(callable);
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Exception> ex = new AtomicReference<>();
        syncExec(() -> {
            try {
                result.setPlain(callable.call());
            } catch (Exception e) {
                ex.setPlain(e);
            }
        });
        if (ex.getPlain() != null) {
            @SuppressWarnings("unchecked")
            E e = (E) ex.getPlain();
            throw e;
        }
        return result.getPlain();
    }

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
     *    <li>ERROR_NO_HANDLES if a handle could not be obtained for timer creation</li>
     * </ul>
     *
     * @see #asyncExec
     */
    public void timerExec(int milliseconds, Runnable runnable) {
        checkDevice();
        if (runnable == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        TimerTask existing = _timerExecTasks.remove(runnable);
        if (existing != null)
            existing.cancel();
        if (milliseconds < 0)
            return;
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                _timerExecTasks.remove(runnable);
                asyncExec(runnable);
            }
        };
        _timerExecTasks.put(runnable, task);
        _timerExecTimer.schedule(task, milliseconds);
    }

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
    public void update() {
        checkDevice();
        Shell[] shells = getShells();
        for (int i = 0; i < shells.length; i++) {
            Shell shell = shells[i];
            if (!shell.isDisposed())
                ((DartControl) shell.getImpl()).update(true);
        }
    }

    void updateDefaultButton() {
    }

    void updateQuitMenu() {
        // If we did not create the menu bar, don't modify it.
        if (isEmbedded)
            return;
        boolean enabled = true;
        Shell[] shells = getShells();
        int mask = SWT.PRIMARY_MODAL | SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL;
        for (int i = 0; i < shells.length; i++) {
            Shell shell = shells[i];
            if ((shell.style & mask) != 0 && shell.isVisible()) {
                enabled = false;
                break;
            }
        }
    }

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
    public void wake() {
        synchronized (DartDisplay.class) {
            if (isDisposed())
                error(SWT.ERROR_DEVICE_DISPOSED);
            if (thread == Thread.currentThread())
                return;
            wakeThread();
        }
    }

    void wakeThread() {
    }

    Control findControl(boolean checkTrim) {
        return null;
    }

    static boolean isActivateShellOnForceFocus() {
        //$NON-NLS-1$
        return "true".equals(System.getProperty("org.eclipse.swt.internal.activateShellOnForceFocus", "true"));
    }

    /**
     * {@return whether rescaling of shells at runtime when the DPI scaling of a
     * shell's monitor changes is activated for this device}
     * <p>
     * <b>Note:</b> This functionality is only available on Windows. Calling this
     * method on other operating system will always return false.
     *
     * @since 3.127
     */
    public boolean isRescalingAtRuntime() {
        return false;
    }

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
     * @deprecated this method should not be used as it needs to be called already
     *             during instantiation to take proper effect
     */
    @Deprecated(since = "2025-03", forRemoval = true)
    public boolean setRescalingAtRuntime(boolean activate) {
        // not implemented for Cocoa
        return false;
    }

    Point cursorLocation = new Point(0, 0);

    Point[] cursorSizes = new Point[0];

    Point[] iconSizes = new Point[0];

    Monitor[] monitors = new Monitor[0];

    Shell[] shells = new Shell[0];

    public Event[] _eventQueue() {
        return eventQueue;
    }

    public EventTable _eventTable() {
        return eventTable;
    }

    public void _eventTable(EventTable eventTable) {
        this.eventTable = eventTable;
    }

    public EventTable _filterTable() {
        return filterTable;
    }

    public boolean _disposing() {
        return disposing;
    }

    public int _sendEventCount() {
        return sendEventCount;
    }

    public double _rotation() {
        return rotation;
    }

    public double _magnification() {
        return magnification;
    }

    public boolean _gestureActive() {
        return gestureActive;
    }

    public int _touchCounter() {
        return touchCounter;
    }

    public long _primaryIdentifier() {
        return primaryIdentifier;
    }

    public TouchSource[] _touchSources() {
        return touchSources;
    }

    public Synchronizer _synchronizer() {
        return synchronizer;
    }

    public Consumer<RuntimeException> _runtimeExceptionHandler() {
        return runtimeExceptionHandler;
    }

    public Consumer<Error> _errorHandler() {
        return errorHandler;
    }

    public Thread _thread() {
        return thread;
    }

    public boolean _allowTimers() {
        return allowTimers;
    }

    public boolean _runAsyncMessages() {
        return runAsyncMessages;
    }

    public GCData[] _contexts() {
        return contexts;
    }

    public Caret _currentCaret() {
        return currentCaret;
    }

    public boolean _sendEvent() {
        return sendEvent;
    }

    public int _clickCountButton() {
        return clickCountButton;
    }

    public int _clickCount() {
        return clickCount;
    }

    public int _blinkTime() {
        return blinkTime;
    }

    public Control _currentControl() {
        return currentControl;
    }

    public Control _trackingControl() {
        return trackingControl;
    }

    public Control _tooltipControl() {
        return tooltipControl;
    }

    public Control _ignoreFocusControl() {
        return ignoreFocusControl;
    }

    public Widget _tooltipTarget() {
        return tooltipTarget;
    }

    public boolean _smallFonts() {
        return smallFonts;
    }

    public boolean _useNativeItemHeight() {
        return useNativeItemHeight;
    }

    public Shell[] _modalShells() {
        return modalShells;
    }

    public Dialog _modalDialog() {
        return modalDialog;
    }

    public Menu _menuBar() {
        return menuBar;
    }

    public Menu[] _menus() {
        return menus;
    }

    public Menu[] _popups() {
        return popups;
    }

    public boolean _escAsAcceleratorPresent() {
        return escAsAcceleratorPresent;
    }

    public boolean _isEmbedded() {
        return isEmbedded;
    }

    public Control _focusControl() {
        return focusControl;
    }

    public Control _currentFocusControl() {
        return currentFocusControl;
    }

    public int _focusEvent() {
        return focusEvent;
    }

    public int _poolCount() {
        return poolCount;
    }

    public int _loopCount() {
        return loopCount;
    }

    public int[] _screenID() {
        return screenID;
    }

    public boolean _lockCursor() {
        return lockCursor;
    }

    public Combo _currentCombo() {
        return currentCombo;
    }

    public Runnable[] _disposeList() {
        return disposeList;
    }

    public Composite[] _layoutDeferred() {
        return layoutDeferred;
    }

    public int _layoutDeferredCount() {
        return layoutDeferredCount;
    }

    public Tray _tray() {
        return tray;
    }

    public TrayItem _currentTrayItem() {
        return currentTrayItem;
    }

    public Menu _trayItemMenu() {
        return trayItemMenu;
    }

    public Menu _appMenuBar() {
        return appMenuBar;
    }

    public Menu _appMenu() {
        return appMenu;
    }

    public TaskBar _taskBar() {
        return taskBar;
    }

    public Image _errorImage() {
        return errorImage;
    }

    public Image _infoImage() {
        return infoImage;
    }

    public Image _warningImage() {
        return warningImage;
    }

    public Cursor[] _cursors() {
        return cursors;
    }

    public double[][] _colors() {
        return colors;
    }

    public double[] _alternateSelectedControlTextColor() {
        return alternateSelectedControlTextColor;
    }

    public double[] _selectedControlTextColor() {
        return selectedControlTextColor;
    }

    public Widget[] _skinList() {
        return skinList;
    }

    public int _skinCount() {
        return skinCount;
    }

    public Runnable[] _timerList() {
        return timerList;
    }

    public boolean _runSettings() {
        return runSettings;
    }

    public Object _data() {
        return data;
    }

    public String[] _keys() {
        return keys;
    }

    public Object[] _values() {
        return values;
    }

    public Runnable _caretTimer() {
        return caretTimer;
    }

    public Point _cursorLocation() {
        return cursorLocation;
    }

    public Point[] _cursorSizes() {
        return cursorSizes;
    }

    public Point[] _iconSizes() {
        return iconSizes;
    }

    public Monitor[] _monitors() {
        return monitors;
    }

    public Shell[] _shells() {
        return shells;
    }

    Rectangle bounds = new Rectangle(0, 0, 1920, 1080);

    boolean performKeyEquivalent(Object window, Object nsEvent) {
        return false;
    }

    void setModalDialog(Dialog window, Object panel) {
    }

    SwtFlutterBridge displayBridge;

    void setBridge(SwtFlutterBridge bridge) {
        this.displayBridge = bridge;
    }

    void addShell(Shell shell) {
        for (Shell s : shells) {
            if (s == shell)
                return;
        }
        Shell[] newShells = new Shell[shells.length + 1];
        System.arraycopy(shells, 0, newShells, 0, shells.length);
        newShells[shells.length] = shell;
        shells = newShells;
    }

    void removeShell(Shell shell) {
        int count = 0;
        for (Shell s : shells) if (s != shell)
            count++;
        Shell[] newShells = new Shell[count];
        int i = 0;
        for (Shell s : shells) if (s != shell)
            newShells[i++] = s;
        shells = newShells;
    }

    Map<Runnable, TimerTask> _timerExecTasks = new HashMap<>();

    Timer _timerExecTimer = new Timer(true);

    ArrayList<ToolTip> activeTooltips = new ArrayList<>();

    public void _addActiveTooltip(ToolTip tip) {
        if (!activeTooltips.contains(tip))
            activeTooltips.add(tip);
        if (displayBridge != null)
            displayBridge.sendDisplayUpdate(this);
    }

    public void _removeActiveTooltip(ToolTip tip) {
        activeTooltips.remove(tip);
        if (displayBridge != null)
            displayBridge.sendDisplayUpdate(this);
    }

    public ToolTip[] _activeTooltips() {
        return activeTooltips.stream().filter(t -> t != null && !t.isDisposed()).toArray(ToolTip[]::new);
    }

    public Display getApi() {
        if (api == null)
            api = Display.createApi(this);
        return (Display) api;
    }
}
