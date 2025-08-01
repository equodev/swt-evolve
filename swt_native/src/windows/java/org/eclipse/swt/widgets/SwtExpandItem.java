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
import org.eclipse.swt.internal.win32.*;

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
public class SwtExpandItem extends SwtItem implements IExpandItem {

    ExpandBar parent;

    Control control;

    boolean expanded, hover;

    int x, y, width, height;

    int imageHeight, imageWidth;

    private static final int IMAGE_MARGIN = 8;

    static final int TEXT_INSET = 6;

    static final int BORDER = 1;

    static final int CHEVRON_SIZE = 24;

    static {
        DPIZoomChangeRegistry.registerHandler(SwtExpandItem::handleDPIChange, ExpandItem.class);
    }

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
    public SwtExpandItem(ExpandBar parent, int style, ExpandItem api) {
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
    public SwtExpandItem(ExpandBar parent, int style, int index, ExpandItem api) {
        super(parent, style, api);
        this.parent = parent;
        ((SwtExpandBar) parent.getImpl()).createItem(this.getApi(), style, index);
    }

    static ExpandBar checkNull(ExpandBar control) {
        if (control == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        return control;
    }

    private void drawChevron(long hDC, RECT rect) {
        long oldBrush = OS.SelectObject(hDC, OS.GetSysColorBrush(OS.COLOR_BTNFACE));
        OS.PatBlt(hDC, rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top, OS.PATCOPY);
        OS.SelectObject(hDC, oldBrush);
        rect.left += 4;
        rect.top += 4;
        rect.right -= 4;
        rect.bottom -= 4;
        long hPen = OS.CreatePen(OS.PS_SOLID, 1, ((SwtControl) parent.getImpl()).getForegroundPixel());
        long oldPen = OS.SelectObject(hDC, hPen);
        int[] polyline1, polyline2;
        if (expanded) {
            int px = rect.left + 5;
            int py = rect.top + 7;
            polyline1 = new int[] { px, py, px + 1, py, px + 1, py - 1, px + 2, py - 1, px + 2, py - 2, px + 3, py - 2, px + 3, py - 3, px + 3, py - 2, px + 4, py - 2, px + 4, py - 1, px + 5, py - 1, px + 5, py, px + 7, py };
            py += 4;
            polyline2 = new int[] { px, py, px + 1, py, px + 1, py - 1, px + 2, py - 1, px + 2, py - 2, px + 3, py - 2, px + 3, py - 3, px + 3, py - 2, px + 4, py - 2, px + 4, py - 1, px + 5, py - 1, px + 5, py, px + 7, py };
        } else {
            int px = rect.left + 5;
            int py = rect.top + 4;
            polyline1 = new int[] { px, py, px + 1, py, px + 1, py + 1, px + 2, py + 1, px + 2, py + 2, px + 3, py + 2, px + 3, py + 3, px + 3, py + 2, px + 4, py + 2, px + 4, py + 1, px + 5, py + 1, px + 5, py, px + 7, py };
            py += 4;
            polyline2 = new int[] { px, py, px + 1, py, px + 1, py + 1, px + 2, py + 1, px + 2, py + 2, px + 3, py + 2, px + 3, py + 3, px + 3, py + 2, px + 4, py + 2, px + 4, py + 1, px + 5, py + 1, px + 5, py, px + 7, py };
        }
        OS.Polyline(hDC, polyline1, polyline1.length / 2);
        OS.Polyline(hDC, polyline2, polyline2.length / 2);
        if (hover) {
            long whitePen = OS.CreatePen(OS.PS_SOLID, 1, OS.GetSysColor(OS.COLOR_3DHILIGHT));
            long darkGrayPen = OS.CreatePen(OS.PS_SOLID, 1, OS.GetSysColor(OS.COLOR_3DSHADOW));
            OS.SelectObject(hDC, whitePen);
            int[] points1 = { rect.left, rect.bottom, rect.left, rect.top, rect.right, rect.top };
            OS.Polyline(hDC, points1, points1.length / 2);
            OS.SelectObject(hDC, darkGrayPen);
            int[] points2 = { rect.right, rect.top, rect.right, rect.bottom, rect.left, rect.bottom };
            OS.Polyline(hDC, points2, points2.length / 2);
            OS.SelectObject(hDC, oldPen);
            OS.DeleteObject(whitePen);
            OS.DeleteObject(darkGrayPen);
        } else {
            OS.SelectObject(hDC, oldPen);
        }
        OS.DeleteObject(hPen);
    }

    void drawItem(GC gc, long hTheme, RECT clipRect, boolean drawFocus) {
        long hDC = gc.handle;
        int headerHeightinPixels = getHeaderHeightInPixels();
        int zoom = getZoom();
        int imageHeightInPixels = DPIUtil.scaleUp(imageHeight, zoom);
        int imageWidthInPixels = DPIUtil.scaleUp(imageWidth, zoom);
        RECT rect = new RECT();
        OS.SetRect(rect, x, y, x + width, y + headerHeightinPixels);
        if (hTheme != 0) {
            OS.DrawThemeBackground(hTheme, hDC, OS.EBP_NORMALGROUPHEAD, 0, rect, clipRect);
        } else {
            long oldBrush = OS.SelectObject(hDC, OS.GetSysColorBrush(OS.COLOR_BTNFACE));
            OS.PatBlt(hDC, rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top, OS.PATCOPY);
            OS.SelectObject(hDC, oldBrush);
        }
        if (image != null) {
            rect.left += SwtExpandItem.TEXT_INSET;
            int yInPoints = DPIUtil.scaleDown(rect.top + ((headerHeightinPixels - imageHeightInPixels) / 2), zoom);
            gc.drawImage(image, DPIUtil.scaleDown(rect.left, zoom), yInPoints);
            rect.left += imageWidthInPixels;
        }
        if (text.length() > 0) {
            rect.left += SwtExpandItem.TEXT_INSET;
            char[] buffer;
            if ((getApi().style & SWT.FLIP_TEXT_DIRECTION) != 0) {
                int bits = OS.GetWindowLong(parent.handle, OS.GWL_EXSTYLE);
                if ((bits & OS.WS_EX_LAYOUTRTL) != 0) {
                    buffer = (LRE + text).toCharArray();
                } else {
                    buffer = (RLE + text).toCharArray();
                }
            } else {
                buffer = text.toCharArray();
            }
            if (hTheme != 0) {
                OS.DrawThemeText(hTheme, hDC, OS.EBP_NORMALGROUPHEAD, 0, buffer, buffer.length, OS.DT_VCENTER | OS.DT_SINGLELINE, 0, rect);
            } else {
                int oldBkMode = OS.SetBkMode(hDC, OS.TRANSPARENT);
                OS.DrawText(hDC, buffer, buffer.length, rect, OS.DT_VCENTER | OS.DT_SINGLELINE);
                OS.SetBkMode(hDC, oldBkMode);
            }
        }
        int chevronSize = SwtExpandItem.CHEVRON_SIZE;
        rect.left = rect.right - chevronSize;
        rect.top = y;
        rect.bottom = rect.top + chevronSize;
        if (hTheme != 0) {
            int partID = expanded ? OS.EBP_NORMALGROUPCOLLAPSE : OS.EBP_NORMALGROUPEXPAND;
            int stateID = hover ? OS.EBNGC_HOT : OS.EBNGC_NORMAL;
            OS.DrawThemeBackground(hTheme, hDC, partID, stateID, rect, clipRect);
        } else {
            drawChevron(hDC, rect);
        }
        if (drawFocus) {
            OS.SetRect(rect, x + 1, y + 1, x + width - 2, y + headerHeightinPixels - 2);
            OS.DrawFocusRect(hDC, rect);
        }
        if (expanded) {
            if (!((SwtExpandBar) parent.getImpl()).isAppThemed()) {
                long pen = OS.CreatePen(OS.PS_SOLID, 1, OS.GetSysColor(OS.COLOR_BTNFACE));
                long oldPen = OS.SelectObject(hDC, pen);
                int[] points = { x, y + headerHeightinPixels, x, y + headerHeightinPixels + height, x + width - 1, y + headerHeightinPixels + height, x + width - 1, y + headerHeightinPixels - 1 };
                OS.Polyline(hDC, points, points.length / 2);
                OS.SelectObject(hDC, oldPen);
                OS.DeleteObject(pen);
            }
        }
    }

    @Override
    void destroyWidget() {
        ((SwtExpandBar) parent.getImpl()).destroyItem(this.getApi());
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
        return DPIUtil.scaleDown(getHeaderHeightInPixels(), getZoom());
    }

    int getHeaderHeightInPixels() {
        int headerHeightInPixels = ((SwtExpandBar) parent.getImpl()).getBandHeight();
        int imageHeightInPixels = DPIUtil.scaleUp(imageHeight, getZoom());
        int imageHeaderDiff = headerHeightInPixels - imageHeightInPixels;
        if (imageHeaderDiff < IMAGE_MARGIN) {
            headerHeightInPixels = imageHeightInPixels + IMAGE_MARGIN;
        }
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
        return DPIUtil.scaleDown(getHeightInPixels(), getZoom());
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
        int width = SwtExpandItem.TEXT_INSET * 2 + SwtExpandItem.CHEVRON_SIZE;
        if (image != null) {
            width += SwtExpandItem.TEXT_INSET + imageWidth;
        }
        if (text.length() > 0) {
            RECT rect = new RECT();
            char[] buffer = text.toCharArray();
            if (hTheme != 0) {
                OS.GetThemeTextExtent(hTheme, hDC, OS.EBP_NORMALGROUPHEAD, 0, buffer, buffer.length, OS.DT_SINGLELINE, null, rect);
            } else {
                OS.DrawText(hDC, buffer, buffer.length, rect, OS.DT_CALCRECT);
            }
            width += (rect.right - rect.left);
        }
        return width;
    }

    boolean isHover(int x, int y) {
        int bandHeight = ((SwtExpandBar) parent.getImpl()).getBandHeight();
        return this.x < x && x < (this.x + width) && this.y < y && y < (this.y + bandHeight);
    }

    void redraw(boolean all) {
        long parentHandle = parent.handle;
        int headerHeightInPixels = getHeaderHeightInPixels();
        int zoom = getZoom();
        int imageHeightInPixels = DPIUtil.scaleUp(imageHeight, zoom);
        int imageWidthInPixels = DPIUtil.scaleUp(imageWidth, zoom);
        RECT rect = new RECT();
        int left = all ? x : x + width - headerHeightInPixels;
        OS.SetRect(rect, left, y, x + width, y + headerHeightInPixels);
        OS.InvalidateRect(parentHandle, rect, true);
        if (imageHeightInPixels > headerHeightInPixels) {
            OS.SetRect(rect, x + SwtExpandItem.TEXT_INSET, y + headerHeightInPixels - imageHeightInPixels, x + SwtExpandItem.TEXT_INSET + imageWidthInPixels, y);
            OS.InvalidateRect(parentHandle, rect, true);
        }
        if (!((SwtExpandBar) parent.getImpl()).isAppThemed()) {
            OS.SetRect(rect, x, y + headerHeightInPixels, x + width, y + headerHeightInPixels + height + 1);
            OS.InvalidateRect(parentHandle, rect, true);
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
            if (!((SwtExpandBar) parent.getImpl()).isAppThemed()) {
                x += BORDER;
                width = Math.max(0, width - BORDER * 2);
                height = Math.max(0, height - BORDER);
            }
            if (move && size)
                ((SwtControl) control.getImpl()).setBoundsInPixels(x, y + headerHeightInPixels, width, height);
            if (move && !size)
                ((SwtControl) control.getImpl()).setLocationInPixels(x, y + headerHeightInPixels);
            if (!move && size)
                ((SwtControl) control.getImpl()).setSizeInPixels(width, height);
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
        checkWidget();
        if (control != null) {
            if (control.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
            if (((SwtControl) control.getImpl()).parent != parent)
                error(SWT.ERROR_INVALID_PARENT);
        }
        this.control = control;
        if (control != null) {
            int headerHeight = ((SwtExpandBar) parent.getImpl()).getBandHeight();
            control.setVisible(expanded);
            if (!((SwtExpandBar) parent.getImpl()).isAppThemed()) {
                int width = Math.max(0, this.width - BORDER * 2);
                int height = Math.max(0, this.height - BORDER);
                if (control.getImpl() instanceof SwtControl) {
                    ((SwtControl) control.getImpl()).setBoundsInPixels(x + BORDER, y + headerHeight, width, height);
                }
            } else {
                if (control.getImpl() instanceof SwtControl) {
                    ((SwtControl) control.getImpl()).setBoundsInPixels(x, y + headerHeight, width, height);
                }
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
        checkWidget();
        this.expanded = expanded;
        ((SwtExpandBar) parent.getImpl()).showItem(this.getApi());
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
        setHeightInPixels(DPIUtil.scaleUp(height, getZoom()));
    }

    void setHeightInPixels(int height) {
        if (height < 0)
            return;
        setBoundsInPixels(0, 0, width, height, false, true);
        if (expanded)
            ((SwtExpandBar) parent.getImpl()).layoutItems(parent.indexOf(this.getApi()) + 1, true);
    }

    @Override
    public void setImage(Image image) {
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
            ((SwtExpandBar) parent.getImpl()).layoutItems(parent.indexOf(this.getApi()), true);
        } else {
            redraw(true);
        }
    }

    @Override
    public void setText(String string) {
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
        if (((SwtExpandItem) item.getImpl()).height != 0 || ((SwtExpandItem) item.getImpl()).width != 0) {
            int newWidth = Math.round(((SwtExpandItem) item.getImpl()).width * scalingFactor);
            int newHeight = Math.round(((SwtExpandItem) item.getImpl()).height * scalingFactor);
            ((SwtExpandItem) item.getImpl()).setBoundsInPixels(((SwtExpandItem) item.getImpl()).x, ((SwtExpandItem) item.getImpl()).y, newWidth, newHeight, true, true);
        }
    }

    public ExpandItem getApi() {
        if (api == null)
            api = ExpandItem.createApi(this);
        return (ExpandItem) api;
    }
}
