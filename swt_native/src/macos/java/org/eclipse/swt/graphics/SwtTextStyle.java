/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2018 IBM Corporation and others.
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
 * <code>TextStyle</code> defines a set of styles that can be applied
 * to a range of text.
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
 * @see TextLayout
 * @see Font
 * @see Color
 * @see <a href="http://www.eclipse.org/swt/snippets/#textlayout">TextLayout, TextStyle snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.0
 */
public class SwtTextStyle implements ITextStyle {

    /**
     * Create an empty text style.
     *
     * @since 3.4
     */
    public SwtTextStyle() {
    }

    /**
     * Create a new text style with the specified font, foreground
     * and background.
     *
     * @param font the font of the style, <code>null</code> if none
     * @param foreground the foreground color of the style, <code>null</code> if none
     * @param background the background color of the style, <code>null</code> if none
     */
    public SwtTextStyle(Font font, Color foreground, Color background) {
        if (font != null && font.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        if (foreground != null && foreground.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        if (background != null && background.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        this.getApi().font = font;
        this.getApi().foreground = foreground;
        this.getApi().background = background;
    }

    /**
     * Create a new text style from an existing text style.
     *
     * @param style the style to copy
     *
     * @since 3.4
     */
    public SwtTextStyle(TextStyle style) {
        if (style == null)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        getApi().font = style.font;
        getApi().foreground = style.foreground;
        getApi().background = style.background;
        getApi().underline = style.underline;
        getApi().underlineColor = style.underlineColor;
        getApi().underlineStyle = style.underlineStyle;
        getApi().strikeout = style.strikeout;
        getApi().strikeoutColor = style.strikeoutColor;
        getApi().borderStyle = style.borderStyle;
        getApi().borderColor = style.borderColor;
        getApi().metrics = style.metrics;
        getApi().rise = style.rise;
        getApi().data = style.data;
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
        if (object == null)
            return false;
        if (!(object instanceof TextStyle style))
            return false;
        if (getApi().foreground != null) {
            if (!getApi().foreground.equals(style.foreground))
                return false;
        } else if (style.foreground != null)
            return false;
        if (getApi().background != null) {
            if (!getApi().background.equals(style.background))
                return false;
        } else if (style.background != null)
            return false;
        if (getApi().font != null) {
            if (!getApi().font.equals(style.font))
                return false;
        } else if (style.font != null)
            return false;
        if (getApi().metrics != null) {
            if (!getApi().metrics.equals(style.metrics))
                return false;
        } else if (style.metrics != null)
            return false;
        if (getApi().underline != style.underline)
            return false;
        if (getApi().underlineStyle != style.underlineStyle)
            return false;
        if (getApi().borderStyle != style.borderStyle)
            return false;
        if (getApi().strikeout != style.strikeout)
            return false;
        if (getApi().rise != style.rise)
            return false;
        if (getApi().underlineColor != null) {
            if (!getApi().underlineColor.equals(style.underlineColor))
                return false;
        } else if (style.underlineColor != null)
            return false;
        if (getApi().strikeoutColor != null) {
            if (!getApi().strikeoutColor.equals(style.strikeoutColor))
                return false;
        } else if (style.strikeoutColor != null)
            return false;
        if (getApi().underlineStyle != style.underlineStyle)
            return false;
        if (getApi().borderColor != null) {
            if (!getApi().borderColor.equals(style.borderColor))
                return false;
        } else if (style.borderColor != null)
            return false;
        if (getApi().data != null) {
            if (!getApi().data.equals(style.data))
                return false;
        } else if (style.data != null)
            return false;
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
        int hash = 0;
        if (getApi().foreground != null)
            hash ^= getApi().foreground.hashCode();
        if (getApi().background != null)
            hash ^= getApi().background.hashCode();
        if (getApi().font != null)
            hash ^= getApi().font.hashCode();
        if (getApi().metrics != null)
            hash ^= getApi().metrics.hashCode();
        if (getApi().underline)
            hash ^= (hash << 1);
        if (getApi().strikeout)
            hash ^= (hash << 2);
        hash ^= getApi().rise;
        if (getApi().underlineColor != null)
            hash ^= getApi().underlineColor.hashCode();
        if (getApi().strikeoutColor != null)
            hash ^= getApi().strikeoutColor.hashCode();
        if (getApi().borderColor != null)
            hash ^= getApi().borderColor.hashCode();
        hash ^= getApi().underlineStyle;
        return hash;
    }

    boolean isAdherentBorder(TextStyle style) {
        if (this.getApi() == style)
            return true;
        if (style == null)
            return false;
        if (getApi().borderStyle != style.borderStyle)
            return false;
        if (getApi().borderColor != null) {
            if (!getApi().borderColor.equals(style.borderColor))
                return false;
        } else {
            if (style.borderColor != null)
                return false;
            if (getApi().foreground != null) {
                if (!getApi().foreground.equals(style.foreground))
                    return false;
            } else if (style.foreground != null)
                return false;
        }
        return true;
    }

    boolean isAdherentUnderline(TextStyle style) {
        if (this.getApi() == style)
            return true;
        if (style == null)
            return false;
        if (getApi().underline != style.underline)
            return false;
        if (getApi().underlineStyle != style.underlineStyle)
            return false;
        if (getApi().underlineColor != null) {
            if (!getApi().underlineColor.equals(style.underlineColor))
                return false;
        } else {
            if (style.underlineColor != null)
                return false;
            if (getApi().foreground != null) {
                if (!getApi().foreground.equals(style.foreground))
                    return false;
            } else if (style.foreground != null)
                return false;
        }
        return true;
    }

    boolean isAdherentStrikeout(TextStyle style) {
        if (this.getApi() == style)
            return true;
        if (style == null)
            return false;
        if (getApi().strikeout != style.strikeout)
            return false;
        if (getApi().strikeoutColor != null) {
            if (!getApi().strikeoutColor.equals(style.strikeoutColor))
                return false;
        } else {
            if (style.strikeoutColor != null)
                return false;
            if (getApi().foreground != null) {
                if (!getApi().foreground.equals(style.foreground))
                    return false;
            } else if (style.foreground != null)
                return false;
        }
        return true;
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the <code>TextStyle</code>
     */
    @Override
    public String toString() {
        //$NON-NLS-1$
        StringBuilder buffer = new StringBuilder("TextStyle {");
        int startLength = buffer.length();
        if (getApi().font != null) {
            //$NON-NLS-1$
            if (buffer.length() > startLength)
                buffer.append(", ");
            //$NON-NLS-1$
            buffer.append("font=");
            buffer.append(getApi().font);
        }
        if (getApi().foreground != null) {
            //$NON-NLS-1$
            if (buffer.length() > startLength)
                buffer.append(", ");
            //$NON-NLS-1$
            buffer.append("foreground=");
            buffer.append(getApi().foreground);
        }
        if (getApi().background != null) {
            //$NON-NLS-1$
            if (buffer.length() > startLength)
                buffer.append(", ");
            //$NON-NLS-1$
            buffer.append("background=");
            buffer.append(getApi().background);
        }
        if (getApi().underline) {
            //$NON-NLS-1$
            if (buffer.length() > startLength)
                buffer.append(", ");
            //$NON-NLS-1$
            buffer.append("underline=");
            switch(getApi().underlineStyle) {
                //$NON-NLS-1$
                case SWT.UNDERLINE_SINGLE:
                    buffer.append("single");
                    break;
                //$NON-NLS-1$
                case SWT.UNDERLINE_DOUBLE:
                    buffer.append("double");
                    break;
                //$NON-NLS-1$
                case SWT.UNDERLINE_SQUIGGLE:
                    buffer.append("squiggle");
                    break;
                //$NON-NLS-1$
                case SWT.UNDERLINE_ERROR:
                    buffer.append("error");
                    break;
                //$NON-NLS-1$
                case SWT.UNDERLINE_LINK:
                    buffer.append("link");
                    break;
            }
            if (getApi().underlineColor != null) {
                //$NON-NLS-1$
                buffer.append(", underlineColor=");
                buffer.append(getApi().underlineColor);
            }
        }
        if (getApi().strikeout) {
            //$NON-NLS-1$
            if (buffer.length() > startLength)
                buffer.append(", ");
            //$NON-NLS-1$
            buffer.append("striked out");
            if (getApi().strikeoutColor != null) {
                //$NON-NLS-1$
                buffer.append(", strikeoutColor=");
                buffer.append(getApi().strikeoutColor);
            }
        }
        if (getApi().borderStyle != SWT.NONE) {
            //$NON-NLS-1$
            if (buffer.length() > startLength)
                buffer.append(", ");
            //$NON-NLS-1$
            buffer.append("border=");
            switch(getApi().borderStyle) {
                //$NON-NLS-1$
                case SWT.BORDER_SOLID:
                    buffer.append("solid");
                    break;
                //$NON-NLS-1$
                case SWT.BORDER_DOT:
                    buffer.append("dot");
                    break;
                //$NON-NLS-1$
                case SWT.BORDER_DASH:
                    buffer.append("dash");
                    break;
            }
            if (getApi().borderColor != null) {
                //$NON-NLS-1$
                buffer.append(", borderColor=");
                buffer.append(getApi().borderColor);
            }
        }
        if (getApi().rise != 0) {
            //$NON-NLS-1$
            if (buffer.length() > startLength)
                buffer.append(", ");
            //$NON-NLS-1$
            buffer.append("rise=");
            buffer.append(getApi().rise);
        }
        if (getApi().metrics != null) {
            //$NON-NLS-1$
            if (buffer.length() > startLength)
                buffer.append(", ");
            //$NON-NLS-1$
            buffer.append("metrics=");
            buffer.append(getApi().metrics);
        }
        //$NON-NLS-1$
        buffer.append("}");
        return buffer.toString();
    }

    public TextStyle getApi() {
        if (api == null)
            api = TextStyle.createApi(this);
        return (TextStyle) api;
    }

    protected TextStyle api;

    public void setApi(TextStyle api) {
        this.api = api;
    }
}
