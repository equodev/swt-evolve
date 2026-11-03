package org.eclipse.swt.dnd;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.WidgetFlutterHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartControl;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack tests for the DND generalization, driven through a real Flutter
 * client via {@link WidgetFlutterHarness} — the same harness {@code RadioGroupFlutterTest}
 * uses. A real {@link Display} is required here (not {@code Mocks}): DND negotiation runs
 * through the standard SWT {@code notifyListeners}/{@code EventTable} event dispatch, which
 * {@code Mocks}' stub {@code Display} doesn't support.
 *
 * <p>{@link #dragAndDrop} fires the cycle by calling the same Java-side entry
 * points Dart would invoke via the FlutterBridge, mirroring how {@code RadioGroupFlutterTest}
 * drives {@code Button#selectRadio()} directly rather than simulating a real gesture. Cases
 * that only need to see what a {@code DropTargetListener}/{@code DragSourceListener} received
 * assert directly on Java state; cases that port an upstream snippet's end-to-end behavior
 * additionally call {@link WidgetFlutterHarness#flush()} and read back Flutter's own rendered
 * state via {@link WidgetFlutterHarness#queryState}.
 */
@Tag("flutter-it")
class DndFlutterTest {

    private WidgetFlutterHarness flutter;
    private Display display;
    private Shell shell;

    @BeforeEach
    void setUp() {
        flutter = new WidgetFlutterHarness();
        flutter.init();
        display = new Display();
        shell = new Shell(display);
        shell.setLayout(new FillLayout());
        shell.setSize(400, 300);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        if (flutter != null) flutter.teardown();
    }

    /**
     * Fires a full drag-and-drop cycle by calling the same Java-side entry points Dart would
     * invoke via the FlutterBridge ({@link DartDragSource#startDrag},
     * {@link DartDropTarget#sendEvent}), instead of a real Flutter gesture — the same idea as
     * driving {@code DartButton#selectRadio()} directly rather than simulating a tap.
     * {@code configure} is applied to a fresh {@link Event} for each of the three DropTarget
     * stages, so it should set whatever a real Dart client would report for the drop
     * position/target (index, itemId, x, y).
     */
    private static void dragAndDrop(DragSource source, DropTarget target, Consumer<Event> configure) {
        ((DartDragSource) source.getImpl()).startDrag(new Event());
        DartDropTarget impl = (DartDropTarget) target.getImpl();
        impl.sendEvent(DND.DragEnter, configuredEvent(configure));
        impl.sendEvent(DND.DragOver, configuredEvent(configure));
        impl.sendEvent(DND.Drop, configuredEvent(configure));
    }

    private static Event configuredEvent(Consumer<Event> configure) {
        Event event = new Event();
        configure.accept(event);
        return event;
    }

    @SuppressWarnings("unchecked")
    private Object renderedField(org.eclipse.swt.widgets.Widget w, String field) {
        Map<String, Object> resp = flutter.queryState(w);
        if (!Boolean.TRUE.equals(resp.get("found"))) return null;
        Map<String, Object> state = (Map<String, Object>) resp.get("state");
        return state == null ? null : state.get(field);
    }

    @Test
    @DisplayName("a drag its DragSource refused drops nothing where it is released")
    void refusedDragDropsNothing() {
        Label source = new Label(shell, SWT.NONE);
        Label target = new Label(shell, SWT.NONE);

        DragSource dragSource = new DragSource(source, DND.DROP_MOVE);
        dragSource.setTransfer(TextTransfer.getInstance());
        dragSource.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragStart(DragSourceEvent event) {
                event.doit = false;
            }
        });

        DropTarget dropTarget = new DropTarget(target, DND.DROP_MOVE);
        dropTarget.setTransfer(TextTransfer.getInstance());
        List<Integer> drops = new ArrayList<>();
        dropTarget.addDropListener(new DropTargetAdapter() {
            @Override
            public void drop(DropTargetEvent event) {
                drops.add(event.detail);
            }
        });

        shell.open();
        dragAndDrop(dragSource, dropTarget, e -> {
        });

        assertThat(drops).as("natively a refused drag never starts, so no DropTarget sees a drop").isEmpty();
    }

    @Test
    @DisplayName("the client is told a DragStart refusal in so many words")
    void dragStartRefusalCarriesItsDoit() {
        Label source = new Label(shell, SWT.NONE);
        DragSource dragSource = new DragSource(source, DND.DROP_MOVE);
        dragSource.setTransfer(TextTransfer.getInstance());
        boolean[] allow = {false};
        dragSource.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragStart(DragSourceEvent event) {
                event.doit = allow[0];
            }
        });
        shell.open();
        flutter.flush();

        List<Object> results = new ArrayList<>();
        FlutterBridge.setSendObserver((eventName, payload) -> {
            if (eventName.endsWith("DragDetect/dragStartResult"))
                results.add(dev.equo.swt.harness.GcFrames.decode(eventName, payload).value());
        });
        try {
            DartDragSource impl = (DartDragSource) dragSource.getImpl();
            impl.startDrag(new Event());
            allow[0] = true;
            impl.startDrag(new Event());
            impl.fireDragEnd(DND.DROP_NONE);
        } finally {
            FlutterBridge.setSendObserver(null);
        }

        assertThat(results).as("the client reads a result without doit as a refusal, so both say it")
                .containsExactly(Map.of("doit", false), Map.of("doit", true));
    }

    @Test
    @DisplayName("dragEnter only offers types the DropTarget actually accepts")
    void mutualDataTypesAreNegotiated() {
        Label source = new Label(shell, SWT.NONE);
        Label target = new Label(shell, SWT.NONE);

        DragSource dragSource = new DragSource(source, DND.DROP_COPY);
        dragSource.setTransfer(TextTransfer.getInstance(), URLTransfer.getInstance());

        DropTarget dropTarget = new DropTarget(target, DND.DROP_COPY);
        dropTarget.setTransfer(URLTransfer.getInstance());

        List<TransferData[]> seen = new ArrayList<>();
        dropTarget.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetEvent event) {
                seen.add(event.dataTypes);
            }
        });

        shell.open();
        dragAndDrop(dragSource, dropTarget, e -> {
        });

        assertThat(seen).hasSize(1);
        TransferData[] offered = seen.get(0);
        assertThat(offered).as("only the mutually-supported type should be offered")
                .allMatch(URLTransfer.getInstance()::isSupportedType);
    }

    @Test
    @DisplayName("drop resolves data from the control actually dragging, not the target's own DragSource")
    void dropResolvesDataFromTheActuallyDraggingSource() {
        Label source = new Label(shell, SWT.NONE);
        Label target = new Label(shell, SWT.NONE);

        DragSource dragSource = new DragSource(source, DND.DROP_COPY);
        dragSource.setTransfer(TextTransfer.getInstance());
        dragSource.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = "dragged from source";
            }
        });

        // The target has no DragSource of its own: matchingDragSource() must not accidentally
        // resolve to it (an earlier bug assumed source and target always share a control).
        DropTarget dropTarget = new DropTarget(target, DND.DROP_COPY);
        dropTarget.setTransfer(TextTransfer.getInstance());

        List<Object> received = new ArrayList<>();
        dropTarget.addDropListener(new DropTargetAdapter() {
            @Override
            public void drop(DropTargetEvent event) {
                received.add(event.data);
            }
        });

        shell.open();
        dragAndDrop(dragSource, dropTarget, e -> {
        });

        assertThat(received).containsExactly("dragged from source");
    }

    @Test
    @DisplayName("drop resolves a nested TreeItem by itemId, not just top-level index")
    void dropResolvesNestedTreeItemByItemId() {
        Composite composite = new Composite(shell, SWT.NONE);
        Tree tree = new Tree(composite, SWT.NONE);
        TreeItem parent = new TreeItem(tree, SWT.NONE);
        parent.setText("parent");
        TreeItem child = new TreeItem(parent, SWT.NONE);
        child.setText("child");
        long childId = FlutterBridge.id(child);

        DragSource dragSource = new DragSource(tree, DND.DROP_MOVE);
        dragSource.setTransfer(TextTransfer.getInstance());
        dragSource.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = "moved item";
            }
        });

        DropTarget dropTarget = new DropTarget(tree, DND.DROP_MOVE);
        dropTarget.setTransfer(TextTransfer.getInstance());

        List<Object> resolvedItems = new ArrayList<>();
        dropTarget.addDropListener(new DropTargetAdapter() {
            @Override
            public void drop(DropTargetEvent event) {
                resolvedItems.add(event.item);
            }
        });

        shell.open();
        dragAndDrop(dragSource, dropTarget, e -> e.itemId = childId);

        assertThat(resolvedItems).containsExactly(child);
    }

    @Test
    @DisplayName("a tab dropped on another CTabFolder resolves the CTabItem under the cursor there")
    void dropResolvesCTabItemOnAnotherFolder() {
        CTabFolder from = new CTabFolder(shell, SWT.NONE);
        CTabItem moving = new CTabItem(from, SWT.NONE);
        moving.setText("moving");

        CTabFolder to = new CTabFolder(shell, SWT.NONE);
        CTabItem first = new CTabItem(to, SWT.NONE);
        first.setText("first");
        CTabItem second = new CTabItem(to, SWT.NONE);
        second.setText("second");

        DragSource dragSource = new DragSource(from, DND.DROP_MOVE);
        dragSource.setTransfer(TextTransfer.getInstance());
        dragSource.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = moving.getText();
            }
        });

        DropTarget dropTarget = new DropTarget(to, DND.DROP_MOVE);
        dropTarget.setTransfer(TextTransfer.getInstance());

        List<Object> resolvedItems = new ArrayList<>();
        List<Object> droppedData = new ArrayList<>();
        dropTarget.addDropListener(new DropTargetAdapter() {
            @Override
            public void drop(DropTargetEvent event) {
                resolvedItems.add(event.item);
                droppedData.add(event.data);
            }
        });

        shell.open();
        dragAndDrop(dragSource, dropTarget, e -> e.itemId = FlutterBridge.id(second));

        assertThat(resolvedItems).as("event.item must be the target folder's own CTabItem")
                .containsExactly(second);
        assertThat(droppedData).as("the source folder's dragSetData must reach the other folder")
                .containsExactly("moving");
    }

    @Test
    @DisplayName("drop reports the operation negotiated during dragEnter/dragOver, not a fixed default")
    void dropDetailReflectsTheNegotiatedOperation() {
        Label source = new Label(shell, SWT.NONE);
        Label target = new Label(shell, SWT.NONE);

        DragSource dragSource = new DragSource(source, DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK);
        dragSource.setTransfer(TextTransfer.getInstance());
        dragSource.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = "text";
            }
        });

        DropTarget dropTarget = new DropTarget(target, DND.DROP_DEFAULT | DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK);
        dropTarget.setTransfer(TextTransfer.getInstance());
        dropTarget.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetEvent event) {
                if (event.detail == DND.DROP_DEFAULT) event.detail = DND.DROP_LINK;
            }
        });

        List<Integer> details = new ArrayList<>();
        dropTarget.addDropListener(new DropTargetAdapter() {
            @Override
            public void drop(DropTargetEvent event) {
                details.add(event.detail);
            }
        });

        shell.open();
        dragAndDrop(dragSource, dropTarget, e -> {
        });

        assertThat(details).as("drop must see the operation resolved during negotiation, not a hardcoded default")
                .containsExactly(DND.DROP_LINK);
    }

    /** The client's word that the drag it started is over, however it ended, as it sends it. */
    private void clientEndsDrag(DragSource source) {
        ((DartDragSource) source.getImpl())._onClientEvent("Drag/dragFinished", new Event());
        while (display.readAndDispatch()) {
        }
    }

    private static List<DragSourceEvent> recordDragEnds(DragSource source) {
        List<DragSourceEvent> ends = new ArrayList<>();
        source.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragFinished(DragSourceEvent event) {
                ends.add(event);
            }
        });
        return ends;
    }

    @Test
    @DisplayName("a drag dropped on nothing ends with DROP_NONE and doit false, as natively")
    void aDragDroppedOnNothingEndsWithNothingDone() {
        Label source = new Label(shell, SWT.NONE);
        DragSource dragSource = new DragSource(source, DND.DROP_MOVE | DND.DROP_COPY);
        dragSource.setTransfer(TextTransfer.getInstance());
        List<DragSourceEvent> ends = recordDragEnds(dragSource);
        shell.open();

        ((DartDragSource) dragSource.getImpl()).startDrag(new Event());
        clientEndsDrag(dragSource);

        assertThat(ends).hasSize(1);
        assertThat(ends.get(0).detail).isEqualTo(DND.DROP_NONE);
        assertThat(ends.get(0).doit).isFalse();
    }

    @Test
    @DisplayName("a drag a drop ended is not ended again when the client says it is over")
    void aDroppedDragEndsOnce() {
        Label source = new Label(shell, SWT.NONE);
        Label target = new Label(shell, SWT.NONE);
        DragSource dragSource = new DragSource(source, DND.DROP_MOVE);
        dragSource.setTransfer(TextTransfer.getInstance());
        dragSource.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = "text";
            }
        });
        List<DragSourceEvent> ends = recordDragEnds(dragSource);
        DropTarget dropTarget = new DropTarget(target, DND.DROP_MOVE);
        dropTarget.setTransfer(TextTransfer.getInstance());
        shell.open();

        dragAndDrop(dragSource, dropTarget, e -> {
        });
        clientEndsDrag(dragSource);

        assertThat(ends).hasSize(1);
        assertThat(ends.get(0).detail).isEqualTo(DND.DROP_MOVE);
        assertThat(ends.get(0).doit).isTrue();
    }

    @Test
    @DisplayName("a drag its DragStart refused never ends, as natively, even when the client says it is over")
    void aRefusedDragNeverEnds() {
        Label source = new Label(shell, SWT.NONE);
        DragSource dragSource = new DragSource(source, DND.DROP_MOVE);
        dragSource.setTransfer(TextTransfer.getInstance());
        dragSource.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragStart(DragSourceEvent event) {
                event.doit = false;
            }
        });
        List<DragSourceEvent> ends = recordDragEnds(dragSource);
        shell.open();

        ((DartDragSource) dragSource.getImpl()).startDrag(new Event());
        clientEndsDrag(dragSource);

        assertThat(ends).as("native DragSource.drag returns right after a refused DragStart").isEmpty();
    }

    @Test
    @DisplayName("a control tells the client which DragSource its drags end on")
    void aControlNamesItsDragSource() {
        Label source = new Label(shell, SWT.NONE);
        assertThat(((DartControl) source.getImpl()).getDragSourceId()).isNull();
        DragSource dragSource = new DragSource(source, DND.DROP_MOVE);
        assertThat(((DartControl) source.getImpl()).getDragSourceId())
                .isEqualTo(FlutterBridge.id(dragSource.getImpl()));
    }

    @Test
    @DisplayName("drop resolves DROP_DEFAULT to a concrete operation when the app never overrides it (Snippet91)")
    void dropResolvesDefaultOperationWhenAppNeverOverridesIt() {
        // Snippet91's dragOver only ever sets event.feedback, never event.detail — matching
        // real SWT semantics (DND.java: "If no value is chosen, the default operation for the
        // platform will be selected"), DROP_DEFAULT must resolve to a real operation by the
        // time drop()/dragFinished() see it. Before this fix, DROP_DEFAULT leaked straight
        // through to dragFinished, so `event.detail == DND.DROP_MOVE` was false, the dragged
        // TreeItem was never disposed, and the tree ended up with a duplicate item.
        Label source = new Label(shell, SWT.NONE);
        Label target = new Label(shell, SWT.NONE);

        DragSource dragSource = new DragSource(source, DND.DROP_MOVE | DND.DROP_COPY);
        dragSource.setTransfer(TextTransfer.getInstance());
        dragSource.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = "text";
            }
        });
        List<Integer> finishedDetails = new ArrayList<>();
        dragSource.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragFinished(DragSourceEvent event) {
                finishedDetails.add(event.detail);
            }
        });

        DropTarget dropTarget = new DropTarget(target, DND.DROP_MOVE | DND.DROP_COPY);
        dropTarget.setTransfer(TextTransfer.getInstance());
        // No dragEnter/dragOver override at all — event.detail is left at DND.DROP_DEFAULT
        // through the whole negotiation, exactly like Snippet91's DropTargetAdapter.

        List<Integer> dropDetails = new ArrayList<>();
        dropTarget.addDropListener(new DropTargetAdapter() {
            @Override
            public void drop(DropTargetEvent event) {
                dropDetails.add(event.detail);
            }
        });

        shell.open();
        dragAndDrop(dragSource, dropTarget, e -> {
        });

        assertThat(dropDetails).as("drop must never see the raw, unresolved DND.DROP_DEFAULT")
                .containsExactly(DND.DROP_MOVE);
        assertThat(finishedDetails).as("dragFinished must see the same resolved operation as drop")
                .containsExactly(DND.DROP_MOVE);
    }

    @Test
    @DisplayName("a custom ByteArrayTransfer subclass round-trips real bytes")
    void byteArrayTransferRoundTripsRealBytes() {
        Label source = new Label(shell, SWT.NONE);
        Label target = new Label(shell, SWT.NONE);

        DragSource dragSource = new DragSource(source, DND.DROP_COPY);
        dragSource.setTransfer(CustomTransfer.INSTANCE);
        dragSource.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = "custom payload";
            }
        });

        DropTarget dropTarget = new DropTarget(target, DND.DROP_COPY);
        dropTarget.setTransfer(CustomTransfer.INSTANCE);

        List<Object> received = new ArrayList<>();
        dropTarget.addDropListener(new DropTargetAdapter() {
            @Override
            public void drop(DropTargetEvent event) {
                received.add(event.data);
            }
        });

        shell.open();
        dragAndDrop(dragSource, dropTarget, e -> {
        });

        assertThat(received).containsExactly("custom payload");
    }

    @Test
    @DisplayName("Each drag event starts from FEEDBACK_SELECT, not the previous event's answer")
    void dragEventsStartFromSelectFeedback() {
        Tree tree = new Tree(shell, SWT.BORDER);
        new TreeItem(tree, SWT.NONE).setText("node");
        DragSource source = new DragSource(tree, DND.DROP_MOVE);
        source.setTransfer(TextTransfer.getInstance());
        DropTarget target = new DropTarget(tree, DND.DROP_MOVE);
        target.setTransfer(TextTransfer.getInstance());
        List<Integer> seen = new ArrayList<>();
        target.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetEvent event) {
                seen.add(event.feedback);
                event.feedback = DND.FEEDBACK_INSERT_AFTER;
            }

            @Override
            public void dragOver(DropTargetEvent event) {
                seen.add(event.feedback);
            }
        });
        shell.open();
        flutter.show(shell);

        ((DartDragSource) source.getImpl()).startDrag(new Event());
        DartDropTarget impl = (DartDropTarget) target.getImpl();
        impl.sendEvent(DND.DragEnter, new Event());
        impl.sendEvent(DND.DragOver, new Event());

        assertThat(seen).containsExactly(DND.FEEDBACK_SELECT, DND.FEEDBACK_SELECT);
    }

    private DragSource textSource(org.eclipse.swt.widgets.Control control, int operations, String data) {
        DragSource source = new DragSource(control, operations);
        source.setTransfer(TextTransfer.getInstance());
        source.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = data;
            }
        });
        return source;
    }

    @Test
    @DisplayName("drag events offer the source's operations ANDed with the target's, and DROP_DEFAULT resolves to MOVE")
    void negotiationOffersTheMutualOperationsAndMove() {
        Label source = new Label(shell, SWT.NONE);
        Label target = new Label(shell, SWT.NONE);
        DragSource dragSource = textSource(source, DND.DROP_COPY | DND.DROP_MOVE, "text");
        DropTarget dropTarget = new DropTarget(target, DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK);
        dropTarget.setTransfer(TextTransfer.getInstance());
        List<String> seen = new ArrayList<>();
        dropTarget.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetEvent event) {
                seen.add("enter " + event.detail + "/" + event.operations);
            }

            @Override
            public void dragOver(DropTargetEvent event) {
                seen.add("over " + event.detail + "/" + event.operations);
            }

            @Override
            public void drop(DropTargetEvent event) {
                seen.add("drop " + event.detail + "/" + event.operations + " feedback " + event.feedback);
            }
        });

        shell.open();
        dragAndDrop(dragSource, dropTarget, e -> {
        });

        int mutual = DND.DROP_COPY | DND.DROP_MOVE;
        assertThat(seen).containsExactly(
                "enter " + DND.DROP_MOVE + "/" + mutual,
                "over " + DND.DROP_MOVE + "/" + mutual,
                "drop " + DND.DROP_MOVE + "/" + mutual + " feedback " + DND.FEEDBACK_SELECT);
    }

    @Test
    @DisplayName("a drag that left a target leaves nothing of its negotiation to the next drag over it")
    void aDragAfterALeaveNegotiatesAfresh() {
        Label source = new Label(shell, SWT.NONE);
        Label target = new Label(shell, SWT.NONE);
        DragSource dragSource = textSource(source, DND.DROP_COPY | DND.DROP_MOVE, "text");
        DropTarget dropTarget = new DropTarget(target, DND.DROP_COPY | DND.DROP_MOVE);
        dropTarget.setTransfer(TextTransfer.getInstance());
        List<Integer> overs = new ArrayList<>();
        dropTarget.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetEvent event) {
                event.detail = DND.DROP_COPY;
            }

            @Override
            public void dragOver(DropTargetEvent event) {
                overs.add(event.detail);
            }
        });
        shell.open();
        DartDropTarget impl = (DartDropTarget) dropTarget.getImpl();

        ((DartDragSource) dragSource.getImpl()).startDrag(new Event());
        impl.sendEvent(DND.DragEnter, new Event());
        impl.sendEvent(DND.DragOver, new Event());
        impl.sendEvent(DND.DragLeave, new Event());
        clientEndsDrag(dragSource);
        // The second drag's DragEnter never arrives.
        ((DartDragSource) dragSource.getImpl()).startDrag(new Event());
        impl.sendEvent(DND.DragOver, new Event());

        assertThat(overs).as("the second drag starts from the default operation, not the first drag's COPY")
                .containsExactly(DND.DROP_COPY, DND.DROP_MOVE);
    }

    @Test
    @DisplayName("a refused drag that is over leaves the next drop alone")
    void aDropAfterARefusedDragIsDelivered() {
        Label source = new Label(shell, SWT.NONE);
        Label target = new Label(shell, SWT.NONE);
        DragSource dragSource = new DragSource(source, DND.DROP_MOVE);
        dragSource.setTransfer(TextTransfer.getInstance());
        dragSource.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragStart(DragSourceEvent event) {
                event.doit = false;
            }
        });
        DropTarget dropTarget = new DropTarget(target, DND.DROP_MOVE);
        dropTarget.setTransfer(TextTransfer.getInstance());
        List<Integer> drops = new ArrayList<>();
        dropTarget.addDropListener(new DropTargetAdapter() {
            @Override
            public void drop(DropTargetEvent event) {
                drops.add(event.detail);
            }
        });
        shell.open();

        ((DartDragSource) dragSource.getImpl()).startDrag(new Event());
        clientEndsDrag(dragSource);
        DartDropTarget impl = (DartDropTarget) dropTarget.getImpl();
        impl.sendEvent(DND.DragEnter, new Event());
        impl.sendEvent(DND.Drop, new Event());

        assertThat(drops).as("a drop from outside the application, after the refused drag ended")
                .containsExactly(DND.DROP_MOVE);
    }

    @Test
    @DisplayName("a drop is announced by DragLeave, then DropAccept, then Drop")
    void dropIsPrecededByDragLeaveThenDropAccept() {
        Label source = new Label(shell, SWT.NONE);
        Label target = new Label(shell, SWT.NONE);
        DragSource dragSource = textSource(source, DND.DROP_MOVE, "text");
        DropTarget dropTarget = new DropTarget(target, DND.DROP_MOVE);
        dropTarget.setTransfer(TextTransfer.getInstance());
        List<String> order = new ArrayList<>();
        dropTarget.addDropListener(new DropTargetListener() {
            public void dragEnter(DropTargetEvent event) { order.add("DragEnter"); }
            public void dragLeave(DropTargetEvent event) { order.add("DragLeave"); }
            public void dragOperationChanged(DropTargetEvent event) { order.add("DragOperationChanged"); }
            public void dragOver(DropTargetEvent event) { order.add("DragOver"); }
            public void drop(DropTargetEvent event) { order.add("Drop"); }
            public void dropAccept(DropTargetEvent event) { order.add("DropAccept " + event.detail + "/" + event.operations); }
        });

        shell.open();
        dragAndDrop(dragSource, dropTarget, e -> {
        });

        assertThat(order).containsExactly("DragEnter", "DragOver", "DragLeave", "DropAccept " + DND.DROP_MOVE + "/" + DND.DROP_MOVE, "Drop");
    }

    @Test
    @DisplayName("a drop the DropAccept listener refuses never reaches Drop, and the source sees DROP_NONE")
    void dropRefusedInDropAcceptIsNotDelivered() {
        Label source = new Label(shell, SWT.NONE);
        Label target = new Label(shell, SWT.NONE);
        DragSource dragSource = textSource(source, DND.DROP_MOVE, "text");
        List<Integer> finished = new ArrayList<>();
        dragSource.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragFinished(DragSourceEvent event) {
                finished.add(event.detail);
            }
        });
        DropTarget dropTarget = new DropTarget(target, DND.DROP_MOVE);
        dropTarget.setTransfer(TextTransfer.getInstance());
        List<Integer> drops = new ArrayList<>();
        dropTarget.addDropListener(new DropTargetAdapter() {
            @Override
            public void dropAccept(DropTargetEvent event) {
                event.detail = DND.DROP_NONE;
            }

            @Override
            public void drop(DropTargetEvent event) {
                drops.add(event.detail);
            }
        });

        shell.open();
        dragAndDrop(dragSource, dropTarget, e -> {
        });

        assertThat(drops).isEmpty();
        assertThat(finished).containsExactly(DND.DROP_NONE);
    }

    @Test
    @DisplayName("each drag starts from one of its own data types, not the previous drag's")
    void eachDragNegotiatesItsOwnDataType() {
        Label first = new Label(shell, SWT.NONE);
        Label second = new Label(shell, SWT.NONE);
        Label target = new Label(shell, SWT.NONE);
        DragSource firstSource = textSource(first, DND.DROP_MOVE, "text");
        DragSource secondSource = new DragSource(second, DND.DROP_MOVE);
        secondSource.setTransfer(URLTransfer.getInstance());
        secondSource.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = "https://example.org";
            }
        });
        DropTarget dropTarget = new DropTarget(target, DND.DROP_MOVE);
        dropTarget.setTransfer(TextTransfer.getInstance(), URLTransfer.getInstance());
        List<String> dropped = new ArrayList<>();
        dropTarget.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetEvent event) {
                dropped.add("enter " + URLTransfer.getInstance().isSupportedType(event.currentDataType));
            }

            @Override
            public void drop(DropTargetEvent event) {
                dropped.add("drop " + URLTransfer.getInstance().isSupportedType(event.currentDataType) + " " + event.data);
            }
        });

        shell.open();
        dragAndDrop(firstSource, dropTarget, e -> {
        });
        dropped.clear();
        dragAndDrop(secondSource, dropTarget, e -> {
        });

        assertThat(dropped).containsExactly("enter true", "drop true https://example.org");
    }

    @Test
    @DisplayName("a drop on a Tree's blank space has no item")
    void dropOnBlankTreeSpaceHasNoItem() {
        Tree tree = new Tree(shell, SWT.NONE);
        new TreeItem(tree, SWT.NONE).setText("root");
        DragSource dragSource = textSource(new Label(shell, SWT.NONE), DND.DROP_MOVE, "text");
        DropTarget dropTarget = new DropTarget(tree, DND.DROP_MOVE);
        dropTarget.setTransfer(TextTransfer.getInstance());
        List<Object> items = new ArrayList<>();
        dropTarget.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragOver(DropTargetEvent event) {
                items.add(event.item);
            }

            @Override
            public void drop(DropTargetEvent event) {
                items.add(event.item);
            }
        });

        shell.open();
        dragAndDrop(dragSource, dropTarget, e -> {
        });

        assertThat(items).containsExactly(null, null);
    }

    @Test
    @DisplayName("a target that keeps detail only when operations allows it and DropAccept came first adds the dragged row")
    void strictDropTargetAddsTheDraggedRow() {
        Table table = new Table(shell, SWT.NONE);
        new TableItem(table, SWT.NONE).setText("row");
        Tree tree = new Tree(shell, SWT.NONE);
        new TreeItem(tree, SWT.NONE).setText("inventory");
        DragSource dragSource = textSource(table, DND.DROP_COPY | DND.DROP_MOVE, "row");
        List<Integer> finished = new ArrayList<>();
        dragSource.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragFinished(DragSourceEvent event) {
                finished.add(event.detail);
            }
        });
        DropTarget dropTarget = new DropTarget(tree, DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK);
        dropTarget.setTransfer(TextTransfer.getInstance());
        boolean[] accepted = new boolean[1];
        dropTarget.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetEvent event) {
                accepted[0] = false;
            }

            @Override
            public void dropAccept(DropTargetEvent event) {
                accepted[0] = true;
            }

            @Override
            public void drop(DropTargetEvent event) {
                if (!accepted[0] || (event.operations & event.detail) == 0 || event.data == null) {
                    event.detail = DND.DROP_NONE;
                    return;
                }
                new TreeItem(tree.getItem(0), SWT.NONE).setText((String) event.data);
            }
        });

        shell.open();
        dragAndDrop(dragSource, dropTarget, e -> {
        });

        assertThat(tree.getItem(0).getItemCount()).isEqualTo(1);
        assertThat(tree.getItem(0).getItem(0).getText()).isEqualTo("row");
        assertThat(finished).containsExactly(DND.DROP_MOVE);
    }

    @Test
    @DisplayName("the drop target's answer to a drag reaches the client and is read there")
    @SuppressWarnings("unchecked")
    void dropTargetAnswerIsReadByTheClient() {
        Tree tree = new Tree(shell, SWT.BORDER);
        new TreeItem(tree, SWT.NONE).setText("node");
        DragSource dragSource = new DragSource(tree, DND.DROP_MOVE);
        dragSource.setTransfer(TextTransfer.getInstance());
        DropTarget dropTarget = new DropTarget(tree, DND.DROP_MOVE);
        dropTarget.setTransfer(TextTransfer.getInstance());
        dropTarget.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragOver(DropTargetEvent event) {
                event.detail = DND.DROP_MOVE;
                event.feedback = DND.FEEDBACK_SELECT;
            }
        });
        shell.open();
        flutter.show(shell);

        ((DartDragSource) dragSource.getImpl()).startDrag(new Event());
        DartDropTarget impl = (DartDropTarget) dropTarget.getImpl();
        impl.sendEvent(DND.DragEnter, new Event());
        impl.sendEvent(DND.DragOver, new Event());
        flutter.flush();

        Map<String, Object> answer = (Map<String, Object>) flutter.queryState(dropTarget).get("dropAnswer");
        assertThat(answer).as("the client decoded the answer Java sent").isNotNull();
        assertThat(((Number) answer.get("detail")).intValue()).isEqualTo(DND.DROP_MOVE);
        assertThat(((Number) answer.get("feedback")).intValue()).isEqualTo(DND.FEEDBACK_SELECT);
    }

    @Test
    @DisplayName("A DragSource attached after the control is rendered reaches Flutter")
    void dragSourceAttachedAfterRenderReachesFlutter() {
        Tree tree = new Tree(shell, SWT.BORDER);
        new TreeItem(tree, SWT.NONE).setText("node");
        shell.open();
        flutter.show(shell);

        new DragSource(tree, DND.DROP_COPY).setTransfer(TextTransfer.getInstance());
        flutter.flush();

        assertThat(renderedField(tree, "dragSource")).isEqualTo(true);
    }

    @Test
    @DisplayName("Disposing a rendered control's DragSource reaches Flutter")
    void dragSourceDisposedAfterRenderReachesFlutter() {
        Tree tree = new Tree(shell, SWT.BORDER);
        new TreeItem(tree, SWT.NONE).setText("node");
        DragSource dragSource = new DragSource(tree, DND.DROP_COPY);
        dragSource.setTransfer(TextTransfer.getInstance());
        shell.open();
        flutter.show(shell);
        assertThat(renderedField(tree, "dragSource")).isEqualTo(true);

        dragSource.dispose();
        flutter.flush();

        assertThat(renderedField(tree, "dragSource")).isNotEqualTo(true);
    }

    @Test
    @DisplayName("Disposing a rendered control's DropTarget reaches Flutter")
    void dropTargetDisposedAfterRenderReachesFlutter() {
        Tree tree = new Tree(shell, SWT.BORDER);
        new TreeItem(tree, SWT.NONE).setText("node");
        DropTarget dropTarget = new DropTarget(tree, DND.DROP_COPY);
        dropTarget.setTransfer(TextTransfer.getInstance());
        shell.open();
        flutter.show(shell);
        assertThat(renderedField(tree, "dropTargetId")).isNotNull();

        dropTarget.dispose();
        flutter.flush();

        assertThat(renderedField(tree, "dropTargetId")).isNull();
    }

    @Test
    @DisplayName("Label to Label drop updates the rendered target text (upstream Snippet78)")
    void labelToLabelDropUpdatesRenderedText() {
        Label source = new Label(shell, SWT.BORDER);
        source.setText("TEXT");
        Label target = new Label(shell, SWT.BORDER);

        DragSource dragSource = new DragSource(source, DND.DROP_COPY);
        dragSource.setTransfer(TextTransfer.getInstance());
        dragSource.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = source.getText();
            }
        });

        DropTarget dropTarget = new DropTarget(target, DND.DROP_COPY);
        dropTarget.setTransfer(TextTransfer.getInstance());
        dropTarget.addDropListener(new DropTargetAdapter() {
            @Override
            public void drop(DropTargetEvent event) {
                target.setText((String) event.data);
            }
        });

        shell.open();
        flutter.show(shell);

        dragAndDrop(dragSource, dropTarget, e -> {
        });
        flutter.flush();

        assertThat(renderedField(target, "text")).isEqualTo("TEXT");
    }

    @Test
    @DisplayName("Table row drop reorders the rendered items")
    void tableRowDropReordersRenderedItems() {
        Table table = new Table(shell, SWT.BORDER);
        List<String> order = new ArrayList<>(List.of("id", "name", "xpath"));
        for (String s : order) {
            new TableItem(table, SWT.NONE).setText(s);
        }

        DragSource dragSource = new DragSource(table, DND.DROP_MOVE);
        dragSource.setTransfer(TextTransfer.getInstance());
        dragSource.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = String.valueOf(table.getSelectionIndex());
            }
        });

        DropTarget dropTarget = new DropTarget(table, DND.DROP_MOVE);
        dropTarget.setTransfer(TextTransfer.getInstance());
        dropTarget.addDropListener(new DropTargetAdapter() {
            @Override
            public void drop(DropTargetEvent event) {
                TableItem targetItem = (TableItem) event.item;
                int newIndex = targetItem != null ? table.indexOf(targetItem) : order.size();
                int oldIndex = Integer.parseInt((String) event.data);
                if (oldIndex == newIndex) return;
                String moved = order.remove(oldIndex);
                if (oldIndex < newIndex) newIndex--;
                order.add(newIndex, moved);
                table.removeAll();
                for (String s : order) new TableItem(table, SWT.NONE).setText(s);
            }
        });

        table.setSelection(0); // drag "id" (row 0)

        shell.open();
        flutter.show(shell);

        dragAndDrop(dragSource, dropTarget, e -> e.index = 2); // ... onto row 2 ("xpath")
        flutter.flush();

        // "id" lands at row 2's own position (pushing "xpath" back), not appended past it —
        // the downward-shift correction this issue's TableDragDropSnippet fix relies on.
        assertThat(order).containsExactly("name", "id", "xpath");
    }

    /** Minimal custom {@link ByteArrayTransfer} subclass, mirroring upstream Snippet79/171. */
    private static final class CustomTransfer extends ByteArrayTransfer {
        static final CustomTransfer INSTANCE = new CustomTransfer();
        private static final String TYPE_NAME = "dnd-flutter-test-type";
        private static final int TYPE_ID = registerType(TYPE_NAME);

        @Override
        public void javaToNative(Object object, TransferData transferData) {
            if (!(object instanceof String) || !isSupportedType(transferData)) {
                DND.error(DND.ERROR_INVALID_DATA);
            }
            super.javaToNative(((String) object).getBytes(java.nio.charset.StandardCharsets.UTF_8), transferData);
        }

        @Override
        public Object nativeToJava(TransferData transferData) {
            if (!isSupportedType(transferData)) return null;
            byte[] bytes = (byte[]) super.nativeToJava(transferData);
            return bytes == null ? null : new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        }

        @Override
        protected String[] getTypeNames() {
            return new String[]{TYPE_NAME};
        }

        @Override
        protected int[] getTypeIds() {
            return new int[]{TYPE_ID};
        }
    }
}
