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
 * *****************************************************************************
 */
package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.cairo.*;
import org.eclipse.swt.internal.gtk.*;
import org.eclipse.swt.internal.gtk3.*;
import org.eclipse.swt.internal.gtk4.*;
import java.util.WeakHashMap;

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
public class Shell extends Decorations {

    /**
     * Constructs a new instance of this class. This is equivalent
     * to calling <code>Shell((Display) null)</code>.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     */
    public Shell() {
        this(new SWTShell());
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
    public Shell(int style) {
        this(new SWTShell(style));
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
    public Shell(Display display) {
        this(new SWTShell((SWTDisplay) display.delegate));
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
    public Shell(Display display, int style) {
        this(new SWTShell((SWTDisplay) display.delegate, style));
    }

    Shell(Display display, Shell parent, int style, long handle, boolean embedded) {
        this(new SWTShell((SWTDisplay) display.delegate, (SWTShell) parent.delegate, style, handle, embedded));
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
    public Shell(Shell parent) {
        this(new SWTShell((SWTShell) parent.delegate));
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
    public Shell(Shell parent, int style) {
        this(new SWTShell((SWTShell) (parent != null ? parent.delegate : null), style));
    }

    /**
     * Invokes platform specific functionality to allocate a new shell
     * that is embedded.
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
     */
    public static Shell gtk_new(Display display, long handle) {
        return Shell.getInstance(SWTShell.gtk_new((SWTDisplay) display.delegate, handle));
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
        return Shell.getInstance(SWTShell.internal_new((SWTDisplay) display.delegate, handle));
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
        ((IShell) this.delegate).addShellListener(listener);
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
        ((IShell) this.delegate).close();
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
        return ToolBar.getInstance(((IShell) this.delegate).getToolBar());
    }

    @Override
    public boolean isEnabled() {
        return ((IShell) this.delegate).isEnabled();
    }

    @Override
    public boolean isVisible() {
        return ((IShell) this.delegate).isVisible();
    }

    @Override
    public void requestLayout() {
        ((IShell) this.delegate).requestLayout();
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
        return ((IShell) this.delegate).getAlpha();
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
        return ((IShell) this.delegate).getFullScreen();
    }

    @Override
    public boolean getMaximized() {
        return ((IShell) this.delegate).getMaximized();
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
        return ((IShell) this.delegate).getMinimumSize();
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
        return ((IShell) this.delegate).getMaximumSize();
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
        return ((IShell) this.delegate).getModified();
    }

    @Override
    public boolean getVisible() {
        return ((IShell) this.delegate).getVisible();
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
        return ((IShell) this.delegate).getRegion();
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
        return ((IShell) this.delegate).getImeInputMode();
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
        return Shell.ofArray(((IShell) this.delegate).getShells(), Shell.class);
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
        ((IShell) this.delegate).open();
    }

    @Override
    public boolean print(GC gc) {
        return ((IShell) this.delegate).print(gc);
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
        ((IShell) this.delegate).removeShellListener(listener);
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
        ((IShell) this.delegate).setActive();
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
        ((IShell) this.delegate).setAlpha(alpha);
    }

    @Override
    public void setEnabled(boolean enabled) {
        ((IShell) this.delegate).setEnabled(enabled);
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
        ((IShell) this.delegate).setFullScreen(fullScreen);
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
        ((IShell) this.delegate).setImeInputMode(mode);
    }

    @Override
    public void setMaximized(boolean maximized) {
        ((IShell) this.delegate).setMaximized(maximized);
    }

    @Override
    public void setMenuBar(Menu menu) {
        ((IShell) this.delegate).setMenuBar((IMenu) menu.delegate);
    }

    @Override
    public void setMinimized(boolean minimized) {
        ((IShell) this.delegate).setMinimized(minimized);
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
        ((IShell) this.delegate).setMinimumSize(width, height);
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
        ((IShell) this.delegate).setMinimumSize(size);
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
        ((IShell) this.delegate).setMaximumSize(width, height);
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
        ((IShell) this.delegate).setMaximumSize(size);
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
        ((IShell) this.delegate).setModified(modified);
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
        ((IShell) this.delegate).setRegion(region);
    }

    @Override
    public void setText(String string) {
        ((IShell) this.delegate).setText(string);
    }

    @Override
    public void setVisible(boolean visible) {
        ((IShell) this.delegate).setVisible(visible);
    }

    @Override
    public void dispose() {
        ((IShell) this.delegate).dispose();
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
        ((IShell) this.delegate).forceActive();
    }

    protected Shell(IShell delegate) {
        super(delegate);
        this.delegate = delegate;
        INSTANCES.put(delegate, this);
    }

    public static Shell getInstance(IShell delegate) {
        if (delegate == null) {
            return null;
        }
        Shell ref = (Shell) INSTANCES.get(delegate);
        if (ref == null) {
            ref = new Shell(delegate);
        }
        return ref;
    }
}
