/**
 * ****************************************************************************
 *  Copyright (c) 2025 Eclipse Platform Contributors and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      Eclipse Platform Contributors - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.swt.printing;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.cocoa.*;

/**
 * Instances of this class are used to create PDF documents.
 * Applications create a GC on a PDFDocument using <code>new GC(pdfDocument)</code>
 * and then draw on the GC using the usual graphics calls.
 * <p>
 * A <code>PDFDocument</code> object may be constructed by providing
 * a filename and the page dimensions. After drawing is complete,
 * the document must be disposed to finalize the PDF file.
 * </p><p>
 * Application code must explicitly invoke the <code>PDFDocument.dispose()</code>
 * method to release the operating system resources managed by each instance
 * when those instances are no longer required.
 * </p>
 * <p>
 * <b>Note:</b> On Windows, this class uses the built-in "Microsoft Print to PDF"
 * printer which is available on Windows 10 and later.
 * </p>
 * <p>
 * The following example demonstrates how to use PDFDocument:
 * </p>
 * <pre>
 *    PDFDocument pdf = new PDFDocument("output.pdf", 612, 792); // Letter size in points
 *    GC gc = new GC(pdf);
 *    gc.drawText("Hello, PDF!", 100, 100);
 *    gc.dispose();
 *    pdf.dispose();
 * </pre>
 *
 * @see GC
 * @since 3.133
 *
 * @noreference This class is provisional API and subject to change. It is being made available to gather early feedback. The API or behavior may change in future releases as the implementation evolves based on user feedback.
 */
public final class PDFDocument extends Device {

    /**
     * Constructs a new PDFDocument with the specified filename and page size.
     * <p>
     * The page size specifies the preferred dimensions in points (1/72 inch). On Windows,
     * the Microsoft Print to PDF driver only supports standard paper sizes, so the actual
     * page size may be larger than requested. Use {@link #getBounds()} to query the actual
     * page dimensions after construction.
     * </p>
     * <p>
     * You must dispose the PDFDocument when it is no longer required.
     * </p>
     *
     * @param filename the path to the PDF file to create
     * @param pageSize the page size specifying width and height in points (1/72 inch)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if filename or pageSize is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if width or height is not positive</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES - if the PDF printer is not available</li>
     * </ul>
     *
     * @see PageSize
     * @see #dispose()
     * @see #getBounds()
     */
    public PDFDocument(String filename, PageSize pageSize) {
        this((IPDFDocument) null);
        setImpl(new SwtPDFDocument(filename, pageSize, this));
    }

    /**
     * Constructs a new PDFDocument with the specified filename and preferred page dimensions.
     * <p>
     * The dimensions specify the preferred page size in points (1/72 inch). On Windows,
     * the Microsoft Print to PDF driver only supports standard paper sizes, so the actual
     * page size may be larger than requested. Use {@link #getBounds()} to query the actual
     * page dimensions after construction.
     * </p>
     * <p>
     * You must dispose the PDFDocument when it is no longer required.
     * </p>
     *
     * @param filename the path to the PDF file to create
     * @param widthInPoints the preferred width of each page in points (1/72 inch)
     * @param heightInPoints the preferred height of each page in points (1/72 inch)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if filename is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if width or height is not positive</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES - if the PDF printer is not available</li>
     * </ul>
     *
     * @see #dispose()
     * @see #getBounds()
     */
    public PDFDocument(String filename, double widthInPoints, double heightInPoints) {
        this((IPDFDocument) null);
        setImpl(new SwtPDFDocument(filename, widthInPoints, heightInPoints, this));
    }

    /**
     * Creates the PDF device in the operating system.
     * This method is called before <code>init</code>.
     *
     * @param data the DeviceData which describes the receiver
     */
    protected void create(DeviceData data) {
        getImpl().create(data);
    }

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
    public void newPage() {
        getImpl().newPage();
    }

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
    public void newPage(double widthInPoints, double heightInPoints) {
        getImpl().newPage(widthInPoints, heightInPoints);
    }

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
    public double getWidth() {
        return getImpl().getWidth();
    }

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
    public double getHeight() {
        return getImpl().getHeight();
    }

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
    public Point getDPI() {
        return getImpl().getDPI();
    }

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
    public Rectangle getBounds() {
        return getImpl().getBounds();
    }

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
    public long internal_new_GC(GCData data) {
        return getImpl().internal_new_GC(data);
    }

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
    public void internal_dispose_GC(long hDC, GCData data) {
        getImpl().internal_dispose_GC(hDC, data);
    }

    /**
     * @noreference This method is not intended to be referenced by clients.
     */
    public boolean isAutoScalable() {
        return getImpl().isAutoScalable();
    }

    /**
     * Destroys the PDF document handle.
     * This method is called internally by the dispose
     * mechanism of the <code>Device</code> class.
     */
    protected void destroy() {
        getImpl().destroy();
    }

    protected PDFDocument(IPDFDocument impl) {
        super(impl);
    }

    static PDFDocument createApi(IPDFDocument impl) {
        return new PDFDocument(impl);
    }

    public IPDFDocument getImpl() {
        return (IPDFDocument) super.getImpl();
    }
}
