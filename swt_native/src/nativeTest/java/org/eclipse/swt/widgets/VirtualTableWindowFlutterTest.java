package org.eclipse.swt.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** A {@code SWT.VIRTUAL} table must ask the application for the rows it shows, not for every row. */
@Tag("flutter-it")
class VirtualTableWindowFlutterTest {

    private static final int ROWS = 50_000;

    private Display display;
    private RecordingBridge bridge;

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
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) {
            display.dispose();
        }
        FlutterBridge.set(null);
    }

    @Test
    void openingALargeVirtualTable_populatesAWindow_notEveryRow() {
        AtomicInteger setData = new AtomicInteger();
        TableViewer viewer = openTestDataStyleViewer(setData);
        Table table = viewer.getTable();

        assertThat(populatedRows(table))
                .as("only the initial window may be populated, not all %d rows", ROWS)
                .isEqualTo(TableHelper.VIRTUAL_INITIAL_ROWS);
        // Two passes: setItemCount() seeds the window, JFace's virtual refresh clears and reseeds it.
        assertThat(setData.get())
                .as("SWT.SetData runs once per row of the window, not once per row of the table")
                .isLessThanOrEqualTo(2 * TableHelper.VIRTUAL_INITIAL_ROWS);

        assertThat(new VTable((DartTable) table.getImpl()).getItemCount())
                .as("Flutter needs the real row count to render the rows it has no data for yet")
                .isEqualTo(ROWS);
    }

    @Test
    void theRowCountIsSerialized_notJustReadableFromJava() {
        AtomicInteger setData = new AtomicInteger();
        Table table = openTestDataStyleViewer(setData).getTable();
        // A getter alone is not enough: read-only properties are left out of the JSON converter, and
        // Flutter then sees no count at all and renders only the rows it was sent.
        table.setData("dev.equo.swt.new", false);
        bridge.comm.sent.clear();
        ((DartTable) table.getImpl()).dirty();
        FlutterBridge.update();

        // Whichever channel carries the table -- its own, or an ancestor's payload -- the count has
        // to be in the JSON, or Flutter renders only the rows it was sent.
        assertThat(bridge.comm.sent)
                .as("the payload must carry the row count over the bridge")
                .anySatisfy(frame -> assertThat(frame.json).contains("\"itemCount\":" + ROWS));
    }

    @Test
    void scrollingAsksForMoreRows_andStopsAtTheRequestedWindow() {
        AtomicInteger setData = new AtomicInteger();
        TableViewer viewer = openTestDataStyleViewer(setData);
        Table table = viewer.getTable();

        requestRowsUpTo(400);

        assertThat(populatedRows(table))
                .as("the requested window is populated, and nothing beyond it")
                .isEqualTo(400);
        assertThat(((DartTableItem) table.getItem(399).getImpl()).getTexts())
                .as("a row inside the requested window carries its data")
                .startsWith("r399c0");
    }

    private void requestRowsUpTo(int endExclusive) {
        Event request = new Event();
        request.end = endExclusive;
        bridge.comm.fireContaining("SetData", request);
        while (display.readAndDispatch()) {
            // drain the asyncExec the request handler posts
        }
    }

    private static int populatedRows(Table table) {
        DartTable impl = (DartTable) table.getImpl();
        int populated = 0;
        for (TableItem item : impl._items()) {
            if (item != null && ((DartTableItem) item.getImpl()).cached) populated++;
        }
        return populated;
    }

    /** The shape a data-file editor builds: VIRTUAL viewer, plain array provider, per-column labels. */
    private TableViewer openTestDataStyleViewer(AtomicInteger setData) {
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());

        TableViewer viewer = new TableViewer(shell, SWT.VIRTUAL | SWT.FULL_SELECTION | SWT.BORDER);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().addListener(SWT.SetData, e -> setData.incrementAndGet());

        for (int c = 0; c < 5; c++) {
            final int column = c;
            TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.LEFT);
            viewerColumn.getColumn().setText("C" + c);
            viewerColumn.getColumn().setWidth(100);
            viewerColumn.setLabelProvider(new ColumnLabelProvider() {
                @Override
                public String getText(Object element) {
                    // JFace asks for a row before setInput() has an element for it.
                    return element instanceof String[] row ? row[column] : "";
                }
            });
        }
        viewer.setContentProvider(ArrayContentProvider.getInstance());

        List<String[]> rows = new ArrayList<>(ROWS);
        for (int i = 0; i < ROWS; i++) {
            rows.add(new String[] { "r" + i + "c0", "r" + i + "c1", "r" + i + "c2", "r" + i + "c3",
                    "r" + i + "c4" });
        }

        viewer.getTable().clearAll();
        viewer.setItemCount(ROWS);
        viewer.setInput(rows);
        return viewer;
    }
}
