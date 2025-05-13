package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface IPopupList {

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
    IFont getFont();

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
    String[] getItems();

    /**
     * Gets the minimum width of the list.
     *
     * @return the minimum width of the list
     */
    int getMinimumWidth();

    /**
     * Launches the Popup List, waits for an item to be selected and then closes the PopupList.
     *
     * @param rect the initial size and location of the PopupList; the dialog will be
     *        positioned so that it does not run off the screen and the largest number of items are visible
     *
     * @return the text of the selected item or null if no item is selected
     */
    String open(IRectangle rect);

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
    void select(String string);

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
    void setFont(IFont font);

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
    void setItems(String[] strings);

    /**
     * Sets the minimum width of the list.
     *
     * @param width the minimum width of the list
     */
    void setMinimumWidth(int width);

    PopupList getApi();

    void setApi(PopupList api);
}
