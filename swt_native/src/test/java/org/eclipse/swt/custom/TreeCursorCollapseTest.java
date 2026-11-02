package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A TreeCursor whose row is hidden by a programmatic collapse moves to the collapsed ancestor, as it does
 * on every native.
 */
@Tag("flutter-it")
class TreeCursorCollapseTest {

    private Display display;
    private Shell shell;

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
        shell = new Shell(display);
        shell.setSize(400, 300);
        shell.open();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("collapsing the parent of the cursor's row moves the cursor to that parent")
    void collapseMovesTheCursorToTheParent() {
        Tree tree = new Tree(shell, SWT.SINGLE | SWT.FULL_SELECTION);
        new TreeColumn(tree, SWT.NONE).setWidth(100);
        new TreeColumn(tree, SWT.NONE).setWidth(100);
        TreeItem alpha = new TreeItem(tree, SWT.NONE);
        alpha.setText(new String[] {"alpha", "0"});
        new TreeItem(alpha, SWT.NONE).setText(new String[] {"alpha.1", "1"});
        TreeItem alpha2 = new TreeItem(alpha, SWT.NONE);
        alpha2.setText(new String[] {"alpha.2", "2"});
        new TreeItem(tree, SWT.NONE).setText(new String[] {"bravo", "3"});
        alpha.setExpanded(true);
        tree.setBounds(20, 20, 260, 150);
        TreeCursor cursor = new TreeCursor(tree, SWT.NONE);
        cursor.setSelection(alpha2, 1);
        drain();
        List<Integer> events = new ArrayList<>();
        cursor.addListener(SWT.Move, e -> events.add(e.type));

        alpha.setExpanded(false);
        drain();

        assertThat(cursor.getRow()).isSameAs(alpha);
        assertThat(cursor.getColumn()).isEqualTo(1);
        assertThat(cursor.getBounds()).isEqualTo(alpha.getBounds(1));
        assertThat(events).containsExactly(SWT.Move);
    }

    @Test
    @DisplayName("a collapse repaints the tree's visible child controls, not its hidden ones")
    void collapseRepaintsOnlyVisibleChildren() {
        Tree tree = new Tree(shell, SWT.SINGLE);
        TreeItem alpha = new TreeItem(tree, SWT.NONE);
        alpha.setText("alpha");
        new TreeItem(alpha, SWT.NONE).setText("alpha.1");
        alpha.setExpanded(true);
        tree.setBounds(20, 20, 260, 150);
        Composite shown = new Composite(tree, SWT.NONE);
        shown.setBounds(0, 0, 40, 20);
        Composite hidden = new Composite(tree, SWT.NONE);
        hidden.setBounds(0, 20, 40, 20);
        hidden.setVisible(false);
        drain();
        List<Widget> painted = new ArrayList<>();
        shown.addListener(SWT.Paint, e -> painted.add(e.widget));
        hidden.addListener(SWT.Paint, e -> painted.add(e.widget));

        alpha.setExpanded(false);
        drain();

        assertThat(painted).containsExactly(shown);
    }

    @Test
    @DisplayName("a cursor placed before the tree has its size sits on its row once every row fits")
    void aCursorPlacedBeforeLayoutSitsOnItsRow() {
        Tree tree = new Tree(shell, SWT.SINGLE | SWT.FULL_SELECTION);
        tree.setHeaderVisible(true);
        new TreeColumn(tree, SWT.NONE).setWidth(100);
        new TreeColumn(tree, SWT.NONE).setWidth(100);
        TreeItem alpha = new TreeItem(tree, SWT.NONE);
        alpha.setText(new String[] {"alpha", "0"});
        new TreeItem(alpha, SWT.NONE).setText(new String[] {"alpha.1", "1"});
        TreeItem alpha2 = new TreeItem(alpha, SWT.NONE);
        alpha2.setText(new String[] {"alpha.2", "2"});
        alpha.setExpanded(true);
        TreeCursor cursor = new TreeCursor(tree, SWT.NONE);
        cursor.setSelection(alpha2, 1);
        tree.setBounds(20, 20, 260, 260);
        drain();

        assertThat(alpha2.getBounds(1).y).isEqualTo(tree.getHeaderHeight() + 2 * tree.getItemHeight());
        assertThat(tree.getTopItem()).isSameAs(alpha);

        alpha.setExpanded(false);
        drain();

        assertThat(cursor.getBounds()).isEqualTo(alpha.getBounds(1));
        assertThat(cursor.getBounds().y).isEqualTo(tree.getHeaderHeight());
    }

    private void drain() {
        while (display.readAndDispatch()) {
            // drain
        }
    }
}
