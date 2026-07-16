package org.eclipse.swt.dnd;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.WidgetFlutterHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
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
 * Full-stack tests for the DND generalization (issue #755), driven through a real Flutter
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
        // resolve to it (the pre-#755 bug assumed source and target always share a control).
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
    @DisplayName("Table row drop reorders the rendered items (issue #755's original repro)")
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
