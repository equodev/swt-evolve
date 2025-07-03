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

public class DOCINFO {

    public int cbSize;

    /**
     * @field cast=(LPCTSTR)
     */
    // LPCTSTR
    public long lpszDocName;

    /**
     * @field cast=(LPCTSTR)
     */
    // LPCTSTR
    public long lpszOutput;

    /**
     * @field cast=(LPCTSTR)
     */
    // LPCTSTR
    public long lpszDatatype;

    // DWORD
    public int fwType;

    public static final int sizeof = OS.DOCINFO_sizeof();
}
