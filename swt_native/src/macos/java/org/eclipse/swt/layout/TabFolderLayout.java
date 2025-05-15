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
        return getDelegate().computeSize(composite.getDelegate(), wHint, hHint, flushCache).getApi();
    }

    protected void layout(Composite composite, boolean flushCache) {
        getDelegate().layout(composite.getDelegate(), flushCache);
    }

    public TabFolderLayout() {
        this(new nat.org.eclipse.swt.layout.TabFolderLayout());
    }

    protected TabFolderLayout(ITabFolderLayout delegate) {
        super(delegate);
    }

    public static TabFolderLayout createApi(ITabFolderLayout delegate) {
        return new TabFolderLayout(delegate);
    }

    public ITabFolderLayout getDelegate() {
        return (ITabFolderLayout) super.getDelegate();
    }
}
