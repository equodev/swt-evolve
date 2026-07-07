package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("metal")
@DisabledOnOs({OS.LINUX, OS.WINDOWS})

/**
 * Regression test for issues #625 and #793.
 *
 * When a child widget's bounds change (e.g., the details panel collapses to height=0
 * or expands back), the parent DartWidget must be marked dirty so Flutter re-renders
 * the parent with the updated child layout.
 *
 * Without the fix: the parent is never dirtied → Flutter keeps the stale layout →
 * expand/collapse has no visible effect on the section (details panel stays as-is).
 *
 * The fix: {@code DartControl.setBounds} calls {@code pw.dirty()} on the parent when
 * the size actually changes.
 */
class SetBoundsParentDirtyTest {

    private TrackingBridge bridge;
    private Display display;

    @BeforeEach
    void setUp() {
        Config.forceEquo();
        bridge = new TrackingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
        Config.defaultToEclipse();
    }

    /**
     * Mirrors the collapse step in the expandable-section pattern (issue #625):
     * the layout manager calls setBounds on the details panel to set height=0.
     * The parent composite must be dirtied so Flutter re-renders the new layout.
     */
    @Test
    @DisplayName("collapse: setBounds height→0 marks parent dirty (issue #625)")
    void setBounds_collapse_marksParentDirty() {
        Shell shell = new Shell(display);
        Composite parent = new Composite(shell, SWT.NONE);
        Composite child = new Composite(parent, SWT.NONE);
        child.setBounds(0, 20, 300, 200); // expanded state

        bridge.dirtied.clear();
        child.setBounds(0, 20, 300, 0); // collapse: height → 0

        assertThat(bridge.dirtied)
                .as("after collapsing a child (height→0), the parent must be dirty so Flutter "
                        + "re-renders the layout — without this fix the section never collapses "
                        + "(issue #625/#793)")
                .contains((DartWidget) parent.getImpl());
    }

    /**
     * Mirrors the expand step: layout manager calls setBounds to restore the child's
     * full height. The parent must again be dirtied.
     */
    @Test
    @DisplayName("expand: setBounds restores height marks parent dirty (issue #625)")
    void setBounds_expand_marksParentDirty() {
        Shell shell = new Shell(display);
        Composite parent = new Composite(shell, SWT.NONE);
        Composite child = new Composite(parent, SWT.NONE);
        child.setBounds(0, 20, 300, 0); // collapsed state

        bridge.dirtied.clear();
        child.setBounds(0, 20, 300, 200); // expand: height restored

        assertThat(bridge.dirtied)
                .as("after expanding a child (height→200), the parent must be dirty so Flutter "
                        + "re-renders the layout — without this fix the section never expands "
                        + "(issue #625/#793)")
                .contains((DartWidget) parent.getImpl());
    }

    /**
     * Sanity: any setBounds call (even move-only) dirtied the parent because the
     * child's position in the parent may have changed. This verifies the fix applies
     * broadly, not only when the size changes.
     */
    @Test
    @DisplayName("move-only setBounds also marks parent dirty")
    void setBounds_moveOnly_alsoMarksParentDirty() {
        Shell shell = new Shell(display);
        Composite parent = new Composite(shell, SWT.NONE);
        Composite child = new Composite(parent, SWT.NONE);
        child.setBounds(0, 0, 300, 200);

        bridge.dirtied.clear();
        child.setBounds(10, 20, 300, 200); // position changes, size stays the same

        assertThat(bridge.dirtied)
                .as("even a move-only setBounds must dirty the parent so Flutter updates "
                        + "the child's position in the layout")
                .contains((DartWidget) parent.getImpl());
    }

    static class TrackingBridge extends RecordingBridge {
        final List<DartWidget> dirtied = new ArrayList<>();

        @Override
        public void dirty(DartWidget widget) {
            dirtied.add(widget);
            super.dirty(widget);
        }
    }
}
