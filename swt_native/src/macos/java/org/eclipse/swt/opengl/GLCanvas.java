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
import org.eclipse.swt.internal.cocoa.*;
import org.eclipse.swt.widgets.*;

/**
 * GLCanvas is a widget capable of displaying OpenGL content.
 *
 * @see GLData
 * @see <a href="http://www.eclipse.org/swt/snippets/#opengl">OpenGL snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.2
 */
public class GLCanvas extends Canvas {

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
    public GLCanvas(Composite parent, int style, GLData data) {
        this(new nat.org.eclipse.swt.opengl.GLCanvas((nat.org.eclipse.swt.widgets.Composite) (parent != null ? parent.getDelegate() : null), style, (nat.org.eclipse.swt.opengl.GLData) (data != null ? data.getDelegate() : null)));
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
        return getDelegate().getGLData().getApi();
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
        return getDelegate().isCurrent();
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
        getDelegate().setCurrent();
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
        getDelegate().swapBuffers();
    }

    protected GLCanvas(IGLCanvas delegate) {
        super(delegate);
    }

    public static GLCanvas createApi(IGLCanvas delegate) {
        return new GLCanvas(delegate);
    }

    public IGLCanvas getDelegate() {
        return (IGLCanvas) super.getDelegate();
    }
}
