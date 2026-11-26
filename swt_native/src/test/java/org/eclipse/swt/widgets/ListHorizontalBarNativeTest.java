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
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;

/** An H_SCROLL List's horizontal bar takes the range and offset Flutter scrolls the items sideways in. */
@Tag("native-unit")
class ListHorizontalBarNativeTest {

    private Display display;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = DartMocks.dartDisplay();
        DartDisplay impl = (DartDisplay) display.getImpl();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
    }

    @AfterEach
    void tearDown() {
        DartDisplay impl = (DartDisplay) display.getImpl();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        FlutterBridge.set(null);
    }

    private List list() {
        List list = new List(DartMocks.dartShell(display), SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
        list.setBounds(0, 0, 100, 100);
        list.setItems(new String[] {"short", "a much longer item than the list is wide"});
        return list;
    }

    /** What the client reports: the items {@code x} pixels sideways in a {@code viewport} wide, {@code itemsWidth} wide. */
    private static void scrolled(List list, int x, int viewport, int itemsWidth) {
        Event e = new Event();
        e.height = 20;
        e.x = x;
        e.width = viewport;
        e.end = itemsWidth;
        ListHelper.recordScrollOffset((DartList) list.getImpl(), e);
    }

    @Test
    @DisplayName("the bar's range is the items' width, its thumb the viewport and its selection the offset")
    void theBarTakesTheSidewaysScroll() {
        List list = list();

        scrolled(list, 40, 96, 300);

        ScrollBar bar = list.getHorizontalBar();
        assertThat(bar.getMinimum()).isZero();
        assertThat(bar.getMaximum()).isEqualTo(300);
        assertThat(bar.getThumb()).isEqualTo(96);
        assertThat(bar.getSelection()).isEqualTo(40);
    }

    @Test
    @DisplayName("items that fit fill the bar with its thumb")
    void itemsThatFitFillTheBar() {
        List list = list();

        scrolled(list, 0, 96, 96);

        ScrollBar bar = list.getHorizontalBar();
        assertThat(bar.getThumb()).isEqualTo(bar.getMaximum() - bar.getMinimum());
        assertThat(bar.getSelection()).isZero();
    }

    @Test
    @DisplayName("a sideways scroll is a Selection on the bar; a new range at the same offset is not")
    void aSidewaysScrollIsASelection() {
        List list = list();
        java.util.List<Integer> heard = new ArrayList<>();
        list.getHorizontalBar().addListener(SWT.Selection, e -> heard.add(list.getHorizontalBar().getSelection()));
        scrolled(list, 0, 96, 300);

        scrolled(list, 204, 96, 300);
        scrolled(list, 204, 120, 324);

        assertThat(heard).containsExactly(204);
    }

    @Test
    @DisplayName("a report before the items are measured sideways leaves the bar as it was, with no Selection")
    void aReportWithoutTheSidewaysScrollLeavesTheBar() {
        List list = list();
        scrolled(list, 40, 96, 300);
        java.util.List<Integer> heard = new ArrayList<>();
        list.getHorizontalBar().addListener(SWT.Selection, e -> heard.add(list.getHorizontalBar().getSelection()));

        scrolled(list, 0, 0, 0);

        ScrollBar bar = list.getHorizontalBar();
        assertThat(bar.getMaximum()).isEqualTo(300);
        assertThat(bar.getThumb()).isEqualTo(96);
        assertThat(bar.getSelection()).isEqualTo(40);
        assertThat(heard).isEmpty();
    }
}
