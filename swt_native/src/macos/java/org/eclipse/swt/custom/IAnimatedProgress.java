package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface IAnimatedProgress extends ICanvas {

    /**
     * Stop the animation if it is not already stopped and
     * reset the presentation to a blank appearance.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void clear();

    IPoint computeSize(int wHint, int hHint, boolean changed);

    /**
     * Start the animation.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void start();

    /**
     * Stop the animation.   Freeze the presentation at its current appearance.
     */
    void stop();

    AnimatedProgress getApi();
}
