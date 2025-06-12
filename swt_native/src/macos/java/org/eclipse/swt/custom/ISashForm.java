package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ISashForm extends IComposite {

    /**
     * Returns SWT.HORIZONTAL if the controls in the SashForm are laid out side by side
     * or SWT.VERTICAL   if the controls in the SashForm are laid out top to bottom.
     *
     * <p>
     * To retrieve the bidi orientation of the SashForm use <code>{@link #getStyle()}</code>
     * and test if the SWT.RIGHT_TO_LEFT or SWT.LEFT_TO_RIGHT bits are set.
     * </p>
     *
     * @return SWT.HORIZONTAL or SWT.VERTICAL
     */
    int getOrientation();

    /**
     * Returns the width of the sashes when the controls in the SashForm are
     * laid out.
     *
     * @return the width of the sashes
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    int getSashWidth();

    int getStyle();

    /**
     * Answer the control that currently is maximized in the SashForm.
     * This value may be null.
     *
     * @return the control that currently is maximized or null
     */
    Control getMaximizedControl();

    int[] getWeights();

    /**
     * If orientation is SWT.HORIZONTAL, lay the controls in the SashForm
     * out side by side.  If orientation is SWT.VERTICAL, lay the
     * controls in the SashForm out top to bottom.
     *
     * <p>
     * Since 3.7, this method can also be called with SWT.RIGHT_TO_LEFT or SWT.LEFT_TO_RIGHT
     * to change the bidi orientation of the SashForm.
     * </p>
     *
     * @param orientation SWT.HORIZONTAL or SWT.VERTICAL, SWT.RIGHT_TO_LEFT or SWT.LEFT_TO_RIGHT
     *
     * @see Control#setOrientation(int)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the value of orientation is not SWT.HORIZONTAL or SWT.VERTICAL, SWT.RIGHT_TO_LEFT or SWT.LEFT_TO_RIGHT
     * </ul>
     */
    void setOrientation(int orientation);

    void setBackground(Color color);

    void setForeground(Color color);

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
     * Specify the control that should take up the entire client area of the SashForm.
     * If one control has been maximized, and this method is called with a different control,
     * the previous control will be minimized and the new control will be maximized.
     * If the value of control is null, the SashForm will minimize all controls and return to
     * the default layout where all controls are laid out separated by sashes.
     *
     * @param control the control to be maximized or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setMaximizedControl(Control control);

    /**
     * Specify the width of the sashes when the controls in the SashForm are
     * laid out.
     *
     * @param width the width of the sashes
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    void setSashWidth(int width);

    void setToolTipText(String string);

    /**
     * Specify the relative weight of each child in the SashForm.  This will determine
     * what percent of the total width (if SashForm has Horizontal orientation) or
     * total height (if SashForm has Vertical orientation) each control will occupy.
     * The weights must be positive values and there must be an entry for each
     * non-sash child of the SashForm.
     *
     * @param weights the relative weight of each child
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the weights value is null or of incorrect length (must match the number of children)</li>
     * </ul>
     */
    void setWeights(int... weights);

    SashForm getApi();
}
