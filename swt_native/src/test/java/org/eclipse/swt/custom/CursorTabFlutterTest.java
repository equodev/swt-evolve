package org.eclipse.swt.custom;

import dev.equo.swt.harness.UserInput;
import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
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
 * Tab reaches a Table, as on every native; from a focused TableCursor or TreeCursor it traverses to
 * its Table or Tree, whose FocusIn hands the focus back to the cursor, with the activation Win32 and
 * GTK send.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CursorTabFlutterTest {

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
    void tab_on_a_table_cursor_passes_through_the_table_and_back() {
        Table table = new Table(shell, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
        for (String header : new String[] {"Name", "Value"}) {
            TableColumn column = new TableColumn(table, SWT.NONE);
            column.setText(header);
            column.setWidth(100);
        }
        TableItem alpha = new TableItem(table, SWT.NONE);
        alpha.setText(new String[] {"alpha", "0"});
        new TableItem(table, SWT.NONE).setText(new String[] {"bravo", "1"});
        table.setBounds(20, 20, 260, 150);
        TableCursor cursor = new TableCursor(table, SWT.NONE);
        cursor.setSelection(alpha, 0);
        assertTabReturnsToCursor(table, cursor, "Table", "TableCursor");
    }

    @Test
    void tab_on_a_tree_cursor_passes_through_the_tree_and_back() {
        Tree tree = new Tree(shell, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
        for (String header : new String[] {"Name", "Value"}) {
            TreeColumn column = new TreeColumn(tree, SWT.NONE);
            column.setText(header);
            column.setWidth(100);
        }
        TreeItem alpha = new TreeItem(tree, SWT.NONE);
        alpha.setText(new String[] {"alpha", "0"});
        new TreeItem(tree, SWT.NONE).setText(new String[] {"bravo", "1"});
        tree.setSelection(alpha);
        tree.setBounds(20, 20, 260, 150);
        TreeCursor cursor = new TreeCursor(tree, SWT.NONE);
        cursor.setSelection(alpha, 0);
        assertTabReturnsToCursor(tree, cursor, "Tree", "TreeCursor");
    }

    @Test
    void tab_from_a_button_reaches_a_table() {
        org.eclipse.swt.widgets.Button button = new org.eclipse.swt.widgets.Button(shell, SWT.PUSH);
        button.setText("B");
        button.setBounds(10, 200, 80, 30);
        Table table = new Table(shell, SWT.SINGLE | SWT.BORDER);
        new TableItem(table, SWT.NONE).setText("alpha");
        table.setBounds(20, 20, 260, 150);
        flutter.settle();
        double[] rect = flutter.renderedRect(button);
        flutter.input().click(rect[0] + rect[2] / 2, rect[1] + rect[3] / 2);
        flutter.settle();
        assertThat(display.getFocusControl()).as("the focus after the click").isSameAs(button);
        flutter.input().press(UserInput.Key.TAB);
        flutter.settle();
        assertThat(display.getFocusControl()).as("the focus after Tab").isSameAs(table);
    }

    private void assertTabReturnsToCursor(Control parent, Control cursor, String parentName, String cursorName) {
        shell.forceFocus();
        flutter.settle();
        double[] rect = flutter.renderedRect(cursor);
        flutter.input().click(rect[0] + rect[2] / 2, rect[1] + rect[3] / 2);
        flutter.settle();
        assertThat(display.getFocusControl()).as("the focus after the click").isSameAs(cursor);

        // Filters, as the natives' event order is read: they run before the cursor's own Table listener.
        List<String> focus = new ArrayList<>();
        int[] types = {SWT.FocusIn, SWT.FocusOut, SWT.Activate, SWT.Deactivate};
        String[] names = {"FocusIn", "FocusOut", "Activate", "Deactivate"};
        org.eclipse.swt.widgets.Listener[] filters = new org.eclipse.swt.widgets.Listener[types.length];
        for (int i = 0; i < types.length; i++) {
            String name = names[i];
            filters[i] = e -> {
                if (e.widget == parent)
                    focus.add(parentName + "." + name);
                else if (e.widget == cursor)
                    focus.add(cursorName + "." + name);
            };
            display.addFilter(types[i], filters[i]);
        }
        try {
            pressTabTwice(cursor, focus, parentName, cursorName);
        } finally {
            for (int i = 0; i < types.length; i++)
                display.removeFilter(types[i], filters[i]);
        }
    }

    private void pressTabTwice(Control cursor, List<String> focus, String parentName, String cursorName) {
        List<String> cycle = List.of(cursorName + ".FocusOut", parentName + ".FocusIn", parentName + ".FocusOut",
                cursorName + ".FocusIn");
        for (int tab = 1; tab <= 2; tab++) {
            focus.clear();
            flutter.input().press(UserInput.Key.TAB);
            flutter.settle();
            // The parent's activation, once its FocusIn returns, deactivates the cursor; the second
            // Tab finds the parent active, so the cursor's FocusIn activates it first.
            List<String> expected = new ArrayList<>(cycle);
            if (tab == 2)
                expected.add(cursorName + ".Activate");
            expected.add(cursorName + ".Deactivate");
            assertThat(focus).as("the focus changes Tab " + tab + " made").containsExactlyElementsOf(expected);
            assertThat(display.getFocusControl()).as("the focus after Tab " + tab).isSameAs(cursor);
        }
    }
}
