package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * What the text a user types into a Spinner does on the Java side. A native spinner reports every edit of its
 * field as it happens: the text is read, a value it stands for in range becomes the selection (and is notified
 * as Selection), and Modify is notified whatever the text was; a partial or out-of-range text leaves the
 * selection where it was. Listeners that verify are asked about the characters being replaced first.
 *
 * <p>Whether Selection and Modify are posted or sent depends on the platform (Win32 sends, Cocoa and GTK post),
 * so the tests that go through the bridge collect both kinds and read what the listeners heard in order.
 */
@Tag("native-unit")
class SpinnerTypedEditFromFlutterNativeTest {

    private RecordingBridge bridge;
    private Display display;
    private final List<Runnable> queued = new ArrayList<>();
    /** The events the spinner posted to the display instead of sending. */
    private final List<Event> posted = new ArrayList<>();

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = DartMocks.dartDisplay();
        doAnswer(invocation -> queued.add(invocation.getArgument(0))).when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = dartDisplay();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        doAnswer(invocation -> posted.add(invocation.getArgument(0))).when(impl).postEvent(any(Event.class));
    }

    @AfterEach
    void tearDown() {
        doNothing().when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = dartDisplay();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        doNothing().when(impl).postEvent(any(Event.class));
        FlutterBridge.set(null);
    }

    private DartDisplay dartDisplay() {
        return (DartDisplay) display.getImpl();
    }

    private Spinner spinner(int selection) {
        Spinner spinner = new Spinner(DartMocks.dartShell(display), SWT.NONE);
        spinner.setSelection(selection);
        posted.clear();
        return spinner;
    }

    /** The edit the client reports: the new text, the range of the previous one it replaced, and how much it put there. */
    private static Event edit(String text, int start, int end, int count) {
        Event event = new Event();
        event.text = text;
        event.start = start;
        event.end = end;
        event.count = count;
        return event;
    }

    /** Typing {@code text} over the whole previous text of {@code previousLength} characters. */
    private static Event typedOver(int previousLength, String text) {
        return edit(text, 0, previousLength, text.length());
    }

    private void fromFlutter(Spinner spinner, Event event) {
        bridge.comm.fireContaining("Spinner/" + spinner.hashCode() + "/Modify/Modify", event);
        runQueued();
    }

    private void runQueued() {
        List<Runnable> now = new ArrayList<>(queued);
        queued.clear();
        now.forEach(Runnable::run);
    }

    /** Hands the posted events to their listeners, as the display does when it runs its deferred events. */
    private void deliverPosted(Spinner spinner) {
        List<Event> now = new ArrayList<>(posted);
        posted.clear();
        for (Event event : now) {
            // The display does not deliver to a widget that went away in the meantime.
            if (!spinner.isDisposed()) ((DartSpinner) spinner.getImpl()).sendEvent(event);
        }
    }

    /** What the Selection and Modify listeners heard, in order, with the selection each one read. */
    private List<String> hear(Spinner spinner) {
        List<String> heard = new ArrayList<>();
        spinner.addListener(SWT.Selection, e -> heard.add("Selection@" + spinner.getSelection()));
        spinner.addListener(SWT.Modify, e -> heard.add("Modify@" + spinner.getSelection()));
        return heard;
    }

    private List<String> verdicts() {
        List<String> verdicts = new ArrayList<>();
        for (RecordingComm.Frame frame : bridge.comm.sent) {
            if (frame.event.contains("modify/verdict")) verdicts.add(frame.json);
        }
        return verdicts;
    }

    @Test
    @DisplayName("a value typed into the field moves the selection and notifies Selection, then Modify")
    void typedValueMovesTheSelection() {
        Spinner spinner = spinner(25);
        List<String> heard = hear(spinner);

        fromFlutter(spinner, typedOver(2, "60"));
        deliverPosted(spinner);

        assertThat(spinner.getSelection()).isEqualTo(60);
        assertThat(spinner.getText()).isEqualTo("60");
        assertThat(heard).containsExactly("Selection@60", "Modify@60");
    }

    @Test
    @DisplayName("each keystroke of a value is an edit of its own")
    void everyKeystrokeNotifies() {
        Spinner spinner = spinner(0);
        List<String> heard = hear(spinner);

        fromFlutter(spinner, edit("5", 0, 1, 1));
        deliverPosted(spinner);
        fromFlutter(spinner, edit("57", 1, 1, 1));
        deliverPosted(spinner);

        assertThat(heard).containsExactly("Selection@5", "Modify@5", "Selection@57", "Modify@57");
    }

    @Test
    @DisplayName("an edit that leaves the same value notifies Modify only")
    void sameValueNotifiesModifyOnly() {
        Spinner spinner = spinner(25);
        List<String> heard = hear(spinner);

        fromFlutter(spinner, edit("025", 0, 0, 1));
        deliverPosted(spinner);

        assertThat(spinner.getSelection()).isEqualTo(25);
        assertThat(heard).containsExactly("Modify@25");
    }

    @Test
    @DisplayName("an empty, partial or non-numeric text keeps the selection and still notifies Modify")
    void partialTextKeepsTheSelection() {
        Spinner spinner = spinner(25);
        spinner.setMinimum(-100);
        List<String> heard = hear(spinner);

        fromFlutter(spinner, edit("", 0, 2, 0));
        deliverPosted(spinner);
        fromFlutter(spinner, edit("-", 0, 0, 1));
        deliverPosted(spinner);
        fromFlutter(spinner, edit("2x", 1, 1, 1));
        deliverPosted(spinner);

        assertThat(spinner.getSelection()).isEqualTo(25);
        assertThat(heard).containsExactly("Modify@25", "Modify@25", "Modify@25");
    }

    @Test
    @DisplayName("a value outside the range is no value: the selection is not clamped to it")
    void outOfRangeTextKeepsTheSelection() {
        Spinner spinner = spinner(25);
        List<String> heard = hear(spinner);

        fromFlutter(spinner, typedOver(2, "500"));
        deliverPosted(spinner);

        assertThat(spinner.getSelection()).isEqualTo(25);
        assertThat(heard).containsExactly("Modify@25");
    }

    @Test
    @DisplayName("a negative value typed into a spinner that can go below zero is the selection")
    void negativeValue() {
        Spinner spinner = spinner(25);
        spinner.setMinimum(-100);

        fromFlutter(spinner, typedOver(2, "-40"));

        assertThat(spinner.getSelection()).isEqualTo(-40);
    }

    @Test
    @DisplayName("the decimals of the text are scaled by the digits, read with the host's separator")
    void decimalText() {
        Spinner spinner = spinner(0);
        spinner.setDigits(2);
        spinner.setMaximum(100000);
        String separator = SpinnerHelper.decimalSeparator();

        fromFlutter(spinner, typedOver(4, "1" + separator + "5"));
        assertThat(spinner.getSelection()).isEqualTo(150);

        fromFlutter(spinner, typedOver(3, "2"));
        assertThat(spinner.getSelection()).isEqualTo(200);

        fromFlutter(spinner, typedOver(1, "7" + separator + "256"));
        assertThat(spinner.getSelection()).as("places beyond the digits are dropped").isEqualTo(725);
    }

    @Test
    @DisplayName("an edit that arrives after the spinner was disposed is dropped")
    void editOfADisposedSpinnerIsDropped() {
        Spinner spinner = spinner(25);
        List<String> heard = hear(spinner);

        bridge.comm.fireContaining("Spinner/" + spinner.hashCode() + "/Modify/Modify", typedOver(2, "60"));
        spinner.dispose();
        runQueued();

        assertThat(heard).isEmpty();
        assertThat(posted).isEmpty();
    }

    @Test
    @DisplayName("a Selection listener that disposes the spinner ends the edit before Modify")
    void selectionListenerDisposesTheSpinner() {
        Spinner spinner = spinner(25);
        spinner.addListener(SWT.Selection, e -> spinner.dispose());
        spinner.addListener(SWT.Modify, e -> {
            throw new AssertionError("Modify reached a disposed spinner");
        });

        fromFlutter(spinner, typedOver(2, "60"));
        deliverPosted(spinner);

        assertThat(spinner.isDisposed()).isTrue();
    }

    @Test
    @DisplayName("Enter on a value an edit already moved notifies DefaultSelection only")
    void enterAfterTypingDoesNotRepeat() {
        Spinner spinner = spinner(25);
        fromFlutter(spinner, typedOver(2, "60"));
        deliverPosted(spinner);
        List<String> heard = hear(spinner);
        List<Event> defaults = new ArrayList<>();
        spinner.addListener(SWT.DefaultSelection, defaults::add);

        Event enter = new Event();
        enter.index = 60;
        bridge.comm.fireContaining("Spinner/" + spinner.hashCode() + "/Selection/DefaultSelection", enter);
        runQueued();
        deliverPosted(spinner);

        assertThat(heard).isEmpty();
        assertThat(defaults).hasSize(1);
    }

    @Test
    @DisplayName("a Verify listener is asked about the characters being replaced, and the edit goes on")
    void verifyIsAskedAboutTheReplacedRange() {
        Spinner spinner = spinner(25);
        spinner.setMaximum(1000);
        List<Event> asked = new ArrayList<>();
        spinner.addListener(SWT.Verify, e -> {
            Event seen = new Event();
            seen.text = e.text;
            seen.start = e.start;
            seen.end = e.end;
            asked.add(seen);
        });

        fromFlutter(spinner, edit("265", 1, 1, 1));

        assertThat(asked).hasSize(1);
        assertThat(asked.get(0).text).isEqualTo("6");
        assertThat(asked.get(0).start).isEqualTo(1);
        assertThat(asked.get(0).end).isEqualTo(1);
        assertThat(spinner.getSelection()).isEqualTo(265);
        assertThat(verdicts()).hasSize(1);
        assertThat(verdicts().get(0)).contains("true");
    }

    @Test
    @DisplayName("a Verify listener that vetoes an edit stops it: no selection, no Modify, and the client is told")
    void vetoStopsTheEdit() {
        Spinner spinner = spinner(25);
        spinner.addListener(SWT.Verify, e -> e.doit = false);
        List<String> heard = hear(spinner);

        fromFlutter(spinner, typedOver(2, "60"));
        deliverPosted(spinner);

        assertThat(spinner.getSelection()).isEqualTo(25);
        assertThat(heard).isEmpty();
        assertThat(verdicts()).hasSize(1);
        assertThat(verdicts().get(0)).contains("false");
    }

    @Test
    @DisplayName("a Verify listener that changes the characters makes the edit be those characters")
    void verifyCanChangeTheCharacters() {
        Spinner spinner = spinner(25);
        spinner.addListener(SWT.Verify, e -> {
            if ("6".equals(e.text)) e.text = "7";
        });

        fromFlutter(spinner, edit("6", 0, 2, 1));

        assertThat(spinner.getSelection()).isEqualTo(7);
        assertThat(verdicts().get(0)).as("the client shows the value Java holds, not what was typed").contains("false");
    }

    @Test
    @DisplayName("nobody listening for Verify, nobody is asked and the client hears nothing back")
    void noVerifyNoVerdict() {
        Spinner spinner = spinner(25);

        fromFlutter(spinner, typedOver(2, "60"));

        assertThat(verdicts()).isEmpty();
    }

    @Test
    @DisplayName("a Modify without an edit in it is only announced")
    void modifyWithoutAnEditIsAnnounced() {
        Spinner spinner = spinner(25);
        List<String> heard = hear(spinner);

        fromFlutter(spinner, new Event());

        assertThat(heard).containsExactly("Modify@25");
        assertThat(spinner.getSelection()).isEqualTo(25);
    }

    @Test
    @DisplayName("Win32 sends Selection and Modify while the edit is handled")
    void win32SendsBoth() {
        Spinner spinner = spinner(25);
        List<String> heard = hear(spinner);

        SpinnerHelper.editedInFlutter((DartSpinner) spinner.getImpl(), typedOver(2, "60"), "win32");

        assertThat(heard).containsExactly("Selection@60", "Modify@60");
        assertThat(posted).isEmpty();
    }

    @Test
    @DisplayName("Cocoa and GTK post Selection, then Modify, to be delivered after the edit is handled")
    void cocoaAndGtkPostBoth() {
        for (String platform : new String[]{"cocoa", "gtk"}) {
            Spinner spinner = spinner(25);
            List<String> heard = hear(spinner);

            SpinnerHelper.editedInFlutter((DartSpinner) spinner.getImpl(), typedOver(2, "60"), platform);

            assertThat(heard).as(platform + ": nothing is heard until the display delivers").isEmpty();
            assertThat(spinner.getSelection()).as(platform).isEqualTo(60);
            assertThat(posted).extracting(e -> e.type).as(platform).containsExactly(SWT.Selection, SWT.Modify);
            deliverPosted(spinner);
            assertThat(heard).as(platform).containsExactly("Selection@60", "Modify@60");
        }
    }

    @Test
    @DisplayName("what Verify is told it may let through: Cocoa takes digits and a point, the others also the minus and the separator")
    void defaultVerdictOfVerify() {
        for (String platform : new String[]{"cocoa", "win32"}) {
            Spinner spinner = spinner(0);
            spinner.setMinimum(-100);
            spinner.setDigits(1);
            List<String> byText = new ArrayList<>();
            spinner.addListener(SWT.Verify, e -> byText.add(e.text + "=" + e.doit));
            DartSpinner impl = (DartSpinner) spinner.getImpl();
            String separator = SpinnerHelper.decimalSeparator();

            SpinnerHelper.editedInFlutter(impl, edit("5", 0, 0, 1), platform);
            SpinnerHelper.editedInFlutter(impl, edit("-", 0, 0, 1), platform);
            SpinnerHelper.editedInFlutter(impl, edit(separator, 0, 0, 1), platform);
            SpinnerHelper.editedInFlutter(impl, edit("x", 0, 0, 1), platform);

            boolean cocoa = platform.equals("cocoa");
            assertThat(byText).as(platform).containsExactly(
                    "5=true",
                    "-=" + !cocoa,
                    separator + "=" + (!cocoa || ".".equals(separator)),
                    "x=false");
        }
    }

    @Test
    @DisplayName("a sign and a leading separator are read as they are written")
    void signsAndLeadingSeparator() {
        Spinner spinner = spinner(0);
        spinner.setDigits(1);
        spinner.setMinimum(-1000);
        spinner.setMaximum(1000);
        String separator = SpinnerHelper.decimalSeparator();

        fromFlutter(spinner, typedOver(3, "+1" + separator + "5"));
        assertThat(spinner.getSelection()).isEqualTo(15);

        fromFlutter(spinner, typedOver(4, "-2" + separator + "5"));
        assertThat(spinner.getSelection()).isEqualTo(-25);

        fromFlutter(spinner, typedOver(4, separator + "5"));
        assertThat(spinner.getSelection()).isEqualTo(5);

        fromFlutter(spinner, typedOver(2, "-" + separator + "5"));
        assertThat(spinner.getSelection()).isEqualTo(-5);
    }

    @Test
    @DisplayName("a value below the minimum is no value either")
    void belowTheMinimumKeepsTheSelection() {
        Spinner spinner = spinner(25);

        fromFlutter(spinner, typedOver(2, "-5"));

        assertThat(spinner.getSelection()).isEqualTo(25);
    }

    @Test
    @DisplayName("Enter on a value out of range that clamps to the one it had notifies DefaultSelection only")
    void enterClampedToTheSameValue() {
        Spinner spinner = spinner(100);
        List<Event> selections = new ArrayList<>();
        spinner.addListener(SWT.Selection, selections::add);
        List<Event> defaults = new ArrayList<>();
        spinner.addListener(SWT.DefaultSelection, defaults::add);

        Event enter = new Event();
        enter.index = 500;
        bridge.comm.fireContaining("Spinner/" + spinner.hashCode() + "/Selection/DefaultSelection", enter);
        runQueued();
        deliverPosted(spinner);

        assertThat(spinner.getSelection()).isEqualTo(100);
        assertThat(selections).isEmpty();
        assertThat(defaults).hasSize(1);
    }

    @Test
    @DisplayName("a Verify filter of the display is asked for, though no listener of the spinner is")
    void verifyFilterOfTheDisplay() {
        Spinner spinner = spinner(25);
        DartDisplay impl = dartDisplay();
        when(impl.filters(SWT.Verify)).thenReturn(true);
        try {
            fromFlutter(spinner, typedOver(2, "60"));
        } finally {
            when(impl.filters(SWT.Verify)).thenReturn(false);
        }

        assertThat(spinner.getSelection()).isEqualTo(60);
        assertThat(verdicts()).hasSize(1);
    }

    @Test
    @DisplayName("a Verify listener that disposes the spinner ends the edit")
    void verifyListenerDisposesTheSpinner() {
        for (String platform : new String[]{"cocoa", "win32"}) {
            Spinner spinner = spinner(25);
            spinner.addListener(SWT.Verify, e -> spinner.dispose());
            List<Event> modifies = new ArrayList<>();
            spinner.addListener(SWT.Modify, modifies::add);

            SpinnerHelper.editedInFlutter((DartSpinner) spinner.getImpl(), typedOver(2, "60"), platform);

            assertThat(spinner.isDisposed()).as(platform).isTrue();
            assertThat(modifies).as(platform).isEmpty();
        }
    }

    @Test
    @DisplayName("a Selection listener that disposes the spinner ends an edit that Win32 sent before Modify")
    void sentSelectionListenerDisposesTheSpinner() {
        Spinner spinner = spinner(25);
        spinner.addListener(SWT.Selection, e -> spinner.dispose());
        List<Event> modifies = new ArrayList<>();
        spinner.addListener(SWT.Modify, modifies::add);

        SpinnerHelper.editedInFlutter((DartSpinner) spinner.getImpl(), typedOver(2, "60"), "win32");

        assertThat(spinner.isDisposed()).isTrue();
        assertThat(modifies).isEmpty();
    }

    @Test
    @DisplayName("without digits, Verify on the others takes no separator; a minus needs a minimum below zero")
    void verifyDefaultsWithoutDigits() {
        Spinner spinner = spinner(0);
        List<String> byText = new ArrayList<>();
        spinner.addListener(SWT.Verify, e -> byText.add(e.text + "=" + e.doit));
        DartSpinner impl = (DartSpinner) spinner.getImpl();

        SpinnerHelper.editedInFlutter(impl, edit("-", 0, 0, 1), "win32");
        SpinnerHelper.editedInFlutter(impl, edit("7", 0, 0, 1), "win32");
        SpinnerHelper.editedInFlutter(impl, edit("7", 0, 0, 1), "cocoa");

        assertThat(byText).containsExactly("-=false", "7=true", "7=true");
    }
}
