package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A workbench resolves a drop by asking its windows in the order they were last activated, which on
 * a desktop is the order they are stacked in. A second top-level window drawn inside the main one is
 * always in front of it, so a drag over it has to make it the active window, or a drag started in
 * the main window is resolved against the main window everywhere it covers -- which, maximized, is
 * everywhere.
 *
 * <pre>./gradlew :swt-evolve:swt_native:test --tests '*DragOverInlineWindowActivatesItFlutterTest'</pre>
 */
@Tag("flutter-it")
class DragOverInlineWindowActivatesItFlutterTest {

    private Display display;
    private TestWebBridge web;
    private Shell main;
    private Shell torn;
    private final List<String> activations = new ArrayList<>();

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        web = new TestWebBridge(dd);
        dd.setBridge(web);
        web.start(dd);

        main = new Shell(display, SWT.SHELL_TRIM);
        main.setBounds(0, 0, 1600, 1000);
        new Text(main, SWT.SINGLE);
        main.open();

        torn = new Shell(display, SWT.SHELL_TRIM);
        torn.setBounds(200, 150, 600, 400);
        new Text(torn, SWT.SINGLE);
        torn.open();

        main.setActive();
        main.addListener(SWT.Activate, e -> activations.add("main"));
        torn.addListener(SWT.Activate, e -> activations.add("torn"));
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void aDragOverTheInlineWindowActivatesIt() {
        assertThat(display.getActiveShell()).as("the premise: the drag starts in the main window").isSameAs(main);

        web.activateInlineWindowAt(new Point(400, 300));

        assertThat(display.getActiveShell()).isSameAs(torn);
        assertThat(activations).containsExactly("torn");
    }

    @Test
    void aDragOverTheMainWindowAloneActivatesNothing() {
        web.activateInlineWindowAt(new Point(1200, 800));

        assertThat(display.getActiveShell()).isSameAs(main);
        assertThat(activations).isEmpty();
    }

    @Test
    void dragFeedbackDrawnOverTheWindowIsNotWhatGetsActivated() {
        // The workbench draws its drop feedback as an untrimmed ON_TOP shell right under the pointer.
        Shell feedback = new Shell(main, SWT.NO_TRIM | SWT.ON_TOP);
        feedback.setBounds(390, 290, 20, 20);
        feedback.setVisible(true);
        main.setActive();
        activations.clear();

        web.activateInlineWindowAt(new Point(400, 300));

        assertThat(display.getActiveShell()).isSameAs(torn);
        assertThat(activations).containsExactly("torn");
    }

    @Test
    void theWindowAlreadyActiveIsNotActivatedAgain() {
        web.activateInlineWindowAt(new Point(400, 300));
        web.activateInlineWindowAt(new Point(420, 310));

        assertThat(activations).containsExactly("torn");
    }

    // ---- harness ----------------------------------------------------------------------------------

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
