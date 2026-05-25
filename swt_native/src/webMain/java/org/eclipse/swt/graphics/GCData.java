/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2010 IBM Corporation and others.
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
 * Instances of this class are descriptions of GCs in terms
 * of unallocated platform-specific data fields.
 * <p>
 * <b>IMPORTANT:</b> This class is <em>not</em> part of the public
 * API for SWT. It is marked public only so that it can be shared
 * within the packages provided by SWT. It is not available on all
 * platforms, and should never be called from application code.
 * </p>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noreference This class is not intended to be referenced by clients
 */
public final class GCData {

    public Device device;

    public int style, state = -1;

    public double[] foreground, background;

    public Pattern foregroundPattern;

    public Pattern backgroundPattern;

    public Font font;

    public int alpha = 0xFF;

    public float lineWidth;

    public int lineStyle = SWT.LINE_SOLID;

    public int lineCap = SWT.CAP_FLAT;

    public int lineJoin = SWT.JOIN_MITER;

    public float lineDashesOffset;

    public float[] lineDashes;

    public float lineMiterLimit = 10;

    public boolean xorMode;

    public int antialias = SWT.DEFAULT;

    public int textAntialias = SWT.DEFAULT;

    public int fillRule = SWT.FILL_EVEN_ODD;

    public Image image;

    public double drawXOffset, drawYOffset;

    public long visibleRgn;

    public Thread thread;

    public boolean restoreContext;

    public GCData() {
        this((IGCData) null);
        setImpl(new DartGCData(this));
    }

    protected IGCData impl;

    protected GCData(IGCData impl) {
        if (impl != null)
            impl.setApi(this);
    }

    static GCData createApi(IGCData impl) {
        return new GCData(impl);
    }

    public IGCData getImpl() {
        return impl;
    }

    protected GCData setImpl(IGCData impl) {
        this.impl = impl;
        return this;
    }
}
