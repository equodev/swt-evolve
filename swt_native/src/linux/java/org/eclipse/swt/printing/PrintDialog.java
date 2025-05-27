/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2019 IBM Corporation and others.
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
import org.eclipse.swt.internal.gtk.*;
import org.eclipse.swt.internal.gtk3.*;
import org.eclipse.swt.internal.gtk4.*;
import org.eclipse.swt.widgets.*;
import java.util.WeakHashMap;

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
        this(new SWTPrintDialog((SWTShell) parent.delegate));
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
        this(new SWTPrintDialog((SWTShell) parent.delegate, style));
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
        ((IPrintDialog) this.delegate).setPrinterData(data);
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
        return ((IPrintDialog) this.delegate).getPrinterData();
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
        return ((IPrintDialog) this.delegate).getScope();
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
        ((IPrintDialog) this.delegate).setScope(scope);
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
        return ((IPrintDialog) this.delegate).getStartPage();
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
        ((IPrintDialog) this.delegate).setStartPage(startPage);
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
        return ((IPrintDialog) this.delegate).getEndPage();
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
        ((IPrintDialog) this.delegate).setEndPage(endPage);
    }

    /**
     * Returns the 'Print to file' setting that the user selected
     * before pressing OK in the dialog.
     *
     * @return the 'Print to file' setting that the user selected
     */
    public boolean getPrintToFile() {
        return ((IPrintDialog) this.delegate).getPrintToFile();
    }

    /**
     * Sets the 'Print to file' setting that the user will see
     * when the dialog is opened.
     *
     * @param printToFile the 'Print to file' setting when the dialog is opened
     */
    public void setPrintToFile(boolean printToFile) {
        ((IPrintDialog) this.delegate).setPrintToFile(printToFile);
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
        return ((IPrintDialog) this.delegate).open();
    }

    protected PrintDialog(IPrintDialog delegate) {
        super(delegate);
        this.delegate = delegate;
        INSTANCES.put(delegate, this);
    }

    public static PrintDialog getInstance(IPrintDialog delegate) {
        if (delegate == null) {
            return null;
        }
        PrintDialog ref = (PrintDialog) INSTANCES.get(delegate);
        if (ref == null) {
            ref = new PrintDialog(delegate);
        }
        return ref;
    }
}
