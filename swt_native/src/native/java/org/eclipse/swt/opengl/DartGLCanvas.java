/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2016 IBM Corporation and others.
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

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import dev.equo.swt.*;

/**
 * GLCanvas is a widget capable of displaying OpenGL content.
 *
 * @see GLData
 * @see <a href="https://eclipse.dev/eclipse/swt/snippets/#opengl">OpenGL snippets</a>
 * @see <a href="https://eclipse.dev/eclipse/swt/">Sample code and further information</a>
 *
 * @since 3.2
 */
public class DartGLCanvas extends DartCanvas implements IGLCanvas {

    static final int MAX_ATTRIBUTES = 32;

    //$NON-NLS-1$
    static final String GLCONTEXT_KEY = "org.eclipse.swt.internal.cocoa.glcontext";

    /**
     * Create a GLCanvas widget using the attributes described in the GLData
     * object provided.
     *
     * @param parent a composite widget
     * @param style the bitwise OR'ing of widget styles
     * @param data the requested attributes of the GLCanvas
     *
     * @exception IllegalArgumentException
     * <ul><li>ERROR_NULL_ARGUMENT when the data is null</li>
     *     <li>ERROR_UNSUPPORTED_DEPTH when the requested attributes cannot be provided</li>
     * </ul>
     */
    public DartGLCanvas(Composite parent, int style, GLData data, GLCanvas api) {
        super(parent, style, api);
        if (data == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        int[] attrib = new int[MAX_ATTRIBUTES];
        int pos = 0;
        /*
	 * Feature in Cocoa: NSOpenGL/CoreOpenGL only supports specifying the total number of bits
	 * in the size of the color component. If specified, the color size is the sum of the red, green
	 * and blue values in the GLData.
	 */
        if ((data.redSize + data.blueSize + data.greenSize) > 0) {
            attrib[pos++] = data.redSize + data.greenSize + data.blueSize;
        }
        if (data.alphaSize > 0) {
            attrib[pos++] = data.alphaSize;
        }
        if (data.depthSize > 0) {
            attrib[pos++] = data.depthSize;
        }
        if (data.stencilSize > 0) {
            attrib[pos++] = data.stencilSize;
        }
        /*
	 * Feature in Cocoa: NSOpenGL/CoreOpenGL only supports specifying the total number of bits
	 * in the size of the color accumulator component. If specified, the color size is the sum of the red, green,
	 * blue and alpha accum values in the GLData.
	 */
        if ((data.accumRedSize + data.accumBlueSize + data.accumGreenSize) > 0) {
            attrib[pos++] = data.accumRedSize + data.accumGreenSize + data.accumBlueSize + data.accumAlphaSize;
        }
        if (data.sampleBuffers > 0) {
            attrib[pos++] = data.sampleBuffers;
        }
        if (data.samples > 0) {
            attrib[pos++] = data.samples;
        }
        attrib[pos++] = 0;
    }

    /**
     * Returns a GLData object describing the created context.
     *
     * @return GLData description of the OpenGL context attributes
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public GLData getGLData() {
        checkWidget();
        GLData data = new GLData();
        long[] value = new long[1];
        data.doubleBuffer = value[0] != 0;
        data.stereo = value[0] != 0;
        data.alphaSize = (int) value[0];
        int colorSize = ((int) (value[0] - data.alphaSize)) / 3;
        data.redSize = colorSize;
        data.greenSize = colorSize;
        data.blueSize = colorSize;
        data.depthSize = (int) value[0];
        data.stencilSize = (int) value[0];
        int accumColorSize = (int) (value[0]) / 4;
        data.accumRedSize = accumColorSize;
        data.accumGreenSize = accumColorSize;
        data.accumBlueSize = accumColorSize;
        data.accumAlphaSize = accumColorSize;
        data.sampleBuffers = (int) value[0];
        data.samples = (int) value[0];
        return data;
    }

    /**
     * Returns a boolean indicating whether the receiver's OpenGL context
     * is the current context.
     *
     * @return true if the receiver holds the current OpenGL context,
     * false otherwise
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean isCurrent() {
        checkWidget();
        return false;
    }

    /**
     * Sets the OpenGL context associated with this GLCanvas to be the
     * current GL context.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setCurrent() {
        checkWidget();
    }

    /**
     * Swaps the front and back color buffers.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void swapBuffers() {
        checkWidget();
    }

    GLData GLData;

    public GLData _GLData() {
        return GLData;
    }

    protected void _hookEvents() {
        super._hookEvents();
    }

    public GLCanvas getApi() {
        if (api == null)
            api = GLCanvas.createApi(this);
        return (GLCanvas) api;
    }

    public VGLCanvas getValue() {
        if (value == null)
            value = new VGLCanvas(this);
        return (VGLCanvas) value;
    }
}
