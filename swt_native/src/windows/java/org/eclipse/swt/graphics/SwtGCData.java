/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
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
import org.eclipse.swt.internal.win32.*;

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
public final class SwtGCData implements IGCData {

    int imageZoom;

    void copyTo(GCData originalData) {
        originalData.device = getApi().device;
        originalData.style = getApi().style;
        originalData.foreground = getApi().foreground;
        originalData.background = getApi().background;
        originalData.font = getApi().font;
        originalData.nativeZoom = getApi().nativeZoom;
        originalData.image = getApi().image;
        ((SwtGCData) originalData.getImpl()).imageZoom = imageZoom;
        originalData.ps = getApi().ps;
        originalData.layout = getApi().layout;
        originalData.hwnd = getApi().hwnd;
        originalData.uiState = getApi().uiState;
        originalData.focusDrawn = getApi().focusDrawn;
    }

    public GCData getApi() {
        if (api == null)
            api = GCData.createApi(this);
        return (GCData) api;
    }

    protected GCData api;

    public void setApi(GCData api) {
        this.api = api;
        if (api != null)
            api.impl = this;
    }

    public SwtGCData(GCData api) {
        setApi(api);
    }
}
