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
package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

/**
 * <code>StyleRange</code> defines a set of styles for a specified
 * range of text.
 * <p>
 * The hashCode() method in this class uses the values of the public
 * fields to compute the hash value. When storing instances of the
 * class in hashed collections, do not modify these fields after the
 * object has been inserted.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public class SwtStyleRange extends SwtTextStyle implements Cloneable, IStyleRange {

    /**
     * Create a new style range with no styles
     *
     * @since 3.2
     */
    public SwtStyleRange(StyleRange api) {
        super(api);
    }

    /**
     * Create a new style range from an existing text style.
     *
     * @param style the text style to copy
     *
     * @since 3.4
     */
    public SwtStyleRange(TextStyle style, StyleRange api) {
        super(style, api);
    }

    /**
     * Create a new style range.
     *
     * @param start start offset of the style
     * @param length length of the style
     * @param foreground foreground color of the style, null if none
     * @param background background color of the style, null if none
     */
    public SwtStyleRange(int start, int length, Color foreground, Color background, StyleRange api) {
        super(null, foreground, background, api);
        this.getApi().start = start;
        this.getApi().length = length;
    }

    /**
     * Create a new style range.
     *
     * @param start start offset of the style
     * @param length length of the style
     * @param foreground foreground color of the style, null if none
     * @param background background color of the style, null if none
     * @param fontStyle font style of the style, may be SWT.NORMAL, SWT.ITALIC or SWT.BOLD
     */
    public SwtStyleRange(int start, int length, Color foreground, Color background, int fontStyle, StyleRange api) {
        this(start, length, foreground, background, api);
        this.getApi().fontStyle = fontStyle;
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
        if (object instanceof StyleRange style) {
            if (getApi().start != style.start)
                return false;
            if (getApi().length != style.length)
                return false;
            return similarTo(style);
        }
        return false;
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
        return super.hashCode() ^ getApi().fontStyle;
    }

    boolean isVariableHeight() {
        return getApi().font != null || (getApi().metrics != null && (getApi().metrics.ascent != 0 || getApi().metrics.descent != 0)) || getApi().rise != 0;
    }

    /**
     * Returns whether or not the receiver is unstyled (i.e., does not have any
     * style attributes specified).
     *
     * @return true if the receiver is unstyled, false otherwise.
     */
    public boolean isUnstyled() {
        if (getApi().font != null)
            return false;
        if (getApi().rise != 0)
            return false;
        if (getApi().metrics != null)
            return false;
        if (getApi().foreground != null)
            return false;
        if (getApi().background != null)
            return false;
        if (getApi().fontStyle != SWT.NORMAL)
            return false;
        if (getApi().underline)
            return false;
        if (getApi().strikeout)
            return false;
        if (getApi().borderStyle != SWT.NONE)
            return false;
        return true;
    }

    /**
     * Compares the specified object to this StyleRange and answer if the two
     * are similar. The object must be an instance of StyleRange and have the
     * same field values for except for start and length.
     *
     * @param style the object to compare with this object
     * @return true if the objects are similar, false otherwise
     */
    public boolean similarTo(StyleRange style) {
        if (!super.equals(style))
            return false;
        if (getApi().fontStyle != style.fontStyle)
            return false;
        return true;
    }

    /**
     * Returns a new StyleRange with the same values as this StyleRange.
     *
     * @return a shallow copy of this StyleRange
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the StyleRange
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("StyleRange {");
        buffer.append(getApi().start);
        buffer.append(", ");
        buffer.append(getApi().length);
        buffer.append(", fontStyle=");
        switch(getApi().fontStyle) {
            case SWT.BOLD:
                buffer.append("bold");
                break;
            case SWT.ITALIC:
                buffer.append("italic");
                break;
            case SWT.BOLD | SWT.ITALIC:
                buffer.append("bold-italic");
                break;
            default:
                buffer.append("normal");
        }
        String str = super.toString();
        int index = str.indexOf('{');
        str = str.substring(index + 1);
        if (str.length() > 1)
            buffer.append(", ");
        buffer.append(str);
        return buffer.toString();
    }

    public StyleRange getApi() {
        if (api == null)
            api = StyleRange.createApi(this);
        return (StyleRange) api;
    }
}
