/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
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

    String[] strings;

    Image[] images;

    boolean checked, grayed, cached;

    Color foreground, background;

    Color[] cellForeground, cellBackground;

    Font font;

    Font[] cellFont;

    int width = -1;

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

    DartTableItem(Table parent, int style, int index, boolean create, TableItem api) {
        super(parent, style, api);
        this.parent = parent;
        if (create)
            ((DartTable) parent.getImpl()).createItem(this.getApi(), index);
    }

    static Table checkNull(Table control) {
        if (control == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        return control;
    }

    int calculateWidth(int index, GC gc, boolean rowSelected) {
        if (index == 0 && width != -1)
            return width;
        Font font = null;
        if (cellFont != null)
            font = cellFont[index];
        if (font == null)
            font = this.font;
        if (font == null)
            font = ((DartControl) parent.getImpl()).font;
        if (font == null)
            font = ((DartControl) parent.getImpl()).defaultFont();
        String text = index == 0 ? this.text : (strings == null ? "" : strings[index]);
        if ((text != null) && (text.length() > TEXT_LIMIT)) {
            text = text.substring(0, TEXT_LIMIT - ELLIPSIS.length()) + ELLIPSIS;
        }
        if (font.extraTraits != 0) {
        } else {
        }
        //	cell.setImage (image != null ? image.handle : null);
        //	NSSize size = cell.cellSize ();
        boolean sendMeasure = true;
        if ((parent.style & SWT.VIRTUAL) != 0) {
            sendMeasure = cached;
        }
        if (sendMeasure && ((DartWidget) parent.getImpl()).hooks(SWT.MeasureItem)) {
            gc.setFont(font);
            Event event = new Event();
            event.item = this.getApi();
            event.index = index;
            event.gc = gc;
            if (rowSelected && ((parent.style & SWT.HIDE_SELECTION) == 0 || ((DartControl) parent.getImpl()).hasFocus()))
                event.detail |= SWT.SELECTED;
            parent.getImpl().sendEvent(SWT.MeasureItem, event);
        }
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
        checked = grayed = cached = false;
        foreground = background = null;
        cellForeground = cellBackground = null;
        font = null;
        cellFont = null;
        width = -1;
    }

    @Override
    void destroyWidget() {
        ((DartTable) parent.getImpl()).destroyItem(this.getApi());
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
        if (!((DartTable) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        return background != null ? background : parent.getBackground();
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
        int count = Math.max(1, ((DartTable) parent.getImpl()).columnCount);
        if (0 > index || index > count - 1)
            return getBackground();
        if (cellBackground == null || cellBackground[index] == null)
            return getBackground();
        return cellBackground[index];
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
        checkWidget();
        if (!((DartTable) parent.getImpl()).checkData(this.getApi()))
            error(SWT.ERROR_WIDGET_DISPOSED);
        if (image != null) {
        }
        Font font = null;
        if (font == null)
            font = this.font;
        if (font == null)
            font = ((DartControl) parent.getImpl()).font;
        if (font == null)
            font = ((DartControl) parent.getImpl()).defaultFont();
        if (font.extraTraits != 0) {
        } else {
        }
        // Inlined for performance.  Also prevents a NPE or potential loop, because cellSize() will
        return null;
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
        if (!(0 <= index && index < Math.max(1, ((DartTable) parent.getImpl()).columnCount)))
            return new Rectangle(0, 0, 0, 0);
        if (((DartTable) parent.getImpl()).columnCount == 0) {
            index = (parent.style & SWT.CHECK) != 0 ? 1 : 0;
        } else {
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
        return checked;
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
        return foreground != null ? foreground : parent.getForeground();
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
        int count = Math.max(1, ((DartTable) parent.getImpl()).columnCount);
        if (0 > index || index > count - 1)
            return getForeground();
        if (cellForeground == null || cellForeground[index] == null)
            return getForeground();
        return cellForeground[index];
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
     */
    public Image getImage(int index) {
        checkWidget();
        if (!((DartTable) parent.getImpl()).checkData(this.getApi()))
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
        if (!(0 <= index && index < Math.max(1, ((DartTable) parent.getImpl()).columnCount)))
            return new Rectangle(0, 0, 0, 0);
        Image image = index == 0 ? this.image : (images != null) ? images[index] : null;
        if (((DartTable) parent.getImpl()).columnCount == 0) {
            index = (parent.style & SWT.CHECK) != 0 ? 1 : 0;
        } else {
        }
        if (image != null) {
        } else {
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
     */
    public String getText(int index) {
        checkWidget();
        if (!((DartTable) parent.getImpl()).checkData(this.getApi()))
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
        if (!(0 <= index && index < Math.max(1, ((DartTable) parent.getImpl()).columnCount)))
            return new Rectangle(0, 0, 0, 0);
        Image image = index == 0 ? this.image : (images != null) ? images[index] : null;
        if (((DartTable) parent.getImpl()).columnCount == 0) {
            index = (parent.style & SWT.CHECK) != 0 ? 1 : 0;
        } else {
        }
        if (image != null) {
        }
        return null;
    }

    @Override
    public boolean isDrawing() {
        return getDrawing() && parent.getImpl().isDrawing();
    }

    void redraw(int columnIndex) {
        if (((DartTable) parent.getImpl()).currentItem == this.getApi() || !isDrawing())
            return;
        if (columnIndex == -1 || ((DartWidget) parent.getImpl()).hooks(SWT.MeasureItem) || ((DartWidget) parent.getImpl()).hooks(SWT.EraseItem) || ((DartWidget) parent.getImpl()).hooks(SWT.PaintItem)) {
        } else {
            if (((DartTable) parent.getImpl()).columnCount == 0) {
            } else {
                if (0 <= columnIndex && columnIndex < ((DartTable) parent.getImpl()).columnCount) {
                } else {
                    return;
                }
            }
        }
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
        parent = null;
    }

    @Override
    void releaseParent() {
        super.releaseParent();
        //	parent.checkItems (true);
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        strings = null;
        images = null;
        background = foreground = null;
        font = null;
        cellBackground = cellForeground = null;
        cellFont = null;
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
        checkWidget();
        if (!java.util.Objects.equals(this.background, color)) {
            dirty();
        }
        if (color != null && color.isDisposed()) {
            error(SWT.ERROR_INVALID_ARGUMENT);
        }
        Color oldColor = background;
        if (oldColor == color)
            return;
        background = color;
        if (oldColor != null && oldColor.equals(color))
            return;
        cached = true;
        redraw(-1);
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
        int count = Math.max(1, ((DartTable) parent.getImpl()).columnCount);
        if (0 > index || index > count - 1)
            return;
        if (cellBackground == null) {
            if (color == null)
                return;
            cellBackground = new Color[count];
        }
        Color oldColor = cellBackground[index];
        if (oldColor == color)
            return;
        cellBackground[index] = color;
        if (oldColor != null && oldColor.equals(color))
            return;
        cached = true;
        redraw(index);
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
        checkWidget();
        if (!java.util.Objects.equals(this.checked, checked)) {
            dirty();
        }
        if ((parent.style & SWT.CHECK) == 0)
            return;
        if (this.checked == checked)
            return;
        this.checked = checked;
        cached = true;
        redraw(-1);
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
        width = -1;
        cached = true;
        redraw(-1);
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
        int count = Math.max(1, ((DartTable) parent.getImpl()).columnCount);
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
        width = -1;
        cached = true;
        redraw(index);
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
        checkWidget();
        if (!java.util.Objects.equals(this.foreground, color)) {
            dirty();
        }
        if (color != null && color.isDisposed()) {
            error(SWT.ERROR_INVALID_ARGUMENT);
        }
        Color oldColor = foreground;
        if (oldColor == color)
            return;
        foreground = color;
        if (oldColor != null && oldColor.equals(color))
            return;
        cached = true;
        redraw(-1);
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
        int count = Math.max(1, ((DartTable) parent.getImpl()).columnCount);
        if (0 > index || index > count - 1)
            return;
        if (cellForeground == null) {
            if (color == null)
                return;
            cellForeground = new Color[count];
        }
        Color oldColor = cellForeground[index];
        if (oldColor == color)
            return;
        cellForeground[index] = color;
        if (oldColor != null && oldColor.equals(color))
            return;
        cached = true;
        redraw(index);
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
        redraw(-1);
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
        int itemIndex = parent.indexOf(this.getApi());
        if (itemIndex == -1)
            return;
        if (((DartTable) parent.getImpl()).imageBounds == null && image != null) {
        }
        if (index == 0) {
            if (image != null && image.type == SWT.ICON) {
                if (image.equals(this.image))
                    return;
            }
            width = -1;
            super.setImage(image);
        }
        int count = Math.max(1, ((DartTable) parent.getImpl()).columnCount);
        if (0 <= index && index < count) {
            if (images == null)
                images = new Image[count];
            if (image != null && image.type == SWT.ICON) {
                if (image.equals(images[index]))
                    return;
            }
            images[index] = image;
        }
        cached = true;
        if (index == 0)
            ((DartTable) parent.getImpl()).setScrollWidth(this.getApi());
        redraw(index);
    }

    @Override
    public void setImage(Image image) {
        checkWidget();
        setImage(0, image);
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
        cached = true;
        /* Image indent is not supported on the Macintosh */
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
        if (index == 0) {
            if (string.equals(text))
                return;
            width = -1;
            super.setText(string);
        }
        int count = Math.max(1, ((DartTable) parent.getImpl()).columnCount);
        if (0 <= index && index < count) {
            if (strings == null)
                strings = new String[count];
            if (string.equals(strings[index]))
                return;
            strings[index] = string;
        }
        cached = true;
        if (index == 0)
            ((DartTable) parent.getImpl()).setScrollWidth(this.getApi());
        redraw(index);
    }

    @Override
    public void setText(String string) {
        checkWidget();
        setText(0, string);
    }

    int imageIndent;

    public Table _parent() {
        return parent;
    }

    public String[] _strings() {
        return strings;
    }

    public Image[] _images() {
        return images;
    }

    public boolean _checked() {
        return checked;
    }

    public boolean _grayed() {
        return grayed;
    }

    public boolean _cached() {
        return cached;
    }

    public Color _foreground() {
        return foreground;
    }

    public Color _background() {
        return background;
    }

    public Color[] _cellForeground() {
        return cellForeground;
    }

    public Color[] _cellBackground() {
        return cellBackground;
    }

    public Font _font() {
        return font;
    }

    public Font[] _cellFont() {
        return cellFont;
    }

    public int _width() {
        return width;
    }

    public int _imageIndent() {
        return imageIndent;
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
