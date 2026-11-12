package dev.equo.swt.delivery;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.WireTable;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A window opens as the IDE's does: the display's record names the new window, which describes the
 * whole tree, and then the flush that has every one of those widgets queued runs. Each is described
 * whole once per client.
 */
@ExtendWith(Mocks.class)
class DescribedOnceTest {

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
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a window the display names is not described whole again by the flush that has it queued")
    void openedWindowTravelsOnce() {
        Shell shell = Mocks.shell();
        Composite area = new Composite(shell, SWT.NONE);
        ToolBar toolBar = new ToolBar(area, SWT.NONE);
        for (int i = 0; i < 5; i++) new ToolItem(toolBar, SWT.PUSH).setText("Tool " + i);
        Tree tree = new Tree(area, SWT.NONE);
        for (int i = 0; i < 20; i++) {
            TreeItem item = new TreeItem(tree, SWT.NONE);
            item.setText("Node " + i);
            new TreeItem(item, SWT.NONE).setText("Leaf " + i);
        }
        Table table = new Table(area, SWT.NONE);
        new TableColumn(table, SWT.LEFT).setText("Name");
        new TableColumn(table, SWT.LEFT).setText("Value");
        for (int i = 0; i < 30; i++) new TableItem(table, SWT.NONE).setText(new String[] {"Row " + i, "Value " + i});
        new Label(area, SWT.NONE).setText("Status");

        FlutterBridge.dirty(new FlutterBridge.DirtyState() {
            @Override
            public boolean isStale() {
                return false;
            }

            @Override
            public void flush() {
                FlutterBridge.writeTape(bridge.comm, tape -> tape.describeHolder("Display", 1,
                        new int[] {WireTable.DISPLAY_SHELLS}, (w, key) -> {
                            w.refs(new Object[] {area});
                            return true;
                        }));
            }
        });
        FlutterBridge.flushDirtyStates();
        int described = bytes();
        assertThat(wholeCounts()).as("the display's record describes the window").containsKey("Table/" + table.hashCode());

        FlutterBridge.update();

        assertThat(bytes() - described).as("bytes the flush adds after the window was described").isZero();
        Map<String, Integer> twice = new TreeMap<>(wholeCounts());
        twice.values().removeIf(count -> count < 2);
        assertThat(twice).as("widgets described whole more than once").isEmpty();
    }

    @Test
    @DisplayName("a widget queued with nothing named after it was described is still described whole")
    void requeuedWithNothingNamedGoesWhole() {
        Composite area = new Composite(Mocks.shell(), SWT.NONE);
        Label label = new Label(area, SWT.NONE);
        label.setText("Status");
        FlutterBridge.writeTape(bridge.comm, tape -> tape.describe((DartWidget) area.getImpl()));
        FlutterBridge.update();
        bridge.comm.sent.clear();

        bridge.dirty((DartWidget) label.getImpl());
        FlutterBridge.update();

        assertThat(wholeCounts()).containsEntry("Label/" + label.hashCode(), 1);
    }

    /** How many times each widget was described whole, by channel. */
    private Map<String, Integer> wholeCounts() {
        Map<String, Integer> counts = new TreeMap<>();
        for (RecordingComm.Frame f : bridge.comm.sent) {
            if (f.wire == 0 || !(f.value instanceof Map<?, ?> state) || state.containsKey("_d") || !state.containsKey("swt")) continue;
            counts.merge(f.event, 1, Integer::sum);
        }
        return counts;
    }

    private int bytes() {
        return bridge.comm.sent.stream().mapToInt(f -> f.size).sum();
    }
}
