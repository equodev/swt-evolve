package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code Shell.setActive()} announces the activation before it moves the focus into the shell: a shell that its
 * Activate listener disposes takes no focus.
 *
 * <pre>./gradlew :swt-evolve:swt_native:test --tests '*ShellActivationFlutterTest'</pre>
 */
@Tag("flutter-it")
class ShellActivationFlutterTest {

    private Display display;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void aShellItsActivateListenerDisposesTakesNoFocus() {
        install(TestWebBridge::new);
        Shell main = new Shell(display);
        main.setSize(400, 300);
        new Text(main, SWT.SINGLE);
        main.open();
        Shell other = new Shell(main);
        Text field = new Text(other, SWT.SINGLE);
        other.setSize(200, 100);
        other.open();
        main.setActive();
        List<String> events = new ArrayList<>();
        other.addListener(SWT.Activate, e -> {
            events.add("Activate");
            other.dispose();
        });
        field.addListener(SWT.FocusIn, e -> events.add("FocusIn"));

        other.setActive();

        assertThat(other.isDisposed()).isTrue();
        assertThat(events).containsExactly("Activate");
    }

    // ---- harness ----------------------------------------------------------------------------------

    /** See {@code ControlFocusRequestFlutterTest.install} — a Display whose bridge is the test bridge. */
    private <B extends DisplayBridge> B install(Function<DartDisplay, B> factory) {
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        B bridge = factory.apply(dd);
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
            // Stands in for the client having connected: update() defers every pending send until then.
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
