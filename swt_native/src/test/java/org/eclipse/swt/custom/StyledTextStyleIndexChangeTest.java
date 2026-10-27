package org.eclipse.swt.custom;

import static org.assertj.core.api.Assertions.assertThat;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * An update sends the style index as a change from what the client holds, carried across edits
 * exactly as the client carries it and checked against the checksum sent with it.
 */
// setText routes renderer font metrics into GTK/GDI on the Linux/Windows embed backends, which
// cannot run under the mocked display.
@DisabledOnOs({ OS.LINUX, OS.WINDOWS })
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

    private void sendWhole(StyledText text) throws Exception {
        serializer.to(text);
        Serializer.markDelivered();
    }

    /** The style index a partial update carries, or null when it carries none. */
    private int[] sentIndex(StyledText text) {
        byte[] diff = serializer.toDiff(impl(text));
        Serializer.markDelivered();
        if (diff == null) return null;
        Matcher m = Pattern.compile("\"styleIndex\":\\[([^\\]]*)\\]")
                .matcher(new String(diff, StandardCharsets.UTF_8));
        if (!m.find()) return null;
        return Arrays.stream(m.group(1).split(",")).mapToInt(Integer::parseInt).toArray();
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
    void an_update_that_never_went_out_does_not_become_what_the_client_holds() throws Exception {
        StyledText text = highlighted();
        sendWhole(text);
        int[] held = StyledTextHelper.wireStyleIndex(impl(text));

        text.setStyleRange(new StyleRange(10 * 6, 5, new Color(Mocks.device(), 0, 128, 0), null));
        serializer.toDiff(impl(text));
        Serializer.discardWritten();
        text.setStyleRange(new StyleRange(12 * 6, 5, new Color(Mocks.device(), 0, 128, 0), null));
        int[] sent = sentIndex(text);

        assertThat(sent[0]).isEqualTo(StyledTextHelper.STYLE_INDEX_EDIT);
        assertThat(apply(held, sent))
                .as("both restyles, relative to the index the client was last sent")
                .containsExactly(StyledTextHelper.wireStyleIndex(impl(text)));
    }

    @Test
    void a_whole_description_carries_the_whole_index() throws Exception {
        StyledText text = highlighted();
        sendWhole(text);
        text.setStyleRange(new StyleRange(6, 5, new Color(Mocks.device(), 0, 128, 0), null));

        String whole = new String(serializer.to(text), StandardCharsets.UTF_8);
        Serializer.markDelivered();

        assertThat(whole).doesNotContain("\"styleIndex\":[" + StyledTextHelper.STYLE_INDEX_EDIT);
    }

    @Test
    void described_whole_inside_its_parents_update_it_carries_the_whole_index() throws Exception {
        org.eclipse.swt.widgets.Composite parent =
                new org.eclipse.swt.widgets.Composite(Mocks.shell(), SWT.NONE);
        StyledText text = highlighted(parent);
        serializer.to(parent);
        Serializer.markDelivered();
        text.setStyleRange(new StyleRange(6, 5, new Color(Mocks.device(), 0, 128, 0), null));
        // Asked for again, so the next time the parent names it, it is described in full there.
        Serializer.forgetDelivery(impl(text).getValue());
        new org.eclipse.swt.widgets.Label(parent, SWT.NONE);

        byte[] diff = serializer.toDiff((org.eclipse.swt.widgets.DartWidget) parent.getImpl());
        Serializer.markDelivered();
        String frame = new String(diff, StandardCharsets.UTF_8);

        assertThat(frame).contains("\"swt\":\"StyledText\"").contains("\"styleIndex\":[");
        assertThat(frame)
                .as("a whole description replaces what the client holds, so it cannot be a change")
                .doesNotContain("\"styleIndex\":[" + StyledTextHelper.STYLE_INDEX_EDIT);
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
