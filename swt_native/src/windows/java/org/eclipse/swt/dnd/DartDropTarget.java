/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *  @see <a href="http://www.eclipse.org/swt/snippets/#dnd">Drag and Drop snippets</a>
 *  @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: DNDExample</a>
 *  @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *  @noextend This class is not intended to be subclassed by clients.
 */
public class DartDropTarget extends DartWidget implements IDropTarget {

    Control control;

    Listener controlListener;

    Transfer[] transferAgents = new Transfer[0];

    DropTargetEffect dropEffect;

    // Track application selections
    TransferData selectedDataType;

    int selectedOperation;

    // workaround - There is no event for "operation changed" so track operation based on key state
    int keyOperation = -1;

    // workaround - The dataobject address is only passed as an argument in drag enter and drop.
    // To allow applications to query the data values during the drag over operations,
    int refCount;

    //$NON-NLS-1$
    static final String DEFAULT_DROP_TARGET_EFFECT = "DEFAULT_DROP_TARGET_EFFECT";

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
        this.AddRef();
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
    }

    static int checkStyle(int style) {
        if (style == SWT.NONE)
            return DND.DROP_MOVE;
        return style;
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

    int AddRef() {
        refCount++;
        return refCount;
    }

    @Override
    public void checkSubclass() {
        String name = getClass().getName();
        String validName = DartDropTarget.class.getName();
        if (!validName.equals(name)) {
            DND.error(SWT.ERROR_INVALID_SUBCLASS);
        }
    }

    int DragEnter_64(long pDataObject, int grfKeyState, long pt, long pdwEffect) {
        return 0;
    }

    int DragEnter(long pDataObject, int grfKeyState, int pt_x, int pt_y, long pdwEffect) {
        Point location = convertPixelToPoint(pt_x, pt_y);
        selectedDataType = null;
        selectedOperation = DND.DROP_NONE;
        DNDEvent event = new DNDEvent();
        if (!setEventData(event, pDataObject, grfKeyState, location.x, location.y, pdwEffect)) {
        }
        int allowedOperations = event.operations;
        TransferData[] allowedDataTypes = new TransferData[event.dataTypes.length];
        System.arraycopy(event.dataTypes, 0, allowedDataTypes, 0, allowedDataTypes.length);
        notifyListeners(DND.DragEnter, event);
        refresh();
        if (event.detail == DND.DROP_DEFAULT) {
            event.detail = (allowedOperations & DND.DROP_MOVE) != 0 ? DND.DROP_MOVE : DND.DROP_NONE;
        }
        selectedDataType = null;
        for (TransferData allowedDataType : allowedDataTypes) {
            if (SwtTransferData.sameType(allowedDataType, event.dataType)) {
                selectedDataType = allowedDataType;
                break;
            }
        }
        selectedOperation = DND.DROP_NONE;
        if (selectedDataType != null && ((allowedOperations & event.detail) != 0)) {
            selectedOperation = event.detail;
        }
        return 0;
    }

    int DragLeave() {
        keyOperation = -1;
        DNDEvent event = new DNDEvent();
        event.widget = this.getApi();
        event.detail = DND.DROP_NONE;
        notifyListeners(DND.DragLeave, event);
        refresh();
        return 0;
    }

    int DragOver_64(int grfKeyState, long pt, long pdwEffect) {
        return 0;
    }

    int DragOver(int grfKeyState, int pt_x, int pt_y, long pdwEffect) {
        int oldKeyOperation = keyOperation;
        DNDEvent event = new DNDEvent();
        int allowedOperations = event.operations;
        TransferData[] allowedDataTypes = new TransferData[event.dataTypes.length];
        System.arraycopy(event.dataTypes, 0, allowedDataTypes, 0, allowedDataTypes.length);
        if (keyOperation == oldKeyOperation) {
            event.type = DND.DragOver;
            event.dataType = selectedDataType;
            event.detail = selectedOperation;
        } else {
            event.type = DND.DragOperationChanged;
            event.dataType = selectedDataType;
        }
        notifyListeners(event.type, event);
        refresh();
        if (event.detail == DND.DROP_DEFAULT) {
            event.detail = (allowedOperations & DND.DROP_MOVE) != 0 ? DND.DROP_MOVE : DND.DROP_NONE;
        }
        selectedDataType = null;
        for (TransferData allowedDataType : allowedDataTypes) {
            if (SwtTransferData.sameType(allowedDataType, event.dataType)) {
                selectedDataType = allowedDataType;
                break;
            }
        }
        selectedOperation = DND.DROP_NONE;
        if (selectedDataType != null && ((allowedOperations & event.detail) == event.detail)) {
            selectedOperation = event.detail;
        }
        return 0;
    }

    int Drop_64(long pDataObject, int grfKeyState, long pt, long pdwEffect) {
        return 0;
    }

    private Point convertPixelToPoint(int xInPixels, int yInPixels) {
        if (this.control == null) {
        }
        // There is no API to convert absolute values in pixels to display relative
        // points. Therefor, the display relative pixel values are converted to control
        return null;
    }

    int Drop(long pDataObject, int grfKeyState, int pt_x, int pt_y, long pdwEffect) {
        try {
            Point location = convertPixelToPoint(pt_x, pt_y);
            DNDEvent event = new DNDEvent();
            event.widget = this.getApi();
            if (dropEffect != null) {
                event.item = dropEffect.getItem(location.x, location.y);
            }
            event.detail = DND.DROP_NONE;
            notifyListeners(DND.DragLeave, event);
            refresh();
            event = new DNDEvent();
            if (!setEventData(event, pDataObject, grfKeyState, location.x, location.y, pdwEffect)) {
                keyOperation = -1;
            }
            keyOperation = -1;
            int allowedOperations = event.operations;
            TransferData[] allowedDataTypes = new TransferData[event.dataTypes.length];
            System.arraycopy(event.dataTypes, 0, allowedDataTypes, 0, allowedDataTypes.length);
            event.dataType = selectedDataType;
            event.detail = selectedOperation;
            notifyListeners(DND.DropAccept, event);
            refresh();
            selectedDataType = null;
            for (TransferData allowedDataType : allowedDataTypes) {
                if (SwtTransferData.sameType(allowedDataType, event.dataType)) {
                    selectedDataType = allowedDataType;
                    break;
                }
            }
            selectedOperation = DND.DROP_NONE;
            if (selectedDataType != null && (allowedOperations & event.detail) == event.detail) {
                selectedOperation = event.detail;
            }
            if (selectedOperation == DND.DROP_NONE) {
            }
            // Get Data in a Java format
            Object object = null;
            for (Transfer transfer : transferAgents) {
                if (transfer != null && transfer.isSupportedType(selectedDataType)) {
                    object = transfer.nativeToJava(selectedDataType);
                    break;
                }
            }
            if (object == null) {
                selectedOperation = DND.DROP_NONE;
            }
            event.detail = selectedOperation;
            event.dataType = selectedDataType;
            event.data = object;
            try {
                notifyListeners(DND.Drop, event);
            } finally {
            }
            refresh();
            selectedOperation = DND.DROP_NONE;
            if ((allowedOperations & event.detail) == event.detail) {
                selectedOperation = event.detail;
            }
        } finally {
        }
        return 0;
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

    int getOperationFromKeyState(int grfKeyState) {
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
        transferAgents = null;
        control = null;
        this.Release();
    }

    int opToOs(int operation) {
        int osOperation = 0;
        if ((operation & DND.DROP_COPY) != 0) {
        }
        if ((operation & DND.DROP_LINK) != 0) {
        }
        if ((operation & DND.DROP_MOVE) != 0) {
        }
        return osOperation;
    }

    int osToOp(int osOperation) {
        int operation = 0;
        return operation;
    }

    /* QueryInterface([in] iid, [out] ppvObject)
 * Ownership of ppvObject transfers from callee to caller so reference count on ppvObject
 * must be incremented before returning.  Caller is responsible for releasing ppvObject.
 */
    int QueryInterface(long riid, long ppvObject) {
        return 0;
    }

    int Release() {
        refCount--;
        if (refCount == 0) {
        }
        return refCount;
    }

    void refresh() {
        if (control == null || control.isDisposed())
            return;
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

    boolean setEventData(DNDEvent event, long pDataObject, int grfKeyState, int pt_x, int pt_y, long pdwEffect) {
        if (pDataObject == 0 || pdwEffect == 0)
            return false;
        // get allowed operations
        int style = getStyle();
        int[] operations = new int[1];
        operations[0] = osToOp(operations[0]) & style;
        if (operations[0] == DND.DROP_NONE)
            return false;
        // get current operation
        int operation = getOperationFromKeyState(grfKeyState);
        keyOperation = operation;
        if (operation == DND.DROP_DEFAULT) {
            if ((style & DND.DROP_DEFAULT) == 0) {
                operation = (operations[0] & DND.DROP_MOVE) != 0 ? DND.DROP_MOVE : DND.DROP_NONE;
            }
        } else {
            if ((operation & operations[0]) == 0)
                operation = DND.DROP_NONE;
        }
        // Get allowed transfer types
        TransferData[] dataTypes = new TransferData[0];
        try {
            try {
                try {
                } finally {
                }
            } finally {
            }
        } finally {
        }
        if (dataTypes.length == 0)
            return false;
        event.widget = this.getApi();
        event.x = pt_x;
        event.y = pt_y;
        event.feedback = DND.FEEDBACK_SELECT;
        event.dataTypes = dataTypes;
        event.dataType = dataTypes[0];
        if (dropEffect != null) {
            event.item = dropEffect.getItem(pt_x, pt_y);
        }
        event.operations = operations[0];
        event.detail = operation;
        return true;
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
        dirty();
        if (transferAgents == null)
            DND.error(SWT.ERROR_NULL_ARGUMENT);
        this.transferAgents = transferAgents;
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

    public TransferData _selectedDataType() {
        return selectedDataType;
    }

    public int _selectedOperation() {
        return selectedOperation;
    }

    public int _keyOperation() {
        return keyOperation;
    }

    public int _refCount() {
        return refCount;
    }

    protected void _hookEvents() {
        super._hookEvents();
        FlutterBridge.on(this, "Drop", "dragEnter", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(DND.DragEnter, e);
            });
        });
        FlutterBridge.on(this, "Drop", "dragLeave", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(DND.DragLeave, e);
            });
        });
        FlutterBridge.on(this, "Drop", "dragOperationChanged", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(DND.DragOperationChanged, e);
            });
        });
        FlutterBridge.on(this, "Drop", "dragOver", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(DND.DragOver, e);
            });
        });
        FlutterBridge.on(this, "Drop", "drop", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(DND.Drop, e);
            });
        });
        FlutterBridge.on(this, "Drop", "dropAccept", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(DND.DropAccept, e);
            });
        });
    }

    public DropTarget getApi() {
        if (api == null)
            api = DropTarget.createApi(this);
        return (DropTarget) api;
    }
}
