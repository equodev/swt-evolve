package org.eclipse.swt.graphics;

import java.lang.ref.*;
import java.lang.ref.Cleaner.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import org.eclipse.swt.*;

public interface IResource extends ImplResource {

    /**
     * Disposes of the operating system resources associated with
     * this resource. Applications must dispose of all resources
     * which they allocate.
     * This method does nothing if the resource is already disposed.
     */
    void dispose();

    /**
     * Returns the <code>Device</code> where this resource was
     * created.
     *
     * @return <code>Device</code> the device of the receiver
     *
     * @since 3.2
     */
    Device getDevice();

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
    abstract boolean isDisposed();

    Resource getApi();

    void setApi(Resource api);
}
