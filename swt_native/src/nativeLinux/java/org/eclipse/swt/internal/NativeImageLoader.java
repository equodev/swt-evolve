/**
 * ****************************************************************************
 *  Copyright (c) 2019, 2025 Red Hat and others. All rights reserved.
 *  The contents of this file are made available under the terms
 *  of the GNU Lesser General Public License (LGPL) Version 2.1 that
 *  accompanies this distribution (lgpl-v21.txt).  The LGPL is also
 *  available at http://www.gnu.org/licenses/lgpl.html.  If the version
 *  of the LGPL at http://www.gnu.org is different to the version of
 *  the LGPL accompanying this distribution and there is any conflict
 *  between the two license versions, the terms of the LGPL accompanying
 *  this distribution shall govern.
 *
 *  Contributors:
 *      Red Hat - initial implementation
 *      Hannes Wellmann - Unify ImageLoader implementations and extract differences into InternalImageLoader
 * *****************************************************************************
 */
package org.eclipse.swt.internal;

import java.io.*;
import java.util.*;
import java.util.List;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.DPIUtil.*;
import org.eclipse.swt.internal.gtk.*;
import org.eclipse.swt.internal.image.*;
import org.eclipse.swt.widgets.*;

public class NativeImageLoader {

    /**
     * If the 29th byte of the PNG file is not zero, then it is interlaced.
     */
    private static final int PNG_INTERLACE_METHOD_OFFSET = 28;

    // --- loading ---
    public static List<ElementAtZoom<ImageData>> load(ElementAtZoom<InputStream> streamAtZoom, ImageLoader imageLoader, int targetZoom) {
        return FileFormat.load(streamAtZoom, imageLoader, targetZoom);
    }

    public static ImageData load(InputStream streamAtZoom, ImageLoader imageLoader, int width, int height) {
        return FileFormat.load(streamAtZoom, imageLoader, width, height);
    }

    // --- saving ---
    public static void save(OutputStream stream, int format, ImageLoader imageLoader) {
        FileFormat.save(stream, format, imageLoader);
    }
}
