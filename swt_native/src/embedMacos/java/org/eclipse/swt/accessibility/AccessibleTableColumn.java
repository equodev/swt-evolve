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
 * Instances of this class represent one column in a table. Cocoa accessibility expects
 * columns to report their location, number of rows, and elements in those rows.
 *
 * @see TableAccessibleDelegate
 */
class AccessibleTableColumn extends Accessible {

    public AccessibleTableColumn(Accessible accessible, int childID) {
        this((IAccessibleTableColumn) null);
        setImpl(new SwtAccessibleTableColumn(accessible, childID, this));
    }

    protected AccessibleTableColumn(IAccessibleTableColumn impl) {
        super(impl);
    }

    static AccessibleTableColumn createApi(IAccessibleTableColumn impl) {
        return new AccessibleTableColumn(impl);
    }

    public IAccessibleTableColumn getImpl() {
        return (IAccessibleTableColumn) super.getImpl();
    }
}
