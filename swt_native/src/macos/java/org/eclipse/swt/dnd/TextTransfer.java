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
 * The class <code>TextTransfer</code> provides a platform specific mechanism
 * for converting plain text represented as a java <code>String</code>
 * to a platform specific representation of the data and vice versa.
 *
 * <p>An example of a java <code>String</code> containing plain text is shown
 * below:</p>
 *
 * <pre><code>
 *     String textData = "Hello World";
 * </code></pre>
 *
 * <p>Note the <code>TextTransfer</code> does not change the content of the text
 * data. For a better integration with the platform, the application should convert
 * the line delimiters used in the text data to the standard line delimiter used by the
 * platform.
 * </p>
 *
 * @see Transfer
 */
public class TextTransfer extends ByteArrayTransfer {

    /**
     * Returns the singleton instance of the TextTransfer class.
     *
     * @return the singleton instance of the TextTransfer class
     */
    public static TextTransfer getInstance() {
        return nat.org.eclipse.swt.dnd.TextTransfer.getInstance().getApi();
    }

    /**
     * This implementation of <code>javaToNative</code> converts plain text
     * represented by a java <code>String</code> to a platform specific representation.
     *
     * @param object a java <code>String</code> containing text
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
     * representation of plain text to a java <code>String</code>.
     *
     * @param transferData the platform specific representation of the data to be converted
     * @return a java <code>String</code> containing text if the conversion was successful; otherwise null
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

    protected TextTransfer(ITextTransfer delegate) {
        super(delegate);
    }

    public ITextTransfer getDelegate() {
        return (ITextTransfer) super.getDelegate();
    }
}
