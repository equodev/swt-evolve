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

import dev.equo.swt.*;

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
public class DartHTMLTransfer extends DartByteArrayTransfer implements IHTMLTransfer {

    static HTMLTransfer _instance = new HTMLTransfer();

    static final String HTML = "public.html";

    static final int HTMLID = registerType(HTML);

    DartHTMLTransfer(HTMLTransfer api) {
        super(api);
    }

    /**
     * Returns the singleton instance of the HTMLTransfer class.
     *
     * @return the singleton instance of the HTMLTransfer class
     */
    public static HTMLTransfer getInstance() {
        return _instance;
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
    @Override
    public void javaToNative(Object object, TransferData transferData) {
        if (!checkHTML(object) || !isSupportedType(transferData)) {
            DND.error(DND.ERROR_INVALID_DATA);
        }
        super.javaToNative(((String) object).getBytes(java.nio.charset.StandardCharsets.UTF_8), transferData);
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
    @Override
    public Object nativeToJava(TransferData transferData) {
        byte[] bytes = (byte[]) super.nativeToJava(transferData);
        return bytes == null ? null : new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    @Override
    public int[] getTypeIds() {
        return new int[] { HTMLID };
    }

    @Override
    public String[] getTypeNames() {
        return new String[] { HTML };
    }

    boolean checkHTML(Object object) {
        return (object != null && object instanceof String && ((String) object).length() > 0);
    }

    @Override
    public boolean validate(Object object) {
        return checkHTML(object);
    }

    public HTMLTransfer getApi() {
        if (api == null)
            api = HTMLTransfer.createApi(this);
        return (HTMLTransfer) api;
    }

    public VHTMLTransfer getValue() {
        if (value == null)
            value = new VHTMLTransfer(this);
        return (VHTMLTransfer) value;
    }
}
