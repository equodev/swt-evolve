package org.eclipse.swt.dnd;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ITreeDragSourceEffect extends IDragSourceEffect, ImplTreeDragSourceEffect {

    /**
     * This implementation of <code>dragFinished</code> disposes the image
     * that was created in <code>TreeDragSourceEffect.dragStart</code>.
     *
     * Subclasses that override this method should call <code>super.dragFinished(event)</code>
     * to dispose the image in the default implementation.
     *
     * @param event the information associated with the drag finished event
     */
    void dragFinished(DragSourceEvent event);

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
    void dragStart(DragSourceEvent event);

    TreeDragSourceEffect getApi();
}
