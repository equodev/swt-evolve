package org.eclipse.swt.custom;

import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A tab dragged to another place in its own folder, as the Eclipse workbench moves it: the item is
 * disposed, which selects another, a new item at the drop index takes the same control, and the
 * workbench selects it again. The client has to select what Java selects, show that tab's control
 * and hide the other.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CTabFolderReorderFlutterTest {

    private final WebDisplayHarness flutter = new WebDisplayHarness();
    private Display display;
    private Shell shell;

    @BeforeAll
    void boot() {
        display = flutter.boot();
        shell = new Shell(display);
        shell.setBounds(0, 0, 500, 300);
        shell.open();
    }

    @AfterAll
    void shutdown() {
        flutter.teardown();
    }

    @AfterEach
    void clear() {
        for (Control c : shell.getChildren()) c.dispose();
        flutter.settle();
    }

    @SuppressWarnings("unchecked")
    private boolean clientVisible(Control control) {
        Map<String, Object> state = (Map<String, Object>) flutter.queryState(control).get("state");
        return !Boolean.FALSE.equals(state.get("visible"));
    }

    @Test
    @DisplayName("a selected tab moved before another: the client shows the control of the tab left selected")
    void selectedTabMovedFirst() {
        CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        folder.setBounds(0, 0, 480, 260);
        Composite second = new Composite(folder, SWT.NONE);
        Composite third = new Composite(folder, SWT.NONE);
        CTabItem secondItem = new CTabItem(folder, SWT.CLOSE);
        secondItem.setText("second");
        secondItem.setControl(second);
        CTabItem thirdItem = new CTabItem(folder, SWT.CLOSE);
        thirdItem.setText("third");
        thirdItem.setControl(third);
        folder.setSelection(thirdItem);
        flutter.settle();
        assertThat(clientVisible(third)).as("sanity: the selected tab's control").isTrue();
        assertThat(clientVisible(second)).as("sanity: the other tab's control").isFalse();

        thirdItem.dispose();
        CTabItem moved = new CTabItem(folder, SWT.CLOSE, 0);
        moved.setText("third");
        moved.setControl(third);
        flutter.settle();

        assertThat(folder.getSelection().getControl()).as("sanity: Java").isSameAs(second);
        assertThat(second.getVisible()).as("sanity: Java").isTrue();
        assertThat(third.getVisible()).as("sanity: Java").isFalse();
        assertThat(clientVisible(second)).as("the selected tab's control").isTrue();
        assertThat(clientVisible(third)).as("the moved tab's control").isFalse();
    }

    @Test
    @DisplayName("a selected tab moved before another and selected again: the client selects it where it went")
    void selectedTabMovedFirstAndReselected() {
        CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        folder.setBounds(0, 0, 480, 260);
        Composite second = new Composite(folder, SWT.NONE);
        Composite third = new Composite(folder, SWT.NONE);
        CTabItem secondItem = new CTabItem(folder, SWT.CLOSE);
        secondItem.setText("second");
        secondItem.setControl(second);
        CTabItem thirdItem = new CTabItem(folder, SWT.CLOSE);
        thirdItem.setText("third");
        thirdItem.setControl(third);
        folder.setSelection(thirdItem);
        flutter.settle();
        assertThat(clientSelection(folder)).as("sanity").isEqualTo(1);

        thirdItem.dispose();
        CTabItem moved = new CTabItem(folder, SWT.CLOSE, 0);
        moved.setText("third");
        moved.setControl(third);
        folder.setSelection(moved);
        flutter.settle();

        assertThat(folder.getSelectionIndex()).as("sanity: Java").isZero();
        assertThat(clientSelection(folder)).as("the client's selected tab").isZero();
        assertThat(clientVisible(third)).as("the moved tab's control").isTrue();
        assertThat(clientVisible(second)).as("the other tab's control").isFalse();
    }

    @Test
    @DisplayName("a drag pressed on the selected tab, then the drop: the client selects the tab Java selects")
    void dragFromTheSelectedTab() {
        CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        folder.setBounds(0, 0, 480, 260);
        Composite second = new Composite(folder, SWT.NONE);
        Composite third = new Composite(folder, SWT.NONE);
        CTabItem secondItem = new CTabItem(folder, SWT.CLOSE);
        secondItem.setText("second");
        secondItem.setControl(second);
        CTabItem thirdItem = new CTabItem(folder, SWT.CLOSE);
        thirdItem.setText("third");
        thirdItem.setControl(third);
        folder.setSelection(thirdItem);
        flutter.settle();

        // A drag starts with a press on its tab, which here is the selected one: Java selects nothing new.
        double[] rect = flutter.renderedRect(folder);
        org.eclipse.swt.graphics.Rectangle tab = thirdItem.getBounds();
        flutter.input().click(rect[0] + tab.x + tab.width / 2.0, rect[1] + tab.y + tab.height / 2.0);
        flutter.settle();
        assertThat(clientSelection(folder)).as("sanity").isEqualTo(1);

        thirdItem.dispose();
        CTabItem moved = new CTabItem(folder, SWT.CLOSE, 0);
        moved.setText("third");
        moved.setControl(third);
        folder.setSelection(moved);
        flutter.settle();

        assertThat(folder.getSelectionIndex()).as("sanity: Java").isZero();
        assertThat(clientSelection(folder)).as("the client's selected tab").isZero();
        assertThat(clientVisible(third)).as("the moved tab's control").isTrue();
        assertThat(clientVisible(second)).as("the other tab's control").isFalse();
    }

    /**
     * The workbench's drop can flush part way: moving the focus while the item is gone sends what
     * Java holds at that point. Bit 1 flushes after the dispose, 2 after the new item, 4 after its
     * control is set.
     */
    @ParameterizedTest(name = "flushed part way ({0})")
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7})
    void flushedPartWay(int flushes) {
        CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        folder.setBounds(0, 0, 480, 260);
        Composite second = new Composite(folder, SWT.NONE);
        Composite third = new Composite(folder, SWT.NONE);
        CTabItem secondItem = new CTabItem(folder, SWT.CLOSE);
        secondItem.setText("second");
        secondItem.setControl(second);
        CTabItem thirdItem = new CTabItem(folder, SWT.CLOSE);
        thirdItem.setText("third");
        thirdItem.setControl(third);
        folder.setSelection(thirdItem);
        flutter.settle();

        thirdItem.dispose();
        if ((flushes & 1) != 0) flutter.settle();
        CTabItem moved = new CTabItem(folder, SWT.CLOSE, 0);
        if ((flushes & 2) != 0) flutter.settle();
        moved.setText("third");
        moved.setControl(third);
        if ((flushes & 4) != 0) flutter.settle();
        folder.setSelection(moved);
        flutter.settle();

        assertThat(clientSelection(folder)).as("the client's selected tab").isEqualTo(folder.getSelectionIndex());
        assertThat(clientVisible(third)).as("the moved tab's control").isEqualTo(third.getVisible());
        assertThat(clientVisible(second)).as("the other tab's control").isEqualTo(second.getVisible());
    }

    @SuppressWarnings("unchecked")
    private int clientSelection(CTabFolder folder) {
        Map<String, Object> state = (Map<String, Object>) flutter.queryState(folder).get("state");
        return ((Number) state.get("selection")).intValue();
    }
}
