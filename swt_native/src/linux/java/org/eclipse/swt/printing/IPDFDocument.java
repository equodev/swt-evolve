package org.eclipse.swt.printing;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;

public interface IPDFDocument extends IDevice {

    /**
     * Creates the PDF device in the operating system.
     * This method is called before <code>init</code>.
     *
     * @param data the DeviceData which describes the receiver
     */
    void create(DeviceData data);

    /**
     * Starts a new page in the PDF document.
     * <p>
     * This method should be called after completing the content of one page
     * and before starting to draw on the next page. The new page will have
     * the same dimensions as the initial page.
     * </p>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void newPage();

    /**
     * Starts a new page in the PDF document with the specified dimensions.
     * <p>
     * This method should be called after completing the content of one page
     * and before starting to draw on the next page.
     * </p>
     * <p>
     * <b>Note:</b> On Windows, changing page dimensions after the document
     * has been started may not be fully supported by all printer drivers.
     * </p>
     *
     * @param widthInPoints the preferred width of the new page in points (1/72 inch)
     * @param heightInPoints the preferred height of the new page in points (1/72 inch)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if width or height is not positive</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void newPage(double widthInPoints, double heightInPoints);

    /**
     * Returns the actual width of the current page in points.
     * <p>
     * On Windows, this may be larger than the preferred width specified
     * in the constructor due to standard paper size constraints.
     * </p>
     *
     * @return the actual width in points (1/72 inch)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    double getWidth();

    /**
     * Returns the actual height of the current page in points.
     * <p>
     * On Windows, this may be larger than the preferred height specified
     * in the constructor due to standard paper size constraints.
     * </p>
     *
     * @return the actual height in points (1/72 inch)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    double getHeight();

    /**
     * Returns the DPI (dots per inch) of the PDF document.
     * Since the coordinate system is scaled to work in points (1/72 inch),
     * this always returns 72 DPI, consistent with GTK and Cocoa implementations.
     *
     * @return a point whose x coordinate is the horizontal DPI and whose y coordinate is the vertical DPI
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    Point getDPI();

    /**
     * Returns a rectangle describing the receiver's size and location.
     * The rectangle dimensions are in points (1/72 inch).
     * <p>
     * On Windows, this returns the actual page size which may be larger
     * than the preferred size specified in the constructor.
     * </p>
     *
     * @return the bounding rectangle
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    Rectangle getBounds();

    /**
     * Invokes platform specific functionality to allocate a new GC handle.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
     * API for <code>PDFDocument</code>. It is marked public only so that it
     * can be shared within the packages provided by SWT. It is not
     * available on all platforms, and should never be called from
     * application code.
     * </p>
     *
     * @param data the platform specific GC data
     * @return the platform specific GC handle
     *
     * @noreference This method is not intended to be referenced by clients.
     */
    long internal_new_GC(GCData data);

    /**
     * Invokes platform specific functionality to dispose a GC handle.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
     * API for <code>PDFDocument</code>. It is marked public only so that it
     * can be shared within the packages provided by SWT. It is not
     * available on all platforms, and should never be called from
     * application code.
     * </p>
     *
     * @param hDC the platform specific GC handle
     * @param data the platform specific GC data
     *
     * @noreference This method is not intended to be referenced by clients.
     */
    void internal_dispose_GC(long hDC, GCData data);

    /**
     * @noreference This method is not intended to be referenced by clients.
     */
    boolean isAutoScalable();

    /**
     * Destroys the PDF document handle.
     * This method is called internally by the dispose
     * mechanism of the <code>Device</code> class.
     */
    void destroy();

    PDFDocument getApi();
}
