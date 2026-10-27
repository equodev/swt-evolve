package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * A click on a check box toggles the item's own {@code checked} before the Selection listener
 * runs, as native SWT does, and leaves {@code grayed} alone. A listener that sets the item back
 * therefore leaves it exactly as it was.
 */
class TreeCheckClickTest extends SerializeTestBase {

    private Tree tree;
    private TreeItem first;
    private TreeItem second;

    private void createTree() {
        Shell shell = shell();
        DartDisplay displayImpl = (DartDisplay) shell.getDisplay().getImpl();
        // Deliver events to the widget's listeners, like the real display does.
        doAnswer(inv -> {
            EventTable table = inv.getArgument(0);
            Event event = inv.getArgument(1);
            if (table != null)
                table.sendEvent(event);
            return null;
        }).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        tree = new Tree(shell, SWT.CHECK);
        first = new TreeItem(tree, SWT.NONE);
        first.setText("First");
        second = new TreeItem(tree, SWT.NONE);
        second.setText("Second");
    }

    private void clickCheckBox(int visibleIndex) {
        Event event = new Event();
        event.index = visibleIndex;
        event.detail = SWT.CHECK;
        TreeHelper.sendSelection((DartTree) tree.getImpl(), event, SWT.Selection);
    }

    @Test
    void a_click_toggles_the_item_before_the_listener_runs() {
        createTree();
        List<Boolean> seen = new ArrayList<>();
        tree.addListener(SWT.Selection, e -> seen.add(((TreeItem) e.item).getChecked()));

        clickCheckBox(1);
        clickCheckBox(1);

        assertThat(seen).containsExactly(true, false);
        assertThat(first.getChecked()).isFalse();
    }

    @Test
    void a_click_on_a_grayed_item_the_listener_reverts_changes_nothing() {
        createTree();
        second.setChecked(true);
        second.setGrayed(true);
        tree.addListener(SWT.Selection, e -> {
            TreeItem item = (TreeItem) e.item;
            item.setChecked(!item.getChecked());
        });

        clickCheckBox(1);

        assertThat(second.getChecked()).isTrue();
        assertThat(second.getGrayed()).isTrue();
    }

    @Test
    void a_click_on_a_grayed_item_unchecks_it_and_keeps_it_grayed() {
        createTree();
        second.setChecked(true);
        second.setGrayed(true);

        clickCheckBox(1);

        assertThat(second.getChecked()).isFalse();
        assertThat(second.getGrayed()).isTrue();
    }
}
