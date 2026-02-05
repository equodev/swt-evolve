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
 *      Lars Vogel <Lars.Vogel@vogella.com> - Bug 497807
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

    // TODO: These should either move out of Display or be accessible to this class.
    static byte[] types = { '*', '\0' };

    static int size = C.PTR_SIZEOF, align = C.PTR_SIZEOF == 4 ? 2 : 3;

    static final byte[] SWT_OBJECT = { 'S', 'W', 'T', '_', 'O', 'B', 'J', 'E', 'C', 'T', '\0' };

    static long proc2 = 0, proc3 = 0, proc4 = 0, proc5 = 0, proc6 = 0;

    String[] paths;

    boolean[] exist;

    // info for registering as a drag source
    Control control;

    Listener controlListener;

    Transfer[] transferAgents = new Transfer[0];

    DragSourceEffect dragEffect;

    Image dragImageFromListener;

    private int dragOperations;

    //$NON-NLS-1$
    static final String DEFAULT_DRAG_SOURCE_EFFECT = "DEFAULT_DRAG_SOURCE_EFFECT";

    private Point dragOffset;

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
                    if (event.widget instanceof Table || event.widget instanceof Tree) {
                        DartDragSource.this.dragOutlineViewStart(event);
                    } else {
                        DartDragSource.this.drag(event);
                    }
                }
            }
        };
        control.addListener(SWT.Dispose, controlListener);
        control.addListener(SWT.DragDetect, controlListener);
        this.addListener(SWT.Dispose, e -> onDispose());
        Object effect = control.getData(DEFAULT_DRAG_SOURCE_EFFECT);
        if (effect instanceof DragSourceEffect) {
            dragEffect = (DragSourceEffect) effect;
        } else if (control instanceof Tree) {
            dragEffect = new TreeDragSourceEffect((Tree) control);
        } else if (control instanceof Table) {
            dragEffect = new TableDragSourceEffect((Table) control);
        }
        // The dragSourceDelegate implements the pasteboard callback to provide the dragged data, so we always need
        // Tables and trees already implement dragging, so we need to override their drag methods instead of creating a dragging source.
        if (control instanceof Tree || control instanceof Table) {
        }
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

    static int checkStyle(int style) {
        if (style == SWT.NONE)
            return DND.DROP_MOVE;
        return style;
    }

    void drag(Event dragDetectEvent) {
        DNDEvent event = startDrag(dragDetectEvent);
        if (event == null)
            return;
        Image defaultDragImage = null;
        try {
            Image image = event.image;
            // If no image was provided, just create a trivial image. dragImage requires a non-null image.
            if (image == null) {
                int width = 20, height = 20;
                Image newDragImage = new Image(SwtDisplay.getCurrent(), width, height);
                GC imageGC = new GC(newDragImage);
                Color grayColor = new Color(SwtDisplay.getCurrent(), 50, 50, 50);
                imageGC.setForeground(grayColor);
                imageGC.drawRectangle(0, 0, 19, 19);
                imageGC.dispose();
                ImageData newImageData = newDragImage.getImageData();
                newImageData.alpha = (int) (255 * .4);
                defaultDragImage = new Image(SwtDisplay.getCurrent(), newImageData);
                newDragImage.dispose();
                grayColor.dispose();
                image = defaultDragImage;
                event.offsetX = width / 2;
                event.offsetY = height / 2;
            }
        } finally {
            if (defaultDragImage != null)
                defaultDragImage.dispose();
        }
    }

    void dragOutlineViewStart(Event dragDetectEvent) {
        DNDEvent event = startDrag(dragDetectEvent);
        if (event == null) {
            // This causes our 'outlineView:writeItems:toPasteboard:' listener
            // to return NO to cancel drag.
            dragDetectEvent.doit = false;
            return;
        }
        // Save off the custom image, if any.
        dragImageFromListener = event.image;
        // Save the computed offset for the image.  This needs to be passed back in dragImageForRowsWithIndexes
        // so the proxy image originates from the selection and not centered under the mouse.
        dragOffset = new Point(event.offsetX, event.offsetY);
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

    boolean canBeginDrag() {
        if (transferAgents == null || transferAgents.length == 0)
            return false;
        return true;
    }

    DNDEvent startDrag(Event dragEvent) {
        DNDEvent event = new DNDEvent();
        event.widget = this.getApi();
        event.x = dragEvent.x;
        event.y = dragEvent.y;
        event.time = dragEvent.time;
        event.doit = true;
        notifyListeners(DND.DragStart, event);
        if (!event.doit || !canBeginDrag())
            return null;
        for (int i = 0; i < transferAgents.length; i++) {
            Transfer transfer = transferAgents[i];
            if (transfer != null) {
                String[] typeNames = transfer.getTypeNames();
                for (int j = 0; j < typeNames.length; j++) {
                }
            }
        }
        // Save off the drag operations -- AppKit will call back to us to request them during the drag.
        dragOperations = opToOsOp(getStyle());
        return event;
    }

    public String[] _paths() {
        return paths;
    }

    public boolean[] _exist() {
        return exist;
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

    public Image _dragImageFromListener() {
        return dragImageFromListener;
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
