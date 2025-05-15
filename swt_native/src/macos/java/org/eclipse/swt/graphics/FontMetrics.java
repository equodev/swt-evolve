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

/**
 * Instances of this class provide measurement information
 * about fonts including ascent, descent, height, leading
 * space between rows, and average character width.
 * <code>FontMetrics</code> are obtained from <code>GC</code>s
 * using the <code>getFontMetrics()</code> method.
 *
 * @see GC#getFontMetrics
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public final class FontMetrics {

    /**
     * Compares the argument to the receiver, and returns true
     * if they represent the <em>same</em> object using a class
     * specific comparison.
     *
     * @param object the object to compare with this object
     * @return <code>true</code> if the object is the same as this object and <code>false</code> otherwise
     *
     * @see #hashCode
     */
    public boolean equals(Object object) {
        return getDelegate().equals(object);
    }

    /**
     * Returns the ascent of the font described by the receiver. A
     * font's <em>ascent</em> is the distance from the baseline to the
     * top of actual characters, not including any of the leading area,
     * measured in points.
     *
     * @return the ascent of the font
     */
    public int getAscent() {
        return getDelegate().getAscent();
    }

    /**
     * Returns the average character width, measured in points,
     * of the font described by the receiver.
     *
     * @return the average character width of the font
     * @since 3.107
     */
    public double getAverageCharacterWidth() {
        return getDelegate().getAverageCharacterWidth();
    }

    /**
     * Returns the average character width, measured in points,
     * of the font described by the receiver.
     *
     * @return the average character width of the font
     * @deprecated Use getAverageCharacterWidth() instead
     */
    @Deprecated
    public int getAverageCharWidth() {
        return getDelegate().getAverageCharWidth();
    }

    /**
     * Returns the descent of the font described by the receiver. A
     * font's <em>descent</em> is the distance from the baseline to the
     * bottom of actual characters, not including any of the leading area,
     * measured in points.
     *
     * @return the descent of the font
     */
    public int getDescent() {
        return getDelegate().getDescent();
    }

    /**
     * Returns the height of the font described by the receiver,
     * measured in points. A font's <em>height</em> is the sum of
     * its ascent, descent and leading area.
     *
     * @return the height of the font
     *
     * @see #getAscent
     * @see #getDescent
     * @see #getLeading
     */
    public int getHeight() {
        return getDelegate().getHeight();
    }

    /**
     * Returns the leading area of the font described by the
     * receiver. A font's <em>leading area</em> is the space
     * above its ascent which may include accents or other marks.
     *
     * @return the leading space of the font
     */
    public int getLeading() {
        return getDelegate().getLeading();
    }

    /**
     * Returns an integer hash code for the receiver. Any two
     * objects that return <code>true</code> when passed to
     * <code>equals</code> must return the same value for this
     * method.
     *
     * @return the receiver's hash
     *
     * @see #equals
     */
    public int hashCode() {
        return getDelegate().hashCode();
    }

    public String toString() {
        return getDelegate().toString();
    }

    IFontMetrics delegate;

    protected FontMetrics(IFontMetrics delegate) {
        this.delegate = delegate;
        delegate.setApi(this);
    }

    public static FontMetrics createApi(IFontMetrics delegate) {
        return new FontMetrics(delegate);
    }

    public IFontMetrics getDelegate() {
        return delegate;
    }
}
