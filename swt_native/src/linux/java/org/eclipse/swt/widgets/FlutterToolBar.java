package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.ICTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.ToolbarSwtSizingConstants;
import org.eclipse.swt.values.ToolBarValue;

/**
 * Instances of this class support the layout of selectable
 * tool bar items.
 * <p>
 * The item children that may be added to instances of this class
 * must be of type <code>ToolItem</code>.
 * </p><p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not make sense to add <code>Control</code> children to it,
 * or set a layout on it.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>FLAT, WRAP, RIGHT, HORIZONTAL, VERTICAL, SHADOW_OUT</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles HORIZONTAL and VERTICAL may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#toolbar">ToolBar, ToolItem snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class FlutterToolBar extends FlutterComposite implements IToolBar {

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
     * @see SWT#FLAT
     * @see SWT#WRAP
     * @see SWT#RIGHT
     * @see SWT#HORIZONTAL
     * @see SWT#SHADOW_OUT
     * @see SWT#VERTICAL
     * @see Widget#checkSubclass()
     * @see Widget#getStyle()
     */
    public FlutterToolBar(IComposite parent, int style) {
        super(parent, checkStyle(style));
        /*
	* Ensure that either of HORIZONTAL or VERTICAL is set.
	* NOTE: HORIZONTAL and VERTICAL have the same values
	* as H_SCROLL and V_SCROLL so it is necessary to first
	* clear these bits to avoid scroll bars and then reset
	* the bits using the original style supplied by the
	* programmer.
	*/
        if ((style & SWT.VERTICAL) != 0) {
            this.style |= SWT.VERTICAL;
        } else {
            this.style |= SWT.HORIZONTAL;
        }
    }

    static int checkStyle(int style) {
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
    public IToolItem getItem(int index) {
        if (children != null && children.size() > index) {
            return (FlutterToolItem)children.get(index);
        }
        return null;
    }

    /**
     * Returns the item at the given point in the receiver
     * or null if no such item exists. The point is in the
     * coordinate system of the receiver.
     *
     * @param point the point used to locate the item
     * @return the item at the given point
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public IToolItem getItem(Point point) {
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
        return children == null ? 0 : children.size();
    }

    /**
     * Returns an array of <code>ToolItem</code>s which are the items
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
    public IToolItem[] getItems() {
        return children == null ? new IToolItem[0] : children.stream().filter(IToolItem.class::isInstance).toArray(IToolItem[]::new);
    }

    /**
     * Returns the number of rows in the receiver. When
     * the receiver has the <code>WRAP</code> style, the
     * number of rows can be greater than one.  Otherwise,
     * the number of rows is always one.
     *
     * @return the number of items
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getRowCount() {
        return -1;
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
     *    <li>ERROR_NULL_ARGUMENT - if the tool item is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the tool item has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int indexOf(IToolItem item) {
        if (!(item instanceof FlutterWidget) || children == null) return -1;
        for (int i = 0; i < children.size(); ++i) {
            if (children.get(i) == item) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void setToolTipText(String string) {
        if (string != null) {
            builder().setToolTipText(string);
            FlutterSwt.dirty(this);
        }
    }

    public ToolBarValue.Builder builder() {
        if (builder == null)
            builder = ToolBarValue.builder().setId(handle).setStyle(style);
        return (ToolBarValue.Builder) builder;
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        checkWidget();
        int width = 0;
        int height = 0;

        // --- Layout-specific calculations for Toolbar ---
        // This part would be custom for Toolbar and not fully auto-generated beyond a template
        double accumulatedWidth = 0;
        double maxChildHeight = 0;
        int visibleItemCount = 0;

        IToolItem [] items = getItems();

        for (IToolItem item : items) {
//            if (!item.getControl().getVisible()) continue; // Or however visibility is handled

            Point itemSize = item.computeSize(); // Get preferred size of child

            if (visibleItemCount > 0) {
                accumulatedWidth += ToolbarSwtSizingConstants.TOOLBAR_HORIZONTAL_PADDING; // Constant for spacing
            }
            accumulatedWidth += itemSize.x;
            maxChildHeight = Math.max(maxChildHeight, itemSize.y);
            visibleItemCount++;
        }

        // Add Toolbar's own padding
        width = (int) (accumulatedWidth + (2 * ToolbarSwtSizingConstants.TOOLBAR_HORIZONTAL_PADDING));
        height = (int) (maxChildHeight + (2 * ToolbarSwtSizingConstants.TOOLBAR_VERTICAL_PADDING));

        // --- Apply hints and border (standard part from your generator) ---
        if (wHint != SWT.DEFAULT) width = wHint;
        if (hHint != SWT.DEFAULT) height = hHint;

        int borderWidth = getBorderWidth(); // Assuming this method exists
        width += borderWidth * 2;
        height += borderWidth * 2;

        // Ensure minimum dimensions if any (e.g. if toolbar is empty but should still show)
        // width = Math.max(width, ToolbarSwtSizingConstants.MINIMUM_TOOLBAR_WIDTH);
        // height = Math.max(height, ToolbarSwtSizingConstants.MINIMUM_TOOLBAR_HEIGHT);

        return new Point(width, height);
    }
}
