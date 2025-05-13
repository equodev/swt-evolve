package org.eclipse.swt.graphics;

import java.io.*;
import org.eclipse.swt.*;

public interface IRectangle {

    /**
     * Destructively replaces the x, y, width and height values
     * in the receiver with ones which represent the union of the
     * rectangles specified by the receiver and the given rectangle.
     * <p>
     * The union of two rectangles is the smallest single rectangle
     * that completely covers both of the areas covered by the two
     * given rectangles.
     * </p>
     *
     * @param rect the rectangle to merge with the receiver
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     * </ul>
     */
    void add(IRectangle rect);

    /**
     * Returns <code>true</code> if the point specified by the
     * arguments is inside the area specified by the receiver,
     * and <code>false</code> otherwise.
     *
     * @param x the x coordinate of the point to test for containment
     * @param y the y coordinate of the point to test for containment
     * @return <code>true</code> if the rectangle contains the point and <code>false</code> otherwise
     */
    boolean contains(int x, int y);

    /**
     * Returns <code>true</code> if the given point is inside the
     * area specified by the receiver, and <code>false</code>
     * otherwise.
     *
     * @param pt the point to test for containment
     * @return <code>true</code> if the rectangle contains the point and <code>false</code> otherwise
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     * </ul>
     */
    boolean contains(IPoint pt);

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
     * Destructively replaces the x, y, width and height values
     * in the receiver with ones which represent the intersection of the
     * rectangles specified by the receiver and the given rectangle.
     *
     * @param rect the rectangle to intersect with the receiver
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     * </ul>
     *
     * since 3.0
     */
    void intersect(IRectangle rect);

    /**
     * Returns a new rectangle which represents the intersection
     * of the receiver and the given rectangle.
     * <p>
     * The intersection of two rectangles is the rectangle that
     * covers the area which is contained within both rectangles.
     * </p>
     *
     * @param rect the rectangle to intersect with the receiver
     * @return the intersection of the receiver and the argument
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     * </ul>
     */
    IRectangle intersection(IRectangle rect);

    /**
     * Returns <code>true</code> if the rectangle described by the
     * arguments intersects with the receiver and <code>false</code>
     * otherwise.
     * <p>
     * Two rectangles intersect if the area of the rectangle
     * representing their intersection is not empty.
     * </p>
     *
     * @param x the x coordinate of the origin of the rectangle
     * @param y the y coordinate of the origin of the rectangle
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     * @return <code>true</code> if the rectangle intersects with the receiver, and <code>false</code> otherwise
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     * </ul>
     *
     * @see #intersection(Rectangle)
     * @see #isEmpty()
     *
     * @since 3.0
     */
    boolean intersects(int x, int y, int width, int height);

    /**
     * Returns <code>true</code> if the given rectangle intersects
     * with the receiver and <code>false</code> otherwise.
     * <p>
     * Two rectangles intersect if the area of the rectangle
     * representing their intersection is not empty.
     * </p>
     *
     * @param rect the rectangle to test for intersection
     * @return <code>true</code> if the rectangle intersects with the receiver, and <code>false</code> otherwise
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     * </ul>
     *
     * @see #intersection(Rectangle)
     * @see #isEmpty()
     */
    boolean intersects(IRectangle rect);

    /**
     * Returns <code>true</code> if the receiver does not cover any
     * area in the (x, y) coordinate plane, and <code>false</code> if
     * the receiver does cover some area in the plane.
     * <p>
     * A rectangle is considered to <em>cover area</em> in the
     * (x, y) coordinate plane if both its width and height are
     * non-zero.
     * </p>
     *
     * @return <code>true</code> if the receiver is empty, and <code>false</code> otherwise
     */
    boolean isEmpty();

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the rectangle
     */
    String toString();

    /**
     * Returns a new rectangle which represents the union of
     * the receiver and the given rectangle.
     * <p>
     * The union of two rectangles is the smallest single rectangle
     * that completely covers both of the areas covered by the two
     * given rectangles.
     * </p>
     *
     * @param rect the rectangle to perform union with
     * @return the union of the receiver and the argument
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     * </ul>
     *
     * @see #add(Rectangle)
     */
    IRectangle union(IRectangle rect);

    Rectangle getApi();

    void setApi(Rectangle api);
}
