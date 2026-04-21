/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2016 IBM Corporation and others.
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
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import dev.equo.swt.*;

/**
 * Instances of this class represent the "windows"
 * which the desktop or "window manager" is managing.
 * Instances that do not have a parent (that is, they
 * are built using the constructor, which takes a
 * <code>Display</code> as the argument) are described
 * as <em>top level</em> shells. Instances that do have
 * a parent are described as <em>secondary</em> or
 * <em>dialog</em> shells.
 * <p>
 * Instances are always displayed in one of the maximized,
 * minimized or normal states:</p>
 * <ul>
 * <li>
 * When an instance is marked as <em>maximized</em>, the
 * window manager will typically resize it to fill the
 * entire visible area of the display, and the instance
 * is usually put in a state where it can not be resized
 * (even if it has style <code>RESIZE</code>) until it is
 * no longer maximized.
 * </li><li>
 * When an instance is in the <em>normal</em> state (neither
 * maximized or minimized), its appearance is controlled by
 * the style constants which were specified when it was created
 * and the restrictions of the window manager (see below).
 * </li><li>
 * When an instance has been marked as <em>minimized</em>,
 * its contents (client area) will usually not be visible,
 * and depending on the window manager, it may be
 * "iconified" (that is, replaced on the desktop by a small
 * simplified representation of itself), relocated to a
 * distinguished area of the screen, or hidden. Combinations
 * of these changes are also possible.
 * </li>
 * </ul>
 * <p>
 * The <em>modality</em> of an instance may be specified using
 * style bits. The modality style bits are used to determine
 * whether input is blocked for other shells on the display.
 * The <code>PRIMARY_MODAL</code> style allows an instance to block
 * input to its parent. The <code>APPLICATION_MODAL</code> style
 * allows an instance to block input to every other shell in the
 * display. The <code>SYSTEM_MODAL</code> style allows an instance
 * to block input to all shells, including shells belonging to
 * different applications.
 * </p><p>
 * Note: The styles supported by this class are treated
 * as <em>HINT</em>s, since the window manager for the
 * desktop on which the instance is visible has ultimate
 * control over the appearance and behavior of decorations
 * and modality. For example, some window managers only
 * support resizable windows and will always assume the
 * RESIZE style, even if it is not set. In addition, if a
 * modality style is not supported, it is "upgraded" to a
 * more restrictive modality style that is supported. For
 * example, if <code>PRIMARY_MODAL</code> is not supported,
 * it would be upgraded to <code>APPLICATION_MODAL</code>.
 * A modality style may also be "downgraded" to a less
 * restrictive style. For example, most operating systems
 * no longer support <code>SYSTEM_MODAL</code> because
 * it can freeze up the desktop, so this is typically
 * downgraded to <code>APPLICATION_MODAL</code>.</p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>BORDER, CLOSE, MIN, MAX, NO_MOVE, NO_TRIM, RESIZE, TITLE, ON_TOP, TOOL, SHEET</dd>
 * <dd>APPLICATION_MODAL, MODELESS, PRIMARY_MODAL, SYSTEM_MODAL</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Activate, Close, Deactivate, Deiconify, Iconify</dd>
 * </dl>
 * <p>
 * Class <code>SWT</code> provides two "convenience constants"
 * for the most commonly required style combinations:</p>
 * <dl>
 * <dt><code>SHELL_TRIM</code></dt>
 * <dd>
 * the result of combining the constants which are required
 * to produce a typical application top level shell: (that
 * is, <code>CLOSE | TITLE | MIN | MAX | RESIZE</code>)
 * </dd>
 * <dt><code>DIALOG_TRIM</code></dt>
 * <dd>
 * the result of combining the constants which are required
 * to produce a typical application dialog shell: (that
 * is, <code>TITLE | CLOSE | BORDER</code>)
 * </dd>
 * </dl>
 * <p>
 * Note: Only one of the styles APPLICATION_MODAL, MODELESS,
 * PRIMARY_MODAL and SYSTEM_MODAL may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see Decorations
 * @see SWT
 * @see <a href="http://www.eclipse.org/swt/snippets/#shell">Shell snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartShell extends DartDecorations implements IShell {

    long hostWindowClass;

    long tooltipOwner, tooltipTag, tooltipUserData;

    int glContextCount;

    boolean opened, moved, resized, fullScreen, center, deferFlushing, scrolling, isPopup;

    Control lastActive;

    Rectangle normalBounds;

    boolean keyInputHappened;

    ToolBar toolBar;

    MenuItem escMenuItem;

    static int DEFAULT_CLIENT_WIDTH = -1;

    static int DEFAULT_CLIENT_HEIGHT = -1;

    /**
     * Constructs a new instance of this class. This is equivalent
     * to calling <code>Shell((Display) null)</code>.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     */
    public DartShell(Shell api) {
        this((Display) null, api);
    }

    /**
     * Constructs a new instance of this class given only the style
     * value describing its behavior and appearance. This is equivalent
     * to calling <code>Shell((Display) null, style)</code>.
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
     * @param style the style of control to construct
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see SWT#BORDER
     * @see SWT#CLOSE
     * @see SWT#MIN
     * @see SWT#MAX
     * @see SWT#RESIZE
     * @see SWT#TITLE
     * @see SWT#TOOL
     * @see SWT#NO_TRIM
     * @see SWT#NO_MOVE
     * @see SWT#SHELL_TRIM
     * @see SWT#DIALOG_TRIM
     * @see SWT#ON_TOP
     * @see SWT#MODELESS
     * @see SWT#PRIMARY_MODAL
     * @see SWT#APPLICATION_MODAL
     * @see SWT#SYSTEM_MODAL
     * @see SWT#SHEET
     */
    public DartShell(int style, Shell api) {
        this((Display) null, style, api);
    }

    /**
     * Constructs a new instance of this class given only the display
     * to create it on. It is created with style <code>SWT.SHELL_TRIM</code>.
     * <p>
     * Note: Currently, null can be passed in for the display argument.
     * This has the effect of creating the shell on the currently active
     * display if there is one. If there is no current display, the
     * shell is created on a "default" display. <b>Passing in null as
     * the display argument is not considered to be good coding style,
     * and may not be supported in a future release of SWT.</b>
     * </p>
     *
     * @param display the display to create the shell on
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     */
    public DartShell(Display display, Shell api) {
        this(display, SWT.SHELL_TRIM, api);
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
     * This has the effect of creating the shell on the currently active
     * display if there is one. If there is no current display, the
     * shell is created on a "default" display. <b>Passing in null as
     * the display argument is not considered to be good coding style,
     * and may not be supported in a future release of SWT.</b>
     * </p>
     *
     * @param display the display to create the shell on
     * @param style the style of control to construct
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see SWT#BORDER
     * @see SWT#CLOSE
     * @see SWT#MIN
     * @see SWT#MAX
     * @see SWT#RESIZE
     * @see SWT#TITLE
     * @see SWT#TOOL
     * @see SWT#NO_TRIM
     * @see SWT#NO_MOVE
     * @see SWT#SHELL_TRIM
     * @see SWT#DIALOG_TRIM
     * @see SWT#ON_TOP
     * @see SWT#MODELESS
     * @see SWT#PRIMARY_MODAL
     * @see SWT#APPLICATION_MODAL
     * @see SWT#SYSTEM_MODAL
     * @see SWT#SHEET
     */
    public DartShell(Display display, int style, Shell api) {
        this(display, null, style, 0, false, api);
    }

    DartShell(Display display, Shell parent, int style, long handle, boolean embedded, Shell api) {
        super(api);
        checkSubclass();
        if (display == null)
            display = DartDisplay.getCurrent();
        if (display == null)
            display = DartDisplay.getDefault();
        if (!((DartDisplay) display.getImpl()).isValidThread()) {
            error(SWT.ERROR_THREAD_INVALID_ACCESS);
        }
        if (parent != null && parent.isDisposed()) {
            error(SWT.ERROR_INVALID_ARGUMENT);
        }
        if (!DartDisplay.getSheetEnabled()) {
            this.center = parent != null && (style & SWT.SHEET) != 0;
        }
        this.getApi().style = checkStyle(parent, style);
        this.parent = parent;
        this.display = display;
        if (handle != 0) {
            if (embedded) {
            } else {
                getApi().state |= FOREIGN_HANDLE;
            }
        }
        reskinWidget();
        createWidget();
    }

    /**
     * Constructs a new instance of this class given only its
     * parent. It is created with style <code>SWT.DIALOG_TRIM</code>.
     * <p>
     * Note: Currently, null can be passed in for the parent.
     * This has the effect of creating the shell on the currently active
     * display if there is one. If there is no current display, the
     * shell is created on a "default" display. <b>Passing in null as
     * the parent is not considered to be good coding style,
     * and may not be supported in a future release of SWT.</b>
     * </p>
     *
     * @param parent a shell which will be the parent of the new instance
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the parent is disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     */
    public DartShell(Shell parent, Shell api) {
        this(parent, SWT.DIALOG_TRIM, api);
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
     * </p><p>
     * Note: Currently, null can be passed in for the parent.
     * This has the effect of creating the shell on the currently active
     * display if there is one. If there is no current display, the
     * shell is created on a "default" display. <b>Passing in null as
     * the parent is not considered to be good coding style,
     * and may not be supported in a future release of SWT.</b>
     * </p>
     *
     * @param parent a shell which will be the parent of the new instance
     * @param style the style of control to construct
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the parent is disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see SWT#BORDER
     * @see SWT#CLOSE
     * @see SWT#MIN
     * @see SWT#MAX
     * @see SWT#RESIZE
     * @see SWT#TITLE
     * @see SWT#NO_TRIM
     * @see SWT#NO_MOVE
     * @see SWT#SHELL_TRIM
     * @see SWT#DIALOG_TRIM
     * @see SWT#ON_TOP
     * @see SWT#TOOL
     * @see SWT#MODELESS
     * @see SWT#PRIMARY_MODAL
     * @see SWT#APPLICATION_MODAL
     * @see SWT#SYSTEM_MODAL
     * @see SWT#SHEET
     */
    public DartShell(Shell parent, int style, Shell api) {
        this(parent != null ? ((DartWidget) parent.getImpl()).display : null, parent, style, 0, false, api);
    }

    /**
     * Invokes platform specific functionality to allocate a new shell
     * that is not embedded.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
     * API for <code>Shell</code>. It is marked public only so that it
     * can be shared within the packages provided by SWT. It is not
     * available on all platforms, and should never be called from
     * application code.
     * </p>
     *
     * @param display the display for the shell
     * @param handle the handle for the shell
     * @return a new shell object containing the specified display and handle
     *
     * @noreference This method is not intended to be referenced by clients.
     *
     * @since 3.3
     */
    public static Shell internal_new(Display display, long handle) {
        return new Shell(display, null, SWT.NO_TRIM, handle, false);
    }

    static int checkStyle(Shell parent, int style) {
        style = DartDecorations.checkStyle(style);
        style &= ~SWT.TRANSPARENT;
        int mask = SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL | SWT.PRIMARY_MODAL;
        if ((style & SWT.SHEET) != 0) {
            if (DartDisplay.getSheetEnabled()) {
                style &= ~(SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.MAX);
                if (parent == null) {
                    style &= ~SWT.SHEET;
                    style |= SWT.SHELL_TRIM;
                }
            } else {
                style &= ~SWT.SHEET;
                style |= parent == null ? SWT.SHELL_TRIM : SWT.DIALOG_TRIM;
            }
            if ((style & mask) == 0) {
                style |= parent == null ? SWT.APPLICATION_MODAL : SWT.PRIMARY_MODAL;
            }
        }
        int bits = style & ~mask;
        if ((style & SWT.SYSTEM_MODAL) != 0)
            return bits | SWT.SYSTEM_MODAL;
        if ((style & SWT.APPLICATION_MODAL) != 0)
            return bits | SWT.APPLICATION_MODAL;
        if ((style & SWT.PRIMARY_MODAL) != 0)
            return bits | SWT.PRIMARY_MODAL;
        return bits;
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when operations are performed on the receiver,
     * by sending the listener one of the messages defined in the
     * <code>ShellListener</code> interface.
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
     * @see ShellListener
     * @see #removeShellListener
     */
    public void addShellListener(ShellListener listener) {
        addTypedListener(listener, SWT.Activate, SWT.Close, SWT.Deactivate, SWT.Iconify, SWT.Deiconify);
    }

    @Override
    void bringToTop(boolean force) {
        if (getMinimized())
            return;
        if (force) {
            forceActive();
        } else {
            setActive();
        }
    }

    @Override
    public void checkOpen() {
        if (!opened)
            resized = false;
    }

    void center() {
        if (parent == null)
            return;
    }

    /**
     * Requests that the window manager close the receiver in
     * the same way it would be closed when the user clicks on
     * the "close box" or performs some other platform specific
     * key or mouse combination that indicates the window
     * should be removed.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SWT#Close
     * @see #dispose
     */
    public void close() {
        checkWidget();
        closeWidget(false);
    }

    void closeWidget(boolean force) {
        if (display.isDisposed())
            return;
        Event event = new Event();
        sendEvent(SWT.Close, event);
        if ((force || event.doit) && !isDisposed())
            dispose();
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        return Sizes.computeSize(this, wHint, hHint, changed);
    }

    @Override
    public Rectangle computeTrim(int x, int y, int width, int height) {
        return Sizes.computeTrim(this, x, y, width, height);
    }

    @Override
    void createHandle() {
        getApi().state |= HIDDEN;
    }

    void deferFlushing() {
        deferFlushing = true;
    }

    @Override
    void deregister() {
        super.deregister();
    }

    @Override
    void destroyWidget() {
        releaseHandle();
        // If another shell is not going to become active, clear the menu bar.
    }

    @Override
    public Control findBackgroundControl() {
        return null;
    }

    @Override
    public Composite findDeferredControl() {
        return layoutCount > 0 ? this.getApi() : null;
    }

    @Override
    public Cursor findCursor() {
        return cursor;
    }

    boolean fixResize() {
        if ((getApi().style & SWT.NO_TRIM) == 0) {
            if ((getApi().style & SWT.RESIZE) != 0 && (getApi().style & (SWT.SHEET | SWT.TITLE | SWT.CLOSE | SWT.MIN | SWT.MAX)) == 0) {
                return true;
            }
        }
        return false;
    }

    void fixShell(Shell newShell, Control control) {
        if (this.getApi() == newShell)
            return;
        if (control == lastActive)
            setActiveControl(null);
    }

    /**
     * If the receiver is visible, moves it to the top of the
     * drawing order for the display on which it was created
     * (so that all other shells on that display, which are not
     * the receiver's children will be drawn behind it) and forces
     * the window manager to make the shell active.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 2.0
     * @see Control#moveAbove
     * @see Control#setFocus
     * @see Control#setVisible
     * @see Display#getActiveShell
     * @see Decorations#setDefaultButton(Button)
     * @see Shell#open
     * @see Shell#setActive
     */
    public void forceActive() {
        checkWidget();
        if (!isVisible())
            return;
        makeKeyAndOrderFront();
    }

    /**
     * Returns the receiver's alpha value. The alpha value
     * is between 0 (transparent) and 255 (opaque).
     *
     * @return the alpha value
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    public int getAlpha() {
        checkWidget();
        return this.alpha;
    }

    @Override
    public Rectangle getClientArea() {
        return Sizes.getClientArea(this);
    }

    /**
     * Returns <code>true</code> if the receiver is currently
     * in fullscreen state, and false otherwise.
     *
     * @return the fullscreen state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    public boolean getFullScreen() {
        checkWidget();
        return _getFullScreen();
    }

    boolean _getFullScreen() {
        return fullScreen;
    }

    /**
     * Returns the receiver's input method editor mode. This
     * will be the result of bitwise OR'ing together one or
     * more of the following constants defined in class
     * <code>SWT</code>:
     * <code>NONE</code>, <code>ROMAN</code>, <code>DBCS</code>,
     * <code>PHONETIC</code>, <code>NATIVE</code>, <code>ALPHA</code>.
     *
     * @return the IME mode
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SWT
     */
    public int getImeInputMode() {
        checkWidget();
        return SWT.NONE;
    }

    @Override
    public boolean getMaximized() {
        checkWidget();
        return this.maximized;
    }

    Shell getModalShell() {
        Shell shell = null;
        Shell[] modalShells = ((DartDisplay) display.getImpl()).modalShells;
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
                            control = ((DartControl) control.getImpl()).parent;
                        }
                        if (control != modal)
                            return modal;
                        break;
                    }
                    if ((modal.style & SWT.PRIMARY_MODAL) != 0) {
                        if (shell == null)
                            shell = getShell();
                        if (((DartControl) modal.getImpl()).parent == shell)
                            return modal;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets the receiver's modified state.
     *
     * @return <code>true</code> if the receiver is marked as modified, or <code>false</code> otherwise
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.5
     */
    public boolean getModified() {
        checkWidget();
        return this.modified;
    }

    @Override
    public boolean getMinimized() {
        checkWidget();
        if (!getVisible())
            return super.getMinimized();
        return this.minimized;
    }

    /**
     * Returns a point describing the maximum receiver's size. The
     * x coordinate of the result is the maximum width of the receiver.
     * The y coordinate of the result is the maximum height of the
     * receiver.
     *
     * @return the receiver's size
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.116
     */
    public Point getMaximumSize() {
        checkWidget();
        return this.maximumSize;
    }

    /**
     * Returns a point describing the minimum receiver's size. The
     * x coordinate of the result is the minimum width of the receiver.
     * The y coordinate of the result is the minimum height of the
     * receiver.
     *
     * @return the receiver's size
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    public Point getMinimumSize() {
        checkWidget();
        return this.minimumSize;
    }

    /**
     * Returns the region that defines the shape of the shell,
     * or <code>null</code> if the shell has the default shape.
     *
     * @return the region that defines the shape of the shell, or <code>null</code>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    @Override
    public Region getRegion() {
        /* This method is needed for the @since 3.0 Javadoc */
        checkWidget();
        return region;
    }

    @Override
    public Shell getShell() {
        checkWidget();
        return this.getApi();
    }

    /**
     * Returns an array containing all shells which are
     * descendants of the receiver.
     *
     * @return the dialog shells
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Shell[] getShells() {
        checkWidget();
        int count = 0;
        Shell[] shells = display.getShells();
        for (Shell activeshell : shells) {
            Control shell = activeshell;
            do {
                shell = shell.getParent();
            } while (shell != null && shell != this.getApi());
            if (shell == this.getApi())
                count++;
        }
        int index = 0;
        Shell[] result = new Shell[count];
        for (Shell activeshell : shells) {
            Control shell = activeshell;
            do {
                shell = shell.getParent();
            } while (shell != null && shell != this.getApi());
            if (shell == this.getApi()) {
                result[index++] = activeshell;
            }
        }
        return result;
    }

    @Override
    public float getThemeAlpha() {
        return 1;
    }

    /**
     * Returns a ToolBar object representing the tool bar that can be shown in the receiver's
     * trim. This will return <code>null</code> if the platform does not support tool bars that
     * are not part of the content area of the shell, or if the Shell's style does not support
     * having a tool bar.
     *
     * @return a ToolBar object representing the Shell's tool bar, or <code>null</code>.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.7
     */
    public ToolBar getToolBar() {
        checkWidget();
        if ((getApi().style & SWT.NO_TRIM) == 0) {
            if (toolBar == null)
                toolBar = new ToolBar(this.getApi(), SWT.HORIZONTAL | SWT.SMOOTH, true);
        }
        return toolBar;
    }

    @Override
    boolean hasBorder() {
        return false;
    }

    @Override
    public boolean hasRegion() {
        return region != null;
    }

    @Override
    void invalidateVisibleRegion() {
        resetVisibleRegion();
        if (toolBar != null)
            toolBar.getImpl().resetVisibleRegion();
        invalidateChildrenVisibleRegion();
    }

    @Override
    public boolean isDrawing() {
        return getDrawing();
    }

    @Override
    public boolean isEnabled() {
        checkWidget();
        return getEnabled();
    }

    @Override
    boolean isEnabledCursor() {
        return true;
    }

    @Override
    boolean isResizing() {
        return (getApi().state & RESIZING) != 0;
    }

    @Override
    public boolean isTransparent() {
        return false;
    }

    @Override
    public boolean isVisible() {
        checkWidget();
        return getVisible();
    }

    void makeKeyAndOrderFront() {
        /*
	* Bug in Cocoa.  If a child window becomes the key window when its
	* parent window is miniaturized, the parent window appears as if
	* restored to its full size without actually being restored. In this
	* case the parent window does become active when its child is closed
	* and the user is forced to restore the window from the dock.
	* The fix is to be sure that the parent window is deminiaturized before
	* making the child a key window.
	*/
        if (parent != null) {
        }
    }

    /**
     * Moves the receiver to the top of the drawing order for
     * the display on which it was created (so that all other
     * shells on that display, which are not the receiver's
     * children will be drawn behind it), marks it visible,
     * sets the focus and asks the window manager to make the
     * shell active.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Control#moveAbove
     * @see Control#setFocus
     * @see Control#setVisible
     * @see Display#getActiveShell
     * @see Decorations#setDefaultButton(Button)
     * @see Shell#setActive
     * @see Shell#forceActive
     */
    public void open() {
        checkWidget();
        int mask = SWT.PRIMARY_MODAL | SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL;
        if ((getApi().style & mask) != 0) {
            ((DartDisplay) display.getImpl()).setModalShell(this.getApi());
        } else {
            updateModal();
        }
        bringToTop(false);
        setWindowVisible(true, true);
        if (isDisposed())
            return;
        if (!restoreFocus() && !traverseGroup(true)) {
            // if the parent shell is minimized, setting focus will cause it
        }
    }

    @Override
    public boolean print(GC gc) {
        checkWidget();
        if (gc == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (gc.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        return false;
    }

    @Override
    void register() {
        /*
	 * Note that if there are multiple SWT_AWT shells only the last one created
	 * will be associated with the NSWindow. This is okay, and intentional because
	 * all of the NSWindow overrides operate on the entire window.
	 */
        super.register();
    }

    @Override
    void releaseChildren(boolean destroy) {
        Shell[] shells = getShells();
        for (int i = 0; i < shells.length; i++) {
            Shell shell = shells[i];
            if (shell != null && !shell.isDisposed()) {
                shell.dispose();
            }
        }
        super.releaseChildren(destroy);
    }

    @Override
    void releaseHandle() {
        removeObserversFromWindow();
        super.releaseHandle();
    }

    @Override
    void releaseParent() {
        /* Do nothing */
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        if (toolBar != null) {
            toolBar.dispose();
            toolBar = null;
        }
        if (tooltipTag != 0) {
            tooltipTag = 0;
        }
        ((DartDisplay) display.getImpl()).clearModal(this.getApi());
        updateParent(false);
        ((DartDisplay) display.getImpl()).updateQuitMenu();
        lastActive = null;
    }

    void removeObserversFromWindow() {
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when operations are performed on the receiver.
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
     * @see ShellListener
     * @see #addShellListener
     */
    public void removeShellListener(ShellListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Activate, listener);
        eventTable.unhook(SWT.Close, listener);
        eventTable.unhook(SWT.Deactivate, listener);
        eventTable.unhook(SWT.Iconify, listener);
        eventTable.unhook(SWT.Deiconify, listener);
    }

    @Override
    public void requestLayout() {
        layout(null, SWT.DEFER);
    }

    @Override
    void reskinChildren(int flags) {
        if (toolBar != null)
            toolBar.reskin(flags);
        Shell[] shells = getShells();
        for (int i = 0; i < shells.length; i++) {
            Shell shell = shells[i];
            if (shell != null)
                shell.reskin(flags);
        }
        super.reskinChildren(flags);
    }

    void sendToolTipEvent(boolean enter) {
        if (!isVisible())
            return;
        if (tooltipTag == 0) {
            if (tooltipTag != 0) {
            }
        }
        if (tooltipTag == 0 || tooltipOwner == 0 || tooltipUserData == 0)
            return;
    }

    /**
     * If the receiver is visible, moves it to the top of the
     * drawing order for the display on which it was created
     * (so that all other shells on that display, which are not
     * the receiver's children will be drawn behind it) and asks
     * the window manager to make the shell active
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 2.0
     * @see Control#moveAbove
     * @see Control#setFocus
     * @see Control#setVisible
     * @see Display#getActiveShell
     * @see Decorations#setDefaultButton(Button)
     * @see Shell#open
     * @see Shell#setActive
     */
    public void setActive() {
        checkWidget();
        if (!isVisible())
            return;
        makeKeyAndOrderFront();
    }

    void setActiveControl(Control control) {
        setActiveControl(control, SWT.None);
    }

    void setActiveControl(Control control, int type) {
        if (control != null && control.isDisposed())
            control = null;
        if (lastActive != null && lastActive.isDisposed())
            lastActive = null;
        if (lastActive == control)
            return;
        /*
	* Compute the list of controls to be activated and
	* deactivated by finding the first common parent
	* control.
	*/
        Control[] activate = (control == null) ? new Control[0] : control.getImpl().getPath();
        Control[] deactivate = (lastActive == null) ? new Control[0] : lastActive.getImpl().getPath();
        lastActive = control;
        int index = 0, length = Math.min(activate.length, deactivate.length);
        while (index < length) {
            if (activate[index] != deactivate[index])
                break;
            index++;
        }
        /*
	* It is possible (but unlikely), that application
	* code could have destroyed some of the widgets. If
	* this happens, keep processing those widgets that
	* are not disposed.
	*/
        for (int i = deactivate.length - 1; i >= index; --i) {
            if (!deactivate[i].isDisposed()) {
                deactivate[i].getImpl().sendEvent(SWT.Deactivate);
            }
        }
        for (int i = activate.length - 1; i >= index; --i) {
            if (!activate[i].isDisposed()) {
                Event event = new Event();
                event.detail = type;
                activate[i].getImpl().sendEvent(SWT.Activate, event);
            }
        }
    }

    /**
     * Sets the receiver's alpha value which must be
     * between 0 (transparent) and 255 (opaque).
     * <p>
     * This operation requires the operating system's advanced
     * widgets subsystem which may not be available on some
     * platforms.
     * </p>
     * @param alpha the alpha value
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    public void setAlpha(int alpha) {
        int newValue = alpha;
        if (!java.util.Objects.equals(this.alpha, newValue)) {
            dirty();
        }
        checkWidget();
        alpha &= 0xFF;
        this.alpha = newValue;
    }

    @Override
    void setBounds(int x, int y, int width, int height, boolean move, boolean resize) {
        if (_getFullScreen())
            setFullScreen(false);
        if (!move) {
        }
        if (resize) {
        } else {
        }
        {
            super.setBounds(x, y, width, height, move, resize);
            resized = true;
            sendEvent(SWT.Resize);
            if (isDisposed())
                return;
            if (layout != null) {
                markLayout(false, false);
                updateLayout(false);
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        dirty();
        checkWidget();
        if (((getApi().state & DISABLED) == 0) == enabled)
            return;
        super.setEnabled(enabled);
    }

    /**
     * Sets the full screen state of the receiver.
     * If the argument is <code>true</code> causes the receiver
     * to switch to the full screen state, and if the argument is
     * <code>false</code> and the receiver was previously switched
     * into full screen state, causes the receiver to switch back
     * to either the maximized or normal states.
     * <p>
     * Note: The result of intermixing calls to <code>setFullScreen(true)</code>,
     * <code>setMaximized(true)</code>, <code>setMinimized(true)</code> and
     * <code>setMaximumSize</code> will vary by platform.
     * Typically, the behavior will match the platform user's
     * expectations, but not always. This should be avoided if possible.
     * </p>
     *
     * @param fullScreen the new fullscreen state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    public void setFullScreen(boolean fullScreen) {
        checkWidget();
        if (!java.util.Objects.equals(this.fullScreen, fullScreen)) {
            dirty();
        }
        if (_getFullScreen() == fullScreen)
            return;
        this.fullScreen = fullScreen;
        if (fullScreen) {
            if (getMonitor().equals(display.getPrimaryMonitor())) {
                if (menuBar != null) {
                } else {
                }
            }
        } else {
        }
    }

    @Override
    public void setMenuBar(Menu menu) {
        dirty();
        checkWidget();
        super.setMenuBar(menu);
        if (display.getActiveShell() == this.getApi()) {
            ((DartDisplay) display.getImpl()).setMenuBar(menuBar);
        }
    }

    /**
     * Sets the input method editor mode to the argument which
     * should be the result of bitwise OR'ing together one or more
     * of the following constants defined in class <code>SWT</code>:
     * <code>NONE</code>, <code>ROMAN</code>, <code>DBCS</code>,
     * <code>PHONETIC</code>, <code>NATIVE</code>, <code>ALPHA</code>.
     *
     * @param mode the new IME mode
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SWT
     */
    public void setImeInputMode(int mode) {
        int newValue = mode;
        if (!java.util.Objects.equals(this.imeInputMode, newValue)) {
            dirty();
        }
        this.imeInputMode = newValue;
        checkWidget();
    }

    @Override
    public void setMaximized(boolean maximized) {
        dirty();
        checkWidget();
        super.setMaximized(maximized);
        if (maximized)
            bounds = display.getBounds();
    }

    /**
     * Sets the receiver's maximum size to the size specified by the arguments.
     * If the new maximum size is smaller than the current size of the receiver,
     * the receiver is resized to the new maximum size.
     * <p>
     * Note: The result of intermixing calls to <code>setMaximumSize</code> and
     * <code>setFullScreen(true)</code> will vary by platform.
     * Typically, the behavior will match the platform user's
     * expectations, but not always. This should be avoided if possible.
     * </p>
     * @param width the new maximum width for the receiver
     * @param height the new maximum height for the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.116
     */
    public void setMaximumSize(int width, int height) {
        dirty();
        Point newValue = new Point(maximumSize.x, maximumSize.y);
        checkWidget();
        this.maximumSize = newValue;
    }

    /**
     * Sets the receiver's maximum size to the size specified by the argument.
     * If the new maximum size is smaller than the current size of the receiver,
     * the receiver is resized to the new maximum size.
     * <p>
     * Note: The result of intermixing calls to <code>setMaximumSize</code> and
     * <code>setFullScreen(true)</code> will vary by platform.
     * Typically, the behavior will match the platform user's
     * expectations, but not always. This should be avoided if possible.
     * </p>
     * @param size the new maximum size for the receiver
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.116
     */
    public void setMaximumSize(Point size) {
        checkWidget();
        if (size == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        setMaximumSize(size.x, size.y);
    }

    @Override
    public void setMinimized(boolean minimized) {
        dirty();
        checkWidget();
        super.setMinimized(minimized);
        if (!getVisible())
            return;
        if (minimized) {
        } else {
        }
    }

    /**
     * Sets the receiver's minimum size to the size specified by the arguments.
     * If the new minimum size is larger than the current size of the receiver,
     * the receiver is resized to the new minimum size.
     *
     * @param width the new minimum width for the receiver
     * @param height the new minimum height for the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    public void setMinimumSize(int width, int height) {
        dirty();
        Point newValue = new Point(minimumSize.x, minimumSize.y);
        checkWidget();
        this.minimumSize = newValue;
    }

    /**
     * Sets the receiver's minimum size to the size specified by the argument.
     * If the new minimum size is larger than the current size of the receiver,
     * the receiver is resized to the new minimum size.
     *
     * @param size the new minimum size for the receiver
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    public void setMinimumSize(Point size) {
        checkWidget();
        if (size == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        setMinimumSize(size.x, size.y);
    }

    /**
     * Sets the receiver's modified state as specified by the argument.
     *
     * @param modified the new modified state for the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.5
     */
    public void setModified(boolean modified) {
        boolean newValue = modified;
        if (!java.util.Objects.equals(this.modified, newValue)) {
            dirty();
        }
        checkWidget();
        this.modified = newValue;
    }

    /**
     * Sets the shape of the shell to the region specified
     * by the argument.  When the argument is null, the
     * default shape of the shell is restored.  The shell
     * must be created with the style SWT.NO_TRIM in order
     * to specify a region.
     * <p>
     * NOTE: This method also sets the size of the shell. Clients should
     * not call {@link #setSize} or {@link #setBounds} on this shell.
     * Furthermore, the passed region should not be modified any more.
     * </p>
     *
     * @param region the region that defines the shape of the shell (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the region has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    @Override
    public void setRegion(Region region) {
        dirty();
        checkWidget();
        if ((getApi().style & SWT.NO_TRIM) == 0)
            return;
        if (region != null) {
            if (region.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
        }
        this.region = region;
        if (region != null) {
        } else {
        }
        updateOpaque();
    }

    public void setScrolling() {
        scrolling = true;
    }

    @Override
    public void setText(String string) {
        dirty();
        checkWidget();
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        super.setText(string);
    }

    @Override
    public void setVisible(boolean visible) {
        dirty();
        checkWidget();
        int mask = SWT.PRIMARY_MODAL | SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL;
        if ((getApi().style & mask) != 0) {
            if (visible) {
                ((DartDisplay) display.getImpl()).setModalShell(this.getApi());
            } else {
                ((DartDisplay) display.getImpl()).clearModal(this.getApi());
            }
        } else {
            updateModal();
        }
        {
            setWindowVisible(visible, false);
        }
    }

    void preventShellActivateJvmCrash() {
    }

    void setWindowVisible(boolean visible, boolean key) {
        if (visible) {
            if ((getApi().state & HIDDEN) == 0)
                return;
            getApi().state &= ~HIDDEN;
        } else {
            if ((getApi().state & HIDDEN) != 0)
                return;
            getApi().state |= HIDDEN;
        }
        if (visible) {
            ((DartDisplay) display.getImpl()).clearPool();
            if (center && !moved) {
                if (isDisposed())
                    return;
                center();
            }
            sendEvent(SWT.Show);
            if (isDisposed())
                return;
            invalidateVisibleRegion();
            if (isDisposed())
                return;
            updateParent(visible);
            opened = true;
            if (!moved) {
                moved = true;
                sendEvent(SWT.Move);
                if (isDisposed())
                    return;
            }
            if (!resized) {
                resized = true;
                sendEvent(SWT.Resize);
                if (isDisposed())
                    return;
                if (layout != null) {
                    markLayout(false, false);
                    updateLayout(false);
                }
            }
        } else {
            updateParent(visible);
            if (isDisposed())
                return;
            invalidateVisibleRegion();
            sendEvent(SWT.Hide);
        }
        if (isDisposed())
            return;
        ((DartDisplay) display.getImpl()).updateQuitMenu();
        if (isDisposed())
            return;
        getBridge().setVisible(this, visible);
    }

    @Override
    void setZOrder() {
        if (fixResize()) {
        }
    }

    @Override
    void setZOrder(Control control, boolean above) {
        if (!getVisible())
            return;
        if (control == null) {
            if (above) {
            } else {
            }
        } else {
        }
    }

    @Override
    boolean traverseEscape() {
        if (parent == null)
            return false;
        if (!isVisible() || !isEnabled())
            return false;
        close();
        return true;
    }

    @Override
    public void updateCursorRects(boolean enabled) {
        super.updateCursorRects(enabled);
        if (toolBar != null)
            toolBar.getImpl().updateCursorRects(enabled);
    }

    void updateModal() {
        // do nothing
    }

    public void updateOpaque() {
    }

    void updateParent(boolean visible) {
        Shell[] shells = getShells();
        for (int i = 0; i < shells.length; i++) {
            Shell shell = shells[i];
            if (((DartControl) shell.getImpl()).parent == this.getApi() && shell.getVisible()) {
                ((DartShell) shell.getImpl()).updateParent(visible);
            }
        }
    }

    void updateSystemUIMode() {
        if (!getMonitor().equals(display.getPrimaryMonitor()))
            return;
        if (fullScreen) {
            if (menuBar != null) {
            }
        }
    }

    private void updateEscMenuItem() {
        if (menuBar != null && !menuBar.isDisposed()) {
            searchForEscMenuItem(menuBar);
        } else if (((DartDisplay) display.getImpl()).appMenuBar != null && !((DartDisplay) display.getImpl()).appMenuBar.isDisposed()) {
            searchForEscMenuItem(((DartDisplay) display.getImpl()).appMenuBar);
        }
    }

    private boolean searchForEscMenuItem(Menu menu) {
        if (menu == null || menu.isDisposed())
            return false;
        MenuItem[] items = menu.getItems();
        if (items == null)
            return false;
        for (MenuItem item : items) {
            if (item == null || item.isDisposed()) {
                continue;
            } else if (item.getAccelerator() == SWT.ESC) {
                escMenuItem = item;
                return true;
            } else if ((item.getStyle() & SWT.CASCADE) != 0) {
                Menu subMenu = item.getMenu();
                if (searchForEscMenuItem(subMenu))
                    return true;
            }
        }
        return false;
    }

    int alpha;

    int imeInputMode;

    Point maximumSize;

    Point minimumSize;

    boolean modified;

    Shell[] shells = new Shell[0];

    public long _hostWindowClass() {
        return hostWindowClass;
    }

    public long _tooltipOwner() {
        return tooltipOwner;
    }

    public long _tooltipTag() {
        return tooltipTag;
    }

    public long _tooltipUserData() {
        return tooltipUserData;
    }

    public int _glContextCount() {
        return glContextCount;
    }

    public boolean _opened() {
        return opened;
    }

    public boolean _moved() {
        return moved;
    }

    public boolean _resized() {
        return resized;
    }

    public boolean _fullScreen() {
        return fullScreen;
    }

    public boolean _center() {
        return center;
    }

    public boolean _deferFlushing() {
        return deferFlushing;
    }

    public boolean _scrolling() {
        return scrolling;
    }

    public boolean _isPopup() {
        return isPopup;
    }

    public Control _lastActive() {
        return lastActive;
    }

    public Rectangle _normalBounds() {
        return normalBounds;
    }

    public boolean _keyInputHappened() {
        return keyInputHappened;
    }

    public ToolBar _toolBar() {
        return toolBar;
    }

    public MenuItem _escMenuItem() {
        return escMenuItem;
    }

    public int _alpha() {
        return alpha;
    }

    public int _imeInputMode() {
        return imeInputMode;
    }

    public Point _maximumSize() {
        return maximumSize;
    }

    public Point _minimumSize() {
        return minimumSize;
    }

    public boolean _modified() {
        return modified;
    }

    public Shell[] _shells() {
        return shells;
    }

    protected void _hookEvents() {
        super._hookEvents();
        FlutterBridge.on(this, "Shell", "Activate", e -> {
            getDisplay().asyncExec(() -> {
                if (isDisposed())
                    return;
                sendEvent(SWT.Activate, e);
            });
        });
        FlutterBridge.on(this, "Shell", "Close", e -> {
            getDisplay().asyncExec(() -> {
                if (isDisposed())
                    return;
                sendEvent(SWT.Close, e);
            });
        });
        FlutterBridge.on(this, "Shell", "Deactivate", e -> {
            getDisplay().asyncExec(() -> {
                if (isDisposed())
                    return;
                sendEvent(SWT.Deactivate, e);
            });
        });
        FlutterBridge.on(this, "Shell", "Deiconify", e -> {
            getDisplay().asyncExec(() -> {
                if (isDisposed())
                    return;
                sendEvent(SWT.Deiconify, e);
            });
        });
        FlutterBridge.on(this, "Shell", "Iconify", e -> {
            getDisplay().asyncExec(() -> {
                if (isDisposed())
                    return;
                sendEvent(SWT.Iconify, e);
            });
        });
    }

    public Shell getApi() {
        if (api == null)
            api = Shell.createApi(this);
        return (Shell) api;
    }

    public VShell getValue() {
        if (value == null)
            value = new VShell(this);
        return (VShell) value;
    }
}
