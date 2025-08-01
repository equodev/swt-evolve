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

    /* sizing, positioning */
    boolean onBottom = false;

    boolean single = false;

    boolean simple = true;

    int fixedTabHeight = SWT.DEFAULT;

    int tabHeight;

    int minChars = 20;

    boolean borderVisible = false;

    /* item management */
    CTabFolderRenderer renderer;

    CTabItem[] items = new CTabItem[0];

    /**
     * index of the left most visible tab.
     */
    int firstIndex = -1;

    int selectedIndex = -1;

    /**
     * Indices of the elements in the {@link #items} array, used to manage tab
     * visibility and candidates to be hidden/shown next.
     * <p>
     * If there is not enough place for all tabs, tabs starting from the end of
     * the {@link #priority} array will be hidden first (independently from the
     * {@link #mru} flag!) =&gt; the right most elements have the highest priority
     * to be hidden.
     * <p>
     * If there is more place to show previously hidden tabs, tabs starting from
     * the beginning of the {@link #priority} array will be made visible first
     * (independently from the {@link #mru} flag!) =&gt; the left most elements
     * have the highest priority to be shown.
     * <p>
     * The update strategy of the {@link #priority} array however depends on the
     * {@link #mru} flag.
     * <p>
     * If {@link #mru} flag is set, the first index is always the index of the
     * currently selected tab, next one is the tab selected before current
     * etc...
     * <p>
     * Example: [4,2,5,1,3,0], just representing the last selection order.
     * <p>
     * If {@link #mru} flag is not set, the first index is always the index of
     * the left most visible tab ({@link #firstIndex} field), next indices are
     * incremented by one up to <code>priority.length-1</code>, and the rest
     * filled with indices starting with <code>firstIndex-1</code> and
     * decremented by one until 0 index is reached.
     * <p>
     * The tabs between first index and the index of the currently selected tab
     * are always visible.
     * <p>
     * Example: 6 tabs, 2 and 3 are indices of currently shown tabs:
     * [2,3,4,5,1,0]. The array consists of two blocks: sorted ascending from
     * first visible (2) to last available (5), and the rest sorted descending
     * (1,0). 4 and 5 are the hidden tabs on the right side, 0 and 1 are the
     * hidden tabs on the left side from the visible tabs 2 and 3.
     *
     * @see #updateItems(int)
     * @see #setItemLocation(GC)
     */
    int[] priority = new int[0];

    boolean mru = false;

    Listener listener;

    boolean ignoreTraverse;

    boolean useDefaultRenderer;

    /* External Listener management */
    CTabFolder2Listener[] folderListeners = new CTabFolder2Listener[0];

    // support for deprecated listener mechanism
    CTabFolderListener[] tabListeners = new CTabFolderListener[0];

    /* Selected item appearance */
    Image selectionBgImage;

    Color[] selectionGradientColors;

    int[] selectionGradientPercents;

    boolean selectionGradientVertical;

    Color selectionForeground;

    Color selectionBackground;

    int selectionHighlightBarThickness = 2;

    /* Unselected item appearance */
    Color[] gradientColors;

    int[] gradientPercents;

    boolean gradientVertical;

    boolean showUnselectedImage = true;

    boolean showSelectedImage = true;

    // close, min/max and chevron buttons
    boolean showClose = false;

    boolean showUnselectedClose = true;

    boolean showMin = false;

    boolean minimized = false;

    boolean showMax = false;

    boolean maximized = false;

    ToolBar minMaxTb;

    ToolItem maxItem;

    ToolItem minItem;

    Image maxImage;

    Image minImage;

    boolean hoverTb;

    Rectangle hoverRect = new Rectangle(0, 0, 0, 0);

    boolean hovering;

    boolean hoverTimerRunning;

    boolean highlight;

    boolean highlightEnabled = true;

    boolean showChevron = false;

    Menu showMenu;

    ToolBar chevronTb;

    ToolItem chevronItem;

    int chevronCount;

    boolean chevronVisible = true;

    Image chevronImage;

    Control topRight;

    int topRightAlignment = SWT.RIGHT;

    boolean ignoreResize;

    Control[] controls;

    int[] controlAlignments;

    Rectangle[] controlRects;

    Rectangle[] bkImageBounds;

    Image[] controlBkImages;

    int updateFlags;

    final static int REDRAW = 1 << 1;

    final static int REDRAW_TABS = 1 << 2;

    final static int UPDATE_TAB_HEIGHT = 1 << 3;

    Runnable updateRun;

    // when disposing CTabFolder, don't try to layout the items or
    // change the selection as each child is destroyed.
    boolean inDispose = false;

    // keep track of size changes in order to redraw only affected area
    // on Resize
    Point oldSize;

    Font oldFont;

    // internal constants
    static final int DEFAULT_WIDTH = 64;

    static final int DEFAULT_HEIGHT = 64;

    static final int SELECTION_FOREGROUND = SWT.COLOR_LIST_FOREGROUND;

    static final int SELECTION_BACKGROUND = SWT.COLOR_LIST_BACKGROUND;

    static final int FOREGROUND = SWT.COLOR_WIDGET_FOREGROUND;

    static final int BACKGROUND = SWT.COLOR_WIDGET_BACKGROUND;

    //TODO: add setter for spacing?
    static final int SPACING = 3;

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
    public DartCTabFolder(Composite parent, int style, CTabFolder api) {
        super(parent, checkStyle(parent, style), api);
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

    void onDeactivate(Event event) {
        if (!highlightEnabled) {
            return;
        }
        this.highlight = false;
        redraw();
    }

    void onActivate(Event event) {
        if (!highlightEnabled) {
            return;
        }
        this.highlight = true;
        redraw();
    }

    static int checkStyle(Composite parent, int style) {
        int mask = SWT.CLOSE | SWT.TOP | SWT.BOTTOM | SWT.FLAT | SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT | SWT.SINGLE | SWT.MULTI;
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
        //TEMPORARY CODE
        /*
	 * In Right To Left orientation on Windows, all GC calls that use a brush are drawing
	 * offset by one pixel.  This results in some parts of the CTabFolder not drawing correctly.
	 * To alleviate some of the appearance problems, allow the OS to draw the background.
	 * This does not draw correctly but the result is less obviously wrong.
	 */
        if ((style & SWT.RIGHT_TO_LEFT) != 0)
            return style;
        if ((parent.getStyle() & SWT.MIRRORED) != 0 && (style & SWT.LEFT_TO_RIGHT) == 0)
            return style;
        return style | SWT.DOUBLE_BUFFERED;
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

    Rectangle[] computeControlBounds(Point size, boolean[][] position) {
        if (controls == null || controls.length == 0)
            return new Rectangle[0];
        Rectangle[] rects = new Rectangle[controls.length];
        for (int i = 0; i < rects.length; i++) {
            rects[i] = new Rectangle(0, 0, 0, 0);
        }
        Rectangle trim = renderer.computeTrim(CTabFolderRenderer.PART_BORDER, SWT.NONE, 0, 0, 0, 0);
        int borderRight = trim.width + trim.x;
        int borderLeft = -trim.x;
        int borderBottom = trim.height + trim.y;
        int borderTop = -trim.y;
        Point[] tabControlSize = new Point[controls.length];
        boolean[] overflow = new boolean[controls.length];
        //Left Control
        int leftWidth = 0;
        int x = borderLeft + SPACING;
        int rightWidth = 0;
        int allWidth = 0;
        boolean spacingRight = false;
        for (int i = 0; i < controls.length; i++) {
            Point ctrlSize = tabControlSize[i] = !controls[i].isDisposed() && controls[i].getVisible() ? controls[i].computeSize(SWT.DEFAULT, SWT.DEFAULT) : new Point(0, 0);
            int alignment = controlAlignments[i];
            if ((alignment & SWT.LEAD) != 0) {
                rects[i].width = ctrlSize.x;
                rects[i].height = getControlHeight(ctrlSize);
                rects[i].x = x;
                rects[i].y = getControlY(size, rects, borderBottom, borderTop, i);
                x += ctrlSize.x;
                leftWidth += ctrlSize.x;
            } else {
                if ((alignment & SWT.WRAP) == 0 && ctrlSize.x > 0) {
                    spacingRight = true;
                }
                if ((alignment & (SWT.FILL | SWT.WRAP)) == 0) {
                    rightWidth += ctrlSize.x;
                }
                allWidth += ctrlSize.x;
            }
        }
        if (leftWidth > 0)
            leftWidth += SPACING * 2;
        int itemWidth = 0;
        for (CTabItem item : items) {
            if (((DartCTabItem) item.getImpl()).showing)
                itemWidth += ((DartCTabItem) item.getImpl()).width;
        }
        int maxWidth = size.x - borderLeft - leftWidth - borderRight;
        int availableWidth = Math.max(0, maxWidth - itemWidth - rightWidth);
        if (spacingRight)
            availableWidth -= SPACING * 2;
        x = size.x - borderRight - SPACING;
        if (itemWidth + allWidth <= maxWidth) {
            //Everything fits
            for (int i = 0; i < controls.length; i++) {
                int alignment = controlAlignments[i];
                if ((alignment & SWT.TRAIL) != 0) {
                    Point ctrlSize = tabControlSize[i];
                    x -= ctrlSize.x;
                    rects[i].width = ctrlSize.x;
                    rects[i].height = getControlHeight(ctrlSize);
                    rects[i].x = x;
                    rects[i].y = getControlY(size, rects, borderBottom, borderTop, i);
                    if ((alignment & (SWT.FILL | SWT.WRAP)) != 0)
                        availableWidth -= ctrlSize.x;
                }
                if (tabControlSize[i].y >= tabHeight && fixedTabHeight == SWT.DEFAULT) {
                    overflow[i] = true;
                }
            }
        } else {
            for (int i = 0; i < controls.length; i++) {
                int alignment = controlAlignments[i];
                Point ctrlSize = tabControlSize[i];
                if ((alignment & SWT.TRAIL) != 0) {
                    if ((alignment & (SWT.FILL | SWT.WRAP)) == 0) {
                        x -= ctrlSize.x;
                        rects[i].width = ctrlSize.x;
                        rects[i].height = getControlHeight(ctrlSize);
                        rects[i].x = x;
                        rects[i].y = getControlY(size, rects, borderBottom, borderTop, i);
                    } else if (((alignment & (SWT.WRAP)) != 0 && ctrlSize.x < availableWidth)) {
                        x -= ctrlSize.x;
                        rects[i].width = ctrlSize.x;
                        rects[i].height = getControlHeight(ctrlSize);
                        rects[i].x = x;
                        rects[i].y = getControlY(size, rects, borderBottom, borderTop, i);
                        availableWidth -= ctrlSize.x;
                    } else if ((alignment & (SWT.FILL)) != 0 && (alignment & (SWT.WRAP)) == 0) {
                        rects[i].width = 0;
                        rects[i].height = getControlHeight(ctrlSize);
                        rects[i].x = x;
                        rects[i].y = getControlY(size, rects, borderBottom, borderTop, i);
                    } else {
                        if ((alignment & (SWT.WRAP)) != 0) {
                            overflow[i] = true;
                        }
                    }
                }
            }
        }
        //Any space, distribute amongst FILL
        if (availableWidth > 0) {
            int fillCount = 0;
            for (int i = 0; i < controls.length; i++) {
                int alignment = controlAlignments[i];
                if ((alignment & SWT.TRAIL) != 0 && (alignment & SWT.FILL) != 0 && !overflow[i]) {
                    fillCount++;
                }
            }
            if (fillCount != 0) {
                int extraSpace = availableWidth / fillCount;
                int addedSpace = 0;
                for (int i = 0; i < controls.length; i++) {
                    int alignment = controlAlignments[i];
                    if ((alignment & SWT.TRAIL) != 0) {
                        if ((alignment & SWT.FILL) != 0 && !overflow[i]) {
                            rects[i].width += extraSpace;
                            addedSpace += extraSpace;
                        }
                        if (!overflow[i]) {
                            rects[i].x -= addedSpace;
                        }
                    }
                }
            }
        }
        //Go through overflow laying out all wrapped controls
        Rectangle bodyTrim = renderer.computeTrim(CTabFolderRenderer.PART_BODY, SWT.NONE, 0, 0, 0, 0);
        int bodyRight = bodyTrim.width + bodyTrim.x;
        int bodyLeft = -bodyTrim.x;
        int bodyWidth = size.x - bodyLeft - bodyRight;
        x = size.x - bodyRight;
        int y = onBottom ? this.getSize().y - getTabHeight() + 2 * bodyTrim.y : -bodyTrim.y;
        availableWidth = bodyWidth;
        int maxHeight = 0;
        for (int i = 0; i < controls.length; i++) {
            Point ctrlSize = tabControlSize[i];
            if (overflow[i]) {
                if (availableWidth > ctrlSize.x) {
                    x -= ctrlSize.x;
                    rects[i].width = ctrlSize.x;
                    rects[i].y = onBottom ? y - ctrlSize.y : y;
                    rects[i].height = ctrlSize.y;
                    rects[i].x = x;
                    availableWidth -= ctrlSize.x;
                    maxHeight = Math.max(maxHeight, ctrlSize.y);
                } else {
                    x = size.x - bodyRight;
                    y += maxHeight;
                    maxHeight = 0;
                    availableWidth = bodyWidth;
                    if (availableWidth > ctrlSize.x) {
                        //Relayout this control in the next line
                        i--;
                    } else {
                        ctrlSize = controls[i].isDisposed() ? new Point(0, 0) : controls[i].computeSize(bodyWidth, SWT.DEFAULT);
                        rects[i].width = bodyWidth;
                        rects[i].y = onBottom ? y - ctrlSize.y : y;
                        rects[i].height = ctrlSize.y;
                        rects[i].x = size.x - ctrlSize.x - bodyRight;
                        y += ctrlSize.y;
                    }
                }
            }
        }
        if (showChevron) {
            int i = 0, lastIndex = -1;
            while (i < priority.length && ((DartCTabItem) items[priority[i]].getImpl()).showing) {
                lastIndex = Math.max(lastIndex, priority[i++]);
            }
            if (lastIndex == -1)
                lastIndex = selectedIndex;
            if (lastIndex != -1) {
                CTabItem lastItem = items[lastIndex];
                int w = ((DartCTabItem) lastItem.getImpl()).x + ((DartCTabItem) lastItem.getImpl()).width + SPACING;
                if (!simple && lastIndex == selectedIndex)
                    w -= (((DartCTabFolderRenderer) renderer.getImpl()).curveIndent - 7);
                rects[controls.length - 1].x = w;
            }
        }
        if (position != null)
            position[0] = overflow;
        return rects;
    }

    int getControlHeight(Point ctrlSize) {
        return fixedTabHeight == SWT.DEFAULT ? Math.max(tabHeight - 1, ctrlSize.y) : ctrlSize.y;
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

    Image createButtonImage(Display display, int button) {
        return null;
    }

    private void notifyItemCountChange() {
        CTabFolderEvent e = new CTabFolderEvent(this.getApi());
        for (CTabFolder2Listener listener : folderListeners) {
            listener.itemsCount(e);
        }
    }

    void createItem(CTabItem item, int index) {
        if (0 > index || index > getItemCount())
            SWT.error(SWT.ERROR_INVALID_RANGE);
        ((DartCTabItem) item.getImpl()).parent = this.getApi();
        CTabItem[] newItems = new CTabItem[items.length + 1];
        System.arraycopy(items, 0, newItems, 0, index);
        newItems[index] = item;
        System.arraycopy(items, index, newItems, index + 1, items.length - index);
        items = newItems;
        if (selectedIndex >= index)
            selectedIndex++;
        int[] newPriority = new int[priority.length + 1];
        int next = 0, priorityIndex = priority.length;
        for (int element : priority) {
            if (!mru && element == index) {
                priorityIndex = next++;
            }
            newPriority[next++] = element >= index ? element + 1 : element;
        }
        newPriority[priorityIndex] = index;
        priority = newPriority;
        if (items.length == 1) {
            updateFolder(UPDATE_TAB_HEIGHT | REDRAW);
        } else {
            updateFolder(REDRAW_TABS);
        }
        notifyItemCountChange();
    }

    void destroyItem(CTabItem item) {
        if (inDispose)
            return;
        int index = indexOf(item);
        if (index == -1)
            return;
        if (items.length == 1) {
            items = new CTabItem[0];
            priority = new int[0];
            firstIndex = -1;
            selectedIndex = -1;
            Control control = ((DartCTabItem) item.getImpl()).control;
            if (control != null && !control.isDisposed()) {
                control.setVisible(false);
            }
            setToolTipText(null);
            updateButtons();
            setButtonBounds();
            redraw();
            notifyItemCountChange();
            return;
        }
        CTabItem[] newItems = new CTabItem[items.length - 1];
        System.arraycopy(items, 0, newItems, 0, index);
        System.arraycopy(items, index + 1, newItems, index, items.length - index - 1);
        items = newItems;
        int[] newPriority = new int[priority.length - 1];
        int next = 0;
        for (int element : priority) {
            if (element == index)
                continue;
            newPriority[next++] = element > index ? element - 1 : element;
        }
        priority = newPriority;
        // move the selection if this item is selected
        if (selectedIndex == index) {
            Control control = item.getControl();
            selectedIndex = -1;
            int nextSelection = mru ? priority[0] : Math.max(0, index - 1);
            setSelection(nextSelection, true);
            if (control != null && !control.isDisposed()) {
                control.setVisible(false);
            }
        } else if (selectedIndex > index) {
            selectedIndex--;
        }
        requestLayout();
        updateFolder(UPDATE_TAB_HEIGHT | REDRAW_TABS);
        notifyItemCountChange();
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
        checkWidget();
        return borderVisible;
    }

    ToolBar getChevron() {
        if (chevronTb == null) {
            chevronTb = new ToolBar(this.getApi(), SWT.FLAT);
            initAccessibleChevronTb();
            addTabControl(chevronTb, SWT.TRAIL, -1, false);
        }
        if (chevronItem == null) {
            chevronItem = new ToolItem(chevronTb, SWT.PUSH);
            chevronItem.setToolTipText(SWT.getMessage("SWT_ShowList"));
            chevronItem.addListener(SWT.Selection, listener);
        }
        return chevronTb;
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
    /*public*/
    boolean getChevronVisible() {
        checkWidget();
        return chevronVisible;
    }

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

    int getLeftItemEdge(GC gc, int part) {
        Rectangle trim = renderer.computeTrim(part, SWT.NONE, 0, 0, 0, 0);
        int x = -trim.x;
        int width = 0;
        for (int i = 0; i < controls.length; i++) {
            if ((controlAlignments[i] & SWT.LEAD) != 0 && !controls[i].isDisposed() && controls[i].getVisible()) {
                width += controls[i].computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
            }
        }
        if (width != 0)
            width += SPACING * 2;
        x += width;
        return Math.max(0, x);
    }

    /*
 * Return the lowercase of the first non-'&' character following
 * an '&' character in the given string. If there are no '&'
 * characters in the given string, return '\0'.
 */
    char _findMnemonic(String string) {
        if (string == null)
            return '\0';
        int index = 0;
        int length = string.length();
        do {
            while (index < length && string.charAt(index) != '&') index++;
            if (++index >= length)
                return '\0';
            if (string.charAt(index) != '&')
                return Character.toLowerCase(string.charAt(index));
            index++;
        } while (index < length);
        return '\0';
    }

    String stripMnemonic(String string) {
        int index = 0;
        int length = string.length();
        do {
            while ((index < length) && (string.charAt(index) != '&')) index++;
            if (++index >= length)
                return string;
            if (string.charAt(index) != '&') {
                return string.substring(0, index - 1) + string.substring(index, length);
            }
            index++;
        } while (index < length);
        return string;
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
        checkWidget();
        return minimized;
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
        checkWidget();
        return showMin;
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
        checkWidget();
        return minChars;
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
        checkWidget();
        return maximized;
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
        checkWidget();
        return showMax;
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
        checkWidget();
        return mru;
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
        checkWidget();
        return renderer;
    }

    int getRightItemEdge(GC gc) {
        Rectangle trim = renderer.computeTrim(CTabFolderRenderer.PART_BORDER, SWT.NONE, 0, 0, 0, 0);
        int x = getSize().x - (trim.width + trim.x);
        int width = 0;
        for (int i = 0; i < controls.length; i++) {
            int align = controlAlignments[i];
            if ((align & SWT.WRAP) == 0 && (align & SWT.LEAD) == 0 && !controls[i].isDisposed() && controls[i].getVisible()) {
                Point rightSize = controls[i].computeSize(SWT.DEFAULT, SWT.DEFAULT);
                width += rightSize.x;
            }
        }
        if (width != 0)
            width += SPACING * 2;
        x -= width;
        return Math.max(0, x);
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
        /*
	 * This call is intentionally commented out, to allow this getter method to be
	 * called from a thread which is different from one that created the widget.
	 */
        //checkWidget();
        if (selectedIndex == -1)
            return null;
        return items[selectedIndex];
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
        checkWidget();
        return selectionBackground;
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
        checkWidget();
        return selectionForeground;
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
        checkWidget();
        return simple;
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
        checkWidget();
        return single;
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
        checkWidget();
        if (fixedTabHeight != SWT.DEFAULT)
            return fixedTabHeight;
        // -1 for line drawn across top of tab //TODO: replace w/ computeTrim of tab area?
        return tabHeight - 1;
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
        checkWidget();
        return onBottom ? SWT.BOTTOM : SWT.TOP;
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
        checkWidget();
        return topRight;
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
        checkWidget();
        return showUnselectedClose;
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
        checkWidget();
        return showUnselectedImage;
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
        checkWidget();
        return showSelectedImage;
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

    void initAccessible() {
        final Accessible accessible = getAccessible();
        accessible.addAccessibleListener(new AccessibleAdapter() {

            @Override
            public void getName(AccessibleEvent e) {
                CTabItem item = null;
                int childID = e.childID;
                if (childID == ACC.CHILDID_SELF) {
                    if (selectedIndex != -1) {
                        item = items[selectedIndex];
                    }
                } else if (childID >= 0 && childID < items.length) {
                    item = items[childID];
                }
                e.result = item == null ? null : stripMnemonic(item.getText());
            }

            @Override
            public void getHelp(AccessibleEvent e) {
                String help = null;
                int childID = e.childID;
                if (childID == ACC.CHILDID_SELF) {
                    help = getToolTipText();
                } else if (childID >= 0 && childID < items.length) {
                    help = items[childID].getToolTipText();
                }
                e.result = help;
            }

            @Override
            public void getKeyboardShortcut(AccessibleEvent e) {
                String shortcut = null;
                int childID = e.childID;
                if (childID >= 0 && childID < items.length) {
                    String text = items[childID].getText();
                    if (text != null) {
                        char mnemonic = _findMnemonic(text);
                        if (mnemonic != '\0') {
                            //$NON-NLS-1$
                            shortcut = SWT.getMessage("SWT_Page_Mnemonic", new Object[] { Character.valueOf(mnemonic) });
                        }
                    }
                }
                if (childID == ACC.CHILDID_SELF) {
                    //$NON-NLS-1$
                    shortcut = SWT.getMessage("SWT_SwitchPage_Shortcut");
                }
                e.result = shortcut;
            }
        });
        accessible.addAccessibleControlListener(new AccessibleControlAdapter() {

            @Override
            public void getChildAtPoint(AccessibleControlEvent e) {
                Point testPoint = toControl(e.x, e.y);
                int childID = ACC.CHILDID_NONE;
                for (int i = 0; i < items.length; i++) {
                    if (items[i].getBounds().contains(testPoint)) {
                        childID = i;
                        break;
                    }
                }
                if (childID == ACC.CHILDID_NONE) {
                    Rectangle location = getBounds();
                    location.x = location.y = 0;
                    location.height = location.height - getClientArea().height;
                    if (location.contains(testPoint)) {
                        childID = ACC.CHILDID_SELF;
                    }
                }
                e.childID = childID;
            }

            @Override
            public void getLocation(AccessibleControlEvent e) {
                Rectangle location = null;
                Point pt = null;
                int childID = e.childID;
                if (childID == ACC.CHILDID_SELF) {
                    location = getBounds();
                    pt = getParent().toDisplay(location.x, location.y);
                } else {
                    if (childID >= 0 && childID < items.length && ((DartCTabItem) items[childID].getImpl()).showing) {
                        if (!items[childID].isDisposed()) {
                            location = items[childID].getBounds();
                        }
                    }
                    if (location != null) {
                        pt = toDisplay(location.x, location.y);
                    }
                }
                if (location != null && pt != null) {
                    e.x = pt.x;
                    e.y = pt.y;
                    e.width = location.width;
                    e.height = location.height;
                }
            }

            @Override
            public void getChildCount(AccessibleControlEvent e) {
                e.detail = items.length;
            }

            @Override
            public void getDefaultAction(AccessibleControlEvent e) {
                String action = null;
                int childID = e.childID;
                if (childID >= 0 && childID < items.length) {
                    //$NON-NLS-1$
                    action = SWT.getMessage("SWT_Switch");
                }
                e.result = action;
            }

            @Override
            public void getFocus(AccessibleControlEvent e) {
                int childID = ACC.CHILDID_NONE;
                if (isFocusControl()) {
                    if (selectedIndex == -1) {
                        childID = ACC.CHILDID_SELF;
                    } else {
                        childID = selectedIndex;
                    }
                }
                e.childID = childID;
            }

            @Override
            public void getRole(AccessibleControlEvent e) {
                int role = 0;
                int childID = e.childID;
                if (childID == ACC.CHILDID_SELF) {
                    role = ACC.ROLE_TABFOLDER;
                } else if (childID >= 0 && childID < items.length) {
                    role = ACC.ROLE_TABITEM;
                }
                e.detail = role;
            }

            @Override
            public void getSelection(AccessibleControlEvent e) {
                e.childID = (selectedIndex == -1) ? ACC.CHILDID_NONE : selectedIndex;
            }

            @Override
            public void getState(AccessibleControlEvent e) {
                int state = 0;
                int childID = e.childID;
                if (childID == ACC.CHILDID_SELF) {
                    state = ACC.STATE_NORMAL;
                } else if (childID >= 0 && childID < items.length) {
                    state = ACC.STATE_SELECTABLE;
                    if (isFocusControl()) {
                        state |= ACC.STATE_FOCUSABLE;
                    }
                    if (selectedIndex == childID) {
                        state |= ACC.STATE_SELECTED;
                        if (isFocusControl()) {
                            state |= ACC.STATE_FOCUSED;
                        }
                    }
                }
                e.detail = state;
            }

            @Override
            public void getChildren(AccessibleControlEvent e) {
                int childIdCount = items.length;
                Object[] children = new Object[childIdCount];
                for (int i = 0; i < childIdCount; i++) {
                    children[i] = Integer.valueOf(i);
                }
                e.children = children;
            }
        });
        addListener(SWT.Selection, event -> {
            if (isFocusControl()) {
                if (selectedIndex == -1) {
                    accessible.setFocus(ACC.CHILDID_SELF);
                } else {
                    accessible.setFocus(selectedIndex);
                }
            }
        });
        addListener(SWT.FocusIn, event -> {
            if (selectedIndex == -1) {
                accessible.setFocus(ACC.CHILDID_SELF);
            } else {
                accessible.setFocus(selectedIndex);
            }
        });
    }

    void initAccessibleMinMaxTb() {
        minMaxTb.getAccessible().addAccessibleListener(new AccessibleAdapter() {

            @Override
            public void getName(AccessibleEvent e) {
                if (e.childID != ACC.CHILDID_SELF) {
                    if (minItem != null && e.childID == minMaxTb.indexOf(minItem)) {
                        e.result = minItem.getToolTipText();
                    } else if (maxItem != null && e.childID == minMaxTb.indexOf(maxItem)) {
                        e.result = maxItem.getToolTipText();
                    }
                }
            }
        });
    }

    void initAccessibleChevronTb() {
        chevronTb.getAccessible().addAccessibleListener(new AccessibleAdapter() {

            @Override
            public void getName(AccessibleEvent e) {
                if (e.childID != ACC.CHILDID_SELF) {
                    if (chevronItem != null && e.childID == chevronTb.indexOf(chevronItem)) {
                        e.result = chevronItem.getToolTipText();
                    }
                }
            }
        });
    }

    void onKeyDown(Event event) {
        runUpdate();
        switch(event.keyCode) {
            case SWT.ARROW_LEFT:
            case SWT.ARROW_RIGHT:
                int count = items.length;
                if (count == 0)
                    return;
                if (selectedIndex == -1)
                    return;
                int leadKey = (getStyle() & SWT.RIGHT_TO_LEFT) != 0 ? SWT.ARROW_RIGHT : SWT.ARROW_LEFT;
                int offset = event.keyCode == leadKey ? -1 : 1;
                int index;
                if (!mru) {
                    index = selectedIndex + offset;
                } else {
                    int[] visible = new int[items.length];
                    int idx = 0;
                    int current = -1;
                    for (int i = 0; i < items.length; i++) {
                        if (((DartCTabItem) items[i].getImpl()).showing) {
                            if (i == selectedIndex)
                                current = idx;
                            visible[idx++] = i;
                        }
                    }
                    if (current + offset >= 0 && current + offset < idx) {
                        index = visible[current + offset];
                    } else {
                        if (showChevron) {
                            Rectangle chevronRect = chevronItem.getBounds();
                            chevronRect = event.display.map(chevronTb, this.getApi(), chevronRect);
                            CTabFolderEvent e = new CTabFolderEvent(this.getApi());
                            e.time = event.time;
                            e.x = chevronRect.x;
                            e.y = chevronRect.y;
                            e.width = chevronRect.width;
                            e.height = chevronRect.height;
                            e.doit = true;
                            for (CTabFolder2Listener folderListener : folderListeners) {
                                folderListener.showList(e);
                            }
                            if (e.doit && !isDisposed()) {
                                showList(chevronRect);
                            }
                        }
                        return;
                    }
                }
                if (index < 0 || index >= count)
                    return;
                setSelection(index, true);
                forceFocus();
        }
    }

    void onDispose(Event event) {
        removeListener(SWT.Dispose, listener);
        notifyListeners(SWT.Dispose, event);
        event.type = SWT.None;
        /*
	 * Usually when an item is disposed, destroyItem will change the size of the items array,
	 * reset the bounds of all the tabs and manage the widget associated with the tab.
	 * Since the whole folder is being disposed, this is not necessary.  For speed
	 * the inDispose flag is used to skip over this part of the item dispose.
	 */
        inDispose = true;
        if (showMenu != null && !showMenu.isDisposed()) {
            showMenu.dispose();
            showMenu = null;
        }
        int length = items.length;
        for (int i = 0; i < length; i++) {
            if (items[i] != null) {
                items[i].dispose();
            }
        }
        gradientColors = null;
        selectionGradientColors = null;
        selectionGradientPercents = null;
        selectionBgImage = null;
        selectionBackground = null;
        selectionForeground = null;
        if (controlBkImages != null) {
            for (int i = 0; i < controlBkImages.length; i++) {
                if (controlBkImages[i] != null) {
                    controlBkImages[i].dispose();
                    controlBkImages[i] = null;
                }
            }
            controlBkImages = null;
        }
        controls = null;
        controlAlignments = null;
        controlRects = null;
        if (maxImage != null)
            maxImage.dispose();
        maxImage = null;
        if (minImage != null)
            minImage.dispose();
        minImage = null;
        if (chevronImage != null)
            chevronImage.dispose();
        chevronImage = null;
        if (renderer != null)
            renderer.dispose();
        renderer = null;
        minItem = null;
        maxItem = null;
        minMaxTb = null;
        chevronItem = null;
        chevronTb = null;
        if (folderListeners.length != 0)
            folderListeners = new CTabFolder2Listener[0];
        if (tabListeners.length != 0)
            tabListeners = new CTabFolderListener[0];
    }

    void onDragDetect(Event event) {
        boolean consume = false;
        for (CTabItem item : items) {
            if (((DartCTabItem) item.getImpl()).closeRect.contains(event.x, event.y)) {
                consume = true;
                break;
            }
        }
        if (consume) {
            event.type = SWT.None;
        }
    }

    void onFocus(Event event) {
        checkWidget();
        if (selectedIndex >= 0) {
            redraw();
        } else {
            setSelection(0, true);
        }
    }

    boolean onMnemonic(Event event, boolean doit) {
        char key = event.character;
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                char mnemonic = _findMnemonic(items[i].getText());
                if (mnemonic != '\0') {
                    if (Character.toLowerCase(key) == mnemonic) {
                        if (doit) {
                            setSelection(i, true);
                            forceFocus();
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    void onMenuDetect(Event event) {
        if (event.detail == SWT.MENU_KEYBOARD) {
            if (selectedIndex != -1) {
                CTabItem item = items[selectedIndex];
                Rectangle rect = getDisplay().map(this.getApi(), null, item.getBounds());
                if (!rect.contains(event.x, event.y)) {
                    /* If the mouse is not in the currently-selected tab,
				 * then pop up the menu near the top-right corner of the current tab.
				 */
                    Rectangle itemTrim = renderer.computeTrim(selectedIndex, SWT.NONE, 0, 0, 0, 0);
                    Rectangle closeTrim = renderer.computeTrim(CTabFolderRenderer.PART_CLOSE_BUTTON, SWT.NONE, 0, 0, 0, 0);
                    event.x = rect.x + rect.width - ((DartCTabItem) item.getImpl()).closeRect.width + itemTrim.x - closeTrim.width;
                    event.y = rect.y - itemTrim.y - closeTrim.y;
                }
            }
        }
    }

    void onMouseDoubleClick(Event event) {
        if (event.button != 1 || (event.stateMask & SWT.BUTTON2) != 0 || (event.stateMask & SWT.BUTTON3) != 0)
            return;
        Event e = new Event();
        e.item = getItem(new Point(event.x, event.y));
        if (e.item != null) {
            notifyListeners(SWT.DefaultSelection, e);
        }
    }

    void onMouse(Event event) {
        if (isDisposed()) {
            return;
        }
        int x = event.x, y = event.y;
        switch(event.type) {
            case SWT.MouseEnter:
                {
                    setToolTipText(null);
                    break;
                }
            case SWT.MouseExit:
                {
                    for (int i = 0; i < items.length; i++) {
                        CTabItem item = items[i];
                        if (i != selectedIndex && ((DartCTabItem) item.getImpl()).closeImageState != SWT.BACKGROUND) {
                            ((DartCTabItem) item.getImpl()).closeImageState = SWT.BACKGROUND;
                            redraw(((DartCTabItem) item.getImpl()).closeRect.x, ((DartCTabItem) item.getImpl()).closeRect.y, ((DartCTabItem) item.getImpl()).closeRect.width, ((DartCTabItem) item.getImpl()).closeRect.height, false);
                        }
                        if ((item.state & SWT.HOT) != 0) {
                            item.state &= ~SWT.HOT;
                            redraw(((DartCTabItem) item.getImpl()).x, ((DartCTabItem) item.getImpl()).y, ((DartCTabItem) item.getImpl()).width, ((DartCTabItem) item.getImpl()).height, false);
                        }
                        if (i == selectedIndex && ((DartCTabItem) item.getImpl()).closeImageState != SWT.NONE) {
                            ((DartCTabItem) item.getImpl()).closeImageState = SWT.NONE;
                            redraw(((DartCTabItem) item.getImpl()).closeRect.x, ((DartCTabItem) item.getImpl()).closeRect.y, ((DartCTabItem) item.getImpl()).closeRect.width, ((DartCTabItem) item.getImpl()).closeRect.height, false);
                        }
                    }
                    break;
                }
            case SWT.MouseHover:
            case SWT.MouseDown:
                {
                    if (hoverTb && hoverRect.contains(x, y) && !hovering) {
                        hovering = true;
                        updateItems();
                        hoverTimerRunning = true;
                        event.display.timerExec(2000, new Runnable() {

                            @Override
                            public void run() {
                                if (isDisposed())
                                    return;
                                if (hovering) {
                                    Display display = getDisplay();
                                    Control c = display.getCursorControl();
                                    boolean reschedule = false;
                                    if (c != null) {
                                        for (Control control : controls) {
                                            Control temp = c;
                                            do {
                                                if (temp.equals(control)) {
                                                    reschedule = true;
                                                } else {
                                                    temp = temp.getParent();
                                                    if (temp == null || temp.equals(DartCTabFolder.this.getApi()))
                                                        break;
                                                }
                                            } while (!reschedule);
                                            if (reschedule)
                                                break;
                                        }
                                    }
                                    if (reschedule && hoverTimerRunning) {
                                        display.timerExec(2000, this);
                                    } else {
                                        hovering = false;
                                        updateItems();
                                    }
                                }
                            }
                        });
                        return;
                    }
                    if (event.button != 1)
                        return;
                    CTabItem item = null;
                    if (single) {
                        if (selectedIndex != -1) {
                            Rectangle bounds = items[selectedIndex].getBounds();
                            if (bounds.contains(x, y)) {
                                item = items[selectedIndex];
                            }
                        }
                    } else {
                        for (CTabItem tabItem : items) {
                            Rectangle bounds = tabItem.getBounds();
                            if (bounds.contains(x, y)) {
                                item = tabItem;
                            }
                        }
                    }
                    if (item != null) {
                        if (((DartCTabItem) item.getImpl()).closeRect.contains(x, y)) {
                            ((DartCTabItem) item.getImpl()).closeImageState = SWT.SELECTED;
                            redraw(((DartCTabItem) item.getImpl()).closeRect.x, ((DartCTabItem) item.getImpl()).closeRect.y, ((DartCTabItem) item.getImpl()).closeRect.width, ((DartCTabItem) item.getImpl()).closeRect.height, false);
                            update();
                            return;
                        }
                        int index = indexOf(item);
                        if (((DartCTabItem) item.getImpl()).showing) {
                            int oldSelectedIndex = selectedIndex;
                            setSelection(index, true);
                            if (oldSelectedIndex == selectedIndex) {
                                /* If the click is on the selected tabitem, then set focus to the tabfolder */
                                forceFocus();
                            }
                        }
                        return;
                    }
                    break;
                }
            case SWT.MouseMove:
                {
                    _setToolTipText(event.x, event.y);
                    boolean close = false;
                    for (int i = 0; i < items.length; i++) {
                        CTabItem item = items[i];
                        close = false;
                        if (item.getBounds().contains(x, y)) {
                            close = true;
                            if (((DartCTabItem) item.getImpl()).closeRect.contains(x, y)) {
                                if (((DartCTabItem) item.getImpl()).closeImageState != SWT.SELECTED && ((DartCTabItem) item.getImpl()).closeImageState != SWT.HOT) {
                                    ((DartCTabItem) item.getImpl()).closeImageState = SWT.HOT;
                                    redraw(((DartCTabItem) item.getImpl()).closeRect.x, ((DartCTabItem) item.getImpl()).closeRect.y, ((DartCTabItem) item.getImpl()).closeRect.width, ((DartCTabItem) item.getImpl()).closeRect.height, false);
                                }
                            } else {
                                if (((DartCTabItem) item.getImpl()).closeImageState != SWT.NONE) {
                                    ((DartCTabItem) item.getImpl()).closeImageState = SWT.NONE;
                                    redraw(((DartCTabItem) item.getImpl()).closeRect.x, ((DartCTabItem) item.getImpl()).closeRect.y, ((DartCTabItem) item.getImpl()).closeRect.width, ((DartCTabItem) item.getImpl()).closeRect.height, false);
                                }
                            }
                            if ((item.state & SWT.HOT) == 0) {
                                item.state |= SWT.HOT;
                                redraw(((DartCTabItem) item.getImpl()).x, ((DartCTabItem) item.getImpl()).y, ((DartCTabItem) item.getImpl()).width, ((DartCTabItem) item.getImpl()).height, false);
                            }
                        }
                        if (i != selectedIndex && ((DartCTabItem) item.getImpl()).closeImageState != SWT.BACKGROUND && !close) {
                            ((DartCTabItem) item.getImpl()).closeImageState = SWT.BACKGROUND;
                            redraw(((DartCTabItem) item.getImpl()).closeRect.x, ((DartCTabItem) item.getImpl()).closeRect.y, ((DartCTabItem) item.getImpl()).closeRect.width, ((DartCTabItem) item.getImpl()).closeRect.height, false);
                        }
                        if ((item.state & SWT.HOT) != 0 && !close) {
                            item.state &= ~SWT.HOT;
                            redraw(((DartCTabItem) item.getImpl()).x, ((DartCTabItem) item.getImpl()).y, ((DartCTabItem) item.getImpl()).width, ((DartCTabItem) item.getImpl()).height, false);
                        }
                        if (i == selectedIndex && ((DartCTabItem) item.getImpl()).closeImageState != SWT.NONE && !close) {
                            ((DartCTabItem) item.getImpl()).closeImageState = SWT.NONE;
                            redraw(((DartCTabItem) item.getImpl()).closeRect.x, ((DartCTabItem) item.getImpl()).closeRect.y, ((DartCTabItem) item.getImpl()).closeRect.width, ((DartCTabItem) item.getImpl()).closeRect.height, false);
                        }
                    }
                    break;
                }
            case SWT.MouseUp:
                {
                    if (event.button != 1)
                        return;
                    CTabItem item = null;
                    if (single) {
                        if (selectedIndex != -1) {
                            Rectangle bounds = items[selectedIndex].getBounds();
                            if (bounds.contains(x, y)) {
                                item = items[selectedIndex];
                            }
                        }
                    } else {
                        for (CTabItem tabItem : items) {
                            Rectangle bounds = tabItem.getBounds();
                            if (bounds.contains(x, y)) {
                                item = tabItem;
                            }
                        }
                    }
                    if (item != null) {
                        if (((DartCTabItem) item.getImpl()).closeRect.contains(x, y)) {
                            boolean selected = ((DartCTabItem) item.getImpl()).closeImageState == SWT.SELECTED;
                            ((DartCTabItem) item.getImpl()).closeImageState = SWT.HOT;
                            redraw(((DartCTabItem) item.getImpl()).closeRect.x, ((DartCTabItem) item.getImpl()).closeRect.y, ((DartCTabItem) item.getImpl()).closeRect.width, ((DartCTabItem) item.getImpl()).closeRect.height, false);
                            if (!selected)
                                return;
                            CTabFolderEvent e = new CTabFolderEvent(this.getApi());
                            e.time = event.time;
                            e.item = item;
                            e.doit = true;
                            for (CTabFolder2Listener listener : folderListeners) {
                                listener.close(e);
                            }
                            for (CTabFolderListener listener : tabListeners) {
                                listener.itemClosed(e);
                            }
                            if (e.doit)
                                item.dispose();
                            if (!isDisposed() && item.isDisposed()) {
                                Display display = getDisplay();
                                Point pt = display.getCursorLocation();
                                pt = display.map(null, this.getApi(), pt.x, pt.y);
                                CTabItem nextItem = getItem(pt);
                                if (nextItem != null) {
                                    if (((DartCTabItem) nextItem.getImpl()).closeRect.contains(pt)) {
                                        if (((DartCTabItem) nextItem.getImpl()).closeImageState != SWT.SELECTED && ((DartCTabItem) nextItem.getImpl()).closeImageState != SWT.HOT) {
                                            ((DartCTabItem) nextItem.getImpl()).closeImageState = SWT.HOT;
                                            redraw(((DartCTabItem) nextItem.getImpl()).closeRect.x, ((DartCTabItem) nextItem.getImpl()).closeRect.y, ((DartCTabItem) nextItem.getImpl()).closeRect.width, ((DartCTabItem) nextItem.getImpl()).closeRect.height, false);
                                        }
                                    } else {
                                        if (((DartCTabItem) nextItem.getImpl()).closeImageState != SWT.NONE) {
                                            ((DartCTabItem) nextItem.getImpl()).closeImageState = SWT.NONE;
                                            redraw(((DartCTabItem) nextItem.getImpl()).closeRect.x, ((DartCTabItem) nextItem.getImpl()).closeRect.y, ((DartCTabItem) nextItem.getImpl()).closeRect.width, ((DartCTabItem) nextItem.getImpl()).closeRect.height, false);
                                        }
                                    }
                                }
                            }
                            return;
                        }
                    }
                }
        }
    }

    void onPageTraversal(Event event) {
        int count = items.length;
        if (count == 0)
            return;
        int index = selectedIndex;
        if (index == -1) {
            index = 0;
        } else {
            int offset = (event.detail == SWT.TRAVERSE_PAGE_NEXT) ? 1 : -1;
            if (!mru) {
                index = (selectedIndex + offset + count) % count;
            } else {
                int[] visible = new int[items.length];
                int idx = 0;
                int current = -1;
                for (int i = 0; i < items.length; i++) {
                    if (((DartCTabItem) items[i].getImpl()).showing) {
                        if (i == selectedIndex)
                            current = idx;
                        visible[idx++] = i;
                    }
                }
                if (current + offset >= 0 && current + offset < idx) {
                    index = visible[current + offset];
                } else {
                    if (showChevron) {
                        Rectangle chevronRect = chevronItem.getBounds();
                        chevronRect = event.display.map(chevronTb, this.getApi(), chevronRect);
                        CTabFolderEvent e = new CTabFolderEvent(this.getApi());
                        e.time = event.time;
                        e.x = chevronRect.x;
                        e.y = chevronRect.y;
                        e.width = chevronRect.width;
                        e.height = chevronRect.height;
                        e.doit = true;
                        for (CTabFolder2Listener folderListener : folderListeners) {
                            folderListener.showList(e);
                        }
                        if (e.doit && !isDisposed()) {
                            showList(chevronRect);
                        }
                    }
                }
            }
        }
        setSelection(index, true);
    }

    void onPaint(Event event) {
        if (inDispose)
            return;
        Font font = getFont();
        if (oldFont == null || !oldFont.equals(font)) {
            // handle case where  default font changes
            oldFont = font;
            if (!updateTabHeight(false)) {
                updateItems();
                redraw();
                return;
            }
        }
        GC gc = event.gc;
        Font gcFont = gc.getFont();
        Color gcBackground = gc.getBackground();
        Color gcForeground = gc.getForeground();
        // Useful for debugging paint problems
        //{
        //Point size = getSize();
        //gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_GREEN));
        //gc.fillRectangle(-10, -10, size.x + 20, size.y+20);
        //}
        Point size = getSize();
        Rectangle bodyRect = new Rectangle(0, 0, size.x, size.y);
        renderer.draw(CTabFolderRenderer.PART_BODY, SWT.BACKGROUND | SWT.FOREGROUND, bodyRect, gc);
        gc.setFont(gcFont);
        gc.setForeground(gcForeground);
        gc.setBackground(gcBackground);
        renderer.draw(CTabFolderRenderer.PART_HEADER, SWT.BACKGROUND | SWT.FOREGROUND, bodyRect, gc);
        gc.setFont(gcFont);
        gc.setForeground(gcForeground);
        gc.setBackground(gcBackground);
        if (!single) {
            for (int i = 0; i < items.length; i++) {
                Rectangle itemBounds = items[i].getBounds();
                if (i != selectedIndex && event.getBounds().intersects(itemBounds)) {
                    renderer.draw(i, SWT.BACKGROUND | SWT.FOREGROUND | items[i].state, itemBounds, gc);
                }
            }
        }
        gc.setFont(gcFont);
        gc.setForeground(gcForeground);
        gc.setBackground(gcBackground);
        if (selectedIndex != -1) {
            renderer.draw(selectedIndex, items[selectedIndex].state | SWT.BACKGROUND | SWT.FOREGROUND, items[selectedIndex].getBounds(), gc);
        }
        gc.setFont(gcFont);
        gc.setForeground(gcForeground);
        gc.setBackground(gcBackground);
        if (hoverTb) {
            Rectangle trim = renderer.computeTrim(CTabFolderRenderer.PART_BORDER, SWT.NONE, 0, 0, 0, 0);
            int x = getSize().x - (trim.width + trim.x);
            hoverRect = new Rectangle(x - 16 - SPACING, 2, 16, getTabHeight() - 2);
            gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
            x = hoverRect.x;
            int y = hoverRect.y;
            gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
            gc.fillRectangle(x + hoverRect.width - 6, y, 5, 5);
            gc.drawRectangle(x + hoverRect.width - 6, y, 5, 5);
            gc.drawLine(x + hoverRect.width - 6, y + 2, x + hoverRect.width - 6 + 5, y + 2);
            gc.fillRectangle(x, y, 5, 2);
            gc.drawRectangle(x, y, 5, 2);
        }
        gc.setFont(gcFont);
        gc.setForeground(gcForeground);
        gc.setBackground(gcBackground);
    }

    void onResize(Event event) {
        if (inDispose)
            return;
        if (ignoreResize)
            return;
        if (updateItems()) {
            redrawTabs();
        }
        Point size = getSize();
        if (oldSize == null) {
            redraw();
        } else {
            if (onBottom && size.y != oldSize.y) {
                redraw();
            } else {
                int x1 = Math.min(size.x, oldSize.x);
                Rectangle trim = renderer.computeTrim(CTabFolderRenderer.PART_BODY, SWT.NONE, 0, 0, 0, 0);
                if (size.x != oldSize.x)
                    x1 -= trim.width + trim.x - getApi().marginWidth + 2;
                // rounded top right corner
                if (!simple)
                    x1 -= 5;
                int y1 = Math.min(size.y, oldSize.y);
                if (size.y != oldSize.y)
                    y1 -= trim.height + trim.y - getApi().marginHeight;
                int x2 = Math.max(size.x, oldSize.x);
                int y2 = Math.max(size.y, oldSize.y);
                redraw(0, y1, x2, y2 - y1, false);
                redraw(x1, 0, x2 - x1, y2, false);
                if (hoverTb) {
                    redraw(hoverRect.x, hoverRect.y, hoverRect.width, hoverRect.height, false);
                }
            }
        }
        oldSize = size;
    }

    void onSelection(Event event) {
        if (hovering) {
            hovering = false;
            updateItems();
        }
        if (event.widget == maxItem) {
            CTabFolderEvent e = new CTabFolderEvent(this.getApi());
            e.time = event.time;
            for (CTabFolder2Listener folderListener : folderListeners) {
                if (maximized) {
                    folderListener.restore(e);
                } else {
                    folderListener.maximize(e);
                }
            }
        } else if (event.widget == minItem) {
            CTabFolderEvent e = new CTabFolderEvent(this.getApi());
            e.time = event.time;
            for (CTabFolder2Listener folderListener : folderListeners) {
                if (minimized) {
                    folderListener.restore(e);
                } else {
                    folderListener.minimize(e);
                }
            }
        } else if (event.widget == chevronItem) {
            Rectangle chevronRect = chevronItem.getBounds();
            chevronRect = event.display.map(chevronTb, this.getApi(), chevronRect);
            CTabFolderEvent e = new CTabFolderEvent(this.getApi());
            e.time = event.time;
            e.x = chevronRect.x;
            e.y = chevronRect.y;
            e.width = chevronRect.width;
            e.height = chevronRect.height;
            e.doit = true;
            for (CTabFolder2Listener folderListener : folderListeners) {
                folderListener.showList(e);
            }
            if (e.doit && !isDisposed()) {
                showList(chevronRect);
            }
        }
    }

    void onTraverse(Event event) {
        if (ignoreTraverse)
            return;
        runUpdate();
        switch(event.detail) {
            case SWT.TRAVERSE_ESCAPE:
            case SWT.TRAVERSE_RETURN:
            case SWT.TRAVERSE_TAB_NEXT:
            case SWT.TRAVERSE_TAB_PREVIOUS:
                Control focusControl = getDisplay().getFocusControl();
                if (focusControl == this.getApi())
                    event.doit = true;
                break;
            case SWT.TRAVERSE_MNEMONIC:
                event.doit = onMnemonic(event, false);
                break;
            case SWT.TRAVERSE_PAGE_NEXT:
            case SWT.TRAVERSE_PAGE_PREVIOUS:
                event.doit = items.length > 0;
                break;
        }
        ignoreTraverse = true;
        notifyListeners(SWT.Traverse, event);
        ignoreTraverse = false;
        event.type = SWT.None;
        if (isDisposed())
            return;
        if (!event.doit)
            return;
        switch(event.detail) {
            case SWT.TRAVERSE_MNEMONIC:
                onMnemonic(event, true);
                event.detail = SWT.TRAVERSE_NONE;
                break;
            case SWT.TRAVERSE_PAGE_NEXT:
            case SWT.TRAVERSE_PAGE_PREVIOUS:
                onPageTraversal(event);
                event.detail = SWT.TRAVERSE_NONE;
                break;
        }
    }

    void redrawTabs() {
        Point size = getSize();
        Rectangle trim = renderer.computeTrim(CTabFolderRenderer.PART_BODY, SWT.NONE, 0, 0, 0, 0);
        if (onBottom) {
            int h = trim.height + trim.y - getApi().marginHeight;
            redraw(0, size.y - h - 1, size.x, h + 1, false);
        } else {
            redraw(0, 0, size.x, -trim.y - getApi().marginHeight + 1, false);
        }
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
        ((DartCTabFolderRenderer) renderer.getImpl()).createAntialiasColors();
        updateBkImages(true);
        redraw();
        getBridge().dirty(this);
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
        getBridge().dirty(this);
    }

    @Override
    public void setBackgroundImage(Image image) {
        super.setBackgroundImage(image);
        //TODO: need better caching strategy
        ((DartCTabFolderRenderer) renderer.getImpl()).createAntialiasColors();
        redraw();
        getBridge().dirty(this);
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
        checkWidget();
        if (borderVisible == show)
            return;
        this.borderVisible = show;
        updateFolder(REDRAW);
        getBridge().dirty(this);
    }

    /**
     * Create or dispose min/max buttons.
     */
    void updateButtons() {
        // max button
        Display display = getDisplay();
        if (showMax) {
            if (minMaxTb == null) {
                minMaxTb = new ToolBar(this.getApi(), SWT.FLAT);
                initAccessibleMinMaxTb();
                addTabControl(minMaxTb, SWT.TRAIL, 0, false);
            }
            if (maxItem == null) {
                maxItem = new ToolItem(minMaxTb, SWT.PUSH);
                if (maxImage == null) {
                    maxImage = createButtonImage(display, CTabFolderRenderer.PART_MAX_BUTTON);
                }
                maxItem.setImage(maxImage);
                //$NON-NLS-1$ //$NON-NLS-2$
                maxItem.setToolTipText(maximized ? SWT.getMessage("SWT_Restore") : SWT.getMessage("SWT_Maximize"));
                maxItem.addListener(SWT.Selection, listener);
            }
        } else {
            //might need to remove it if already there
            if (maxItem != null) {
                maxItem.dispose();
                maxItem = null;
            }
        }
        // min button
        if (showMin) {
            if (minMaxTb == null) {
                minMaxTb = new ToolBar(this.getApi(), SWT.FLAT);
                initAccessibleMinMaxTb();
                addTabControl(minMaxTb, SWT.TRAIL, 0, false);
            }
            if (minItem == null) {
                minItem = new ToolItem(minMaxTb, SWT.PUSH, 0);
                if (minImage == null) {
                    minImage = createButtonImage(display, CTabFolderRenderer.PART_MIN_BUTTON);
                }
                minItem.setImage(minImage);
                //$NON-NLS-1$ //$NON-NLS-2$
                minItem.setToolTipText(minimized ? SWT.getMessage("SWT_Restore") : SWT.getMessage("SWT_Minimize"));
                minItem.addListener(SWT.Selection, listener);
            }
        } else {
            //might need to remove it if already there
            if (minItem != null) {
                minItem.dispose();
                minItem = null;
            }
        }
        if (minMaxTb != null && minMaxTb.getItemCount() == 0) {
            removeTabControl(minMaxTb, false);
            minMaxTb.dispose();
            minMaxTb = null;
        }
    }

    /**
     * Update button bounds for min/max and update chevron button.
     */
    void setButtonBounds() {
        if (showChevron) {
            updateChevronImage(false);
        }
        Point size = getSize();
        boolean[][] overflow = new boolean[1][0];
        Rectangle[] rects = computeControlBounds(size, overflow);
        if (fixedTabHeight != SWT.DEFAULT) {
            int height = fixedTabHeight;
            if (!hovering) {
                hoverTb = false;
                Rectangle tabBounds = this.getBounds();
                for (int i = 0; i < rects.length; i++) {
                    if (!(overflow[0][i])) {
                        if (rects[i].height > height) {
                            hoverTb = true;
                            break;
                        }
                    }
                }
                if (hoverTb) {
                    for (int i = 0; i < rects.length; i++) {
                        if (!(overflow[0][i])) {
                            if (rects[i].height > height) {
                                rects[i].x = tabBounds.width + 20;
                            }
                        }
                    }
                }
            }
        }
        int headerHeight = 0;
        for (int i = 0; i < rects.length; i++) {
            if (!overflow[0][i])
                headerHeight = Math.max(rects[i].height, headerHeight);
        }
        boolean changed = false;
        ignoreResize = true;
        for (int i = 0; i < controls.length; i++) {
            if (!controls[i].isDisposed()) {
                if (overflow[0][i]) {
                    controls[i].setBounds(rects[i]);
                } else {
                    controls[i].moveAbove(null);
                    controls[i].setBounds(rects[i].x, rects[i].y, rects[i].width, headerHeight);
                }
            }
            if (!changed && !rects[i].equals(controlRects[i])) {
                // also updateBkImages after translation
                changed = true;
            }
        }
        ignoreResize = false;
        controlRects = rects;
        if (changed || hovering) {
            updateBkImages(false);
        }
    }

    /**
     * Get the number of hidden items or the number which is to be drawn in the
     * chevon item.
     * <p>
     * Note: do not confuse this with {@link #chevronCount} which contains the count
     * from the last time the cached chevron image was drawn. It can be different
     * from the value returned by this method.
     * </p>
     *
     * @return the chevron count
     */
    int getChevronCount() {
        int itemCount = items.length;
        int count;
        if (single) {
            count = selectedIndex == -1 ? itemCount : itemCount - 1;
        } else {
            int showCount = 0;
            while (showCount < priority.length && ((DartCTabItem) items[priority[showCount]].getImpl()).showing) {
                showCount++;
            }
            count = itemCount - showCount;
        }
        return count;
    }

    /**
     * Update the cached chevron image.
     *
     * @param styleChange <code>true</code> if the update is required for changed
     *                    appearance of the chevron. In this case the image is not
     *                    created if it does not already exist and is updated even
     *                    if the drawn number (chevonCount) has not changed.
     */
    private void updateChevronImage(boolean styleChange) {
        if (styleChange && chevronImage == null)
            return;
        int newCount = getChevronCount();
        if (!styleChange && chevronCount == newCount)
            return;
        if (chevronImage != null)
            chevronImage.dispose();
        chevronImage = createButtonImage(getDisplay(), CTabFolderRenderer.PART_CHEVRON_BUTTON);
        chevronItem.setImage(chevronImage);
        chevronCount = newCount;
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
                if (((DartCTabItem) item.getImpl()).setFocus())
                    return true;
            }
        }
        return super.setFocus();
    }

    /* Copy of isFocusAncestor from Control. */
    boolean isAncestor(Control control) {
        while (control != null && control != this.getApi() && !(control instanceof Shell)) {
            control = control.getParent();
        }
        return control == this.getApi();
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
        ((DartCTabFolderRenderer) renderer.getImpl()).resetChevronFont();
        updateChevronImage(true);
        updateFolder(REDRAW);
        getBridge().dirty(this);
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
        getBridge().dirty(this);
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

    boolean setItemLocation(GC gc) {
        boolean changed = false;
        if (items.length == 0)
            return false;
        Rectangle trim = renderer.computeTrim(CTabFolderRenderer.PART_BORDER, SWT.NONE, 0, 0, 0, 0);
        int borderBottom = trim.height + trim.y;
        int borderTop = -trim.y;
        Point size = getSize();
        int y = onBottom ? Math.max(borderBottom, size.y - borderBottom - tabHeight) : borderTop;
        Point closeButtonSize = renderer.computeSize(CTabFolderRenderer.PART_CLOSE_BUTTON, 0, gc, SWT.DEFAULT, SWT.DEFAULT);
        int leftItemEdge = getLeftItemEdge(gc, CTabFolderRenderer.PART_BORDER);
        if (single) {
            // off screen
            int defaultX = getDisplay().getBounds().width + 10;
            for (int i = 0; i < items.length; i++) {
                CTabItem item = items[i];
                if (i == selectedIndex) {
                    firstIndex = selectedIndex;
                    int oldX = ((DartCTabItem) item.getImpl()).x, oldY = ((DartCTabItem) item.getImpl()).y;
                    ((DartCTabItem) item.getImpl()).x = leftItemEdge;
                    ((DartCTabItem) item.getImpl()).y = y;
                    ((DartCTabItem) item.getImpl()).showing = true;
                    if (showClose || ((DartCTabItem) item.getImpl()).showClose) {
                        ((DartCTabItem) item.getImpl()).closeRect.x = leftItemEdge - renderer.computeTrim(i, SWT.NONE, 0, 0, 0, 0).x;
                        ((DartCTabItem) item.getImpl()).closeRect.y = onBottom ? size.y - borderBottom - tabHeight + (tabHeight - closeButtonSize.y) / 2 : borderTop + (tabHeight - closeButtonSize.y) / 2;
                    }
                    if (((DartCTabItem) item.getImpl()).x != oldX || ((DartCTabItem) item.getImpl()).y != oldY)
                        changed = true;
                } else {
                    ((DartCTabItem) item.getImpl()).x = defaultX;
                    ((DartCTabItem) item.getImpl()).showing = false;
                }
            }
        } else {
            int rightItemEdge = getRightItemEdge(gc);
            int maxWidth = rightItemEdge - leftItemEdge;
            int width = 0;
            for (int i = 0; i < priority.length; i++) {
                CTabItem item = items[priority[i]];
                width += ((DartCTabItem) item.getImpl()).width;
                ((DartCTabItem) item.getImpl()).showing = i == 0 ? true : ((DartCTabItem) item.getImpl()).width > 0 && width <= maxWidth;
            }
            int x = getLeftItemEdge(gc, CTabFolderRenderer.PART_HEADER);
            // off screen
            int defaultX = getDisplay().getBounds().width + 10;
            firstIndex = items.length - 1;
            for (int i = 0; i < items.length; i++) {
                CTabItem item = items[i];
                if (!((DartCTabItem) item.getImpl()).showing) {
                    if (((DartCTabItem) item.getImpl()).x != defaultX)
                        changed = true;
                    ((DartCTabItem) item.getImpl()).x = defaultX;
                } else {
                    firstIndex = Math.min(firstIndex, i);
                    if (((DartCTabItem) item.getImpl()).x != x || ((DartCTabItem) item.getImpl()).y != y)
                        changed = true;
                    ((DartCTabItem) item.getImpl()).x = x;
                    ((DartCTabItem) item.getImpl()).y = y;
                    int state = SWT.NONE;
                    if (i == selectedIndex)
                        state |= SWT.SELECTED;
                    Rectangle edgeTrim = renderer.computeTrim(i, state, 0, 0, 0, 0);
                    ((DartCTabItem) item.getImpl()).closeRect.x = ((DartCTabItem) item.getImpl()).x + ((DartCTabItem) item.getImpl()).width - (edgeTrim.width + edgeTrim.x) - closeButtonSize.x;
                    ((DartCTabItem) item.getImpl()).closeRect.y = onBottom ? size.y - borderBottom - tabHeight + (tabHeight - closeButtonSize.y) / 2 : borderTop + (tabHeight - closeButtonSize.y) / 2;
                    x = x + ((DartCTabItem) item.getImpl()).width;
                    //TODO: fix next item position
                    if (!simple && i == selectedIndex)
                        x -= ((DartCTabFolderRenderer) renderer.getImpl()).curveIndent;
                }
            }
        }
        return changed;
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
    /*public*/
    void setItemOrder(int[] indices) {
        checkWidget();
        if (indices == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (indices.length != items.length)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        int newSelectedIndex = -1;
        boolean[] seen = new boolean[items.length];
        CTabItem[] temp = new CTabItem[items.length];
        for (int i = 0; i < indices.length; i++) {
            int index = indices[i];
            if (!(0 <= index && index < items.length))
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
            if (seen[index])
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
            seen[index] = true;
            if (index == selectedIndex)
                newSelectedIndex = i;
            temp[i] = items[index];
        }
        items = temp;
        selectedIndex = newSelectedIndex;
        updateFolder(REDRAW);
    }

    boolean setItemSize(GC gc) {
        boolean changed = false;
        if (isDisposed())
            return changed;
        Point size = getSize();
        if (size.x <= 0 || size.y <= 0)
            return changed;
        ToolBar chevron = getChevron();
        if (chevron != null)
            chevron.setVisible(false);
        showChevron = false;
        if (single) {
            showChevron = chevronVisible && items.length > 1;
            if (showChevron) {
                chevron.setVisible(true);
            }
            if (selectedIndex != -1) {
                CTabItem tab = items[selectedIndex];
                int width = renderer.computeSize(selectedIndex, SWT.SELECTED, gc, SWT.DEFAULT, SWT.DEFAULT).x;
                width = Math.min(width, getRightItemEdge(gc) - getLeftItemEdge(gc, CTabFolderRenderer.PART_BORDER));
                if (((DartCTabItem) tab.getImpl()).height != tabHeight || ((DartCTabItem) tab.getImpl()).width != width) {
                    changed = true;
                    ((DartCTabItem) tab.getImpl()).shortenedText = null;
                    ((DartCTabItem) tab.getImpl()).shortenedTextWidth = 0;
                    ((DartCTabItem) tab.getImpl()).height = tabHeight;
                    ((DartCTabItem) tab.getImpl()).width = width;
                    ((DartCTabItem) tab.getImpl()).closeRect.width = ((DartCTabItem) tab.getImpl()).closeRect.height = 0;
                    if (showClose || ((DartCTabItem) tab.getImpl()).showClose) {
                        Point closeSize = renderer.computeSize(CTabFolderRenderer.PART_CLOSE_BUTTON, SWT.SELECTED, gc, SWT.DEFAULT, SWT.DEFAULT);
                        ((DartCTabItem) tab.getImpl()).closeRect.width = closeSize.x;
                        ((DartCTabItem) tab.getImpl()).closeRect.height = closeSize.y;
                    }
                }
            }
            return changed;
        }
        if (items.length == 0)
            return changed;
        int[] widths;
        int tabAreaWidth = Math.max(0, getRightItemEdge(gc) - getLeftItemEdge(gc, CTabFolderRenderer.PART_BORDER));
        // First, try the minimum tab size at full compression.
        int minWidth = 0;
        int[] minWidths = new int[items.length];
        for (int element : priority) {
            int index = element;
            int state = CTabFolderRenderer.MINIMUM_SIZE;
            if (index == selectedIndex)
                state |= SWT.SELECTED;
            minWidths[index] = renderer.computeSize(index, state, gc, SWT.DEFAULT, SWT.DEFAULT).x;
            minWidth += minWidths[index];
            if (minWidth > tabAreaWidth)
                break;
        }
        if (minWidth > tabAreaWidth) {
            // full compression required and a chevron
            showChevron = chevronVisible && items.length > 1;
            if (showChevron) {
                tabAreaWidth -= chevron.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
                chevron.setVisible(true);
            }
            widths = minWidths;
            int index = selectedIndex != -1 ? selectedIndex : 0;
            if (tabAreaWidth < widths[index]) {
                widths[index] = Math.max(0, tabAreaWidth);
            }
        } else {
            int maxWidth = 0;
            int[] maxWidths = new int[items.length];
            for (int i = 0; i < items.length; i++) {
                int state = 0;
                if (i == selectedIndex)
                    state |= SWT.SELECTED;
                maxWidths[i] = renderer.computeSize(i, state, gc, SWT.DEFAULT, SWT.DEFAULT).x;
                maxWidth += maxWidths[i];
            }
            if (maxWidth <= tabAreaWidth) {
                // no compression required
                widths = maxWidths;
            } else {
                // determine compression for each item
                int extra = (tabAreaWidth - minWidth) / items.length;
                while (true) {
                    int large = 0, totalWidth = 0;
                    for (int i = 0; i < items.length; i++) {
                        if (maxWidths[i] > minWidths[i] + extra) {
                            totalWidth += minWidths[i] + extra;
                            large++;
                        } else {
                            totalWidth += maxWidths[i];
                        }
                    }
                    if (totalWidth >= tabAreaWidth) {
                        extra--;
                        break;
                    }
                    if (large == 0 || tabAreaWidth - totalWidth < large)
                        break;
                    extra++;
                }
                widths = new int[items.length];
                for (int i = 0; i < items.length; i++) {
                    widths[i] = Math.min(maxWidths[i], minWidths[i] + extra);
                }
            }
        }
        for (int i = 0; i < items.length; i++) {
            CTabItem tab = items[i];
            int width = widths[i];
            changed = true;
            ((DartCTabItem) tab.getImpl()).shortenedText = null;
            ((DartCTabItem) tab.getImpl()).shortenedTextWidth = 0;
            ((DartCTabItem) tab.getImpl()).height = tabHeight;
            ((DartCTabItem) tab.getImpl()).width = width;
            ((DartCTabItem) tab.getImpl()).closeRect.width = ((DartCTabItem) tab.getImpl()).closeRect.height = 0;
            if (showClose || ((DartCTabItem) tab.getImpl()).showClose) {
                if (i == selectedIndex || showUnselectedClose) {
                    Point closeSize = renderer.computeSize(CTabFolderRenderer.PART_CLOSE_BUTTON, SWT.NONE, gc, SWT.DEFAULT, SWT.DEFAULT);
                    ((DartCTabItem) tab.getImpl()).closeRect.width = closeSize.x;
                    ((DartCTabItem) tab.getImpl()).closeRect.height = closeSize.y;
                }
            }
        }
        return changed;
    }

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
        checkWidget();
        if (showMax == visible)
            return;
        // display maximize button
        showMax = visible;
        updateFolder(UPDATE_TAB_HEIGHT | REDRAW);
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
        checkWidget();
        if (this.maximized == maximize)
            return;
        if (maximize && this.minimized)
            setMinimized(false);
        this.maximized = maximize;
        if (minMaxTb != null && maxItem != null) {
            if (maxImage != null)
                maxImage.dispose();
            maxImage = createButtonImage(getDisplay(), CTabFolderRenderer.PART_MAX_BUTTON);
            maxItem.setImage(maxImage);
            //$NON-NLS-1$ //$NON-NLS-2$
            maxItem.setToolTipText(maximized ? SWT.getMessage("SWT_Restore") : SWT.getMessage("SWT_Maximize"));
        }
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
        checkWidget();
        if (showMin == visible)
            return;
        // display minimize button
        showMin = visible;
        updateFolder(UPDATE_TAB_HEIGHT | REDRAW);
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
        checkWidget();
        if (this.minimized == minimize)
            return;
        if (minimize && this.maximized)
            setMaximized(false);
        this.minimized = minimize;
        if (minMaxTb != null && minItem != null) {
            if (minImage != null)
                minImage.dispose();
            minImage = createButtonImage(getDisplay(), CTabFolderRenderer.PART_MIN_BUTTON);
            minItem.setImage(minImage);
            //$NON-NLS-1$ //$NON-NLS-2$
            minItem.setToolTipText(minimized ? SWT.getMessage("SWT_Restore") : SWT.getMessage("SWT_Minimize"));
        }
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
        checkWidget();
        if (count < 0)
            SWT.error(SWT.ERROR_INVALID_RANGE);
        if (minChars == count)
            return;
        minChars = count;
        updateFolder(REDRAW_TABS);
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
        checkWidget();
        if (mru == show)
            return;
        mru = show;
        if (!mru) {
            if (firstIndex == -1)
                return;
            int idx = firstIndex;
            int next = 0;
            for (int i = firstIndex; i < items.length; i++) {
                priority[next++] = i;
            }
            for (int i = 0; i < idx; i++) {
                priority[next++] = i;
            }
            updateFolder(REDRAW_TABS);
        }
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
        checkWidget();
        if (this.renderer == renderer || (useDefaultRenderer && renderer == null))
            return;
        if (this.renderer != null)
            this.renderer.dispose();
        useDefaultRenderer = renderer == null;
        if (useDefaultRenderer)
            renderer = new CTabFolderRenderer(this.getApi());
        this.renderer = renderer;
        updateFolder(REDRAW);
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
        checkWidget();
        if (item == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        int index = indexOf(item);
        setSelection(index);
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
        checkWidget();
        if (index < 0 || index >= items.length)
            return;
        CTabItem selection = items[index];
        if (selectedIndex == index) {
            showItem(selection);
            return;
        }
        int oldIndex = selectedIndex;
        selectedIndex = index;
        if (oldIndex != -1) {
            ((DartCTabItem) items[oldIndex].getImpl()).closeImageState = SWT.BACKGROUND;
            items[oldIndex].state &= ~SWT.SELECTED;
        }
        ((DartCTabItem) selection.getImpl()).closeImageState = SWT.NONE;
        ((DartCTabItem) selection.getImpl()).showing = false;
        selection.state |= SWT.SELECTED;
        Control newControl = ((DartCTabItem) selection.getImpl()).control;
        Control oldControl = null;
        if (oldIndex != -1) {
            oldControl = ((DartCTabItem) items[oldIndex].getImpl()).control;
        }
        if (newControl != oldControl) {
            if (newControl != null && !newControl.isDisposed()) {
                newControl.setBounds(getClientArea());
                newControl.setVisible(true);
            }
            if (oldControl != null && !oldControl.isDisposed()) {
                oldControl.setVisible(false);
            }
        }
        showItem(selection);
        redraw();
        getBridge().dirty(this);
    }

    void setSelection(int index, boolean notify) {
        int oldSelectedIndex = selectedIndex;
        setSelection(index);
        if (notify && selectedIndex != oldSelectedIndex && selectedIndex != -1) {
            Event event = new Event();
            event.item = getItem(selectedIndex);
            notifyListeners(SWT.Selection, event);
        }
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
        if (inDispose)
            return;
        checkWidget();
        setSelectionHighlightGradientColor(null);
        if (selectionBackground == color)
            return;
        if (color == null)
            color = getDisplay().getSystemColor(SELECTION_BACKGROUND);
        selectionBackground = color;
        //TODO:  need better caching strategy
        ((DartCTabFolderRenderer) renderer.getImpl()).createAntialiasColors();
        if (selectedIndex > -1)
            redraw();
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
        setSelectionBackground(colors, percents, false);
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
        checkWidget();
        int colorsLength;
        //null == no highlight
        Color highlightBeginColor = null;
        if (colors != null) {
            //The colors array can optionally have an extra entry which describes the highlight top color
            //Thus its either one or two larger than the percents array
            if (percents == null || !((percents.length == colors.length - 1) || (percents.length == colors.length - 2))) {
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
            //If the colors is exactly two more than percents then last is highlight
            //Keep track of *real* colorsLength (minus the highlight)
            if (percents.length == colors.length - 2) {
                highlightBeginColor = colors[colors.length - 1];
                colorsLength = colors.length - 1;
            } else {
                colorsLength = colors.length;
            }
            if (getDisplay().getDepth() < 15) {
                // Don't use gradients on low color displays
                colors = new Color[] { colors[colorsLength - 1] };
                colorsLength = colors.length;
                percents = new int[] {};
            }
        } else {
            colorsLength = 0;
        }
        // Are these settings the same as before?
        if (selectionBgImage == null) {
            if ((selectionGradientColors != null) && (colors != null) && (selectionGradientColors.length == colorsLength)) {
                boolean same = false;
                for (int i = 0; i < selectionGradientColors.length; i++) {
                    if (selectionGradientColors[i] == null) {
                        same = colors[i] == null;
                    } else {
                        same = selectionGradientColors[i].equals(colors[i]);
                    }
                    if (!same)
                        break;
                }
                if (same) {
                    for (int i = 0; i < selectionGradientPercents.length; i++) {
                        same = selectionGradientPercents[i] == percents[i];
                        if (!same)
                            break;
                    }
                }
                if (same && this.selectionGradientVertical == vertical)
                    return;
            }
        } else {
            selectionBgImage = null;
        }
        // Store the new settings
        if (colors == null) {
            selectionGradientColors = null;
            selectionGradientPercents = null;
            selectionGradientVertical = false;
            setSelectionBackground((Color) null);
            setSelectionHighlightGradientColor(null);
        } else {
            selectionGradientColors = new Color[colorsLength];
            for (int i = 0; i < colorsLength; ++i) {
                selectionGradientColors[i] = colors[i];
            }
            selectionGradientPercents = new int[percents.length];
            for (int i = 0; i < percents.length; ++i) {
                selectionGradientPercents[i] = percents[i];
            }
            selectionGradientVertical = vertical;
            setSelectionBackground(selectionGradientColors[selectionGradientColors.length - 1]);
            setSelectionHighlightGradientColor(highlightBeginColor);
        }
        // Refresh with the new settings
        if (selectedIndex > -1)
            redraw();
        getBridge().dirty(this);
    }

    /*
 * Set the color for the highlight start for selected tabs.
 * Update the cache of highlight gradient colors if required.
 */
    void setSelectionHighlightGradientColor(Color start) {
        if (inDispose)
            return;
        //TODO: need better caching strategy
        ((DartCTabFolderRenderer) renderer.getImpl()).setSelectionHighlightGradientColor(start);
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
        checkWidget();
        setSelectionHighlightGradientColor(null);
        if (image == selectionBgImage)
            return;
        if (image != null) {
            selectionGradientColors = null;
            selectionGradientPercents = null;
            //TODO: need better caching strategy
            ((DartCTabFolderRenderer) renderer.getImpl()).disposeSelectionHighlightGradientColors();
        }
        selectionBgImage = image;
        //TODO:  need better caching strategy
        ((DartCTabFolderRenderer) renderer.getImpl()).createAntialiasColors();
        if (selectedIndex > -1)
            redraw();
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
        checkWidget();
        if (selectionForeground == color)
            return;
        if (color == null)
            color = getDisplay().getSystemColor(SELECTION_FOREGROUND);
        selectionForeground = color;
        if (selectedIndex > -1)
            redraw();
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
        getBridge().dirty(this);
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
        checkWidget();
        if (this.simple != simple) {
            this.simple = simple;
            updateFolder(UPDATE_TAB_HEIGHT | REDRAW);
        }
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
        checkWidget();
        if (this.single != single) {
            this.single = single;
            if (!single) {
                for (int i = 0; i < items.length; i++) {
                    if (i != selectedIndex && ((DartCTabItem) items[i].getImpl()).closeImageState == SWT.NONE) {
                        ((DartCTabItem) items[i].getImpl()).closeImageState = SWT.BACKGROUND;
                    }
                }
            }
            updateFolder(REDRAW);
        }
        getBridge().dirty(this);
    }

    int getControlY(Point size, Rectangle[] rects, int borderBottom, int borderTop, int i) {
        int center = fixedTabHeight != SWT.DEFAULT ? 0 : (tabHeight - rects[i].height) / 2;
        return onBottom ? size.y - borderBottom - tabHeight + center : 1 + borderTop + center;
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
        checkWidget();
        if (height < -1) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        fixedTabHeight = height;
        updateFolder(UPDATE_TAB_HEIGHT);
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
        checkWidget();
        this.tabPosition = position;
        if (position != SWT.TOP && position != SWT.BOTTOM) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        if (onBottom != (position == SWT.BOTTOM)) {
            onBottom = position == SWT.BOTTOM;
            updateFolder(REDRAW);
        }
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
        setTopRight(control, SWT.RIGHT);
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
        checkWidget();
        if (alignment != SWT.RIGHT && alignment != SWT.FILL && alignment != (SWT.RIGHT | SWT.WRAP)) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        if (control != null && (control.isDisposed() || control.getParent() != this.getApi())) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        if (topRight == control && topRightAlignment == alignment)
            return;
        if (topRight != null && !topRight.isDisposed())
            removeTabControl(topRight, false);
        topRight = control;
        topRightAlignment = alignment;
        alignment &= ~SWT.RIGHT;
        if (control != null)
            addTabControl(control, SWT.TRAIL | alignment, -1, false);
        updateFolder(UPDATE_TAB_HEIGHT | REDRAW);
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
        checkWidget();
        if (showUnselectedClose == visible)
            return;
        // display close button when mouse hovers
        showUnselectedClose = visible;
        updateFolder(REDRAW);
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
        checkWidget();
        if (showUnselectedImage == visible)
            return;
        // display image on unselected items
        showUnselectedImage = visible;
        updateFolder(REDRAW);
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
        checkWidget();
        if (showSelectedImage == visible)
            return;
        // display image on selected items
        showSelectedImage = visible;
        updateFolder(REDRAW);
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
        if (((DartCTabItem) item.getImpl()).showing)
            return;
        updateFolder(REDRAW_TABS);
    }

    void showList(Rectangle rect) {
        if (items.length == 0 || !showChevron)
            return;
        if (showMenu == null || showMenu.isDisposed()) {
            showMenu = new Menu(getShell(), getStyle() & (SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT));
        } else {
            for (MenuItem item : showMenu.getItems()) {
                item.dispose();
            }
        }
        //$NON-NLS-1$
        final String id = "CTabFolder_showList_Index";
        for (CTabItem tab : items) {
            if (((DartCTabItem) tab.getImpl()).showing)
                continue;
            MenuItem item = new MenuItem(showMenu, SWT.NONE);
            // Bug 533124 In the case where you have multi line tab text, we force the drop-down menu to have single line entries to ensure consistent behavior across platforms.
            item.setText(tab.getText().replace("\n", " "));
            item.setImage(tab.getImage());
            item.setData(id, tab);
            item.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    MenuItem menuItem = (MenuItem) e.widget;
                    int index = indexOf((CTabItem) menuItem.getData(id));
                    DartCTabFolder.this.setSelection(index, true);
                }
            });
        }
        int x = rect.x;
        int y = rect.y + rect.height;
        Point location = getDisplay().map(this.getApi(), null, x, y);
        showMenu.setLocation(location.x, location.y);
        showMenu.setVisible(true);
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

    void _setToolTipText(int x, int y) {
        String oldTip = getToolTipText();
        String newTip = _getToolTip(x, y);
        if (newTip == null || !newTip.equals(oldTip)) {
            setToolTipText(newTip);
        }
    }

    boolean updateItems() {
        return updateItems(selectedIndex);
    }

    boolean updateItems(int showIndex) {
        setButtonBounds();
        return false;
    }

    boolean updateTabHeight(boolean force) {
        tabHeight = 25;
        return false;
    }

    void updateFolder(int flags) {
        updateFlags |= flags;
        if (updateRun != null)
            return;
        updateRun = () -> {
            updateRun = null;
            if (isDisposed())
                return;
            runUpdate();
        };
        getDisplay().asyncExec(updateRun);
    }

    void runUpdate() {
        if (updateFlags == 0)
            return;
        int flags = updateFlags;
        updateFlags = 0;
        Rectangle rectBefore = getClientArea();
        updateButtons();
        updateTabHeight(false);
        updateItems(selectedIndex);
        if ((flags & REDRAW) != 0) {
            redraw();
        } else if ((flags & REDRAW_TABS) != 0) {
            redrawTabs();
        }
        Rectangle rectAfter = getClientArea();
        if (!rectBefore.equals(rectAfter)) {
            notifyListeners(SWT.Resize, new Event());
            layout();
        }
    }

    void updateBkImages(boolean colorChanged) {
        if (controls != null && controls.length > 0) {
            if (bkImageBounds == null) {
                bkImageBounds = new Rectangle[controls.length];
            }
            if (bkImageBounds.length != controls.length) {
                bkImageBounds = new Rectangle[controls.length];
            }
            for (int i = 0; i < controls.length; i++) {
                Control control = controls[i];
                if (!control.isDisposed()) {
                    if (hovering) {
                        if (control instanceof Composite)
                            ((Composite) control).setBackgroundMode(SWT.INHERIT_NONE);
                        control.setBackgroundImage(null);
                        control.setBackground(getBackground());
                    } else {
                        if (control instanceof Composite)
                            ((Composite) control).setBackgroundMode(SWT.INHERIT_DEFAULT);
                        Rectangle bounds = control.getBounds();
                        int tabHeight = getTabHeight();
                        int height = this.getSize().y;
                        boolean wrapped = onBottom ? bounds.y + bounds.height < height - tabHeight : bounds.y > tabHeight;
                        if (wrapped || gradientColors == null) {
                            bkImageBounds[i] = null;
                            control.setBackgroundImage(null);
                            control.setBackground(getBackground());
                        } else {
                            bounds.width = 10;
                            if (!onBottom) {
                                bounds.y = -bounds.y;
                                bounds.height -= 2 * bounds.y - 1;
                            } else {
                                bounds.height += height - (bounds.y + bounds.height);
                                bounds.y = -1;
                            }
                            bounds.x = 0;
                            // do not redraw when only translated:
                            if (colorChanged || !bounds.equals(bkImageBounds[i])) {
                                bkImageBounds[i] = bounds;
                                if (controlBkImages[i] != null)
                                    controlBkImages[i].dispose();
                                controlBkImages[i] = new Image(control.getDisplay(), bounds);
                                GC gc = new GC(controlBkImages[i]);
                                renderer.draw(CTabFolderRenderer.PART_BACKGROUND, 0, bounds, gc);
                                gc.dispose();
                                control.setBackground(null);
                                control.setBackgroundImage(controlBkImages[i]);
                            }
                        }
                    }
                }
            }
        }
    }

    String _getToolTip(int x, int y) {
        CTabItem item = getItem(new Point(x, y));
        if (item == null)
            return null;
        if (!((DartCTabItem) item.getImpl()).showing)
            return null;
        if ((showClose || ((DartCTabItem) item.getImpl()).showClose) && ((DartCTabItem) item.getImpl()).closeRect.contains(x, y)) {
            //$NON-NLS-1$
            return SWT.getMessage("SWT_Close");
        }
        return item.getToolTipText();
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
    /*public*/
    void addTabControl(Control control, int flags) {
        checkWidget();
        addTabControl(control, flags, -1, true);
    }

    void addTabControl(Control control, int flags, int index, boolean update) {
        switch(flags) {
            case SWT.TRAIL:
            case SWT.TRAIL | SWT.WRAP:
            case SWT.TRAIL | SWT.FILL:
            case SWT.TRAIL | SWT.FILL | SWT.WRAP:
            case SWT.LEAD:
                break;
            default:
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
                break;
        }
        if (control != null && control.getParent() != this.getApi()) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        //check for duplicates
        for (Control ctrl : controls) {
            if (ctrl == control) {
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
            }
        }
        int length = controls.length;
        control.addListener(SWT.Resize, listener);
        //Grow all 4 arrays
        Control[] newControls = new Control[length + 1];
        System.arraycopy(controls, 0, newControls, 0, length);
        controls = newControls;
        int[] newAlignment = new int[length + 1];
        System.arraycopy(controlAlignments, 0, newAlignment, 0, length);
        controlAlignments = newAlignment;
        Rectangle[] newRect = new Rectangle[length + 1];
        System.arraycopy(controlRects, 0, newRect, 0, length);
        controlRects = newRect;
        Image[] newImage = new Image[length + 1];
        System.arraycopy(controlBkImages, 0, newImage, 0, length);
        controlBkImages = newImage;
        if (index == -1) {
            index = length;
            if (chevronTb != null && control != chevronTb)
                index--;
        }
        System.arraycopy(controls, index, controls, index + 1, length - index);
        System.arraycopy(controlAlignments, index, controlAlignments, index + 1, length - index);
        System.arraycopy(controlRects, index, controlRects, index + 1, length - index);
        System.arraycopy(controlBkImages, index, controlBkImages, index + 1, length - index);
        controls[index] = control;
        controlAlignments[index] = flags;
        controlRects[index] = new Rectangle(0, 0, 0, 0);
        if (update) {
            updateFolder(UPDATE_TAB_HEIGHT | REDRAW);
        }
    }

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
    /*public*/
    void removeTabControl(Control control) {
        checkWidget();
        removeTabControl(control, true);
    }

    void removeTabControl(Control control, boolean update) {
        if (control != null && control.getParent() != this.getApi()) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        int index = -1;
        for (int i = 0; i < controls.length; i++) {
            if (controls[i] == control) {
                index = i;
                break;
            }
        }
        if (index == -1)
            return;
        if (!control.isDisposed()) {
            control.removeListener(SWT.Resize, listener);
            control.setBackground(null);
            control.setBackgroundImage(null);
            if (control instanceof Composite)
                ((Composite) control).setBackgroundMode(SWT.INHERIT_NONE);
        }
        if (controlBkImages[index] != null && !controlBkImages[index].isDisposed())
            controlBkImages[index].dispose();
        if (controls.length == 1) {
            controls = new Control[0];
            controlAlignments = new int[0];
            controlRects = new Rectangle[0];
            controlBkImages = new Image[0];
        } else {
            Control[] newControls = new Control[controls.length - 1];
            System.arraycopy(controls, 0, newControls, 0, index);
            System.arraycopy(controls, index + 1, newControls, index, controls.length - index - 1);
            controls = newControls;
            int[] newAlignments = new int[controls.length];
            System.arraycopy(controlAlignments, 0, newAlignments, 0, index);
            System.arraycopy(controlAlignments, index + 1, newAlignments, index, controls.length - index);
            controlAlignments = newAlignments;
            Rectangle[] newRects = new Rectangle[controls.length];
            System.arraycopy(controlRects, 0, newRects, 0, index);
            System.arraycopy(controlRects, index + 1, newRects, index, controls.length - index);
            controlRects = newRects;
            Image[] newBkImages = new Image[controls.length];
            System.arraycopy(controlBkImages, 0, newBkImages, 0, index);
            System.arraycopy(controlBkImages, index + 1, newBkImages, index, controls.length - index);
            controlBkImages = newBkImages;
        }
        if (update) {
            updateFolder(UPDATE_TAB_HEIGHT | REDRAW);
        }
    }

    int getWrappedHeight(Point size) {
        boolean[][] positions = new boolean[1][];
        Rectangle[] rects = computeControlBounds(size, positions);
        int minY = Integer.MAX_VALUE, maxY = 0, wrapHeight = 0;
        for (int i = 0; i < rects.length; i++) {
            if (positions[0][i]) {
                minY = Math.min(minY, rects[i].y);
                maxY = Math.max(maxY, rects[i].y + rects[i].height);
                wrapHeight = maxY - minY;
            }
        }
        return wrapHeight;
    }

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
    /*public*/
    void setChevronVisible(boolean visible) {
        checkWidget();
        if (chevronVisible == visible)
            return;
        chevronVisible = visible;
        updateFolder(UPDATE_TAB_HEIGHT | REDRAW);
    }

    boolean shouldHighlight() {
        return this.highlight && highlightEnabled;
    }

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
        checkWidget();
        if (highlightEnabled == enabled) {
            return;
        }
        highlightEnabled = enabled;
        updateFolder(REDRAW);
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
        checkWidget();
        return highlightEnabled;
    }

    /**
     * Update the cached minimize button image when color is changed.
     */
    private void updateMinImage() {
        if (showMin && minMaxTb != null && minItem != null) {
            if (minImage != null)
                minImage.dispose();
            minImage = createButtonImage(getDisplay(), CTabFolderRenderer.PART_MIN_BUTTON);
            minItem.setImage(minImage);
        }
    }

    /**
     * Update the cached maximize button image when color is changed.
     */
    private void updateMaxImage() {
        if (showMax && minMaxTb != null && maxItem != null) {
            if (maxImage != null)
                maxImage.dispose();
            maxImage = createButtonImage(getDisplay(), CTabFolderRenderer.PART_MAX_BUTTON);
            maxItem.setImage(maxImage);
        }
    }

    int tabPosition;

    public boolean _onBottom() {
        return onBottom;
    }

    public boolean _single() {
        return single;
    }

    public boolean _simple() {
        return simple;
    }

    public int _fixedTabHeight() {
        return fixedTabHeight;
    }

    public int _tabHeight() {
        return tabHeight;
    }

    public int _minChars() {
        return minChars;
    }

    public boolean _borderVisible() {
        return borderVisible;
    }

    public CTabFolderRenderer _renderer() {
        return renderer;
    }

    public CTabItem[] _items() {
        return items;
    }

    public int _firstIndex() {
        return firstIndex;
    }

    public int _selectedIndex() {
        return selectedIndex;
    }

    public int[] _priority() {
        return priority;
    }

    public boolean _mru() {
        return mru;
    }

    public Listener _listener() {
        return listener;
    }

    public boolean _ignoreTraverse() {
        return ignoreTraverse;
    }

    public boolean _useDefaultRenderer() {
        return useDefaultRenderer;
    }

    public CTabFolder2Listener[] _folderListeners() {
        return folderListeners;
    }

    public CTabFolderListener[] _tabListeners() {
        return tabListeners;
    }

    public Image _selectionBgImage() {
        return selectionBgImage;
    }

    public Color[] _selectionGradientColors() {
        return selectionGradientColors;
    }

    public int[] _selectionGradientPercents() {
        return selectionGradientPercents;
    }

    public boolean _selectionGradientVertical() {
        return selectionGradientVertical;
    }

    public Color _selectionForeground() {
        return selectionForeground;
    }

    public Color _selectionBackground() {
        return selectionBackground;
    }

    public int _selectionHighlightBarThickness() {
        return selectionHighlightBarThickness;
    }

    public Color[] _gradientColors() {
        return gradientColors;
    }

    public int[] _gradientPercents() {
        return gradientPercents;
    }

    public boolean _gradientVertical() {
        return gradientVertical;
    }

    public boolean _showUnselectedImage() {
        return showUnselectedImage;
    }

    public boolean _showSelectedImage() {
        return showSelectedImage;
    }

    public boolean _showClose() {
        return showClose;
    }

    public boolean _showUnselectedClose() {
        return showUnselectedClose;
    }

    public boolean _showMin() {
        return showMin;
    }

    public boolean _minimized() {
        return minimized;
    }

    public boolean _showMax() {
        return showMax;
    }

    public boolean _maximized() {
        return maximized;
    }

    public ToolBar _minMaxTb() {
        return minMaxTb;
    }

    public ToolItem _maxItem() {
        return maxItem;
    }

    public ToolItem _minItem() {
        return minItem;
    }

    public Image _maxImage() {
        return maxImage;
    }

    public Image _minImage() {
        return minImage;
    }

    public boolean _hoverTb() {
        return hoverTb;
    }

    public Rectangle _hoverRect() {
        return hoverRect;
    }

    public boolean _hovering() {
        return hovering;
    }

    public boolean _hoverTimerRunning() {
        return hoverTimerRunning;
    }

    public boolean _highlight() {
        return highlight;
    }

    public boolean _highlightEnabled() {
        return highlightEnabled;
    }

    public boolean _showChevron() {
        return showChevron;
    }

    public Menu _showMenu() {
        return showMenu;
    }

    public ToolBar _chevronTb() {
        return chevronTb;
    }

    public ToolItem _chevronItem() {
        return chevronItem;
    }

    public int _chevronCount() {
        return chevronCount;
    }

    public boolean _chevronVisible() {
        return chevronVisible;
    }

    public Image _chevronImage() {
        return chevronImage;
    }

    public Control _topRight() {
        return topRight;
    }

    public int _topRightAlignment() {
        return topRightAlignment;
    }

    public boolean _ignoreResize() {
        return ignoreResize;
    }

    public Control[] _controls() {
        return controls;
    }

    public int[] _controlAlignments() {
        return controlAlignments;
    }

    public Rectangle[] _controlRects() {
        return controlRects;
    }

    public Rectangle[] _bkImageBounds() {
        return bkImageBounds;
    }

    public Image[] _controlBkImages() {
        return controlBkImages;
    }

    public int _updateFlags() {
        return updateFlags;
    }

    public Runnable _updateRun() {
        return updateRun;
    }

    public boolean _inDispose() {
        return inDispose;
    }

    public Point _oldSize() {
        return oldSize;
    }

    public Font _oldFont() {
        return oldFont;
    }

    public int _tabPosition() {
        return tabPosition;
    }

    protected void hookEvents() {
        super.hookEvents();
        FlutterBridge.on(this, "Selection", "Selection", e -> {
            getDisplay().asyncExec(() -> {
                setSelection(e.index, true);
            });
        });
        FlutterBridge.on(this, "Selection", "DefaultSelection", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.DefaultSelection, e);
            });
        });
    }

    public CTabFolder getApi() {
        if (api == null)
            api = CTabFolder.createApi(this);
        return (CTabFolder) api;
    }

    public VCTabFolder getValue() {
        if (value == null)
            value = new VCTabFolder(this);
        return (VCTabFolder) value;
    }
}
