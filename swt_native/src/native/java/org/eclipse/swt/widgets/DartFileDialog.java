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
package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;
import dev.equo.swt.*;

/**
 * Instances of this class allow the user to navigate
 * the file system and select or enter a file name.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SAVE, OPEN, MULTI</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles SAVE and OPEN may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#filedialog">FileDialog snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample, Dialog tab</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartFileDialog extends DartDialog implements IIFileDialog {

    String[] filterNames = new String[0];

    String[] filterExtensions = new String[0];

    String[] fileNames = new String[0];

    String filterPath = "", fileName = "";

    String fullPath;

    int filterIndex = -1;

    long jniRef = 0;

    long method_overwriteExistingFileCheck = 0;

    long methodImpl_overwriteExistingFileCheck = 0;

    long method_performKeyEquivalent = 0;

    long methodImpl_performKeyEquivalent = 0;

    static final char EXTENSION_SEPARATOR = ';';

    private String selectedExtension;

    boolean overwrite = true;

    /**
     * Constructs a new instance of this class given only its parent.
     *
     * @param parent a shell which will be the parent of the new instance
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     */
    public DartFileDialog(Shell parent, FileDialog api) {
        this(parent, SWT.APPLICATION_MODAL, api);
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
     * @param parent a shell which will be the parent of the new instance
     * @param style the style of dialog to construct
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see SWT#SAVE
     * @see SWT#OPEN
     * @see SWT#MULTI
     */
    public DartFileDialog(Shell parent, int style, FileDialog api) {
        super(parent, checkStyle(parent, style), api);
        if (DartDisplay.getSheetEnabled()) {
            if (parent != null && (style & SWT.SHEET) != 0)
                this.style |= SWT.SHEET;
        }
        checkSubclass();
    }

    long _completionHandler(long result) {
        handleResponse(result);
        return result;
    }

    /**
     * Returns the path of the first file that was
     * selected in the dialog relative to the filter path, or an
     * empty string if no such file has been selected.
     *
     * @return the relative path of the file
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns a (possibly empty) array with the paths of all files
     * that were selected in the dialog relative to the filter path.
     *
     * @return the relative paths of the files
     */
    public String[] getFileNames() {
        return fileNames;
    }

    /**
     * Returns the file extensions which the dialog will
     * use to filter the files it shows.
     *
     * @return the file extensions filter
     */
    public String[] getFilterExtensions() {
        return filterExtensions;
    }

    /**
     * Get the 0-based index of the file extension filter
     * which was selected by the user, or -1 if no filter
     * was selected.
     * <p>
     * This is an index into the FilterExtensions array and
     * the FilterNames array.
     * </p>
     *
     * @return index the file extension filter index
     *
     * @see #getFilterExtensions
     * @see #getFilterNames
     *
     * @since 3.4
     */
    public int getFilterIndex() {
        return filterIndex;
    }

    /**
     * Returns the names that describe the filter extensions
     * which the dialog will use to filter the files it shows.
     *
     * @return the list of filter names
     */
    public String[] getFilterNames() {
        return filterNames;
    }

    /**
     * Returns the directory path that the dialog will use, or an empty
     * string if this is not set.  File names in this path will appear
     * in the dialog, filtered according to the filter extensions.
     *
     * @return the directory path string
     *
     * @see #setFilterExtensions
     */
    public String getFilterPath() {
        return filterPath;
    }

    /**
     * Returns the flag that the dialog will use to
     * determine whether to prompt the user for file
     * overwrite if the selected file already exists.
     *
     * @return true if the dialog will prompt for file overwrite, false otherwise
     *
     * @since 3.4
     */
    public boolean getOverwrite() {
        return overwrite;
    }

    /**
     * Returns the extension selected in the filter pop-up. When the filter has multiple extensions,
     * the first extension us returned.
     * Returns null if no extension is selected or if the selected filter is * or *.*
     */
    private String getSelectedExtension() {
        return selectedExtension;
    }

    void handleResponse(long response) {
        if (parent != null && (style & SWT.SHEET) != 0) {
        }
        Display display = parent != null ? parent.getDisplay() : DartDisplay.getCurrent();
        ((DartDisplay) display.getImpl()).setModalDialog(null);
        fullPath = null;
        releaseHandles();
    }

    /**
     * Makes the dialog visible and brings it to the front
     * of the display.
     *
     * @return a string describing the absolute path of the first selected file,
     *         or null if the dialog was cancelled or an error occurred
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the dialog has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the dialog</li>
     * </ul>
     */
    public String open() {
        try {
            return openDialog().orElse(null);
        } catch (SWTException e) {
            if (e.code == SWT.ERROR_INVALID_RETURN_VALUE) {
                return null;
            }
            throw e;
        }
    }

    /**
     * Makes the dialog visible and brings it to the front of the display.
     * Equal to {@link FileDialog#open()} but also exposes for state information like user cancellation.
     *
     * @return an Optional that either contains the absolute path of the first selected file
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
    public Optional<String> openDialog() {
        fullPath = null;
        String configuredPath = System.getProperty("dev.equo.swt.test.fileDialog.path");
        String configuredUri = System.getProperty("dev.equo.swt.test.fileDialog.path.uri");
        if (configuredPath == null && configuredUri != null) {
            configuredPath = new java.io.File(java.net.URI.create(configuredUri)).getPath();
        }
        if (configuredPath != null) {
            if (configuredPath.isBlank()) {
                return Optional.empty();
            }
            java.io.File file = new java.io.File(configuredPath);
            fullPath = file.getAbsolutePath();
            fileName = file.getName();
            fileNames = new String[] { fileName };
            String parentPath = file.getParent();
            if (parentPath != null) {
                filterPath = parentPath;
            }
            return Optional.of(fullPath);
        }
        if ((style & SWT.SAVE) != 0) {
            if (!overwrite) {
                if (method_overwriteExistingFileCheck != 0) {
                }
            }
        } else {
        }
        if (method_performKeyEquivalent != 0) {
        }
        if (filterPath != null && filterPath.length() > 0) {
        }
        if (fileName != null && fileName.length() > 0) {
        }
        /*
	 * This line is intentionally commented. Don't show hidden files forcefully,
	 * instead allow File dialog to use the system preference.
	 */
        //	OS.objc_msgSend(panel.id, OS.sel_setShowsHiddenFiles_, true);
        jniRef = 0;
        if (filterExtensions != null && filterExtensions.length != 0) {
            if (jniRef == 0)
                error(SWT.ERROR_NO_HANDLES);
            for (int i = 0; i < filterExtensions.length; i++) {
                String str = filterExtensions[i];
                if (filterNames != null && filterNames.length > i) {
                    str = filterNames[i];
                }
            }
            if ((style & SWT.SAVE) != 0) {
            } else {
            }
        } else {
        }
        if (parent != null && (style & SWT.SHEET) != 0) {
        } else {
        }
        return Optional.ofNullable(fullPath);
    }

    void releaseHandles() {
        if (!overwrite) {
            if (method_overwriteExistingFileCheck != 0) {
            }
        }
        if (method_performKeyEquivalent != 0) {
        }
        jniRef = 0;
    }

    /**
     * Set the initial filename which the dialog will
     * select by default when opened to the argument,
     * which may be null.  The name will be prefixed with
     * the filter path when one is supplied.
     *
     * @param string the file name
     */
    public void setFileName(String string) {
        fileName = string;
    }

    /**
     * Set the file extensions which the dialog will
     * use to filter the files it shows to the argument,
     * which may be null.
     * <p>
     * The strings are platform specific. For example, on
     * some platforms, an extension filter string is typically
     * of the form "*.extension", where "*.*" matches all files.
     * For filters with multiple extensions, use semicolon as
     * a separator, e.g. "*.jpg;*.png".
     * </p>
     * <p>
     * Note: On Mac, setting the file extension filter affects how
     * app bundles are treated by the dialog. When a filter extension
     * having the app extension (.app) is selected, bundles are treated
     * as files. For all other extension filters, bundles are treated
     * as directories. When no filter extension is set, bundles are
     * treated as files.
     * </p>
     *
     * @param extensions the file extension filter
     *
     * @see #setFilterNames to specify the user-friendly
     * names corresponding to the extensions
     */
    public void setFilterExtensions(String... extensions) {
        filterExtensions = extensions;
    }

    /**
     * Set the 0-based index of the file extension filter
     * which the dialog will use initially to filter the files
     * it shows to the argument.
     * <p>
     * This is an index into the FilterExtensions array and
     * the FilterNames array.
     * </p>
     *
     * @param index the file extension filter index
     *
     * @see #setFilterExtensions
     * @see #setFilterNames
     *
     * @since 3.4
     */
    public void setFilterIndex(int index) {
        filterIndex = index;
    }

    /**
     * Sets the names that describe the filter extensions
     * which the dialog will use to filter the files it shows
     * to the argument, which may be null.
     * <p>
     * Each name is a user-friendly short description shown for
     * its corresponding filter. The <code>names</code> array must
     * be the same length as the <code>extensions</code> array.
     * </p>
     *
     * @param names the list of filter names, or null for no filter names
     *
     * @see #setFilterExtensions
     */
    public void setFilterNames(String... names) {
        filterNames = names;
    }

    /**
     * Sets the directory path that the dialog will use
     * to the argument, which may be null. File names in this
     * path will appear in the dialog, filtered according
     * to the filter extensions. If the string is null,
     * then the operating system's default filter path
     * will be used.
     * <p>
     * Note that the path string is platform dependent.
     * For convenience, either '/' or '\' can be used
     * as a path separator.
     * </p>
     *
     * @param string the directory path
     *
     * @see #setFilterExtensions
     */
    public void setFilterPath(String string) {
        filterPath = string;
    }

    /**
     * Sets the flag that the dialog will use to
     * determine whether to prompt the user for file
     * overwrite if the selected file already exists.
     * <p>
     * Note: On some platforms where suppressing the overwrite prompt
     * is not supported, the prompt is shown even when invoked with
     * overwrite false.
     * </p>
     *
     * @param overwrite true if the dialog will prompt for file overwrite, false otherwise
     *
     * @since 3.4
     */
    public void setOverwrite(boolean overwrite) {
    }

    /**
     * Determines if the treatAppAsBundle property has to be enabled for the NSOpenPanel
     * for the filterExtensions passed as a parameter.
     */
    boolean shouldTreatAppAsDirectory(String extensions) {
        if ((style & SWT.SAVE) != 0)
            return false;
        StringTokenizer fileTypesToken = new StringTokenizer(extensions, String.valueOf(EXTENSION_SEPARATOR));
        while (fileTypesToken.hasMoreTokens()) {
            String fileType = fileTypesToken.nextToken();
            if (fileType.equals("*") || fileType.equals("*.*"))
                return true;
            if (fileType.equals("*.app") || fileType.equals(".app"))
                return false;
        }
        return true;
    }

    public String[] _filterNames() {
        return filterNames;
    }

    public String[] _filterExtensions() {
        return filterExtensions;
    }

    public String[] _fileNames() {
        return fileNames;
    }

    public String _filterPath() {
        return filterPath;
    }

    public String _fileName() {
        return fileName;
    }

    public int _filterIndex() {
        return filterIndex;
    }

    public boolean _overwrite() {
        return overwrite;
    }

    public FileDialog getApi() {
        if (api == null)
            api = FileDialog.createApi(this);
        return (FileDialog) api;
    }

    public VFileDialog getValue() {
        if (value == null)
            value = new VFileDialog(this);
        return (VFileDialog) value;
    }
}
