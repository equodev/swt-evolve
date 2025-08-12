package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ICBanner extends IComposite {

    /**
     * Returns the Control that appears on the bottom side of the banner.
     *
     * @return the control that appears on the bottom side of the banner or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    Control getBottom();

    Rectangle getClientArea();

    /**
     * Returns the Control that appears on the left side of the banner.
     *
     * @return the control that appears on the left side of the banner or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    Control getLeft();

    /**
     * Returns the Control that appears on the right side of the banner.
     *
     * @return the control that appears on the right side of the banner or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    Control getRight();

    /**
     * Returns the minimum size of the control that appears on the right of the banner.
     *
     * @return the minimum size of the control that appears on the right of the banner
     *
     * @since 3.1
     */
    Point getRightMinimumSize();

    /**
     * Returns the width of the control that appears on the right of the banner.
     *
     * @return the width of the control that appears on the right of the banner
     *
     * @since 3.0
     */
    int getRightWidth();

    /**
     * Returns <code>true</code> if the CBanner is rendered
     * with a simple, traditional shape.
     *
     * @return <code>true</code> if the CBanner is rendered with a simple shape
     *
     * @since 3.0
     */
    boolean getSimple();

    /**
     * Set the control that appears on the bottom side of the banner.
     * The bottom control is optional.  Setting the bottom control to null will remove it from
     * the banner - however, the creator of the control must dispose of the control.
     *
     * @param control the control to be displayed on the bottom or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the bottom control was not created as a child of the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    void setBottom(Control control);

    /**
     * Sets the layout which is associated with the receiver to be
     * the argument which may be null.
     * <p>
     * Note: No Layout can be set on this Control because it already
     * manages the size and position of its children.
     * </p>
     *
     * @param layout the receiver's new layout or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setLayout(Layout layout);

    /**
     * Set the control that appears on the left side of the banner.
     * The left control is optional.  Setting the left control to null will remove it from
     * the banner - however, the creator of the control must dispose of the control.
     *
     * @param control the control to be displayed on the left or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the left control was not created as a child of the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    void setLeft(Control control);

    /**
     * Set the control that appears on the right side of the banner.
     * The right control is optional.  Setting the right control to null will remove it from
     * the banner - however, the creator of the control must dispose of the control.
     *
     * @param control the control to be displayed on the right or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the right control was not created as a child of the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    void setRight(Control control);

    /**
     * Set the minimum height of the control that appears on the right side of the banner.
     *
     * @param size the minimum size of the control on the right
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the size is null or the values of size are less than SWT.DEFAULT</li>
     * </ul>
     *
     * @since 3.1
     */
    void setRightMinimumSize(Point size);

    /**
     * Set the width of the control that appears on the right side of the banner.
     *
     * @param width the width of the control on the right
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if width is less than SWT.DEFAULT</li>
     * </ul>
     *
     * @since 3.0
     */
    void setRightWidth(int width);

    /**
     * Sets the shape that the CBanner will use to render itself.
     *
     * @param simple <code>true</code> if the CBanner should render itself in a simple, traditional style
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    void setSimple(boolean simple);

    CBanner getApi();
}
