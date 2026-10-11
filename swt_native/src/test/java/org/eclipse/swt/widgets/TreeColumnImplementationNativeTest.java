package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;

/**
 * What {@link DartTreeColumn} does when it is driven directly rather than through {@link TreeColumn}, and
 * what a header click, a header drag and a divider drag arriving from Flutter do to it. Every event from
 * Flutter hops through {@code asyncExec}, so by the time it runs the column may be gone.
 */
@Tag("native-unit")
class TreeColumnImplementationNativeTest {

    private RecordingBridge bridge;
    private Display display;
    private Tree tree;
    /** The runnables a hook queued with asyncExec, run when the test says so. */
    private final List<Runnable> queued = new ArrayList<>();

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = DartMocks.dartDisplay();
        doAnswer(invocation -> queued.add(invocation.getArgument(0))).when(display).asyncExec(any(Runnable.class));
        // The mock display would swallow every event; its real dispatch only needs the listener table.
        DartDisplay impl = dartDisplay();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        tree = new Tree(DartMocks.dartShell(display), SWT.NONE);
    }

    @AfterEach
    void tearDown() {
        // The display mock is shared by the whole suite: hand it back as the other tests expect it.
        doNothing().when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = dartDisplay();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        FlutterBridge.set(null);
    }

    private DartDisplay dartDisplay() {
        return (DartDisplay) display.getImpl();
    }

    private static DartTreeColumn impl(TreeColumn column) {
        return (DartTreeColumn) column.getImpl();
    }

    private void fromFlutter(TreeColumn column, String channel, Event event) {
        bridge.comm.fireContaining("TreeColumn/" + column.hashCode() + "/" + channel, event);
    }

    private void runQueued() {
        List<Runnable> now = new ArrayList<>(queued);
        queued.clear();
        now.forEach(Runnable::run);
    }

    private static Event width(int width) {
        Event event = new Event();
        event.width = width;
        return event;
    }

    @Test
    @DisplayName("an implementation built without its API object creates one, once, bound to itself")
    void implementationWithoutApiCreatesOne() {
        DartTreeColumn impl = new DartTreeColumn(tree, SWT.NONE, null);

        TreeColumn api = impl.getApi();

        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
        assertThat(tree.getColumn(0)).isSameAs(api);
    }

    @Test
    @DisplayName("a column of SWT's own class passes the subclass check")
    void ownClassPassesTheSubclassCheck() {
        TreeColumn column = new TreeColumn(tree, SWT.NONE);

        assertThatCode(column::checkSubclass).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("the implementation reports what the column holds, its header text without mnemonics")
    void implementationReportsWhatItHolds() {
        TreeColumn column = new TreeColumn(tree, SWT.NONE);
        column.setText("&Size && kind");
        column.setToolTipText("tip");
        column.setMoveable(true);
        column.setResizable(false);
        column.setWidth(80);

        DartTreeColumn impl = impl(column);
        assertThat(impl._parent()).isSameAs(tree);
        assertThat(impl._displayText()).isEqualTo("Size & kind");
        assertThat(impl._toolTipText()).isEqualTo("tip");
        assertThat(impl._movable()).isTrue();
        assertThat(impl._resizable()).isFalse();
        assertThat(impl._width()).isEqualTo(80);
    }

    @Test
    @DisplayName("a divider dragged in Flutter resizes the column")
    void dividerDragResizesTheColumn() {
        TreeColumn column = new TreeColumn(tree, SWT.NONE);
        List<Integer> widths = new ArrayList<>();
        column.addListener(SWT.Resize, e -> widths.add(column.getWidth()));

        fromFlutter(column, "Control/Resize", width(140));
        runQueued();

        assertThat(column.getWidth()).isEqualTo(140);
        assertThat(widths).containsExactly(140);
    }

    @Test
    @DisplayName("a header click reaches the column's listeners")
    void headerClickReachesTheListeners() {
        TreeColumn column = new TreeColumn(tree, SWT.NONE);
        List<Integer> types = new ArrayList<>();
        for (int type : new int[] {SWT.Selection, SWT.DefaultSelection})
            column.addListener(type, e -> types.add(e.type));

        fromFlutter(column, "Selection/Selection", new Event());
        fromFlutter(column, "Selection/DefaultSelection", new Event());
        runQueued();

        assertThat(types).containsExactly(SWT.Selection, SWT.DefaultSelection);
    }

    private TreeColumn[] columns(int... widths) {
        TreeColumn[] columns = new TreeColumn[widths.length];
        for (int i = 0; i < widths.length; i++) {
            columns[i] = new TreeColumn(tree, SWT.NONE);
            columns[i].setWidth(widths[i]);
        }
        return columns;
    }

    private static Event droppedAt(int displayIndex) {
        Event event = new Event();
        event.index = displayIndex;
        return event;
    }

    @Test
    @DisplayName("a moveable column dragged in the header is shown where it was dropped, and the columns that moved hear it")
    void headerDragReordersTheColumns() {
        TreeColumn[] columns = columns(50, 60, 70);
        columns[0].setMoveable(true);
        List<TreeColumn> moved = new ArrayList<>();
        for (TreeColumn column : columns)
            column.addListener(SWT.Move, e -> moved.add(column));

        fromFlutter(columns[0], "Control/Move", droppedAt(2));
        runQueued();

        assertThat(tree.getColumnOrder()).containsExactly(1, 2, 0);
        assertThat(moved).containsExactlyInAnyOrder(columns);
        assertThat(((DartTree) tree.getImpl()).getValue().changedKeys()).contains(VTree.COLUMN_ORDER);
    }

    @Test
    @DisplayName("a header drag past the last column puts the column last; one dropped on its own place moves nothing")
    void headerDragIsClampedAndADropInPlaceIsNoMove() {
        TreeColumn[] columns = columns(50, 60, 70);
        columns[1].setMoveable(true);
        List<TreeColumn> moved = new ArrayList<>();
        for (TreeColumn column : columns)
            column.addListener(SWT.Move, e -> moved.add(column));

        fromFlutter(columns[1], "Control/Move", droppedAt(1));
        runQueued();
        assertThat(moved).isEmpty();

        fromFlutter(columns[1], "Control/Move", droppedAt(9));
        runQueued();
        assertThat(tree.getColumnOrder()).containsExactly(0, 2, 1);
        assertThat(moved).containsExactlyInAnyOrder(columns[1], columns[2]);
    }

    @Test
    @DisplayName("a column that is not moveable stays where it is when its header is dragged")
    void headerDragOfAColumnThatIsNotMoveableMovesNothing() {
        TreeColumn[] columns = columns(50, 60);
        List<TreeColumn> moved = new ArrayList<>();
        for (TreeColumn column : columns)
            column.addListener(SWT.Move, e -> moved.add(column));

        fromFlutter(columns[0], "Control/Move", droppedAt(1));
        runQueued();

        assertThat(tree.getColumnOrder()).containsExactly(0, 1);
        assertThat(moved).isEmpty();
    }

    @Test
    @DisplayName("an event that arrives after the column was disposed is dropped")
    void eventsForADisposedColumnAreDropped() {
        TreeColumn column = new TreeColumn(tree, SWT.NONE);
        fromFlutter(column, "Control/Resize", width(50));
        fromFlutter(column, "Control/Move", new Event());
        fromFlutter(column, "Selection/Selection", new Event());
        fromFlutter(column, "Selection/DefaultSelection", new Event());
        column.dispose();

        assertThatCode(this::runQueued).doesNotThrowAnyException();
    }
}
