package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface IStyleRange extends ITextStyle {

    /**
     * Compares the argument to the receiver, and returns true
     * if they represent the <em>same</em> object using a class
     * specific comparison.
     *
     * @param object the object to compare with this object
     * @return <code>true</code> if the object is the same as this object and <code>false</code> otherwise
     *
     * @see #hashCode()
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
     * @see #equals(Object)
     */
    int hashCode();

    /**
     * Returns whether or not the receiver is unstyled (i.e., does not have any
     * style attributes specified).
     *
     * @return true if the receiver is unstyled, false otherwise.
     */
    boolean isUnstyled();

    /**
     * Compares the specified object to this StyleRange and answer if the two
     * are similar. The object must be an instance of StyleRange and have the
     * same field values for except for start and length.
     *
     * @param style the object to compare with this object
     * @return true if the objects are similar, false otherwise
     */
    boolean similarTo(IStyleRange style);

    /**
     * Returns a new StyleRange with the same values as this StyleRange.
     *
     * @return a shallow copy of this StyleRange
     */
    Object clone();

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the StyleRange
     */
    String toString();

    StyleRange getApi();
}
