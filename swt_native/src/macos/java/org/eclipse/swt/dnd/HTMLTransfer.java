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
package org.eclipse.swt.dnd;

import org.eclipse.swt.internal.cocoa.*;

/**
 * The class <code>HTMLTransfer</code> provides a platform specific mechanism
 * for converting text in HTML format represented as a java <code>String</code>
 * to a platform specific representation of the data and vice versa.
 *
 * <p>An example of a java <code>String</code> containing HTML text is shown
 * below:</p>
 *
 * <pre><code>
 *     String htmlData = "&lt;p&gt;This is a paragraph of text.&lt;/p&gt;";
 * </code></pre>
 *
 * @see Transfer
 */
public class HTMLTransfer extends ByteArrayTransfer {

    /**
     * Returns the singleton instance of the HTMLTransfer class.
     *
     * @return the singleton instance of the HTMLTransfer class
     */
    public static HTMLTransfer getInstance() {
        return nat.org.eclipse.swt.dnd.HTMLTransfer.getInstance().getApi();
    }

    /**
     * This implementation of <code>javaToNative</code> converts HTML-formatted text
     * represented by a java <code>String</code> to a platform specific representation.
     *
     * @param object a java <code>String</code> containing HTML text
     * @param transferData an empty <code>TransferData</code> object that will
     *  	be filled in on return with the platform specific format of the data
     *
     * @see Transfer#nativeToJava
     */
    public void javaToNative(Object object, TransferData transferData) {
        getDelegate().javaToNative(object, transferData.getDelegate());
    }

    /**
     * This implementation of <code>nativeToJava</code> converts a platform specific
     * representation of HTML text to a java <code>String</code>.
     *
     * @param transferData the platform specific representation of the data to be converted
     * @return a java <code>String</code> containing HTML text if the conversion was successful;
     * 		otherwise null
     *
     * @see Transfer#javaToNative
     */
    public Object nativeToJava(TransferData transferData) {
        return getDelegate().nativeToJava(transferData.getDelegate());
    }

    protected int[] getTypeIds() {
        return getDelegate().getTypeIds();
    }

    protected String[] getTypeNames() {
        return getDelegate().getTypeNames();
    }

    protected boolean validate(Object object) {
        return getDelegate().validate(object);
    }

    protected HTMLTransfer(IHTMLTransfer delegate) {
        super(delegate);
    }

    public IHTMLTransfer getDelegate() {
        return (IHTMLTransfer) super.getDelegate();
    }
}
