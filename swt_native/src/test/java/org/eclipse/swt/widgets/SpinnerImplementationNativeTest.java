package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;

/** What {@link DartSpinner} does when it is driven directly rather than through {@link Spinner}. */
@Tag("native-unit")
class SpinnerImplementationNativeTest {

    private Display display;
    private Shell shell;
    /** Events posted to the display queue; a Selection is posted, as in SWT, not sent. */
    private final List<Event> posted = new ArrayList<>();

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = DartMocks.dartDisplay();
        // The mock display would swallow every event; its real dispatch only needs the listener table.
        DartDisplay impl = dartDisplay();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        doAnswer(invocation -> posted.add(invocation.getArgument(0))).when(impl).postEvent(any(Event.class));
        shell = DartMocks.dartShell(display);
    }

    @AfterEach
    void tearDown() {
        // The display mock is shared by the whole suite: hand it back as the other tests expect it.
        DartDisplay impl = dartDisplay();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        doNothing().when(impl).postEvent(any(Event.class));
        FlutterBridge.set(null);
    }

    private DartDisplay dartDisplay() {
        return (DartDisplay) display.getImpl();
    }

    private List<Event> postedSelections(Spinner spinner) {
        return posted.stream().filter(e -> e.widget == spinner && e.type == SWT.Selection).toList();
    }

    private static List<Integer> held(DartSpinner impl) {
        return List.of(impl._minimum(), impl._maximum(), impl._selection(), impl._increment(), impl._pageIncrement(),
                impl._digits(), impl._textLimit());
    }

    @Test
    @DisplayName("an implementation built without its API object creates one, once, bound to itself")
    void implementationWithoutApiCreatesOne() {
        DartSpinner impl = new DartSpinner(shell, SWT.NONE, null);

        Spinner api = impl.getApi();

        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
    }

    @Test
    @DisplayName("the implementation reports every value it holds")
    void implementationReportsWhatItHolds() {
        Spinner spinner = new Spinner(shell, SWT.NONE);
        DartSpinner impl = (DartSpinner) spinner.getImpl();

        assertThat(held(impl)).containsExactly(0, 100, 0, 1, 10, 0, Spinner.LIMIT);

        spinner.setValues(137, 20, 250, 2, 3, 40);
        spinner.setTextLimit(12);
        assertThat(held(impl)).containsExactly(20, 250, 137, 3, 40, 2, 12);
    }

    @Test
    @DisplayName("the native view callbacks answer as SWT's do and leave the values alone")
    void nativeCallbacksAreInert() {
        Spinner spinner = new Spinner(shell, SWT.NONE);
        DartSpinner impl = (DartSpinner) spinner.getImpl();
        spinner.setValues(40, 0, 100, 1, 2, 20);
        List<Event> seen = new ArrayList<>();
        spinner.addListener(SWT.Selection, seen::add);
        List<Integer> before = held(impl);

        assertThat(impl.handleIsAccessible(0)).isTrue();
        assertThat(impl.isEventView(0)).isTrue();
        impl.setSmallSize();
        impl.setForeground((double[]) null);
        impl.setForeground(new double[] {0.1, 0.2, 0.3});

        assertThat(held(impl)).isEqualTo(before);
        assertThat(seen).isEmpty();
    }

    @Test
    @DisplayName("the stripped text parser reports a parse failure for any digits")
    void textParserAlwaysFails() {
        Spinner spinner = new Spinner(shell, SWT.NONE);
        DartSpinner impl = (DartSpinner) spinner.getImpl();

        for (int digits : new int[] {0, 2}) {
            spinner.setDigits(digits);
            boolean[] parseFail = new boolean[1];
            assertThat(impl.getSelectionText(parseFail)).isEqualTo(-1);
            assertThat(parseFail[0]).as("digits %d", digits).isTrue();
        }
    }

    @Test
    @DisplayName("the native selection callback sends Modify and Selection for the value it holds")
    void nativeSelectionCallbackReportsTheHeldValue() {
        Spinner spinner = new Spinner(shell, SWT.NONE);
        DartSpinner impl = (DartSpinner) spinner.getImpl();
        spinner.setSelection(40);
        List<Event> modify = new ArrayList<>();
        spinner.addListener(SWT.Modify, modify::add);
        spinner.addListener(SWT.Selection, e -> { });

        impl.sendSelection();

        assertThat(spinner.getSelection()).isEqualTo(40);
        assertThat(modify).hasSize(1);
        assertThat(postedSelections(spinner)).hasSize(1);
    }

    @Test
    @DisplayName("setSelection with the text flag off stores the value without a Modify")
    void selectionWithoutTheTextFlagSendsNoModify() {
        Spinner spinner = new Spinner(shell, SWT.NONE);
        DartSpinner impl = (DartSpinner) spinner.getImpl();
        List<Event> modify = new ArrayList<>();
        spinner.addListener(SWT.Modify, modify::add);

        impl.setSelection(55, false, false, false);

        assertThat(spinner.getSelection()).isEqualTo(55);
        assertThat(modify).isEmpty();
    }

    @Test
    @DisplayName("setSelection with the notify flag sends Selection once")
    void selectionWithTheNotifyFlagSendsSelection() {
        Spinner spinner = new Spinner(shell, SWT.NONE);
        DartSpinner impl = (DartSpinner) spinner.getImpl();
        spinner.addListener(SWT.Selection, e -> { });

        impl.setSelection(55, true, true, true);

        assertThat(spinner.getSelection()).isEqualTo(55);
        assertThat(postedSelections(spinner)).hasSize(1);
    }

    @Test
    @DisplayName("a spinner with a Verify listener still takes the value it is given")
    void aVerifyListenerDoesNotChangeTheStoredValue() {
        Spinner spinner = new Spinner(shell, SWT.NONE);
        DartSpinner impl = (DartSpinner) spinner.getImpl();
        impl.addVerifyListener(e -> { });

        spinner.setSelection(33);

        assertThat(spinner.getSelection()).isEqualTo(33);
        assertThat(spinner.getText()).isEqualTo("33");
    }

    @Test
    @DisplayName("Verify listeners are added and removed, and a null one is refused")
    void verifyListenersAreAddedAndRemoved() {
        Spinner spinner = new Spinner(shell, SWT.NONE);
        DartSpinner impl = (DartSpinner) spinner.getImpl();
        VerifyListener listener = e -> { };

        impl.removeVerifyListener(listener);
        impl.addVerifyListener(listener);
        assertThat(spinner.isListening(SWT.Verify)).isTrue();
        impl.removeVerifyListener(listener);
        assertThat(spinner.isListening(SWT.Verify)).isFalse();
        assertThatThrownBy(() -> impl.removeVerifyListener(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
