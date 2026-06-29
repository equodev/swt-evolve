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
package org.eclipse.swt.internal.win32;

public class PRINTDLG {

    // DWORD
    public int lStructSize;

    /**
     * @field cast=(HWND)
     */
    // HWND
    public long hwndOwner;

    /**
     * @field cast=(HGLOBAL)
     */
    // HGLOBAL
    public long hDevMode;

    /**
     * @field cast=(HGLOBAL)
     */
    // HGLOBAL
    public long hDevNames;

    /**
     * @field cast=(HDC)
     */
    // HDC
    public long hDC;

    // DWORD
    public int Flags;

    // WORD
    public short nFromPage;

    // WORD
    public short nToPage;

    // WORD
    public short nMinPage;

    // WORD
    public short nMaxPage;

    // WORD
    public short nCopies;

    /**
     * @field cast=(HINSTANCE)
     */
    // HINSTANCE
    public long hInstance;

    // LPARAM
    public long lCustData;

    /**
     * @field cast=(LPPRINTHOOKPROC)
     */
    // LPPRINTHOOKPROC
    public long lpfnPrintHook;

    /**
     * @field cast=(LPPRINTHOOKPROC)
     */
    // LPSETUPHOOKPROC
    public long lpfnSetupHook;

    /**
     * @field cast=(LPCTSTR)
     */
    // LPCTSTR
    public long lpPrintTemplateName;

    /**
     * @field cast=(LPCTSTR)
     */
    // LPCTSTR
    public long lpSetupTemplateName;

    /**
     * @field cast=(HGLOBAL)
     */
    // HGLOBAL
    public long hPrintTemplate;

    /**
     * @field cast=(HGLOBAL)
     */
    // HGLOBAL
    public long hSetupTemplate;

    public static final int sizeof = OS.PRINTDLG_sizeof();
}
