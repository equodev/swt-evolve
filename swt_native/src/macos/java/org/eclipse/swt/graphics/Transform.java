/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2011 IBM Corporation and others.
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

import org.eclipse.swt.*;
import org.eclipse.swt.internal.cocoa.*;

/**
 * Instances of this class represent transformation matrices for
 * points expressed as (x, y) pairs of floating point numbers.
 * <p>
 * Application code must explicitly invoke the <code>Transform.dispose()</code>
 * method to release the operating system resources managed by each instance
 * when those instances are no longer required.
 * </p>
 * <p>
 * This class requires the operating system's advanced graphics subsystem
 * which may not be available on some platforms.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: GraphicsExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.1
 */
public class Transform extends Resource {

    /**
     * the OS resource for the Transform
     * (Warning: This field is platform dependent)
     * <p>
     * <b>IMPORTANT:</b> This field is <em>not</em> part of the SWT
     * public API. It is marked public only so that it can be shared
     * within the packages provided by SWT. It is not available on all
     * platforms and should never be accessed from application code.
     * </p>
     *
     * @noreference This field is not intended to be referenced by clients.
     */
    public NSAffineTransform handle;

    /**
     * Constructs a new identity Transform.
     * <p>
     * This operation requires the operating system's advanced
     * graphics subsystem which may not be available on some
     * platforms.
     * </p>
     * <p>
     * You must dispose the transform when it is no longer required.
     * </p>
     *
     * @param device the device on which to allocate the Transform
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES if a handle for the Transform could not be obtained</li>
     * </ul>
     *
     * @see #dispose()
     */
    public Transform(Device device) {
        this(new nat.org.eclipse.swt.graphics.Transform((nat.org.eclipse.swt.graphics.Device) device.getDelegate()));
    }

    /**
     * Constructs a new Transform given an array of elements that represent the
     * matrix that describes the transformation.
     * <p>
     * This operation requires the operating system's advanced
     * graphics subsystem which may not be available on some
     * platforms.
     * </p>
     * <p>
     * You must dispose the transform when it is no longer required.
     * </p>
     *
     * @param device the device on which to allocate the Transform
     * @param elements an array of floats that describe the transformation matrix
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device, or the elements array is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the elements array is too small to hold the matrix values</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES if a handle for the Transform could not be obtained</li>
     * </ul>
     *
     * @see #dispose()
     */
    public Transform(Device device, float[] elements) {
        this(new nat.org.eclipse.swt.graphics.Transform((nat.org.eclipse.swt.graphics.Device) device.getDelegate(), elements));
    }

    /**
     * Constructs a new Transform given all of the elements that represent the
     * matrix that describes the transformation.
     * <p>
     * This operation requires the operating system's advanced
     * graphics subsystem which may not be available on some
     * platforms.
     * </p>
     * <p>
     * You must dispose the transform when it is no longer required.
     * </p>
     *
     * @param device the device on which to allocate the Transform
     * @param m11 the first element of the first row of the matrix
     * @param m12 the second element of the first row of the matrix
     * @param m21 the first element of the second row of the matrix
     * @param m22 the second element of the second row of the matrix
     * @param dx the third element of the first row of the matrix
     * @param dy the third element of the second row of the matrix
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES if a handle for the Transform could not be obtained</li>
     * </ul>
     *
     * @see #dispose()
     */
    public Transform(Device device, float m11, float m12, float m21, float m22, float dx, float dy) {
        this(new nat.org.eclipse.swt.graphics.Transform((nat.org.eclipse.swt.graphics.Device) device.getDelegate(), m11, m12, m21, m22, dx, dy));
    }

    /**
     * Fills the parameter with the values of the transformation matrix
     * that the receiver represents, in the order {m11, m12, m21, m22, dx, dy}.
     *
     * @param elements array to hold the matrix values
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parameter is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the parameter is too small to hold the matrix values</li>
     * </ul>
     */
    public void getElements(float[] elements) {
        getDelegate().getElements(elements);
    }

    /**
     * Modifies the receiver such that the matrix it represents becomes the
     * identity matrix.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.4
     */
    public void identity() {
        getDelegate().identity();
    }

    /**
     * Modifies the receiver such that the matrix it represents becomes
     * the mathematical inverse of the matrix it previously represented.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_CANNOT_INVERT_MATRIX - if the matrix is not invertible</li>
     * </ul>
     */
    public void invert() {
        getDelegate().invert();
    }

    /**
     * Returns <code>true</code> if the Transform has been disposed,
     * and <code>false</code> otherwise.
     * <p>
     * This method gets the dispose state for the Transform.
     * When a Transform has been disposed, it is an error to
     * invoke any other method (except {@link #dispose()}) using the Transform.
     *
     * @return <code>true</code> when the Transform is disposed, and <code>false</code> otherwise
     */
    public boolean isDisposed() {
        return getDelegate().isDisposed();
    }

    /**
     * Returns <code>true</code> if the Transform represents the identity matrix
     * and false otherwise.
     *
     * @return <code>true</code> if the receiver is an identity Transform, and <code>false</code> otherwise
     */
    public boolean isIdentity() {
        return getDelegate().isIdentity();
    }

    /**
     * Modifies the receiver such that the matrix it represents becomes the
     * the result of multiplying the matrix it previously represented by the
     * argument.
     *
     * @param matrix the matrix to multiply the receiver by
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parameter is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the parameter has been disposed</li>
     * </ul>
     */
    public void multiply(Transform matrix) {
        getDelegate().multiply(matrix.getDelegate());
    }

    /**
     * Modifies the receiver so that it represents a transformation that is
     * equivalent to its previous transformation rotated by the specified angle.
     * The angle is specified in degrees and for the identity transform 0 degrees
     * is at the 3 o'clock position. A positive value indicates a clockwise rotation
     * while a negative value indicates a counter-clockwise rotation.
     *
     * @param angle the angle to rotate the transformation by
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public void rotate(float angle) {
        getDelegate().rotate(angle);
    }

    /**
     * Modifies the receiver so that it represents a transformation that is
     * equivalent to its previous transformation scaled by (scaleX, scaleY).
     *
     * @param scaleX the amount to scale in the X direction
     * @param scaleY the amount to scale in the Y direction
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public void scale(float scaleX, float scaleY) {
        getDelegate().scale(scaleX, scaleY);
    }

    /**
     * Modifies the receiver to represent a new transformation given all of
     * the elements that represent the matrix that describes that transformation.
     *
     * @param m11 the first element of the first row of the matrix
     * @param m12 the second element of the first row of the matrix
     * @param m21 the first element of the second row of the matrix
     * @param m22 the second element of the second row of the matrix
     * @param dx the third element of the first row of the matrix
     * @param dy the third element of the second row of the matrix
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public void setElements(float m11, float m12, float m21, float m22, float dx, float dy) {
        getDelegate().setElements(m11, m12, m21, m22, dx, dy);
    }

    /**
     * Modifies the receiver so that it represents a transformation that is
     * equivalent to its previous transformation sheared by (shearX, shearY).
     *
     * @param shearX the shear factor in the X direction
     * @param shearY the shear factor in the Y direction
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.4
     */
    public void shear(float shearX, float shearY) {
        getDelegate().shear(shearX, shearY);
    }

    /**
     * Given an array containing points described by alternating x and y values,
     * modify that array such that each point has been replaced with the result of
     * applying the transformation represented by the receiver to that point.
     *
     * @param pointArray an array of alternating x and y values to be transformed
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the point array is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public void transform(float[] pointArray) {
        getDelegate().transform(pointArray);
    }

    /**
     * Modifies the receiver so that it represents a transformation that is
     * equivalent to its previous transformation translated by (offsetX, offsetY).
     *
     * @param offsetX the distance to translate in the X direction
     * @param offsetY the distance to translate in the Y direction
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public void translate(float offsetX, float offsetY) {
        getDelegate().translate(offsetX, offsetY);
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the receiver
     */
    public String toString() {
        return getDelegate().toString();
    }

    protected Transform(ITransform delegate) {
        super(delegate);
    }

    public static Transform createApi(ITransform delegate) {
        return new Transform(delegate);
    }

    public ITransform getDelegate() {
        return (ITransform) super.getDelegate();
    }
}
