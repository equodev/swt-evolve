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
        Rectangle clientArea = composite.getClientArea();
        int clientX = clientArea.x + getApi().marginWidth;
        int clientY = clientArea.y + getApi().marginHeight;
        int clientWidth = clientArea.width - 2 * getApi().marginWidth;
        int clientHeight = clientArea.height - 2 * getApi().marginHeight;
        java.util.stream.Stream<java.util.Map.Entry<Control, BorderData>> children = Arrays.stream(composite.getChildren()).map(control -> borderDataControl(control, flushCache));
        Map<Integer, java.util.List<java.util.Map.Entry<Control, BorderData>>> regionMap = children.collect(Collectors.groupingBy(DartBorderLayout::region));
        regionMap.getOrDefault(SWT.NONE, Collections.emptyList()).forEach(entry -> entry.getKey().setBounds(clientX, clientY, 0, 0));
        java.util.List<java.util.Map.Entry<Control, BorderData>> northList = regionMap.getOrDefault(TOP, Collections.emptyList());
        java.util.List<java.util.Map.Entry<Control, BorderData>> southList = regionMap.getOrDefault(BOTTOM, Collections.emptyList());
        java.util.List<java.util.Map.Entry<Control, BorderData>> westList = regionMap.getOrDefault(LEFT, Collections.emptyList());
        java.util.List<java.util.Map.Entry<Control, BorderData>> eastList = regionMap.getOrDefault(RIGHT, Collections.emptyList());
        java.util.List<java.util.Map.Entry<Control, BorderData>> centerList = regionMap.getOrDefault(CENTER, Collections.emptyList());
        int northControlCount = northList.size();
        int northPerControlWidth = northControlCount > 0 ? (clientWidth - (northControlCount - 1) * getApi().controlSpacing) / northControlCount : 0;
        int northControlHeight = getMax(HEIGHT, northList, northPerControlWidth, SWT.DEFAULT, flushCache);
        int southControlCount = southList.size();
        int southPerControlWidth = southControlCount > 0 ? (clientWidth - (southControlCount - 1) * getApi().controlSpacing) / southControlCount : 0;
        int southControlHeight = getMax(HEIGHT, southList, southPerControlWidth, SWT.DEFAULT, flushCache);
        if (northControlHeight + southControlHeight > clientHeight) {
            int distributionSize = (int) (clientHeight * getApi().heightDistributionFactor);
            if (northControlHeight > distributionSize) {
                northControlHeight = distributionSize;
            }
            southControlHeight = clientHeight - northControlHeight;
        }
        int centerControlHeight = clientHeight - northControlHeight - southControlHeight;
        int westControlCount = westList.size();
        int westControlWidth = getMax(WIDTH, westList, -1, -1, flushCache);
        int eastControlCount = eastList.size();
        int eastControlWidth = getMax(WIDTH, eastList, -1, -1, flushCache);
        if (westControlWidth + eastControlWidth > clientWidth) {
            int distributionSize = (int) (clientWidth * getApi().widthDistributionFactor);
            if (westControlWidth > distributionSize) {
                westControlWidth = distributionSize;
            }
            eastControlWidth = clientWidth - westControlWidth;
        }
        int centerControlWidth = clientWidth - westControlWidth - eastControlWidth;
        int centerControlCount = centerList.size();
        if (northControlCount > 0) {
            int x = clientX;
            int y = clientY;
            for (java.util.Map.Entry<Control, BorderData> entry : northList) {
                entry.getKey().setBounds(x, y, northPerControlWidth, northControlHeight);
                x += northPerControlWidth + getApi().controlSpacing;
            }
        }
        if (southControlCount > 0) {
            int x = clientX;
            int y = clientY + centerControlHeight + northControlHeight;
            for (java.util.Map.Entry<Control, BorderData> entry : southList) {
                entry.getKey().setBounds(x, y, southPerControlWidth, southControlHeight);
                x += southPerControlWidth + getApi().controlSpacing;
            }
        }
        if (westControlCount > 0) {
            int x = clientX;
            int y = clientY + northControlHeight;
            int h = clientHeight - northControlHeight - southControlHeight;
            if (northControlCount > 0) {
                y += getApi().spacing;
                h -= getApi().spacing;
            }
            if (southControlCount > 0) {
                h -= getApi().spacing;
            }
            int controlHeight = (h - (westControlCount - 1) * getApi().controlSpacing) / westControlCount;
            for (java.util.Map.Entry<Control, BorderData> entry : westList) {
                entry.getKey().setBounds(x, y, westControlWidth, controlHeight);
                y += controlHeight + getApi().controlSpacing;
            }
        }
        if (eastControlCount > 0) {
            int x = clientX + centerControlWidth + westControlWidth;
            int y = clientY + northControlHeight;
            int h = clientHeight - northControlHeight - southControlHeight;
            if (northControlCount > 0) {
                y += getApi().spacing;
                h -= getApi().spacing;
            }
            if (southControlCount > 0) {
                h -= getApi().spacing;
            }
            int controlHeight = (h - (eastControlCount - 1) * getApi().controlSpacing) / eastControlCount;
            for (java.util.Map.Entry<Control, BorderData> entry : eastList) {
                entry.getKey().setBounds(x, y, eastControlWidth, controlHeight);
                y += controlHeight + getApi().controlSpacing;
            }
        }
        if (centerControlCount > 0) {
            int x = clientX + westControlWidth;
            int y = clientY + northControlHeight;
            int h = centerControlHeight;
            int w = centerControlWidth;
            if (westControlCount > 0) {
                x += getApi().spacing;
                w -= getApi().spacing;
            }
            if (eastControlCount > 0) {
                w -= getApi().spacing;
            }
            if (northControlCount > 0) {
                y += getApi().spacing;
                h -= getApi().spacing;
            }
            if (southControlCount > 0) {
                h -= getApi().spacing;
            }
            int controlHeight;
            int controlWidth;
            if (getApi().type == SWT.HORIZONTAL) {
                controlHeight = h;
                controlWidth = (w - (centerControlCount - 1) * getApi().controlSpacing) / centerControlCount;
            } else {
                controlWidth = w;
                controlHeight = (h - (centerControlCount - 1) * getApi().controlSpacing) / centerControlCount;
            }
            for (java.util.Map.Entry<Control, BorderData> entry : centerList) {
                entry.getKey().setBounds(x, y, controlWidth, controlHeight);
                if (getApi().type == SWT.HORIZONTAL) {
                    x += controlWidth + getApi().controlSpacing;
                } else {
                    y += controlHeight + getApi().controlSpacing;
                }
            }
        }
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

    private <C extends org.eclipse.swt.widgets.Control> java.util.Map.Entry<C, org.eclipse.swt.layout.BorderData> borderDataControl(C control, boolean flushCache) {
        Object layoutData = control.getLayoutData();
        if (layoutData instanceof org.eclipse.swt.layout.BorderData borderData) {
            if (flushCache) {
                borderData.flushCache(control);
            }
            return new java.util.AbstractMap.SimpleEntry<>(control, borderData);
        } else {
            org.eclipse.swt.layout.BorderData borderData = flushCache ? null : (org.eclipse.swt.layout.BorderData) control.getData(LAYOUT_KEY);
            if (borderData == null) {
                control.setData(LAYOUT_KEY, borderData = new org.eclipse.swt.layout.BorderData());
            }
            return new java.util.AbstractMap.SimpleEntry<>(control, borderData);
        }
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
