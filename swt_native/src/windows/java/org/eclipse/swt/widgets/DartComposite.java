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
package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import java.util.Objects;
import dev.equo.swt.*;

/**
 * Instances of this class are controls which are capable
 * of containing other controls.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>NO_BACKGROUND, NO_FOCUS, NO_MERGE_PAINTS, NO_REDRAW_RESIZE, NO_RADIO_GROUP, EMBEDDED, DOUBLE_BUFFERED</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * Note: The <code>NO_BACKGROUND</code>, <code>NO_FOCUS</code>, <code>NO_MERGE_PAINTS</code>,
 * and <code>NO_REDRAW_RESIZE</code> styles are intended for use with <code>Canvas</code>.
 * They can be used with <code>Composite</code> if you are drawing your own, but their
 * behavior is undefined if they are used with subclasses of <code>Composite</code> other
 * than <code>Canvas</code>.
 * </p><p>
 * Note: The <code>CENTER</code> style, although undefined for composites, has the
 * same value as <code>EMBEDDED</code> which is used to embed widgets from other
 * widget toolkits into SWT.  On some operating systems (GTK), this may cause
 * the children of this composite to be obscured.
 * </p><p>
 * This class may be subclassed by custom control implementors
 * who are building controls that are constructed from aggregates
 * of other controls.
 * </p>
 *
 * @see Canvas
 * @see <a href="http://www.eclipse.org/swt/snippets/#composite">Composite snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public class DartComposite extends DartScrollable implements IComposite {

    Layout layout;

    Control[] tabList;

    int layoutCount, backgroundMode;

    static final int TOOLTIP_LIMIT = 4096;

    /**
     * Prevents uninitialized instances from being created outside the package.
     */
    DartComposite(Composite api) {
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
     * @see SWT#NO_BACKGROUND
     * @see SWT#NO_FOCUS
     * @see SWT#NO_MERGE_PAINTS
     * @see SWT#NO_REDRAW_RESIZE
     * @see SWT#NO_RADIO_GROUP
     * @see SWT#EMBEDDED
     * @see SWT#DOUBLE_BUFFERED
     * @see Widget#getStyle
     */
    public DartComposite(Composite parent, int style, Composite api) {
        super(parent, style, api);
    }

    public Control[] _getChildren() {
        if (children == null)
            return new Control[0];
        java.util.ArrayList<Control> validChildren = new java.util.ArrayList<Control>();
        for (int i = 0; i < children.length; i++) {
            if (children[i] != null && !children[i].isDisposed()) {
                validChildren.add(children[i]);
            }
        }
        return validChildren.toArray(new Control[validChildren.size()]);
    }

    public Control[] _getTabList() {
        if (tabList == null)
            return tabList;
        int count = 0;
        for (Control element : tabList) {
            if (!element.isDisposed())
                count++;
        }
        if (count == tabList.length)
            return tabList;
        Control[] newList = new Control[count];
        int index = 0;
        for (Control element : tabList) {
            if (!element.isDisposed()) {
                newList[index++] = element;
            }
        }
        tabList = newList;
        return tabList;
    }

    /**
     * Clears any data that has been cached by a Layout for all widgets that
     * are in the parent hierarchy of the changed control up to and including the
     * receiver.  If an ancestor does not have a layout, it is skipped.
     *
     * @param changed an array of controls that changed state and require a recalculation of size
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the changed array is null any of its controls are null or have been disposed</li>
     *    <li>ERROR_INVALID_PARENT - if any control in changed is not in the widget tree of the receiver</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @deprecated use {@link Composite#layout(Control[], int)} instead
     * @since 3.1
     */
    @Deprecated
    public void changed(Control[] changed) {
        layout(changed, SWT.DEFER);
    }

    @Override
    void checkBuffered() {
        if ((getApi().state & CANVAS) == 0) {
            super.checkBuffered();
        }
    }

    @Override
    void checkComposited() {
        if ((getApi().state & CANVAS) != 0) {
            if ((getApi().style & SWT.TRANSPARENT) != 0) {
            }
        }
    }

    @Override
    public void checkSubclass() {
        /* Do nothing - Subclassing is allowed */
    }

    @Override
    public Widget[] computeTabList() {
        Widget[] result = super.computeTabList();
        if (result.length == 0)
            return result;
        Control[] list = tabList != null ? _getTabList() : _getChildren();
        for (Control child : list) {
            Widget[] childList = child.getImpl().computeTabList();
            if (childList.length != 0) {
                Widget[] newResult = new Widget[result.length + childList.length];
                System.arraycopy(result, 0, newResult, 0, result.length);
                System.arraycopy(childList, 0, newResult, result.length, childList.length);
                result = newResult;
            }
        }
        return result;
    }

    @Override
    Point computeSizeInPixels(int wHint, int hHint, boolean changed) {
        ((SwtDisplay) display.getImpl()).runSkin();
        if (layout != null) {
            if (wHint == SWT.DEFAULT || hHint == SWT.DEFAULT) {
                changed |= (getApi().state & LAYOUT_CHANGED) != 0;
                getApi().state &= ~LAYOUT_CHANGED;
            } else {
            }
        } else {
        }
        return null;
    }

    /**
     * Copies a rectangular area of the receiver at the specified
     * position using the gc.
     *
     * @param gc the gc where the rectangle is to be filled
     * @param x the x coordinate of the rectangle to be filled
     * @param y the y coordinate of the rectangle to be filled
     * @param width the width of the rectangle to be filled
     * @param height the height of the rectangle to be filled
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the gc is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the gc has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    /*public*/
    void copyArea(GC gc, int x, int y, int width, int height) {
        checkWidget();
        if (gc == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (gc.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        //XP only, no GDI+
        //#define PW_CLIENTONLY 0x00000001
        //DCOrg() wrong
    }

    @Override
    void createHandle() {
    }

    @Override
    int applyThemeBackground() {
        /*
	 * Composite with scrollbars would not inherit the theme because it was
	 * probably being used to implement a control similar to a Text, List,
	 * Table, or Tree, and those controls do not inherit the background theme.
	 * We assume that a Composite that did not have scrollbars was probably just
	 * being used to group some other controls, therefore it should inherit.
	 *
	 * But when Composite background is set to COLOR_TRANSPARENT (i.e.
	 * backgroundAlpha as '0') which means parent theme should be inherited, so
	 * enable the THEME_BACKGROUND in 'state' to support background transparent.
	 * Refer bug 463127 & related bug 234649.
	 */
        return (backgroundAlpha == 0 || (getApi().style & (SWT.H_SCROLL | SWT.V_SCROLL)) == 0 || findThemeControl() == parent) ? 1 : 0;
    }

    /**
     * Fills the interior of the rectangle specified by the arguments,
     * with the receiver's background.
     *
     * <p>The <code>offsetX</code> and <code>offsetY</code> are used to map from
     * the <code>gc</code> origin to the origin of the parent image background. This is useful
     * to ensure proper alignment of the image background.</p>
     *
     * @param gc the gc where the rectangle is to be filled
     * @param x the x coordinate of the rectangle to be filled
     * @param y the y coordinate of the rectangle to be filled
     * @param width the width of the rectangle to be filled
     * @param height the height of the rectangle to be filled
     * @param offsetX the image background x offset
     * @param offsetY the image background y offset
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
     * @since 3.6
     */
    public void drawBackground(GC gc, int x, int y, int width, int height, int offsetX, int offsetY) {
        checkWidget();
    }

    void drawBackgroundInPixels(GC gc, int x, int y, int width, int height, int offsetX, int offsetY) {
        if (gc == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (gc.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
    }

    public Composite findDeferredControl() {
        return layoutCount > 0 ? this.getApi() : parent.getImpl().findDeferredControl();
    }

    @Override
    public Menu[] findMenus(Control control) {
        if (control == this.getApi())
            return new Menu[0];
        Menu[] result = super.findMenus(control);
        for (Control child : _getChildren()) {
            Menu[] menuList = child.getImpl().findMenus(control);
            if (menuList.length != 0) {
                Menu[] newResult = new Menu[result.length + menuList.length];
                System.arraycopy(result, 0, newResult, 0, result.length);
                System.arraycopy(menuList, 0, newResult, result.length, menuList.length);
                result = newResult;
            }
        }
        return result;
    }

    @Override
    public void fixChildren(Shell newShell, Shell oldShell, Decorations newDecorations, Decorations oldDecorations, Menu[] menus) {
        super.fixChildren(newShell, oldShell, newDecorations, oldDecorations, menus);
        for (Control child : _getChildren()) {
            child.getImpl().fixChildren(newShell, oldShell, newDecorations, oldDecorations, menus);
        }
    }

    void fixTabList(Control control) {
        if (tabList == null)
            return;
        int count = 0;
        for (Control element : tabList) {
            if (element == control)
                count++;
        }
        if (count == 0)
            return;
        Control[] newList = null;
        int length = tabList.length - count;
        if (length != 0) {
            newList = new Control[length];
            int index = 0;
            for (Control element : tabList) {
                if (element != control) {
                    newList[index++] = element;
                }
            }
        }
        tabList = newList;
    }

    /**
     * Returns the receiver's background drawing mode. This
     * will be one of the following constants defined in class
     * <code>SWT</code>:
     * <code>INHERIT_NONE</code>, <code>INHERIT_DEFAULT</code>,
     * <code>INHERIT_FORCE</code>.
     *
     * @return the background mode
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SWT
     *
     * @since 3.2
     */
    public int getBackgroundMode() {
        checkWidget();
        return backgroundMode;
    }

    /**
     * Returns a (possibly empty) array containing the receiver's children.
     * Children are returned in the order that they are drawn.  The topmost
     * control appears at the beginning of the array.  Subsequent controls
     * draw beneath this control and appear later in the array.
     * <p>
     * Note: This is not the actual structure used by the receiver
     * to maintain its list of children, so modifying the array will
     * not affect the receiver.
     * </p>
     *
     * @return an array of children
     *
     * @see Control#moveAbove
     * @see Control#moveBelow
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Control[] getChildren() {
        checkWidget();
        Control[] allChildren = _getChildren();
        return java.util.Arrays.stream(allChildren).filter(child -> child != null && !child.isDisposed()).toArray(Control[]::new);
    }

    int getChildrenCount() {
        /*
	* NOTE: The current implementation will count
	* non-registered children.
	*/
        int count = 0;
        return count;
    }

    /**
     * Returns layout which is associated with the receiver, or
     * null if one has not been set.
     *
     * @return the receiver's layout or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Layout getLayout() {
        checkWidget();
        return layout;
    }

    /**
     * Gets the (possibly empty) tabbing order for the control.
     *
     * @return tabList the ordered list of controls representing the tab order
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setTabList
     */
    public Control[] getTabList() {
        checkWidget();
        Control[] tabList = _getTabList();
        if (tabList == null) {
            int count = 0;
            Control[] list = _getChildren();
            for (Control element : list) {
                if (element.getImpl().isTabGroup())
                    count++;
            }
            tabList = new Control[count];
            int index = 0;
            for (Control element : list) {
                if (element.getImpl().isTabGroup()) {
                    tabList[index++] = element;
                }
            }
        }
        return tabList;
    }

    boolean hooksKeys() {
        return hooks(SWT.KeyDown) || hooks(SWT.KeyUp);
    }

    /**
     * Returns <code>true</code> if the receiver has deferred
     * the performing of layout, and <code>false</code> otherwise.
     *
     * @return the receiver's deferred layout state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setLayoutDeferred(boolean)
     * @see #isLayoutDeferred()
     *
     * @since 3.1
     */
    public boolean getLayoutDeferred() {
        checkWidget();
        return layoutCount > 0;
    }

    /**
     * Returns <code>true</code> if the receiver or any ancestor
     * up to and including the receiver's nearest ancestor shell
     * has deferred the performing of layouts.  Otherwise, <code>false</code>
     * is returned.
     *
     * @return the receiver's deferred layout state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setLayoutDeferred(boolean)
     * @see #getLayoutDeferred()
     *
     * @since 3.1
     */
    public boolean isLayoutDeferred() {
        checkWidget();
        return findDeferredControl() != null;
    }

    /**
     * If the receiver has a layout, asks the layout to <em>lay out</em>
     * (that is, set the size and location of) the receiver's children.
     * If the receiver does not have a layout, do nothing.
     * <p>
     * Use of this method is discouraged since it is the least-efficient
     * way to trigger a layout. The use of <code>layout(true)</code>
     * discards all cached layout information, even from controls which
     * have not changed. It is much more efficient to invoke
     * {@link Control#requestLayout()} on every control which has changed
     * in the layout than it is to invoke this method on the layout itself.
     * </p>
     * <p>
     * This is equivalent to calling <code>layout(true)</code>.
     * </p>
     * <p>
     * Note: Layout is different from painting. If a child is
     * moved or resized such that an area in the parent is
     * exposed, then the parent will paint. If no child is
     * affected, the parent will not paint.
     * </p>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void layout() {
        checkWidget();
        layout(true);
    }

    /**
     * If the receiver has a layout, asks the layout to <em>lay out</em>
     * (that is, set the size and location of) the receiver's children.
     * If the argument is <code>true</code> the layout must not rely
     * on any information it has cached about the immediate children. If it
     * is <code>false</code> the layout may (potentially) optimize the
     * work it is doing by assuming that none of the receiver's
     * children has changed state since the last layout.
     * If the receiver does not have a layout, do nothing.
     * <p>
     * It is normally more efficient to invoke {@link Control#requestLayout()}
     * on every control which has changed in the layout than it is to invoke
     * this method on the layout itself. Clients are encouraged to use
     * {@link Control#requestLayout()} where possible instead of calling
     * this method.
     * </p>
     * <p>
     * If a child is resized as a result of a call to layout, the
     * resize event will invoke the layout of the child.  The layout
     * will cascade down through all child widgets in the receiver's widget
     * tree until a child is encountered that does not resize.  Note that
     * a layout due to a resize will not flush any cached information
     * (same as <code>layout(false)</code>).
     * </p>
     * <p>
     * Note: Layout is different from painting. If a child is
     * moved or resized such that an area in the parent is
     * exposed, then the parent will paint. If no child is
     * affected, the parent will not paint.
     * </p>
     *
     * @param changed <code>true</code> if the layout must flush its caches, and <code>false</code> otherwise
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void layout(boolean changed) {
        checkWidget();
        if (layout == null)
            return;
        layout(changed, false);
    }

    /**
     * If the receiver has a layout, asks the layout to <em>lay out</em>
     * (that is, set the size and location of) the receiver's children.
     * If the changed argument is <code>true</code> the layout must not rely
     * on any information it has cached about its children. If it
     * is <code>false</code> the layout may (potentially) optimize the
     * work it is doing by assuming that none of the receiver's
     * children has changed state since the last layout.
     * If the all argument is <code>true</code> the layout will cascade down
     * through all child widgets in the receiver's widget tree, regardless of
     * whether the child has changed size.  The changed argument is applied to
     * all layouts.  If the all argument is <code>false</code>, the layout will
     * <em>not</em> cascade down through all child widgets in the receiver's widget
     * tree.  However, if a child is resized as a result of a call to layout, the
     * resize event will invoke the layout of the child.  Note that
     * a layout due to a resize will not flush any cached information
     * (same as <code>layout(false)</code>).
     * <p>
     * It is normally more efficient to invoke {@link Control#requestLayout()}
     * on every control which has changed in the layout than it is to invoke
     * this method on the layout itself. Clients are encouraged to use
     * {@link Control#requestLayout()} where possible instead of calling
     * this method.
     * </p>
     * <p>
     * Note: Layout is different from painting. If a child is
     * moved or resized such that an area in the parent is
     * exposed, then the parent will paint. If no child is
     * affected, the parent will not paint.
     * </p>
     *
     * @param changed <code>true</code> if the layout must flush its caches, and <code>false</code> otherwise
     * @param all <code>true</code> if all children in the receiver's widget tree should be laid out, and <code>false</code> otherwise
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    public void layout(boolean changed, boolean all) {
        checkWidget();
        if (layout == null && !all)
            return;
        markLayout(changed, all);
        updateLayout(all);
    }

    /**
     * Forces a lay out (that is, sets the size and location) of all widgets that
     * are in the parent hierarchy of the changed control up to and including the
     * receiver.  The layouts in the hierarchy must not rely on any information
     * cached about the changed control or any of its ancestors.  The layout may
     * (potentially) optimize the work it is doing by assuming that none of the
     * peers of the changed control have changed state since the last layout.
     * If an ancestor does not have a layout, skip it.
     * <p>
     * It is normally more efficient to invoke {@link Control#requestLayout()}
     * on every control which has changed in the layout than it is to invoke
     * this method on the layout itself. Clients are encouraged to use
     * {@link Control#requestLayout()} where possible instead of calling
     * this method.
     * </p>
     * <p>
     * Note: Layout is different from painting. If a child is
     * moved or resized such that an area in the parent is
     * exposed, then the parent will paint. If no child is
     * affected, the parent will not paint.
     * </p>
     *
     * @param changed a control that has had a state change which requires a recalculation of its size
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the changed array is null any of its controls are null or have been disposed</li>
     *    <li>ERROR_INVALID_PARENT - if any control in changed is not in the widget tree of the receiver</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    public void layout(Control[] changed) {
        checkWidget();
        if (changed == null)
            error(SWT.ERROR_INVALID_ARGUMENT);
        layout(changed, SWT.NONE);
    }

    /**
     * Forces a lay out (that is, sets the size and location) of all widgets that
     * are in the parent hierarchy of the changed control up to and including the
     * receiver.
     * <p>
     * The parameter <code>flags</code> may be a combination of:
     * </p>
     * <dl>
     * <dt><b>SWT.ALL</b></dt>
     * <dd>all children in the receiver's widget tree should be laid out</dd>
     * <dt><b>SWT.CHANGED</b></dt>
     * <dd>the layout must flush its caches</dd>
     * <dt><b>SWT.DEFER</b></dt>
     * <dd>layout will be deferred</dd>
     * </dl>
     * <p>
     * When the <code>changed</code> array is specified, the flags <code>SWT.ALL</code>
     * and <code>SWT.CHANGED</code> have no effect. In this case, the layouts in the
     * hierarchy must not rely on any information cached about the changed control or
     * any of its ancestors.  The layout may (potentially) optimize the
     * work it is doing by assuming that none of the peers of the changed
     * control have changed state since the last layout.
     * If an ancestor does not have a layout, skip it.
     * </p>
     * <p>
     * When the <code>changed</code> array is not specified, the flag <code>SWT.ALL</code>
     * indicates that the whole widget tree should be laid out. And the flag
     * <code>SWT.CHANGED</code> indicates that the layouts should flush any cached
     * information for all controls that are laid out.
     * </p>
     * <p>
     * The <code>SWT.DEFER</code> flag always causes the layout to be deferred by
     * calling <code>Composite.setLayoutDeferred(true)</code> and scheduling a call
     * to <code>Composite.setLayoutDeferred(false)</code>, which will happen when
     * appropriate (usually before the next event is handled). When this flag is set,
     * the application should not call <code>Composite.setLayoutDeferred(boolean)</code>.
     * </p>
     * <p>
     * Note: Layout is different from painting. If a child is
     * moved or resized such that an area in the parent is
     * exposed, then the parent will paint. If no child is
     * affected, the parent will not paint.
     * </p>
     *
     * @param changed a control that has had a state change which requires a recalculation of its size
     * @param flags the flags specifying how the layout should happen
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if any of the controls in changed is null or has been disposed</li>
     *    <li>ERROR_INVALID_PARENT - if any control in changed is not in the widget tree of the receiver</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.6
     */
    public void layout(Control[] changed, int flags) {
        checkWidget();
        if (changed != null) {
            for (Control control : changed) {
                if (control == null)
                    error(SWT.ERROR_INVALID_ARGUMENT);
                if (control.isDisposed())
                    error(SWT.ERROR_INVALID_ARGUMENT);
                boolean ancestor = false;
                Composite composite = control.getImpl()._parent();
                while (composite != null) {
                    ancestor = composite == this.getApi();
                    if (ancestor)
                        break;
                    composite = composite.getImpl()._parent();
                }
                if (!ancestor)
                    error(SWT.ERROR_INVALID_PARENT);
            }
            int updateCount = 0;
            Composite[] update = new Composite[16];
            for (Control element : changed) {
                Control child = element;
                Composite composite = child.getImpl()._parent();
                // Update layout when the list of children has changed.
                // See bug 497812.
                child.getImpl().markLayout(false, false);
                while (child != this.getApi()) {
                    if (composite.getImpl()._layout() != null) {
                        composite.state |= LAYOUT_NEEDED;
                        if (!composite.getImpl()._layout().flushCache(child)) {
                            composite.state |= LAYOUT_CHANGED;
                        }
                    }
                    if (updateCount == update.length) {
                        Composite[] newUpdate = new Composite[update.length + 16];
                        System.arraycopy(update, 0, newUpdate, 0, update.length);
                        update = newUpdate;
                    }
                    child = update[updateCount++] = composite;
                    composite = child.getImpl()._parent();
                }
            }
            if (!((SwtDisplay) display.getImpl()).externalEventLoop && (flags & SWT.DEFER) != 0) {
                setLayoutDeferred(true);
                ((SwtDisplay) display.getImpl()).addLayoutDeferred(this.getApi());
            }
            for (int i = updateCount - 1; i >= 0; i--) {
                update[i].getImpl().updateLayout(false);
            }
        } else {
            if (layout == null && (flags & SWT.ALL) == 0)
                return;
            markLayout((flags & SWT.CHANGED) != 0, (flags & SWT.ALL) != 0);
            if (!((SwtDisplay) display.getImpl()).externalEventLoop && (flags & SWT.DEFER) != 0) {
                setLayoutDeferred(true);
                ((SwtDisplay) display.getImpl()).addLayoutDeferred(this.getApi());
            }
            updateLayout((flags & SWT.ALL) != 0);
        }
    }

    @Override
    public void markLayout(boolean changed, boolean all) {
        if (layout != null) {
            getApi().state |= LAYOUT_NEEDED;
            if (changed)
                getApi().state |= LAYOUT_CHANGED;
        }
        if (all) {
            for (Control element : _getChildren()) {
                element.getImpl().markLayout(changed, all);
            }
        }
    }

    Point minimumSize(int wHint, int hHint, boolean changed) {
        int width = 0, height = 0;
        return new Point(width, height);
    }

    @Override
    boolean redrawChildren() {
        if (!super.redrawChildren())
            return false;
        for (Control element : _getChildren()) {
            ((DartControl) element.getImpl()).redrawChildren();
        }
        return true;
    }

    @Override
    void releaseParent() {
        if (parent != null && parent.getImpl() instanceof DartComposite p) {
            p.updateChildren();
        }
        super.releaseParent();
        if ((getApi().state & CANVAS) != 0) {
            if ((getApi().style & SWT.TRANSPARENT) != 0) {
            }
        }
    }

    @Override
    void releaseChildren(boolean destroy) {
        try (ExceptionStash exceptions = new ExceptionStash()) {
            for (Control child : _getChildren()) {
                if (child == null || child.isDisposed())
                    continue;
                try {
                    child.getImpl().release(false);
                } catch (Error | RuntimeException ex) {
                    exceptions.stash(ex);
                }
            }
            super.releaseChildren(destroy);
        }
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        if ((getApi().state & CANVAS) != 0 && (getApi().style & SWT.EMBEDDED) != 0) {
        }
        layout = null;
        tabList = null;
    }

    public void removeControl(Control control) {
        fixTabList(control);
        resizeChildren();
        if (children != null) {
            children = java.util.Arrays.stream(children).filter(child -> child != control).toArray(Control[]::new);
        }
    }

    @Override
    void reskinChildren(int flags) {
        super.reskinChildren(flags);
        for (Control child : _getChildren()) {
            if (child != null)
                child.reskin(flags);
        }
    }

    void resizeChildren() {
    }

    void resizeEmbeddedHandle(long embeddedHandle, int width, int height) {
        if (embeddedHandle == 0)
            return;
    }

    @Override
    void sendResize() {
        setResizeChildren(false);
        super.sendResize();
        if (isDisposed())
            return;
        if (layout != null) {
            markLayout(false, false);
            updateLayout(false, false);
        }
        setResizeChildren(true);
    }

    /**
     * Sets the background drawing mode to the argument which should
     * be one of the following constants defined in class <code>SWT</code>:
     * <code>INHERIT_NONE</code>, <code>INHERIT_DEFAULT</code>,
     * <code>INHERIT_FORCE</code>.
     *
     * @param mode the new background mode
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SWT
     *
     * @since 3.2
     */
    public void setBackgroundMode(int mode) {
        checkWidget();
        if (!java.util.Objects.equals(this.backgroundMode, mode)) {
            dirty();
        }
        backgroundMode = mode;
        for (Control element : _getChildren()) {
            element.getImpl().updateBackgroundMode();
        }
    }

    @Override
    void setBoundsInPixels(int x, int y, int width, int height, int flags, boolean defer) {
        if (((SwtDisplay) display.getImpl()).resizeCount > SwtDisplay.RESIZE_LIMIT) {
            defer = false;
        }
        if (!defer && (getApi().state & CANVAS) != 0) {
            getApi().state &= ~(RESIZE_OCCURRED | MOVE_OCCURRED);
            getApi().state |= RESIZE_DEFERRED | MOVE_DEFERRED;
        }
        super.setBoundsInPixels(x, y, width, height, flags, defer);
        if (!defer && (getApi().state & CANVAS) != 0) {
            boolean wasMoved = (getApi().state & MOVE_OCCURRED) != 0;
            boolean wasResized = (getApi().state & RESIZE_OCCURRED) != 0;
            getApi().state &= ~(RESIZE_DEFERRED | MOVE_DEFERRED);
            if (wasMoved && !isDisposed())
                sendMove();
            if (wasResized && !isDisposed())
                sendResize();
        }
    }

    @Override
    public boolean setFocus() {
        checkWidget();
        Control[] children = _getChildren();
        for (Control child : children) {
            if (child.getVisible() && child.getImpl().setRadioFocus(false))
                return true;
        }
        for (Control child : children) {
            if (child.getVisible() && child.setFocus())
                return true;
        }
        return super.setFocus();
    }

    /**
     * Sets the layout which is associated with the receiver to be
     * the argument which may be null.
     *
     * @param layout the receiver's new layout or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setLayout(Layout layout) {
        checkWidget();
        this.layout = layout;
    }

    /**
     * If the argument is <code>true</code>, causes subsequent layout
     * operations in the receiver or any of its children to be ignored.
     * No layout of any kind can occur in the receiver or any of its
     * children until the flag is set to false.
     * Layout operations that occurred while the flag was
     * <code>true</code> are remembered and when the flag is set to
     * <code>false</code>, the layout operations are performed in an
     * optimized manner.  Nested calls to this method are stacked.
     *
     * @param defer the new defer state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #layout(boolean)
     * @see #layout(Control[])
     *
     * @since 3.1
     */
    public void setLayoutDeferred(boolean defer) {
        boolean newValue = defer;
        if (!java.util.Objects.equals(this.layoutDeferred, newValue)) {
            dirty();
        }
        checkWidget();
        this.layoutDeferred = newValue;
        if (!defer) {
            if (--layoutCount == 0) {
                if ((getApi().state & LAYOUT_CHILD) != 0 || (getApi().state & LAYOUT_NEEDED) != 0) {
                    updateLayout(true);
                }
            }
        } else {
            layoutCount++;
        }
    }

    /**
     * Sets the tabbing order for the specified controls to
     * match the order that they occur in the argument list.
     *
     * @param tabList the ordered list of controls representing the tab order or null
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if a widget in the tabList is null or has been disposed</li>
     *    <li>ERROR_INVALID_PARENT - if widget in the tabList is not in the same widget tree</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setTabList(Control[] tabList) {
        checkWidget();
        if (!java.util.Objects.equals(this.tabList, tabList)) {
            dirty();
        }
        if (tabList != null) {
            for (Control control : tabList) {
                if (control == null)
                    error(SWT.ERROR_INVALID_ARGUMENT);
                if (control.isDisposed())
                    error(SWT.ERROR_INVALID_ARGUMENT);
                if (control.getImpl()._parent() != this.getApi())
                    error(SWT.ERROR_INVALID_PARENT);
            }
            Control[] newList = new Control[tabList.length];
            System.arraycopy(tabList, 0, newList, 0, tabList.length);
            tabList = newList;
        }
        this.tabList = tabList;
    }

    void setResizeChildren(boolean resize) {
        if (resize) {
            resizeChildren();
        } else {
            if (((SwtDisplay) display.getImpl()).resizeCount > SwtDisplay.RESIZE_LIMIT) {
                return;
            }
        }
    }

    @Override
    public boolean setTabGroupFocus() {
        if (isTabItem())
            return setTabItemFocus();
        boolean takeFocus = (getApi().style & SWT.NO_FOCUS) == 0;
        if ((getApi().state & CANVAS) != 0) {
            takeFocus = hooksKeys();
            if ((getApi().style & SWT.EMBEDDED) != 0)
                takeFocus = true;
        }
        if (takeFocus && setTabItemFocus())
            return true;
        Control[] children = _getChildren();
        for (Control child : children) {
            /*
		 * It is unlikely but possible that a child is disposed at this point, for more
		 * details refer bug 381668.
		 */
            if (!child.isDisposed() && child.getImpl().isTabItem() && child.getImpl().setRadioFocus(true))
                return true;
        }
        for (Control child : children) {
            if (!child.isDisposed() && child.getImpl().isTabItem() && !child.getImpl().isTabGroup() && child.getImpl().setTabItemFocus()) {
                return true;
            }
        }
        return false;
    }

    @Override
    boolean updateTextDirection(int textDirection) {
        super.updateTextDirection(textDirection);
        /*
	 * Always continue, communicating the direction to the children since
	 * OS.WS_EX_RTLREADING doesn't propagate to them natively, and since
	 * the direction might need to be handled by each child individually.
	 */
        Control[] children = _getChildren();
        int i = children.length;
        while (i-- > 0) {
            if (children[i] != null && !children[i].isDisposed()) {
                ((DartControl) children[i].getImpl()).updateTextDirection(textDirection);
            }
        }
        /*
	 * Return value indicates whether or not to update derivatives, so in case
	 * of AUTO always return true regardless of the actual update.
	 */
        return true;
    }

    @Override
    public boolean translateMnemonic(Event event, Control control) {
        if (super.translateMnemonic(event, control))
            return true;
        if (control != null) {
            for (Control child : _getChildren()) {
                if (child.getImpl().translateMnemonic(event, control))
                    return true;
            }
        }
        return false;
    }

    @Override
    void updateBackgroundColor() {
        super.updateBackgroundColor();
        for (Control element : _getChildren()) {
            if ((element.state & PARENT_BACKGROUND) != 0) {
                ((DartControl) element.getImpl()).updateBackgroundColor();
            }
        }
    }

    @Override
    void updateBackgroundImage() {
        super.updateBackgroundImage();
        for (Control element : _getChildren()) {
            if ((element.state & PARENT_BACKGROUND) != 0) {
                ((DartControl) element.getImpl()).updateBackgroundImage();
            }
        }
    }

    @Override
    public void updateBackgroundMode() {
        super.updateBackgroundMode();
        for (Control element : _getChildren()) {
            element.getImpl().updateBackgroundMode();
        }
    }

    @Override
    void updateFont(Font oldFont, Font newFont) {
        super.updateFont(oldFont, newFont);
        for (Control control : _getChildren()) {
            if (!control.isDisposed()) {
                ((DartControl) control.getImpl()).updateFont(oldFont, newFont);
            }
        }
    }

    public void updateLayout(boolean all) {
        updateLayout(true, all);
    }

    @Override
    public void updateLayout(boolean resize, boolean all) {
        Composite parent = findDeferredControl();
        if (parent != null) {
            parent.state |= LAYOUT_CHILD;
            return;
        }
        if ((getApi().state & LAYOUT_NEEDED) != 0) {
            boolean changed = (getApi().state & LAYOUT_CHANGED) != 0;
            getApi().state &= ~(LAYOUT_NEEDED | LAYOUT_CHANGED);
            ((SwtDisplay) display.getImpl()).runSkin();
            if (resize)
                setResizeChildren(false);
            layout.layout(this.getApi(), changed);
            if (resize)
                setResizeChildren(true);
        }
        if (all) {
            getApi().state &= ~LAYOUT_CHILD;
            for (Control element : _getChildren()) {
                element.getImpl().updateLayout(resize, all);
            }
        }
    }

    @Override
    void updateOrientation() {
        Control[] controls = _getChildren();
        for (int i = 0; i < controls.length; i++) {
            Control control = controls[i];
            ((DartControl) control.getImpl()).forceResize();
        }
        int orientation = getApi().style & (SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT);
        super.updateOrientation();
        for (int i = 0; i < controls.length; i++) {
            Control control = controls[i];
            control.setOrientation(orientation);
        }
    }

    void updateUIState() {
    }

    @Override
    int widgetStyle() {
        return 0;
    }

    @Override
    public String toString() {
        return super.toString() + " [layout=" + layout + "]";
    }

    private static void handleDPIChange(Widget widget, int newZoom, float scalingFactor) {
        if (!(widget instanceof Composite composite)) {
            return;
        }
    }

    Control[] children = new Control[0];

    boolean layoutDeferred;

    public Layout _layout() {
        return layout;
    }

    public Control[] _tabList() {
        return tabList;
    }

    public int _layoutCount() {
        return layoutCount;
    }

    public int _backgroundMode() {
        return backgroundMode;
    }

    public Control[] _children() {
        return children;
    }

    public boolean _layoutDeferred() {
        return layoutDeferred;
    }

    Object contentView() {
        return getBridge().container(this);
    }

    public void updateChildren() {
        if (children == null)
            return;
        children = java.util.Arrays.stream(children).filter(child -> child != null && !child.isDisposed()).toArray(Control[]::new);
    }

    public void dispose() {
        if (parent != null && !parent.isDisposed()) {
            parent.getImpl().removeControl(this.getApi());
        }
        if (parent != null && !parent.isDisposed() && parent.getImpl() instanceof DartComposite p) {
            p.updateChildren();
        }
    }

    protected void _hookEvents() {
        super._hookEvents();
    }

    public Composite getApi() {
        if (api == null)
            api = Composite.createApi(this);
        return (Composite) api;
    }

    public VComposite getValue() {
        if (value == null)
            value = new VComposite(this);
        return (VComposite) value;
    }
}
