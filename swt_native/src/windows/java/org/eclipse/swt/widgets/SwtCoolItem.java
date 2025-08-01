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
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.win32.*;

/**
 * Instances of this class are selectable user interface
 * objects that represent the dynamically positionable
 * areas of a <code>CoolBar</code>.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>DROP_DOWN</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class SwtCoolItem extends SwtItem implements ICoolItem {

    CoolBar parent;

    Control control;

    int id;

    boolean ideal, minimum;

    /**
     * Constructs a new instance of this class given its parent
     * (which must be a <code>CoolBar</code>) and a style value
     * describing its behavior and appearance. The item is added
     * to the end of the items maintained by its parent.
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
     * @see SWT#DROP_DOWN
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public SwtCoolItem(CoolBar parent, int style, CoolItem api) {
        super(parent, style, api);
        this.parent = parent;
        ((SwtCoolBar) parent.getImpl()).createItem(this.getApi(), parent.getItemCount());
    }

    /**
     * Constructs a new instance of this class given its parent
     * (which must be a <code>CoolBar</code>), a style value
     * describing its behavior and appearance, and the index
     * at which to place it in the items maintained by its parent.
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
     * @param index the zero-relative index at which to store the receiver in its parent
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the parent (inclusive)</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see SWT#DROP_DOWN
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public SwtCoolItem(CoolBar parent, int style, int index, CoolItem api) {
        super(parent, style, api);
        this.parent = parent;
        ((SwtCoolBar) parent.getImpl()).createItem(this.getApi(), index);
    }

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
    public void addSelectionListener(SelectionListener listener) {
        addTypedListener(listener, SWT.Selection, SWT.DefaultSelection);
    }

    @Override
    public void checkSubclass() {
        if (!isValidSubclass())
            error(SWT.ERROR_INVALID_SUBCLASS);
    }

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
    public Point computeSize(int wHint, int hHint) {
        checkWidget();
        int zoom = getZoom();
        wHint = (wHint != SWT.DEFAULT ? DPIUtil.scaleUp(wHint, zoom) : wHint);
        hHint = (hHint != SWT.DEFAULT ? DPIUtil.scaleUp(hHint, zoom) : hHint);
        return DPIUtil.scaleDown(computeSizeInPixels(wHint, hHint), zoom);
    }

    Point computeSizeInPixels(int wHint, int hHint) {
        int index = parent.indexOf(this.getApi());
        if (index == -1)
            return new Point(0, 0);
        int width = wHint, height = hHint;
        if (wHint == SWT.DEFAULT)
            width = 32;
        if (hHint == SWT.DEFAULT)
            height = 32;
        if ((parent.style & SWT.VERTICAL) != 0) {
            height += ((SwtCoolBar) parent.getImpl()).getMargin(index);
        } else {
            width += ((SwtCoolBar) parent.getImpl()).getMargin(index);
        }
        return new Point(width, height);
    }

    @Override
    void destroyWidget() {
        ((SwtCoolBar) parent.getImpl()).destroyItem(this.getApi());
        releaseHandle();
    }

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
    public Rectangle getBounds() {
        checkWidget();
        return DPIUtil.scaleDown(getBoundsInPixels(), getZoom());
    }

    Rectangle getBoundsInPixels() {
        int index = parent.indexOf(this.getApi());
        if (index == -1)
            return new Rectangle(0, 0, 0, 0);
        long hwnd = parent.handle;
        RECT rect = new RECT();
        OS.SendMessage(hwnd, OS.RB_GETRECT, index, rect);
        MARGINS margins = new MARGINS();
        OS.SendMessage(hwnd, OS.RB_GETBANDMARGINS, 0, margins);
        rect.left -= margins.cxLeftWidth;
        rect.right += margins.cxRightWidth;
        if (!((SwtCoolBar) parent.getImpl()).isLastItemOfRow(index)) {
            rect.right += (parent.style & SWT.FLAT) == 0 ? SwtCoolBar.SEPARATOR_WIDTH : 0;
        }
        int width = rect.right - rect.left;
        int height = rect.bottom - rect.top;
        if ((parent.style & SWT.VERTICAL) != 0) {
            return new Rectangle(rect.top, rect.left, height, width);
        }
        return new Rectangle(rect.left, rect.top, width, height);
    }

    Rectangle getClientArea() {
        checkWidget();
        int index = parent.indexOf(this.getApi());
        if (index == -1)
            return new Rectangle(0, 0, 0, 0);
        long hwnd = parent.handle;
        RECT insetRect = new RECT();
        OS.SendMessage(hwnd, OS.RB_GETBANDBORDERS, index, insetRect);
        RECT rect = new RECT();
        OS.SendMessage(hwnd, OS.RB_GETRECT, index, rect);
        int x = rect.left + insetRect.left;
        int y = rect.top;
        int width = rect.right - rect.left - insetRect.left;
        int height = rect.bottom - rect.top;
        if ((parent.style & SWT.FLAT) == 0) {
            y += insetRect.top;
            width -= insetRect.right;
            height -= insetRect.top + insetRect.bottom;
        }
        if (index == 0) {
            REBARBANDINFO rbBand = new REBARBANDINFO();
            rbBand.cbSize = REBARBANDINFO.sizeof;
            rbBand.fMask = OS.RBBIM_HEADERSIZE;
            OS.SendMessage(hwnd, OS.RB_GETBANDINFO, index, rbBand);
            width = width - rbBand.cxHeader + 1;
        }
        return new Rectangle(x, y, Math.max(0, width), Math.max(0, height));
    }

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
    public Control getControl() {
        checkWidget();
        return control;
    }

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
    public CoolBar getParent() {
        checkWidget();
        return parent;
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
        parent = null;
        id = -1;
        control = null;
    }

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
    public void setControl(Control control) {
        checkWidget();
        if (control != null) {
            if (control.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
            if (((SwtControl) control.getImpl()).parent != parent)
                error(SWT.ERROR_INVALID_PARENT);
        }
        int index = parent.indexOf(this.getApi());
        if (index == -1)
            return;
        if (this.control != null && this.control.isDisposed()) {
            this.control = null;
        }
        Control oldControl = this.control, newControl = control;
        long hwnd = parent.handle;
        if (control == null || control.getImpl() instanceof SwtControl) {
            long hwndChild = newControl != null ? ((SwtControl) control.getImpl()).topHandle() : 0;
            REBARBANDINFO rbBand = new REBARBANDINFO();
            rbBand.cbSize = REBARBANDINFO.sizeof;
            rbBand.fMask = OS.RBBIM_CHILD;
            rbBand.hwndChild = hwndChild;
            this.control = newControl;
            /*
	* Feature in Windows.  When Windows sets the rebar band child,
	* it makes the new child visible and hides the old child and
	* moves the new child to the top of the Z-order.  The fix is
	* to save and restore the visibility and Z-order.
	*/
            long hwndAbove = 0;
            if (newControl != null) {
                hwndAbove = OS.GetWindow(hwndChild, OS.GW_HWNDPREV);
            }
            boolean hideNew = newControl != null && !newControl.getVisible();
            boolean showOld = oldControl != null && oldControl.getVisible();
            OS.SendMessage(hwnd, OS.RB_SETBANDINFO, index, rbBand);
            if (hideNew)
                newControl.setVisible(false);
            if (showOld)
                oldControl.setVisible(true);
            if (hwndAbove != 0 && hwndAbove != hwndChild) {
                int flags = OS.SWP_NOSIZE | OS.SWP_NOMOVE | OS.SWP_NOACTIVATE;
                OS.SetWindowPos(hwndChild, hwndAbove, 0, 0, 0, 0, flags);
            }
        }
    }

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
    public Point getPreferredSize() {
        checkWidget();
        return DPIUtil.scaleDown(getPreferredSizeInPixels(), getZoom());
    }

    Point getPreferredSizeInPixels() {
        int index = parent.indexOf(this.getApi());
        if (index == -1)
            return new Point(0, 0);
        long hwnd = parent.handle;
        REBARBANDINFO rbBand = new REBARBANDINFO();
        rbBand.cbSize = REBARBANDINFO.sizeof;
        rbBand.fMask = OS.RBBIM_CHILDSIZE | OS.RBBIM_IDEALSIZE;
        OS.SendMessage(hwnd, OS.RB_GETBANDINFO, index, rbBand);
        int width = rbBand.cxIdeal + ((SwtCoolBar) parent.getImpl()).getMargin(index);
        if ((parent.style & SWT.VERTICAL) != 0) {
            return new Point(rbBand.cyMaxChild, width);
        }
        return new Point(width, rbBand.cyMaxChild);
    }

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
    public void setPreferredSize(int width, int height) {
        checkWidget();
        int zoom = getZoom();
        setPreferredSizeInPixels(DPIUtil.scaleUp(width, zoom), DPIUtil.scaleUp(height, zoom));
    }

    void setPreferredSizeInPixels(int width, int height) {
        int index = parent.indexOf(this.getApi());
        if (index == -1)
            return;
        width = Math.max(0, width);
        height = Math.max(0, height);
        ideal = true;
        long hwnd = parent.handle;
        int cxIdeal, cyMaxChild;
        if ((parent.style & SWT.VERTICAL) != 0) {
            cxIdeal = Math.max(0, height - ((SwtCoolBar) parent.getImpl()).getMargin(index));
            cyMaxChild = width;
        } else {
            cxIdeal = Math.max(0, width - ((SwtCoolBar) parent.getImpl()).getMargin(index));
            cyMaxChild = height;
        }
        REBARBANDINFO rbBand = new REBARBANDINFO();
        rbBand.cbSize = REBARBANDINFO.sizeof;
        /* Get the child size fields first so we don't overwrite them. */
        rbBand.fMask = OS.RBBIM_CHILDSIZE;
        OS.SendMessage(hwnd, OS.RB_GETBANDINFO, index, rbBand);
        /* Set the size fields we are currently modifying. */
        rbBand.fMask = OS.RBBIM_CHILDSIZE | OS.RBBIM_IDEALSIZE;
        rbBand.cxIdeal = cxIdeal;
        rbBand.cyMaxChild = cyMaxChild;
        if (!minimum)
            rbBand.cyMinChild = cyMaxChild;
        OS.SendMessage(hwnd, OS.RB_SETBANDINFO, index, rbBand);
    }

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
    public void setPreferredSize(Point size) {
        checkWidget();
        if (size == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        size = DPIUtil.scaleUp(size, getZoom());
        setPreferredSizeInPixels(size.x, size.y);
    }

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
    public Point getSize() {
        checkWidget();
        return DPIUtil.scaleDown(getSizeInPixels(), getZoom());
    }

    public Point getSizeInPixels() {
        int index = parent.indexOf(this.getApi());
        if (index == -1)
            return new Point(0, 0);
        long hwnd = parent.handle;
        RECT rect = new RECT();
        OS.SendMessage(hwnd, OS.RB_GETRECT, index, rect);
        MARGINS margins = new MARGINS();
        OS.SendMessage(hwnd, OS.RB_GETBANDMARGINS, 0, margins);
        rect.left -= margins.cxLeftWidth;
        rect.right += margins.cxRightWidth;
        if (!((SwtCoolBar) parent.getImpl()).isLastItemOfRow(index)) {
            rect.right += (parent.style & SWT.FLAT) == 0 ? SwtCoolBar.SEPARATOR_WIDTH : 0;
        }
        int width = rect.right - rect.left;
        int height = rect.bottom - rect.top;
        if ((parent.style & SWT.VERTICAL) != 0) {
            return new Point(height, width);
        }
        return new Point(width, height);
    }

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
    public void setSize(int width, int height) {
        checkWidget();
        int zoom = getZoom();
        setSizeInPixels(DPIUtil.scaleUp(width, zoom), DPIUtil.scaleUp(height, zoom));
    }

    void setSizeInPixels(int width, int height) {
        int index = parent.indexOf(this.getApi());
        if (index == -1)
            return;
        width = Math.max(0, width);
        height = Math.max(0, height);
        long hwnd = parent.handle;
        int cx, cyChild, cxIdeal;
        if ((parent.style & SWT.VERTICAL) != 0) {
            cx = height;
            cyChild = width;
            cxIdeal = Math.max(0, height - ((SwtCoolBar) parent.getImpl()).getMargin(index));
        } else {
            cx = width;
            cyChild = height;
            cxIdeal = Math.max(0, width - ((SwtCoolBar) parent.getImpl()).getMargin(index));
        }
        REBARBANDINFO rbBand = new REBARBANDINFO();
        rbBand.cbSize = REBARBANDINFO.sizeof;
        /* Get the child size fields first so we don't overwrite them. */
        rbBand.fMask = OS.RBBIM_CHILDSIZE | OS.RBBIM_IDEALSIZE;
        OS.SendMessage(hwnd, OS.RB_GETBANDINFO, index, rbBand);
        /* Set the size fields we are currently modifying. */
        if (!ideal)
            rbBand.cxIdeal = cxIdeal;
        if (!minimum)
            rbBand.cyMinChild = cyChild;
        rbBand.cyChild = cyChild;
        /*
	* Do not set the size for the last item on the row.
	*/
        if (!((SwtCoolBar) parent.getImpl()).isLastItemOfRow(index)) {
            MARGINS margins = new MARGINS();
            OS.SendMessage(hwnd, OS.RB_GETBANDMARGINS, 0, margins);
            cx -= margins.cxLeftWidth + margins.cxRightWidth;
            int separator = (parent.style & SWT.FLAT) == 0 ? SwtCoolBar.SEPARATOR_WIDTH : 0;
            rbBand.cx = cx - separator;
            rbBand.fMask |= OS.RBBIM_SIZE;
        }
        OS.SendMessage(hwnd, OS.RB_SETBANDINFO, index, rbBand);
    }

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
    public void setSize(Point size) {
        checkWidget();
        if (size == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        size = DPIUtil.scaleUp(size, getZoom());
        setSizeInPixels(size.x, size.y);
    }

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
    public Point getMinimumSize() {
        checkWidget();
        return DPIUtil.scaleDown(getMinimumSizeInPixels(), getZoom());
    }

    Point getMinimumSizeInPixels() {
        int index = parent.indexOf(this.getApi());
        if (index == -1)
            return new Point(0, 0);
        long hwnd = parent.handle;
        REBARBANDINFO rbBand = new REBARBANDINFO();
        rbBand.cbSize = REBARBANDINFO.sizeof;
        rbBand.fMask = OS.RBBIM_CHILDSIZE;
        OS.SendMessage(hwnd, OS.RB_GETBANDINFO, index, rbBand);
        if ((parent.style & SWT.VERTICAL) != 0) {
            return new Point(rbBand.cyMinChild, rbBand.cxMinChild);
        }
        return new Point(rbBand.cxMinChild, rbBand.cyMinChild);
    }

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
    public void setMinimumSize(int width, int height) {
        checkWidget();
        int zoom = getZoom();
        setMinimumSizeInPixels(DPIUtil.scaleUp(width, zoom), DPIUtil.scaleUp(height, zoom));
    }

    void setMinimumSizeInPixels(int width, int height) {
        int index = parent.indexOf(this.getApi());
        if (index == -1)
            return;
        width = Math.max(0, width);
        height = Math.max(0, height);
        minimum = true;
        long hwnd = parent.handle;
        int cxMinChild, cyMinChild;
        if ((parent.style & SWT.VERTICAL) != 0) {
            cxMinChild = height;
            cyMinChild = width;
        } else {
            cxMinChild = width;
            cyMinChild = height;
        }
        REBARBANDINFO rbBand = new REBARBANDINFO();
        rbBand.cbSize = REBARBANDINFO.sizeof;
        /* Get the child size fields first so we don't overwrite them. */
        rbBand.fMask = OS.RBBIM_CHILDSIZE;
        OS.SendMessage(hwnd, OS.RB_GETBANDINFO, index, rbBand);
        /* Set the size fields we are currently modifying. */
        rbBand.cxMinChild = cxMinChild;
        rbBand.cyMinChild = cyMinChild;
        OS.SendMessage(hwnd, OS.RB_SETBANDINFO, index, rbBand);
    }

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
    public void setMinimumSize(Point size) {
        checkWidget();
        if (size == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        size = DPIUtil.scaleUp(size, getZoom());
        setMinimumSizeInPixels(size.x, size.y);
    }

    boolean getWrap() {
        int index = parent.indexOf(this.getApi());
        long hwnd = parent.handle;
        REBARBANDINFO rbBand = new REBARBANDINFO();
        rbBand.cbSize = REBARBANDINFO.sizeof;
        rbBand.fMask = OS.RBBIM_STYLE;
        OS.SendMessage(hwnd, OS.RB_GETBANDINFO, index, rbBand);
        return (rbBand.fStyle & OS.RBBS_BREAK) != 0;
    }

    void setWrap(boolean wrap) {
        int index = parent.indexOf(this.getApi());
        long hwnd = parent.handle;
        REBARBANDINFO rbBand = new REBARBANDINFO();
        rbBand.cbSize = REBARBANDINFO.sizeof;
        rbBand.fMask = OS.RBBIM_STYLE;
        OS.SendMessage(hwnd, OS.RB_GETBANDINFO, index, rbBand);
        if (wrap) {
            rbBand.fStyle |= OS.RBBS_BREAK;
        } else {
            rbBand.fStyle &= ~OS.RBBS_BREAK;
        }
        OS.SendMessage(hwnd, OS.RB_SETBANDINFO, index, rbBand);
    }

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
    public void removeSelectionListener(SelectionListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Selection, listener);
        eventTable.unhook(SWT.DefaultSelection, listener);
    }

    public CoolItem getApi() {
        if (api == null)
            api = CoolItem.createApi(this);
        return (CoolItem) api;
    }
}
