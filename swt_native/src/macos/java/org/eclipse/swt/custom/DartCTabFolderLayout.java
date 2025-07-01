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
import dev.equo.swt.*;

/**
 * This class provides the layout for CTabFolder
 *
 * @see CTabFolder
 */
class DartCTabFolderLayout extends DartLayout implements ICTabFolderLayout {

    @Override
    public Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        return Sizes.compute((DartCTabFolder) composite.getImpl());
    }

    @Override
    public boolean flushCache(Control control) {
        return true;
    }

    @Override
    public void layout(Composite composite, boolean flushCache) {
        CTabFolder folder = (CTabFolder) composite;
        // resize content
        if (folder.getImpl()._selectedIndex() != -1) {
            Control control = ((DartCTabItem) folder.getImpl()._items()[folder.getImpl()._selectedIndex()].getImpl()).control;
            if (control != null && !control.isDisposed()) {
                control.setBounds(folder.getClientArea());
            }
        }
    }

    public CTabFolderLayout getApi() {
        if (api == null)
            api = CTabFolderLayout.createApi(this);
        return (CTabFolderLayout) api;
    }

    public DartCTabFolderLayout(CTabFolderLayout api) {
        super(api);
    }
}
