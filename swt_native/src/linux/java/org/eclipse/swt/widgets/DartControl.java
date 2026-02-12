/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2025 IBM Corporation and others.
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
 *      Stefan Xenos (Google) - bug 468854 - Add a requestLayout method to Control
 * *****************************************************************************
 */
package org.eclipse.swt.widgets;

import java.lang.reflect.*;
import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.cairo.*;
import dev.equo.swt.*;

/**
 * Control is the abstract superclass of all windowed user interface classes.
 * <dl>
 * <dt><b>Styles:</b>
 * <dd>BORDER</dd>
 * <dd>LEFT_TO_RIGHT, RIGHT_TO_LEFT, FLIP_TEXT_DIRECTION</dd>
 * <dt><b>Events:</b>
 * <dd>DragDetect, FocusIn, FocusOut, Help, KeyDown, KeyUp, MenuDetect, MouseDoubleClick, MouseDown, MouseEnter,
 *     MouseExit, MouseHover, MouseUp, MouseMove, MouseWheel, MouseHorizontalWheel, MouseVerticalWheel, Move,
 *     Paint, Resize, Traverse</dd>
 * </dl>
 * <p>
 * Only one of LEFT_TO_RIGHT or RIGHT_TO_LEFT may be specified.
 * </p><p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em>
 * within the SWT implementation.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#control">Control snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class DartControl extends DartWidget implements Drawable, IControl {

    // allows to disable context menu entry for "insert emoji"
    static final boolean DISABLE_EMOJI = Boolean.getBoolean("SWT_GTK_INPUT_HINT_NO_EMOJI");

    long fixedHandle;

    long firstFixedHandle = 0;

    long keyController;

    long redrawWindow, enableWindow, provider;

    int drawCount, backgroundAlpha = 255;

    long dragGesture, zoomGesture, rotateGesture, panGesture;

    Composite parent;

    Cursor cursor;

    Menu menu;

    Image backgroundImage;

    Font font;

    Region region;

    long eventRegion;

    /**
     * The handle to the Region, which is neccessary in the case
     * that <code>region</code> is disposed before this Control.
     */
    long regionHandle;

    String toolTipText;

    Object layoutData;

    Accessible accessible;

    Control labelRelation;

    String cssBackground, cssForeground = " ";

    boolean drawRegion;

    /**
     * Cache the NO_BACKGROUND flag as it gets removed automatically in
     * Composite. Only relevant for GTK3.10+ as we need it for Cairo setRegion()
     * functionality. See bug 475784.
     */
    boolean cachedNoBackground;

    /**
     * Point for storing the (x, y) coordinate of the last input (click/scroll/etc.).
     * This is useful for checking input event coordinates in methods that act on input,
     * but do not receive coordinates (like gtk_clicked, for example). See bug 529431.
     */
    Point lastInput = new Point(0, 0);

    LinkedList<Event> dragDetectionQueue;

    /**
     * Bug 541635, 515396: GTK Wayland only flag to keep track whether mouse
     * is currently pressed or released for DND.
     */
    static boolean mouseDown;

    /**
     * Flag to check the scale factor upon the first drawing of this Control.
     * This is done by checking the scale factor of the Cairo surface in gtk_draw().
     *
     * Doing so provides an accurate scale factor, and will determine if this Control
     * needs to be scaled manually by SWT. See bug 507020.
     */
    boolean checkScaleFactor = true;

    /**
     * True if GTK has autoscaled this Control, meaning SWT does not need to do any
     * manual scaling. See bug 507020.
     */
    boolean autoScale = true;

    DartControl(Control api) {
        super(api);
    }

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
     * @see SWT#BORDER
     * @see SWT#LEFT_TO_RIGHT
     * @see SWT#RIGHT_TO_LEFT
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartControl(Composite parent, int style, Control api) {
        super(parent, style, api);
        this.parent = parent;
        createWidget(0);
        notifyCreationTracker();
        ControlUtils.addToParentChildren(this);
    }

    Font defaultFont() {
        return display.getSystemFont();
    }

    @Override
    void deregister() {
        if (bridge != null)
            bridge.destroy(this);
    }

    void drawBackground(Control control, long gdkResource, long cr, int x, int y, int width, int height) {
        long cairo = 0;
        long region = 0;
        /*
	 * It's possible that a client is using an SWT.NO_BACKGROUND Composite with custom painting
	 * and a region to provide "overlay" functionality. In this case we don't want to paint
	 * any background color, as it will likely break desired behavior. The fix is to paint
	 * this Control as transparent. See bug 475784.
	 */
        boolean noBackgroundRegion = drawRegion && hooks(SWT.Paint) && cachedNoBackground;
        if (cairo == 0)
            error(SWT.ERROR_NO_HANDLES);
        if (region != 0) {
        }
        if (control.getImpl()._backgroundImage() != null) {
            Point pt = ((SwtDisplay) display.getImpl()).mapInPixels(this.getApi(), control, 0, 0);
            x += pt.x;
            y += pt.y;
            if ((getApi().style & SWT.MIRRORED) != 0) {
            }
        } else {
            if (noBackgroundRegion) {
            } else {
            }
        }
    }

    boolean drawGripper(GC gc, int x, int y, int width, int height, boolean vertical) {
        if ((getApi().style & SWT.MIRRORED) != 0)
            x = getClientWidth() - width - x;
        return true;
    }

    void drawWidget(GC gc) {
    }

    void enableWidget(boolean enabled) {
    }

    long enterExitHandle() {
        return eventHandle();
    }

    long eventHandle() {
        return getApi().handle;
    }

    long eventWindow() {
        return 0;
    }

    long eventSurface() {
        return 0;
    }

    void fixFocus(Control focusControl) {
        Shell shell = getShell();
        Control control = this.getApi();
        while (control != shell && (control = control.getImpl()._parent()) != null) {
            if (control.setFocus())
                return;
        }
        ((SwtDecorations) shell.getImpl()).setSavedFocus(focusControl);
        // widget could be disposed at this point
        if (isDisposed())
            return;
    }

    public void fixStyle() {
        if (fixedHandle != 0)
            fixStyle(fixedHandle);
    }

    public void fixStyle(long handle) {
        /*
	* Feature in GTK.  Some GTK themes apply a different background to
	* the contents of a GtkNotebook.  However, in an SWT TabFolder, the
	* children are not parented below the GtkNotebook widget, and usually
	* have their own GtkFixed.  The fix is to look up the correct style
	* for a child of a GtkNotebook and apply its background to any GtkFixed
	* widgets that are direct children of an SWT TabFolder.
	*
	* Note that this has to be when the theme settings changes and that it
	* should not override the application background.
	*/
        if ((getApi().state & BACKGROUND) != 0)
            return;
        if ((getApi().state & THEME_BACKGROUND) == 0)
            return;
    }

    long focusHandle() {
        return getApi().handle;
    }

    long fontHandle() {
        return getApi().handle;
    }

    long gestureHandle() {
        return getApi().handle;
    }

    /**
     * Returns the orientation of the receiver, which will be one of the
     * constants <code>SWT.LEFT_TO_RIGHT</code> or <code>SWT.RIGHT_TO_LEFT</code>.
     *
     * @return the orientation style
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.7
     */
    public int getOrientation() {
        checkWidget();
        return getApi().style & (SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT);
    }

    /**
     * Returns the text direction of the receiver, which will be one of the
     * constants <code>SWT.LEFT_TO_RIGHT</code> or <code>SWT.RIGHT_TO_LEFT</code>.
     *
     * @return the text direction style
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.102
     */
    public int getTextDirection() {
        checkWidget();
        if (this.textDirection == AUTO_TEXT_DIRECTION) {
            int resolved = resolveTextDirection();
            if (resolved == SWT.NONE) {
                return getOrientation();
            }
            return resolved;
        }
        return this.textDirection != 0 ? this.textDirection : getOrientation();
    }

    boolean hasFocus() {
        return getBridge().hasFocus(this);
    }

    @Override
    void hookEvents() {
        super.hookEvents();
        long focusHandle = focusHandle();
        hookKeyboardAndFocusSignals(focusHandle);
        hookMouseSignals(eventHandle());
        hookWidgetSignals(focusHandle);
        hookPaintSignals();
        connectIMSignals();
        /*Connect gesture signals */
        setZoomGesture();
        setDragGesture();
        setRotateGesture();
    }

    private void hookKeyboardAndFocusSignals(long focusHandle) {
    }

    private void hookMouseSignals(long eventHandle) {
    }

    private void hookWidgetSignals(long focusHandle) {
    }

    private void hookPaintSignals() {
    }

    private void connectIMSignals() {
        long imHandle = imHandle();
        if (imHandle != 0) {
        }
    }

    boolean hooksPaint() {
        return hooks(SWT.Paint) || filters(SWT.Paint);
    }

    @Override
    long hoverProc(long widget) {
        int[] x = new int[1], y = new int[1], mask = new int[1];
        {
            ((SwtDisplay) display.getImpl()).getWindowPointerPosition(0, x, y, mask);
        }
        if (containedInRegion(x[0], y[0]))
            return 0;
        sendMouseEvent(SWT.MouseHover, 0, 0, x[0], y[0], false, mask[0]);
        /* Always return zero in order to cancel the hover timer */
        return 0;
    }

    @Override
    public long topHandle() {
        if (fixedHandle != 0)
            return fixedHandle;
        return super.topHandle();
    }

    long paintHandle() {
        long topHandle = topHandle();
        long paintHandle = getApi().handle;
        while (paintHandle != topHandle) {
        }
        return paintHandle;
    }

    @Override
    long paintWindow() {
        return 0;
    }

    @Override
    long paintSurface() {
        return 0;
    }

    /**
     * Prints the receiver and all children.
     *
     * @param gc the gc where the drawing occurs
     * @return <code>true</code> if the operation was successful and <code>false</code> otherwise
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the gc is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the gc has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    public boolean print(GC gc) {
        checkWidget();
        if (gc == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (gc.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        return true;
    }

    void printWidget(GC gc, long drawable, int depth, int x, int y) {
        boolean obscured = (getApi().state & OBSCURED) != 0;
        getApi().state &= ~OBSCURED;
        if (obscured)
            getApi().state |= OBSCURED;
    }

    void printWindow(boolean first, Control control, GC gc, long drawable, int depth, long window, int x, int y) {
        /*
	 * TODO: this needs to be re-implemented for GTK3 as it uses GdkDrawable which is gone.
	 * See: https://developer.gnome.org/gtk3/stable/ch26s02.html#id-1.6.3.4.7
	 */
        return;
    }

    /**
     * Returns the preferred size (in points) of the receiver.
     * <p>
     * The <em>preferred size</em> of a control is the size that it would
     * best be displayed at. The width hint and height hint arguments
     * allow the caller to ask a control questions such as "Given a particular
     * width, how high does the control need to be to show all of the contents?"
     * To indicate that the caller does not wish to constrain a particular
     * dimension, the constant <code>SWT.DEFAULT</code> is passed for the hint.
     * </p>
     *
     * @param wHint the width hint (can be <code>SWT.DEFAULT</code>)
     * @param hHint the height hint (can be <code>SWT.DEFAULT</code>)
     * @return the preferred size of the control
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Layout
     * @see #getBorderWidth
     * @see #getBounds
     * @see #getSize
     * @see #pack(boolean)
     * @see "computeTrim, getClientArea for controls that implement them"
     */
    public Point computeSize(int wHint, int hHint) {
        return computeSize(wHint, hHint, true);
    }

    Point computeSizeInPixels(int wHint, int hHint) {
        return computeSizeInPixels(wHint, hHint, true);
    }

    Widget computeTabGroup() {
        if (isTabGroup())
            return this.getApi();
        if (parent instanceof Decorations) {
            return parent;
        }
        return ((DartControl) parent.getImpl()).computeTabGroup();
    }

    public Widget[] computeTabList() {
        if (isTabGroup()) {
            if (getVisible() && getEnabled()) {
                return new Widget[] { this.getApi() };
            }
        }
        return new Widget[0];
    }

    public Control computeTabRoot() {
        Control[] tabList = parent.getImpl()._getTabList();
        if (tabList != null) {
            int index = 0;
            while (index < tabList.length) {
                if (tabList[index] == this.getApi())
                    break;
                index++;
            }
            if (index == tabList.length) {
                if (isTabGroup())
                    return this.getApi();
            }
        }
        return parent.getImpl().computeTabRoot();
    }

    void checkBuffered() {
        getApi().style |= SWT.DOUBLE_BUFFERED;
    }

    void checkBackground() {
    }

    void checkBorder() {
        if (getBorderWidthInPixels() == 0)
            getApi().style &= ~SWT.BORDER;
    }

    void checkMirrored() {
        if ((getApi().style & SWT.RIGHT_TO_LEFT) != 0)
            getApi().style |= SWT.MIRRORED;
    }

    /**
     * Convenience method for checking whether an (x, y) coordinate is in the set
     * region. Only relevant for GTK3.10+.
     *
     * @param x an x coordinate
     * @param y a y coordinate
     * @return true if the coordinate (x, y) is in the region, false otherwise
     */
    boolean containedInRegion(int x, int y) {
        if (drawRegion && eventRegion != 0) {
        }
        return false;
    }

    @Override
    void createWidget(int index) {
        getApi().state |= DRAG_DETECT;
        checkOrientation(parent);
        super.createWidget(index);
        checkBackground();
        if ((getApi().state & PARENT_BACKGROUND) != 0)
            setParentBackground();
        checkBuffered();
        showWidget();
        setInitialBounds();
        setZOrder(null, false, false);
        checkMirrored();
        checkBorder();
    }

    /**
     * Returns the preferred size (in points) of the receiver.
     * <p>
     * The <em>preferred size</em> of a control is the size that it would
     * best be displayed at. The width hint and height hint arguments
     * allow the caller to ask a control questions such as "Given a particular
     * width, how high does the control need to be to show all of the contents?"
     * To indicate that the caller does not wish to constrain a particular
     * dimension, the constant <code>SWT.DEFAULT</code> is passed for the hint.
     * </p><p>
     * If the changed flag is <code>true</code>, it indicates that the receiver's
     * <em>contents</em> have changed, therefore any caches that a layout manager
     * containing the control may have been keeping need to be flushed. When the
     * control is resized, the changed flag will be <code>false</code>, so layout
     * manager caches can be retained.
     * </p>
     *
     * @param wHint the width hint (can be <code>SWT.DEFAULT</code>)
     * @param hHint the height hint (can be <code>SWT.DEFAULT</code>)
     * @param changed <code>true</code> if the control's contents have changed, and <code>false</code> otherwise
     * @return the preferred size of the control.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Layout
     * @see #getBorderWidth
     * @see #getBounds
     * @see #getSize
     * @see #pack(boolean)
     * @see "computeTrim, getClientArea for controls that implement them"
     */
    public Point computeSize(int wHint, int hHint, boolean changed) {
        return computeSizeInPixels(wHint, hHint, changed);
    }

    Point computeSizeInPixels(int wHint, int hHint, boolean changed) {
        return Sizes.computeSize(this, wHint, hHint, changed);
    }

    Point computeNativeSize(long h, int wHint, int hHint, boolean changed) {
        int width = wHint, height = hHint;
        if (wHint == SWT.DEFAULT && hHint == SWT.DEFAULT) {
        } else if (wHint == SWT.DEFAULT || hHint == SWT.DEFAULT) {
            int[] natural_size = new int[1];
            if (wHint == SWT.DEFAULT) {
                width = natural_size[0];
            } else {
                height = natural_size[0];
            }
        }
        return new Point(width, height);
    }

    void forceResize() {
    }

    /**
     * Returns the accessible object for the receiver.
     * <p>
     * If this is the first time this object is requested,
     * then the object is created and returned. The object
     * returned by getAccessible() does not need to be disposed.
     * </p>
     *
     * @return the accessible object
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Accessible#addAccessibleListener
     * @see Accessible#addAccessibleControlListener
     *
     * @since 2.0
     */
    public Accessible getAccessible() {
        checkWidget();
        return _getAccessible();
    }

    Accessible _getAccessible() {
        if (accessible == null) {
            Widget current = this.getApi();
            while (current != null) {
                if (current instanceof Control ctrl) {
                    if (ctrl.handle != 0) {
                        this.getApi().handle = ctrl.handle;
                        break;
                    }
                    current = ctrl.getParent();
                }
            }
            if (accessible == null) {
                accessible = SwtAccessible.internal_new_Accessible(this.getApi());
            }
        }
        return accessible;
    }

    /**
     * Returns a rectangle describing the receiver's size and location in points
     * relative to its parent (or its display if its parent is null),
     * unless the receiver is a shell. In this case, the location is
     * relative to the display.
     *
     * @return the receiver's bounding rectangle
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Rectangle getBounds() {
        checkWidget();
        return getBoundsInPixels();
    }

    Rectangle getBoundsInPixels() {
        checkWidget();
        return this.bounds;
    }

    /**
     * Sets the receiver's size and location in points to the rectangular
     * area specified by the argument. The <code>x</code> and
     * <code>y</code> fields of the rectangle are relative to
     * the receiver's parent (or its display if its parent is null).
     * <p>
     * Note: Attempting to set the width or height of the
     * receiver to a negative number will cause that
     * value to be set to zero instead.
     * </p>
     * <p>
     * Note: On GTK, attempting to set the width or height of the
     * receiver to a number higher or equal 2^14 will cause them to be
     * set to (2^14)-1 instead.
     * </p>
     *
     * @param rect the new bounds for the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setBounds(Rectangle rect) {
        checkWidget();
        if (rect == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        setBounds(rect.x, rect.y, Math.max(0, rect.width), Math.max(0, rect.height), true, true);
    }

    void setBoundsInPixels(Rectangle rect) {
        checkWidget();
        if (rect == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        setBounds(rect.x, rect.y, Math.max(0, rect.width), Math.max(0, rect.height), true, true);
    }

    /**
     * Sets the receiver's size and location in points to the rectangular
     * area specified by the arguments. The <code>x</code> and
     * <code>y</code> arguments are relative to the receiver's
     * parent (or its display if its parent is null), unless
     * the receiver is a shell. In this case, the <code>x</code>
     * and <code>y</code> arguments are relative to the display.
     * <p>
     * Note: Attempting to set the width or height of the
     * receiver to a negative number will cause that
     * value to be set to zero instead.
     * </p>
     * <p>
     * Note: On GTK, attempting to set the width or height of the
     * receiver to a number higher or equal 2^14 will cause them to be
     * set to (2^14)-1 instead.
     * </p>
     *
     * @param x the new x coordinate for the receiver
     * @param y the new y coordinate for the receiver
     * @param width the new width for the receiver
     * @param height the new height for the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setBounds(int x, int y, int width, int height) {
        checkWidget();
        Rectangle rect = new Rectangle(x, y, width, height);
        setBounds(rect.x, rect.y, Math.max(0, rect.width), Math.max(0, rect.height), true, true);
    }

    void setBoundsInPixels(int x, int y, int width, int height) {
        checkWidget();
        setBounds(x, y, Math.max(0, width), Math.max(0, height), true, true);
    }

    public void markLayout(boolean changed, boolean all) {
        /* Do nothing */
    }

    void moveHandle(int x, int y) {
    }

    void resizeHandle(int width, int height) {
        long topHandle = topHandle();
        if (topHandle != getApi().handle) {
        }
    }

    int setBounds(int x, int y, int width, int height, boolean move, boolean resize) {
        dirty();
        int finalX = move ? x : this.bounds.x;
        int finalY = move ? y : this.bounds.y;
        int finalWidth = resize ? width : this.bounds.width;
        int finalHeight = resize ? height : this.bounds.height;
        Rectangle newValue = new Rectangle(finalX, finalY, finalWidth, finalHeight);
        // bug in GTK3 the crashes new shell only. See bug 472743
        width = Math.min(width, (2 << 14) - 1);
        height = Math.min(height, (2 << 14) - 1);
        boolean sendMove = move;
        if ((parent.style & SWT.MIRRORED) != 0) {
            int clientWidth = ((DartControl) parent.getImpl()).getClientWidth();
            if (move) {
            } else {
                move = true;
            }
        }
        boolean sameOrigin = true, sameExtent = true;
        if (move) {
            if (!sameOrigin) {
                moveHandle(x, y);
            }
        }
        int clientWidth = 0;
        if (resize) {
            if (!sameExtent && (getApi().style & SWT.MIRRORED) != 0)
                clientWidth = getClientWidth();
            if (!sameExtent && !(width == 0 && height == 0)) {
                int newWidth = Math.max(1, width);
                int newHeight = Math.max(1, height);
                resizeHandle(newWidth, newHeight);
            }
        }
        if (!sameOrigin || !sameExtent) {
            if (move) {
            }
            if (resize) {
            }
        }
        /*
	* Bug in GTK.  Widgets cannot be sized smaller than 1x1.
	* The fix is to hide zero-sized widgets and show them again
	* when they are resized larger.
	*/
        if (!sameExtent) {
            getApi().state = (width == 0) ? getApi().state | ZERO_WIDTH : getApi().state & ~ZERO_WIDTH;
            getApi().state = (height == 0) ? getApi().state | ZERO_HEIGHT : getApi().state & ~ZERO_HEIGHT;
            if ((getApi().state & (ZERO_WIDTH | ZERO_HEIGHT)) != 0) {
            } else {
                if ((getApi().state & HIDDEN) == 0) {
                }
            }
            if ((getApi().style & SWT.MIRRORED) != 0)
                moveChildren(clientWidth);
        }
        int result = 0;
        if (move && !sameOrigin) {
            Control control = findBackgroundControl();
            if (control != null && control.getImpl()._backgroundImage() != null) {
                if (isVisible())
                    redrawWidget(0, 0, 0, 0, true, true, true);
            }
            if (sendMove)
                sendEvent(SWT.Move);
            result |= MOVED;
        }
        this.bounds = newValue;
        if (resize && !sameExtent) {
            sendEvent(SWT.Resize);
            result |= RESIZED;
        }
        getBridge().setBounds(this, bounds);
        return result;
    }

    /**
     * Returns a point describing the receiver's location relative to its parent in
     * points (or its display if its parent is null), unless the receiver is a
     * shell. In this case, the point is usually relative to the display.
     * <p>
     * <b>Warning:</b> When executing this operation on a shell, it may not yield a
     * value with the expected meaning on some platforms. For example, executing
     * this operation on a shell when the environment uses the Wayland protocol, the
     * result is <b>not</b> a coordinate relative to the display. It will not change
     * when moving the shell.
     *
     * @return the receiver's location
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Point getLocation() {
        checkWidget();
        return getLocationInPixels();
    }

    Point getLocationInPixels() {
        checkWidget();
        if ((parent.style & SWT.MIRRORED) != 0) {
        }
        return new Point(bounds.x, bounds.y);
    }

    /**
     * Sets the receiver's location to the point specified by the argument which
     * is relative to the receiver's parent (or its display if its parent is null),
     * unless the receiver is a shell. In this case, the point is relative to the
     * display.
     * <p>
     * <b>Warning:</b> When executing this operation on a shell, it may not have the
     * intended effect on some platforms. For example, executing this operation on a
     * shell when the environment uses the Wayland protocol, nothing will happen.
     *
     * @param location the new location for the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setLocation(Point location) {
        dirty();
        checkWidget();
        if (location == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        setBounds(location.x, location.y, 0, 0, true, false);
    }

    void setLocationInPixels(Point location) {
        checkWidget();
        if (location == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        setBounds(location.x, location.y, 0, 0, true, false);
    }

    /**
     * Sets the receiver's location to the point specified by the arguments which
     * are relative to the receiver's parent (or its display if its parent is null),
     * unless the receiver is a shell. In this case, the point is relative to the
     * display.
     * <p>
     * <b>Warning:</b> When executing this operation on a shell, it may not have the
     * intended effect on some platforms. For example, executing this operation on a
     * shell when the environment uses the Wayland protocol, nothing will happen.
     *
     * @param x the new x coordinate for the receiver
     * @param y the new y coordinate for the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setLocation(int x, int y) {
        dirty();
        checkWidget();
        Point loc = new Point(x, y);
        setBounds(loc.x, loc.y, 0, 0, true, false);
    }

    void setLocationInPixels(int x, int y) {
        checkWidget();
        setBounds(x, y, 0, 0, true, false);
    }

    /**
     * Returns a point describing the receiver's size in points. The
     * x coordinate of the result is the width of the receiver.
     * The y coordinate of the result is the height of the
     * receiver.
     *
     * @return the receiver's size
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Point getSize() {
        checkWidget();
        return getSizeInPixels();
    }

    public Point getSizeInPixels() {
        checkWidget();
        return new Point(bounds.width, bounds.height);
    }

    /**
     * Sets the receiver's size to the point specified by the argument.
     * <p>
     * Note: Attempting to set the width or height of the
     * receiver to a negative number will cause them to be
     * set to zero instead.
     * </p>
     * <p>
     * Note: On GTK, attempting to set the width or height of the
     * receiver to a number higher or equal 2^14 will cause them to be
     * set to (2^14)-1 instead.
     * </p>
     *
     * @param size the new size in points for the receiver
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setSize(Point size) {
        dirty();
        checkWidget();
        if (size == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        setBounds(0, 0, Math.max(0, size.x), Math.max(0, size.y), false, true);
    }

    void setSizeInPixels(Point size) {
        checkWidget();
        if (size == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        setBounds(0, 0, Math.max(0, size.x), Math.max(0, size.y), false, true);
    }

    /**
     * Sets the shape of the control to the region specified
     * by the argument.  When the argument is null, the
     * default shape of the control is restored.
     *
     * @param region the region that defines the shape of the control (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the region has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    public void setRegion(Region region) {
        checkWidget();
        if (!java.util.Objects.equals(this.region, region)) {
            dirty();
        }
        if (region != null && region.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        this.region = region;
    }

    void setRelations() {
        long handle = 0;
        if (handle != 0) {
            Widget widget = ((SwtDisplay) display.getImpl()).getWidget(handle);
            if (widget != null && widget != this.getApi()) {
                if (widget instanceof Control) {
                    Control sibling = (Control) widget;
                    sibling.getImpl().addRelation(this.getApi());
                }
            }
        }
    }

    /**
     * Sets the receiver's size to the point specified by the arguments.
     * <p>
     * Note: Attempting to set the width or height of the
     * receiver to a negative number will cause that
     * value to be set to zero instead.
     * </p>
     * <p>
     * Note: On GTK, attempting to set the width or height of the
     * receiver to a number higher or equal 2^14 will cause them to be
     * set to (2^14)-1 instead.
     * </p>
     *
     * @param width the new width in points for the receiver
     * @param height the new height in points for the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setSize(int width, int height) {
        dirty();
        checkWidget();
        Point size = new Point(width, height);
        setBounds(0, 0, Math.max(0, size.x), Math.max(0, size.y), false, true);
    }

    void setSizeInPixels(int width, int height) {
        checkWidget();
        setBounds(0, 0, Math.max(0, width), Math.max(0, height), false, true);
    }

    @Override
    public boolean isActive() {
        return ((SwtShell) getShell().getImpl()).getModalShell() == null && ((SwtDisplay) display.getImpl()).getModalDialog() == null;
    }

    @Override
    public boolean isAutoScalable() {
        return autoScale;
    }

    /*
 * Answers a boolean indicating whether a Label that precedes the receiver in
 * a layout should be read by screen readers as the recevier's label.
 */
    public boolean isDescribedByLabel() {
        return true;
    }

    boolean isFocusHandle(long widget) {
        return widget == focusHandle();
    }

    /**
     * Moves the receiver above the specified control in the
     * drawing order. If the argument is null, then the receiver
     * is moved to the top of the drawing order. The control at
     * the top of the drawing order will not be covered by other
     * controls even if they occupy intersecting areas.
     *
     * @param control the sibling control (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the control has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Control#moveBelow
     * @see Composite#getChildren
     */
    public void moveAbove(Control control) {
        checkWidget();
        if (control != null) {
            if (control.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
            if (parent != control.getImpl()._parent())
                return;
            if (this.getApi() == control)
                return;
        }
        setZOrder(control, true, true);
        ControlUtils.updateChildrenOrderOnMove(this.getApi(), control, true);
    }

    /**
     * Moves the receiver below the specified control in the
     * drawing order. If the argument is null, then the receiver
     * is moved to the bottom of the drawing order. The control at
     * the bottom of the drawing order will be covered by all other
     * controls which occupy intersecting areas.
     *
     * @param control the sibling control (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the control has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Control#moveAbove
     * @see Composite#getChildren
     */
    public void moveBelow(Control control) {
        checkWidget();
        if (control != null) {
            if (control.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
            if (parent != control.getImpl()._parent())
                return;
            if (this.getApi() == control)
                return;
        }
        setZOrder(control, false, true);
        ControlUtils.updateChildrenOrderOnMove(this.getApi(), control, false);
    }

    void moveChildren(int oldWidth) {
    }

    /**
     * Causes the receiver to be resized to its preferred size.
     * For a composite, this involves computing the preferred size
     * from its layout, if there is one.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #computeSize(int, int, boolean)
     */
    public void pack() {
        pack(true);
    }

    /**
     * Causes the receiver to be resized to its preferred size.
     * For a composite, this involves computing the preferred size
     * from its layout, if there is one.
     * <p>
     * If the changed flag is <code>true</code>, it indicates that the receiver's
     * <em>contents</em> have changed, therefore any caches that a layout manager
     * containing the control may have been keeping need to be flushed. When the
     * control is resized, the changed flag will be <code>false</code>, so layout
     * manager caches can be retained.
     * </p>
     *
     * @param changed whether or not the receiver's contents have changed
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #computeSize(int, int, boolean)
     */
    public void pack(boolean changed) {
        setSize(computeSize(SWT.DEFAULT, SWT.DEFAULT, changed));
    }

    /**
     * Sets the layout data associated with the receiver to the argument.
     *
     * @param layoutData the new layout data for the receiver.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setLayoutData(Object layoutData) {
        checkWidget();
        this.layoutData = layoutData;
    }

    /**
     * Returns a point which is the result of converting the
     * argument, which is specified in display relative coordinates,
     * to coordinates relative to the receiver.
     * <p>
     * NOTE: To properly map a rectangle or a corner of a rectangle on a right-to-left platform, use
     * {@link Display#map(Control, Control, Rectangle)}.
     * </p>
     *
     * @param x the x coordinate in points to be translated
     * @param y the y coordinate in points to be translated
     * @return the translated coordinates
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 2.1
     */
    public Point toControl(int x, int y) {
        checkWidget();
        return ControlHelper.toControl(this, x, y);
    }

    /**
     * Returns a point which is the result of converting the
     * argument, which is specified in display relative coordinates,
     * to coordinates relative to the receiver.
     * <p>
     * NOTE: To properly map a rectangle or a corner of a rectangle on a right-to-left platform, use
     * {@link Display#map(Control, Control, Rectangle)}.
     * </p>
     *
     * @param point the point to be translated (must not be null)
     * @return the translated coordinates
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Point toControl(Point point) {
        checkWidget();
        if (point == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        return toControl(point.x, point.y);
    }

    /**
     * Returns a point which is the result of converting the
     * argument, which is specified in coordinates relative to
     * the receiver, to display relative coordinates.
     * <p>
     * NOTE: To properly map a rectangle or a corner of a rectangle on a right-to-left platform, use
     * {@link Display#map(Control, Control, Rectangle)}.
     * </p>
     *
     * @param x the x coordinate to be translated
     * @param y the y coordinate to be translated
     * @return the translated coordinates
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 2.1
     */
    public Point toDisplay(int x, int y) {
        checkWidget();
        return ControlHelper.toDisplay(this, x, y);
    }

    Point toDisplayInPixels(int x, int y) {
        checkWidget();
        int[] origin_x = new int[1], origin_y = new int[1];
        if ((getApi().style & SWT.MIRRORED) != 0)
            x = getClientWidth() - x;
        x += origin_x[0];
        y += origin_y[0];
        return new Point(x, y);
    }

    /**
     * Returns a point which is the result of converting the
     * argument, which is specified in coordinates relative to
     * the receiver, to display relative coordinates.
     * <p>
     * NOTE: To properly map a rectangle or a corner of a rectangle on a right-to-left platform, use
     * {@link Display#map(Control, Control, Rectangle)}.
     * </p>
     *
     * @param point the point to be translated (must not be null)
     * @return the translated coordinates
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Point toDisplay(Point point) {
        checkWidget();
        if (point == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        return toDisplay(point.x, point.y);
    }

    /**
     * GTK4 only function to replace gdk_surface_get_origin
     * @return the origin of the Control's SWTFixed container relative to the Shell
     */
    Point getControlOrigin() {
        return null;
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the control is moved or resized, by sending
     * it one of the messages defined in the <code>ControlListener</code>
     * interface.
     *
     * @param listener the listener which should be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see ControlListener
     * @see #removeControlListener
     */
    public void addControlListener(ControlListener listener) {
        addTypedListener(listener, SWT.Resize, SWT.Move);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when a drag gesture occurs, by sending it
     * one of the messages defined in the <code>DragDetectListener</code>
     * interface.
     *
     * @param listener the listener which should be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see DragDetectListener
     * @see #removeDragDetectListener
     *
     * @since 3.3
     */
    public void addDragDetectListener(DragDetectListener listener) {
        addTypedListener(listener, SWT.DragDetect);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the control gains or loses focus, by sending
     * it one of the messages defined in the <code>FocusListener</code>
     * interface.
     *
     * @param listener the listener which should be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see FocusListener
     * @see #removeFocusListener
     */
    public void addFocusListener(FocusListener listener) {
        addTypedListener(listener, SWT.FocusIn, SWT.FocusOut);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when gesture events are generated for the control,
     * by sending it one of the messages defined in the
     * <code>GestureListener</code> interface.
     * <p>
     * NOTE: If <code>setTouchEnabled(true)</code> has previously been
     * invoked on the receiver then <code>setTouchEnabled(false)</code>
     * must be invoked on it to specify that gesture events should be
     * sent instead of touch events.
     * </p>
     * <p>
     * <b>Warning</b>: This API is currently only implemented on Windows and Cocoa.
     * SWT doesn't send Gesture or Touch events on GTK.
     * </p>
     *
     * @param listener the listener which should be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see GestureListener
     * @see #removeGestureListener
     * @see #setTouchEnabled
     *
     * @since 3.7
     */
    public void addGestureListener(GestureListener listener) {
        addTypedListener(listener, SWT.Gesture);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when help events are generated for the control,
     * by sending it one of the messages defined in the
     * <code>HelpListener</code> interface.
     *
     * @param listener the listener which should be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see HelpListener
     * @see #removeHelpListener
     */
    public void addHelpListener(HelpListener listener) {
        addTypedListener(listener, SWT.Help);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when keys are pressed and released on the system keyboard, by sending
     * it one of the messages defined in the <code>KeyListener</code>
     * interface.
     * <p>
     * When a key listener is added to a control, the control
     * will take part in widget traversal.  By default, all
     * traversal keys (such as the tab key and so on) are
     * delivered to the control.  In order for a control to take
     * part in traversal, it should listen for traversal events.
     * Otherwise, the user can traverse into a control but not
     * out.  Note that native controls such as table and tree
     * implement key traversal in the operating system.  It is
     * not necessary to add traversal listeners for these controls,
     * unless you want to override the default traversal.
     * </p>
     * @param listener the listener which should be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see KeyListener
     * @see #removeKeyListener
     */
    public void addKeyListener(KeyListener listener) {
        addTypedListener(listener, SWT.KeyUp, SWT.KeyDown);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the platform-specific context menu trigger
     * has occurred, by sending it one of the messages defined in
     * the <code>MenuDetectListener</code> interface.
     *
     * @param listener the listener which should be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see MenuDetectListener
     * @see #removeMenuDetectListener
     *
     * @since 3.3
     */
    public void addMenuDetectListener(MenuDetectListener listener) {
        addTypedListener(listener, SWT.MenuDetect);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when mouse buttons are pressed and released, by sending
     * it one of the messages defined in the <code>MouseListener</code>
     * interface.
     *
     * @param listener the listener which should be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see MouseListener
     * @see #removeMouseListener
     */
    public void addMouseListener(MouseListener listener) {
        addTypedListener(listener, SWT.MouseDown, SWT.MouseUp, SWT.MouseDoubleClick);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the mouse moves, by sending it one of the
     * messages defined in the <code>MouseMoveListener</code>
     * interface.
     *
     * @param listener the listener which should be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see MouseMoveListener
     * @see #removeMouseMoveListener
     */
    public void addMouseMoveListener(MouseMoveListener listener) {
        addTypedListener(listener, SWT.MouseMove);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the mouse passes or hovers over controls, by sending
     * it one of the messages defined in the <code>MouseTrackListener</code>
     * interface.
     *
     * @param listener the listener which should be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see MouseTrackListener
     * @see #removeMouseTrackListener
     */
    public void addMouseTrackListener(MouseTrackListener listener) {
        addTypedListener(listener, SWT.MouseEnter, SWT.MouseExit, SWT.MouseHover);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the mouse wheel is scrolled, by sending
     * it one of the messages defined in the
     * <code>MouseWheelListener</code> interface.
     *
     * @param listener the listener which should be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see MouseWheelListener
     * @see #removeMouseWheelListener
     *
     * @since 3.3
     */
    public void addMouseWheelListener(MouseWheelListener listener) {
        addTypedListener(listener, SWT.MouseWheel);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the receiver needs to be painted, by sending it
     * one of the messages defined in the <code>PaintListener</code>
     * interface.
     *
     * @param listener the listener which should be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see PaintListener
     * @see #removePaintListener
     */
    public void addPaintListener(PaintListener listener) {
        addTypedListener(listener, SWT.Paint);
    }

    /**
     * Allows Controls to adjust the clipping of themselves or
     * their children.
     *
     * @param widget the handle to the widget
     */
    void adjustChildClipping(long widget) {
    }

    public void addRelation(Control control) {
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when touch events occur, by sending it
     * one of the messages defined in the <code>TouchListener</code>
     * interface.
     * <p>
     * NOTE: You must also call <code>setTouchEnabled(true)</code> to
     * specify that touch events should be sent, which will cause gesture
     * events to not be sent.
     * </p>
     * <p>
     * <b>Warning</b>: This API is currently only implemented on Windows and Cocoa.
     * SWT doesn't send Gesture or Touch events on GTK.
     * </p>
     *
     * @param listener the listener which should be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see TouchListener
     * @see #removeTouchListener
     * @see #setTouchEnabled
     *
     * @since 3.7
     */
    public void addTouchListener(TouchListener listener) {
        addTypedListener(listener, SWT.Touch);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when traversal events occur, by sending it
     * one of the messages defined in the <code>TraverseListener</code>
     * interface.
     *
     * @param listener the listener which should be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see TraverseListener
     * @see #removeTraverseListener
     */
    public void addTraverseListener(TraverseListener listener) {
        addTypedListener(listener, SWT.Traverse);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the control is moved or resized.
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
     * @see ControlListener
     * @see #addControlListener
     */
    public void removeControlListener(ControlListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Move, listener);
        eventTable.unhook(SWT.Resize, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when a drag gesture occurs.
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
     * @see DragDetectListener
     * @see #addDragDetectListener
     *
     * @since 3.3
     */
    public void removeDragDetectListener(DragDetectListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.DragDetect, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the control gains or loses focus.
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
     * @see FocusListener
     * @see #addFocusListener
     */
    public void removeFocusListener(FocusListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.FocusIn, listener);
        eventTable.unhook(SWT.FocusOut, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when gesture events are generated for the control.
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
     * @see GestureListener
     * @see #addGestureListener
     *
     * @since 3.7
     */
    public void removeGestureListener(GestureListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Gesture, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the help events are generated for the control.
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
     * @see HelpListener
     * @see #addHelpListener
     */
    public void removeHelpListener(HelpListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Help, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when keys are pressed and released on the system keyboard.
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
     * @see KeyListener
     * @see #addKeyListener
     */
    public void removeKeyListener(KeyListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.KeyUp, listener);
        eventTable.unhook(SWT.KeyDown, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the platform-specific context menu trigger has
     * occurred.
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
     * @see MenuDetectListener
     * @see #addMenuDetectListener
     *
     * @since 3.3
     */
    public void removeMenuDetectListener(MenuDetectListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.MenuDetect, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when mouse buttons are pressed and released.
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
     * @see MouseListener
     * @see #addMouseListener
     */
    public void removeMouseListener(MouseListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.MouseDown, listener);
        eventTable.unhook(SWT.MouseUp, listener);
        eventTable.unhook(SWT.MouseDoubleClick, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the mouse moves.
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
     * @see MouseMoveListener
     * @see #addMouseMoveListener
     */
    public void removeMouseMoveListener(MouseMoveListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.MouseMove, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the mouse passes or hovers over controls.
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
     * @see MouseTrackListener
     * @see #addMouseTrackListener
     */
    public void removeMouseTrackListener(MouseTrackListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.MouseEnter, listener);
        eventTable.unhook(SWT.MouseExit, listener);
        eventTable.unhook(SWT.MouseHover, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the mouse wheel is scrolled.
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
     * @see MouseWheelListener
     * @see #addMouseWheelListener
     *
     * @since 3.3
     */
    public void removeMouseWheelListener(MouseWheelListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.MouseWheel, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the receiver needs to be painted.
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
     * @see PaintListener
     * @see #addPaintListener
     */
    public void removePaintListener(PaintListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Paint, listener);
    }

    /*
 * Remove "Labelled by" relation from the receiver.
 */
    public void removeRelation() {
        if (!isDescribedByLabel())
            return;
        /* there will not be any */
        if (labelRelation != null) {
            _getAccessible().removeRelation(ACC.RELATION_LABELLED_BY, ((DartControl) labelRelation.getImpl())._getAccessible());
            labelRelation = null;
        }
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when touch events occur.
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
     * @see TouchListener
     * @see #addTouchListener
     *
     * @since 3.7
     */
    public void removeTouchListener(TouchListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Touch, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when traversal events occur.
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
     * @see TraverseListener
     * @see #addTraverseListener
     */
    public void removeTraverseListener(TraverseListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Traverse, listener);
    }

    /**
     * Detects a drag and drop gesture.  This method is used
     * to detect a drag gesture when called from within a mouse
     * down listener.
     *
     * <p>By default, a drag is detected when the gesture
     * occurs anywhere within the client area of a control.
     * Some controls, such as tables and trees, override this
     * behavior.  In addition to the operating system specific
     * drag gesture, they require the mouse to be inside an
     * item.  Custom widget writers can use <code>setDragDetect</code>
     * to disable the default detection, listen for mouse down,
     * and then call <code>dragDetect()</code> from within the
     * listener to conditionally detect a drag.
     * </p>
     *
     * @param event the mouse down event
     *
     * @return <code>true</code> if the gesture occurred, and <code>false</code> otherwise.
     *
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_NULL_ARGUMENT if the event is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see DragDetectListener
     * @see #addDragDetectListener
     *
     * @see #getDragDetect
     * @see #setDragDetect
     *
     * @since 3.3
     */
    public boolean dragDetect(Event event) {
        checkWidget();
        if (event == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        return dragDetect(event.button, event.count, event.stateMask, event.x, event.y);
    }

    /**
     * Detects a drag and drop gesture.  This method is used
     * to detect a drag gesture when called from within a mouse
     * down listener.
     *
     * <p>By default, a drag is detected when the gesture
     * occurs anywhere within the client area of a control.
     * Some controls, such as tables and trees, override this
     * behavior.  In addition to the operating system specific
     * drag gesture, they require the mouse to be inside an
     * item.  Custom widget writers can use <code>setDragDetect</code>
     * to disable the default detection, listen for mouse down,
     * and then call <code>dragDetect()</code> from within the
     * listener to conditionally detect a drag.
     * </p>
     *
     * @param event the mouse down event
     *
     * @return <code>true</code> if the gesture occurred, and <code>false</code> otherwise.
     *
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_NULL_ARGUMENT if the event is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see DragDetectListener
     * @see #addDragDetectListener
     *
     * @see #getDragDetect
     * @see #setDragDetect
     *
     * @since 3.3
     */
    public boolean dragDetect(MouseEvent event) {
        checkWidget();
        if (event == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        return dragDetect(event.button, event.count, event.stateMask, event.x, event.y);
    }

    boolean dragDetect(int button, int count, int stateMask, int x, int y) {
        if (button != 1 || count != 1)
            return false;
        if (!dragDetect(x, y, false, true, null)) {
            return false;
        }
        return sendDragEvent(button, stateMask, x, y, true);
    }

    boolean dragDetect(int x, int y, boolean filter, boolean dragOnTimeout, boolean[] consume) {
        // Historically, SWT detected drag&drop on X11 this way:
        //   1) on left mouse down event, begin a modal event loop
        //      In GTK, this is `button-press-event` signal.
        //      In SWT, this is `Control#gtk_button_press_event()`
        //   2) In the loop, wait for mouse to move far enough to trigger drag&drop action
        //   3) If drag&drop is not triggered within 500ms, or mouse button released, or Esc pressed,
        //      and a few other conditions, consider it NOT drag&drop and a mere mouse click.
        //
        // However, on Wayland, it is no longer possible: as long as SWT does not
        // return from `button-press-event` signal handler, it is not possible to
        // receive ANY mouse events from `gdk_event_get()`. Not mouse movements,
        // not even mouse up event. This causes SWT to pointlessly loop for 500ms
        // after each mouse down.
        //
        // In Bug 503431, approach was changed to trigger drag&drop from MouseMove signal instead
        //   In GTK, this is `motion-notify-event` signal.
        //   In SWT, this is `Control#gtk_motion_notify_event()`
        //
        // Initially the new code was supposed to work on both X11 and Wayland, but unfortunately
        // the new implementation had its problems:
        // * Bug 503431 - `StyledText` wants to know if DND has started right after calling `dragDetect()`
        //   This required the workaround where `MouseDown` event is not reported until DND decision is made.
        // * Bug 503431 - multi-select `Tree`, `Table`, `List` drag&drop broken (only drags one item)
        //   This is because GTK itself doesn't support multi-item DND.
        // * Bug 503431 - various mistakes
        //   For example, there was a `Callback` leak. It was fixed soon after.
        // * Issue 400  - wrong mouse events reporting
        //   This is what I'm fixing now.
        //
        // As a result, in Bug 510446, the new implementation was limited to Wayland only, so that more
        // common back then X11 could continue to work without bugs.
        //
        // I hope that I fixed the last of the problems on Wayland now (2023-03-11),
        // and X11 code could finally be thrown away. But I don't think I dare to try it right now.
        return false;
    }

    boolean filterKey(long event) {
        long imHandle = imHandle();
        if (imHandle != 0) {
        }
        return false;
    }

    public Control findBackgroundControl() {
        return parent.getImpl().findBackgroundControl();
    }

    public Menu[] findMenus(Control control) {
        if (menu != null && this.getApi() != control)
            return new Menu[] { menu };
        return new Menu[0];
    }

    public void fixChildren(Shell newShell, Shell oldShell, Decorations newDecorations, Decorations oldDecorations, Menu[] menus) {
        ((SwtShell) oldShell.getImpl()).fixShell(newShell, this.getApi());
        ((SwtDecorations) oldDecorations.getImpl()).fixDecorations(newDecorations, this.getApi(), menus);
    }

    void fixModal(long group, long modalGroup) {
    }

    /**
     * Forces the receiver to have the <em>keyboard focus</em>, causing
     * all keyboard events to be delivered to it.
     *
     * @return <code>true</code> if the control got focus, and <code>false</code> if it was unable to.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setFocus
     */
    public boolean forceFocus() {
        checkWidget();
        if (((SwtDisplay) display.getImpl()).focusEvent == SWT.FocusOut)
            return false;
        Shell shell = getShell();
        ((SwtDecorations) shell.getImpl()).setSavedFocus(this.getApi());
        if (!isEnabled() || !isVisible())
            return false;
        if (display.getActiveShell() != shell && !SwtDisplay.isActivateShellOnForceFocus())
            return false;
        ((SwtShell) shell.getImpl()).bringToTop(false);
        boolean result = getBridge().setFocus(this);
        return result;
    }

    /**
     * Returns the receiver's background color.
     * <p>
     * Note: This operation is a hint and may be overridden by the platform.
     * For example, on some versions of Windows the background of a TabFolder,
     * is a gradient rather than a solid color.
     * </p>
     * @return the background color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Color getBackground() {
        checkWidget();
        if (this.background != null) {
            return this.background;
        }
        if (backgroundAlpha != 0) {
            Control control = findBackgroundControl();
            if (control != null && control != this.getApi()) {
                return control.getBackground();
            }
        }
        if (parent != null) {
            return parent.getBackground();
        }
        return this.background != null ? this.background : GraphicsUtils.getDefaultBackground(display);
    }

    /**
     * Returns the receiver's background image.
     *
     * @return the background image
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    public Image getBackgroundImage() {
        checkWidget();
        Control control = findBackgroundControl();
        if (control == null)
            control = this.getApi();
        return control.getImpl()._backgroundImage();
    }

    /**
     * Returns the receiver's border width in points.
     *
     * @return the border width
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getBorderWidth() {
        return getBorderWidthInPixels();
    }

    int getBorderWidthInPixels() {
        checkWidget();
        return 0;
    }

    int getClientWidth() {
        if (getApi().handle == 0 || (getApi().state & ZERO_WIDTH) != 0)
            return 0;
        return 0;
    }

    /**
     * Returns the receiver's cursor, or null if it has not been set.
     * <p>
     * When the mouse pointer passes over a control its appearance
     * is changed to match the control's cursor.
     * </p>
     *
     * @return the receiver's cursor or <code>null</code>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.3
     */
    public Cursor getCursor() {
        checkWidget();
        return cursor;
    }

    /**
     * Returns <code>true</code> if the receiver is detecting
     * drag gestures, and  <code>false</code> otherwise.
     *
     * @return the receiver's drag detect state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.3
     */
    public boolean getDragDetect() {
        checkWidget();
        return (getApi().state & DRAG_DETECT) != 0;
    }

    /**
     * Returns <code>true</code> if the receiver is enabled, and
     * <code>false</code> otherwise. A disabled control is typically
     * not selectable from the user interface and draws with an
     * inactive or "grayed" look.
     *
     * @return the receiver's enabled state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #isEnabled
     */
    public boolean getEnabled() {
        checkWidget();
        return (getApi().state & DISABLED) == 0;
    }

    /**
     * Returns the font that the receiver will use to paint textual information.
     *
     * @return the receiver's font
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Font getFont() {
        checkWidget();
        return font != null ? font : defaultFont();
    }

    /**
     * @return A newly allocated <code>PangoFontDescription*</code>, caller
     *         must free it with {@link OS#pango_font_description_free}.
     */
    long getFontDescription() {
        if ("ppc64le".equals(System.getProperty("os.arch"))) {
            // Unlike 'gtk_style_context_get()', 'gtk_style_context_get_font()'
        } else {
        }
        return 0;
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
     */
    public Color getForeground() {
        checkWidget();
        return this.foreground;
    }

    Point getIMCaretPos() {
        return new Point(0, 0);
    }

    /**
     * Returns layout data which is associated with the receiver.
     *
     * @return the receiver's layout data
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Object getLayoutData() {
        checkWidget();
        return layoutData;
    }

    /**
     * Returns the receiver's pop up menu if it has one, or null
     * if it does not. All controls may optionally have a pop up
     * menu that is displayed when the user requests one for
     * the control. The sequence of key strokes, button presses
     * and/or button releases that are used to request a pop up
     * menu is platform specific.
     *
     * @return the receiver's menu
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Menu getMenu() {
        checkWidget();
        return menu;
    }

    /**
     * Returns the receiver's monitor.
     *
     * @return the receiver's monitor
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    public Monitor getMonitor() {
        checkWidget();
        return display.getPrimaryMonitor();
    }

    /**
     * Returns the receiver's parent, which must be a <code>Composite</code>
     * or null when the receiver is a shell that was created with null or
     * a display for a parent.
     *
     * @return the receiver's parent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Composite getParent() {
        checkWidget();
        return parent;
    }

    public Control[] getPath() {
        int count = 0;
        Shell shell = getShell();
        Control control = this.getApi();
        while (control != shell) {
            count++;
            control = control.getImpl()._parent();
        }
        control = this.getApi();
        Control[] result = new Control[count];
        while (control != shell) {
            result[--count] = control;
            control = control.getImpl()._parent();
        }
        return result;
    }

    /**
     * Returns the region that defines the shape of the control,
     * or null if the control has the default shape.
     *
     * @return the region that defines the shape of the shell (or null)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    public Region getRegion() {
        checkWidget();
        return region;
    }

    /**
     * Returns the receiver's shell. For all controls other than
     * shells, this simply returns the control's nearest ancestor
     * shell. Shells return themselves, even if they are children
     * of other shells.
     *
     * @return the receiver's shell
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #getParent
     */
    public Shell getShell() {
        checkWidget();
        return _getShell();
    }

    public Shell _getShell() {
        return parent.getImpl()._getShell();
    }

    /**
     * Returns the receiver's tool tip text, or null if it has
     * not been set.
     *
     * @return the receiver's tool tip text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public String getToolTipText() {
        checkWidget();
        return toolTipText;
    }

    /**
     * Returns <code>true</code> if this control is set to send touch events, or
     * <code>false</code> if it is set to send gesture events instead.  This method
     * also returns <code>false</code> if a touch-based input device is not detected
     * (this can be determined with <code>Display#getTouchEnabled()</code>).  Use
     * {@link #setTouchEnabled(boolean)} to switch the events that a control sends
     * between touch events and gesture events.
     *
     * @return <code>true</code> if the control is set to send touch events, or <code>false</code> otherwise
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setTouchEnabled
     * @see Display#getTouchEnabled
     *
     * @since 3.7
     */
    public boolean getTouchEnabled() {
        checkWidget();
        return false;
    }

    /**
     * Returns <code>true</code> if the receiver is visible, and
     * <code>false</code> otherwise.
     * <p>
     * If one of the receiver's ancestors is not visible or some
     * other condition makes the receiver not visible, this method
     * may still indicate that it is considered visible even though
     * it may not actually be showing.
     * </p>
     *
     * @return the receiver's visibility state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean getVisible() {
        checkWidget();
        return (getApi().state & HIDDEN) == 0;
    }

    Point getThickness(long widget) {
        int xthickness = 0, ythickness = 0;
        return new Point(xthickness, ythickness);
    }

    boolean wantDragDropDetection() {
        // Drag&drop detection has its costs: when mouse button is pressed,
        // mouse events are not reported until SWT can decide if drag&drop
        // was triggered or not. For some applications, this could be a problem.
        // Consider for example a graphical drawing application: it would expect
        // to begin drawing as soon as mouse button is pressed.
        // If app is interested in drag&drop, it will likely listen to `DragDetect`.
        return hooks(SWT.DragDetect);
    }

    boolean checkSubwindow() {
        return false;
    }

    /**
     * Invokes platform specific functionality to allocate a new GC handle.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
     * API for <code>Control</code>. It is marked public only so that it
     * can be shared within the packages provided by SWT. It is not
     * available on all platforms, and should never be called from
     * application code.
     * </p>
     *
     * @param data the platform specific GC data
     * @return the platform specific GC handle
     *
     * @noreference This method is not intended to be referenced by clients.
     */
    @Override
    public long internal_new_GC(GCData data) {
        return 0;
    }

    long imHandle() {
        return 0;
    }

    /**
     * Invokes platform specific functionality to dispose a GC handle.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
     * API for <code>Control</code>. It is marked public only so that it
     * can be shared within the packages provided by SWT. It is not
     * available on all platforms, and should never be called from
     * application code.
     * </p>
     *
     * @param hDC the platform specific GC handle
     * @param data the platform specific GC data
     *
     * @noreference This method is not intended to be referenced by clients.
     */
    @Override
    public void internal_dispose_GC(long hDC, GCData data) {
        checkWidget();
    }

    /**
     * Returns <code>true</code> if the underlying operating
     * system supports this reparenting, otherwise <code>false</code>
     *
     * @return <code>true</code> if the widget can be reparented, otherwise <code>false</code>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean isReparentable() {
        checkWidget();
        return true;
    }

    boolean isShowing() {
        /*
	* This is not complete.  Need to check if the
	* widget is obscurred by a parent or sibling.
	*/
        if (!isVisible())
            return false;
        Control control = this.getApi();
        while (control != null) {
            Point size = control.getImpl().getSizeInPixels();
            if (size.x == 0 || size.y == 0) {
                return false;
            }
            control = control.getImpl()._parent();
        }
        return true;
    }

    public boolean isTabGroup() {
        Control[] tabList = parent.getImpl()._getTabList();
        if (tabList != null) {
            for (int i = 0; i < tabList.length; i++) {
                if (tabList[i] == this.getApi())
                    return true;
            }
        }
        int code = traversalCode(0, 0);
        if ((code & (SWT.TRAVERSE_ARROW_PREVIOUS | SWT.TRAVERSE_ARROW_NEXT)) != 0)
            return false;
        return (code & (SWT.TRAVERSE_TAB_PREVIOUS | SWT.TRAVERSE_TAB_NEXT)) != 0;
    }

    public boolean isTabItem() {
        Control[] tabList = parent.getImpl()._getTabList();
        if (tabList != null) {
            for (int i = 0; i < tabList.length; i++) {
                if (tabList[i] == this.getApi())
                    return false;
            }
        }
        int code = traversalCode(0, 0);
        return (code & (SWT.TRAVERSE_ARROW_PREVIOUS | SWT.TRAVERSE_ARROW_NEXT)) != 0;
    }

    /**
     * Returns <code>true</code> if the receiver is enabled and all
     * ancestors up to and including the receiver's nearest ancestor
     * shell are enabled.  Otherwise, <code>false</code> is returned.
     * A disabled control is typically not selectable from the user
     * interface and draws with an inactive or "grayed" look.
     *
     * @return the receiver's enabled state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #getEnabled
     */
    public boolean isEnabled() {
        checkWidget();
        return getEnabled() && parent.isEnabled();
    }

    boolean isFocusAncestor(Control control) {
        while (control != null && control != this.getApi() && !(control instanceof Shell)) {
            control = control.getImpl()._parent();
        }
        return control == this.getApi();
    }

    /**
     * Returns <code>true</code> if the receiver has the user-interface
     * focus, and <code>false</code> otherwise.
     *
     * @return the receiver's focus state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean isFocusControl() {
        checkWidget();
        Control focusControl = ((SwtDisplay) display.getImpl()).focusControl;
        if (focusControl != null && !focusControl.isDisposed()) {
            return this.getApi() == focusControl;
        }
        return hasFocus();
    }

    /**
     * Returns <code>true</code> if the receiver is visible and all
     * ancestors up to and including the receiver's nearest ancestor
     * shell are visible. Otherwise, <code>false</code> is returned.
     *
     * @return the receiver's visibility state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #getVisible
     */
    public boolean isVisible() {
        checkWidget();
        return getVisible() && parent.isVisible();
    }

    public Decorations menuShell() {
        return parent.getImpl().menuShell();
    }

    boolean mnemonicHit(char key) {
        return false;
    }

    boolean mnemonicMatch(char key) {
        return false;
    }

    @Override
    void register() {
        _hookEvents();
        bridge = FlutterBridge.of(this);
    }

    /**
     * Requests that this control and all of its ancestors be repositioned by
     * their layouts at the earliest opportunity. This should be invoked after
     * modifying the control in order to inform any dependent layouts of
     * the change.
     * <p>
     * The control will not be repositioned synchronously. This method is
     * fast-running and only marks the control for future participation in
     * a deferred layout.
     * <p>
     * Invoking this method multiple times before the layout occurs is an
     * inexpensive no-op.
     *
     * @since 3.105
     */
    public void requestLayout() {
        getShell().layout(new Control[] { this.getApi() }, SWT.DEFER);
    }

    /**
     * Causes the entire bounds of the receiver to be marked
     * as needing to be redrawn. The next time a paint request
     * is processed, the control will be completely painted,
     * including the background.
     * <p>
     * Schedules a paint request if the invalidated area is visible
     * or becomes visible later. It is not necessary for the caller
     * to explicitly call {@link #update()} after calling this method,
     * but depending on the platform, the automatic repaints may be
     * delayed considerably.
     * </p>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #update()
     * @see PaintListener
     * @see SWT#Paint
     * @see SWT#NO_BACKGROUND
     * @see SWT#NO_REDRAW_RESIZE
     * @see SWT#NO_MERGE_PAINTS
     * @see SWT#DOUBLE_BUFFERED
     */
    public void redraw() {
        checkWidget();
        redraw(false);
    }

    void redraw(boolean all) {
        redrawWidget(0, 0, 0, 0, true, all, false);
    }

    /**
     * Causes the rectangular area of the receiver specified by
     * the arguments to be marked as needing to be redrawn.
     * The next time a paint request is processed, that area of
     * the receiver will be painted, including the background.
     * If the <code>all</code> flag is <code>true</code>, any
     * children of the receiver which intersect with the specified
     * area will also paint their intersecting areas. If the
     * <code>all</code> flag is <code>false</code>, the children
     * will not be painted.
     * <p>
     * Schedules a paint request if the invalidated area is visible
     * or becomes visible later. It is not necessary for the caller
     * to explicitly call {@link #update()} after calling this method,
     * but depending on the platform, the automatic repaints may be
     * delayed considerably.
     * </p>
     *
     * @param x the x coordinate of the area to draw
     * @param y the y coordinate of the area to draw
     * @param width the width of the area to draw
     * @param height the height of the area to draw
     * @param all <code>true</code> if children should redraw, and <code>false</code> otherwise
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #update()
     * @see PaintListener
     * @see SWT#Paint
     * @see SWT#NO_BACKGROUND
     * @see SWT#NO_REDRAW_RESIZE
     * @see SWT#NO_MERGE_PAINTS
     * @see SWT#DOUBLE_BUFFERED
     */
    public void redraw(int x, int y, int width, int height, boolean all) {
        checkWidget();
        if ((getApi().style & SWT.MIRRORED) != 0)
            x = getClientWidth() - width - x;
        redrawWidget(x, y, width, height, false, all, false);
    }

    void redrawChildren() {
    }

    void redrawWidget(int x, int y, int width, int height, boolean redrawAll, boolean all, boolean trim) {
    }

    @Override
    public void release(boolean destroy) {
        Control next = null, previous = null;
        if (destroy && parent != null) {
            Control[] children = parent.getImpl()._getChildren();
            int index = 0;
            while (index < children.length) {
                if (children[index] == this.getApi())
                    break;
                index++;
            }
            if (index > 0) {
                previous = children[index - 1];
            }
            if (index + 1 < children.length) {
                next = children[index + 1];
                next.getImpl().removeRelation();
            }
            removeRelation();
        }
        super.release(destroy);
        if (destroy) {
            if (previous != null && next != null)
                previous.getImpl().addRelation(next);
        }
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
        fixedHandle = 0;
        parent = null;
    }

    @Override
    void releaseParent() {
        parent.getImpl().removeControl(this.getApi());
    }

    @Override
    void releaseWidget() {
        boolean hadFocus = display.getFocusControl() == this.getApi();
        super.releaseWidget();
        if (hadFocus)
            fixFocus(this.getApi());
        if (((SwtDisplay) display.getImpl()).currentControl == this.getApi())
            ((SwtDisplay) display.getImpl()).currentControl = null;
        ((SwtDisplay) display.getImpl()).removeMouseHoverTimeout(getApi().handle);
        if (menu != null && !menu.isDisposed()) {
            menu.dispose();
        }
        menu = null;
        cursor = null;
        toolTipText = null;
        layoutData = null;
        if (accessible != null) {
            accessible.internal_dispose_Accessible();
        }
        accessible = null;
        region = null;
        if (dragGesture != 0) {
            dragGesture = 0;
        }
        if (rotateGesture != 0) {
            rotateGesture = 0;
        }
        if (zoomGesture != 0) {
            zoomGesture = 0;
        }
    }

    @Override
    void destroyWidget() {
        {
            super.destroyWidget();
        }
    }

    /**
     * GTK3 only, do not call on GTK4.
     * @param window a GdkWindow
     * @param sibling the sibling thereof, or 0
     * @param above a boolean setting for whether the window
     * should be raised or lowered
     */
    void restackWindow(long window, long sibling, boolean above) {
    }

    void flushQueueOnDnd() {
        // Case where mouse motion triggered a DnD:
        // Send only initial MouseDown but not the MouseMove events that were used
        // to determine DnD threshold.
        // This is to preserve backwards Cocoa/Win32 compatibility.
        Event mouseDownEvent = dragDetectionQueue.getFirst();
        // force send MouseDown to avoid subsequent MouseMove before MouseDown.
        mouseDownEvent.data = Boolean.valueOf(true);
        dragDetectionQueue = null;
        sendOrPost(SWT.MouseDown, mouseDownEvent);
    }

    boolean sendDragEvent(int button, int stateMask, int x, int y, boolean isStateMask) {
        Event event = new Event();
        event.button = button;
        Rectangle eventRect = new Rectangle(x, y, 0, 0);
        event.setBounds(eventRect);
        if ((getApi().style & SWT.MIRRORED) != 0)
            event.x = getClientWidth() - event.x;
        if (isStateMask) {
            event.stateMask = stateMask;
        } else {
            setInputState(event, stateMask);
        }
        postEvent(SWT.DragDetect, event);
        if (isDisposed())
            return false;
        return event.doit;
    }

    public void sendFocusEvent(int type) {
        Shell shell = _getShell();
        Display display = this.display;
        ((SwtDisplay) display.getImpl()).focusControl = this.getApi();
        ((SwtDisplay) display.getImpl()).focusEvent = type;
        sendEvent(type);
        ((SwtDisplay) display.getImpl()).focusControl = null;
        ((SwtDisplay) display.getImpl()).focusEvent = SWT.None;
        /*
	* It is possible that the shell may be
	* disposed at this point.  If this happens
	* don't send the activate and deactivate
	* events.
	*/
        if (!shell.isDisposed()) {
            switch(type) {
                case SWT.FocusIn:
                    ((SwtShell) shell.getImpl()).setActiveControl(this.getApi());
                    break;
                case SWT.FocusOut:
                    if (shell != ((SwtDisplay) display.getImpl()).activeShell) {
                        ((SwtShell) shell.getImpl()).setActiveControl(null);
                    }
                    break;
            }
        }
    }

    boolean sendGestureEvent(int stateMask, int detail, int x, int y, double delta) {
        if (containedInRegion(x, y))
            return false;
        switch(detail) {
            case SWT.GESTURE_ROTATE:
                {
                    return sendGestureEvent(stateMask, detail, x, y, delta, 0, 0, 0);
                }
            case SWT.GESTURE_MAGNIFY:
                {
                    return sendGestureEvent(stateMask, detail, x, y, 0, 0, 0, delta);
                }
            case SWT.GESTURE_BEGIN:
                {
                    return sendGestureEvent(stateMask, detail, x, y, 0, 0, 0, delta);
                }
            case SWT.GESTURE_END:
                {
                    return sendGestureEvent(stateMask, detail, 0, 0, 0, 0, 0, 0);
                }
            default:
                //case not supported.
                return false;
        }
    }

    boolean sendGestureEvent(int stateMask, int detail, int x, int y, double xDirection, double yDirection) {
        if (containedInRegion(x, y))
            return false;
        if (detail == SWT.GESTURE_SWIPE) {
            return sendGestureEvent(stateMask, detail, x, y, 0, (int) xDirection, (int) yDirection, 0);
        } else
            return false;
    }

    boolean sendGestureEvent(int stateMask, int detail, int x, int y, double rotation, int xDirection, int yDirection, double magnification) {
        if (containedInRegion(x, y))
            return false;
        Event event = new Event();
        event.stateMask = stateMask;
        event.detail = detail;
        event.x = x;
        event.y = y;
        switch(detail) {
            case SWT.GESTURE_ROTATE:
                {
                    event.rotation = rotation;
                    break;
                }
            case SWT.GESTURE_MAGNIFY:
                {
                    event.magnification = magnification;
                    break;
                }
            case SWT.GESTURE_SWIPE:
                {
                    event.xDirection = xDirection;
                    event.yDirection = yDirection;
                    break;
                }
            case SWT.GESTURE_BEGIN:
            case SWT.GESTURE_END:
                {
                    break;
                }
        }
        postEvent(SWT.Gesture, event);
        if (isDisposed())
            return false;
        return event.doit;
    }

    boolean sendHelpEvent(long helpType) {
        Control control = this.getApi();
        while (control != null) {
            if (((DartWidget) control.getImpl()).hooks(SWT.Help)) {
                ((DartWidget) control.getImpl()).postEvent(SWT.Help);
                return true;
            }
            control = control.getImpl()._parent();
        }
        return false;
    }

    boolean sendLeaveNotify() {
        return false;
    }

    boolean sendMouseEvent(int type, int button, int time, double x, double y, boolean is_hint, int state) {
        if (containedInRegion((int) x, (int) y))
            return true;
        return sendMouseEvent(type, button, 0, 0, false, time, x, y, is_hint, state);
    }

    /*
 * @return
 * 	true - event sending not canceled by user.
 *  false - event sending canceled by user.
 */
    boolean sendMouseEvent(int type, int button, int count, int detail, boolean send, int time, double x, double y, boolean is_hint, int state) {
        if (containedInRegion((int) x, (int) y))
            return true;
        if (!hooks(type) && !filters(type)) {
        }
        Event event = new Event();
        event.time = time;
        event.button = button;
        event.detail = detail;
        event.count = count;
        if (is_hint) {
            // coordinates are already window-relative, see #gtk_motion_notify_event(..) and bug 94502
            Rectangle eventRect = new Rectangle((int) x, (int) y, 0, 0);
            event.setBounds(eventRect);
        } else {
        }
        if ((getApi().style & SWT.MIRRORED) != 0)
            event.x = getClientWidth() - event.x;
        setInputState(event, state);
        /**
         * Bug 510446:
         * For Wayland support, Drag detection is now done in mouseMove (as does gtk internally).
         *
         * However, traditionally external widgets (e.g StyledText or non-SWT widgets) expect to
         * know if a drag has started by the time mouseDown is sent.
         * As such, for backwards compatibility with external widgets (e.g StyledText.java), we
         * delay sending of SWT.MouseDown (and also queue up SWT.MouseMove) until we know if a
         * drag started or not.
         *
         * Technical notes:
         * - To ensure we follow 'send/post' contract as per parameter, we
         *   temporarily utilize event.data to hold send/post flag.
         *   There's also logic in place such that mouseDown/mouseMotion is always sent before mouseUp.
         * - On Gtk3x11 it's not due to hacky implementation of DnD.
         *   On Wayland mouseMove is once again sent during DnD as per improved architecture.
         */
        event.data = Boolean.valueOf(send);
        return sendOrPost(type, event);
    }

    private boolean sendOrPost(int type, Event event) {
        assert event.data != null : "event.data should have been a Boolean, but received null";
        boolean send = (Boolean) event.data;
        event.data = null;
        if (send) {
            sendEvent(type, event);
            if (isDisposed())
                return false;
        } else {
            postEvent(type, event);
        }
        return event.doit;
    }

    void setBackground() {
        if ((getApi().state & BACKGROUND) == 0 && backgroundImage == null) {
            if ((getApi().state & PARENT_BACKGROUND) != 0) {
                setParentBackground();
            } else {
                setWidgetBackground();
            }
            redrawWidget(0, 0, 0, 0, true, false, false);
        }
    }

    /**
     * Sets the receiver's background color to the color specified
     * by the argument, or to the default system color for the control
     * if the argument is null.
     * <p>
     * Note: This operation is a hint and may be overridden by the platform.
     * </p>
     * <p>
     * Note: The background color can be overridden by setting a background image.
     * </p>
     * @param color the new color (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setBackground(Color color) {
        checkWidget();
        _setBackground(color);
        if (color != null) {
            this.updateBackgroundMode();
        }
    }

    private void _setBackground(Color color) {
        Color newValue = color;
        if (!java.util.Objects.equals(this.background, newValue)) {
            dirty();
        }
        if (((getApi().state & BACKGROUND) == 0) && color == null)
            return;
        if (color != null && color.isDisposed()) {
            error(SWT.ERROR_INVALID_ARGUMENT);
        }
        boolean set = false;
        if (color != null) {
            backgroundAlpha = color.getAlpha();
        }
        set = true;
        if (set) {
            if (color == null) {
                getApi().state &= ~BACKGROUND;
            } else {
                getApi().state |= BACKGROUND;
            }
        }
        this.background = newValue;
        redrawChildren();
    }

    /**
     * Sets the receiver's background image to the image specified
     * by the argument, or to the default system color for the control
     * if the argument is null.  The background image is tiled to fill
     * the available space.
     * <p>
     * Note: This operation is a hint and may be overridden by the platform.
     * For example, on Windows the background of a Button cannot be changed.
     * </p>
     * <p>
     * Note: Setting a background image overrides a set background color.
     * </p>
     * @param image the new image (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument is not a bitmap</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    public void setBackgroundImage(Image image) {
        image = GraphicsUtils.copyImage(getDisplay(), image);
        checkWidget();
        if (!java.util.Objects.equals(this.backgroundImage, image)) {
            dirty();
        }
        if (image != null && image.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        if (image == backgroundImage && backgroundAlpha > 0)
            return;
        backgroundAlpha = 255;
        this.backgroundImage = image;
        if (backgroundImage != null) {
            setBackgroundSurface(backgroundImage);
            redrawWidget(0, 0, 0, 0, true, false, false);
        } else {
            setWidgetBackground();
        }
        redrawChildren();
    }

    void setBackgroundSurface(Image image) {
    }

    /**
     * If the argument is <code>true</code>, causes the receiver to have
     * all mouse events delivered to it until the method is called with
     * <code>false</code> as the argument.  Note that on some platforms,
     * a mouse button must currently be down for capture to be assigned.
     *
     * @param capture <code>true</code> to capture the mouse, and <code>false</code> to release it
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setCapture(boolean capture) {
        boolean newValue = capture;
        if (!java.util.Objects.equals(this.capture, newValue)) {
            dirty();
        }
        this.capture = newValue;
        checkWidget();
        /* FIXME !!!!! */
        /*
	if (capture) {
		OS.gtk_widget_grab_focus (handle);
	} else {
		OS.gtk_widget_grab_default (handle);
	}
	*/
    }

    /**
     * Sets the receiver's cursor to the cursor specified by the
     * argument, or to the default cursor for that kind of control
     * if the argument is null.
     * <p>
     * When the mouse pointer passes over a control its appearance
     * is changed to match the control's cursor.
     * </p>
     *
     * @param cursor the new cursor (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setCursor(Cursor cursor) {
        checkWidget();
        if (!java.util.Objects.equals(this.cursor, cursor)) {
            dirty();
        }
        if (cursor != null && cursor.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        this.cursor = cursor;
        setCursor(cursor != null ? cursor.handle : 0);
    }

    public void setCursor(long cursor) {
        getBridge().setCursor(this, cursor);
    }

    /**
     * Sets the receiver's drag detect state. If the argument is
     * <code>true</code>, the receiver will detect drag gestures,
     * otherwise these gestures will be ignored.
     *
     * @param dragDetect the new drag detect state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.3
     */
    public void setDragDetect(boolean dragDetect) {
        boolean newValue = dragDetect;
        if (!java.util.Objects.equals(this.dragDetect, newValue)) {
            dirty();
        }
        checkWidget();
        this.dragDetect = newValue;
        if (dragDetect) {
            getApi().state |= DRAG_DETECT;
        } else {
            getApi().state &= ~DRAG_DETECT;
        }
    }

    /**
     * Enables the receiver if the argument is <code>true</code>,
     * and disables it otherwise. A disabled control is typically
     * not selectable from the user interface and draws with an
     * inactive or "grayed" look.
     *
     * @param enabled the new enabled state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setEnabled(boolean enabled) {
        boolean newValue = enabled;
        if (!java.util.Objects.equals(this.enabled, newValue)) {
            dirty();
        }
        checkWidget();
        if (((getApi().state & DISABLED) == 0) == enabled)
            return;
        Control control = null;
        boolean fixFocus = false;
        if (!enabled) {
            if (((SwtDisplay) display.getImpl()).focusEvent != SWT.FocusOut) {
                control = display.getFocusControl();
                fixFocus = isFocusAncestor(control);
            }
        }
        if (enabled) {
            getApi().state &= ~DISABLED;
        } else {
            getApi().state |= DISABLED;
        }
        enableWidget(enabled);
        if (isDisposed())
            return;
        this.enabled = newValue;
        if (fixFocus)
            fixFocus(control);
    }

    void cleanupEnableWindow() {
        enableWindow = 0;
    }

    /**
     * Causes the receiver to have the <em>keyboard focus</em>,
     * such that all keyboard events will be delivered to it.  Focus
     * reassignment will respect applicable platform constraints.
     *
     * @return <code>true</code> if the control got focus, and <code>false</code> if it was unable to.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #forceFocus
     */
    public boolean setFocus() {
        checkWidget();
        if ((getApi().style & SWT.NO_FOCUS) != 0)
            return false;
        return forceFocus();
    }

    /**
     * Sets the font that the receiver will use to paint textual information
     * to the font specified by the argument, or to the default font for that
     * kind of control if the argument is null.
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
     */
    public void setFont(Font font) {
        font = GraphicsUtils.copyFont(font);
        checkWidget();
        if (!java.util.Objects.equals(this.font, font)) {
            dirty();
        }
        if (((getApi().state & FONT) == 0) && font == null)
            return;
        this.font = font;
        long fontDesc;
        if (font == null) {
            fontDesc = defaultFont().handle;
        } else {
            if (font.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
            fontDesc = font.handle;
        }
        if (font == null) {
            getApi().state &= ~FONT;
        } else {
            getApi().state |= FONT;
        }
        setFontDescription(fontDesc);
    }

    void setFontDescription(long font) {
        setFontDescription(getApi().handle, font);
    }

    /**
     * Sets the receiver's foreground color to the color specified
     * by the argument, or to the default system color for the control
     * if the argument is null.
     * <p>
     * Note: This operation is a hint and may be overridden by the platform.
     * </p>
     * @param color the new color (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setForeground(Color color) {
        checkWidget();
        if (((getApi().state & FOREGROUND) == 0) && color == null)
            return;
        if (color != null && color.isDisposed()) {
            error(SWT.ERROR_INVALID_ARGUMENT);
        }
        boolean set = false;
        this.foreground = color;
        set = !getForeground().equals(color);
        if (set) {
            if (color == null) {
                getApi().state &= ~FOREGROUND;
            } else {
                getApi().state |= FOREGROUND;
            }
        }
    }

    void setInitialBounds() {
        if ((getApi().state & ZERO_WIDTH) != 0 && (getApi().state & ZERO_HEIGHT) != 0) {
        } else {
            resizeHandle(1, 1);
            forceResize();
        }
    }

    /**
     * Widgets with unusual bounds calculation behavior can override this method
     * to return {@code true} if the widget must be visible during call to
     * {@link #setInitialBounds()}.
     *
     * @return {@code false} by default on modern GTK 3 versions (3.20+).
     */
    boolean mustBeVisibleOnInitBounds() {
        return false;
    }

    /*
 * Sets the receivers Drag Gestures in order to do drag detection correctly for
 * X11/Wayland window managers after GTK3.14.
 * TODO currently phase is set to BUBBLE = 2. Look into using groups perhaps.
 */
    private void setDragGesture() {
        return;
    }

    //private void setPanGesture () {
    ///* TODO: Panning gesture requires a GtkOrientation object. Need to discuss what orientation should be default. */
    //}
    private void setRotateGesture() {
        return;
    }

    private void setZoomGesture() {
        return;
    }

    static Control getControl(long handle) {
        Display display = SwtDisplay.findDisplay(Thread.currentThread());
        if (display == null || display.isDisposed())
            return null;
        Widget widget = display.findWidget(handle);
        if (widget == null)
            return null;
        return (Control) widget;
    }

    static void rotateProc(long gesture, double angle, double angle_delta, long user_data) {
    }

    static void magnifyProc(long gesture, double zoom, long user_data) {
    }

    static void swipeProc(long gesture, double velocity_x, double velocity_y, long user_data) {
    }

    static void gestureBeginProc(long gesture, long sequence, long user_data) {
    }

    static void gestureEndProc(long gesture, long sequence, long user_data) {
    }

    /**
     * Sets the receiver's pop up menu to the argument.
     * All controls may optionally have a pop up
     * menu that is displayed when the user requests one for
     * the control. The sequence of key strokes, button presses
     * and/or button releases that are used to request a pop up
     * menu is platform specific.
     * <p>
     * Note: Disposing of a control that has a pop up menu will
     * dispose of the menu.  To avoid this behavior, set the
     * menu to null before the control is disposed.
     * </p>
     *
     * @param menu the new pop up menu
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_MENU_NOT_POP_UP - the menu is not a pop up menu</li>
     *    <li>ERROR_INVALID_PARENT - if the menu is not in the same widget tree</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the menu has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setMenu(Menu menu) {
        checkWidget();
        if (!java.util.Objects.equals(this.menu, menu)) {
            dirty();
        }
        if (menu != null) {
            if ((menu.style & SWT.POP_UP) == 0) {
                error(SWT.ERROR_MENU_NOT_POP_UP);
            }
            if (menu.getImpl()._parent() != menuShell()) {
                error(SWT.ERROR_INVALID_PARENT);
            }
        }
        this.menu = menu;
        if (menu != null && menu.getImpl() instanceof DartMenu) {
            ((DartMenu) menu.getImpl()).ownerControl = this.getApi();
        }
    }

    @Override
    void setOrientation(boolean create) {
        dirty();
        if ((getApi().style & SWT.RIGHT_TO_LEFT) != 0 || !create) {
        }
    }

    /**
     * Sets the orientation of the receiver, which must be one
     * of the constants <code>SWT.LEFT_TO_RIGHT</code> or <code>SWT.RIGHT_TO_LEFT</code>.
     *
     * @param orientation new orientation style
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.7
     */
    public void setOrientation(int orientation) {
        dirty();
        checkWidget();
        int flags = SWT.RIGHT_TO_LEFT | SWT.LEFT_TO_RIGHT;
        if ((orientation & flags) == 0 || (orientation & flags) == flags)
            return;
        getApi().style &= ~flags;
        getApi().style |= orientation & flags;
        setOrientation(false);
        getApi().style &= ~SWT.MIRRORED;
        checkMirrored();
    }

    /**
     *  Changes the parent of the widget to be the one provided.
     *  Returns <code>true</code> if the parent is successfully changed.
     *
     *  @param parent the new parent for the control.
     *  @return <code>true</code> if the parent is changed and <code>false</code> otherwise.
     *
     *  @exception IllegalArgumentException <ul>
     *     <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     *     <li>ERROR_NULL_ARGUMENT - if the parent is <code>null</code></li>
     *  </ul>
     *  @exception SWTException <ul>
     *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * 	</ul>
     */
    public boolean setParent(Composite parent) {
        checkWidget();
        if (parent == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (parent.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        if (this.parent == parent)
            return true;
        if (!isReparentable())
            return false;
        // preserve focus when re-parenting
        Control focusControlBeforeReparent = display.getFocusControl();
        if ((this.parent.style & SWT.MIRRORED) != 0) {
        }
        if ((parent.style & SWT.MIRRORED) != 0) {
        }
        releaseParent();
        Shell newShell = parent.getShell(), oldShell = getShell();
        Decorations newDecorations = parent.getImpl().menuShell(), oldDecorations = menuShell();
        Menu[] menus = oldShell.getImpl().findMenus(this.getApi());
        if (oldShell != newShell || oldDecorations != newDecorations) {
            fixChildren(newShell, oldShell, newDecorations, oldDecorations, menus);
        }
        getBridge().reparent(this, parent);
        ControlUtils.reparent(this, parent);
        setZOrder(null, false, true);
        reskin(SWT.ALL);
        // restore focus to the last Control that had it, if focus is now gone
        if (focusControlBeforeReparent != null && !focusControlBeforeReparent.isDisposed() && display.getFocusControl() == null) {
            focusControlBeforeReparent.setFocus();
        }
        return true;
    }

    void setParentBackground() {
    }

    boolean setRadioSelection(boolean value) {
        return false;
    }

    /**
     * If the argument is <code>false</code>, causes subsequent drawing
     * operations in the receiver to be ignored. No drawing of any kind
     * can occur in the receiver until the flag is set to true.
     * Graphics operations that occurred while the flag was
     * <code>false</code> are lost. When the flag is set to <code>true</code>,
     * the entire widget is marked as needing to be redrawn.  Nested calls
     * to this method are stacked.
     * <p>
     * Note: This operation is a hint and may not be supported on some
     * platforms or for some widgets.
     * </p>
     *
     * @param redraw the new redraw state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #redraw(int, int, int, int, boolean)
     * @see #update()
     */
    public void setRedraw(boolean redraw) {
        boolean newValue = redraw;
        if (!java.util.Objects.equals(this.redraw, newValue)) {
            dirty();
        }
        checkWidget();
        this.redraw = newValue;
        if (redraw) {
            if (--drawCount == 0) {
            }
        } else {
            if (drawCount++ == 0) {
            }
        }
    }

    @Override
    public boolean setTabItemFocus(boolean next) {
        if (!isShowing())
            return false;
        return forceFocus();
    }

    /**
     * Sets the base text direction (a.k.a. "paragraph direction") of the receiver,
     * which must be one of the constants <code>SWT.LEFT_TO_RIGHT</code>,
     * <code>SWT.RIGHT_TO_LEFT</code>, or <code>SWT.AUTO_TEXT_DIRECTION</code>.
     * <p>
     * <code>setOrientation</code> would override this value with the text direction
     * that is consistent with the new orientation.
     * </p>
     * <p>
     * <b>Warning</b>: This API is currently only implemented on Windows.
     * It doesn't set the base text direction on GTK and Cocoa.
     * </p>
     *
     * @param textDirection the base text direction style
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SWT#LEFT_TO_RIGHT
     * @see SWT#RIGHT_TO_LEFT
     * @see SWT#AUTO_TEXT_DIRECTION
     * @see SWT#FLIP_TEXT_DIRECTION
     *
     * @since 3.102
     */
    public void setTextDirection(int textDirection) {
        int newValue = textDirection;
        if (!java.util.Objects.equals(this.textDirection, newValue)) {
            dirty();
        }
        this.textDirection = newValue;
        checkWidget();
        textDirection &= (SWT.RIGHT_TO_LEFT | SWT.LEFT_TO_RIGHT);
        updateTextDirection(textDirection);
        if (textDirection == AUTO_TEXT_DIRECTION) {
            getApi().state |= HAS_AUTO_DIRECTION;
        } else {
            getApi().state &= ~HAS_AUTO_DIRECTION;
        }
    }

    /**
     * Sets the receiver's tool tip text to the argument, which
     * may be null indicating that the default tool tip for the
     * control will be shown. For a control that has a default
     * tool tip, such as the Tree control on Windows, setting
     * the tool tip text to an empty string replaces the default,
     * causing no tool tip text to be shown.
     * <p>
     * The mnemonic indicator (character '&amp;') is not displayed in a tool tip.
     * To display a single '&amp;' in the tool tip, the character '&amp;' can be
     * escaped by doubling it in the string.
     * </p>
     * <p>
     * NOTE: This operation is a hint and behavior is platform specific, on Windows
     * for CJK-style mnemonics of the form " (&amp;C)" at the end of the tooltip text
     * are not shown in tooltip.
     * </p>
     *
     * @param string the new tool tip text (or null)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setToolTipText(String string) {
        checkWidget();
        if (!java.util.Objects.equals(this.toolTipText, string)) {
            dirty();
        }
        if (!Objects.equals(string, toolTipText)) {
            toolTipText = string;
            setToolTipText(_getShell(), string);
        }
    }

    void setToolTipText(Shell shell, String newString) {
        dirty();
        /*
	* Feature in GTK.  In order to prevent children widgets
	* from inheriting their parent's tooltip, the tooltip is
	* a set on a shell only. In order to force the shell tooltip
	* to update when a new tip string is set, the existing string
	* in the tooltip is set to null, followed by running a query.
	* The real tip text can then be set.
	*
	* Note that this will only run if the control for which the
	* tooltip is being set is the current control (i.e. the control
	* under the pointer).
	*/
        if (((SwtDisplay) display.getImpl()).currentControl == this.getApi()) {
            setToolTipText(shell.handle, newString);
        }
    }

    /**
     * Sets whether this control should send touch events (by default controls do not).
     * Setting this to <code>false</code> causes the receiver to send gesture events
     * instead.  No exception is thrown if a touch-based input device is not
     * detected (this can be determined with <code>Display#getTouchEnabled()</code>).
     *
     * @param enabled the new touch-enabled state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    </ul>
     *
     * @see Display#getTouchEnabled
     *
     * @since 3.7
     */
    public void setTouchEnabled(boolean enabled) {
        boolean newValue = enabled;
        if (!java.util.Objects.equals(this.touchEnabled, newValue)) {
            dirty();
        }
        this.touchEnabled = newValue;
        checkWidget();
    }

    /**
     * Marks the receiver as visible if the argument is <code>true</code>,
     * and marks it invisible otherwise.
     * <p>
     * If one of the receiver's ancestors is not visible or some
     * other condition makes the receiver not visible, marking
     * it visible may not actually cause it to be displayed.
     * </p>
     *
     * @param visible the new visibility state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setVisible(boolean visible) {
        boolean newValue = visible;
        if (!java.util.Objects.equals(this.visible, newValue)) {
            dirty();
        }
        checkWidget();
        if (((getApi().state & HIDDEN) == 0) == visible)
            return;
        this.visible = newValue;
        if (visible) {
            /*
		* It is possible (but unlikely), that application
		* code could have disposed the widget in the show
		* event.  If this happens, just return.
		*/
            sendEvent(SWT.Show);
            if (isDisposed())
                return;
            getApi().state &= ~HIDDEN;
            if ((getApi().state & (ZERO_WIDTH | ZERO_HEIGHT)) == 0) {
            }
        } else {
            /*
		* Bug in GTK.  Invoking gtk_widget_hide() on a widget that has
		* focus causes a focus_out_event to be sent. If the client disposes
		* the widget inside the event, GTK GP's.  The fix is to reassign focus
		* before hiding the widget.
		*
		* NOTE: In order to stop the same widget from taking focus,
		* temporarily clear and set the GTK_VISIBLE flag.
		*/
            Control control = null;
            boolean fixFocus = false;
            if (((SwtDisplay) display.getImpl()).focusEvent != SWT.FocusOut) {
                control = display.getFocusControl();
                fixFocus = isFocusAncestor(control);
            }
            getApi().state |= HIDDEN;
            if (fixFocus) {
                fixFocus(control);
                if (isDisposed())
                    return;
            }
            if (isDisposed())
                return;
            sendEvent(SWT.Hide);
        }
        getBridge().setVisible(this, visible);
    }

    void setZOrder(Control sibling, boolean above, boolean fixRelations) {
        setZOrder(sibling, above, fixRelations, true);
    }

    void setZOrder(Control sibling, boolean above, boolean fixRelations, boolean fixChildren) {
        int index = 0, siblingIndex = 0, oldNextIndex = -1;
        Control[] children = null;
        if (fixRelations) {
            /* determine the receiver's and sibling's indexes in the parent */
            children = parent.getImpl()._getChildren();
            while (index < children.length) {
                if (children[index] == this.getApi())
                    break;
                index++;
            }
            if (sibling != null) {
                while (siblingIndex < children.length) {
                    if (children[siblingIndex] == sibling)
                        break;
                    siblingIndex++;
                }
            }
            /* remove "Labelled by" relationships that will no longer be valid */
            removeRelation();
            if (index + 1 < children.length) {
                oldNextIndex = index + 1;
                children[oldNextIndex].getImpl().removeRelation();
            }
            if (sibling != null) {
                if (above) {
                    sibling.getImpl().removeRelation();
                } else {
                    if (siblingIndex + 1 < children.length) {
                        children[siblingIndex + 1].getImpl().removeRelation();
                    }
                }
            }
        }
        long topHandle = topHandle();
        long siblingHandle = sibling != null ? sibling.getImpl().topHandle() : 0;
        if (fixChildren) {
            if (above) {
                parent.getImpl().moveAbove(topHandle, siblingHandle);
            } else {
                parent.getImpl().moveBelow(topHandle, siblingHandle);
            }
        }
        /*  Make sure that the parent internal windows are on the bottom of the stack	*/
        if (!above && fixChildren)
            parent.getImpl().fixZOrder();
        if (fixRelations) {
            /* determine the receiver's new index in the parent */
            if (sibling != null) {
                if (above) {
                    index = siblingIndex - (index < siblingIndex ? 1 : 0);
                } else {
                    index = siblingIndex + (siblingIndex < index ? 1 : 0);
                }
            } else {
                if (above) {
                    index = 0;
                } else {
                    index = children.length - 1;
                }
            }
            /* add new "Labelled by" relations as needed */
            children = parent.getImpl()._getChildren();
            if (0 < index) {
                children[index - 1].getImpl().addRelation(this.getApi());
            }
            if (index + 1 < children.length) {
                addRelation(children[index + 1]);
            }
            if (oldNextIndex != -1) {
                if (oldNextIndex <= index)
                    oldNextIndex--;
                /* the last two conditions below ensure that duplicate relations are not hooked */
                if (0 < oldNextIndex && oldNextIndex != index && oldNextIndex != index + 1) {
                    children[oldNextIndex - 1].getImpl().addRelation(children[oldNextIndex]);
                }
            }
        }
    }

    void setWidgetBackground() {
    }

    boolean showMenu(int x, int y) {
        return showMenu(x, y, SWT.MENU_MOUSE);
    }

    boolean showMenu(int x, int y, int detail) {
        Event event = new Event();
        Rectangle eventRect = new Rectangle(x, y, 0, 0);
        event.setBounds(eventRect);
        event.detail = detail;
        sendEvent(SWT.MenuDetect, event);
        //widget could be disposed at this point
        if (isDisposed())
            return false;
        if (event.doit) {
            if (menu != null && !menu.isDisposed()) {
                {
                    Rectangle rect = event.getBounds();
                    if (rect.x != x || rect.y != y) {
                        menu.setLocation(rect.x, rect.y);
                    }
                    menu.setVisible(true);
                    return true;
                }
            }
        }
        return false;
    }

    void showWidget() {
        // Comment this line to disable zero-sized widgets
        getApi().state |= ZERO_WIDTH | ZERO_HEIGHT;
        if ((getApi().state & (ZERO_WIDTH | ZERO_HEIGHT)) == 0) {
        }
        if (fixedHandle != 0)
            fixStyle(fixedHandle);
    }

    void sort(int[] items) {
        /* Shell Sort from K&R, pg 108 */
        int length = items.length;
        for (int gap = length / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < length; i++) {
                for (int j = i - gap; j >= 0; j -= gap) {
                    if (items[j] <= items[j + gap]) {
                        int swap = items[j];
                        items[j] = items[j + gap];
                        items[j + gap] = swap;
                    }
                }
            }
        }
    }

    /**
     * Based on the argument, perform one of the expected platform
     * traversal action. The argument should be one of the constants:
     * <code>SWT.TRAVERSE_ESCAPE</code>, <code>SWT.TRAVERSE_RETURN</code>,
     * <code>SWT.TRAVERSE_TAB_NEXT</code>, <code>SWT.TRAVERSE_TAB_PREVIOUS</code>,
     * <code>SWT.TRAVERSE_ARROW_NEXT</code>, <code>SWT.TRAVERSE_ARROW_PREVIOUS</code>,
     * <code>SWT.TRAVERSE_PAGE_NEXT</code> and <code>SWT.TRAVERSE_PAGE_PREVIOUS</code>.
     *
     * @param traversal the type of traversal
     * @return true if the traversal succeeded
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean traverse(int traversal) {
        checkWidget();
        Event event = new Event();
        event.doit = true;
        event.detail = traversal;
        return traverse(event);
    }

    /**
     * Performs a platform traversal action corresponding to a <code>KeyDown</code> event.
     *
     * <p>Valid traversal values are
     * <code>SWT.TRAVERSE_NONE</code>, <code>SWT.TRAVERSE_MNEMONIC</code>,
     * <code>SWT.TRAVERSE_ESCAPE</code>, <code>SWT.TRAVERSE_RETURN</code>,
     * <code>SWT.TRAVERSE_TAB_NEXT</code>, <code>SWT.TRAVERSE_TAB_PREVIOUS</code>,
     * <code>SWT.TRAVERSE_ARROW_NEXT</code>, <code>SWT.TRAVERSE_ARROW_PREVIOUS</code>,
     * <code>SWT.TRAVERSE_PAGE_NEXT</code> and <code>SWT.TRAVERSE_PAGE_PREVIOUS</code>.
     * If <code>traversal</code> is <code>SWT.TRAVERSE_NONE</code> then the Traverse
     * event is created with standard values based on the KeyDown event.  If
     * <code>traversal</code> is one of the other traversal constants then the Traverse
     * event is created with this detail, and its <code>doit</code> is taken from the
     * KeyDown event.
     * </p>
     *
     * @param traversal the type of traversal, or <code>SWT.TRAVERSE_NONE</code> to compute
     * this from <code>event</code>
     * @param event the KeyDown event
     *
     * @return <code>true</code> if the traversal succeeded
     *
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_NULL_ARGUMENT if the event is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.6
     */
    public boolean traverse(int traversal, Event event) {
        checkWidget();
        if (event == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        return traverse(traversal, event.character, event.keyCode, event.keyLocation, event.stateMask, event.doit);
    }

    /**
     * Performs a platform traversal action corresponding to a <code>KeyDown</code> event.
     *
     * <p>Valid traversal values are
     * <code>SWT.TRAVERSE_NONE</code>, <code>SWT.TRAVERSE_MNEMONIC</code>,
     * <code>SWT.TRAVERSE_ESCAPE</code>, <code>SWT.TRAVERSE_RETURN</code>,
     * <code>SWT.TRAVERSE_TAB_NEXT</code>, <code>SWT.TRAVERSE_TAB_PREVIOUS</code>,
     * <code>SWT.TRAVERSE_ARROW_NEXT</code>, <code>SWT.TRAVERSE_ARROW_PREVIOUS</code>,
     * <code>SWT.TRAVERSE_PAGE_NEXT</code> and <code>SWT.TRAVERSE_PAGE_PREVIOUS</code>.
     * If <code>traversal</code> is <code>SWT.TRAVERSE_NONE</code> then the Traverse
     * event is created with standard values based on the KeyDown event.  If
     * <code>traversal</code> is one of the other traversal constants then the Traverse
     * event is created with this detail, and its <code>doit</code> is taken from the
     * KeyDown event.
     * </p>
     *
     * @param traversal the type of traversal, or <code>SWT.TRAVERSE_NONE</code> to compute
     * this from <code>event</code>
     * @param event the KeyDown event
     *
     * @return <code>true</code> if the traversal succeeded
     *
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_NULL_ARGUMENT if the event is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.6
     */
    public boolean traverse(int traversal, KeyEvent event) {
        checkWidget();
        if (event == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        return traverse(traversal, event.character, event.keyCode, event.keyLocation, event.stateMask, event.doit);
    }

    public boolean traverse(int traversal, char character, int keyCode, int keyLocation, int stateMask, boolean doit) {
        if (traversal == SWT.TRAVERSE_NONE) {
            switch(keyCode) {
                case SWT.ESC:
                    {
                        traversal = SWT.TRAVERSE_ESCAPE;
                        doit = true;
                        break;
                    }
                case SWT.CR:
                    {
                        traversal = SWT.TRAVERSE_RETURN;
                        doit = true;
                        break;
                    }
                case SWT.ARROW_DOWN:
                case SWT.ARROW_RIGHT:
                    {
                        traversal = SWT.TRAVERSE_ARROW_NEXT;
                        doit = false;
                        break;
                    }
                case SWT.ARROW_UP:
                case SWT.ARROW_LEFT:
                    {
                        traversal = SWT.TRAVERSE_ARROW_PREVIOUS;
                        doit = false;
                        break;
                    }
                case SWT.TAB:
                    {
                        traversal = (stateMask & SWT.SHIFT) != 0 ? SWT.TRAVERSE_TAB_PREVIOUS : SWT.TRAVERSE_TAB_NEXT;
                        doit = true;
                        break;
                    }
                case SWT.PAGE_DOWN:
                    {
                        if ((stateMask & SWT.CTRL) != 0) {
                            traversal = SWT.TRAVERSE_PAGE_NEXT;
                            doit = true;
                        }
                        break;
                    }
                case SWT.PAGE_UP:
                    {
                        if ((stateMask & SWT.CTRL) != 0) {
                            traversal = SWT.TRAVERSE_PAGE_PREVIOUS;
                            doit = true;
                        }
                        break;
                    }
                default:
                    {
                        if (character != 0 && (stateMask & (SWT.ALT | SWT.CTRL)) == SWT.ALT) {
                            traversal = SWT.TRAVERSE_MNEMONIC;
                            doit = true;
                        }
                        break;
                    }
            }
        }
        Event event = new Event();
        event.character = character;
        event.detail = traversal;
        event.doit = doit;
        event.keyCode = keyCode;
        event.keyLocation = keyLocation;
        event.stateMask = stateMask;
        Shell shell = getShell();
        boolean all = false;
        switch(traversal) {
            case SWT.TRAVERSE_ESCAPE:
            case SWT.TRAVERSE_RETURN:
            case SWT.TRAVERSE_PAGE_NEXT:
            case SWT.TRAVERSE_PAGE_PREVIOUS:
                {
                    all = true;
                    // FALL THROUGH
                }
            case SWT.TRAVERSE_ARROW_NEXT:
            case SWT.TRAVERSE_ARROW_PREVIOUS:
            case SWT.TRAVERSE_TAB_NEXT:
            case SWT.TRAVERSE_TAB_PREVIOUS:
                {
                    /* traversal is a valid traversal action */
                    break;
                }
            case SWT.TRAVERSE_MNEMONIC:
                {
                    return translateMnemonic(event, null) || shell.getImpl().translateMnemonic(event, this.getApi());
                }
            default:
                {
                    /* traversal is not a valid traversal action */
                    return false;
                }
        }
        Control control = this.getApi();
        do {
            if (control.getImpl().traverse(event))
                return true;
            if (!event.doit && ((DartWidget) control.getImpl()).hooks(SWT.Traverse))
                return false;
            if (control == shell)
                return false;
            control = control.getImpl()._parent();
        } while (all && control != null);
        return false;
    }

    public boolean translateMnemonic(Event event, Control control) {
        if (control == this.getApi())
            return false;
        if (!isVisible() || !isEnabled())
            return false;
        event.doit = this.getApi() == ((SwtDisplay) display.getImpl()).mnemonicControl || mnemonicMatch(event.character);
        return traverse(event);
    }

    public boolean translateMnemonic(int keyval, long event) {
        int[] state = new int[1];
        if (state[0] == 0) {
            int code = traversalCode(keyval, event);
            if ((code & SWT.TRAVERSE_MNEMONIC) == 0)
                return false;
        } else {
        }
        Decorations shell = menuShell();
        if (shell.isVisible() && shell.isEnabled()) {
            Event javaEvent = new Event();
            javaEvent.detail = SWT.TRAVERSE_MNEMONIC;
            if (setKeyState(javaEvent, event)) {
                return translateMnemonic(javaEvent, null) || shell.getImpl().translateMnemonic(javaEvent, this.getApi());
            }
        }
        return false;
    }

    boolean translateTraversal(long event) {
        int detail = SWT.TRAVERSE_NONE;
        int[] eventKeyval = new int[1];
        int key = eventKeyval[0];
        int code = traversalCode(key, event);
        boolean all = false;
        Event javaEvent = new Event();
        javaEvent.doit = (code & detail) != 0;
        javaEvent.detail = detail;
        if (!setKeyState(javaEvent, event))
            return false;
        Shell shell = getShell();
        Control control = this.getApi();
        do {
            if (control.getImpl().traverse(javaEvent))
                return true;
            if (!javaEvent.doit && ((DartWidget) control.getImpl()).hooks(SWT.Traverse))
                return false;
            if (control == shell)
                return false;
            control = control.getImpl()._parent();
        } while (all && control != null);
        return false;
    }

    int traversalCode(int key, Object event) {
        int code = SWT.TRAVERSE_RETURN | SWT.TRAVERSE_TAB_NEXT | SWT.TRAVERSE_TAB_PREVIOUS | SWT.TRAVERSE_PAGE_NEXT | SWT.TRAVERSE_PAGE_PREVIOUS;
        Shell shell = getShell();
        if (shell.getImpl()._parent() != null)
            code |= SWT.TRAVERSE_ESCAPE;
        return code;
    }

    public boolean traverse(Event event) {
        /*
	* It is possible (but unlikely), that application
	* code could have disposed the widget in the traverse
	* event.  If this happens, return true to stop further
	* event processing.
	*/
        sendEvent(SWT.Traverse, event);
        if (isDisposed())
            return true;
        if (!event.doit)
            return false;
        switch(event.detail) {
            case SWT.TRAVERSE_NONE:
                return true;
            case SWT.TRAVERSE_ESCAPE:
                return traverseEscape();
            case SWT.TRAVERSE_RETURN:
                return traverseReturn();
            case SWT.TRAVERSE_TAB_NEXT:
                return traverseGroup(true);
            case SWT.TRAVERSE_TAB_PREVIOUS:
                return traverseGroup(false);
            case SWT.TRAVERSE_ARROW_NEXT:
                return traverseItem(true);
            case SWT.TRAVERSE_ARROW_PREVIOUS:
                return traverseItem(false);
            case SWT.TRAVERSE_MNEMONIC:
                return traverseMnemonic(event.character);
            case SWT.TRAVERSE_PAGE_NEXT:
                return traversePage(true);
            case SWT.TRAVERSE_PAGE_PREVIOUS:
                return traversePage(false);
        }
        return false;
    }

    boolean traverseEscape() {
        return false;
    }

    boolean traverseGroup(boolean next) {
        Control root = computeTabRoot();
        Widget group = computeTabGroup();
        Widget[] list = root.getImpl().computeTabList();
        int length = list.length;
        int index = 0;
        while (index < length) {
            if (list[index] == group)
                break;
            index++;
        }
        /*
	* It is possible (but unlikely), that application
	* code could have disposed the widget in focus in
	* or out events.  Ensure that a disposed widget is
	* not accessed.
	*/
        if (index == length)
            return false;
        int start = index, offset = (next) ? 1 : -1;
        while ((index = ((index + offset + length) % length)) != start) {
            Widget widget = list[index];
            if (!widget.isDisposed() && widget.getImpl().setTabGroupFocus(next)) {
                return true;
            }
        }
        if (group.isDisposed())
            return false;
        return group.getImpl().setTabGroupFocus(next);
    }

    boolean traverseItem(boolean next) {
        Control[] children = parent.getImpl()._getChildren();
        int length = children.length;
        int index = 0;
        while (index < length) {
            if (children[index] == this.getApi())
                break;
            index++;
        }
        /*
	* It is possible (but unlikely), that application
	* code could have disposed the widget in focus in
	* or out events.  Ensure that a disposed widget is
	* not accessed.
	*/
        if (index == length)
            return false;
        int start = index, offset = (next) ? 1 : -1;
        while ((index = (index + offset + length) % length) != start) {
            Control child = children[index];
            if (!child.isDisposed() && child.getImpl().isTabItem()) {
                if (child.getImpl().setTabItemFocus(next))
                    return true;
            }
        }
        return false;
    }

    boolean traverseReturn() {
        return false;
    }

    boolean traversePage(boolean next) {
        return false;
    }

    boolean traverseMnemonic(char key) {
        return mnemonicHit(key);
    }

    /**
     * Forces all outstanding paint requests for the widget
     * to be processed before this method returns. If there
     * are no outstanding paint request, this method does
     * nothing.
     * <p>Note:</p>
     * <ul>
     * <li>This method does not cause a redraw.</li>
     * <li>Some OS versions forcefully perform automatic deferred painting.
     * This method does nothing in that case.</li>
     * </ul>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #redraw()
     * @see #redraw(int, int, int, int, boolean)
     * @see PaintListener
     * @see SWT#Paint
     */
    public void update() {
        checkWidget();
        update(false, true);
    }

    void update(boolean all, boolean flush) {
        long window = paintWindow();
        if (flush)
            ((SwtDisplay) display.getImpl()).flushExposes(window, all);
    }

    public void updateBackgroundMode() {
        int oldState = getApi().state & PARENT_BACKGROUND;
        checkBackground();
        if (oldState != (getApi().state & PARENT_BACKGROUND)) {
            setBackground();
        }
    }

    public void updateLayout(boolean all) {
        /* Do nothing */
    }

    /**
     * Gets the position of the top left corner of the control in root window (display) coordinates.
     * GTK3 only, do not call on GTK4.
     *
     * @return the origin
     */
    public Point getWindowOrigin() {
        return getBridge().getWindowOrigin(this);
    }

    /**
     * Gets the position of the top left corner of the control in root window (display) coordinates.
     * GTK4 only, do not call on GTK3.
     *
     * @return the origin
     */
    public Point getSurfaceOrigin() {
        return getBridge().getWindowOrigin(this);
    }

    Color background;

    Rectangle bounds = new Rectangle(0, 0, 0, 0);

    boolean capture;

    boolean dragDetect;

    boolean enabled = true;

    Color foreground = new Color(0, 0, 0);

    boolean redraw;

    int textDirection;

    boolean touchEnabled;

    boolean visible = true;

    public long _fixedHandle() {
        return fixedHandle;
    }

    public long _firstFixedHandle() {
        return firstFixedHandle;
    }

    public long _keyController() {
        return keyController;
    }

    public long _redrawWindow() {
        return redrawWindow;
    }

    public long _enableWindow() {
        return enableWindow;
    }

    public long _provider() {
        return provider;
    }

    public int _drawCount() {
        return drawCount;
    }

    public int _backgroundAlpha() {
        return backgroundAlpha;
    }

    public long _dragGesture() {
        return dragGesture;
    }

    public long _zoomGesture() {
        return zoomGesture;
    }

    public long _rotateGesture() {
        return rotateGesture;
    }

    public long _panGesture() {
        return panGesture;
    }

    public Composite _parent() {
        return parent;
    }

    public Cursor _cursor() {
        return cursor;
    }

    public Menu _menu() {
        return menu;
    }

    public Image _backgroundImage() {
        return backgroundImage;
    }

    public Font _font() {
        return font;
    }

    public Region _region() {
        return region;
    }

    public long _eventRegion() {
        return eventRegion;
    }

    public long _regionHandle() {
        return regionHandle;
    }

    public String _toolTipText() {
        return toolTipText;
    }

    public Object _layoutData() {
        return layoutData;
    }

    public Accessible _accessible() {
        return accessible;
    }

    public Control _labelRelation() {
        return labelRelation;
    }

    public String _cssBackground() {
        return cssBackground;
    }

    public String _cssForeground() {
        return cssForeground;
    }

    public boolean _drawRegion() {
        return drawRegion;
    }

    public boolean _cachedNoBackground() {
        return cachedNoBackground;
    }

    public Point _lastInput() {
        return lastInput;
    }

    public LinkedList<Event> _dragDetectionQueue() {
        return dragDetectionQueue;
    }

    public boolean _checkScaleFactor() {
        return checkScaleFactor;
    }

    public boolean _autoScale() {
        return autoScale;
    }

    public Color _background() {
        return background;
    }

    public Rectangle _bounds() {
        return bounds;
    }

    public boolean _capture() {
        return capture;
    }

    public boolean _dragDetect() {
        return dragDetect;
    }

    public boolean _enabled() {
        return enabled;
    }

    public Color _foreground() {
        return foreground;
    }

    public boolean _redraw() {
        return redraw;
    }

    public int _textDirection() {
        return textDirection;
    }

    public boolean _touchEnabled() {
        return touchEnabled;
    }

    public boolean _visible() {
        return visible;
    }

    int resolveTextDirection() {
        /*
                     * For generic Controls do nothing here. Text-enabled Controls will resolve
                     * AUTO text direction according to their text content.
                     */
        return SWT.NONE;
    }

    boolean updateTextDirection(int textDirection) {
        if (textDirection == AUTO_TEXT_DIRECTION) {
            textDirection = resolveTextDirection();
            getApi().state |= HAS_AUTO_DIRECTION;
        } else {
            getApi().state &= ~HAS_AUTO_DIRECTION;
        }
        if (textDirection == 0)
            return false;
        return true;
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
        FlutterBridge.on(this, "Control", "Move", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Move, e);
            });
        });
        FlutterBridge.on(this, "Control", "Resize", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Resize, e);
            });
        });
        FlutterBridge.on(this, "DragDetect", "DragDetect", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.DragDetect, e);
            });
        });
        FlutterBridge.on(this, "Focus", "FocusIn", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.FocusIn, e);
            });
        });
        FlutterBridge.on(this, "Focus", "FocusOut", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.FocusOut, e);
            });
        });
        FlutterBridge.on(this, "Gesture", "Gesture", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Gesture, e);
            });
        });
        FlutterBridge.on(this, "Help", "Help", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Help, e);
            });
        });
        FlutterBridge.on(this, "Key", "KeyDown", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.KeyDown, e);
            });
        });
        FlutterBridge.on(this, "Key", "KeyUp", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.KeyUp, e);
            });
        });
        FlutterBridge.on(this, "MenuDetect", "MenuDetect", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.MenuDetect, e);
            });
        });
        FlutterBridge.on(this, "Mouse", "MouseDoubleClick", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.MouseDoubleClick, e);
            });
        });
        FlutterBridge.on(this, "Mouse", "MouseDown", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.MouseDown, e);
            });
        });
        FlutterBridge.on(this, "Mouse", "MouseUp", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.MouseUp, e);
            });
        });
        FlutterBridge.on(this, "MouseMove", "MouseMove", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.MouseMove, e);
            });
        });
        FlutterBridge.on(this, "MouseTrack", "MouseEnter", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.MouseEnter, e);
            });
        });
        FlutterBridge.on(this, "MouseTrack", "MouseExit", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.MouseExit, e);
            });
        });
        FlutterBridge.on(this, "MouseTrack", "MouseHover", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.MouseHover, e);
            });
        });
        FlutterBridge.on(this, "MouseWheel", "MouseWheel", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.MouseWheel, e);
            });
        });
        FlutterBridge.on(this, "Paint", "Paint", e -> {
            getDisplay().asyncExec(() -> {
                ControlHelper.paint(this, e);
            });
        });
        FlutterBridge.on(this, "Touch", "Touch", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Touch, e);
            });
        });
        FlutterBridge.on(this, "Traverse", "Traverse", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Traverse, e);
            });
        });
    }

    public Control getApi() {
        return (Control) api;
    }

    public VControl getValue() {
        if (value == null)
            value = new VControl(this);
        return (VControl) value;
    }
}
