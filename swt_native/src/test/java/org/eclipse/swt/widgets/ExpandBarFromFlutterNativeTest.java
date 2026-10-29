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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** An Expand or Collapse from Flutter carries the index of its item; Java's item follows before the event is sent. */
@Tag("native-unit")
class ExpandBarFromFlutterNativeTest {

    private RecordingBridge bridge;
    private final List<Runnable> queued = new ArrayList<>();
    private Display display;
    private Shell shell;

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
        shell = DartMocks.dartShell(display);
    }

    @AfterEach
    void tearDown() {
        doNothing().when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = (DartDisplay) display.getImpl();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        FlutterBridge.set(null);
    }

    private ExpandBar barWithTwoItems() {
        ExpandBar bar = new ExpandBar(shell, SWT.NONE);
        new ExpandItem(bar, SWT.NONE).setText("a");
        new ExpandItem(bar, SWT.NONE).setText("b");
        return bar;
    }

    private static Event at(int index) {
        Event event = new Event();
        event.index = index;
        return event;
    }

    private static List<String> record(ExpandBar bar) {
        List<String> seen = new ArrayList<>();
        bar.addListener(SWT.Expand, e -> seen.add("expand " + ((ExpandItem) e.item).getText()));
        bar.addListener(SWT.Collapse, e -> seen.add("collapse " + ((ExpandItem) e.item).getText()));
        return seen;
    }

    @Test
    void an_expand_opens_the_item_of_the_index_and_says_so() {
        ExpandBar bar = barWithTwoItems();
        List<String> seen = record(bar);

        ExpandBarHelper.sendExpand((DartExpandBar) bar.getImpl(), at(1), true);

        assertThat(bar.getItem(1).getExpanded()).isTrue();
        assertThat(bar.getItem(0).getExpanded()).isFalse();
        assertThat(seen).containsExactly("expand b");
    }

    @Test
    void a_collapse_closes_it_again() {
        ExpandBar bar = barWithTwoItems();
        bar.getItem(0).setExpanded(true);
        List<String> seen = record(bar);

        ExpandBarHelper.sendExpand((DartExpandBar) bar.getImpl(), at(0), false);

        assertThat(bar.getItem(0).getExpanded()).isFalse();
        assertThat(seen).containsExactly("collapse a");
    }

    @Test
    void a_state_Java_already_holds_sends_nothing() {
        ExpandBar bar = barWithTwoItems();
        bar.getItem(0).setExpanded(true);
        List<String> seen = record(bar);

        ExpandBarHelper.sendExpand((DartExpandBar) bar.getImpl(), at(0), true);

        assertThat(seen).isEmpty();
    }

    @Test
    void an_index_that_is_no_item_is_ignored() {
        ExpandBar bar = barWithTwoItems();
        List<String> seen = record(bar);
        DartExpandBar impl = (DartExpandBar) bar.getImpl();

        ExpandBarHelper.sendExpand(impl, at(-1), true);
        ExpandBarHelper.sendExpand(impl, at(2), true);
        ExpandBarHelper.sendExpand(impl, at(99), true);
        ExpandBarHelper.sendExpand(impl, null, true);

        assertThat(seen).isEmpty();
        assertThat(bar.getItem(0).getExpanded()).isFalse();
        assertThat(bar.getItem(1).getExpanded()).isFalse();
    }

    @Test
    void a_bar_that_is_gone_ignores_it() {
        ExpandBar bar = barWithTwoItems();
        DartExpandBar impl = (DartExpandBar) bar.getImpl();
        bar.dispose();

        ExpandBarHelper.sendExpand(impl, at(0), true);
    }

    private void fromFlutter(ExpandBar bar, String channel, int index) {
        bridge.comm.fireContaining("ExpandBar/" + bar.hashCode() + "/" + channel, at(index));
        List<Runnable> now = new ArrayList<>(queued);
        queued.clear();
        now.forEach(Runnable::run);
    }

    @Test
    void the_expand_channel_opens_the_item_and_the_collapse_channel_closes_it() {
        ExpandBar bar = barWithTwoItems();
        List<String> seen = record(bar);

        fromFlutter(bar, "Expand/Expand", 0);
        assertThat(bar.getItem(0).getExpanded()).isTrue();
        fromFlutter(bar, "Expand/Collapse", 0);

        assertThat(bar.getItem(0).getExpanded()).isFalse();
        assertThat(seen).containsExactly("expand a", "collapse a");
    }

    @Test
    void a_notice_that_arrives_after_the_bar_is_gone_is_dropped() {
        ExpandBar bar = barWithTwoItems();
        List<String> seen = record(bar);
        bridge.comm.fireContaining("ExpandBar/" + bar.hashCode() + "/Expand/Expand", at(0));
        bridge.comm.fireContaining("ExpandBar/" + bar.hashCode() + "/Expand/Collapse", at(0));
        bar.dispose();

        List<Runnable> now = new ArrayList<>(queued);
        queued.clear();
        now.forEach(Runnable::run);

        assertThat(seen).isEmpty();
    }

    private static Event key(int keyCode) {
        Event event = new Event();
        event.keyCode = keyCode;
        return event;
    }

    @ParameterizedTest
    @ValueSource(ints = {' ', SWT.CR})
    void a_key_that_opens_the_focused_item_tells_the_client(int keyCode) {
        ExpandBar bar = barWithTwoItems();
        List<String> seen = record(bar);
        ExpandItem first = bar.getItem(0);
        ((DartWidget) first.getImpl()).getValue().clearDirty();

        bar.notifyListeners(SWT.KeyDown, key(keyCode));

        assertThat(seen).containsExactly("expand a");
        assertThat(first.getExpanded()).isTrue();
        assertThat(((DartWidget) first.getImpl()).getValue().changedKeys()).contains("expanded");
    }

    @Test
    void a_key_that_moves_the_focus_changes_nothing_the_client_shows() {
        ExpandBar bar = barWithTwoItems();
        ExpandItem first = bar.getItem(0);
        ((DartWidget) first.getImpl()).getValue().clearDirty();

        bar.notifyListeners(SWT.KeyDown, key(SWT.ARROW_DOWN));

        assertThat(first.getExpanded()).isFalse();
        assertThat(((DartWidget) first.getImpl()).getValue().changedKeys()).doesNotContain("expanded");
    }
}
