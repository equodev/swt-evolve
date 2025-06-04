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
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * Controls the several aspects of a {@link BorderLayout}.
 *
 * @since 3.119
 */
public final class SwtBorderData implements IBorderData {

    private final Map<Control, Point> cachedSize = new IdentityHashMap<>(1);

    /**
     * creates a {@link BorderData} with default options
     */
    public SwtBorderData(BorderData api) {
        setApi(api);
    }

    /**
     * creates a {@link BorderData} initialized with the given region, valid values
     * are {@link SWT#TOP}, {@link SWT#CENTER}, {@link SWT#LEFT}, {@link SWT#RIGHT},
     * {@link SWT#BOTTOM}
     *
     * @param region the region valid values are {@link SWT#TOP},
     *               {@link SWT#CENTER}, {@link SWT#LEFT}, {@link SWT#RIGHT},
     *               {@link SWT#BOTTOM}
     */
    public SwtBorderData(int region, BorderData api) {
        setApi(api);
        this.getApi().region = region;
    }

    /**
     * creates a {@link BorderData} initialized with the given region and width and
     * height hints
     *
     * @param region     the region valid values are {@link SWT#TOP},
     *                   {@link SWT#CENTER}, {@link SWT#LEFT}, {@link SWT#RIGHT},
     *                   {@link SWT#BOTTOM}
     * @param widthHint  the default hint for the width
     * @param heightHint he default hint for the height
     */
    public SwtBorderData(int region, int widthHint, int heightHint, BorderData api) {
        setApi(api);
        this.getApi().region = region;
        this.getApi().wHint = widthHint;
        this.getApi().hHint = heightHint;
    }

    Point getSize(Control control) {
        return cachedSize.computeIfAbsent(control, c -> c.computeSize(getApi().wHint, getApi().hHint, true));
    }

    Point computeSize(Control control, int wHint, int hHint, boolean changed) {
        if (wHint == SWT.DEFAULT) {
            wHint = this.getApi().wHint;
        }
        if (hHint == SWT.DEFAULT) {
            hHint = this.getApi().hHint;
        }
        return control.computeSize(wHint, hHint, changed);
    }

    void flushCache(Control control) {
        cachedSize.remove(control);
    }

    @Override
    public String toString() {
        return "BorderData [region=" + getRegionString(getApi().region) + ", hHint=" + getApi().hHint + ", wHint=" + getApi().wHint + "]";
    }

    static String getRegionString(int region) {
        return switch(region) {
            case SWT.TOP ->
                "SWT.TOP";
            case SWT.RIGHT ->
                "SWT.RIGHT";
            case SWT.BOTTOM ->
                "SWT.BOTTOM";
            case SWT.LEFT ->
                "SWT.LEFT";
            case SWT.CENTER ->
                "SWT.CENTER";
            default ->
                "SWT.NONE";
        };
    }

    /**
     * @return the region of this BorderData or {@link SWT#NONE} if it is out of
     *         range
     */
    int getRegion() {
        return switch(getApi().region) {
            case TOP, BOTTOM, CENTER, RIGHT, LEFT ->
                getApi().region;
            case SWT.NONE ->
                SWT.NONE;
            default ->
                SWT.NONE;
        };
    }

    public BorderData getApi() {
        if (api == null)
            api = BorderData.createApi(this);
        return (BorderData) api;
    }

    protected BorderData api;

    public void setApi(BorderData api) {
        this.api = api;
        if (api != null)
            api.impl = this;
    }
}
