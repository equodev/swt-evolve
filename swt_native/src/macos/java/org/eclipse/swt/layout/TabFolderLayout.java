/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2005 IBM Corporation and others.
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
package org.eclipse.swt.layout;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * This layout controls the position and size of the children of a tab folder.
 * In addition this layout can be used for any component that wants
 * TabFolder-Like behaviour (e.g. only showing all component at the full size
 * of the composite stacked onto each other)
 *
 * @since 3.128
 */
public class TabFolderLayout extends Layout {

    protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        return getImpl().computeSize(composite, wHint, hHint, flushCache);
    }

    protected void layout(Composite composite, boolean flushCache) {
        getImpl().layout(composite, flushCache);
    }

    public TabFolderLayout() {
        this(new SwtTabFolderLayout());
    }

    protected TabFolderLayout(ITabFolderLayout impl) {
        super(impl);
    }

    public static TabFolderLayout createApi(ITabFolderLayout impl) {
        return new TabFolderLayout(impl);
    }

    public ITabFolderLayout getImpl() {
        return (ITabFolderLayout) super.getImpl();
    }
}
