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

    long clientHandle, boxHandle, labelHandle, imageHandle;

    int width, height;

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
        super(parent, style, api);
        this.parent = parent;
        createWidget(parent.getItemCount());
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
        createWidget(index);
    }

    @Override
    public void checkSubclass() {
        if (!isValidSubclass())
            error(SWT.ERROR_INVALID_SUBCLASS);
    }

    @Override
    void createHandle(int index) {
    }

    @Override
    void createWidget(int index) {
        super.createWidget(index);
        showWidget(index);
        ((DartExpandBar) parent.getImpl()).createItem(this.getApi(), getApi().style, index);
    }

    @Override
    void deregister() {
        super.deregister();
        ((SwtDisplay) display.getImpl()).removeWidget(clientHandle);
        ((SwtDisplay) display.getImpl()).removeWidget(boxHandle);
        ((SwtDisplay) display.getImpl()).removeWidget(labelHandle);
        ((SwtDisplay) display.getImpl()).removeWidget(imageHandle);
    }

    @Override
    public void release(boolean destroy) {
        //454940 ExpandBar DND fix.
        //Since controls are now nested under the Item,
        //Item is responsible for it's release.
        if (control != null && !control.isDisposed()) {
            control.getImpl().release(destroy);
        }
        super.release(destroy);
    }

    @Override
    void destroyWidget() {
        ((DartExpandBar) parent.getImpl()).destroyItem(this.getApi());
        super.destroyWidget();
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
        return this.expanded;
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
        return DPIUtil.autoScaleDown(getHeaderHeightInPixels());
    }

    int getHeaderHeightInPixels() {
        checkWidget();
        return 0;
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
        return DPIUtil.autoScaleDown(height);
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

    boolean hasFocus() {
        return false;
    }

    @Override
    void hookEvents() {
        super.hookEvents();
    }

    @Override
    void register() {
        super.register();
        ((SwtDisplay) display.getImpl()).addWidget(clientHandle, this.getApi());
        ((SwtDisplay) display.getImpl()).addWidget(boxHandle, this.getApi());
        ((SwtDisplay) display.getImpl()).addWidget(labelHandle, this.getApi());
        ((SwtDisplay) display.getImpl()).addWidget(imageHandle, this.getApi());
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
        clientHandle = boxHandle = labelHandle = imageHandle = 0;
        parent = null;
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        if (((DartExpandBar) parent.getImpl()).lastFocus == this.getApi())
            ((DartExpandBar) parent.getImpl()).lastFocus = null;
        control = null;
    }

    void resizeControl() {
        if (control != null && !control.isDisposed()) {
            //454940 change in hierarchy
            /*
		* Feature in GTK. When the ExpandBar is resize too small the control
		* shows up on top of the vertical scrollbar. This happen because the
		* GtkExpander does not set the size of child smaller than the request
		* size of its parent and because the control is not parented in the
		* hierarchy of the GtkScrolledWindow.
		* The fix is calculate the width ourselves when the scrollbar is visible.
		*/
            ScrollBar vBar = ((DartScrollable) parent.getImpl()).verticalBar;
            if (vBar != null) {
            }
            // Bug 479242: Bound calculation is correct without needing to use yScroll in GTK3
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
        if (this.control == control)
            return;
        this.control = control;
        if (control != null) {
            //454940 ExpandBar DND fix.
            //Reparenting on the GTK side.
            //Proper hierachy on gtk side is required for DND to function properly.
            //As ExpandItem's child can be created before the ExpandItem, our only
            //option is to reparent the child upon the setControl(..) call.
        }
        ((DartExpandBar) parent.getImpl()).layoutItems();
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
        ((DartExpandBar) parent.getImpl()).layoutItems();
        this.expanded = expanded;
    }

    boolean setFocus() {
        // widget could be disposed at this point
        if (isDisposed())
            return false;
        return false;
    }

    void setFontDescription(long font) {
        setFontDescription(getApi().handle, font);
        if (labelHandle != 0)
            setFontDescription(labelHandle, font);
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
        setHeightInPixels(DPIUtil.autoScaleUp(height));
    }

    void setHeightInPixels(int height) {
        dirty();
        checkWidget();
        if (height < 0)
            return;
        this.height = height;
        ((DartExpandBar) parent.getImpl()).layoutItems();
    }

    @Override
    public void setImage(Image image) {
        dirty();
        super.setImage(image);
        if (image != null) {
            if (image.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
        } else {
        }
    }

    @Override
    void setOrientation(boolean create) {
        super.setOrientation(create);
        if ((parent.style & SWT.RIGHT_TO_LEFT) != 0 || !create) {
        }
    }

    @Override
    public void setText(String string) {
        dirty();
        super.setText(string);
    }

    void showWidget(int index) {
    }

    @Override
    long dpiChanged(long object, long arg0) {
        super.dpiChanged(object, arg0);
        if (image != null) {
            setImage(image);
        }
        return 0;
    }

    boolean expanded;

    public ExpandBar _parent() {
        return parent;
    }

    public Control _control() {
        return control;
    }

    public long _clientHandle() {
        return clientHandle;
    }

    public long _boxHandle() {
        return boxHandle;
    }

    public long _labelHandle() {
        return labelHandle;
    }

    public long _imageHandle() {
        return imageHandle;
    }

    public int _width() {
        return width;
    }

    public int _height() {
        return height;
    }

    public boolean _expanded() {
        return expanded;
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
