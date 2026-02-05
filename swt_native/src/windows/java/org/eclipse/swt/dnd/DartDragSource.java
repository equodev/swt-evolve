/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2020 IBM Corporation and others.
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
 *      Lars Vogel <Lars.Vogel@vogella.com> - Bug 497807
 *      Paul Pazderski - Improved implementation of IDataObject for bug 549643, 549661 and 567422
 * *****************************************************************************
 */
package org.eclipse.swt.dnd;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.widgets.*;
import dev.equo.swt.*;

/**
 *  <code>DragSource</code> defines the source object for a drag and drop transfer.
 *
 *  <p>IMPORTANT: This class is <em>not</em> intended to be subclassed.</p>
 *
 *  <p>A drag source is the object which originates a drag and drop operation. For the specified widget,
 *  it defines the type of data that is available for dragging and the set of operations that can
 *  be performed on that data.  The operations can be any bit-wise combination of DND.MOVE, DND.COPY or
 *  DND.LINK.  The type of data that can be transferred is specified by subclasses of Transfer such as
 *  TextTransfer or FileTransfer.  The type of data transferred can be a predefined system type or it
 *  can be a type defined by the application.  For instructions on how to define your own transfer type,
 *  refer to <code>ByteArrayTransfer</code>.</p>
 *
 *  <p>You may have several DragSources in an application but you can only have one DragSource
 *  per Control.  Data dragged from this DragSource can be dropped on a site within this application
 *  or it can be dropped on another application such as an external Text editor.</p>
 *
 *  <p>The application supplies the content of the data being transferred by implementing the
 *  <code>DragSourceListener</code> and associating it with the DragSource via DragSource#addDragListener.</p>
 *
 *  <p>When a successful move operation occurs, the application is required to take the appropriate
 *  action to remove the data from its display and remove any associated operating system resources or
 *  internal references.  Typically in a move operation, the drop target makes a copy of the data
 *  and the drag source deletes the original.  However, sometimes copying the data can take a long
 *  time (such as copying a large file).  Therefore, on some platforms, the drop target may actually
 *  move the data in the operating system rather than make a copy.  This is usually only done in
 *  file transfers.  In this case, the drag source is informed in the DragEnd event that a
 *  DROP_TARGET_MOVE was performed.  It is the responsibility of the drag source at this point to clean
 *  up its displayed information.  No action needs to be taken on the operating system resources.</p>
 *
 *  <p> The following example shows a Label widget that allows text to be dragged from it.</p>
 *
 *  <pre><code>
 * 	// Enable a label as a Drag Source
 * 	Label label = new Label(shell, SWT.NONE);
 * 	// This example will allow text to be dragged
 * 	Transfer[] types = new Transfer[] {TextTransfer.getInstance()};
 * 	// This example will allow the text to be copied or moved to the drop target
 * 	int operations = DND.DROP_MOVE | DND.DROP_COPY;
 *
 * 	DragSource source = new DragSource(label, operations);
 * 	source.setTransfer(types);
 * 	source.addDragListener(new DragSourceListener() {
 * 		public void dragStart(DragSourceEvent e) {
 * 			// Only start the drag if there is actually text in the
 * 			// label - this text will be what is dropped on the target.
 * 			if (label.getText().length() == 0) {
 * 				event.doit = false;
 * 			}
 * 		};
 * 		public void dragSetData(DragSourceEvent event) {
 * 			// A drop has been performed, so provide the data of the
 * 			// requested type.
 * 			// (Checking the type of the requested data is only
 * 			// necessary if the drag source supports more than
 * 			// one data type but is shown here as an example).
 * 			if (TextTransfer.getInstance().isSupportedType(event.dataType)){
 * 				event.data = label.getText();
 * 			}
 * 		}
 * 		public void dragFinished(DragSourceEvent event) {
 * 			// A Move operation has been performed so remove the data
 * 			// from the source
 * 			if (event.detail == DND.DROP_MOVE)
 * 				label.setText("");
 * 		}
 * 	});
 *  </code></pre>
 *
 *  <dl>
 * 	<dt><b>Styles</b></dt> <dd>DND.DROP_NONE, DND.DROP_COPY, DND.DROP_MOVE, DND.DROP_LINK</dd>
 * 	<dt><b>Events</b></dt> <dd>DND.DragStart, DND.DragSetData, DND.DragEnd</dd>
 *  </dl>
 *
 *  @see <a href="http://www.eclipse.org/swt/snippets/#dnd">Drag and Drop snippets</a>
 *  @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: DNDExample</a>
 *  @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *  @noextend This class is not intended to be subclassed by clients.
 */
public class DartDragSource extends DartWidget implements IDragSource {

    // info for registering as a drag source
    Control control;

    Listener controlListener;

    Transfer[] transferAgents = new Transfer[0];

    DragSourceEffect dragEffect;

    Composite topControl;

    long hwndDrag;

    //workaround - track the operation performed by the drop target for DragEnd event
    int dataEffect = DND.DROP_NONE;

    //$NON-NLS-1$
    static final String DEFAULT_DRAG_SOURCE_EFFECT = "DEFAULT_DRAG_SOURCE_EFFECT";

    //$NON-NLS-1$
    static final int CFSTR_PERFORMEDDROPEFFECT = SwtTransfer.registerType("Performed DropEffect");

    /**
     *  Creates a new <code>DragSource</code> to handle dragging from the specified <code>Control</code>.
     *  Creating an instance of a DragSource may cause system resources to be allocated depending on the platform.
     *  It is therefore mandatory that the DragSource instance be disposed when no longer required.
     *
     *  @param control the <code>Control</code> that the user clicks on to initiate the drag
     *  @param style the bitwise OR'ing of allowed operations; this may be a combination of any of
     * 					DND.DROP_NONE, DND.DROP_COPY, DND.DROP_MOVE, DND.DROP_LINK
     *
     *  @exception SWTException <ul>
     *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *     <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     *  </ul>
     *  @exception SWTError <ul>
     *     <li>ERROR_CANNOT_INIT_DRAG - unable to initiate drag source; this will occur if more than one
     *         drag source is created for a control or if the operating system will not allow the creation
     *         of the drag source</li>
     *  </ul>
     *
     *  <p>NOTE: ERROR_CANNOT_INIT_DRAG should be an SWTException, since it is a
     *  recoverable error, but can not be changed due to backward compatibility.</p>
     *
     *  @see Widget#dispose
     *  @see DragSource#checkSubclass
     *  @see DND#DROP_NONE
     *  @see DND#DROP_COPY
     *  @see DND#DROP_MOVE
     *  @see DND#DROP_LINK
     */
    public DartDragSource(Control control, int style, DragSource api) {
        super(control, checkStyle(style), api);
        this.control = control;
        if (control.getData(DND.DRAG_SOURCE_KEY) != null) {
            DND.error(DND.ERROR_CANNOT_INIT_DRAG);
        }
        control.setData(DND.DRAG_SOURCE_KEY, this.getApi());
        controlListener = event -> {
            if (event.type == SWT.Dispose) {
                if (!DartDragSource.this.getApi().isDisposed()) {
                    DartDragSource.this.getApi().dispose();
                }
            }
            if (event.type == SWT.DragDetect) {
                if (!DartDragSource.this.getApi().isDisposed()) {
                    DartDragSource.this.drag(event);
                }
            }
            if (event.type == SWT.ZoomChanged) {
                if (!DartDragSource.this.getApi().isDisposed()) {
                    this.getApi().nativeZoom = event.detail;
                }
            }
        };
        control.addListener(SWT.Dispose, controlListener);
        control.addListener(SWT.DragDetect, controlListener);
        control.addListener(SWT.ZoomChanged, controlListener);
        this.addListener(SWT.Dispose, e -> DartDragSource.this.onDispose());
        Object effect = control.getData(DEFAULT_DRAG_SOURCE_EFFECT);
        if (effect instanceof DragSourceEffect) {
            dragEffect = (DragSourceEffect) effect;
        } else if (control instanceof Tree) {
            dragEffect = new TreeDragSourceEffect((Tree) control);
        } else if (control instanceof Table) {
            dragEffect = new TableDragSourceEffect((Table) control);
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
    public void addDragListener(DragSourceListener listener) {
        if (listener == null)
            DND.error(SWT.ERROR_NULL_ARGUMENT);
        DNDListener typedListener = new DNDListener(listener);
        typedListener.dndWidget = this.getApi();
        addListener(DND.DragStart, typedListener);
        addListener(DND.DragSetData, typedListener);
        addListener(DND.DragEnd, typedListener);
    }

    @Override
    public void checkSubclass() {
        String name = getClass().getName();
        String validName = DartDragSource.class.getName();
        if (!validName.equals(name)) {
            DND.error(SWT.ERROR_INVALID_SUBCLASS);
        }
    }

    boolean canBeginDrag() {
        if (transferAgents == null || transferAgents.length == 0)
            return false;
        return true;
    }

    private void drag(Event dragEvent) {
        DNDEvent event = new DNDEvent();
        event.widget = this.getApi();
        event.x = dragEvent.x;
        event.y = dragEvent.y;
        event.doit = true;
        notifyListeners(DND.DragStart, event);
        if (!event.doit || !canBeginDrag())
            return;
        int[] pdwEffect = new int[1];
        Display display = control.getDisplay();
        //$NON-NLS-1$
        String key = "org.eclipse.swt.internal.win32.runMessagesInIdle";
        Object oldValue = display.getData(key);
        display.setData(key, Boolean.TRUE);
        Image image = event.image;
        hwndDrag = 0;
        topControl = null;
        if (image != null) {
            topControl = control.getShell();
            hwndDrag = topControl.handle;
            if ((topControl.getStyle() & SWT.RIGHT_TO_LEFT) != 0) {
            }
        }
        String externalLoopKey = "org.eclipse.swt.internal.win32.externalEventLoop";
        try {
            display.setData(externalLoopKey, Boolean.TRUE);
        } finally {
            display.setData(externalLoopKey, Boolean.FALSE);
            // ensure that we don't leave transparent window around
            if (hwndDrag != 0) {
                hwndDrag = 0;
                topControl = null;
            }
            display.setData(key, oldValue);
        }
        int operation = osToOp(pdwEffect[0]);
        if (dataEffect == DND.DROP_MOVE) {
            operation = (operation == DND.DROP_NONE || operation == DND.DROP_COPY) ? DND.DROP_TARGET_MOVE : DND.DROP_MOVE;
        } else {
            if (dataEffect != DND.DROP_NONE) {
                operation = dataEffect;
            }
        }
        event = new DNDEvent();
        event.widget = this.getApi();
        event.detail = operation;
        notifyListeners(DND.DragEnd, event);
        dataEffect = DND.DROP_NONE;
    }

    /*
 * EnumFormatEtc([in] dwDirection, [out] ppenumFormatetc)
 * Ownership of ppenumFormatetc transfers from callee to caller so reference count on ppenumFormatetc
 * must be incremented before returning.  Caller is responsible for releasing ppenumFormatetc.
 */
    private static int EnumFormatEtc(Transfer[] transferAgents, int dwDirection, long ppenumFormatetc) {
        // what types have been registered?
        TransferData[] allowedDataTypes = new TransferData[0];
        for (Transfer transferAgent : transferAgents) {
            if (transferAgent != null) {
                TransferData[] formats = transferAgent.getSupportedTypes();
                TransferData[] newAllowedDataTypes = new TransferData[allowedDataTypes.length + formats.length];
                System.arraycopy(allowedDataTypes, 0, newAllowedDataTypes, 0, allowedDataTypes.length);
                System.arraycopy(formats, 0, newAllowedDataTypes, allowedDataTypes.length, formats.length);
                allowedDataTypes = newAllowedDataTypes;
            }
        }
        OleEnumFORMATETC enumFORMATETC = new OleEnumFORMATETC();
        enumFORMATETC.AddRef();
        return 0;
    }

    /**
     * Returns the Control which is registered for this DragSource.  This is the control that the
     * user clicks in to initiate dragging.
     *
     * @return the Control which is registered for this DragSource
     */
    public Control getControl() {
        return control;
    }

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
    public DragSourceListener[] getDragListeners() {
        return getTypedListeners(DND.DragStart, DragSourceListener.class).toArray(DragSourceListener[]::new);
    }

    /**
     * Returns the drag effect that is registered for this DragSource.  This drag
     * effect will be used during a drag and drop operation.
     *
     * @return the drag effect that is registered for this DragSource
     *
     * @since 3.3
     */
    public DragSourceEffect getDragSourceEffect() {
        return dragEffect;
    }

    /**
     * Returns the list of data types that can be transferred by this DragSource.
     *
     * @return the list of data types that can be transferred by this DragSource
     */
    public Transfer[] getTransfer() {
        return transferAgents;
    }

    private int GiveFeedback(int dwEffect) {
        return 0;
    }

    private int QueryContinueDrag(int fEscapePressed, int grfKeyState) {
        if (fEscapePressed != 0) {
        }
        if (hwndDrag != 0) {
        }
        return 0;
    }

    private void onDispose() {
        if (control == null)
            return;
        if (controlListener != null) {
            control.removeListener(SWT.Dispose, controlListener);
            control.removeListener(SWT.DragDetect, controlListener);
        }
        controlListener = null;
        control.setData(DND.DRAG_SOURCE_KEY, null);
        control = null;
        transferAgents = null;
    }

    private int opToOs(int operation) {
        int osOperation = 0;
        if ((operation & DND.DROP_COPY) != 0) {
        }
        if ((operation & DND.DROP_LINK) != 0) {
        }
        if ((operation & DND.DROP_MOVE) != 0) {
        }
        return osOperation;
    }

    private int osToOp(int osOperation) {
        int operation = 0;
        return operation;
    }

    private static int QueryGetData(Transfer[] transferAgents, long pFormatetc) {
        return 0;
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
     * @see DragSourceListener
     * @see #addDragListener
     * @see #getDragListeners
     */
    public void removeDragListener(DragSourceListener listener) {
        if (listener == null)
            DND.error(SWT.ERROR_NULL_ARGUMENT);
        removeTypedListener(DND.DragStart, listener);
        removeTypedListener(DND.DragSetData, listener);
        removeTypedListener(DND.DragEnd, listener);
    }

    /**
     * Specifies the drag effect for this DragSource.  This drag effect will be
     * used during a drag and drop operation.
     *
     * @param effect the drag effect that is registered for this DragSource
     *
     * @since 3.3
     */
    public void setDragSourceEffect(DragSourceEffect effect) {
        dragEffect = effect;
    }

    /**
     * Specifies the list of data types that can be transferred by this DragSource.
     * The application must be able to provide data to match each of these types when
     * a successful drop has occurred.
     *
     * @param transferAgents a list of Transfer objects which define the types of data that can be
     * dragged from this source
     */
    public void setTransfer(Transfer... transferAgents) {
        dirty();
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

    public DragSourceEffect _dragEffect() {
        return dragEffect;
    }

    public Composite _topControl() {
        return topControl;
    }

    public long _hwndDrag() {
        return hwndDrag;
    }

    public int _dataEffect() {
        return dataEffect;
    }

    protected void _hookEvents() {
        super._hookEvents();
        FlutterBridge.on(this, "Drag", "dragFinished", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(DND.DragEnd, e);
            });
        });
        FlutterBridge.on(this, "Drag", "dragSetData", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(DND.DragSetData, e);
            });
        });
        FlutterBridge.on(this, "Drag", "dragStart", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(DND.DragStart, e);
            });
        });
    }

    public DragSource getApi() {
        if (api == null)
            api = DragSource.createApi(this);
        return (DragSource) api;
    }
}
