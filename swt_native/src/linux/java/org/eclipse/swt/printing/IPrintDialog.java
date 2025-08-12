package org.eclipse.swt.printing;

import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.widgets.*;

public interface IPrintDialog extends IDialog {

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
    void setPrinterData(PrinterData data);

    /**
     * Returns the printer data that will be used when the dialog
     * is opened.
     *
     * @return the data that will be used when the dialog is opened
     *
     * @since 3.4
     */
    PrinterData getPrinterData();

    void checkSubclass();

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
    int getScope();

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
    void setScope(int scope);

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
    int getStartPage();

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
    void setStartPage(int startPage);

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
    int getEndPage();

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
    void setEndPage(int endPage);

    /**
     * Returns the 'Print to file' setting that the user selected
     * before pressing OK in the dialog.
     *
     * @return the 'Print to file' setting that the user selected
     */
    boolean getPrintToFile();

    /**
     * Sets the 'Print to file' setting that the user will see
     * when the dialog is opened.
     *
     * @param printToFile the 'Print to file' setting when the dialog is opened
     */
    void setPrintToFile(boolean printToFile);

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
    PrinterData open();

    PrintDialog getApi();
}
