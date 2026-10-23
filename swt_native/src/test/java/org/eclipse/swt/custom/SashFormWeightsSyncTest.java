package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * A drag on a SashForm's divider is its Sash's Selection: the form moves the panes to the sash
 * and keeps their new weights, so a later layout (a window resize) does not undo the drag.
 */
@ExtendWith(Mocks.class)
class SashFormWeightsSyncTest {

    private RecordingBridge bridge;

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
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private SashForm laidOutForm() {
        Shell shell = Mocks.shell();
        // The Sash's Selection hook hops through asyncExec; run it inline on the mocked Display.
        doAnswer(inv -> {
            ((Runnable) inv.getArgument(0)).run();
            return null;
        }).when(shell.getDisplay()).asyncExec(any(Runnable.class));
        SashForm form = new SashForm(shell, SWT.HORIZONTAL);
        form.setBounds(0, 0, 400, 300);
        new Composite(form, SWT.NONE);
        new Composite(form, SWT.NONE);
        // Explicit array: setWeights takes int[] in older SWT and int... in newer ones.
        form.setWeights(new int[] { 1, 1 });
        form.layout();
        return form;
    }

    private static Sash sashOf(SashForm form) {
        return (Sash) Arrays.stream(form.getChildren()).filter(c -> c instanceof Sash).findFirst().orElseThrow();
    }

    private void dragSashTo(Sash sash, int x) {
        Rectangle bounds = sash.getBounds();
        Event e = new Event();
        e.x = x;
        e.y = bounds.y;
        e.width = bounds.width;
        e.height = bounds.height;
        bridge.comm.fireContaining("Sash/" + sash.hashCode() + "/Selection/Selection", e);
    }

    @Test
    @DisplayName("dragging the sash moves the panes on either side of it")
    void draggingTheSashMovesThePanes() {
        SashForm form = laidOutForm();
        Control[] panes = form.getControls(false);
        Sash sash = sashOf(form);
        Rectangle left = panes[0].getBounds();
        Rectangle right = panes[1].getBounds();
        Rectangle before = sash.getBounds();

        dragSashTo(sash, before.x + 50);

        assertThat(sash.getBounds().x).isEqualTo(before.x + 50);
        assertThat(panes[0].getBounds().width).isEqualTo(left.width + 50);
        assertThat(panes[1].getBounds().x).isEqualTo(right.x + 50);
        assertThat(panes[1].getBounds().width).isEqualTo(right.width - 50);
        assertThat(form.getWeights()[0]).isGreaterThan(form.getWeights()[1]);
    }

    @Test
    @DisplayName("the panes keep the size a sash drag gave them through the next layout")
    void aDragSurvivesTheNextLayout() {
        SashForm form = laidOutForm();
        Control[] panes = form.getControls(false);
        Sash sash = sashOf(form);

        dragSashTo(sash, sash.getBounds().x + 50);
        Rectangle left = panes[0].getBounds();
        Rectangle right = panes[1].getBounds();
        form.layout();

        assertThat(panes[0].getBounds()).isEqualTo(left);
        assertThat(panes[1].getBounds()).isEqualTo(right);
    }
}
