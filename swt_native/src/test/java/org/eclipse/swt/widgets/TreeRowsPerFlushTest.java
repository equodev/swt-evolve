package org.eclipse.swt.widgets;

import dev.equo.swt.Serializer;
import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.tree;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * An owner-drawing listener asks each item it paints where its row is, as a styled label provider
 * does. A flush counts the tree's rows once for all of them, and the next flush counts them again:
 * the rows an item was given are the rows the tree had when that flush was written.
 */
class TreeRowsPerFlushTest extends SerializeTestBase {

    private static final int ROW = 20;

    @Test
    void eachFlushGivesTheRowsTheTreeHasThen() {
        Tree tree = tree();
        DartTree impl = (DartTree) tree.getImpl();
        when(impl.hooks(SWT.PaintItem)).thenReturn(true);
        when(impl.getItemHeight()).thenReturn(ROW);
        Map<String, Integer> rows = new LinkedHashMap<>();
        doAnswer(invocation -> {
            Event event = invocation.getArgument(1);
            TreeItem item = (TreeItem) event.item;
            rows.put(item.getText(), (item.getBounds().y - impl.getHeaderHeight()) / ROW);
            return null;
        }).when(impl).sendEvent(eq(SWT.PaintItem), any(Event.class));
        TreeItem first = item(tree, "first");
        TreeItem second = item(tree, "second");
        TreeItem third = item(tree, "third");
        when(impl.getApi()).thenReturn(tree);
        when(tree.getItems()).thenReturn(new TreeItem[] { first, second, third });

        flush(first, second, third);

        assertThat(rows).containsEntry("first", 0).containsEntry("second", 1).containsEntry("third", 2);

        TreeItem added = item(tree, "added");
        when(tree.getItems()).thenReturn(new TreeItem[] { added, first, second, third });
        rows.clear();
        flush(added, first, second, third);

        assertThat(rows).containsEntry("added", 0).containsEntry("first", 1).containsEntry("second", 2)
                .containsEntry("third", 3);
    }

    private static TreeItem item(Tree parent, String text) {
        TreeItem item = new TreeItem(parent, SWT.NONE);
        item.setText(text);
        return item;
    }

    /** One flush that writes {@code items}, as the bridge writes every dirty item of a tree in one. */
    private static void flush(TreeItem... items) {
        Serializer.Tape tape = new Serializer.Tape(0);
        for (TreeItem item : items) tape.describe((DartWidget) item.getImpl());
        Serializer.discardWritten();
        tape.release();
    }
}
