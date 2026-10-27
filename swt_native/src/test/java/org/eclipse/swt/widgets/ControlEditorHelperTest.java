package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.DartControlEditor;
import org.eclipse.swt.custom.TreeEditor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** The list of cell editors a Dart-backed Tree or Table keeps so that it can describe them to the client. */
@Tag("flutter-it")
class ControlEditorHelperTest {

    private Display display;
    private Tree tree;

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
        tree = new Tree(new Shell(display), SWT.NONE);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("an editor is added once, after the ones already there")
    void addEditorAppendsOnce() {
        TreeEditor first = new TreeEditor(tree);
        TreeEditor second = new TreeEditor(tree);

        TreeEditor[] one = ControlEditorHelper.addEditor(null, first, TreeEditor.class);
        TreeEditor[] two = ControlEditorHelper.addEditor(one, second, TreeEditor.class);

        assertThat(one).containsExactly(first);
        assertThat(two).containsExactly(first, second);
        assertThat(ControlEditorHelper.addEditor(two, first, TreeEditor.class)).isSameAs(two);
    }

    @Test
    @DisplayName("removing an editor keeps the others in order, and an unknown one changes nothing")
    void removeEditorKeepsTheOthers() {
        TreeEditor first = new TreeEditor(tree);
        TreeEditor second = new TreeEditor(tree);
        TreeEditor third = new TreeEditor(tree);
        TreeEditor[] all = {first, second, third};

        assertThat(ControlEditorHelper.removeEditor(all, second, TreeEditor.class)).containsExactly(first, third);
        assertThat(ControlEditorHelper.removeEditor(new TreeEditor[] {first}, third, TreeEditor.class))
                .containsExactly(first);
        assertThat(ControlEditorHelper.removeEditor(null, first, TreeEditor.class)).isNull();
    }

    @Test
    @DisplayName("only an editor that holds the control answers that it does")
    void holdsEditorControlLooksForTheControl() {
        TreeEditor holding = new TreeEditor(tree);
        TreeEditor empty = new TreeEditor(tree);
        Text text = new Text(tree, SWT.NONE);
        Text other = new Text(tree, SWT.NONE);
        holding.setEditor(text);

        TreeEditor[] editors = {null, empty, holding};
        assertThat(ControlEditorHelper.holdsEditorControl(editors, text)).isTrue();
        assertThat(ControlEditorHelper.holdsEditorControl(editors, other)).isFalse();
        assertThat(ControlEditorHelper.holdsEditorControl(editors, null)).isFalse();
        assertThat(ControlEditorHelper.holdsEditorControl(null, text)).isFalse();
        assertThat(((DartControlEditor) holding.getImpl()).holdsEditor(other)).isFalse();
    }

    @Test
    @DisplayName("an editor describes itself with one value, made the first time it is asked for")
    void oneValuePerEditor() {
        DartControlEditor impl = (DartControlEditor) new ControlEditor(new Composite(tree.getShell(), SWT.NONE)).getImpl();

        assertThat(impl.getValue()).isSameAs(impl.getValue());
    }
}
