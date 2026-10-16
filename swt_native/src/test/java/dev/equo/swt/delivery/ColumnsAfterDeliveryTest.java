package dev.equo.swt.delivery;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DartTable;
import org.eclipse.swt.widgets.DartTree;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
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
 * Columns created or disposed after their Tree or Table was first delivered.
 *
 * <p>A JFace viewer that reconfigures its columns at runtime disposes them all and creates new
 * ones on a widget the client already holds. The column list travels inside the parent's value,
 * so unless the parent reports it changed, the client keeps the list it had: no columns at all,
 * while every row carries a text per column and owner-drawn cells are placed by columns the
 * client never laid out.
 */
@ExtendWith(Mocks.class)
class ColumnsAfterDeliveryTest {

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
    @DisplayName("a TreeColumn created after the Tree was delivered is sent with the Tree")
    void treeColumnAddedAfterDelivery() throws java.io.IOException {
        Shell shell = Mocks.shell();
        Tree tree = new Tree(shell, SWT.NONE);
        DartTree impl = (DartTree) tree.getImpl();
        serializer.to(tree);
        Serializer.markDelivered();

        TreeColumn name = new TreeColumn(tree, SWT.NONE);
        TreeColumn owner = new TreeColumn(tree, SWT.NONE);

        assertThat(impl.getValue().changedKeys())
                .as("the column list changed, so the Tree has to say so")
                .contains("columns");
        assertThat(new String(serializer.toDiff(impl), StandardCharsets.UTF_8))
                .as("and the update carries both new columns")
                .contains(String.valueOf(name.hashCode()))
                .contains(String.valueOf(owner.hashCode()));
    }

    @Test
    @DisplayName("a TreeColumn disposed after the Tree was delivered leaves the Tree's list")
    void treeColumnDisposedAfterDelivery() throws java.io.IOException {
        Shell shell = Mocks.shell();
        Tree tree = new Tree(shell, SWT.NONE);
        DartTree impl = (DartTree) tree.getImpl();
        TreeColumn gone = new TreeColumn(tree, SWT.NONE);
        new TreeColumn(tree, SWT.NONE);
        serializer.to(tree);
        Serializer.markDelivered();

        gone.dispose();

        assertThat(impl.getValue().changedKeys()).contains("columns");
        assertThat(new String(serializer.toDiff(impl), StandardCharsets.UTF_8))
                .doesNotContain(String.valueOf(gone.hashCode()));
    }

    @Test
    @DisplayName("a TableColumn created after the Table was delivered is sent with the Table")
    void tableColumnAddedAfterDelivery() throws java.io.IOException {
        Shell shell = Mocks.shell();
        Table table = new Table(shell, SWT.NONE);
        DartTable impl = (DartTable) table.getImpl();
        serializer.to(table);
        Serializer.markDelivered();

        TableColumn column = new TableColumn(table, SWT.NONE);

        assertThat(impl.getValue().changedKeys()).contains("columns");
        assertThat(new String(serializer.toDiff(impl), StandardCharsets.UTF_8))
                .contains(String.valueOf(column.hashCode()));
    }

    @Test
    @DisplayName("a TableColumn disposed after the Table was delivered leaves the Table's list")
    void tableColumnDisposedAfterDelivery() throws java.io.IOException {
        Shell shell = Mocks.shell();
        Table table = new Table(shell, SWT.NONE);
        DartTable impl = (DartTable) table.getImpl();
        TableColumn gone = new TableColumn(table, SWT.NONE);
        new TableColumn(table, SWT.NONE);
        serializer.to(table);
        Serializer.markDelivered();

        gone.dispose();

        assertThat(impl.getValue().changedKeys()).contains("columns");
        assertThat(new String(serializer.toDiff(impl), StandardCharsets.UTF_8))
                .doesNotContain(String.valueOf(gone.hashCode()));
    }
}
