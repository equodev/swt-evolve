package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * What the Dart-backed TreeCursor, TableCursor and cell editors do when they are driven directly rather than
 * through their API classes: an implementation built without its API object makes one, and reports what it
 * holds.
 */
@Tag("flutter-it")
class CellCursorAndEditorImplementationTest {

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
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("an implementation built without its API object creates one, once, bound to itself")
    void implementationsWithoutApiCreateOne() {
        Tree tree = new Tree(shell, SWT.NONE);
        Table table = new Table(shell, SWT.NONE);
        Composite composite = new Composite(shell, SWT.NONE);

        DartTreeCursor treeCursor = new DartTreeCursor(tree, SWT.NONE, null);
        assertThat(treeCursor.getApi().getImpl()).isSameAs(treeCursor);
        assertThat(treeCursor.getApi()).isSameAs(treeCursor.getApi());

        DartTableCursor tableCursor = new DartTableCursor(table, SWT.NONE, null);
        assertThat(tableCursor.getApi().getImpl()).isSameAs(tableCursor);
        assertThat(tableCursor.getApi()).isSameAs(tableCursor.getApi());

        DartTreeEditor treeEditor = new DartTreeEditor(tree, null);
        assertThat(treeEditor.getApi().getImpl()).isSameAs(treeEditor);
        assertThat(treeEditor.getApi().grabVertical).isTrue();

        DartTableEditor tableEditor = new DartTableEditor(table, null);
        assertThat(tableEditor.getApi().getImpl()).isSameAs(tableEditor);
        assertThat(tableEditor.getApi().grabVertical).isTrue();

        DartControlEditor controlEditor = new DartControlEditor(composite, null);
        assertThat(controlEditor.getApi().getImpl()).isSameAs(controlEditor);
        assertThat(controlEditor.getApi()).isSameAs(controlEditor.getApi());
    }

    @Test
    @DisplayName("a TreeCursor reports its tree, its cell, its listeners and its own colours")
    void treeCursorReportsWhatItHolds() {
        Tree tree = new Tree(shell, SWT.NONE);
        TreeColumn column = new TreeColumn(tree, SWT.NONE);
        TreeItem row = new TreeItem(tree, SWT.NONE);
        TreeCursor cursor = new TreeCursor(tree, SWT.NONE);
        DartTreeCursor impl = (DartTreeCursor) cursor.getImpl();

        assertThat(impl._tree()).isSameAs(tree);
        assertThat(impl._row()).isNull();
        assertThat(impl._column()).isNull();
        assertThat(impl._background()).isNull();
        assertThat(impl._foreground()).isNull();
        assertThat(new Object[] {impl._listener(), impl._treeListener(), impl._resizeListener(),
                impl._disposeItemListener(), impl._disposeColumnListener()}).doesNotContainNull();

        Color red = display.getSystemColor(SWT.COLOR_RED);
        cursor.setSelection(row, 0);
        cursor.setBackground(red);
        cursor.setForeground(red);
        assertThat(impl._row()).isSameAs(row);
        assertThat(impl._column()).isSameAs(column);
        assertThat(impl._background()).isSameAs(red);
        assertThat(impl._foreground()).isSameAs(red);
    }

    @Test
    @DisplayName("a tree disposed under its cursor: the cursor, moving off each released row, shows only live rows")
    void treeDisposedUnderItsCursor() {
        Tree tree = new Tree(shell, SWT.NONE);
        new TreeColumn(tree, SWT.NONE);
        new TreeItem(tree, SWT.NONE);
        TreeItem middle = new TreeItem(tree, SWT.NONE);
        new TreeItem(tree, SWT.NONE);
        TreeCursor cursor = new TreeCursor(tree, SWT.NONE);
        cursor.setSelection(middle, 0);

        assertThatCode(shell::dispose).doesNotThrowAnyException();
        assertThat(tree.isDisposed()).isTrue();
        assertThat(cursor.isDisposed()).isTrue();
    }

    @Test
    @DisplayName("a TableCursor reports its table, its cell, its listeners and its own colours")
    void tableCursorReportsWhatItHolds() {
        Table table = new Table(shell, SWT.NONE);
        TableColumn column = new TableColumn(table, SWT.NONE);
        TableItem row = new TableItem(table, SWT.NONE);
        TableCursor cursor = new TableCursor(table, SWT.NONE);
        DartTableCursor impl = (DartTableCursor) cursor.getImpl();

        assertThat(impl._table()).isSameAs(table);
        assertThat(impl._row()).isNull();
        assertThat(impl._column()).isNull();
        assertThat(impl._background()).isNull();
        assertThat(impl._foreground()).isNull();
        assertThat(new Object[] {impl._listener(), impl._tableListener(), impl._resizeListener(),
                impl._disposeItemListener(), impl._disposeColumnListener()}).doesNotContainNull();

        Color red = display.getSystemColor(SWT.COLOR_RED);
        cursor.setSelection(row, 0);
        cursor.setBackground(red);
        cursor.setForeground(red);
        assertThat(impl._row()).isSameAs(row);
        assertThat(impl._column()).isSameAs(column);
        assertThat(impl._background()).isSameAs(red);
        assertThat(impl._foreground()).isSameAs(red);
    }

    @Test
    @DisplayName("the cell editors report their parent, their cell, their control and their listeners")
    void editorsReportWhatTheyHold() {
        Tree tree = new Tree(shell, SWT.NONE);
        new TreeColumn(tree, SWT.NONE);
        TreeItem treeRow = new TreeItem(tree, SWT.NONE);
        TreeEditor treeEditor = new TreeEditor(tree);
        Text treeText = new Text(tree, SWT.NONE);
        treeEditor.setEditor(treeText, treeRow, 0);
        DartTreeEditor treeImpl = (DartTreeEditor) treeEditor.getImpl();
        assertThat(treeImpl._tree()).isSameAs(tree);
        assertThat(treeImpl._parent()).isSameAs(tree);
        assertThat(treeImpl._item()).isSameAs(treeRow);
        assertThat(treeImpl._column()).isZero();
        assertThat(treeImpl._editor()).isSameAs(treeText);
        assertThat(new Object[] {treeImpl._columnListener(), treeImpl._treeListener(), treeImpl._timer()})
                .doesNotContainNull();

        Table table = new Table(shell, SWT.NONE);
        new TableColumn(table, SWT.NONE);
        TableItem tableRow = new TableItem(table, SWT.NONE);
        TableEditor tableEditor = new TableEditor(table);
        Text tableText = new Text(table, SWT.NONE);
        tableEditor.setEditor(tableText, tableRow, 0);
        DartTableEditor tableImpl = (DartTableEditor) tableEditor.getImpl();
        assertThat(tableImpl._table()).isSameAs(table);
        assertThat(tableImpl._item()).isSameAs(tableRow);
        assertThat(tableImpl._column()).isZero();
        assertThat(tableImpl._editor()).isSameAs(tableText);
        assertThat(new Object[] {tableImpl._columnListener(), tableImpl._timer()}).doesNotContainNull();

        Composite composite = new Composite(shell, SWT.NONE);
        ControlEditor controlEditor = new ControlEditor(composite);
        Text text = new Text(composite, SWT.NONE);
        controlEditor.setEditor(text);
        DartControlEditor controlImpl = (DartControlEditor) controlEditor.getImpl();
        assertThat(controlImpl._parent()).isSameAs(composite);
        assertThat(controlImpl._editor()).isSameAs(text);
        assertThat(controlImpl.holdsEditor(text)).isTrue();
        assertThat(controlImpl.holdsEditor(null)).isFalse();
    }
}
