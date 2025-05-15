/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2008 IBM Corporation and others.
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

/**
 * Instances of this class represent bullets in the <code>StyledText</code>.
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
 * @see StyledText#setLineBullet(int, int, Bullet)
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.2
 */
public class Bullet {

    /**
     * The bullet type.  Possible values are:
     * <ul>
     * <li><code>ST.BULLET_DOT</code></li>
     * <li><code>ST.BULLET_NUMBER</code></li>
     * <li><code>ST.BULLET_LETTER_LOWER</code></li>
     * <li><code>ST.BULLET_LETTER_UPPER</code></li>
     * <li><code>ST.BULLET_TEXT</code></li>
     * <li><code>ST.BULLET_CUSTOM</code></li>
     * </ul>
     */
    public int type;

    /**
     * The bullet style.
     */
    public StyleRange style;

    /**
     * The bullet text.
     */
    public String text;

    /**
     * Create a new bullet with the specified style, and type <code>ST.BULLET_DOT</code>.
     * The style must have a glyph metrics set.
     *
     * @param style the style
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when the style or the glyph metrics are null</li>
     * </ul>
     */
    public Bullet(StyleRange style) {
        this(new nat.org.eclipse.swt.custom.Bullet((nat.org.eclipse.swt.custom.StyleRange) style.getDelegate()));
    }

    /**
     * Create a new bullet the specified style and type.
     * The style must have a glyph metrics set.
     *
     * @param type the bullet type
     * @param style the style
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when the style or the glyph metrics are null</li>
     * </ul>
     */
    public Bullet(int type, StyleRange style) {
        this(new nat.org.eclipse.swt.custom.Bullet(type, (nat.org.eclipse.swt.custom.StyleRange) style.getDelegate()));
    }

    public int hashCode() {
        return getDelegate().hashCode();
    }

    IBullet delegate;

    protected Bullet(IBullet delegate) {
        this.delegate = delegate;
        delegate.setApi(this);
    }

    public static Bullet createApi(IBullet delegate) {
        return new Bullet(delegate);
    }

    public IBullet getDelegate() {
        return delegate;
    }
}
