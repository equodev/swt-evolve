package org.eclipse.swt.widgets;

import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.TextLayout;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An owner-drawn row is described with an empty list of painted texts until the client mounts its
 * overlay, and that empty list is what makes the client mount it. The client has to receive it
 * empty, not left out as if the row drew nothing of its own, or no overlay ever listens and the row
 * shows none of what its listeners draw.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OwnerDrawOverlayMountsFlutterTest {

    private static final long MOUNT_TIMEOUT_MS = 5_000;

    private final WebDisplayHarness flutter = new WebDisplayHarness();
    private Display display;
    private Shell shell;
    private TextLayout layout;

    @BeforeAll
    void boot() {
        display = flutter.boot();
        shell = new Shell(display);
        shell.setBounds(0, 0, 400, 300);
        shell.open();
        layout = new TextLayout(display);
    }

    @AfterAll
    void shutdown() {
        layout.dispose();
        flutter.teardown();
    }

    @AfterEach
    void disposeChildren() {
        for (Control child : shell.getChildren())
            child.dispose();
        settle();
    }

    @Test
    void aTableRowsOverlayIsMountedByTheClient() {
        Table table = new Table(shell, SWT.SINGLE | SWT.FULL_SELECTION);
        table.setBounds(0, 0, 300, 120);
        new TableColumn(table, SWT.NONE).setWidth(280);
        drawsThroughATextLayout(table);
        TableItem[] rows = new TableItem[3];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = new TableItem(table, SWT.NONE);
            rows[i].setText("row " + i);
        }
        settle();

        assertOverlaysListen(rows);
    }

    @Test
    void aTreeRowsOverlayIsMountedByTheClient() {
        Tree tree = new Tree(shell, SWT.SINGLE | SWT.FULL_SELECTION);
        tree.setBounds(0, 0, 300, 120);
        drawsThroughATextLayout(tree);
        TreeItem[] rows = new TreeItem[3];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = new TreeItem(tree, SWT.NONE);
            rows[i].setText("row " + i);
        }
        settle();

        assertOverlaysListen(rows);
    }

    /** The client mounts a row's overlay once it has drawn the row, which can be after the Display is quiet. */
    private void assertOverlaysListen(Item... rows) {
        flutter.pumpUntil(() -> Arrays.stream(rows).allMatch(OwnerDrawOverlay::isListening), MOUNT_TIMEOUT_MS);
        for (Item row : rows)
            assertThat(OwnerDrawOverlay.isListening(row)).as("%s's overlay listens", row.getText()).isTrue();
    }

    /** As Eclipse's Quick Access: the item's own text suppressed, the cell's text drawn through a layout. */
    private void drawsThroughATextLayout(Control owner) {
        owner.addListener(SWT.MeasureItem, e -> e.height = 22);
        owner.addListener(SWT.EraseItem, e -> e.detail &= ~SWT.FOREGROUND);
        owner.addListener(SWT.PaintItem, e -> {
            layout.setText(((Item) e.item).getText());
            layout.draw(e.gc, e.x + 4, e.y + 3);
        });
    }

    private void settle() {
        int quiet = 0;
        for (int round = 0; round < 40 && quiet < 3; round++) {
            long before = flutter.dispatched();
            flutter.drain();
            flutter.flush();
            flutter.drain();
            quiet = flutter.dispatched() == before ? quiet + 1 : 0;
        }
    }
}
