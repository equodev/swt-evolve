package org.eclipse.swt.graphics;

import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;

public interface ICursor extends IResource {

    /**
     * Compares the argument to the receiver, and returns true
     * if they represent the <em>same</em> object using a class
     * specific comparison.
     *
     * @param object the object to compare with this object
     * @return <code>true</code> if the object is the same as this object and <code>false</code> otherwise
     *
     * @see #hashCode
     */
    boolean equals(Object object);

    /**
     * Returns an integer hash code for the receiver. Any two
     * objects that return <code>true</code> when passed to
     * <code>equals</code> must return the same value for this
     * method.
     *
     * @return the receiver's hash
     *
     * @see #equals
     */
    int hashCode();

    /**
     * Returns <code>true</code> if the cursor has been disposed,
     * and <code>false</code> otherwise.
     * <p>
     * This method gets the dispose state for the cursor.
     * When a cursor has been disposed, it is an error to
     * invoke any other method (except {@link #dispose()}) using the cursor.
     *
     * @return <code>true</code> when the cursor is disposed and <code>false</code> otherwise
     */
    boolean isDisposed();

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the receiver
     */
    String toString();

    Cursor getApi();
}
