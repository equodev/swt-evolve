package org.eclipse.swt.graphics;

import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;

public interface IFontData {

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
     * Returns the height of the receiver in points.
     *
     * @return the height of this FontData
     *
     * @see #setHeight(int)
     */
    int getHeight();

    /**
     * Returns the locale of the receiver.
     * <p>
     * The locale determines which platform character set this
     * font is going to use. Widgets and graphics operations that
     * use this font will convert UNICODE strings to the platform
     * character set of the specified locale.
     * </p>
     * <p>
     * On platforms where there are multiple character sets for a
     * given language/country locale, the variant portion of the
     * locale will determine the character set.
     * </p>
     *
     * @return the <code>String</code> representing a Locale object
     * @since 3.0
     */
    String getLocale();

    /**
     * Returns the name of the receiver.
     * On platforms that support font foundries, the return value will
     * be the foundry followed by a dash ("-") followed by the face name.
     *
     * @return the name of this <code>FontData</code>
     *
     * @see #setName
     */
    String getName();

    /**
     * Returns the style of the receiver which is a bitwise OR of
     * one or more of the <code>SWT</code> constants NORMAL, BOLD
     * and ITALIC.
     *
     * @return the style of this <code>FontData</code>
     *
     * @see #setStyle
     */
    int getStyle();

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

    /**
     * Sets the height of the receiver. The parameter is
     * specified in terms of points, where a point is one
     * seventy-second of an inch.
     *
     * @param height the height of the <code>FontData</code>
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the height is negative</li>
     * </ul>
     *
     * @see #getHeight
     */
    void setHeight(int height);

    /**
     * Sets the locale of the receiver.
     * <p>
     * The locale determines which platform character set this
     * font is going to use. Widgets and graphics operations that
     * use this font will convert UNICODE strings to the platform
     * character set of the specified locale.
     * </p>
     * <p>
     * On platforms where there are multiple character sets for a
     * given language/country locale, the variant portion of the
     * locale will determine the character set.
     * </p>
     *
     * @param locale the <code>String</code> representing a Locale object
     * @see java.util.Locale#toString
     */
    void setLocale(String locale);

    /**
     * Sets the name of the receiver.
     * <p>
     * Some platforms support font foundries. On these platforms, the name
     * of the font specified in setName() may have one of the following forms:</p>
     * <ol>
     * <li>a face name (for example, "courier")</li>
     * <li>a foundry followed by a dash ("-") followed by a face name (for example, "adobe-courier")</li>
     * </ol>
     * <p>
     * In either case, the name returned from getName() will include the
     * foundry.
     * </p>
     * <p>
     * On platforms that do not support font foundries, only the face name
     * (for example, "courier") is used in <code>setName()</code> and
     * <code>getName()</code>.
     * </p>
     *
     * @param name the name of the font data (must not be null)
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - when the font name is null</li>
     * </ul>
     *
     * @see #getName
     */
    void setName(String name);

    /**
     * Sets the style of the receiver to the argument which must
     * be a bitwise OR of one or more of the <code>SWT</code>
     * constants NORMAL, BOLD and ITALIC.  All other style bits are
     * ignored.
     *
     * @param style the new style for this <code>FontData</code>
     *
     * @see #getStyle
     */
    void setStyle(int style);

    /**
     * Returns a string representation of the receiver which is suitable
     * for constructing an equivalent instance using the
     * <code>FontData(String)</code> constructor.
     *
     * @return a string representation of the FontData
     *
     * @see FontData
     */
    String toString();

    FontData getApi();

    void setApi(FontData api);
}
