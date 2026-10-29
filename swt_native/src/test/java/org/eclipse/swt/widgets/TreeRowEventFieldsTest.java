package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * The client locates a row by its visible index and says which button and click made the event; a
 * native tree's Selection, DefaultSelection, Expand and Collapse carry the item alone, with no
 * index, button or count. A MULTI tree's client names every selected row, and Java selects them all.
 */
class TreeRowEventFieldsTest extends SerializeTestBase {

    private Tree tree;

    private void createTree() {
        createTree(SWT.NONE);
    }

    private void createTree(int style) {
        Shell shell = shell();
        DartDisplay displayImpl = (DartDisplay) shell.getDisplay().getImpl();
        doAnswer(inv -> {
            EventTable table = inv.getArgument(0);
            Event event = inv.getArgument(1);
            if (table != null)
                table.sendEvent(event);
            return null;
        }).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        tree = new Tree(shell, style);
        new TreeItem(tree, SWT.NONE).setText("alpha");
        TreeItem parent = new TreeItem(tree, SWT.NONE);
        parent.setText("bravo");
        new TreeItem(parent, SWT.NONE).setText("bravo one");
    }

    private static Event fromClient() {
        Event event = new Event();
        event.index = 1;
        event.button = 1;
        event.count = 1;
        event.stateMask = SWT.BUTTON1;
        return event;
    }

    private List<Event> record(int... types) {
        List<Event> seen = new ArrayList<>();
        for (int type : types) tree.addListener(type, seen::add);
        return seen;
    }

    @Test
    void a_row_selection_carries_the_item_and_the_button_held_only() {
        createTree();
        List<Event> seen = record(SWT.Selection, SWT.DefaultSelection);

        TreeHelper.sendSelection((DartTree) tree.getImpl(), fromClient(), SWT.Selection);
        TreeHelper.sendSelection((DartTree) tree.getImpl(), fromClient(), SWT.DefaultSelection);

        assertThat(seen).hasSize(2).allSatisfy(e -> {
            assertThat(((TreeItem) e.item).getText()).isEqualTo("bravo");
            assertThat(e.index).isZero();
            assertThat(e.button).isZero();
            assertThat(e.count).isZero();
            assertThat(e.stateMask).isEqualTo(SWT.BUTTON1);
        });
    }

    @Test
    void an_expand_and_a_collapse_carry_the_item_only() {
        createTree();
        List<Event> seen = record(SWT.Expand, SWT.Collapse);

        TreeHelper.sendExpand((DartTree) tree.getImpl(), fromClient(), true);
        TreeHelper.sendExpand((DartTree) tree.getImpl(), fromClient(), false);

        assertThat(seen).hasSize(2).allSatisfy(e -> {
            assertThat(((TreeItem) e.item).getText()).isEqualTo("bravo");
            assertThat(e.index).isZero();
            assertThat(e.button).isZero();
            assertThat(e.count).isZero();
        });
    }

    @Test
    void a_multi_selection_selects_every_row_the_client_names() {
        createTree(SWT.MULTI);
        TreeItem alpha = tree.getItem(0);
        TreeItem bravo = tree.getItem(1);
        List<Event> seen = record(SWT.Selection);
        Event event = fromClient();
        event.segments = new int[] {(int) FlutterBridge.id(alpha), (int) FlutterBridge.id(bravo)};

        TreeHelper.sendSelection((DartTree) tree.getImpl(), event, SWT.Selection);

        assertThat(tree.getSelection()).containsExactly(alpha, bravo);
        assertThat(seen).singleElement().satisfies(e -> {
            assertThat(e.item).isSameAs(bravo);
            assertThat(e.segments).isNull();
        });
    }
}
