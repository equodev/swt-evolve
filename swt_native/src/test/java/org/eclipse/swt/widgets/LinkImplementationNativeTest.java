package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.graphics.Cursor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.LinkedHashMap;
import java.util.Map;

/** What {@link DartLink} does when it is driven directly rather than through {@link Link}. */
@Tag("native-unit")
class LinkImplementationNativeTest {

    private Shell shell;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        shell = DartMocks.dartShell();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a Link takes the Tab key only while its text holds an anchor")
    void tabStopNeedsAnAnchor() {
        Composite group = new Composite(shell, SWT.NONE);
        Link link = new Link(group, SWT.NONE);
        Button button = new Button(group, SWT.PUSH);
        assertThat(group.getTabList()).containsExactly(button);

        link.setText("plain text");
        assertThat(group.getTabList()).containsExactly(button);

        link.setText("<a>go</a> and <a href=\"x\">more</a>");
        assertThat(group.getTabList()).containsExactly(link, button);

        link.setText("<a>never closed");
        assertThat(Arrays.asList(group.getTabList())).containsExactly(button);
    }

    @Test
    @DisplayName("setLinkForeground on a disposed Link answers ERROR_WIDGET_DISPOSED")
    void linkForegroundOnADisposedLink() {
        Link link = new Link(shell, SWT.NONE);
        link.dispose();
        assertThatThrownBy(() -> link.setLinkForeground(null))
                .isInstanceOfSatisfying(SWTException.class, e -> assertThat(e.code).isEqualTo(SWT.ERROR_WIDGET_DISPOSED));
    }

    @Test
    @DisplayName("Enter or Space picks the focused anchor, and Tab or Shift+Tab moves the focus between anchors")
    void keysPickAndMoveBetweenAnchors() {
        deliverEvents();
        Link link = new Link(shell, SWT.NONE);
        link.setText("<a href=\"one\">first</a> and <a href=\"two\">second</a>");
        DartLink impl = (DartLink) link.getImpl();
        List<String> picked = new ArrayList<>();
        link.addListener(SWT.Selection, e -> picked.add(e.text));
        impl.focusIndex = 0;

        assertThat(impl.sendKeyEvent(SWT.KeyDown, key(SWT.CR, 0))).isTrue();
        assertThat(impl.sendKeyEvent(SWT.KeyDown, key(SWT.KEYPAD_CR, 0))).isTrue();
        assertThat(impl.sendKeyEvent(SWT.KeyDown, key(' ', 0))).isTrue();
        assertThat(impl.sendKeyEvent(SWT.KeyDown, key('x', 0))).isTrue();
        assertThat(impl.sendKeyEvent(SWT.KeyDown, key(SWT.TAB, 0))).isFalse();
        assertThat(impl.focusIndex).isEqualTo(1);
        assertThat(impl.sendKeyEvent(SWT.KeyDown, key(SWT.TAB, 0))).as("Tab on the last anchor leaves the link").isTrue();
        assertThat(impl.sendKeyEvent(SWT.KeyDown, key(SWT.TAB, SWT.SHIFT))).isFalse();
        assertThat(impl.focusIndex).isZero();
        assertThat(impl.sendKeyEvent(SWT.KeyDown, key(SWT.TAB, SWT.SHIFT))).as("Shift+Tab on the first anchor leaves the link").isTrue();
        assertThat(impl.sendKeyEvent(SWT.KeyUp, key(SWT.CR, 0))).isTrue();
        assertThat(picked).containsExactly("one", "one", "one");

        impl.focusIndex = -1;
        assertThat(impl.sendKeyEvent(SWT.KeyDown, key(SWT.CR, 0))).isTrue();
        impl.focusIndex = 0;
        link.addListener(SWT.KeyDown, e -> e.doit = false);
        assertThat(impl.sendKeyEvent(SWT.KeyDown, key(SWT.CR, 0))).isFalse();
        assertThat(picked).containsExactly("one", "one", "one");
    }

    @Test
    @DisplayName("a pick from Flutter reaches the link; a DefaultSelection only an active one, and nothing a disposed one")
    void picksFromFlutter() {
        deliverEvents();
        RecordingBridge bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        Display display = shell.getDisplay();
        List<Runnable> queued = new ArrayList<>();
        doAnswer(inv -> queued.add(inv.getArgument(0))).when(display).asyncExec(any(Runnable.class));
        Link link = new Link(shell, SWT.NONE);
        link.setText("<a>go</a>");
        List<Integer> heard = new ArrayList<>();
        link.addListener(SWT.Selection, e -> heard.add(e.type));
        link.addListener(SWT.DefaultSelection, e -> heard.add(e.type));

        bridge.comm.fireContaining("/Selection/Selection", new Event());
        bridge.comm.fireContaining("/Selection/DefaultSelection", new Event());
        runAll(queued);
        assertThat(heard).containsExactly(SWT.Selection, SWT.DefaultSelection);

        Shell modal = DartMocks.dartShell();
        when(((DartShell) shell.getImpl()).getModalShell()).thenReturn(modal);
        bridge.comm.fireContaining("/Selection/DefaultSelection", new Event());
        runAll(queued);
        assertThat(heard).containsExactly(SWT.Selection, SWT.DefaultSelection);

        bridge.comm.fireContaining("/Selection/Selection", new Event());
        bridge.comm.fireContaining("/Selection/DefaultSelection", new Event());
        link.dispose();
        runAll(queued);
        assertThat(heard).containsExactly(SWT.Selection, SWT.DefaultSelection);
    }

    @Test
    @DisplayName("a link shows the cursor it is given, and asks its parent's otherwise")
    void cursorOfItsOwnOrNone() {
        Link link = new Link(shell, SWT.NONE);
        Cursor hand = mock(Cursor.class);

        assertThat(((DartLink) link.getImpl()).findCursor()).isNull();
        link.setCursor(hand);
        assertThat(((DartLink) link.getImpl()).findCursor()).isSameAs(hand);
    }

    @Test
    @DisplayName("malformed markup reads the same in parse and in the helper that sizes a link: what is not an anchor stays as written")
    void malformedMarkupReadsTheSameInBothParsers() {
        DartLink impl = (DartLink) new Link(shell, SWT.NONE).getImpl();
        Map<String, String> shown = new LinkedHashMap<>();
        shown.put("<a>x</b>y", "x</b>y");
        shown.put("<a>x</ab>y", "x</ab>y");
        shown.put("<a hx>z", "<a hx>z");
        shown.put("<a hrx>z", "<a hrx>z");
        shown.put("<a hrex>z", "<a hrex>z");
        shown.put("<a hrefx>z", "<a hrefx>z");
        shown.put("<a title x>z", "<a title x>z");
        shown.put("<a t=x>z", "<a t=x>z");

        shown.forEach((markup, text) -> {
            assertThat(LinkHelper.displayText(markup)).as(markup).isEqualTo(text);
            assertThat(impl.parse(markup)).as(markup).isEqualTo(text);
        });
    }

    private void deliverEvents() {
        DartDisplay displayImpl = (DartDisplay) shell.getDisplay().getImpl();
        doCallRealMethod().when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
    }

    private static void runAll(List<Runnable> queued) {
        List<Runnable> now = new ArrayList<>(queued);
        queued.clear();
        now.forEach(Runnable::run);
    }

    private static Event key(int keyCode, int stateMask) {
        Event event = new Event();
        event.keyCode = keyCode;
        event.stateMask = stateMask;
        return event;
    }
}
