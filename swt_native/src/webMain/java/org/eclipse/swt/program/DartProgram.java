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
package org.eclipse.swt.program;

import java.io.*;
import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.widgets.*;
import dev.equo.swt.*;

/**
 * Instances of this class represent programs and
 * their associated file extensions in the operating
 * system.
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#program">Program snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public final class DartProgram implements IProgram {

    String name, fullPath, identifier;

    //$NON-NLS-1$
    static final String PREFIX_FILE = "file:/";

    //$NON-NLS-1$
    static final String PREFIX_HTTP = "http://";

    //$NON-NLS-1$
    static final String PREFIX_HTTPS = "https://";

    /**
     * Prevents uninitialized instances from being created outside the package.
     */
    DartProgram(Program api) {
        setApi(api);
    }

    /**
     *  Finds the program that is associated with an extension.
     *  The extension may or may not begin with a '.'.  Note that
     *  a <code>Display</code> must already exist to guarantee that
     *  this method returns an appropriate result.
     *
     *  @param extension the program extension
     *  @return the program or <code>null</code>
     *
     *  @exception IllegalArgumentException <ul>
     * 		<li>ERROR_NULL_ARGUMENT when extension is null</li>
     * 	</ul>
     */
    public static Program findProgram(String extension) {
        try {
            if (extension == null)
                SWT.error(SWT.ERROR_NULL_ARGUMENT);
            if (extension.length() == 0)
                return null;
            Program program = null;
            char[] chars;
            if (extension.charAt(0) != '.') {
                chars = new char[extension.length()];
                extension.getChars(0, chars.length, chars, 0);
            } else {
                chars = new char[extension.length() - 1];
                extension.getChars(1, extension.length(), chars, 0);
            }
            return program;
        } finally {
        }
    }

    /**
     * Answer all program extensions in the operating system.  Note
     * that a <code>Display</code> must already exist to guarantee
     * that this method returns an appropriate result.
     *
     * @return an array of extensions
     */
    public static String[] getExtensions() {
        try {
        } finally {
        }
        return null;
    }

    /**
     * Answers all available programs in the operating system.  Note
     * that a <code>Display</code> must already exist to guarantee
     * that this method returns an appropriate result.
     *
     * @return an array of programs
     */
    public static Program[] getPrograms() {
        try {
            LinkedHashSet<Program> programs = new LinkedHashSet<>();
            return programs.toArray(new Program[programs.size()]);
        } finally {
        }
    }

    static boolean isExecutable(String fileName) {
        boolean result = false;
        return result;
    }

    /**
     * Launches the operating system executable associated with the file or
     * URL (http:// or https://).  If the file is an executable then the
     * executable is launched.  Note that a <code>Display</code> must already
     * exist to guarantee that this method returns an appropriate result.
     *
     * @param fileName the file or program name or URL (http:// or https://)
     * @return <code>true</code> if the file is launched, otherwise <code>false</code>
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when fileName is null</li>
     * </ul>
     */
    public static boolean launch(String fileName) {
        return launch(fileName, null);
    }

    /**
     * Launches the operating system executable associated with the file or
     * URL (http:// or https://).  If the file is an executable then the
     * executable is launched. The program is launched with the specified
     * working directory only when the <code>workingDir</code> exists and
     * <code>fileName</code> is an executable.
     * Note that a <code>Display</code> must already exist to guarantee
     * that this method returns an appropriate result.
     *
     * @param fileName the file name or program name or URL (http:// or https://)
     * @param workingDir the name of the working directory or null
     * @return <code>true</code> if the file is launched, otherwise <code>false</code>
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when fileName is null</li>
     * </ul>
     *
     * @since 3.6
     */
    public static boolean launch(String fileName, String workingDir) {
        if (fileName == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        //try {    if (workingDir != null && isExecutable(fileName)) {        try {            Compatibility.exec(new String[] { fileName }, null, workingDir);            return true;        } catch (IOException e) {            return false;        }    }    NSURL url = getURL(fileName);    NSWorkspace workspace = NSWorkspace.sharedWorkspace();    return workspace.openURL(url);} finally {    pool.release();}
        ;
        return false;
    }

    /**
     * Executes the program with the file as the single argument
     * in the operating system.  It is the responsibility of the
     * programmer to ensure that the file contains valid data for
     * this program.
     *
     * @param fileName the file or program name
     * @return <code>true</code> if the file is launched, otherwise <code>false</code>
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when fileName is null</li>
     * </ul>
     */
    public boolean execute(String fileName) {
        if (fileName == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        try {
        } finally {
        }
        return false;
    }

    /**
     * Returns the receiver's image data at 100% zoom level.
     * This is the icon that is associated with the receiver
     * in the operating system.
     *
     * @return the image data for the program, may be null
     */
    public ImageData getImageData() {
        return getImageData(100);
    }

    /**
     * Returns the receiver's image data based on the given zoom level.
     * This is the icon that is associated with the receiver in the
     * operating system.
     *
     * @param zoom
     *            The zoom level in % of the standard resolution
     *
     * @return the image data for the program, may be null
     * @since 3.125
     */
    public ImageData getImageData(int zoom) {
        try {
            if (this.fullPath != null) {
            } else {
            }
            return null;
        } finally {
        }
    }

    /**
     * Returns the receiver's name.  This is as short and
     * descriptive a name as possible for the program.  If
     * the program has no descriptive name, this string may
     * be the executable name, path or empty.
     *
     * @return the name of the program
     */
    public String getName() {
        return name;
    }

    /**
     * Compares the argument to the receiver, and returns true
     * if they represent the <em>same</em> object using a class
     * specific comparison.
     *
     * @param other the object to compare with this object
     * @return <code>true</code> if the object is the same as this object and <code>false</code> otherwise
     *
     * @see #hashCode()
     */
    @Override
    public boolean equals(Object other) {
        if (this.getApi() == other)
            return true;
        if (other instanceof final Program program) {
            return name.equals(((DartProgram) program.getImpl()).name) && identifier.equals(((DartProgram) program.getImpl()).identifier);
        }
        return false;
    }

    /**
     * Returns an integer hash code for the receiver. Any two
     * objects that return <code>true</code> when passed to
     * <code>equals</code> must return the same value for this
     * method.
     *
     * @return the receiver's hash
     *
     * @see #equals(Object)
     */
    @Override
    public int hashCode() {
        return name.hashCode() ^ identifier.hashCode();
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the program
     */
    @Override
    public String toString() {
        return "Program {" + name + "}";
    }

    public String _name() {
        return name;
    }

    public Program getApi() {
        if (api == null)
            api = Program.createApi(this);
        return (Program) api;
    }

    protected Program api;

    public void setApi(Program api) {
        this.api = api;
        if (api != null)
            api.impl = this;
    }

    protected VProgram value;

    public VProgram getValue() {
        if (value == null)
            value = new VProgram(this);
        return (VProgram) value;
    }
}
