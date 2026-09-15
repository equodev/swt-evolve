package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * How a cell editor ends: the application disposes the control it placed over the cell. The
 * {@link TableEditor} outlives that — it is reused for the next cell — so nothing in SWT clears its
 * reference, and a Dart-backed table has no OS to make the dead control disappear. It stops
 * existing for the client only if it stops being serialized under the table's editors.
 */
@Tag("flutter-it")
class TableCellEditorDisposeFlutterTest {

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

    @Test
    void disposingTheEditorControlTakesItOutOfTheTablesEditors() {
        Shell shell = new Shell(display);
        Table table = new Table(shell, SWT.NONE);
        new TableColumn(table, SWT.NONE);
        TableItem row = new TableItem(table, SWT.NONE);
        TableEditor cellEditor = new TableEditor(table);

        Text editor = new Text(table, SWT.SINGLE);
        cellEditor.setEditor(editor, row, 0);
        assertThat(cellEditor.getEditor())
                .as("sanity: the open editor is the control that was placed over the cell")
                .isSameAs(editor);

        DartTable impl = (DartTable) table.getImpl();
        impl.getValue().changedKeys().clear();

        editor.dispose();

        assertThat(cellEditor.getEditor())
                .as("a disposed editor control must not stay in the table's editors: the client "
                        + "would keep its overlay mounted over a control that no longer exists")
                .isNull();
        assertThat(impl.getValue().changedKeys())
                .as("the client only learns what an update names, and nothing else names the "
                        + "editors of a table whose child was disposed")
                .contains(VTable.EDITORS);
    }

    @Test
    void disposingOneEditorControlLeavesAnotherEditorAlone() {
        Shell shell = new Shell(display);
        Table table = new Table(shell, SWT.NONE);
        new TableColumn(table, SWT.NONE);
        new TableColumn(table, SWT.NONE);
        TableItem row = new TableItem(table, SWT.NONE);

        TableEditor first = new TableEditor(table);
        Text firstControl = new Text(table, SWT.SINGLE);
        first.setEditor(firstControl, row, 0);

        TableEditor second = new TableEditor(table);
        Button secondControl = new Button(table, SWT.CHECK);
        second.setEditor(secondControl, row, 1);

        firstControl.dispose();

        assertThat(first.getEditor()).isNull();
        assertThat(second.getEditor())
                .as("a table can carry a permanent control on every row; disposing one must not "
                        + "take the others down with it")
                .isSameAs(secondControl);
    }
}
