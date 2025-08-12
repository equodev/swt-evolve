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

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.gtk.*;
import org.eclipse.swt.internal.gtk3.*;
import org.eclipse.swt.internal.gtk4.*;
import org.eclipse.swt.widgets.*;

/**
 *  Class <code>DropTarget</code> defines the target object for a drag and drop transfer.
 *
 *  <p>IMPORTANT: This class is <em>not</em> intended to be subclassed.</p>
 *
 *  <p>This class identifies the <code>Control</code> over which the user must position the cursor
 *  in order to drop the data being transferred.  It also specifies what data types can be dropped on
 *  this control and what operations can be performed.  You may have several DropTragets in an
 *  application but there can only be a one to one mapping between a <code>Control</code> and a <code>DropTarget</code>.
 *  The DropTarget can receive data from within the same application or from other applications
 *  (such as text dragged from a text editor like Word).</p>
 *
 *  <pre><code>
 * 	int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
 * 	Transfer[] types = new Transfer[] {TextTransfer.getInstance()};
 * 	DropTarget target = new DropTarget(label, operations);
 * 	target.setTransfer(types);
 *  </code></pre>
 *
 *  <p>The application is notified of data being dragged over this control and of when a drop occurs by
 *  implementing the interface <code>DropTargetListener</code> which uses the class
 *  <code>DropTargetEvent</code>.  The application can modify the type of drag being performed
 *  on this Control at any stage of the drag by modifying the <code>event.detail</code> field or the
 *  <code>event.currentDataType</code> field.  When the data is dropped, it is the responsibility of
 *  the application to copy this data for its own purposes.
 *
 *  <pre><code>
 * 	target.addDropListener (new DropTargetListener() {
 * 		public void dragEnter(DropTargetEvent event) {};
 * 		public void dragOver(DropTargetEvent event) {};
 * 		public void dragLeave(DropTargetEvent event) {};
 * 		public void dragOperationChanged(DropTargetEvent event) {};
 * 		public void dropAccept(DropTargetEvent event) {}
 * 		public void drop(DropTargetEvent event) {
 * 			// A drop has occurred, copy over the data
 * 			if (event.data == null) { // no data to copy, indicate failure in event.detail
 * 				event.detail = DND.DROP_NONE;
 * 				return;
 * 			}
 * 			label.setText ((String) event.data); // data copied to label text
 * 		}
 *  	});
 *  </code></pre>
 *
 *  <dl>
 * 	<dt><b>Styles</b></dt> <dd>DND.DROP_NONE, DND.DROP_COPY, DND.DROP_MOVE, DND.DROP_LINK</dd>
 * 	<dt><b>Events</b></dt> <dd>DND.DragEnter, DND.DragLeave, DND.DragOver, DND.DragOperationChanged,
 *                              DND.DropAccept, DND.Drop </dd>
 *  </dl>
 *
 *  @see <a href="http://www.eclipse.org/swt/snippets/#dnd">Drag and Drop snippets</a>
 *  @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: DNDExample</a>
 *  @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *  @noextend This class is not intended to be subclassed by clients.
 */
public class DropTarget extends Widget {

    /**
     *  Creates a new <code>DropTarget</code> to allow data to be dropped on the specified
     *  <code>Control</code>.
     *  Creating an instance of a DropTarget may cause system resources to be allocated
     *  depending on the platform.  It is therefore mandatory that the DropTarget instance
     *  be disposed when no longer required.
     *
     *  @param control the <code>Control</code> over which the user positions the cursor to drop the data
     *  @param style the bitwise OR'ing of allowed operations; this may be a combination of any of
     * 		   DND.DROP_NONE, DND.DROP_COPY, DND.DROP_MOVE, DND.DROP_LINK
     *
     *  @exception SWTException <ul>
     *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *     <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     *  </ul>
     *  @exception SWTError <ul>
     *     <li>ERROR_CANNOT_INIT_DROP - unable to initiate drop target; this will occur if more than one
     *         drop target is created for a control or if the operating system will not allow the creation
     *         of the drop target</li>
     *  </ul>
     *
     *  <p>NOTE: ERROR_CANNOT_INIT_DROP should be an SWTException, since it is a
     *  recoverable error, but can not be changed due to backward compatibility.</p>
     *
     *  @see Widget#dispose
     *  @see DropTarget#checkSubclass
     *  @see DND#DROP_NONE
     *  @see DND#DROP_COPY
     *  @see DND#DROP_MOVE
     *  @see DND#DROP_LINK
     */
    public DropTarget(Control control, int style) {
        this((IDropTarget) null);
        setImpl(new SwtDropTarget(control, style, this));
    }

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
    public void addDropListener(DropTargetListener listener) {
        getImpl().addDropListener(listener);
    }

    protected void checkSubclass() {
        getImpl().checkSubclass();
    }

    /**
     * Returns the Control which is registered for this DropTarget.  This is the control over which the
     * user positions the cursor to drop the data.
     *
     * @return the Control which is registered for this DropTarget
     */
    public Control getControl() {
        return getImpl().getControl();
    }

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
    public DropTargetListener[] getDropListeners() {
        return getImpl().getDropListeners();
    }

    /**
     * Returns the drop effect for this DropTarget.  This drop effect will be
     * used during a drag and drop to display the drag under effect on the
     * target widget.
     *
     * @return the drop effect that is registered for this DropTarget
     *
     * @since 3.3
     */
    public DropTargetEffect getDropTargetEffect() {
        return getImpl().getDropTargetEffect();
    }

    /**
     * Returns a list of the data types that can be transferred to this DropTarget.
     *
     * @return a list of the data types that can be transferred to this DropTarget
     */
    public Transfer[] getTransfer() {
        return getImpl().getTransfer();
    }

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
    public void removeDropListener(DropTargetListener listener) {
        getImpl().removeDropListener(listener);
    }

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
    public void setTransfer(Transfer... transferAgents) {
        getImpl().setTransfer(transferAgents);
    }

    /**
     * Specifies the drop effect for this DropTarget.  This drop effect will be
     * used during a drag and drop to display the drag under effect on the
     * target widget.
     *
     * @param effect the drop effect that is registered for this DropTarget
     *
     * @since 3.3
     */
    public void setDropTargetEffect(DropTargetEffect effect) {
        getImpl().setDropTargetEffect(effect);
    }

    protected DropTarget(IDropTarget impl) {
        super(impl);
    }

    static DropTarget createApi(IDropTarget impl) {
        return new DropTarget(impl);
    }

    public IDropTarget getImpl() {
        return (IDropTarget) super.getImpl();
    }
}
