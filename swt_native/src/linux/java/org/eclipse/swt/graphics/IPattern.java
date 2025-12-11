package org.eclipse.swt.graphics;

import org.eclipse.swt.*;
import org.eclipse.swt.internal.cairo.*;

public interface IPattern extends IResource {

    /**
     * Returns <code>true</code> if the Pattern has been disposed,
     * and <code>false</code> otherwise.
     * <p>
     * This method gets the dispose state for the Pattern.
     * When a Pattern has been disposed, it is an error to
     * invoke any other method (except {@link #dispose()}) using the Pattern.
     *
     * @return <code>true</code> when the Pattern is disposed, and <code>false</code> otherwise
     */
    boolean isDisposed();

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the receiver
     */
    String toString();

    Pattern getApi();
}
