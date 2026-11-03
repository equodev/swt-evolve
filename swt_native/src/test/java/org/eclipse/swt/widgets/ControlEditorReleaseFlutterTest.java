package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.custom.TreeEditor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Replacing or clearing an editor's control only makes the editor forget it, as in SWT: the
 * application owns the control and may hide it, move it to another cell or reuse it later. What a
 * Dart-backed table or tree must do instead is stop drawing the control over the cell, which it
 * learns only from an update that names its editors.
 */
@Tag("flutter-it")
class ControlEditorReleaseFlutterTest {

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

    private static List<Integer> hides(Control control) {
        List<Integer> hides = new ArrayList<>();
        control.addListener(SWT.Hide, e -> hides.add(e.type));
        return hides;
    }

    private static void assertUntouched(Control control, List<Integer> hides) {
        assertThat(control.isDisposed()).as("the application still owns the control").isFalse();
        assertThat(control.getVisible()).as("SWT leaves the control's visibility to the application").isTrue();
        assertThat(hides).as("SWT fires no Hide on the control").isEmpty();
    }

    @Test
    void clearingAControlEditorLeavesItsControlAlone() {
        Shell shell = new Shell(display);
        Composite host = new Composite(shell, SWT.NONE);
        Text text = new Text(host, SWT.NONE);
        ControlEditor editor = new ControlEditor(host);
        editor.setEditor(text);
        List<Integer> hides = hides(text);

        editor.setEditor(null);

        assertThat(editor.getEditor()).isNull();
        assertUntouched(text, hides);
    }

    @Test
    void clearingATableEditorLeavesItsControlAloneAndUpdatesTheTable() {
        Shell shell = new Shell(display);
        Table table = new Table(shell, SWT.NONE);
        new TableColumn(table, SWT.NONE);
        TableItem row = new TableItem(table, SWT.NONE);
        TableEditor editor = new TableEditor(table);
        Text text = new Text(table, SWT.NONE);
        editor.setEditor(text, row, 0);
        List<Integer> hides = hides(text);
        DartTable impl = (DartTable) table.getImpl();
        impl.getValue().changedKeys().clear();

        editor.setEditor(null, null, 0);

        assertThat(editor.getEditor()).isNull();
        assertUntouched(text, hides);
        assertThat(impl.getValue().changedKeys())
                .as("the client stops drawing the control over the cell only when told the editors changed")
                .contains(VTable.EDITORS);
    }

    @Test
    void replacingATableEditorControlLeavesThePreviousOneAlone() {
        Shell shell = new Shell(display);
        Table table = new Table(shell, SWT.NONE);
        new TableColumn(table, SWT.NONE);
        new TableItem(table, SWT.NONE);
        TableItem row1 = new TableItem(table, SWT.NONE);
        TableEditor editor = new TableEditor(table);
        Text first = new Text(table, SWT.NONE);
        editor.setEditor(first, table.getItem(0), 0);
        List<Integer> hides = hides(first);
        DartTable impl = (DartTable) table.getImpl();
        impl.getValue().changedKeys().clear();

        Text second = new Text(table, SWT.NONE);
        editor.setEditor(second, row1, 0);

        assertThat(editor.getEditor()).isSameAs(second);
        assertUntouched(first, hides);
        assertThat(impl.getValue().changedKeys()).contains(VTable.EDITORS);
    }

    @Test
    void clearingATreeEditorLeavesItsControlAloneAndUpdatesTheTree() {
        Shell shell = new Shell(display);
        Tree tree = new Tree(shell, SWT.NONE);
        TreeItem item = new TreeItem(tree, SWT.NONE);
        TreeEditor editor = new TreeEditor(tree);
        Text text = new Text(tree, SWT.NONE);
        editor.setEditor(text, item, 0);
        List<Integer> hides = hides(text);
        DartTree impl = (DartTree) tree.getImpl();
        impl.getValue().changedKeys().clear();

        editor.setEditor(null, null, 0);

        assertThat(editor.getEditor()).isNull();
        assertUntouched(text, hides);
        assertThat(impl.getValue().changedKeys())
                .as("the client stops drawing the control over the item only when told the editors changed")
                .contains(VTree.EDITORS);
    }

    @Test
    void replacingATreeEditorControlLeavesThePreviousOneAlone() {
        Shell shell = new Shell(display);
        Tree tree = new Tree(shell, SWT.NONE);
        TreeItem first = new TreeItem(tree, SWT.NONE);
        TreeItem second = new TreeItem(tree, SWT.NONE);
        TreeEditor editor = new TreeEditor(tree);
        Text firstText = new Text(tree, SWT.NONE);
        editor.setEditor(firstText, first, 0);
        List<Integer> hides = hides(firstText);
        DartTree impl = (DartTree) tree.getImpl();
        impl.getValue().changedKeys().clear();

        Text secondText = new Text(tree, SWT.NONE);
        editor.setEditor(secondText, second, 0);

        assertThat(editor.getEditor()).isSameAs(secondText);
        assertUntouched(firstText, hides);
        assertThat(impl.getValue().changedKeys()).contains(VTree.EDITORS);
    }
}
