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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Java half of the SashForm surface (a multi-pane app layout backed by a 3-pane SashForm):
 * when the user drags a divider, Flutter recomputes the panel weights locally
 * and must report them back; DartSashForm has to apply them, or its stale weights win at the next
 * Java-side layout (a window resize) and the user's drag is discarded — the reported symptom,
 * confirmed live in a running app (VSashForm.weights stayed [5,4,6] after a drag).
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
        Shell shell = Mocks.shell();
        org.eclipse.swt.widgets.Display display = shell.getDisplay();
        // The weights hook hops through asyncExec; run it inline on the mocked Display.
        doAnswer(inv -> {
            ((Runnable) inv.getArgument(0)).run();
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        SashForm form = new SashForm(shell, SWT.HORIZONTAL);
        new Composite(form, SWT.NONE);
        new Composite(form, SWT.NONE);
        // Explicit array: setWeights takes int[] in older SWT and int... in newer ones, and the
        // array form is the call that compiles against both.
        form.setWeights(new int[] { 1, 1 });
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
    @DisplayName("weights reported by a Flutter divider drag are applied")
    void weightsFromFlutterAreApplied() {
        SashForm form = sashForm();

        fireWeightsFromFlutter(form, "30,70");

        assertThat(form.getWeights())
                .as("the dragged weights must be applied to DartSashForm — otherwise the next "
                        + "Java-side layout recomputes the panels from the stale weights and "
                        + "discards the user's drag; getWeights() normalizes to a "
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

    @Test
    @DisplayName("weights Flutter reports for a form disposed before they arrive are dropped")
    void weightsForADisposedFormAreDropped() {
        SashForm form = sashForm();
        List<Runnable> queued = new ArrayList<>();
        doAnswer(inv -> queued.add(inv.getArgument(0))).when(form.getDisplay()).asyncExec(any(Runnable.class));

        fireWeightsFromFlutter(form, "2,1");
        form.dispose();
        bridge.dirtied.clear();

        assertThatCode(() -> queued.forEach(Runnable::run)).doesNotThrowAnyException();
        assertThat(bridge.dirtied).as("no weights update is scheduled for the disposed form").doesNotContain(form.getImpl());
    }

    @Test
    @DisplayName("the helper keeps the weights for no form, a disposed one, no payload or a negative weight")
    void theHelperKeepsTheWeightsForAPayloadItCannotUse() {
        SashForm form = sashForm();
        int[] before = form.getWeights();
        DartSashForm impl = (DartSashForm) form.getImpl();
        Event noText = new Event();
        Event negative = new Event();
        negative.text = "2,-1";

        SashFormHelper.applyWeightsFromFlutter(null, negative);
        SashFormHelper.applyWeightsFromFlutter(impl, null);
        SashFormHelper.applyWeightsFromFlutter(impl, noText);
        SashFormHelper.applyWeightsFromFlutter(impl, negative);
        assertThat(form.getWeights()).isEqualTo(before);

        form.dispose();
        assertThatCode(() -> SashFormHelper.applyWeightsFromFlutter(impl, negative)).doesNotThrowAnyException();
    }
}
