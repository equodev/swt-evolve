package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

/**
 * The order a click on a List item reports its events in. Flutter picks the item at the press; the list
 * selects it then, and tells the Selection listeners after the MouseUp, as Win32 and Cocoa do.
 */
@Tag("native-unit")
class ListClickSelectionNativeTest {

    private RecordingBridge bridge;
    private Display display;
    private final java.util.List<Runnable> queued = new ArrayList<>();
    private final java.util.List<String> heard = new ArrayList<>();

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = DartMocks.dartDisplay();
        doAnswer(invocation -> queued.add(invocation.getArgument(0))).when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = (DartDisplay) display.getImpl();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
    }

    @AfterEach
    void tearDown() {
        doNothing().when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = (DartDisplay) display.getImpl();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        FlutterBridge.set(null);
    }

    private List list() {
        List list = new List(DartMocks.dartShell(display), SWT.SINGLE | SWT.V_SCROLL);
        list.setBounds(0, 0, 200, 120);
        for (int i = 0; i < 3; i++)
            list.add("item " + i);
        list.addListener(SWT.MouseDown, e -> heard.add("MouseDown"));
        list.addListener(SWT.MouseUp, e -> heard.add("MouseUp selection=" + list.getSelectionIndex()));
        list.addListener(SWT.Selection, e -> heard.add("Selection stateMask=" + e.stateMask));
        list.addListener(SWT.DefaultSelection, e -> heard.add("DefaultSelection"));
        return list;
    }

    private void fromFlutter(Widget widget, String channel, Event event) {
        bridge.comm.fireContaining(widget.getClass().getSimpleName() + "/" + widget.hashCode() + "/" + channel, event);
    }

    private void runQueued() {
        java.util.List<Runnable> now = new ArrayList<>(queued);
        queued.clear();
        now.forEach(Runnable::run);
    }

    private static Event mouse(int button) {
        Event event = new Event();
        event.button = button;
        event.x = 10;
        event.y = 30;
        return event;
    }

    private static Event pick(int index) {
        Event event = new Event();
        event.segments = new int[] {index};
        return event;
    }

    @Test
    @DisplayName("a click selects the item at the press and reports the Selection after the MouseUp, with no button held")
    void aClickReportsTheSelectionAfterTheMouseUp() {
        List list = list();

        fromFlutter(list, "Mouse/MouseDown", mouse(1));
        fromFlutter(list, "Selection/Selection", pick(1));
        fromFlutter(list, "Mouse/MouseUp", mouse(1));
        runQueued();

        assertThat(heard).containsExactly("MouseDown", "MouseUp selection=1", "Selection stateMask=0");
    }

    @Test
    @DisplayName("a Selection held for a MouseUp that never came is reported before the next press")
    void aHeldSelectionIsReportedBeforeTheNextPress() {
        List list = list();

        fromFlutter(list, "Mouse/MouseDown", mouse(1));
        fromFlutter(list, "Selection/Selection", pick(2));
        fromFlutter(list, "Mouse/MouseDown", mouse(1));
        runQueued();

        assertThat(heard).containsExactly("MouseDown", "Selection stateMask=0", "MouseDown");
    }

    @Test
    @DisplayName("a Selection with no button held, from the keyboard or between clicks, is reported at once")
    void aSelectionWithNoPressIsReportedAtOnce() {
        List list = list();

        fromFlutter(list, "Selection/Selection", pick(0));
        runQueued();
        assertThat(heard).containsExactly("Selection stateMask=0");

        Event down = new Event();
        down.keyCode = SWT.ARROW_DOWN;
        ListHelper.moveSelectionByKey((DartList) list.getImpl(), down);
        assertThat(heard).containsExactly("Selection stateMask=0", "Selection stateMask=0");
        assertThat(list.getSelectionIndex()).isEqualTo(1);
    }

    @Test
    @DisplayName("a double click reports its DefaultSelection at the second press, after the first click's Selection")
    void aDoubleClickKeepsItsOrder() {
        List list = list();

        fromFlutter(list, "Mouse/MouseDown", mouse(1));
        fromFlutter(list, "Selection/Selection", pick(1));
        fromFlutter(list, "Mouse/MouseUp", mouse(1));
        fromFlutter(list, "Mouse/MouseDown", mouse(1));
        fromFlutter(list, "Selection/DefaultSelection", pick(1));
        fromFlutter(list, "Mouse/MouseUp", mouse(1));
        runQueued();

        assertThat(heard).containsExactly("MouseDown", "MouseUp selection=1", "Selection stateMask=0",
                "MouseDown", "DefaultSelection", "MouseUp selection=1");
    }

    @Test
    @DisplayName("a pick made with another button down is reported at once, before the MouseUp")
    void aSelectionWithAnotherButtonIsReportedAtOnce() {
        List list = list();

        fromFlutter(list, "Mouse/MouseDown", mouse(3));
        fromFlutter(list, "Selection/Selection", pick(1));
        fromFlutter(list, "Mouse/MouseUp", mouse(3));
        runQueued();

        assertThat(heard).containsExactly("MouseDown", "Selection stateMask=0", "MouseUp selection=1");
    }

    @Test
    @DisplayName("a held Selection whose listener disposes the list ends the press: no later event is reported")
    void aHeldSelectionThatDisposesTheListAtTheNextPressEndsIt() {
        List list = list();
        list.addListener(SWT.Selection, e -> list.dispose());

        fromFlutter(list, "Mouse/MouseDown", mouse(1));
        fromFlutter(list, "Selection/Selection", pick(2));
        fromFlutter(list, "Mouse/MouseDown", mouse(1));
        runQueued();

        assertThat(heard).containsExactly("MouseDown", "Selection stateMask=0");
        assertThat(list.isDisposed()).isTrue();
    }

    @Test
    @DisplayName("a held Selection whose listener disposes the list drops the DefaultSelection that flushed it, filters included")
    void aHeldSelectionThatDisposesTheListDropsTheDefaultSelection() {
        List list = list();
        list.addListener(SWT.Selection, e -> list.dispose());
        DartDisplay impl = (DartDisplay) display.getImpl();
        java.util.List<Integer> filtered = new ArrayList<>();
        doReturn(true).when(impl).filters(SWT.DefaultSelection);
        doAnswer(invocation -> {
            filtered.add(invocation.<Event>getArgument(0).type);
            return false;
        }).when(impl).filterEvent(any(Event.class));
        try {
            fromFlutter(list, "Mouse/MouseDown", mouse(1));
            fromFlutter(list, "Selection/Selection", pick(1));
            fromFlutter(list, "Selection/DefaultSelection", pick(1));
            runQueued();
        } finally {
            doReturn(false).when(impl).filters(anyInt());
            doReturn(false).when(impl).filterEvent(any(Event.class));
        }

        assertThat(heard).containsExactly("MouseDown", "Selection stateMask=0");
        assertThat(filtered).doesNotContain(SWT.DefaultSelection);
    }
}
