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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * What a scroll of the items arriving from Flutter does to a List, and how API scrolling tells Flutter where
 * to scroll. Every event from Flutter hops through {@code asyncExec}, so by the time it runs the list may be
 * gone.
 */
@Tag("native-unit")
class ListScrollFromFlutterNativeTest {

    /** The height Flutter lays an item out in; Java's item pitch may differ from it. */
    private static final int FLUTTER_ITEM_EXTENT = 24;

    private RecordingBridge bridge;
    private Display display;
    private final java.util.List<Runnable> queued = new ArrayList<>();

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

    private List list(int items) {
        List list = new List(DartMocks.dartShell(display), SWT.SINGLE | SWT.V_SCROLL);
        list.setBounds(0, 0, 200, 120);
        for (int i = 0; i < items; i++)
            list.add("item " + i);
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

    private static VList value(List list) {
        return ((DartList) list.getImpl()).getValue();
    }

    private static Event scrolledTo(int y) {
        Event event = new Event();
        event.y = y;
        event.height = FLUTTER_ITEM_EXTENT;
        return event;
    }

    @Test
    @DisplayName("a scroll the client reports moves the top index to the item it shows at the top")
    void aScrollMovesTheTopIndex() {
        List list = list(60);

        fromFlutter(list, "Scroll/Scroll", scrolledTo(7 * FLUTTER_ITEM_EXTENT));
        runQueued();
        assertThat(list.getTopIndex()).isEqualTo(7);

        fromFlutter(list, "Scroll/Scroll", scrolledTo(12 * FLUTTER_ITEM_EXTENT + FLUTTER_ITEM_EXTENT / 2));
        runQueued();
        assertThat(list.getTopIndex()).isEqualTo(12);
    }

    @Test
    @DisplayName("a scroll reported without the item height is read in the list's own pixels")
    void aScrollWithoutTheItemHeightIsInTheListsPixels() {
        List list = list(60);
        Event event = new Event();
        event.y = 10 * list.getItemHeight();

        fromFlutter(list, "Scroll/Scroll", event);
        runQueued();

        assertThat(list.getTopIndex()).isEqualTo(10);
    }

    @Test
    @DisplayName("the top index the client reports is the one the next description names, without asking it to scroll")
    void aClientScrollIsNotSentBack() {
        List list = list(60);
        value(list).clearDirty();

        fromFlutter(list, "Scroll/Scroll", scrolledTo(5 * FLUTTER_ITEM_EXTENT));
        runQueued();

        assertThat(value(list).getTopIndex()).isEqualTo(5);
        assertThat(value(list).changedKeys()).doesNotContain(VList.TOP_INDEX);
    }

    @Test
    @DisplayName("API scrolling starts from where the client scrolled to")
    void apiScrollingStartsFromTheClientScroll() {
        List list = list(60);
        fromFlutter(list, "Scroll/Scroll", scrolledTo(30 * FLUTTER_ITEM_EXTENT));
        runQueued();

        list.setSelection(31);
        assertThat(list.getTopIndex()).isEqualTo(30);

        list.setSelection(2);
        assertThat(list.getTopIndex()).isEqualTo(2);
    }

    @Test
    @DisplayName("setTopIndex and selecting ask the client to scroll to the item they bring into view")
    void apiScrollingTellsTheClient() {
        List list = list(60);
        value(list).clearDirty();

        list.setTopIndex(20);
        assertThat(value(list).changedKeys()).contains(VList.TOP_INDEX);
        assertThat(value(list).getTopIndex()).isEqualTo(20);

        value(list).clearDirty();
        list.setSelection(21);
        assertThat(value(list).changedKeys()).doesNotContain(VList.TOP_INDEX);

        list.setSelection(50);
        assertThat(value(list).changedKeys()).contains(VList.TOP_INDEX);
        assertThat(value(list).getTopIndex()).isEqualTo(list.getTopIndex()).isLessThanOrEqualTo(50).isGreaterThan(20);
    }

    @Test
    @DisplayName("a scroll the client reports replaces the top index asked for")
    void aClientScrollReplacesTheTopIndexAskedFor() {
        List list = list(2);
        list.setTopIndex(1);

        fromFlutter(list, "Scroll/Scroll", scrolledTo(0));
        runQueued();

        assertThat(list.getTopIndex()).isZero();
    }

    @Test
    @DisplayName("a scroll that arrives after the list was disposed is dropped")
    void aScrollForADisposedListIsDropped() {
        List list = list(5);
        fromFlutter(list, "Scroll/Scroll", scrolledTo(10));
        list.dispose();

        assertThatCode(this::runQueued).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("a pick from Flutter selects the items it carries and tells the listeners; a DefaultSelection reaches only an active list, and nothing a disposed one")
    void picksFromFlutter() {
        List list = list(5);
        java.util.List<Integer> heard = new ArrayList<>();
        list.addListener(SWT.Selection, e -> heard.add(e.type));
        list.addListener(SWT.DefaultSelection, e -> heard.add(e.type));
        Event pick = new Event();
        pick.segments = new int[] {2};
        Event doubleClick = new Event();
        doubleClick.segments = new int[] {3};

        fromFlutter(list, "Selection/Selection", pick);
        fromFlutter(list, "Selection/DefaultSelection", doubleClick);
        runQueued();
        assertThat(list.getSelectionIndices()).containsExactly(3);
        assertThat(heard).containsExactly(SWT.Selection, SWT.DefaultSelection);

        Shell modal = DartMocks.dartShell(display);
        when(((DartShell) list.getShell().getImpl()).getModalShell()).thenReturn(modal);
        fromFlutter(list, "Selection/DefaultSelection", doubleClick);
        runQueued();
        assertThat(heard).hasSize(2);

        fromFlutter(list, "Selection/Selection", pick);
        fromFlutter(list, "Selection/DefaultSelection", doubleClick);
        list.dispose();
        runQueued();
        assertThat(heard).hasSize(2);
    }
}
