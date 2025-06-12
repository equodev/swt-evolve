package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.TabfolderSwtSizingConstants;
import org.eclipse.swt.values.TabFolderValue;

/**
 * Instances of this class implement the notebook user interface
 * metaphor.  It allows the user to select a notebook page from
 * set of pages.
 * <p>
 * The item children that may be added to instances of this class
 * must be of type <code>TabItem</code>.
 * <code>Control</code> children are created and then set into a
 * tab item using <code>TabItem#setControl</code>.
 * </p><p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not make sense to set a layout on it.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>TOP, BOTTOM</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles TOP and BOTTOM may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#tabfolder">TabFolder, TabItem snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class FlutterTabFolder extends FlutterComposite implements ITabFolder{

    /*
	 * Implementation note (see bug 454936, bug 480794):
	 *
	 * Architecture Change on GTK3:
	 * In TabItem#setControl(Control), we reparent the child to be a child of the 'tab'
	 * rather than tabfolder's parent swtFixed container.
	 * Note, this reparenting is only on the GTK side, not on the SWT side.
	 *
	 * GTK3+:
	 * 	swtFixed
	 * 	|--	GtkNoteBook
	 * 		|-- tabLabel1
	 * 		|-- tabLabel2
	 * 		|-- pageHandle (tabItem1)
	 * 			|-- child1 //child now child of Notebook.pageHandle.
	 * 		|-- pageHandle (tabItem2)
	 * 			|-- child1
	 *
	 * This changes the hierarchy so that children are beneath gtkNotebook (as oppose to
	 * being siblings) and thus fixes DND and background color issues.
	 *
	 * Note about the reason for reparenting:
	 * Reparenting (as opposed to adding widget to a tab in the first place) is necessary
	 * because the SWT API allows situation where you create a child control before you create a TabItem.
	 */
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
     * @see SWT#TOP
     * @see SWT#BOTTOM
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public FlutterTabFolder(IComposite parent, int style) {
        super(parent, checkStyle(style));
    }

    static int checkStyle(int style) {
        style = checkBits(style, SWT.TOP, SWT.BOTTOM, 0, 0, 0, 0);
        /*
	* Even though it is legal to create this widget
	* with scroll bars, they serve no useful purpose
	* because they do not automatically scroll the
	* widget's client area.  The fix is to clear
	* the SWT style.
	*/
        return style & ~(SWT.H_SCROLL | SWT.V_SCROLL);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the user changes the receiver's selection, by sending
     * it one of the messages defined in the <code>SelectionListener</code>
     * interface.
     * <p>
     * When <code>widgetSelected</code> is called, the item field of the event object is valid.
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
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        TypedListener typedListener = new TypedListener(listener);
        addListener(SWT.Selection, typedListener);
        addListener(SWT.DefaultSelection, typedListener);
    }

    @Override
    IControl[] _getChildren() {
        // Not Generated
        return super.getChildren();
    }

    /**
     * Returns the item at the given, zero-relative index in the
     * receiver. Throws an exception if the index is out of range.
     *
     * @param index the index of the item to return
     * @return the item at the given index
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public ITabItem getItem(int index) {
        // Not Generated
        return (ITabItem) children.get(index);
    }

    /**
     * Returns the tab item at the given point in the receiver
     * or null if no such item exists. The point is in the
     * coordinate system of the receiver.
     *
     * @param point the point used to locate the item
     * @return the tab item at the given point, or null if the point is not in a tab item
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    public ITabItem getItem(Point point) {
        return null;
    }

    /**
     * Returns the number of items contained in the receiver.
     *
     * @return the number of items
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getItemCount() {
        // Not Generated
        return children.size();
    }

    /**
     * Returns an array of <code>TabItem</code>s which are the items
     * in the receiver.
     * <p>
     * Note: This is not the actual structure used by the receiver
     * to maintain its list of items, so modifying the array will
     * not affect the receiver.
     * </p>
     *
     * @return the items in the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public ITabItem[] getItems() {
        // Not Generated
        return children == null ? new ITabItem[0]
                : children.stream().filter(ITabItem.class::isInstance).toArray(ITabItem[]::new);
    }

    /**
     * Returns an array of <code>TabItem</code>s that are currently
     * selected in the receiver. An empty array indicates that no
     * items are selected.
     * <p>
     * Note: This is not the actual structure used by the receiver
     * to maintain its selection, so modifying the array will
     * not affect the receiver.
     * </p>
     * @return an array representing the selection
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public ITabItem[] getSelection() {
        // Not Generated
        return new ITabItem[] { (ITabItem) children.get(getSelectionIndex()) };
    }

    /**
     * Returns the zero-relative index of the item which is currently
     * selected in the receiver, or -1 if no item is selected.
     *
     * @return the index of the selected item
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getSelectionIndex() {
        return builder().getSelectionIndex().orElse(0);
    }

    @Override
    protected void hookEvents() {
        // Not Generated
        super.hookEvents();
        String ev = FlutterSwt.getEvent(this);
        FlutterSwt.CLIENT.getComm().on(ev + "/Selection/Selection", p -> {
            System.out.println(ev + "/Selection/Selection event");
            int newSelection = FlutterSwt.SERIALIZER.from(p, Integer.class);
            builder().setSelectionIndex(newSelection);
            display.asyncExec(() -> {
                sendEvent(SWT.Selection);
            });
        });
        FlutterSwt.CLIENT.getComm().on(ev + "/Selection/DefaultSelection", p -> {
            System.out.println(ev + "/Selection/DefaultSelection event");
            display.asyncExec(() -> {
                sendEvent(SWT.DefaultSelection);
            });
        });
    }

    /**
     * Searches the receiver's list starting at the first item
     * (index 0) until an item is found that is equal to the
     * argument, and returns the index of that item. If no item
     * is found, returns -1.
     *
     * @param item the search item
     * @return the index of the item
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the item is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int indexOf(ITabItem item) {
        // Not Generated
        return children != null && !children.isEmpty() ? children.indexOf(item) : -1;
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
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Selection, listener);
        eventTable.unhook(SWT.DefaultSelection, listener);
    }

    /**
     * Selects the item at the given zero-relative index in the receiver.
     * If the item at the index was already selected, it remains selected.
     * The current selection is first cleared, then the new items are
     * selected. Indices that are out of range are ignored.
     *
     * @param index the index of the item to select
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setSelection(int index) {
        // Not Generated
        builder().setSelectionIndex(index);
        FlutterSwt.dirty(this);
        //        Control control = item.control;
        //        if (control != null && !control.isDisposed ()) {
        //            control.setBoundsInPixels (getClientAreaInPixels ());
        //            control.setVisible (true);
        //        }
    }

    /**
     * Sets the receiver's selection to the given item.
     * The current selected is first cleared, then the new item is
     * selected.
     *
     * @param item the item to select
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the item is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    public void setSelection(ITabItem item) {
        // Not Generated
        setSelection(indexOf(item));
    }

    /**
     * Sets the receiver's selection to be the given array of items.
     * The current selected is first cleared, then the new items are
     * selected.
     *
     * @param items the array of items
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the items array is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setSelection(ITabItem[] items) {
        // Not Generated
        if (items.length > 0) {
            setSelection(items[0]);
        }
    }

    public TabFolderValue.Builder builder() {
        if (builder == null)
            builder = TabFolderValue.builder().setId(handle).setStyle(style);
        return (TabFolderValue.Builder) builder;
    }

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
        ITabItem[] items = getItems();

        for (ITabItem item : items) {
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
}
