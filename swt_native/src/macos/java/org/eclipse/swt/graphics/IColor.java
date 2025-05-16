package org.eclipse.swt.graphics;

import org.eclipse.swt.*;

public interface IColor extends IResource {

    /**
     * Colors do not need to be disposed, however to maintain compatibility
     * with older code, disposing a Color is not an error.
     */
    void dispose();

    /**
     * Returns the <code>Device</code> where this resource was
     * created. In cases where no <code>Device</code> was used
     * at creation, returns the current or default Device.
     *
     * <p>
     * As Color does not require a Device it is recommended to not
     * use {@link Color#getDevice()}.
     * </p>
     *
     * @return <code>Device</code> the device of the receiver
     * @since 3.2
     */
    Device getDevice();

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
     * Returns the amount of alpha in the color, from 0 (transparent) to 255 (opaque).
     *
     * @return the alpha component of the color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     * @since 3.104
     */
    int getAlpha();

    /**
     * Returns the amount of blue in the color, from 0 to 255.
     *
     * @return the blue component of the color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    int getBlue();

    /**
     * Returns the amount of green in the color, from 0 to 255.
     *
     * @return the green component of the color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    int getGreen();

    /**
     * Returns the amount of red in the color, from 0 to 255.
     *
     * @return the red component of the color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    int getRed();

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
     * Returns an <code>RGB</code> representing the receiver.
     *
     * @return the RGB for the color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    RGB getRGB();

    /**
     * Returns an <code>RGBA</code> representing the receiver.
     *
     * @return the RGBA for the color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     * @since 3.104
     */
    RGBA getRGBA();

    /**
     * Returns <code>true</code> if the color has been disposed,
     * and <code>false</code> otherwise.
     * <p>
     * This method gets the dispose state for the color.
     * When a color has been disposed, it is an error to
     * invoke any other method (except {@link #dispose()}) using the color.
     *
     * @return <code>true</code> when the color is disposed and <code>false</code> otherwise
     */
    boolean isDisposed();

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the receiver
     */
    String toString();

    Color getApi();
}
