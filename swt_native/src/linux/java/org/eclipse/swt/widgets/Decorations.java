/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2018 IBM Corporation and others.
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
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.gtk.*;
import org.eclipse.swt.internal.gtk3.*;
import org.eclipse.swt.internal.gtk4.*;
import java.util.WeakHashMap;

/**
 * Instances of this class provide the appearance and
 * behavior of <code>Shells</code>, but are not top
 * level shells or dialogs. Class <code>Shell</code>
 * shares a significant amount of code with this class,
 * and is a subclass.
 * <p>
 * IMPORTANT: This class was intended to be abstract and
 * should <em>never</em> be referenced or instantiated.
 * Instead, the class <code>Shell</code> should be used.
 * </p>
 * <p>
 * Instances are always displayed in one of the maximized,
 * minimized or normal states:
 * </p>
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
 * Note: The styles supported by this class must be treated
 * as <em>HINT</em>s, since the window manager for the
 * desktop on which the instance is visible has ultimate
 * control over the appearance and behavior of decorations.
 * For example, some window managers only support resizable
 * windows and will always assume the RESIZE style, even if
 * it is not set.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>BORDER, CLOSE, MIN, MAX, NO_TRIM, RESIZE, TITLE, ON_TOP, TOOL</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * Class <code>SWT</code> provides two "convenience constants"
 * for the most commonly required style combinations:
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
 * IMPORTANT: This class is intended to be subclassed <em>only</em>
 * within the SWT implementation.
 * </p>
 *
 * @see #getMinimized
 * @see #getMaximized
 * @see Shell
 * @see SWT
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class Decorations extends Canvas {

    Decorations() {
        this(new SWTDecorations());
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
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public Decorations(Composite parent, int style) {
        this(new SWTDecorations((SWTComposite) parent.delegate, style));
    }

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
    public Button getDefaultButton() {
        return Button.getInstance(((IDecorations) this.delegate).getDefaultButton());
    }

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
    public Image getImage() {
        return ((IDecorations) this.delegate).getImage();
    }

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
    public Image[] getImages() {
        return ((IDecorations) this.delegate).getImages();
    }

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
    public boolean getMaximized() {
        return ((IDecorations) this.delegate).getMaximized();
    }

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
    public Menu getMenuBar() {
        return Menu.getInstance(((IDecorations) this.delegate).getMenuBar());
    }

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
    public boolean getMinimized() {
        return ((IDecorations) this.delegate).getMinimized();
    }

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
    public String getText() {
        return ((IDecorations) this.delegate).getText();
    }

    @Override
    public boolean isReparentable() {
        return ((IDecorations) this.delegate).isReparentable();
    }

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
    public void setDefaultButton(Button button) {
        ((IDecorations) this.delegate).setDefaultButton((IButton) button.delegate);
    }

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
    public void setImage(Image image) {
        ((IDecorations) this.delegate).setImage(image);
    }

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
    public void setImages(Image[] images) {
        ((IDecorations) this.delegate).setImages(images);
    }

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
    public void setMaximized(boolean maximized) {
        ((IDecorations) this.delegate).setMaximized(maximized);
    }

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
    public void setMenuBar(Menu menu) {
        ((IDecorations) this.delegate).setMenuBar((IMenu) menu.delegate);
    }

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
    public void setMinimized(boolean minimized) {
        ((IDecorations) this.delegate).setMinimized(minimized);
    }

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
    public void setText(String string) {
        ((IDecorations) this.delegate).setText(string);
    }

    protected Decorations(IDecorations delegate) {
        super(delegate);
        this.delegate = delegate;
        INSTANCES.put(delegate, this);
    }

    public static Decorations getInstance(IDecorations delegate) {
        if (delegate == null) {
            return null;
        }
        Decorations ref = (Decorations) INSTANCES.get(delegate);
        if (ref == null) {
            ref = new Decorations(delegate);
        }
        return ref;
    }
}
