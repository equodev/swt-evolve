package org.eclipse.swt.dnd;

import java.lang.reflect.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.widgets.*;

public interface IDragSource extends IWidget, ImplDragSource {

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when a drag and drop operation is in progress, by sending
     * it one of the messages defined in the <code>DragSourceListener</code>
     * interface.
     *
     * <ul>
     * <li><code>dragStart</code> is called when the user has begun the actions required to drag the widget.
     * This event gives the application the chance to decide if a drag should be started.
     * <li><code>dragSetData</code> is called when the data is required from the drag source.
     * <li><code>dragFinished</code> is called when the drop has successfully completed (mouse up
     * over a valid target) or has been terminated (such as hitting the ESC key). Perform cleanup
     * such as removing data from the source side on a successful move operation.
     * </ul>
     *
     * @param listener the listener which should be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see DragSourceListener
     * @see #getDragListeners
     * @see #removeDragListener
     * @see DragSourceEvent
     */
    void addDragListener(DragSourceListener listener);

    void checkSubclass();

    /**
     * Returns the Control which is registered for this DragSource.  This is the control that the
     * user clicks in to initiate dragging.
     *
     * @return the Control which is registered for this DragSource
     */
    Control getControl();

    /**
     * Returns an array of listeners who will be notified when a drag and drop
     * operation is in progress, by sending it one of the messages defined in
     * the <code>DragSourceListener</code> interface.
     *
     * @return the listeners who will be notified when a drag and drop
     * operation is in progress
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see DragSourceListener
     * @see #addDragListener
     * @see #removeDragListener
     * @see DragSourceEvent
     *
     * @since 3.4
     */
    DragSourceListener[] getDragListeners();

    /**
     * Returns the drag effect that is registered for this DragSource.  This drag
     * effect will be used during a drag and drop operation.
     *
     * @return the drag effect that is registered for this DragSource
     *
     * @since 3.3
     */
    DragSourceEffect getDragSourceEffect();

    /**
     * Returns the list of data types that can be transferred by this DragSource.
     *
     * @return the list of data types that can be transferred by this DragSource
     */
    Transfer[] getTransfer();

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when a drag and drop operation is in progress.
     *
     * @param listener the listener which should no longer be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see DragSourceListener
     * @see #addDragListener
     * @see #getDragListeners
     */
    void removeDragListener(DragSourceListener listener);

    /**
     * Specifies the drag effect for this DragSource.  This drag effect will be
     * used during a drag and drop operation.
     *
     * @param effect the drag effect that is registered for this DragSource
     *
     * @since 3.3
     */
    void setDragSourceEffect(DragSourceEffect effect);

    /**
     * Specifies the list of data types that can be transferred by this DragSource.
     * The application must be able to provide data to match each of these types when
     * a successful drop has occurred.
     *
     * @param transferAgents a list of Transfer objects which define the types of data that can be
     * dragged from this source
     */
    void setTransfer(Transfer... transferAgents);

    DragSource getApi();
}
