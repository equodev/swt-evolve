package org.eclipse.swt.dnd;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The DragOvers the client reports while the drop listener is still busy with an earlier one merge into
 * the latest, as pointer moves do: a slow drop listener hears where the drag is, not where it was.
 */
@Tag("flutter-it")
class DragOverMergeFlutterTest {

    private RecordingBridge bridge;
    private Display display;
    private Tree tree;
    private DropTarget target;
    private final List<Integer> heard = new ArrayList<>();

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
        Shell shell = new Shell(display);
        shell.setBounds(0, 0, 300, 300);
        tree = new Tree(shell, SWT.NONE);
        tree.setBounds(0, 0, 200, 200);
        target = new DropTarget(tree, DND.DROP_MOVE);
        target.setTransfer(new Transfer[] { TextTransfer.getInstance() });
        target.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragOver(DropTargetEvent event) {
                heard.add(tree.toControl(event.x, event.y).y);
            }
        });
        shell.open();
        drain();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    private void drain() {
        while (display.readAndDispatch()) {
            // the drop target's asyncExec
        }
    }

    private void over(int y) {
        Event e = new Event();
        e.x = 10;
        e.y = y;
        bridge.comm.fireContaining("DropTarget/" + target.hashCode() + "/Drop/dragOver", e);
    }

    @Test
    void dragOversReportedBeforeTheFirstIsHandledReachTheListenerAsOneAtTheLast() {
        over(10);
        over(20);
        over(30);
        drain();
        assertThat(heard).containsExactly(30);
    }

    @Test
    void theMovesOverTheControlBetweenThemDoNotKeepDragOversApart() {
        List<Integer> moves = new ArrayList<>();
        tree.addListener(SWT.MouseMove, e -> moves.add(e.y));
        for (int y = 10; y <= 30; y += 10) {
            Event move = new Event();
            move.x = 10;
            move.y = y;
            bridge.comm.fireContaining(FlutterBridge.event((org.eclipse.swt.widgets.DartWidget) tree.getImpl(),
                    "MouseMove", "MouseMove"), move);
            over(y);
        }
        drain();
        assertThat(heard).containsExactly(30);
        assertThat(moves).containsExactly(30);
    }

    @Test
    void aDragOverReportedOnceTheOneBeforeIsHandledIsHeardOnItsOwn() {
        over(10);
        drain();
        over(20);
        over(30);
        drain();
        assertThat(heard).containsExactly(10, 30);
    }
}
