package org.eclipse.swt.custom;

import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
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
 * A click on a TreeCursor focuses the cursor and nothing else, as on Win32: the tree it lies in
 * never takes the focus on the way.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TreeCursorFocusFlutterTest {

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
    void disposeTrees() {
        for (Control child : shell.getChildren())
            child.dispose();
        flutter.settle();
    }

    @Test
    void a_click_on_the_cursor_focuses_only_the_cursor() {
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
        shell.forceFocus();
        flutter.settle();

        List<String> focus = new ArrayList<>();
        tree.addListener(SWT.FocusIn, e -> focus.add("Tree.FocusIn"));
        tree.addListener(SWT.FocusOut, e -> focus.add("Tree.FocusOut"));
        cursor.addListener(SWT.FocusIn, e -> focus.add("TreeCursor.FocusIn"));
        cursor.addListener(SWT.FocusOut, e -> focus.add("TreeCursor.FocusOut"));
        double[] rect = flutter.renderedRect(cursor);
        flutter.input().click(rect[0] + rect[2] / 2, rect[1] + rect[3] / 2);
        flutter.settle();

        assertThat(focus).as("the focus changes the click made").containsExactly("TreeCursor.FocusIn");
        assertThat(display.getFocusControl()).as("the focus control").isSameAs(cursor);
    }
}
