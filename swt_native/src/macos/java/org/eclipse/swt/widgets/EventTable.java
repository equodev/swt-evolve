/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2024 IBM Corporation and others.
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

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;

/**
 * Instances of this class implement a simple
 * look up mechanism that maps an event type
 * to a listener.  Multiple listeners for the
 * same event type are supported.
 */
public class EventTable {

    public Listener[] getListeners(int eventType) {
        return getDelegate().getListeners(eventType);
    }

    public void hook(int eventType, Listener listener) {
        getDelegate().hook(eventType, listener);
    }

    public boolean hooks(int eventType) {
        return getDelegate().hooks(eventType);
    }

    public void sendEvent(Event event) {
        getDelegate().sendEvent(event);
    }

    public int size() {
        return getDelegate().size();
    }

    public void unhook(int eventType, Listener listener) {
        getDelegate().unhook(eventType, listener);
    }

    public void unhook(int eventType, EventListener listener) {
        getDelegate().unhook(eventType, listener);
    }

    IEventTable delegate;

    EventTable(IEventTable delegate) {
        this.delegate = delegate;
        delegate.setApi(this);
    }

    IEventTable getDelegate() {
        return (IEventTable) delegate;
    }
}
