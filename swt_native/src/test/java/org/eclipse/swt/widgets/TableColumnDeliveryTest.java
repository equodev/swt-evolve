package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.WireFrames;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A table already delivered is updated with the properties that changed, so adding or removing a
 * column has to name {@code columns}. A viewer that builds its columns when its input arrives
 * otherwise shows the header with none of them.
 */
@ExtendWith(Mocks.class)
class TableColumnDeliveryTest {

    private static final int CLIENT = 1;

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
        FlutterBridge.set(new RecordingBridge());
        Serializer.enterConnection(CLIENT);
    }

    @AfterEach
    void tearDown() {
        Serializer.exitConnection();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a column added after the table was delivered is named in the next update")
    void addedColumnIsNamed() {
        Table table = new Table(Mocks.shell(), SWT.NONE);
        sendWhole(table);

        TableColumn name = new TableColumn(table, SWT.LEFT, 0);
        name.setText("Name");
        table.setHeaderVisible(true);

        assertThat(diffOf(table)).containsKey("columns").containsKey("headerVisible");
    }

    @Test
    @DisplayName("a column disposed after the table was delivered is named in the next update")
    void disposedColumnIsNamed() {
        Table table = new Table(Mocks.shell(), SWT.NONE);
        new TableColumn(table, SWT.LEFT).setText("Name");
        TableColumn value = new TableColumn(table, SWT.LEFT);
        value.setText("Value");
        sendWhole(table);

        value.dispose();

        assertThat(diffOf(table)).containsKey("columns");
    }

    /** The update the table is due, or empty when it has nothing to say. */
    private Map<String, Object> diffOf(Table table) {
        return changeOf((DartWidget) table.getImpl());
    }

    /**
     * What changed in {@code impl} since the client last heard of it, as its record names it; empty
     * when nothing did. A widget with nothing changed is described whole on request, which says
     * nothing about what an update names, so only a changed one is asked for, and it has to come back
     * as a change: a whole record would hold every property, named or not.
     */
    private static Map<String, Object> changeOf(DartWidget impl) {
        if (!impl.getValue().anyDirty()) return Map.of();
        Map<String, Object> update = WireFrames.record(impl, CLIENT);
        if (update == null) return Map.of();
        if (!update.containsKey("_d")) throw new AssertionError("described whole, not as a change: " + update);
        return update;
    }

    private void sendWhole(Table table) {
        WireFrames.record((DartWidget) table.getImpl(), CLIENT);
    }
}
