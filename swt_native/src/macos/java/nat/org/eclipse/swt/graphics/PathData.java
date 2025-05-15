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
package nat.org.eclipse.swt.graphics;

import org.eclipse.swt.graphics.IPathData;

/**
 * Instances of this class describe device-independent paths.
 *
 * @see Path
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.1
 */
public final class PathData implements IPathData {

    /**
     * The type of each point.
     */
    public byte[] types;

    /**
     * The points of a path.
     */
    public float[] points;

    public org.eclipse.swt.graphics.PathData getApi() {
        if (api == null)
            api = org.eclipse.swt.graphics.PathData.createApi(this);
        return (org.eclipse.swt.graphics.PathData) api;
    }

    protected org.eclipse.swt.graphics.PathData api;

    public void setApi(org.eclipse.swt.graphics.PathData api) {
        this.api = api;
    }
}
