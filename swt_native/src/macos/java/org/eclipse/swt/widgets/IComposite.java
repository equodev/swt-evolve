package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.graphics.*;

public interface IComposite extends IScrollable {

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
    void changed(IControl[] changed);

    IPoint computeSize(int wHint, int hHint, boolean changed);

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
    void drawBackground(IGC gc, int x, int y, int width, int height, int offsetX, int offsetY);

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
    int getBackgroundMode();

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
    IControl[] getChildren();

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
    ILayout getLayout();

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
    boolean getLayoutDeferred();

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
    IControl[] getTabList();

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
    boolean isLayoutDeferred();

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
    void layout();

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
    void layout(boolean changed);

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
    void layout(boolean changed, boolean all);

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
    void layout(IControl[] changed);

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
    void layout(IControl[] changed, int flags);

    void redraw(int x, int y, int width, int height, boolean all);

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
    void setBackgroundMode(int mode);

    boolean setFocus();

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
    void setLayout(ILayout layout);

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
    void setLayoutDeferred(boolean defer);

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
    void setTabList(IControl[] tabList);

    String toString();

    Composite getApi();
}
