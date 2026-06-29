package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ICaret extends IWidget, ImplCaret {

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
    Rectangle getBounds();

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
    Font getFont();

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
    Image getImage();

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
    Point getLocation();

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
    Canvas getParent();

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
    Point getSize();

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
    boolean getVisible();

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
    boolean isVisible();

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
    void setBounds(int x, int y, int width, int height);

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
    void setBounds(Rectangle rect);

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
    void setFont(Font font);

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
    void setImage(Image image);

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
    void setLocation(int x, int y);

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
    void setLocation(Point location);

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
    void setSize(int width, int height);

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
    void setSize(Point size);

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
    void setVisible(boolean visible);

    Caret getApi();
}
