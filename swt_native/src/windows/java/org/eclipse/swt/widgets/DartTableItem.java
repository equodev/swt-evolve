/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
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

    Font font;

    Font[] cellFont;

    boolean checked, grayed, cached;

    int imageIndent, background = -1, foreground = -1;

    int[] cellBackground, cellForeground;

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
        imageIndent = 0;
        checked = grayed = false;
        font = null;
        background = foreground = -1;
        cellFont = null;
        cellBackground = cellForeground = null;
        if ((parent.style & SWT.VIRTUAL) != 0)
            cached = false;
    }

    @Override
    void destroyWidget() {
        ((DartTable) parent.getImpl()).destroyItem(this.getApi());
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
        if (!((DartTable) parent.getImpl()).checkData(this.getApi(), true))
            error(SWT.ERROR_WIDGET_DISPOSED);
        if (background == -1)
            return parent.getBackground();
        return SwtColor.win32_new(display, background);
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
        if (!((DartTable) parent.getImpl()).checkData(this.getApi(), true))
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
     *
     * @since 3.2
     */
    public Rectangle getBounds() {
        checkWidget();
        return null;
    }

    Rectangle getBoundsInPixels() {
        if (!((DartTable) parent.getImpl()).checkData(this.getApi(), true))
            error(SWT.ERROR_WIDGET_DISPOSED);
        int itemIndex = parent.indexOf(this.getApi());
        if (itemIndex == -1)
            return new Rectangle(0, 0, 0, 0);
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
        return null;
    }

    Rectangle getBoundsInPixels(int index) {
        if (!((DartTable) parent.getImpl()).checkData(this.getApi(), true))
            error(SWT.ERROR_WIDGET_DISPOSED);
        int itemIndex = parent.indexOf(this.getApi());
        if (itemIndex == -1)
            return new Rectangle(0, 0, 0, 0);
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
        if (!((DartTable) parent.getImpl()).checkData(this.getApi(), true))
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
        if (!((DartTable) parent.getImpl()).checkData(this.getApi(), true))
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
        if (!((DartTable) parent.getImpl()).checkData(this.getApi(), true))
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
        if (!((DartTable) parent.getImpl()).checkData(this.getApi(), true))
            error(SWT.ERROR_WIDGET_DISPOSED);
        if (foreground == -1)
            return parent.getForeground();
        return SwtColor.win32_new(display, foreground);
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
        if (!((DartTable) parent.getImpl()).checkData(this.getApi(), true))
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
        if (!((DartTable) parent.getImpl()).checkData(this.getApi(), true))
            error(SWT.ERROR_WIDGET_DISPOSED);
        if ((parent.style & SWT.CHECK) == 0)
            return false;
        return grayed;
    }

    @Override
    public Image getImage() {
        checkWidget();
        if (!((DartTable) parent.getImpl()).checkData(this.getApi(), true))
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
        if (!((DartTable) parent.getImpl()).checkData(this.getApi(), true))
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
        return null;
    }

    Rectangle getImageBoundsInPixels(int index) {
        if (!((DartTable) parent.getImpl()).checkData(this.getApi(), true))
            error(SWT.ERROR_WIDGET_DISPOSED);
        int itemIndex = parent.indexOf(this.getApi());
        if (itemIndex == -1)
            return new Rectangle(0, 0, 0, 0);
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
        if (!((DartTable) parent.getImpl()).checkData(this.getApi(), true))
            error(SWT.ERROR_WIDGET_DISPOSED);
        return imageIndent;
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
        if (!((DartTable) parent.getImpl()).checkData(this.getApi(), true))
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
        if (!((DartTable) parent.getImpl()).checkData(this.getApi(), true))
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
        return null;
    }

    Rectangle getTextBoundsInPixels(int index) {
        if (!((DartTable) parent.getImpl()).checkData(this.getApi(), true))
            error(SWT.ERROR_WIDGET_DISPOSED);
        int itemIndex = parent.indexOf(this.getApi());
        if (itemIndex == -1)
            return new Rectangle(0, 0, 0, 0);
        return null;
    }

    void redraw() {
        if (((DartTable) parent.getImpl()).currentItem == this.getApi() || !((DartControl) parent.getImpl()).getDrawing())
            return;
        int index = parent.indexOf(this.getApi());
        if (index == -1)
            return;
    }

    void redraw(int column, boolean drawText, boolean drawImage) {
        if (((DartTable) parent.getImpl()).currentItem == this.getApi() || !((DartControl) parent.getImpl()).getDrawing())
            return;
        int index = parent.indexOf(this.getApi());
        if (index == -1)
            return;
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
        parent = null;
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        strings = null;
        images = null;
        cellFont = null;
        cellBackground = cellForeground = null;
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
            ((DartTable) parent.getImpl()).setCustomDraw(true);
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
     * @since 3.0
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
            ((DartTable) parent.getImpl()).setCustomDraw(true);
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
        if ((parent.style & SWT.CHECK) == 0)
            return;
        if (this.checked == checked)
            return;
        setChecked(checked, false);
    }

    void setChecked(boolean checked, boolean notify) {
        dirty();
        this.checked = checked;
        if ((parent.style & SWT.VIRTUAL) != 0)
            cached = true;
        if (notify) {
            Event event = new Event();
            event.item = this.getApi();
            event.detail = SWT.CHECK;
            ((DartWidget) parent.getImpl()).sendSelectionEvent(SWT.Selection, event, false);
        }
        redraw();
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
        if (oldFont != null && oldFont.equals(newFont))
            return;
        if (font != null)
            ((DartTable) parent.getImpl()).setCustomDraw(true);
        if ((parent.style & SWT.VIRTUAL) != 0)
            cached = true;
        /*
	* Bug in Windows.  Despite the fact that every item in the
	* table always has LPSTR_TEXTCALLBACK, Windows caches the
	* bounds for the selected items.  This means that
	* when you change the string to be something else, Windows
	* correctly asks you for the new string but when the item
	* is selected, the selection draws using the bounds of the
	* previous item.  The fix is to reset LPSTR_TEXTCALLBACK
	* even though it has not changed, causing Windows to flush
	* cached bounds.
	*/
        if ((parent.style & SWT.VIRTUAL) == 0 && cached) {
            int itemIndex = parent.indexOf(this.getApi());
            if (itemIndex != -1) {
                cached = false;
            }
        }
        ((DartTable) parent.getImpl()).setScrollWidth(this.getApi(), false);
        redraw();
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
        cellFont[index] = font == null ? font : DartFont.win32_new(font, getNativeZoom());
        if (oldFont != null && oldFont.equals(font))
            return;
        if (font != null)
            ((DartTable) parent.getImpl()).setCustomDraw(true);
        if ((parent.style & SWT.VIRTUAL) != 0)
            cached = true;
        if (index == 0) {
            /*
		* Bug in Windows.  Despite the fact that every item in the
		* table always has LPSTR_TEXTCALLBACK, Windows caches the
		* bounds for the selected items.  This means that
		* when you change the string to be something else, Windows
		* correctly asks you for the new string but when the item
		* is selected, the selection draws using the bounds of the
		* previous item.  The fix is to reset LPSTR_TEXTCALLBACK
		* even though it has not changed, causing Windows to flush
		* cached bounds.
		*/
            if ((parent.style & SWT.VIRTUAL) == 0 && cached) {
                int itemIndex = parent.indexOf(this.getApi());
                if (itemIndex != -1) {
                    cached = false;
                }
            }
            ((DartTable) parent.getImpl()).setScrollWidth(this.getApi(), false);
        }
        redraw(index, true, false);
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
            ((DartTable) parent.getImpl()).setCustomDraw(true);
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
     * @since 3.0
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
            ((DartTable) parent.getImpl()).setCustomDraw(true);
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
        dirty();
        checkWidget();
        if ((parent.style & SWT.CHECK) == 0)
            return;
        if (this.grayed == grayed)
            return;
        this.grayed = grayed;
        if ((parent.style & SWT.VIRTUAL) != 0)
            cached = true;
        redraw();
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
        ((DartTable) parent.getImpl()).imageIndex(image, index);
        if (index == 0)
            ((DartTable) parent.getImpl()).setScrollWidth(this.getApi(), false);
        boolean drawText = (image == null && oldImage != null) || (image != null && oldImage == null);
        redraw(index, drawText, true);
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
        dirty();
        checkWidget();
        if (indent < 0)
            return;
        if (imageIndent == indent)
            return;
        imageIndent = indent;
        if ((parent.style & SWT.VIRTUAL) != 0) {
            cached = true;
        } else {
            int index = parent.indexOf(this.getApi());
            if (index != -1) {
            }
        }
        ((DartTable) parent.getImpl()).setScrollWidth(this.getApi(), false);
        redraw();
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
            int count = Math.max(1, parent.getColumnCount());
            if (strings == null) {
                strings = new String[count];
            }
            strings[0] = string;
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
            /*
		* Bug in Windows.  Despite the fact that every item in the
		* table always has LPSTR_TEXTCALLBACK, Windows caches the
		* bounds for the selected items.  This means that
		* when you change the string to be something else, Windows
		* correctly asks you for the new string but when the item
		* is selected, the selection draws using the bounds of the
		* previous item.  The fix is to reset LPSTR_TEXTCALLBACK
		* even though it has not changed, causing Windows to flush
		* cached bounds.
		*/
            if ((parent.style & SWT.VIRTUAL) == 0 && cached) {
                int itemIndex = parent.indexOf(this.getApi());
                if (itemIndex != -1) {
                    cached = false;
                }
            }
            ((DartTable) parent.getImpl()).setScrollWidth(this.getApi(), false);
        }
        redraw(index, true, false);
    }

    @Override
    public void setText(String string) {
        checkWidget();
        setText(0, string);
    }

    private static void handleDPIChange(Widget widget, int newZoom, float scalingFactor) {
        if (!(widget instanceof TableItem tableItem)) {
            return;
        }
        Font font = ((DartTableItem) tableItem.getImpl()).font;
        if (font != null) {
            tableItem.setFont(((DartTableItem) tableItem.getImpl()).font);
        }
        Font[] cellFonts = ((DartTableItem) tableItem.getImpl()).cellFont;
        if (cellFonts != null) {
            for (int index = 0; index < cellFonts.length; index++) {
                Font cellFont = cellFonts[index];
                cellFonts[index] = cellFont == null ? null : DartFont.win32_new(cellFont, ((DartWidget) tableItem.getImpl()).getNativeZoom());
            }
        }
    }

    Color _background;

    Color _foreground;

    public Table _parent() {
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

    public boolean _checked() {
        return checked;
    }

    public boolean _grayed() {
        return grayed;
    }

    public boolean _cached() {
        return cached;
    }

    public int _imageIndent() {
        return imageIndent;
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

    public Color __foreground() {
        return _foreground;
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
