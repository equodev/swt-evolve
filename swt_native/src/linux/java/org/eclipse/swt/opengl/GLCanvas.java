/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2018 IBM Corporation and others.
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
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.gtk.*;
import org.eclipse.swt.internal.gtk3.*;
import org.eclipse.swt.internal.opengl.glx.*;
import org.eclipse.swt.widgets.*;
import java.util.WeakHashMap;

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
        this(new SWTGLCanvas((SWTComposite) parent.delegate, style, data));
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
        return ((IGLCanvas) this.delegate).getGLData();
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
        return ((IGLCanvas) this.delegate).isCurrent();
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
        ((IGLCanvas) this.delegate).setCurrent();
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
        ((IGLCanvas) this.delegate).swapBuffers();
    }

    public long getContext() {
        return ((IGLCanvas) this.delegate).getContext();
    }

    protected GLCanvas(IGLCanvas delegate) {
        super(delegate);
        this.delegate = delegate;
        INSTANCES.put(delegate, this);
    }

    public static GLCanvas getInstance(IGLCanvas delegate) {
        if (delegate == null) {
            return null;
        }
        GLCanvas ref = (GLCanvas) INSTANCES.get(delegate);
        if (ref == null) {
            ref = new GLCanvas(delegate);
        }
        return ref;
    }
}
