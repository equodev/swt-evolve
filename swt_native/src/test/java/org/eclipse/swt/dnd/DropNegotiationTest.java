package org.eclipse.swt.dnd;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
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

/**
 * What a DropTarget answers while the client negotiates a drag over it: the operation and data type
 * it keeps, what reaches its listeners, and what it tells the client.
 */
class DropNegotiationTest {

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

    /** A drag of "dragged" as text and RTF, started; its dragFinished details land in {@code ends}. */
    private DragSource dragging(int operations, List<Integer> ends) {
        Composite from = new Composite(shell, SWT.NONE);
        DragSource source = new DragSource(from, operations);
        source.setTransfer(new Transfer[] {TextTransfer.getInstance(), RTFTransfer.getInstance()});
        source.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = "dragged";
            }

            @Override
            public void dragFinished(DragSourceEvent event) {
                ends.add(event.detail);
            }
        });
        from.notifyListeners(SWT.DragDetect, new Event());
        return source;
    }

    private DropTarget target(int operations, DropTargetListener listener) {
        DropTarget target = new DropTarget(new Composite(shell, SWT.NONE), operations);
        target.setTransfer(new Transfer[] {TextTransfer.getInstance(), RTFTransfer.getInstance()});
        target.addDropListener(listener);
        return target;
    }

    private static void send(DropTarget target, int type) {
        ((DartDropTarget) target.getImpl()).sendEvent(type, new Event());
    }

    private static Event naming(long itemId) {
        Event e = new Event();
        e.index = -1;
        e.itemId = itemId;
        return e;
    }

    @SuppressWarnings("unchecked")
    private void fire(String channel, Event e) {
        Consumer<Event> handler = (Consumer<Event>) bridge.comm.handlers.get(channel);
        assertThat(handler).as(channel).isNotNull();
        handler.accept(e);
        while (display.readAndDispatch()) {
            // runs the asyncExec each message hops through
        }
    }

    /** Sends the items' controls on their own, which delivers the items: the client can then name them. */
    private void delivered(Item... items) {
        for (Item item : items) {
            Control parent = item instanceof TableItem ? ((TableItem) item).getParent()
                    : item instanceof TreeItem ? ((TreeItem) item).getParent() : ((CTabItem) item).getParent();
            parent.setData("dev.equo.swt.new", false);
            // A change, so the control is sent.
            parent.setVisible(false);
            parent.setVisible(true);
        }
        FlutterBridge.update();
        for (Item w : items)
            assertThat(FlutterBridge.findById(FlutterBridge.id(w))).as("delivered " + w).isNotNull();
    }

    private int answersSent(DropTarget target) {
        String channel = FlutterBridge.eventName(target.getImpl(), "Drop/negotiationResult");
        return (int) bridge.comm.sent.stream().filter(f -> f.event.equals(channel)).count();
    }

    @Test
    void aDragReleasedOutsideEveryTargetOnASurfaceWithOneWindowEndsWithNothingDropped() {
        List<Integer> ends = new ArrayList<>();
        DragSource source = dragging(DND.DROP_COPY, ends);
        Composite from = (Composite) source.getControl();
        Event at = new Event();
        at.x = 5000;
        at.y = 5000;

        fire(FlutterBridge.event(from.getImpl(), "DragDetect", "dragMove"), at);
        fire(FlutterBridge.event(from.getImpl(), "DragDetect", "dragRelease"), at);

        assertThat(bridge.comm.sent).noneMatch(f -> f.event.endsWith("/DnD/remoteDrag"));
        assertThat(ends).containsExactly(DND.DROP_NONE);
    }

    @Test
    void anItemOfAnotherControlIsNotTheDropItem() {
        Table table = new Table(shell, SWT.NONE);
        TableItem elsewhereRow = new TableItem(new Table(shell, SWT.NONE), SWT.NONE);
        Tree tree = new Tree(shell, SWT.NONE);
        TreeItem elsewhereNode = new TreeItem(new Tree(shell, SWT.NONE), SWT.NONE);
        CTabFolder folder = new CTabFolder(shell, SWT.NONE);
        CTabItem elsewhereTab = new CTabItem(new CTabFolder(shell, SWT.NONE), SWT.NONE);
        delivered(elsewhereRow, elsewhereNode, elsewhereTab);

        assertThat(DropTargetHelper.findItemById(table, FlutterBridge.id(elsewhereRow))).isNull();
        assertThat(DropTargetHelper.findItemById(tree, FlutterBridge.id(elsewhereNode))).isNull();
        assertThat(DropTargetHelper.findItemById(folder, FlutterBridge.id(elsewhereTab))).isNull();
    }

    @Test
    void anItemOfTheControlIsFoundByItsId() {
        Table table = new Table(shell, SWT.NONE);
        TableItem row = new TableItem(table, SWT.NONE);
        Tree tree = new Tree(shell, SWT.NONE);
        TreeItem node = new TreeItem(new TreeItem(tree, SWT.NONE), SWT.NONE);
        CTabFolder folder = new CTabFolder(shell, SWT.NONE);
        CTabItem tab = new CTabItem(folder, SWT.NONE);
        delivered(row, node, tab);

        assertThat(DropTargetHelper.findItemById(table, FlutterBridge.id(row))).isSameAs(row);
        assertThat(DropTargetHelper.findItemById(tree, FlutterBridge.id(node))).isSameAs(node);
        assertThat(DropTargetHelper.findItemById(folder, FlutterBridge.id(tab))).isSameAs(tab);
    }

    @Test
    void theItemTheEventAlreadyCarriesIsTheDropItem() {
        Table table = new Table(shell, SWT.NONE);
        new TableItem(table, SWT.NONE);
        TableItem second = new TableItem(table, SWT.NONE);
        List<Object> items = new ArrayList<>();
        DropTarget target = new DropTarget(table, DND.DROP_COPY);
        target.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        target.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetEvent event) {
                items.add(event.item);
            }

            @Override
            public void drop(DropTargetEvent event) {
                items.add(event.item);
            }
        });
        Event enter = naming(0);
        enter.item = second;
        Event drop = naming(0);
        drop.item = second;

        ((DartDropTarget) target.getImpl()).sendEvent(DND.DragEnter, enter);
        ((DartDropTarget) target.getImpl()).sendEvent(DND.Drop, drop);

        assertThat(items).containsExactly(second, second);
    }

    @Test
    void aDefaultOperationTheDragOverListenerAnswersIsTheFirstBothEndsAllow() {
        List<Integer> ends = new ArrayList<>();
        dragging(DND.DROP_COPY | DND.DROP_MOVE, ends);
        List<Integer> drops = new ArrayList<>();
        DropTarget target = target(DND.DROP_COPY | DND.DROP_MOVE, new DropTargetAdapter() {
            @Override
            public void dragOver(DropTargetEvent event) {
                event.detail = DND.DROP_DEFAULT;
            }

            @Override
            public void drop(DropTargetEvent event) {
                drops.add(event.detail);
            }
        });

        send(target, DND.DragEnter);
        send(target, DND.DragOver);
        send(target, DND.Drop);

        assertThat(drops).containsExactly(DND.DROP_MOVE);
        assertThat(ends).containsExactly(DND.DROP_MOVE);
    }

    @Test
    void anOperationTheSourceDoesNotAllowIsNotKept() {
        List<Integer> ends = new ArrayList<>();
        dragging(DND.DROP_COPY, ends);
        List<Integer> overDetails = new ArrayList<>();
        List<Integer> drops = new ArrayList<>();
        DropTarget target = target(DND.DROP_COPY | DND.DROP_LINK, new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetEvent event) {
                event.detail = DND.DROP_LINK;
            }

            @Override
            public void dragOver(DropTargetEvent event) {
                overDetails.add(event.detail);
                event.detail = DND.DROP_LINK;
            }

            @Override
            public void drop(DropTargetEvent event) {
                drops.add(event.detail);
            }
        });

        send(target, DND.DragEnter);
        send(target, DND.DragOver);
        send(target, DND.DragOver);
        send(target, DND.Drop);

        assertThat(overDetails).as("the refused LINK is not kept").containsExactly(DND.DROP_NONE, DND.DROP_NONE);
        assertThat(drops).isEmpty();
        assertThat(ends).containsExactly(DND.DROP_NONE);
    }

    @Test
    void aTypeTheDragDoesNotOfferKeepsNoOperation() {
        List<Integer> ends = new ArrayList<>();
        dragging(DND.DROP_COPY, ends);
        List<Integer> overDetails = new ArrayList<>();
        TransferData html = HTMLTransfer.getInstance().getSupportedTypes()[0];
        DropTarget target = target(DND.DROP_COPY, new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetEvent event) {
                event.currentDataType = html;
            }

            @Override
            public void dragOver(DropTargetEvent event) {
                overDetails.add(event.detail);
            }
        });

        send(target, DND.DragEnter);
        send(target, DND.DragOver);

        assertThat(overDetails).containsExactly(DND.DROP_NONE);
    }

    @Test
    void aDropWhoseDropAcceptPicksATypeTheDragDoesNotOfferDropsNothing() {
        List<Integer> ends = new ArrayList<>();
        dragging(DND.DROP_COPY, ends);
        List<Integer> drops = new ArrayList<>();
        TransferData html = HTMLTransfer.getInstance().getSupportedTypes()[0];
        DropTarget target = target(DND.DROP_COPY, new DropTargetAdapter() {
            @Override
            public void dropAccept(DropTargetEvent event) {
                event.currentDataType = html;
            }

            @Override
            public void drop(DropTargetEvent event) {
                drops.add(event.detail);
            }
        });

        send(target, DND.DragEnter);
        send(target, DND.Drop);

        assertThat(drops).isEmpty();
        assertThat(ends).containsExactly(DND.DROP_NONE);
    }

    @Test
    void theDropListenerDecidesTheOperationTheSourceHears() {
        int[][] answeredAndHeard = {
                {DND.DROP_DEFAULT, DND.DROP_MOVE},
                {DND.DROP_COPY, DND.DROP_COPY},
                {DND.DROP_LINK, DND.DROP_NONE},
        };
        for (int[] c : answeredAndHeard) {
            List<Integer> ends = new ArrayList<>();
            dragging(DND.DROP_COPY | DND.DROP_MOVE, ends);
            DropTarget target = target(DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK, new DropTargetAdapter() {
                @Override
                public void drop(DropTargetEvent event) {
                    event.detail = c[0];
                }
            });

            send(target, DND.DragEnter);
            send(target, DND.Drop);

            assertThat(ends).as("answered %d", c[0]).containsExactly(c[1]);
        }
    }

    @Test
    void anOperationTheDropAcceptListenerAnswersThatIsNotAllowedDropsNothing() {
        List<Integer> ends = new ArrayList<>();
        dragging(DND.DROP_COPY | DND.DROP_MOVE, ends);
        List<Integer> drops = new ArrayList<>();
        DropTarget target = target(DND.DROP_COPY | DND.DROP_MOVE, new DropTargetAdapter() {
            @Override
            public void dropAccept(DropTargetEvent event) {
                event.detail = DND.DROP_LINK;
            }

            @Override
            public void drop(DropTargetEvent event) {
                drops.add(event.detail);
            }
        });

        send(target, DND.DragEnter);
        send(target, DND.Drop);

        assertThat(drops).isEmpty();
        assertThat(ends).containsExactly(DND.DROP_NONE);
    }

    @Test
    void aDropFromOutsideTheApplicationReadsNoDataForTheTypeItsListenerPicks() {
        List<DropTargetEvent> drops = new ArrayList<>();
        TransferData text = TextTransfer.getInstance().getSupportedTypes()[0];
        DropTarget target = target(DND.DROP_COPY, new DropTargetAdapter() {
            @Override
            public void dropAccept(DropTargetEvent event) {
                event.currentDataType = text;
            }

            @Override
            public void drop(DropTargetEvent event) {
                drops.add(event);
            }
        });

        send(target, DND.Drop);

        assertThat(drops).singleElement().satisfies(e -> {
            assertThat(e.currentDataType).isSameAs(text);
            assertThat(e.data).isNull();
        });
    }

    @Test
    void theClientHearsAnAnswerAgainOnlyWhenItsFeedbackOrTypeChanges() {
        dragging(DND.DROP_COPY, new ArrayList<>());
        TransferData rtf = RTFTransfer.getInstance().getSupportedTypes()[0];
        int[] over = {0};
        DropTarget target = target(DND.DROP_COPY, new DropTargetAdapter() {
            @Override
            public void dragOver(DropTargetEvent event) {
                over[0]++;
                if (over[0] == 2) event.feedback = DND.FEEDBACK_SCROLL;
                if (over[0] == 3) event.feedback = DND.FEEDBACK_SCROLL;
                if (over[0] == 4) {
                    event.feedback = DND.FEEDBACK_SCROLL;
                    event.currentDataType = rtf;
                }
            }
        });

        send(target, DND.DragEnter);
        int afterEnter = answersSent(target);
        for (int i = 0; i < 4; i++) send(target, DND.DragOver);

        assertThat(afterEnter).isEqualTo(1);
        assertThat(answersSent(target))
                .as("enter, the unchanged first over sends nothing, the feedback change, the same again nothing, the type change")
                .isEqualTo(3);
    }
}
