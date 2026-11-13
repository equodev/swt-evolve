/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2017 IBM Corporation and others.
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

import java.util.*;
import java.util.List;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.widgets.*;
import dev.equo.swt.*;

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
 *  @see <a href="https://eclipse.dev/eclipse/swt/snippets/#dnd">Drag and Drop snippets</a>
 *  @see <a href="https://eclipse.dev/eclipse/swt/examples.html">SWT Example: DNDExample</a>
 *  @see <a href="https://eclipse.dev/eclipse/swt/">Sample code and further information</a>
 *  @noextend This class is not intended to be subclassed by clients.
 */
public class DartDropTarget extends DartWidget implements IDropTarget {

    static long proc2Args, proc3Args, proc6Args;

    //$NON-NLS-1$
    static final String LOCK_CURSOR = "org.eclipse.swt.internal.lockCursor";

    static boolean dropNotAllowed = false;

    Control control;

    Listener controlListener;

    Transfer[] transferAgents = new Transfer[0];

    DropTargetEffect dropEffect;

    int feedback = DND.FEEDBACK_NONE;

    boolean labelDragHandlersAdded = false;

    // Track application selections
    TransferData selectedDataType;

    int selectedOperation;

    // workaround - There is no event for "operation changed" so track operation based on key state
    int keyOperation = -1;

    //$NON-NLS-1$
    static final String DEFAULT_DROP_TARGET_EFFECT = "DEFAULT_DROP_TARGET_EFFECT";

    //$NON-NLS-1$
    static final String IS_ACTIVE = "org.eclipse.swt.internal.isActive";

    /**
     * Recursively traverses the hierarchy of a control to handle labels with image
     * views.
     * If the child of a control is a label with an image view, drag handlers are added for
     * the corresponding label class to enable it to act as a drop target.
     * @param c the Control whose hierarchy needs to be iterated to check for label with imageView
     */
    void handleLabels(Control c) {
        if (labelDragHandlersAdded)
            return;
        if (c instanceof Label) {
        } else if (c instanceof Composite) {
            Control[] cal = ((Composite) c).getChildren();
            for (Control child : cal) {
                handleLabels(child);
            }
        }
    }

    void addDragHandlers() {
        handleLabels(control);
    }

    void addDragHandlers(long cls) {
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
        if (listener == null)
            DND.error(SWT.ERROR_NULL_ARGUMENT);
        DNDListener typedListener = new DNDListener(listener);
        typedListener.dndWidget = this.getApi();
        addListener(DND.DragEnter, typedListener);
        addListener(DND.DragLeave, typedListener);
        addListener(DND.DragOver, typedListener);
        addListener(DND.DragOperationChanged, typedListener);
        addListener(DND.Drop, typedListener);
        addListener(DND.DropAccept, typedListener);
    }

    static int checkStyle(int style) {
        if (style == SWT.NONE)
            return DND.DROP_MOVE;
        return style;
    }

    @Override
    public void checkSubclass() {
        if (!DropTarget.class.getName().equals(getApi().getClass().getName()))
            DND.error(SWT.ERROR_INVALID_SUBCLASS);
    }

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
    public DartDropTarget(Control control, int style, DropTarget api) {
        super(control, checkStyle(style), api);
        this.control = control;
        if (control.getData(DND.DROP_TARGET_KEY) != null) {
            DND.error(DND.ERROR_CANNOT_INIT_DROP);
        }
        control.setData(DND.DROP_TARGET_KEY, this.getApi());
        this.bridge = ((DartWidget) control.getImpl()).getBridge();
        if (this.bridge != null && control.getImpl() instanceof DartControl) {
            ((DartControl) control.getImpl()).getValue().markDirty(VControl.DROP_TARGET_ID);
        }
        _hookEvents();
        controlListener = event -> {
            if (!DartDropTarget.this.getApi().isDisposed()) {
                DartDropTarget.this.getApi().dispose();
            }
        };
        control.addListener(SWT.Dispose, controlListener);
        this.addListener(SWT.Dispose, event -> onDispose());
        Object effect = control.getData(DEFAULT_DROP_TARGET_EFFECT);
        if (effect instanceof DropTargetEffect) {
            dropEffect = (DropTargetEffect) effect;
        } else if (control instanceof Table) {
            dropEffect = new TableDropTargetEffect((Table) control);
        } else if (control instanceof Tree) {
            dropEffect = new TreeDropTargetEffect((Tree) control);
        }
        addDragHandlers();
    }

    /**
     * Returns the Control which is registered for this DropTarget.  This is the control over which the
     * user positions the cursor to drop the data.
     *
     * @return the Control which is registered for this DropTarget
     */
    public Control getControl() {
        return control;
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
        return getTypedListeners(DND.DragEnter, DropTargetListener.class).toArray(DropTargetListener[]::new);
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
        return dropEffect;
    }

    int getOperationFromKeyState() {
        // The NSDraggingInfo object already combined the modifier keys with the
        // drag source's allowed events. This might be better accomplished by diffing
        // the base drag source mask with the active drag state mask instead of snarfing
        // the current event.
        // See documentation on [NSDraggingInfo draggingSourceOperationMask] for the
        // correct Cocoa behavior.  Control + Option or Command is NSDragOperationGeneric,
        return DND.DROP_DEFAULT;
    }

    /**
     * Returns a list of the data types that can be transferred to this DropTarget.
     *
     * @return a list of the data types that can be transferred to this DropTarget
     */
    public Transfer[] getTransfer() {
        return transferAgents;
    }

    void onDispose() {
        if (control == null)
            return;
        if (controlListener != null)
            control.removeListener(SWT.Dispose, controlListener);
        controlListener = null;
        control.setData(DND.DROP_TARGET_KEY, null);
        if (this.bridge != null && !control.isDisposed() && control.getImpl() instanceof DartControl) {
            ((DartControl) control.getImpl()).getValue().markDirty(VControl.DROP_TARGET_ID);
        }
        transferAgents = null;
        control = null;
    }

    int opToOsOp(int operation) {
        int osOperation = 0;
        if ((operation & DND.DROP_COPY) != 0) {
        }
        if ((operation & DND.DROP_LINK) != 0) {
        }
        if ((operation & DND.DROP_MOVE) != 0) {
        }
        if ((operation & DND.DROP_TARGET_MOVE) != 0) {
        }
        return osOperation;
    }

    int osOpToOp(long osOperation) {
        int operation = 0;
        return operation;
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
        if (listener == null)
            DND.error(SWT.ERROR_NULL_ARGUMENT);
        removeTypedListener(DND.DragEnter, listener);
        removeTypedListener(DND.DragLeave, listener);
        removeTypedListener(DND.DragOver, listener);
        removeTypedListener(DND.DragOperationChanged, listener);
        removeTypedListener(DND.Drop, listener);
        removeTypedListener(DND.DropAccept, listener);
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
        dropEffect = effect;
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
        if (transferAgents == null)
            DND.error(SWT.ERROR_NULL_ARGUMENT);
        this.transferAgents = transferAgents;
    }

    void setDropNotAllowed() {
        if (!dropNotAllowed) {
            Display display = getDisplay();
            display.setData(LOCK_CURSOR, Boolean.FALSE);
            display.setData(LOCK_CURSOR, Boolean.TRUE);
            dropNotAllowed = true;
        }
    }

    void clearDropNotAllowed() {
        if (dropNotAllowed) {
            Display display = getDisplay();
            display.setData(LOCK_CURSOR, Boolean.FALSE);
            display.setData(LOCK_CURSOR, Boolean.TRUE);
            dropNotAllowed = false;
        }
    }

    public Control _control() {
        return control;
    }

    public Listener _controlListener() {
        return controlListener;
    }

    public Transfer[] _transferAgents() {
        return transferAgents;
    }

    public DropTargetEffect _dropEffect() {
        return dropEffect;
    }

    public int _feedback() {
        return feedback;
    }

    public boolean _labelDragHandlersAdded() {
        return labelDragHandlersAdded;
    }

    public TransferData _selectedDataType() {
        return selectedDataType;
    }

    public int _selectedOperation() {
        return selectedOperation;
    }

    public int _keyOperation() {
        return keyOperation;
    }

    @Override
    public void sendEvent(int eventType, Event event) {
        if (eventType == DND.Drop) {
            if (matchingDragSource() == null && ActiveDrag.refused(getDisplay())) {
                ActiveDrag.endRefused(getDisplay());
                return;
            }
            dropHelper.drop(event);
            return;
        }
        if (eventType == DND.DragLeave) {
            dropHelper.leave(event);
            return;
        }
        if (eventType == DND.DragEnter || eventType == DND.DragOver || eventType == DND.DragOperationChanged || eventType == DND.DropAccept) {
            dropHelper.negotiate(eventType, event);
            return;
        }
        super.sendEvent(eventType, event);
    }

    private final DropAnswer answer = new DropAnswer(199, 203, 198, 34);

    final DropTargetHelper dropHelper = new DropTargetHelper(this, answer);

    void deliver(int eventType, DNDEvent event) {
        super.sendEvent(eventType, event);
    }

    TransferData[] computeMutualDataTypes(DartDragSource source) {
        if (source == null)
            return new TransferData[0];
        java.util.List<TransferData> mutual = new java.util.ArrayList<>();
        for (Transfer offered : source._transferAgents()) {
            if (offered == null)
                continue;
            for (int offeredTypeId : offered.getTypeIds()) {
                for (Transfer target : transferAgents) {
                    if (target == null)
                        continue;
                    for (int targetTypeId : target.getTypeIds()) {
                        if (targetTypeId == offeredTypeId) {
                            TransferData data = new TransferData();
                            data.type = targetTypeId;
                            mutual.add(data);
                        }
                    }
                }
            }
        }
        return mutual.toArray(new TransferData[0]);
    }

    Widget resolveItem(Event e) {
        if (e.itemId != 0) {
            Widget found = DropTargetHelper.findItemById(control, e.itemId);
            if (found != null)
                return found;
        }
        if (control instanceof Table && e.index >= 0 && e.index < ((Table) control).getItemCount()) {
            Table table = (Table) control;
            return table.getItem(e.index);
        } else if (control instanceof org.eclipse.swt.custom.CTabFolder && e.index >= 0 && e.index < ((org.eclipse.swt.custom.CTabFolder) control).getItemCount()) {
            org.eclipse.swt.custom.CTabFolder folder = (org.eclipse.swt.custom.CTabFolder) control;
            return folder.getItem(e.index);
        }
        return null;
    }

    void notifyDragSourceOfDragEnd(int detail) {
        DartDragSource source = matchingDragSource();
        if (source != null)
            source.fireDragEnd(detail);
    }

    DartDragSource matchingDragSource() {
        DragSource source = ActiveDrag.of(getDisplay());
        return source != null ? (DartDragSource) source.getImpl() : null;
    }

    protected void _hookEvents() {
        super._hookEvents();
    }

    @Override
    protected String[] _clientEvents() {
        return FlutterBridge.joinEvents(super._clientEvents(), "Drop/dragEnter", "Drop/dragLeave", "Drop/dragOperationChanged", "Drop/dragOver", "Drop/drop", "Drop/dropAccept");
    }

    @Override
    protected void _onClientEvent(String _channel, org.eclipse.swt.widgets.Event e) {
        switch(_channel) {
            case "Drop/dragEnter":
                {
                    getDisplay().asyncExec(() -> {
                        if (isDisposed())
                            return;
                        sendEvent(DND.DragEnter, e);
                    });
                    return;
                }
            case "Drop/dragLeave":
                {
                    getDisplay().asyncExec(() -> {
                        if (isDisposed())
                            return;
                        sendEvent(DND.DragLeave, e);
                    });
                    return;
                }
            case "Drop/dragOperationChanged":
                {
                    getDisplay().asyncExec(() -> {
                        if (isDisposed())
                            return;
                        sendEvent(DND.DragOperationChanged, e);
                    });
                    return;
                }
            case "Drop/dragOver":
                {
                    getDisplay().asyncExec(() -> {
                        if (isDisposed())
                            return;
                        sendEvent(DND.DragOver, e);
                    });
                    return;
                }
            case "Drop/drop":
                {
                    getDisplay().asyncExec(() -> {
                        if (isDisposed())
                            return;
                        sendEvent(DND.Drop, e);
                    });
                    return;
                }
            case "Drop/dropAccept":
                {
                    getDisplay().asyncExec(() -> {
                        if (isDisposed())
                            return;
                        sendEvent(DND.DropAccept, e);
                    });
                    return;
                }
            default:
                super._onClientEvent(_channel, e);
        }
    }

    public DropTarget getApi() {
        if (api == null)
            api = DropTarget.createApi(this);
        return (DropTarget) api;
    }
}
