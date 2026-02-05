package org.eclipse.swt.dnd;

public interface IDragSourceAdapter extends ImplDragSourceAdapter {

    /**
     * This implementation of <code>dragStart</code> permits the drag operation to start.
     * For additional information see <code>DragSourceListener.dragStart</code>.
     *
     * @param event the information associated with the drag start event
     */
    void dragStart(DragSourceEvent event);

    /**
     * This implementation of <code>dragFinished</code> does nothing.
     * For additional information see <code>DragSourceListener.dragFinished</code>.
     *
     * @param event the information associated with the drag finished event
     */
    void dragFinished(DragSourceEvent event);

    /**
     * This implementation of <code>dragSetData</code> does nothing.
     * For additional information see <code>DragSourceListener.dragSetData</code>.
     *
     * @param event the information associated with the drag set data event
     */
    void dragSetData(DragSourceEvent event);

    DragSourceAdapter getApi();

    void setApi(DragSourceAdapter api);
}
