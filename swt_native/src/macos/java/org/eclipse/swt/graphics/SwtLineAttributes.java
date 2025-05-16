/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2011 IBM Corporation and others.
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
package org.eclipse.swt.graphics;

import org.eclipse.swt.*;

/**
 * <code>LineAttributes</code> defines a set of line attributes that
 * can be modified in a GC.
 * <p>
 * Application code does <em>not</em> need to explicitly release the
 * resources managed by each instance when those instances are no longer
 * required, and thus no <code>dispose()</code> method is provided.
 * </p>
 *
 * @see GC#getLineAttributes()
 * @see GC#setLineAttributes(LineAttributes)
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.3
 */
public class SwtLineAttributes implements ILineAttributes {

    /**
     * Create a new line attributes with the specified line width.
     *
     * @param width the line width
     */
    public SwtLineAttributes(float width) {
        this(width, SWT.CAP_FLAT, SWT.JOIN_MITER, SWT.LINE_SOLID, null, 0, 10);
    }

    /**
     * Create a new line attributes with the specified line cap, join and width.
     *
     * @param width the line width
     * @param cap the line cap style
     * @param join the line join style
     */
    public SwtLineAttributes(float width, int cap, int join) {
        this(width, cap, join, SWT.LINE_SOLID, null, 0, 10);
    }

    /**
     * Create a new line attributes with the specified arguments.
     *
     * @param width the line width
     * @param cap the line cap style
     * @param join the line join style
     * @param style the line style
     * @param dash the line dash style
     * @param dashOffset the line dash style offset
     * @param miterLimit the line miter limit
     */
    public SwtLineAttributes(float width, int cap, int join, int style, float[] dash, float dashOffset, float miterLimit) {
        this.getApi().width = width;
        this.getApi().cap = cap;
        this.getApi().join = join;
        this.getApi().style = style;
        this.getApi().dash = dash;
        this.getApi().dashOffset = dashOffset;
        this.getApi().miterLimit = miterLimit;
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
        if (object == this.getApi())
            return true;
        if (!(object instanceof LineAttributes p))
            return false;
        if (p.width != getApi().width)
            return false;
        if (p.cap != getApi().cap)
            return false;
        if (p.join != getApi().join)
            return false;
        if (p.style != getApi().style)
            return false;
        if (p.dashOffset != getApi().dashOffset)
            return false;
        if (p.miterLimit != getApi().miterLimit)
            return false;
        if (p.dash != null && getApi().dash != null) {
            if (p.dash.length != getApi().dash.length)
                return false;
            for (int i = 0; i < getApi().dash.length; i++) {
                if (p.dash[i] != getApi().dash[i])
                    return false;
            }
        } else {
            if (p.dash != null || getApi().dash != null)
                return false;
        }
        return true;
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
        int hashCode = Float.floatToIntBits(getApi().width);
        hashCode = 31 * hashCode + getApi().cap;
        hashCode = 31 * hashCode + getApi().join;
        hashCode = 31 * hashCode + getApi().style;
        hashCode = 31 * hashCode + Float.floatToIntBits(getApi().dashOffset);
        hashCode = 31 * hashCode + Float.floatToIntBits(getApi().miterLimit);
        if (getApi().dash != null) {
            for (float element : getApi().dash) {
                hashCode = 31 * hashCode + Float.floatToIntBits(element);
            }
        }
        return hashCode;
    }

    public LineAttributes getApi() {
        if (api == null)
            api = LineAttributes.createApi(this);
        return (LineAttributes) api;
    }

    protected LineAttributes api;

    public void setApi(LineAttributes api) {
        this.api = api;
    }
}
