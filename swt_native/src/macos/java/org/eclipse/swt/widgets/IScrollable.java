package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface IScrollable extends IControl {

    /**
     * Given a desired <em>client area</em> for the receiver
     * (as described by the arguments), returns the bounding
     * rectangle which would be required to produce that client
     * area.
     * <p>
     * In other words, it returns a rectangle such that, if the
     * receiver's bounds were set to that rectangle, the area
     * of the receiver which is capable of displaying data
     * (that is, not covered by the "trimmings") would be the
     * rectangle described by the arguments (relative to the
     * receiver's parent).
     * </p>
     *
     * @param x the desired x coordinate of the client area
     * @param y the desired y coordinate of the client area
     * @param width the desired width of the client area
     * @param height the desired height of the client area
     * @return the required bounds to produce the given client area
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #getClientArea
     */
    Rectangle computeTrim(int x, int y, int width, int height);

    /**
     * Returns a rectangle which describes the area of the
     * receiver which is capable of displaying data (that is,
     * not covered by the "trimmings").
     *
     * @return the client area
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #computeTrim
     */
    Rectangle getClientArea();

    /**
     * Returns the receiver's horizontal scroll bar if it has
     * one, and null if it does not.
     *
     * @return the horizontal scroll bar (or null)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    IScrollBar getHorizontalBar();

    /**
     * Returns the mode of the receiver's scrollbars. This will be
     * <em>bitwise</em> OR of one or more of the constants defined in class
     * <code>SWT</code>.<br>
     * <ul>
     * <li><code>SWT.SCROLLBAR_OVERLAY</code> - if receiver
     * uses overlay scrollbars</li>
     * <li><code>SWT.NONE</code> - otherwise</li>
     * </ul>
     *
     * @return the mode of scrollbars
     *
     * @exception SWTException <ul>
     * <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     * disposed</li>
     * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
     * thread that created the receiver</li>
     * </ul>
     *
     * @see SWT#SCROLLBAR_OVERLAY
     *
     * @since 3.8
     */
    int getScrollbarsMode();

    /**
     * Sets the mode of the receiver's scrollbars. This will be
     * <em>bitwise</em> OR of one or more of the constants defined in class
     * <code>SWT</code>.<br>
     * <ul>
     * <li><code>SWT.SCROLLBAR_OVERLAY</code> - if receiver
     * uses overlay scrollbars</li>
     * <li><code>SWT.NONE</code> - otherwise</li>
     * </ul>
     *
     * @exception SWTException <ul>
     * <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     * disposed</li>
     * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
     * thread that created the receiver</li>
     * </ul>
     *
     * @see SWT#SCROLLBAR_OVERLAY
     *
     * @since 3.126
     */
    void setScrollbarsMode(int mode);

    /**
     * Returns the receiver's vertical scroll bar if it has
     * one, and null if it does not.
     *
     * @return the vertical scroll bar (or null)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    IScrollBar getVerticalBar();

    Scrollable getApi();

    void setApi(Scrollable api);
}
