package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;

public interface IShell extends IDecorations {

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
    void addShellListener(ShellListener listener);

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
    void close();

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
    void forceActive();

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
    int getAlpha();

    boolean getEnabled();

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
    boolean getFullScreen();

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
    int getImeInputMode();

    boolean getMaximized();

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
    Point getMaximumSize();

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
    Point getMinimumSize();

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
    boolean getModified();

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
    Region getRegion();

    Shell getShell();

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
    Shell[] getShells();

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
    ToolBar getToolBar();

    boolean isEnabled();

    boolean isVisible();

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
    void open();

    boolean print(GC gc);

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
    void removeShellListener(ShellListener listener);

    void requestLayout();

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
    void setActive();

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
    void setAlpha(int alpha);

    Rectangle getBounds();

    Point getLocation();

    void setLocation(Point location);

    void setLocation(int x, int y);

    void setBounds(Rectangle rect);

    void setBounds(int x, int y, int width, int height);

    void setEnabled(boolean enabled);

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
    void setFullScreen(boolean fullScreen);

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
    void setImeInputMode(int mode);

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
    void setMaximumSize(int width, int height);

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
    void setMaximumSize(Point size);

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
    void setMinimumSize(int width, int height);

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
    void setMinimumSize(Point size);

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
    void setModified(boolean modified);

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
    void setRegion(Region region);

    void setVisible(boolean visible);

    Shell getApi();
}
