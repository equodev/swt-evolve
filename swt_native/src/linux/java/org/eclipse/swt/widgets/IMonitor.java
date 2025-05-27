package org.eclipse.swt.widgets;

import org.eclipse.swt.graphics.*;

public interface IMonitor {

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
    boolean equals(Object object);

    /**
     * Returns a rectangle describing the receiver's size and location
     * relative to its device. Note that on multi-monitor systems the
     * origin can be negative.
     *
     * @return the receiver's bounding rectangle
     */
    Rectangle getBounds();

    /**
     * Returns a rectangle which describes the area of the
     * receiver which is capable of displaying data.
     *
     * @return the client area
     */
    Rectangle getClientArea();

    /**
     * Returns the zoom value for the monitor
     *
     * @return monitor's zoom value
     *
     * @since 3.107
     */
    int getZoom();

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
    int hashCode();
}
