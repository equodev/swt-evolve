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
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.swt.dnd.TransferHelper;
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
 * <p>The drag offers the SWT transfers of the {@link DragSource} it started from, read when the drag
 * reaches the canvas, since a source may set them in its own {@code DragStart} listener. Each is
 * mapped to the AWT flavors the native clipboard formats map to; an application-defined transfer
 * reaches AWT through the flavors {@link SystemFlavorMap} associates with its type name, looked up
 * on every query as the native peer does, so a target that registers that mapping during the drag
 * sees it at once. The drag is delivered whether or not any flavor maps: the AWT target decides.
 * Only the transfer the drop negotiated carries data, and only at the drop: the SWT side hands over
 * nothing earlier.
 */
final class AwtDropBridge {

    private static final int SWT_OPERATIONS = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;

    private static final String DRAG_STATE_KEY = AwtDropBridge.class.getName() + ".dragState";

    /** One per {@link Display}: the drag in progress, every bridge, and the ones that can take it. */
    private static final class DragState {
        final Set<AwtDropBridge> all = ConcurrentHashMap.newKeySet();
        final Set<AwtDropBridge> active = ConcurrentHashMap.newKeySet();
        volatile WeakReference<DragSource> source = new WeakReference<>(null);

        DragSource source() {
            DragSource s = source.get();
            return s == null || s.isDisposed() ? null : s;
        }

        Transfer[] transfers() {
            DragSource s = source();
            Transfer[] transfers = s == null ? null : s.getTransfer();
            if (transfers == null) return new Transfer[0];
            List<Transfer> present = new ArrayList<>();
            for (Transfer transfer : transfers) {
                if (transfer != null) present.add(transfer);
            }
            return present.toArray(new Transfer[0]);
        }

        int operations() {
            DragSource s = source();
            return s == null ? SWT_OPERATIONS : s.getStyle() & SWT_OPERATIONS;
        }
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
        drag.all.add(this);
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
        drag.all.remove(this);
        drag.active.remove(this);
    }

    /**
     * Records which source the drag that is starting comes from. {@code SWT.DragDetect} is the only
     * point where the source is known to a party other than the source itself, and it is raised
     * before the drag reaches any drop target.
     */
    private static DragState dragState(Display display) {
        Object existing = display.getData(DRAG_STATE_KEY);
        if (existing instanceof DragState) return (DragState) existing;
        DragState state = new DragState();
        display.setData(DRAG_STATE_KEY, state);
        display.addFilter(SWT.DragDetect, e -> {
            Object source = e.widget == null ? null : e.widget.getData(DND.DRAG_SOURCE_KEY);
            if (!(source instanceof DragSource) || ((DragSource) source).isDisposed()) return;
            state.source = new WeakReference<>((DragSource) source);
            for (AwtDropBridge bridge : state.all) bridge.sync();
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

    /**
     * Gives the canvas an SWT drop target while the frame holds any AWT one. The frame only reports
     * those of lightweight components; a heavyweight one (an OpenGL canvas) registers with its own
     * peer, so the tree is searched for it too, when a drag starts.
     */
    private void sync() {
        if (canvas.isDisposed()) return;
        boolean wanted = !registered.isEmpty() || hasDropTarget(root);
        if (wanted && swtTarget == null) {
            swtTarget = new DropTarget(canvas, SWT_OPERATIONS);
            swtTarget.addDropListener(new Listener());
            drag.active.add(this);
            offerDragTransfers();
        } else if (!wanted && swtTarget != null) {
            drag.active.remove(this);
            swtTarget.dispose();
            swtTarget = null;
        }
    }

    private void offerDragTransfers() {
        if (swtTarget == null || swtTarget.isDisposed()) return;
        swtTarget.setTransfer(drag.transfers());
    }

    private final class Listener implements DropTargetListener {
        @Override
        public void dragEnter(DropTargetEvent e) {
            offerDragTransfers();
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
            Transfer[] offered = drag.transfers();
            if (frame == null || offered.length == 0) {
                e.detail = DND.DROP_NONE;
                return;
            }
            Point p = toFrame(e.x, e.y);
            Transferable data = new SwtTransferable(offered, transferFor(offered, e.currentDataType), e.data);
            int action = awtAction(() -> session.drop(frame, p, userAction(), sourceActions(), data));
            e.detail = toSwt(action);
        }
    }

    private void answer(DropTargetEvent e, boolean operationChanged) {
        Container frame = root;
        Transfer[] offered = drag.transfers();
        if (frame == null || offered.length == 0) {
            onAwt(() -> {
                session.exit();
                return null;
            });
            e.detail = DND.DROP_NONE;
            return;
        }
        TransferData type = negotiatedType(offered);
        if (type != null) e.currentDataType = type;
        Point p = toFrame(e.x, e.y);
        Transferable transferable = new SwtTransferable(offered, null, null);
        int action = awtAction(() -> session.motion(frame, p, userAction(), sourceActions(), transferable, operationChanged));
        e.detail = toSwt(action);
    }

    /**
     * The type whose data the drop will carry: the first transfer AWT has a flavor for, or the first
     * offered while none has one yet. Re-chosen on every event, because the target may map a native
     * to a flavor while the drag is over it.
     */
    private static TransferData negotiatedType(Transfer[] offered) {
        Transfer chosen = offered[0];
        for (Transfer transfer : offered) {
            if (flavorsFor(transfer).length > 0) {
                chosen = transfer;
                break;
            }
        }
        TransferData[] types = chosen.getSupportedTypes();
        return types.length > 0 ? types[0] : null;
    }

    /** Drop events carry display coordinates; the frame sits exactly over the canvas. */
    private Point toFrame(int x, int y) {
        org.eclipse.swt.graphics.Point local = canvas.toControl(x, y);
        return new Point(local.x, local.y);
    }

    private static Transfer transferFor(Transfer[] offered, TransferData type) {
        if (type == null) return null;
        for (Transfer transfer : offered) {
            if (transfer.isSupportedType(type)) return transfer;
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
        return toAwt(drag.operations());
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

    /** The flavors a transfer whose data is handed over as a Java object maps to, else null. */
    private static DataFlavor[] fixedFlavors(Transfer transfer) {
        if (transfer instanceof TextTransfer) return new DataFlavor[] {DataFlavor.stringFlavor};
        if (transfer instanceof FileTransfer) return new DataFlavor[] {DataFlavor.javaFileListFlavor};
        if (transfer instanceof URLTransfer) return new DataFlavor[] {urlFlavor(), DataFlavor.stringFlavor};
        if (transfer instanceof HTMLTransfer) return new DataFlavor[] {DataFlavor.allHtmlFlavor};
        return null;
    }

    /** The AWT flavors the native clipboard formats of {@code transfer} map to. */
    static DataFlavor[] flavorsFor(Transfer transfer) {
        DataFlavor[] fixed = fixedFlavors(transfer);
        if (fixed != null) return fixed;
        Set<DataFlavor> flavors = new LinkedHashSet<>();
        SystemFlavorMap map = (SystemFlavorMap) SystemFlavorMap.getDefaultFlavorMap();
        String[] names = TransferHelper.typeNames(transfer);
        if (names != null) {
            for (String name : names) {
                if (name != null) flavors.addAll(map.getFlavorsForNative(name));
            }
        }
        return flavors.toArray(new DataFlavor[0]);
    }

    /**
     * Under Evolve a {@code ByteArrayTransfer} leaves its native bytes in {@code TransferData.data};
     * the embedded backends' class has no such field, and this source set compiles against them too.
     */
    private static Object nativeData(TransferData type) {
        try {
            return TransferData.class.getField("data").get(type);
        } catch (ReflectiveOperationException | RuntimeException unavailable) {
            return null;
        }
    }

    /**
     * The SWT transfers of one drag seen from AWT. Flavors are looked up on every call; {@code data},
     * the drop's data for {@code dataTransfer}, is null until the drop.
     */
    static final class SwtTransferable implements Transferable {
        private final Transfer[] transfers;
        private final Transfer dataTransfer;
        private final Object data;

        SwtTransferable(Transfer[] transfers, Transfer dataTransfer, Object data) {
            this.transfers = transfers;
            this.dataTransfer = dataTransfer;
            this.data = data;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            Set<DataFlavor> flavors = new LinkedHashSet<>();
            for (Transfer transfer : transfers) flavors.addAll(Arrays.asList(flavorsFor(transfer)));
            return flavors.toArray(new DataFlavor[0]);
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return Arrays.asList(getTransferDataFlavors()).contains(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);
            if (data == null) throw new IOException("The drag's data is only available on drop");
            if (dataTransfer == null || !Arrays.asList(flavorsFor(dataTransfer)).contains(flavor)) {
                throw new IOException("The drop carries data for another flavor than " + flavor);
            }
            if (DataFlavor.javaFileListFlavor.equals(flavor) && data instanceof String[]) {
                List<File> files = new ArrayList<>();
                for (String path : (String[]) data) files.add(new File(path));
                return files;
            }
            if (urlFlavor().equals(flavor) && data instanceof String) return new URL((String) data);
            if (fixedFlavors(dataTransfer) == null) {
                byte[] bytes = nativeBytes(dataTransfer, data);
                if (bytes != null) {
                    try {
                        return fromNativeBytes(bytes, flavor);
                    } catch (IOException | UnsupportedFlavorException notInThatFormat) {
                    }
                }
            }
            Class<?> representation = flavor.getRepresentationClass();
            if (representation.isInstance(data)) return data;
            if (representation == InputStream.class && data instanceof byte[]) {
                return new ByteArrayInputStream((byte[]) data);
            }
            throw new UnsupportedFlavorException(flavor);
        }
    }

    /**
     * The bytes {@code transfer} hands the platform for {@code data}: what a native AWT drop target
     * reads for that clipboard format.
     */
    private static byte[] nativeBytes(Transfer transfer, Object data) {
        TransferData type;
        try {
            type = TransferHelper.toNative(transfer, data);
        } catch (RuntimeException notConvertible) {
            return null;
        }
        Object bytes = type == null ? null : nativeData(type);
        return bytes instanceof byte[] ? (byte[]) bytes : null;
    }

    /**
     * Builds {@code flavor}'s representation from a non-text native format's bytes, as AWT's
     * {@code DataTransferer.translateBytes} does for a native drop.
     */
    static Object fromNativeBytes(byte[] bytes, DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.getRepresentationClass() == byte[].class) return bytes;
        if (flavor.isRepresentationClassByteBuffer()) return construct(ByteBuffer.wrap(bytes), flavor, ByteBuffer.class);
        if (flavor.isRepresentationClassInputStream()) {
            return construct(new ByteArrayInputStream(bytes), flavor, InputStream.class);
        }
        if (flavor.isRepresentationClassSerializable()) {
            try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
                return in.readObject();
            } catch (ClassNotFoundException e) {
                throw new IOException(e.getMessage(), e);
            }
        }
        throw new UnsupportedFlavorException(flavor);
    }

    private static Object construct(Object arg, DataFlavor flavor, Class<?> argType) throws IOException {
        Class<?> representation = flavor.getRepresentationClass();
        if (representation == argType) return arg;
        try {
            return representation.getConstructor(argType).newInstance(arg);
        } catch (ReflectiveOperationException e) {
            throw new IOException("No public " + representation.getName() + "(" + argType.getName() + ")", e);
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

    static boolean hasDropTarget(Component c) {
        if (c == null) return false;
        if (c.getDropTarget() != null) return true;
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                if (hasDropTarget(child)) return true;
            }
        }
        return false;
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
