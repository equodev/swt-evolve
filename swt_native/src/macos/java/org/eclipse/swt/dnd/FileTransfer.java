/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
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
 *      Outhink - support for typeFileURL
 * *****************************************************************************
 */
package org.eclipse.swt.dnd;

import org.eclipse.swt.internal.cocoa.*;

/**
 * The class <code>FileTransfer</code> provides a platform specific mechanism
 * for converting a list of files represented as a java <code>String[]</code> to a
 * platform specific representation of the data and vice versa.
 * Each <code>String</code> in the array contains the absolute path for a single
 * file or directory.
 *
 * <p>An example of a java <code>String[]</code> containing a list of files is shown
 * below:</p>
 *
 * <pre><code>
 *     File file1 = new File("C:\\temp\\file1");
 *     File file2 = new File("C:\\temp\\file2");
 *     String[] fileData = new String[2];
 *     fileData[0] = file1.getAbsolutePath();
 *     fileData[1] = file2.getAbsolutePath();
 * </code></pre>
 *
 * @see Transfer
 */
public class FileTransfer extends ByteArrayTransfer {

    /**
     * Returns the singleton instance of the FileTransfer class.
     *
     * @return the singleton instance of the FileTransfer class
     */
    public static FileTransfer getInstance() {
        return SwtFileTransfer.getInstance();
    }

    /**
     * This implementation of <code>javaToNative</code> converts a list of file names
     * represented by a java <code>String[]</code> to a platform specific representation.
     * Each <code>String</code> in the array contains the absolute path for a single
     * file or directory.
     *
     * @param object a java <code>String[]</code> containing the file names to be converted
     * @param transferData an empty <code>TransferData</code> object that will
     *  	be filled in on return with the platform specific format of the data
     *
     * @see Transfer#nativeToJava
     */
    public void javaToNative(Object object, TransferData transferData) {
        getImpl().javaToNative(object, transferData);
    }

    /**
     * This implementation of <code>nativeToJava</code> converts a platform specific
     * representation of a list of file names to a java <code>String[]</code>.
     * Each String in the array contains the absolute path for a single file or directory.
     *
     * @param transferData the platform specific representation of the data to be converted
     * @return a java <code>String[]</code> containing a list of file names if the conversion
     * 		was successful; otherwise null
     *
     * @see Transfer#javaToNative
     */
    public Object nativeToJava(TransferData transferData) {
        return getImpl().nativeToJava(transferData);
    }

    protected int[] getTypeIds() {
        return getImpl().getTypeIds();
    }

    protected String[] getTypeNames() {
        return getImpl().getTypeNames();
    }

    protected boolean validate(Object object) {
        return getImpl().validate(object);
    }

    protected FileTransfer(IFileTransfer impl) {
        super(impl);
    }

    public static FileTransfer createApi(IFileTransfer impl) {
        return new FileTransfer(impl);
    }

    public IFileTransfer getImpl() {
        return (IFileTransfer) super.getImpl();
    }
}
