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
import java.util.WeakHashMap;

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
     * Prevents uninitialized instances from being created outside the package.
     */
    Monitor() {
        this(new SWTMonitor());
    }

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
    @Override
    public boolean equals(Object object) {
        return ((IMonitor) this.delegate).equals(object);
    }

    /**
     * Returns a rectangle describing the receiver's size and location
     * relative to its device. Note that on multi-monitor systems the
     * origin can be negative.
     *
     * @return the receiver's bounding rectangle
     */
    public Rectangle getBounds() {
        return ((IMonitor) this.delegate).getBounds();
    }

    /**
     * Returns a rectangle which describes the area of the
     * receiver which is capable of displaying data.
     *
     * @return the client area
     */
    public Rectangle getClientArea() {
        return ((IMonitor) this.delegate).getClientArea();
    }

    /**
     * Returns the zoom value for the monitor
     *
     * @return monitor's zoom value
     *
     * @since 3.107
     */
    public int getZoom() {
        return ((IMonitor) this.delegate).getZoom();
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
    @Override
    public int hashCode() {
        return ((IMonitor) this.delegate).hashCode();
    }

    public IMonitor delegate;

    protected static <T extends Monitor, I extends IMonitor> T[] ofArray(I[] items, Class<T> clazz, java.util.function.Function<I, T> factory) {
        @SuppressWarnings("unchecked")
        T[] target = (T[]) java.lang.reflect.Array.newInstance(clazz, items.length);
        for (int i = 0; i < target.length; ++i) target[i] = factory.apply(items[i]);
        return target;
    }

    @SuppressWarnings("unchecked")
    protected static <T extends Monitor, I extends IMonitor> I[] fromArray(T[] items) {
        if (items.length == 0)
            return (I[]) java.lang.reflect.Array.newInstance(IMonitor.class, 0);
        Class<I> targetClazz = null;
        for (T item : items) outer: {
            for (Class<?> i : item.getClass().getInterfaces()) {
                if (IMonitor.class.isAssignableFrom(i)) {
                    targetClazz = (Class<I>) i;
                    break outer;
                }
            }
        }
        if (targetClazz == null)
            return (I[]) java.lang.reflect.Array.newInstance(IMonitor.class, 0);
        I[] target = (I[]) java.lang.reflect.Array.newInstance(targetClazz, items.length);
        for (int i = 0; i < target.length; ++i) target[i] = (I) items[i].delegate;
        return target;
    }

    protected static final WeakHashMap<IMonitor, Monitor> INSTANCES = new WeakHashMap<IMonitor, Monitor>();

    protected Monitor(IMonitor delegate) {
        this.delegate = delegate;
        INSTANCES.put(delegate, this);
    }

    public static Monitor getInstance(IMonitor delegate) {
        if (delegate == null) {
            return null;
        }
        Monitor ref = (Monitor) INSTANCES.get(delegate);
        if (ref == null) {
            ref = new Monitor(delegate);
        }
        return ref;
    }
}
