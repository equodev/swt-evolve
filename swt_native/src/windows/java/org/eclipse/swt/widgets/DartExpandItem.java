/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2013 IBM Corporation and others.
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
import org.eclipse.swt.internal.*;
import dev.equo.swt.*;

/**
 * Instances of this class represent a selectable user interface object
 * that represents a expandable item in a expand bar.
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
 * @see ExpandBar
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.2
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartExpandItem extends DartItem implements IExpandItem {

    ExpandBar parent;

    Control control;

    boolean expanded, hover;

    int x, y, width, height;

    int imageHeight, imageWidth;

    private static final int IMAGE_MARGIN = 8;

    static final int TEXT_INSET = 6;

    static final int BORDER = 1;

    static final int CHEVRON_SIZE = 24;

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
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartExpandItem(ExpandBar parent, int style, ExpandItem api) {
        this(parent, style, checkNull(parent).getItemCount(), api);
    }

    /**
     * Constructs a new instance of this class given its parent, a
     * style value describing its behavior and appearance, and the index
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
     * @param index the zero-relative index to store the receiver in its parent
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
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartExpandItem(ExpandBar parent, int style, int index, ExpandItem api) {
        super(parent, style, api);
        this.parent = parent;
        ((DartExpandBar) parent.getImpl()).createItem(this.getApi(), style, index);
    }

    static ExpandBar checkNull(ExpandBar control) {
        if (control == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        return control;
    }

    @Override
    void destroyWidget() {
        ((DartExpandBar) parent.getImpl()).destroyItem(this.getApi());
        releaseHandle();
    }

    /**
     * Returns the control that is shown when the item is expanded.
     * If no control has been set, return <code>null</code>.
     *
     * @return the control
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
     * Returns <code>true</code> if the receiver is expanded,
     * and false otherwise.
     *
     * @return the expanded state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean getExpanded() {
        checkWidget();
        return expanded;
    }

    /**
     * Returns the height of the receiver's header
     *
     * @return the height of the header
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getHeaderHeight() {
        checkWidget();
        return DPIUtil.pixelToPoint(getHeaderHeightInPixels(), getZoom());
    }

    int getHeaderHeightInPixels() {
        int headerHeightInPixels = ((DartExpandBar) parent.getImpl()).getBandHeight();
        return headerHeightInPixels;
    }

    /**
     * Gets the height of the receiver.
     *
     * @return the height
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getHeight() {
        checkWidget();
        return DPIUtil.pixelToPoint(getHeightInPixels(), getZoom());
    }

    int getHeightInPixels() {
        return height;
    }

    /**
     * Returns the receiver's parent, which must be a <code>ExpandBar</code>.
     *
     * @return the receiver's parent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public ExpandBar getParent() {
        checkWidget();
        return parent;
    }

    int getPreferredWidth(long hTheme, long hDC) {
        int width = DartExpandItem.TEXT_INSET * 2 + DartExpandItem.CHEVRON_SIZE;
        if (image != null) {
            width += DartExpandItem.TEXT_INSET + imageWidth;
        }
        if (text.length() > 0) {
            if (hTheme != 0) {
            } else {
            }
        }
        return width;
    }

    boolean isHover(int x, int y) {
        int bandHeight = ((DartExpandBar) parent.getImpl()).getBandHeight();
        return this.x < x && x < (this.x + width) && this.y < y && y < (this.y + bandHeight);
    }

    void redraw(boolean all) {
        int headerHeightInPixels = getHeaderHeightInPixels();
        if (!((DartExpandBar) parent.getImpl()).isAppThemed()) {
        }
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
        parent = null;
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        control = null;
    }

    void setBoundsInPixels(int x, int y, int width, int height, boolean move, boolean size) {
        redraw(true);
        int headerHeightInPixels = getHeaderHeightInPixels();
        if (move) {
            this.x = x;
            this.y = y;
            redraw(true);
        }
        if (size) {
            this.width = width;
            this.height = height;
            redraw(true);
        }
        if (control != null && !control.isDisposed()) {
            if (!((DartExpandBar) parent.getImpl()).isAppThemed()) {
                x += BORDER;
                width = Math.max(0, width - BORDER * 2);
                height = Math.max(0, height - BORDER);
            }
            if (move && size)
                ((DartControl) control.getImpl()).setBoundsInPixels(x, y + headerHeightInPixels, width, height);
            if (move && !size)
                ((DartControl) control.getImpl()).setLocationInPixels(x, y + headerHeightInPixels);
            if (!move && size)
                ((DartControl) control.getImpl()).setSizeInPixels(width, height);
        }
    }

    /**
     * Sets the control that is shown when the item is expanded.
     *
     * @param control the new control (or null)
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
        dirty();
        checkWidget();
        if (control != null) {
            if (control.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
            if (((DartControl) control.getImpl()).parent != parent)
                error(SWT.ERROR_INVALID_PARENT);
        }
        this.control = control;
        if (control != null) {
            int headerHeight = ((DartExpandBar) parent.getImpl()).getBandHeight();
            control.setVisible(expanded);
            if (!((DartExpandBar) parent.getImpl()).isAppThemed()) {
                Rectangle controlBounds = control.getBounds();
                int width = (controlBounds != null && controlBounds.width > 0) ? controlBounds.width : Math.max(0, this.width - BORDER * 2);
                int height = (controlBounds != null && controlBounds.height > 0) ? controlBounds.height : Math.max(0, this.height - BORDER);
                ((DartControl) control.getImpl()).setBoundsInPixels(x + BORDER, y + headerHeight, width, height);
            } else {
                Rectangle controlBounds = control.getBounds();
                int width = (controlBounds != null && controlBounds.width > 0) ? controlBounds.width : this.width;
                int height = (controlBounds != null && controlBounds.height > 0) ? controlBounds.height : this.height;
                ((DartControl) control.getImpl()).setBoundsInPixels(x, y + headerHeight, width, height);
            }
        }
    }

    /**
     * Sets the expanded state of the receiver.
     *
     * @param expanded the new expanded state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setExpanded(boolean expanded) {
        dirty();
        checkWidget();
        this.expanded = expanded;
        ((DartExpandBar) parent.getImpl()).showItem(this.getApi());
    }

    /**
     * Sets the height of the receiver. This is height of the item when it is expanded,
     * excluding the height of the header.
     *
     * @param height the new height
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setHeight(int height) {
        checkWidget();
    }

    void setHeightInPixels(int height) {
        dirty();
        if (height < 0)
            return;
        setBoundsInPixels(0, 0, width, height, false, true);
        if (expanded)
            ((DartExpandBar) parent.getImpl()).layoutItems(parent.indexOf(this.getApi()) + 1, true);
    }

    @Override
    public void setImage(Image image) {
        dirty();
        super.setImage(image);
        int oldImageHeight = imageHeight;
        if (image != null) {
            Rectangle bounds = image.getBounds();
            imageHeight = bounds.height;
            imageWidth = bounds.width;
        } else {
            imageHeight = imageWidth = 0;
        }
        if (oldImageHeight != imageHeight) {
            ((DartExpandBar) parent.getImpl()).layoutItems(parent.indexOf(this.getApi()), true);
        } else {
            redraw(true);
        }
    }

    @Override
    public void setText(String string) {
        dirty();
        super.setText(string);
        if ((getApi().state & HAS_AUTO_DIRECTION) != 0) {
            updateTextDirection(AUTO_TEXT_DIRECTION);
        }
        redraw(true);
    }

    private static void handleDPIChange(Widget widget, int newZoom, float scalingFactor) {
        if (!(widget instanceof ExpandItem item)) {
            return;
        }
        if (((DartExpandItem) item.getImpl()).height != 0 || ((DartExpandItem) item.getImpl()).width != 0) {
            int newWidth = Math.round(((DartExpandItem) item.getImpl()).width * scalingFactor);
            int newHeight = Math.round(((DartExpandItem) item.getImpl()).height * scalingFactor);
            ((DartExpandItem) item.getImpl()).setBoundsInPixels(((DartExpandItem) item.getImpl()).x, ((DartExpandItem) item.getImpl()).y, newWidth, newHeight, true, true);
        }
    }

    public ExpandBar _parent() {
        return parent;
    }

    public Control _control() {
        return control;
    }

    public boolean _expanded() {
        return expanded;
    }

    public boolean _hover() {
        return hover;
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

    public int _imageHeight() {
        return imageHeight;
    }

    public int _imageWidth() {
        return imageWidth;
    }

    public FlutterBridge getBridge() {
        if (bridge != null)
            return bridge;
        Composite p = parent;
        while (p != null && !(p.getImpl() instanceof DartWidget)) p = p.getImpl()._parent();
        return p != null ? ((DartWidget) p.getImpl()).getBridge() : null;
    }

    protected void _hookEvents() {
        super._hookEvents();
    }

    public ExpandItem getApi() {
        if (api == null)
            api = ExpandItem.createApi(this);
        return (ExpandItem) api;
    }

    public VExpandItem getValue() {
        if (value == null)
            value = new VExpandItem(this);
        return (VExpandItem) value;
    }
}
