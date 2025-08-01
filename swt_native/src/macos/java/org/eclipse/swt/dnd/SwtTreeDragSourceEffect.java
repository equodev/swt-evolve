/**
 * ****************************************************************************
 *  Copyright (c) 2007, 2017 IBM Corporation and others.
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

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.cocoa.*;
import org.eclipse.swt.widgets.*;

/**
 * This class provides default implementations to display a source image
 * when a drag is initiated from a <code>Tree</code>.
 *
 * <p>Classes that wish to provide their own source image for a <code>Tree</code> can
 * extend <code>TreeDragSourceEffect</code> class and override the <code>TreeDragSourceEffect.dragStart</code>
 * method and set the field <code>DragSourceEvent.image</code> with their own image.</p>
 *
 * Subclasses that override any methods of this class must call the corresponding
 * <code>super</code> method to get the default drag under effect implementation.
 *
 * @see DragSourceEffect
 * @see DragSourceEvent
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.3
 */
public class SwtTreeDragSourceEffect extends SwtDragSourceEffect implements ITreeDragSourceEffect {

    Image dragSourceImage = null;

    /**
     * Creates a new <code>TreeDragSourceEffect</code> to handle drag effect
     * from the specified <code>Tree</code>.
     *
     * @param tree the <code>Tree</code> that the user clicks on to initiate the drag
     */
    public SwtTreeDragSourceEffect(Tree tree, TreeDragSourceEffect api) {
        super(tree, api);
    }

    /**
     * This implementation of <code>dragFinished</code> disposes the image
     * that was created in <code>TreeDragSourceEffect.dragStart</code>.
     *
     * Subclasses that override this method should call <code>super.dragFinished(event)</code>
     * to dispose the image in the default implementation.
     *
     * @param event the information associated with the drag finished event
     */
    @Override
    public void dragFinished(DragSourceEvent event) {
        if (dragSourceImage != null)
            dragSourceImage.dispose();
        dragSourceImage = null;
    }

    /**
     * This implementation of <code>dragStart</code> will create a default
     * image that will be used during the drag. The image should be disposed
     * when the drag is completed in the <code>TreeDragSourceEffect.dragFinished</code>
     * method.
     *
     * Subclasses that override this method should call <code>super.dragStart(event)</code>
     * to use the image from the default implementation.
     *
     * @param event the information associated with the drag start event
     */
    @Override
    public void dragStart(DragSourceEvent event) {
        event.image = getDragSourceImage(event);
    }

    Image getDragSourceImage(DragSourceEvent event) {
        if (dragSourceImage != null)
            dragSourceImage.dispose();
        dragSourceImage = null;
        NSPoint point = new NSPoint();
        long ptr = C.malloc(NSPoint.sizeof);
        OS.memmove(ptr, point, NSPoint.sizeof);
        NSEvent nsEvent = NSApplication.sharedApplication().currentEvent();
        NSTableView widget = (NSTableView) control.view;
        NSImage nsImage = widget.dragImageForRowsWithIndexes(widget.selectedRowIndexes(), widget.tableColumns(), nsEvent, ptr);
        OS.memmove(point, ptr, NSPoint.sizeof);
        C.free(ptr);
        //TODO: Image representation wrong???
        Image image = SwtImage.cocoa_new(control.getDisplay(), SWT.BITMAP, nsImage);
        dragSourceImage = image;
        nsImage.retain();
        event.offsetX = (int) point.x;
        event.offsetY = (int) point.y;
        return image;
    }

    public TreeDragSourceEffect getApi() {
        if (api == null)
            api = TreeDragSourceEffect.createApi(this);
        return (TreeDragSourceEffect) api;
    }
}
