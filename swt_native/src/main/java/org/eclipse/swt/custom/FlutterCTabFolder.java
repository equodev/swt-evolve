package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.CtabfolderSwtSizingConstants;
import org.eclipse.swt.internal.TabfolderSwtSizingConstants;
import org.eclipse.swt.values.CTabFolderValue;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FlutterComposite;
import org.eclipse.swt.widgets.FlutterSwt;
import org.eclipse.swt.widgets.FlutterSwt.ExpandPolicy;
import org.eclipse.swt.widgets.FlutterWidget;
import org.eclipse.swt.widgets.IComposite;
import org.eclipse.swt.widgets.IControl;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.TypedListener;
import org.eclipse.swt.widgets.Widget;

import java.util.ArrayList;

/**
 * Instances of this class implement the notebook user interface metaphor. It
 * allows the user to select a notebook page from set of pages.
 * <p>
 * The item children that may be added to instances of this class must be of
 * type <code>CTabItem</code>. <code>Control</code> children are created and
 * then set into a tab item using <code>CTabItem#setControl</code>.
 * </p>
 * <p>
 * Note that although this class is a subclass of <code>Composite</code>, it
 * does not make sense to set a layout on it.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>CLOSE, TOP, BOTTOM, FLAT, BORDER, SINGLE, MULTI</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * <dd>"CTabFolder2"</dd>
 * </dl>
 * Note: Only one of the styles TOP and BOTTOM may be specified.
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#ctabfolder">CTabFolder,
 *      CTabItem snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example:
 *      CustomControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class FlutterCTabFolder extends FlutterComposite implements ICTabFolder {

    /**
     * marginWidth specifies the number of points of horizontal margin that will be
     * placed along the left and right edges of the form.
     *
     * The default value is 0.
     */
    public int marginWidth = 0;

    /**
     * marginHeight specifies the number of points of vertical margin that will be
     * placed along the top and bottom edges of the form.
     *
     * The default value is 0.
     */
    public int marginHeight = 0;

    protected ExpandPolicy getExpandPolicy() {
        return ExpandPolicy.FOLLOW_W_PARENT;
    }

    /**
     * A multiple of the tab height that specifies the minimum width to which a tab
     * will be compressed before scrolling arrows are used to navigate the tabs.
     *
     * NOTE This field is badly named and can not be fixed for backwards
     * compatibility. It should not be capitalized.
     *
     * @deprecated This field is no longer used. See setMinimumCharacters(int)
     */
    @Deprecated
    public int MIN_TAB_WIDTH = 4;

    /**
     * Color of innermost line of drop shadow border.
     *
     * NOTE This field is badly named and can not be fixed for backwards
     * compatibility. It should be capitalized.
     *
     * @deprecated drop shadow border is no longer drawn in 3.0
     */
//    @Deprecated
//    public static RGB borderInsideRGB = new RGB(132, 130, 132);

    /**
     * Color of middle line of drop shadow border.
     *
     * NOTE This field is badly named and can not be fixed for backwards
     * compatibility. It should be capitalized.
     *
     * @deprecated drop shadow border is no longer drawn in 3.0
     */
//    @Deprecated
//    public static RGB borderMiddleRGB = new RGB(143, 141, 138);

    /**
     * Color of outermost line of drop shadow border.
     *
     * NOTE This field is badly named and can not be fixed for backwards
     * compatibility. It should be capitalized.
     *
     * @deprecated drop shadow border is no longer drawn in 3.0
     */
//    @Deprecated
//    public static RGB borderOutsideRGB = new RGB(171, 168, 165);

    // when disposing CTabFolder, don't try to layout the items or
    // keep track of size changes in order to redraw only affected area
    /**
     * Constructs a new instance of this class given its parent and a style value
     * describing its behavior and appearance.
     * <p>
     * The style value is either one of the style constants defined in class
     * <code>SWT</code> which is applicable to instances of this class, or must be
     * built by <em>bitwise OR</em>'ing together (that is, using the
     * <code>int</code> "|" operator) two or more of those <code>SWT</code> style
     * constants. The class description lists the style constants that are
     * applicable to the class. Style bits are also inherited from superclasses.
     * </p>
     *
     * @param parent a widget which will be the parent of the new instance (cannot
     *               be null)
     * @param style  the style of widget to construct
     *
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_NULL_ARGUMENT - if the parent
     *                                     is null</li>
     *                                     </ul>
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS - if not
     *                                     called from the thread that created the
     *                                     parent</li>
     *                                     </ul>
     *
     * @see SWT#TOP
     * @see SWT#BOTTOM
     * @see SWT#FLAT
     * @see SWT#BORDER
     * @see SWT#SINGLE
     * @see SWT#MULTI
     * @see #getStyle()
     */
    public FlutterCTabFolder(IComposite parent, int style) {
         super(parent, checkStyle(parent, style));
        children = new ArrayList<>();
//        init(style);
    }

    static int checkStyle(IComposite parent, int style) {
        int mask = SWT.CLOSE | SWT.TOP | SWT.BOTTOM | SWT.FLAT | SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT | SWT.SINGLE
                | SWT.MULTI;
        style = style & mask;
        // TOP and BOTTOM are mutually exclusive.
        // TOP is the default
        if ((style & SWT.TOP) != 0)
            style = style & ~SWT.BOTTOM;
        // SINGLE and MULTI are mutually exclusive.
        // MULTI is the default
        if ((style & SWT.MULTI) != 0)
            style = style & ~SWT.SINGLE;
        // reduce the flash by not redrawing the entire area on a Resize event
        style |= SWT.NO_REDRAW_RESIZE;
        // TEMPORARY CODE
        /*
         * In Right To Left orientation on Windows, all GC calls that use a brush are
         * drawing offset by one pixel. This results in some parts of the CTabFolder not
         * drawing correctly. To alleviate some of the appearance problems, allow the OS
         * to draw the background. This does not draw correctly but the result is less
         * obviously wrong.
         */
        if ((style & SWT.RIGHT_TO_LEFT) != 0)
            return style;
        if ((parent.getStyle() & SWT.MIRRORED) != 0 && (style & SWT.LEFT_TO_RIGHT) == 0)
            return style;
        return style | SWT.DOUBLE_BUFFERED;
    }

    /**
     * Adds the listener to the collection of listeners who will be notified when a
     * tab item is closed, minimized, maximized, restored, or to show the list of
     * items that are not currently visible.
     *
     * @param listener the listener which should be notified
     *
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_NULL_ARGUMENT - if the listener
     *                                     is null</li>
     *                                     </ul>
     *
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS when
     *                                     called from the wrong thread</li>
     *                                     <li>ERROR_WIDGET_DISPOSED when the widget
     *                                     has been disposed</li>
     *                                     </ul>
     *
     * @see CTabFolder2Listener
     * @see #removeCTabFolder2Listener(CTabFolder2Listener)
     *
     * @since 3.0
     */
    public void addCTabFolder2Listener(CTabFolder2Listener listener) {
//        checkWidget();
//        if (listener == null)
//            SWT.error(SWT.ERROR_NULL_ARGUMENT);
//        // add to array
//        CTabFolder2Listener[] newListeners = new CTabFolder2Listener[folderListeners.length + 1];
//        System.arraycopy(folderListeners, 0, newListeners, 0, folderListeners.length);
//        folderListeners = newListeners;
//        folderListeners[folderListeners.length - 1] = listener;
    }

    /**
     * Adds the listener to the collection of listeners who will be notified when a
     * tab item is closed.
     *
     * @param listener the listener which should be notified
     *
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_NULL_ARGUMENT - if the listener
     *                                     is null</li>
     *                                     </ul>
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS when
     *                                     called from the wrong thread</li>
     *                                     <li>ERROR_WIDGET_DISPOSED when the widget
     *                                     has been disposed</li>
     *                                     </ul>
     *
     * @see CTabFolderListener
     * @see #removeCTabFolderListener(CTabFolderListener)
     *
     * @deprecated use addCTabFolder2Listener(CTabFolder2Listener)
     */
    @Deprecated
    public void addCTabFolderListener(CTabFolderListener listener) {
//        checkWidget();
//        if (listener == null)
//            SWT.error(SWT.ERROR_NULL_ARGUMENT);
//        // add to array
//        CTabFolderListener[] newTabListeners = new CTabFolderListener[tabListeners.length + 1];
//        System.arraycopy(tabListeners, 0, newTabListeners, 0, tabListeners.length);
//        tabListeners = newTabListeners;
//        tabListeners[tabListeners.length - 1] = listener;
//        // display close button to be backwards compatible
//        if (!showClose) {
//            showClose = true;
//            updateFolder(REDRAW);
//        }
    }

    /**
     * Adds the listener to the collection of listeners who will be notified when
     * the user changes the receiver's selection, by sending it one of the messages
     * defined in the <code>SelectionListener</code> interface.
     * <p>
     * <code>widgetSelected</code> is called when the user changes the selected tab.
     * <code>widgetDefaultSelected</code> is not called.
     * </p>
     *
     * @param listener the listener which should be notified when the user changes
     *                 the receiver's selection
     *
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_NULL_ARGUMENT - if the listener
     *                                     is null</li>
     *                                     </ul>
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_WIDGET_DISPOSED - if the
     *                                     receiver has been disposed</li>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS - if not
     *                                     called from the thread that created the
     *                                     receiver</li>
     *                                     </ul>
     *
     * @see SelectionListener
     * @see #removeSelectionListener
     * @see SelectionEvent
     */
    public void addSelectionListener(SelectionListener listener) {
        checkWidget();
        if (listener == null) {
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        }
        TypedListener typedListener = new TypedListener(listener);
        addListener(SWT.Selection, typedListener);
        addListener(SWT.DefaultSelection, typedListener);
    }

    /*
     * This class was not intended to be subclassed but this restriction cannot be
     * enforced without breaking backward compatibility.
     */
    // protected void checkSubclass () {
    // String name = getClass ().getName ();
    // int index = name.lastIndexOf ('.');
    // if (!name.substring (0, index + 1).equals ("org.eclipse.swt.custom.")) {
    // SWT.error (SWT.ERROR_INVALID_SUBCLASS);
    // }
    // }
    @Override
    public Rectangle computeTrim(int x, int y, int width, int height) {
        return parentComposite.computeTrim(x, y, width, height);
    }

    /**
     * Returns <code>true</code> if the receiver's border is visible.
     *
     * @return the receiver's border visibility state
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     *
     * @since 3.0
     */
    public boolean getBorderVisible() {
        return builder().getBorderVisible().orElse(false);
    }

    @Override
    public Rectangle getClientArea() {
        Point parentSize = parentComposite.getSize();
        Point childSize = childComposite.computeSize(-1, -1, false);
        Rectangle clientArea = new Rectangle(0, 0, Math.max(parentSize.x, childSize.x), Math.max(parentSize.y, childSize.y));
        return clientArea;
    }

    /**
     * Return the tab that is located at the specified index.
     *
     * @param index the index of the tab item
     * @return the item at the specified index
     *
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_INVALID_RANGE - if the index is
     *                                     out of range</li>
     *                                     </ul>
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS when
     *                                     called from the wrong thread</li>
     *                                     <li>ERROR_WIDGET_DISPOSED when the widget
     *                                     has been disposed</li>
     *                                     </ul>
     */
    public ICTabItem getItem(int index) {
        if (children != null && children.size() > index) {
            return (FlutterCTabItem)children.get(index);
        }
        return null;
        /*
         * This call is intentionally commented out, to allow this getter method to be
         * called from a thread which is different from one that created the widget.
         */
    }

    /**
     * Gets the item at a point in the widget.
     *
     * @param pt the point in coordinates relative to the CTabFolder
     * @return the item at a point or null
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                         wrong thread</li>
     *                         <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                         disposed</li>
     *                         </ul>
     */
    public ICTabItem getItem(Point pt) {
        return null;
        /*
         * This call is intentionally commented out, to allow this getter method to be
         * called from a thread which is different from one that created the widget.
         */
    }

    /**
     * Return the number of tabs in the folder.
     *
     * @return the number of tabs in the folder
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                         wrong thread</li>
     *                         <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                         disposed</li>
     *                         </ul>
     */
    public int getItemCount() {
        return -1;
    }

    /**
     * Return the tab items.
     *
     * @return the tab items
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                         wrong thread</li>
     *                         <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                         disposed</li>
     *                         </ul>
     */
    public ICTabItem[] getItems() {
        return children == null ? new ICTabItem[0] : children.stream().filter(ICTabItem.class::isInstance).toArray(ICTabItem[]::new);
        /*
         * This call is intentionally commented out, to allow this getter method to be
         * called from a thread which is different from one that created the widget.
         */
    }

    /**
     * Returns <code>true</code> if the receiver is minimized.
     *
     * @return the receiver's minimized state
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     *
     * @since 3.0
     */
    public boolean getMinimized() {
        return builder().getMinimized().orElse(false);
    }

    /**
     * Returns <code>true</code> if the minimize button is visible.
     *
     * @return the visibility of the minimized button
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     *
     * @since 3.0
     */
    public boolean getMinimizeVisible() {
        return builder().getMinimizeVisible().orElse(false);
    }

    /**
     * Returns the number of characters that will appear in a fully compressed tab.
     *
     * @return number of characters that will appear in a fully compressed tab
     *
     * @since 3.0
     */
    public int getMinimumCharacters() {
        return builder().getMinimumCharacters().orElse(0);
    }

    /**
     * Returns <code>true</code> if the receiver is maximized.
     *
     * @return the receiver's maximized state
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     *
     * @since 3.0
     */
    public boolean getMaximized() {
        return builder().getMaximized().orElse(false);
    }

    /**
     * Returns <code>true</code> if the maximize button is visible.
     *
     * @return the visibility of the maximized button
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     *
     * @since 3.0
     */
    public boolean getMaximizeVisible() {
        return builder().getMaximizeVisible().orElse(false);
    }

    /**
     * Returns <code>true</code> if the receiver displays most recently used tabs
     * and <code>false</code> otherwise.
     * <p>
     * When there is not enough horizontal space to show all the tabs, by default,
     * tabs are shown sequentially from left to right in order of their index. When
     * the MRU visibility is turned on, the tabs that are visible will be the tabs
     * most recently selected. Tabs will still maintain their left to right order
     * based on index but only the most recently selected tabs are visible.
     * <p>
     * For example, consider a CTabFolder that contains "Tab 1", "Tab 2", "Tab 3"
     * and "Tab 4" (in order by index). The user selects "Tab 1" and then "Tab 3".
     * If the CTabFolder is now compressed so that only two tabs are visible, by
     * default, "Tab 2" and "Tab 3" will be shown ("Tab 3" since it is currently
     * selected and "Tab 2" because it is the previous item in index order). If MRU
     * visibility is enabled, the two visible tabs will be "Tab 1" and "Tab 3" (in
     * that order from left to right).
     * </p>
     *
     * @return the receiver's header's visibility state
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     *
     * @since 3.1
     */
    public boolean getMRUVisible() {
        return builder().getMRUVisible().orElse(false);
    }
//
//    /**
//     *  Returns the receiver's renderer.
//     *
//     *  @return the receiver's renderer
//     *
//     *  @exception SWTException <ul>
//     * 		<li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
//     * 		<li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
//     * 	</ul>
//     *
//     *  @see #setRenderer(CTabFolderRenderer)
//     *  @see CTabFolderRenderer
//     *
//     *  @since 3.6
//     */
//    public CTabFolderRenderer getRenderer() {
//        return null;
//    }

    /**
     * Return the selected tab item, or null if there is no selection.
     *
     * @return the selected tab item, or null if none has been selected
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                         wrong thread</li>
     *                         <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                         disposed</li>
     *                         </ul>
     */
    public ICTabItem getSelection() {
        return null;
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
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     *
     * @since 3.0
     */
    public Color getSelectionBackground() {
        return null;
    }

    /**
     * Returns the receiver's selection foreground color.
     *
     * @return the selection foreground color of the receiver
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     *
     * @since 3.0
     */
    public Color getSelectionForeground() {
        return null;
    }

    /**
     * Return the index of the selected tab item, or -1 if there is no selection.
     *
     * @return the index of the selected tab item or -1
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                         wrong thread</li>
     *                         <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                         disposed</li>
     *                         </ul>
     */
    public int getSelectionIndex() {
        return builder().getSelectionIndex().orElse(-1);
        /*
         * This call is intentionally commented out, to allow this getter method to be
         * called from a thread which is different from one that created the widget.
         */
    }

    /**
     * Returns <code>true</code> if the CTabFolder is rendered with a simple,
     * traditional shape.
     *
     * @return <code>true</code> if the CTabFolder is rendered with a simple shape
     *
     * @since 3.0
     */
    public boolean getSimple() {
        return builder().getSimple().orElse(false);
    }

    /**
     * Returns <code>true</code> if the CTabFolder only displays the selected tab
     * and <code>false</code> if the CTabFolder displays multiple tabs.
     *
     * @return <code>true</code> if the CTabFolder only displays the selected tab
     *         and <code>false</code> if the CTabFolder displays multiple tabs
     *
     * @since 3.0
     */
    public boolean getSingle() {
        return builder().getSingle().orElse(false);
    }

    @Override
    public int getStyle() {
        return builder().getStyle().orElse(0);
    }

    /**
     * Returns the height of the tab
     *
     * @return the height of the tab
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                         wrong thread</li>
     *                         <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                         disposed</li>
     *                         </ul>
     */
    public int getTabHeight() {
        return builder().getTabHeight().orElse(0);
    }

    /**
     * Returns the position of the tab. Possible values are SWT.TOP or SWT.BOTTOM.
     *
     * @return the position of the tab
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                         wrong thread</li>
     *                         <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                         disposed</li>
     *                         </ul>
     */
    public int getTabPosition() {
        return builder().getTabPosition().orElse(0);
    }

    /**
     * Returns the control in the top right corner of the tab folder. Typically this
     * is a close button or a composite with a menu and close button.
     *
     * @return the control in the top right corner of the tab folder or null
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                         wrong thread</li>
     *                         <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                         disposed</li>
     *                         </ul>
     *
     * @since 2.1
     */
    public IControl getTopRight() {
        return topRight;
    }

    /**
     * Returns the alignment of the top right control.
     *
     * @return the alignment of the top right control which is either
     *         <code>SWT.RIGHT</code> or <code>SWT.FILL</code>
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                         wrong thread</li>
     *                         <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                         disposed</li>
     *                         </ul>
     *
     * @since 3.6
     */
    public int getTopRightAlignment() {
        return -1;
    }

    /**
     * Returns <code>true</code> if the close button appears when the user hovers
     * over an unselected tabs.
     *
     * @return <code>true</code> if the close button appears on unselected tabs
     *
     * @since 3.0
     */
    public boolean getUnselectedCloseVisible() {
        return builder().getUnselectedCloseVisible().orElse(false);
    }

    /**
     * Returns <code>true</code> if an image appears in unselected tabs.
     *
     * @return <code>true</code> if an image appears in unselected tabs
     *
     * @since 3.0
     */
    public boolean getUnselectedImageVisible() {
        return builder().getUnselectedImageVisible().orElse(false);
    }

    /**
     * Return the index of the specified tab or -1 if the tab is not in the
     * receiver.
     *
     * @param item the tab item for which the index is required
     *
     * @return the index of the specified tab item or -1
     *
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_NULL_ARGUMENT - if the listener
     *                                     is null</li>
     *                                     </ul>
     *
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS when
     *                                     called from the wrong thread</li>
     *                                     <li>ERROR_WIDGET_DISPOSED when the widget
     *                                     has been disposed</li>
     *                                     </ul>
     */
    public int indexOf(ICTabItem item) {
        if (!(item instanceof FlutterWidget) || children == null) return -1;
        for (int i = 0; i < children.size(); ++i) {
            if (children.get(i) == item) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Removes the listener.
     *
     * @param listener the listener which should no longer be notified
     *
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_NULL_ARGUMENT - if the listener
     *                                     is null</li>
     *                                     </ul>
     *
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS when
     *                                     called from the wrong thread</li>
     *                                     <li>ERROR_WIDGET_DISPOSED when the widget
     *                                     has been disposed</li>
     *                                     </ul>
     *
     * @see #addCTabFolder2Listener(CTabFolder2Listener)
     *
     * @since 3.0
     */
    public void removeCTabFolder2Listener(CTabFolder2Listener listener) {
//        checkWidget();
//        if (listener == null)
//            SWT.error(SWT.ERROR_NULL_ARGUMENT);
//        if (folderListeners.length == 0)
//            return;
//        int index = -1;
//        for (int i = 0; i < folderListeners.length; i++) {
//            if (listener == folderListeners[i]) {
//                index = i;
//                break;
//            }
//        }
//        if (index == -1)
//            return;
//        if (folderListeners.length == 1) {
//            folderListeners = new CTabFolder2Listener[0];
//            return;
//        }
//        CTabFolder2Listener[] newTabListeners = new CTabFolder2Listener[folderListeners.length - 1];
//        System.arraycopy(folderListeners, 0, newTabListeners, 0, index);
//        System.arraycopy(folderListeners, index + 1, newTabListeners, index, folderListeners.length - index - 1);
//        folderListeners = newTabListeners;
    }

    /**
     * Removes the listener.
     *
     * @param listener the listener which should no longer be notified
     *
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_NULL_ARGUMENT - if the listener
     *                                     is null</li>
     *                                     </ul>
     *
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS when
     *                                     called from the wrong thread</li>
     *                                     <li>ERROR_WIDGET_DISPOSED when the widget
     *                                     has been disposed</li>
     *                                     </ul>
     *
     * @deprecated see removeCTabFolderCloseListener(CTabFolderListener)
     */
    @Deprecated
    public void removeCTabFolderListener(CTabFolderListener listener) {
//        checkWidget();
//        if (listener == null)
//            SWT.error(SWT.ERROR_NULL_ARGUMENT);
//        if (tabListeners.length == 0)
//            return;
//        int index = -1;
//        for (int i = 0; i < tabListeners.length; i++) {
//            if (listener == tabListeners[i]) {
//                index = i;
//                break;
//            }
//        }
//        if (index == -1)
//            return;
//        if (tabListeners.length == 1) {
//            tabListeners = new CTabFolderListener[0];
//            return;
//        }
//        CTabFolderListener[] newTabListeners = new CTabFolderListener[tabListeners.length - 1];
//        System.arraycopy(tabListeners, 0, newTabListeners, 0, index);
//        System.arraycopy(tabListeners, index + 1, newTabListeners, index, tabListeners.length - index - 1);
//        tabListeners = newTabListeners;
    }

    /**
     * Removes the listener from the collection of listeners who will be notified
     * when the user changes the receiver's selection.
     *
     * @param listener the listener which should no longer be notified
     *
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_NULL_ARGUMENT - if the listener
     *                                     is null</li>
     *                                     </ul>
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_WIDGET_DISPOSED - if the
     *                                     receiver has been disposed</li>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS - if not
     *                                     called from the thread that created the
     *                                     receiver</li>
     *                                     </ul>
     *
     * @see SelectionListener
     * @see #addSelectionListener
     */
    public void removeSelectionListener(SelectionListener listener) {
        checkWidget();
        if (listener == null) {
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        }
        removeListener(SWT.Selection, listener);
        removeListener(SWT.DefaultSelection, listener);
    }

    @Override
    public void reskin(int flags) {
    }

    @Override
    public void setBackground(Color color) {
    }

    /**
     * Specify a gradient of colors to be drawn in the background of the unselected
     * tabs. For example to draw a gradient that varies from dark blue to blue and
     * then to white, use the following call to setBackground:
     * 
     * <pre>
     * cfolder.setBackground(
     *         new Color[] { display.getSystemColor(SWT.COLOR_DARK_BLUE), display.getSystemColor(SWT.COLOR_BLUE),
     *                 display.getSystemColor(SWT.COLOR_WHITE), display.getSystemColor(SWT.COLOR_WHITE) },
     *         new int[] { 25, 50, 100 });
     * </pre>
     *
     * @param colors   an array of Color that specifies the colors to appear in the
     *                 gradient in order of appearance left to right. The value
     *                 <code>null</code> clears the background gradient. The value
     *                 <code>null</code> can be used inside the array of Color to
     *                 specify the background color.
     * @param percents an array of integers between 0 and 100 specifying the percent
     *                 of the width of the widget at which the color should change.
     *                 The size of the <code>percents</code> array must be one less
     *                 than the size of the <code>colors</code> array.
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                         wrong thread</li>
     *                         <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                         disposed</li>
     *                         </ul>
     *
     * @since 3.6
     */
    public void setBackground(Color[] colors, int[] percents) {
    }

    /**
     * Specify a gradient of colors to be drawn in the background of the unselected
     * tab. For example to draw a vertical gradient that varies from dark blue to
     * blue and then to white, use the following call to setBackground:
     * 
     * <pre>
     * cfolder.setBackground(
     *         new Color[] { display.getSystemColor(SWT.COLOR_DARK_BLUE), display.getSystemColor(SWT.COLOR_BLUE),
     *                 display.getSystemColor(SWT.COLOR_WHITE), display.getSystemColor(SWT.COLOR_WHITE) },
     *         new int[] { 25, 50, 100 }, true);
     * </pre>
     *
     * @param colors   an array of Color that specifies the colors to appear in the
     *                 gradient in order of appearance left to right. The value
     *                 <code>null</code> clears the background gradient. The value
     *                 <code>null</code> can be used inside the array of Color to
     *                 specify the background color.
     * @param percents an array of integers between 0 and 100 specifying the percent
     *                 of the width of the widget at which the color should change.
     *                 The size of the <code>percents</code> array must be one less
     *                 than the size of the <code>colors</code> array.
     *
     * @param vertical indicate the direction of the gradient. <code>True</code> is
     *                 vertical and <code>false</code> is horizontal.
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                         wrong thread</li>
     *                         <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                         disposed</li>
     *                         </ul>
     *
     * @since 3.6
     */
    public void setBackground(Color[] colors, int[] percents, boolean vertical) {
    }

    @Override
    public void setBackgroundImage(Image image) {
    }

    /**
     * Toggle the visibility of the border
     *
     * @param show true if the border should be displayed
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     */
    public void setBorderVisible(boolean show) {
        builder().setBorderVisible(show);
        FlutterSwt.dirty(this);
    }

    @Override
    public boolean setFocus() {
        return false;
    }

    @Override
    public void setFont(Font font) {
        // Chevron painting is cached as image and only recreated if number of hidden
        // tabs changed.
        // To apply the new font the cached image must be recreated with new font.
        // Redraw request alone would only redraw the cached image with old font.
    }

    @Override
    public void setForeground(Color color) {
        // Chevron painting is cached as image and only recreated if number of hidden
        // tabs changed.
        // To apply the new foreground color the image must be recreated with new
        // foreground color.
    }

    /**
     * Display an insert marker before or after the specified tab item.
     *
     * A value of null will clear the mark.
     *
     * @param item  the item with which the mark is associated or null
     *
     * @param after true if the mark should be displayed after the specified item
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     */
    public void setInsertMark(ICTabItem item, boolean after) {
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
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_INVALID_ARGUMENT when the index
     *                                     is invalid</li>
     *                                     </ul>
     *
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_WIDGET_DISPOSED - if the
     *                                     receiver has been disposed</li>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS - if not
     *                                     called from the thread that created the
     *                                     receiver</li>
     *                                     </ul>
     */
    public void setInsertMark(int index, boolean after) {
    }

    /**
     * Reorder the items of the receiver.
     * 
     * @param indices an array containing the new indices for all items
     *
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_NULL_ARGUMENT - if the indices
     *                                     array is null</li>
     *                                     <li>ERROR_INVALID_ARGUMENT - if the
     *                                     indices array is not the same length as
     *                                     the number of items, if there are
     *                                     duplicate indices or an index is out of
     *                                     range.</li>
     *                                     </ul>
     *
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_WIDGET_DISPOSED - if the
     *                                     receiver has been disposed</li>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS - if not
     *                                     called from the thread that created the
     *                                     receiver</li>
     *                                     </ul>
     */
    /**
     * Marks the receiver's maximize button as visible if the argument is
     * <code>true</code>, and marks it invisible otherwise.
     *
     * @param visible the new visibility state
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     *
     * @since 3.0
     */
    public void setMaximizeVisible(boolean visible) {
        builder().setMaximizeVisible(visible);
        FlutterSwt.dirty(this);
    }

    /**
     * Sets the layout which is associated with the receiver to be the argument
     * which may be null.
     * <p>
     * Note: No Layout can be set on this Control because it already manages the
     * size and position of its children.
     * </p>
     *
     * @param layout the receiver's new layout or null
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     */
    @Override
    public void setLayout(Layout layout) {
    }

    /**
     * Sets the maximized state of the receiver.
     *
     * @param maximize the new maximized state
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     *
     * @since 3.0
     */
    public void setMaximized(boolean maximize) {
        builder().setMaximized(maximize);
        FlutterSwt.dirty(this);
    }

    /**
     * Marks the receiver's minimize button as visible if the argument is
     * <code>true</code>, and marks it invisible otherwise.
     *
     * @param visible the new visibility state
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     *
     * @since 3.0
     */
    public void setMinimizeVisible(boolean visible) {
        builder().setMinimizeVisible(visible);
        FlutterSwt.dirty(this);
    }

    /**
     * Sets the minimized state of the receiver.
     *
     * @param minimize the new minimized state
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     *
     * @since 3.0
     */
    public void setMinimized(boolean minimize) {
        builder().setMinimized(minimize);
        FlutterSwt.dirty(this);
    }

    /**
     * Sets the minimum number of characters that will be displayed in a fully
     * compressed tab.
     *
     * @param count the minimum number of characters that will be displayed in a
     *              fully compressed tab
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         <li>ERROR_INVALID_RANGE - if the count is less than
     *                         zero</li>
     *                         </ul>
     *
     * @since 3.0
     */
    public void setMinimumCharacters(int count) {
        builder().setMinimumCharacters(count);
        FlutterSwt.dirty(this);
    }

    /**
     * When there is not enough horizontal space to show all the tabs, by default,
     * tabs are shown sequentially from left to right in order of their index. When
     * the MRU visibility is turned on, the tabs that are visible will be the tabs
     * most recently selected. Tabs will still maintain their left to right order
     * based on index but only the most recently selected tabs are visible.
     * <p>
     * For example, consider a CTabFolder that contains "Tab 1", "Tab 2", "Tab 3"
     * and "Tab 4" (in order by index). The user selects "Tab 1" and then "Tab 3".
     * If the CTabFolder is now compressed so that only two tabs are visible, by
     * default, "Tab 2" and "Tab 3" will be shown ("Tab 3" since it is currently
     * selected and "Tab 2" because it is the previous item in index order). If MRU
     * visibility is enabled, the two visible tabs will be "Tab 1" and "Tab 3" (in
     * that order from left to right).
     * </p>
     *
     * @param show the new visibility state
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     *
     * @since 3.1
     */
    public void setMRUVisible(boolean show) {
        builder().setMRUVisible(show);
        FlutterSwt.dirty(this);
    }
//
//    /**
//     *  Sets the renderer which is associated with the receiver to be
//     *  the argument which may be null. In the case of null, the default
//     *  renderer is used.
//     *
//     *  @param renderer a new renderer
//     *
//     *  @exception SWTException <ul>
//     * 		<li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
//     * 		<li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
//     * 	</ul>
//     *
//     *  @see CTabFolderRenderer
//     *
//     *  @since 3.6
//     */
//    public void setRenderer(CTabFolderRenderer renderer) {
//    }

    /**
     * Set the selection to the tab at the specified item.
     *
     * @param item the tab item to be selected
     *
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_NULL_ARGUMENT - if the item is
     *                                     null</li>
     *                                     </ul>
     *
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS when
     *                                     called from the wrong thread</li>
     *                                     <li>ERROR_WIDGET_DISPOSED when the widget
     *                                     has been disposed</li>
     *                                     </ul>
     */
    public void setSelection(ICTabItem item) {
        setSelection(indexOf(item));
    }

    /**
     * Set the selection to the tab at the specified index.
     *
     * @param index the index of the tab item to be selected
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     */
    public void setSelection(int index) {
        checkWidget();
        if (index < 0 || index >= children.size())
            return;
        FlutterCTabItem selection = (FlutterCTabItem) (children.get(index));
        if (getSelectionIndex() == index) {
            IControl itemControl = selection.getControl();
            display.asyncExec(() -> {
                if (itemControl != null
//                        && !itemControl.isVisible()
                        ) {
                    itemControl.setBounds(getClientArea());
                    itemControl.setVisible(true);
                    if (childComposite.getLayout() instanceof StackLayout layout) {
                        layout.topControl = Control.getInstance(itemControl);
                    }
                }
            });

            showItem(selection);
            return;
        }
        int oldIndex = getSelectionIndex();
        builder().setSelectionIndex(index);
        IControl newControl = selection.getControl();
        IControl oldControl = null;
        if (oldIndex != -1) {
            oldControl = ((FlutterCTabItem)children.get(oldIndex)).getControl();
        }
        if (newControl != oldControl) {
            if (newControl != null && !newControl.isDisposed()) {
                display.asyncExec(() -> {
                    newControl.setBounds(getClientArea());
                    newControl.setVisible(true);
                    if (childComposite.getLayout() instanceof StackLayout layout) {
                        layout.topControl = Control.getInstance(newControl);
                    }
                });
            }
            if (oldControl != null && !oldControl.isDisposed()) {
                final IControl fOldControl = oldControl;
                display.asyncExec(() -> {
                    fOldControl.setVisible(false);
                });
            }
        }
        showItem(selection);
    }

    /**
     * Sets the receiver's selection background color to the color specified by the
     * argument, or to the default system color for the control if the argument is
     * null.
     *
     * @param color the new color (or null)
     *
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_INVALID_ARGUMENT - if the
     *                                     argument has been disposed</li>
     *                                     </ul>
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_WIDGET_DISPOSED - if the
     *                                     receiver has been disposed</li>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS - if not
     *                                     called from the thread that created the
     *                                     receiver</li>
     *                                     </ul>
     *
     * @since 3.0
     */
    public void setSelectionBackground(Color color) {
    }

    /**
     * Specify a gradient of colours to be draw in the background of the selected
     * tab. For example to draw a gradient that varies from dark blue to blue and
     * then to white, use the following call to setBackground:
     * 
     * <pre>
     * cfolder.setBackground(
     *         new Color[] { display.getSystemColor(SWT.COLOR_DARK_BLUE), display.getSystemColor(SWT.COLOR_BLUE),
     *                 display.getSystemColor(SWT.COLOR_WHITE), display.getSystemColor(SWT.COLOR_WHITE) },
     *         new int[] { 25, 50, 100 });
     * </pre>
     *
     * @param colors   an array of Color that specifies the colors to appear in the
     *                 gradient in order of appearance left to right. The value
     *                 <code>null</code> clears the background gradient. The value
     *                 <code>null</code> can be used inside the array of Color to
     *                 specify the background color.
     * @param percents an array of integers between 0 and 100 specifying the percent
     *                 of the width of the widget at which the color should change.
     *                 The size of the percents array must be one less than the size
     *                 of the colors array.
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                         wrong thread</li>
     *                         <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                         disposed</li>
     *                         </ul>
     */
    public void setSelectionBackground(Color[] colors, int[] percents) {
    }

    /**
     * Specify a gradient of colours to be draw in the background of the selected
     * tab. For example to draw a vertical gradient that varies from dark blue to
     * blue and then to white, use the following call to setBackground:
     * 
     * <pre>
     * cfolder.setBackground(
     *         new Color[] { display.getSystemColor(SWT.COLOR_DARK_BLUE), display.getSystemColor(SWT.COLOR_BLUE),
     *                 display.getSystemColor(SWT.COLOR_WHITE), display.getSystemColor(SWT.COLOR_WHITE) },
     *         new int[] { 25, 50, 100 }, true);
     * </pre>
     *
     * @param colors   an array of Color that specifies the colors to appear in the
     *                 gradient in order of appearance left to right. The value
     *                 <code>null</code> clears the background gradient. The value
     *                 <code>null</code> can be used inside the array of Color to
     *                 specify the background color.
     * @param percents an array of integers between 0 and 100 specifying the percent
     *                 of the width of the widget at which the color should change.
     *                 The size of the percents array must be one less than the size
     *                 of the colors array.
     *
     * @param vertical indicate the direction of the gradient. True is vertical and
     *                 false is horizontal.
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                         wrong thread</li>
     *                         <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                         disposed</li>
     *                         </ul>
     *
     * @since 3.0
     */
    public void setSelectionBackground(Color[] colors, int[] percents, boolean vertical) {
    }

    /**
     * Set the image to be drawn in the background of the selected tab. Image is
     * stretched or compressed to cover entire selection tab area.
     *
     * @param image the image to be drawn in the background
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     */
    public void setSelectionBackground(Image image) {
    }

    /**
     * Set the foreground color of the selected tab.
     *
     * @param color the color of the text displayed in the selected tab
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     */
    public void setSelectionForeground(Color color) {
    }

    /**
     * Sets the thickness of the highlight bar on the selected tab. The highlight
     * bar is drawn in the top margin of the selected tab.
     *
     * @param thickness the desired thickness. Must be positive and lower than
     *                  {@link CTabFolderRenderer#ITEM_TOP_MARGIN} (that is either
     *                  {@code 0} {@code 1} or {@code 2} at the moment),for correct
     *                  results. Set to {@code 0} to not draw a highlight bar.
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         <li>ERROR_INVALID_ARGUMENT - if the parameter value
     *                         is invalid</li>
     *                         </ul>
     * @implNote Default {@link CTabFolderRenderer} currently ignores this setting
     *           if {@link #getSimple()} is {@code true}.
     * @since 3.121
     */
    public void setSelectionBarThickness(int thickness) {
    }

    /**
     * Sets the shape that the CTabFolder will use to render itself.
     *
     * @param simple <code>true</code> if the CTabFolder should render itself in a
     *               simple, traditional style
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     *
     * @since 3.0
     */
    public void setSimple(boolean simple) {
        builder().setSimple(simple);
        FlutterSwt.dirty(this);
    }

    /**
     * Sets the number of tabs that the CTabFolder should display
     *
     * @param single <code>true</code> if only the selected tab should be displayed
     *               otherwise, multiple tabs will be shown.
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     *
     * @since 3.0
     */
    public void setSingle(boolean single) {
        builder().setSingle(single);
        FlutterSwt.dirty(this);
    }

    /**
     * Specify a fixed height for the tab items. If no height is specified, the
     * default height is the height of the text or the image, whichever is greater.
     * Specifying a height of -1 will revert to the default height.
     *
     * @param height the point value of the height or -1
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         <li>ERROR_INVALID_ARGUMENT - if called with a height
     *                         of less than 0</li>
     *                         </ul>
     */
    public void setTabHeight(int height) {
        builder().setTabHeight(height);
        FlutterSwt.dirty(this);
    }

    /**
     * Specify whether the tabs should appear along the top of the folder or along
     * the bottom of the folder.
     *
     * @param position <code>SWT.TOP</code> for tabs along the top or
     *                 <code>SWT.BOTTOM</code> for tabs along the bottom
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         <li>ERROR_INVALID_ARGUMENT - if the position value is
     *                         not either SWT.TOP or SWT.BOTTOM</li>
     *                         </ul>
     *
     * @since 3.0
     */
    public void setTabPosition(int position) {
        builder().setTabPosition(position);
        FlutterSwt.dirty(this);
    }

    /**
     * Set the control that appears in the top right corner of the tab folder.
     * Typically this is a close button or a composite with a Menu and close button.
     * The topRight control is optional. Setting the top right control to null will
     * remove it from the tab folder.
     *
     * @param control the control to be displayed in the top right corner or null
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         <li>ERROR_INVALID_ARGUMENT - if the control is
     *                         disposed, or not a child of this CTabFolder</li>
     *                         </ul>
     *
     * @since 2.1
     */
    public void setTopRight(IControl control) {
        setTopRight(control, SWT.RIGHT);
    }
    private IControl topRight;

    /**
     * Set the control that appears in the top right corner of the tab folder.
     * Typically this is a close button or a composite with a Menu and close button.
     * The topRight control is optional. Setting the top right control to null will
     * remove it from the tab folder.
     * <p>
     * The alignment parameter sets the layout of the control in the tab area.
     * <code>SWT.RIGHT</code> will cause the control to be positioned on the far
     * right of the folder and it will have its default size. <code>SWT.FILL</code>
     * will size the control to fill all the available space to the right of the
     * last tab. If there is no available space, the control will not be visible.
     * <code>SWT.RIGHT | SWT.WRAP</code> will allow the control to wrap below the
     * tabs if there is not enough available space to the right of the last tab.
     * </p>
     *
     * @param control   the control to be displayed in the top right corner or null
     * @param alignment <code>SWT.RIGHT</code> or <code>SWT.FILL</code> or
     *                  <code>SWT.RIGHT | SWT.WRAP</code>
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         <li>ERROR_INVALID_ARGUMENT - if the control is
     *                         disposed, or not a child of this CTabFolder</li>
     *                         </ul>
     *
     * @since 3.0
     */
    public void setTopRight(IControl control, int alignment) {
        topRight = control;
    }

    /**
     * Specify whether the close button appears when the user hovers over an
     * unselected tabs.
     *
     * @param visible <code>true</code> makes the close button appear
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     *
     * @since 3.0
     */
    public void setUnselectedCloseVisible(boolean visible) {
        builder().setUnselectedCloseVisible(visible);
        FlutterSwt.dirty(this);
    }

    /**
     * Specify whether the image appears on unselected tabs.
     *
     * @param visible <code>true</code> makes the image appear
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     *
     * @since 3.0
     */
    public void setUnselectedImageVisible(boolean visible) {
        builder().setUnselectedImageVisible(visible);
        FlutterSwt.dirty(this);
    }

    /**
     * Shows the item. If the item is already showing in the receiver, this method
     * simply returns. Otherwise, the items are scrolled until the item is visible.
     *
     * @param item the item to be shown
     *
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_NULL_ARGUMENT - if the item is
     *                                     null</li>
     *                                     <li>ERROR_INVALID_ARGUMENT - if the item
     *                                     has been disposed</li>
     *                                     </ul>
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_WIDGET_DISPOSED - if the
     *                                     receiver has been disposed</li>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS - if not
     *                                     called from the thread that created the
     *                                     receiver</li>
     *                                     </ul>
     *
     * @see CTabFolder#showSelection()
     *
     * @since 2.0
     */
    public void showItem(ICTabItem item) {
    }

    /**
     * Shows the selection. If the selection is already showing in the receiver,
     * this method simply returns. Otherwise, the items are scrolled until the
     * selection is visible.
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     *
     * @see CTabFolder#showItem(CTabItem)
     *
     * @since 2.0
     */
    public void showSelection() {
    }

    /**
     * Set a control that can appear to the left or to the right of the folder tabs.
     * This method can also be used instead of #setTopRight(Control). To remove a
     * tab control, see#removeTabControl(Control);
     * <p>
     * The flags parameter sets the layout of the control in the tab area.
     * <code>SWT.LEAD</code> will cause the control to be positioned on the left of
     * the tabs. <code>SWT.TRAIL</code> will cause the control to be positioned on
     * the far right of the folder and it will have its default size.
     * <code>SWT.TRAIL</code> can be combined with <code>SWT.FILL</code>to fill all
     * the available space to the right of the last tab. <code>SWT.WRAP</code> can
     * also be added to <code>SWT.TRAIL</code> only to cause a control to wrap if
     * there is not enough space to display it in its entirety.
     * </p>
     * 
     * @param control the control to be displayed in the top right corner or null
     *
     * @param flags   valid combinations are:
     *                <ul>
     *                <li>SWT.LEAD
     *                <li>SWT.TRAIL (| SWT.FILL | SWT.WRAP)
     *                </ul>
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         <li>ERROR_INVALID_ARGUMENT - if the control is not a
     *                         child of this CTabFolder</li>
     *                         </ul>
     */
    /**
     * Removes the control from the list of tab controls.
     *
     * @param control the control to be removed
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         <li>ERROR_INVALID_ARGUMENT - if the control is not a
     *                         child of this CTabFolder</li>
     *                         </ul>
     */
    /**
     * Sets whether a chevron is shown when there are more items to be displayed.
     *
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_INVALID_RANGE - if the index is
     *                                     out of range</li>
     *                                     </ul>
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS when
     *                                     called from the wrong thread</li>
     *                                     <li>ERROR_WIDGET_DISPOSED when the widget
     *                                     has been disposed</li>
     *                                     </ul>
     */
    /**
     * Sets whether the selected tab is rendered as highlighted.
     *
     * @param enabled {@code true} if the selected tab should be highlighted,
     *                {@code false} otherwise.
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     * @since 3.106
     */
    public void setHighlightEnabled(boolean enabled) {
        builder().setHighlightEnabled(enabled);
        FlutterSwt.dirty(this);
    }

    /**
     * Returns <code>true</code> if the selected tab is rendered as highlighted.
     *
     * @return <code>true</code> if the selected tab is rendered as highlighted.
     *
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                         disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
     *                         the thread that created the receiver</li>
     *                         </ul>
     * @since 3.106
     */
    public boolean getHighlightEnabled() {
        return builder().getHighlightEnabled().orElse(false);
    }

    protected void hookEvents() {
        super.hookEvents();
        String ev = FlutterSwt.getEvent(this);
        FlutterSwt.CLIENT.getComm().on(ev + "/Selection/Selection", p -> {
            System.out.println(ev + "/Selection/Selection event");
            int oldSelection = getSelectionIndex();
            int newSelection = FlutterSwt.SERIALIZER.from(p, Integer.class);
            setSelection(newSelection);
            
            display.asyncExec(() -> {
                if (oldSelection != newSelection) {
                    Event event = new Event();
                    event.item = Widget.getInstance(getItem(newSelection));
                    notifyListeners(SWT.Selection, event);
                }
            });
        });
        FlutterSwt.CLIENT.getComm().on(ev + "/Selection/DefaultSelection", p -> {
            System.out.println(ev + "/Selection/DefaultSelection event");
            display.asyncExec(() -> {
                sendEvent(SWT.DefaultSelection);
            });
        });
    }

    public CTabFolderValue.Builder builder() {
        if (builder == null)
            builder = CTabFolderValue.builder().setId(hashCode()).setStyle(style);
        return (CTabFolderValue.Builder) builder;
    }

    @Override
    public CTabFolderRenderer getRenderer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getSelectedImageVisible() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setRenderer(CTabFolderRenderer renderer) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setSelectedImageVisible(boolean visible) {
        // TODO Auto-generated method stub
    }

    // --- Generated computeSize method for Tabfolder ---
    // IMPORTANT: Review and adjust the parameter initializations (TODO sections)
    // and SWT style checks below to match the specific behavior of Tabfolder.
    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        checkWidget();
        int width = 0;
        int height = 0;

        // --- Layout-specific calculations for TabFolder ---
        // This part would be custom for TabFolder and not fully auto-generated beyond a template
        double accumulatedWidth = 0;
        double maxChildHeight = 0;
        int visibleItemCount = 0;

        // Assuming getItems() returns the TabItems or equivalent Control objects
        ICTabItem[] items = getItems();

        for (ICTabItem item : items) {
            if (!item.getControl().getVisible()) continue; // Or however visibility is handled

            Point itemSize = item.computeSize(); // Get preferred size of child

            if (visibleItemCount > 0) {
                accumulatedWidth += TabfolderSwtSizingConstants.TAB_SPACING; // Constant for spacing
            }
            accumulatedWidth += itemSize.x;
            maxChildHeight = Math.max(maxChildHeight, itemSize.y);
            visibleItemCount++;
        }

        // Add TabFolder's own padding
        width = (int) (accumulatedWidth + 2 * TabfolderSwtSizingConstants.TAB_HORIZONTAL_PADDING);
        height = (int) (maxChildHeight + 2 * TabfolderSwtSizingConstants.TAB_VERTICAL_PADDING);

        // --- Apply hints and border (standard part from your generator) ---
        if (wHint != SWT.DEFAULT) width = wHint;
        if (hHint != SWT.DEFAULT) height = hHint;

        int borderWidth = (int) TabfolderSwtSizingConstants.BORDER_WIDTH; // Assuming this method exists
        width += borderWidth * 2;
        height += borderWidth * 2;

        return new Point(width, height);
    }

    public String toString() {
        FlutterCTabItem firstItem = ((FlutterCTabItem) getItem(0));
        return firstItem == null ? "" : firstItem.getText();
    }

}
