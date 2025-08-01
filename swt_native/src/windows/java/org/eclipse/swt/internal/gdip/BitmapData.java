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
package org.eclipse.swt.internal.gdip;

public class BitmapData {

    public int Width;

    public int Height;

    public int Stride;

    /**
     * @field cast=(PixelFormat)
     */
    public int PixelFormat;

    /**
     * @field cast=(void*)
     */
    public long Scan0;

    /**
     * @field cast=(UINT_PTR)
     */
    public long Reserved;
}
