/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2008 IBM Corporation and others.
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

public class DROPFILES {

    // Offset of the file list from the beginning of this structure, in bytes.
    public int pFiles;

    //	POINT pt; // Drop point. The coordinates depend on fNC.
    /**
     * @field accessor=pt.x
     */
    public int pt_x;

    /**
     * @field accessor=pt.y
     */
    public int pt_y;

    // Nonclient area flag. If this member is TRUE, pt specifies the screen
    public int fNC;

    // coordinates of a point in a window's nonclient area. If it is FALSE,
    // pt specifies the client coordinates of a point in the client area.
    // Value that indicates whether the file contains ANSI or Unicode
    public int fWide;

    // characters. If it is zero, it contains ANSI characters. Otherwise, it
    // contains Unicode characters.
    public static final int sizeof = OS.DROPFILES_sizeof();
}
