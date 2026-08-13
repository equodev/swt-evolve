package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("flutter-it")
class TableNoColumnsSizeFlutterTest {

    private static final int OLD_FIXED_WIDTH = 70 + 17;

    private Display display;

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
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) {
            display.dispose();
        }
        FlutterBridge.set(null);
    }

    private static Table tableWith(Shell shell, String... items) {
        Table table = new Table(shell, SWT.BORDER | SWT.V_SCROLL);
        for (String item : items) {
            new TableItem(table, SWT.NONE).setText(item);
        }
        return table;
    }

    @Test
    void aColumnlessTableIsWideEnoughForItsWidestItem() {
        Shell shell = new Shell(display);
        Table table = tableWith(shell, "Current range", "Range linked to current", "Editable range",
                "Final caret location");

        Point size = table.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        assertThat(size.x)
                .as("a columnless table must size to its widest item, not to a fixed default")
                .isGreaterThan(OLD_FIXED_WIDTH);
    }

    @Test
    void theWidestItemDrivesTheWidth() {
        Shell shell = new Shell(display);
        Table narrow = tableWith(shell, "ab");
        Table wide = tableWith(shell, "a considerably longer annotation label");

        assertThat(wide.computeSize(SWT.DEFAULT, SWT.DEFAULT).x)
                .as("width must grow with the widest item's text")
                .isGreaterThan(narrow.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
    }

    @Test
    void aCheckTableReservesRoomForTheCheckbox() {
        Shell shell = new Shell(display);
        Table plain = tableWith(shell, "Current range", "Range linked to current");
        Table checked = new Table(shell, SWT.BORDER | SWT.V_SCROLL | SWT.CHECK);
        for (String item : new String[] { "Current range", "Range linked to current" }) {
            new TableItem(checked, SWT.NONE).setText(item);
        }

        assertThat(checked.computeSize(SWT.DEFAULT, SWT.DEFAULT).x)
                .as("a CHECK table must reserve the checkbox column on top of its widest item")
                .isEqualTo(plain.computeSize(SWT.DEFAULT, SWT.DEFAULT).x + 20 + 8);
    }

    @Test
    void aWidthHintStillWins() {
        Shell shell = new Shell(display);
        Table table = tableWith(shell, "a considerably longer annotation label");

        assertThat(table.computeSize(120, SWT.DEFAULT).x).isEqualTo(120);
    }

    @Test
    void anEmptyColumnlessTableKeepsTheDefaultWidth() {
        Shell shell = new Shell(display);
        Table table = tableWith(shell);

        assertThat(table.computeSize(SWT.DEFAULT, SWT.DEFAULT).x).isEqualTo(OLD_FIXED_WIDTH);
    }

    @Test
    void aVirtualColumnlessTableIsNotMeasured() {
        Shell shell = new Shell(display);
        Table table = new Table(shell, SWT.BORDER | SWT.V_SCROLL | SWT.VIRTUAL);
        table.setItemCount(500);

        assertThat(table.computeSize(SWT.DEFAULT, SWT.DEFAULT).x).isEqualTo(OLD_FIXED_WIDTH);
    }

    @Test
    void aTableWithPackedColumnsSizesToTheSumOfItsColumnWidths() {
        // A table with columns used to fall back to a fixed columnCount*70 guess here, ignoring
        // whatever the columns were actually packed to - so a caller that packs a column to its
        // content, then reads Table.computeSize(SWT.DEFAULT, ...) to size itself against that
        // content, got a guess instead of the real width.
        Shell shell = new Shell(display);
        Table table = new Table(shell, SWT.BORDER | SWT.V_SCROLL);
        TableColumn first = new TableColumn(table, SWT.LEFT);
        first.setWidth(120);
        TableColumn second = new TableColumn(table, SWT.LEFT);
        second.setWidth(80);
        new TableItem(table, SWT.NONE).setText(new String[] {
                "a considerably longer annotation label", "and another one" });

        assertThat(table.computeSize(SWT.DEFAULT, SWT.DEFAULT).x)
                .as("width must be the sum of the packed column widths, not a fixed per-column guess")
                .isGreaterThanOrEqualTo(120 + 80);
    }
}
