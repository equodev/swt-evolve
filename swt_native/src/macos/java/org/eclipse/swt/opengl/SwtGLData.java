/**
 * ****************************************************************************
 *  Copyright (c) 2005, 2009 IBM Corporation and others.
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
package org.eclipse.swt.opengl;

/**
 * The GLData class is a device-independent description
 * of the pixel format attributes of a GL drawable.
 *
 * @see GLCanvas
 * @see <a href="http://www.eclipse.org/swt/snippets/#opengl">OpenGL snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.2
 */
public class SwtGLData implements IGLData {

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the data
     */
    @Override
    public String toString() {
        return (getApi().doubleBuffer ? "doubleBuffer," : "") + (getApi().stereo ? "stereo," : "") + "r:" + getApi().redSize + " g:" + getApi().greenSize + " b:" + getApi().blueSize + " a:" + getApi().alphaSize + "," + "depth:" + getApi().depthSize + ",stencil:" + getApi().stencilSize + ",accum r:" + getApi().accumRedSize + "g:" + getApi().accumGreenSize + "b:" + getApi().accumBlueSize + "a:" + getApi().accumAlphaSize + ",sampleBuffers:" + getApi().sampleBuffers + ",samples:" + getApi().samples;
    }

    public GLData getApi() {
        if (api == null)
            api = GLData.createApi(this);
        return (GLData) api;
    }

    protected GLData api;

    public void setApi(GLData api) {
        this.api = api;
    }
}
