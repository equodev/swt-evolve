/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2016 IBM Corporation and others.
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
import java.util.Objects;
import dev.equo.swt.*;

/**
 * Instances of the receiver represent an unselectable
 * user interface object that is used to display progress,
 * typically in the form of a bar.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SMOOTH, HORIZONTAL, VERTICAL, INDETERMINATE</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles HORIZONTAL and VERTICAL may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#progressbar">ProgressBar snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartProgressBar extends DartControl implements IProgressBar {

    int timerId, minimum = 0, maximum = 100, selection = 0;

    static final int DELAY = 100;

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
     * @see SWT#SMOOTH
     * @see SWT#HORIZONTAL
     * @see SWT#VERTICAL
     * @see SWT#INDETERMINATE
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartProgressBar(Composite parent, int style, ProgressBar api) {
        super(parent, checkStyle(style), api);
    }

    static int checkStyle(int style) {
        style |= SWT.NO_FOCUS;
        return checkBits(style, SWT.HORIZONTAL, SWT.VERTICAL, 0, 0, 0, 0);
    }

    @Override
    void createHandle(int index) {
    }

    @Override
    long eventHandle() {
        return fixedHandle;
    }

    /**
     * Returns the maximum value which the receiver will allow.
     *
     * @return the maximum
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getMaximum() {
        checkWidget();
        return maximum;
    }

    /**
     * Returns the minimum value which the receiver will allow.
     *
     * @return the minimum
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getMinimum() {
        checkWidget();
        return minimum;
    }

    /**
     * Returns the single 'selection' that is the receiver's position.
     *
     * @return the selection
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getSelection() {
        checkWidget();
        return selection;
    }

    /**
     * Returns the state of the receiver. The value will be one of:
     * <ul>
     * 	<li>{@link SWT#NORMAL}</li>
     * 	<li>{@link SWT#ERROR}</li>
     * 	<li>{@link SWT#PAUSED}</li>
     * </ul>
     *
     * @return the state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    public int getState() {
        checkWidget();
        return SWT.NORMAL;
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        timerId = 0;
    }

    @Override
    void setParentBackground() {
        /*
	* Bug in GTK.  For some reason, some theme managers will crash
	* when the progress bar is inheriting the background from a parent.
	* The fix is to stop inheriting the background. This is acceptable
	* since progress bars do not use the inherited background.
	*/
    }

    /**
     * Sets the maximum value that the receiver will allow.  This new
     * value will be ignored if it is not greater than the receiver's current
     * minimum value.  If the new maximum is applied then the receiver's
     * selection value will be adjusted if necessary to fall within its new range.
     *
     * @param value the new maximum, which must be greater than the current minimum
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setMaximum(int value) {
        checkWidget();
        if (!java.util.Objects.equals(this.maximum, value)) {
            dirty();
        }
        if (value <= minimum)
            return;
        maximum = value;
        selection = Math.min(selection, maximum);
        updateBar();
    }

    /**
     * Sets the minimum value that the receiver will allow.  This new
     * value will be ignored if it is negative or is not less than the receiver's
     * current maximum value.  If the new minimum is applied then the receiver's
     * selection value will be adjusted if necessary to fall within its new range.
     *
     * @param value the new minimum, which must be nonnegative and less than the current maximum
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setMinimum(int value) {
        checkWidget();
        if (!java.util.Objects.equals(this.minimum, value)) {
            dirty();
        }
        if (value < 0 || value >= maximum)
            return;
        minimum = value;
        selection = Math.max(selection, minimum);
        updateBar();
    }

    /**
     * Sets the single 'selection' that is the receiver's
     * position to the argument which must be greater than or equal
     * to zero.
     *
     * @param value the new selection (must be zero or greater)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setSelection(int value) {
        checkWidget();
        if (!java.util.Objects.equals(this.selection, value)) {
            dirty();
        }
        selection = Math.max(minimum, Math.min(maximum, value));
        updateBar();
    }

    /**
     * Sets the state of the receiver. The state must be one of these values:
     * <ul>
     * 	<li>{@link SWT#NORMAL}</li>
     * 	<li>{@link SWT#ERROR}</li>
     * 	<li>{@link SWT#PAUSED}</li>
     * </ul>
     * <p>
     * Note: This operation is a hint and is not supported on
     * platforms that do not have this concept.
     * </p>
     *
     * @param state the new state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    public void setState(int state) {
        checkWidget();
        //NOT SUPPORTED BY GTK
    }

    @Override
    long timerProc(long widget) {
        return 1;
    }

    void updateBar() {
    }

    public int _timerId() {
        return timerId;
    }

    public int _minimum() {
        return minimum;
    }

    public int _maximum() {
        return maximum;
    }

    public int _selection() {
        return selection;
    }

    protected void _hookEvents() {
        super._hookEvents();
    }

    public ProgressBar getApi() {
        if (api == null)
            api = ProgressBar.createApi(this);
        return (ProgressBar) api;
    }

    public VProgressBar getValue() {
        if (value == null)
            value = new VProgressBar(this);
        return (VProgressBar) value;
    }
}
