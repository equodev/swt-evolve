/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2007 IBM Corporation and others.
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
package org.eclipse.swt.internal.ole.win32;

public class VARIANT {

    public short vt;

    public short wReserved1;

    public short wReserved2;

    public short wReserved3;

    public int lVal;

    public static final int sizeof = COM.VARIANT_sizeof();
}
