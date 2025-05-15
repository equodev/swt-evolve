/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2020 IBM Corporation and others.
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
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import dev.equo.swt.Convert;

/**
 * Instances of this class implement the notebook user interface
 * metaphor.  It allows the user to select a notebook page from
 * set of pages.
 * <p>
 * The item children that may be added to instances of this class
 * must be of type <code>CTabItem</code>.
 * <code>Control</code> children are created and then set into a
 * tab item using <code>CTabItem#setControl</code>.
 * </p><p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not make sense to set a layout on it.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>CLOSE, TOP, BOTTOM, FLAT, BORDER, SINGLE, MULTI</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * <dd>"CTabFolder2"</dd>
 * </dl>
 * Note: Only one of the styles TOP and BOTTOM
 * may be specified.
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#ctabfolder">CTabFolder, CTabItem snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: CustomControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CTabFolder extends Composite {

    /**
     * marginWidth specifies the number of points of horizontal margin
     * that will be placed along the left and right edges of the form.
     *
     * The default value is 0.
     */
    public int marginWidth = 0;

    /**
     * marginHeight specifies the number of points of vertical margin
     * that will be placed along the top and bottom edges of the form.
     *
     * The default value is 0.
     */
    public int marginHeight = 0;

    /**
     * A multiple of the tab height that specifies the minimum width to which a tab
     * will be compressed before scrolling arrows are used to navigate the tabs.
     *
     * NOTE This field is badly named and can not be fixed for backwards compatibility.
     * It should not be capitalized.
     *
     * @deprecated This field is no longer used.  See setMinimumCharacters(int)
     */
    @Deprecated
    public int MIN_TAB_WIDTH = 4;

    /**
     * Color of innermost line of drop shadow border.
     *
     * NOTE This field is badly named and can not be fixed for backwards compatibility.
     * It should be capitalized.
     *
     * @deprecated drop shadow border is no longer drawn in 3.0
     */
    @Deprecated
    public static RGB borderInsideRGB = new RGB(132, 130, 132);

    /**
     * Color of middle line of drop shadow border.
     *
     * NOTE This field is badly named and can not be fixed for backwards compatibility.
     * It should be capitalized.
     *
     * @deprecated drop shadow border is no longer drawn in 3.0
     */
    @Deprecated
    public static RGB borderMiddleRGB = new RGB(143, 141, 138);

    /**
     * Color of outermost line of drop shadow border.
     *
     * NOTE This field is badly named and can not be fixed for backwards compatibility.
     * It should be capitalized.
     *
     * @deprecated drop shadow border is no longer drawn in 3.0
     */
    @Deprecated
    public static RGB borderOutsideRGB = new RGB(171, 168, 165);

    // when disposing CTabFolder, don't try to layout the items or
    // keep track of size changes in order to redraw only affected area
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
     * @param parent a widget which will be the parent of the new instance (cannot be null)
     * @param style the style of widget to construct
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     * </ul>
     *
     * @see SWT#TOP
     * @see SWT#BOTTOM
     * @see SWT#FLAT
     * @see SWT#BORDER
     * @see SWT#SINGLE
     * @see SWT#MULTI
     * @see #getStyle()
     */
    public CTabFolder(Composite parent, int style) {
        this(new nat.org.eclipse.swt.custom.CTabFolder((nat.org.eclipse.swt.widgets.Composite) (parent != null ? parent.getDelegate() : null), style));
    }

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
    public void addCTabFolder2Listener(CTabFolder2Listener listener) {
        getDelegate().addCTabFolder2Listener(listener);
    }

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
    @Deprecated
    public void addCTabFolderListener(CTabFolderListener listener) {
        getDelegate().addCTabFolderListener(listener);
    }

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
    public void addSelectionListener(SelectionListener listener) {
        getDelegate().addSelectionListener(listener);
    }

    /*
* This class was not intended to be subclassed but this restriction
* cannot be enforced without breaking backward compatibility.
*/
    //protected void checkSubclass () {
    //	String name = getClass ().getName ();
    //	int index = name.lastIndexOf ('.');
    //	if (!name.substring (0, index + 1).equals ("org.eclipse.swt.custom.")) {
    //		SWT.error (SWT.ERROR_INVALID_SUBCLASS);
    //	}
    //}
    public Rectangle computeTrim(int x, int y, int width, int height) {
        IRectangle ret = getDelegate().computeTrim(x, y, width, height);
        return ret != null ? ret.getApi() : null;
    }

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
    public boolean getBorderVisible() {
        return getDelegate().getBorderVisible();
    }

    /**
     * Returns <code>true</code> if the chevron button
     * is visible when necessary.
     *
     * @return the visibility of the chevron button
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Rectangle getClientArea() {
        IRectangle ret = getDelegate().getClientArea();
        return ret != null ? ret.getApi() : null;
    }

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
    public CTabItem getItem(int index) {
        ICTabItem ret = getDelegate().getItem(index);
        return ret != null ? ret.getApi() : null;
    }

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
    public CTabItem getItem(Point pt) {
        ICTabItem ret = getDelegate().getItem((pt != null ? pt.getDelegate() : null));
        return ret != null ? ret.getApi() : null;
    }

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
    public int getItemCount() {
        return getDelegate().getItemCount();
    }

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
    public CTabItem[] getItems() {
        return Convert.array(getDelegate().getItems(), ICTabItem::getApi, CTabItem[]::new);
    }

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
    public boolean getMinimized() {
        return getDelegate().getMinimized();
    }

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
    public boolean getMinimizeVisible() {
        return getDelegate().getMinimizeVisible();
    }

    /**
     * Returns the number of characters that will
     * appear in a fully compressed tab.
     *
     * @return number of characters that will appear in a fully compressed tab
     *
     * @since 3.0
     */
    public int getMinimumCharacters() {
        return getDelegate().getMinimumCharacters();
    }

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
    public boolean getMaximized() {
        return getDelegate().getMaximized();
    }

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
    public boolean getMaximizeVisible() {
        return getDelegate().getMaximizeVisible();
    }

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
    public boolean getMRUVisible() {
        return getDelegate().getMRUVisible();
    }

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
    public CTabFolderRenderer getRenderer() {
        ICTabFolderRenderer ret = getDelegate().getRenderer();
        return ret != null ? ret.getApi() : null;
    }

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
    public CTabItem getSelection() {
        ICTabItem ret = getDelegate().getSelection();
        return ret != null ? ret.getApi() : null;
    }

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
    public Color getSelectionBackground() {
        IColor ret = getDelegate().getSelectionBackground();
        return ret != null ? ret.getApi() : null;
    }

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
    public Color getSelectionForeground() {
        IColor ret = getDelegate().getSelectionForeground();
        return ret != null ? ret.getApi() : null;
    }

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
    public int getSelectionIndex() {
        return getDelegate().getSelectionIndex();
    }

    /**
     * Returns <code>true</code> if the CTabFolder is rendered
     * with a simple, traditional shape.
     *
     * @return <code>true</code> if the CTabFolder is rendered with a simple shape
     *
     * @since 3.0
     */
    public boolean getSimple() {
        return getDelegate().getSimple();
    }

    /**
     * Returns <code>true</code> if the CTabFolder only displays the selected tab
     * and <code>false</code> if the CTabFolder displays multiple tabs.
     *
     * @return <code>true</code> if the CTabFolder only displays the selected tab and <code>false</code> if the CTabFolder displays multiple tabs
     *
     * @since 3.0
     */
    public boolean getSingle() {
        return getDelegate().getSingle();
    }

    public int getStyle() {
        return getDelegate().getStyle();
    }

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
    public int getTabHeight() {
        return getDelegate().getTabHeight();
    }

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
    public int getTabPosition() {
        return getDelegate().getTabPosition();
    }

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
    public Control getTopRight() {
        IControl ret = getDelegate().getTopRight();
        return ret != null ? ret.getApi() : null;
    }

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
    public int getTopRightAlignment() {
        return getDelegate().getTopRightAlignment();
    }

    /**
     * Returns <code>true</code> if the close button appears
     * when the user hovers over an unselected tabs.
     *
     * @return <code>true</code> if the close button appears on unselected tabs
     *
     * @since 3.0
     */
    public boolean getUnselectedCloseVisible() {
        return getDelegate().getUnselectedCloseVisible();
    }

    /**
     * Returns <code>true</code> if an image appears
     * in unselected tabs.
     *
     * @return <code>true</code> if an image appears in unselected tabs
     *
     * @since 3.0
     */
    public boolean getUnselectedImageVisible() {
        return getDelegate().getUnselectedImageVisible();
    }

    /**
     * Returns <code>true</code> if an image appears
     * in selected tabs.
     *
     * @return <code>true</code> if an image appears in selected tabs
     *
     * @since 3.125
     */
    public boolean getSelectedImageVisible() {
        return getDelegate().getSelectedImageVisible();
    }

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
    public int indexOf(CTabItem item) {
        return getDelegate().indexOf((item != null ? item.getDelegate() : null));
    }

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
    public void removeCTabFolder2Listener(CTabFolder2Listener listener) {
        getDelegate().removeCTabFolder2Listener(listener);
    }

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
    @Deprecated
    public void removeCTabFolderListener(CTabFolderListener listener) {
        getDelegate().removeCTabFolderListener(listener);
    }

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
    public void removeSelectionListener(SelectionListener listener) {
        getDelegate().removeSelectionListener(listener);
    }

    public void reskin(int flags) {
        getDelegate().reskin(flags);
    }

    public void setBackground(Color color) {
        getDelegate().setBackground((color != null ? color.getDelegate() : null));
    }

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
    public void setBackground(Color[] colors, int[] percents) {
        getDelegate().setBackground(Convert.array(colors, Color::getDelegate, IColor[]::new), percents);
    }

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
    public void setBackground(Color[] colors, int[] percents, boolean vertical) {
        getDelegate().setBackground(Convert.array(colors, Color::getDelegate, IColor[]::new), percents, vertical);
    }

    public void setBackgroundImage(Image image) {
        getDelegate().setBackgroundImage((image != null ? image.getDelegate() : null));
    }

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
    public void setBorderVisible(boolean show) {
        getDelegate().setBorderVisible(show);
    }

    public boolean setFocus() {
        return getDelegate().setFocus();
    }

    public void setFont(Font font) {
        getDelegate().setFont((font != null ? font.getDelegate() : null));
    }

    public void setForeground(Color color) {
        getDelegate().setForeground((color != null ? color.getDelegate() : null));
    }

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
    public void setInsertMark(CTabItem item, boolean after) {
        getDelegate().setInsertMark((item != null ? item.getDelegate() : null), after);
    }

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
    public void setInsertMark(int index, boolean after) {
        getDelegate().setInsertMark(index, after);
    }

    /**
     * Reorder the items of the receiver.
     * @param indices an array containing the new indices for all items
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the indices array is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the indices array is not the same length as the number of items,
     *    if there are duplicate indices or an index is out of range.</li>
     * </ul>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
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
    public void setMaximizeVisible(boolean visible) {
        getDelegate().setMaximizeVisible(visible);
    }

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
    public void setLayout(Layout layout) {
        getDelegate().setLayout((layout != null ? layout.getDelegate() : null));
    }

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
    public void setMaximized(boolean maximize) {
        getDelegate().setMaximized(maximize);
    }

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
    public void setMinimizeVisible(boolean visible) {
        getDelegate().setMinimizeVisible(visible);
    }

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
    public void setMinimized(boolean minimize) {
        getDelegate().setMinimized(minimize);
    }

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
    public void setMinimumCharacters(int count) {
        getDelegate().setMinimumCharacters(count);
    }

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
    public void setMRUVisible(boolean show) {
        getDelegate().setMRUVisible(show);
    }

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
    public void setRenderer(CTabFolderRenderer renderer) {
        getDelegate().setRenderer((renderer != null ? renderer.getDelegate() : null));
    }

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
    public void setSelection(CTabItem item) {
        getDelegate().setSelection((item != null ? item.getDelegate() : null));
    }

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
    public void setSelection(int index) {
        getDelegate().setSelection(index);
    }

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
    public void setSelectionBackground(Color color) {
        getDelegate().setSelectionBackground((color != null ? color.getDelegate() : null));
    }

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
    public void setSelectionBackground(Color[] colors, int[] percents) {
        getDelegate().setSelectionBackground(Convert.array(colors, Color::getDelegate, IColor[]::new), percents);
    }

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
    public void setSelectionBackground(Color[] colors, int[] percents, boolean vertical) {
        getDelegate().setSelectionBackground(Convert.array(colors, Color::getDelegate, IColor[]::new), percents, vertical);
    }

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
    public void setSelectionBackground(Image image) {
        getDelegate().setSelectionBackground((image != null ? image.getDelegate() : null));
    }

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
    public void setSelectionForeground(Color color) {
        getDelegate().setSelectionForeground((color != null ? color.getDelegate() : null));
    }

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
    public void setSelectionBarThickness(int thickness) {
        getDelegate().setSelectionBarThickness(thickness);
    }

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
    public void setSimple(boolean simple) {
        getDelegate().setSimple(simple);
    }

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
    public void setSingle(boolean single) {
        getDelegate().setSingle(single);
    }

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
    public void setTabHeight(int height) {
        getDelegate().setTabHeight(height);
    }

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
    public void setTabPosition(int position) {
        getDelegate().setTabPosition(position);
    }

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
    public void setTopRight(Control control) {
        getDelegate().setTopRight((control != null ? control.getDelegate() : null));
    }

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
    public void setTopRight(Control control, int alignment) {
        getDelegate().setTopRight((control != null ? control.getDelegate() : null), alignment);
    }

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
    public void setUnselectedCloseVisible(boolean visible) {
        getDelegate().setUnselectedCloseVisible(visible);
    }

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
    public void setUnselectedImageVisible(boolean visible) {
        getDelegate().setUnselectedImageVisible(visible);
    }

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
    public void setSelectedImageVisible(boolean visible) {
        getDelegate().setSelectedImageVisible(visible);
    }

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
    public void showItem(CTabItem item) {
        getDelegate().showItem((item != null ? item.getDelegate() : null));
    }

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
    public void showSelection() {
        getDelegate().showSelection();
    }

    /**
     * Set a control that can appear to the left or to the right of the folder tabs.
     * This method can also be used instead of #setTopRight(Control). To remove a tab
     * control, see#removeTabControl(Control);
     * <p>
     * The flags parameter sets the layout of the control in the tab area.
     * <code>SWT.LEAD</code> will cause the control to be positioned on the left
     * of the tabs. <code>SWT.TRAIL</code> will cause the control to be positioned on
     * the far right of the folder and it will have its default size. <code>SWT.TRAIL</code>
     * can be combined with <code>SWT.FILL</code>to fill all the available space to the
     * right of the last tab. <code>SWT.WRAP</code> can also be added to <code>SWT.TRAIL</code>
     * only to cause a control to wrap if there is not enough space to display it in its
     * entirety.
     * </p>
     * @param control the control to be displayed in the top right corner or null
     *
     * @param flags valid combinations are:
     * <ul><li>SWT.LEAD
     * <li> SWT.TRAIL (| SWT.FILL | SWT.WRAP)
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the control is not a child of this CTabFolder</li>
     * </ul>
     */
    /**
     * Removes the control from the list of tab controls.
     *
     * @param control the control to be removed
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the control is not a child of this CTabFolder</li>
     * </ul>
     */
    /**
     * Sets whether a chevron is shown when there are more items to be displayed.
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_RANGE - if the index is out of range</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
     *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
     * </ul>
     */
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
    public void setHighlightEnabled(boolean enabled) {
        getDelegate().setHighlightEnabled(enabled);
    }

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
    public boolean getHighlightEnabled() {
        return getDelegate().getHighlightEnabled();
    }

    protected CTabFolder(ICTabFolder delegate) {
        super(delegate);
    }

    public static CTabFolder createApi(ICTabFolder delegate) {
        return new CTabFolder(delegate);
    }

    public ICTabFolder getDelegate() {
        return (ICTabFolder) super.getDelegate();
    }
}
