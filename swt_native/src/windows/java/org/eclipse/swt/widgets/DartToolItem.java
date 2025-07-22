/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2021 IBM Corporation and others.
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
import dev.equo.swt.*;

/**
 * Instances of this class represent a selectable user interface object
 * that represents a button in a tool bar.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>PUSH, CHECK, RADIO, SEPARATOR, DROP_DOWN</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles CHECK, PUSH, RADIO, SEPARATOR and DROP_DOWN
 * may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#toolbar">ToolBar, ToolItem snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartToolItem extends DartItem implements IToolItem {

    ToolBar parent;

    Control control;

    String toolTipText;

    Image disabledImage, hotImage;

    Image disabledImage2;

    int id;

    short cx;

    int foreground = -1, background = -1;

    /**
     * Constructs a new instance of this class given its parent
     * (which must be a <code>ToolBar</code>) and a style value
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
     * @see SWT#PUSH
     * @see SWT#CHECK
     * @see SWT#RADIO
     * @see SWT#SEPARATOR
     * @see SWT#DROP_DOWN
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartToolItem(ToolBar parent, int style, ToolItem api) {
        super(parent, checkStyle(style), api);
        this.parent = parent;
        ((DartToolBar) parent.getImpl()).createItem(this.getApi(), parent.getItemCount());
    }

    /**
     * Constructs a new instance of this class given its parent
     * (which must be a <code>ToolBar</code>), a style value
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
     * @see SWT#PUSH
     * @see SWT#CHECK
     * @see SWT#RADIO
     * @see SWT#SEPARATOR
     * @see SWT#DROP_DOWN
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartToolItem(ToolBar parent, int style, int index, ToolItem api) {
        super(parent, checkStyle(style), api);
        this.parent = parent;
        ((DartToolBar) parent.getImpl()).createItem(this.getApi(), index);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the control is selected by the user, by sending
     * it one of the messages defined in the <code>SelectionListener</code>
     * interface.
     * <p>
     * When <code>widgetSelected</code> is called when the mouse is over the arrow portion of a drop-down tool,
     * the event object detail field contains the value <code>SWT.ARROW</code>.
     * <code>widgetDefaultSelected</code> is not called.
     * </p>
     * <p>
     * When the <code>SWT.RADIO</code> style bit is set, the <code>widgetSelected</code> method is
     * also called when the receiver loses selection because another item in the same radio group
     * was selected by the user. During <code>widgetSelected</code> the application can use
     * <code>getSelection()</code> to determine the current selected state of the receiver.
     * </p>
     *
     * @param listener the listener which should be notified when the control is selected by the user,
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
     */
    public void addSelectionListener(SelectionListener listener) {
        addTypedListener(listener, SWT.Selection, SWT.DefaultSelection);
    }

    static int checkStyle(int style) {
        return checkBits(style, SWT.PUSH, SWT.CHECK, SWT.RADIO, SWT.SEPARATOR, SWT.DROP_DOWN, 0);
    }

    @Override
    public void checkSubclass() {
        if (!isValidSubclass())
            error(SWT.ERROR_INVALID_SUBCLASS);
    }

    void click(boolean dropDown) {
        long hwnd = parent.handle;
        ((DartToolBar) parent.getImpl()).ignoreMouse = true;
        ((DartToolBar) parent.getImpl()).ignoreMouse = false;
    }

    public Widget[] computeTabList() {
        if (isTabGroup()) {
            if (getEnabled()) {
                if ((getApi().style & SWT.SEPARATOR) != 0) {
                    if (control != null)
                        return control.getImpl().computeTabList();
                } else {
                    return new Widget[] { this.getApi() };
                }
            }
        }
        return new Widget[0];
    }

    @Override
    void destroyWidget() {
        ((DartToolBar) parent.getImpl()).destroyItem(this.getApi());
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
        return null;
    }

    /**
     * Returns the control that is used to fill the bounds of
     * the item when the item is a <code>SEPARATOR</code>.
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
     * Returns the receiver's disabled image if it has one, or null
     * if it does not.
     * <p>
     * The disabled image is displayed when the receiver is disabled.
     * </p>
     *
     * @return the receiver's disabled image
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Image getDisabledImage() {
        checkWidget();
        return disabledImage;
    }

    /**
     * Returns the receiver's background color.
     * <p>
     * Note: This operation is a hint and may be overridden by the platform.
     * For example, on some versions of Windows the background of a TabFolder,
     * is a gradient rather than a solid color.
     * </p>
     * @return the background color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.120
     */
    public Color getBackground() {
        checkWidget();
        return SwtColor.win32_new(display, ((DartToolBar) parent.getImpl()).getBackgroundPixel(this.getApi()));
    }

    /**
     * Returns <code>true</code> if the receiver is enabled, and
     * <code>false</code> otherwise. A disabled control is typically
     * not selectable from the user interface and draws with an
     * inactive or "grayed" look.
     *
     * @return the receiver's enabled state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #isEnabled
     */
    public boolean getEnabled() {
        checkWidget();
        if ((getApi().style & SWT.SEPARATOR) != 0) {
            return (getApi().state & DISABLED) == 0;
        }
        return this.enabled;
    }

    /**
     * Returns the foreground color that the receiver will use to draw.
     *
     * @return the receiver's foreground color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.120
     */
    public Color getForeground() {
        checkWidget();
        return SwtColor.win32_new(display, ((DartToolBar) parent.getImpl()).getForegroundPixel(this.getApi()));
    }

    /**
     * Returns the receiver's hot image if it has one, or null
     * if it does not.
     * <p>
     * The hot image is displayed when the mouse enters the receiver.
     * </p>
     *
     * @return the receiver's hot image
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Image getHotImage() {
        checkWidget();
        return hotImage;
    }

    /**
     * Returns the receiver's enabled image if it has one, or null
     * if it does not.
     *
     * @return the receiver's enabled image
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    @Override
    public Image getImage() {
        return super.getImage();
    }

    /**
     * Returns the receiver's parent, which must be a <code>ToolBar</code>.
     *
     * @return the receiver's parent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public ToolBar getParent() {
        checkWidget();
        return parent;
    }

    /**
     * Returns <code>true</code> if the receiver is selected,
     * and false otherwise.
     * <p>
     * When the receiver is of type <code>CHECK</code> or <code>RADIO</code>,
     * it is selected when it is checked (which some platforms draw as a
     * pushed in button). If the receiver is of any other type, this method
     * returns false.
     * </p>
     *
     * @return the selection state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean getSelection() {
        checkWidget();
        if ((getApi().style & (SWT.CHECK | SWT.RADIO)) == 0)
            return false;
        return this.selection;
    }

    /**
     * Returns the receiver's tool tip text, or null if it has not been set.
     *
     * @return the receiver's tool tip text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public String getToolTipText() {
        checkWidget();
        return toolTipText;
    }

    /**
     * Gets the width of the receiver.
     *
     * @return the width
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getWidth() {
        checkWidget();
        return DPIUtil.scaleDown(getWidthInPixels(), getZoom());
    }

    int getWidthInPixels() {
        return this.width;
    }

    /**
     * Returns <code>true</code> if the receiver is enabled and all
     * of the receiver's ancestors are enabled, and <code>false</code>
     * otherwise. A disabled control is typically not selectable from the
     * user interface and draws with an inactive or "grayed" look.
     *
     * @return the receiver's enabled state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #getEnabled
     */
    public boolean isEnabled() {
        checkWidget();
        return getEnabled() && parent.isEnabled();
    }

    public boolean isTabGroup() {
        ToolItem[] tabList = ((DartToolBar) parent.getImpl())._getTabItemList();
        if (tabList != null) {
            for (ToolItem item : tabList) {
                if (item == this.getApi())
                    return true;
            }
        }
        if ((getApi().style & SWT.SEPARATOR) != 0)
            return true;
        int index = parent.indexOf(this.getApi());
        if (index == 0)
            return true;
        ToolItem previous = parent.getItem(index - 1);
        return (previous.getStyle() & SWT.SEPARATOR) != 0;
    }

    void redraw() {
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        releaseImages();
        control = null;
        toolTipText = null;
        disabledImage = hotImage = null;
        if (disabledImage2 != null)
            disabledImage2.dispose();
        disabledImage2 = null;
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
        parent = null;
        id = -1;
    }

    void releaseImages() {
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the control is selected by the user.
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

    void resizeControl() {
        if (control != null && !control.isDisposed()) {
            /*
		* Set the size and location of the control
		* separately to minimize flashing in the
		* case where the control does not resize
		* to the size that was requested.  This
		* case can occur when the control is a
		* combo box.
		*/
            Rectangle itemRect = getBounds();
            control.setSize(itemRect.width, itemRect.height);
            Rectangle rect = control.getBounds();
            rect.x = itemRect.x + (itemRect.width - rect.width) / 2;
            rect.y = itemRect.y + (itemRect.height - rect.height) / 2;
            control.setLocation(rect.x, rect.y);
        }
    }

    void selectRadio() {
        int index = 0;
        ToolItem[] items = parent.getItems();
        while (index < items.length && items[index] != this.getApi()) index++;
        int i = index - 1;
        while (i >= 0 && ((DartToolItem) items[i].getImpl()).setRadioSelection(false)) --i;
        int j = index + 1;
        while (j < items.length && ((DartToolItem) items[j].getImpl()).setRadioSelection(false)) j++;
        setSelection(true);
    }

    /**
     * Sets the receiver's background color to the color specified
     * by the argument, or to the default system color for the control
     * if the argument is null.
     * <p>
     * Note: This operation is a hint and may be overridden by the platform.
     * </p>
     * @param color the new color (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.120
     */
    public void setBackground(Color color) {
        checkWidget();
        this._background = color;
        if (color != null && color.isDisposed()) {
            error(SWT.ERROR_INVALID_ARGUMENT);
        }
        parent.state |= CUSTOM_DRAW_ITEM;
        int pixel = (color != null) ? color.handle : -1;
        if (pixel == background)
            return;
        background = pixel;
        redraw();
        getBridge().dirty(this);
    }

    /**
     * Sets the control that is used to fill the bounds of
     * the item when the item is a <code>SEPARATOR</code>.
     *
     * @param control the new control
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
            if (((DartControl) control.getImpl()).parent != parent)
                error(SWT.ERROR_INVALID_PARENT);
        }
        if ((getApi().style & SWT.SEPARATOR) == 0)
            return;
        parent.layout(true);
        this.control = control;
        /*
	* Feature in Windows.  When a tool bar wraps, tool items
	* with the style BTNS_SEP are used as wrap points.  This
	* means that controls that are placed on top of separator
	* items are not positioned properly.  Also, vertical tool
	* bars are implemented using TB_SETROWS to set the number
	* of rows.  When a control is placed on top of a separator,
	* the height of the separator does not grow.  The fix in
	* both cases is to change the tool item style from BTNS_SEP
	* to BTNS_BUTTON, causing the item to wrap like a tool item
	* button.  The new tool item button is disabled to avoid key
	* traversal and the image is set to I_IMAGENONE to avoid
	* getting the first image from the image list.
	*/
        if ((parent.style & (SWT.WRAP | SWT.VERTICAL)) != 0) {
            boolean changed = false;
            long hwnd = parent.handle;
            if (control == null) {
            } else {
            }
            if (changed) {
            }
        }
        resizeControl();
        getBridge().dirty(this);
    }

    /**
     * Enables the receiver if the argument is <code>true</code>,
     * and disables it otherwise.
     * <p>
     * A disabled control is typically
     * not selectable from the user interface and draws with an
     * inactive or "grayed" look.
     * </p>
     *
     * @param enabled the new enabled state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setEnabled(boolean enabled) {
        checkWidget();
        this.enabled = enabled;
        long hwnd = parent.handle;
        if (enabled) {
            getApi().state &= ~DISABLED;
        } else {
            getApi().state |= DISABLED;
        }
        if ((getApi().style & SWT.SEPARATOR) == 0) {
            if (image != null)
                updateImages(enabled && parent.getEnabled());
        }
        if (!enabled && ((DartToolBar) parent.getImpl()).lastFocusId == id) {
            ((DartToolBar) parent.getImpl()).lastFocusId = -1;
        }
        getBridge().dirty(this);
    }

    /**
     * Sets the receiver's disabled image to the argument, which may be
     * null indicating that no disabled image should be displayed.
     * <p>
     * The disabled image is displayed when the receiver is disabled.
     * </p>
     *
     * @param image the disabled image to display on the receiver (may be null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setDisabledImage(Image image) {
        checkWidget();
        if (this.disabledImage == image)
            return;
        if ((getApi().style & SWT.SEPARATOR) != 0)
            return;
        if (image != null && image.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        parent.layout(isImageSizeChanged(disabledImage, image));
        disabledImage = image;
        updateImages(getEnabled() && parent.getEnabled());
        getBridge().dirty(this);
    }

    /**
     * Sets the receiver's foreground color to the color specified
     * by the argument, or to the default system color for the control
     * if the argument is null.
     * <p>
     * Note: This operation is a hint and may be overridden by the platform.
     * </p>
     * @param color the new color (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.120
     */
    public void setForeground(Color color) {
        checkWidget();
        this._foreground = color;
        if (color != null && color.isDisposed()) {
            error(SWT.ERROR_INVALID_ARGUMENT);
        }
        parent.state |= CUSTOM_DRAW_ITEM;
        int pixel = (color != null) ? color.handle : -1;
        if (pixel == foreground)
            return;
        foreground = pixel;
        redraw();
        getBridge().dirty(this);
    }

    /**
     * Sets the receiver's hot image to the argument, which may be
     * null indicating that no hot image should be displayed.
     * <p>
     * The hot image is displayed when the mouse enters the receiver.
     * </p>
     *
     * @param image the hot image to display on the receiver (may be null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setHotImage(Image image) {
        checkWidget();
        if (this.hotImage == image)
            return;
        if ((getApi().style & SWT.SEPARATOR) != 0)
            return;
        if (image != null && image.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        parent.layout(isImageSizeChanged(hotImage, image));
        hotImage = image;
        updateImages(getEnabled() && parent.getEnabled());
        getBridge().dirty(this);
    }

    @Override
    public void setImage(Image image) {
        checkWidget();
        if (this.image == image)
            return;
        if ((getApi().style & SWT.SEPARATOR) != 0)
            return;
        if (image != null && image.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        parent.layout(isImageSizeChanged(super.image, image));
        super.setImage(image);
        updateImages(getEnabled() && parent.getEnabled());
        getBridge().dirty(this);
    }

    boolean isImageSizeChanged(Image oldImage, Image image) {
        boolean changed = true;
        // check if image size really changed for old and new images
        if (oldImage != null && !oldImage.isDisposed() && image != null && !image.isDisposed()) {
            changed = !oldImage.getBounds().equals(image.getBounds());
        }
        return changed;
    }

    boolean setRadioSelection(boolean value) {
        if ((getApi().style & SWT.RADIO) == 0)
            return false;
        if (getSelection() != value) {
            setSelection(value);
            sendSelectionEvent(SWT.Selection);
        }
        return true;
    }

    /**
     * Sets the selection state of the receiver.
     * <p>
     * When the receiver is of type <code>CHECK</code> or <code>RADIO</code>,
     * it is selected when it is checked (which some platforms draw as a
     * pushed in button).
     * </p>
     *
     * @param selected the new selection state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setSelection(boolean selected) {
        checkWidget();
        this.selection = selected;
        if ((getApi().style & (SWT.CHECK | SWT.RADIO)) == 0)
            return;
        long hwnd = parent.handle;
        if (selected) {
        } else {
        }
        /*
	* Bug in Windows.  When a tool item with the style
	* BTNS_CHECK or BTNS_CHECKGROUP is selected and then
	* disabled, the item does not draw using the disabled
	* image.  The fix is to use the disabled image in all
	* image lists for the item.
	*
	* NOTE: This means that the image list must be updated
	* when the selection changes in a disabled tool item.
	*/
        if ((getApi().style & (SWT.CHECK | SWT.RADIO)) != 0) {
            if (!getEnabled() || !parent.getEnabled()) {
                updateImages(false);
            }
        }
        getBridge().dirty(this);
    }

    @Override
    public boolean setTabItemFocus() {
        if (parent.getImpl().setTabItemFocus()) {
            long hwnd = parent.handle;
            return true;
        }
        return false;
    }

    void _setText(String string) {
        long hwnd = parent.handle;
        long pszText = 0;
        if (string.length() != 0) {
            if ((getApi().style & SWT.FLIP_TEXT_DIRECTION) != 0) {
            } else {
            }
        }
    }

    /**
     * Sets the receiver's text. The string may include
     * the mnemonic character.
     * <p>
     * Mnemonics are indicated by an '&amp;' that causes the next
     * character to be the mnemonic.  When the user presses a
     * key sequence that matches the mnemonic, a selection
     * event occurs. On most platforms, the mnemonic appears
     * underlined but may be emphasised in a platform specific
     * manner.  The mnemonic indicator character '&amp;' can be
     * escaped by doubling it in the string, causing a single
     * '&amp;' to be displayed.
     * </p><p>
     * Note: If control characters like '\n', '\t' etc. are used
     * in the string, then the behavior is platform dependent.
     * </p>
     *
     * @param string the new text
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the text is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    @Override
    public void setText(String string) {
        checkWidget();
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if ((getApi().style & SWT.SEPARATOR) != 0)
            return;
        if (string.equals(text))
            return;
        parent.layout(true);
        super.setText(string);
        if ((getApi().state & HAS_AUTO_DIRECTION) == 0 || !updateTextDirection(AUTO_TEXT_DIRECTION)) {
            _setText(string);
        }
        /*
	* Bug in Windows.  For some reason, when the font is set
	* before any tool item has text, the tool items resize to
	* a very small size.  Also, a tool item will only show text
	* when text has already been set on one item and then a new
	* item is created.  The fix is to use WM_SETFONT to force
	* the tool bar to redraw and layout.
	*/
        ((DartToolBar) parent.getImpl()).setDropDownItems(false);
        long hwnd = parent.handle;
        ((DartToolBar) parent.getImpl()).setDropDownItems(true);
        ((DartToolBar) parent.getImpl()).layoutItems();
        getBridge().dirty(this);
    }

    @Override
    boolean updateTextDirection(int textDirection) {
        /* AUTO is handled by super */
        if (super.updateTextDirection(textDirection) && text.length() != 0) {
            _setText(text);
            return true;
        }
        return false;
    }

    /**
     * Sets the receiver's tool tip text to the argument, which
     * may be null indicating that the default tool tip for the
     * control will be shown. For a control that has a default
     * tool tip, such as the Tree control on Windows, setting
     * the tool tip text to an empty string replaces the default,
     * causing no tool tip text to be shown.
     * <p>
     * The mnemonic indicator (character '&amp;') is not displayed in a tool tip.
     * To display a single '&amp;' in the tool tip, the character '&amp;' can be
     * escaped by doubling it in the string.
     * </p>
     * <p>
     * NOTE: This operation is a hint and behavior is platform specific, on Windows
     * for CJK-style mnemonics of the form " (&amp;C)" at the end of the tooltip text
     * are not shown in tooltip.
     * </p>
     *
     * @param string the new tool tip text (or null)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setToolTipText(String string) {
        checkWidget();
        toolTipText = string;
        getBridge().dirty(this);
    }

    /**
     * Sets the width of the receiver, for <code>SEPARATOR</code> ToolItems.
     *
     * @param width the new width. If the new value is <code>SWT.DEFAULT</code>,
     * the width is a fixed-width area whose amount is determined by the platform.
     * If the new value is 0 a vertical or horizontal line will be drawn, depending
     * on the setting of the corresponding style bit (<code>SWT.VERTICAL</code> or
     * <code>SWT.HORIZONTAL</code>). If the new value is <code>SWT.SEPARATOR_FILL</code>
     * a variable-width space is inserted that acts as a spring between the two adjoining
     * items which will push them out to the extent of the containing ToolBar.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setWidth(int width) {
        checkWidget();
        this.width = width;
        setWidthInPixels(DPIUtil.scaleUp(width, getZoom()));
        getBridge().dirty(this);
    }

    void setWidthInPixels(int width) {
        if ((getApi().style & SWT.SEPARATOR) == 0)
            return;
        if (width < 0)
            return;
        long hwnd = parent.handle;
        ((DartToolBar) parent.getImpl()).layoutItems();
    }

    void updateImages(boolean enabled) {
        if ((getApi().style & SWT.SEPARATOR) != 0)
            return;
        long hwnd = parent.handle;
        ((DartToolBar) parent.getImpl()).layoutItems();
    }

    int widgetStyle() {
        /*
	* This code is intentionally commented.  In order to
	* consistently support radio tool items across platforms,
	* the platform radio behavior is not used.
	*/
        return 0;
    }

    Color _background;

    boolean enabled;

    Color _foreground;

    boolean selection;

    int width;

    public ToolBar _parent() {
        return parent;
    }

    public Control _control() {
        return control;
    }

    public String _toolTipText() {
        return toolTipText;
    }

    public Image _disabledImage() {
        return disabledImage;
    }

    public Image _hotImage() {
        return hotImage;
    }

    public Image _disabledImage2() {
        return disabledImage2;
    }

    public int _id() {
        return id;
    }

    public short _cx() {
        return cx;
    }

    public int _foreground() {
        return foreground;
    }

    public int _background() {
        return background;
    }

    public Color __background() {
        return _background;
    }

    public boolean _enabled() {
        return enabled;
    }

    public Color __foreground() {
        return _foreground;
    }

    public boolean _selection() {
        return selection;
    }

    public int _width() {
        return width;
    }

    public FlutterBridge getBridge() {
        if (bridge != null)
            return bridge;
        Composite p = parent;
        while (!(p.getImpl() instanceof DartWidget)) p = p.getImpl()._parent();
        return ((DartWidget) p.getImpl()).getBridge();
    }

    protected void hookEvents() {
        super.hookEvents();
        FlutterBridge.on(this, "Selection", "Selection", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Selection, e);
            });
        });
        FlutterBridge.on(this, "Selection", "DefaultSelection", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.DefaultSelection, e);
            });
        });
    }

    public ToolItem getApi() {
        if (api == null)
            api = ToolItem.createApi(this);
        return (ToolItem) api;
    }

    public VToolItem getValue() {
        if (value == null)
            value = new VToolItem(this);
        return (VToolItem) value;
    }
}
