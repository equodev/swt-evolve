package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * What a pick, a default selection and a drop-down request arriving from Flutter does to a ToolItem and its
 * radio group. Every one hops through {@code asyncExec}, so by the time it runs the item may be gone.
 */
@Tag("native-unit")
class ToolItemEventsFromFlutterNativeTest {

    private RecordingBridge bridge;
    private Display display;
    private Shell shell;
    private final List<Runnable> queued = new ArrayList<>();

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
        display = DartMocks.dartDisplay();
        doAnswer(invocation -> queued.add(invocation.getArgument(0))).when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = (DartDisplay) display.getImpl();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        doAnswer(invocation -> {
            Event posted = invocation.getArgument(0);
            posted.widget.notifyListeners(posted.type, posted);
            return null;
        }).when(impl).postEvent(any(Event.class));
        shell = DartMocks.dartShell(display);
    }

    @AfterEach
    void tearDown() {
        doNothing().when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = (DartDisplay) display.getImpl();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        doNothing().when(impl).postEvent(any(Event.class));
        FlutterBridge.set(null);
    }

    private void fromFlutter(Widget widget, String channel) {
        fromFlutter(widget, channel, new Event());
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
    @DisplayName("an event the item does not handle itself reaches the one its base class handles")
    void anEventTheItemDoesNotHandleReachesItsBaseClass() {
        ToolItem item = new ToolItem(new ToolBar(shell, SWT.NONE), SWT.PUSH);
        List<Event> events = record(item, SWT.Dispose);

        fromFlutter(item, "Dispose/Dispose");
        runQueued();

        assertThat(events).hasSize(1);
    }

    @Test
    @DisplayName("a pick on a check item flips it and tells the Selection listeners")
    void aPickFlipsACheckItem() {
        ToolItem item = new ToolItem(new ToolBar(shell, SWT.NONE), SWT.CHECK);
        List<Event> events = record(item, SWT.Selection);

        fromFlutter(item, "Selection/Selection");
        runQueued();
        assertThat(item.getSelection()).isTrue();
        assertThat(events).hasSize(1);

        fromFlutter(item, "Selection/Selection");
        runQueued();
        assertThat(item.getSelection()).isFalse();
        assertThat(events).hasSize(2);
    }

    @Test
    @DisplayName("a pick on a push item only tells the Selection listeners")
    void aPickOnAPushItemOnlyTellsTheListeners() {
        ToolItem item = new ToolItem(new ToolBar(shell, SWT.NONE), SWT.PUSH);
        List<Event> events = record(item, SWT.Selection);

        fromFlutter(item, "Selection/Selection");
        runQueued();

        assertThat(item.getSelection()).isFalse();
        assertThat(events).hasSize(1);
    }

    @Test
    @DisplayName("a pick on a radio item selects it and deselects the selected ones of its group, telling each")
    void aPickSelectsOneRadioOfItsGroup() {
        ToolBar bar = new ToolBar(shell, SWT.NONE);
        ToolItem first = new ToolItem(bar, SWT.RADIO);
        ToolItem second = new ToolItem(bar, SWT.RADIO);
        ToolItem third = new ToolItem(bar, SWT.RADIO);
        first.setSelection(true);
        third.setSelection(true);
        List<Event> firstHears = record(first, SWT.Selection);
        List<Event> secondHears = record(second, SWT.Selection);
        List<Event> thirdHears = record(third, SWT.Selection);

        fromFlutter(second, "Selection/Selection");
        runQueued();

        assertThat(first.getSelection()).isFalse();
        assertThat(second.getSelection()).isTrue();
        assertThat(third.getSelection()).isFalse();
        assertThat(firstHears).hasSize(1);
        assertThat(secondHears).hasSize(1);
        assertThat(thirdHears).hasSize(1);
    }

    @Test
    @DisplayName("a pick on the first or the last radio of a group walks the group from its end")
    void aPickAtTheEndsOfAGroup() {
        ToolBar bar = new ToolBar(shell, SWT.NONE);
        ToolItem first = new ToolItem(bar, SWT.RADIO);
        ToolItem middle = new ToolItem(bar, SWT.RADIO);
        ToolItem last = new ToolItem(bar, SWT.RADIO);

        fromFlutter(last, "Selection/Selection");
        runQueued();
        assertThat(new boolean[] {first.getSelection(), middle.getSelection(), last.getSelection()})
                .containsExactly(false, false, true);

        fromFlutter(first, "Selection/Selection");
        runQueued();
        assertThat(new boolean[] {first.getSelection(), middle.getSelection(), last.getSelection()})
                .containsExactly(true, false, false);
    }

    @Test
    @DisplayName("a radio group ends at an item that is not a radio")
    void aRadioGroupEndsAtANonRadioItem() {
        ToolBar bar = new ToolBar(shell, SWT.NONE);
        ToolItem above = new ToolItem(bar, SWT.RADIO);
        new ToolItem(bar, SWT.SEPARATOR);
        ToolItem below = new ToolItem(bar, SWT.RADIO);
        above.setSelection(true);

        fromFlutter(below, "Selection/Selection");
        runQueued();

        assertThat(above.getSelection()).isTrue();
        assertThat(below.getSelection()).isTrue();

        fromFlutter(above, "Selection/Selection");
        runQueued();

        assertThat(above.getSelection()).isTrue();
        assertThat(below.getSelection()).isTrue();
    }

    @Test
    @DisplayName("a bar with NO_RADIO_GROUP leaves the other radios as they are")
    void noRadioGroupLeavesTheOthersAlone() {
        ToolBar bar = new ToolBar(shell, SWT.NO_RADIO_GROUP);
        ToolItem first = new ToolItem(bar, SWT.RADIO);
        ToolItem second = new ToolItem(bar, SWT.RADIO);
        first.setSelection(true);

        fromFlutter(second, "Selection/Selection");
        runQueued();

        assertThat(first.getSelection()).isTrue();
        assertThat(second.getSelection()).isFalse();
    }

    @Test
    @DisplayName("a default selection reaches the DefaultSelection listeners and changes nothing")
    void aDefaultSelectionReachesTheListeners() {
        ToolItem item = new ToolItem(new ToolBar(shell, SWT.NONE), SWT.CHECK);
        List<Event> defaults = record(item, SWT.DefaultSelection);

        fromFlutter(item, "Selection/DefaultSelection");
        runQueued();

        assertThat(defaults).hasSize(1);
        assertThat(item.getSelection()).isFalse();
    }

    @Test
    @DisplayName("opening the menu of a drop-down sends a Selection with detail ARROW")
    void openingTheDropDownMenuSendsAnArrowSelection() {
        ToolItem item = new ToolItem(new ToolBar(shell, SWT.NONE), SWT.DROP_DOWN);
        List<Event> events = record(item, SWT.Selection);

        fromFlutter(item, "Selection/OpenMenu");
        runQueued();

        assertThat(events).hasSize(1);
        assertThat(events.get(0).detail).isEqualTo(SWT.ARROW);
    }

    @Test
    @DisplayName("opening the menu with the item's bounds records them and asks for the menu below the item")
    void openingTheMenuWithTheItemsBounds() {
        ToolItem item = new ToolItem(new ToolBar(shell, SWT.NONE), SWT.DROP_DOWN);
        List<Event> events = record(item, SWT.Selection);
        Event fromClient = new Event();
        fromClient.x = 10;
        fromClient.y = 5;
        fromClient.width = 30;
        fromClient.height = 20;

        fromFlutter(item, "Selection/OpenMenu", fromClient);
        runQueued();

        assertThat(item.getBounds()).isEqualTo(new Rectangle(10, 5, 30, 20));
        assertThat(events).hasSize(1);
        assertThat(events.get(0).detail).isEqualTo(SWT.ARROW);
        assertThat(events.get(0).y).isEqualTo(25);

        Event onlyTall = new Event();
        onlyTall.height = 8;
        fromFlutter(item, "Selection/OpenMenu", onlyTall);
        runQueued();

        assertThat(events).hasSize(2);
        assertThat(events.get(1).y).isEqualTo(8);
    }

    @Test
    @DisplayName("a notice that arrives after the item was disposed is dropped")
    void aNoticeForADisposedItemIsDropped() {
        ToolItem item = new ToolItem(new ToolBar(shell, SWT.NONE), SWT.CHECK);
        List<Event> events = record(item, SWT.Selection);
        List<Event> defaults = record(item, SWT.DefaultSelection);

        fromFlutter(item, "Selection/Selection");
        fromFlutter(item, "Selection/DefaultSelection");
        fromFlutter(item, "Selection/OpenMenu");
        item.dispose();
        runQueued();

        assertThat(events).isEmpty();
        assertThat(defaults).isEmpty();
    }

    @Test
    @DisplayName("a pick whose activation disposes the item tells no Selection listener")
    void aPickWhoseActivationDisposesTheItemIsDropped() {
        Composite parent = new Composite(shell, SWT.NONE);
        ToolItem item = new ToolItem(new ToolBar(parent, SWT.NONE), SWT.PUSH);
        List<Event> events = record(item, SWT.Selection);
        parent.addListener(SWT.Activate, e -> item.dispose());

        fromFlutter(item, "Selection/Selection");
        runQueued();

        assertThat(item.isDisposed()).isTrue();
        assertThat(events).isEmpty();
    }
}
