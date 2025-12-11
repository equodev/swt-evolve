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
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import dev.equo.swt.*;

/**
 * Instances of this class represent a selectable user interface object
 * that represents a hierarchy of tree items in a tree widget.
 *
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
 * @see <a href="http://www.eclipse.org/swt/snippets/#tree">Tree, TreeItem, TreeColumn snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartTreeItem extends DartItem implements ITreeItem {

    Tree parent;

    String[] strings;

    Image[] images;

    Font font;

    Font[] cellFont;

    boolean cached;

    int background = -1, foreground = -1;

    int[] cellBackground, cellForeground;

    /**
     * Constructs <code>TreeItem</code> and <em>inserts</em> it into <code>Tree</code>.
     * Item is inserted as last direct child of the tree.
     * <p>
     * The fastest way to insert many items is documented in {@link TreeItem#TreeItem(Tree,int,int)}
     * and {@link TreeItem#setItemCount}
     *
     * @param parent a tree control which will be the parent of the new instance (cannot be null)
     * @param style no styles are currently supported, pass SWT.NONE
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see SWT
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartTreeItem(Tree parent, int style, TreeItem api) {
        this(parent, style, 0, 0, 0, api);
    }

    /**
     * Constructs <code>TreeItem</code> and <em>inserts</em> it into <code>Tree</code>.
     * Item is inserted as <code>index</code> direct child of the tree.
     * <p>
     * The fastest way to insert many items is:
     * <ol>
     * <li>Use {@link Tree#setRedraw} to disable drawing during bulk insert</li>
     * <li>Insert every item at index 0 (insert them in reverse to get the same result)</li>
     * <li>Collapse the parent item before inserting (gives massive improvement on Windows)</li>
     * </ol>
     *
     * @param parent a tree control which will be the parent of the new instance (cannot be null)
     * @param style no styles are currently supported, pass SWT.NONE
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
     * @see SWT
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     * @see Tree#setRedraw
     */
    public DartTreeItem(Tree parent, int style, int index, TreeItem api) {
        this(parent, style, 0, findPrevious(parent, index), 0, api);
    }

    /**
     * Constructs <code>TreeItem</code> and <em>inserts</em> it into <code>Tree</code>.
     * Item is inserted as last direct child of the specified <code>TreeItem</code>.
     * <p>
     * The fastest way to insert many items is documented in {@link TreeItem#TreeItem(Tree,int,int)}
     * and {@link TreeItem#setItemCount}
     *
     * @param parentItem a tree control which will be the parent of the new instance (cannot be null)
     * @param style no styles are currently supported, pass SWT.NONE
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see SWT
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartTreeItem(TreeItem parentItem, int style, TreeItem api) {
        this(((DartTreeItem) checkNull(parentItem).getImpl()).parent, style, parentItem, -1, 0, api);
    }

    /**
     * Constructs <code>TreeItem</code> and <em>inserts</em> it into <code>Tree</code>.
     * Item is inserted as <code>index</code> direct child of the specified <code>TreeItem</code>.
     * <p>
     * The fastest way to insert many items is documented in {@link TreeItem#TreeItem(Tree,int,int)}
     * and {@link TreeItem#setItemCount}
     *
     * @param parentItem a tree control which will be the parent of the new instance (cannot be null)
     * @param style no styles are currently supported, pass SWT.NONE
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
     * @see SWT
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     * @see Tree#setRedraw
     */
    public DartTreeItem(TreeItem parentItem, int style, int index, TreeItem api) {
        this(((DartTreeItem) checkNull(parentItem).getImpl()).parent, style, parentItem, index, 0, api);
    }

    DartTreeItem(Tree parent, int style, long hParent, long hInsertAfter, long hItem, TreeItem api) {
        super(parent, style, api);
        this.parent = parent;
        ((DartTree) parent.getImpl()).createItem(this.getApi(), hParent, hInsertAfter, hItem);
    }

    static TreeItem checkNull(TreeItem item) {
        if (item == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        return item;
    }

    static long findPrevious(Tree parent, int index) {
        if (parent == null)
            return 0;
        if (index < 0)
            SWT.error(SWT.ERROR_INVALID_RANGE);
        return 0;
    }

    static long findPrevious(TreeItem parentItem, int index) {
        if (parentItem == null)
            return 0;
        if (index < 0)
            SWT.error(SWT.ERROR_INVALID_RANGE);
        return 0;
    }

    @Override
    public void checkSubclass() {
        if (!isValidSubclass())
            error(SWT.ERROR_INVALID_SUBCLASS);
    }

    void clear() {
        text = "";
        image = null;
        strings = null;
        images = null;
        if ((parent.style & SWT.CHECK) != 0) {
        }
        background = foreground = -1;
        font = null;
        cellBackground = cellForeground = null;
        cellFont = null;
        if ((parent.style & SWT.VIRTUAL) != 0)
            cached = false;
    }

    /**
     * Clears the item at the given zero-relative index in the receiver.
     * The text, icon and other attributes of the item are set to the default
     * value.  If the tree was created with the <code>SWT.VIRTUAL</code> style,
     * these attributes are requested again as needed.
     *
     * @param index the index of the item to clear
     * @param all <code>true</code> if all child items of the indexed item should be
     * cleared recursively, and <code>false</code> otherwise
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SWT#VIRTUAL
     * @see SWT#SetData
     *
     * @since 3.2
     */
    public void clear(int index, boolean all) {
        checkWidget();
        if (all) {
        }
    }

    /**
     * Clears all the items in the receiver. The text, icon and other
     * attributes of the items are set to their default values. If the
     * tree was created with the <code>SWT.VIRTUAL</code> style, these
     * attributes are requested again as needed.
     *
     * @param all <code>true</code> if all child items should be cleared
     * recursively, and <code>false</code> otherwise
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SWT#VIRTUAL
     * @see SWT#SetData
     *
     * @since 3.2
     */
    public void clearAll(boolean all) {
        checkWidget();
    }

    @Override
    void destroyWidget() {
        ((DartTree) parent.getImpl()).destroyItem(this.getApi(), getApi().handle);
        releaseHandle();
    }

    long fontHandle(int index) {
        return -1;
    }

    /**
     * Returns the receiver's background color.
     *
     * @return the background color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 2.0
     */
    public Color getBackground() {
        checkWidget();
        return this._background;
    }

    /**
     * Returns the background color at the given column index in the receiver.
     *
     * @param index the column index
     * @return the background color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    public Color getBackground(int index) {
        checkWidget();
        if (!((DartTree) parent.getImpl()).checkData(this.getApi(), true))
            error(SWT.ERROR_WIDGET_DISPOSED);
        int count = Math.max(1, parent.getColumnCount());
        if (0 > index || index > count - 1)
            return getBackground();
        int pixel = cellBackground != null ? cellBackground[index] : -1;
        return pixel == -1 ? getBackground() : SwtColor.win32_new(display, pixel);
    }

    /**
     * Returns a rectangle describing the size and location of the receiver's
     * text relative to its parent.
     *
     * @return the bounding rectangle of the receiver's text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Rectangle getBounds() {
        checkWidget();
        return null;
    }

    Rectangle getBoundsInPixels() {
        if (!((DartTree) parent.getImpl()).checkData(this.getApi(), true))
            error(SWT.ERROR_WIDGET_DISPOSED);
        return null;
    }

    /**
     * Returns a rectangle describing the receiver's size and location
     * relative to its parent at a column in the tree.
     *
     * @param index the index that specifies the column
     * @return the receiver's bounding column rectangle
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    public Rectangle getBounds(int index) {
        checkWidget();
        return null;
    }

    Rectangle getBoundsInPixels(int index) {
        if (!((DartTree) parent.getImpl()).checkData(this.getApi(), true))
            error(SWT.ERROR_WIDGET_DISPOSED);
        return null;
    }

    /**
     * Returns <code>true</code> if the receiver is checked,
     * and false otherwise.  When the parent does not have
     * the <code>CHECK</code> style, return false.
     *
     * @return the checked state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean getChecked() {
        checkWidget();
        if (!((DartTree) parent.getImpl()).checkData(this.getApi(), true))
            error(SWT.ERROR_WIDGET_DISPOSED);
        if ((parent.style & SWT.CHECK) == 0)
            return false;
        return this.cached;
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
     * Returns the font that the receiver will use to paint textual information for this item.
     *
     * @return the receiver's font
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    public Font getFont() {
        checkWidget();
        if (!((DartTree) parent.getImpl()).checkData(this.getApi(), true))
            error(SWT.ERROR_WIDGET_DISPOSED);
        return font != null ? font : parent.getFont();
    }

    /**
     * Returns the font that the receiver will use to paint textual information
     * for the specified cell in this item.
     *
     * @param index the column index
     * @return the receiver's font
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    public Font getFont(int index) {
        checkWidget();
        if (!((DartTree) parent.getImpl()).checkData(this.getApi(), true))
            error(SWT.ERROR_WIDGET_DISPOSED);
        int count = Math.max(1, parent.getColumnCount());
        if (0 > index || index > count - 1)
            return getFont();
        if (cellFont == null || cellFont[index] == null)
            return getFont();
        return cellFont[index];
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
     * @since 2.0
     */
    public Color getForeground() {
        checkWidget();
        return this._foreground;
    }

    /**
     * Returns the foreground color at the given column index in the receiver.
     *
     * @param index the column index
     * @return the foreground color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    public Color getForeground(int index) {
        checkWidget();
        if (!((DartTree) parent.getImpl()).checkData(this.getApi(), true))
            error(SWT.ERROR_WIDGET_DISPOSED);
        int count = Math.max(1, parent.getColumnCount());
        if (0 > index || index > count - 1)
            return getForeground();
        int pixel = cellForeground != null ? cellForeground[index] : -1;
        return pixel == -1 ? getForeground() : SwtColor.win32_new(display, pixel);
    }

    /**
     * Returns <code>true</code> if the receiver is grayed,
     * and false otherwise. When the parent does not have
     * the <code>CHECK</code> style, return false.
     *
     * @return the grayed state of the checkbox
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean getGrayed() {
        checkWidget();
        if (!((DartTree) parent.getImpl()).checkData(this.getApi(), true))
            error(SWT.ERROR_WIDGET_DISPOSED);
        if ((parent.style & SWT.CHECK) == 0)
            return false;
        return this.cached;
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
     *
     * @since 3.1
     */
    public TreeItem getItem(int index) {
        if (items == null)
            error(SWT.ERROR_INVALID_RANGE);
        int nonNullCount = 0;
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                if (nonNullCount == index) {
                    return items[i];
                }
                nonNullCount++;
            }
        }
        error(SWT.ERROR_INVALID_RANGE);
        // Never reached due to error() above
        return null;
    }

    /**
     * Returns the number of items contained in the receiver
     * that are direct item children of the receiver.
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
        if (this.items == null)
            return 0;
        int count = 0;
        for (TreeItem item : this.items) {
            if (item != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns a (possibly empty) array of <code>TreeItem</code>s which
     * are the direct item children of the receiver.
     * <p>
     * Note: This is not the actual structure used by the receiver
     * to maintain its list of items, so modifying the array will
     * not affect the receiver.
     * </p>
     *
     * @return the receiver's items
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public TreeItem[] getItems() {
        checkWidget();
        if (this.items == null)
            return new TreeItem[0];
        int count = getItemCount();
        TreeItem[] result = new TreeItem[count];
        int resultIndex = 0;
        for (int i = 0; i < this.items.length && resultIndex < count; i++) {
            if (this.items[i] != null) {
                result[resultIndex++] = this.items[i];
            }
        }
        return result;
    }

    @Override
    public Image getImage() {
        checkWidget();
        if (!((DartTree) parent.getImpl()).checkData(this.getApi(), true))
            error(SWT.ERROR_WIDGET_DISPOSED);
        return super.getImage();
    }

    /**
     * Returns the image stored at the given column index in the receiver,
     * or null if the image has not been set or if the column does not exist.
     *
     * @param index the column index
     * @return the image stored at the given column index in the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    public Image getImage(int index) {
        checkWidget();
        if (!((DartTree) parent.getImpl()).checkData(this.getApi(), true))
            error(SWT.ERROR_WIDGET_DISPOSED);
        if (index == 0)
            return getImage();
        if (images != null) {
            if (0 <= index && index < images.length)
                return images[index];
        }
        return null;
    }

    /**
     * Returns a rectangle describing the size and location
     * relative to its parent of an image at a column in the
     * tree.
     *
     * @param index the index that specifies the column
     * @return the receiver's bounding image rectangle
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    public Rectangle getImageBounds(int index) {
        checkWidget();
        return null;
    }

    Rectangle getImageBoundsInPixels(int index) {
        if (!((DartTree) parent.getImpl()).checkData(this.getApi(), true))
            error(SWT.ERROR_WIDGET_DISPOSED);
        return null;
    }

    /**
     * Returns the receiver's parent, which must be a <code>Tree</code>.
     *
     * @return the receiver's parent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Tree getParent() {
        checkWidget();
        return parent;
    }

    /**
     * Returns the receiver's parent item, which must be a
     * <code>TreeItem</code> or null when the receiver is a
     * root.
     *
     * @return the receiver's parent item
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public TreeItem getParentItem() {
        checkWidget();
        return parentItem;
    }

    @Override
    public String getText() {
        checkWidget();
        if (!((DartTree) parent.getImpl()).checkData(this.getApi(), true))
            error(SWT.ERROR_WIDGET_DISPOSED);
        return super.getText();
    }

    /**
     * Returns the text stored at the given column index in the receiver,
     * or empty string if the text has not been set.
     *
     * @param index the column index
     * @return the text stored at the given column index in the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    public String getText(int index) {
        checkWidget();
        if (!((DartTree) parent.getImpl()).checkData(this.getApi(), true))
            error(SWT.ERROR_WIDGET_DISPOSED);
        if (index == 0)
            return getText();
        if (strings != null) {
            if (0 <= index && index < strings.length) {
                String string = strings[index];
                return string != null ? string : "";
            }
        }
        return "";
    }

    /**
     * Returns a rectangle describing the size and location
     * relative to its parent of the text at a column in the
     * tree.
     *
     * @param index the index that specifies the column
     * @return the receiver's bounding text rectangle
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.3
     */
    public Rectangle getTextBounds(int index) {
        checkWidget();
        return null;
    }

    Rectangle getTextBoundsInPixels(int index) {
        if (!((DartTree) parent.getImpl()).checkData(this.getApi(), true))
            error(SWT.ERROR_WIDGET_DISPOSED);
        return null;
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
     *    <li>ERROR_NULL_ARGUMENT - if the item is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the item has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    public int indexOf(TreeItem item) {
        checkWidget();
        if (item == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (item.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        return 0;
    }

    void redraw() {
        if (((DartTree) parent.getImpl()).currentItem == this.getApi() || !((DartControl) parent.getImpl()).getDrawing())
            return;
        /*
	* When there are no columns and the tree is not
	* full selection, redraw only the text.  This is
	* an optimization to reduce flashing.
	*/
        boolean full = (parent.style & (SWT.FULL_SELECTION | SWT.VIRTUAL)) != 0;
        if (!full) {
            full = ((DartTree) parent.getImpl()).columnCount != 0;
            if (!full) {
                if (((DartWidget) parent.getImpl()).hooks(SWT.EraseItem) || ((DartWidget) parent.getImpl()).hooks(SWT.PaintItem)) {
                    full = true;
                }
            }
        }
    }

    void redraw(int column, boolean drawText, boolean drawImage) {
        if (((DartTree) parent.getImpl()).currentItem == this.getApi() || !((DartControl) parent.getImpl()).getDrawing())
            return;
    }

    @Override
    void releaseChildren(boolean destroy) {
        if (destroy) {
        }
        super.releaseChildren(destroy);
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
        getApi().handle = 0;
        parent = null;
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        strings = null;
        images = null;
        cellBackground = cellForeground = null;
        cellFont = null;
    }

    /**
     * Removes all of the items from the receiver.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    public void removeAll() {
        checkWidget();
        /**
         * Performance optimization, switch off redraw for high amount of elements
         */
        boolean disableRedraw = ((DartTree) parent.getImpl()).cachedItemCount > 30;
        if (disableRedraw) {
            parent.setRedraw(false);
        }
        try {
        } finally {
            if (disableRedraw) {
                parent.setRedraw(true);
            }
        }
    }

    /**
     * Sets the receiver's background color to the color specified
     * by the argument, or to the default system color for the item
     * if the argument is null.
     *
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
     * @since 2.0
     */
    public void setBackground(Color color) {
        dirty();
        checkWidget();
        if (color != null && color.isDisposed()) {
            error(SWT.ERROR_INVALID_ARGUMENT);
        }
        int pixel = -1;
        if (color != null) {
            ((DartTree) parent.getImpl()).customDraw = true;
            pixel = color.handle;
        }
        if (background == pixel)
            return;
        background = pixel;
        if ((parent.style & SWT.VIRTUAL) != 0)
            cached = true;
        redraw();
        this._background = color;
    }

    /**
     * Sets the background color at the given column index in the receiver
     * to the color specified by the argument, or to the default system color for the item
     * if the argument is null.
     *
     * @param index the column index
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
     * @since 3.1
     */
    public void setBackground(int index, Color color) {
        checkWidget();
        if (color != null && color.isDisposed()) {
            error(SWT.ERROR_INVALID_ARGUMENT);
        }
        int count = Math.max(1, parent.getColumnCount());
        if (0 > index || index > count - 1)
            return;
        int pixel = -1;
        if (color != null) {
            ((DartTree) parent.getImpl()).customDraw = true;
            pixel = color.handle;
        }
        if (cellBackground == null) {
            cellBackground = new int[count];
            for (int i = 0; i < count; i++) {
                cellBackground[i] = -1;
            }
        }
        if (cellBackground[index] == pixel)
            return;
        cellBackground[index] = pixel;
        if ((parent.style & SWT.VIRTUAL) != 0)
            cached = true;
        redraw(index, true, true);
    }

    /**
     * Sets the checked state of the receiver.
     *
     * @param checked the new checked state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setChecked(boolean checked) {
        dirty();
        checkWidget();
        if ((parent.style & SWT.CHECK) == 0)
            return;
        if (checked) {
        } else {
        }
        if ((parent.style & SWT.VIRTUAL) != 0)
            cached = true;
        /*
	* Bug in Windows.  When TVM_SETITEM is used to set
	* the state image of an item inside TVN_GETDISPINFO,
	* the new state is not redrawn.  The fix is to force
	* a redraw.
	*/
        if ((parent.style & SWT.VIRTUAL) != 0) {
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
        /* Expand or collapse the item */
        ((DartTree) parent.getImpl()).ignoreExpand = true;
        ((DartTree) parent.getImpl()).ignoreExpand = false;
        this.expanded = expanded;
    }

    /**
     * Sets the font that the receiver will use to paint textual information
     * for this item to the font specified by the argument, or to the default font
     * for that kind of control if the argument is null.
     *
     * @param font the new font (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    public void setFont(Font font) {
        dirty();
        checkWidget();
        if (font != null && font.isDisposed()) {
            error(SWT.ERROR_INVALID_ARGUMENT);
        }
        Font oldFont = this.font;
        Font newFont = (font == null ? font : DartFont.win32_new(font, getNativeZoom()));
        if (oldFont == newFont)
            return;
        this.font = newFont;
        if (oldFont != null && oldFont.equals(font))
            return;
        if (font != null)
            ((DartTree) parent.getImpl()).customDraw = true;
        if ((parent.style & SWT.VIRTUAL) != 0)
            cached = true;
        /*
	* Bug in Windows.  When the font is changed for an item,
	* the bounds for the item are not updated, causing the text
	* to be clipped.  The fix is to reset the text, causing
	* Windows to compute the new bounds using the new font.
	*/
        if ((parent.style & SWT.VIRTUAL) == 0 && !cached && !((DartTree) parent.getImpl()).painted) {
            return;
        }
    }

    /**
     * Sets the font that the receiver will use to paint textual information
     * for the specified cell in this item to the font specified by the
     * argument, or to the default font for that kind of control if the
     * argument is null.
     *
     * @param index the column index
     * @param font the new font (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    public void setFont(int index, Font font) {
        dirty();
        checkWidget();
        if (font != null && font.isDisposed()) {
            error(SWT.ERROR_INVALID_ARGUMENT);
        }
        int count = Math.max(1, parent.getColumnCount());
        if (0 > index || index > count - 1)
            return;
        if (cellFont == null) {
            if (font == null)
                return;
            cellFont = new Font[count];
        }
        Font oldFont = cellFont[index];
        if (oldFont == font)
            return;
        cellFont[index] = font == null ? font : DartFont.win32_new(font, getNativeZoom());
        if (oldFont != null && oldFont.equals(font))
            return;
        if (font != null)
            ((DartTree) parent.getImpl()).customDraw = true;
        if ((parent.style & SWT.VIRTUAL) != 0)
            cached = true;
        /*
	* Bug in Windows.  When the font is changed for an item,
	* the bounds for the item are not updated, causing the text
	* to be clipped.  The fix is to reset the text, causing
	* Windows to compute the new bounds using the new font.
	*/
        if (index == 0) {
            if ((parent.style & SWT.VIRTUAL) == 0 && !cached && !((DartTree) parent.getImpl()).painted) {
                return;
            }
        } else {
            redraw(index, true, false);
        }
    }

    /**
     * Sets the receiver's foreground color to the color specified
     * by the argument, or to the default system color for the item
     * if the argument is null.
     *
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
     * @since 2.0
     */
    public void setForeground(Color color) {
        dirty();
        checkWidget();
        if (color != null && color.isDisposed()) {
            error(SWT.ERROR_INVALID_ARGUMENT);
        }
        int pixel = -1;
        if (color != null) {
            ((DartTree) parent.getImpl()).customDraw = true;
            pixel = color.handle;
        }
        if (foreground == pixel)
            return;
        foreground = pixel;
        if ((parent.style & SWT.VIRTUAL) != 0)
            cached = true;
        redraw();
        this._foreground = color;
    }

    /**
     * Sets the foreground color at the given column index in the receiver
     * to the color specified by the argument, or to the default system color for the item
     * if the argument is null.
     *
     * @param index the column index
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
     * @since 3.1
     */
    public void setForeground(int index, Color color) {
        checkWidget();
        if (color != null && color.isDisposed()) {
            error(SWT.ERROR_INVALID_ARGUMENT);
        }
        int count = Math.max(1, parent.getColumnCount());
        if (0 > index || index > count - 1)
            return;
        int pixel = -1;
        if (color != null) {
            ((DartTree) parent.getImpl()).customDraw = true;
            pixel = color.handle;
        }
        if (cellForeground == null) {
            cellForeground = new int[count];
            for (int i = 0; i < count; i++) {
                cellForeground[i] = -1;
            }
        }
        if (cellForeground[index] == pixel)
            return;
        cellForeground[index] = pixel;
        if ((parent.style & SWT.VIRTUAL) != 0)
            cached = true;
        redraw(index, true, false);
    }

    /**
     * Sets the grayed state of the checkbox for this item.  This state change
     * only applies if the Tree was created with the SWT.CHECK style.
     *
     * @param grayed the new grayed state of the checkbox
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setGrayed(boolean grayed) {
        dirty();
        checkWidget();
        if ((parent.style & SWT.CHECK) == 0)
            return;
        if (grayed) {
        } else {
        }
        if ((parent.style & SWT.VIRTUAL) != 0)
            cached = true;
        /*
	* Bug in Windows.  When TVM_SETITEM is used to set
	* the state image of an item inside TVN_GETDISPINFO,
	* the new state is not redrawn.  The fix is to force
	* a redraw.
	*/
        if ((parent.style & SWT.VIRTUAL) != 0) {
        }
    }

    /**
     * Sets the image for multiple columns in the tree.
     *
     * @param images the array of new images
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the array of images is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if one of the images has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    public void setImage(Image[] images) {
        checkWidget();
        if (images == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        for (int i = 0; i < images.length; i++) {
            setImage(i, images[i]);
        }
    }

    /**
     * Sets the receiver's image at a column.
     *
     * @param index the column index
     * @param image the new image
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    public void setImage(int index, Image image) {
        dirty();
        checkWidget();
        if (image != null && image.isDisposed()) {
            error(SWT.ERROR_INVALID_ARGUMENT);
        }
        Image oldImage = null;
        if (index == 0) {
            if (image != null && image.type == SWT.ICON) {
                if (image.equals(this.image))
                    return;
            }
            oldImage = this.image;
            super.setImage(image);
        }
        int count = Math.max(1, parent.getColumnCount());
        if (0 > index || index > count - 1)
            return;
        if (images == null && index != 0) {
            images = new Image[count];
            images[0] = image;
        }
        if (images != null) {
            if (image != null && image.type == SWT.ICON) {
                if (image.equals(images[index]))
                    return;
            }
            oldImage = images[index];
            images[index] = image;
        }
        if ((parent.style & SWT.VIRTUAL) != 0)
            cached = true;
        /* Ensure that the image list is created */
        //TODO - items that are not in column zero don't need to be in the image list
        ((DartTree) parent.getImpl()).imageIndex(image, index);
        if (index == 0) {
            if ((parent.style & SWT.VIRTUAL) == 0 && !cached && !((DartTree) parent.getImpl()).painted) {
                return;
            }
        } else {
            boolean drawText = (image == null && oldImage != null) || (image != null && oldImage == null);
            redraw(index, drawText, true);
        }
    }

    @Override
    public void setImage(Image image) {
        checkWidget();
        setImage(0, image);
    }

    /**
     * Sets the number of child items contained in the receiver.
     * <p>
     * The fastest way to insert many items is:
     * <ol>
     * <li>Use {@link Tree#setRedraw} to disable drawing during bulk insert</li>
     * <li>Collapse the parent item before inserting (gives massive improvement on Windows)</li>
     * </ol>
     *
     * @param count the number of items
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    public void setItemCount(int count) {
        checkWidget();
        count = Math.max(0, count);
        ((DartTree) parent.getImpl()).setItemCount(count, getApi().handle);
    }

    /**
     * Sets the text for multiple columns in the tree.
     * <p>
     * Note: If control characters like '\n', '\t' etc. are used
     * in the string, then the behavior is platform dependent.
     * </p>
     * @param strings the array of new strings
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the text is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    public void setText(String[] strings) {
        checkWidget();
        if (strings == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            if (string != null)
                setText(i, string);
        }
    }

    /**
     * Sets the receiver's text at a column
     * <p>
     * Note: If control characters like '\n', '\t' etc. are used
     * in the string, then the behavior is platform dependent.
     * </p>
     * @param index the column index
     * @param string the new text
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the text is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    public void setText(int index, String string) {
        dirty();
        checkWidget();
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (index == 0) {
            if (string.equals(text))
                return;
            super.setText(string);
        }
        int count = Math.max(1, parent.getColumnCount());
        if (0 > index || index > count - 1)
            return;
        if (strings == null && index != 0) {
            strings = new String[count];
            strings[0] = text;
        }
        if (strings != null) {
            if (string.equals(strings[index]))
                return;
            strings[index] = string;
        }
        if ((parent.style & SWT.VIRTUAL) != 0)
            cached = true;
        if (index == 0) {
            if ((parent.style & SWT.VIRTUAL) == 0 && !cached && !((DartTree) parent.getImpl()).painted) {
                return;
            }
        } else {
            redraw(index, true, false);
        }
    }

    @Override
    public void setText(String string) {
        checkWidget();
        setText(0, string);
    }

    /*public*/
    void sort() {
        checkWidget();
        if ((parent.style & SWT.VIRTUAL) != 0)
            return;
        ((DartTree) parent.getImpl()).sort(getApi().handle, false);
    }

    @Override
    String getNameText() {
        if ((parent.style & SWT.VIRTUAL) != 0) {
            //$NON-NLS-1$
            if (!cached)
                return "*virtual*";
        }
        return super.getNameText();
    }

    private static void handleDPIChange(Widget widget, int newZoom, float scalingFactor) {
        if (!(widget instanceof TreeItem treeItem)) {
            return;
        }
        Font font = ((DartTreeItem) treeItem.getImpl()).font;
        if (font != null) {
            treeItem.setFont(font);
        }
        Font[] cellFonts = ((DartTreeItem) treeItem.getImpl()).cellFont;
        if (cellFonts != null) {
            for (int index = 0; index < cellFonts.length; index++) {
                Font cellFont = cellFonts[index];
                cellFonts[index] = cellFont == null ? null : DartFont.win32_new(cellFont, ((DartWidget) treeItem.getImpl()).getNativeZoom());
            }
        }
    }

    Color _background;

    boolean expanded;

    Color _foreground;

    TreeItem[] items = new TreeItem[0];

    public Tree _parent() {
        return parent;
    }

    public String[] _strings() {
        return strings;
    }

    public Image[] _images() {
        return images;
    }

    public Font _font() {
        return font;
    }

    public Font[] _cellFont() {
        return cellFont;
    }

    public boolean _cached() {
        return cached;
    }

    public int _background() {
        return background;
    }

    public int _foreground() {
        return foreground;
    }

    public int[] _cellBackground() {
        return cellBackground;
    }

    public int[] _cellForeground() {
        return cellForeground;
    }

    public Color __background() {
        return _background;
    }

    public boolean _expanded() {
        return expanded;
    }

    public Color __foreground() {
        return _foreground;
    }

    public TreeItem[] _items() {
        return items;
    }

    public void updateChildItems() {
        if (items == null)
            return;
        items = java.util.Arrays.stream(items).filter(child -> child != null && !child.isDisposed()).toArray(TreeItem[]::new);
    }

    private TreeItem parentItem;

    @Override
    public void dispose() {
        Tree tmpParent = parent;
        TreeItem tmpParentItem = parentItem;
        super.dispose();
        if (tmpParent.getImpl() instanceof DartTree p) {
            p.updateChildItems();
        }
        if (tmpParentItem.getImpl() instanceof DartTreeItem p) {
            p.updateChildItems();
        }
    }

    public DartTreeItem(Tree parent, int style, TreeItem parentItem, int hInsertAfter, int hItem, TreeItem api) {
        super(parent, style, api);
        this.parent = parent;
        this.parentItem = parentItem;
        createItem(this.getApi(), parentItem, hInsertAfter);
    }

    void createItem(TreeItem item, TreeItem parentItem, int index) {
        int count;
        TreeItem[] items;
        if (parentItem != null) {
            count = ((DartTreeItem) parentItem.getImpl()).getItemCount();
            items = ((DartTreeItem) parentItem.getImpl()).items;
        } else {
            count = ((DartTree) parent.getImpl()).getItemCount();
            items = ((DartTree) parent.getImpl()).items;
        }
        if (index == -1)
            index = count;
        if (!(0 <= index && index <= count))
            error(SWT.ERROR_INVALID_RANGE);
        if (count == items.length) {
            TreeItem[] newItems = new TreeItem[items.length + 4];
            System.arraycopy(items, 0, newItems, 0, items.length);
            items = newItems;
            if (parentItem != null) {
                ((DartTreeItem) parentItem.getImpl()).items = items;
            } else {
                ((DartTree) parent.getImpl()).items = items;
            }
        }
        System.arraycopy(items, index, items, index + 1, count++ - index);
        items[index] = item;
        ((DartTreeItem) item.getImpl()).items = new TreeItem[4];
        if (parentItem != null) {
            ((DartTreeItem) parentItem.getImpl()).setItemCount(count);
        } else {
            ((DartTree) parent.getImpl()).setItemCount(count);
        }
        if (parentItem == null && ((DartTree) parent.getImpl()).getItemCount() == 1) {
            Event event = new Event();
            event.detail = 0;
            parent.getImpl().sendEvent(SWT.EmptinessChanged, event);
        }
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

    public TreeItem getApi() {
        if (api == null)
            api = TreeItem.createApi(this);
        return (TreeItem) api;
    }

    public VTreeItem getValue() {
        if (value == null)
            value = new VTreeItem(this);
        return (VTreeItem) value;
    }
}
