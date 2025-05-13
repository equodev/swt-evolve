package org.eclipse.swt.opengl;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

public interface IGLCanvas extends ICanvas {

    /**
     * Returns a GLData object describing the created context.
     *
     * @return GLData description of the OpenGL context attributes
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    IGLData getGLData();

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
    boolean isCurrent();

    /**
     * Sets the OpenGL context associated with this GLCanvas to be the
     * current GL context.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setCurrent();

    /**
     * Swaps the front and back color buffers.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void swapBuffers();

    GLCanvas getApi();
}
