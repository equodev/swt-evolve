/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2019 IBM Corporation and others.
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
import org.eclipse.swt.internal.gtk.*;
import org.eclipse.swt.internal.gtk3.*;
import org.eclipse.swt.internal.gtk4.*;
import java.util.WeakHashMap;

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
public class TableItem extends Item {

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
    public TableItem(Table parent, int style, int index) {
        this(new SWTTableItem((SWTTable) parent.delegate, style, index));
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
    public TableItem(Table parent, int style) {
        this(new SWTTableItem((SWTTable) parent.delegate, style));
    }

    TableItem(Table parent, int style, int index, boolean create) {
        this(new SWTTableItem((SWTTable) parent.delegate, style, index, create));
    }

    @Override
    public void dispose() {
        ((ITableItem) this.delegate).dispose();
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
        return ((ITableItem) this.delegate).getBackground();
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
        return ((ITableItem) this.delegate).getBounds();
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
        return ((ITableItem) this.delegate).getBackground(index);
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
        return ((ITableItem) this.delegate).getBounds(index);
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
        return ((ITableItem) this.delegate).getChecked();
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
        return ((ITableItem) this.delegate).getFont();
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
        return ((ITableItem) this.delegate).getFont(index);
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
        return ((ITableItem) this.delegate).getForeground();
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
        return ((ITableItem) this.delegate).getForeground(index);
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
        return ((ITableItem) this.delegate).getGrayed();
    }

    @Override
    public Image getImage() {
        return ((ITableItem) this.delegate).getImage();
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
        return ((ITableItem) this.delegate).getImage(index);
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
        return ((ITableItem) this.delegate).getImageBounds(index);
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
        return ((ITableItem) this.delegate).getImageIndent();
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
        return Table.getInstance(((ITableItem) this.delegate).getParent());
    }

    @Override
    public String getText() {
        return ((ITableItem) this.delegate).getText();
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
        return ((ITableItem) this.delegate).getText(index);
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
        return ((ITableItem) this.delegate).getTextBounds(index);
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
        ((ITableItem) this.delegate).setBackground(color);
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
        ((ITableItem) this.delegate).setBackground(index, color);
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
        ((ITableItem) this.delegate).setChecked(checked);
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
        ((ITableItem) this.delegate).setFont(font);
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
        ((ITableItem) this.delegate).setFont(index, font);
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
        ((ITableItem) this.delegate).setForeground(color);
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
        ((ITableItem) this.delegate).setForeground(index, color);
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
        ((ITableItem) this.delegate).setGrayed(grayed);
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
        ((ITableItem) this.delegate).setImage(index, image);
    }

    @Override
    public void setImage(Image image) {
        ((ITableItem) this.delegate).setImage(image);
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
        ((ITableItem) this.delegate).setImage(images);
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
        ((ITableItem) this.delegate).setImageIndent(indent);
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
        ((ITableItem) this.delegate).setText(index, string);
    }

    @Override
    public void setText(String string) {
        ((ITableItem) this.delegate).setText(string);
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
        ((ITableItem) this.delegate).setText(strings);
    }

    protected TableItem(ITableItem delegate) {
        super(delegate);
        this.delegate = delegate;
        INSTANCES.put(delegate, this);
    }

    public static TableItem getInstance(ITableItem delegate) {
        if (delegate == null) {
            return null;
        }
        TableItem ref = (TableItem) INSTANCES.get(delegate);
        if (ref == null) {
            ref = new TableItem(delegate);
        }
        return ref;
    }
}
