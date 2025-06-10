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
package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import dev.equo.swt.Config;

/**
 * This class provides the layout for CTabFolder
 *
 * @see CTabFolder
 */
class CTabFolderLayout extends Layout {

    protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        return getImpl().computeSize(composite, wHint, hHint, flushCache);
    }

    protected boolean flushCache(Control control) {
        return getImpl().flushCache(control);
    }

    protected void layout(Composite composite, boolean flushCache) {
        getImpl().layout(composite, flushCache);
    }

    public CTabFolderLayout() {
        this((ICTabFolderLayout) null);
        setImpl(Config.isEquo(CTabFolderLayout.class) ? new DartCTabFolderLayout(this) : new SwtCTabFolderLayout(this));
    }

    protected CTabFolderLayout(ICTabFolderLayout impl) {
        super(impl);
    }

    static CTabFolderLayout createApi(ICTabFolderLayout impl) {
        return new CTabFolderLayout(impl);
    }

    public ICTabFolderLayout getImpl() {
        return (ICTabFolderLayout) super.getImpl();
    }
}
