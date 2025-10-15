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

import dev.equo.swt.*;

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
public final class DartFontMetrics implements IFontMetrics {

    int ascent = 11, descent = 4, leading, height;

    double averageCharWidth = 12;

    DartFontMetrics(FontMetrics api) {
        setApi(api);
    }

    /**
     * Convenience method to make a copy of receiver.
     */
    FontMetrics makeCopy() {
        FontMetrics fontMetrics = new FontMetrics();
        if (fontMetrics.getImpl() instanceof DartFontMetrics) {
            ((DartFontMetrics) fontMetrics.getImpl()).ascent = this.ascent;
        }
        if (fontMetrics.getImpl() instanceof SwtFontMetrics) {
            ((SwtFontMetrics) fontMetrics.getImpl()).ascent = this.ascent;
        }
        if (fontMetrics.getImpl() instanceof DartFontMetrics) {
            ((DartFontMetrics) fontMetrics.getImpl()).descent = this.descent;
        }
        if (fontMetrics.getImpl() instanceof SwtFontMetrics) {
            ((SwtFontMetrics) fontMetrics.getImpl()).descent = this.descent;
        }
        if (fontMetrics.getImpl() instanceof DartFontMetrics) {
            ((DartFontMetrics) fontMetrics.getImpl()).averageCharWidth = this.averageCharWidth;
        }
        if (fontMetrics.getImpl() instanceof SwtFontMetrics) {
            ((SwtFontMetrics) fontMetrics.getImpl()).averageCharWidth = this.averageCharWidth;
        }
        if (fontMetrics.getImpl() instanceof DartFontMetrics) {
            ((DartFontMetrics) fontMetrics.getImpl()).leading = this.leading;
        }
        if (fontMetrics.getImpl() instanceof SwtFontMetrics) {
            ((SwtFontMetrics) fontMetrics.getImpl()).leading = this.leading;
        }
        if (fontMetrics.getImpl() instanceof DartFontMetrics) {
            ((DartFontMetrics) fontMetrics.getImpl()).height = this.height;
        }
        if (fontMetrics.getImpl() instanceof SwtFontMetrics) {
            ((SwtFontMetrics) fontMetrics.getImpl()).height = this.height;
        }
        return fontMetrics;
    }

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
    @Override
    public boolean equals(Object object) {
        if (object == this.getApi())
            return true;
        if (!(object instanceof FontMetrics metrics))
            return false;
        return ascent == metrics.getImpl()._ascent() && descent == metrics.getImpl()._descent() && leading == metrics.getImpl()._leading() && height == metrics.getImpl()._height() && (Double.compare(averageCharWidth, metrics.getImpl()._averageCharWidth()) == 0);
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
        return ascent;
    }

    /**
     * Returns the average character width, measured in points,
     * of the font described by the receiver.
     *
     * @return the average character width of the font
     * @since 3.107
     */
    public double getAverageCharacterWidth() {
        return averageCharWidth;
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
        return (int) averageCharWidth;
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
        return 4;
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
        return 12;
    }

    /**
     * Returns the leading area of the font described by the
     * receiver. A font's <em>leading area</em> is the space
     * above its ascent which may include accents or other marks.
     *
     * @return the leading space of the font
     */
    public int getLeading() {
        return leading;
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
    @Override
    public int hashCode() {
        return ascent ^ descent ^ Double.hashCode(averageCharWidth) ^ leading ^ height;
    }

    String getName() {
        String string = getClass().getName();
        int index = string.lastIndexOf('.');
        if (index == -1)
            return string;
        return string.substring(index + 1, string.length());
    }

    @Override
    public String toString() {
        return getName() + "{" + " ascent=" + ascent + " descent=" + descent + " averageCharWidth=" + averageCharWidth + " leading=" + leading + " height=" + height + "}";
    }

    public int _ascent() {
        return ascent;
    }

    public int _descent() {
        return descent;
    }

    public int _leading() {
        return leading;
    }

    public int _height() {
        return height;
    }

    public double _averageCharWidth() {
        return averageCharWidth;
    }

    public FontMetrics getApi() {
        if (api == null)
            api = FontMetrics.createApi(this);
        return (FontMetrics) api;
    }

    protected FontMetrics api;

    public void setApi(FontMetrics api) {
        this.api = api;
        if (api != null)
            api.impl = this;
    }

    protected VFontMetrics value;

    public VFontMetrics getValue() {
        if (value == null)
            value = new VFontMetrics(this);
        return (VFontMetrics) value;
    }
}
