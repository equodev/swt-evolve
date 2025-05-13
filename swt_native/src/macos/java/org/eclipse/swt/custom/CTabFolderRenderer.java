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

    ICTabFolderRenderer delegate;

    protected CTabFolderRenderer(ICTabFolderRenderer delegate) {
        this.delegate = delegate;
        delegate.setApi(this);
    }

    public ICTabFolderRenderer getDelegate() {
        return delegate;
    }
}
