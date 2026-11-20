package dev.equo.swt.awt;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.e4.ui.workbench.renderers.swt.SashLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** An AWT drag carried out of a Swing island, as the SWT drop targets under the pointer see it. */
class AwtDragToSwtTest {

    private Display display;
    private Shell shell;
    private final List<String> events = new ArrayList<>();
    private final List<Object> dropped = new ArrayList<>();

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    private RecordingBridge bridge;

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
        shell = new Shell(display);
        shell.setBounds(0, 0, 400, 300);
        shell.open();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    /** A target filling the shell's top half that accepts {@code transfers}. */
    private Composite target(Transfer... transfers) {
        Composite control = new Composite(shell, SWT.NONE);
        control.setBounds(0, 0, 400, 150);
        return accepting(control, transfers);
    }

    /** {@code control}, given a drop target that accepts {@code transfers} and records its events. */
    private <C extends Control> C accepting(C control, Transfer... transfers) {
        DropTarget target = new DropTarget(control, DND.DROP_COPY | DND.DROP_MOVE);
        target.setTransfer(transfers);
        target.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetEvent event) {
                events.add("enter " + event.detail);
            }

            @Override
            public void dragLeave(DropTargetEvent event) {
                events.add("leave");
            }

            @Override
            public void drop(DropTargetEvent event) {
                events.add("drop " + event.detail);
                dropped.add(event.data);
            }
        });
        return control;
    }

    private Point inTarget(Composite control) {
        return control.toDisplay(20, 20);
    }

    /**
     * A part stack of the workbench's main area is painted inset and scaled inside its bounds: the
     * gap around it is the main area's, so a drag there is over no target.
     */
    @Test
    void aDragInTheGapAroundAWorkbenchPanelIsNotOverThePanel() {
        shell.setBounds(0, 0, 1400, 900);
        Composite main = new Composite(shell, SWT.NONE);
        main.setLayout(new SashLayout());
        main.setBounds(0, 40, 1400, 824);
        CTabFolder folder = accepting(new CTabFolder(main, SWT.NONE), TextTransfer.getInstance());
        folder.setBounds(353, 0, 730, 824);
        Point painted = folder.toDisplay(0, 0);
        AwtDragToSwt drag = new AwtDragToSwt(display, new StringSelection("Swing item"), DnDConstants.ACTION_COPY);

        assertThat(drag.over(new Point(painted.x - 2, painted.y - 2))).isEqualTo(DnDConstants.ACTION_NONE);
        assertThat(events).as("the gap is not the part's").isEmpty();
        assertThat(drag.over(new Point(painted.x + 20, painted.y + 20))).isEqualTo(DnDConstants.ACTION_COPY);
    }

    @Test
    void aTextDragDropsItsStringOnATextTarget() {
        Composite control = target(TextTransfer.getInstance());
        AwtDragToSwt drag = new AwtDragToSwt(display, new StringSelection("Swing item"), DnDConstants.ACTION_COPY);

        assertThat(drag.over(inTarget(control))).isEqualTo(DnDConstants.ACTION_COPY);
        assertThat(drag.drop(inTarget(control))).isEqualTo(DnDConstants.ACTION_COPY);

        assertThat(events).containsExactly("enter " + DND.DROP_COPY, "leave", "drop " + DND.DROP_COPY);
        assertThat(dropped).containsExactly("Swing item");
    }

    @Test
    void aFileListDragDropsItsPathsOnAFileTargetAsAMoveWhenBothAllowIt() {
        Composite control = target(FileTransfer.getInstance());
        File file = new File("dropped.txt").getAbsoluteFile();
        AwtDragToSwt drag = new AwtDragToSwt(display, files(file), DnDConstants.ACTION_COPY_OR_MOVE);

        assertThat(drag.drop(inTarget(control))).isEqualTo(DnDConstants.ACTION_MOVE);

        assertThat(events).containsExactly("enter " + DND.DROP_MOVE, "leave", "drop " + DND.DROP_MOVE);
        assertThat(dropped).hasSize(1);
        assertThat((String[]) dropped.get(0)).containsExactly(file.getAbsolutePath());
    }

    @Test
    void aTargetAcceptingNoneOfTheDragsTypesSeesNoEvent() {
        Composite control = target(FileTransfer.getInstance());
        AwtDragToSwt drag = new AwtDragToSwt(display, new StringSelection("Swing item"), DnDConstants.ACTION_COPY);

        assertThat(drag.over(inTarget(control))).isEqualTo(DnDConstants.ACTION_NONE);
        assertThat(drag.drop(inTarget(control))).isEqualTo(DnDConstants.ACTION_NONE);

        assertThat(events).isEmpty();
    }

    @Test
    void movingOffTheTargetLeavesIt() {
        Composite control = target(TextTransfer.getInstance());
        AwtDragToSwt drag = new AwtDragToSwt(display, new StringSelection("Swing item"), DnDConstants.ACTION_COPY);

        drag.over(inTarget(control));
        assertThat(drag.over(shell.toDisplay(20, 250))).isEqualTo(DnDConstants.ACTION_NONE);

        assertThat(events).containsExactly("enter " + DND.DROP_COPY, "leave");
    }

    @Test
    void aTargetThatTakesNoDataTypeRefusesTheDrag() {
        Composite control = target(TextTransfer.getInstance());
        dropTarget(control).addDropListener(new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetEvent event) {
                event.currentDataType = null;
            }

            @Override
            public void dragOver(DropTargetEvent event) {
                event.currentDataType = null;
            }
        });
        AwtDragToSwt drag = new AwtDragToSwt(display, new StringSelection("Swing item"), DnDConstants.ACTION_COPY);

        assertThat(drag.over(inTarget(control))).isEqualTo(DnDConstants.ACTION_NONE);
        assertThat(drag.drop(inTarget(control))).isEqualTo(DnDConstants.ACTION_NONE);

        assertThat(events).containsExactly("enter " + DND.DROP_COPY, "leave");
        assertThat(dropped).isEmpty();
    }

    @Test
    void aTargetThatDropsTheDataTypeInDropAcceptGetsNoDrop() {
        Composite control = target(TextTransfer.getInstance());
        dropTarget(control).addDropListener(new DropTargetAdapter() {
            @Override
            public void dropAccept(DropTargetEvent event) {
                event.currentDataType = null;
            }
        });
        AwtDragToSwt drag = new AwtDragToSwt(display, new StringSelection("Swing item"), DnDConstants.ACTION_COPY);

        assertThat(drag.over(inTarget(control))).isEqualTo(DnDConstants.ACTION_COPY);
        assertThat(drag.drop(inTarget(control))).isEqualTo(DnDConstants.ACTION_NONE);

        assertThat(events).containsExactly("enter " + DND.DROP_COPY, "leave");
        assertThat(dropped).isEmpty();
    }

    private static DropTarget dropTarget(Control control) {
        return (DropTarget) control.getData(DND.DROP_TARGET_KEY);
    }

    @Test
    void outsideTheIslandTheShellUnderThePointerDrawsThePictureAndTheCursorUntilTheDragLeaves() {
        Composite control = target(TextTransfer.getInstance());
        AwtDragToSwt drag = new AwtDragToSwt(display, new StringSelection("Swing item"), DnDConstants.ACTION_COPY);
        drag.picture(new BufferedImage(30, 10, BufferedImage.TYPE_INT_ARGB), new Rectangle(-5, -4, 30, 10));

        Point onTarget = inTarget(control);
        Point offTarget = shell.toDisplay(20, 250);
        drag.over(onTarget);
        drag.over(offTarget);
        drag.end();

        List<Object> sent = feedback();
        assertThat(sent).hasSize(4);
        @SuppressWarnings("unchecked")
        Map<String, Object> image = (Map<String, Object>) sent.get(0);
        assertThat(image).containsEntry("width", 30L).containsEntry("height", 10L).containsEntry("x", -5L)
                .containsEntry("y", -4L).containsKey("segments");
        assertThat(sent.get(1)).isEqualTo(step(4, onTarget, DND.DROP_COPY));
        assertThat(sent.get(2)).as("off the target: moved, with the no-drop cursor").isEqualTo(step(4, offTarget, DND.DROP_NONE));
        assertThat(sent.get(3)).as("hidden").isEqualTo(Map.of("detail", 2));
    }

    @Test
    void overTheIslandTheDragShowsWithTheSwingTargetsAnswerAndStaysShownAcrossTheEdge() {
        Composite control = target(TextTransfer.getInstance());
        AwtDragToSwt drag = new AwtDragToSwt(display, new StringSelection("Swing item"), DnDConstants.ACTION_COPY);
        drag.picture(new BufferedImage(30, 10, BufferedImage.TYPE_INT_ARGB), new Rectangle(-5, -4, 30, 10));

        Point start = shell.toDisplay(20, 200);
        Point overSwingTarget = shell.toDisplay(40, 200);
        Point onTarget = inTarget(control);
        drag.inside(start, DnDConstants.ACTION_NONE);
        drag.inside(overSwingTarget, DnDConstants.ACTION_COPY);
        drag.over(onTarget);
        drag.leave();
        drag.inside(start, DnDConstants.ACTION_NONE);
        drag.end();

        List<Object> sent = feedback();
        assertThat(sent).as("the picture once, then every step: " + sent).hasSize(6);
        assertThat(sent.get(0)).isInstanceOf(Map.class);
        assertThat(sent.subList(1, 6)).containsExactly(step(4, start, DND.DROP_NONE),
                step(4, overSwingTarget, DND.DROP_COPY), step(4, onTarget, DND.DROP_COPY),
                step(4, start, DND.DROP_NONE), Map.of("detail", 2));
        assertThat(events).as("back over the island, the SWT target sees the drag leave").containsExactly(
                "enter " + DND.DROP_COPY, "leave");
    }

    @Test
    void theDropHidesThePicture() {
        Composite control = target(TextTransfer.getInstance());
        AwtDragToSwt drag = new AwtDragToSwt(display, new StringSelection("Swing item"), DnDConstants.ACTION_COPY);

        drag.over(inTarget(control));
        drag.drop(inTarget(control));

        assertThat(feedback()).as("no picture: the cursor alone, then hidden")
                .containsExactly(step(4, inTarget(control), DND.DROP_COPY), Map.of("detail", 2));
    }

    /** What the shell was sent to draw the drag: its image, then the steps. */
    private List<Object> feedback() {
        String prefix = "Shell/" + FlutterBridge.id(shell) + "/";
        List<Object> sent = new ArrayList<>();
        for (var f : bridge.comm.sent) {
            if (f.event.equals(prefix + "DragDetect/dragImage") || f.event.equals(prefix + "DnD/remoteDrag")) sent.add(f.value);
        }
        return sent;
    }

    /** A step at the display point {@code at}, which the client gets relative to the shell. */
    private Map<String, Object> step(int phase, Point at, int operation) {
        Rectangle bounds = shell.getBounds();
        return Map.of("detail", phase, "x", at.x - bounds.x, "y", at.y - bounds.y, "feedback", operation);
    }

    private static Transferable files(File... files) {
        return new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[] {DataFlavor.javaFileListFlavor};
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return DataFlavor.javaFileListFlavor.equals(flavor);
            }

            @Override
            public Object getTransferData(DataFlavor flavor) {
                return Arrays.asList(files);
            }
        };
    }
}
