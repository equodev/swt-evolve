package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.DartTreeEditor;
import org.eclipse.swt.custom.TreeEditor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * How a cell editor ends: the application disposes the control it placed over the cell. The
 * {@link TreeEditor} outlives that — it is reused for the next cell — and SWT goes on answering the
 * disposed control from {@code getEditor()}. A Dart-backed tree has no OS to make the dead control
 * disappear: it stops existing for the client only if it stops being serialized under the tree's
 * editors.
 */
@Tag("flutter-it")
class TreeCellEditorDisposeFlutterTest {

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
    void disposingTheEditorControlTakesItOutOfTheTreesEditors() {
        Shell shell = new Shell(display);
        Tree tree = new Tree(shell, SWT.NONE);
        new TreeColumn(tree, SWT.NONE);
        TreeItem row = new TreeItem(tree, SWT.NONE);
        TreeEditor cellEditor = new TreeEditor(tree);

        Text editor = new Text(tree, SWT.SINGLE);
        cellEditor.setEditor(editor, row, 0);
        assertThat(cellEditor.getEditor())
                .as("sanity: the open editor is the control that was placed over the cell")
                .isSameAs(editor);

        DartTree impl = (DartTree) tree.getImpl();
        impl.getValue().changedKeys().clear();

        editor.dispose();

        assertThat(cellEditor.getEditor())
                .as("SWT's TreeEditor keeps answering the control it was given")
                .isSameAs(editor);
        assertThat(((DartTreeEditor) cellEditor.getImpl()).getValue().getEditor())
                .as("a disposed editor control must not stay in the tree's editors: the client "
                        + "would keep its overlay mounted over a control that no longer exists")
                .isNull();
        assertThat(impl.getValue().changedKeys())
                .as("the client only learns what an update names, and nothing else names the "
                        + "editors of a tree whose child was disposed")
                .contains(VTree.EDITORS);
    }

    @Test
    void disposingOneEditorControlLeavesAnotherEditorAlone() {
        Shell shell = new Shell(display);
        Tree tree = new Tree(shell, SWT.NONE);
        new TreeColumn(tree, SWT.NONE);
        new TreeColumn(tree, SWT.NONE);
        TreeItem row = new TreeItem(tree, SWT.NONE);

        TreeEditor first = new TreeEditor(tree);
        Text firstControl = new Text(tree, SWT.SINGLE);
        first.setEditor(firstControl, row, 0);

        TreeEditor second = new TreeEditor(tree);
        Button secondControl = new Button(tree, SWT.CHECK);
        second.setEditor(secondControl, row, 1);

        firstControl.dispose();

        assertThat(((DartTreeEditor) first.getImpl()).getValue().getEditor()).isNull();
        assertThat(((DartTreeEditor) second.getImpl()).getValue().getEditor())
                .as("a tree can carry a permanent control on every row; disposing one must not "
                        + "take the others down with it")
                .isSameAs(secondControl);
    }
}
