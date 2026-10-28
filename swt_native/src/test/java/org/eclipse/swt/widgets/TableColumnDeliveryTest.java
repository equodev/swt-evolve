package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A table already delivered is updated with the properties that changed, so adding or removing a
 * column has to name {@code columns}. A viewer that builds its columns when its input arrives
 * otherwise shows the header with none of them.
 */
@ExtendWith(Mocks.class)
class TableColumnDeliveryTest {

    private static final int CLIENT = 1;

    private final Serializer serializer = new Serializer();

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

        assertThat(diffOf(table)).contains("\"columns\"").contains("\"headerVisible\"");
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

        assertThat(diffOf(table)).contains("\"columns\"");
    }

    /** The update the table is due, or empty when it has nothing to say. */
    private String diffOf(Table table) {
        byte[] update = serializer.toDiff((DartTable) table.getImpl());
        return update == null ? "" : new String(update, StandardCharsets.UTF_8);
    }

    private void sendWhole(Table table) {
        try {
            serializer.to(table);
            Serializer.markDelivered();
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
    }
}
