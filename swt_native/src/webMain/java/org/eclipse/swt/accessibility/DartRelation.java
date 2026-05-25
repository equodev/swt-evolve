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

import dev.equo.swt.*;

class DartRelation implements IRelation {

    Accessible accessible;

    Accessible[] targets;

    int type;

    DartRelation(Accessible accessible, int type, Relation api) {
        setApi(api);
        this.accessible = accessible;
        this.type = type;
        this.targets = new Accessible[0];
    }

    void addTarget(Accessible target) {
        if (containsTarget(target))
            return;
        Accessible[] newTargets = new Accessible[targets.length + 1];
        System.arraycopy(targets, 0, newTargets, 0, targets.length);
        newTargets[targets.length] = target;
        targets = newTargets;
    }

    boolean containsTarget(Accessible target) {
        for (int i = 0; i < targets.length; i++) {
            if (targets[i] == target)
                return true;
        }
        return false;
    }

    void removeTarget(Accessible target) {
        if (!containsTarget(target))
            return;
        Accessible[] newTargets = new Accessible[targets.length - 1];
        int j = 0;
        for (int i = 0; i < targets.length; i++) {
            if (targets[i] != target) {
                newTargets[j++] = targets[i];
            }
        }
        targets = newTargets;
    }

    public Accessible _accessible() {
        return accessible;
    }

    public Accessible[] _targets() {
        return targets;
    }

    public int _type() {
        return type;
    }

    public Relation getApi() {
        if (api == null)
            api = Relation.createApi(this);
        return (Relation) api;
    }

    protected Relation api;

    public void setApi(Relation api) {
        this.api = api;
        if (api != null)
            api.impl = this;
    }
}
