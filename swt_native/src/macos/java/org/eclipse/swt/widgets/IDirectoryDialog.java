package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;

public interface IDirectoryDialog extends IDialog {

    /**
     * Returns the path which the dialog will use to filter
     * the directories it shows.
     *
     * @return the filter path
     *
     * @see #setFilterPath
     */
    String getFilterPath();

    /**
     * Returns the dialog's message, which is a description of
     * the purpose for which it was opened. This message will be
     * visible on the dialog while it is open.
     *
     * @return the message
     */
    String getMessage();

    /**
     * Makes the dialog visible and brings it to the front
     * of the display.
     *
     * @return a string describing the absolute path of the selected directory,
     *         or null if the dialog was cancelled or an error occurred
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the dialog has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the dialog</li>
     * </ul>
     */
    String open();

    /**
     * Makes the dialog visible and brings it to the front of the display.
     * Equal to {@link DirectoryDialog#open()} but also exposes for state information like user cancellation.
     *
     * @return an Optional that either contains the absolute path of the selected directory
     *         or is empty in case the dialog was canceled
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the dialog has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the dialog</li>
     *    <li>ERROR_INVALID_RETURN_VALUE - if the dialog was not cancelled and did not return a valid path</li>
     * </ul>
     *
     * @since 3.126
     */
    Optional<String> openDialog();

    /**
     * Sets the dialog's message, which is a description of
     * the purpose for which it was opened. This message will be
     * visible on the dialog while it is open.
     * <p>
     * NOTE: This operation is a hint and is not supported on some platforms. For
     * example, on Windows (Vista and later), the <code>DirectoryDialog</code>
     * doesn't have any provision to set a message.
     * </p>
     *
     * @param string the message
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
     * </ul>
     */
    void setMessage(String string);

    /**
     * Sets the path that the dialog will use to filter
     * the directories it shows to the argument, which may
     * be null. If the string is null, then the operating
     * system's default filter path will be used.
     * <p>
     * Note that the path string is platform dependent.
     * For convenience, either '/' or '\' can be used
     * as a path separator.
     * </p>
     *
     * @param string the filter path
     */
    void setFilterPath(String string);

    DirectoryDialog getApi();

    void setApi(DirectoryDialog api);
}
