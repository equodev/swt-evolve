package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A programmatic {@link TableColumn#setWidth(int)} must notify {@code ControlListener}s, as all
 * three native backends do — Cocoa through {@code NSTableViewColumnDidResizeNotification}, Win32
 * through {@code HDN_ITEMCHANGED}, GTK straight from {@code setWidth}. Applications keep their own
 * column-width model in that listener; without the event the model silently drifts from the widths
 * on screen, and a later relayout that restores the model collapses the columns.
 *
 * <p>Only a width that actually changes notifies: re-setting the same width is a no-op natively.
 *
 * <pre>./gradlew :swt-evolve:swt_native:test --tests '*TableColumnSetWidthNotifies*'</pre>
 */
@Tag("flutter-it")
class TableColumnSetWidthNotifiesFlutterTest {

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

    private TableColumn column(Table table, String text, int width) {
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText(text);
        column.setWidth(width);
        return column;
    }

    @Test
    @DisplayName("setWidth() fires SWT.Resize with the new width already applied")
    void programmaticSetWidthNotifiesListener() {
        Table table = new Table(shell, SWT.NONE);
        table.setHeaderVisible(true);
        TableColumn column = column(table, "Name", 120);

        AtomicInteger fired = new AtomicInteger();
        AtomicInteger widthAtNotify = new AtomicInteger(-1);
        column.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                fired.incrementAndGet();
                widthAtNotify.set(column.getWidth());
            }
        });

        column.setWidth(210);

        assertThat(fired.get())
                .as("a programmatic width change notifies ControlListeners, as native SWT does")
                .isEqualTo(1);
        assertThat(widthAtNotify.get())
                .as("the width is already applied when the listener fires")
                .isEqualTo(210);
    }

    @Test
    @DisplayName("re-setting the same width notifies nobody")
    void unchangedWidthIsSilent() {
        Table table = new Table(shell, SWT.NONE);
        TableColumn column = column(table, "Name", 120);

        AtomicInteger fired = new AtomicInteger();
        column.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                fired.incrementAndGet();
            }
        });

        column.setWidth(120);

        assertThat(fired.get()).as("no width change, no event").isZero();
    }

    /**
     * The shape a real application uses: a listener on every column mirrors the live widths into the
     * application's own store, and a relayout later restores the columns from that store. When the
     * event is missing the store keeps the widths of the previous layout pass, so the restore
     * collapses the table's columns and never gives the width back.
     */
    @Test
    @DisplayName("a listener-backed width model stays in sync with a redistribution")
    void widthModelTracksRedistribution() {
        Table table = new Table(shell, SWT.NONE);
        table.setHeaderVisible(true);
        int[] percent = {10, 10, 20, 25, 20, 15};
        List<TableColumn> columns = new ArrayList<>();
        for (int i = 0; i < percent.length; i++) {
            columns.add(column(table, "c" + i, 400 * percent[i] / 100));
        }

        int[] persisted = new int[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            int index = i;
            columns.get(i).addControlListener(new ControlAdapter() {
                @Override
                public void controlResized(ControlEvent e) {
                    persisted[index] = columns.get(index).getWidth();
                }
            });
        }

        // the parent grew: the application redistributes the columns over the new width
        for (int i = 0; i < columns.size(); i++) {
            columns.get(i).setWidth(1200 * percent[i] / 100);
        }

        int[] live = new int[columns.size()];
        for (int i = 0; i < columns.size(); i++) live[i] = columns.get(i).getWidth();

        assertThat(persisted)
                .as("the width model the application restores from mirrors what is on screen")
                .containsExactly(live);
    }

    @Test
    @DisplayName("a Tree column notifies the same way")
    void treeColumnSetWidthNotifiesListener() {
        Tree tree = new Tree(shell, SWT.NONE);
        tree.setHeaderVisible(true);
        TreeColumn column = new TreeColumn(tree, SWT.NONE);
        column.setText("Name");
        column.setWidth(120);

        AtomicInteger fired = new AtomicInteger();
        AtomicInteger widthAtNotify = new AtomicInteger(-1);
        column.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                fired.incrementAndGet();
                widthAtNotify.set(column.getWidth());
            }
        });

        column.setWidth(210);

        assertThat(fired.get()).as("Tree columns notify like Table columns").isEqualTo(1);
        assertThat(widthAtNotify.get()).isEqualTo(210);
    }
}
