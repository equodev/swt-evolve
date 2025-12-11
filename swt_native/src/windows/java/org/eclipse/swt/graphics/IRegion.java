package org.eclipse.swt.graphics;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;

public interface IRegion extends IResource {

    /**
     * Adds the given polygon to the collection of polygons
     * the receiver maintains to describe its area.
     *
     * @param pointArray points that describe the polygon to merge with the receiver
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.0
     */
    void add(int[] pointArray);

    /**
     * Adds the given rectangle to the collection of polygons
     * the receiver maintains to describe its area.
     *
     * @param rect the rectangle to merge with the receiver
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the rectangle's width or height is negative</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void add(Rectangle rect);

    /**
     * Adds the given rectangle to the collection of polygons
     * the receiver maintains to describe its area.
     *
     * @param x the x coordinate of the rectangle
     * @param y the y coordinate of the rectangle
     * @param width the width coordinate of the rectangle
     * @param height the height coordinate of the rectangle
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the rectangle's width or height is negative</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.1
     */
    void add(int x, int y, int width, int height);

    /**
     * Adds all of the polygons which make up the area covered
     * by the argument to the collection of polygons the receiver
     * maintains to describe its area.
     *
     * @param region the region to merge
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void add(Region region);

    /**
     * Returns <code>true</code> if the point specified by the
     * arguments is inside the area specified by the receiver,
     * and <code>false</code> otherwise.
     *
     * @param x the x coordinate of the point to test for containment
     * @param y the y coordinate of the point to test for containment
     * @return <code>true</code> if the region contains the point and <code>false</code> otherwise
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    boolean contains(int x, int y);

    /**
     * Returns <code>true</code> if the given point is inside the
     * area specified by the receiver, and <code>false</code>
     * otherwise.
     *
     * @param pt the point to test for containment
     * @return <code>true</code> if the region contains the point and <code>false</code> otherwise
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    boolean contains(Point pt);

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
     * Returns a rectangle which represents the rectangular
     * union of the collection of polygons the receiver
     * maintains to describe its area.
     *
     * @return a bounding rectangle for the region
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see Rectangle#union
     */
    Rectangle getBounds();

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
     * Intersects the given rectangle to the collection of polygons
     * the receiver maintains to describe its area.
     *
     * @param rect the rectangle to intersect with the receiver
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the rectangle's width or height is negative</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.0
     */
    void intersect(Rectangle rect);

    /**
     * Intersects the given rectangle to the collection of polygons
     * the receiver maintains to describe its area.
     *
     * @param x the x coordinate of the rectangle
     * @param y the y coordinate of the rectangle
     * @param width the width coordinate of the rectangle
     * @param height the height coordinate of the rectangle
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the rectangle's width or height is negative</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.1
     */
    void intersect(int x, int y, int width, int height);

    /**
     * Intersects all of the polygons which make up the area covered
     * by the argument to the collection of polygons the receiver
     * maintains to describe its area.
     *
     * @param region the region to intersect
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.0
     */
    void intersect(Region region);

    /**
     * Returns <code>true</code> if the rectangle described by the
     * arguments intersects with any of the polygons the receiver
     * maintains to describe its area, and <code>false</code> otherwise.
     *
     * @param x the x coordinate of the origin of the rectangle
     * @param y the y coordinate of the origin of the rectangle
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     * @return <code>true</code> if the rectangle intersects with the receiver, and <code>false</code> otherwise
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see Rectangle#intersects(Rectangle)
     */
    boolean intersects(int x, int y, int width, int height);

    /**
     * Returns <code>true</code> if the given rectangle intersects
     * with any of the polygons the receiver maintains to describe
     * its area and <code>false</code> otherwise.
     *
     * @param rect the rectangle to test for intersection
     * @return <code>true</code> if the rectangle intersects with the receiver, and <code>false</code> otherwise
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see Rectangle#intersects(Rectangle)
     */
    boolean intersects(Rectangle rect);

    /**
     * Returns <code>true</code> if the region has been disposed,
     * and <code>false</code> otherwise.
     * <p>
     * This method gets the dispose state for the region.
     * When a region has been disposed, it is an error to
     * invoke any other method (except {@link #dispose()}) using the region.
     *
     * @return <code>true</code> when the region is disposed, and <code>false</code> otherwise
     */
    boolean isDisposed();

    /**
     * Returns <code>true</code> if the receiver does not cover any
     * area in the (x, y) coordinate plane, and <code>false</code> if
     * the receiver does cover some area in the plane.
     *
     * @return <code>true</code> if the receiver is empty, and <code>false</code> otherwise
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    boolean isEmpty();

    /**
     * Subtracts the given polygon from the collection of polygons
     * the receiver maintains to describe its area.
     *
     * @param pointArray points that describe the polygon to merge with the receiver
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.0
     */
    void subtract(int[] pointArray);

    /**
     * Subtracts the given rectangle from the collection of polygons
     * the receiver maintains to describe its area.
     *
     * @param rect the rectangle to subtract from the receiver
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the rectangle's width or height is negative</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.0
     */
    void subtract(Rectangle rect);

    /**
     * Subtracts the given rectangle from the collection of polygons
     * the receiver maintains to describe its area.
     *
     * @param x the x coordinate of the rectangle
     * @param y the y coordinate of the rectangle
     * @param width the width coordinate of the rectangle
     * @param height the height coordinate of the rectangle
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the rectangle's width or height is negative</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.1
     */
    void subtract(int x, int y, int width, int height);

    /**
     * Subtracts all of the polygons which make up the area covered
     * by the argument from the collection of polygons the receiver
     * maintains to describe its area.
     *
     * @param region the region to subtract
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.0
     */
    void subtract(Region region);

    /**
     * Translate all of the polygons the receiver maintains to describe
     * its area by the specified point.
     *
     * @param x the x coordinate of the point to translate
     * @param y the y coordinate of the point to translate
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.1
     */
    void translate(int x, int y);

    /**
     * Translate all of the polygons the receiver maintains to describe
     * its area by the specified point.
     *
     * @param pt the point to translate
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.1
     */
    void translate(Point pt);

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the receiver
     */
    String toString();

    Region getApi();
}
