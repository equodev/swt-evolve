/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2017 IBM Corporation and others.
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
package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import java.util.WeakHashMap;

/**
 * A control for showing progress feedback for a long running operation.
 *
 * @deprecated As of Eclipse 2.1, use ProgressBar with the style SWT.INDETERMINATE
 *
 * <dl>
 * <dt><b>Styles:</b><dd>VERTICAL, HORIZONTAL, BORDER
 * </dl>
 *
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
@Deprecated
public class AnimatedProgress extends Canvas {

    /**
     * Constructs a new instance of this class given its parent
     * and a style value describing its behavior and appearance.
     * <p>
     * The style value is either one of the style constants defined in
     * class <code>SWT</code> which is applicable to instances of this
     * class, or must be built by <em>bitwise OR</em>'ing together
     * (that is, using the <code>int</code> "|" operator) two or more
     * of those <code>SWT</code> style constants. The class description
     * lists the style constants that are applicable to the class.
     * Style bits are also inherited from superclasses.
     * </p>
     *
     * @param parent a widget which will be the parent of the new instance (cannot be null)
     * @param style the style of widget to construct
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     * </ul>
     *
     * @see SWT#VERTICAL
     * @see SWT#HORIZONTAL
     * @see SWT#BORDER
     * @see #getStyle()
     */
    public AnimatedProgress(Composite parent, int style) {
        this(new SWTAnimatedProgress((SWTComposite) parent.delegate, style));
    }

    /**
     * Stop the animation if it is not already stopped and
     * reset the presentation to a blank appearance.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public synchronized void clear() {
        ((IAnimatedProgress) this.delegate).clear();
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        return ((IAnimatedProgress) this.delegate).computeSize(wHint, hHint, changed);
    }

    /**
     * Start the animation.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public synchronized void start() {
        ((IAnimatedProgress) this.delegate).start();
    }

    /**
     * Stop the animation.   Freeze the presentation at its current appearance.
     */
    public synchronized void stop() {
        ((IAnimatedProgress) this.delegate).stop();
    }

    protected AnimatedProgress(IAnimatedProgress delegate) {
        super(delegate);
        this.delegate = delegate;
        INSTANCES.put(delegate, this);
    }

    public static AnimatedProgress getInstance(IAnimatedProgress delegate) {
        if (delegate == null) {
            return null;
        }
        AnimatedProgress ref = (AnimatedProgress) INSTANCES.get(delegate);
        if (ref == null) {
            ref = new AnimatedProgress(delegate);
        }
        return ref;
    }
}
