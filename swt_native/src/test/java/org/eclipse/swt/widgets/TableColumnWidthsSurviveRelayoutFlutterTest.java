package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The column-width pattern a real editor uses: one listener on the table's parent lays the columns
 * out as fixed percentages of the client width, unless the width it last laid out for is still the
 * current one — then it restores the widths from the application's own store instead. A listener on
 * each column is what fills that store, from the widths actually in effect.
 *
 * <p>Collapsing and re-expanding a section changes the parent's height, not its width, so the
 * restore branch is what runs. It only gives the columns back if the per-column listener has been
 * keeping the store in step — which is what a column resize notification is for.
 *
 * <pre>./gradlew :swt-evolve:swt_native:test --tests '*TableColumnWidthsSurviveRelayout*'</pre>
 */
@Tag("flutter-it")
class TableColumnWidthsSurviveRelayoutFlutterTest {

    private static final int[] PERCENT = {10, 10, 20, 25, 20, 15};
    private static final int WIDTH_KEY = -1;

    private RecordingBridge bridge;
    private Display display;
    private Shell shell;
    private Composite parent;
    private Table table;
    private TableColumn[] columns;
    private final Map<Integer, Integer> store = new HashMap<>();

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge(); // makes Display.init() skip the real WebFlutterServer
        FlutterBridge.set(bridge);
        display = new Display();
        shell = new Shell(display);

        parent = new Composite(shell, SWT.NONE);
        parent.setLayout(new FillLayout());
        table = new Table(parent, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        table.setHeaderVisible(true);
        columns = new TableColumn[PERCENT.length];
        for (int i = 0; i < PERCENT.length; i++) {
            columns[i] = new TableColumn(table, SWT.NONE);
            columns[i].setText("c" + i);
            int index = i;
            columns[i].addControlListener(new ControlAdapter() {
                @Override
                public void controlResized(ControlEvent e) {
                    store.put(index, columns[index].getWidth());
                }
            });
        }
        parent.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                int clientWidth = parent.getClientArea().width;
                if (store.getOrDefault(WIDTH_KEY, -1) == clientWidth) {
                    for (int i = 0; i < columns.length; i++) {
                        columns[i].setWidth(store.getOrDefault(i, 0));
                    }
                    return;
                }
                store.put(WIDTH_KEY, clientWidth);
                for (int i = 0; i < columns.length; i++) {
                    columns[i].setWidth(clientWidth * PERCENT[i] / 100);
                }
            }
        });
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    private int[] widths() {
        int[] widths = new int[columns.length];
        for (int i = 0; i < columns.length; i++) widths[i] = columns[i].getWidth();
        return widths;
    }

    @Test
    @DisplayName("collapsing and re-expanding the section leaves the column widths alone")
    void widthsSurviveACollapseAndExpand() {
        parent.setSize(1200, 400);
        int[] laidOut = widths();
        assertThat(laidOut).as("the first layout distributes the client width").containsExactly(
                120, 120, 240, 300, 240, 180);

        parent.setSize(1200, 160); // collapse: same width, shorter
        parent.setSize(1200, 400); // expand

        assertThat(widths())
                .as("the restore path gives back the widths that were on screen")
                .containsExactly(laidOut);
    }
}
