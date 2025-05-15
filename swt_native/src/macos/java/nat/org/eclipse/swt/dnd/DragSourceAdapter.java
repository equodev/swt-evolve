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
package nat.org.eclipse.swt.dnd;

import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.IDragSourceAdapter;

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
public class DragSourceAdapter implements DragSourceListener, IDragSourceAdapter {

    /**
     * This implementation of <code>dragStart</code> permits the drag operation to start.
     * For additional information see <code>DragSourceListener.dragStart</code>.
     *
     * @param event the information associated with the drag start event
     */
    @Override
    public void dragStart(DragSourceEvent event) {
    }

    /**
     * This implementation of <code>dragFinished</code> does nothing.
     * For additional information see <code>DragSourceListener.dragFinished</code>.
     *
     * @param event the information associated with the drag finished event
     */
    @Override
    public void dragFinished(DragSourceEvent event) {
    }

    /**
     * This implementation of <code>dragSetData</code> does nothing.
     * For additional information see <code>DragSourceListener.dragSetData</code>.
     *
     * @param event the information associated with the drag set data event
     */
    @Override
    public void dragSetData(DragSourceEvent event) {
    }

    public org.eclipse.swt.dnd.DragSourceAdapter getApi() {
        if (api == null)
            api = org.eclipse.swt.dnd.DragSourceAdapter.createApi(this);
        return (org.eclipse.swt.dnd.DragSourceAdapter) api;
    }

    protected org.eclipse.swt.dnd.DragSourceAdapter api;

    public void setApi(org.eclipse.swt.dnd.DragSourceAdapter api) {
        this.api = api;
    }
}
