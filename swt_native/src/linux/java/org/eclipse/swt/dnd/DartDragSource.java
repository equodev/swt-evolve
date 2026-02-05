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
 *      Lars Vogel <Lars.Vogel@vogella.com> - Bug 497807
 * *****************************************************************************
 */
package org.eclipse.swt.dnd;

import java.lang.reflect.*;
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

    long targetList;

    //workaround - remember action performed for DragEnd
    boolean moveData = false;

    //$NON-NLS-1$
    static final String DEFAULT_DRAG_SOURCE_EFFECT = "DEFAULT_DRAG_SOURCE_EFFECT";

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
    }

    static int checkStyle(int style) {
        if (style == SWT.NONE)
            return DND.DROP_MOVE;
        return style;
    }

    static void dragBeginProc(long source, long drag) {
    }

    static long dragPrepareProc(long source, double x, double y) {
        return 0;
    }

    long dragPrepare() {
        TransferData transferData = new TransferData();
        DNDEvent event = new DNDEvent();
        event.widget = this.getApi();
        event.dataType = transferData;
        notifyListeners(DND.DragSetData, event);
        if (!event.doit)
            return 0;
        // TODO: Need to return GdkContentProvider for the data given from event.data
        // Data from event.data also has to be converted to native through the Transfer class
        return 0;
    }

    static void dragEndProc(long source, long drag, boolean delete_data) {
    }

    static long DragBegin(long widget, long context) {
        DragSource source = FindDragSource(widget);
        if (source == null)
            return 0;
        ((DartDragSource) source.getImpl()).dragBegin(widget, context);
        return 0;
    }

    static long DragDataDelete(long widget, long context) {
        DragSource source = FindDragSource(widget);
        if (source == null)
            return 0;
        ((DartDragSource) source.getImpl()).dragDataDelete(widget, context);
        return 0;
    }

    static long DragEnd(long widget, long context) {
        DragSource source = FindDragSource(widget);
        if (source == null)
            return 0;
        ((DartDragSource) source.getImpl()).dragEnd(widget, context);
        return 0;
    }

    static long DragGetData(long widget, long context, long selection_data, long info, long time) {
        DragSource source = FindDragSource(widget);
        if (source == null)
            return 0;
        ((DartDragSource) source.getImpl()).dragGetData(widget, context, selection_data, (int) info, (int) time);
        return 0;
    }

    static DragSource FindDragSource(long handle) {
        Display display = SwtDisplay.findDisplay(Thread.currentThread());
        if (display == null || display.isDisposed())
            return null;
        Widget widget = display.findWidget(handle);
        if (widget == null)
            return null;
        return (DragSource) widget.getData(DND.DRAG_SOURCE_KEY);
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
        if (targetList == 0)
            return false;
        return true;
    }

    void drag(Event dragEvent) {
        moveData = false;
        DNDEvent event = new DNDEvent();
        event.widget = this.getApi();
        event.x = dragEvent.x;
        event.y = dragEvent.y;
        event.time = dragEvent.time;
        event.doit = true;
        notifyListeners(DND.DragStart, event);
        if (!event.doit || !canBeginDrag())
            return;
    }

    void dragBegin(long widget, long context) {
        /*
	 * Bug 515035: GTK DnD hijacks the D&D logic we have in SWT.
	 * When we recieve the signal from GTK of DragBegin, we will
	 * notify SWT that a drag has occurred.
	 */
        if (this.control instanceof Text) {
            DNDEvent event = new DNDEvent();
            Display display = SwtDisplay.getCurrent();
            Point loc = display.getCursorLocation();
            event.widget = this.getApi();
            event.doit = true;
            event.x = loc.x;
            event.y = loc.y;
            notifyListeners(DND.DragStart, event);
            if (!event.doit || transferAgents == null || transferAgents.length == 0)
                return;
            if (targetList == 0)
                return;
            Image image = event.image;
            if (context != 0 && image != null) {
            }
        }
    }

    void dragEnd(long widget, long context) {
        int operation = DND.DROP_NONE;
        if (context != 0) {
        }
        DNDEvent event = new DNDEvent();
        event.widget = this.getApi();
        //event.time = ???
        event.doit = operation != 0;
        event.detail = operation;
        moveData = false;
    }

    void dragGetData(long widget, long context, long selection_data, int info, int time) {
        if (selection_data == 0)
            return;
        TransferData transferData = new TransferData();
        DNDEvent event = new DNDEvent();
        event.widget = this.getApi();
        event.time = time;
        event.dataType = transferData;
        notifyListeners(DND.DragSetData, event);
        if (!event.doit)
            return;
        Transfer transfer = null;
        for (int i = 0; i < transferAgents.length; i++) {
            Transfer transferAgent = transferAgents[i];
            if (transferAgent != null && transferAgent.isSupportedType(transferData)) {
                transfer = transferAgent;
                break;
            }
        }
        if (transfer == null)
            return;
        transfer.javaToNative(event.data, transferData);
        if (transferData.result != 1)
            return;
        return;
    }

    void dragDataDelete(long widget, long context) {
        moveData = true;
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

    void onDispose() {
        if (control == null)
            return;
        if (targetList != 0) {
        }
        targetList = 0;
        if (controlListener != null) {
            control.removeListener(SWT.Dispose, controlListener);
            control.removeListener(SWT.DragDetect, controlListener);
        }
        controlListener = null;
        control.setData(DND.DRAG_SOURCE_KEY, null);
        control = null;
        transferAgents = null;
    }

    int opToOsOp(int operation) {
        int osOperation = 0;
        return osOperation;
    }

    int osOpToOp(int osOperation) {
        int operation = DND.DROP_NONE;
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
     * @see DragSourceListener
     * @see #addDragListener
     * @see #getDragListeners
     */
    public void removeDragListener(DragSourceListener listener) {
        if (listener == null)
            DND.error(SWT.ERROR_NULL_ARGUMENT);
        removeTypedListener(DND.DragStart, listener);
        removeTypedListener(DND.DragSetData, listener);
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

    public long _targetList() {
        return targetList;
    }

    public boolean _moveData() {
        return moveData;
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
