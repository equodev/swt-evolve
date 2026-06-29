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
import org.eclipse.swt.internal.cocoa.*;

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
