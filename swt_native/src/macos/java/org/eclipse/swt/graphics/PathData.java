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
public final class PathData {

    /**
     * The type of each point.
     */
    public byte[] types;

    /**
     * The points of a path.
     */
    public float[] points;

    public PathData() {
        this((IPathData) null);
        setImpl(new SwtPathData(this));
    }

    protected IPathData impl;

    protected PathData(IPathData impl) {
        if (impl != null)
            impl.setApi(this);
    }

    static PathData createApi(IPathData impl) {
        return new PathData(impl);
    }

    public IPathData getImpl() {
        return impl;
    }

    protected PathData setImpl(IPathData impl) {
        this.impl = impl;
        return this;
    }
}
