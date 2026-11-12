package org.eclipse.swt.custom;

import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A click on a cell away from a TableCursor or TreeCursor focuses the Table or Tree, whose FocusIn
 * hands the focus on to the cursor, once, as on Win32 and GTK: the client then follows the cursor and
 * does not give the focus back to the Table or Tree it pressed.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CursorCellClickFocusFlutterTest {

    private static final String[] ROWS = {"alpha", "bravo", "charlie", "delta"};

    private final WebDisplayHarness flutter = new WebDisplayHarness();
    private Display display;
    private Shell shell;

    @BeforeAll
    void boot() {
        display = flutter.boot();
        shell = new Shell(display);
        shell.setBounds(0, 0, 400, 300);
        shell.open();
    }

    @AfterAll
    void shutdown() {
        flutter.teardown();
    }

    @AfterEach
    void disposeChildren() {
        for (Control child : shell.getChildren())
            child.dispose();
        flutter.settle();
    }

    @Test
    void a_click_on_another_table_cell_hands_the_focus_to_the_cursor_once() {
        Table table = new Table(shell, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
        for (String header : new String[] {"Name", "Value"}) {
            TableColumn column = new TableColumn(table, SWT.NONE);
            column.setText(header);
            column.setWidth(100);
        }
        for (int i = 0; i < ROWS.length; i++)
            new TableItem(table, SWT.NONE).setText(new String[] {ROWS[i], String.valueOf(i)});
        table.setSelection(0);
        table.setBounds(20, 20, 260, 150);
        TableCursor cursor = new TableCursor(table, SWT.NONE);
        cursor.setSelection(0, 0);

        List<String> focus = clickTwoRowsBelow(table, "Table", cursor, "TableCursor");

        assertThat(focus).as("the focus changes the click made")
                .containsExactly("Table.FocusIn", "Table.FocusOut", "TableCursor.FocusIn");
        assertThat(display.getFocusControl()).as("the focus control").isSameAs(cursor);
        assertThat(cursor.getRow().getText()).as("the cursor's row").isEqualTo("charlie");
    }

    @Test
    void a_click_on_another_tree_cell_hands_the_focus_to_the_cursor_once() {
        Tree tree = new Tree(shell, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
        for (String header : new String[] {"Name", "Value"}) {
            TreeColumn column = new TreeColumn(tree, SWT.NONE);
            column.setText(header);
            column.setWidth(100);
        }
        for (int i = 0; i < ROWS.length; i++)
            new TreeItem(tree, SWT.NONE).setText(new String[] {ROWS[i], String.valueOf(i)});
        tree.setSelection(tree.getItem(0));
        tree.setBounds(20, 20, 260, 150);
        TreeCursor cursor = new TreeCursor(tree, SWT.NONE);
        cursor.setSelection(tree.getItem(0), 0);

        List<String> focus = clickTwoRowsBelow(tree, "Tree", cursor, "TreeCursor");

        assertThat(focus).as("the focus changes the click made")
                .containsExactly("Tree.FocusIn", "Tree.FocusOut", "TreeCursor.FocusIn");
        assertThat(display.getFocusControl()).as("the focus control").isSameAs(cursor);
        assertThat(cursor.getRow().getText()).as("the cursor's row").isEqualTo("charlie");
    }

    /** Clicks the cell two rows under the cursor, where the client draws it, and returns the focus changes. */
    private List<String> clickTwoRowsBelow(Control parent, String parentName, Control cursor, String cursorName) {
        shell.forceFocus();
        flutter.settle();
        List<String> focus = new ArrayList<>();
        // Filters, which see each event before any listener: the cursor's own FocusIn listener on its
        // parent moves the focus on from inside the parent's FocusIn.
        Listener log = e -> {
            String name = e.widget == parent ? parentName : e.widget == cursor ? cursorName : null;
            if (name != null) focus.add(name + (e.type == SWT.FocusIn ? ".FocusIn" : ".FocusOut"));
        };
        display.addFilter(SWT.FocusIn, log);
        display.addFilter(SWT.FocusOut, log);
        double[] rect = flutter.renderedRect(cursor);
        flutter.input().click(rect[0] + rect[2] / 2, rect[1] + rect[3] * 2.5);
        flutter.settle();
        display.removeFilter(SWT.FocusIn, log);
        display.removeFilter(SWT.FocusOut, log);
        return focus;
    }
}
