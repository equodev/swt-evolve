/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2011 IBM Corporation and others.
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
package org.eclipse.swt.widgets;

import org.eclipse.swt.graphics.*;

/**
 * A layout controls the position and size
 * of the children of a composite widget.
 * This class is the abstract base class for
 * layouts.
 *
 * @see Composite#setLayout(Layout)
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public abstract class Layout {

    public Layout() {
    }

    ILayout delegate;

    protected Layout(ILayout delegate) {
        this.delegate = delegate;
        delegate.setApi(this);
    }

    public ILayout getDelegate() {
        return delegate;
    }
}
