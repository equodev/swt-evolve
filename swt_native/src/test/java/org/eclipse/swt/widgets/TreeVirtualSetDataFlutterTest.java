package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Regression for issue #867 — "The Expressions section of Debug does not show the table it should".
 *
 * The Eclipse debug Expressions/Breakpoints views are SWT.VIRTUAL {@link Tree}s whose rows are
 * filled lazily through an SWT.SetData listener (the top-level row is a single "Add new expression"
 * entry). On the Flutter desk/web backend there is no native paint loop to fire SetData when a row
 * scrolls into view, so DartTree eagerly loads an initial page of a level's rows. That eager load
 * used to run only when a node was expanded ({@code onItemExpanded -> loadVirtualPage}); the ROOT
 * level was never loaded after {@code setItemCount}, so top-level virtual rows that are never
 * expanded never received SetData, serialized with {@code items = []}, and rendered blank.
 *
 * These tests lock the invariant that a VIRTUAL tree fires SetData for its top-level rows as soon as
 * their count is known — without any expand — mirroring what {@link Table#setItemCount(int)} already
 * does. They are red before the generator fix and green after.
 */
@Tag("flutter-it")
class TreeVirtualSetDataFlutterTest {

    private Display display;

    @BeforeAll static void useEquo() { Config.forceEquo(); }
    @AfterAll static void reset() { Config.defaultToEclipse(); }

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    /** A VIRTUAL tree that fills each requested row's text from a SetData listener, like the debug views. */
    private Tree virtualTreeFilledOnSetData(List<Integer> setDataIndices) {
        Shell shell = new Shell(display);
        Tree tree = new Tree(shell, SWT.VIRTUAL | SWT.BORDER);
        tree.addListener(SWT.SetData, e -> {
            TreeItem item = (TreeItem) e.item;
            int index = tree.indexOf(item);
            setDataIndices.add(index);
            item.setText("Row " + index);
        });
        return tree;
    }

    @Test
    @DisplayName("a VIRTUAL tree's single top-level row is populated via SetData on setItemCount, without expanding (issue #867)")
    void topLevelRowIsPopulatedOnSetItemCount() {
        List<Integer> setDataIndices = new ArrayList<>();
        Tree tree = virtualTreeFilledOnSetData(setDataIndices);

        // The debug Expressions view sets a single top-level "Add new expression" row.
        tree.setItemCount(1);

        assertThat(setDataIndices)
                .as("SWT.SetData must fire for the top-level row on setItemCount so its content exists "
                        + "(no native paint loop, and the row is never expanded)")
                .contains(0);
        assertThat(tree.getItem(0).getText())
                .as("the top-level row must carry the text the SetData listener set, so Flutter has a row to render")
                .isEqualTo("Row 0");
    }

    @Test
    @DisplayName("all top-level rows of a VIRTUAL tree (up to the initial page) receive SetData, not just the first (issue #867)")
    void allTopLevelRowsInInitialPageReceiveSetData() {
        List<Integer> setDataIndices = new ArrayList<>();
        Tree tree = virtualTreeFilledOnSetData(setDataIndices);

        tree.setItemCount(5);

        assertThat(setDataIndices)
                .as("every top-level row in the initial page must be populated, not only the first")
                .contains(0, 1, 2, 3, 4);
        for (int i = 0; i < 5; i++) {
            assertThat(tree.getItem(i).getText()).isEqualTo("Row " + i);
        }
    }
}
