/**
 * ****************************************************************************
 *  Copyright (c) 2010 IBM Corporation and others.
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
package org.eclipse.swt.accessibility;

import org.eclipse.swt.internal.cocoa.*;

/**
 * This class is used to describe a table header for objects that have an accessible
 * role of ACC.ROLE_TABLE, but aren't implemented like NSTableViews. That means they
 * report back their children as a list of cells in row-major order instead of a list of
 * rows with cells as children of those rows. The assumption is that the first 'row'
 * of cells (cell 0 to cell 'column-count - 1') are the column headers of the table.
 *
 * This class works with the parent control to act as the header section of the table,
 * and reports the cells in the header so that screen readers (VoiceOver, mainly) can
 * identify the column of the cell that the VoiceOver cursor is reading.
 */
class AccessibleTableHeader extends Accessible {

    public AccessibleTableHeader(Accessible accessible, int childID) {
        this(new SwtAccessibleTableHeader(accessible, childID));
    }

    protected AccessibleTableHeader(IAccessibleTableHeader impl) {
        super(impl);
    }

    public static AccessibleTableHeader createApi(IAccessibleTableHeader impl) {
        return new AccessibleTableHeader(impl);
    }

    public IAccessibleTableHeader getImpl() {
        return (IAccessibleTableHeader) super.getImpl();
    }
}
