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
 * *****************************************************************************
 */
package org.eclipse.swt.dnd;

import org.eclipse.swt.internal.ole.win32.*;
import org.eclipse.swt.internal.win32.*;

/**
 * The class <code>RTFTransfer</code> provides a platform specific mechanism
 * for converting text in RTF format represented as a java <code>String</code>
 * to a platform specific representation of the data and vice versa.
 *
 * <p>An example of a java <code>String</code> containing RTF text is shown
 * below:</p>
 *
 * <pre><code>
 *     String rtfData = "{\\rtf1{\\colortbl;\\red255\\green0\\blue0;}\\uc1\\b\\i Hello World}";
 * </code></pre>
 *
 * @see Transfer
 */
public class SwtRTFTransfer extends SwtByteArrayTransfer implements IRTFTransfer {

    private static RTFTransfer _instance = new RTFTransfer();

    //$NON-NLS-1$
    private static final String CF_RTF = "Rich Text Format";

    private static final int CF_RTFID = registerType(CF_RTF);

    SwtRTFTransfer(RTFTransfer api) {
        super(api);
    }

    /**
     * Returns the singleton instance of the RTFTransfer class.
     *
     * @return the singleton instance of the RTFTransfer class
     */
    public static RTFTransfer getInstance() {
        return _instance;
    }

    /**
     * This implementation of <code>javaToNative</code> converts RTF-formatted text
     * represented by a java <code>String</code> to a platform specific representation.
     *
     * @param object a java <code>String</code> containing RTF text
     * @param transferData an empty <code>TransferData</code> object that will
     *  	be filled in on return with the platform specific format of the data
     *
     * @see Transfer#nativeToJava
     */
    @Override
    public void javaToNative(Object object, TransferData transferData) {
        if (!checkRTF(object) || !isSupportedType(transferData)) {
            DND.error(DND.ERROR_INVALID_DATA);
        }
        // CF_RTF is stored as a null terminated byte array
        String string = (String) object;
        int count = string.length();
        char[] chars = new char[count + 1];
        string.getChars(0, count, chars, 0);
        int codePage = OS.GetACP();
        int cchMultiByte = OS.WideCharToMultiByte(codePage, 0, chars, -1, null, 0, null, null);
        if (cchMultiByte == 0) {
            transferData.stgmedium = new STGMEDIUM();
            transferData.result = COM.DV_E_STGMEDIUM;
            return;
        }
        long lpMultiByteStr = OS.GlobalAlloc(COM.GMEM_FIXED | COM.GMEM_ZEROINIT, cchMultiByte);
        OS.WideCharToMultiByte(codePage, 0, chars, -1, lpMultiByteStr, cchMultiByte, null, null);
        transferData.stgmedium = new STGMEDIUM();
        transferData.stgmedium.tymed = COM.TYMED_HGLOBAL;
        transferData.stgmedium.unionField = lpMultiByteStr;
        transferData.stgmedium.pUnkForRelease = 0;
        transferData.result = COM.S_OK;
        return;
    }

    /**
     * This implementation of <code>nativeToJava</code> converts a platform specific
     * representation of RTF text to a java <code>String</code>.
     *
     * @param transferData the platform specific representation of the data to be converted
     * @return a java <code>String</code> containing RTF text if the conversion was successful;
     * 		otherwise null
     *
     * @see Transfer#javaToNative
     */
    @Override
    public Object nativeToJava(TransferData transferData) {
        if (!isSupportedType(transferData) || transferData.pIDataObject == 0)
            return null;
        IDataObject data = new IDataObject(transferData.pIDataObject);
        data.AddRef();
        STGMEDIUM stgmedium = new STGMEDIUM();
        FORMATETC formatetc = transferData.formatetc;
        stgmedium.tymed = COM.TYMED_HGLOBAL;
        transferData.result = getData(data, formatetc, stgmedium);
        data.Release();
        if (transferData.result != COM.S_OK)
            return null;
        long hMem = stgmedium.unionField;
        try {
            long lpMultiByteStr = OS.GlobalLock(hMem);
            if (lpMultiByteStr == 0)
                return null;
            try {
                int codePage = OS.GetACP();
                int cchWideChar = OS.MultiByteToWideChar(codePage, OS.MB_PRECOMPOSED, lpMultiByteStr, -1, null, 0);
                if (cchWideChar == 0)
                    return null;
                char[] lpWideCharStr = new char[cchWideChar - 1];
                OS.MultiByteToWideChar(codePage, OS.MB_PRECOMPOSED, lpMultiByteStr, -1, lpWideCharStr, lpWideCharStr.length);
                return new String(lpWideCharStr);
            } finally {
                OS.GlobalUnlock(hMem);
            }
        } finally {
            OS.GlobalFree(hMem);
        }
    }

    @Override
    public int[] getTypeIds() {
        return new int[] { CF_RTFID };
    }

    @Override
    public String[] getTypeNames() {
        return new String[] { CF_RTF };
    }

    boolean checkRTF(Object object) {
        return (object instanceof String && ((String) object).length() > 0);
    }

    @Override
    public boolean validate(Object object) {
        return checkRTF(object);
    }

    public RTFTransfer getApi() {
        if (api == null)
            api = RTFTransfer.createApi(this);
        return (RTFTransfer) api;
    }
}
