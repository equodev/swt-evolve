/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2025 IBM Corporation and others.
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
import org.eclipse.swt.internal.cairo.*;
import java.util.Objects;
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

    Font font;

    Font[] cellFont;

    String[] strings;

    boolean cached, grayed, isExpanded, updated, settingData;

    static final int EXPANDER_EXTRA_PADDING = 4;

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
        this(checkNull(parent), 0, style, -1, 0, api);
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
        this(checkNull(parent), 0, style, checkIndex(index), 0, api);
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

    DartTreeItem(Tree parent, long parentIter, int style, int index, long iter, TreeItem api) {
        super(parent, style, api);
        this.parent = parent;
        if (iter == 0) {
            ((DartTree) parent.getImpl()).createItem(this.getApi(), parentIter, index);
        } else {
            assert getApi().handle == 0;
            if (getApi().handle == 0)
                error(SWT.ERROR_NO_HANDLES);
        }
    }

    static int checkIndex(int index) {
        if (index < 0)
            SWT.error(SWT.ERROR_INVALID_RANGE);
        return index;
    }

    static TreeItem checkNull(TreeItem item) {
        if (item == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        return item;
    }

    static Tree checkNull(Tree control) {
        if (control == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        return control;
    }

    @Override
    public void checkSubclass() {
        if (!isValidSubclass())
            error(SWT.ERROR_INVALID_SUBCLASS);
    }

    Color _getBackground() {
        long[] ptr = new long[1];
        if (ptr[0] == 0)
            return parent.getBackground();
        return null;
    }

    Color _getBackground(int index) {
        int count = Math.max(1, ((DartTree) parent.getImpl()).columnCount);
        if (0 > index || index > count - 1)
            return _getBackground();
        long[] ptr = new long[1];
        if (ptr[0] == 0)
            return _getBackground();
        return null;
    }

    boolean _getChecked() {
        return true;
    }

    Color _getForeground() {
        long[] ptr = new long[1];
        if (ptr[0] == 0)
            return parent.getForeground();
        return null;
    }

    Color _getForeground(int index) {
        int count = Math.max(1, ((DartTree) parent.getImpl()).columnCount);
        if (0 > index || index > count - 1)
            return _getForeground();
        long[] ptr = new long[1];
        if (ptr[0] == 0)
            return _getForeground();
        return null;
    }

    Image _getImage(int index) {
        int count = Math.max(1, parent.getColumnCount());
        if (0 > index || index > count - 1)
            return null;
        long[] surfaceHandle = new long[1];
        if (surfaceHandle[0] == 0)
            return null;
        return null;
    }

    String _getText(int index) {
        int count = Math.max(1, parent.getColumnCount());
        if (0 > index || index > count - 1)
            return "";
        long[] ptr = new long[1];
        //$NON-NLS-1$
        if (ptr[0] == 0)
            return "";
        return null;
    }

    void clear() {
        if (((DartTree) parent.getImpl()).currentItem == this.getApi())
            return;
        if (cached || (parent.style & SWT.VIRTUAL) == 0) {
            /* the columns before FOREGROUND_COLUMN contain int values, subsequent columns contain pointers */
            for (int i = DartTree.CHECKED_COLUMN; i < DartTree.FOREGROUND_COLUMN; i++) {
            }
        }
        cached = false;
        font = null;
        strings = null;
        cellFont = null;
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
        ((DartTree) parent.getImpl()).clear(getApi().handle, index, all);
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
        ((DartTree) parent.getImpl()).clearAll(all, getApi().handle);
    }

    @Override
    void destroyWidget() {
        ((DartTree) parent.getImpl()).releaseItem(this.getApi(), false);
        ((DartTree) parent.getImpl()).destroyItem(this.getApi());
        releaseHandle();
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
        if (!((DartTree) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        return _getBackground();
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
        if (!((DartTree) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        return _getBackground(index);
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
        // TODO fully test on early and later versions of GTK
        if (!((DartTree) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        long column = 0;
        if (index >= 0 && index < ((DartTree) parent.getImpl()).columnCount) {
            column = ((DartTree) parent.getImpl()).columns[index].handle;
        } else {
        }
        if (column == 0)
            return new Rectangle(0, 0, 0, 0);
        if (index == 0 && (parent.style & SWT.CHECK) != 0) {
        }
        return new Rectangle(0, 0, 0, 0);
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
        // TODO fully test on early and later versions of GTK
        // shifted a bit too far right on later versions of GTK - however, old Tree also had this problem
        if (!((DartTree) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        ((DartTree) parent.getImpl()).ignoreSize = true;
        ((DartTree) parent.getImpl()).ignoreSize = false;
        if (((DartTree) parent.getImpl()).columnCount > 0) {
        }
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
        if (!((DartTree) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        if ((parent.style & SWT.CHECK) == 0)
            return false;
        return _getChecked();
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
        return this.isExpanded;
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
        if (!((DartTree) parent.getImpl()).checkData(this.getApi()))
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
        if (!((DartTree) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        int count = Math.max(1, ((DartTree) parent.getImpl()).columnCount);
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
        if (!((DartTree) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        return _getForeground();
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
        if (!((DartTree) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        return _getForeground(index);
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
        if (!((DartTree) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        if ((parent.style & SWT.CHECK) == 0)
            return false;
        return grayed;
    }

    @Override
    public Image getImage() {
        checkWidget();
        if (!((DartTree) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        return getImage(0);
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
        if (!((DartTree) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        return _getImage(index);
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
        // TODO fully test on early and later versions of GTK
        if (!((DartTree) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        long column = 0;
        if (index >= 0 && index < parent.getColumnCount()) {
            column = ((DartTree) parent.getImpl()).columns[index].handle;
        } else {
        }
        if (column == 0)
            return new Rectangle(0, 0, 0, 0);
        long pixbufRenderer = ((DartTree) parent.getImpl()).getPixbufRenderer(column);
        if (pixbufRenderer == 0)
            return new Rectangle(0, 0, 0, 0);
        /*
	 * Feature in GTK. When a pixbufRenderer has size of 0x0, gtk_tree_view_column_cell_get_position
	 * returns a position of 0 as well. This causes offset issues meaning that images/widgets/etc.
	 * can be placed over the text. We need to account for the base case of a pixbufRenderer that has
	 * yet to be sized, as per Bug 469277 & 476419. NOTE: this change has been ported to Tables since Tables/Trees both
	 * use the same underlying GTK structure.
	 */
        int[] x = new int[1];
        if (((DartTree) parent.getImpl()).pixbufSizeSet) {
            if (x[0] > 0) {
            }
        } else {
            /*
		 * If the size of the pixbufRenderer hasn't been set, we need to take into account the
		 * position of the textRenderer, to ensure images/widgets/etc. aren't placed over the TreeItem's
		 * text.
		 */
            long textRenderer = ((DartTree) parent.getImpl()).getTextRenderer(column);
            if (textRenderer == 0)
                return new Rectangle(0, 0, 0, 0);
        }
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
    String getNameText() {
        if ((parent.style & SWT.VIRTUAL) != 0) {
            //$NON-NLS-1$
            if (!cached)
                return "*virtual*";
        }
        return super.getNameText();
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
        if (!((DartTree) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        return getText(0);
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
        if (!((DartTree) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        if (strings != null) {
            if (0 <= index && index < strings.length) {
                String string = strings[index];
                return string != null ? string : "";
            }
        }
        return _getText(index);
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
        if (!((DartTree) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        int count = Math.max(1, parent.getColumnCount());
        if (0 > index || index > count - 1)
            return new Rectangle(0, 0, 0, 0);
        // TODO fully test on early and later versions of GTK
        long column = 0;
        if (index >= 0 && index < ((DartTree) parent.getImpl()).columnCount) {
            column = ((DartTree) parent.getImpl()).columns[index].handle;
        } else {
        }
        if (column == 0)
            return new Rectangle(0, 0, 0, 0);
        long textRenderer = ((DartTree) parent.getImpl()).getTextRenderer(column);
        long pixbufRenderer = ((DartTree) parent.getImpl()).getPixbufRenderer(column);
        if (textRenderer == 0 || pixbufRenderer == 0)
            return new Rectangle(0, 0, 0, 0);
        int[] x = new int[1];
        ((DartTree) parent.getImpl()).ignoreSize = true;
        ((DartTree) parent.getImpl()).ignoreSize = false;
        /*
	 * Fix for Eclipse bug 476562, we need to re-adjust the bounds for the text
	 * when the separator value is less than the width of the image. Previously
	 * images larger than 16px in width would be cut off on the right side.
	 * NOTE: this change has been ported to Tables since Tables/Trees both use the
	 * same underlying GTK structure.
	 */
        Image image = _getImage(index);
        int imageWidth = 0;
        if (image != null) {
            imageWidth = image.getBounds().width;
        }
        if (x[0] < imageWidth) {
        } else {
        }
        if (((DartTree) parent.getImpl()).columnCount > 0) {
        }
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
        int index = -1;
        boolean isParent = false;
        if (!isParent)
            return index;
        return index;
    }

    @Override
    void releaseChildren(boolean destroy) {
        if (destroy) {
            ((DartTree) parent.getImpl()).releaseItems(getApi().handle);
        }
        super.releaseChildren(destroy);
    }

    @Override
    void releaseHandle() {
        getApi().handle = 0;
        super.releaseHandle();
        parent = null;
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        font = null;
        cellFont = null;
        strings = null;
    }

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
        Color newValue = color;
        if (!java.util.Objects.equals(this.background, newValue)) {
            dirty();
        }
        checkWidget();
        if (color != null && color.isDisposed()) {
            error(SWT.ERROR_INVALID_ARGUMENT);
        }
        if (_getBackground().equals(color))
            return;
        this.background = newValue;
        cached = true;
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
        if (_getBackground(index).equals(color))
            return;
        int count = Math.max(1, parent.getColumnCount());
        if (0 > index || index > count - 1)
            return;
        cached = true;
        updated = true;
        if (color != null) {
            boolean customDraw = (((DartTree) parent.getImpl()).columnCount == 0) ? ((DartTree) parent.getImpl()).firstCustomDraw : ((DartTreeColumn) ((DartTree) parent.getImpl()).columns[index].getImpl()).customDraw;
            if (!customDraw) {
                if ((parent.style & SWT.VIRTUAL) == 0) {
                    long column = 0;
                    if (((DartTree) parent.getImpl()).columnCount > 0) {
                        column = ((DartTree) parent.getImpl()).columns[index].handle;
                    } else {
                    }
                    if (column == 0)
                        return;
                }
                if (((DartTree) parent.getImpl()).columnCount == 0) {
                    ((DartTree) parent.getImpl()).firstCustomDraw = true;
                } else {
                    ((DartTreeColumn) ((DartTree) parent.getImpl()).columns[index].getImpl()).customDraw = true;
                }
            }
        }
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
        boolean newValue = checked;
        if (!java.util.Objects.equals(this.checked, newValue)) {
            dirty();
        }
        checkWidget();
        if ((parent.style & SWT.CHECK) == 0)
            return;
        if (_getChecked() == checked)
            return;
        this.checked = newValue;
        cached = true;
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
        if (!java.util.Objects.equals(this.isExpanded, expanded)) {
            dirty();
        }
        isExpanded = expanded;
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
        font = GraphicsUtils.copyFont(font);
        checkWidget();
        if (!java.util.Objects.equals(this.font, font)) {
            dirty();
        }
        if (font != null && font.isDisposed()) {
            error(SWT.ERROR_INVALID_ARGUMENT);
        }
        Font oldFont = this.font;
        if (oldFont == font)
            return;
        this.font = font;
        if (oldFont != null && oldFont.equals(font))
            return;
        cached = true;
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
        cellFont[index] = font;
        if (oldFont != null && oldFont.equals(font))
            return;
        cached = true;
        if (font != null) {
            boolean customDraw = (((DartTree) parent.getImpl()).columnCount == 0) ? ((DartTree) parent.getImpl()).firstCustomDraw : ((DartTreeColumn) ((DartTree) parent.getImpl()).columns[index].getImpl()).customDraw;
            if (!customDraw) {
                if ((parent.style & SWT.VIRTUAL) == 0) {
                    long column = 0;
                    if (((DartTree) parent.getImpl()).columnCount > 0) {
                        column = ((DartTree) parent.getImpl()).columns[index].handle;
                    } else {
                    }
                    if (column == 0)
                        return;
                }
                if (((DartTree) parent.getImpl()).columnCount == 0) {
                    ((DartTree) parent.getImpl()).firstCustomDraw = true;
                } else {
                    ((DartTreeColumn) ((DartTree) parent.getImpl()).columns[index].getImpl()).customDraw = true;
                }
            }
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
        Color newValue = color;
        if (!java.util.Objects.equals(this.foreground, newValue)) {
            dirty();
        }
        checkWidget();
        if (color != null && color.isDisposed()) {
            error(SWT.ERROR_INVALID_ARGUMENT);
        }
        if (_getForeground().equals(color))
            return;
        this.foreground = newValue;
        cached = true;
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
        if (_getForeground(index).equals(color))
            return;
        int count = Math.max(1, parent.getColumnCount());
        if (0 > index || index > count - 1)
            return;
        cached = true;
        updated = true;
        if (color != null) {
            boolean customDraw = (((DartTree) parent.getImpl()).columnCount == 0) ? ((DartTree) parent.getImpl()).firstCustomDraw : ((DartTreeColumn) ((DartTree) parent.getImpl()).columns[index].getImpl()).customDraw;
            if (!customDraw) {
                if ((parent.style & SWT.VIRTUAL) == 0) {
                    long column = 0;
                    if (((DartTree) parent.getImpl()).columnCount > 0) {
                        column = ((DartTree) parent.getImpl()).columns[index].handle;
                    } else {
                    }
                    if (column == 0)
                        return;
                }
                if (((DartTree) parent.getImpl()).columnCount == 0) {
                    ((DartTree) parent.getImpl()).firstCustomDraw = true;
                } else {
                    ((DartTreeColumn) ((DartTree) parent.getImpl()).columns[index].getImpl()).customDraw = true;
                }
            }
        }
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
        checkWidget();
        if (!java.util.Objects.equals(this.grayed, grayed)) {
            dirty();
        }
        if ((parent.style & SWT.CHECK) == 0)
            return;
        if (this.grayed == grayed)
            return;
        this.grayed = grayed;
        cached = true;
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
        if (image != null && image.type == SWT.ICON) {
            if (image.equals(_getImage(index)))
                return;
        }
        int count = Math.max(1, parent.getColumnCount());
        if (0 > index || index > count - 1)
            return;
        long pixbuf = 0;
        if (image != null) {
            // When we create a blank image surface gets created with dimensions 0, 0.
        }
        int[] currentWidth = new int[1];
        int[] currentHeight = new int[1];
        if (!((DartTree) parent.getImpl()).pixbufSizeSet) {
            if (image != null) {
                int iWidth = image.getBounds().width;
                int iHeight = image.getBounds().height;
                if (iWidth > currentWidth[0] || iHeight > currentHeight[0]) {
                    ((DartTree) parent.getImpl()).pixbufSizeSet = true;
                    ((DartTree) parent.getImpl()).pixbufHeight = iHeight;
                    ((DartTree) parent.getImpl()).pixbufWidth = iWidth;
                    /*
				 * Feature in GTK: a Tree with the style SWT.VIRTUAL has
				 * fixed-height-mode enabled. This will limit the size of
				 * any cells, including renderers. In order to prevent
				 * images from disappearing/being cropped, we re-create
				 * the renderers when the first image is set. Fix for
				 * bug 480261.
				 */
                    if ((parent.style & SWT.VIRTUAL) != 0) {
                    }
                }
            }
        } else {
            /*
		 * Bug 483112: We check to see if the cached value is greater than the size of the pixbufRenderer.
		 * If it is, then we change the size of the pixbufRenderer accordingly.
		 * Bug 489025: There is a corner case where the below is triggered when current(Width|Height) is -1,
		 * which results in icons being set to 0. Fix is to compare only positive sizes.
		 */
            if (((DartTree) parent.getImpl()).pixbufWidth > Math.max(currentWidth[0], 0) || ((DartTree) parent.getImpl()).pixbufHeight > Math.max(currentHeight[0], 0)) {
            }
        }
        /*
	 * Bug 573633: gtk_tree_store_set() will reference the handle. So we unref the pixbuf here,
	 * and leave the destruction of the handle to be done later on by the GTK+ tree.
	 */
        if (pixbuf != 0) {
        }
        cached = true;
        updated = true;
    }

    @Override
    public void setImage(Image image) {
        checkWidget();
        setImage(0, image);
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
        ((DartTree) parent.getImpl()).setItemCount(getApi().handle, count);
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
        if (strings == null) {
            if (_getText(index).equals(string))
                return;
        } else if (getText(index).equals(string))
            return;
        int count = Math.max(1, parent.getColumnCount());
        if (0 > index || index > count - 1)
            return;
        if (0 <= index && index < count) {
            if (strings == null)
                strings = new String[count];
            if (string.equals(strings[index]))
                return;
            strings[index] = string;
        }
        if ((string != null) && (string.length() > TEXT_LIMIT)) {
            string = string.substring(0, TEXT_LIMIT - ELLIPSIS.length()) + ELLIPSIS;
        }
        cached = true;
        updated = true;
    }

    @Override
    public void setText(String string) {
        checkWidget();
        setText(0, string);
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

    Color background;

    boolean checked;

    Color foreground;

    TreeItem[] items = new TreeItem[0];

    public Tree _parent() {
        return parent;
    }

    public Font _font() {
        return font;
    }

    public Font[] _cellFont() {
        return cellFont;
    }

    public String[] _strings() {
        return strings;
    }

    public boolean _cached() {
        return cached;
    }

    public boolean _grayed() {
        return grayed;
    }

    public boolean _isExpanded() {
        return isExpanded;
    }

    public boolean _updated() {
        return updated;
    }

    public boolean _settingData() {
        return settingData;
    }

    public Color _background() {
        return background;
    }

    public boolean _checked() {
        return checked;
    }

    public Color _foreground() {
        return foreground;
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

    public DartTreeItem(Tree parent, int style, TreeItem parentItem, int hInsertAfter, int hItem, TreeItem api) {
        super(parent, style, api);
        this.parent = parent;
        this.parentItem = parentItem;
        TreeHelper.createItem(this.getApi(), parentItem, hInsertAfter, parent);
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
