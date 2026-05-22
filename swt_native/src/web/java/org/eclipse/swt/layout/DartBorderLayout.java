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
import dev.equo.swt.*;

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
public class DartBorderLayout extends DartLayout implements IBorderLayout {

    private static final String LAYOUT_KEY = DartBorderLayout.class.getName() + ".layoutData";

    private static final ToIntFunction<Point> WIDTH = p -> p.x;

    private static final ToIntFunction<Point> HEIGHT = p -> p.y;

    @Override
    public Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        if (hHint > SWT.DEFAULT && wHint > SWT.DEFAULT) {
            return new Point(wHint, hHint);
        }
        int width;
        if (wHint <= SWT.DEFAULT) {
            Builder widthBuilder = IntStream.builder();
            if (getApi().type == SWT.HORIZONTAL) {
            } else {
            }
            width = widthBuilder.build().max().orElse(0) + 2 * getApi().marginWidth;
        } else {
            width = wHint;
        }
        int height;
        if (hHint <= SWT.DEFAULT) {
            Builder heightBuilder = IntStream.builder();
            if (getApi().type == SWT.HORIZONTAL) {
            } else {
            }
            height = heightBuilder.build().max().orElse(0) + 2 * getApi().marginHeight;
        } else {
            height = hHint;
        }
        return new Point(width, height);
    }

    /**
     * Calculates the total W/H according to the extractor
     *
     * @param extractor either {@link #WIDTH} or {@link #HEIGHT}
     * @param region    the region to compute
     * @param regionMap the map of regions
     * @return the total W/H including the {@link #controlSpacing}
     */
    private int getTotal(ToIntFunction<Point> extractor, int region, Map<Integer, java.util.List<Entry<Control, BorderData>>> regionMap) {
        java.util.List<Entry<Control, BorderData>> list = regionMap.getOrDefault(region, Collections.emptyList());
        if (list.isEmpty()) {
            return 0;
        }
        return list.stream().mapToInt(entry -> extractor.applyAsInt(entry.getValue().getSize(entry.getKey()))).sum() + ((list.size() - 1) * getApi().controlSpacing);
    }

    private static int getMax(ToIntFunction<Point> extractor, int region, Map<Integer, java.util.List<Entry<Control, BorderData>>> regionMap) {
        java.util.List<Entry<Control, BorderData>> list = regionMap.getOrDefault(region, Collections.emptyList());
        return getMax(extractor, list, SWT.DEFAULT, SWT.DEFAULT, false);
    }

    private static int getMax(ToIntFunction<Point> extractor, java.util.List<Entry<Control, BorderData>> list, int maxW, int maxH, boolean flushCache) {
        if (list.isEmpty()) {
            return 0;
        }
        if (maxW != SWT.DEFAULT || maxH != SWT.DEFAULT) {
            // we need to compute a restricted size to at least one of the given sizes
            return list.stream().mapToInt(entry -> extractor.applyAsInt(entry.getValue().computeSize(entry.getKey(), maxW, maxH, flushCache))).max().orElse(0);
        }
        return list.stream().mapToInt(entry -> extractor.applyAsInt(entry.getValue().getSize(entry.getKey()))).max().orElse(0);
    }

    @Override
    public void layout(Composite composite, boolean flushCache) {
        // remaining height for WEST and EAST, preferred width for WEST and EAST if
    }

    private static int region(Entry<Control, BorderData> entry) {
        BorderData borderData = entry.getValue();
        if (borderData == null) {
            // we assume all controls without explicit data to be placed in the center area
            return SWT.CENTER;
        }
        return borderData.getRegion();
    }

    @Override
    public String toString() {
        return //
        "BorderLayout [" + "type=" + //
        ((getApi().type == SWT.HORIZONTAL) ? "SWT.HORIZONTAL" : "SWT.VERTICAL") + ", marginWidth=" + getApi().marginWidth + ", marginHeight=" + getApi().marginHeight + ", spacing=" + getApi().spacing + ", controlSpacing=" + getApi().controlSpacing + ", widthDistributionFactor=" + getApi().widthDistributionFactor + ", heightDistributionFactor=" + getApi().heightDistributionFactor + "]";
    }

    public BorderLayout getApi() {
        if (api == null)
            api = BorderLayout.createApi(this);
        return (BorderLayout) api;
    }

    public DartBorderLayout(BorderLayout api) {
        super(api);
    }
}
