package org.eclipse.swt.graphics;

import java.util.*;
import java.util.function.*;
import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.gdip.*;

public interface ITransform extends IResource {

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
    void getElements(float[] elements);

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
    void identity();

    /**
     * Modifies the receiver such that the matrix it represents becomes
     * the mathematical inverse of the matrix it previously represented.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_CANNOT_INVERT_MATRIX - if the matrix is not invertible</li>
     * </ul>
     */
    void invert();

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
    boolean isDisposed();

    /**
     * Returns <code>true</code> if the Transform represents the identity matrix
     * and false otherwise.
     *
     * @return <code>true</code> if the receiver is an identity Transform, and <code>false</code> otherwise
     */
    boolean isIdentity();

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
    void multiply(Transform matrix);

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
    void rotate(float angle);

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
    void scale(float scaleX, float scaleY);

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
    void setElements(float m11, float m12, float m21, float m22, float dx, float dy);

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
    void shear(float shearX, float shearY);

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
    void transform(float[] pointArray);

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
    void translate(float offsetX, float offsetY);

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the receiver
     */
    String toString();

    Transform getApi();
}
