package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Issue #509 — Java half of the SashForm surface (the Windows Action Recorder in an Rcp App is a
 * 3-pane SashForm): when the user drags a divider, Flutter recomputes the panel weights locally
 * and must report them back; DartSashForm has to apply them, or its stale weights win at the next
 * Java-side layout (a window resize) and the user's drag is discarded — the reported symptom,
 * confirmed live (VSashForm.weights stayed [5,4,6] after a drag).
 *
 * Flutter reports the weights as a Selection event whose {@code text} is the comma-separated
 * weight list. Uses a {@link RecordingBridge} whose {@code RecordingComm} captures the
 * {@code comm().on(...)} handler DartSashForm registers, so the Dart→Java event can be fired
 * directly. Fails on current code: DartSashForm hooks no Selection handler at all.
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

    private SashForm sashForm() {
        Shell shell = Mocks.swtShell();
        org.eclipse.swt.widgets.Display display = shell.getDisplay();
        // The weights hook hops through asyncExec; run it inline on the mocked Display.
        doAnswer(inv -> {
            ((Runnable) inv.getArgument(0)).run();
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        SashForm form = new SashForm(shell, SWT.HORIZONTAL);
        new Composite(form, SWT.NONE);
        new Composite(form, SWT.NONE);
        form.setWeights(1, 1);
        return form;
    }

    private void fireWeightsFromFlutter(SashForm form, String weightsCsv) {
        Event e = new Event();
        e.text = weightsCsv;
        bridge.comm.fireContaining("SashForm/" + form.hashCode() + "/Selection/Selection", e);
    }

    @Test
    @DisplayName("helper applies weights directly")
    void helperAppliesWeights() {
        SashForm form = sashForm();
        Event e = new Event();
        e.text = "30,70";
        SashFormHelper.applyWeightsFromFlutter((DartSashForm) form.getImpl(), e);
        // getWeights() reports weights normalized to a total of 1000.
        assertThat(form.getWeights()).containsExactly(300, 700);
    }

    @Test
    @DisplayName("weights reported by a Flutter divider drag are applied (issue #509)")
    void weightsFromFlutterAreApplied() {
        SashForm form = sashForm();

        fireWeightsFromFlutter(form, "30,70");

        assertThat(form.getWeights())
                .as("the dragged weights must be applied to DartSashForm — otherwise the next "
                        + "Java-side layout recomputes the panels from the stale weights and "
                        + "discards the user's drag (issue #509); getWeights() normalizes to a "
                        + "total of 1000")
                .containsExactly(300, 700);
    }

    @Test
    @DisplayName("malformed or mismatched weights are ignored, not fatal")
    void malformedWeightsAreIgnored() {
        SashForm form = sashForm();

        fireWeightsFromFlutter(form, "garbage");
        fireWeightsFromFlutter(form, "1,2,3"); // wrong arity for 2 children
        fireWeightsFromFlutter(form, "0,0");   // all-zero is invalid in SWT

        assertThat(form.getWeights()).as("invalid payloads must leave the weights untouched")
                .hasSize(2);
    }
}
