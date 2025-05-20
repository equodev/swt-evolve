/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2020 IBM Corporation and others.
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
 *      Lars Vogel <Lars.Vogel@vogella.com> - Bug 455263
 * *****************************************************************************
 */
package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * Instances of this class provide all of the measuring and drawing functionality
 * required by <code>CTabFolder</code>. This class can be subclassed in order to
 * customize the look of a CTabFolder.
 *
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @since 3.6
 */
public class CTabFolderRenderer {

    /* Selected item appearance */
    //Although we are given new colours all the time to show different states (active, etc),
    //some of which may have a highlight and some not, we'd like to retain the highlight colours
    //as a cache so that we can reuse them if we're again told to show the highlight.
    //We are relying on the fact that only one tab state usually gets a highlight, so only
    //a single cache is required. If that happens to not be true, cache simply becomes less effective,
    //but we don't leak colours.
    //TOP_LEFT_CORNER_HILITE is laid out in reverse (ie. top to bottom)
    //Part constants
    /**
     * Part constant indicating the body of the tab folder. The body is the
     * underlying container for all of the tab folder and all other parts are
     * drawn on top of it. (value is -1).
     *
     * @see #computeSize(int, int, GC, int, int)
     * @see #computeTrim(int, int, int, int, int, int)
     * @see #draw(int, int, Rectangle, GC)
     */
    public static final int PART_BODY = -1;

    /**
     * Part constant indicating the tab header of the folder (value is -2). The
     * header is drawn on top of the body and provides an area for the tabs and
     * other tab folder buttons to be rendered.
     *
     * @see #computeSize(int, int, GC, int, int)
     * @see #computeTrim(int, int, int, int, int, int)
     * @see #draw(int, int, Rectangle, GC)
     */
    public static final int PART_HEADER = -2;

    /**
     * Part constant indicating the border of the tab folder. (value is -3). The
     * border is drawn around the body and is part of the body trim.
     *
     * @see #computeSize(int, int, GC, int, int)
     * @see #computeTrim(int, int, int, int, int, int)
     * @see #draw(int, int, Rectangle, GC)
     */
    public static final int PART_BORDER = -3;

    /**
     * Part constant indicating the background of the tab folder. (value is -4).
     *
     * @see #computeSize(int, int, GC, int, int)
     * @see #computeTrim(int, int, int, int, int, int)
     * @see #draw(int, int, Rectangle, GC)
     */
    public static final int PART_BACKGROUND = -4;

    /**
     * Part constant indicating the maximize button of the tab folder. (value is
     * -5).
     *
     * @see #computeSize(int, int, GC, int, int)
     * @see #computeTrim(int, int, int, int, int, int)
     * @see #draw(int, int, Rectangle, GC)
     */
    public static final int PART_MAX_BUTTON = -5;

    /**
     * Part constant indicating the minimize button of the tab folder. (value is
     * -6).
     *
     * @see #computeSize(int, int, GC, int, int)
     * @see #computeTrim(int, int, int, int, int, int)
     * @see #draw(int, int, Rectangle, GC)
     */
    public static final int PART_MIN_BUTTON = -6;

    /**
     * Part constant indicating the chevron button of the tab folder. (value is
     * -7).
     *
     * @see #computeSize(int, int, GC, int, int)
     * @see #computeTrim(int, int, int, int, int, int)
     * @see #draw(int, int, Rectangle, GC)
     */
    public static final int PART_CHEVRON_BUTTON = -7;

    /**
     * Part constant indicating the close button of a tab item. (value is -8).
     *
     * @see #computeSize(int, int, GC, int, int)
     * @see #computeTrim(int, int, int, int, int, int)
     * @see #draw(int, int, Rectangle, GC)
     */
    public static final int PART_CLOSE_BUTTON = -8;

    //TODO: Should this be a state?
    public static final int MINIMUM_SIZE = 1 << 24;

    /**
     * Constructs a new instance of this class given its parent.
     *
     * @param parent CTabFolder
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the parent is disposed</li>
     * </ul>
     *
     * @see Widget#getStyle
     */
    protected CTabFolderRenderer(CTabFolder parent) {
        this((ICTabFolderRenderer) null);
        setImpl(new SwtCTabFolderRenderer(parent));
    }

    /**
     * Returns the preferred size of a part.
     * <p>
     * The <em>preferred size</em> of a part is the size that it would
     * best be displayed at. The width hint and height hint arguments
     * allow the caller to ask a control questions such as "Given a particular
     * width, how high does the part need to be to show all of the contents?"
     * To indicate that the caller does not wish to constrain a particular
     * dimension, the constant <code>SWT.DEFAULT</code> is passed for the hint.
     * </p><p>
     * The <code>part</code> value indicated what component the preferred size is
     * to be calculated for. Valid values are any of the part constants:
     * </p>
     * <ul>
     * <li>PART_BODY</li>
     * <li>PART_HEADER</li>
     * <li>PART_BORDER</li>
     * <li>PART_BACKGROUND</li>
     * <li>PART_MAX_BUTTON</li>
     * <li>PART_MIN_BUTTON</li>
     * <li>PART_CHEVRON_BUTTON</li>
     * <li>PART_CLOSE_BUTTON</li>
     * <li>A positive integer which is the index of an item in the CTabFolder.</li>
     * </ul>
     * <p>
     * The <code>state</code> parameter may be one of the following:
     * </p>
     * <ul>
     * <li>SWT.NONE</li>
     * <li>SWT.SELECTED - whether the part is selected</li>
     * </ul>
     * @param part a part constant
     * @param state current state
     * @param gc the gc to use for measuring
     * @param wHint the width hint (can be <code>SWT.DEFAULT</code>)
     * @param hHint the height hint (can be <code>SWT.DEFAULT</code>)
     * @return the preferred size of the part
     *
     * @since 3.6
     */
    protected Point computeSize(int part, int state, GC gc, int wHint, int hHint) {
        return getImpl().computeSize(part, state, gc, wHint, hHint);
    }

    /**
     * Given a desired <em>client area</em> for the part
     * (as described by the arguments), returns the bounding
     * rectangle which would be required to produce that client
     * area.
     * <p>
     * In other words, it returns a rectangle such that, if the
     * part's bounds were set to that rectangle, the area
     * of the part which is capable of displaying data
     * (that is, not covered by the "trimmings") would be the
     * rectangle described by the arguments (relative to the
     * receiver's parent).
     * </p>
     *
     * @param part one of the part constants
     * @param state the state of the part
     * @param x the desired x coordinate of the client area
     * @param y the desired y coordinate of the client area
     * @param width the desired width of the client area
     * @param height the desired height of the client area
     * @return the required bounds to produce the given client area
     *
     * @see CTabFolderRenderer#computeSize(int, int, GC, int, int) valid part and state values
     *
     * @since 3.6
     */
    protected Rectangle computeTrim(int part, int state, int x, int y, int width, int height) {
        return getImpl().computeTrim(part, state, x, y, width, height);
    }

    /**
     * Dispose of any operating system resources associated with
     * the renderer. Called by the CTabFolder parent upon receiving
     * the dispose event or when changing the renderer.
     *
     * @since 3.6
     */
    protected void dispose() {
        getImpl().dispose();
    }

    /**
     * Draw a specified <code>part</code> of the CTabFolder using the provided <code>bounds</code> and <code>GC</code>.
     * <p>The valid CTabFolder <code>part</code> constants are:
     * </p>
     * <ul>
     * <li>PART_BODY - the entire body of the CTabFolder</li>
     * <li>PART_HEADER - the upper tab area of the CTabFolder</li>
     * <li>PART_BORDER - the border of the CTabFolder</li>
     * <li>PART_BACKGROUND - the background of the CTabFolder</li>
     * <li>PART_MAX_BUTTON</li>
     * <li>PART_MIN_BUTTON</li>
     * <li>PART_CHEVRON_BUTTON</li>
     * <li>PART_CLOSE_BUTTON</li>
     * <li>A positive integer which is the index of an item in the CTabFolder.</li>
     * </ul>
     * <p>
     * The <code>state</code> parameter may be a combination of:
     * </p>
     * <ul>
     * <li>SWT.BACKGROUND - whether the background should be drawn</li>
     * <li>SWT.FOREGROUND - whether the foreground should be drawn</li>
     * <li>SWT.SELECTED - whether the part is selected</li>
     * <li>SWT.HOT - whether the part is hot (i.e. mouse is over the part)</li>
     * </ul>
     *
     * @param part part to draw
     * @param state state of the part
     * @param bounds the bounds of the part
     * @param gc the gc to draw the part on
     *
     * @since 3.6
     */
    protected void draw(int part, int state, Rectangle bounds, GC gc) {
        getImpl().draw(part, state, bounds, gc);
    }

    protected ICTabFolderRenderer impl;

    protected CTabFolderRenderer(ICTabFolderRenderer impl) {
        if (impl == null)
            dev.equo.swt.Creation.creating.push(this);
        else
            setImpl(impl);
    }

    static CTabFolderRenderer createApi(ICTabFolderRenderer impl) {
        if (dev.equo.swt.Creation.creating.peek() instanceof CTabFolderRenderer inst) {
            inst.impl = impl;
            return inst;
        } else
            return new CTabFolderRenderer(impl);
    }

    public ICTabFolderRenderer getImpl() {
        return impl;
    }

    protected CTabFolderRenderer setImpl(ICTabFolderRenderer impl) {
        this.impl = impl;
        impl.setApi(this);
        dev.equo.swt.Creation.creating.pop();
        return this;
    }
}
