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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * What a click, an edit, an expander, a scroll or a request for rows arriving from Flutter does to a
 * Tree. Every one hops through {@code asyncExec}, so by the time it runs the tree or the row it names
 * may be gone.
 */
@Tag("native-unit")
class TreeEventsFromFlutterNativeTest {

    private RecordingBridge bridge;
    private Display display;
    private final List<Runnable> queued = new ArrayList<>();

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = DartMocks.dartDisplay();
        doAnswer(invocation -> queued.add(invocation.getArgument(0))).when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = dartDisplay();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
    }

    @AfterEach
    void tearDown() {
        doNothing().when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = dartDisplay();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        FlutterBridge.set(null);
    }

    private DartDisplay dartDisplay() {
        return (DartDisplay) display.getImpl();
    }

    private Tree tree(int style) {
        return new Tree(DartMocks.dartShell(display), style);
    }

    private static TreeItem[] roots(Tree tree, int count) {
        TreeItem[] items = new TreeItem[count];
        for (int i = 0; i < count; i++) {
            items[i] = new TreeItem(tree, SWT.NONE);
            items[i].setText("item " + i);
        }
        return items;
    }

    private void fromFlutter(Tree tree, String channel, Event event) {
        bridge.comm.fireContaining("Tree/" + tree.hashCode() + "/" + channel, event);
    }

    private void runQueued() {
        List<Runnable> now = new ArrayList<>(queued);
        queued.clear();
        now.forEach(Runnable::run);
    }

    private static List<Event> record(Tree tree, int type) {
        List<Event> events = new ArrayList<>();
        tree.addListener(type, events::add);
        return events;
    }

    private static Event row(int index) {
        Event event = new Event();
        event.index = index;
        return event;
    }

    private static Event item(TreeItem item) {
        Event event = new Event();
        event.itemId = FlutterBridge.id(item);
        return event;
    }

    @Test
    @DisplayName("a click selects the row it names and tells the Selection listeners")
    void aClickSelectsTheRow() {
        Tree tree = tree(SWT.MULTI);
        TreeItem[] items = roots(tree, 3);
        List<Event> events = record(tree, SWT.Selection);

        fromFlutter(tree, "Selection/Selection", item(items[2]));
        runQueued();

        assertThat(tree.getSelection()).containsExactly(items[2]);
        assertThat(events).hasSize(1);
        assertThat(events.get(0).item).isSameAs(items[2]);
    }

    @Test
    @DisplayName("a click selects without scrolling: the row clicked is on screen")
    void aClickDoesNotScroll() {
        Tree tree = tree(SWT.SINGLE);
        tree.setBounds(0, 0, 200, 100);
        TreeItem[] items = roots(tree, 40);
        Event scroll = new Event();
        scroll.y = 10 * tree.getItemHeight();
        fromFlutter(tree, "Scroll/Scroll", scroll);
        runQueued();
        ((DartTree) tree.getImpl()).getValue().clearDirty();

        fromFlutter(tree, "Selection/Selection", item(items[12]));
        runQueued();

        assertThat(tree.getSelection()).containsExactly(items[12]);
        assertThat(((DartTree) tree.getImpl()).getValue().changedKeys()).doesNotContain(VTree.TOP_ITEM);
        assertThat(tree.getTopItem()).isSameAs(items[10]);
    }

    @Test
    @DisplayName("a click on a row that is gone, or past the last row, changes nothing")
    void aClickOnNoRowChangesNothing() {
        Tree tree = tree(SWT.MULTI);
        TreeItem[] items = roots(tree, 3);
        Event gone = item(items[1]);
        items[1].dispose();
        List<Event> events = record(tree, SWT.Selection);

        fromFlutter(tree, "Selection/Selection", gone);
        fromFlutter(tree, "Selection/Selection", row(9));
        fromFlutter(tree, "Selection/Selection", row(-1));
        runQueued();

        assertThat(tree.getSelectionCount()).isZero();
        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("a click that arrives after the tree is gone is dropped")
    void aClickAfterDisposeIsDropped() {
        Tree tree = tree(SWT.SINGLE);
        roots(tree, 2);

        fromFlutter(tree, "Selection/Selection", row(0));
        fromFlutter(tree, "Selection/DefaultSelection", row(0));
        fromFlutter(tree, "Modify/Modify", row(0));
        fromFlutter(tree, "Tree/Expand", row(0));
        fromFlutter(tree, "Tree/Collapse", row(0));
        fromFlutter(tree, "SetData/SetData", row(0));
        fromFlutter(tree, "Scroll/Scroll", row(0));
        fromFlutter(tree, "PaintItem/PaintItem", row(0));
        tree.dispose();
        runQueued();

        assertThat(tree.isDisposed()).isTrue();
    }

    @Test
    @DisplayName("a double click reaches the DefaultSelection listeners with the row")
    void aDoubleClickReachesDefaultSelection() {
        Tree tree = tree(SWT.SINGLE);
        TreeItem[] items = roots(tree, 2);
        List<Event> events = record(tree, SWT.DefaultSelection);

        fromFlutter(tree, "Selection/DefaultSelection", row(1));
        runQueued();

        assertThat(events).hasSize(1);
        assertThat(events.get(0).item).isSameAs(items[1]);
    }

    @Test
    @DisplayName("a double click while a modal shell blocks the tree's shell is dropped")
    void aDoubleClickBehindAModalShellIsDropped() {
        Shell shell = DartMocks.dartShell(display);
        when(((DartShell) shell.getImpl()).getModalShell()).thenReturn(mock(Shell.class));
        Tree tree = new Tree(shell, SWT.SINGLE);
        roots(tree, 2);
        List<Event> events = record(tree, SWT.DefaultSelection);

        fromFlutter(tree, "Selection/DefaultSelection", row(1));
        runQueued();

        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("a check box click toggles the item before the listener runs")
    void aCheckBoxClickTogglesTheItem() {
        Tree tree = tree(SWT.CHECK);
        TreeItem[] items = roots(tree, 2);
        List<Boolean> seen = new ArrayList<>();
        tree.addListener(SWT.Selection, e -> seen.add(((TreeItem) e.item).getChecked()));
        Event click = row(0);
        click.detail = SWT.CHECK;

        fromFlutter(tree, "Selection/Selection", click);
        runQueued();

        assertThat(seen).containsExactly(true);
        assertThat(items[0].getChecked()).isTrue();
    }

    @Test
    @DisplayName("an edit sets the text of the row it names; one without text or row changes nothing")
    void anEditSetsTheRowText() {
        Tree tree = tree(SWT.SINGLE);
        TreeItem[] items = roots(tree, 2);
        Event edit = row(1);
        edit.text = "renamed";
        Event noText = row(0);
        Event noRow = row(7);
        noRow.text = "lost";
        Event negative = row(-1);
        negative.text = "lost";

        fromFlutter(tree, "Modify/Modify", edit);
        fromFlutter(tree, "Modify/Modify", noText);
        fromFlutter(tree, "Modify/Modify", noRow);
        fromFlutter(tree, "Modify/Modify", negative);
        runQueued();

        assertThat(items[1].getText()).isEqualTo("renamed");
        assertThat(items[0].getText()).isEqualTo("item 0");
    }

    @Test
    @DisplayName("an expander opens the item after telling the Expand listeners, and closes it again")
    void anExpanderOpensAndClosesTheItem() {
        Tree tree = tree(SWT.SINGLE);
        TreeItem[] items = roots(tree, 2);
        new TreeItem(items[0], SWT.NONE);
        List<Boolean> expandedWhenTold = new ArrayList<>();
        tree.addListener(SWT.Expand, e -> expandedWhenTold.add(((TreeItem) e.item).getExpanded()));
        List<Event> collapses = record(tree, SWT.Collapse);

        fromFlutter(tree, "Tree/Expand", item(items[0]));
        runQueued();
        assertThat(items[0].getExpanded()).isTrue();
        assertThat(expandedWhenTold).containsExactly(false);

        fromFlutter(tree, "Tree/Expand", item(items[0]));
        runQueued();
        assertThat(expandedWhenTold).hasSize(1);

        fromFlutter(tree, "Tree/Collapse", item(items[0]));
        runQueued();
        assertThat(items[0].getExpanded()).isFalse();
        assertThat(collapses).hasSize(1);
    }

    @Test
    @DisplayName("an expander on a leaf or on no row does nothing")
    void anExpanderOnALeafDoesNothing() {
        Tree tree = tree(SWT.SINGLE);
        roots(tree, 2);
        List<Event> events = record(tree, SWT.Expand);

        fromFlutter(tree, "Tree/Expand", row(1));
        fromFlutter(tree, "Tree/Expand", row(5));
        runQueued();

        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("an Expand listener that disposes the item ends the expansion")
    void anExpandListenerDisposingTheItem() {
        Tree tree = tree(SWT.SINGLE);
        TreeItem[] items = roots(tree, 2);
        new TreeItem(items[0], SWT.NONE);
        tree.addListener(SWT.Expand, e -> e.item.dispose());

        fromFlutter(tree, "Tree/Expand", row(0));
        runQueued();

        assertThat(items[0].isDisposed()).isTrue();
        assertThat(tree.getItemCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("a request for the rows of an open virtual item fills them up to the end it names")
    void aRequestForRowsFillsTheOpenItem() {
        Tree tree = tree(SWT.VIRTUAL);
        List<Integer> asked = new ArrayList<>();
        tree.addListener(SWT.SetData, e -> {
            TreeItem item = (TreeItem) e.item;
            if (item.getParentItem() == null) {
                item.setText("root " + e.index);
                item.setItemCount(40);
            } else {
                asked.add(e.index);
            }
        });
        tree.setItemCount(3);
        TreeItem closed = tree.getItem(0);
        TreeItem open = tree.getItem(1);
        open.setExpanded(true);
        asked.clear();
        Event request = new Event();
        request.index = (int) FlutterBridge.id(open);
        request.end = 30;
        Event toClosed = new Event();
        toClosed.index = (int) FlutterBridge.id(closed);
        toClosed.end = 30;

        fromFlutter(tree, "SetData/SetData", request);
        fromFlutter(tree, "SetData/SetData", toClosed);
        runQueued();

        assertThat(asked).hasSize(30 - 16);
        assertThat(open.getItem(29).getText()).isEmpty();
    }

    @Test
    @DisplayName("a request for rows finds an open item under another open item")
    void aRequestFindsANestedOpenItem() {
        Tree tree = tree(SWT.VIRTUAL);
        List<TreeItem> asked = new ArrayList<>();
        tree.addListener(SWT.SetData, e -> {
            TreeItem item = (TreeItem) e.item;
            asked.add(item);
            item.setItemCount(item.getParentItem() == null ? 2 : 20);
        });
        tree.setItemCount(2);
        TreeItem root = tree.getItem(1);
        root.setExpanded(true);
        TreeItem nested = root.getItem(1);
        nested.getText();
        nested.setExpanded(true);
        tree.getItem(0).dispose();
        asked.clear();
        Event request = new Event();
        request.index = (int) FlutterBridge.id(nested);
        request.end = 18;

        fromFlutter(tree, "SetData/SetData", request);
        runQueued();

        assertThat(asked).hasSize(18 - 16).allMatch(item -> item.getParentItem() == nested);
    }

    @Test
    @DisplayName("a request for rows on a tree that is not virtual does nothing")
    void aRequestOnANonVirtualTree() {
        Tree tree = tree(SWT.SINGLE);
        TreeItem[] items = roots(tree, 2);
        new TreeItem(items[0], SWT.NONE);
        items[0].setExpanded(true);
        List<Event> events = record(tree, SWT.SetData);
        Event request = new Event();
        request.index = (int) FlutterBridge.id(items[0]);
        request.end = 5;

        fromFlutter(tree, "SetData/SetData", request);
        runQueued();

        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("a scroll the client reports moves the item at the top")
    void aScrollMovesTheTopItem() {
        Tree tree = tree(SWT.SINGLE);
        tree.setBounds(0, 0, 200, 100);
        TreeItem[] items = roots(tree, 40);
        Event scroll = new Event();
        scroll.y = 7 * tree.getItemHeight();

        fromFlutter(tree, "Scroll/Scroll", scroll);
        runQueued();

        assertThat(tree.getTopItem()).isSameAs(items[7]);
    }

    @Test
    @DisplayName("the rows the client paints over are handed to a PaintItem listener")
    void paintedRowsReachTheOwnerDrawOverlay() {
        Tree tree = tree(SWT.SINGLE);
        TreeItem[] items = roots(tree, 2);
        tree.addListener(SWT.PaintItem, e -> {});
        Event paint = new Event();
        paint.segments = new int[] {(int) FlutterBridge.id(items[1])};

        fromFlutter(tree, "PaintItem/PaintItem", paint);
        runQueued();

        assertThat(OwnerDrawOverlay.isListening(items[1])).isTrue();
        assertThat(OwnerDrawOverlay.isListening(items[0])).isFalse();
    }
}
