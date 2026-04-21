package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.widgets.*;

public interface ICTabFolder extends IComposite, ImplCTabFolder {

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when a tab item is closed, minimized, maximized,
     * restored, or to show the list of items that are not
     * currently visible.
     *
     * @param listener the listener which should be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     * </ul>
     *
     * @see CTabFolder2Listener
     * @see #removeCTabFolder2Listener(CTabFolder2Listener)
     *
     * @since 3.0
     */
    void addCTabFolder2Listener(CTabFolder2Listener listener);

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when a tab item is closed.
     *
     * @param listener the listener which should be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     * </ul>
     *
     * @see CTabFolderListener
     * @see #removeCTabFolderListener(CTabFolderListener)
     *
     * @deprecated use addCTabFolder2Listener(CTabFolder2Listener)
     */
    void addCTabFolderListener(CTabFolderListener listener);

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the user changes the receiver's selection, by sending
     * it one of the messages defined in the <code>SelectionListener</code>
     * interface.
     * <p>
     * <code>widgetSelected</code> is called when the user changes the selected tab.
     * <code>widgetDefaultSelected</code> is not called.
     * </p>
     *
     * @param listener the listener which should be notified when the user changes the receiver's selection
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SelectionListener
     * @see #removeSelectionListener
     * @see SelectionEvent
     */
    void addSelectionListener(SelectionListener listener);

    //}
    Rectangle computeTrim(int x, int y, int width, int height);

    /**
     * Returns <code>true</code> if the receiver's border is visible.
     *
     * @return the receiver's border visibility state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    boolean getBorderVisible();

    Rectangle getClientArea();

    /**
     * Return the tab that is located at the specified index.
     *
     * @param index the index of the tab item
     * @return the item at the specified index
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_RANGE - if the index is out of range</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     * </ul>
     */
    CTabItem getItem(int index);

    /**
     *  Gets the item at a point in the widget.
     *
     *  @param pt the point in coordinates relative to the CTabFolder
     *  @return the item at a point or null
     *
     *  @exception SWTException <ul>
     * 		<li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     * 		<li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     * 	</ul>
     */
    CTabItem getItem(Point pt);

    /**
     *  Return the number of tabs in the folder.
     *
     *  @return the number of tabs in the folder
     *
     *  @exception SWTException <ul>
     * 		<li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     * 		<li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     * 	</ul>
     */
    int getItemCount();

    /**
     *  Return the tab items.
     *
     *  @return the tab items
     *
     *  @exception SWTException <ul>
     * 		<li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     * 		<li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     * 	</ul>
     */
    CTabItem[] getItems();

    /**
     * Returns <code>true</code> if the receiver is minimized.
     *
     * @return the receiver's minimized state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    boolean getMinimized();

    /**
     * Returns <code>true</code> if the minimize button
     * is visible.
     *
     * @return the visibility of the minimized button
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    boolean getMinimizeVisible();

    /**
     * Returns the number of characters that will
     * appear in a fully compressed tab.
     *
     * @return number of characters that will appear in a fully compressed tab
     *
     * @since 3.0
     */
    int getMinimumCharacters();

    /**
     * Returns <code>true</code> if the receiver is maximized.
     *
     * @return the receiver's maximized state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    boolean getMaximized();

    /**
     * Returns <code>true</code> if the maximize button
     * is visible.
     *
     * @return the visibility of the maximized button
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    boolean getMaximizeVisible();

    /**
     * Returns <code>true</code> if the receiver displays most
     * recently used tabs and <code>false</code> otherwise.
     * <p>
     * When there is not enough horizontal space to show all the tabs,
     * by default, tabs are shown sequentially from left to right in
     * order of their index.  When the MRU visibility is turned on,
     * the tabs that are visible will be the tabs most recently selected.
     * Tabs will still maintain their left to right order based on index
     * but only the most recently selected tabs are visible.
     * <p>
     * For example, consider a CTabFolder that contains "Tab 1", "Tab 2",
     * "Tab 3" and "Tab 4" (in order by index).  The user selects
     * "Tab 1" and then "Tab 3".  If the CTabFolder is now
     * compressed so that only two tabs are visible, by default,
     * "Tab 2" and "Tab 3" will be shown ("Tab 3" since it is currently
     * selected and "Tab 2" because it is the previous item in index order).
     * If MRU visibility is enabled, the two visible tabs will be "Tab 1"
     * and "Tab 3" (in that order from left to right).</p>
     *
     * @return the receiver's header's visibility state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    boolean getMRUVisible();

    /**
     *  Returns the receiver's renderer.
     *
     *  @return the receiver's renderer
     *
     *  @exception SWTException <ul>
     * 		<li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     * 		<li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     * 	</ul>
     *
     *  @see #setRenderer(CTabFolderRenderer)
     *  @see CTabFolderRenderer
     *
     *  @since 3.6
     */
    CTabFolderRenderer getRenderer();

    /**
     *  Return the selected tab item, or null if there is no selection.
     *
     *  @return the selected tab item, or null if none has been selected
     *
     *  @exception SWTException <ul>
     * 		<li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     * 		<li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     * 	</ul>
     */
    CTabItem getSelection();

    /**
     * Returns the receiver's selection background color.
     *
     * @return the selection background color of the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    Color getSelectionBackground();

    /**
     * Returns the receiver's selection foreground color.
     *
     * @return the selection foreground color of the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    Color getSelectionForeground();

    /**
     *  Return the index of the selected tab item, or -1 if there
     *  is no selection.
     *
     *  @return the index of the selected tab item or -1
     *
     *  @exception SWTException <ul>
     * 		<li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     * 		<li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     * 	</ul>
     */
    int getSelectionIndex();

    /**
     * Returns <code>true</code> if the CTabFolder is rendered
     * with a simple, traditional shape.
     *
     * @return <code>true</code> if the CTabFolder is rendered with a simple shape
     *
     * @since 3.0
     */
    boolean getSimple();

    /**
     * Returns <code>true</code> if the CTabFolder only displays the selected tab
     * and <code>false</code> if the CTabFolder displays multiple tabs.
     *
     * @return <code>true</code> if the CTabFolder only displays the selected tab and <code>false</code> if the CTabFolder displays multiple tabs
     *
     * @since 3.0
     */
    boolean getSingle();

    int getStyle();

    /**
     *  Returns the height of the tab
     *
     *  @return the height of the tab
     *
     *  @exception SWTException <ul>
     * 		<li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     * 		<li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     * 	</ul>
     */
    int getTabHeight();

    /**
     *  Returns the position of the tab.  Possible values are SWT.TOP or SWT.BOTTOM.
     *
     *  @return the position of the tab
     *
     *  @exception SWTException <ul>
     * 		<li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     * 		<li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     * 	</ul>
     */
    int getTabPosition();

    /**
     *  Returns the control in the top right corner of the tab folder.
     *  Typically this is a close button or a composite with a menu and close button.
     *
     *  @return the control in the top right corner of the tab folder or null
     *
     *  @exception  SWTException <ul>
     * 		<li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     * 		<li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     * 	</ul>
     *
     *  @since 2.1
     */
    Control getTopRight();

    /**
     *  Returns the alignment of the top right control.
     *
     *  @return the alignment of the top right control which is either
     *  <code>SWT.RIGHT</code> or <code>SWT.FILL</code>
     *
     *  @exception  SWTException <ul>
     * 		<li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     * 		<li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     * 	</ul>
     *
     *  @since 3.6
     */
    int getTopRightAlignment();

    /**
     * Returns <code>true</code> if the close button appears
     * when the user hovers over an unselected tabs.
     *
     * @return <code>true</code> if the close button appears on unselected tabs
     *
     * @since 3.0
     */
    boolean getUnselectedCloseVisible();

    /**
     * Returns <code>true</code> if an image appears
     * in unselected tabs.
     *
     * @return <code>true</code> if an image appears in unselected tabs
     *
     * @since 3.0
     */
    boolean getUnselectedImageVisible();

    /**
     * Returns <code>true</code> if an image appears
     * in selected tabs.
     *
     * @return <code>true</code> if an image appears in selected tabs
     *
     * @since 3.125
     */
    boolean getSelectedImageVisible();

    /**
     * Return the index of the specified tab or -1 if the tab is not
     * in the receiver.
     *
     * @param item the tab item for which the index is required
     *
     * @return the index of the specified tab item or -1
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     * </ul>
     */
    int indexOf(CTabItem item);

    /**
     * Removes the listener.
     *
     * @param listener the listener which should no longer be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     * </ul>
     *
     * @see #addCTabFolder2Listener(CTabFolder2Listener)
     *
     * @since 3.0
     */
    void removeCTabFolder2Listener(CTabFolder2Listener listener);

    /**
     * Removes the listener.
     *
     * @param listener the listener which should no longer be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     * </ul>
     *
     * @deprecated see removeCTabFolderCloseListener(CTabFolderListener)
     */
    void removeCTabFolderListener(CTabFolderListener listener);

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the user changes the receiver's selection.
     *
     * @param listener the listener which should no longer be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SelectionListener
     * @see #addSelectionListener
     */
    void removeSelectionListener(SelectionListener listener);

    void reskin(int flags);

    void setBackground(Color color);

    /**
     *  Specify a gradient of colors to be drawn in the background of the unselected tabs.
     *  For example to draw a gradient that varies from dark blue to blue and then to
     *  white, use the following call to setBackground:
     *  <pre>
     * 	cfolder.setBackground(new Color[]{display.getSystemColor(SWT.COLOR_DARK_BLUE),
     * 		                           display.getSystemColor(SWT.COLOR_BLUE),
     * 		                           display.getSystemColor(SWT.COLOR_WHITE),
     * 		                           display.getSystemColor(SWT.COLOR_WHITE)},
     * 		               new int[] {25, 50, 100});
     *  </pre>
     *
     *  @param colors an array of Color that specifies the colors to appear in the gradient
     *                in order of appearance left to right.  The value <code>null</code> clears the
     *                background gradient. The value <code>null</code> can be used inside the array of
     *                Color to specify the background color.
     *  @param percents an array of integers between 0 and 100 specifying the percent of the width
     *                  of the widget at which the color should change.  The size of the <code>percents</code>
     *                  array must be one less than the size of the <code>colors</code> array.
     *
     *  @exception SWTException <ul>
     * 		<li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     * 		<li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     * 	</ul>
     *
     *  @since 3.6
     */
    void setBackground(Color[] colors, int[] percents);

    /**
     *  Specify a gradient of colors to be drawn in the background of the unselected tab.
     *  For example to draw a vertical gradient that varies from dark blue to blue and then to
     *  white, use the following call to setBackground:
     *  <pre>
     * 	cfolder.setBackground(new Color[]{display.getSystemColor(SWT.COLOR_DARK_BLUE),
     * 		                           display.getSystemColor(SWT.COLOR_BLUE),
     * 		                           display.getSystemColor(SWT.COLOR_WHITE),
     * 		                           display.getSystemColor(SWT.COLOR_WHITE)},
     * 		                  new int[] {25, 50, 100}, true);
     *  </pre>
     *
     *  @param colors an array of Color that specifies the colors to appear in the gradient
     *                in order of appearance left to right.  The value <code>null</code> clears the
     *                background gradient. The value <code>null</code> can be used inside the array of
     *                Color to specify the background color.
     *  @param percents an array of integers between 0 and 100 specifying the percent of the width
     *                  of the widget at which the color should change.  The size of the <code>percents</code>
     *                  array must be one less than the size of the <code>colors</code> array.
     *
     *  @param vertical indicate the direction of the gradient. <code>True</code> is vertical and <code>false</code> is horizontal.
     *
     *  @exception SWTException <ul>
     * 		<li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     * 		<li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     * 	</ul>
     *
     *  @since 3.6
     */
    void setBackground(Color[] colors, int[] percents, boolean vertical);

    void setBackgroundImage(Image image);

    /**
     * Toggle the visibility of the border
     *
     * @param show true if the border should be displayed
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setBorderVisible(boolean show);

    boolean setFocus();

    void setFont(Font font);

    void setForeground(Color color);

    /**
     * Display an insert marker before or after the specified tab item.
     *
     * A value of null will clear the mark.
     *
     * @param item the item with which the mark is associated or null
     *
     * @param after true if the mark should be displayed after the specified item
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setInsertMark(CTabItem item, boolean after);

    /**
     * Display an insert marker before or after the specified tab item.
     *
     * A value of -1 will clear the mark.
     *
     * @param index the index of the item with which the mark is associated or -1
     *
     * @param after true if the mark should be displayed after the specified item
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT when the index is invalid</li>
     * </ul>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setInsertMark(int index, boolean after);

    /**
     * Marks the receiver's maximize button as visible if the argument is <code>true</code>,
     * and marks it invisible otherwise.
     *
     * @param visible the new visibility state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    void setMaximizeVisible(boolean visible);

    /**
     * Sets the layout which is associated with the receiver to be
     * the argument which may be null.
     * <p>
     * Note: No Layout can be set on this Control because it already
     * manages the size and position of its children.
     * </p>
     *
     * @param layout the receiver's new layout or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setLayout(Layout layout);

    /**
     * Sets the maximized state of the receiver.
     *
     * @param maximize the new maximized state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    void setMaximized(boolean maximize);

    /**
     * Marks the receiver's minimize button as visible if the argument is <code>true</code>,
     * and marks it invisible otherwise.
     *
     * @param visible the new visibility state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    void setMinimizeVisible(boolean visible);

    /**
     * Sets the minimized state of the receiver.
     *
     * @param minimize the new minimized state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    void setMinimized(boolean minimize);

    /**
     * Sets the minimum number of characters that will
     * be displayed in a fully compressed tab.
     *
     * @param count the minimum number of characters that will be displayed in a fully compressed tab
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_RANGE - if the count is less than zero</li>
     * </ul>
     *
     * @since 3.0
     */
    void setMinimumCharacters(int count);

    /**
     * When there is not enough horizontal space to show all the tabs,
     * by default, tabs are shown sequentially from left to right in
     * order of their index.  When the MRU visibility is turned on,
     * the tabs that are visible will be the tabs most recently selected.
     * Tabs will still maintain their left to right order based on index
     * but only the most recently selected tabs are visible.
     * <p>
     * For example, consider a CTabFolder that contains "Tab 1", "Tab 2",
     * "Tab 3" and "Tab 4" (in order by index).  The user selects
     * "Tab 1" and then "Tab 3".  If the CTabFolder is now
     * compressed so that only two tabs are visible, by default,
     * "Tab 2" and "Tab 3" will be shown ("Tab 3" since it is currently
     * selected and "Tab 2" because it is the previous item in index order).
     * If MRU visibility is enabled, the two visible tabs will be "Tab 1"
     * and "Tab 3" (in that order from left to right).</p>
     *
     * @param show the new visibility state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    void setMRUVisible(boolean show);

    /**
     *  Sets the renderer which is associated with the receiver to be
     *  the argument which may be null. In the case of null, the default
     *  renderer is used.
     *
     *  @param renderer a new renderer
     *
     *  @exception SWTException <ul>
     * 		<li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     * 		<li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     * 	</ul>
     *
     *  @see CTabFolderRenderer
     *
     *  @since 3.6
     */
    void setRenderer(CTabFolderRenderer renderer);

    /**
     * Set the selection to the tab at the specified item.
     *
     * @param item the tab item to be selected
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the item is null</li>
     * </ul>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     * </ul>
     */
    void setSelection(CTabItem item);

    /**
     * Set the selection to the tab at the specified index.
     *
     * @param index the index of the tab item to be selected
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setSelection(int index);

    /**
     * Sets the receiver's selection background color to the color specified
     * by the argument, or to the default system color for the control
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
     * @since 3.0
     */
    void setSelectionBackground(Color color);

    /**
     *  Specify a gradient of colours to be draw in the background of the selected tab.
     *  For example to draw a gradient that varies from dark blue to blue and then to
     *  white, use the following call to setBackground:
     *  <pre>
     * 	cfolder.setBackground(new Color[]{display.getSystemColor(SWT.COLOR_DARK_BLUE),
     * 		                           display.getSystemColor(SWT.COLOR_BLUE),
     * 		                           display.getSystemColor(SWT.COLOR_WHITE),
     * 		                           display.getSystemColor(SWT.COLOR_WHITE)},
     * 		               new int[] {25, 50, 100});
     *  </pre>
     *
     *  @param colors an array of Color that specifies the colors to appear in the gradient
     *                in order of appearance left to right.  The value <code>null</code> clears the
     *                background gradient. The value <code>null</code> can be used inside the array of
     *                Color to specify the background color.
     *  @param percents an array of integers between 0 and 100 specifying the percent of the width
     *                  of the widget at which the color should change.  The size of the percents array must be one
     *                  less than the size of the colors array.
     *
     *  @exception SWTException <ul>
     * 		<li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     * 		<li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     * 	</ul>
     */
    void setSelectionBackground(Color[] colors, int[] percents);

    /**
     *  Specify a gradient of colours to be draw in the background of the selected tab.
     *  For example to draw a vertical gradient that varies from dark blue to blue and then to
     *  white, use the following call to setBackground:
     *  <pre>
     * 	cfolder.setBackground(new Color[]{display.getSystemColor(SWT.COLOR_DARK_BLUE),
     * 		                           display.getSystemColor(SWT.COLOR_BLUE),
     * 		                           display.getSystemColor(SWT.COLOR_WHITE),
     * 		                           display.getSystemColor(SWT.COLOR_WHITE)},
     * 		                  new int[] {25, 50, 100}, true);
     *  </pre>
     *
     *  @param colors an array of Color that specifies the colors to appear in the gradient
     *                in order of appearance left to right.  The value <code>null</code> clears the
     *                background gradient. The value <code>null</code> can be used inside the array of
     *                Color to specify the background color.
     *  @param percents an array of integers between 0 and 100 specifying the percent of the width
     *                  of the widget at which the color should change.  The size of the percents array must be one
     *                  less than the size of the colors array.
     *
     *  @param vertical indicate the direction of the gradient.  True is vertical and false is horizontal.
     *
     *  @exception SWTException <ul>
     * 		<li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     * 		<li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     * 	</ul>
     *
     *  @since 3.0
     */
    void setSelectionBackground(Color[] colors, int[] percents, boolean vertical);

    /**
     * Set the image to be drawn in the background of the selected tab.  Image
     * is stretched or compressed to cover entire selection tab area.
     *
     * @param image the image to be drawn in the background
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setSelectionBackground(Image image);

    /**
     * Set the foreground color of the selected tab.
     *
     * @param color the color of the text displayed in the selected tab
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setSelectionForeground(Color color);

    /**
     * Sets the thickness of the highlight bar on the selected tab. The highlight bar is drawn in the top margin of the selected tab.
     *
     * @param thickness the desired thickness. Must be positive and lower than {@link CTabFolderRenderer#ITEM_TOP_MARGIN} (that is either {@code 0} {@code 1} or {@code 2} at the moment),for correct results.
     *                  Set to {@code 0} to not draw a highlight bar.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the parameter value is invalid</li>
     * </ul>
     * @implNote Default {@link CTabFolderRenderer} currently ignores this setting if {@link #getSimple()} is {@code true}.
     * @since 3.121
     */
    void setSelectionBarThickness(int thickness);

    /**
     * Sets the shape that the CTabFolder will use to render itself.
     *
     * @param simple <code>true</code> if the CTabFolder should render itself in a simple, traditional style
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    void setSimple(boolean simple);

    /**
     * Sets the number of tabs that the CTabFolder should display
     *
     * @param single <code>true</code> if only the selected tab should be displayed otherwise, multiple tabs will be shown.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    void setSingle(boolean single);

    /**
     * Specify a fixed height for the tab items.  If no height is specified,
     * the default height is the height of the text or the image, whichever
     * is greater. Specifying a height of -1 will revert to the default height.
     *
     * @param height the point value of the height or -1
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if called with a height of less than 0</li>
     * </ul>
     */
    void setTabHeight(int height);

    /**
     * Specify whether the tabs should appear along the top of the folder
     * or along the bottom of the folder.
     *
     * @param position <code>SWT.TOP</code> for tabs along the top or <code>SWT.BOTTOM</code> for tabs along the bottom
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the position value is not either SWT.TOP or SWT.BOTTOM</li>
     * </ul>
     *
     * @since 3.0
     */
    void setTabPosition(int position);

    /**
     * Set the control that appears in the top right corner of the tab folder.
     * Typically this is a close button or a composite with a Menu and close button.
     * The topRight control is optional.  Setting the top right control to null will
     * remove it from the tab folder.
     *
     * @param control the control to be displayed in the top right corner or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the control is disposed, or not a child of this CTabFolder</li>
     * </ul>
     *
     * @since 2.1
     */
    void setTopRight(Control control);

    /**
     * Set the control that appears in the top right corner of the tab folder.
     * Typically this is a close button or a composite with a Menu and close button.
     * The topRight control is optional.  Setting the top right control to null
     * will remove it from the tab folder.
     * <p>
     * The alignment parameter sets the layout of the control in the tab area.
     * <code>SWT.RIGHT</code> will cause the control to be positioned on the far
     * right of the folder and it will have its default size.  <code>SWT.FILL</code>
     * will size the control to fill all the available space to the right of the
     * last tab.  If there is no available space, the control will not be visible.
     * <code>SWT.RIGHT | SWT.WRAP</code> will allow the control to wrap below the
     * tabs if there is not enough available space to the right of the last tab.
     * </p>
     *
     * @param control the control to be displayed in the top right corner or null
     * @param alignment <code>SWT.RIGHT</code> or <code>SWT.FILL</code> or <code>SWT.RIGHT | SWT.WRAP</code>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the control is disposed, or not a child of this CTabFolder</li>
     * </ul>
     *
     * @since 3.0
     */
    void setTopRight(Control control, int alignment);

    /**
     * Specify whether the close button appears
     * when the user hovers over an unselected tabs.
     *
     * @param visible <code>true</code> makes the close button appear
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    void setUnselectedCloseVisible(boolean visible);

    /**
     * Specify whether the image appears on unselected tabs.
     *
     * @param visible <code>true</code> makes the image appear
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    void setUnselectedImageVisible(boolean visible);

    /**
     * Specify whether the image appears on selected tabs.
     *
     * @param visible <code>true</code> makes the image appear
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.125
     */
    void setSelectedImageVisible(boolean visible);

    /**
     * Shows the item.  If the item is already showing in the receiver,
     * this method simply returns.  Otherwise, the items are scrolled until
     * the item is visible.
     *
     * @param item the item to be shown
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
     * @see CTabFolder#showSelection()
     *
     * @since 2.0
     */
    void showItem(CTabItem item);

    /**
     * Shows the selection.  If the selection is already showing in the receiver,
     * this method simply returns.  Otherwise, the items are scrolled until
     * the selection is visible.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see CTabFolder#showItem(CTabItem)
     *
     * @since 2.0
     */
    void showSelection();

    /**
     * Sets whether the selected tab is rendered as highlighted.
     *
     * @param enabled
     *            {@code true} if the selected tab should be highlighted,
     *            {@code false} otherwise.
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
     *                thread that created the receiver</li>
     *                </ul>
     * @since 3.106
     */
    void setHighlightEnabled(boolean enabled);

    /**
     * Returns <code>true</code> if the selected tab is rendered as
     * highlighted.
     *
     * @return <code>true</code> if the selected tab is rendered as
     *         highlighted.
     *
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
     *                thread that created the receiver</li>
     *                </ul>
     * @since 3.106
     */
    boolean getHighlightEnabled();

    CTabFolder getApi();
}
