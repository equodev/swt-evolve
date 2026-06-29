package org.eclipse.swt.dnd;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.widgets.*;

public interface IDropTarget extends IWidget, ImplDropTarget {

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when a drag and drop operation is in progress, by sending
     * it one of the messages defined in the <code>DropTargetListener</code>
     * interface.
     *
     * <ul>
     * <li><code>dragEnter</code> is called when the cursor has entered the drop target boundaries
     * <li><code>dragLeave</code> is called when the cursor has left the drop target boundaries and just before
     * the drop occurs or is cancelled.
     * <li><code>dragOperationChanged</code> is called when the operation being performed has changed
     * (usually due to the user changing the selected modifier key(s) while dragging)
     * <li><code>dragOver</code> is called when the cursor is moving over the drop target
     * <li><code>dropAccept</code> is called just before the drop is performed.  The drop target is given
     * the chance to change the nature of the drop or veto the drop by setting the <code>event.detail</code> field
     * <li><code>drop</code> is called when the data is being dropped
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
     * @see DropTargetListener
     * @see #getDropListeners
     * @see #removeDropListener
     * @see DropTargetEvent
     */
    void addDropListener(DropTargetListener listener);

    void checkSubclass();

    /**
     * Returns the Control which is registered for this DropTarget.  This is the control over which the
     * user positions the cursor to drop the data.
     *
     * @return the Control which is registered for this DropTarget
     */
    Control getControl();

    /**
     * Returns an array of listeners who will be notified when a drag and drop
     * operation is in progress, by sending it one of the messages defined in
     * the <code>DropTargetListener</code> interface.
     *
     * @return the listeners who will be notified when a drag and drop
     * operation is in progress
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see DropTargetListener
     * @see #addDropListener
     * @see #removeDropListener
     * @see DropTargetEvent
     *
     * @since 3.4
     */
    DropTargetListener[] getDropListeners();

    /**
     * Returns the drop effect for this DropTarget.  This drop effect will be
     * used during a drag and drop to display the drag under effect on the
     * target widget.
     *
     * @return the drop effect that is registered for this DropTarget
     *
     * @since 3.3
     */
    DropTargetEffect getDropTargetEffect();

    /**
     * Returns a list of the data types that can be transferred to this DropTarget.
     *
     * @return a list of the data types that can be transferred to this DropTarget
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
     * @see DropTargetListener
     * @see #addDropListener
     * @see #getDropListeners
     */
    void removeDropListener(DropTargetListener listener);

    /**
     * Specifies the drop effect for this DropTarget.  This drop effect will be
     * used during a drag and drop to display the drag under effect on the
     * target widget.
     *
     * @param effect the drop effect that is registered for this DropTarget
     *
     * @since 3.3
     */
    void setDropTargetEffect(DropTargetEffect effect);

    /**
     *  Specifies the data types that can be transferred to this DropTarget.  If data is
     *  being dragged that does not match one of these types, the drop target will be notified of
     *  the drag and drop operation but the currentDataType will be null and the operation
     *  will be DND.NONE.
     *
     *  @param transferAgents a list of Transfer objects which define the types of data that can be
     * 						 dropped on this target
     *
     *  @exception IllegalArgumentException <ul>
     *     <li>ERROR_NULL_ARGUMENT - if transferAgents is null</li>
     *  </ul>
     */
    void setTransfer(Transfer... transferAgents);

    DropTarget getApi();
}
