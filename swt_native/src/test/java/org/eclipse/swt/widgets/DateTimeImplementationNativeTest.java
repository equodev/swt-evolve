package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

/** What {@link DartDateTime} does when it is driven directly rather than through {@link DateTime}. */
@Tag("native-unit")
class DateTimeImplementationNativeTest {

    private final RecordingBridge bridge = new RecordingBridge();
    private Shell shell;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(bridge);
        shell = DartMocks.dartShell();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a rejected change does not mark the value for the next push")
    void aRejectedChangeIsNotPushed() {
        DateTime picker = new DateTime(shell, SWT.TIME);
        picker.setTime(10, 20, 30);
        VDateTime value = ((DartDateTime) picker.getImpl()).getValue();
        value.clearDirty();
        picker.setHours(24);
        picker.setMinutes(60);
        picker.setSeconds(-1);
        assertThat(value.anyDirty()).isFalse();
        picker.setHours(11);
        assertThat(value.anyDirty()).isTrue();
    }

    @Test
    @DisplayName("a pick from Flutter sets the date or the time it carries and tells the listeners; a disposed picker drops it")
    void picksFromFlutter() {
        List<Runnable> queued = queueAsyncExec();
        DateTime date = new DateTime(shell, SWT.DATE);
        DateTime time = new DateTime(shell, SWT.TIME);
        List<Integer> heard = new ArrayList<>();
        date.addListener(SWT.Selection, e -> heard.add(e.type));
        time.addListener(SWT.Selection, e -> heard.add(e.type));

        Event day = new Event();
        day.x = 2024;
        day.y = 1;
        day.width = 29;
        bridge.comm.fireContaining("DateTime/" + date.hashCode() + "/Selection/Selection", day);
        Event clock = new Event();
        clock.height = 13;
        clock.count = 45;
        clock.index = 30;
        bridge.comm.fireContaining("DateTime/" + time.hashCode() + "/Selection/Selection", clock);
        runAll(queued);

        assertThat(new int[] {date.getYear(), date.getMonth(), date.getDay()}).containsExactly(2024, 1, 29);
        assertThat(new int[] {time.getHours(), time.getMinutes(), time.getSeconds()}).containsExactly(13, 45, 30);
        assertThat(heard).containsExactly(SWT.Selection, SWT.Selection);

        bridge.comm.fireContaining("DateTime/" + date.hashCode() + "/Selection/Selection", day);
        date.dispose();
        runAll(queued);
        assertThat(heard).hasSize(2);
    }

    @Test
    @DisplayName("a DefaultSelection from Flutter reaches an active picker, and not one under a modal shell or disposed")
    void defaultSelectionFromFlutter() {
        List<Runnable> queued = queueAsyncExec();
        DateTime picker = new DateTime(shell, SWT.DATE);
        List<Event> heard = new ArrayList<>();
        picker.addListener(SWT.DefaultSelection, heard::add);

        bridge.comm.fireContaining("/Selection/DefaultSelection", new Event());
        runAll(queued);
        assertThat(heard).hasSize(1);

        Shell modal = DartMocks.dartShell();
        when(((DartShell) shell.getImpl()).getModalShell()).thenReturn(modal);
        bridge.comm.fireContaining("/Selection/DefaultSelection", new Event());
        runAll(queued);
        assertThat(heard).hasSize(1);

        bridge.comm.fireContaining("/Selection/DefaultSelection", new Event());
        picker.dispose();
        runAll(queued);
        assertThat(heard).hasSize(1);
    }

    /** Events reach the listeners, and what the hooks post with asyncExec waits until {@link #runAll}. */
    private List<Runnable> queueAsyncExec() {
        Display display = shell.getDisplay();
        DartDisplay displayImpl = (DartDisplay) display.getImpl();
        doCallRealMethod().when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        List<Runnable> queued = new ArrayList<>();
        doAnswer(inv -> queued.add(inv.getArgument(0))).when(display).asyncExec(any(Runnable.class));
        return queued;
    }

    private static void runAll(List<Runnable> queued) {
        List<Runnable> now = new ArrayList<>(queued);
        queued.clear();
        now.forEach(Runnable::run);
    }
}
