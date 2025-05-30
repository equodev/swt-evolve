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
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.ExceptionStash;
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

    int layoutCount, backgroundMode;

    DartComposite() {
        /* Do nothing */
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
    public DartComposite(Composite parent, int style) {
        super(parent, style);
    }

    Control[] _getChildren() {
        return getValue().children.toArray(Control[]::new);
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
    public Point computeSize(int wHint, int hHint, boolean changed) {
        checkWidget();
        ((SwtDisplay) display.getImpl()).runSkin();
        Point size;
        if (layout != null) {
            if ((wHint == SWT.DEFAULT) || (hHint == SWT.DEFAULT)) {
                changed |= (state & LAYOUT_CHANGED) != 0;
                size = layout.computeSize(this.getApi(), wHint, hHint, changed);
                state &= ~LAYOUT_CHANGED;
            } else {
                size = new Point(wHint, hHint);
            }
        } else {
            size = minimumSize(wHint, hHint, changed);
            if (size.x == 0)
                size.x = DEFAULT_WIDTH;
            if (size.y == 0)
                size.y = DEFAULT_HEIGHT;
        }
        if (wHint != SWT.DEFAULT)
            size.x = wHint;
        if (hHint != SWT.DEFAULT)
            size.y = hHint;
        Rectangle trim = computeTrim(0, 0, size.x, size.y);
        return new Point(trim.width, trim.height);
    }

    @Override
    public void checkSubclass() {
        /* Do nothing - Subclassing is allowed */
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
    }

    Composite findDeferredControl() {
        return layoutCount > 0 ? this.getApi() : ((SwtComposite) parent.getImpl()).findDeferredControl();
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
        return getValue().backgroundMode;
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
        return _getChildren();
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
        return getValue().layout;
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
        return getValue().layoutDeferred;
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
        return getValue().tabList;
    }

    @Override
    boolean hooksKeys() {
        return hooks(SWT.KeyDown) || hooks(SWT.KeyUp);
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
            for (int i = 0; i < changed.length; i++) {
                Control control = changed[i];
                if (control == null)
                    error(SWT.ERROR_INVALID_ARGUMENT);
                if (control.isDisposed())
                    error(SWT.ERROR_INVALID_ARGUMENT);
                boolean ancestor = false;
                Composite composite = ((SwtControl) control.getImpl()).parent;
                while (composite != null) {
                    ancestor = composite == this.getApi();
                    if (ancestor)
                        break;
                    composite = ((SwtControl) composite.getImpl()).parent;
                }
                if (!ancestor)
                    error(SWT.ERROR_INVALID_PARENT);
            }
            int updateCount = 0;
            Composite[] update = new Composite[16];
            for (int i = 0; i < changed.length; i++) {
                Control child = changed[i];
                Composite composite = ((SwtControl) child.getImpl()).parent;
                // Update layout when the list of children has changed.
                // See bug 497812.
                ((SwtControl) child.getImpl()).markLayout(false, false);
                while (child != this.getApi()) {
                    if (((SwtComposite) composite.getImpl()).layout != null) {
                        ((SwtWidget) composite.getImpl()).state |= LAYOUT_NEEDED;
                        if (!((SwtComposite) composite.getImpl()).layout.flushCache(child)) {
                            ((SwtWidget) composite.getImpl()).state |= LAYOUT_CHANGED;
                        }
                    }
                    if (updateCount == update.length) {
                        Composite[] newUpdate = new Composite[update.length + 16];
                        System.arraycopy(update, 0, newUpdate, 0, update.length);
                        update = newUpdate;
                    }
                    child = update[updateCount++] = composite;
                    composite = ((SwtControl) child.getImpl()).parent;
                }
            }
            if ((flags & SWT.DEFER) != 0) {
                setLayoutDeferred(true);
                ((SwtDisplay) display.getImpl()).addLayoutDeferred(this.getApi());
            }
            for (int i = updateCount - 1; i >= 0; i--) {
                ((SwtComposite) update[i].getImpl()).updateLayout(false);
            }
        } else {
            if (layout == null && (flags & SWT.ALL) == 0)
                return;
            markLayout((flags & SWT.CHANGED) != 0, (flags & SWT.ALL) != 0);
            if ((flags & SWT.DEFER) != 0) {
                setLayoutDeferred(true);
                ((SwtDisplay) display.getImpl()).addLayoutDeferred(this.getApi());
            }
            updateLayout((flags & SWT.ALL) != 0);
        }
    }

    @Override
    void markLayout(boolean changed, boolean all) {
        if (layout != null) {
            state |= LAYOUT_NEEDED;
            if (changed)
                state |= LAYOUT_CHANGED;
        }
        if (all) {
            Control[] children = _getChildren();
            for (int i = 0; i < children.length; i++) {
                ((SwtControl) children[i].getImpl()).markLayout(changed, all);
            }
        }
    }

    Point minimumSize(int wHint, int Hint, boolean changed) {
        Control[] children = _getChildren();
        Rectangle clientArea = getClientArea();
        int width = 0, height = 0;
        for (int i = 0; i < children.length; i++) {
            Rectangle rect = children[i].getBounds();
            width = Math.max(width, rect.x - clientArea.x + rect.width);
            height = Math.max(height, rect.y - clientArea.y + rect.height);
        }
        return new Point(width, height);
    }

    @Override
    public void redraw(int x, int y, int width, int height, boolean all) {
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
        getValue().backgroundMode = mode;
        getBridge().dirty(this);
    }

    @Override
    public boolean setFocus() {
        checkWidget();
        Control[] children = _getChildren();
        for (int i = 0; i < children.length; i++) {
            if (children[i].getVisible() && children[i].setFocus())
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
        getValue().layout = layout;
        getBridge().dirty(this);
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
        getValue().layoutDeferred = defer;
        getBridge().dirty(this);
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
        getValue().tabList = tabList;
        getBridge().dirty(this);
    }

    @Override
    int traversalCode(int key, Object theEvent) {
        if ((state & CANVAS) != 0) {
            if ((style & SWT.NO_FOCUS) != 0)
                return 0;
            if (hooksKeys())
                return 0;
        }
        return super.traversalCode(key, theEvent);
    }

    @Override
    void updateLayout(boolean all) {
        Composite parent = findDeferredControl();
        if (parent != null) {
            ((SwtWidget) parent.getImpl()).state |= LAYOUT_CHILD;
            return;
        }
        if ((state & LAYOUT_NEEDED) != 0) {
            boolean changed = (state & LAYOUT_CHANGED) != 0;
            state &= ~(LAYOUT_NEEDED | LAYOUT_CHANGED);
            ((SwtDisplay) display.getImpl()).runSkin();
            layout.layout(this.getApi(), changed);
        }
        if (all) {
            state &= ~LAYOUT_CHILD;
            Control[] children = _getChildren();
            for (int i = 0; i < children.length; i++) {
                ((SwtControl) children[i].getImpl()).updateLayout(all);
            }
        }
    }

    @Override
    public String toString() {
        return super.toString() + " [layout=" + layout + "]";
    }

    public Composite getApi() {
        if (api == null)
            api = Composite.createApi(this);
        return (Composite) api;
    }

    public VComposite getValue() {
        if (value == null)
            value = new VComposite();
        return (VComposite) value;
    }
}
