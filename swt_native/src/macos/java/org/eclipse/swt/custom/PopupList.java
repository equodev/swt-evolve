/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2017 IBM Corporation and others.
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
package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * A PopupList is a list of selectable items that appears in its own shell positioned above
 * its parent shell.  It is used for selecting items when editing a Table cell (similar to the
 * list that appears when you open a Combo box).
 *
 * The list will be positioned so that it does not run off the screen and the largest number of items
 * are visible.  It may appear above the current cursor location or below it depending how close you
 * are to the edge of the screen.
 *
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public class PopupList {

    /**
     * Creates a PopupList above the specified shell.
     *
     * @param parent a Shell control which will be the parent of the new instance (cannot be null)
     */
    public PopupList(Shell parent) {
        this(new SwtPopupList(parent));
    }

    /**
     * Creates a PopupList above the specified shell.
     *
     * @param parent a widget which will be the parent of the new instance (cannot be null)
     * @param style the style of widget to construct
     *
     * @since 3.0
     */
    public PopupList(Shell parent, int style) {
        this(new SwtPopupList(parent, style));
    }

    /**
     *  Gets the widget font.
     *
     *  @return the widget font
     *
     *  @exception SWTException <ul>
     *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * 	</ul>
     */
    public Font getFont() {
        return getImpl().getFont();
    }

    /**
     *  Gets the items.
     *  <p>
     *  This operation will fail if the items cannot
     *  be queried from the OS.
     *
     *  @return the items in the widget
     *
     *  @exception SWTException <ul>
     *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * 	</ul>
     */
    public String[] getItems() {
        return getImpl().getItems();
    }

    /**
     * Gets the minimum width of the list.
     *
     * @return the minimum width of the list
     */
    public int getMinimumWidth() {
        return getImpl().getMinimumWidth();
    }

    /**
     * Launches the Popup List, waits for an item to be selected and then closes the PopupList.
     *
     * @param rect the initial size and location of the PopupList; the dialog will be
     *        positioned so that it does not run off the screen and the largest number of items are visible
     *
     * @return the text of the selected item or null if no item is selected
     */
    public String open(Rectangle rect) {
        return getImpl().open(rect);
    }

    /**
     *  Selects an item with text that starts with specified String.
     *  <p>
     *  If the item is not currently selected, it is selected.
     *  If the item at an index is selected, it remains selected.
     *  If the string is not matched, it is ignored.
     *
     *  @param string the text of the item
     *
     *  @exception SWTException <ul>
     *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * 	</ul>
     */
    public void select(String string) {
        getImpl().select(string);
    }

    /**
     *  Sets the widget font.
     *  <p>
     *  When new font is null, the font reverts
     *  to the default system font for the widget.
     *
     *  @param font the new font (or null)
     *
     *  @exception SWTException <ul>
     *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * 	</ul>
     */
    public void setFont(Font font) {
        getImpl().setFont(font);
    }

    /**
     *  Sets all items.
     *  <p>
     *  The previous selection is cleared.
     *  The previous items are deleted.
     *  The new items are added.
     *  The top index is set to 0.
     *
     *  @param strings the array of items
     *
     *  This operation will fail when an item is null
     *  or could not be added in the OS.
     *
     *  @exception IllegalArgumentException <ul>
     *     <li>ERROR_NULL_ARGUMENT - if the items array is null</li>
     *     <li>ERROR_INVALID_ARGUMENT - if an item in the items array is null</li>
     *  </ul>
     *  @exception SWTException <ul>
     *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * 	</ul>
     */
    public void setItems(String[] strings) {
        getImpl().setItems(strings);
    }

    /**
     * Sets the minimum width of the list.
     *
     * @param width the minimum width of the list
     */
    public void setMinimumWidth(int width) {
        getImpl().setMinimumWidth(width);
    }

    IPopupList impl;

    protected PopupList(IPopupList impl) {
        this.impl = impl;
        impl.setApi(this);
    }

    public static PopupList createApi(IPopupList impl) {
        return new PopupList(impl);
    }

    public IPopupList getImpl() {
        return impl;
    }
}
