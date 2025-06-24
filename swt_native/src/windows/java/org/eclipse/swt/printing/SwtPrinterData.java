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
package org.eclipse.swt.printing;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

/**
 * Instances of this class are descriptions of a print job
 * in terms of the printer, and the scope and type of printing
 * that is desired. For example, the number of pages and copies
 * can be specified, as well as whether or not the print job
 * should go to a file.
 * <p>
 * Application code does <em>not</em> need to explicitly release the
 * resources managed by each instance when those instances are no longer
 * required, and thus no <code>dispose()</code> method is provided.
 * </p>
 *
 * @see Printer
 * @see Printer#getPrinterList
 * @see PrintDialog#open
 * @see <a href="http://www.eclipse.org/swt/snippets/#printing">Printing snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public final class SwtPrinterData extends SwtDeviceData implements IPrinterData {

    /**
     * the printer driver
     * On Windows systems, this is the name of the driver (often "winspool").
     * On Mac OSX, this is the destination type ("Printer", "Fax", "File", or "Preview").
     * On X/Window systems, this is the name of a display connection to the
     * Xprt server (the default is ":1").
     * On GTK+, this is the backend type name (eg. GtkPrintBackendCups).
     */
    /**
     * private, platform-specific data
     * On Windows, this contains a copy of the DEVMODE struct
     * returned from the <code>PrintDialog</code>.
     * On GTK, this contains a copy of the print_settings and page_setup
     * returned from the <code>PrintDialog</code>.
     * On OS X Cocoa, this contains a copy of the PrintSettings and PageFormat
     * returned from the <code>PrintDialog</code>.
     * This field is not currently used on the X/Window System.
     */
    byte[] otherData;

    /**
     * Constructs an instance of this class that can be
     * used to print to the default printer.
     *
     * @see Printer#getDefaultPrinterData
     */
    public SwtPrinterData(PrinterData api) {
        super(api);
    }

    /**
     * Constructs an instance of this class with the given
     * printer driver and printer name.
     *
     * @param driver the printer driver for the printer
     * @param name the name of the printer
     *
     * @see #driver
     * @see #name
     */
    public SwtPrinterData(String driver, String name, PrinterData api) {
        super(api);
        this.getApi().driver = driver;
        this.getApi().name = name;
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the receiver
     */
    @Override
    public String toString() {
        //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        return "PrinterData {" + "driver = " + getApi().driver + ", name = " + getApi().name + "}";
    }

    public PrinterData getApi() {
        if (api == null)
            api = PrinterData.createApi(this);
        return (PrinterData) api;
    }
}
