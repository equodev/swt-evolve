/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
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
import org.eclipse.swt.internal.cocoa.*;

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
public abstract class Scrollable extends Control {

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
        return getDelegate().computeTrim(x, y, width, height).getApi();
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
        return getDelegate().getClientArea().getApi();
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
        return getDelegate().getHorizontalBar().getApi();
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
        return getDelegate().getScrollbarsMode();
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
        getDelegate().setScrollbarsMode(mode);
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
        return getDelegate().getVerticalBar().getApi();
    }

    protected Scrollable(IScrollable delegate) {
        super(delegate);
    }

    public IScrollable getDelegate() {
        return (IScrollable) super.getDelegate();
    }
}
