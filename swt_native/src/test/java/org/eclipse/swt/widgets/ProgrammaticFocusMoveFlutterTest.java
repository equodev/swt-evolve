package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Natively, {@link Control#setFocus()} on another control makes the previous focus holder lose
 * focus: it receives {@link SWT#FocusOut}, then the new one receives {@link SWT#FocusIn}. Apps rely
 * on that pair to react to a focus move they started themselves.
 *
 * <p>The shape that depends on it: a search field that, on Escape, clears itself and hands the
 * focus to a list, and a FocusOut listener that hides the field once it is empty. Without the
 * FocusOut the field stays on screen and nothing else can dismiss it.
 *
 * <pre>./gradlew :swt-evolve:swt_native:nativeTest</pre>
 */
@Tag("flutter-it")
class ProgrammaticFocusMoveFlutterTest {

    private Display display;
    private TestWebBridge web;
    private Shell shell;
    private Text filter;
    private Table list;
    private Text other;
    private final List<String> focusEvents = new ArrayList<>();

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    private void openFilterAboveList() {
        web = install();
        shell = new Shell(display);
        shell.setLayout(new FillLayout(SWT.VERTICAL));
        filter = new Text(shell, SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH);
        list = new Table(shell, SWT.NONE);
        other = new Text(shell, SWT.SINGLE);
        shell.setSize(400, 300);
        shell.open();

        filter.addListener(SWT.Traverse, e -> {
            if (e.detail != SWT.TRAVERSE_ESCAPE)
                return;
            filter.setText("");
            list.setFocus();
            e.doit = false;
        });
        filter.addListener(SWT.FocusOut, e -> {
            focusEvents.add("filter FocusOut");
            if (filter.getText().isEmpty())
                filter.setVisible(false);
        });
        filter.addListener(SWT.FocusIn, e -> focusEvents.add("filter FocusIn"));
        list.addListener(SWT.FocusIn, e -> focusEvents.add("list FocusIn"));
        list.addListener(SWT.FocusOut, e -> focusEvents.add("list FocusOut"));
        other.addListener(SWT.FocusIn, e -> focusEvents.add("other FocusIn"));

        filter.setFocus();
        filter.setText("fix");
        drain();
        focusEvents.clear();
    }

    private void drain() {
        while (display.readAndDispatch()) {
        }
    }

    private void pressEscapeInFilter() {
        Event key = new Event();
        key.keyCode = SWT.ESC;
        key.character = SWT.ESC;
        ControlHelper.routeKeyDown((DartControl) filter.getImpl(), key);
        drain();
    }

    @Test
    void setFocusOnAnotherControlSendsFocusOutThenFocusIn() {
        openFilterAboveList();
        assertThat(display.getFocusControl()).as("precondition: the filter holds the focus").isSameAs(filter);

        list.setFocus();
        drain();

        assertThat(display.getFocusControl()).isSameAs(list);
        assertThat(focusEvents).containsExactly("filter FocusOut", "list FocusIn");
    }

    @Test
    void escapeInAFilterThatFocusesTheListHidesTheFilter() {
        openFilterAboveList();

        pressEscapeInFilter();

        assertThat(filter.getText()).isEmpty();
        assertThat(display.getFocusControl()).isSameAs(list);
        assertThat(filter.getVisible()).as("the emptied filter hid itself on losing the focus").isFalse();
    }

    @Test
    void theClientReportingTheSameMoveLaterDeliversNothingTwice() {
        openFilterAboveList();
        list.setFocus();
        drain();
        focusEvents.clear();

        web.comm.fireContaining(channel(filter, "Focus/FocusOut"), new Event());
        web.comm.fireContaining(channel(list, "Focus/FocusIn"), new Event());
        drain();

        assertThat(focusEvents).isEmpty();
    }

    @Test
    void aClientMoveAfterwardsStillTellsTheNewHolderItLostTheFocus() {
        openFilterAboveList();
        list.setFocus();
        drain();
        focusEvents.clear();

        // The render side never saw the move above, so the next one it reports starts from the filter.
        web.comm.fireContaining(channel(filter, "Focus/FocusOut"), new Event());
        web.comm.fireContaining(channel(other, "Focus/FocusIn"), new Event());
        drain();

        assertThat(focusEvents).containsExactly("list FocusOut", "other FocusIn");
    }

    @Test
    void settingFocusOnTheHolderAgainSendsNothing() {
        openFilterAboveList();

        filter.setFocus();
        drain();

        assertThat(focusEvents).isEmpty();
    }

    // ---- harness ----------------------------------------------------------------------------------

    private static String channel(Widget widget, String suffix) {
        return "/" + widget.hashCode() + "/" + suffix;
    }

    /** See {@code PopupShellFocusHolderFlutterTest.install} — a Display whose bridge is the test bridge. */
    private TestWebBridge install() {
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        TestWebBridge bridge = new TestWebBridge(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

    /** A stub injected only so {@code Display.init()} skips creating a real surface bridge. */
    private static final class NoopBridge extends FlutterBridge {
        final RecordingComm comm = new RecordingComm();

        NoopBridge() {
            clientReady.complete(true);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        public void initFlutterView(Composite parent, DartControl control) {
        }

        @Override
        public void destroy(DartWidget control) {
        }
    }

    private static final class TestWebBridge extends WebDisplayBridge {
        final RecordingComm comm = new RecordingComm();

        TestWebBridge(DartDisplay display) {
            super(display);
            clientReady.complete(true);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        protected void start(DartDisplay display) {
            registerDisplayClientReady(display);
        }
    }
}
