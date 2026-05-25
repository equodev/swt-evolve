/**
 * ****************************************************************************
 *  Copyright (c) 2009, 2010 IBM Corporation and others.
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

class Relation {

    Relation(Accessible accessible, int type) {
        this((IRelation) null);
        setImpl(new DartRelation(accessible, type, this));
    }

    protected IRelation impl;

    protected Relation(IRelation impl) {
        if (impl != null)
            impl.setApi(this);
    }

    static Relation createApi(IRelation impl) {
        return new Relation(impl);
    }

    public IRelation getImpl() {
        return impl;
    }

    protected Relation setImpl(IRelation impl) {
        this.impl = impl;
        return this;
    }
}
