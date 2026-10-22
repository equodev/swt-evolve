package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * What a click on a tip and on a scroll bar arriving from Flutter does. Both hop through {@code asyncExec}, so by
 * the time they run the widget may be gone.
 */
@Tag("native-unit")
class ToolTipAndScrollBarEventsFromFlutterNativeTest {

    private RecordingBridge bridge;
    private Display display;
    private Shell shell;
    private final List<Runnable> queued = new ArrayList<>();

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = DartMocks.dartDisplay();
        doAnswer(invocation -> queued.add(invocation.getArgument(0))).when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = (DartDisplay) display.getImpl();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        shell = DartMocks.dartShell(display);
    }

    @AfterEach
    void tearDown() {
        doNothing().when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = (DartDisplay) display.getImpl();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        FlutterBridge.set(null);
    }

    private void fromFlutter(Widget widget, String channel, Event event) {
        bridge.comm.fireContaining(widget.getClass().getSimpleName() + "/" + widget.hashCode() + "/" + channel, event);
    }

    private void runQueued() {
        List<Runnable> now = new ArrayList<>(queued);
        queued.clear();
        now.forEach(Runnable::run);
    }

    private static List<Event> record(Widget widget, int type) {
        List<Event> events = new ArrayList<>();
        widget.addListener(type, events::add);
        return events;
    }

    @Test
    @DisplayName("a click on a tip hides it and tells the Selection listeners")
    void aClickOnATipHidesIt() {
        ToolTip tip = new ToolTip(shell, SWT.NONE);
        tip.setVisible(true);
        List<Event> events = record(tip, SWT.Selection);

        fromFlutter(tip, "Selection/Selection", new Event());
        runQueued();

        assertThat(tip.getVisible()).isFalse();
        assertThat(events).hasSize(1);
    }

    @Test
    @DisplayName("a DefaultSelection on a tip tells the DefaultSelection listeners and leaves it shown")
    void aDefaultSelectionOnATipLeavesItShown() {
        ToolTip tip = new ToolTip(shell, SWT.NONE);
        tip.setVisible(true);
        List<Event> events = record(tip, SWT.DefaultSelection);

        fromFlutter(tip, "Selection/DefaultSelection", new Event());
        runQueued();

        assertThat(tip.getVisible()).isTrue();
        assertThat(events).hasSize(1);
    }

    @Test
    @DisplayName("a drag on a scroll bar moves it and tells the Selection listeners the drag")
    void aDragOnAScrollBarMovesIt() {
        ScrollBar bar = new Canvas(shell, SWT.H_SCROLL).getHorizontalBar();
        List<Event> events = record(bar, SWT.Selection);
        Event drag = new Event();
        drag.index = 30;
        drag.detail = SWT.DRAG;

        fromFlutter(bar, "Selection/Selection", drag);
        runQueued();

        assertThat(bar.getSelection()).isEqualTo(30);
        assertThat(events).singleElement().satisfies(e -> assertThat(e.detail).isEqualTo(SWT.DRAG));
    }

    @Test
    @DisplayName("a DefaultSelection on a scroll bar tells the DefaultSelection listeners")
    void aDefaultSelectionOnAScrollBar() {
        ScrollBar bar = new Canvas(shell, SWT.V_SCROLL).getVerticalBar();
        List<Event> events = record(bar, SWT.DefaultSelection);

        fromFlutter(bar, "Selection/DefaultSelection", new Event());
        runQueued();

        assertThat(events).hasSize(1);
    }

    @Test
    @DisplayName("what Flutter sends for a tip or a scroll bar disposed before it arrives is dropped")
    void eventsForADisposedWidgetAreDropped() {
        ToolTip tip = new ToolTip(shell, SWT.NONE);
        Canvas canvas = new Canvas(shell, SWT.H_SCROLL);
        ScrollBar bar = canvas.getHorizontalBar();
        List<Event> events = new ArrayList<>();
        for (Widget widget : new Widget[] {tip, bar}) {
            widget.addListener(SWT.Selection, events::add);
            widget.addListener(SWT.DefaultSelection, events::add);
            fromFlutter(widget, "Selection/Selection", new Event());
            fromFlutter(widget, "Selection/DefaultSelection", new Event());
        }

        tip.dispose();
        canvas.dispose();
        runQueued();

        assertThat(events).isEmpty();
    }
}
