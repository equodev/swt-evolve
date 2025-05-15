/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2016 IBM Corporation and others.
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

import org.eclipse.swt.graphics.*;

/**
 * Instances of this class are descriptions of monitors.
 *
 * @see Display
 * @see <a href="http://www.eclipse.org/swt/snippets/#monitor">Monitor snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.0
 */
public final class Monitor {

    /**
     * Compares the argument to the receiver, and returns true
     * if they represent the <em>same</em> object using a class
     * specific comparison.
     *
     * @param object the object to compare with this object
     * @return <code>true</code> if the object is the same as this object and <code>false</code> otherwise
     *
     * @see #hashCode()
     */
    public boolean equals(Object object) {
        return getDelegate().equals(object);
    }

    /**
     * Returns a rectangle describing the receiver's size and location
     * relative to its device. Note that on multi-monitor systems the
     * origin can be negative.
     *
     * @return the receiver's bounding rectangle
     */
    public Rectangle getBounds() {
        return getDelegate().getBounds().getApi();
    }

    /**
     * Returns a rectangle which describes the area of the
     * receiver which is capable of displaying data.
     *
     * @return the client area
     */
    public Rectangle getClientArea() {
        return getDelegate().getClientArea().getApi();
    }

    /**
     * Returns the zoom value for the monitor
     *
     * @return monitor's zoom value
     *
     * @since 3.107
     */
    public int getZoom() {
        return getDelegate().getZoom();
    }

    /**
     * Returns an integer hash code for the receiver. Any two
     * objects that return <code>true</code> when passed to
     * <code>equals</code> must return the same value for this
     * method.
     *
     * @return the receiver's hash
     *
     * @see #equals(Object)
     */
    public int hashCode() {
        return getDelegate().hashCode();
    }

    IMonitor delegate;

    protected Monitor(IMonitor delegate) {
        this.delegate = delegate;
        delegate.setApi(this);
    }

    public static Monitor createApi(IMonitor delegate) {
        return new Monitor(delegate);
    }

    public IMonitor getDelegate() {
        return delegate;
    }
}
