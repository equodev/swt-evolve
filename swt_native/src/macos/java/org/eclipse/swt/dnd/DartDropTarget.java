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
 *  @see <a href="http://www.eclipse.org/swt/snippets/#dnd">Drag and Drop snippets</a>
 *  @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: DNDExample</a>
 *  @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
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
        String name = getClass().getName();
        String validName = DartDropTarget.class.getName();
        if (!validName.equals(name)) {
            DND.error(SWT.ERROR_INVALID_SUBCLASS);
        }
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
        dirty();
        if (transferAgents == null)
            DND.error(SWT.ERROR_NULL_ARGUMENT);
        this.transferAgents = transferAgents;
        // Register the types as valid drop types in Cocoa.
        // Accumulate all of the transfer types into a list.
        List<String> typeStrings = new ArrayList<>();
        for (int i = 0; i < this.transferAgents.length; i++) {
            String[] types = transferAgents[i].getTypeNames();
            for (int j = 0; j < types.length; j++) {
                typeStrings.add(types[j]);
            }
        }
        // Convert to an NSArray of NSStrings so we can register with the Control.
        int typeStringCount = typeStrings.size();
        for (int i = 0; i < typeStringCount; i++) {
        }
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
