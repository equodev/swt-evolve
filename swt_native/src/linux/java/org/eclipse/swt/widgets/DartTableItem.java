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
 * that represents an item in a table.
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
 * @see <a href="http://www.eclipse.org/swt/snippets/#table">Table, TableItem, TableColumn snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartTableItem extends DartItem implements ITableItem {

    Table parent;

    Font font;

    Font[] cellFont;

    String[] strings;

    boolean cached, grayed, settingData;

    /**
     * Constructs a new instance of this class given its parent
     * (which must be a <code>Table</code>), a style value
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
     * @see SWT
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartTableItem(Table parent, int style, int index, TableItem api) {
        this(parent, style, index, true, api);
    }

    /**
     * Constructs a new instance of this class given its parent
     * (which must be a <code>Table</code>) and a style value
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
     * @see SWT
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartTableItem(Table parent, int style, TableItem api) {
        this(parent, style, checkNull(parent).getItemCount(), true, api);
    }

    DartTableItem(Table parent, int style, int index, boolean create, TableItem api) {
        super(parent, style, api);
        this.parent = parent;
        if (create) {
            ((DartTable) parent.getImpl()).createItem(this.getApi(), index);
        } else {
        }
    }

    static Table checkNull(Table control) {
        if (control == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        return control;
    }

    Color _getBackground() {
        long[] ptr = new long[1];
        if (ptr[0] == 0)
            return parent.getBackground();
        return null;
    }

    Color _getBackground(int index) {
        int count = Math.max(1, ((DartTable) parent.getImpl()).columnCount);
        if (0 > index || index > count - 1)
            return _getBackground();
        long[] ptr = new long[1];
        if (ptr[0] == 0)
            return _getBackground();
        return null;
    }

    boolean _getChecked() {
        int[] ptr = new int[1];
        return ptr[0] != 0;
    }

    Color _getForeground() {
        long[] ptr = new long[1];
        if (ptr[0] == 0)
            return parent.getForeground();
        return null;
    }

    Color _getForeground(int index) {
        int count = Math.max(1, ((DartTable) parent.getImpl()).columnCount);
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
        if (ptr[0] == 0)
            return "";
        return null;
    }

    @Override
    public void checkSubclass() {
        if (!isValidSubclass())
            error(SWT.ERROR_INVALID_SUBCLASS);
    }

    void clear() {
        if (((DartTable) parent.getImpl()).currentItem == this.getApi())
            return;
        if (cached || (parent.style & SWT.VIRTUAL) == 0) {
            /* the columns before FOREGROUND_COLUMN contain int values, subsequent columns contain pointers */
            for (int i = DartTable.CHECKED_COLUMN; i < DartTable.FOREGROUND_COLUMN; i++) {
            }
        }
        cached = false;
        font = null;
        cellFont = null;
        strings = null;
    }

    @Override
    void destroyWidget() {
        ((DartTable) parent.getImpl()).destroyItem(this.getApi());
        releaseHandle();
    }

    @Override
    public void dispose() {
        // Workaround to Bug489751, avoid selecting next node when selected node is disposed.
        Table tmpParent = null;
        if (parent != null && parent.getItemCount() > 0 && parent.getSelectionCount() == 0) {
            tmpParent = parent;
        }
        super.dispose();
        if (tmpParent != null && !tmpParent.isDisposed())
            tmpParent.deselectAll();
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
        if (!((DartTable) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        return _getBackground();
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
     *
     * @since 3.2
     */
    public Rectangle getBounds() {
        return getBoundsinPixels();
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
     *
     * @since 3.105
     */
    Rectangle getBoundsinPixels() {
        // TODO fully test on early and later versions of GTK
        // shifted a bit too far right on later versions of GTK - however, old Tree also had this problem
        checkWidget();
        if (!((DartTable) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        ((DartTable) parent.getImpl()).ignoreSize = true;
        ((DartTable) parent.getImpl()).ignoreSize = false;
        if (((DartTable) parent.getImpl()).columnCount > 0) {
        }
        return null;
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
     * @since 3.0
     */
    public Color getBackground(int index) {
        checkWidget();
        if (!((DartTable) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        return _getBackground(index);
    }

    /**
     * Returns a rectangle describing the receiver's size and location
     * relative to its parent at a column in the table.
     *
     * @param index the index that specifies the column
     * @return the receiver's bounding column rectangle
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Rectangle getBounds(int index) {
        checkWidget();
        if (!((DartTable) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        long column = 0;
        if (index >= 0 && index < ((DartTable) parent.getImpl()).columnCount) {
            column = ((DartTable) parent.getImpl()).columns[index].handle;
        } else {
        }
        if (column == 0)
            return new Rectangle(0, 0, 0, 0);
        ((DartTable) parent.getImpl()).ignoreSize = true;
        ((DartTable) parent.getImpl()).ignoreSize = false;
        if (index == 0 && (parent.style & SWT.CHECK) != 0) {
        }
        return null;
    }

    /**
     * Returns <code>true</code> if the receiver is checked,
     * and false otherwise.  When the parent does not have
     * the <code>CHECK</code> style, return false.
     *
     * @return the checked state of the checkbox
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean getChecked() {
        checkWidget();
        if (!((DartTable) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        if ((parent.style & SWT.CHECK) == 0)
            return false;
        return _getChecked();
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
        if (!((DartTable) parent.getImpl()).checkData(this.getApi()))
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
     * @since 3.0
     */
    public Font getFont(int index) {
        checkWidget();
        if (!((DartTable) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        int count = Math.max(1, ((DartTable) parent.getImpl()).columnCount);
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
        if (!((DartTable) parent.getImpl()).checkData(this.getApi()))
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
     * @since 3.0
     */
    public Color getForeground(int index) {
        checkWidget();
        if (!((DartTable) parent.getImpl()).checkData(this.getApi()))
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
        if (!((DartTable) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        if ((parent.style & SWT.CHECK) == 0)
            return false;
        return grayed;
    }

    @Override
    public Image getImage() {
        checkWidget();
        if (!((DartTable) parent.getImpl()).checkData(this.getApi()))
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
     */
    public Image getImage(int index) {
        checkWidget();
        if (!((DartTable) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        return _getImage(index);
    }

    /**
     * Returns a rectangle describing the size and location
     * relative to its parent of an image at a column in the
     * table.  An empty rectangle is returned if index exceeds
     * the index of the table's last column.
     *
     * @param index the index that specifies the column
     * @return the receiver's bounding image rectangle
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Rectangle getImageBounds(int index) {
        checkWidget();
        if (!((DartTable) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        long column = 0;
        if (index >= 0 && index < ((DartTable) parent.getImpl()).columnCount) {
            column = ((DartTable) parent.getImpl()).columns[index].handle;
        } else {
        }
        if (column == 0)
            return new Rectangle(0, 0, 0, 0);
        long pixbufRenderer = ((DartTable) parent.getImpl()).getPixbufRenderer(column);
        if (pixbufRenderer == 0)
            return new Rectangle(0, 0, 0, 0);
        int[] x = new int[1];
        /*
	 * Feature in GTK. When a pixbufRenderer has size of 0x0, gtk_tree_view_column_cell_get_position
	 * returns a position of 0 as well. This causes offset issues meaning that images/widgets/etc.
	 * can be placed over the text. We need to account for the base case of a pixbufRenderer that has
	 * yet to be sized, as per Bug 469277 & 476419. NOTE: this change has been ported to Tables since Tables/Trees both
	 * use the same underlying GTK structure.
	 */
        if (((DartTable) parent.getImpl()).pixbufSizeSet) {
            if (x[0] > 0) {
            }
        } else {
            /*
		 * If the size of the pixbufRenderer hasn't been set, we need to take into account the
		 * position of the textRenderer, to ensure images/widgets/etc. aren't placed over the TableItem's
		 * text.
		 */
            long textRenderer = ((DartTable) parent.getImpl()).getTextRenderer(column);
            if (textRenderer == 0)
                return new Rectangle(0, 0, 0, 0);
        }
        return null;
    }

    /**
     * Gets the image indent.
     *
     * @return the indent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getImageIndent() {
        checkWidget();
        if (!((DartTable) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        /* Image indent is not supported on GTK */
        return 0;
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
     * Returns the receiver's parent, which must be a <code>Table</code>.
     *
     * @return the receiver's parent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Table getParent() {
        checkWidget();
        return parent;
    }

    @Override
    public String getText() {
        checkWidget();
        if (!((DartTable) parent.getImpl()).checkData(this.getApi()))
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
     */
    public String getText(int index) {
        checkWidget();
        if (!((DartTable) parent.getImpl()).checkData(this.getApi()))
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
     * table.  An empty rectangle is returned if index exceeds
     * the index of the table's last column.
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
        if (!((DartTable) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        int count = Math.max(1, parent.getColumnCount());
        if (0 > index || index > count - 1)
            return new Rectangle(0, 0, 0, 0);
        // TODO fully test on early and later versions of GTK
        long column = 0;
        if (index >= 0 && index < ((DartTable) parent.getImpl()).columnCount) {
            column = ((DartTable) parent.getImpl()).columns[index].handle;
        } else {
        }
        if (column == 0)
            return new Rectangle(0, 0, 0, 0);
        long textRenderer = ((DartTable) parent.getImpl()).getTextRenderer(column);
        long pixbufRenderer = ((DartTable) parent.getImpl()).getPixbufRenderer(column);
        if (textRenderer == 0 || pixbufRenderer == 0)
            return new Rectangle(0, 0, 0, 0);
        int[] x = new int[1];
        ((DartTable) parent.getImpl()).ignoreSize = true;
        ((DartTable) parent.getImpl()).ignoreSize = false;
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
        if (((DartTable) parent.getImpl()).columnCount > 0) {
        }
        return null;
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
     * @since 3.0
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
        if (color != null) {
            boolean customDraw = (((DartTable) parent.getImpl()).columnCount == 0) ? ((DartTable) parent.getImpl()).firstCustomDraw : ((DartTableColumn) ((DartTable) parent.getImpl()).columns[index].getImpl()).customDraw;
            if (!customDraw) {
                if ((parent.style & SWT.VIRTUAL) == 0) {
                    long column = 0;
                    if (((DartTable) parent.getImpl()).columnCount > 0) {
                        column = ((DartTable) parent.getImpl()).columns[index].handle;
                    } else {
                    }
                    if (column == 0)
                        return;
                }
                if (((DartTable) parent.getImpl()).columnCount == 0) {
                    ((DartTable) parent.getImpl()).firstCustomDraw = true;
                } else {
                    ((DartTableColumn) ((DartTable) parent.getImpl()).columns[index].getImpl()).customDraw = true;
                }
            }
        }
    }

    /**
     * Sets the checked state of the checkbox for this item.  This state change
     * only applies if the Table was created with the SWT.CHECK style.
     *
     * @param checked the new checked state of the checkbox
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
     * @since 3.0
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
            boolean customDraw = (((DartTable) parent.getImpl()).columnCount == 0) ? ((DartTable) parent.getImpl()).firstCustomDraw : ((DartTableColumn) ((DartTable) parent.getImpl()).columns[index].getImpl()).customDraw;
            if (!customDraw) {
                if ((parent.style & SWT.VIRTUAL) == 0) {
                    long column = 0;
                    if (((DartTable) parent.getImpl()).columnCount > 0) {
                        column = ((DartTable) parent.getImpl()).columns[index].handle;
                    } else {
                    }
                    if (column == 0)
                        return;
                }
                if (((DartTable) parent.getImpl()).columnCount == 0) {
                    ((DartTable) parent.getImpl()).firstCustomDraw = true;
                } else {
                    ((DartTableColumn) ((DartTable) parent.getImpl()).columns[index].getImpl()).customDraw = true;
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
     * @since 3.0
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
        if (color != null) {
            boolean customDraw = (((DartTable) parent.getImpl()).columnCount == 0) ? ((DartTable) parent.getImpl()).firstCustomDraw : ((DartTableColumn) ((DartTable) parent.getImpl()).columns[index].getImpl()).customDraw;
            if (!customDraw) {
                if ((parent.style & SWT.VIRTUAL) == 0) {
                    long column = 0;
                    if (((DartTable) parent.getImpl()).columnCount > 0) {
                        column = ((DartTable) parent.getImpl()).columns[index].handle;
                    } else {
                    }
                    if (column == 0)
                        return;
                }
                if (((DartTable) parent.getImpl()).columnCount == 0) {
                    ((DartTable) parent.getImpl()).firstCustomDraw = true;
                } else {
                    ((DartTableColumn) ((DartTable) parent.getImpl()).columns[index].getImpl()).customDraw = true;
                }
            }
        }
    }

    /**
     * Sets the grayed state of the checkbox for this item.  This state change
     * only applies if the Table was created with the SWT.CHECK style.
     *
     * @param grayed the new grayed state of the checkbox;
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
        if (!((DartTable) parent.getImpl()).pixbufSizeSet) {
            if (image != null) {
                int iWidth = image.getBounds().width;
                int iHeight = image.getBounds().height;
                if (iWidth > currentWidth[0] || iHeight > currentHeight[0]) {
                    ((DartTable) parent.getImpl()).pixbufHeight = iHeight;
                    ((DartTable) parent.getImpl()).pixbufWidth = iWidth;
                    ((DartTable) parent.getImpl()).pixbufSizeSet = true;
                }
            }
        } else {
            /*
		 * We check to see if the cached value is greater than the size of the pixbufRenderer.
		 * If it is, then we change the size of the pixbufRenderer accordingly.
		 * Bug 489025: There is a corner case where the below is triggered when current(Width|Height) is -1,
		 * which results in icons being set to 0. Fix is to compare only positive sizes.
		 */
            if (((DartTable) parent.getImpl()).pixbufWidth > Math.max(currentWidth[0], 0) || ((DartTable) parent.getImpl()).pixbufHeight > Math.max(currentHeight[0], 0)) {
            }
        }
        /*
	 * Bug 573633: gtk_list_store_set() will reference the handle. So we unref the pixbuf here,
	 * and leave the destruction of the handle to be done later on by the GTK+ tree.
	 */
        if (pixbuf != 0) {
        }
        cached = true;
        /*
	 * Bug 465056: single column Tables have a very small initial width.
	 * Fix: when text or an image is set for a Table, compute its
	 * width and see if it's larger than the maximum of the previous widths.
	 */
        if (((DartTable) parent.getImpl()).columnCount == 0) {
        }
    }

    @Override
    public void setImage(Image image) {
        checkWidget();
        setImage(0, image);
    }

    /**
     * Sets the image for multiple columns in the table.
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
     * Sets the indent of the first column's image, expressed in terms of the image's width.
     *
     * @param indent the new indent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @deprecated this functionality is not supported on most platforms
     */
    @Deprecated
    public void setImageIndent(int indent) {
        int newValue = indent;
        if (!java.util.Objects.equals(this.imageIndent, newValue)) {
            dirty();
        }
        checkWidget();
        if (indent < 0)
            return;
        this.imageIndent = newValue;
        /* Image indent is not supported on GTK */
        cached = true;
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
        if (((DartTable) parent.getImpl()).columnCount == 0) {
        }
    }

    @Override
    public void setText(String string) {
        checkWidget();
        setText(0, string);
    }

    /**
     * Sets the text for multiple columns in the table.
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

    int imageIndent;

    public Table _parent() {
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

    public int _imageIndent() {
        return imageIndent;
    }

    public Image[] _images() {
        return null;
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

    public TableItem getApi() {
        if (api == null)
            api = TableItem.createApi(this);
        return (TableItem) api;
    }

    public VTableItem getValue() {
        if (value == null)
            value = new VTableItem(this);
        return (VTableItem) value;
    }
}
