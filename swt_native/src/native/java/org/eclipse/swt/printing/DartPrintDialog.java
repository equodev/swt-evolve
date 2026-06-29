/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2017 IBM Corporation and others.
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
import org.eclipse.swt.internal.*;
import org.eclipse.swt.widgets.*;
import dev.equo.swt.*;

/**
 * Instances of this class allow the user to select
 * a printer and various print-related parameters
 * prior to starting a print job.
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#printing">Printing snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample, Dialog tab</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartPrintDialog extends DartDialog implements IPrintDialog {

    PrinterData printerData = new PrinterData();

    int returnCode;

    static final byte[] SWT_OBJECT = { 'S', 'W', 'T', '_', 'O', 'B', 'J', 'E', 'C', 'T', '\0' };

    //$NON-NLS-1$
    static final String SET_MODAL_DIALOG = "org.eclipse.swt.internal.modalDialog";

    /**
     * Constructs a new instance of this class given only its parent.
     *
     * @param parent a composite control which will be the parent of the new instance (cannot be null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see SWT
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartPrintDialog(Shell parent, PrintDialog api) {
        this(parent, SWT.PRIMARY_MODAL, api);
    }

    /**
     * Constructs a new instance of this class given its parent
     * and a style value describing its behavior and appearance.
     * <p>
     * The style value is either one of the style constants defined in
     * class <code>SWT</code> which is applicable to instances of this
     * class, or must be built by <em>bitwise OR</em>'ing together
     * (that is, using the <code>int</code> "|" operator) two or more
     * of those <code>SWT</code> style constants. The class description
     * lists the style constants that are applicable to the class.
     * Style bits are also inherited from superclasses.
     * </p>
     *
     * @param parent a composite control which will be the parent of the new instance (cannot be null)
     * @param style the style of control to construct
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see SWT
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartPrintDialog(Shell parent, int style, PrintDialog api) {
        super(parent, checkStyle(parent, style), api);
        checkSubclass();
    }

    static int checkStyle(Shell parent, int style) {
        int mask = SWT.PRIMARY_MODAL | SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL;
        if ((style & SWT.SHEET) != 0) {
            if (getSheetEnabled()) {
                if (parent == null) {
                    style &= ~SWT.SHEET;
                }
            } else {
                style &= ~SWT.SHEET;
            }
            if ((style & mask) == 0) {
                style |= parent == null ? SWT.APPLICATION_MODAL : SWT.PRIMARY_MODAL;
            }
        }
        return style;
    }

    /**
     * Sets the printer data that will be used when the dialog
     * is opened.
     * <p>
     * Setting the printer data to null is equivalent to
     * resetting all data fields to their default values.
     * </p>
     *
     * @param data the data that will be used when the dialog is opened or null to use default data
     *
     * @since 3.4
     */
    public void setPrinterData(PrinterData data) {
        if (data == null)
            data = new PrinterData();
        this.printerData = data;
    }

    /**
     * Returns the printer data that will be used when the dialog
     * is opened.
     *
     * @return the data that will be used when the dialog is opened
     *
     * @since 3.4
     */
    public PrinterData getPrinterData() {
        return printerData;
    }

    /**
     * Makes the receiver visible and brings it to the front
     * of the display.
     *
     * @return a printer data object describing the desired print job parameters,
     *         or null if the dialog was canceled, no printers were found, or an error occurred
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public PrinterData open() {
        PrinterData data = null;
        if (printerData.duplex != SWT.DEFAULT) {
        }
        if (printerData.name != null) {
        }
        if (printerData.printToFile) {
        }
        if (printerData.fileName != null && printerData.fileName.length() > 0) {
        }
        if (printerData.scope == PrinterData.PAGE_RANGE) {
        }
        Shell parent = getParent();
        Display display = parent != null ? parent.getDisplay() : DartDisplay.getCurrent();
        if ((getStyle() & SWT.SHEET) != 0) {
            initClasses();
            returnCode = -1;
            while (returnCode == -1) {
                if (!display.readAndDispatch())
                    display.sleep();
            }
        } else {
            display.setData(SET_MODAL_DIALOG, this.getApi());
        }
        display.setData(SET_MODAL_DIALOG, null);
        return data;
    }

    /**
     * Returns the print job scope that the user selected
     * before pressing OK in the dialog. This will be one
     * of the following values:
     * <dl>
     * <dt><code>PrinterData.ALL_PAGES</code></dt>
     * <dd>Print all pages in the current document</dd>
     * <dt><code>PrinterData.PAGE_RANGE</code></dt>
     * <dd>Print the range of pages specified by startPage and endPage</dd>
     * <dt><code>PrinterData.SELECTION</code></dt>
     * <dd>Print the current selection</dd>
     * </dl>
     *
     * @return the scope setting that the user selected
     */
    public int getScope() {
        return printerData.scope;
    }

    static boolean getSheetEnabled() {
        return !"false".equals(System.getProperty("org.eclipse.swt.sheet"));
    }

    void initClasses() {
    }

    /**
     * Sets the scope of the print job. The user will see this
     * setting when the dialog is opened. This can have one of
     * the following values:
     * <dl>
     * <dt><code>PrinterData.ALL_PAGES</code></dt>
     * <dd>Print all pages in the current document</dd>
     * <dt><code>PrinterData.PAGE_RANGE</code></dt>
     * <dd>Print the range of pages specified by startPage and endPage</dd>
     * <dt><code>PrinterData.SELECTION</code></dt>
     * <dd>Print the current selection</dd>
     * </dl>
     *
     * @param scope the scope setting when the dialog is opened
     */
    public void setScope(int scope) {
        int newValue = scope;
        this.scope = newValue;
        printerData.scope = scope;
    }

    /**
     * Returns the start page setting that the user selected
     * before pressing OK in the dialog.
     * <p>
     * This value can be from 1 to the maximum number of pages for the platform.
     * Note that it is only valid if the scope is <code>PrinterData.PAGE_RANGE</code>.
     * </p>
     *
     * @return the start page setting that the user selected
     */
    public int getStartPage() {
        return printerData.startPage;
    }

    /**
     * Sets the start page that the user will see when the dialog
     * is opened.
     * <p>
     * This value can be from 1 to the maximum number of pages for the platform.
     * Note that it is only valid if the scope is <code>PrinterData.PAGE_RANGE</code>.
     * </p>
     *
     * @param startPage the startPage setting when the dialog is opened
     */
    public void setStartPage(int startPage) {
        int newValue = startPage;
        this.startPage = newValue;
        printerData.startPage = startPage;
    }

    /**
     * Returns the end page setting that the user selected
     * before pressing OK in the dialog.
     * <p>
     * This value can be from 1 to the maximum number of pages for the platform.
     * Note that it is only valid if the scope is <code>PrinterData.PAGE_RANGE</code>.
     * </p>
     *
     * @return the end page setting that the user selected
     */
    public int getEndPage() {
        return printerData.endPage;
    }

    /**
     * Sets the end page that the user will see when the dialog
     * is opened.
     * <p>
     * This value can be from 1 to the maximum number of pages for the platform.
     * Note that it is only valid if the scope is <code>PrinterData.PAGE_RANGE</code>.
     * </p>
     *
     * @param endPage the end page setting when the dialog is opened
     */
    public void setEndPage(int endPage) {
        int newValue = endPage;
        this.endPage = newValue;
        printerData.endPage = endPage;
    }

    /**
     * Returns the 'Print to file' setting that the user selected
     * before pressing OK in the dialog.
     *
     * @return the 'Print to file' setting that the user selected
     */
    public boolean getPrintToFile() {
        return printerData.printToFile;
    }

    /**
     * Sets the 'Print to file' setting that the user will see
     * when the dialog is opened.
     *
     * @param printToFile the 'Print to file' setting when the dialog is opened
     */
    public void setPrintToFile(boolean printToFile) {
        boolean newValue = printToFile;
        this.printToFile = newValue;
        printerData.printToFile = printToFile;
    }

    @Override
    public void checkSubclass() {
        String name = getClass().getName();
        String validName = DartPrintDialog.class.getName();
        if (!validName.equals(name)) {
            SWT.error(SWT.ERROR_INVALID_SUBCLASS);
        }
    }

    int endPage;

    boolean printToFile;

    int scope;

    int startPage;

    public PrinterData _printerData() {
        return printerData;
    }

    public int _returnCode() {
        return returnCode;
    }

    public int _endPage() {
        return endPage;
    }

    public boolean _printToFile() {
        return printToFile;
    }

    public int _scope() {
        return scope;
    }

    public int _startPage() {
        return startPage;
    }

    public PrintDialog getApi() {
        if (api == null)
            api = PrintDialog.createApi(this);
        return (PrintDialog) api;
    }

    public VPrintDialog getValue() {
        if (value == null)
            value = new VPrintDialog(this);
        return (VPrintDialog) value;
    }
}
