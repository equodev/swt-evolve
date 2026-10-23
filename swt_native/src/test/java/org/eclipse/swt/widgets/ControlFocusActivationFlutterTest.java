package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A focus change in the whole-tree backend, as SWT announces it: FocusOut on the control that had the focus,
 * FocusIn on the one that takes it, then the shell's active control moves with one Deactivate and one Activate.
 * Disabling the control that has the focus hands the focus to the next control an ancestor offers it to, as
 * hiding it does.
 */
@Tag("flutter-it")
class ControlFocusActivationFlutterTest {

    private Display display;
    private TestWebBridge web;
    private Shell shell;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        web = new TestWebBridge(dd);
        dd.setBridge(web);
        web.start(dd);
        shell = new Shell(display);
        shell.setSize(300, 200);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    private void drain() {
        while (display.readAndDispatch()) {
        }
    }

    private Text text(Composite parent) {
        Text text = new Text(parent, SWT.SINGLE);
        text.setSize(100, 20);
        return text;
    }

    private static List<String> record(Map<String, Control> controls) {
        List<String> log = new ArrayList<>();
        controls.forEach((name, control) -> {
            control.addListener(SWT.FocusIn, e -> log.add(name + " FocusIn"));
            control.addListener(SWT.FocusOut, e -> log.add(name + " FocusOut"));
            control.addListener(SWT.Activate, e -> log.add(name + " Activate " + e.detail));
            control.addListener(SWT.Deactivate, e -> log.add(name + " Deactivate"));
        });
        return log;
    }

    private Map<String, Control> named(Object... pairs) {
        Map<String, Control> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2)
            map.put((String) pairs[i], (Control) pairs[i + 1]);
        return map;
    }

    @Test
    void setFocusAnnouncesTheMoveOnce() {
        Text first = text(shell);
        Text second = text(shell);
        shell.open();
        assertThat(first.setFocus()).isTrue();
        drain();
        List<String> log = record(named("first", first, "second", second));

        assertThat(second.setFocus()).isTrue();
        drain();

        assertThat(log).containsExactly("first FocusOut", "second FocusIn", "first Deactivate", "second Activate 0");
    }

    /** A focus the client reports comes from a press, which activates the chain before the focus moves (Win32, GTK). */
    @Test
    void aFocusTheClientReportsIsActivatedOnce() {
        Text first = text(shell);
        Text second = text(shell);
        shell.open();
        assertThat(first.setFocus()).isTrue();
        drain();
        List<String> log = record(named("first", first, "second", second));

        web.comm.fireContaining("/" + first.hashCode() + "/Focus/FocusOut", new Event());
        web.comm.fireContaining("/" + second.hashCode() + "/Focus/FocusIn", new Event());
        drain();

        assertThat(log).containsExactly("first Deactivate", "second Activate 0", "first FocusOut", "second FocusIn");
    }

    /** A click on a control that takes no focus activates its chain with the click's detail, once. */
    @Test
    void aClickActivationCarriesItsDetailAndIsNotRepeated() {
        Composite part = new Composite(shell, SWT.NONE);
        Composite other = new Composite(shell, SWT.NONE);
        Text inOther = text(other);
        shell.open();
        assertThat(inOther.setFocus()).isTrue();
        drain();
        List<String> log = record(named("part", part, "other", other));

        ControlHelper.sendActivateToAncestors((DartControl) part.getImpl(), SWT.MouseDown);
        ControlHelper.sendActivateToAncestors((DartControl) part.getImpl(), SWT.MouseDown);

        assertThat(log).containsExactly("other Deactivate", "part Activate " + SWT.MouseDown);
    }

    /** Hiding the focus control hands the focus on with one FocusOut, one FocusIn, one Deactivate and one Activate. */
    @Test
    void hidingTheFocusControlAnnouncesTheMoveOnce() {
        Text first = text(shell);
        Text second = text(shell);
        shell.open();
        assertThat(first.setFocus()).isTrue();
        drain();
        List<String> log = record(named("first", first, "second", second));

        first.setVisible(false);
        drain();

        assertThat(log).containsExactly("first FocusOut", "second FocusIn", "first Deactivate", "second Activate 0");
    }

    @Test
    void disablingTheFocusControlHandsTheFocusOn() {
        Text first = text(shell);
        Text second = text(shell);
        shell.open();
        assertThat(first.setFocus()).isTrue();
        drain();

        first.setEnabled(false);
        drain();

        assertThat(first.isFocusControl()).isFalse();
        assertThat(display.getFocusControl()).isSameAs(second);
    }

    @Test
    void disablingAnAncestorOfTheFocusControlHandsTheFocusOn() {
        Composite group = new Composite(shell, SWT.NONE);
        group.setSize(200, 50);
        Text inside = text(group);
        Text outside = text(shell);
        shell.open();
        assertThat(inside.setFocus()).isTrue();
        drain();

        group.setEnabled(false);
        drain();

        assertThat(inside.isFocusControl()).isFalse();
        assertThat(display.getFocusControl()).isSameAs(outside);
    }

    @Test
    void disablingAnotherControlLeavesTheFocus() {
        Text first = text(shell);
        Text second = text(shell);
        shell.open();
        assertThat(first.setFocus()).isTrue();
        drain();

        second.setEnabled(false);
        drain();

        assertThat(display.getFocusControl()).isSameAs(first);
    }

    /** A control disabled while it is losing the focus does not move the focus again. */
    @Test
    void disablingTheControlThatIsLosingTheFocusMovesNothing() {
        Text first = text(shell);
        Text second = text(shell);
        Text third = text(shell);
        shell.open();
        assertThat(first.setFocus()).isTrue();
        drain();
        first.addListener(SWT.FocusOut, e -> first.setEnabled(false));

        assertThat(third.setFocus()).isTrue();
        drain();

        assertThat(display.getFocusControl()).isSameAs(third);
        assertThat(second.isFocusControl()).isFalse();
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
