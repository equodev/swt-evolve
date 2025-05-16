package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface IDecorations extends ICanvas {

    void checkSubclass();

    /**
     * Returns the receiver's default button if one had
     * previously been set, otherwise returns null.
     *
     * @return the default button or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setDefaultButton(Button)
     */
    Button getDefaultButton();

    /**
     * Returns the receiver's image if it had previously been
     * set using <code>setImage()</code>. The image is typically
     * displayed by the window manager when the instance is
     * marked as iconified, and may also be displayed somewhere
     * in the trim when the instance is in normal or maximized
     * states.
     * <p>
     * Note: This method will return null if called before
     * <code>setImage()</code> is called. It does not provide
     * access to a window manager provided, "default" image
     * even if one exists.
     * </p>
     *
     * @return the image
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    Image getImage();

    /**
     * Returns the receiver's images if they had previously been
     * set using <code>setImages()</code>. Images are typically
     * displayed by the window manager when the instance is
     * marked as iconified, and may also be displayed somewhere
     * in the trim when the instance is in normal or maximized
     * states. Depending where the icon is displayed, the platform
     * chooses the icon with the "best" attributes.  It is expected
     * that the array will contain the same icon rendered at different
     * sizes, with different depth and transparency attributes.
     *
     * <p>
     * Note: This method will return an empty array if called before
     * <code>setImages()</code> is called. It does not provide
     * access to a window manager provided, "default" image
     * even if one exists.
     * </p>
     *
     * @return the images
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    Image[] getImages();

    /**
     * Returns <code>true</code> if the receiver is currently
     * maximized, and false otherwise.
     *
     * @return the maximized state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setMaximized
     */
    boolean getMaximized();

    /**
     * Returns the receiver's menu bar if one had previously
     * been set, otherwise returns null.
     *
     * @return the menu bar or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    Menu getMenuBar();

    /**
     * Returns <code>true</code> if the receiver is currently
     * minimized, and false otherwise.
     *
     * @return the minimized state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setMinimized
     */
    boolean getMinimized();

    /**
     * Returns the receiver's text, which is the string that the
     * window manager will typically display as the receiver's
     * <em>title</em>. If the text has not previously been set,
     * returns an empty string.
     *
     * @return the text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    String getText();

    boolean isReparentable();

    /**
     * If the argument is not null, sets the receiver's default
     * button to the argument, and if the argument is null, sets
     * the receiver's default button to the first button which
     * was set as the receiver's default button (called the
     * <em>saved default button</em>). If no default button had
     * previously been set, or the saved default button was
     * disposed, the receiver's default button will be set to
     * null.
     * <p>
     * The default button is the button that is selected when
     * the receiver is active and the user presses ENTER.
     * </p>
     *
     * @param button the new default button
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the button has been disposed</li>
     *    <li>ERROR_INVALID_PARENT - if the control is not in the same widget tree</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setDefaultButton(Button button);

    /**
     * Sets the receiver's image to the argument, which may
     * be null. The image is typically displayed by the window
     * manager when the instance is marked as iconified, and
     * may also be displayed somewhere in the trim when the
     * instance is in normal or maximized states.
     *
     * @param image the new image (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setImage(Image image);

    /**
     * Sets the receiver's images to the argument, which may
     * be an empty array. Images are typically displayed by the
     * window manager when the instance is marked as iconified,
     * and may also be displayed somewhere in the trim when the
     * instance is in normal or maximized states. Depending where
     * the icon is displayed, the platform chooses the icon with
     * the "best" attributes. It is expected that the array will
     * contain the same icon rendered at different sizes, with
     * different depth and transparency attributes.
     *
     * @param images the new image array
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the array of images is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if one of the images is null or has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    void setImages(Image[] images);

    /**
     * Sets the maximized state of the receiver.
     * If the argument is <code>true</code> causes the receiver
     * to switch to the maximized state, and if the argument is
     * <code>false</code> and the receiver was previously maximized,
     * causes the receiver to switch back to either the minimized
     * or normal states.
     * <p>
     * Note: The result of intermixing calls to <code>setMaximized(true)</code>
     * and <code>setMinimized(true)</code> will vary by platform. Typically,
     * the behavior will match the platform user's expectations, but not
     * always. This should be avoided if possible.
     * </p>
     *
     * @param maximized the new maximized state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setMinimized
     */
    void setMaximized(boolean maximized);

    /**
     * Sets the receiver's menu bar to the argument, which
     * may be null.
     *
     * @param menu the new menu bar
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the menu has been disposed</li>
     *    <li>ERROR_INVALID_PARENT - if the menu is not in the same widget tree</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setMenuBar(Menu menu);

    /**
     * Sets the minimized stated of the receiver.
     * If the argument is <code>true</code> causes the receiver
     * to switch to the minimized state, and if the argument is
     * <code>false</code> and the receiver was previously minimized,
     * causes the receiver to switch back to either the maximized
     * or normal states.
     * <p>
     * Note: The result of intermixing calls to <code>setMaximized(true)</code>
     * and <code>setMinimized(true)</code> will vary by platform. Typically,
     * the behavior will match the platform user's expectations, but not
     * always. This should be avoided if possible.
     * </p>
     *
     * @param minimized the new minimized state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setMaximized
     */
    void setMinimized(boolean minimized);

    /**
     * Sets the receiver's text, which is the string that the
     * window manager will typically display as the receiver's
     * <em>title</em>, to the argument, which must not be null.
     * <p>
     * Note: If control characters like '\n', '\t' etc. are used
     * in the string, then the behavior is platform dependent.
     * </p>
     *
     * @param string the new text
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the text is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setText(String string);

    Decorations getApi();
}
