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
import dev.equo.swt.*;

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
public class DartCTabFolder extends DartComposite implements ICTabFolder {

    /* External Listener management */
    CTabFolder2Listener[] folderListeners = new CTabFolder2Listener[0];

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
    public DartCTabFolder(Composite parent, int style) {
        super(parent, checkStyle(parent, style));
        init(style);
    }

    void init(int style) {
        super.setLayout(new CTabFolderLayout());
        int style2 = super.getStyle();
        oldFont = getFont();
        onBottom = (style2 & SWT.BOTTOM) != 0;
        showClose = (style2 & SWT.CLOSE) != 0;
        //	showMin = (style2 & SWT.MIN) != 0; - conflicts with SWT.TOP
        //	showMax = (style2 & SWT.MAX) != 0; - conflicts with SWT.BOTTOM
        single = (style2 & SWT.SINGLE) != 0;
        borderVisible = (style & SWT.BORDER) != 0;
        //set up default colors
        Display display = getDisplay();
        selectionForeground = display.getSystemColor(SELECTION_FOREGROUND);
        selectionBackground = display.getSystemColor(SELECTION_BACKGROUND);
        renderer = new CTabFolderRenderer(this.getApi());
        useDefaultRenderer = true;
        controls = new Control[0];
        controlAlignments = new int[0];
        controlRects = new Rectangle[0];
        controlBkImages = new Image[0];
        updateTabHeight(false);
        // Add all listeners
        listener = event -> {
            switch(event.type) {
                case SWT.Dispose:
                    onDispose(event);
                    break;
                case SWT.DragDetect:
                    onDragDetect(event);
                    break;
                case SWT.FocusIn:
                    onFocus(event);
                    break;
                case SWT.FocusOut:
                    onFocus(event);
                    break;
                case SWT.KeyDown:
                    onKeyDown(event);
                    break;
                case SWT.MenuDetect:
                    onMenuDetect(event);
                    break;
                case SWT.MouseDoubleClick:
                    onMouseDoubleClick(event);
                    break;
                case SWT.MouseDown:
                    onMouse(event);
                    break;
                case SWT.MouseEnter:
                    onMouse(event);
                    break;
                case SWT.MouseExit:
                    onMouse(event);
                    break;
                case SWT.MouseHover:
                    onMouse(event);
                    break;
                case SWT.MouseMove:
                    onMouse(event);
                    break;
                case SWT.MouseUp:
                    onMouse(event);
                    break;
                case SWT.Paint:
                    onPaint(event);
                    break;
                case SWT.Resize:
                    onResize(event);
                    break;
                case SWT.Traverse:
                    onTraverse(event);
                    break;
                case SWT.Selection:
                    onSelection(event);
                    break;
                case SWT.Activate:
                    onActivate(event);
                    break;
                case SWT.Deactivate:
                    onDeactivate(event);
                    break;
            }
        };
        int[] folderEvents = new int[] { SWT.Dispose, SWT.DragDetect, SWT.FocusIn, SWT.FocusOut, SWT.KeyDown, SWT.MenuDetect, SWT.MouseDoubleClick, SWT.MouseDown, SWT.MouseEnter, SWT.MouseExit, SWT.MouseHover, SWT.MouseMove, SWT.MouseUp, SWT.Paint, SWT.Resize, SWT.Traverse, SWT.Activate, SWT.Deactivate };
        for (int folderEvent : folderEvents) {
            addListener(folderEvent, listener);
        }
        initAccessible();
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
        checkWidget();
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        // add to array
        CTabFolder2Listener[] newListeners = new CTabFolder2Listener[folderListeners.length + 1];
        System.arraycopy(folderListeners, 0, newListeners, 0, folderListeners.length);
        folderListeners = newListeners;
        folderListeners[folderListeners.length - 1] = listener;
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
        checkWidget();
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        // add to array
        CTabFolderListener[] newTabListeners = new CTabFolderListener[tabListeners.length + 1];
        System.arraycopy(tabListeners, 0, newTabListeners, 0, tabListeners.length);
        tabListeners = newTabListeners;
        tabListeners[tabListeners.length - 1] = listener;
        // display close button to be backwards compatible
        if (!showClose) {
            showClose = true;
            updateFolder(REDRAW);
        }
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
        addTypedListener(listener, SWT.Selection, SWT.DefaultSelection);
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
    @Override
    public Rectangle computeTrim(int x, int y, int width, int height) {
        checkWidget();
        Rectangle trim = renderer.computeTrim(CTabFolderRenderer.PART_BODY, SWT.NONE, x, y, width, height);
        Point size = new Point(width, height);
        int wrapHeight = getWrappedHeight(size);
        if (onBottom) {
            trim.height += wrapHeight;
        } else {
            trim.y -= wrapHeight;
            trim.height += wrapHeight;
        }
        return trim;
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
        return getValue().borderVisible;
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
    @Override
    public Rectangle getClientArea() {
        checkWidget();
        //TODO: HACK - find a better way to get padding
        Rectangle trim = renderer.computeTrim(CTabFolderRenderer.PART_BODY, SWT.FILL, 0, 0, 0, 0);
        Point size = getSize();
        int wrapHeight = getWrappedHeight(size);
        if (onBottom) {
            trim.height += wrapHeight;
        } else {
            trim.y -= wrapHeight;
            trim.height += wrapHeight;
        }
        if (minimized)
            return new Rectangle(-trim.x, -trim.y, 0, 0);
        int width = size.x - trim.width;
        int height = size.y - trim.height;
        return new Rectangle(-trim.x, -trim.y, width, height);
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
        /*
	 * This call is intentionally commented out, to allow this getter method to be
	 * called from a thread which is different from one that created the widget.
	 */
        //checkWidget();
        if (index < 0 || index >= items.length)
            SWT.error(SWT.ERROR_INVALID_RANGE);
        return items[index];
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
        /*
	 * This call is intentionally commented out, to allow this getter method to be
	 * called from a thread which is different from one that created the widget.
	 */
        //checkWidget();
        if (items.length == 0)
            return null;
        runUpdate();
        Point size = getSize();
        Rectangle trim = renderer.computeTrim(CTabFolderRenderer.PART_BORDER, SWT.NONE, 0, 0, 0, 0);
        if (size.x <= trim.width)
            return null;
        for (int element : priority) {
            CTabItem item = items[element];
            Rectangle rect = item.getBounds();
            if (rect.contains(pt))
                return item;
        }
        return null;
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
        //checkWidget();
        return items.length;
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
        /*
	 * This call is intentionally commented out, to allow this getter method to be
	 * called from a thread which is different from one that created the widget.
	 */
        //checkWidget();
        CTabItem[] tabItems = new CTabItem[items.length];
        System.arraycopy(items, 0, tabItems, 0, items.length);
        return tabItems;
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
        return getValue().minimized;
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
        return getValue().minimizeVisible;
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
        return getValue().minimumCharacters;
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
        return getValue().maximized;
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
        return getValue().maximizeVisible;
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
        return getValue().mRUVisible;
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
        return getValue().renderer;
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
        return getValue().selection;
        /*
	 * This call is intentionally commented out, to allow this getter method to be
	 * called from a thread which is different from one that created the widget.
	 */
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
        return getValue().selectionBackground;
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
        return getValue().selectionForeground;
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
        /*
	 * This call is intentionally commented out, to allow this getter method to be
	 * called from a thread which is different from one that created the widget.
	 */
        //checkWidget();
        return selectedIndex;
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
        return getValue().simple;
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
        return getValue().single;
    }

    @Override
    public int getStyle() {
        int style = super.getStyle();
        style &= ~(SWT.TOP | SWT.BOTTOM);
        style |= onBottom ? SWT.BOTTOM : SWT.TOP;
        style &= ~(SWT.SINGLE | SWT.MULTI);
        style |= single ? SWT.SINGLE : SWT.MULTI;
        if (borderVisible)
            style |= SWT.BORDER;
        style &= ~SWT.CLOSE;
        if (showClose)
            style |= SWT.CLOSE;
        return style;
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
        return getValue().tabHeight;
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
        return getValue().tabPosition;
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
        return getValue().topRight;
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
        checkWidget();
        return topRightAlignment;
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
        return getValue().unselectedCloseVisible;
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
        return getValue().unselectedImageVisible;
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
        return getValue().selectedImageVisible;
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
        checkWidget();
        if (item == null) {
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        }
        for (int i = 0; i < items.length; i++) {
            if (items[i] == item)
                return i;
        }
        return -1;
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
        checkWidget();
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (folderListeners.length == 0)
            return;
        int index = -1;
        for (int i = 0; i < folderListeners.length; i++) {
            if (listener == folderListeners[i]) {
                index = i;
                break;
            }
        }
        if (index == -1)
            return;
        if (folderListeners.length == 1) {
            folderListeners = new CTabFolder2Listener[0];
            return;
        }
        CTabFolder2Listener[] newTabListeners = new CTabFolder2Listener[folderListeners.length - 1];
        System.arraycopy(folderListeners, 0, newTabListeners, 0, index);
        System.arraycopy(folderListeners, index + 1, newTabListeners, index, folderListeners.length - index - 1);
        folderListeners = newTabListeners;
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
        checkWidget();
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (tabListeners.length == 0)
            return;
        int index = -1;
        for (int i = 0; i < tabListeners.length; i++) {
            if (listener == tabListeners[i]) {
                index = i;
                break;
            }
        }
        if (index == -1)
            return;
        if (tabListeners.length == 1) {
            tabListeners = new CTabFolderListener[0];
            return;
        }
        CTabFolderListener[] newTabListeners = new CTabFolderListener[tabListeners.length - 1];
        System.arraycopy(tabListeners, 0, newTabListeners, 0, index);
        System.arraycopy(tabListeners, index + 1, newTabListeners, index, tabListeners.length - index - 1);
        tabListeners = newTabListeners;
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
        checkWidget();
        if (listener == null) {
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        }
        removeTypedListener(SWT.Selection, listener);
        removeTypedListener(SWT.DefaultSelection, listener);
    }

    @Override
    public void reskin(int flags) {
        super.reskin(flags);
        for (CTabItem item : items) {
            item.reskin(flags);
        }
    }

    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        //TODO: need better caching strategy
        ((SwtCTabFolderRenderer) renderer.getImpl()).createAntialiasColors();
        updateBkImages(true);
        redraw();
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
        setBackground(colors, percents, false);
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
        checkWidget();
        if (colors != null) {
            if (percents == null || percents.length != colors.length - 1) {
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
            }
            for (int i = 0; i < percents.length; i++) {
                if (percents[i] < 0 || percents[i] > 100) {
                    SWT.error(SWT.ERROR_INVALID_ARGUMENT);
                }
                if (i > 0 && percents[i] < percents[i - 1]) {
                    SWT.error(SWT.ERROR_INVALID_ARGUMENT);
                }
            }
            if (getDisplay().getDepth() < 15) {
                // Don't use gradients on low color displays
                colors = new Color[] { colors[colors.length - 1] };
                percents = new int[] {};
            }
        }
        // Are these settings the same as before?
        if ((gradientColors != null) && (colors != null) && (gradientColors.length == colors.length)) {
            boolean same = false;
            for (int i = 0; i < gradientColors.length; i++) {
                if (gradientColors[i] == null) {
                    same = colors[i] == null;
                } else {
                    same = gradientColors[i].equals(colors[i]);
                }
                if (!same)
                    break;
            }
            if (same) {
                for (int i = 0; i < gradientPercents.length; i++) {
                    same = gradientPercents[i] == percents[i];
                    if (!same)
                        break;
                }
            }
            if (same && this.gradientVertical == vertical)
                return;
        }
        // Store the new settings
        if (colors == null) {
            gradientColors = null;
            gradientPercents = null;
            gradientVertical = false;
            setBackground((Color) null);
        } else {
            gradientColors = new Color[colors.length];
            for (int i = 0; i < colors.length; ++i) {
                gradientColors[i] = colors[i];
            }
            gradientPercents = new int[percents.length];
            for (int i = 0; i < percents.length; ++i) {
                gradientPercents[i] = percents[i];
            }
            gradientVertical = vertical;
            setBackground(gradientColors[gradientColors.length - 1]);
        }
        // Refresh with the new settings
        redraw();
    }

    @Override
    public void setBackgroundImage(Image image) {
        super.setBackgroundImage(image);
        //TODO: need better caching strategy
        ((SwtCTabFolderRenderer) renderer.getImpl()).createAntialiasColors();
        redraw();
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
        getValue().borderVisible = show;
        getBridge().dirty(this);
    }

    @Override
    public boolean setFocus() {
        checkWidget();
        /*
	* Feature in SWT.  When a new tab item is selected
	* and the previous tab item had focus, removing focus
	* from the previous tab item causes fixFocus() to give
	* focus to the first child, which is usually one of the
	* toolbars. This is unexpected.
	* The fix is to try to set focus on the first tab item
	* if fixFocus() is called.
	*/
        Control focusControl = getDisplay().getFocusControl();
        boolean fixFocus = isAncestor(focusControl);
        if (fixFocus) {
            CTabItem item = getSelection();
            if (item != null) {
                if (((SwtCTabItem) item.getImpl()).setFocus())
                    return true;
            }
        }
        return super.setFocus();
    }

    @Override
    public void setFont(Font font) {
        checkWidget();
        if (font != null && font.equals(getFont()))
            return;
        super.setFont(font);
        oldFont = getFont();
        // Chevron painting is cached as image and only recreated if number of hidden tabs changed.
        // To apply the new font the cached image must be recreated with new font.
        // Redraw request alone would only redraw the cached image with old font.
        // renderer will pickup and adjust(!) the new font automatically
        ((SwtCTabFolderRenderer) renderer.getImpl()).resetChevronFont();
        updateChevronImage(true);
        updateFolder(REDRAW);
    }

    @Override
    public void setForeground(Color color) {
        super.setForeground(color);
        // Chevron painting is cached as image and only recreated if number of hidden tabs changed.
        // To apply the new foreground color the image must be recreated with new foreground color.
        // redraw() alone would only redraw the cached image with old color.
        updateChevronImage(true);
        updateMaxImage();
        updateMinImage();
        redraw();
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
        checkWidget();
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
        checkWidget();
        if (index < -1 || index >= getItemCount()) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
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
        getValue().maximizeVisible = visible;
        getBridge().dirty(this);
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
    @Override
    public void setLayout(Layout layout) {
        checkWidget();
        return;
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
        getValue().maximized = maximize;
        getBridge().dirty(this);
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
        getValue().minimizeVisible = visible;
        getBridge().dirty(this);
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
        getValue().minimized = minimize;
        getBridge().dirty(this);
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
        getValue().minimumCharacters = count;
        getBridge().dirty(this);
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
        getValue().mRUVisible = show;
        getBridge().dirty(this);
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
        getValue().renderer = renderer;
        getBridge().dirty(this);
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
        getValue().selection = item;
        getBridge().dirty(this);
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
        getValue().selection = index;
        getBridge().dirty(this);
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
        getValue().selectionBackground = color;
        getBridge().dirty(this);
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
        getValue().selectionBackground = colors;
        getBridge().dirty(this);
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
        getValue().selectionBackground = colors;
        getBridge().dirty(this);
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
        getValue().selectionBackground = image;
        getBridge().dirty(this);
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
        getValue().selectionForeground = color;
        getBridge().dirty(this);
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
        checkWidget();
        if (thickness < 0) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        this.selectionHighlightBarThickness = thickness;
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
        getValue().simple = simple;
        getBridge().dirty(this);
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
        getValue().single = single;
        getBridge().dirty(this);
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
        getValue().tabHeight = height;
        getBridge().dirty(this);
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
        getValue().tabPosition = position;
        getBridge().dirty(this);
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
        getValue().topRight = control;
        getBridge().dirty(this);
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
        getValue().topRight = control;
        getBridge().dirty(this);
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
        getValue().unselectedCloseVisible = visible;
        getBridge().dirty(this);
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
        getValue().unselectedImageVisible = visible;
        getBridge().dirty(this);
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
        getValue().selectedImageVisible = visible;
        getBridge().dirty(this);
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
        checkWidget();
        if (item == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (item.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        int index = indexOf(item);
        if (index == -1)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        int idx = -1;
        for (int i = 0; i < priority.length; i++) {
            if (priority[i] == index) {
                idx = i;
                break;
            }
        }
        if (mru) {
            // move to front of mru order
            int[] newPriority = new int[priority.length];
            System.arraycopy(priority, 0, newPriority, 1, idx);
            System.arraycopy(priority, idx + 1, newPriority, idx + 1, priority.length - idx - 1);
            newPriority[0] = index;
            priority = newPriority;
        }
        if (((SwtCTabItem) item.getImpl()).showing)
            return;
        updateFolder(REDRAW_TABS);
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
        checkWidget();
        if (selectedIndex != -1) {
            showItem(getSelection());
        }
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
        getValue().highlightEnabled = enabled;
        getBridge().dirty(this);
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
        return getValue().highlightEnabled;
    }

    public CTabFolder getApi() {
        if (api == null)
            api = CTabFolder.createApi(this);
        return (CTabFolder) api;
    }

    public VCTabFolder getValue() {
        if (value == null)
            value = new VCTabFolder();
        return (VCTabFolder) value;
    }
}
