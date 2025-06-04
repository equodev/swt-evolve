/**
 * ****************************************************************************
 *  Copyright (c) 2022 Christoph Läubrich and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Christoph Läubrich - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.swt.layout;

import static org.eclipse.swt.SWT.*;
import java.util.*;
import java.util.AbstractMap.*;
import java.util.List;
import java.util.Map.*;
import java.util.function.*;
import java.util.stream.*;
import java.util.stream.IntStream.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * {@link BorderLayout} places controls in five regions
 *
 * <pre>
 * +--------------------------------+
 * |       NORTH / TOP              |
 * +---+------------------------+---+
 * |   |                        |   |
 * | W |                        | E |
 * | E |                        | A |
 * | S |                        | S |
 * | T |                        | T |
 * | / |                        | / |
 * | L |       CENTER           | R |
 * | E |                        | I |
 * | F |                        | G |
 * | T |                        | H |
 * |   |                        | T |
 * |   |                        |   |
 * +---+------------------------+---+
 * |       SOUTH / BOTTOM           |
 * +--------------------------------+
 * </pre>
 *
 * The controls at the NORTH/SOUTH borders get their preferred heights, the
 * controls at the EAST/WEST get their preferred widths and the center region
 * grow/shrink according to the remaining space. If more than one control is
 * placed inside a region the controls are equally distributed across their axis
 * where the grow (CENTER controlled by the {@link BorderLayout#type} value)
 *
 * @since 3.119
 */
public class BorderLayout extends Layout {

    /**
     * type specifies how controls will be positioned within the center region.
     *
     * The default value is {@link SWT#HORIZONTAL}.
     *
     * Possible values are:
     * <ul>
     * <li>{@link SWT#HORIZONTAL}: Position the controls horizontally from left to
     * right</li>
     * <li>{@link SWT#VERTICAL}: Position the controls vertically from top to
     * bottom</li>
     * </ul>
     */
    public int type = SWT.HORIZONTAL;

    /**
     * marginWidth specifies the number of points of horizontal margin that will be
     * placed along the left and right edges of the layout.
     *
     * The default value is 0.
     */
    public int marginWidth = 0;

    /**
     * marginHeight specifies the number of points of vertical margin that will be
     * placed along the top and bottom edges of the layout.
     *
     * The default value is 0.
     */
    public int marginHeight = 0;

    /**
     * spacing specifies the number of points between the edge of one region and its
     * neighboring regions.
     *
     * The default value is 0.
     */
    public int spacing = 0;

    /**
     * controlSpacing specifies the number of points between the edge of one control
     * and its neighboring control inside a region.
     *
     * The default value is 0.
     */
    public int controlSpacing = 0;

    /**
     * If the width of the {@link SWT#LEFT} and {@link SWT#RIGHT} region exceeds the
     * available space this factor is used to distribute the size to the controls,
     * valid values range between [0 ... 1]
     *
     * The default value is 0.5 (equal distribution of available space)
     */
    public double widthDistributionFactor = 0.5;

    /**
     * If the height of the {@link SWT#TOP} and {@link SWT#BOTTOM} region exceeds
     * the available space this factor is used to distribute the size to the
     * controls, valid values range between [0 ... 1]
     *
     * The default value is 0.5 (equal distribution of available space)
     */
    public double heightDistributionFactor = 0.5;

    protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        return getImpl().computeSize(composite, wHint, hHint, flushCache);
    }

    protected void layout(Composite composite, boolean flushCache) {
        getImpl().layout(composite, flushCache);
    }

    public String toString() {
        return getImpl().toString();
    }

    public BorderLayout() {
        this((IBorderLayout) null);
        setImpl(new SwtBorderLayout());
    }

    protected BorderLayout(IBorderLayout impl) {
        super(impl);
    }

    static BorderLayout createApi(IBorderLayout impl) {
        if (dev.equo.swt.Creation.creating.peek() instanceof BorderLayout inst) {
            inst.impl = impl;
            return inst;
        } else
            return new BorderLayout(impl);
    }

    public IBorderLayout getImpl() {
        return (IBorderLayout) super.getImpl();
    }
}
