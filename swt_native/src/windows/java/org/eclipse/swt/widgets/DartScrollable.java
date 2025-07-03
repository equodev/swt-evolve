/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
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
import dev.equo.swt.*;

/**
 * This class is the abstract superclass of all classes which
 * represent controls that have standard scroll bars.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>H_SCROLL, V_SCROLL</dd>
 * <dt><b>Events:</b>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em>
 * within the SWT implementation.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class DartScrollable extends DartControl implements IScrollable {

    ScrollBar horizontalBar, verticalBar;

    /**
     * The regular expression used to determine the string which should be deleted
     * when Ctrl+Bs is hit.
     */
    static final java.util.regex.Pattern CTRL_BS_PATTERN = java.util.regex.Pattern.compile("\\r?\\n\\z|[\\p{Punct}]+[\\t ]*\\z|[^\\p{Punct}\\s\\n\\r]*[\\t ]*\\z");

    /**
     * Prevents uninitialized instances from being created outside the package.
     */
    DartScrollable(Scrollable api) {
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
     * @see SWT#H_SCROLL
     * @see SWT#V_SCROLL
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartScrollable(Composite parent, int style, Scrollable api) {
        super(parent, style, api);
    }

    /**
     * Given a desired <em>client area</em> for the receiver
     * (as described by the arguments), returns the bounding
     * rectangle which would be required to produce that client
     * area.
     * <p>
     * In other words, it returns a rectangle such that, if the
     * receiver's bounds were set to that rectangle, the area
     * of the receiver which is capable of displaying data
     * (that is, not covered by the "trimmings") would be the
     * rectangle described by the arguments (relative to the
     * receiver's parent).
     * </p>
     *
     * @param x the desired x coordinate of the client area
     * @param y the desired y coordinate of the client area
     * @param width the desired width of the client area
     * @param height the desired height of the client area
     * @return the required bounds to produce the given client area
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #getClientArea
     */
    public Rectangle computeTrim(int x, int y, int width, int height) {
        checkWidget();
        int zoom = getZoom();
        x = DPIUtil.scaleUp(x, zoom);
        y = DPIUtil.scaleUp(y, zoom);
        width = DPIUtil.scaleUp(width, zoom);
        height = DPIUtil.scaleUp(height, zoom);
        return DPIUtil.scaleDown(computeTrimInPixels(x, y, width, height), zoom);
    }

    Rectangle computeTrimInPixels(int x, int y, int width, int height) {
        return null;
    }

    @Override
    void createHandle() {
        super.createHandle();
        maybeEnableDarkSystemTheme();
    }

    ScrollBar createScrollBar(int type) {
        ScrollBar bar = new ScrollBar(this.getApi(), type);
        if ((getApi().state & CANVAS) != 0) {
            bar.setMaximum(100);
            bar.setThumb(10);
        }
        return bar;
    }

    @Override
    void createWidget() {
        super.createWidget();
        if ((getApi().style & SWT.H_SCROLL) != 0)
            horizontalBar = createScrollBar(SWT.H_SCROLL);
        if ((getApi().style & SWT.V_SCROLL) != 0)
            verticalBar = createScrollBar(SWT.V_SCROLL);
    }

    @Override
    void updateBackgroundColor() {
        switch(applyThemeBackground()) {
            case 0:
                getApi().state &= ~THEME_BACKGROUND;
                break;
            case 1:
                getApi().state |= THEME_BACKGROUND;
                break;
            default:
        }
        super.updateBackgroundColor();
    }

    /**
     * @return
     * 		<li>0 to remove THEME_BACKGROUND</li>
     *      <li>1 to apply THEME_BACKGROUND</li>
     *      <li>otherwise don't change THEME_BACKGROUND state</li>
     */
    int applyThemeBackground() {
        return (backgroundAlpha == 0) ? 1 : 0;
    }

    void destroyScrollBar(int type) {
        long hwnd = scrolledHandle();
        if ((type & SWT.HORIZONTAL) != 0) {
            getApi().style &= ~SWT.H_SCROLL;
        }
        if ((type & SWT.VERTICAL) != 0) {
            getApi().style &= ~SWT.V_SCROLL;
        }
    }

    /**
     * Returns a rectangle which describes the area of the
     * receiver which is capable of displaying data (that is,
     * not covered by the "trimmings").
     *
     * @return the client area
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #computeTrim
     */
    public Rectangle getClientArea() {
        checkWidget();
        return DPIUtil.scaleDown(getClientAreaInPixels(), getZoom());
    }

    Rectangle getClientAreaInPixels() {
        forceResize();
        long scrolledHandle = scrolledHandle();
         ;
        if (scrolledHandle != getApi().handle) {
        }
        return null;
    }

    /**
     * Returns the receiver's horizontal scroll bar if it has
     * one, and null if it does not.
     *
     * @return the horizontal scroll bar (or null)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public ScrollBar getHorizontalBar() {
        checkWidget();
        return horizontalBar;
    }

    /**
     * Returns the mode of the receiver's scrollbars. This will be
     * <em>bitwise</em> OR of one or more of the constants defined in class
     * <code>SWT</code>.<br>
     * <ul>
     * <li><code>SWT.SCROLLBAR_OVERLAY</code> - if receiver
     * uses overlay scrollbars</li>
     * <li><code>SWT.NONE</code> - otherwise</li>
     * </ul>
     *
     * @return the mode of scrollbars
     *
     * @exception SWTException <ul>
     * <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     * disposed</li>
     * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
     * thread that created the receiver</li>
     * </ul>
     *
     * @see SWT#SCROLLBAR_OVERLAY
     *
     * @since 3.8
     */
    public int getScrollbarsMode() {
        checkWidget();
        return SWT.NONE;
    }

    /**
     * Sets the mode of the receiver's scrollbars. This will be
     * <em>bitwise</em> OR of one or more of the constants defined in class
     * <code>SWT</code>.<br>
     * <ul>
     * <li><code>SWT.SCROLLBAR_OVERLAY</code> - if receiver
     * uses overlay scrollbars</li>
     * <li><code>SWT.NONE</code> - otherwise</li>
     * </ul>
     *
     * @exception SWTException <ul>
     * <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     * disposed</li>
     * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
     * thread that created the receiver</li>
     * </ul>
     *
     * @see SWT#SCROLLBAR_OVERLAY
     *
     * @since 3.126
     */
    public void setScrollbarsMode(int mode) {
        checkWidget();
        this.scrollbarsMode = mode;
        getBridge().dirty(this);
    }

    /**
     * Returns the receiver's vertical scroll bar if it has
     * one, and null if it does not.
     *
     * @return the vertical scroll bar (or null)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public ScrollBar getVerticalBar() {
        checkWidget();
        return verticalBar;
    }

    @Override
    void releaseChildren(boolean destroy) {
        if (horizontalBar != null) {
            horizontalBar.getImpl().release(false);
            horizontalBar = null;
        }
        if (verticalBar != null) {
            verticalBar.getImpl().release(false);
            verticalBar = null;
        }
        super.releaseChildren(destroy);
    }

    @Override
    void reskinChildren(int flags) {
        if (horizontalBar != null)
            horizontalBar.reskin(flags);
        if (verticalBar != null)
            verticalBar.reskin(flags);
        super.reskinChildren(flags);
    }

    long scrolledHandle() {
        return getApi().handle;
    }

    @Override
    int widgetStyle() {
        return 0;
    }

    int scrollbarsMode;

    public ScrollBar _horizontalBar() {
        return horizontalBar;
    }

    public ScrollBar _verticalBar() {
        return verticalBar;
    }

    public int _scrollbarsMode() {
        return scrollbarsMode;
    }

    protected void hookEvents() {
        super.hookEvents();
    }

    public Scrollable getApi() {
        return (Scrollable) api;
    }

    public VScrollable getValue() {
        if (value == null)
            value = new VScrollable(this);
        return (VScrollable) value;
    }
}
