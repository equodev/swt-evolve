package dev.equo.swt.awt;

import dev.equo.swt.harness.WidgetFlutterHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DartDropTarget;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import javax.swing.JPanel;

import sun.awt.AWTAccessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * A drag from an SWT control reaches the Swing {@link java.awt.dnd.DropTarget}s inside an
 * {@code SWT_AWT} embedding, as it does natively where the OS delivers the drag to the embedded
 * frame's window. The drag is fired through the same Java entry points Dart invokes, as in
 * {@code DndFlutterTest}, with the pointer relative to the drop target's control as Dart reports it.
 *
 * <p>Needs a real lightweight frame and AWT drop targets, so it is skipped where AWT runs headless.
 */
@Tag("flutter-it")
class SwingDropTargetFlutterTest {

    private WidgetFlutterHarness flutter;
    private Display display;
    private Shell shell;

    @BeforeEach
    void setUp() {
        assumeFalse(GraphicsEnvironment.isHeadless(), "AWT drop targets need a display");
        flutter = new WidgetFlutterHarness();
        flutter.init();
        display = new Display();
        shell = new Shell(display);
        shell.setLayout(new FillLayout());
        shell.setSize(600, 300);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        if (flutter != null) flutter.teardown();
        // The embedding routed AWT dispatch through this Display; left in place, every later
        // EventQueue task in this JVM is handed to a disposed Display and never runs.
        AWTAccessor.getEventQueueAccessor().setFwDispatcher(Toolkit.getDefaultToolkit().getSystemEventQueue(), null);
    }

    @Test
    @DisplayName("a Tree drag drops on a Swing DropTarget inside an embedded frame")
    void treeDragDropsOnSwingDropTarget() {
        Tree tree = new Tree(shell, SWT.NONE);
        TreeItem item = new TreeItem(tree, SWT.NONE);
        item.setText("Item A");
        tree.setSelection(item);
        DragSource dragSource = new DragSource(tree, DND.DROP_COPY | DND.DROP_MOVE);
        dragSource.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        dragSource.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = tree.getSelection()[0].getText();
            }
        });

        Composite embedded = new Composite(shell, SWT.EMBEDDED);
        Frame frame = SWT_AWT.new_Frame(embedded);
        List<String> entered = new ArrayList<>();
        List<Object> dropped = new ArrayList<>();
        JPanel view = new JPanel(new BorderLayout());
        new java.awt.dnd.DropTarget(view, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetDragEvent e) {
                entered.add(String.valueOf(e.isDataFlavorSupported(DataFlavor.stringFlavor)));
                e.acceptDrag(DnDConstants.ACTION_COPY);
            }

            @Override
            public void drop(DropTargetDropEvent e) {
                try {
                    e.acceptDrop(DnDConstants.ACTION_COPY);
                    dropped.add(e.getTransferable().getTransferData(DataFlavor.stringFlavor));
                    e.dropComplete(true);
                } catch (Exception ex) {
                    e.dropComplete(false);
                }
            }
        }, true);
        frame.add(view);
        frame.validate();
        shell.open();

        Control canvas = embedded.getChildren()[0];
        pumpUntil(() -> canvas.getData(DND.DROP_TARGET_KEY) != null && view.getWidth() == canvas.getSize().x);
        DropTarget dropTarget = (DropTarget) canvas.getData(DND.DROP_TARGET_KEY);
        assertThat(dropTarget).as("nothing on the embedding's surface takes a drag").isNotNull();

        tree.notifyListeners(SWT.DragDetect, new Event());
        Point size = canvas.getSize();
        Point center = new Point(size.x / 2, size.y / 2);
        DartDropTarget impl = (DartDropTarget) dropTarget.getImpl();
        impl.sendEvent(DND.DragEnter, at(center));
        impl.sendEvent(DND.DragOver, at(center));
        impl.sendEvent(DND.Drop, at(center));

        assertThat(entered).containsExactly("true");
        assertThat(dropped).containsExactly("Item A");
    }

    /**
     * A drop target on a heavyweight AWT {@link java.awt.Canvas} (an OpenGL viewer's surface) is
     * registered with that canvas's own peer, never with the lightweight frame, so the embedding
     * has to find it in its component tree.
     */
    @Test
    @DisplayName("a Tree drag drops on a DropTarget of a heavyweight canvas inside an embedded frame")
    void treeDragDropsOnHeavyweightCanvasDropTarget() {
        Tree tree = new Tree(shell, SWT.NONE);
        TreeItem item = new TreeItem(tree, SWT.NONE);
        item.setText("Item A");
        tree.setSelection(item);
        DragSource dragSource = new DragSource(tree, DND.DROP_COPY | DND.DROP_MOVE);
        dragSource.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        dragSource.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = tree.getSelection()[0].getText();
            }
        });

        Composite embedded = new Composite(shell, SWT.EMBEDDED);
        Frame frame = SWT_AWT.new_Frame(embedded);
        List<Object> dropped = new ArrayList<>();
        java.awt.Canvas glSurface = new java.awt.Canvas();
        new java.awt.dnd.DropTarget(glSurface, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter() {
            @Override
            public void dragOver(DropTargetDragEvent e) {
                e.acceptDrag(DnDConstants.ACTION_COPY);
            }

            @Override
            public void drop(DropTargetDropEvent e) {
                try {
                    e.acceptDrop(DnDConstants.ACTION_COPY);
                    dropped.add(e.getTransferable().getTransferData(DataFlavor.stringFlavor));
                    e.dropComplete(true);
                } catch (Exception ex) {
                    e.dropComplete(false);
                }
            }
        }, true);
        JPanel view = new JPanel(new BorderLayout());
        view.add(glSurface, BorderLayout.CENTER);
        frame.add(view);
        frame.validate();
        shell.open();

        Control canvas = embedded.getChildren()[0];
        pumpUntil(() -> glSurface.getWidth() == canvas.getSize().x);

        tree.notifyListeners(SWT.DragDetect, new Event());
        DropTarget dropTarget = (DropTarget) canvas.getData(DND.DROP_TARGET_KEY);
        assertThat(dropTarget).as("nothing on the embedding's surface takes a drag").isNotNull();
        Point size = canvas.getSize();
        Point center = new Point(size.x / 2, size.y / 2);
        DartDropTarget impl = (DartDropTarget) dropTarget.getImpl();
        impl.sendEvent(DND.DragEnter, at(center));
        impl.sendEvent(DND.DragOver, at(center));
        impl.sendEvent(DND.Drop, at(center));

        assertThat(dropped).containsExactly("Item A");
    }

    /**
     * The shape of a workbench view handing XML to a Swing view: the tree sets its transfers when the
     * drag starts, the transfer is application-defined, and the AWT listener only maps that native
     * to a flavor once the drag is over it, then reads the data back through the flavor's
     * {@code InputStream} constructor, the way AWT converts the native bytes.
     */
    @Test
    @DisplayName("an application transfer set at drag start drops as the flavor the Swing view maps to it")
    void applicationTransferSetAtDragStartDropsOnSwingDropTarget() {
        String nativeName = "EvolveTestPayload_" + System.nanoTime();
        PayloadTransfer transfer = new PayloadTransfer(nativeName);
        DataFlavor flavor = new DataFlavor(Payload.class, nativeName);

        Tree tree = new Tree(shell, SWT.NONE);
        TreeItem item = new TreeItem(tree, SWT.NONE);
        item.setText("Item A");
        tree.setSelection(item);
        DragSource dragSource = new DragSource(tree, DND.DROP_COPY | DND.DROP_MOVE);
        dragSource.setTransfer(new Transfer[] {new PayloadTransfer(nativeName + "_initial")});
        dragSource.addListener(DND.DragStart, e -> dragSource.setTransfer(new Transfer[] {transfer}));
        dragSource.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = new Payload("Item A");
            }
        });

        Composite embedded = new Composite(shell, SWT.EMBEDDED);
        Frame frame = SWT_AWT.new_Frame(embedded);
        List<Object> dropped = new ArrayList<>();
        JPanel view = new JPanel(new BorderLayout());
        new java.awt.dnd.DropTarget(view, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter() {
            @Override
            public void dragOver(DropTargetDragEvent e) {
                if (e.getCurrentDataFlavorsAsList().isEmpty()) {
                    SystemFlavorMap map = (SystemFlavorMap) SystemFlavorMap.getDefaultFlavorMap();
                    map.setNativesForFlavor(flavor, new String[] {nativeName});
                    map.setFlavorsForNative(nativeName, new DataFlavor[] {flavor});
                }
                if (e.isDataFlavorSupported(flavor)) e.acceptDrag(e.getDropAction());
                else e.rejectDrag();
            }

            @Override
            public void drop(DropTargetDropEvent e) {
                try {
                    e.acceptDrop(DnDConstants.ACTION_COPY);
                    dropped.add(((Payload) e.getTransferable().getTransferData(flavor)).text);
                    e.dropComplete(true);
                } catch (Exception ex) {
                    e.dropComplete(false);
                }
            }
        }, true);
        frame.add(view);
        frame.validate();
        shell.open();

        Control canvas = embedded.getChildren()[0];
        pumpUntil(() -> canvas.getData(DND.DROP_TARGET_KEY) != null && view.getWidth() == canvas.getSize().x);
        DropTarget dropTarget = (DropTarget) canvas.getData(DND.DROP_TARGET_KEY);
        assertThat(dropTarget).isNotNull();

        tree.notifyListeners(SWT.DragDetect, new Event());
        Point size = canvas.getSize();
        Point center = new Point(size.x / 2, size.y / 2);
        DartDropTarget impl = (DartDropTarget) dropTarget.getImpl();
        impl.sendEvent(DND.DragEnter, at(center));
        impl.sendEvent(DND.DragOver, at(center));
        impl.sendEvent(DND.Drop, at(center));

        assertThat(dropped).containsExactly("Item A");
    }

    @Test
    @DisplayName("an application transfer whose bytes are not in the flavor's format drops the object it was given")
    void applicationTransferDropsItsObjectWhenItsBytesAreNotInTheFlavorsFormat() throws Exception {
        String nativeName = "EvolveTestText_" + System.nanoTime();
        TextBytesTransfer transfer = new TextBytesTransfer(nativeName);
        DataFlavor flavor = new DataFlavor(String.class, nativeName);
        ((SystemFlavorMap) SystemFlavorMap.getDefaultFlavorMap()).setFlavorsForNative(nativeName, new DataFlavor[] {flavor});

        AwtDropBridge.SwtTransferable dropped =
                new AwtDropBridge.SwtTransferable(new Transfer[] {transfer}, transfer, "Item A");

        assertThat(dropped.getTransferData(flavor)).isEqualTo("Item A");
    }

    /** Stands for an application's transfer object: XML text, rebuilt from the native bytes. */
    public static final class Payload extends InputStream {
        final String text;

        Payload(String text) {
            this.text = text;
        }

        public Payload(InputStream in) throws IOException {
            this(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }

        @Override
        public int read() {
            return -1;
        }
    }

    private static final class PayloadTransfer extends ByteArrayTransfer {
        private final String name;
        private final int id;

        PayloadTransfer(String name) {
            this.name = name;
            this.id = registerType(name);
        }

        @Override
        protected String[] getTypeNames() {
            return new String[] {name};
        }

        @Override
        protected int[] getTypeIds() {
            return new int[] {id};
        }

        @Override
        public void javaToNative(Object object, TransferData transferData) {
            super.javaToNative(((Payload) object).text.getBytes(StandardCharsets.UTF_8), transferData);
        }

        @Override
        public Object nativeToJava(TransferData transferData) {
            byte[] bytes = (byte[]) super.nativeToJava(transferData);
            return bytes == null ? null : new Payload(new String(bytes, StandardCharsets.UTF_8));
        }
    }

    /** An application transfer of a String, handed to the platform as its UTF-8 bytes. */
    private static final class TextBytesTransfer extends ByteArrayTransfer {
        private final String name;
        private final int id;

        TextBytesTransfer(String name) {
            this.name = name;
            this.id = registerType(name);
        }

        @Override
        protected String[] getTypeNames() {
            return new String[] {name};
        }

        @Override
        protected int[] getTypeIds() {
            return new int[] {id};
        }

        @Override
        public void javaToNative(Object object, TransferData transferData) {
            super.javaToNative(((String) object).getBytes(StandardCharsets.UTF_8), transferData);
        }
    }

    private static Event at(Point p) {
        Event e = new Event();
        e.x = p.x;
        e.y = p.y;
        return e;
    }

    private void pumpUntil(BooleanSupplier done) {
        long deadline = System.currentTimeMillis() + 5000;
        while (!done.getAsBoolean() && System.currentTimeMillis() < deadline) {
            if (!display.readAndDispatch()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
