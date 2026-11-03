package org.eclipse.swt.dnd;

import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * Turns the drag events Flutter reports over a DropTarget into the ones SWT sends natively. Drag
 * events offer the operations both the DragSource and the DropTarget allow, with DROP_DEFAULT
 * resolved to MOVE, else COPY, else LINK. A drop sends DragLeave, then DropAccept, then Drop, and
 * reaches Drop only while the operation and a data type on offer still stand. Each drag negotiates
 * its data type afresh.
 *
 * <p>One per DropTarget; what it holds lasts one drag.
 */
public final class DropTargetHelper {

    private final DartDropTarget target;

    /** The target's last answer to the client, sent only when it changes within a drag. */
    private final DropAnswer answer;

    /** Whether the current drag has entered the target. */
    private boolean entered;

    DropTargetHelper(DartDropTarget target, DropAnswer answer) {
        this.target = target;
        this.answer = answer;
    }

    /** The item of {@code control} (a Table, Tree or CTabFolder) the client names by {@code id}, or null. */
    public static Widget findItemById(Control control, long id) {
        // An item the client points at was sent to it, so it is found by its id; one of this control's.
        Object byId = FlutterBridge.findById(id);
        Widget known = byId instanceof DartWidget ? ((DartWidget) byId).getApi() : null;
        if (known instanceof TableItem && ((TableItem) known).getParent() == control) return known;
        if (known instanceof TreeItem && ((TreeItem) known).getParent() == control) return known;
        if (known instanceof CTabItem && ((CTabItem) known).getParent() == control) return known;
        if (control instanceof Table) {
            for (TableItem item : ((Table) control).getItems()) {
                if (FlutterBridge.id(item) == id)
                    return item;
            }
        } else if (control instanceof Tree) {
            for (TreeItem item : ((Tree) control).getItems()) {
                Widget found = findTreeItemById(item, id);
                if (found != null)
                    return found;
            }
        } else if (control instanceof CTabFolder) {
            for (CTabItem item : ((CTabFolder) control).getItems()) {
                if (FlutterBridge.id(item) == id)
                    return item;
            }
        }
        return null;
    }

    private static Widget findTreeItemById(TreeItem item, long id) {
        if (FlutterBridge.id(item) == id)
            return item;
        for (TreeItem child : item.getItems()) {
            Widget found = findTreeItemById(child, id);
            if (found != null)
                return found;
        }
        return null;
    }

    /** DragEnter, DragOver, DragOperationChanged or DropAccept. */
    void negotiate(int eventType, Event e) {
        // A drag whose DragEnter never arrived starts here all the same.
        boolean entering = eventType == DND.DragEnter || !entered;
        if (entering)
            start();
        DNDEvent event = negotiationEvent(entering, e);
        int operations = event.operations;
        TransferData[] offered = event.dataTypes;
        target.deliver(eventType, event);
        target.selectedDataType = offeredType(offered, event.dataType);
        target.selectedOperation = acceptedOperation(entering, event.detail, operations);
        sendResult(target.selectedOperation, event.feedback);
        answerSource(target.selectedOperation);
    }

    void leave(Event e) {
        DNDEvent event = new DNDEvent();
        event.widget = target.getApi();
        event.time = e.time;
        event.detail = DND.DROP_NONE;
        target.deliver(DND.DragLeave, event);
        sendResult(DND.DROP_NONE, DND.FEEDBACK_NONE);
        answer.reset();
        entered = false;
        target.selectedDataType = null;
        target.selectedOperation = DND.DROP_NONE;
        answerSource(DND.DROP_NONE);
    }

    void drop(Event e) {
        Widget item = item(e);
        DNDEvent leave = new DNDEvent();
        leave.widget = target.getApi();
        leave.time = e.time;
        leave.item = item;
        leave.detail = DND.DROP_NONE;
        target.deliver(DND.DragLeave, leave);
        if (!entered) {
            start();
            target.selectedOperation = resolveDefault(allowedOperations());
        }
        DartDragSource source = target.matchingDragSource();
        DNDEvent event = new DNDEvent();
        event.widget = target.getApi();
        event.time = e.time;
        setDisplayLocation(event, e);
        event.doit = true;
        event.item = item;
        event.feedback = DND.FEEDBACK_SELECT;
        event.operations = allowedOperations();
        event.dataTypes = target.computeMutualDataTypes(source);
        event.dataType = currentType(event.dataTypes);
        event.detail = e.detail != DND.DROP_NONE ? e.detail : target.selectedOperation;
        int operations = event.operations;
        TransferData[] offered = event.dataTypes;
        target.deliver(DND.DropAccept, event);
        TransferData type = offeredType(offered, event.dataType);
        int detail = event.detail == DND.DROP_DEFAULT ? resolveDefault(operations) : event.detail;
        int operation = (type != null || source == null) && (operations & detail) == detail ? detail : DND.DROP_NONE;
        if (operation != DND.DROP_NONE) {
            event.detail = operation;
            event.dataType = type;
            event.data = source != null && type != null ? source.requestData(type) : null;
            target.deliver(DND.Drop, event);
            detail = event.detail == DND.DROP_DEFAULT ? resolveDefault(operations) : event.detail;
            operation = (operations & detail) == detail ? detail : DND.DROP_NONE;
        }
        entered = false;
        target.selectedDataType = null;
        target.selectedOperation = DND.DROP_NONE;
        target.notifyDragSourceOfDragEnd(operation);
    }

    private void start() {
        entered = true;
        answer.reset();
        target.selectedDataType = null;
        target.selectedOperation = DND.DROP_NONE;
    }

    private DNDEvent negotiationEvent(boolean entering, Event e) {
        DNDEvent event = new DNDEvent();
        event.widget = target.getApi();
        event.time = e.time;
        setDisplayLocation(event, e);
        event.item = item(e);
        event.operations = allowedOperations();
        event.dataTypes = target.computeMutualDataTypes(target.matchingDragSource());
        event.dataType = currentType(event.dataTypes);
        event.detail = entering ? initialOperation(event.operations) : target.selectedOperation;
        event.feedback = DND.FEEDBACK_SELECT;
        return event;
    }

    private Widget item(Event e) {
        return e.item != null ? e.item : target.resolveItem(e);
    }

    private TransferData currentType(TransferData[] offered) {
        if (target.selectedDataType != null)
            return target.selectedDataType;
        return offered.length > 0 ? offered[0] : null;
    }

    /** The operations both ends allow; a drag from outside SWT is bounded by the target alone. */
    private int allowedOperations() {
        int operations = target.getStyle() & (DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK);
        DartDragSource source = target.matchingDragSource();
        return source != null ? operations & source.getStyle() : operations;
    }

    private int initialOperation(int operations) {
        return (target.getStyle() & DND.DROP_DEFAULT) != 0 ? DND.DROP_DEFAULT : resolveDefault(operations);
    }

    private TransferData offeredType(TransferData[] offered, TransferData chosen) {
        if (offered.length == 0 && target.matchingDragSource() == null)
            return chosen;
        if (chosen == null)
            return null;
        for (TransferData type : offered) {
            if (type.type == chosen.type)
                return type;
        }
        return null;
    }

    /**
     * What the listener answered, as SWT keeps it: DROP_DEFAULT made concrete, then dropped when the
     * source does not allow it or no data type was agreed on.
     */
    private int acceptedOperation(boolean entering, int detail, int operations) {
        if (detail == DND.DROP_DEFAULT)
            detail = resolveDefault(operations);
        if (target.selectedDataType == null && target.matchingDragSource() != null)
            return DND.DROP_NONE;
        boolean allowed = entering ? (operations & detail) != 0 : (operations & detail) == detail;
        return allowed ? detail : DND.DROP_NONE;
    }

    private static int resolveDefault(int operations) {
        if ((operations & DND.DROP_MOVE) != 0) return DND.DROP_MOVE;
        if ((operations & DND.DROP_COPY) != 0) return DND.DROP_COPY;
        if ((operations & DND.DROP_LINK) != 0) return DND.DROP_LINK;
        return DND.DROP_NONE;
    }

    /** Flutter reports the pointer relative to the control; SWT drop events carry display coordinates. */
    private void setDisplayLocation(DNDEvent event, Event e) {
        Point onDisplay = target.control.toDisplay(e.x, e.y);
        event.x = onDisplay.x;
        event.y = onDisplay.y;
    }

    private void sendResult(int detail, int feedback) {
        int dataType = target.selectedDataType != null ? (int) target.selectedDataType.type : 0;
        answer.sendIfChanged(target, detail, feedback, dataType);
    }

    /** Tells a drag from another window what this target answered. */
    private void answerSource(int operation) {
        DartDragSource source = target.matchingDragSource();
        if (source != null)
            source.remoteDrag.answered(target.control, operation);
    }
}
