package dev.equo.swt.awt;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DartDropTarget;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetHelper;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.ForeignDragImage;
import org.eclipse.swt.dnd.HTMLTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.dnd.TransferHelper;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.DisplayInput;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;

/**
 * Delivers an AWT drag carried out of a Swing island to the SWT {@link DropTarget}s under the
 * pointer: the reverse of {@link AwtDropBridge}.
 *
 * <p>Natively the island's frame is a real window and the platform hands a drag that leaves it to
 * whatever is under the pointer. Here the drag runs in this JVM, so it is delivered from Java: to
 * the first control under the pointer, or ancestor of it, that has a {@code DropTarget}, offering
 * the target's transfers whose AWT flavors ({@link AwtDropBridge#flavorsFor}) the drag supports. A
 * target it offers none of sees no event, as on a platform. Over the island and past its edge, the
 * drag's picture and cursor are Evolve's ({@link ForeignDragImage}), as for an SWT drag.
 *
 * <p>One per drag, used on the Display's thread.
 */
public final class AwtDragToSwt {

    private final Display display;
    private final Transferable data;
    private final int operations;
    private final Foreign foreign = new Foreign();

    /** The target the drag is over, else null. */
    private DropTarget over;

    private int dropped = DND.DROP_NONE;

    private ForeignDragImage image = new ForeignDragImage(null, 0, 0, 0, 0);

    /** A drag offering {@code data}, whose source allows {@code awtSourceActions} ({@link DnDConstants}). */
    public AwtDragToSwt(Display display, Transferable data, int awtSourceActions) {
        this.display = display;
        this.data = data;
        int ops = DND.DROP_NONE;
        if ((awtSourceActions & DnDConstants.ACTION_COPY) != 0) ops |= DND.DROP_COPY;
        if ((awtSourceActions & DnDConstants.ACTION_MOVE) != 0) ops |= DND.DROP_MOVE;
        if ((awtSourceActions & DnDConstants.ACTION_LINK) != 0) ops |= DND.DROP_LINK;
        this.operations = ops;
    }

    /** The island control showing {@code frame}, or null when it is no live island's frame. */
    public static Control islandOf(Window frame) {
        return frame instanceof SwingIslandHost.IslandFrame ? ((SwingIslandHost.IslandFrame) frame).island : null;
    }

    /**
     * Where {@code inFrame}, a point in {@code frame}'s pixels, is on the display. The frame's
     * pixels scale to SWT's as {@link SwingIslandHost#placeFrame} scales its position.
     */
    public static Point toDisplay(Control island, Window frame, java.awt.Point inFrame) {
        double swingPerSwt = swingPerSwt(island, frame);
        return island.toDisplay((int) Math.round(inFrame.x / swingPerSwt), (int) Math.round(inFrame.y / swingPerSwt));
    }

    /** {@code inFrame}, a rectangle in {@code frame}'s pixels, in SWT points, as {@link #toDisplay} scales. */
    public static Rectangle toPoints(Control island, Window frame, java.awt.Rectangle inFrame) {
        double swingPerSwt = swingPerSwt(island, frame);
        return new Rectangle((int) Math.round(inFrame.x / swingPerSwt), (int) Math.round(inFrame.y / swingPerSwt),
                (int) Math.round(inFrame.width / swingPerSwt), (int) Math.round(inFrame.height / swingPerSwt));
    }

    private static double swingPerSwt(Control island, Window frame) {
        int width = island.getSize().x;
        return width > 0 && frame.getWidth() > 0 ? (double) frame.getWidth() / width : 1;
    }

    /** {@code picture} drawn at {@code width} by {@code height} pixels, as PNG bytes; null when it cannot be. */
    private static byte[] png(Image picture, int width, int height) {
        BufferedImage pixels = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = pixels.createGraphics();
        try {
            g.drawImage(picture, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            return ImageIO.write(pixels, "png", out) ? out.toByteArray() : null;
        } catch (IOException unwritable) {
            return null;
        }
    }

    /**
     * Draws the drag as {@code picture}, at {@code aroundPointer} from the pointer in points; without
     * one, only the cursor shows.
     */
    public void picture(Image picture, Rectangle aroundPointer) {
        int pixelWidth = picture.getWidth(null);
        int pixelHeight = picture.getHeight(null);
        if (pixelWidth <= 0 || pixelHeight <= 0) {
            pixelWidth = aroundPointer.width;
            pixelHeight = aroundPointer.height;
        }
        if (pixelWidth <= 0 || pixelHeight <= 0) return;
        byte[] png = png(picture, pixelWidth, pixelHeight);
        if (png == null) return;
        image = new ForeignDragImage(png, aroundPointer.width, aroundPointer.height, aroundPointer.x, aroundPointer.y);
    }

    /** The pointer is at {@code onDisplay}: the AWT action the target there accepts, or {@code ACTION_NONE}. */
    public int over(Point onDisplay) {
        int operation = negotiate(onDisplay);
        image.show(shellAt(onDisplay), onDisplay.x, onDisplay.y, operation);
        return AwtDropBridge.toAwt(operation);
    }

    /** The SWT operation the target at {@code onDisplay} accepts, or {@code DROP_NONE}. */
    private int negotiate(Point onDisplay) {
        DropTarget target = targetAt(onDisplay);
        if (target != over) {
            leaveTarget();
            if (target == null || foreign.types(target).length == 0) return DND.DROP_NONE;
            over = target;
            return send(DND.DragEnter, target, onDisplay);
        }
        return target == null ? DND.DROP_NONE : send(DND.DragOver, target, onDisplay);
    }

    /**
     * The pointer is at {@code onDisplay} over the island, whose drop target there accepts
     * {@code awtAction} ({@link DnDConstants}): the drag shows there with that answer's cursor.
     */
    public void inside(Point onDisplay, int awtAction) {
        image.show(shellAt(onDisplay), onDisplay.x, onDisplay.y, AwtDropBridge.toSwt(awtAction));
    }

    /** The pointer left SWT's controls for the island's: the target it was over sees it leave. */
    public void leave() {
        leaveTarget();
    }

    /** The drag ended without a drop on SWT's controls: its picture and cursor go. */
    public void end() {
        image.hide();
        leaveTarget();
    }

    private void leaveTarget() {
        DropTarget was = over;
        over = null;
        if (was != null && !was.isDisposed()) {
            Event e = new Event();
            e.time = (int) System.currentTimeMillis();
            deliver(was, DND.DragLeave, e);
        }
    }

    /** Released at {@code onDisplay}: the AWT action the drop performed, or {@code ACTION_NONE}. */
    public int drop(Point onDisplay) {
        image.hide();
        if (negotiate(onDisplay) == DND.DROP_NONE || over == null) {
            leaveTarget();
            return DnDConstants.ACTION_NONE;
        }
        DropTarget target = over;
        over = null;
        dropped = DND.DROP_NONE;
        deliver(target, DND.Drop, event(target, onDisplay));
        return AwtDropBridge.toAwt(dropped);
    }

    private int send(int type, DropTarget target, Point onDisplay) {
        deliver(target, type, event(target, onDisplay));
        if (target.isDisposed()) return DND.DROP_NONE;
        return ((DartDropTarget) target.getImpl())._selectedOperation();
    }

    /** Sends {@code type} to {@code target} with this drag as the one it negotiates with. */
    private void deliver(DropTarget target, int type, Event e) {
        DropTargetHelper.setForeignDrag(display, foreign);
        try {
            target.notifyListeners(type, e);
        } finally {
            DropTargetHelper.setForeignDrag(display, null);
        }
    }

    private static Event event(DropTarget target, Point onDisplay) {
        Control control = target.getControl();
        Point local = control.toControl(onDisplay);
        Event e = new Event();
        e.x = local.x;
        e.y = local.y;
        e.time = (int) System.currentTimeMillis();
        if (control instanceof Tree) e.item = ((Tree) control).getItem(local);
        else if (control instanceof Table) e.item = ((Table) control).getItem(local);
        return e;
    }

    /** The drop target of the topmost control under {@code onDisplay}, or of its nearest ancestor that has one. */
    private DropTarget targetAt(Point onDisplay) {
        for (Control c = DisplayInput.controlAt(display, onDisplay.x, onDisplay.y); c != null; c = c.getParent()) {
            Object target = c.getData("DropTarget");
            if (target instanceof DropTarget && !((DropTarget) target).isDisposed()) return (DropTarget) target;
        }
        return null;
    }

    /** The shell of the control under {@code onDisplay}, or null. */
    private Shell shellAt(Point onDisplay) {
        Control under = DisplayInput.controlAt(display, onDisplay.x, onDisplay.y);
        return under == null ? null : under.getShell();
    }

    private final class Foreign implements DropTargetHelper.ForeignDrag {

        @Override
        public int operations() {
            return operations;
        }

        @Override
        public TransferData[] types(DropTarget target) {
            List<TransferData> types = new ArrayList<>();
            for (Transfer transfer : target.getTransfer()) {
                if (transfer == null || supported(transfer) == null) continue;
                Collections.addAll(types, transfer.getSupportedTypes());
            }
            return types.toArray(new TransferData[0]);
        }

        @Override
        public Object data(DropTarget target, TransferData type) {
            if (type == null) return null;
            for (Transfer transfer : target.getTransfer()) {
                if (transfer != null && transfer.isSupportedType(type)) return valueFor(transfer, type);
            }
            return null;
        }

        @Override
        public void dropped(int operation) {
            dropped = operation;
        }
    }

    /** The first of {@code transfer}'s flavors the drag supports, or null. */
    private DataFlavor supported(Transfer transfer) {
        for (DataFlavor flavor : AwtDropBridge.flavorsFor(transfer)) {
            if (data.isDataFlavorSupported(flavor)) return flavor;
        }
        return null;
    }

    /** The drag's data as {@code transfer}'s Java value, or null when it cannot be read. */
    private Object valueFor(Transfer transfer, TransferData type) {
        DataFlavor flavor = supported(transfer);
        if (flavor == null) return null;
        try {
            Object value = data.getTransferData(flavor);
            if (transfer instanceof FileTransfer && value instanceof List) {
                List<String> paths = new ArrayList<>();
                for (Object file : (List<?>) value) paths.add(((File) file).getAbsolutePath());
                return paths.toArray(new String[0]);
            }
            if (transfer instanceof TextTransfer || transfer instanceof HTMLTransfer || transfer instanceof URLTransfer) {
                return value instanceof String || value instanceof URL ? value.toString() : null;
            }
            byte[] bytes = value instanceof byte[] ? (byte[]) value : value instanceof InputStream ? read((InputStream) value) : null;
            if (bytes == null || !setNativeData(type, bytes)) return value;
            return TransferHelper.toJava(transfer, type);
        } catch (Exception unreadable) {
            return null;
        }
    }

    private static byte[] read(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        for (int n; (n = in.read(buffer)) > 0; ) out.write(buffer, 0, n);
        return out.toByteArray();
    }

    /** {@code TransferData.data}, which only Evolve's class has: see {@code AwtDropBridge.nativeData}. */
    private static boolean setNativeData(TransferData type, byte[] bytes) {
        try {
            TransferData.class.getField("data").set(type, bytes);
            return true;
        } catch (ReflectiveOperationException | RuntimeException unavailable) {
            return false;
        }
    }
}
