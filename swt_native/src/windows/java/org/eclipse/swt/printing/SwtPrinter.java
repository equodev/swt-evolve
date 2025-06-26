/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2016 IBM Corporation and others.
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
package org.eclipse.swt.printing;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.win32.*;

/**
 * Instances of this class are used to print to a printer.
 * Applications create a GC on a printer using <code>new GC(printer)</code>
 * and then draw on the printer GC using the usual graphics calls.
 * <p>
 * A <code>Printer</code> object may be constructed by providing
 * a <code>PrinterData</code> object which identifies the printer.
 * A <code>PrintDialog</code> presents a print dialog to the user
 * and returns an initialized instance of <code>PrinterData</code>.
 * Alternatively, calling <code>new Printer()</code> will construct a
 * printer object for the user's default printer.
 * </p><p>
 * Application code must explicitly invoke the <code>Printer.dispose()</code>
 * method to release the operating system resources managed by each instance
 * when those instances are no longer required.
 * </p>
 *
 * @see PrinterData
 * @see PrintDialog
 * @see <a href="http://www.eclipse.org/swt/snippets/#printing">Printing snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public final class SwtPrinter extends SwtDevice implements IPrinter {

    /**
     * the printer data describing this printer
     */
    PrinterData data;

    /**
     * whether or not a GC was created for this printer
     */
    boolean isGCCreated = false;

    /**
     * strings used to access the Windows registry
     */
    static TCHAR profile;

    static TCHAR appName;

    static TCHAR keyName;

    static {
        //$NON-NLS-1$
        profile = new TCHAR(0, "PrinterPorts", true);
        //$NON-NLS-1$
        appName = new TCHAR(0, "windows", true);
        //$NON-NLS-1$
        keyName = new TCHAR(0, "device", true);
    }

    /**
     * Returns an array of <code>PrinterData</code> objects
     * representing all available printers.  If there are no
     * printers, the array will be empty.
     *
     * @return an array of PrinterData objects representing the available printers
     */
    public static PrinterData[] getPrinterList() {
        int length = 1024;
        TCHAR buf = new TCHAR(0, length);
        TCHAR nullBuf = new TCHAR(0, 1);
        int n = OS.GetProfileString(profile, null, nullBuf, buf, length);
        if (n == 0)
            return new PrinterData[0];
        String[] deviceNames = new String[5];
        int nameCount = 0;
        int index = 0;
        for (int i = 0; i < n; i++) {
            if (buf.tcharAt(i) == 0) {
                if (nameCount == deviceNames.length) {
                    String[] newNames = new String[deviceNames.length + 5];
                    System.arraycopy(deviceNames, 0, newNames, 0, deviceNames.length);
                    deviceNames = newNames;
                }
                deviceNames[nameCount] = buf.toString(index, i - index);
                nameCount++;
                index = i + 1;
            }
        }
        PrinterData[] printerList = new PrinterData[nameCount];
        for (int p = 0; p < nameCount; p++) {
            String device = deviceNames[p];
            //$NON-NLS-1$
            String driver = "";
            if (OS.GetProfileString(profile, new TCHAR(0, device, true), nullBuf, buf, length) > 0) {
                int commaIndex = 0;
                while (buf.tcharAt(commaIndex) != ',' && commaIndex < length) commaIndex++;
                if (commaIndex < length) {
                    driver = buf.toString(0, commaIndex);
                }
            }
            printerList[p] = new PrinterData(driver, device);
        }
        return printerList;
    }

    /**
     * Returns a <code>PrinterData</code> object representing
     * the default printer or <code>null</code> if there is no
     * default printer.
     *
     * @return the default printer data or null
     *
     * @since 2.1
     */
    public static PrinterData getDefaultPrinterData() {
        String deviceName = null;
        int length = 1024;
        TCHAR buf = new TCHAR(0, length);
        TCHAR nullBuf = new TCHAR(0, 1);
        int n = OS.GetProfileString(appName, keyName, nullBuf, buf, length);
        if (n == 0)
            return null;
        int commaIndex = 0;
        while (buf.tcharAt(commaIndex) != ',' && commaIndex < length) commaIndex++;
        if (commaIndex < length) {
            deviceName = buf.toString(0, commaIndex);
        }
        if (deviceName == null)
            return null;
        //$NON-NLS-1$
        String driver = "";
        if (OS.GetProfileString(profile, new TCHAR(0, deviceName, true), nullBuf, buf, length) > 0) {
            commaIndex = 0;
            while (buf.tcharAt(commaIndex) != ',' && commaIndex < length) commaIndex++;
            if (commaIndex < length) {
                driver = buf.toString(0, commaIndex);
            }
        }
        return new PrinterData(driver, deviceName);
    }

    static DeviceData checkNull(PrinterData data) {
        if (data == null)
            data = new PrinterData();
        if (data.driver == null || data.name == null) {
            PrinterData defaultPrinter = getDefaultPrinterData();
            if (defaultPrinter == null)
                SWT.error(SWT.ERROR_NO_HANDLES);
            data.driver = defaultPrinter.driver;
            data.name = defaultPrinter.name;
        }
        return data;
    }

    /**
     * Constructs a new printer representing the default printer.
     * <p>
     * Note: You must dispose the printer when it is no longer required.
     * </p>
     *
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES - if there is no valid default printer
     * </ul>
     *
     * @see Device#dispose
     */
    public SwtPrinter(Printer api) {
        this(null, api);
    }

    /**
     * Constructs a new printer given a <code>PrinterData</code>
     * object representing the desired printer. If the argument
     * is null, then the default printer will be used.
     * <p>
     * Note: You must dispose the printer when it is no longer required.
     * </p>
     *
     * @param data the printer data for the specified printer, or null to use the default printer
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the specified printer data does not represent a valid printer
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES - if there are no valid printers
     * </ul>
     *
     * @see Device#dispose
     */
    public SwtPrinter(PrinterData data, Printer api) {
        super(checkNull(data), api);
    }

    /**
     * Creates the printer handle.
     * This method is called internally by the instance creation
     * mechanism of the <code>Device</code> class.
     * @param deviceData the device data
     */
    @Override
    public void create(DeviceData deviceData) {
        data = (PrinterData) deviceData;
        TCHAR driver = new TCHAR(0, data.driver, true);
        TCHAR device = new TCHAR(0, data.name, true);
        long lpInitData = 0;
        byte[] devmodeData = ((SwtPrinterData) data.getImpl()).otherData;
        long hHeap = OS.GetProcessHeap();
        if (devmodeData != null && devmodeData.length != 0) {
            /* If user setup info from a print dialog was specified, restore the DEVMODE struct. */
            lpInitData = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, devmodeData.length);
            OS.MoveMemory(lpInitData, devmodeData, devmodeData.length);
        } else {
            long[] hPrinter = new long[1];
            OS.OpenPrinter(device, hPrinter, 0);
            if (hPrinter[0] != 0) {
                int dwNeeded = OS.DocumentProperties(0, hPrinter[0], device, 0, 0, 0);
                if (dwNeeded >= 0) {
                    lpInitData = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, dwNeeded);
                    int rc = OS.DocumentProperties(0, hPrinter[0], device, lpInitData, 0, OS.DM_OUT_BUFFER);
                    if (rc != OS.IDOK) {
                        OS.HeapFree(hHeap, 0, lpInitData);
                        lpInitData = 0;
                    }
                }
                OS.ClosePrinter(hPrinter[0]);
            }
        }
        /* Initialize DEVMODE struct fields from the printerData. */
        if (lpInitData != 0) {
            DEVMODE devmode = new DEVMODE();
            OS.MoveMemory(devmode, lpInitData, DEVMODE.sizeof);
            devmode.dmFields |= OS.DM_ORIENTATION;
            devmode.dmOrientation = data.orientation == PrinterData.LANDSCAPE ? OS.DMORIENT_LANDSCAPE : OS.DMORIENT_PORTRAIT;
            if (data.copyCount != 1) {
                devmode.dmFields |= OS.DM_COPIES;
                devmode.dmCopies = (short) data.copyCount;
            }
            if (data.collate) {
                devmode.dmFields |= OS.DM_COLLATE;
                devmode.dmCollate = OS.DMCOLLATE_TRUE;
            }
            if (data.duplex != SWT.DEFAULT) {
                devmode.dmFields |= OS.DM_DUPLEX;
                switch(data.duplex) {
                    case PrinterData.DUPLEX_SHORT_EDGE:
                        devmode.dmDuplex = OS.DMDUP_HORIZONTAL;
                        break;
                    case PrinterData.DUPLEX_LONG_EDGE:
                        devmode.dmDuplex = OS.DMDUP_VERTICAL;
                        break;
                    default:
                        devmode.dmDuplex = OS.DMDUP_SIMPLEX;
                }
            }
            OS.MoveMemory(lpInitData, devmode, DEVMODE.sizeof);
        }
        getApi().handle = OS.CreateDC(driver, device, 0, lpInitData);
        if (lpInitData != 0)
            OS.HeapFree(hHeap, 0, lpInitData);
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_NO_HANDLES);
    }

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
    @Override
    public long internal_new_GC(GCData data) {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_NO_HANDLES);
        if (data != null) {
            if (isGCCreated)
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
            int mask = SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT;
            if ((data.style & mask) != 0) {
                data.layout = (data.style & SWT.RIGHT_TO_LEFT) != 0 ? OS.LAYOUT_RTL : 0;
            } else {
                data.style |= SWT.LEFT_TO_RIGHT;
            }
            data.device = this.getApi();
            data.font = SwtFont.win32_new(this.getApi(), OS.GetCurrentObject(getApi().handle, OS.OBJ_FONT));
            isGCCreated = true;
        }
        return getApi().handle;
    }

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
    @Override
    public void internal_dispose_GC(long hDC, GCData data) {
        if (data != null)
            isGCCreated = false;
    }

    /**
     * @noreference This method is not intended to be referenced by clients.
     */
    @Override
    public boolean isAutoScalable() {
        return false;
    }

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
    public boolean startJob(String jobName) {
        checkDevice();
        DOCINFO di = new DOCINFO();
        di.cbSize = DOCINFO.sizeof;
        long hHeap = OS.GetProcessHeap();
        long lpszDocName = 0;
        if (jobName != null && jobName.length() != 0) {
            TCHAR buffer = new TCHAR(0, jobName, true);
            int byteCount = buffer.length() * TCHAR.sizeof;
            lpszDocName = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, byteCount);
            OS.MoveMemory(lpszDocName, buffer, byteCount);
            di.lpszDocName = lpszDocName;
        }
        long lpszOutput = 0;
        if (data.printToFile) {
            if (data.fileName == null) {
                /* Prompt the user for a file name. */
                //$NON-NLS-1$
                data.fileName = "FILE:";
            }
            TCHAR buffer = new TCHAR(0, data.fileName, true);
            int byteCount = buffer.length() * TCHAR.sizeof;
            lpszOutput = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, byteCount);
            OS.MoveMemory(lpszOutput, buffer, byteCount);
            di.lpszOutput = lpszOutput;
        }
        int rc = OS.StartDoc(getApi().handle, di);
        if (lpszDocName != 0)
            OS.HeapFree(hHeap, 0, lpszDocName);
        if (lpszOutput != 0)
            OS.HeapFree(hHeap, 0, lpszOutput);
        return rc > 0;
    }

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
    public void endJob() {
        checkDevice();
        OS.EndDoc(getApi().handle);
    }

    /**
     * Cancels a print job in progress.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public void cancelJob() {
        checkDevice();
        OS.AbortDoc(getApi().handle);
    }

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
    public boolean startPage() {
        checkDevice();
        int rc = OS.StartPage(getApi().handle);
        if (rc <= 0)
            OS.AbortDoc(getApi().handle);
        return rc > 0;
    }

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
    public void endPage() {
        checkDevice();
        OS.EndPage(getApi().handle);
    }

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
    @Override
    public Point getDPI() {
        checkDevice();
        int dpiX = OS.GetDeviceCaps(getApi().handle, OS.LOGPIXELSX);
        int dpiY = OS.GetDeviceCaps(getApi().handle, OS.LOGPIXELSY);
        return new Point(dpiX, dpiY);
    }

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
    @Override
    public Rectangle getBounds() {
        checkDevice();
        int width = OS.GetDeviceCaps(getApi().handle, OS.PHYSICALWIDTH);
        int height = OS.GetDeviceCaps(getApi().handle, OS.PHYSICALHEIGHT);
        return new Rectangle(0, 0, width, height);
    }

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
    @Override
    public Rectangle getClientArea() {
        checkDevice();
        int width = OS.GetDeviceCaps(getApi().handle, OS.HORZRES);
        int height = OS.GetDeviceCaps(getApi().handle, OS.VERTRES);
        return new Rectangle(0, 0, width, height);
    }

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
    public Rectangle computeTrim(int x, int y, int width, int height) {
        checkDevice();
        int printX = -OS.GetDeviceCaps(getApi().handle, OS.PHYSICALOFFSETX);
        int printY = -OS.GetDeviceCaps(getApi().handle, OS.PHYSICALOFFSETY);
        int printWidth = OS.GetDeviceCaps(getApi().handle, OS.HORZRES);
        int printHeight = OS.GetDeviceCaps(getApi().handle, OS.VERTRES);
        int paperWidth = OS.GetDeviceCaps(getApi().handle, OS.PHYSICALWIDTH);
        int paperHeight = OS.GetDeviceCaps(getApi().handle, OS.PHYSICALHEIGHT);
        int hTrim = paperWidth - printWidth;
        int vTrim = paperHeight - printHeight;
        return new Rectangle(x + printX, y + printY, width + hTrim, height + vTrim);
    }

    /**
     * Returns a <code>PrinterData</code> object representing the
     * target printer for this print job.
     *
     * @return a PrinterData object describing the receiver
     */
    public PrinterData getPrinterData() {
        return data;
    }

    /**
     * Checks the validity of this device.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    @Override
    public void checkDevice() {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_DEVICE_DISPOSED);
    }

    /**
     * Releases any internal state prior to destroying this printer.
     * This method is called internally by the dispose
     * mechanism of the <code>Device</code> class.
     */
    @Override
    public void release() {
        super.release();
        data = null;
    }

    /**
     * Destroys the printer handle.
     * This method is called internally by the dispose
     * mechanism of the <code>Device</code> class.
     */
    @Override
    public void destroy() {
        if (getApi().handle != 0)
            OS.DeleteDC(getApi().handle);
        getApi().handle = 0;
    }

    public Printer getApi() {
        if (api == null)
            api = Printer.createApi(this);
        return (Printer) api;
    }
}
