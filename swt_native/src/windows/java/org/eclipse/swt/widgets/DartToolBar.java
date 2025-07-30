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

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import dev.equo.swt.*;

/**
 * Instances of this class support the layout of selectable
 * tool bar items.
 * <p>
 * The item children that may be added to instances of this class
 * must be of type <code>ToolItem</code>.
 * </p><p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not make sense to add <code>Control</code> children to it,
 * or set a layout on it.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>FLAT, WRAP, RIGHT, HORIZONTAL, VERTICAL, SHADOW_OUT</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles HORIZONTAL and VERTICAL may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#toolbar">ToolBar, ToolItem snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartToolBar extends DartComposite implements IToolBar {

    int lastFocusId, lastArrowId, lastHotId, _width, _height, _count = -1, _wHint = -1, _hHint = -1;

    long currentToolItemToolTip;

    ToolItem[] items;

    ToolItem[] tabItemList;

    boolean ignoreResize, ignoreMouse;

    /*
	* From the Windows SDK for TB_SETBUTTONSIZE:
	*
	*   "If an application does not explicitly
	*	set the button size, the size defaults
	*	to 24 by 22 pixels".
	*/
    static final int DEFAULT_WIDTH = 24;

    static final int DEFAULT_HEIGHT = 22;

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
     * @see SWT#FLAT
     * @see SWT#WRAP
     * @see SWT#RIGHT
     * @see SWT#HORIZONTAL
     * @see SWT#SHADOW_OUT
     * @see SWT#VERTICAL
     * @see Widget#checkSubclass()
     * @see Widget#getStyle()
     */
    public DartToolBar(Composite parent, int style, ToolBar api) {
        super(parent, checkStyle(style), api);
        /*
	* Ensure that either of HORIZONTAL or VERTICAL is set.
	* NOTE: HORIZONTAL and VERTICAL have the same values
	* as H_SCROLL and V_SCROLL so it is necessary to first
	* clear these bits to avoid scroll bars and then reset
	* the bits using the original style supplied by the
	* programmer.
	*
	* NOTE: The CCS_VERT style cannot be applied when the
	* widget is created because of this conflict.
	*/
        if ((style & SWT.VERTICAL) != 0) {
            this.getApi().style |= SWT.VERTICAL;
        } else {
            this.getApi().style |= SWT.HORIZONTAL;
        }
    }

    static int checkStyle(int style) {
        /*
	* On Windows, only flat tool bars can be traversed.
	*/
        if ((style & SWT.FLAT) == 0)
            style |= SWT.NO_FOCUS;
        /*
	* A vertical tool bar cannot wrap because TB_SETROWS
	* fails when the toolbar has TBSTYLE_WRAPABLE.
	*/
        if ((style & SWT.VERTICAL) != 0)
            style &= ~SWT.WRAP;
        /*
	* Even though it is legal to create this widget
	* with scroll bars, they serve no useful purpose
	* because they do not automatically scroll the
	* widget's client area.  The fix is to clear
	* the SWT style.
	*/
        return style & ~(SWT.H_SCROLL | SWT.V_SCROLL);
    }

    @Override
    void checkBuffered() {
        super.checkBuffered();
        getApi().style |= SWT.DOUBLE_BUFFERED;
    }

    @Override
    public void checkSubclass() {
        if (!isValidSubclass())
            error(SWT.ERROR_INVALID_SUBCLASS);
    }

    @Override
    public void layout(boolean changed) {
        checkWidget();
        clearSizeCache(changed);
        super.layout(changed);
    }

    void clearSizeCache(boolean changed) {
        // If changed, discard the cached layout information
        if (changed) {
            _count = _wHint = _hHint = -1;
        }
    }

    @Override
    Point computeSizeInPixels(int wHint, int hHint, boolean changed) {
        return Sizes.compute(this);
    }

    @Override
    Rectangle computeTrimInPixels(int x, int y, int width, int height) {
        Rectangle trim = super.computeTrimInPixels(x, y, width, height);
        return trim;
    }

    @Override
    Widget computeTabGroup() {
        ToolItem[] items = _getItems();
        if (tabItemList == null) {
            int i = 0;
            while (i < items.length && ((DartToolItem) items[i].getImpl()).control == null) i++;
            if (i == items.length)
                return super.computeTabGroup();
        }
        return super.computeTabGroup();
    }

    @Override
    public Widget[] computeTabList() {
        ToolItem[] items = _getItems();
        if (tabItemList == null) {
            int i = 0;
            while (i < items.length && ((DartToolItem) items[i].getImpl()).control == null) i++;
            if (i == items.length)
                return super.computeTabList();
        }
        Widget[] result = {};
        if (!isTabGroup() || !isEnabled() || !isVisible())
            return result;
        ToolItem[] list = tabList != null ? _getTabItemList() : items;
        for (ToolItem child : list) {
            Widget[] childList = child.getImpl().computeTabList();
            if (childList.length != 0) {
                Widget[] newResult = new Widget[result.length + childList.length];
                System.arraycopy(result, 0, newResult, 0, result.length);
                System.arraycopy(childList, 0, newResult, result.length, childList.length);
                result = newResult;
            }
        }
        if (result.length == 0)
            result = new Widget[] { this.getApi() };
        return result;
    }

    @Override
    void createHandle() {
        super.createHandle();
        getApi().state &= ~CANVAS;
        /*
	* Feature in Windows.  When TBSTYLE_FLAT is used to create
	* a flat toolbar, for some reason TBSTYLE_TRANSPARENT is
	* also set.  This causes the toolbar to flicker when it is
	* moved or resized.  The fix is to clear TBSTYLE_TRANSPARENT.
	*
	* NOTE:  This work around is unnecessary on XP.  There is no
	* flickering and clearing the TBSTYLE_TRANSPARENT interferes
	* with the XP theme.
	*/
        if ((getApi().style & SWT.FLAT) != 0) {
        }
        /*
	* Feature in Windows.  Despite the fact that the
	* tool tip text contains \r\n, the tooltip will
	* not honour the new line unless TTM_SETMAXTIPWIDTH
	* is set.  The fix is to set TTM_SETMAXTIPWIDTH to
	* a large value.
	*/
        /*
	* These lines are intentionally commented.  The tool
	* bar currently sets this value to 300 so it is not
	* necessary to set TTM_SETMAXTIPWIDTH.
	*/
        //	long hwndToolTip = OS.SendMessage (handle, OS.TB_GETTOOLTIPS, 0, 0);
        //	OS.SendMessage (hwndToolTip, OS.TTM_SETMAXTIPWIDTH, 0, 0x7FFF);
    }

    void createItem(ToolItem item, int index) {
        int id = 0;
        while (id < items.length && items[id] != null) id++;
        if (id == items.length) {
            ToolItem[] newItems = new ToolItem[items.length + 4];
            System.arraycopy(items, 0, newItems, 0, items.length);
            items = newItems;
        }
        int bits = ((DartToolItem) item.getImpl()).widgetStyle();
        items[((DartToolItem) item.getImpl()).id = id] = item;
        layoutItems();
    }

    @Override
    void createWidget() {
        super.createWidget();
        items = new ToolItem[4];
        lastFocusId = lastArrowId = lastHotId = -1;
    }

    @Override
    int applyThemeBackground() {
        return -1;
        /* No Change */
    }

    void destroyItem(ToolItem item) {
        if (((DartToolItem) item.getImpl()).id == lastFocusId)
            lastFocusId = -1;
        if (((DartToolItem) item.getImpl()).id == lastArrowId)
            lastArrowId = -1;
        if (((DartToolItem) item.getImpl()).id == lastHotId)
            lastHotId = -1;
        items[((DartToolItem) item.getImpl()).id] = null;
        ((DartToolItem) item.getImpl()).id = -1;
        layoutItems();
    }

    @Override
    void enableWidget(boolean enabled) {
        super.enableWidget(enabled);
        /*
	* Bug in Windows.  When a tool item with the style
	* BTNS_CHECK or BTNS_CHECKGROUP is selected and then
	* disabled, the item does not draw using the disabled
	* image.  The fix is to use the disabled image in all
	* image lists for the item.
	*
	* Feature in Windows.  When a tool bar is disabled,
	* the text draws disabled but the images do not.
	* The fix is to use the disabled image in all image
	* lists for all items.
	*/
        for (ToolItem item : items) {
            if (item != null) {
                if ((item.style & SWT.SEPARATOR) == 0) {
                    ((DartToolItem) item.getImpl()).updateImages(enabled && item.getEnabled());
                }
            }
        }
    }

    /**
     * Returns the item at the given, zero-relative index in the
     * receiver. Throws an exception if the index is out of range.
     *
     * @param index the index of the item to return
     * @return the item at the given index
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public ToolItem getItem(int index) {
        checkWidget();
        return null;
    }

    /**
     * Returns the item at the given point in the receiver
     * or null if no such item exists. The point is in the
     * coordinate system of the receiver.
     *
     * @param point the point used to locate the item
     * @return the item at the given point
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public ToolItem getItem(Point point) {
        checkWidget();
        if (point == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        return getItemInPixels(DPIUtil.scaleUp(point, getZoom()));
    }

    ToolItem getItemInPixels(Point point) {
        for (ToolItem item : getItems()) {
            Rectangle rect = ((DartToolItem) item.getImpl()).getBoundsInPixels();
            if (rect.contains(point))
                return item;
        }
        return null;
    }

    /**
     * Returns the number of items contained in the receiver.
     *
     * @return the number of items
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getItemCount() {
        checkWidget();
        return 0;
    }

    /**
     * Returns an array of <code>ToolItem</code>s which are the items
     * in the receiver.
     * <p>
     * Note: This is not the actual structure used by the receiver
     * to maintain its list of items, so modifying the array will
     * not affect the receiver.
     * </p>
     *
     * @return the items in the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public ToolItem[] getItems() {
        checkWidget();
        return _getItems();
    }

    ToolItem[] _getItems() {
        return null;
    }

    /**
     * Returns the number of rows in the receiver. When
     * the receiver has the <code>WRAP</code> style, the
     * number of rows can be greater than one.  Otherwise,
     * the number of rows is always one.
     *
     * @return the number of items
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getRowCount() {
        checkWidget();
        if ((getApi().style & SWT.VERTICAL) != 0) {
        }
        return 0;
    }

    ToolItem[] _getTabItemList() {
        if (tabItemList == null)
            return tabItemList;
        int count = 0;
        for (ToolItem item : tabItemList) {
            if (!item.isDisposed())
                count++;
        }
        if (count == tabItemList.length)
            return tabItemList;
        ToolItem[] newList = new ToolItem[count];
        int index = 0;
        for (ToolItem item : tabItemList) {
            if (!item.isDisposed()) {
                newList[index++] = item;
            }
        }
        tabItemList = newList;
        return tabItemList;
    }

    /**
     * Searches the receiver's list starting at the first item
     * (index 0) until an item is found that is equal to the
     * argument, and returns the index of that item. If no item
     * is found, returns -1.
     *
     * @param item the search item
     * @return the index of the item
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the tool item is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the tool item has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int indexOf(ToolItem item) {
        checkWidget();
        if (item == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (item.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        return 0;
    }

    void layoutItems() {
        clearSizeCache(true);
        if ((getApi().style & SWT.WRAP) != 0) {
        }
        /*
	*  When the tool bar is vertical, make the width of each button
	*  be the width of the widest button in the tool bar.  Note that
	*  when the tool bar contains a drop down item, it needs to take
	*  into account extra padding.
	*/
        if ((getApi().style & SWT.VERTICAL) != 0) {
        }
        /*
	* Feature on Windows. When SWT.WRAP or SWT.VERTICAL are set
	* the separator items with control are implemented using BTNS_BUTTON
	* instead of BTNS_SEP. When that is the case and TBSTYLE_LIST is
	* set, the layout of the ToolBar recalculates the width for all
	* BTNS_BUTTON based on the text and bitmap of the item.
	* This is not strictly wrong, but the user defined width for the
	* separators has to be respected if set.
	* The fix is to detect this case and reset the cx width for the item.
	*/
        if ((getApi().style & (SWT.WRAP | SWT.VERTICAL)) != 0) {
        }
        for (ToolItem item : items) {
            if (item != null)
                ((DartToolItem) item.getImpl()).resizeControl();
        }
    }

    @Override
    boolean mnemonicHit(char ch) {
        int[] id = new int[1];
        if ((getApi().style & SWT.FLAT) != 0 && !setTabGroupFocus())
            return false;
        ((DartToolItem) items[id[0]].getImpl()).click(false);
        return true;
    }

    @Override
    boolean mnemonicMatch(char ch) {
        int[] id = new int[1];
        return findMnemonic(((DartItem) items[id[0]].getImpl()).text) != '\0';
    }

    @Override
    void releaseChildren(boolean destroy) {
        if (items != null) {
            for (ToolItem item : items) {
                if (item != null && !item.isDisposed()) {
                    item.getImpl().release(false);
                }
            }
            items = null;
        }
        super.releaseChildren(destroy);
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
    }

    @Override
    public void removeControl(Control control) {
        super.removeControl(control);
        for (ToolItem item : items) {
            if (item != null && ((DartToolItem) item.getImpl()).control == control) {
                item.setControl(null);
            }
        }
    }

    @Override
    void reskinChildren(int flags) {
        if (items != null) {
            for (ToolItem item : items) {
                if (item != null)
                    item.reskin(flags);
            }
        }
        super.reskinChildren(flags);
    }

    @Override
    void setBackgroundImage(long hBitmap) {
        super.setBackgroundImage(hBitmap);
        setBackgroundTransparent(hBitmap != 0);
    }

    @Override
    void setBackgroundPixel(int pixel) {
        super.setBackgroundPixel(pixel);
        setBackgroundTransparent(pixel != -1);
    }

    void setBackgroundTransparent(boolean transparent) {
        /*
	* Feature in Windows.  When TBSTYLE_TRANSPARENT is set
	* in a tool bar that is drawing a background, images in
	* the image list that include transparency information
	* do not draw correctly.  The fix is to clear and set
	* TBSTYLE_TRANSPARENT depending on the background color.
	*
	* NOTE:  This work around is unnecessary on XP.  The
	* TBSTYLE_TRANSPARENT style is never cleared on that
	* platform.
	*/
        if ((getApi().style & SWT.FLAT) != 0) {
        }
    }

    @Override
    void setBoundsInPixels(int x, int y, int width, int height, int flags) {
        super.setBoundsInPixels(x, y, width, height, flags);
    }

    @Override
    void setDefaultFont() {
        super.setDefaultFont();
    }

    void setDropDownItems(boolean set) {
    }

    @Override
    public void setFont(Font font) {
        checkWidget();
        setDropDownItems(false);
        super.setFont(font);
        setDropDownItems(true);
        /*
	* Bug in Windows.  When WM_SETFONT is sent to a tool bar
	* that contains only separators, causes the bitmap and button
	* sizes to be set.  The fix is to reset these sizes after the font
	* has been changed when the tool bar contains only separators.
	*/
        int index = 0;
        int mask = SWT.PUSH | SWT.CHECK | SWT.RADIO | SWT.DROP_DOWN;
        while (index < items.length) {
            ToolItem item = items[index];
            if (item != null && (item.style & mask) != 0)
                break;
            index++;
        }
        if (index == items.length) {
        }
        layoutItems();
        getBridge().dirty(this);
    }

    @Override
    public boolean setParent(Composite parent) {
        checkWidget();
        if (!super.setParent(parent))
            return false;
        long hwndParent = parent.handle;
        /*
	* Bug in Windows.  When a tool bar is reparented, the tooltip
	* control that is automatically created for the item is not
	* reparented to the new shell.  The fix is to move the tooltip
	* over using SetWindowLongPtr().  Note that for some reason,
	* SetParent() does not work.
	*/
        long hwndShell = parent.getShell().handle;
        return true;
    }

    @Override
    public void setRedraw(boolean redraw) {
        checkWidget();
        setDropDownItems(false);
        super.setRedraw(redraw);
        setDropDownItems(true);
        getBridge().dirty(this);
    }

    void setRowCount(int count) {
        if ((getApi().style & SWT.VERTICAL) != 0) {
            ignoreResize = true;
            /*
		* Feature in Windows.  When the last button in a tool bar has the
		* style BTNS_SEP and TB_SETROWS is used to set the number of rows
		* in the tool bar, depending on the number of buttons, the toolbar
		* will wrap items with the style BTNS_CHECK, even when the fLarger
		* flags is used to force the number of rows to be larger than the
		* number of items.  The fix is to set the number of rows to be two
		* larger than the actual number of rows in the tool bar.  When items
		* are being added, as long as the number of rows is at least one
		* item larger than the count, the tool bar is laid out properly.
		* When items are being removed, setting the number of rows to be
		* one more than the item count has no effect.  The number of rows
		* is already one more causing TB_SETROWS to do nothing.  Therefore,
		* choosing two instead of one as the row increment fixes both cases.
		*/
            count += 2;
            ignoreResize = false;
        }
    }

    /*public*/
    void setTabItemList(ToolItem[] tabList) {
        checkWidget();
        if (tabList != null) {
            for (ToolItem item : tabList) {
                if (item == null)
                    error(SWT.ERROR_INVALID_ARGUMENT);
                if (item.isDisposed())
                    error(SWT.ERROR_INVALID_ARGUMENT);
                if (((DartToolItem) item.getImpl()).parent != this.getApi())
                    error(SWT.ERROR_INVALID_PARENT);
            }
            ToolItem[] newList = new ToolItem[tabList.length];
            System.arraycopy(tabList, 0, newList, 0, tabList.length);
            tabList = newList;
        }
        this.tabItemList = tabList;
    }

    @Override
    public boolean setTabItemFocus() {
        int index = 0;
        while (index < items.length) {
            ToolItem item = items[index];
            if (item != null && (item.style & SWT.SEPARATOR) == 0) {
                if (item.getEnabled())
                    break;
            }
            index++;
        }
        if (index == items.length)
            return false;
        return super.setTabItemFocus();
    }

    @Override
    boolean updateTextDirection(int textDirection) {
        if (super.updateTextDirection(textDirection)) {
            ToolItem[] items = _getItems();
            int i = items.length;
            while (i-- > 0) {
                ((DartToolItem) items[i].getImpl()).updateTextDirection(getApi().style & SWT.FLIP_TEXT_DIRECTION);
            }
            return true;
        }
        return false;
    }

    @Override
    void updateOrientation() {
        super.updateOrientation();
    }

    @Override
    int widgetStyle() {
        return 0;
    }

    int getForegroundPixel(ToolItem item) {
        if (item != null && ((DartToolItem) item.getImpl()).foreground != -1) {
            return ((DartToolItem) item.getImpl()).foreground;
        }
        return getForegroundPixel();
    }

    int getBackgroundPixel(ToolItem item) {
        if (item != null && ((DartToolItem) item.getImpl()).background != -1) {
            return ((DartToolItem) item.getImpl()).background;
        }
        return getBackgroundPixel();
    }

    private static void handleDPIChange(Widget widget, int newZoom, float scalingFactor) {
        if (!(widget instanceof ToolBar toolBar)) {
            return;
        }
        ToolItem[] toolItems = ((DartToolBar) toolBar.getImpl())._getItems();
        var seperatorWidth = new int[toolItems.length];
        int itemCount = toolItems.length;
        if (itemCount == 0) {
            return;
        }
        for (int i = itemCount - 1; i >= 0; i--) {
            ToolItem item = toolItems[i];
            if ((item.style & SWT.SEPARATOR) != 0 && item.getControl() != null) {
                // Take note of widths of separators with control, so they can be resized
                // at the end
                seperatorWidth[i] = item.getWidth();
            }
        }
        for (int i = 0; i < itemCount; i++) {
            ToolItem item = toolItems[i];
            // If the separator is used with a control, we must reset the size to the cached value,
            // cause windows will treat the separator as normal separator and shrinks it accordingly
            if ((item.style & SWT.SEPARATOR) != 0 && item.getControl() != null) {
                item.setWidth(seperatorWidth[i]);
            }
        }
        toolBar.layout(true);
    }

    public int _lastFocusId() {
        return lastFocusId;
    }

    public int _lastArrowId() {
        return lastArrowId;
    }

    public int _lastHotId() {
        return lastHotId;
    }

    public int __width() {
        return _width;
    }

    public int __height() {
        return _height;
    }

    public int __count() {
        return _count;
    }

    public int __wHint() {
        return _wHint;
    }

    public int __hHint() {
        return _hHint;
    }

    public long _currentToolItemToolTip() {
        return currentToolItemToolTip;
    }

    public ToolItem[] _items() {
        return items;
    }

    public ToolItem[] _tabItemList() {
        return tabItemList;
    }

    public boolean _ignoreResize() {
        return ignoreResize;
    }

    public boolean _ignoreMouse() {
        return ignoreMouse;
    }

    protected void hookEvents() {
        super.hookEvents();
    }

    public ToolBar getApi() {
        if (api == null)
            api = ToolBar.createApi(this);
        return (ToolBar) api;
    }

    public VToolBar getValue() {
        if (value == null)
            value = new VToolBar(this);
        return (VToolBar) value;
    }
}
