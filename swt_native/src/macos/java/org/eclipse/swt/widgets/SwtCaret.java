/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2019 IBM Corporation and others.
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

import org.eclipse.swt.internal.cocoa.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

/**
 * Instances of this class provide an i-beam that is typically used
 * as the insertion point for text.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#caret">Caret snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample, Canvas tab</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class SwtCaret extends SwtWidget implements ICaret {

    Canvas parent;

    int x, y, width, height;

    boolean isVisible, isShowing;

    int blinkRate;

    Image image;

    Font font;

    static final int DEFAULT_WIDTH = 1;

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
     * @see SWT
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public SwtCaret(Canvas parent, int style, Caret api) {
        super(parent, style, api);
        this.parent = parent;
        createWidget();
    }

    public boolean blinkCaret() {
        if (!isVisible)
            return true;
        if (!isShowing)
            return showCaret();
        if (blinkRate == 0)
            return true;
        return hideCaret();
    }

    @Override
    void createWidget() {
        super.createWidget();
        blinkRate = ((SwtDisplay) display.getImpl()).getCaretBlinkTime();
        isVisible = true;
        if (parent.getCaret() == null) {
            parent.setCaret(this.getApi());
        }
    }

    boolean drawCaret() {
        if (parent == null)
            return false;
        if (parent.isDisposed())
            return false;
        int nWidth = width, nHeight = height;
        if (nWidth <= 0)
            nWidth = DEFAULT_WIDTH;
        if (image != null) {
            NSSize size = image.handle.size();
            nWidth = (int) size.width;
            nHeight = (int) size.height;
        }
        NSRect rect = new NSRect();
        rect.x = x;
        rect.y = y;
        rect.width = nWidth;
        rect.height = nHeight;
        if (parent == null || parent.getImpl() instanceof SwtCanvas) {
            parent.view.setNeedsDisplayInRect(rect);
        }
        return true;
    }

    /**
     * Returns a rectangle describing the receiver's size and location
     * relative to its parent (or its display if its parent is null).
     *
     * @return the receiver's bounding rectangle
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Rectangle getBounds() {
        checkWidget();
        if (image != null) {
            Rectangle rect = image.getBounds();
            return new Rectangle(x, y, rect.width, rect.height);
        } else {
            if (width == 0) {
                return new Rectangle(x, y, DEFAULT_WIDTH, height);
            }
        }
        return new Rectangle(x, y, width, height);
    }

    /**
     * Returns the font that the receiver will use to paint textual information.
     *
     * @return the receiver's font
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Font getFont() {
        checkWidget();
        if (font != null)
            return font;
        return parent.getFont();
    }

    /**
     * Returns the image that the receiver will use to paint the caret.
     *
     * @return the receiver's image
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Image getImage() {
        checkWidget();
        return image;
    }

    /**
     * Returns a point describing the receiver's location relative
     * to its parent (or its display if its parent is null).
     *
     * @return the receiver's location
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Point getLocation() {
        checkWidget();
        return new Point(x, y);
    }

    /**
     * Returns the receiver's parent, which must be a <code>Canvas</code>.
     *
     * @return the receiver's parent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Canvas getParent() {
        checkWidget();
        return parent;
    }

    /**
     * Returns a point describing the receiver's size.
     *
     * @return the receiver's size
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Point getSize() {
        checkWidget();
        if (image != null) {
            Rectangle rect = image.getBounds();
            return new Point(rect.width, rect.height);
        } else {
            if (width == 0) {
                return new Point(DEFAULT_WIDTH, height);
            }
        }
        return new Point(width, height);
    }

    /**
     * Returns <code>true</code> if the receiver is visible, and
     * <code>false</code> otherwise.
     * <p>
     * If one of the receiver's ancestors is not visible or some
     * other condition makes the receiver not visible, this method
     * may still indicate that it is considered visible even though
     * it may not actually be showing.
     * </p>
     *
     * @return the receiver's visibility state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean getVisible() {
        checkWidget();
        return isVisible;
    }

    boolean hideCaret() {
        if (!isShowing)
            return true;
        isShowing = false;
        return drawCaret();
    }

    /**
     * Returns <code>true</code> if the receiver is visible and all
     * of the receiver's ancestors are visible and <code>false</code>
     * otherwise.
     *
     * @return the receiver's visibility state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #getVisible
     */
    public boolean isVisible() {
        checkWidget();
        return isVisible && parent.isVisible() && ((SwtControl) parent.getImpl()).hasFocus();
    }

    boolean isFocusCaret() {
        return this.getApi() == ((SwtDisplay) display.getImpl()).currentCaret;
    }

    void killFocus() {
        if (((SwtDisplay) display.getImpl()).currentCaret != this.getApi())
            return;
        ((SwtDisplay) display.getImpl()).setCurrentCaret(null);
        if (isVisible)
            hideCaret();
    }

    @Override
    void releaseParent() {
        super.releaseParent();
        if (parent != null && this.getApi() == parent.getImpl()._caret()) {
            if (!parent.isDisposed())
                parent.setCaret(null);
            else {
                if (parent.getImpl() instanceof DartCanvas) {
                    ((DartCanvas) parent.getImpl()).caret = null;
                }
                if (parent.getImpl() instanceof SwtCanvas) {
                    ((SwtCanvas) parent.getImpl()).caret = null;
                }
            }
        }
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        if (((SwtDisplay) display.getImpl()).currentCaret == this.getApi()) {
            hideCaret();
            ((SwtDisplay) display.getImpl()).setCurrentCaret(null);
        }
        parent = null;
        image = null;
    }

    /**
     * Sets the receiver's size and location to the rectangular
     * area specified by the arguments. The <code>x</code> and
     * <code>y</code> arguments are relative to the receiver's
     * parent (or its display if its parent is null).
     *
     * @param x the new x coordinate for the receiver
     * @param y the new y coordinate for the receiver
     * @param width the new width for the receiver
     * @param height the new height for the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setBounds(int x, int y, int width, int height) {
        checkWidget();
        if (this.x == x && this.y == y && this.width == width && this.height == height)
            return;
        boolean isFocus = isFocusCaret();
        if (isFocus && isVisible)
            hideCaret();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        if (isFocus && isVisible)
            showCaret();
    }

    /**
     * Sets the receiver's size and location to the rectangular
     * area specified by the argument. The <code>x</code> and
     * <code>y</code> fields of the rectangle are relative to
     * the receiver's parent (or its display if its parent is null).
     *
     * @param rect the new bounds for the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setBounds(Rectangle rect) {
        checkWidget();
        if (rect == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        setBounds(rect.x, rect.y, rect.width, rect.height);
    }

    void setFocus() {
        if (((SwtDisplay) display.getImpl()).currentCaret == this.getApi())
            return;
        ((SwtDisplay) display.getImpl()).setCurrentCaret(this.getApi());
        if (isVisible)
            showCaret();
    }

    /**
     * Sets the font that the receiver will use to paint textual information
     * to the font specified by the argument, or to the default font for that
     * kind of control if the argument is null.
     *
     * @param font the new font (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the font has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setFont(Font font) {
        checkWidget();
        if (font != null && font.isDisposed()) {
            error(SWT.ERROR_INVALID_ARGUMENT);
        }
        this.font = font;
    }

    /**
     * Sets the image that the receiver will use to paint the caret
     * to the image specified by the argument, or to the default
     * which is a filled rectangle if the argument is null
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
        checkWidget();
        if (image != null && image.isDisposed()) {
            error(SWT.ERROR_INVALID_ARGUMENT);
        }
        boolean isFocus = isFocusCaret();
        if (isFocus && isVisible)
            hideCaret();
        this.image = image;
        if (isFocus && isVisible)
            showCaret();
    }

    /**
     * Sets the receiver's location to the point specified by
     * the arguments which are relative to the receiver's
     * parent (or its display if its parent is null).
     *
     * @param x the new x coordinate for the receiver
     * @param y the new y coordinate for the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setLocation(int x, int y) {
        checkWidget();
        setBounds(x, y, width, height);
    }

    /**
     * Sets the receiver's location to the point specified by
     * the argument which is relative to the receiver's
     * parent (or its display if its parent is null).
     *
     * @param location the new location for the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setLocation(Point location) {
        checkWidget();
        if (location == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        setLocation(location.x, location.y);
    }

    /**
     * Sets the receiver's size to the point specified by the arguments.
     *
     * @param width the new width for the receiver
     * @param height the new height for the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setSize(int width, int height) {
        checkWidget();
        setBounds(x, y, width, height);
    }

    /**
     * Sets the receiver's size to the point specified by the argument.
     *
     * @param size the new extent for the receiver
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setSize(Point size) {
        checkWidget();
        if (size == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        setSize(size.x, size.y);
    }

    /**
     * Marks the receiver as visible if the argument is <code>true</code>,
     * and marks it invisible otherwise.
     * <p>
     * If one of the receiver's ancestors is not visible or some
     * other condition makes the receiver not visible, marking
     * it visible may not actually cause it to be displayed.
     * </p>
     *
     * @param visible the new visibility state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setVisible(boolean visible) {
        checkWidget();
        if (visible == isVisible)
            return;
        isVisible = visible;
        if (!isFocusCaret())
            return;
        if (isVisible) {
            showCaret();
        } else {
            hideCaret();
        }
    }

    boolean showCaret() {
        if (isShowing)
            return true;
        isShowing = true;
        return drawCaret();
    }

    public Canvas _parent() {
        return parent;
    }

    public int _x() {
        return x;
    }

    public int _y() {
        return y;
    }

    public int _width() {
        return width;
    }

    public int _height() {
        return height;
    }

    public boolean _isVisible() {
        return isVisible;
    }

    public boolean _isShowing() {
        return isShowing;
    }

    public int _blinkRate() {
        return blinkRate;
    }

    public Image _image() {
        return image;
    }

    public Font _font() {
        return font;
    }

    public Caret getApi() {
        if (api == null)
            api = Caret.createApi(this);
        return (Caret) api;
    }
}
