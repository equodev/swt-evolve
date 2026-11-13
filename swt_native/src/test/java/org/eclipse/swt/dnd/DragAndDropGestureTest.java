package org.eclipse.swt.dnd;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The drag gesture as the Flutter client drives it: SWT.DragDetect on the source's control starts
 * the drag, and the client's Drop/* messages reach the drop target.
 */
class DragAndDropGestureTest {

    private RecordingBridge bridge;
    private Display display;
    private Shell shell;

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
        shell = new Shell(display);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    private DragSource source(Control control, String data, Consumer<DragSourceEvent> onStart, Transfer... transfers) {
        DragSource source = new DragSource(control, DND.DROP_COPY | DND.DROP_MOVE);
        if (transfers.length > 0) source.setTransfer(transfers);
        source.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragStart(DragSourceEvent event) {
                onStart.accept(event);
            }

            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = data;
            }
        });
        return source;
    }

    private static Event dragDetect(Control control) {
        Event e = new Event();
        e.x = 3;
        e.y = 4;
        control.notifyListeners(SWT.DragDetect, e);
        return e;
    }

    private DropTarget target(Control control, int style, List<DropTargetEvent> drops, Transfer... transfers) {
        DropTarget target = new DropTarget(control, style);
        target.setTransfer(transfers);
        target.addDropListener(new DropTargetAdapter() {
            @Override
            public void drop(DropTargetEvent event) {
                drops.add(event);
            }
        });
        return target;
    }

    private static DartDropTarget impl(DropTarget target) {
        return (DartDropTarget) target.getImpl();
    }

    private static Event at(int index, long itemId) {
        Event e = new Event();
        e.index = index;
        e.itemId = itemId;
        return e;
    }

    @Test
    void aDragDetectOnTheControlStartsTheDragAndTheDropReadsItsData() {
        Composite from = new Composite(shell, SWT.NONE);
        List<DragSourceEvent> starts = new ArrayList<>();
        source(from, "dragged", starts::add, TextTransfer.getInstance());
        List<DropTargetEvent> drops = new ArrayList<>();
        DropTarget target = target(new Composite(shell, SWT.NONE), DND.DROP_COPY, drops, TextTransfer.getInstance());

        dragDetect(from);
        impl(target).sendEvent(DND.Drop, new Event());

        assertThat(starts).singleElement().satisfies(e -> {
            assertThat(e.x).isEqualTo(3);
            assertThat(e.y).isEqualTo(4);
        });
        assertThat(drops).singleElement().extracting(e -> e.data).isEqualTo("dragged");
    }

    @Test
    void theImageADragStartListenerGivesStaysTheApplications() {
        Composite from = new Composite(shell, SWT.NONE);
        Image image = new Image(display, 4, 4);
        source(from, "dragged", e -> e.image = image, TextTransfer.getInstance());

        dragDetect(from);

        assertThat(image.isDisposed()).isFalse();
        image.dispose();
    }

    @Test
    void aRefusedDragStartStartsNoDrag() {
        Composite from = new Composite(shell, SWT.NONE);
        source(from, "dragged", e -> e.doit = false, TextTransfer.getInstance());
        List<DropTargetEvent> drops = new ArrayList<>();
        DropTarget target = target(new Composite(shell, SWT.NONE), DND.DROP_COPY, drops, TextTransfer.getInstance());

        dragDetect(from);
        impl(target).sendEvent(DND.Drop, new Event());

        assertThat(drops).isEmpty();
    }

    @Test
    void aSourceWithoutTransfersStartsNoDrag() {
        Composite from = new Composite(shell, SWT.NONE);
        List<DragSourceEvent> starts = new ArrayList<>();
        source(from, "dragged", starts::add);
        List<DropTargetEvent> drops = new ArrayList<>();
        DropTarget target = target(new Composite(shell, SWT.NONE), DND.DROP_COPY, drops, TextTransfer.getInstance());

        dragDetect(from);
        impl(target).sendEvent(DND.Drop, new Event());

        assertThat(starts).hasSize(1);
        assertThat(drops).isEmpty();
    }

    @Test
    void aSourceItsDragStartListenerDisposesStartsNoDrag() {
        Composite from = new Composite(shell, SWT.NONE);
        DragSource[] source = new DragSource[1];
        source[0] = source(from, "dragged", e -> source[0].dispose(), TextTransfer.getInstance());
        List<DropTargetEvent> drops = new ArrayList<>();
        DropTarget target = target(new Composite(shell, SWT.NONE), DND.DROP_COPY, drops, TextTransfer.getInstance());

        dragDetect(from);
        impl(target).sendEvent(DND.Drop, new Event());

        assertThat(source[0].isDisposed()).isTrue();
        assertThat(drops).isEmpty();
    }

    @Test
    void aRefusedDragStartOnATableCancelsTheDragDetect() {
        // SWT.getVersion() 4950 is 3.119, the first release whose Cocoa DragSource cancels it (bug 577858).
        assumeTrue(SWT.getVersion() >= 4950);
        Table table = new Table(shell, SWT.NONE);
        source(table, "row", e -> e.doit = false, TextTransfer.getInstance());

        assertThat(dragDetect(table).doit).isFalse();
    }

    @Test
    void anAcceptedDragStartOnATreeKeepsTheDragDetect() {
        Tree tree = new Tree(shell, SWT.NONE);
        source(tree, "node", e -> {}, TextTransfer.getInstance());
        List<DropTargetEvent> drops = new ArrayList<>();
        DropTarget target = target(new Composite(shell, SWT.NONE), DND.DROP_COPY, drops, TextTransfer.getInstance());

        assertThat(dragDetect(tree).doit).isTrue();
        impl(target).sendEvent(DND.Drop, new Event());
        assertThat(drops).singleElement().extracting(e -> e.data).isEqualTo("node");
    }

    @Test
    void aNullTransferOfTheSourceIsSkipped() {
        Composite from = new Composite(shell, SWT.NONE);
        source(from, "dragged", e -> {}, null, TextTransfer.getInstance());
        List<DropTargetEvent> drops = new ArrayList<>();
        DropTarget target = target(new Composite(shell, SWT.NONE), DND.DROP_COPY, drops, TextTransfer.getInstance());

        dragDetect(from);
        impl(target).sendEvent(DND.Drop, new Event());

        assertThat(drops).singleElement().extracting(e -> e.data).isEqualTo("dragged");
    }

    @Test
    void aDragADragEndListenerStartsStaysActive() {
        Composite first = new Composite(shell, SWT.NONE);
        Composite second = new Composite(shell, SWT.NONE);
        DragSource firstSource = source(first, "first", e -> {}, TextTransfer.getInstance());
        source(second, "second", e -> {}, TextTransfer.getInstance());
        firstSource.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragFinished(DragSourceEvent event) {
                dragDetect(second);
            }
        });
        List<DropTargetEvent> drops = new ArrayList<>();
        DropTarget target = target(new Composite(shell, SWT.NONE), DND.DROP_COPY, drops, TextTransfer.getInstance());

        dragDetect(first);
        impl(target).sendEvent(DND.Drop, new Event());
        impl(target).sendEvent(DND.Drop, new Event());

        assertThat(drops).extracting(e -> e.data).containsExactly("first", "second");
    }

    @Test
    void aDragOfTypesTheTargetDoesNotAcceptDropsNothing() {
        Composite from = new Composite(shell, SWT.NONE);
        DragSource source = source(from, "dragged", e -> {}, TextTransfer.getInstance());
        List<DragSourceEvent> requests = new ArrayList<>();
        source.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragSetData(DragSourceEvent event) {
                requests.add(event);
            }
        });
        List<DropTargetEvent> drops = new ArrayList<>();
        DropTarget target = target(new Composite(shell, SWT.NONE), DND.DROP_COPY, drops, RTFTransfer.getInstance());

        dragDetect(from);
        impl(target).sendEvent(DND.Drop, new Event());

        assertThat(requests).isEmpty();
        assertThat(drops).isEmpty();
    }

    @Test
    void aNullTransferTheTargetKeptIsSkipped() {
        Composite from = new Composite(shell, SWT.NONE);
        source(from, "dragged", e -> {}, TextTransfer.getInstance());
        List<DropTargetEvent> drops = new ArrayList<>();
        DropTarget target = target(new Composite(shell, SWT.NONE), DND.DROP_COPY, drops, RTFTransfer.getInstance());
        target.setTransfer(new Transfer[] {TextTransfer.getInstance(), null});

        dragDetect(from);
        impl(target).sendEvent(DND.Drop, new Event());

        assertThat(drops).singleElement().satisfies(e -> {
            assertThat(e.dataTypes).singleElement().matches(TextTransfer.getInstance()::isSupportedType);
            assertThat(e.data).isEqualTo("dragged");
        });
    }

    @Test
    void aDropAfterTheDraggingSourceIsDisposedReadsNoData() {
        Composite from = new Composite(shell, SWT.NONE);
        DragSource source = source(from, "dragged", e -> {}, TextTransfer.getInstance());
        List<DropTargetEvent> drops = new ArrayList<>();
        DropTarget target = target(new Composite(shell, SWT.NONE), DND.DROP_COPY, drops, TextTransfer.getInstance());

        dragDetect(from);
        source.dispose();
        impl(target).sendEvent(DND.Drop, new Event());

        assertThat(drops).singleElement().satisfies(e -> {
            assertThat(e.data).isNull();
            assertThat(e.dataTypes).isEmpty();
        });
    }

    @Test
    void aDropFromOutsideTheApplicationOffersNoTypes() {
        List<DropTargetEvent> enters = new ArrayList<>();
        DropTarget target = new DropTarget(new Composite(shell, SWT.NONE), DND.DROP_COPY);
        target.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        target.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetEvent event) {
                enters.add(event);
            }
        });

        impl(target).sendEvent(DND.DragEnter, new Event());

        assertThat(enters).singleElement().satisfies(e -> {
            assertThat(e.dataTypes).isEmpty();
            assertThat(e.currentDataType).isNull();
            assertThat(e.detail).isEqualTo(DND.DROP_COPY);
        });
    }

    @Test
    void aDefaultOperationDropsAsTheFirstOneTheTargetAllowsOfMoveCopyLink() {
        int[][] styleAndExpected = {
                {DND.DROP_COPY | DND.DROP_LINK, DND.DROP_COPY},
                {DND.DROP_LINK, DND.DROP_LINK},
                {DND.DROP_TARGET_MOVE, DND.DROP_NONE},
        };
        for (int[] c : styleAndExpected) {
            List<DropTargetEvent> drops = new ArrayList<>();
            DropTarget target = target(new Composite(shell, SWT.NONE), c[0], drops, TextTransfer.getInstance());
            Event drop = new Event();
            drop.detail = DND.DROP_DEFAULT;

            impl(target).sendEvent(DND.Drop, drop);

            assertThat(drops).extracting(e -> e.detail).containsExactly(
                    c[1] == DND.DROP_NONE ? new Integer[0] : new Integer[] {c[1]});
        }
    }

    @Test
    void theOperationTheDragOverListenerChoseIsTheOneDropped() {
        Composite from = new Composite(shell, SWT.NONE);
        source(from, "dragged", e -> {}, TextTransfer.getInstance());
        List<DropTargetEvent> drops = new ArrayList<>();
        DropTarget target = target(new Composite(shell, SWT.NONE), DND.DROP_COPY | DND.DROP_MOVE, drops, TextTransfer.getInstance());
        List<Integer> overDetails = new ArrayList<>();
        target.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragOver(DropTargetEvent event) {
                overDetails.add(event.detail);
                event.detail = DND.DROP_COPY;
            }
        });

        dragDetect(from);
        impl(target).sendEvent(DND.DragEnter, new Event());
        impl(target).sendEvent(DND.DragOver, new Event());
        impl(target).sendEvent(DND.DragOver, new Event());
        impl(target).sendEvent(DND.Drop, new Event());

        assertThat(overDetails).containsExactly(DND.DROP_MOVE, DND.DROP_COPY);
        assertThat(drops).singleElement().satisfies(e -> {
            assertThat(e.detail).isEqualTo(DND.DROP_COPY);
            assertThat(e.data).isEqualTo("dragged");
        });
    }

    @Test
    void theDropItemIsTheRowAtTheIndexWhenTheIdNamesNone() {
        Table table = new Table(shell, SWT.NONE);
        TableItem first = new TableItem(table, SWT.NONE);
        TableItem second = new TableItem(table, SWT.NONE);
        List<DropTargetEvent> drops = new ArrayList<>();
        DropTarget target = target(table, DND.DROP_COPY, drops, TextTransfer.getInstance());

        impl(target).sendEvent(DND.Drop, at(1, -42));
        impl(target).sendEvent(DND.Drop, at(-1, FlutterBridge.id(first)));
        impl(target).sendEvent(DND.Drop, at(2, 0));
        impl(target).sendEvent(DND.Drop, at(-1, 0));

        assertThat(drops).extracting(e -> e.item).containsExactly(second, first, null, null);
    }

    @Test
    void theDropItemOnATreeIsTheItemNamed() {
        Tree tree = new Tree(shell, SWT.NONE);
        TreeItem first = new TreeItem(tree, SWT.NONE);
        new TreeItem(first, SWT.NONE);
        TreeItem nested = new TreeItem(first, SWT.NONE);
        TreeItem second = new TreeItem(tree, SWT.NONE);
        List<DropTargetEvent> drops = new ArrayList<>();
        DropTarget target = target(tree, DND.DROP_COPY, drops, TextTransfer.getInstance());

        impl(target).sendEvent(DND.Drop, at(-1, FlutterBridge.id(second)));
        impl(target).sendEvent(DND.Drop, at(-1, FlutterBridge.id(nested)));
        impl(target).sendEvent(DND.Drop, at(0, -42));
        impl(target).sendEvent(DND.Drop, at(1, 0));
        impl(target).sendEvent(DND.Drop, at(2, 0));
        impl(target).sendEvent(DND.Drop, at(-1, 0));

        assertThat(drops).extracting(e -> e.item).containsExactly(second, nested, null, null, null, null);
    }

    @Test
    void theDropItemOnACTabFolderIsTheTabNamedOrAtTheIndex() {
        CTabFolder folder = new CTabFolder(shell, SWT.NONE);
        CTabItem first = new CTabItem(folder, SWT.NONE);
        CTabItem second = new CTabItem(folder, SWT.NONE);
        List<DropTargetEvent> drops = new ArrayList<>();
        DropTarget target = target(folder, DND.DROP_COPY, drops, TextTransfer.getInstance());

        impl(target).sendEvent(DND.Drop, at(-1, FlutterBridge.id(second)));
        impl(target).sendEvent(DND.Drop, at(0, -42));
        impl(target).sendEvent(DND.Drop, at(2, 0));
        impl(target).sendEvent(DND.Drop, at(-1, 0));

        assertThat(drops).extracting(e -> e.item).containsExactly(second, first, null, null);
    }

    @Test
    void anItemIdOnAControlWithoutItemsNamesNoItem() {
        List<DropTargetEvent> drops = new ArrayList<>();
        DropTarget target = target(new Composite(shell, SWT.NONE), DND.DROP_COPY, drops, TextTransfer.getInstance());

        impl(target).sendEvent(DND.Drop, at(0, 7));

        assertThat(drops).singleElement().extracting(e -> e.item).isNull();
    }

    @Test
    void theClientsGestureMessagesReachTheDropListener() {
        List<String> seen = new ArrayList<>();
        DropTarget target = new DropTarget(new Composite(shell, SWT.NONE), DND.DROP_COPY);
        target.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        target.addDropListener(new DropTargetListener() {
            @Override public void dragEnter(DropTargetEvent event) { seen.add("dragEnter"); }
            @Override public void dragLeave(DropTargetEvent event) { seen.add("dragLeave"); }
            @Override public void dragOperationChanged(DropTargetEvent event) { seen.add("dragOperationChanged"); }
            @Override public void dragOver(DropTargetEvent event) { seen.add("dragOver"); }
            @Override public void drop(DropTargetEvent event) { seen.add("drop"); }
            @Override public void dropAccept(DropTargetEvent event) { seen.add("dropAccept"); }
        });

        for (String message : List.of("dragEnter", "dragOver", "dragLeave", "dragEnter", "drop")) {
            fire(target.getImpl(), "Drop", message);
        }

        assertThat(seen).containsExactly("dragEnter", "dragOver", "dragLeave", "dragEnter", "dragLeave", "dropAccept", "drop");
    }

    @Test
    void aGestureMessageThatArrivesAfterTheTargetIsDisposedIsDropped() {
        List<String> seen = new ArrayList<>();
        DropTarget target = new DropTarget(new Composite(shell, SWT.NONE), DND.DROP_COPY);
        target.addDropListener(new DropTargetAdapter() {
            @Override public void dragEnter(DropTargetEvent event) { seen.add("dragEnter"); }
        });
        DartWidget impl = (DartWidget) target.getImpl();
        List<String> messages = List.of("dragEnter", "dragOver", "dragLeave", "drop");
        List<String> channels = new ArrayList<>();
        for (String message : messages) channels.add(FlutterBridge.event(impl, "Drop", message));

        for (String channel : channels) send(channel);
        target.dispose();
        drain();

        assertThat(seen).isEmpty();
    }

    @Test
    void anEventThatIsNotADragOneReachesItsListenersAsItIs() {
        List<String> seen = new ArrayList<>();
        DropTarget target = new DropTarget(new Composite(shell, SWT.NONE), DND.DROP_COPY);
        target.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        target.addDropListener(new DropTargetAdapter() {
            @Override public void dragOperationChanged(DropTargetEvent event) { seen.add("dragOperationChanged"); }
            @Override public void dropAccept(DropTargetEvent event) { seen.add("dropAccept"); }
        });
        List<Integer> custom = new ArrayList<>();
        target.addListener(SWT.Modify, e -> custom.add(e.detail));

        impl(target).sendEvent(DND.DragOperationChanged, new Event());
        impl(target).sendEvent(DND.DropAccept, new Event());
        Event modify = new Event();
        modify.detail = 9;
        impl(target).sendEvent(SWT.Modify, modify);

        assertThat(seen).containsExactly("dragOperationChanged", "dropAccept");
        assertThat(custom).containsExactly(9);
    }

    private void fire(Object impl, String group, String message) {
        send(FlutterBridge.event((DartWidget) impl, group, message));
        drain();
    }

    @SuppressWarnings("unchecked")
    private void send(String channel) {
        Consumer<Event> handler = (Consumer<Event>) bridge.comm.handlers.get(channel);
        assertThat(handler).as(channel).isNotNull();
        handler.accept(new Event());
    }

    private void drain() {
        while (display.readAndDispatch()) {
            // runs the asyncExec each message hops through
        }
    }
}
