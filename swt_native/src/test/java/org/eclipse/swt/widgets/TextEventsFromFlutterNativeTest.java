package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * What an edit, a selection change, a segment request or a default selection arriving from Flutter does
 * to a Text. Every one hops through {@code asyncExec}, so by the time it runs the text may be gone or a
 * modal shell may have opened; a vetoed edit is answered so the field can drop what it held back.
 */
@Tag("native-unit")
class TextEventsFromFlutterNativeTest {

    private RecordingBridge bridge;
    private Display display;
    /** The runnables a hook queued with asyncExec, run when the test says so. */
    private final List<Runnable> queued = new ArrayList<>();

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = DartMocks.dartDisplay();
        doAnswer(invocation -> queued.add(invocation.getArgument(0))).when(display).asyncExec(any(Runnable.class));
        // The mock display would swallow every event; its real dispatch only needs the listener table.
        DartDisplay impl = dartDisplay();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
    }

    @AfterEach
    void tearDown() {
        // The display mock is shared by the whole suite: hand it back as the other tests expect it.
        doNothing().when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = dartDisplay();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        FlutterBridge.set(null);
    }

    private DartDisplay dartDisplay() {
        return (DartDisplay) display.getImpl();
    }

    private Text text(int style, String content) {
        Text text = new Text(DartMocks.dartShell(display), style);
        text.setText(content);
        return text;
    }

    private void fromFlutter(Text text, String channel, Event event) {
        bridge.comm.fireContaining("Text/" + text.hashCode() + "/" + channel, event);
    }

    private static Event event(String text, int start, int end) {
        Event event = new Event();
        event.text = text;
        event.start = start;
        event.end = end;
        return event;
    }

    private void runQueued() {
        List<Runnable> now = new ArrayList<>(queued);
        queued.clear();
        now.forEach(Runnable::run);
    }

    private static List<Event> record(Text text, int type) {
        List<Event> events = new ArrayList<>();
        text.addListener(type, events::add);
        return events;
    }

    private List<String> verdicts() {
        List<String> verdicts = new ArrayList<>();
        for (RecordingComm.Frame frame : bridge.comm.sent) {
            if (frame.event.contains("modify/verdict")) verdicts.add(frame.json);
        }
        return verdicts;
    }

    @Test
    @DisplayName("an edit from Flutter sets the text and puts the caret where the field says")
    void editSetsTheTextAndTheCaret() {
        Text text = text(SWT.SINGLE, "old");

        fromFlutter(text, "Modify/Modify", event("typed", 5, 5));
        runQueued();

        assertThat(text.getText()).isEqualTo("typed");
        assertThat(text.getSelection()).isEqualTo(new Point(5, 5));
        assertThat(verdicts()).as("nobody can veto it, so nobody asks").isEmpty();
    }

    @Test
    @DisplayName("an edit that arrives after the text was disposed is dropped")
    void editOfADisposedTextIsDropped() {
        Text text = text(SWT.SINGLE, "old");

        fromFlutter(text, "Modify/Modify", event("typed", 5, 5));
        text.dispose();

        assertThatCode(this::runQueued).as("a disposed text is not written to").doesNotThrowAnyException();
    }

    @Test
    @DisplayName("an edit without text is ignored")
    void editWithoutTextIsIgnored() {
        Text text = text(SWT.SINGLE, "old");

        fromFlutter(text, "Modify/Modify", new Event());
        runQueued();

        assertThat(text.getText()).isEqualTo("old");
    }

    @Test
    @DisplayName("a Verify listener that vetoes an edit leaves the text and is answered doit=false")
    void vetoedEditIsAnswered() {
        Text text = text(SWT.SINGLE, "old");
        text.addVerifyListener(e -> e.doit = false);

        fromFlutter(text, "Modify/Modify", event("typed", 5, 5));
        runQueued();

        assertThat(text.getText()).isEqualTo("old");
        assertThat(verdicts()).singleElement().asString().contains("false");
    }

    @Test
    @DisplayName("a Verify listener that lets an edit through is answered doit=true")
    void allowedEditIsAnswered() {
        Text text = text(SWT.SINGLE, "old");
        text.addVerifyListener(e -> { });

        fromFlutter(text, "Modify/Modify", event("typed", 5, 5));
        runQueued();

        assertThat(text.getText()).isEqualTo("typed");
        assertThat(verdicts()).singleElement().asString().contains("true");
    }

    @Test
    @DisplayName("an edit a Verify listener rewrites is answered doit=false, and the caret stays where the field put it")
    void rewrittenEditIsAnsweredAsRefused() {
        Text text = text(SWT.SINGLE, "old");
        text.addVerifyListener(e -> e.text = e.text.toUpperCase());

        fromFlutter(text, "Modify/Modify", event("typed", 5, 5));
        runQueued();

        assertThat(text.getText()).isEqualTo("TYPED");
        assertThat(text.getSelection()).isEqualTo(new Point(5, 5));
        assertThat(verdicts()).singleElement().asString().contains("false");
    }

    @Test
    @DisplayName("an edit a Verify listener shortens leaves the caret after what the listener kept")
    void shortenedEditKeepsTheCaretAfterWhatWasKept() {
        Text text = text(SWT.SINGLE, "ab");
        text.addVerifyListener(e -> e.text = e.text.replace("12", "1"));

        fromFlutter(text, "Modify/Modify", event("a12b", 3, 3));
        runQueued();

        assertThat(text.getText()).isEqualTo("a1b");
        assertThat(text.getSelection()).isEqualTo(new Point(2, 2));
    }

    @Test
    @DisplayName("a vetoed edit leaves the text and its selection as they were")
    void vetoedEditKeepsTheSelection() {
        Text text = text(SWT.SINGLE, "old");
        text.setSelection(1, 2);
        text.addVerifyListener(e -> e.doit = false);

        fromFlutter(text, "Modify/Modify", event("oXd", 2, 2));
        runQueued();

        assertThat(text.getText()).isEqualTo("old");
        assertThat(text.getSelection()).isEqualTo(new Point(1, 2));
        assertThat(verdicts()).singleElement().asString().contains("false");
    }

    @Test
    @DisplayName("an edit that reports no caret leaves it at the start, where setText puts it")
    void editWithoutACaretLeavesItAtTheStart() {
        Text text = text(SWT.SINGLE, "old");
        text.setSelection(2);

        fromFlutter(text, "Modify/Modify", event("typed", -1, -1));
        runQueued();

        assertThat(text.getText()).isEqualTo("typed");
        assertThat(text.getSelection()).isEqualTo(new Point(0, 0));
    }

    @Test
    @DisplayName("an edit the text limit cuts is answered doit=false, with the caret inside what is left")
    void editCutByTheLimitIsAnsweredAsRefused() {
        Text text = text(SWT.SINGLE, "old");
        text.setTextLimit(4);
        text.addVerifyListener(e -> { });

        fromFlutter(text, "Modify/Modify", event("typed", 5, 5));
        runQueued();

        assertThat(text.getText()).isEqualTo("type");
        assertThat(text.getSelection()).isEqualTo(new Point(4, 4));
        assertThat(verdicts()).singleElement().asString().contains("false");
    }

    @Test
    @DisplayName("the segment listeners are asked for the segments of the text after it is replaced")
    void segmentsAreAskedAfterTheTextIsReplaced() {
        Text text = text(SWT.SINGLE, "");
        List<String> asked = new ArrayList<>();
        text.addSegmentListener(e -> asked.add(e.lineText));

        text.setText("abc");

        assertThat(asked).contains("abc");
    }

    @Test
    @DisplayName("segments up to the end of the text are accepted, and the text is kept without separators")
    void segmentsUpToTheEndOfTheTextAreAccepted() {
        Text text = text(SWT.SINGLE, "");
        text.addSegmentListener(e -> e.segments = new int[] {0, 1, 3});

        assertThatCode(() -> text.setText("abc")).doesNotThrowAnyException();
        assertThat(text.getText()).isEqualTo("abc");
    }

    @Test
    @DisplayName("segments out of order are refused")
    void segmentsOutOfOrderAreRefused() {
        Text text = text(SWT.SINGLE, "");
        text.addSegmentListener(e -> e.segments = new int[] {2, 1});

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> text.setText("abc"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("segments past the end of the text are refused")
    void segmentsPastTheEndAreRefused() {
        Text text = text(SWT.SINGLE, "");
        text.addSegmentListener(e -> e.segments = new int[] {0, 99});

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> text.setText("abc"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("an edit is answered when only a Display filter watches Verify")
    void editIsAnsweredWhenOnlyADisplayFilterWatches() {
        Text text = text(SWT.SINGLE, "old");
        DartDisplay impl = dartDisplay();
        when(impl.filters(SWT.Verify)).thenReturn(true);
        try {
            fromFlutter(text, "Modify/Modify", event("typed", 5, 5));
            runQueued();
        } finally {
            when(impl.filters(SWT.Verify)).thenReturn(false);
        }

        assertThat(text.getText()).isEqualTo("typed");
        assertThat(verdicts()).singleElement().asString().contains("true");
    }

    @Test
    @DisplayName("a segment request from Flutter reaches the segment listeners")
    void segmentRequestReachesListeners() {
        Text text = text(SWT.SINGLE, "abc");
        List<Event> events = record(text, SWT.Segments);

        fromFlutter(text, "Segment/Segments", event("abc", 0, 0));
        runQueued();

        assertThat(events).hasSize(1);
    }

    @Test
    @DisplayName("a segment request that arrives after the text was disposed is dropped")
    void segmentRequestToADisposedTextIsDropped() {
        Text text = text(SWT.SINGLE, "abc");
        List<Event> events = record(text, SWT.Segments);

        fromFlutter(text, "Segment/Segments", event("abc", 0, 0));
        text.dispose();
        runQueued();

        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("a selection change from Flutter moves the caret")
    void selectionFromFlutterMovesTheCaret() {
        Text text = text(SWT.SINGLE, "abcdef");
        Event selected = new Event();
        selected.index = 3;

        fromFlutter(text, "Selection/Selection", selected);
        runQueued();

        assertThat(text.getSelection()).isEqualTo(new Point(3, 3));
    }

    @Test
    @DisplayName("a selection change that arrives after the text was disposed is dropped")
    void selectionOfADisposedTextIsDropped() {
        Text text = text(SWT.SINGLE, "abcdef");
        fromFlutter(text, "Selection/Selection", new Event());
        text.dispose();

        assertThatCode(this::runQueued).as("a disposed text has no caret to move").doesNotThrowAnyException();
    }

    @Test
    @DisplayName("a caret report from Flutter reaches the Verify listeners clamped to the text")
    void caretReportReachesVerifyListenersClamped() {
        Text text = text(SWT.SINGLE, "abc");
        List<Event> verifies = record(text, SWT.Verify);

        fromFlutter(text, "Verify/Verify", event("x", 2, 99));
        runQueued();

        assertThat(verifies).hasSize(1);
        assertThat(verifies.get(0).start).isEqualTo(2);
        assertThat(verifies.get(0).end).isEqualTo(3);
    }

    @Test
    @DisplayName("a Verify from Flutter that carries no text is a real report")
    void verifyWithoutTextIsAReport() {
        Text text = text(SWT.SINGLE, "abc");
        List<Event> verifies = record(text, SWT.Verify);

        fromFlutter(text, "Verify/Verify", new Event());
        runQueued();

        assertThat(verifies).hasSize(1);
    }

    @Test
    @DisplayName("a deletion from Flutter is a Verify: it has a range, though no text")
    void deletionIsAVerify() {
        Text text = text(SWT.SINGLE, "abc");
        List<Event> verifies = record(text, SWT.Verify);

        fromFlutter(text, "Verify/Verify", event("", 1, 2));
        runQueued();

        assertThat(verifies).hasSize(1);
    }

    @Test
    @DisplayName("an empty proposal from Flutter, the echo of a write, is not a Verify")
    void echoOfAWriteIsNotAVerify() {
        Text text = text(SWT.SINGLE, "abc");
        List<Event> verifies = record(text, SWT.Verify);

        fromFlutter(text, "Verify/Verify", event("", 1, 1));
        runQueued();

        assertThat(verifies).isEmpty();
    }

    @Test
    @DisplayName("a Verify that arrives after the text was disposed is dropped")
    void verifyToADisposedTextIsDropped() {
        Text text = text(SWT.SINGLE, "abc");
        List<Event> verifies = record(text, SWT.Verify);

        fromFlutter(text, "Verify/Verify", event("x", 1, 1));
        text.dispose();
        runQueued();

        assertThat(verifies).isEmpty();
    }

    @Test
    @DisplayName("Return in a single-line text reaches the DefaultSelection listeners")
    void returnReachesDefaultSelectionListeners() {
        Text text = text(SWT.SINGLE, "abc");
        List<Event> events = record(text, SWT.DefaultSelection);

        fromFlutter(text, "Selection/DefaultSelection", new Event());
        runQueued();

        assertThat(events).hasSize(1);
        assertThat(events.get(0).widget).isSameAs(text);
    }

    @Test
    @DisplayName("a key listener that disposes the text before the DefaultSelection stops it")
    void disposingOnTheReturnKeyStopsTheDefaultSelection() {
        Text text = text(SWT.SINGLE, "abc");
        text.addListener(SWT.KeyDown, e -> text.dispose());
        List<Event> defaults = record(text, SWT.DefaultSelection);

        fromFlutter(text, "Selection/DefaultSelection", new Event());
        runQueued();

        assertThat(text.isDisposed()).isTrue();
        assertThat(defaults).isEmpty();
    }

    @Test
    @DisplayName("a Modify listener that disposes the text on cancel stops the DefaultSelection")
    void disposingOnTheClearedTextStopsTheCancel() {
        Text text = text(SWT.SEARCH | SWT.ICON_CANCEL, "query");
        text.addListener(SWT.Modify, e -> text.dispose());
        List<Event> defaults = record(text, SWT.DefaultSelection);
        Event cancel = new Event();
        cancel.detail = SWT.ICON_CANCEL;

        fromFlutter(text, "Selection/DefaultSelection", cancel);
        runQueued();

        assertThat(text.isDisposed()).isTrue();
        assertThat(defaults).isEmpty();
    }

    @Test
    @DisplayName("Return reaches the key listeners as a carriage return when no traversal takes it")
    void returnIsAlsoAKeyDown() {
        Text text = text(SWT.SINGLE, "abc");
        List<Event> keys = record(text, SWT.KeyDown);

        fromFlutter(text, "Selection/DefaultSelection", new Event());
        runQueued();

        assertThat(keys).extracting(e -> (int) e.character).contains((int) '\r');
    }

    @Test
    @DisplayName("the cancel icon of a search text clears it before the DefaultSelection")
    void cancelIconClearsTheText() {
        Text text = text(SWT.SEARCH | SWT.ICON_CANCEL, "query");
        List<String> textSeen = new ArrayList<>();
        text.addListener(SWT.DefaultSelection, e -> textSeen.add(text.getText()));
        Event cancel = new Event();
        cancel.detail = SWT.ICON_CANCEL;

        fromFlutter(text, "Selection/DefaultSelection", cancel);
        runQueued();

        assertThat(text.getText()).isEmpty();
        assertThat(textSeen).containsExactly("");
    }

    @Test
    @DisplayName("the cancel icon of an empty search text still reports the DefaultSelection")
    void cancelIconOfAnEmptyTextStillReports() {
        Text text = text(SWT.SEARCH | SWT.ICON_CANCEL, "");
        List<Event> events = record(text, SWT.DefaultSelection);
        Event cancel = new Event();
        cancel.detail = SWT.ICON_CANCEL;

        fromFlutter(text, "Selection/DefaultSelection", cancel);
        runQueued();

        assertThat(events).hasSize(1);
        assertThat(events.get(0).detail).isEqualTo(SWT.ICON_CANCEL);
    }

    @Test
    @DisplayName("a DefaultSelection that arrives after the text was disposed is dropped")
    void defaultSelectionToADisposedTextIsDropped() {
        Text text = text(SWT.SINGLE, "abc");
        List<Event> events = record(text, SWT.DefaultSelection);

        fromFlutter(text, "Selection/DefaultSelection", new Event());
        text.dispose();
        runQueued();

        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("a DefaultSelection while a modal shell blocks the text's shell is dropped")
    void defaultSelectionBehindAModalShellIsDropped() {
        Shell shell = DartMocks.dartShell(display);
        Shell modal = mock(Shell.class);
        when(((DartShell) shell.getImpl()).getModalShell()).thenReturn(modal);
        Text text = new Text(shell, SWT.SINGLE);
        List<Event> events = record(text, SWT.DefaultSelection);

        fromFlutter(text, "Selection/DefaultSelection", new Event());
        runQueued();

        assertThat(events).isEmpty();
    }
}
