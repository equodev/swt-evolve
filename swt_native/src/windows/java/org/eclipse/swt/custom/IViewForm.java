package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface IViewForm extends IComposite {

    Rectangle computeTrim(int x, int y, int width, int height);

    Rectangle getClientArea();

    /**
     * Returns the content area.
     *
     * @return the control in the content area of the pane or null
     */
    Control getContent();

    /**
     * Returns Control that appears in the top center of the pane.
     * Typically this is a toolbar.
     *
     * @return the control in the top center of the pane or null
     */
    Control getTopCenter();

    /**
     * Returns the Control that appears in the top left corner of the pane.
     * Typically this is a label such as CLabel.
     *
     * @return the control in the top left corner of the pane or null
     */
    Control getTopLeft();

    /**
     * Returns the control in the top right corner of the pane.
     * Typically this is a Close button or a composite with a Menu and Close button.
     *
     * @return the control in the top right corner of the pane or null
     */
    Control getTopRight();

    /**
     * Sets the content.
     * Setting the content to null will remove it from
     * the pane - however, the creator of the content must dispose of the content.
     *
     * @param content the control to be displayed in the content area or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the control is not a child of this ViewForm</li>
     * </ul>
     */
    void setContent(Control content);

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
     * Set the control that appears in the top center of the pane.
     * Typically this is a toolbar.
     * The topCenter is optional.  Setting the topCenter to null will remove it from
     * the pane - however, the creator of the topCenter must dispose of the topCenter.
     *
     * @param topCenter the control to be displayed in the top center or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the control is not a child of this ViewForm</li>
     * </ul>
     */
    void setTopCenter(Control topCenter);

    /**
     * Set the control that appears in the top left corner of the pane.
     * Typically this is a label such as CLabel.
     * The topLeft is optional.  Setting the top left control to null will remove it from
     * the pane - however, the creator of the control must dispose of the control.
     *
     * @param c the control to be displayed in the top left corner or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the control is not a child of this ViewForm</li>
     * </ul>
     */
    void setTopLeft(Control c);

    /**
     * Set the control that appears in the top right corner of the pane.
     * Typically this is a Close button or a composite with a Menu and Close button.
     * The topRight is optional.  Setting the top right control to null will remove it from
     * the pane - however, the creator of the control must dispose of the control.
     *
     * @param c the control to be displayed in the top right corner or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the control is not a child of this ViewForm</li>
     * </ul>
     */
    void setTopRight(Control c);

    /**
     * Specify whether the border should be displayed or not.
     *
     * @param show true if the border should be displayed
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setBorderVisible(boolean show);

    /**
     * If true, the topCenter will always appear on a separate line by itself, otherwise the
     * topCenter will appear in the top row if there is room and will be moved to the second row if
     * required.
     *
     * @param show true if the topCenter will always appear on a separate line by itself
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setTopCenterSeparate(boolean show);

    ViewForm getApi();
}
