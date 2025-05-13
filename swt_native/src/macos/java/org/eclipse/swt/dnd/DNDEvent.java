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
package org.eclipse.swt.dnd;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class DNDEvent extends Event {

    public TransferData dataType;

    public TransferData[] dataTypes;

    public int operations;

    public int feedback;

    public Image image;

    public int offsetX;

    public int offsetY;

    IDNDEvent delegate;

    protected DNDEvent(IDNDEvent delegate) {
        this.delegate = delegate;
        delegate.setApi(this);
    }

    public IDNDEvent getDelegate() {
        return delegate;
    }
}
