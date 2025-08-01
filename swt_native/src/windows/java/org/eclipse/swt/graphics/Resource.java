/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2010 IBM Corporation and others.
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

import java.util.*;
import java.util.function.*;
import org.eclipse.swt.*;

/**
 * This class is the abstract superclass of all graphics resource objects.
 * Resources created by the application must be disposed.
 * <p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em>
 * within the SWT implementation. However, it has not been marked
 * final to allow those outside of the SWT development team to implement
 * patched versions of the class in order to get around specific
 * limitations in advance of when those limitations can be addressed
 * by the team.  Any class built using subclassing to access the internals
 * of this class will likely fail to compile or run between releases and
 * may be strongly platform specific. Subclassing should not be attempted
 * without an intimate and detailed understanding of the workings of the
 * hierarchy. No support is provided for user-written classes which are
 * implemented as subclasses of this class.
 * </p>
 *
 * @see #dispose
 * @see #isDisposed
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.1
 */
public abstract class Resource {

    public Resource() {
    }

    Resource(Device device) {
    }

    /**
     * Disposes of the operating system resources associated with
     * this resource. Applications must dispose of all resources
     * which they allocate.
     * This method does nothing if the resource is already disposed.
     */
    public void dispose() {
        getImpl().dispose();
    }

    /**
     * Returns the <code>Device</code> where this resource was
     * created.
     *
     * @return <code>Device</code> the device of the receiver
     *
     * @since 3.2
     */
    public Device getDevice() {
        return getImpl().getDevice();
    }

    /**
     * Returns <code>true</code> if the resource has been disposed,
     * and <code>false</code> otherwise.
     * <p>
     * This method gets the dispose state for the resource.
     * When a resource has been disposed, it is an error to
     * invoke any other method (except {@link #dispose()}) using the resource.
     *
     * @return <code>true</code> when the resource is disposed and <code>false</code> otherwise
     */
    public abstract boolean isDisposed();

    /**
     * Enables detection of Resource objects for which {@link #dispose()} wasn't
     * called, which means a leak of native memory and/or OS resources.
     *
     * WARNING: the reporter will be called from a different thread. Do not block
     * it and do not throw any exceptions. It's best to queue the errors for some
     * other worker to process.
     *
     * @param reporter                object used to report detected errors. Use
     *                                null to disable tracking. Setting a new
     *                                reporter has an immediate effect.
     *
     * @since 3.116
     */
    public static void setNonDisposeHandler(Consumer<Error> reporter) {
        SwtResource.setNonDisposeHandler(reporter);
    }

    protected IResource impl;

    protected Resource(IResource impl) {
        if (impl != null)
            impl.setApi(this);
    }

    public IResource getImpl() {
        return impl;
    }

    protected Resource setImpl(IResource impl) {
        this.impl = impl;
        return this;
    }
}
