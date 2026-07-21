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
import dev.equo.swt.*;

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
 * @see <a href="https://eclipse.dev/eclipse/swt/examples.html">SWT Example: GraphicsExample</a>
 * @see <a href="https://eclipse.dev/eclipse/swt/">Sample code and further information</a>
 *
 * @since 3.1
 */
public class DartTransform extends DartResource implements ITransform {

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
    public DartTransform(Device device, Transform api) {
        this(device, 1, 0, 0, 1, 0, 0, api);
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
    public DartTransform(Device device, float[] elements, Transform api) {
        this(device, checkTransform(elements)[0], elements[1], elements[2], elements[3], elements[4], elements[5], api);
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
    public DartTransform(Device device, float m11, float m12, float m21, float m22, float dx, float dy, Transform api) {
        super(device, api);
        try {
            setElements(m11, m12, m21, m22, dx, dy);
            init();
        } finally {
        }
    }

    static float[] checkTransform(float[] elements) {
        if (elements == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (elements.length < 6)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        return elements;
    }

    @Override
    void destroy() {
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (elements == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (elements.length < 6)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        elements[0] = m11;
        elements[1] = m12;
        elements[2] = m21;
        elements[3] = m22;
        elements[4] = dx;
        elements[5] = dy;
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        m11 = 1;
        m12 = 0;
        m21 = 0;
        m22 = 1;
        dx = 0;
        dy = 0;
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        float det = m11 * m22 - m21 * m12;
        if (det == 0)
            SWT.error(SWT.ERROR_CANNOT_INVERT_MATRIX);
        float nm11 = m22 / det, nm12 = -m12 / det, nm21 = -m21 / det, nm22 = m11 / det;
        float ndx = (-m22 * dx + m21 * dy) / det, ndy = (m12 * dx - m11 * dy) / det;
        m11 = nm11;
        m12 = nm12;
        m21 = nm21;
        m22 = nm22;
        dx = ndx;
        dy = ndy;
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
    @Override
    public boolean isDisposed() {
        return device == null;
    }

    /**
     * Returns <code>true</code> if the Transform represents the identity matrix
     * and false otherwise.
     *
     * @return <code>true</code> if the receiver is an identity Transform, and <code>false</code> otherwise
     */
    public boolean isIdentity() {
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        return m11 == 1 && m12 == 0 && m21 == 0 && m22 == 1 && dx == 0 && dy == 0;
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (matrix == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (matrix.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        float[] e = new float[6];
        matrix.getElements(e);
        multiplyBy(e[0], e[1], e[2], e[3], e[4], e[5]);
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        float rad = (float) (angle * Math.PI / 180.0);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);
        multiplyBy(cos, sin, -sin, cos, 0, 0);
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        multiplyBy(scaleX, 0, 0, scaleY, 0, 0);
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        this.m11 = m11;
        this.m12 = m12;
        this.m21 = m21;
        this.m22 = m22;
        this.dx = dx;
        this.dy = dy;
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        multiplyBy(1, shearY, shearX, 1, 0, 0);
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (pointArray == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        int length = pointArray.length / 2;
        for (int i = 0, j = 0; i < length; i++, j += 2) {
            float x = pointArray[j], y = pointArray[j + 1];
            pointArray[j] = m11 * x + m21 * y + dx;
            pointArray[j + 1] = m12 * x + m22 * y + dy;
        }
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        multiplyBy(1, 0, 0, 1, offsetX, offsetY);
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the receiver
     */
    @Override
    public String toString() {
        if (isDisposed())
            return "Transform {*DISPOSED*}";
        float[] elements = new float[6];
        getElements(elements);
        return "Transform {" + elements[0] + ", " + elements[1] + ", " + elements[2] + ", " + elements[3] + ", " + elements[4] + ", " + elements[5] + "}";
    }

    float m11, m12, m21, m22, dx, dy;

    private void multiplyBy(float bm11, float bm12, float bm21, float bm22, float bdx, float bdy) {
        float am11 = this.m11, am12 = this.m12, am21 = this.m21;
        float am22 = this.m22, adx = this.dx, ady = this.dy;
        this.m11 = am11 * bm11 + am12 * bm21;
        this.m12 = am11 * bm12 + am12 * bm22;
        this.m21 = am21 * bm11 + am22 * bm21;
        this.m22 = am21 * bm12 + am22 * bm22;
        this.dx = am11 * bdx + am21 * bdy + adx;
        this.dy = am12 * bdx + am22 * bdy + ady;
    }

    public Transform getApi() {
        if (api == null)
            api = Transform.createApi(this);
        return (Transform) api;
    }

    public VTransform getValue() {
        if (value == null)
            value = new VTransform(this);
        return (VTransform) value;
    }
}
