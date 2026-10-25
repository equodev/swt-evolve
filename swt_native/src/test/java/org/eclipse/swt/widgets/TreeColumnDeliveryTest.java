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
 * A tree already delivered is updated with the properties that changed, so adding or removing a
 * column has to name {@code columns}. A viewer that builds its columns when its input arrives - the
 * Debug views do - otherwise shows the header with none of them.
 */
@ExtendWith(Mocks.class)
class TreeColumnDeliveryTest {

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
    @DisplayName("a column added after the tree was delivered is named in the next update")
    void addedColumnIsNamed() {
        Tree tree = new Tree(Mocks.shell(), SWT.NONE);
        sendWhole(tree);

        TreeColumn name = new TreeColumn(tree, SWT.LEFT, 0);
        name.setText("Name");
        tree.setHeaderVisible(true);

        assertThat(diffOf(tree)).contains("\"columns\"").contains("\"headerVisible\"");
    }

    @Test
    @DisplayName("a column disposed after the tree was delivered is named in the next update")
    void disposedColumnIsNamed() {
        Tree tree = new Tree(Mocks.shell(), SWT.NONE);
        new TreeColumn(tree, SWT.LEFT).setText("Name");
        TreeColumn value = new TreeColumn(tree, SWT.LEFT);
        value.setText("Value");
        sendWhole(tree);

        value.dispose();

        assertThat(diffOf(tree)).contains("\"columns\"");
    }

    @Test
    @DisplayName("the column that becomes the first when the first is disposed is left aligned, and the update says so")
    void columnThatBecomesTheFirstIsLeftAligned() {
        Tree tree = new Tree(Mocks.shell(), SWT.NONE);
        TreeColumn first = new TreeColumn(tree, SWT.LEFT);
        TreeColumn second = new TreeColumn(tree, SWT.LEFT);
        second.setAlignment(SWT.RIGHT);
        sendWhole(tree);

        first.dispose();

        assertThat(second.getAlignment()).isEqualTo(SWT.LEFT);
        assertThat(second.getStyle() & (SWT.LEFT | SWT.RIGHT | SWT.CENTER)).isEqualTo(SWT.LEFT);
        assertThat(diffOf(second)).contains("\"alignment\"");
        second.setAlignment(SWT.CENTER);
        assertThat(second.getAlignment()).as("the first column refuses a new alignment").isEqualTo(SWT.LEFT);
    }

    /** The update the tree is due, or empty when it has nothing to say. */
    private String diffOf(Tree tree) {
        return diffOf((DartWidget) tree.getImpl());
    }

    private String diffOf(TreeColumn column) {
        return diffOf((DartWidget) column.getImpl());
    }

    private String diffOf(DartWidget impl) {
        byte[] update = serializer.toDiff(impl);
        return update == null ? "" : new String(update, StandardCharsets.UTF_8);
    }

    private void sendWhole(Tree tree) {
        try {
            serializer.to(tree);
            Serializer.markDelivered();
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
    }
}
