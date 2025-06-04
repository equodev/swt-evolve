package org.eclipse.swt.program;

import java.io.*;
import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface IProgram {

    /**
     * Executes the program with the file as the single argument
     * in the operating system.  It is the responsibility of the
     * programmer to ensure that the file contains valid data for
     * this program.
     *
     * @param fileName the file or program name
     * @return <code>true</code> if the file is launched, otherwise <code>false</code>
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when fileName is null</li>
     * </ul>
     */
    boolean execute(String fileName);

    /**
     * Returns the receiver's image data at 100% zoom level.
     * This is the icon that is associated with the receiver
     * in the operating system.
     *
     * @return the image data for the program, may be null
     */
    ImageData getImageData();

    /**
     * Returns the receiver's image data based on the given zoom level.
     * This is the icon that is associated with the receiver in the
     * operating system.
     *
     * @param zoom
     *            The zoom level in % of the standard resolution
     *
     * @return the image data for the program, may be null
     * @since 3.125
     */
    ImageData getImageData(int zoom);

    /**
     * Returns the receiver's name.  This is as short and
     * descriptive a name as possible for the program.  If
     * the program has no descriptive name, this string may
     * be the executable name, path or empty.
     *
     * @return the name of the program
     */
    String getName();

    /**
     * Compares the argument to the receiver, and returns true
     * if they represent the <em>same</em> object using a class
     * specific comparison.
     *
     * @param other the object to compare with this object
     * @return <code>true</code> if the object is the same as this object and <code>false</code> otherwise
     *
     * @see #hashCode()
     */
    boolean equals(Object other);

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
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the program
     */
    String toString();

    Program getApi();

    void setApi(Program api);
}
