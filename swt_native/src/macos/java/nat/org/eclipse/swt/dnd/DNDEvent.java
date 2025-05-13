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
package nat.org.eclipse.swt.dnd;

import nat.org.eclipse.swt.graphics.*;
import nat.org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.dnd.IDNDEvent;

class DNDEvent extends Event implements IDNDEvent {

    public TransferData dataType;

    public TransferData[] dataTypes;

    public int operations;

    public int feedback;

    public Image image;

    public int offsetX;

    public int offsetY;

    public org.eclipse.swt.dnd.DNDEvent getApi() {
        return (org.eclipse.swt.dnd.DNDEvent) api;
    }

    org.eclipse.swt.dnd.DNDEvent api;

    public void setApi(org.eclipse.swt.dnd.DNDEvent api) {
        this.api = api;
    }
}
