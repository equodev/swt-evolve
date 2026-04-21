/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
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

/**
 * This class provides the layout for SashForm
 *
 * @see SashForm
 */
class SashFormLayout extends Layout {

    protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        return getImpl().computeSize(composite, wHint, hHint, flushCache);
    }

    protected boolean flushCache(Control control) {
        return getImpl().flushCache(control);
    }

    protected void layout(Composite composite, boolean flushCache) {
        getImpl().layout(composite, flushCache);
    }

    public SashFormLayout() {
        this((ISashFormLayout) null);
        setImpl(new DartSashFormLayout(this));
    }

    protected SashFormLayout(ISashFormLayout impl) {
        super(impl);
    }

    static SashFormLayout createApi(ISashFormLayout impl) {
        return new SashFormLayout(impl);
    }

    public ISashFormLayout getImpl() {
        return (ISashFormLayout) super.getImpl();
    }
}
