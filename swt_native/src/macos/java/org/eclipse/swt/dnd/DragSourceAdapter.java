/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2016 IBM Corporation and others.
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

/**
 * This adapter class provides default implementations for the
 * methods described by the <code>DragSourceListener</code> interface.
 *
 * <p>Classes that wish to deal with <code>DragSourceEvent</code>s can
 * extend this class and override only the methods which they are
 * interested in.</p>
 *
 * @see DragSourceListener
 * @see DragSourceEvent
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public class DragSourceAdapter implements DragSourceListener {

    /**
     * This implementation of <code>dragStart</code> permits the drag operation to start.
     * For additional information see <code>DragSourceListener.dragStart</code>.
     *
     * @param event the information associated with the drag start event
     */
    public void dragStart(DragSourceEvent event) {
        getDelegate().dragStart(event);
    }

    /**
     * This implementation of <code>dragFinished</code> does nothing.
     * For additional information see <code>DragSourceListener.dragFinished</code>.
     *
     * @param event the information associated with the drag finished event
     */
    public void dragFinished(DragSourceEvent event) {
        getDelegate().dragFinished(event);
    }

    /**
     * This implementation of <code>dragSetData</code> does nothing.
     * For additional information see <code>DragSourceListener.dragSetData</code>.
     *
     * @param event the information associated with the drag set data event
     */
    public void dragSetData(DragSourceEvent event) {
        getDelegate().dragSetData(event);
    }

    public DragSourceAdapter() {
        this(new nat.org.eclipse.swt.dnd.DragSourceAdapter());
    }

    IDragSourceAdapter delegate;

    protected DragSourceAdapter(IDragSourceAdapter delegate) {
        this.delegate = delegate;
        delegate.setApi(this);
    }

    public static DragSourceAdapter createApi(IDragSourceAdapter delegate) {
        return new DragSourceAdapter(delegate);
    }

    public IDragSourceAdapter getDelegate() {
        return delegate;
    }
}
