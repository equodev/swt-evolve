package org.eclipse.swt.printing;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface IPrinter extends IDevice {

    /**
     * Creates the printer handle.
     * This method is called internally by the instance creation
     * mechanism of the <code>Device</code> class.
     * @param deviceData the device data
     */
    void create(DeviceData deviceData);

    /**
     * Invokes platform specific functionality to allocate a new GC handle.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
     * API for <code>Printer</code>. It is marked public only so that it
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
     * API for <code>Printer</code>. It is marked public only so that it
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
     * Starts a print job and returns true if the job started successfully
     * and false otherwise.
     * <p>
     * This must be the first method called to initiate a print job,
     * followed by any number of startPage/endPage calls, followed by
     * endJob. Calling startPage, endPage, or endJob before startJob
     * will result in undefined behavior.
     * </p>
     *
     * @param jobName the name of the print job to start
     * @return true if the job started successfully and false otherwise.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #startPage
     * @see #endPage
     * @see #endJob
     */
    boolean startJob(String jobName);

    /**
     * Ends the current print job.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #startJob
     * @see #startPage
     * @see #endPage
     */
    void endJob();

    /**
     * Cancels a print job in progress.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void cancelJob();

    /**
     * Starts a page and returns true if the page started successfully
     * and false otherwise.
     * <p>
     * After calling startJob, this method may be called any number of times
     * along with a matching endPage.
     * </p>
     *
     * @return true if the page started successfully and false otherwise.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #endPage
     * @see #startJob
     * @see #endJob
     */
    boolean startPage();

    /**
     * Ends the current page.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #startPage
     * @see #startJob
     * @see #endJob
     */
    void endPage();

    /**
     * Returns a point whose x coordinate is the horizontal
     * dots per inch of the printer, and whose y coordinate
     * is the vertical dots per inch of the printer.
     *
     * @return the horizontal and vertical DPI
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    Point getDPI();

    /**
     * Returns a rectangle describing the receiver's size and location.
     * <p>
     * For a printer, this is the size of the physical page, in pixels.
     * </p>
     *
     * @return the bounding rectangle
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #getClientArea
     * @see #computeTrim
     */
    Rectangle getBounds();

    /**
     * Returns a rectangle which describes the area of the
     * receiver which is capable of displaying data.
     * <p>
     * For a printer, this is the size of the printable area
     * of the page, in pixels.
     * </p>
     *
     * @return the client area
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #getBounds
     * @see #computeTrim
     */
    Rectangle getClientArea();

    /**
     * Given a <em>client area</em> (as described by the arguments),
     * returns a rectangle, relative to the client area's coordinates,
     * that is the client area expanded by the printer's trim (or minimum margins).
     * <p>
     * Most printers have a minimum margin on each edge of the paper where the
     * printer device is unable to print.  This margin is known as the "trim."
     * This method can be used to calculate the printer's minimum margins
     * by passing in a client area of 0, 0, 0, 0 and then using the resulting
     * x and y coordinates (which will be &lt;= 0) to determine the minimum margins
     * for the top and left edges of the paper, and the resulting width and height
     * (offset by the resulting x and y) to determine the minimum margins for the
     * bottom and right edges of the paper, as follows:
     * </p>
     * <ul>
     * 		<li>The left trim width is -x pixels</li>
     * 		<li>The top trim height is -y pixels</li>
     * 		<li>The right trim width is (x + width) pixels</li>
     * 		<li>The bottom trim height is (y + height) pixels</li>
     * </ul>
     *
     * @param x the x coordinate of the client area
     * @param y the y coordinate of the client area
     * @param width the width of the client area
     * @param height the height of the client area
     * @return a rectangle, relative to the client area's coordinates, that is
     * 		the client area expanded by the printer's trim (or minimum margins)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #getBounds
     * @see #getClientArea
     */
    Rectangle computeTrim(int x, int y, int width, int height);

    /**
     * Returns a <code>PrinterData</code> object representing the
     * target printer for this print job.
     *
     * @return a PrinterData object describing the receiver
     */
    PrinterData getPrinterData();

    /**
     * Checks the validity of this device.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void checkDevice();

    /**
     * Releases any internal state prior to destroying this printer.
     * This method is called internally by the dispose
     * mechanism of the <code>Device</code> class.
     */
    void release();

    /**
     * Destroys the printer handle.
     * This method is called internally by the dispose
     * mechanism of the <code>Device</code> class.
     */
    void destroy();

    Printer getApi();
}
