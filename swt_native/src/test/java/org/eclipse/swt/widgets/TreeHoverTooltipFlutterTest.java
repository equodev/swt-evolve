package org.eclipse.swt.widgets;

import dev.equo.swt.MockFlutterBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("flutter-it")
@ExtendWith(MockFlutterBridge.Extension.class)
class TreeHoverTooltipFlutterTest {

    private static final int RENDERED_HEADER_HEIGHT = 32;

    private Display display;
    private Shell shell;

    @BeforeEach
    void setUp() {
        display = new Display();
        shell = new Shell(display);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
    }

    @Test
    void hovering_a_row_shows_that_rows_tooltip() {
        Tree tree = new Tree(shell, SWT.BORDER);
        tree.setHeaderVisible(true);

        TreeItem item1 = new TreeItem(tree, SWT.NONE);
        item1.setText("Binary Statement");
        TreeItem item2 = new TreeItem(tree, SWT.NONE);
        item2.setText("If Statement");
        TreeItem item3 = new TreeItem(tree, SWT.NONE);
        item3.setText("Else Statement");

        tree.addListener(SWT.MouseHover, event -> {
            TreeItem hovered = tree.getItem(new Point(event.x, event.y));
            tree.setToolTipText(hovered != null ? hovered.getText() : null);
        });

        int headerHeight = RENDERED_HEADER_HEIGHT;
        int itemHeight = tree.getItemHeight();

        hoverAt(tree, headerHeight + itemHeight / 2);
        assertThat(tree.getToolTipText()).as("hover on row 1").isEqualTo("Binary Statement");

        hoverAt(tree, headerHeight + itemHeight + itemHeight / 2);
        assertThat(tree.getToolTipText()).as("hover on row 2").isEqualTo("If Statement");

        hoverAt(tree, headerHeight + 2 * itemHeight + itemHeight / 2);
        assertThat(tree.getToolTipText()).as("hover on row 3 (last item)").isEqualTo("Else Statement");
    }

    private static void hoverAt(Tree tree, int y) {
        Event event = new Event();
        event.x = 10;
        event.y = y;
        tree.notifyListeners(SWT.MouseHover, event);
    }
}
