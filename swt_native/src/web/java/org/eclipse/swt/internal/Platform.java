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
package org.eclipse.swt.internal;

public class Platform {

    //$NON-NLS-1$
    public static final String PLATFORM = "web";

    public static boolean isLoadable() {
        // Pure-web build has no native libraries to load.
        return true;
    }

    public static void exitIfNotLoadable() {
        // Pure-web build has no native libraries; nothing to check.
    }
}
