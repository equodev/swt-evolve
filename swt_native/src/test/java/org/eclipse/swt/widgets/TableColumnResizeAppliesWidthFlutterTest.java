package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.junit.jupiter.api.*;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Dragging a column boundary in the Flutter-rendered header must resize the column in Java too.
 * Flutter reports the drag as an inbound {@code "Control"/"Resize"} event carrying the new width;
 * native SWT applies the resize itself and only then notifies, so a {@code ControlListener} — and
 * every JFace column layout built on one — reads the new width from {@code getWidth()}.
 *
 * <p>Notifying without applying is not merely a stale getter: the next Java-side push re-sends the
 * column's unchanged width, and the render side snaps the column back to where the drag started.
 *
 * <pre>./gradlew :swt-evolve:swt_native:nativeTest --tests '*TableColumnResizeAppliesWidth*'</pre>
 */
@Tag("flutter-it")
class TableColumnResizeAppliesWidthFlutterTest {

    private static final int INITIAL_WIDTH = 120;
    private static final int DRAGGED_WIDTH = 210;

    private RecordingBridge bridge;
    private Display display;
    private Shell shell;

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge(); // makes Display.init() skip the real WebFlutterServer
        FlutterBridge.set(bridge);
        display = new Display();
        shell = new Shell(display);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    private void reportResize(TableColumn column, int width) {
        Event event = new Event();
        event.width = width;
        bridge.comm.fireContaining(
                "TableColumn/" + column.hashCode() + "/Control/Resize", event);
        while (display.readAndDispatch()) {
            /* run the queued resize */
        }
    }

    @Test
    @DisplayName("a Flutter column drag updates getWidth() and fires SWT.Resize")
    void columnDragFromFlutterAppliesWidth() {
        Table table = new Table(shell, SWT.NONE);
        table.setHeaderVisible(true);
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText("Name");
        column.setWidth(INITIAL_WIDTH);

        AtomicInteger fired = new AtomicInteger();
        AtomicInteger widthAtNotify = new AtomicInteger(-1);
        column.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                fired.incrementAndGet();
                widthAtNotify.set(column.getWidth());
            }
        });

        reportResize(column, DRAGGED_WIDTH);

        assertThat(column.getWidth())
                .as("Java-side width tracks the drag, so the next push cannot snap the column back")
                .isEqualTo(DRAGGED_WIDTH);
        assertThat(fired.get())
                .as("a user column resize from Flutter fires SWT.Resize to app listeners")
                .isEqualTo(1);
        assertThat(widthAtNotify.get())
                .as("the width is already applied when the listener fires, as in native SWT")
                .isEqualTo(DRAGGED_WIDTH);
    }
}
