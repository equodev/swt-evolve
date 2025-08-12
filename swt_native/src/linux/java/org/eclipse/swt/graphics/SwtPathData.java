/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2008 IBM Corporation and others.
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

/**
 * Instances of this class describe device-independent paths.
 *
 * @see Path
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.1
 */
public final class SwtPathData implements IPathData {

    public PathData getApi() {
        if (api == null)
            api = PathData.createApi(this);
        return (PathData) api;
    }

    protected PathData api;

    public void setApi(PathData api) {
        this.api = api;
        if (api != null)
            api.impl = this;
    }

    public SwtPathData(PathData api) {
        setApi(api);
    }
}
