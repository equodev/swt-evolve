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
import org.eclipse.swt.internal.cocoa.*;
import org.eclipse.swt.widgets.*;

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
public class PrintDialog extends Dialog {

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
    public PrintDialog(Shell parent) {
        this((IPrintDialog) null);
        setImpl(new SwtPrintDialog(parent));
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
    public PrintDialog(Shell parent, int style) {
        this((IPrintDialog) null);
        setImpl(new SwtPrintDialog(parent, style));
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
        getImpl().setPrinterData(data);
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
        return getImpl().getPrinterData();
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
        return getImpl().open();
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
        return getImpl().getScope();
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
        getImpl().setScope(scope);
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
        return getImpl().getStartPage();
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
        getImpl().setStartPage(startPage);
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
        return getImpl().getEndPage();
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
        getImpl().setEndPage(endPage);
    }

    /**
     * Returns the 'Print to file' setting that the user selected
     * before pressing OK in the dialog.
     *
     * @return the 'Print to file' setting that the user selected
     */
    public boolean getPrintToFile() {
        return getImpl().getPrintToFile();
    }

    /**
     * Sets the 'Print to file' setting that the user will see
     * when the dialog is opened.
     *
     * @param printToFile the 'Print to file' setting when the dialog is opened
     */
    public void setPrintToFile(boolean printToFile) {
        getImpl().setPrintToFile(printToFile);
    }

    protected void checkSubclass() {
        getImpl().checkSubclass();
    }

    protected PrintDialog(IPrintDialog impl) {
        super(impl);
    }

    static PrintDialog createApi(IPrintDialog impl) {
        if (dev.equo.swt.Creation.creating.peek() instanceof PrintDialog inst) {
            inst.impl = impl;
            return inst;
        } else
            return new PrintDialog(impl);
    }

    public IPrintDialog getImpl() {
        return (IPrintDialog) super.getImpl();
    }
}
