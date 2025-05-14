package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ICoolItem extends IItem {

    /**
     * Adds the listener to the collection of listeners that will
     * be notified when the control is selected by the user, by sending it one
     * of the messages defined in the <code>SelectionListener</code>
     * interface.
     * <p>
     * If <code>widgetSelected</code> is called when the mouse is over
     * the drop-down arrow (or 'chevron') portion of the cool item,
     * the event object detail field contains the value <code>SWT.ARROW</code>,
     * and the x and y fields in the event object represent the point at
     * the bottom left of the chevron, where the menu should be popped up.
     * <code>widgetDefaultSelected</code> is not called.
     * </p>
     *
     * @param listener the listener which should be notified when the control is selected by the user
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SelectionListener
     * @see #removeSelectionListener
     * @see SelectionEvent
     *
     * @since 2.0
     */
    void addSelectionListener(SelectionListener listener);

    void checkSubclass();

    /**
     * Returns the preferred size of the receiver.
     * <p>
     * The <em>preferred size</em> of a <code>CoolItem</code> is the size that
     * it would best be displayed at. The width hint and height hint arguments
     * allow the caller to ask the instance questions such as "Given a particular
     * width, how high does it need to be to show all of the contents?"
     * To indicate that the caller does not wish to constrain a particular
     * dimension, the constant <code>SWT.DEFAULT</code> is passed for the hint.
     * </p>
     *
     * @param wHint the width hint (can be <code>SWT.DEFAULT</code>)
     * @param hHint the height hint (can be <code>SWT.DEFAULT</code>)
     * @return the preferred size
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Layout
     * @see #getBounds
     * @see #getSize
     * @see Control#getBorderWidth
     * @see Scrollable#computeTrim
     * @see Scrollable#getClientArea
     */
    IPoint computeSize(int wHint, int hHint);

    void dispose();

    /**
     * Returns a rectangle describing the receiver's size and location
     * relative to its parent.
     *
     * @return the receiver's bounding rectangle
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    IRectangle getBounds();

    /**
     * Returns the control that is associated with the receiver.
     *
     * @return the control that is contained by the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    IControl getControl();

    /**
     * Returns the minimum size that the cool item can
     * be resized to using the cool item's gripper.
     *
     * @return a point containing the minimum width and height of the cool item, in SWT logical points
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 2.0
     */
    IPoint getMinimumSize();

    /**
     * Returns the receiver's parent, which must be a <code>CoolBar</code>.
     *
     * @return the receiver's parent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    ICoolBar getParent();

    /**
     * Returns a point describing the receiver's ideal size.
     * The x coordinate of the result is the ideal width of the receiver.
     * The y coordinate of the result is the ideal height of the receiver.
     *
     * @return the receiver's ideal size
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    IPoint getPreferredSize();

    /**
     * Returns a point describing the receiver's size. The
     * x coordinate of the result is the width of the receiver.
     * The y coordinate of the result is the height of the
     * receiver.
     *
     * @return the receiver's size
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    IPoint getSize();

    /**
     * Removes the listener from the collection of listeners that
     * will be notified when the control is selected by the user.
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
     * @see SelectionListener
     * @see #addSelectionListener
     *
     * @since 2.0
     */
    void removeSelectionListener(SelectionListener listener);

    /**
     * Sets the control that is associated with the receiver
     * to the argument.
     *
     * @param control the new control that will be contained by the receiver
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the control has been disposed</li>
     *    <li>ERROR_INVALID_PARENT - if the control is not in the same widget tree</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setControl(IControl control);

    /**
     * Sets the minimum size that the cool item can be resized to
     * using the cool item's gripper, to the point specified by the arguments.
     *
     * @param width the minimum width of the cool item, in SWT logical points
     * @param height the minimum height of the cool item, in SWT logical points
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 2.0
     */
    void setMinimumSize(int width, int height);

    /**
     * Sets the minimum size that the cool item can be resized to
     * using the cool item's gripper, to the point specified by the argument.
     *
     * @param size a point representing the minimum width and height of the cool item, in SWT logical points
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 2.0
     */
    void setMinimumSize(IPoint size);

    /**
     * Sets the receiver's ideal size to the point specified by the arguments.
     *
     * @param width the new ideal width for the receiver
     * @param height the new ideal height for the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setPreferredSize(int width, int height);

    /**
     * Sets the receiver's ideal size to the point specified by the argument.
     *
     * @param size the new ideal size for the receiver
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setPreferredSize(IPoint size);

    /**
     * Sets the receiver's size to the point specified by the arguments.
     * <p>
     * Note: Attempting to set the width or height of the
     * receiver to a negative number will cause that
     * value to be set to zero instead.
     * </p>
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
     * <p>
     * Note: Attempting to set the width or height of the
     * receiver to a negative number will cause them to be
     * set to zero instead.
     * </p>
     *
     * @param size the new size for the receiver
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setSize(IPoint size);

    CoolItem getApi();
}
