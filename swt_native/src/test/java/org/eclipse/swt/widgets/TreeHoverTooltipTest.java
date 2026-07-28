package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.swtShell;

class TreeHoverTooltipTest extends SerializeTestBase {

    private static final int RENDERED_HEADER_HEIGHT = 32;

    @Test
    @DisabledOnOs(OS.LINUX)
    void hovering_a_row_shows_that_rows_tooltip() {
        Tree tree = new Tree(swtShell(), SWT.BORDER);
        tree.setHeaderVisible(true);

        TreeItem item1 = new TreeItem(tree, SWT.NONE);
        item1.setText("Binary Statement");
        TreeItem item2 = new TreeItem(tree, SWT.NONE);
        item2.setText("If Statement");
        TreeItem item3 = new TreeItem(tree, SWT.NONE);
        item3.setText("Else Statement");

        Listener hoverListener = event -> {
            TreeItem hovered = tree.getItem(new Point(event.x, event.y));
            tree.setToolTipText(hovered != null ? hovered.getText() : null);
        };

        int headerHeight = RENDERED_HEADER_HEIGHT;
        int itemHeight = tree.getItemHeight();

        hoverAt(tree, hoverListener, headerHeight + itemHeight / 2);
        assertThat(tree.getToolTipText()).as("hover on row 1").isEqualTo("Binary Statement");

        hoverAt(tree, hoverListener, headerHeight + itemHeight + itemHeight / 2);
        assertThat(tree.getToolTipText()).as("hover on row 2").isEqualTo("If Statement");

        hoverAt(tree, hoverListener, headerHeight + 2 * itemHeight + itemHeight / 2);
        assertThat(tree.getToolTipText()).as("hover on row 3 (last item)").isEqualTo("Else Statement");
    }

    private static void hoverAt(Tree tree, Listener listener, int y) {
        Event event = new Event();
        event.type = SWT.MouseHover;
        event.widget = tree;
        event.x = 10;
        event.y = y;
        listener.handleEvent(event);
    }
}
