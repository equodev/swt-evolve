package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import dev.equo.swt.harness.WireFrames;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What an editor of a large highlighted document sends: once whole, then per keystroke, and when
 * its current line moves. Each is held to a budget, so a change that makes any of them grow with
 * the document again fails here rather than in a capture of a running workbench.
 */
@ExtendWith(Mocks.class)
class StyledTextWireBudgetTest {

    private static final int CLIENT = 1;
    private static final int LINES = 6500;
    private static final String LINE = "        final Money total = prices.get(sku).times(quantity); // line ";

    private Color[] palette;
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
        FlutterBridge.set(bridge = new RecordingBridge());
        Serializer.enterConnection(CLIENT);
        palette = new Color[8];
        for (int i = 0; i < palette.length; i++) palette[i] = new Color(Mocks.device(), 30 * i, 255 - 30 * i, 100);
    }

    @AfterEach
    void tearDown() {
        Serializer.exitConnection();
        FlutterBridge.set(null);
    }

    @Test
    void anEditorOfALargeDocumentSendsWithinItsBudgets() {
        StyledText text = editor();
        DartStyledText impl = (DartStyledText) text.getImpl();
        // Past the text itself, which travels as it is.
        int whole = bytes(impl) - text.getCharCount();

        int lineStart = text.getOffsetAtLine(9);
        int length = text.getLine(9).length();
        text.replaceTextRange(lineStart + 14, 0, "x");
        text.replaceStyleRanges(lineStart, length + 1, styles(lineStart, 1, length + 2));
        int keystroke = bytes(impl);

        Color highlight = new Color(Mocks.device(), 55, 55, 55);
        text.setLineBackground(9, 1, highlight);
        bytes(impl);
        text.setLineBackground(9, 1, null);
        text.setLineBackground(4000, 1, highlight);
        int lineMove = bytes(impl);

        System.out.printf("[wire] %d-line editor: whole %d B past its text, keystroke %d B, current line moved %d B%n",
                LINES, whole, keystroke, lineMove);
        assertThat(whole).as("the whole editor, past its text").isLessThan(WHOLE_BUDGET);
        assertThat(keystroke).as("a keystroke").isLessThan(KEYSTROKE_BUDGET);
        assertThat(lineMove).as("the current line moving").isLessThan(LINE_MOVE_BUDGET);
    }

    @Test
    void editsAfterTheDocumentIsSetAgainSendWithinTheKeystrokeBudget() {
        StyledText text = editor();
        DartStyledText impl = (DartStyledText) text.getImpl();
        Client client = new Client(text);
        client.take(impl);
        String document = text.getText();
        StyleRange[] styles = text.getStyleRanges();
        type(text, 9, "x");
        client.take(impl);

        // As an editor reverting to what it last sent whole.
        text.setText(document);
        text.setStyleRanges(styles);
        client.take(impl);
        String setAgain = client.text;

        int keystroke = client.take(type(text, 9, "x"));
        int enter = client.take(type(text, 9, "\n"));
        int backspace = client.take(type(text, 10, "\b"));

        System.out.printf("[wire] %d-line editor set again: keystroke %d B, Enter %d B, Backspace %d B%n",
                LINES, keystroke, enter, backspace);
        assertThat(setAgain).as("the client's text once the document is set again").isEqualTo(document);
        assertThat(client.text).as("the client's text after the edits").isEqualTo(text.getText());
        assertThat(keystroke).as("the first keystroke after the document is set again").isLessThan(KEYSTROKE_BUDGET);
        assertThat(enter).as("an Enter").isLessThan(KEYSTROKE_BUDGET);
        assertThat(backspace).as("a Backspace joining the line back").isLessThan(KEYSTROKE_BUDGET);
    }

    @Test
    void aClientWhoseTextMissedAnEditIsSentTheWholeTextAgain() {
        StyledText text = editor();
        DartStyledText impl = (DartStyledText) text.getImpl();
        Client client = new Client(text);
        client.take(impl);
        type(text, 9, "x");
        bridge.comm.sent.clear();
        WireFrames.frame(impl, CLIENT);

        client.take(type(text, 9, "y"));

        assertThat(client.text).as("the client's text after asking for it again").isEqualTo(text.getText());
    }

    @Test
    void anEditorDescribedBeforeItIsLaidOutSendsTheStylesOfItsShellsHeight() {
        org.eclipse.swt.widgets.Shell shell = Mocks.shell();
        org.mockito.Mockito.when(shell.getClientArea()).thenReturn(new org.eclipse.swt.graphics.Rectangle(0, 0, 1600, 900));
        StyledText text = editor(shell, false);
        int whole = bytes((DartStyledText) text.getImpl()) - text.getCharCount();

        System.out.printf("[wire] %d-line editor before layout: whole %d B past its text%n", LINES, whole);
        assertThat(whole).as("the whole editor, past its text").isLessThan(BEFORE_LAYOUT_BUDGET);
    }

    @Test
    void aKeystrokeTellsTheClientOnlyTheRangeTheTextAndTheLength() {
        StyledText text = editor();
        int at = text.getOffsetAtLine(9) + 14;
        bridge.comm.sent.clear();

        text.replaceTextRange(at, 0, "x");

        java.util.List<dev.equo.swt.harness.RecordingComm.Frame> edits = new java.util.ArrayList<>();
        for (dev.equo.swt.harness.RecordingComm.Frame f : bridge.comm.sent) if (f.event.endsWith("/TextChange")) edits.add(f);
        assertThat(edits).hasSize(1);
        java.util.Map<String, Object> edit = edits.get(0).state();
        assertThat(edit).as("an event's numbered fields").containsOnlyKeys("start", "end", "text", "count");
        assertThat(edit).containsEntry("start", at).containsEntry("end", at).containsEntry("text", "x")
                .containsEntry("count", text.getCharCount());
        // Three bytes for the length of a document under a million characters, two for an offset under 8192.
        assertThat(edits.get(0).size).as("bytes of the edit's payload").isLessThanOrEqualTo(15);
    }

    @Test
    void aStyleThatComesAndGoesIsDescribedToTheClientOnce() {
        StyledText text = editor();
        DartStyledText impl = (DartStyledText) text.getImpl();
        bytes(impl);
        int start = text.getOffsetAtLine(9) + 8;
        int length = 6;
        StyleRange[] plain = text.getStyleRanges(start, length);
        StyleRange link = new StyleRange(start, length, new Color(Mocks.device(), 0, 0, 255), null);
        link.underline = true;
        link.underlineStyle = SWT.UNDERLINE_LINK;

        text.replaceStyleRanges(start, length, new StyleRange[] {link});
        assertThat(WireFrames.frame(impl, CLIENT)).as("the link's style, new to the client")
                .anyMatch(r -> r.state().containsKey("styles"));
        text.replaceStyleRanges(start, length, plain);
        java.util.List<WireFrames.Record> unlink = WireFrames.frame(impl, CLIENT);
        text.replaceStyleRanges(start, length, new StyleRange[] {link});
        java.util.List<WireFrames.Record> relink = WireFrames.frame(impl, CLIENT);

        assertThat(unlink).as("unlinked: only the runs' names change").noneMatch(r -> r.state().containsKey("styles"));
        assertThat(relink).as("linked again: the client still holds the style").noneMatch(r -> r.state().containsKey("styles"));
    }

    @Test
    void aStyleKeptWhoseColourIsDisposedIsLeftOutOfThePaletteAtTheNextFlush() {
        StyledText text = editor();
        DartStyledText impl = (DartStyledText) text.getImpl();
        bytes(impl);
        int start = text.getOffsetAtLine(9) + 8;
        int length = 6;
        StyleRange[] plain = text.getStyleRanges(start, length);
        Color blue = new Color(Mocks.device(), 0, 0, 255);
        text.replaceStyleRanges(start, length, new StyleRange[] {new StyleRange(start, length, blue, null)});
        WireFrames.frame(impl, CLIENT);
        text.replaceStyleRanges(start, length, plain);
        WireFrames.frame(impl, CLIENT);

        Color highlight = new Color(Mocks.device(), 55, 55, 55);
        text.setLineBackground(9, 1, highlight);
        assertThat(WireFrames.frame(impl, CLIENT)).as("a line background alone: the palette is kept")
                .noneMatch(r -> r.state().containsKey("styles"));

        blue.dispose();
        text.setLineBackground(10, 1, highlight);

        assertThat(WireFrames.frame(impl, CLIENT)).as("the palette, started over without the disposed colour")
                .anyMatch(r -> r.state().containsKey("styles"));
    }

    /** About a quarter above what each takes today: room for a property, not for anything that grows with the document. */
    private static final int WHOLE_BUDGET = 3500;
    private static final int KEYSTROKE_BUDGET = 400;
    private static final int LINE_MOVE_BUDGET = 120;
    /** A band as tall as the shell, where the view's own would be shorter. */
    private static final int BEFORE_LAYOUT_BUDGET = 4500;

    private StyledText editor() {
        return editor(Mocks.shell(), true);
    }

    private StyledText editor(org.eclipse.swt.widgets.Shell shell, boolean laidOut) {
        StyledText text = new StyledText(shell, SWT.MULTI);
        if (laidOut) text.setBounds(0, 0, 1000, 600);
        StringBuilder doc = new StringBuilder();
        for (int i = 0; i < LINES; i++) doc.append(LINE).append(i).append('\n');
        text.setText(doc.toString());
        int at = 0;
        java.util.List<StyleRange> all = new java.util.ArrayList<>();
        for (int i = 0; i < LINES; i++) {
            int length = text.getLine(i).length();
            all.addAll(java.util.Arrays.asList(styles(at, 1, length + 1)));
            at += length + 1;
        }
        text.setStyleRanges(all.toArray(new StyleRange[0]));
        return text;
    }

    /** The bytes the editor's edits and records take in the update a flush would send now. */
    private int bytes(DartStyledText impl) {
        int total = 0;
        for (RecordingComm.Frame f : bridge.comm.sent) total += f.size;
        bridge.comm.sent.clear();
        for (WireFrames.Record r : WireFrames.frame(impl, CLIENT)) total += r.size();
        return total;
    }

    /**
     * {@code key} typed at the caret on {@code line} (a letter or an Enter after its 14th character, a
     * Backspace at its start), then the line restyled, as a highlighter does.
     */
    private DartStyledText type(StyledText text, int line, String key) {
        int at = text.getOffsetAtLine(line) + ("\b".equals(key) ? 0 : 14);
        if ("\b".equals(key)) text.replaceTextRange(at - 1, 1, "");
        else text.replaceTextRange(at, 0, key);
        int edited = text.getLineAtOffset("\b".equals(key) ? at - 1 : at);
        int lineStart = text.getOffsetAtLine(edited);
        int length = text.getLine(edited).length();
        // A line an Enter cut short is too short for six runs: one run colours it.
        StyleRange[] restyled = length > 30 ? styles(lineStart, 1, length + 1)
                : new StyleRange[] { new StyleRange(lineStart, length, palette[0], null) };
        text.replaceStyleRanges(lineStart, length + 1, restyled);
        return (DartStyledText) text.getImpl();
    }

    /**
     * The text the client holds: what each record and edit sent makes of it, asking for the whole
     * text again where an edit does not fit it, as the client's {@code _onTextChange} does.
     */
    private final class Client {
        String text;

        Client(StyledText widget) {
            org.mockito.Mockito.doAnswer(call -> {
                ((Runnable) call.getArgument(0)).run();
                return null;
            }).when(widget.getDisplay()).asyncExec(org.mockito.ArgumentMatchers.any(Runnable.class));
        }

        /** The bytes of the edits sent and of the next flush, the whole text a drift asks for included. */
        int take(DartStyledText impl) {
            int total = 0;
            for (RecordingComm.Frame f : bridge.comm.sent) {
                total += f.size;
                if (!f.event.endsWith("/TextChange")) continue;
                java.util.Map<String, Object> change = f.state();
                int start = ((Number) change.get("start")).intValue();
                int replaced = ((Number) change.get("end")).intValue() - start;
                String updated = text == null || start + replaced > text.length() ? null
                        : text.substring(0, start) + change.get("text") + text.substring(start + replaced);
                if (updated == null || updated.length() != ((Number) change.get("count")).intValue()) {
                    bridge.comm.fireContaining("/ResendText", new byte[0]);
                }
                text = updated;
            }
            bridge.comm.sent.clear();
            for (WireFrames.Record r : WireFrames.frame(impl, CLIENT)) {
                total += r.size();
                if (r.state().get("text") instanceof String sent) text = sent;
            }
            return total;
        }
    }

    /** Six runs a line, as a source highlighter colours one, over {@code count} lines of {@code lineLength} from {@code start}. */
    private StyleRange[] styles(int start, int count, int lineLength) {
        int[] widths = {8, 6, 6, 6, 3, 0};
        StyleRange[] out = new StyleRange[count * widths.length];
        int n = 0;
        for (int l = 0; l < count; l++) {
            int lineStart = start + l * lineLength;
            int at = lineStart;
            for (int k = 0; k < widths.length; k++) {
                int width = k == widths.length - 1 ? lineLength - 1 - (at - lineStart) : widths[k];
                out[n++] = new StyleRange(at, width, palette[(l + k) % palette.length], null);
                at += width;
            }
        }
        return out;
    }
}
