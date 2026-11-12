package org.eclipse.swt.custom;

import static org.assertj.core.api.Assertions.assertThat;
import dev.equo.swt.harness.WireFrames;
import dev.equo.swt.harness.WireValues;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * An update sends the style index as a change from what the client holds, carried across edits
 * exactly as the client carries it and checked against the checksum sent with it.
 */
@ExtendWith(Mocks.class)
class StyledTextStyleIndexChangeTest {

    private static final int CLIENT = 1;
    private static final int WORDS = 60;

    private final Serializer serializer = new Serializer();

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
        Serializer.enterConnection(CLIENT);
    }

    @AfterEach
    void tearDown() {
        Serializer.exitConnection();
        FlutterBridge.set(null);
    }

    /** Sixty words, alternately red and blue: an index of sixty runs. */
    private static StyledText highlighted() {
        return highlighted(Mocks.shell());
    }

    private static StyledText highlighted(org.eclipse.swt.widgets.Composite parent) {
        StyledText text = new StyledText(parent, SWT.NONE);
        StringBuilder words = new StringBuilder();
        for (int i = 0; i < WORDS; i++) words.append("word").append(i % 10).append(' ');
        text.setText(words.toString());
        Color red = new Color(Mocks.device(), 255, 0, 0);
        Color blue = new Color(Mocks.device(), 0, 0, 255);
        for (int i = 0; i < WORDS; i++) {
            text.setStyleRange(new StyleRange(i * 6, 5, i % 2 == 0 ? red : blue, null));
        }
        return text;
    }

    private static DartStyledText impl(StyledText text) {
        return (DartStyledText) text.getImpl();
    }

    private void sendWhole(StyledText text) {
        Serializer.forgetDelivery(impl(text).getValue());
        WireFrames.record(impl(text), CLIENT);
    }

    /** The style index a partial update carries, or null when it carries none. */
    private int[] sentIndex(StyledText text) {
        Map<String, Object> diff = WireFrames.record(impl(text), CLIENT);
        if (diff == null) return null;
        List<int[]> indexes = styleIndexes(diff);
        return indexes.isEmpty() ? null : indexes.get(0);
    }

    /** Every style index [tree] carries, at any depth. */
    private static List<int[]> styleIndexes(Object tree) {
        List<int[]> out = new ArrayList<>();
        for (Map<String, Object> m : WireValues.find(tree, candidate -> candidate.get("styleIndex") instanceof List)) {
            out.add(((List<?>) m.get("styleIndex")).stream().mapToInt(v -> ((Number) v).intValue()).toArray());
        }
        return out;
    }

    /** Whether any style index in [tree] is a change rather than a whole index. */
    private static boolean carriesAChange(Object tree) {
        return styleIndexes(tree).stream()
                .anyMatch(index -> index.length > 0 && index[0] == StyledTextHelper.STYLE_INDEX_EDIT);
    }

    /** What the client does with a change: the same arithmetic, written again here. */
    private static int[] apply(int[] base, int[] change) {
        int from = change[4], removed = change[5];
        int[] out = new int[1 + (from - 1) + (change.length - 6) + (base.length - from - removed)];
        int n = 0;
        out[n++] = change[3];
        for (int i = 1; i < from; i++) out[n++] = base[i];
        for (int i = 6; i < change.length; i++) out[n++] = change[i];
        for (int i = from + removed; i < base.length; i++) out[n++] = base[i];
        assertThat(StyledTextHelper.styleIndexChecksum(out))
                .as("the checksum names the index the change produces")
                .isEqualTo(change[1]);
        return out;
    }

    @Test
    void a_change_replaces_whole_runs_so_it_travels_as_runs() {
        // Palette 7; runs (name, start, length). The second run's length and the third's name differ.
        int[] base = { 7, 0, 0, 4, 1, 4, 2, 2, 6, 3, 0, 9, 5 };
        int[] target = { 7, 0, 0, 4, 1, 4, 3, 3, 7, 2, 0, 10, 4 };
        int[] change = StyledTextHelper.styleIndexChange(base, target, 1);

        assertThat((change[4] - 1) % 3).as("it starts at a run").isZero();
        assertThat((change.length - 6) % 3).as("it inserts whole runs").isZero();
        assertThat(dev.equo.swt.WidgetWire.runsFrom(change[0], change.length)).isEqualTo(6);
        assertThat(apply(base, change)).containsExactly(target);
    }

    @Test
    void restyling_one_run_sends_a_change_not_the_index() throws Exception {
        StyledText text = highlighted();
        sendWhole(text);
        int[] held = StyledTextHelper.wireStyleIndex(impl(text));

        text.setStyleRange(new StyleRange(30 * 6, 5, new Color(Mocks.device(), 0, 128, 0), null));
        int[] sent = sentIndex(text);

        assertThat(sent).isNotNull();
        assertThat(sent[0]).as("a change").isEqualTo(StyledTextHelper.STYLE_INDEX_EDIT);
        assertThat(sent.length).as("a run or so, not sixty").isLessThan(held.length / 4);
        assertThat(apply(held, sent)).containsExactly(StyledTextHelper.wireStyleIndex(impl(text)));
    }

    @Test
    void a_change_after_typing_is_relative_to_the_index_carried_across_it() throws Exception {
        StyledText text = highlighted();
        sendWhole(text);
        int[] held = StyledTextHelper.wireStyleIndex(impl(text));

        // Typed ahead of every run: the client moves them all itself when the edit arrives.
        text.replaceTextRange(0, 0, "xy");
        int[] carried = StyledTextHelper.spliceStyleIndex(held, 0, 0, 2);
        int[] sent = sentIndex(text);

        assertThat(sent[0]).isEqualTo(StyledTextHelper.STYLE_INDEX_EDIT);
        assertThat(sent.length)
                .as("moving every run is what the client already did, so nothing of it travels")
                .isLessThan(held.length / 4);
        assertThat(apply(carried, sent)).containsExactly(StyledTextHelper.wireStyleIndex(impl(text)));
    }

    @Test
    void the_index_a_client_holds_for_one_editor_is_not_held_for_another() throws Exception {
        Color green = new Color(Mocks.device(), 0, 128, 0);
        StyledText text = highlighted(), other = highlighted();
        text.setStyleRange(new StyleRange(10 * 6, 5, green, null));
        sendWhole(text);
        int[] held = StyledTextHelper.wireStyleIndex(impl(text));
        // The client now holds the other editor's index last, which differs from this one's.
        sendWhole(other);

        text.setStyleRange(new StyleRange(30 * 6, 5, green, null));
        int[] sent = sentIndex(text);

        assertThat(sent[0]).isEqualTo(StyledTextHelper.STYLE_INDEX_EDIT);
        assertThat(apply(held, sent)).as("a change to the index the client holds for this editor")
                .containsExactly(StyledTextHelper.wireStyleIndex(impl(text)));
    }

    @Test
    void a_whole_description_carries_the_whole_index() throws Exception {
        StyledText text = highlighted();
        sendWhole(text);
        text.setStyleRange(new StyleRange(6, 5, new Color(Mocks.device(), 0, 128, 0), null));

        Serializer.forgetDelivery(impl(text).getValue());
        Map<String, Object> whole = WireFrames.record(impl(text), CLIENT);

        assertThat(carriesAChange(whole)).isFalse();
    }

    @Test
    void described_whole_inside_its_parents_update_it_carries_the_whole_index() throws Exception {
        org.eclipse.swt.widgets.Composite parent =
                new org.eclipse.swt.widgets.Composite(Mocks.shell(), SWT.NONE);
        StyledText text = highlighted(parent);
        WireFrames.frame((org.eclipse.swt.widgets.DartWidget) parent.getImpl(), CLIENT);
        text.setStyleRange(new StyleRange(6, 5, new Color(Mocks.device(), 0, 128, 0), null));
        // Asked for again, so the next time the parent names it, it is described in full there.
        Serializer.forgetDelivery(impl(text).getValue());
        new org.eclipse.swt.widgets.Label(parent, SWT.NONE);

        List<Map<String, Object>> frame = WireFrames.frame((org.eclipse.swt.widgets.DartWidget) parent.getImpl(), CLIENT)
                .stream().map(WireFrames.Record::state).toList();

        assertThat(WireValues.find(frame, m -> "StyledText".equals(m.get("swt")))).isNotEmpty();
        assertThat(styleIndexes(frame)).isNotEmpty();
        assertThat(carriesAChange(frame))
                .as("a whole description replaces what the client holds, so it cannot be a change")
                .isFalse();
    }

    @Test
    void a_repaint_that_restyles_nothing_stops_sending_the_index() throws Exception {
        StyledText text = highlighted();
        sendWhole(text);

        // What a scroll step does: the renderer is dirtied and the styling is what it was.
        impl(text).getValue().markDirty(VStyledText.STYLE_INDEX);
        sentIndex(text);
        impl(text).getValue().markDirty(VStyledText.STYLE_INDEX);

        assertThat(sentIndex(text))
                .as("the same nothing again is a repeat, so it does not travel")
                .isNull();
    }

    @Test
    void the_client_carries_an_index_across_an_edit_as_the_renderer_does() {
        // [name, (entry, start, length)...]: runs at 0..5, 10..15, 20..25.
        int[] index = { 7, 0, 0, 5, 1, 10, 5, 0, 20, 5 };

        assertThat(StyledTextHelper.spliceStyleIndex(index, 5, 0, 3))
                .as("typed just past a run: it does not grow, the later ones move")
                .containsExactly(7, 0, 0, 5, 1, 13, 5, 0, 23, 5);
        assertThat(StyledTextHelper.spliceStyleIndex(index, 9, 3, 0))
                .as("a deletion over the start of a run cuts it back")
                .containsExactly(7, 0, 0, 5, 1, 9, 3, 0, 17, 5);
        assertThat(StyledTextHelper.spliceStyleIndex(index, 9, 7, 0))
                .as("one over the whole of a run removes it")
                .containsExactly(7, 0, 0, 5, 0, 13, 5);
    }
}
