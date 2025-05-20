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
 * This class is used to describe a table column for objects that have an accessible
 * role of ACC.ROLE_TABLE, but aren't implemented like NSTableViews.
 *
 * Instances of this class represent one row in a table. Cocoa accessibility expects
 * rows to report their location, and assumes the cells of the table are children of the rows.
 *
 * @see TableAccessibleDelegate
 */
class AccessibleTableRow extends Accessible {

    public AccessibleTableRow(Accessible accessible, int childID) {
        this(new SwtAccessibleTableRow(accessible, childID));
    }

    protected AccessibleTableRow(IAccessibleTableRow impl) {
        super(impl);
    }

    public static AccessibleTableRow createApi(IAccessibleTableRow impl) {
        return new AccessibleTableRow(impl);
    }

    public IAccessibleTableRow getImpl() {
        return (IAccessibleTableRow) super.getImpl();
    }
}
