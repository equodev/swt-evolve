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

/** A V_SCROLL List's vertical bar takes the range and offset Flutter scrolls the rows in. */
@Tag("native-unit")
class ListVerticalBarNativeTest {

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
        List list = new List(DartMocks.dartShell(display), SWT.SINGLE | SWT.V_SCROLL);
        list.setBounds(0, 0, 100, 100);
        String[] items = new String[100];
        for (int i = 0; i < items.length; i++)
            items[i] = "item " + i;
        list.setItems(items);
        return list;
    }

    /**
     * What the client reports: the rows {@code y} pixels down in a viewport {@code viewport} high, the rows being
     * {@code rowsHeight} high, each {@code extent} high.
     */
    private static void scrolled(List list, int extent, int y, int viewport, int rowsHeight) {
        Event e = new Event();
        e.y = y;
        e.height = extent;
        e.detail = viewport;
        e.count = rowsHeight;
        ListHelper.recordScrollOffset((DartList) list.getImpl(), e);
    }

    @Test
    @DisplayName("the bar's range is the rows' height, its thumb the viewport and its selection the offset")
    void theBarTakesTheScroll() {
        List list = list();
        int pitch = list.getItemHeight();

        scrolled(list, pitch, 5 * pitch, 4 * pitch, 100 * pitch);

        ScrollBar bar = list.getVerticalBar();
        assertThat(bar.getMinimum()).isZero();
        assertThat(bar.getMaximum()).isEqualTo(100 * pitch);
        assertThat(bar.getThumb()).isEqualTo(4 * pitch);
        assertThat(bar.getSelection()).isEqualTo(5 * pitch);
    }

    @Test
    @DisplayName("rows Flutter lays out at another height are measured in this list's item height")
    void theRangeIsInTheItemHeight() {
        List list = list();
        int pitch = list.getItemHeight();
        int extent = 2 * pitch;

        scrolled(list, extent, 5 * extent, 4 * extent, 100 * extent);

        ScrollBar bar = list.getVerticalBar();
        assertThat(bar.getMaximum()).isEqualTo(100 * pitch);
        assertThat(bar.getThumb()).isEqualTo(4 * pitch);
        assertThat(bar.getSelection()).isEqualTo(5 * pitch);
    }

    @Test
    @DisplayName("rows that fit fill the bar with its thumb")
    void rowsThatFitFillTheBar() {
        List list = list();
        int pitch = list.getItemHeight();

        scrolled(list, pitch, 0, 4 * pitch, 3 * pitch);

        ScrollBar bar = list.getVerticalBar();
        assertThat(bar.getThumb()).isEqualTo(bar.getMaximum() - bar.getMinimum());
        assertThat(bar.getSelection()).isZero();
    }

    @Test
    @DisplayName("a scroll is a Selection on the bar; a new range at the same offset is not")
    void aScrollIsASelection() {
        List list = list();
        int pitch = list.getItemHeight();
        ScrollBar bar = list.getVerticalBar();
        java.util.List<Integer> heard = new ArrayList<>();
        bar.addListener(SWT.Selection, e -> heard.add(bar.getSelection()));
        scrolled(list, pitch, 0, 4 * pitch, 100 * pitch);

        scrolled(list, pitch, 7 * pitch, 4 * pitch, 100 * pitch);
        scrolled(list, pitch, 7 * pitch, 5 * pitch, 101 * pitch);

        assertThat(heard).containsExactly(7 * pitch);
    }

    @Test
    @DisplayName("setTopIndex moves the bar at once, with no Selection, and the client's scroll there adds none")
    void setTopIndexMovesTheBarSilently() {
        List list = list();
        int pitch = list.getItemHeight();
        ScrollBar bar = list.getVerticalBar();
        scrolled(list, pitch, 0, 4 * pitch, 100 * pitch);
        java.util.List<Integer> heard = new ArrayList<>();
        bar.addListener(SWT.Selection, e -> heard.add(bar.getSelection()));

        list.setTopIndex(50);

        assertThat(bar.getSelection()).isEqualTo(50 * pitch);
        scrolled(list, pitch, 50 * pitch, 4 * pitch, 100 * pitch);
        assertThat(heard).isEmpty();
    }
}
