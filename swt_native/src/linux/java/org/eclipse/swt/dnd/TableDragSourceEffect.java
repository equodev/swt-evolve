/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2018 IBM Corporation and others.
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
import dev.equo.swt.Config;

/**
 * This class provides default implementations to display a source image
 * when a drag is initiated from a <code>Table</code>.
 *
 * <p>Classes that wish to provide their own source image for a <code>Table</code> can
 * extend the <code>TableDragSourceEffect</code> class, override the
 * <code>TableDragSourceEffect.dragStart</code> method and set the field
 * <code>DragSourceEvent.image</code> with their own image.</p>
 *
 * Subclasses that override any methods of this class must call the corresponding
 * <code>super</code> method to get the default drag source effect implementation.
 *
 * @see DragSourceEffect
 * @see DragSourceEvent
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.3
 */
public class TableDragSourceEffect extends DragSourceEffect {

    /**
     * Creates a new <code>TableDragSourceEffect</code> to handle drag effect
     * from the specified <code>Table</code>.
     *
     * @param table the <code>Table</code> that the user clicks on to initiate the drag
     */
    public TableDragSourceEffect(Table table) {
        this((ITableDragSourceEffect) null);
        setImpl(Config.isEquo(TableDragSourceEffect.class) ? new DartTableDragSourceEffect(table, this) : new SwtTableDragSourceEffect(table, this));
    }

    /**
     * This implementation of <code>dragFinished</code> disposes the image
     * that was created in <code>TableDragSourceEffect.dragStart</code>.
     *
     * Subclasses that override this method should call <code>super.dragFinished(event)</code>
     * to dispose the image in the default implementation.
     *
     * @param event the information associated with the drag finished event
     */
    public void dragFinished(DragSourceEvent event) {
        getImpl().dragFinished(event);
    }

    /**
     * This implementation of <code>dragStart</code> will create a default
     * image that will be used during the drag. The image should be disposed
     * when the drag is completed in the <code>TableDragSourceEffect.dragFinished</code>
     * method.
     *
     * Subclasses that override this method should call <code>super.dragStart(event)</code>
     * to use the image from the default implementation.
     *
     * @param event the information associated with the drag start event
     */
    public void dragStart(DragSourceEvent event) {
        getImpl().dragStart(event);
    }

    protected TableDragSourceEffect(ITableDragSourceEffect impl) {
        super(impl);
    }

    static TableDragSourceEffect createApi(ITableDragSourceEffect impl) {
        return new TableDragSourceEffect(impl);
    }

    public ITableDragSourceEffect getImpl() {
        return (ITableDragSourceEffect) super.getImpl();
    }
}
