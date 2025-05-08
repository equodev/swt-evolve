package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.graphics.*;

public interface ICanvas extends IComposite {

    /**
     * Fills the interior of the rectangle specified by the arguments,
     * with the receiver's background.
     *
     * @param gc the gc where the rectangle is to be filled
     * @param x the x coordinate of the rectangle to be filled
     * @param y the y coordinate of the rectangle to be filled
     * @param width the width of the rectangle to be filled
     * @param height the height of the rectangle to be filled
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the gc is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the gc has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    void drawBackground(GC gc, int x, int y, int width, int height);

    /**
     * Returns the caret.
     * <p>
     * The caret for the control is automatically hidden
     * and shown when the control is painted or resized,
     * when focus is gained or lost and when an the control
     * is scrolled.  To avoid drawing on top of the caret,
     * the programmer must hide and show the caret when
     * drawing in the window any other time.
     * </p>
     *
     * @return the caret for the receiver, may be null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    ICaret getCaret();

    /**
     * Returns the IME.
     *
     * @return the IME
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    IIME getIME();

    /**
     * Scrolls a rectangular area of the receiver by first copying
     * the source area to the destination and then causing the area
     * of the source which is not covered by the destination to
     * be repainted. Children that intersect the rectangle are
     * optionally moved during the operation. In addition, all outstanding
     * paint events are flushed before the source area is copied to
     * ensure that the contents of the canvas are drawn correctly.
     *
     * @param destX the x coordinate of the destination
     * @param destY the y coordinate of the destination
     * @param x the x coordinate of the source
     * @param y the y coordinate of the source
     * @param width the width of the area
     * @param height the height of the area
     * @param all <code>true</code>if children should be scrolled, and <code>false</code> otherwise
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void scroll(int destX, int destY, int x, int y, int width, int height, boolean all);

    /**
     * Sets the receiver's caret.
     * <p>
     * The caret for the control is automatically hidden
     * and shown when the control is painted or resized,
     * when focus is gained or lost and when an the control
     * is scrolled.  To avoid drawing on top of the caret,
     * the programmer must hide and show the caret when
     * drawing in the window any other time.
     * </p>
     * @param caret the new caret for the receiver, may be null
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the caret has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setCaret(ICaret caret);

    void setFont(Font font);

    /**
     * Sets the receiver's IME.
     *
     * @param ime the new IME for the receiver, may be null
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the IME has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    void setIME(IIME ime);

    Canvas getApi();

    void setApi(Canvas api);
}
