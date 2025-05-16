/**
 * ****************************************************************************
 *  Copyright (c) 20007 IBM Corporation and others.
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
package org.eclipse.swt.dnd;

import org.eclipse.swt.internal.cocoa.*;

/**
 * The class <code>URLTransfer</code> provides a platform specific mechanism
 * for converting text in URL format represented as a java <code>String</code>
 * to a platform specific representation of the data and vice versa. The string
 * must contain a fully specified url.
 *
 * <p>An example of a java <code>String</code> containing a URL is shown below:</p>
 *
 * <pre><code>
 *     String url = "http://www.eclipse.org";
 * </code></pre>
 *
 * @see Transfer
 * @since 3.4
 */
public class URLTransfer extends ByteArrayTransfer {

    /**
     * Returns the singleton instance of the URLTransfer class.
     *
     * @return the singleton instance of the URLTransfer class
     */
    public static URLTransfer getInstance() {
        return SwtURLTransfer.getInstance();
    }

    /**
     * This implementation of <code>javaToNative</code> converts a URL
     * represented by a java <code>String</code> to a platform specific representation.
     *
     * @param object a java <code>String</code> containing a URL
     * @param transferData an empty <code>TransferData</code> object that will
     *  	be filled in on return with the platform specific format of the data
     *
     * @see Transfer#nativeToJava
     */
    public void javaToNative(Object object, TransferData transferData) {
        getImpl().javaToNative(object, transferData);
    }

    /**
     * This implementation of <code>nativeToJava</code> converts a platform
     * specific representation of a URL to a java <code>String</code>.
     *
     * @param transferData the platform specific representation of the data to be converted
     * @return a java <code>String</code> containing a URL if the conversion was successful;
     * 		otherwise null
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

    protected URLTransfer(IURLTransfer impl) {
        super(impl);
    }

    public static URLTransfer createApi(IURLTransfer impl) {
        return new URLTransfer(impl);
    }

    public IURLTransfer getImpl() {
        return (IURLTransfer) super.getImpl();
    }
}
