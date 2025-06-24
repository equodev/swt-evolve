package org.eclipse.swt.dnd;

public interface IDropTargetAdapter {

    /**
     * This implementation of <code>dragEnter</code> permits the default
     * operation defined in <code>event.detail</code>to be performed on the current data type
     * defined in <code>event.currentDataType</code>.
     * For additional information see <code>DropTargetListener.dragEnter</code>.
     *
     * @param event the information associated with the drag enter event
     */
    void dragEnter(DropTargetEvent event);

    /**
     * This implementation of <code>dragLeave</code> does nothing.
     * For additional information see <code>DropTargetListener.dragOperationChanged</code>.
     *
     * @param event the information associated with the drag leave event
     */
    void dragLeave(DropTargetEvent event);

    /**
     * This implementation of <code>dragOperationChanged</code> permits the default
     * operation defined in <code>event.detail</code>to be performed on the current data type
     * defined in <code>event.currentDataType</code>.
     * For additional information see <code>DropTargetListener.dragOperationChanged</code>.
     *
     * @param event the information associated with the drag operation changed event
     */
    void dragOperationChanged(DropTargetEvent event);

    /**
     * This implementation of <code>dragOver</code> permits the default
     * operation defined in <code>event.detail</code>to be performed on the current data type
     * defined in <code>event.currentDataType</code>.
     * For additional information see <code>DropTargetListener.dragOver</code>.
     *
     * @param event the information associated with the drag over event
     */
    void dragOver(DropTargetEvent event);

    /**
     * This implementation of <code>drop</code> does nothing.
     * For additional information see <code>DropTargetListener.drop</code>.
     *
     * @param event the information associated with the drop event
     */
    void drop(DropTargetEvent event);

    /**
     * This implementation of <code>dropAccept</code> permits the default
     * operation defined in <code>event.detail</code>to be performed on the current data type
     * defined in <code>event.currentDataType</code>.
     * For additional information see <code>DropTargetListener.dropAccept</code>.
     *
     * @param event the information associated with the drop accept event
     */
    void dropAccept(DropTargetEvent event);

    DropTargetAdapter getApi();

    void setApi(DropTargetAdapter api);
}
