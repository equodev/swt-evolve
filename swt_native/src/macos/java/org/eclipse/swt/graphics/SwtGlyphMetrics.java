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
package org.eclipse.swt.graphics;

import org.eclipse.swt.*;

/**
 * Instances of this class represent glyph metrics.
 * <p>
 * The hashCode() method in this class uses the values of the public
 * fields to compute the hash value. When storing instances of the
 * class in hashed collections, do not modify these fields after the
 * object has been inserted.
 * </p>
 * <p>
 * Application code does <em>not</em> need to explicitly release the
 * resources managed by each instance when those instances are no longer
 * required, and thus no <code>dispose()</code> method is provided.
 * </p>
 *
 * @see TextStyle
 * @see TextLayout
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.2
 */
public final class SwtGlyphMetrics implements IGlyphMetrics {

    /**
     * Constructs an instance of this class with the given
     * ascent, descent and width values.
     *
     * @param ascent the GlyphMetrics ascent
     * @param descent the GlyphMetrics descent
     * @param width the GlyphMetrics width
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the ascent, descent or width argument is negative</li>
     * </ul>
     */
    public SwtGlyphMetrics(int ascent, int descent, int width) {
        if (ascent < 0 || descent < 0 || width < 0) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        this.getApi().ascent = ascent;
        this.getApi().descent = descent;
        this.getApi().width = width;
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
        if (!(object instanceof GlyphMetrics metrics))
            return false;
        return metrics.ascent == getApi().ascent && metrics.descent == getApi().descent && metrics.width == getApi().width;
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
        return getApi().ascent ^ getApi().descent ^ getApi().width;
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the <code>GlyphMetrics</code>
     */
    @Override
    public String toString() {
        //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        return "GlyphMetrics {" + getApi().ascent + ", " + getApi().descent + ", " + getApi().width + "}";
    }

    public GlyphMetrics getApi() {
        if (api == null)
            api = GlyphMetrics.createApi(this);
        return (GlyphMetrics) api;
    }

    protected GlyphMetrics api;

    public void setApi(GlyphMetrics api) {
        this.api = api;
    }
}
