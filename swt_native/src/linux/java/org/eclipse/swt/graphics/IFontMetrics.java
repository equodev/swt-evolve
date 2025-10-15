package org.eclipse.swt.graphics;

public interface IFontMetrics extends ImplFontMetrics {

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
    boolean equals(Object object);

    /**
     * Returns the ascent of the font described by the receiver. A
     * font's <em>ascent</em> is the distance from the baseline to the
     * top of actual characters, not including any of the leading area,
     * measured in points.
     *
     * @return the ascent of the font
     */
    int getAscent();

    /**
     * Returns the average character width, measured in points,
     * of the font described by the receiver.
     *
     * @return the average character width of the font
     * @since 3.107
     */
    double getAverageCharacterWidth();

    /**
     * Returns the average character width, measured in points,
     * of the font described by the receiver.
     *
     * @return the average character width of the font
     * @deprecated Use getAverageCharacterWidth() instead
     */
    int getAverageCharWidth();

    /**
     * Returns the descent of the font described by the receiver. A
     * font's <em>descent</em> is the distance from the baseline to the
     * bottom of actual characters, not including any of the leading area,
     * measured in points.
     *
     * @return the descent of the font
     */
    int getDescent();

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
    int getHeight();

    /**
     * Returns the leading area of the font described by the
     * receiver. A font's <em>leading area</em> is the space
     * above its ascent which may include accents or other marks.
     *
     * @return the leading space of the font
     */
    int getLeading();

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
    int hashCode();

    FontMetrics getApi();

    void setApi(FontMetrics api);
}
