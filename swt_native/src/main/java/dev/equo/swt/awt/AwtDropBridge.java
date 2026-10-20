package dev.equo.swt.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.HTMLTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;

import sun.awt.AWTAccessor;

/**
 * Delivers an SWT drag over an embedding's canvas to the Swing {@link java.awt.dnd.DropTarget}s
 * inside it.
 *
 * <p>Natively the embedded frame is a real window and the OS delivers a drag from any toolkit to
 * the AWT drop targets registered on it. Here the frame is off-screen, so the canvas carries an SWT
 * {@link DropTarget} while the frame has any AWT drop target, and replays each SWT drag event as
 * the AWT event the native peer would have dispatched: to the deepest component under the pointer
 * that has a drop target, with enter/exit as that component changes.
 *
 * <p>The drag offers the SWT transfers of the {@link DragSource} it started from, each mapped to the
 * AWT flavors the native clipboard formats map to; an application-defined transfer reaches AWT
 * through the flavors {@link SystemFlavorMap} associates with its type name. Only the transfer the
 * drop negotiated carries data, and only at the drop: the SWT side hands over nothing earlier.
 */
final class AwtDropBridge {

    private static final int SWT_OPERATIONS = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;

    private static final String DRAG_STATE_KEY = AwtDropBridge.class.getName() + ".dragState";

    /** One per {@link Display}: what the drag in progress offers, and the bridges that can take it. */
    private static final class DragState {
        final Set<AwtDropBridge> active = ConcurrentHashMap.newKeySet();
        volatile Transfer[] transfers = new Transfer[0];
        volatile int operations = SWT_OPERATIONS;
    }

    private final Canvas canvas;
    private final Display display;
    private final DragState drag;
    private final Set<java.awt.dnd.DropTarget> registered = ConcurrentHashMap.newKeySet();
    private final DropSession session = new DropSession();
    private volatile Container root;
    private DropTarget swtTarget;

    AwtDropBridge(Canvas canvas) {
        this.canvas = canvas;
        this.display = canvas.getDisplay();
        this.drag = dragState(display);
    }

    void bind(Container root) {
        this.root = root;
    }

    void register(java.awt.dnd.DropTarget dt) {
        if (registered.add(dt)) syncSoon();
    }

    void unregister(java.awt.dnd.DropTarget dt) {
        if (registered.remove(dt)) syncSoon();
    }

    void dispose() {
        drag.active.remove(this);
    }

    /**
     * Records what the drag that is starting offers. {@code SWT.DragDetect} is the only point where
     * the source is known to a party other than the source itself, and it is raised before the drag
     * reaches any drop target.
     */
    private static DragState dragState(Display display) {
        Object existing = display.getData(DRAG_STATE_KEY);
        if (existing instanceof DragState) return (DragState) existing;
        DragState state = new DragState();
        display.setData(DRAG_STATE_KEY, state);
        display.addFilter(SWT.DragDetect, e -> {
            Object source = e.widget == null ? null : e.widget.getData(DND.DRAG_SOURCE_KEY);
            if (!(source instanceof DragSource) || ((DragSource) source).isDisposed()) return;
            DragSource dragSource = (DragSource) source;
            Transfer[] transfers = dragSource.getTransfer();
            state.transfers = transfers == null ? new Transfer[0] : transfers;
            state.operations = dragSource.getStyle() & SWT_OPERATIONS;
            for (AwtDropBridge bridge : state.active) bridge.offerDragTransfers();
        });
        return state;
    }

    private void syncSoon() {
        if (display.isDisposed()) return;
        try {
            display.asyncExec(this::sync);
        } catch (SWTException disposed) {
        }
    }

    private void sync() {
        if (canvas.isDisposed()) return;
        if (!registered.isEmpty() && swtTarget == null) {
            swtTarget = new DropTarget(canvas, SWT_OPERATIONS);
            swtTarget.addDropListener(new Listener());
            drag.active.add(this);
            offerDragTransfers();
        } else if (registered.isEmpty() && swtTarget != null) {
            drag.active.remove(this);
            swtTarget.dispose();
            swtTarget = null;
        }
    }

    private void offerDragTransfers() {
        if (swtTarget == null || swtTarget.isDisposed()) return;
        List<Transfer> mappable = new ArrayList<>();
        for (Transfer transfer : drag.transfers) {
            if (transfer != null && flavorsFor(transfer).length > 0) mappable.add(transfer);
        }
        swtTarget.setTransfer(mappable.toArray(new Transfer[0]));
    }

    private final class Listener implements DropTargetListener {
        @Override
        public void dragEnter(DropTargetEvent e) {
            answer(e, false);
        }

        @Override
        public void dragOver(DropTargetEvent e) {
            answer(e, false);
        }

        @Override
        public void dragOperationChanged(DropTargetEvent e) {
            answer(e, true);
        }

        @Override
        public void dragLeave(DropTargetEvent e) {
            onAwt(() -> {
                session.exit();
                return null;
            });
        }

        @Override
        public void dropAccept(DropTargetEvent e) {
        }

        @Override
        public void drop(DropTargetEvent e) {
            Container frame = root;
            Transfer transfer = transferFor(e.currentDataType);
            if (frame == null || transfer == null) {
                e.detail = DND.DROP_NONE;
                return;
            }
            Point p = toFrame(e.x, e.y);
            Transferable data = new SwtTransferable(transfer, e.data);
            int action = awtAction(() -> session.drop(frame, p, userAction(), sourceActions(), data));
            e.detail = toSwt(action);
        }
    }

    private void answer(DropTargetEvent e, boolean operationChanged) {
        Container frame = root;
        TransferData type = firstMappable(e.dataTypes);
        if (frame == null || type == null) {
            onAwt(() -> {
                session.exit();
                return null;
            });
            e.detail = DND.DROP_NONE;
            return;
        }
        e.currentDataType = type;
        Point p = toFrame(e.x, e.y);
        Transferable offered = new SwtTransferable(transferFor(type), null);
        int action = awtAction(() -> session.motion(frame, p, userAction(), sourceActions(), offered, operationChanged));
        e.detail = toSwt(action);
    }

    /** Drop events carry display coordinates; the frame sits exactly over the canvas. */
    private Point toFrame(int x, int y) {
        org.eclipse.swt.graphics.Point local = canvas.toControl(x, y);
        return new Point(local.x, local.y);
    }

    private TransferData firstMappable(TransferData[] types) {
        if (types == null) return null;
        for (TransferData type : types) {
            if (transferFor(type) != null) return type;
        }
        return null;
    }

    private Transfer transferFor(TransferData type) {
        if (type == null) return null;
        for (Transfer transfer : drag.transfers) {
            if (transfer != null && transfer.isSupportedType(type) && flavorsFor(transfer).length > 0) return transfer;
        }
        return null;
    }

    /** The action a drag with no modifier keys asks for, as the native peer derives it. */
    private int userAction() {
        int ops = sourceActions();
        if ((ops & DnDConstants.ACTION_MOVE) != 0) return DnDConstants.ACTION_MOVE;
        if ((ops & DnDConstants.ACTION_COPY) != 0) return DnDConstants.ACTION_COPY;
        if ((ops & DnDConstants.ACTION_LINK) != 0) return DnDConstants.ACTION_LINK;
        return DnDConstants.ACTION_NONE;
    }

    private int sourceActions() {
        return toAwt(drag.operations);
    }

    static int toAwt(int swtOperations) {
        int awt = DnDConstants.ACTION_NONE;
        if ((swtOperations & DND.DROP_COPY) != 0) awt |= DnDConstants.ACTION_COPY;
        if ((swtOperations & DND.DROP_MOVE) != 0) awt |= DnDConstants.ACTION_MOVE;
        if ((swtOperations & DND.DROP_LINK) != 0) awt |= DnDConstants.ACTION_LINK;
        return awt;
    }

    static int toSwt(int awtAction) {
        switch (awtAction) {
            case DnDConstants.ACTION_COPY: return DND.DROP_COPY;
            case DnDConstants.ACTION_MOVE: return DND.DROP_MOVE;
            case DnDConstants.ACTION_LINK: return DND.DROP_LINK;
            default: return DND.DROP_NONE;
        }
    }

    private static DataFlavor urlFlavor() {
        try {
            return new DataFlavor("application/x-java-url;class=java.net.URL");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    /** The AWT flavors the native clipboard formats of {@code transfer} map to. */
    static DataFlavor[] flavorsFor(Transfer transfer) {
        if (transfer instanceof TextTransfer) return new DataFlavor[] {DataFlavor.stringFlavor};
        if (transfer instanceof FileTransfer) return new DataFlavor[] {DataFlavor.javaFileListFlavor};
        if (transfer instanceof URLTransfer) return new DataFlavor[] {urlFlavor(), DataFlavor.stringFlavor};
        if (transfer instanceof HTMLTransfer) return new DataFlavor[] {DataFlavor.allHtmlFlavor};
        Set<DataFlavor> flavors = new LinkedHashSet<>();
        SystemFlavorMap map = (SystemFlavorMap) SystemFlavorMap.getDefaultFlavorMap();
        String[] names = typeNames(transfer);
        if (names != null) {
            for (String name : names) {
                if (name != null) flavors.addAll(map.getFlavorsForNative(name));
            }
        }
        return flavors.toArray(new DataFlavor[0]);
    }

    /** {@code Transfer.getTypeNames()} is protected: only subclasses are meant to call it. */
    private static String[] typeNames(Transfer transfer) {
        try {
            Method typeNames = Transfer.class.getDeclaredMethod("getTypeNames");
            typeNames.setAccessible(true);
            return (String[]) typeNames.invoke(transfer);
        } catch (ReflectiveOperationException | RuntimeException unavailable) {
            return null;
        }
    }

    /** One SWT transfer seen from AWT. {@code data} is null until the drop. */
    static final class SwtTransferable implements Transferable {
        private final Object data;
        private final DataFlavor[] flavors;

        SwtTransferable(Transfer transfer, Object data) {
            this.data = data;
            this.flavors = flavorsFor(transfer);
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return flavors.clone();
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            for (DataFlavor f : flavors) {
                if (f.equals(flavor)) return true;
            }
            return false;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);
            if (data == null) throw new IOException("The drag's data is only available on drop");
            if (DataFlavor.javaFileListFlavor.equals(flavor) && data instanceof String[]) {
                List<File> files = new ArrayList<>();
                for (String path : (String[]) data) files.add(new File(path));
                return files;
            }
            if (urlFlavor().equals(flavor) && data instanceof String) return new URL((String) data);
            Class<?> representation = flavor.getRepresentationClass();
            if (representation.isInstance(data)) return data;
            if (representation == InputStream.class && data instanceof byte[]) {
                return new ByteArrayInputStream((byte[]) data);
            }
            throw new UnsupportedFlavorException(flavor);
        }
    }

    private static int awtAction(Callable<Integer> task) {
        Integer action = onAwt(task);
        return action == null ? DnDConstants.ACTION_NONE : action;
    }

    /**
     * Runs on the AWT dispatch thread. Under Evolve that is the SWT thread itself (see
     * {@code EvolveDispatcherWrapper}), which keeps the drop answer synchronous with the SWT event.
     */
    private static <T> T onAwt(Callable<T> task) {
        try {
            if (EventQueue.isDispatchThread()) return task.call();
            Object[] result = new Object[1];
            EventQueue.invokeAndWait(() -> {
                try {
                    result[0] = task.call();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });
            @SuppressWarnings("unchecked") T t = (T) result[0];
            return t;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Finds the deepest visible component at {@code (x, y)} of {@code c} that has a drop target. */
    static Component dropTargetAt(Component c, int x, int y) {
        if (!c.isVisible() || !c.contains(x, y)) return null;
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                Component hit = dropTargetAt(child, x - child.getX(), y - child.getY());
                if (hit != null) return hit;
            }
        }
        return c.getDropTarget() != null ? c : null;
    }

    static Point toComponent(Container root, Component c, Point p) {
        int x = p.x, y = p.y;
        for (Component at = c; at != null && at != root; at = at.getParent()) {
            x -= at.getX();
            y -= at.getY();
        }
        return new Point(x, y);
    }

    /**
     * The AWT side of one drag over one embedding: the bookkeeping {@code SunDropTargetContextPeer}
     * keeps for a native drag, and the {@code DropTargetContextPeer} its drop target contexts talk to.
     * That interface lives in a package {@code java.desktop} does not export, so it is implemented
     * through a {@link Proxy} rather than requiring another export of every host application.
     */
    static final class DropSession implements InvocationHandler {
        private static final int STATUS_WAIT = 0, STATUS_ACCEPT = 1, STATUS_REJECT = -1;

        private final Object peer;
        private Component current;
        private java.awt.dnd.DropTarget currentDT;
        private DropTargetContext currentDTC;
        private Transferable transferable;
        private int currentDA, currentSA, currentA;
        private boolean dragRejected;
        private int dropStatus;
        private boolean dropComplete, dropSucceeded;

        DropSession() {
            Object p;
            try {
                Class<?> peerType = Class.forName("java.awt.dnd.peer.DropTargetContextPeer");
                p = Proxy.newProxyInstance(DropSession.class.getClassLoader(), new Class<?>[] {peerType}, this);
            } catch (ReflectiveOperationException | RuntimeException unavailable) {
                p = null;
            }
            peer = p;
        }

        int motion(Container root, Point p, int userAction, int sourceActions, Transferable t, boolean operationChanged) {
            Component target = dropTargetAt(root, p.x, p.y);
            if (target != current) {
                exit();
                if (target == null) return DnDConstants.ACTION_NONE;
                current = target;
                begin(target, userAction, sourceActions, t);
                dragRejected = false;
                try {
                    currentDT.dragEnter(
                            new DropTargetDragEvent(currentDTC, toComponent(root, target, p), currentDA, currentSA));
                } catch (Exception e) {
                    e.printStackTrace();
                    currentDA = DnDConstants.ACTION_NONE;
                }
                return currentDA;
            }
            if (target == null) return DnDConstants.ACTION_NONE;
            currentDA = userAction;
            currentSA = sourceActions;
            transferable = t;
            currentA = currentDT.getDefaultActions();
            try {
                DropTargetDragEvent event = new DropTargetDragEvent(currentDTC, toComponent(root, target, p), currentDA, currentSA);
                if (operationChanged) currentDT.dropActionChanged(event);
                else currentDT.dragOver(event);
                if (dragRejected) currentDA = DnDConstants.ACTION_NONE;
            } catch (Exception e) {
                e.printStackTrace();
                currentDA = DnDConstants.ACTION_NONE;
            }
            return currentDA;
        }

        int drop(Container root, Point p, int userAction, int sourceActions, Transferable t) {
            Component target = dropTargetAt(root, p.x, p.y);
            if (target != current) exit();
            if (target == null || !target.getDropTarget().isActive()) {
                exit();
                return DnDConstants.ACTION_NONE;
            }
            if (current == null) {
                current = target;
                begin(target, userAction, sourceActions, t);
            }
            currentDA = userAction;
            currentSA = sourceActions;
            transferable = t;
            dropStatus = STATUS_WAIT;
            dropComplete = false;
            dropSucceeded = false;
            try {
                currentDT.drop(new DropTargetDropEvent(currentDTC, toComponent(root, target, p), currentDA, currentSA, false));
            } catch (Exception e) {
                e.printStackTrace();
            }
            int result = dropStatus == STATUS_ACCEPT && (!dropComplete || dropSucceeded) ? currentDA : DnDConstants.ACTION_NONE;
            release();
            return result;
        }

        void exit() {
            if (current == null) return;
            try {
                currentDT.dragExit(new java.awt.dnd.DropTargetEvent(currentDTC));
            } catch (Exception e) {
                e.printStackTrace();
            }
            release();
        }

        private void begin(Component target, int userAction, int sourceActions, Transferable t) {
            currentDT = target.getDropTarget();
            currentDTC = currentDT.getDropTargetContext();
            currentDA = userAction;
            currentSA = sourceActions;
            currentA = currentDT.getDefaultActions();
            transferable = t;
            if (peer != null) setPeer(currentDTC, peer);
        }

        private void release() {
            if (currentDTC != null) AWTAccessor.getDropTargetContextAccessor().reset(currentDTC);
            current = null;
            currentDT = null;
            currentDTC = null;
            transferable = null;
        }

        private static void setPeer(DropTargetContext dtc, Object peer) {
            try {
                Object accessor = AWTAccessor.getDropTargetContextAccessor();
                Method set = AWTAccessor.DropTargetContextAccessor.class.getMethod("setDropTargetContextPeer",
                        DropTargetContext.class, Class.forName("java.awt.dnd.peer.DropTargetContextPeer"));
                set.invoke(accessor, dtc, peer);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }

        private static int mapOperation(int operation) {
            for (int op : new int[] {DnDConstants.ACTION_MOVE, DnDConstants.ACTION_COPY, DnDConstants.ACTION_LINK}) {
                if ((operation & op) == op) return op;
            }
            return DnDConstants.ACTION_NONE;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "setTargetActions": currentA = (Integer) args[0]; return null;
                case "getTargetActions": return currentA;
                case "getDropTarget": return currentDT;
                case "getTransferDataFlavors": return transferable == null ? new DataFlavor[0] : transferable.getTransferDataFlavors();
                case "getTransferable": return transferable;
                case "isTransferableJVMLocal": return false;
                case "acceptDrag":
                    currentDA = mapOperation((Integer) args[0]);
                    if (currentDA != DnDConstants.ACTION_NONE) dragRejected = false;
                    return null;
                case "rejectDrag":
                    currentDA = DnDConstants.ACTION_NONE;
                    dragRejected = true;
                    return null;
                case "acceptDrop":
                    int op = (Integer) args[0];
                    if (op == DnDConstants.ACTION_NONE) throw new IllegalArgumentException("invalid acceptDrop() action");
                    currentDA = currentA = mapOperation(op & currentSA);
                    dropStatus = STATUS_ACCEPT;
                    dropComplete = false;
                    return null;
                case "rejectDrop":
                    dropStatus = STATUS_REJECT;
                    currentDA = DnDConstants.ACTION_NONE;
                    return null;
                case "dropComplete":
                    dropComplete = true;
                    dropSucceeded = (Boolean) args[0];
                    return null;
                case "hashCode": return System.identityHashCode(proxy);
                case "equals": return proxy == args[0];
                case "toString": return "AwtDropBridge.DropSession";
                default: return null;
            }
        }
    }
}
