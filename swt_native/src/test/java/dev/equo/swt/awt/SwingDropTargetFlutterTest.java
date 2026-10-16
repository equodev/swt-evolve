package dev.equo.swt.awt;

import dev.equo.swt.harness.WidgetFlutterHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DartDropTarget;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
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
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
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
 * {@code DndFlutterTest}.
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
        Point center = canvas.toDisplay(size.x / 2, size.y / 2);
        DartDropTarget impl = (DartDropTarget) dropTarget.getImpl();
        impl.sendEvent(DND.DragEnter, at(center));
        impl.sendEvent(DND.DragOver, at(center));
        impl.sendEvent(DND.Drop, at(center));

        assertThat(entered).containsExactly("true");
        assertThat(dropped).containsExactly("Item A");
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
