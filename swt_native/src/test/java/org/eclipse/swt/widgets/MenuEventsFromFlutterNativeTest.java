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
 * What a pick, a hover, a help request and the opening and closing of a menu arriving from Flutter does to a
 * Menu and its items. Every one hops through {@code asyncExec}, so by the time it runs the widget may be gone.
 */
@Tag("native-unit")
class MenuEventsFromFlutterNativeTest {

    private RecordingBridge bridge;
    private Display display;
    private Menu menu;
    private final List<Runnable> queued = new ArrayList<>();

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = DartMocks.dartDisplay();
        doAnswer(invocation -> queued.add(invocation.getArgument(0))).when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = (DartDisplay) display.getImpl();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        menu = new Menu(DartMocks.dartShell(display), SWT.POP_UP);
    }

    @AfterEach
    void tearDown() {
        doNothing().when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = (DartDisplay) display.getImpl();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        FlutterBridge.set(null);
    }

    private void fromFlutter(Widget widget, String channel) {
        bridge.comm.fireContaining(widget.getClass().getSimpleName() + "/" + widget.hashCode() + "/" + channel, new Event());
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
    @DisplayName("a pick on a check item flips it and tells the Selection listeners")
    void aPickFlipsACheckItem() {
        MenuItem item = new MenuItem(menu, SWT.CHECK);
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
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        List<Event> events = record(item, SWT.Selection);

        fromFlutter(item, "Selection/Selection");
        runQueued();

        assertThat(item.getSelection()).isFalse();
        assertThat(events).hasSize(1);
    }

    @Test
    @DisplayName("a pick on a radio item selects it, deselects the selected one of its group and tells the picked one")
    void aPickSelectsOneRadioOfItsGroup() {
        MenuItem first = new MenuItem(menu, SWT.RADIO);
        MenuItem second = new MenuItem(menu, SWT.RADIO);
        MenuItem third = new MenuItem(menu, SWT.RADIO);
        first.setSelection(true);
        List<Event> secondHears = record(second, SWT.Selection);
        List<Event> thirdHears = record(third, SWT.Selection);

        fromFlutter(second, "Selection/Selection");
        runQueued();

        assertThat(first.getSelection()).isFalse();
        assertThat(second.getSelection()).isTrue();
        assertThat(third.getSelection()).isFalse();
        assertThat(secondHears).hasSize(1);
        assertThat(thirdHears).isEmpty();
    }

    @Test
    @DisplayName("a radio group ends at an item that is not a radio")
    void aRadioGroupEndsAtANonRadioItem() {
        MenuItem above = new MenuItem(menu, SWT.RADIO);
        new MenuItem(menu, SWT.SEPARATOR);
        MenuItem below = new MenuItem(menu, SWT.RADIO);
        above.setSelection(true);

        fromFlutter(below, "Selection/Selection");
        runQueued();

        assertThat(above.getSelection()).isTrue();
        assertThat(below.getSelection()).isTrue();
    }

    @Test
    @DisplayName("a hover, a help request and a default selection reach the item's listeners")
    void hoverHelpAndDefaultSelectionReachTheListeners() {
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        List<Event> arms = record(item, SWT.Arm);
        List<Event> helps = record(item, SWT.Help);
        List<Event> defaults = record(item, SWT.DefaultSelection);

        fromFlutter(item, "Arm/Arm");
        fromFlutter(item, "Help/Help");
        fromFlutter(item, "Selection/DefaultSelection");
        runQueued();

        assertThat(arms).hasSize(1);
        assertThat(helps).hasSize(1);
        assertThat(defaults).hasSize(1);
    }

    @Test
    @DisplayName("an event for an item that was disposed meanwhile is dropped")
    void anEventForADisposedItemIsDropped() {
        MenuItem item = new MenuItem(menu, SWT.CHECK);
        List<Event> events = new ArrayList<>();
        item.addListener(SWT.Selection, events::add);
        item.addListener(SWT.Arm, events::add);
        item.addListener(SWT.Help, events::add);
        item.addListener(SWT.DefaultSelection, events::add);

        fromFlutter(item, "Selection/Selection");
        fromFlutter(item, "Arm/Arm");
        fromFlutter(item, "Help/Help");
        fromFlutter(item, "Selection/DefaultSelection");
        item.dispose();
        runQueued();

        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("a help request reaches the menu's listeners")
    void aHelpRequestReachesTheMenu() {
        List<Event> helps = record(menu, SWT.Help);

        fromFlutter(menu, "Help/Help");
        runQueued();

        assertThat(helps).hasSize(1);
    }

    @Test
    @DisplayName("opening the menu tells the Show listeners, and a listener that fails does not stop the client's ack")
    void openingTheMenuTellsTheShowListeners() {
        List<Event> shows = record(menu, SWT.Show);
        menu.addListener(SWT.Show, e -> {
            throw new IllegalStateException("a listener that fails");
        });

        fromFlutter(menu, "Menu/Show");
        runQueued();

        assertThat(shows).hasSize(1);
    }

    @Test
    @DisplayName("the client hiding a shown popup makes it not visible and tells the Hide listeners")
    void theClientHidingAPopupHidesIt() {
        List<Event> hides = record(menu, SWT.Hide);
        menu.setVisible(true);
        assertThat(menu.isVisible()).isTrue();

        fromFlutter(menu, "Menu/Hide");
        runQueued();

        assertThat(menu.isVisible()).isFalse();
        assertThat(hides).hasSize(1);
    }

    @Test
    @DisplayName("a notice for a menu that was disposed meanwhile is dropped")
    void aNoticeForADisposedMenuIsDropped() {
        List<Event> events = new ArrayList<>();
        menu.addListener(SWT.Hide, events::add);
        menu.addListener(SWT.Show, events::add);
        menu.addListener(SWT.Help, events::add);

        fromFlutter(menu, "Menu/Hide");
        fromFlutter(menu, "Menu/Show");
        fromFlutter(menu, "Help/Help");
        menu.dispose();

        org.assertj.core.api.Assertions.assertThatCode(this::runQueued).doesNotThrowAnyException();
        assertThat(events).isEmpty();
    }
}
